package pl.c0.sayard.uekplan

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import pl.c0.sayard.uekplan.Utils.Companion.FIRST_RUN_SHARED_PREFS_KEY
import pl.c0.sayard.uekplan.fragments.NotesFragment
import pl.c0.sayard.uekplan.fragments.ScheduleFragment
import pl.c0.sayard.uekplan.fragments.SearchFragment
import pl.c0.sayard.uekplan.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val selectedFragment: Any
        val transaction = supportFragmentManager.beginTransaction()
        selectedFragment = when {
            item.itemId == R.id.navigation_search -> SearchFragment.newInstance()
            item.itemId == R.id.navigation_schedule -> ScheduleFragment.newInstance()
            item.itemId == R.id.navigation_notes -> NotesFragment.newInstance()
            item.itemId == R.id.navigation_settings -> SettingsFragment.newInstance()
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
            transaction.replace(R.id.main_frame, ScheduleFragment.newInstance())
            transaction.commit()
        }
    }
}
