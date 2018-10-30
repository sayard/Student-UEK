package pl.c0.sayard.studentUEK.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.CalendarView
import pl.c0.sayard.studentUEK.R
import android.view.animation.TranslateAnimation


class EventsFragment : Fragment() {
    private var viewUp = true

    companion object {
        fun newInstance(): EventsFragment{
            return EventsFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_events, container, false)
        setHasOptionsMenu(true)
        return view
    }

    override fun onResume() {
        viewUp = true
        val calendarView = view?.findViewById<CalendarView>(R.id.events_calendar)
        calendarView?.post {
            animateCalendarUp(0)
        }
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.events_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.toggle_events_calendar -> {
                if (!viewUp){
                    animateCalendarUp(500)
                    item.icon = ContextCompat.getDrawable(context!!, R.drawable.ic_drop_down_24dp)

                }else{
                    animateCalendarDown(500)
                    item.icon = ContextCompat.getDrawable(context!!, R.drawable.ic_drop_up_24dp)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun animateCalendarDown(duration: Long){
        val calendarView = view?.findViewById<CalendarView>(R.id.events_calendar)
        if(calendarView != null){
            val calendarAnimation = TranslateAnimation(
                    0f,
                    0f,
                    -calendarView.height.toFloat(),
                    0f)
            calendarAnimation.duration = duration
            calendarAnimation.fillAfter = true

            calendarView.visibility = View.VISIBLE
            calendarView.startAnimation(calendarAnimation)
            viewUp = false
        }
    }

    private fun animateCalendarUp(duration: Long){
        val calendarView = view?.findViewById<CalendarView>(R.id.events_calendar)
        if(calendarView != null){
            val calendarAnimation = TranslateAnimation(
                    0f,
                    0f,
                    0f,
                    -calendarView.height.toFloat())
            calendarAnimation.duration = duration
            calendarAnimation.fillAfter = true

            calendarView.startAnimation(calendarAnimation)
            calendarView.postDelayed(Runnable {
                calendarView.visibility = View.GONE
            }, duration)
            viewUp = true
        }
    }

}
