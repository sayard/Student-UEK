package pl.c0.sayard.uekplan

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.widget.TextView
import pl.c0.sayard.uekplan.data.Group
import pl.c0.sayard.uekplan.data.ScheduleGroup
import pl.c0.sayard.uekplan.data.ScheduleItem
import pl.c0.sayard.uekplan.data.SchedulePE
import pl.c0.sayard.uekplan.db.ScheduleContract
import pl.c0.sayard.uekplan.receivers.BootReceiver
import pl.c0.sayard.uekplan.receivers.ScheduleRefreshReceiver
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Karol on 1/1/2018.
 */
class Utils {
    companion object {
        val SCHEDULE_REFRESH_TASK_REQUEST_CODE = 101

        val FIRST_RUN_SHARED_PREFS_KEY = "firstRun"
        val AUTOMATIC_SCHEDULE_REFRESH_PREFS_KEY = "automaticScheduleRefresh"

        fun getGroupURL(group: Group): String{
            return "http://planzajec.uek.krakow.pl/index.php?xml&typ=G&id=" +
                    group.id.toString() +
                    "&okres=1"
        }

        fun getScheduleList(cursor: Cursor, db: SQLiteDatabase): MutableList<ScheduleItem> {
            val scheduleList = mutableListOf<ScheduleItem>()
            cursor.moveToFirst()
            do{
                val dateStr = cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.DATE))
                val comments = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleContract.LessonEntry.COMMENTS))
                val scheduleItem = ScheduleItem(
                        cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.SUBJECT)),
                        cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.TYPE)),
                        cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.TEACHER)),
                        cursor.getInt(cursor.getColumnIndex(ScheduleContract.LessonEntry.TEACHER_ID)),
                        cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.CLASSROOM)),
                        comments,
                        dateStr,
                        cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.START_DATE)),
                        cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.END_DATE))
                )
                scheduleList.add(scheduleItem)
            }while(cursor.moveToNext())
            val pe = getPe(db)
            if(pe != null){
                val days = HashSet<String>(scheduleList.map { it.dateStr })
                val peDays = mutableListOf<String>()
                val shortDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("pl", "PL"))
                for(day in days){
                    val date = shortDateFormat.parse(day)
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    if(calendar.get(Calendar.DAY_OF_WEEK) == pe.day+2){
                        peDays.add(day)
                    }
                }
                peDays.mapTo(scheduleList) {
                    ScheduleItem(
                            pe.name,
                            "P.E.",
                            "",
                            0,
                            "",
                            "",
                            it,
                            "$it ${pe.startHour}",
                            "$it ${pe.endHour}"
                    )
                }
                Collections.sort(scheduleList) { p0, p1 -> p0?.startDate!!.compareTo(p1?.startDate) }
            }
            for(i in 0 until scheduleList.size){
                val scheduleItem = scheduleList[i]
                if(i==0){
                    scheduleItem.isFirstOnTheDay = true
                }else{
                    val previousScheduleItem = scheduleList[i-1]
                    if(scheduleItem.dateStr != previousScheduleItem.dateStr){
                        scheduleItem.isFirstOnTheDay = true
                    }
                }
            }
            return scheduleList
        }

        private fun getPe(db: SQLiteDatabase): SchedulePE?{
            val cursor = db.query(ScheduleContract.PeEntry.TABLE_NAME, null, null, null, null, null, null)
            if(cursor.count > 0){
                cursor.moveToLast()
                val peName = cursor.getString(cursor.getColumnIndex(ScheduleContract.PeEntry.PE_NAME))
                val peDay = cursor.getInt(cursor.getColumnIndex(ScheduleContract.PeEntry.PE_DAY))
                val peStartHour = cursor.getString(cursor.getColumnIndex(ScheduleContract.PeEntry.PE_START_HOUR))
                val peEndHour = cursor.getString(cursor.getColumnIndex(ScheduleContract.PeEntry.PE_END_HOUR))
                cursor.close()
                return SchedulePE(peName, peDay, peStartHour, peEndHour)
            }
            cursor.close()
            return null
        }

        fun getGroups(db: SQLiteDatabase): MutableList<ScheduleGroup> {
            val cursor = db.query(ScheduleContract.GroupEntry.TABLE_NAME, null, null, null, null, null, null)
            val groups = mutableListOf<ScheduleGroup>()
            while(cursor.moveToNext()){
                val groupName = cursor.getString(cursor.getColumnIndex(ScheduleContract.GroupEntry.GROUP_NAME))
                val groupURL = cursor.getString(cursor.getColumnIndex(ScheduleContract.GroupEntry.GROUP_URL))
                groups.add(ScheduleGroup(groupName, groupURL))
            }
            cursor.close()
            return groups
        }

        fun getLanguageGroups(db: SQLiteDatabase): MutableList<ScheduleGroup> {
            val cursor = db.query(ScheduleContract.LanguageGroupsEntry.TABLE_NAME, null, null, null, null, null, null)
            val languageGroups = mutableListOf<ScheduleGroup>()
            while(cursor.moveToNext()){
                val languageGroupName = cursor.getString(cursor.getColumnIndex(ScheduleContract.LanguageGroupsEntry.LANGUAGE_GROUP_NAME))
                val languageGroupURL = cursor.getString(cursor.getColumnIndex(ScheduleContract.LanguageGroupsEntry.LANGUAGE_GROUP_URL))
                languageGroups.add(ScheduleGroup(languageGroupName, languageGroupURL))
            }
            cursor.close()
            return languageGroups
        }

        fun startScheduleRefreshTask(context: Context){
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ScheduleRefreshReceiver::class.java)
            val alarmIntent = PendingIntent.getBroadcast(context, SCHEDULE_REFRESH_TASK_REQUEST_CODE, intent, 0)
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 1)
            calendar.set(Calendar.MINUTE, 0)
            alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    alarmIntent
            )
            enableBootReceiver(context)
        }

        fun getScheduleCursor(db: SQLiteDatabase): Cursor{
            return db.query(
                    ScheduleContract.LessonEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    ScheduleContract.LessonEntry.START_DATE
            )
        }

        private fun enableBootReceiver(context: Context){
            val receiver = ComponentName(context, BootReceiver::class.java)
            val packageManager = context.packageManager

            packageManager.setComponentEnabledSetting(
                    receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            )
        }

        fun getTime(hourTv: TextView): Calendar{
            val startHour = hourTv.text.substring(0, 2).toInt()
            val startMinute = hourTv.text.substring(3).toInt()
            val startTime = Calendar.getInstance()
            startTime.set(Calendar.HOUR_OF_DAY, startHour)
            startTime.set(Calendar.MINUTE, startMinute)
            return startTime
        }
    }
}
