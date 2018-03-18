package pl.c0.sayard.studentUEK.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.data.Note
import pl.c0.sayard.studentUEK.db.ScheduleContract
import pl.c0.sayard.studentUEK.db.ScheduleDbHelper

/**
 * Created by karol on 27.02.18.
 */
class NotesAdapter(private val context: Context, private var notesListOriginal: MutableList<Note>) : BaseAdapter(), Filterable {

    private var notesListDisplay = notesListOriginal
    private var positionToDelete: Int? = null
    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
        when(which){
            DialogInterface.BUTTON_POSITIVE ->{
                try{
                    val dbHelper = ScheduleDbHelper(context)
                    val db = dbHelper.readableDatabase
                    val deleteCount = db.delete(ScheduleContract.NotesEntry.TABLE_NAME,
                            "${ScheduleContract.NotesEntry._ID} = ${(getItem(positionToDelete!!) as Note).id}",
                            null)
                    if(deleteCount > 0){
                        notesListDisplay.removeAt(positionToDelete!!)
                        notifyDataSetChanged()
                    }else{
                        Toast.makeText(context, context.getString(R.string.note_delete_error), Toast.LENGTH_SHORT).show()
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(context, context.getString(R.string.note_delete_error), Toast.LENGTH_SHORT).show()
                }

                return@OnClickListener
            }
            DialogInterface.BUTTON_NEGATIVE -> return@OnClickListener
        }
    }

    override fun getItem(position: Int): Any {
        return notesListDisplay[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return notesListDisplay.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ListRowHolder
        if(convertView == null){
            view = this.mInflater.inflate(R.layout.notes_row, parent, false)
            vh = ListRowHolder(view)
            view.tag = vh
        }else{
            view = convertView
            vh = view.tag as ListRowHolder
        }
        vh.title?.text = notesListDisplay[position].title
        vh.content?.text = notesListDisplay[position].content
        val dateAndHourText = "${notesListDisplay[position].dateStr} ${notesListDisplay[position].hourStr}"
        vh.dateAndHour?.text = dateAndHourText
        vh.content?.text = notesListDisplay[position].content
        vh.deleteButton?.setOnClickListener {
            positionToDelete = position
            val builder = AlertDialog.Builder(context)
            builder.setMessage(context.getString(R.string.note_delete_message))
                    .setPositiveButton(context.getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(context.getString(R.string.no), dialogClickListener)
                    .show()
        }
        return view
    }

    private class ListRowHolder(row: View?){
        val title = row?.findViewById<TextView>(R.id.notes_row_title)
        val content = row?.findViewById<TextView>(R.id.notes_row_content)
        val dateAndHour = row?.findViewById<TextView>(R.id.notes_row_date_and_hour)
        val deleteButton = row?.findViewById<Button>(R.id.notes_row_delete_button)
    }

    override fun getFilter(): Filter {
        return object: Filter(){

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filteredList = mutableListOf<Note>()

                if(notesListOriginal == null){
                    notesListOriginal = mutableListOf()
                }

                if(constraint == null || constraint == ""){
                    results.count = notesListOriginal.size
                    results.values = notesListOriginal
                }else{
                    val constraintLowerCase = constraint.toString().toLowerCase()
                    for(i in 0 until notesListOriginal.size){
                        val noteObject = notesListOriginal[i]
                        val dataTitle = noteObject.title.toLowerCase()
                        val dataContent = noteObject.content.toLowerCase()
                        if(dataTitle.contains(constraintLowerCase)
                                || dataContent.contains(constraintLowerCase)){
                            filteredList.add(Note(
                                    noteObject.id,
                                    noteObject.title,
                                    noteObject.content,
                                    noteObject.dateStr,
                                    noteObject.hourStr
                            ))
                        }
                    }
                    results.count = filteredList.size
                    results.values = filteredList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notesListDisplay = results?.values as MutableList<Note>
                notifyDataSetChanged()
            }

        }
    }
}
