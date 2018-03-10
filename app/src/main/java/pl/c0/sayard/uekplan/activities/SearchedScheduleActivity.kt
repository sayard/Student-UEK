package pl.c0.sayard.uekplan.activities

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.adapters.ScheduleAdapter
import pl.c0.sayard.uekplan.data.Group
import pl.c0.sayard.uekplan.data.ScheduleItem
import pl.c0.sayard.uekplan.parsers.SearchedScheduleParser

class SearchedScheduleActivity : AppCompatActivity() {

    private var groupType: String? = null
    private var teacherId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_searched_schedule)
        this.groupType = intent.getStringExtra(getString(R.string.EXTRA_SEARCHED_SCHEDULE_GROUP_TYPE))
        val groupName = intent.getStringExtra(getString(R.string.EXTRA_SEARCHED_SCHEDULE_GROUP_NAME))
        title = groupName
        if(groupType != null){
            val group = Group(
                    intent.getIntExtra(getString(R.string.EXTRA_SEARCHED_SCHEDULE_GROUP_ID), 0),
                    groupName,
                    groupType!!
            )
            val progressBar = findViewById<ProgressBar>(R.id.searched_schedule_progress_bar)
            val errorMessage= findViewById<TextView>(R.id.searched_schedule_error_message)
            executeSearchedScheduleParser(progressBar, errorMessage, group)
            val searchedScheduleSwipe = findViewById<SwipeRefreshLayout>(R.id.searched_schedule_swipe)
            searchedScheduleSwipe.setOnRefreshListener {
                executeSearchedScheduleParser(progressBar, errorMessage, group)
                Toast.makeText(this, getString(R.string.schedule_refreshed), Toast.LENGTH_SHORT).show()
                searchedScheduleSwipe.isRefreshing = false
            }
        }
    }

    private fun executeSearchedScheduleParser(progressBar: ProgressBar, errorMessage: TextView, group: Group){
        SearchedScheduleParser(progressBar, errorMessage, object: SearchedScheduleParser.OnTaskCompleted{
            override fun onTaskCompleted(result: List<ScheduleItem>) {
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
                    Toast.makeText(this@SearchedScheduleActivity, getText(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
                    errorMessage.visibility = View.VISIBLE
                }else{
                    errorMessage.visibility = View.GONE
                }

                val listView = findViewById<ListView>(R.id.searched_schedule_list_view)
                listView.adapter = adapter
            }

        }).execute(group)
    }

    private fun getAdapter(scheduleList: List<ScheduleItem>): ScheduleAdapter {
        return ScheduleAdapter(this, scheduleList)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.searched_schedule_menu, menu)
        if(groupType == "G"){
            menu?.findItem(R.id.teacher_page)?.isVisible = false
        }
        return super.onCreateOptionsMenu(menu)
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
}
