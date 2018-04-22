package pl.c0.sayard.studentUEK.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils.Companion.removeFilteredLesson
import pl.c0.sayard.studentUEK.data.FilteredLesson

class FilteredLessonsAdapter(val context: Context, private val filteredLessonsList: MutableList<FilteredLesson>): BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: FilteredLessonsAdapter.ListRowHolder

        if(convertView == null){
            view = this.inflater.inflate(R.layout.filtered_lesson_row, parent, false)
            vh = FilteredLessonsAdapter.ListRowHolder(view)
            view.tag = vh
        }else{
            view = convertView
            vh = view.tag as FilteredLessonsAdapter.ListRowHolder
        }
        val filteredListItem = filteredLessonsList[position]
        vh.subject?.text = filteredListItem.subject
        vh.type?.text = filteredListItem.type
        val dayAndHourText = "${filteredListItem.dayOfWeek} ${filteredListItem.startHour}"
        vh.dayAndHour?.text = dayAndHourText
        vh.removeButton?.setOnClickListener{
            removeFilteredLesson(filteredListItem.id, context)
            filteredLessonsList.removeAt(position)
            notifyDataSetChanged()
        }
        return view
    }

    private class ListRowHolder(row: View?){
        val subject = row?.findViewById<TextView>(R.id.filtered_lesson_row_subject)
        val removeButton = row?.findViewById<TextView>(R.id.filtered_lesson_remove)
        val type = row?.findViewById<TextView>(R.id.filtered_lesson_row_type)
        val dayAndHour = row?.findViewById<TextView>(R.id.filtered_lesson_day_of_week_and_hour)
    }

    override fun getItem(position: Int): Any {
        return filteredLessonsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return filteredLessonsList.count()
    }
}