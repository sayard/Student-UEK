package pl.c0.sayard.uekplan.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar

import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.data.ScheduleItem
import pl.c0.sayard.uekplan.Utils
import pl.c0.sayard.uekplan.Utils.Companion.getLanguageGroups
import pl.c0.sayard.uekplan.adapters.ScheduleAdapter
import pl.c0.sayard.uekplan.db.ScheduleContract
import pl.c0.sayard.uekplan.db.ScheduleDbHelper
import pl.c0.sayard.uekplan.parsers.ScheduleParser


class ScheduleFragment : Fragment() {

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
            val group = Utils.getGroup(db)
            val languageGroups = getLanguageGroups(db)
            val urls = mutableListOf(group.url)
            languageGroups.mapTo(urls) { it.url }
            val progressBar = view.findViewById<ProgressBar>(R.id.schedule_progress_bar)
            ScheduleParser(context, activity, progressBar).execute(urls)
        }else{
            val scheduleList = Utils.getScheduleList(cursor, db)
            val adapter = getAdapter(scheduleList)
            val listView = view.findViewById<ListView>(R.id.schedule_list_view)
            listView.adapter = adapter
        }
        cursor.close()

        return view
    }

    private fun getAdapter(scheduleList: List<ScheduleItem>): ScheduleAdapter{
        return ScheduleAdapter(context, scheduleList)
    }

}
