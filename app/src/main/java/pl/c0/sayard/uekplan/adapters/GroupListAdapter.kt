package pl.c0.sayard.uekplan.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import pl.c0.sayard.uekplan.Group
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.parsers.GroupParser

/**
 * Created by karol on 29.12.17.
 */
class GroupListAdapter(context: Context, activity: Activity, getLanguageGroups: Boolean) : BaseAdapter(), Filterable {

    private var groupListOriginal = GroupParser(activity, getLanguageGroups).execute().get()
    private var groupListDisplay = groupListOriginal
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getItem(position: Int): Any {
        return groupListDisplay[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return groupListDisplay.size
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

        vh.tv.text = groupListDisplay[position].name
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

                if(groupListOriginal == null){
                    groupListOriginal = mutableListOf()
                }

                if(constraint == null || constraint.isEmpty()){
                    results.count = groupListOriginal.size
                    results.values = groupListOriginal
                }else{
                    val constraintLowerCase = constraint.toString().toLowerCase()
                    for(i in 0 until groupListOriginal.size){
                        val groupObject = groupListOriginal[i]
                        val data = groupObject.name
                        if(data.toLowerCase().contains(constraintLowerCase)){
                            filteredList.add(Group(groupObject.id, groupObject.name))
                        }
                    }
                    results.count = filteredList.size
                    results.values = filteredList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                groupListDisplay = results?.values as List<Group>?
                notifyDataSetChanged()
            }
        }
    }
}
