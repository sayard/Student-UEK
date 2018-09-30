package pl.c0.sayard.studentUEK.adapters

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.edit
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.data.Group
import pl.c0.sayard.studentUEK.db.DatabaseManager

/**
 * Created by karol on 29.12.17.
 */
class GroupListAdapter(val context: Context, var groupListOriginal: List<Group>, private val isFirstRunConfig:Boolean = true) : BaseAdapter(), Filterable {

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
        if(!isFirstRunConfig){
            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when(which){
                    DialogInterface.BUTTON_POSITIVE -> {
                        val isLanguageGroup = groupListDisplay[position].name.toLowerCase().startsWith("cj-")
                        val dbManager = DatabaseManager(context)
                        val newId = dbManager.addGroupToDb(groupListDisplay[position], isLanguageGroup)
                        if(newId == -1L){
                            Toast.makeText(context, context.getString(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
                        }else{
                            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                            prefs.edit {
                                putBoolean(context.getString(R.string.PREFS_REFRESH_SCHEDULE), true)
                            }
                        }
                        (context as Activity).finish()
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {}
                }
            }
            vh.tv.setOnClickListener {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("${context.getString(R.string.do_you_want_to_add_group)} ${groupListDisplay[position].name}?")
                        .setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(context.getString(R.string.no), dialogClickListener)
                        .show()
            }
        }
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
                groupListDisplay = results?.values as List<Group>
                notifyDataSetChanged()
            }
        }
    }
}
