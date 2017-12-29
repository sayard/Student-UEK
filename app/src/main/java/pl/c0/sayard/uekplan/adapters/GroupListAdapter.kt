package pl.c0.sayard.uekplan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.parsers.GroupParser

/**
 * Created by karol on 29.12.17.
 */
class GroupListAdapter(context: Context) : BaseAdapter() {

    private var sList = GroupParser().execute().get()
    private val mInflator: LayoutInflater = LayoutInflater.from(context)

    override fun getItem(position: Int): Any {
        return sList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return sList.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ListRowHolder
        if(convertView == null){
            view = this.mInflator.inflate(R.layout.list_row, parent, false)
            vh = ListRowHolder(view)
            view.tag = vh
        }else{
            view = convertView
            vh = view.tag as ListRowHolder
        }

        vh.tv.text = sList[position]
        return view
    }

    private class ListRowHolder(row: View?){
        val tv: TextView = row?.findViewById<TextView>(R.id.list_row_text_view) as TextView
    }
}
