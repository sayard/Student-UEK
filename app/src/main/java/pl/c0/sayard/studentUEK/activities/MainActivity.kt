package pl.c0.sayard.studentUEK.activities

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import androidx.core.content.edit
import androidx.core.net.toUri
import com.anjlab.android.iab.v3.BillingProcessor
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardedVideoAd
import pl.c0.sayard.studentUEK.BillingHandler
import pl.c0.sayard.studentUEK.HideAdsListener
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import pl.c0.sayard.studentUEK.Utils.Companion.FIRST_RUN_SHARED_PREFS_KEY
import pl.c0.sayard.studentUEK.Utils.Companion.isDeviceOnline
import pl.c0.sayard.studentUEK.Utils.Companion.isHideAdsEnabled
import pl.c0.sayard.studentUEK.Utils.Companion.onActivityCreateSetTheme
import pl.c0.sayard.studentUEK.Utils.Companion.subscribeToTopics
import pl.c0.sayard.studentUEK.adapters.ViewPagerAdapter
import pl.c0.sayard.studentUEK.fragments.*

class MainActivity : AppCompatActivity() {

    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mRewardedVideoAd:RewardedVideoAd

    companion object {
        var bp: BillingProcessor? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onActivityCreateSetTheme(this)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if(!prefs.contains(getString(R.string.PREFS_DISCOURSES_VISIBLE))
            || !prefs.contains(getString(R.string.PREFS_EXERCISES_VISIBLE))
            || !prefs.contains(getString(R.string.PREFS_LECTURES_VISIBLE))){

            prefs.edit {
                putBoolean(getString(R.string.PREFS_DISCOURSES_VISIBLE), true)
                putBoolean(getString(R.string.PREFS_EXERCISES_VISIBLE), true)
                putBoolean(getString(R.string.PREFS_LECTURES_VISIBLE), true)
            }
        }
        val firstRun = prefs.getBoolean(FIRST_RUN_SHARED_PREFS_KEY, true)
        if(firstRun){
            val intent = Intent(this, FirstRunStepOneActivity::class.java)
            startActivity(intent)
        }else{
            setContentView(R.layout.activity_main)

            mDrawerLayout = findViewById(R.id.drawer_layout)

            val viewPager = findViewById<ViewPager>(R.id.main_frame)
            setUpViewPager(viewPager)

            val navigationView = findViewById<NavigationView>(R.id.nav_view)
            navigationView.setNavigationItemSelectedListener { menuItem ->
                menuItem.isChecked = true

                when(menuItem.itemId){

                    R.id.navigation_schedule -> {
                        viewPager.currentItem = 0
                        setTitle(R.string.schedule)
                    }
                    R.id.navigation_search -> {
                        viewPager.currentItem = 1
                        setTitle(R.string.search)
                    }
                    R.id.navigation_notes -> {
                        viewPager.currentItem = 2
                        setTitle(R.string.notes)
                    }
                    R.id.navigation_moodle -> {
                        viewPager.currentItem = 3
                        setTitle(R.string.courses)
                    }
//TODO                    R.id.navigation_messages -> {
//                        viewPager.currentItem = 4
//                        setTitle(R.string.messages)
//                    }
//                    R.id.navigation_events -> {
//                        viewPager.currentItem = 5
//                        setTitle(R.string.events)
//                    }
                    R.id.navigation_settings -> {
//                        TODO viewPager.currentItem = 6
                        viewPager.currentItem = 4
                        setTitle(R.string.settings)
                    }
                    R.id.navigation_hide_ads -> {
                        loadRewardAd()
                    }

                }
                mDrawerLayout.closeDrawers()

                true
            }
            navigationView.menu.getItem(0).isChecked = true

            navigationView.menu.findItem(R.id.navigation_hide_ads).isVisible = !(prefs.getBoolean(getString(R.string.PREFS_PREMIUM_PURCHASED), false) || isHideAdsEnabled(this))

            viewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener{
                override fun onPageScrollStateChanged(state: Int) {}

                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

                override fun onPageSelected(position: Int) {
                    when(position){
                        0 -> setTitle(R.string.schedule)
                        1 -> setTitle(R.string.search)
                        2 -> setTitle(R.string.notes)
                        3 -> setTitle(R.string.courses)
                        4 -> setTitle(R.string.settings)
//TODO                        4 -> setTitle(R.string.messages)
//TODO                        5 -> setTitle(R.string.events)
//TODO                        6 -> setTitle(R.string.settings)
                    }
                    navigationView.menu.getItem(position).isChecked = true
                }

            })

            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.ic_menu)
            }

            if(isDeviceOnline(this) &&
                    !prefs.getBoolean(getString(R.string.PREFS_PREMIUM_PURCHASED), false) &&
                    !Utils.isHideAdsEnabled(this)){
                MobileAds.initialize(this, "") //TODO supply adMob app id
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
                mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
                mRewardedVideoAd.rewardedVideoAdListener = HideAdsListener(this, mRewardedVideoAd, navigationView, adView)
            }
            if(prefs.getBoolean(getString(R.string.PREFS_APP_NOT_RATED), true)){
                val ratingCounter = prefs.getInt(getString(R.string.PREFS_APP_RATING_DIALOG_COUNTER), 20) - 1
                prefs.edit {
                    putInt(getString(R.string.PREFS_APP_RATING_DIALOG_COUNTER), ratingCounter)
                }
                if(ratingCounter == 0){
                    AlertDialog.Builder(this)
                            .setTitle(getString(R.string.do_you_like_this_app))
                            .setMessage(getString(R.string.app_rating_message))
                            .setNeutralButton(getString(R.string.maybe_later), null)
                            .setNegativeButton(getString(R.string.no_thanks)) { _, _ -> prefs.edit { putBoolean(getString(R.string.PREFS_APP_NOT_RATED), false) } }
                            .setPositiveButton(getString(R.string.sure_take_me_there)) { _, _ ->

                                prefs.edit {
                                    putBoolean(getString(R.string.PREFS_APP_NOT_RATED), false)
                                }

                                var marketFound = false
                                val rateIntent = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
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
                                    startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri()))
                                }
                            }
                            .create()
                            .show()
                    prefs.edit {
                        putInt(getString(R.string.PREFS_APP_RATING_DIALOG_COUNTER), 20)
                    }
                }
            }
            bp = BillingProcessor.newBillingProcessor(
                    this,
                    "",//TODO supply license key from google play
                    BillingHandler(this, this)
            )
            bp?.initialize()
            if(!prefs.getBoolean(getString(R.string.PREFS_PREMIUM_PURCHASED), false) &&
                    bp?.getPurchaseTransactionDetails(getString(R.string.student_uek_premium_item_id))!=null){
                prefs.edit {
                    putBoolean(getString(R.string.PREFS_PREMIUM_PURCHASED), true)
                }
            }
            if(prefs.getBoolean(getString(R.string.PREFS_PIGEON_TOPICS_SET), false)){
                subscribeToTopics(this)
            }
            if(intent.getBooleanExtra(getString(R.string.EXTRA_OPEN_MESSAGES), false)){
                viewPager.currentItem = 4
                setTitle(R.string.messages)
                navigationView.menu.getItem(4).isChecked = true
            }
        }
    }

    private fun setUpViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        val scheduleFragment = ScheduleFragment.newInstance()
        val searchFragment = SearchFragment.newInstance()
        val notesFragment = NotesFragment.newInstance()
        val moodleFragment = MoodleFragment.newInstance()
        val messagesFragment = MessagesFragment.newInstance()
        val eventsFragment = EventsFragment.newInstance()
        val settingsFragment = SettingsFragment.newInstance()
        adapter.addFragment(scheduleFragment)
        adapter.addFragment(searchFragment)
        adapter.addFragment(notesFragment)
        adapter.addFragment(moodleFragment)
//TODO        adapter.addFragment(messagesFragment)
//TODO        adapter.addFragment(eventsFragment)
        adapter.addFragment(settingsFragment)
        viewPager.adapter = adapter
        viewPager.currentItem = 0
        setTitle(R.string.schedule)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(bp != null && !bp!!.handleActivityResult(requestCode, resultCode, data)){
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        if(bp != null){
            bp?.release()
        }
        super.onDestroy()
    }

    override fun onBackPressed(){
        val webView = findViewById<WebView>(R.id.moodle_web_view)
        if(webView != null && webView.visibility == View.VISIBLE){
            webView.visibility = View.GONE
        }else{
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                if (!mDrawerLayout.isDrawerOpen(R.id.drawer_layout)){
                    mDrawerLayout.openDrawer(GravityCompat.START)
                }else{
                    mDrawerLayout.closeDrawers()
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadRewardAd(){
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if(!prefs.getBoolean(getString(R.string.PREFS_VIDEO_ADD_MESSAGE_SHOWN), false)){
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.hide_ads))
                    .setMessage(getString(R.string.support_creators_msg))
                    .setPositiveButton(getString(R.string.buy_us_a_beer)){dialog, _ ->
                        prefs.edit {
                            putBoolean(getString(R.string.PREFS_VIDEO_ADD_MESSAGE_SHOWN), true)
                        }
                        dialog.dismiss()
                    }
                    .show()
        }
        mRewardedVideoAd.loadAd("ca-app-pub-4145044771989791/5285223374",//TODO supply video ad id
                AdRequest.Builder().build())
    }

}
