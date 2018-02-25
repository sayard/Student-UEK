package pl.c0.sayard.uekplan.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.view.*
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.adapters.ScheduleAdapter
import pl.c0.sayard.uekplan.data.Group
import pl.c0.sayard.uekplan.data.ScheduleItem
import pl.c0.sayard.uekplan.parsers.SearchedScheduleParser

class SearchedScheduleFragment : Fragment() {

    private var groupType: String? = null
    private var teacherId = 0

    companion object {
        fun newInstance(group: Group): SearchedScheduleFragment {
            val bundle = Bundle()
            bundle.putInt("group_id", group.id)
            bundle.putString("group_name", group.name)
            bundle.putString("group_type", group.type)

            val fragment = SearchedScheduleFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.groupType = arguments.getString("group_type")
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        activity.title = arguments.getString("group_name")
        val view = inflater!!.inflate(R.layout.fragment_searched_schedule, container, false)
        val group = Group(arguments.getInt("group_id"), arguments.getString("group_name"), arguments.getString("group_type"))
        val progressBar = view.findViewById<ProgressBar>(R.id.searched_schedule_progress_bar)
        val errorMessage= view.findViewById<TextView>(R.id.searched_schedule_error_message)
        executeSearchedScheduleParser(progressBar, errorMessage, group, view)
        val searchedScheduleSwipe = view.findViewById<SwipeRefreshLayout>(R.id.searched_schedule_swipe)
        searchedScheduleSwipe.setOnRefreshListener {
            executeSearchedScheduleParser(progressBar, errorMessage, group, view)
            Toast.makeText(context, getString(R.string.schedule_refreshed), Toast.LENGTH_SHORT).show()
            searchedScheduleSwipe.isRefreshing = false
        }
        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.searched_schedule_menu, menu)
        if(groupType == "G"){
            menu?.findItem(R.id.teacher_page)?.isVisible = false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.teacher_page -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://e-uczelnia.uek.krakow.pl/course/view.php?id=$teacherId"))
                startActivity(browserIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun executeSearchedScheduleParser(progressBar: ProgressBar, errorMessage: TextView, group: Group, view: View){
        SearchedScheduleParser(progressBar, errorMessage, this, object: SearchedScheduleParser.OnTaskCompleted{
            override fun onTaskCompleted(result: List<ScheduleItem>, fragment: SearchedScheduleFragment) {
                for(i in 0 until result.size){
                    val scheduleItem = result[i]
                    if(i==0){
                        scheduleItem.isFirstOnTheDay = true
                        teacherId = result[i].teacherId
                    }else{
                        val previousScheduleItem = result[i-1]
                        if(scheduleItem.dateStr != previousScheduleItem.dateStr){
                            scheduleItem.isFirstOnTheDay = true
                        }
                    }
                }
                val adapter = getAdapter(result)
                if(adapter.count <= 0){
                    Toast.makeText(this@SearchedScheduleFragment.context, getText(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
                    errorMessage.visibility = View.VISIBLE
                }else{
                    errorMessage.visibility = View.GONE
                }

                val listView = view.findViewById<ListView>(R.id.searched_schedule_list_view)
                listView.adapter = adapter
            }

        }).execute(group)
    }

    private fun getAdapter(scheduleList: List<ScheduleItem>): ScheduleAdapter {
        return ScheduleAdapter(context, scheduleList)
    }

}