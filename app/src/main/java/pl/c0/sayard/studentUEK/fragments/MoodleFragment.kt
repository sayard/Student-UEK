package pl.c0.sayard.studentUEK.fragments

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.*
import androidx.core.content.edit
import androidx.core.net.toUri
import pl.c0.sayard.studentUEK.uiElements.BackButtonEditText
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils.Companion.isDeviceOnline
import pl.c0.sayard.studentUEK.adapters.CoursesAdapter
import pl.c0.sayard.studentUEK.data.Course
import pl.c0.sayard.studentUEK.parsers.CourseParser
import pl.c0.sayard.studentUEK.parsers.MoodleTokenParser


class MoodleFragment : Fragment() {

    private var searchBox: BackButtonEditText? = null
    private val WRITE_PERMISSIONS_REQUEST_CODE = 222
    private var requestPermissionsState = false
    private var USOS_LOGIN_PAGE_URL = "https://logowanie.uek.krakow.pl/cas/login?service=https%3A%2F%2Fe-uczelnia.uek.krakow.pl%2Flogin%2Findex.php%3FauthCAS%3DCAS"
    private val MOODLE_HOME_PAGE_URL = "https://e-uczelnia.uek.krakow.pl/"

    private val logoutDialogListener = DialogInterface.OnClickListener { dialog, which ->
        when(which){
            DialogInterface.BUTTON_POSITIVE->{
                PreferenceManager.getDefaultSharedPreferences(context).edit{
                    putString(getString(R.string.pl_c0_sayard_StudentUEK_PREFS_MOODLE_TOKEN), null)
                }
                fragmentManager?.beginTransaction()
                        ?.detach(this)
                        ?.attach(this)
                        ?.commit()
            }
            DialogInterface.BUTTON_NEGATIVE->{}
        }
    }

    companion object {
        fun newInstance(): MoodleFragment{
            return MoodleFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_moodle, container, false)

        val coursesView = view.findViewById<LinearLayout>(R.id.moodle_courses_view)
        val loginView = view.findViewById<ScrollView>(R.id.moodle_login_view)
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
                MoodleTokenParser(context, this, loginProgressBar, loginButton)
                        .execute(login.text.toString(), password.text.toString())
                prefs.edit {
                    putString(getString(R.string.PREFS_MOODLE_LOGIN), login.text.toString())
                    putString(getString(R.string.PREFS_MOODLE_PASSWORD), password.text.toString())
                }
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
        if(PreferenceManager
                        .getDefaultSharedPreferences(context)
                        .getString(getString(R.string.pl_c0_sayard_StudentUEK_PREFS_MOODLE_TOKEN), null) != null){
            inflater?.inflate(R.menu.moodle_menu, menu)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.search_courses->{
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
            R.id.moodle_logout->{
                val logoutDialogBuilder = AlertDialog.Builder(context)
                logoutDialogBuilder
                        .setMessage(getString(R.string.do_you_want_to_log_out))
                        .setPositiveButton(getString(R.string.yes), logoutDialogListener)
                        .setNegativeButton(getString(R.string.no), logoutDialogListener)
                        .show()
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
        val webView = view.findViewById<WebView>(R.id.moodle_web_view)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        if(isDeviceOnline(context)
                && prefs.getString(context?.getString(R.string.PREFS_MOODLE_LOGIN), null) != null
                && prefs.getString(context?.getString(R.string.PREFS_MOODLE_PASSWORD), null) != null){
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
                            val courseUrl = "https://e-uczelnia.uek.krakow.pl/course/view.php?id=$courseId"
                            webView.apply {
                                visibility = View.VISIBLE
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.databaseEnabled = true
                            }

                            webView.webViewClient = object: WebViewClient(){

                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    view?.loadUrl(request?.url.toString())
                                    return false
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    view?.visibility = View.GONE
                                    progressBar.visibility = View.VISIBLE
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    val login = prefs.getString(context?.getString(R.string.PREFS_MOODLE_LOGIN), "")
                                    val password = prefs.getString(context?.getString(R.string.PREFS_MOODLE_PASSWORD), "")
                                    val js = "$('#username').val('$login');" +
                                            "$('#password').val('$password');" +
                                            "$(\"input[name='submit']\").attr('disabled', false);" +
                                            "$(\"input[name='submit']\").click();"
                                    webView.evaluateJavascript(
                                            js,
                                            null)
                                    if(url == MOODLE_HOME_PAGE_URL){
                                        webView.loadUrl(courseUrl)
                                    }
                                    if(url == courseUrl){
                                        view?.visibility = View.VISIBLE
                                        progressBar.visibility = View.GONE
                                    }
                                }
                            }
                            swipeRefreshLayout.isEnabled = false
                            webView.setDownloadListener(DownloadListener { url, userAgent, contentDescription, mimetype, _ ->
                                if(!isWriteStoragePermissionGranted()){
                                    requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_PERMISSIONS_REQUEST_CODE)
                                    if(!requestPermissionsState){
                                        Toast.makeText(context, getString(R.string.writing_perms_required), Toast.LENGTH_LONG).show()
                                        return@DownloadListener
                                    }
                                }
                                val request = DownloadManager.Request(url.toUri())
                                request.setMimeType(mimetype)
                                val cookies = CookieManager.getInstance().getCookie(url)
                                request.addRequestHeader("cookie", cookies)
                                request.addRequestHeader("User-Agent", userAgent)
                                request.allowScanningByMediaScanner()
                                request.setNotificationVisibility(
                                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                val fileName = URLUtil.guessFileName(url, contentDescription, mimetype)
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                                val dManager = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                                dManager.enqueue(request)
                            })
                            webView.loadUrl(USOS_LOGIN_PAGE_URL)
                        }
                        listView.setOnScrollListener(object: AbsListView.OnScrollListener{
                            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                                if(webView.visibility == View.GONE){
                                    var topRowVerticalPosition = 0
                                    if(listView != null && listView.childCount != 0){
                                        topRowVerticalPosition = listView.getChildAt(0).top
                                    }
                                    swipeRefreshLayout.isEnabled = (firstVisibleItem == 0 && topRowVerticalPosition >= 0)
                                }else{
                                    swipeRefreshLayout.isEnabled = false
                                }
                            }

                            override fun onScrollStateChanged(p0: AbsListView?, p1: Int) {
                            }

                        })
                    }else{
                        errorView.visibility = View.VISIBLE
                    }
                }
            }, prefs, context).execute(token)
        }else{
            errorView.visibility = View.VISIBLE
        }
        logInAgainButton.setOnClickListener{
            prefs.edit {
                putString(context?.getString(R.string.pl_c0_sayard_StudentUEK_PREFS_MOODLE_TOKEN), null)
            }
            val fragmentManager = fragmentManager
            fragmentManager?.beginTransaction()
                    ?.detach(this)
                    ?.attach(this)
                    ?.commit()
        }
    }

    private fun getAdapter(context: Context?, coursesList: List<Course>): CoursesAdapter {
        return CoursesAdapter(context, coursesList)
    }

    private fun isWriteStoragePermissionGranted(): Boolean{
        return if(Build.VERSION.SDK_INT >= 23){
            context?.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }else{
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == WRITE_PERMISSIONS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            requestPermissionsState = true
        }
    }

}