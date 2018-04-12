package pl.c0.sayard.studentUEK.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
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
import pl.c0.sayard.studentUEK.adapters.CoursesAdapter
import pl.c0.sayard.studentUEK.data.Course
import pl.c0.sayard.studentUEK.parsers.CourseParser
import pl.c0.sayard.studentUEK.parsers.MoodleTokenParser

class MoodleFragment : Fragment() {

    private var searchBox:BackButtonEditText? = null

    companion object {
        fun newInstance(): MoodleFragment{
            return MoodleFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_moodle, container, false)

        val coursesView = view.findViewById<LinearLayout>(R.id.moodle_courses_view)
        val loginView = view.findViewById<ConstraintLayout>(R.id.moodle_login_view)
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.courses_swipe)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val token = prefs.getString(getString(R.string.pl_c0_sayard_StudentUEK_PREFS_MOODLE_TOKEN), null)

        if(token != null){
            coursesView.visibility = View.VISIBLE
            loginView.visibility = View.GONE

            searchBox = view.findViewById(R.id.courses_search)
            swipeRefreshLayout.setOnRefreshListener{
                loadCourses(view, token)
                swipeRefreshLayout.isRefreshing = false
            }
            loadCourses(view, token)

        }else{
            coursesView.visibility = View.GONE
            loginView.visibility = View.VISIBLE

            val login = view.findViewById<EditText>(R.id.moodle_login)
            val password = view.findViewById<EditText>(R.id.moodle_password)
            val loginButton = view.findViewById<Button>(R.id.mooodle_login_button)
            val loginProgressBar = view.findViewById<ProgressBar>(R.id.moodle_login_progress)
            loginButton.setOnClickListener{
                MoodleTokenParser(context, this, loginProgressBar, loginButton).execute(login.text.toString(), password.text.toString())
            }
            password.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                if((event?.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    loginButton.performClick()
                    return@OnKeyListener true
                }
                false
            })
        }
        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.moodle_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.search_courses->{
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if(searchBox?.visibility == View.GONE){
                    searchBox?.visibility = View.VISIBLE
                    searchBox?.isFocusableInTouchMode = true
                    searchBox?.requestFocus()
                    imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT)
                }else{
                    searchBox?.visibility = View.GONE
                    imm.hideSoftInputFromWindow(searchBox?.windowToken, 0)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadCourses(view: View, token: String){

        val progressBar = view.findViewById<ProgressBar>(R.id.courses_progress_bar)
        val errorView = view.findViewById<ConstraintLayout>(R.id.courses_error)
        val listView = view.findViewById<ListView>(R.id.courses_list_view)
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.courses_swipe)
        val logInAgainButton = view.findViewById<Button>(R.id.log_in_again_button)

        if(isDeviceOnline(context)){
            errorView.visibility = View.GONE
            CourseParser(progressBar, object: CourseParser.OnTaskCompleted{
                override fun onTaskCompleted(result: List<Course>?) {
                    if(result != null){
                        val adapter = getAdapter(context, result)

                        searchBox?.addTextChangedListener(object: TextWatcher{
                            override fun afterTextChanged(s: Editable?) {
                            }

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            }

                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                adapter.filter.filter(s.toString())
                            }

                        })

                        listView.adapter = adapter
                        listView.onItemClickListener = AdapterView.OnItemClickListener {_, _, position, _ ->
                            val courseId = result[position].id
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://e-uczelnia.uek.krakow.pl/course/view.php?id=$courseId"))
                            context.startActivity(browserIntent)
                        }
                        listView.setOnScrollListener(object: AbsListView.OnScrollListener{
                            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                                var topRowVerticalPosition = 0
                                if(listView != null && listView.childCount != 0){
                                    topRowVerticalPosition = listView.getChildAt(0).top
                                }
                                swipeRefreshLayout.isEnabled = (firstVisibleItem == 0 && topRowVerticalPosition >= 0)
                            }

                            override fun onScrollStateChanged(p0: AbsListView?, p1: Int) {
                            }

                        })
                    }else{
                        errorView.visibility = View.VISIBLE
                    }
                }
            }).execute(token)
        }else{
            errorView.visibility = View.VISIBLE
        }
        logInAgainButton.setOnClickListener{
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit().putString(context.getString(R.string.pl_c0_sayard_StudentUEK_PREFS_MOODLE_TOKEN), null).apply()
            val fragmentManager = fragmentManager
            fragmentManager.beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit()
        }
    }

    private fun getAdapter(context: Context, coursesList: List<Course>): CoursesAdapter {
        return CoursesAdapter(context, coursesList)
    }

}