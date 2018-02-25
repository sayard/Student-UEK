package pl.c0.sayard.uekplan.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.adapters.GroupAndTeacherListAdapter
import pl.c0.sayard.uekplan.data.Group
import pl.c0.sayard.uekplan.parsers.GroupAndTeacherParser


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
            executeGroupAndTeacherParser(view)
            searchBox.setText("", TextView.BufferType.EDITABLE)
            Toast.makeText(context, getString(R.string.groups_and_teachers_refreshed), Toast.LENGTH_SHORT).show()
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
                val adapter = getAdapter(context, result!!)
                if(adapter.count <= 0){
                    Toast.makeText(this@SearchFragment.context, getText(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
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
                    val searchedScheduleFragment = SearchedScheduleFragment.newInstance(group)
                    activity.supportFragmentManager.beginTransaction().replace(R.id.main_frame, searchedScheduleFragment).addToBackStack(null).commit()
                }
            }

        }).execute()
    }

    override fun onResume() {
        super.onResume()
        activity.title = getString(R.string.notes)
    }

    private fun getAdapter(context: Context, groupList: List<Group>): GroupAndTeacherListAdapter{
        return GroupAndTeacherListAdapter(context, groupList)
    }
}
