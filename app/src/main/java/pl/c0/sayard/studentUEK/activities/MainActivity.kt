package pl.c0.sayard.studentUEK.activities

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import pl.c0.sayard.studentUEK.Utils.Companion.FIRST_RUN_SHARED_PREFS_KEY
import pl.c0.sayard.studentUEK.adapters.ViewPagerAdapter
import pl.c0.sayard.studentUEK.fragments.NotesFragment
import pl.c0.sayard.studentUEK.fragments.ScheduleFragment
import pl.c0.sayard.studentUEK.fragments.SearchFragment
import pl.c0.sayard.studentUEK.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private var prevMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                when(item.itemId){
                    R.id.navigation_search ->{
                        viewPager.currentItem = 0
                        setTitle(R.string.search)
                    }
                    R.id.navigation_schedule -> {
                        viewPager.currentItem = 1
                        setTitle(R.string.schedule)
                    }
                    R.id.navigation_notes -> {
                        viewPager.currentItem = 2
                        setTitle(R.string.notes)
                    }
                    R.id.navigation_settings -> {
                        viewPager.currentItem = 3
                        setTitle(R.string.settings)
                    }
                    else -> return@OnNavigationItemSelectedListener false
                }
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
        }
    }

    private fun setUpViewPager(viewPager: ViewPager){
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

}
