package pl.c0.sayard.studentUEK.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.data.Message
import pl.c0.sayard.studentUEK.db.DatabaseManager

class MessagesAdapter(var context: Context, var activity: FragmentActivity?, var fragment: Fragment?, var messagesListOriginal:MutableList<Message>):BaseAdapter(), Filterable {

    private var messagesListDisplay = messagesListOriginal
    private var mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: MessageViewHolder
        val messageObj: Message = messagesListDisplay[position]
        if(convertView == null){
            view = this.mInflater.inflate(R.layout.message_row, parent, false)
            vh = MessageViewHolder(view)
            view.tag = vh
        }else{
            view = convertView
            vh = view.tag as MessageViewHolder
        }
        vh.messageTitleAuthorTv?.text = "${messageObj.title} - ${messageObj.author}"
        vh.messageBodyTv?.text = messageObj.body
        vh.messageDateTv?.text = messageObj.date
        vh.messageGroupsTv?.text = messageObj.groups
        vh.messageDeleteButton?.setOnClickListener {

            val dialogClickListener = DialogInterface.OnClickListener { _, which ->
                when(which){
                    DialogInterface.BUTTON_POSITIVE -> {
                        val dbManager = DatabaseManager(context)
                        val deleteCount = dbManager.removeMessageFromDb(messageObj.id)
                        if(deleteCount<0){
                            Toast.makeText(context, context.getString(R.string.msg_delete_error), Toast.LENGTH_SHORT).show()
                        }else{
                            messagesListDisplay.removeAt(position)
                            notifyDataSetChanged()
                        }
                    }
                    DialogInterface.BUTTON_NEGATIVE -> {}
                }
            }

            AlertDialog.Builder(context)
                    .setMessage(context.getString(R.string.msg_delete_question))
                    .setPositiveButton(R.string.yes, dialogClickListener)
                    .setNegativeButton(R.string.no, dialogClickListener)
                    .show()

        }
        return view
    }

    private class MessageViewHolder(row: View?){
        val messageTitleAuthorTv = row?.findViewById<TextView>(R.id.messages_row_title_author)
        val messageDeleteButton = row?.findViewById<Button>(R.id.messages_row_delete_button)
        val messageBodyTv = row?.findViewById<TextView>(R.id.messages_row_body)
        val messageDateTv = row?.findViewById<TextView>(R.id.messages_row_date)
        val messageGroupsTv = row?.findViewById<TextView>(R.id.messages_row_groups)
    }

    override fun getItem(position: Int): Any {
        return messagesListDisplay[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return messagesListDisplay.size
    }

    override fun getFilter(): Filter {
        return object: Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filteredList = mutableListOf<Message>()

                if(constraint == null || constraint.isEmpty()){
                    results.count = messagesListOriginal.size
                    results.values = messagesListOriginal
                }else{
                    val constraintLowerCase = constraint.toString().toLowerCase()
                    for(i in 0 until messagesListOriginal.size){
                        val messageObj = messagesListOriginal[i]
                        if(messageObj.title.toLowerCase().contains(constraintLowerCase) ||
                                messageObj.body.toLowerCase().contains(constraintLowerCase) ||
                                messageObj.author.toLowerCase().contains(constraintLowerCase) ||
                                messageObj.groups.toLowerCase().contains(constraintLowerCase)){
                            filteredList.add(
                                    Message(
                                            messageObj.title,
                                            messageObj.body,
                                            messageObj.author,
                                            messageObj.date,
                                            messageObj.groups
                                    )
                            )
                        }
                    }
                    results.count = filteredList.size
                    results.values = filteredList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                messagesListDisplay = results?.values as MutableList<Message>
                notifyDataSetChanged()
            }

        }
    }


}