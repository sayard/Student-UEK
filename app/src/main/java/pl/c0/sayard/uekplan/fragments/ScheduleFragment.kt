package pl.c0.sayard.uekplan.fragments

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.data.ScheduleContract
import pl.c0.sayard.uekplan.data.ScheduleDbHelper
import pl.c0.sayard.uekplan.parsers.ScheduleParser


class ScheduleFragment : Fragment() {

    private class ScheduleGroup(var name: String, var url: String)
    private class SchedulePE(var name: String, var day: Int, var startHour: String, var endHour: String)

    companion object {
        fun newInstance(): ScheduleFragment{
            return ScheduleFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = ScheduleDbHelper(activity)
        val db = dbHelper.readableDatabase
        val cursor = db.query(
                ScheduleContract.LessonEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                ScheduleContract.LessonEntry.START_DATE + " ASC"
        )
        if(cursor.count == 0){
            val group = getGroup(db)
            val languageGroups = getLanguageGroups(db)
            val urls = mutableListOf(group.url)
            languageGroups.mapTo(urls) { it.url }
            ScheduleParser(context, activity).execute(urls)
        }else{
            val pe = getPe(db)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_schedule, container, false)
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

}
