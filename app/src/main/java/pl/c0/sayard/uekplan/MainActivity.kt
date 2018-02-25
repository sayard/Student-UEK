package pl.c0.sayard.uekplan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import pl.c0.sayard.uekplan.Utils.Companion.FIRST_RUN_SHARED_PREFS_KEY
import pl.c0.sayard.uekplan.fragments.NotesFragment
import pl.c0.sayard.uekplan.fragments.ScheduleFragment
import pl.c0.sayard.uekplan.fragments.SearchFragment
import pl.c0.sayard.uekplan.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private var menu: Menu? = null

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val selectedFragment: Any
        val transaction = supportFragmentManager.beginTransaction()
        val addNewScheduleItemMenuItem = menu?.findItem(R.id.new_schedule_item)
        when(item.itemId){
            R.id.navigation_search ->{
                selectedFragment = SearchFragment.newInstance()
                setTitle(R.string.search)
                addNewScheduleItemMenuItem?.isVisible = false
            }
            R.id.navigation_schedule -> {
                selectedFragment = ScheduleFragment.newInstance()
                setTitle(R.string.schedule)
                addNewScheduleItemMenuItem?.isVisible = true
            }
            R.id.navigation_notes -> {
                selectedFragment = NotesFragment.newInstance()
                setTitle(R.string.notes)
                addNewScheduleItemMenuItem?.isVisible = false
            }
            R.id.navigation_settings -> {
                selectedFragment = SettingsFragment.newInstance()
                setTitle(R.string.settings)
                addNewScheduleItemMenuItem?.isVisible = false
            }
            else -> return@OnNavigationItemSelectedListener false
        }
        transaction.replace(R.id.main_frame, selectedFragment as android.support.v4.app.Fragment?)
        transaction.commit()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("pl.c0.sayard.uekplan", Context.MODE_PRIVATE)
        val firstRun = prefs.getBoolean(FIRST_RUN_SHARED_PREFS_KEY, true)
        if(firstRun){
            val intent = Intent(this, FirstRunStepOneActivity::class.java)
            startActivity(intent)
        }else{
            setContentView(R.layout.activity_main)
            navigation.selectedItemId = R.id.navigation_schedule
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
            val transaction = supportFragmentManager.beginTransaction()
            setTitle(R.string.schedule)
            val scheduleFragment = ScheduleFragment.newInstance()
            transaction.replace(R.id.main_frame, scheduleFragment)
            transaction.commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.schedule_menu, menu)
        this.menu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.new_schedule_item -> {
                val newLessonIntent = Intent(this, AddLessonActivity::class.java)
                startActivity(newLessonIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
