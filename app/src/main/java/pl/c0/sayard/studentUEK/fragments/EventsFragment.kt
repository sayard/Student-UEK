package pl.c0.sayard.studentUEK.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.CalendarView
import pl.c0.sayard.studentUEK.R
import android.view.animation.TranslateAnimation
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import pl.c0.sayard.studentUEK.adapters.EventsAdapter
import pl.c0.sayard.studentUEK.data.Event
import pl.c0.sayard.studentUEK.parsers.EventsParser
import java.util.*


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
        val calendar = Calendar.getInstance()
        calendarView?.minDate = calendar.time.time

        val progressBar = view?.findViewById<ProgressBar>(R.id.events_progress_bar)
        val couldNotLoadTextView = view?.findViewById<TextView>(R.id.could_not_load_events_text_view)
        EventsParser(progressBar, object: EventsParser.OnTaskCompleted{
            override fun onTaskCompleted(result: List<Event>?) {
                couldNotLoadTextView?.visibility = View.GONE
                if(result != null){
                    val noEventsTextView = view?.findViewById<TextView>(R.id.no_events_text_view)
                    val adapter = EventsAdapter(context, result, noEventsTextView)

                    calendarView?.setOnDateChangeListener { _, year, month, dayOfMonth ->
                        val constraint = if(dayOfMonth<10){
                            "0$dayOfMonth-${month+1}-$year"
                        }else{
                            "$dayOfMonth-${month+1}-$year"
                        }
                        adapter.filter.filter(constraint)
                    }
                    val eventsListView = view?.findViewById<ListView>(R.id.events_list_view)
                    eventsListView?.adapter = adapter
                    adapter.filter.filter("")
                }else{
                    couldNotLoadTextView?.visibility = View.VISIBLE
                }
            }

        }).execute()

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
            R.id.refresh_events -> {
                val ft = activity?.supportFragmentManager?.beginTransaction()
                ft?.detach(this)
                ft?.attach(this)
                ft?.commit()
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
