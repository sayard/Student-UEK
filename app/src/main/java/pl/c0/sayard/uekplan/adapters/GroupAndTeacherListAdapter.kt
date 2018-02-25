package pl.c0.sayard.uekplan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.data.Group

class GroupAndTeacherListAdapter(context: Context, var groupAndTeacherListOriginal: List<Group>): BaseAdapter(), Filterable{

    private var groupAndTeacherListDisplay = groupAndTeacherListOriginal
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItem(position: Int): Any {
        return groupAndTeacherListDisplay[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return groupAndTeacherListDisplay.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ListRowHolder
        if(convertView == null){
            view = this.mInflater.inflate(R.layout.list_row, parent, false)
            vh = ListRowHolder(view)
            view.tag = vh
        }else{
            view = convertView
            vh = view.tag as ListRowHolder
        }
        vh.tv.text = groupAndTeacherListDisplay[position].name
        return view
    }

    private class ListRowHolder(row: View?){
        val tv: TextView = row?.findViewById<TextView>(R.id.list_row_text_view) as TextView
    }

    override fun getFilter(): Filter {
        return object: Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filteredList = mutableListOf<Group>()
                if(groupAndTeacherListOriginal == null){
                    groupAndTeacherListOriginal = mutableListOf()
                }

                if(constraint == null || constraint.isEmpty()){
                    results.count = groupAndTeacherListOriginal.size
                    results.values = groupAndTeacherListOriginal
                }else{
                    val constraintLowerCase = constraint.toString().toLowerCase()
                    groupAndTeacherListOriginal.map {
                        val data = it.name
                        if(it.name.toLowerCase().contains(constraintLowerCase)){
                            filteredList.add(Group(it.id, it.name, it.type))
                        }
                    }
                    results.count = filteredList.size
                    results.values = filteredList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
               groupAndTeacherListDisplay = results?.values as List<Group>
                notifyDataSetChanged()
            }

        }
    }
}
