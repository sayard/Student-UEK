package pl.c0.sayard.uekplan.fragments

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar

import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.ScheduleItem
import pl.c0.sayard.uekplan.adapters.ScheduleAdapter
import pl.c0.sayard.uekplan.data.ScheduleContract
import pl.c0.sayard.uekplan.data.ScheduleDbHelper
import pl.c0.sayard.uekplan.parsers.ScheduleParser


class ScheduleFragment : Fragment() {

    private class ScheduleGroup(var name: String, var url: String)
    class SchedulePE(var name: String, var day: Int, var startHour: String, var endHour: String)

    companion object {
        fun newInstance(): ScheduleFragment{
            return ScheduleFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_schedule, container, false)

        val dbHelper = ScheduleDbHelper(activity)
        val db = dbHelper.readableDatabase
        val cursor = db.query(
                ScheduleContract.LessonEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                ScheduleContract.LessonEntry.START_DATE
        )
        if(cursor.count == 0){
            val group = getGroup(db)
            val languageGroups = getLanguageGroups(db)
            val urls = mutableListOf(group.url)
            languageGroups.mapTo(urls) { it.url }
            val progressBar = view.findViewById<ProgressBar>(R.id.schedule_progress_bar)
            ScheduleParser(context, activity, progressBar).execute(urls)
        }else{
            val pe = getPe(db)
            val scheduleList = mutableListOf<ScheduleItem>()
            cursor.moveToFirst()
            do{
                val dateStr = cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.DATE))
                var isFirstOnTheDay = true
                if(cursor.position != 0){
                    cursor.moveToPrevious()
                    if(dateStr == cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.DATE))){
                        isFirstOnTheDay = false
                    }
                    cursor.moveToNext()
                }
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
                        cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.END_DATE)),
                        isFirstOnTheDay
                )
                scheduleList.add(scheduleItem)
            }while(cursor.moveToNext())
            val adapter = getAdapter(scheduleList, pe)

            val listView = view.findViewById<ListView>(R.id.schedule_list_view)
            listView.adapter = adapter
        }
        cursor.close()

        return view
    }

    private fun getGroup(db: SQLiteDatabase): ScheduleGroup{
        val cursor = db.query(ScheduleContract.GroupEntry.TABLE_NAME, null, null, null, null, null, null)
        cursor.moveToLast()
        val groupName = cursor.getString(cursor.getColumnIndex(ScheduleContract.GroupEntry.GROUP_NAME))
        val groupURL = cursor.getString(cursor.getColumnIndex(ScheduleContract.GroupEntry.GROUP_URL))
        cursor.close()
        return ScheduleGroup(groupName, groupURL)
    }

    private fun getLanguageGroups(db: SQLiteDatabase): MutableList<ScheduleGroup> {
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

    private fun getAdapter(scheduleList: List<ScheduleItem>, pe: SchedulePE?): ScheduleAdapter{
        return ScheduleAdapter(context, scheduleList, pe!!)
    }

}
