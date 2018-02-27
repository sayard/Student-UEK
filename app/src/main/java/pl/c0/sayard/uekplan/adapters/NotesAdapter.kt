package pl.c0.sayard.uekplan.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.data.Note
import pl.c0.sayard.uekplan.db.ScheduleContract
import pl.c0.sayard.uekplan.db.ScheduleDbHelper

/**
 * Created by karol on 27.02.18.
 */
class NotesAdapter(private val context: Context, private val notesList: MutableList<Note>) : BaseAdapter() {

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
                        notesList.removeAt(positionToDelete!!)
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
        return notesList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return notesList.size
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
        vh.title?.text = notesList[position].title
        vh.content?.text = notesList[position].content
        val dateAndHourText = "${notesList[position].dateStr} ${notesList[position].hourStr}"
        vh.dateAndHour?.text = dateAndHourText
        vh.content?.text = notesList[position].content
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
}
