package pl.c0.sayard.uekplan.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast

import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.data.ScheduleItem
import pl.c0.sayard.uekplan.Utils
import pl.c0.sayard.uekplan.Utils.Companion.getLanguageGroups
import pl.c0.sayard.uekplan.Utils.Companion.getScheduleCursor
import pl.c0.sayard.uekplan.adapters.ScheduleAdapter
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
        val cursor = getScheduleCursor(db)
        val progressBar = view.findViewById<ProgressBar>(R.id.schedule_progress_bar)
        val errorMessage = view.findViewById<TextView>(R.id.schedule_error_message)
        val urls = mutableListOf<String>()
        val groups = Utils.getGroups(db)
        groups.mapTo(urls){it.url}
        val languageGroups = getLanguageGroups(db)
        languageGroups.mapTo(urls) { it.url }
        if(cursor.count == 0){
            ScheduleParser(context, activity, progressBar, errorMessage, null, null).execute(urls)
        }else{
            val scheduleList = Utils.getScheduleList(cursor, db)
            val adapter = getAdapter(scheduleList)
            val listView = view.findViewById<ListView>(R.id.schedule_list_view)
            listView.adapter = adapter
            val scheduleSwipe = view.findViewById<SwipeRefreshLayout>(R.id.schedule_swipe)
            scheduleSwipe.setOnRefreshListener{
                ScheduleParser(context, null, null, errorMessage, adapter, scheduleSwipe).execute(urls)
                Toast.makeText(context, "Schedule refreshed", Toast.LENGTH_SHORT).show()
            }
        }
        cursor.close()

        return view
    }

    private fun getAdapter(scheduleList: List<ScheduleItem>): ScheduleAdapter{
        return ScheduleAdapter(context, scheduleList)
    }

}
