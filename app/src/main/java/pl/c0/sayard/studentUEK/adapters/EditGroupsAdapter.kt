package pl.c0.sayard.studentUEK.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.data.ScheduleGroup
import pl.c0.sayard.studentUEK.db.DatabaseManager

class EditGroupsAdapter(val context: Context, val activity: AppCompatActivity, private val groups: List<ScheduleGroup>): BaseAdapter() {

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
        val dialogClickListener = DialogInterface.OnClickListener { _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> {
                    val isLanguageGroup = getGroupType(groups[position].name) == context.getString(R.string.language_group)
                    val dbManager = DatabaseManager(context)
                    val deleteCount = dbManager.removeGroupByName(groups[position].name, isLanguageGroup)
                    if (deleteCount != null && deleteCount > 0){
                        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                        prefs.edit().putBoolean(context.getString(R.string.PREFS_REFRESH_SCHEDULE), true).apply()
                        activity.finish()
                    }else{
                        Toast.makeText(context, context.getString(R.string.group_delete_error), Toast.LENGTH_LONG).show()
                    }
                }
                DialogInterface.BUTTON_NEGATIVE -> {}
            }
        }
        vh.removeGroup.setOnClickListener{
            val builder = AlertDialog.Builder(context)
            builder.setMessage("${context.getString(R.string.do_you_want_to_remove_group)} ${groups[position].name} ?")
                    .setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(context.getString(R.string.no), dialogClickListener)
                    .show()
        }
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
        val removeGroup: TextView = row?.findViewById<TextView>(R.id.edit_groups_remove) as TextView
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