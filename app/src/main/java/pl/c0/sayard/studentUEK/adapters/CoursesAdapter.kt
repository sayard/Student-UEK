package pl.c0.sayard.studentUEK.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.data.Course

class CoursesAdapter(private val context: Context, private var courseListOriginal: List<Course>): BaseAdapter(), Filterable{

    private var courseListDisplay = courseListOriginal
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = this.mInflater.inflate(R.layout.course_row, parent, false)
        val vh: ListRowHolder
        vh = ListRowHolder(view)
        vh.fullName?.text = courseListDisplay[position].fullName
        return view
    }

    private class ListRowHolder(row: View?){
        val fullName = row?.findViewById<TextView>(R.id.course_row_title)
    }

    override fun getItem(position: Int): Any {
        return courseListDisplay[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return courseListDisplay.size
    }

    override fun getFilter(): Filter {
        return object: Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filteredList = mutableListOf<Course>()


                if(constraint == null || constraint == ""){
                    results.count = courseListOriginal.size
                    results.values = courseListOriginal
                }else{
                    val constraintLowerCase = constraint.toString().toLowerCase()
                    for(i in 0 until courseListOriginal.size){
                        val courseObject = courseListOriginal[i]
                        val dataFullName = courseObject.fullName.toLowerCase()
                        val dataShortName = courseObject.shortName.toLowerCase()
                        if(dataFullName.contains(constraintLowerCase)
                            || dataShortName.contains(constraintLowerCase)){
                            filteredList.add(Course(
                                    courseObject.id,
                                    courseObject.fullName,
                                    courseObject.shortName
                            ))
                        }
                    }
                    results.count = filteredList.size
                    results.values = filteredList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                courseListDisplay = results?.values as MutableList<Course>
                notifyDataSetChanged()
            }

        }
    }
}