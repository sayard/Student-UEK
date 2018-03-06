package pl.c0.sayard.uekplan.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import com.github.pwittchen.swipe.library.rx2.Swipe
import com.github.pwittchen.swipe.library.rx2.SwipeListener
import kotlinx.android.synthetic.main.activity_main.*
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.Utils
import pl.c0.sayard.uekplan.Utils.Companion.FIRST_RUN_SHARED_PREFS_KEY
import pl.c0.sayard.uekplan.fragments.NotesFragment
import pl.c0.sayard.uekplan.fragments.ScheduleFragment
import pl.c0.sayard.uekplan.fragments.SearchFragment
import pl.c0.sayard.uekplan.fragments.SettingsFragment

class MainActivity : AppCompatActivity() {
    private val swipe = Swipe(20, 420)

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val selectedFragment: Any
        val transaction = supportFragmentManager.beginTransaction()
        when(item.itemId){
            R.id.navigation_search ->{
                selectedFragment = SearchFragment.newInstance()
                setTitle(R.string.search)
            }
            R.id.navigation_schedule -> {
                selectedFragment = ScheduleFragment.newInstance()
                setTitle(R.string.schedule)
            }
            R.id.navigation_notes -> {
                selectedFragment = NotesFragment.newInstance()
                setTitle(R.string.notes)
            }
            R.id.navigation_settings -> {
                selectedFragment = SettingsFragment.newInstance()
                setTitle(R.string.settings)
            }
            else -> return@OnNavigationItemSelectedListener false
        }
        transaction.replace(R.id.main_frame, selectedFragment as android.support.v4.app.Fragment?)
        transaction.commit()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
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
            swipe.setListener(object: SwipeListener{
                override fun onSwipedUp(event: MotionEvent?): Boolean {
                    return false
                }

                override fun onSwipedDown(event: MotionEvent?): Boolean {
                    return false
                }

                override fun onSwipingUp(event: MotionEvent?) {
                    return
                }

                override fun onSwipingLeft(event: MotionEvent?) {
                    return
                }

                override fun onSwipingRight(event: MotionEvent?) {
                    return
                }

                override fun onSwipingDown(event: MotionEvent?) {
                    return
                }

                override fun onSwipedRight(event: MotionEvent?): Boolean {
                    navigation.selectedItemId = Utils.getSwipeFragmentId(navigation.selectedItemId, getString(R.string.right_swipe), this@MainActivity)
                    return true
                }

                override fun onSwipedLeft(event: MotionEvent?): Boolean {
                    navigation.selectedItemId = Utils.getSwipeFragmentId(navigation.selectedItemId, getString(R.string.left_swipe), this@MainActivity)
                    return true
                }

            })
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        swipe.dispatchTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

}
