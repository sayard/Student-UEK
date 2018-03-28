package pl.c0.sayard.studentUEK.activities

import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_main.*
import pl.c0.sayard.studentUEK.BackButtonEditText
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import pl.c0.sayard.studentUEK.Utils.Companion.FIRST_RUN_SHARED_PREFS_KEY
import pl.c0.sayard.studentUEK.Utils.Companion.isDeviceOnline
import pl.c0.sayard.studentUEK.Utils.Companion.onActivityCreateSetTheme
import pl.c0.sayard.studentUEK.adapters.ViewPagerAdapter
import pl.c0.sayard.studentUEK.fragments.NotesFragment
import pl.c0.sayard.studentUEK.fragments.ScheduleFragment
import pl.c0.sayard.studentUEK.fragments.SearchFragment
import pl.c0.sayard.studentUEK.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private var prevMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onActivityCreateSetTheme(this)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val firstRun = prefs.getBoolean(FIRST_RUN_SHARED_PREFS_KEY, true)
        if(firstRun){
            val intent = Intent(this, FirstRunStepOneActivity::class.java)
            startActivity(intent)
        }else{
            setContentView(R.layout.activity_main)

            val viewPager = findViewById<ViewPager>(R.id.main_frame)

            navigation.selectedItemId = R.id.navigation_schedule
            navigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
                val view = this.currentFocus
                when(item.itemId){
                    R.id.navigation_search ->{
                        if(viewPager.currentItem == 0){
                            val searchBox = findViewById<BackButtonEditText>(R.id.group_and_teacher_search)
                            showSearchBox(searchBox)
                        }
                        viewPager.currentItem = 0
                        setTitle(R.string.search)
                    }
                    R.id.navigation_schedule -> {
                        if(viewPager.currentItem == 1){
                            val searchBox = findViewById<BackButtonEditText>(R.id.schedule_search)
                            showSearchBox(searchBox)
                        }
                        viewPager.currentItem = 1
                        setTitle(R.string.schedule)
                    }
                    R.id.navigation_notes -> {
                        if(viewPager.currentItem == 2){
                            val searchBox = findViewById<BackButtonEditText>(R.id.notes_search)
                            showSearchBox(searchBox)
                        }
                        viewPager.currentItem = 2
                        setTitle(R.string.notes)
                    }
                    R.id.navigation_settings -> {
                        viewPager.currentItem = 3
                        setTitle(R.string.settings)
                    }
                    else -> return@OnNavigationItemSelectedListener false
                }
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view?.windowToken, 0)
                true
            })

            viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
                override fun onPageScrollStateChanged(state: Int) {
                }

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                }

                override fun onPageSelected(position: Int) {
                    if(prevMenuItem != null){
                        prevMenuItem!!.isChecked = false
                    }else{
                        navigation.menu.getItem(1).isChecked = false
                    }
                    val menuItem = navigation.menu.getItem(position)
                    menuItem.isChecked = true
                    prevMenuItem = menuItem
                    title = getString(Utils.getTitleBasedOnPosition(position))
                }

            })
            setUpViewPager(viewPager)
            if(isDeviceOnline(this)){
                MobileAds.initialize(this, "") //TODO: supply admob app id
                val adView = findViewById<AdView>(R.id.banner_ad)
                val adRequest = AdRequest.Builder().build()
                adView.loadAd(adRequest)
                adView.adListener = object : AdListener(){
                    override fun onAdLoaded() {
                        super.onAdLoaded()
                        adView.visibility = View.VISIBLE
                    }

                    override fun onAdFailedToLoad(p0: Int) {
                        super.onAdFailedToLoad(p0)
                        adView.visibility = View.GONE
                    }
                }
            }
            if(prefs.getBoolean(getString(R.string.PREFS_APP_NOT_RATED), true)){
                val editor = prefs.edit()
                val ratingCounter = prefs.getInt(getString(R.string.PREFS_APP_RATING_DIALOG_COUNTER), 20) - 1
                editor.putInt(getString(R.string.PREFS_APP_RATING_DIALOG_COUNTER), ratingCounter).apply()
                if(ratingCounter == 0){
                    AlertDialog.Builder(this)
                            .setTitle(getString(R.string.do_you_like_this_app))
                            .setMessage(getString(R.string.app_rating_message))
                            .setNeutralButton(getString(R.string.maybe_later), null)
                            .setNegativeButton(getString(R.string.no_thanks)) { _, _ -> editor.putBoolean(getString(R.string.PREFS_APP_NOT_RATED), false).apply() }
                            .setPositiveButton(getString(R.string.sure_take_me_there)) { _, _ ->
                                editor.putBoolean(getString(R.string.PREFS_APP_NOT_RATED), false).apply()

                                var marketFound = false
                                val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                                val otherApps = packageManager.queryIntentActivities(rateIntent, 0)

                                for(otherApp in otherApps){
                                    if(otherApp.activityInfo.applicationInfo.packageName == "com.android.vending"){
                                        val otherAppActivity = otherApp.activityInfo
                                        val componentName = ComponentName(
                                                otherAppActivity.applicationInfo.packageName,
                                                otherAppActivity.name
                                        )
                                        rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                                        rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                        rateIntent.component = componentName
                                        startActivity(rateIntent)
                                        marketFound = true
                                        break
                                    }
                                }
                                if(!marketFound){
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                                }
                            }
                            .create()
                            .show()
                    editor.putInt(getString(R.string.PREFS_APP_RATING_DIALOG_COUNTER), 20).apply()
                }
            }
        }
    }

    private fun setUpViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        val searchFragment = SearchFragment.newInstance()
        val scheduleFragment = ScheduleFragment.newInstance()
        val notesFragment = NotesFragment.newInstance()
        val settingsFragment = SettingsFragment.newInstance()
        adapter.addFragment(searchFragment)
        adapter.addFragment(scheduleFragment)
        adapter.addFragment(notesFragment)
        adapter.addFragment(settingsFragment)
        viewPager.adapter = adapter
        viewPager.currentItem = 1
    }

    private fun showSearchBox(searchBox: BackButtonEditText){
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        if(searchBox.visibility == View.GONE){
            searchBox.visibility = View.VISIBLE
            searchBox.isFocusableInTouchMode = true
            searchBox.requestFocus()
            searchBox.postDelayed({ imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT) }, 50)
        }else{
            searchBox.visibility = View.GONE
            imm.hideSoftInputFromWindow(searchBox.windowToken, 0)
        }
    }

}
