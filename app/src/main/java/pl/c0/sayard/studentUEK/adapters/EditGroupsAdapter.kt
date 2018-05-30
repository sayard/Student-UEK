package pl.c0.sayard.studentUEK.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.data.ScheduleGroup

class EditGroupsAdapter(val context: Context, private val groups: List<ScheduleGroup>): BaseAdapter() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ListRowHolder
        if(convertView == null){
            view = this.mInflater.inflate(R.layout.edit_groups_row, parent, false)
            vh = ListRowHolder(view)
            view.tag = vh
        }else{
            view = convertView
            vh = view.tag as ListRowHolder
        }
        vh.groupName.text = groups[position].name
        vh.groupType.text = getGroupType(groups[position].name)
        return view
    }

    private fun getGroupType(name: String): String{
        if(name.toLowerCase().startsWith("sjo")){
            return context.getString(R.string.language_group)
        }
        return context.getString(R.string.group)
    }

    private class ListRowHolder(row: View?){
        val groupName: TextView = row?.findViewById<TextView>(R.id.edit_groups_group_name) as TextView
        val groupType: TextView = row?.findViewById<TextView>(R.id.edit_groups_group_type) as TextView
    }

    override fun getItem(position: Int): Any {
        return groups[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return groups.size
    }
}