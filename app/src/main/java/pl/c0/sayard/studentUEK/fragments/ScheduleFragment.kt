package pl.c0.sayard.studentUEK.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import pl.c0.sayard.studentUEK.BackButtonEditText
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils.Companion.isDeviceOnline
import pl.c0.sayard.studentUEK.Utils.Companion.setFilters
import pl.c0.sayard.studentUEK.Utils.Companion.setFiltersUiState
import pl.c0.sayard.studentUEK.activities.AddLessonActivity
import pl.c0.sayard.studentUEK.activities.ScheduleItemDetailsActivity
import pl.c0.sayard.studentUEK.adapters.FilteredLessonsAdapter
import pl.c0.sayard.studentUEK.adapters.ScheduleAdapter
import pl.c0.sayard.studentUEK.data.FilteredLesson
import pl.c0.sayard.studentUEK.data.ScheduleItem
import pl.c0.sayard.studentUEK.db.DatabaseManager
import pl.c0.sayard.studentUEK.jobs.RefreshScheduleJob
import pl.c0.sayard.studentUEK.parsers.ScheduleParser


class ScheduleFragment : Fragment() {

    private var scheduleSearch: BackButtonEditText? = null

    companion object {
        fun newInstance(): ScheduleFragment{
            return ScheduleFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_schedule, container, false)
        val progressBar = view.findViewById<ProgressBar>(R.id.schedule_progress_bar)
        val errorMessage = view.findViewById<TextView>(R.id.schedule_error_message)
        val dbManager = DatabaseManager(context)

        val urls = mutableListOf<String>()
        val groups = dbManager.getGroups()
        groups.mapTo(urls){it.url}
        val languageGroups = dbManager.getLanguageGroups()
        languageGroups.mapTo(urls) { it.url }
        val cursor = dbManager.getScheduleCursor()
        var cursorCount = cursor.count
        if(!isDeviceOnline(context)){
            cursorCount = -1
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if((prefs.getBoolean(getString(R.string.PREFS_REFRESH_SCHEDULE), false) || cursorCount == 0)){
            ScheduleParser(context, activity, progressBar, errorMessage, null, null).execute(urls)
            prefs.edit().putBoolean(getString(R.string.PREFS_REFRESH_SCHEDULE), false).apply()
        }else{
            val scheduleList = dbManager.getScheduleList()
            if (scheduleList.isNotEmpty()){
                errorMessage.visibility = View.GONE
                val adapter = getAdapter(scheduleList)
                scheduleSearch = view.findViewById<BackButtonEditText>(R.id.schedule_search)
                scheduleSearch?.addTextChangedListener(object: TextWatcher{
                    override fun afterTextChanged(p0: Editable?) {
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        adapter.filter.filter(p0.toString())
                    }
                })
                scheduleSearch?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    if(!hasFocus && scheduleSearch?.text.toString() == ""){
                        scheduleSearch?.visibility = View.GONE
                    }
                }
                val listView = view.findViewById<ListView>(R.id.schedule_list_view)
                listView.adapter = adapter
                listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                    val scheduleItem = parent.getItemAtPosition(position) as ScheduleItem
                    val intent = Intent(context, ScheduleItemDetailsActivity::class.java).apply {
                        putExtra(getString(R.string.subject_extra), scheduleItem.subject)
                        putExtra(getString(R.string.type_extra), scheduleItem.type)
                        putExtra(getString(R.string.teacher_extra), scheduleItem.teacher)
                        putExtra(getString(R.string.teacher_id_extra), scheduleItem.teacherId)
                        putExtra(getString(R.string.classroom_extra), scheduleItem.classroom)
                        putExtra(getString(R.string.comments_extra), scheduleItem.comments)
                        putExtra(getString(R.string.date_extra), scheduleItem.dateStr)
                        putExtra(getString(R.string.start_date_extra), scheduleItem.startDateStr)
                        putExtra(getString(R.string.end_date_extra), scheduleItem.endDateStr)
                        putExtra(getString(R.string.is_custom_extra), scheduleItem.isCustom)
                        putExtra(getString(R.string.extra_custom_id), scheduleItem.customId)
                        putExtra(getString(R.string.extra_note_id), scheduleItem.noteId)
                        putExtra(getString(R.string.extra_note_content), scheduleItem.noteContent)
                    }
                    startActivity(intent)
                }
                listView.isLongClickable = true
                listView.onItemLongClickListener = AdapterView.OnItemLongClickListener { parent, _, position, _ ->
                    val dialogBuilder = AlertDialog.Builder(context)
                    val scheduleItem = parent.getItemAtPosition(position) as ScheduleItem

                    dialogBuilder
                            .setTitle(getString(R.string.hide_lesson_from_schedule))
                            .setMessage(getString(R.string.hide_lesson_from_schedule_message))
                            .setPositiveButton(getString(R.string.remove)) { _, _ ->
                                DatabaseManager(context).addLessonToFilteredLessons(scheduleItem)
                                val ft = activity.supportFragmentManager.beginTransaction()
                                ft.detach(this)
                                ft.attach(this)
                                ft.commit()
                            }
                            .setNegativeButton(context.getString(R.string.cancel)) { _, _ ->}
                            .show()
                    true
                }
                val scheduleSwipe = view.findViewById<SwipeRefreshLayout>(R.id.schedule_swipe)
                scheduleSwipe.setOnRefreshListener{
                    if(isDeviceOnline(context)){
                        ScheduleParser(context, null, null, errorMessage, adapter, scheduleSwipe).execute(urls)
                        scheduleSearch?.setText("", TextView.BufferType.EDITABLE)
                        Toast.makeText(context, getString(R.string.schedule_refreshed), Toast.LENGTH_SHORT).show()
                        Thread{
                            kotlin.run {
                                RefreshScheduleJob.refreshSchedule(context)
                            }
                        }.start()
                    }else{
                        Toast.makeText(context, getString(R.string.no_internet_conn), Toast.LENGTH_SHORT).show()
                    }
                    scheduleSwipe.isRefreshing = false
                }
                listView.setOnScrollListener(object: AbsListView.OnScrollListener{
                    override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                        var topRowVerticalPosition = 0
                        if(listView != null && listView.childCount != 0){
                            topRowVerticalPosition = listView.getChildAt(0).top
                        }
                        scheduleSwipe.isEnabled = (firstVisibleItem == 0 && topRowVerticalPosition >= 0)
                    }

                    override fun onScrollStateChanged(p0: AbsListView?, p1: Int) {
                    }

                })
            }else{
                errorMessage.visibility = View.VISIBLE
                val scheduleSwipe = view.findViewById<SwipeRefreshLayout>(R.id.schedule_swipe)
                scheduleSwipe.setOnRefreshListener{
                    val ft = activity.supportFragmentManager.beginTransaction()
                    ft.detach(this)
                    ft.attach(this)
                    ft.commit()
                    scheduleSwipe.isRefreshing = false
                }
            }
        }
        cursor.close()
        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.schedule_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.schedule_filter_item->{
                val dialogBuilder = AlertDialog.Builder(context)
                val inflater = activity.layoutInflater
                val dialogView = inflater.inflate(R.layout.schedule_filter, null)
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                setFiltersUiState(dialogView, prefs, context)
                val dbManager = DatabaseManager(context)
                val filteredLessonsList = dbManager.getFilteredLessons()
                val message = dialogView.findViewById<TextView>(R.id.filtered_lessons_empty_message)

                if(filteredLessonsList.isNotEmpty()){
                    val filteredLessonAdapter = FilteredLessonsAdapter(context, filteredLessonsList as MutableList<FilteredLesson>)
                    val filteredLessonsListView = dialogView.findViewById<ListView>(R.id.filtered_lessons_lv)
                    filteredLessonsListView.adapter = filteredLessonAdapter
                    message.visibility = View.GONE
                }else{
                    message.visibility = View.VISIBLE
                }

                dialogBuilder
                        .setView(dialogView)
                        .setTitle(getString(R.string.schedule_filters))
                        .setPositiveButton(getString(R.string.accept)) { _, _ ->
                            setFilters(dialogView, prefs, context)
                            prefs.edit()
                                    .putBoolean(getString(R.string.PREFS_REFRESH_SCHEDULE), true)
                                    .apply()
                            activity.supportFragmentManager
                                    .beginTransaction()
                                    .detach(this)
                                    .attach(this)
                                    .commit()
                        }
                        .setNegativeButton(getString(R.string.cancel)) { _, _ -> }
                        .create()
                        .show()
            }
            R.id.new_schedule_item -> {
                val newLessonIntent = Intent(context, AddLessonActivity::class.java)
                startActivity(newLessonIntent)
            }
            R.id.search_schedule_item -> {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if(scheduleSearch?.visibility == View.GONE){
                    scheduleSearch?.visibility = View.VISIBLE
                    scheduleSearch?.isFocusableInTouchMode = true
                    scheduleSearch?.requestFocus()
                    imm.showSoftInput(scheduleSearch, InputMethodManager.SHOW_IMPLICIT)
                }else{
                    scheduleSearch?.visibility = View.GONE
                    imm.hideSoftInputFromWindow(scheduleSearch?.windowToken, 0)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getAdapter(scheduleList: List<ScheduleItem>): ScheduleAdapter{
        return ScheduleAdapter(context, scheduleList)
    }

    override fun onResume() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if(prefs.getBoolean(getString(R.string.PREFS_REFRESH_SCHEDULE), false)){
            val ft = activity.supportFragmentManager.beginTransaction()
            ft.detach(this).attach(this).commit()
        }
        super.onResume()
    }

}
