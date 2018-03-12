package pl.c0.sayard.studentUEK.fragments

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.activities.SearchedScheduleActivity
import pl.c0.sayard.studentUEK.adapters.GroupAndTeacherListAdapter
import pl.c0.sayard.studentUEK.data.Group
import pl.c0.sayard.studentUEK.parsers.GroupAndTeacherParser


class SearchFragment : Fragment() {

    companion object {
        fun newInstance(): SearchFragment{
            return SearchFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_search, container, false)
        val searchSwipe = view.findViewById<SwipeRefreshLayout>(R.id.search_swipe)
        val searchBox = view.findViewById<EditText>(R.id.group_and_teacher_search)
        executeGroupAndTeacherParser(view)
        searchSwipe.setOnRefreshListener {
            val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            if(networkInfo != null && networkInfo.isConnected){
                executeGroupAndTeacherParser(view)
                searchBox.setText("", TextView.BufferType.EDITABLE)
                Toast.makeText(context, getString(R.string.groups_and_teachers_refreshed), Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, getString(R.string.no_internet_conn), Toast.LENGTH_SHORT).show()
            }
            searchSwipe.isRefreshing = false
        }
        return view
    }

    private fun executeGroupAndTeacherParser(view: View){
        val errorMessage = view.findViewById<TextView>(R.id.group_and_teacher_error_message)
        val searchBox = view.findViewById<EditText>(R.id.group_and_teacher_search)
        val progressBar = view.findViewById<ProgressBar>(R.id.group_and_teacher_progress_bar)
        val listView = view.findViewById<ListView>(R.id.group_and_teacher_list_view)
        GroupAndTeacherParser(this, progressBar, listView,object:GroupAndTeacherParser.OnTaskCompleted{
            override fun onTaskCompleted(result: List<Group>?, fragment: SearchFragment) {
                if(context != null){
                    val adapter = getAdapter(context, result!!)
                    if(adapter.count <= 0){
                        errorMessage.visibility = View.VISIBLE
                        searchBox.visibility = View.GONE
                    }else{
                        errorMessage.visibility = View.GONE
                        searchBox.visibility = View.VISIBLE
                    }

                    searchBox.addTextChangedListener(object: TextWatcher{
                        override fun afterTextChanged(p0: Editable?) {
                        }

                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            adapter.filter.filter(p0.toString())
                        }

                    })

                    val listView = view.findViewById<ListView>(R.id.group_and_teacher_list_view)
                    listView.adapter = adapter
                    listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                        val group = parent.getItemAtPosition(position) as Group
                        val intent = Intent(context, SearchedScheduleActivity::class.java).apply {
                            putExtra(getString(R.string.EXTRA_SEARCHED_SCHEDULE_GROUP_ID), group.id)
                            putExtra(getString(R.string.EXTRA_SEARCHED_SCHEDULE_GROUP_NAME), group.name)
                            putExtra(getString(R.string.EXTRA_SEARCHED_SCHEDULE_GROUP_TYPE), group.type)
                        }
                        startActivity(intent)
                    }
                }
            }

        }).execute()
    }

    override fun onResume() {
        super.onResume()
        val searchBox = view?.findViewById<EditText>(R.id.group_and_teacher_search)
        searchBox?.setText("", TextView.BufferType.EDITABLE)
    }

    private fun getAdapter(context: Context, groupList: List<Group>): GroupAndTeacherListAdapter{
        return GroupAndTeacherListAdapter(context, groupList)
    }
}
