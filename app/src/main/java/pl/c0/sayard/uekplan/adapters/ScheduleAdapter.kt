package pl.c0.sayard.uekplan.adapters

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.data.ScheduleItem
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by karol on 11.01.18.
 */
class ScheduleAdapter(var context: Context, var scheduleListOriginal:List<ScheduleItem>) : BaseAdapter(), Filterable {

    private var scheduleListDisplay = scheduleListOriginal
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ScheduleViewHolder
        val scheduleItemObj: ScheduleItem = scheduleListDisplay[position]
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
            if(scheduleItemObj.comments == ""){
                vh.scheduleLineFour?.visibility = View.GONE
            }
            if(scheduleItemObj.noteId == -1 && scheduleItemObj.noteContent == ""){
                vh.noteIconTextView?.visibility = View.GONE
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
        if(scheduleItemObj.noteId != -1 && scheduleItemObj.noteContent != ""){
            vh.noteIconTextView?.visibility = View.VISIBLE
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
        val noteIconTextView = row?.findViewById<TextView>(R.id.schedule_note_icon)
    }

    override fun getItem(position: Int): Any {
        return scheduleListDisplay[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return scheduleListDisplay.size
    }

    fun changeAdapterData(scheduleList:List<ScheduleItem>){
        scheduleListOriginal = scheduleList
        scheduleListDisplay = scheduleListOriginal
    }

    override fun getFilter(): Filter {
        return object: Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filteredList = mutableListOf<ScheduleItem>()
                if(scheduleListOriginal == null){
                    scheduleListOriginal = mutableListOf()
                }

                if(constraint == null || constraint.isEmpty()){
                    results.count = scheduleListOriginal.size
                    results.values = scheduleListOriginal
                }else{
                    val constraintLowerCase = constraint.toString().toLowerCase()
                    scheduleListOriginal.map {
                        val dataSubject = it.subject
                        val dataType = it.type
                        val dataTeacher = it.teacher
                        val dataClassroom = it.classroom
                        val dateFormatShort = SimpleDateFormat("yyyy-MM-dd HH:mm")
                        if(dataSubject.toLowerCase().contains(constraintLowerCase) ||
                           dataType.toLowerCase().contains(constraintLowerCase) ||
                           dataTeacher.toLowerCase().contains(constraintLowerCase) ||
                           dataClassroom.toLowerCase().contains(constraintLowerCase)){
                            filteredList.add(ScheduleItem(
                                    it.subject,
                                    it.type,
                                    it.teacher,
                                    it.teacherId,
                                    it.classroom,
                                    it.comments,
                                    it.dateStr,
                                    dateFormatShort.format(it.startDate),
                                    dateFormatShort.format(it.endDate),
                                    it.isFirstOnTheDay,
                                    isCustom = it.isCustom,
                                    customId = it.customId,
                                    noteId = it.noteId,
                                    noteContent = it.noteContent
                            ))
                        }
                    }
                    results.count = filteredList.size
                    results.values = filteredList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                scheduleListDisplay = results?.values as List<ScheduleItem>
                for(i in 0 until scheduleListDisplay.size){
                    val scheduleItem = scheduleListDisplay[i]
                    if(i==0){
                        scheduleItem.isFirstOnTheDay = true
                    }else{
                        val previousScheduleItem = scheduleListDisplay[i-1]
                        if(scheduleItem.dateStr != previousScheduleItem.dateStr){
                            scheduleItem.isFirstOnTheDay = true
                        }
                    }
                }
                notifyDataSetChanged()
            }

        }
    }
}
