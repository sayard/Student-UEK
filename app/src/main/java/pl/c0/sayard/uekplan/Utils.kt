package pl.c0.sayard.uekplan

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.widget.TextView
import pl.c0.sayard.uekplan.data.Group
import pl.c0.sayard.uekplan.data.ScheduleGroup
import pl.c0.sayard.uekplan.data.ScheduleItem
import pl.c0.sayard.uekplan.data.SchedulePE
import pl.c0.sayard.uekplan.db.ScheduleContract
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Karol on 1/1/2018.
 */
class Utils {
    companion object {
        val FIRST_RUN_SHARED_PREFS_KEY = "firstRun"
        val AUTOMATIC_SCHEDULE_REFRESH_PREFS_KEY = "automaticScheduleRefresh"

        fun getGroupURL(group: Group): String{
            return "http://planzajec.uek.krakow.pl/index.php?xml&typ=G&id=" +
                    group.id.toString() +
                    "&okres=1"
        }

        fun getTeacherURL(group: Group?): String {
            return "http://planzajec.uek.krakow.pl/index.php?xml&typ=N&id=" +
                    group?.id.toString() +
                    "&okres=1"
        }

        fun getScheduleList(cursor: Cursor, db: SQLiteDatabase): MutableList<ScheduleItem> {
            val scheduleList = mutableListOf<ScheduleItem>()
            cursor.moveToFirst()
            if(cursor != null && cursor.count >0){
                do{
                    val dateStr = cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.DATE))
                    val comments = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleContract.LessonEntry.COMMENTS))
                    var isCustom = false
                    if(cursor.getInt(cursor.getColumnIndex(ScheduleContract.LessonEntry.IS_CUSTOM)) == 1){
                        isCustom = true
                    }
                    val scheduleItem = ScheduleItem(
                            cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.SUBJECT)),
                            cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.TYPE)),
                            cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.TEACHER)),
                            cursor.getInt(cursor.getColumnIndex(ScheduleContract.LessonEntry.TEACHER_ID)),
                            cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.CLASSROOM)),
                            comments,
                            dateStr,
                            cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.START_DATE)),
                            cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.END_DATE)),
                            isCustom = isCustom,
                            customId = cursor.getInt(cursor.getColumnIndex(ScheduleContract.LessonEntry.CUSTOM_ID))
                    )
                    scheduleList.add(scheduleItem)
                }while(cursor.moveToNext())
            }else{
                return mutableListOf<ScheduleItem>()
            }
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
                scheduleList.sortWith(Comparator { p0, p1 -> p0?.startDate!!.compareTo(p1?.startDate) })
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
                val lessonNoteCursor = db.query(
                        ScheduleContract.LessonNoteEntry.TABLE_NAME,
                        arrayOf(ScheduleContract.LessonNoteEntry._ID, ScheduleContract.LessonNoteEntry.CONTENT),
                        "${ScheduleContract.LessonNoteEntry.LESSON_SUBJECT} = ? AND " +
                                "${ScheduleContract.LessonNoteEntry.LESSON_TYPE} = ? AND " +
                                "${ScheduleContract.LessonNoteEntry.LESSON_TEACHER} = ? AND " +
                                "${ScheduleContract.LessonNoteEntry.LESSON_TEACHER_ID} = ? AND " +
                                "${ScheduleContract.LessonNoteEntry.LESSON_CLASSROOM} = ? AND " +
                                "${ScheduleContract.LessonNoteEntry.LESSON_DATE} = ? AND " +
                                "${ScheduleContract.LessonNoteEntry.LESSON_START_DATE} = ? AND " +
                                "${ScheduleContract.LessonNoteEntry.LESSON_END_DATE} = ? ",
                        arrayOf(scheduleItem.subject,
                                scheduleItem.type,
                                scheduleItem.teacher,
                                "${scheduleItem.teacherId}",
                                scheduleItem.classroom,
                                scheduleItem.dateStr,
                                scheduleItem.startDateStr,
                                scheduleItem.endDateStr
                        ),
                        null,
                        null,
                        ScheduleContract.LessonNoteEntry._ID
                )
                if(lessonNoteCursor != null && lessonNoteCursor.count > 0){
                    lessonNoteCursor.moveToFirst()
                    scheduleItem.noteId = lessonNoteCursor.getInt(lessonNoteCursor.getColumnIndex(ScheduleContract.LessonNoteEntry._ID))
                    scheduleItem.noteContent = lessonNoteCursor.getString(lessonNoteCursor.getColumnIndex(ScheduleContract.LessonNoteEntry.CONTENT))
                }
                lessonNoteCursor.close()
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

        fun getTime(hourTv: TextView): Calendar{
            val startHour = hourTv.text.substring(0, 2).toInt()
            val startMinute = hourTv.text.substring(3).toInt()
            val startTime = Calendar.getInstance()
            startTime.set(Calendar.HOUR_OF_DAY, startHour)
            startTime.set(Calendar.MINUTE, startMinute)
            return startTime
        }

        fun getTitleBasedOnPosition(position: Int): Int{
            return when(position){
                0 -> R.string.search
                1 -> R.string.schedule
                2 -> R.string.notes
                3 -> R.string.settings
                else -> R.string.app_name
            }
        }
    }
}
