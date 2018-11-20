package pl.c0.sayard.studentUEK.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.data.Event
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class EventsAdapter(private val context: Context?, private var eventsListOriginal: List<Event>, private val noEventsTextView: TextView?):BaseAdapter(), Filterable {

    private var eventsListDisplay = eventsListOriginal
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ListRowHolder
        if(convertView == null){
            view = this.mInflater.inflate(R.layout.events_row, parent, false)
            vh = ListRowHolder(view)
            view.tag = vh
        }else{
            view = convertView
            vh = view.tag as ListRowHolder
        }
        vh.name?.text = eventsListDisplay[position].name
        vh.desc?.text = eventsListDisplay[position].description
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale("pl"))
        vh.dateSpan?.text = "${dateFormat.format(eventsListDisplay[position].startDate)} - ${dateFormat.format(eventsListDisplay[position].endDate)}"
        vh.wrapper?.setOnClickListener {
            val intent = try{
                val packageManager = context?.packageManager
                packageManager?.getPackageInfo("com.facebook.katana", 0)
                val versionCode = packageManager?.getPackageInfo("com.facebook.katana", 0)?.versionCode;
                if (versionCode != null && versionCode >= 3002850) {
                    Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=${eventsListDisplay[position].fb}"))
                } else {
                    val eventId = eventsListDisplay[position].fb.split("/").last()
                    Intent(Intent.ACTION_VIEW, Uri.parse("fb://events/$eventId"))
                }
            }catch (e: Exception){
                Intent(Intent.ACTION_VIEW, Uri.parse(eventsListDisplay[position].fb))
            }
            context?.startActivity(intent)
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(eventsListDisplay[position].fb))
            context?.startActivity(browserIntent)
        }
        return view
    }

    private class ListRowHolder(row: View?){
        val name = row?.findViewById<TextView>(R.id.events_row_name)
        val desc = row?.findViewById<TextView>(R.id.events_row_desc)
        val dateSpan = row?.findViewById<TextView>(R.id.events_row_date_span)
        val wrapper = row?.findViewById<LinearLayout>(R.id.events_row_wrapper)
    }

    override fun getItem(position: Int): Any {
        return eventsListDisplay[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return eventsListDisplay.size
    }

    override fun getFilter(): Filter {
        return object: Filter(){

            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                val filteredList = mutableListOf<Event>()

                if(constraint == null || constraint == ""){
                    results.count = eventsListOriginal.size
                    results.values = eventsListOriginal
                }else{
                    for(i in 0 until eventsListOriginal.size){
                        val eventObject = eventsListOriginal[i]
                        if(eventObject.startsOnDate(constraint.toString())){
                            filteredList.add(eventObject)
                        }
                    }
                    results.count = filteredList.size
                    results.values = filteredList
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                eventsListDisplay = results?.values as List<Event>
                if(eventsListDisplay.isEmpty()){
                    noEventsTextView?.visibility = View.VISIBLE
                }else{
                    noEventsTextView?.visibility = View.GONE
                }
                notifyDataSetChanged()
            }

        }
    }
}