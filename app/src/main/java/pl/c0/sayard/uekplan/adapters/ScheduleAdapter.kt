package pl.c0.sayard.uekplan.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.data.ScheduleItem
import java.util.*

/**
 * Created by karol on 11.01.18.
 */
class ScheduleAdapter(var context: Context, scheduleList:List<ScheduleItem>) : BaseAdapter() {


    private var scheduleOriginal: List<ScheduleItem>? = null
    private var scheduleDisplay: List<ScheduleItem>? = null
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    init {
        scheduleOriginal = scheduleList
        scheduleDisplay = scheduleOriginal
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ScheduleViewHolder
        val scheduleItemObj: ScheduleItem = scheduleDisplay!![position]
        if(convertView == null){
            view = this.mInflater.inflate(R.layout.schedule_row, parent, false)
            vh = ScheduleViewHolder(view)
            view.tag = vh
        }else{
            view = convertView
            vh = view.tag as ScheduleViewHolder
            if(!scheduleItemObj.isFirstOnTheDay){
                view.findViewById<TextView>(R.id.schedule_day_tv).visibility = View.GONE
            }
        }
        val calendar = scheduleItemObj.calendar
        val dayString =
                "${scheduleItemObj.dayOfTheWeekStr}, ${scheduleItemObj.dateStr}"
        vh.scheduleDayTv?.text = dayString
        when(calendar.get(Calendar.DAY_OF_WEEK)){
            Calendar.MONDAY -> vh.scheduleDayTv?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorMonday))
            Calendar.TUESDAY -> vh.scheduleDayTv?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTuesday))
            Calendar.WEDNESDAY -> vh.scheduleDayTv?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWednesday))
            Calendar.THURSDAY -> vh.scheduleDayTv?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorThursday))
            Calendar.FRIDAY -> vh.scheduleDayTv?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorFriday))
            Calendar.SATURDAY -> vh.scheduleDayTv?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSaturday))
            Calendar.SUNDAY -> vh.scheduleDayTv?.setBackgroundColor(ContextCompat.getColor(context, R.color.colorSunday))
        }
        if(scheduleItemObj.isFirstOnTheDay){
            vh.scheduleDayTv?.visibility = View.VISIBLE
        }

        vh.scheduleSubjectTv?.text = scheduleItemObj.subject
        calendar.time = scheduleItemObj.startDate
        var hoursString = String.format("%02d:%02d-", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        calendar.time = scheduleItemObj.endDate
        hoursString += String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        vh.scheduleHoursTv?.text = hoursString
        vh.scheduleTypeTv?.text = scheduleItemObj.type
        vh.scheduleTeacherTv?.text = scheduleItemObj.teacher
        vh.scheduleClassroomTv?.text = scheduleItemObj.classroom
        if(scheduleItemObj.comments != ""){
            vh.scheduleLineFour?.visibility = View.VISIBLE
            vh.scheduleCommentsTv?.text = scheduleItemObj.comments
        }
        return view
    }

    private class ScheduleViewHolder(row: View?){
        val scheduleDayTv = row?.findViewById<TextView>(R.id.schedule_day_tv)
        val scheduleSubjectTv = row?.findViewById<TextView>(R.id.schedule_subject_tv)
        val scheduleHoursTv = row?.findViewById<TextView>(R.id.schedule_hours_tv)
        val scheduleTypeTv = row?.findViewById<TextView>(R.id.schedule_type_tv)
        val scheduleTeacherTv = row?.findViewById<TextView>(R.id.schedule_teacher_tv)
        val scheduleClassroomTv = row?.findViewById<TextView>(R.id.schedule_classroom_tv)
        val scheduleLineFour = row?.findViewById<LinearLayout>(R.id.schedule_line_four)
        val scheduleCommentsTv = row?.findViewById<TextView>(R.id.schedule_comments_tv)
    }

    override fun getItem(position: Int): Any {
        return scheduleOriginal!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return scheduleOriginal!!.size
    }

    fun changeAdapterData(scheduleList:List<ScheduleItem>){
        scheduleOriginal = scheduleList
        scheduleDisplay = scheduleOriginal
    }
}
