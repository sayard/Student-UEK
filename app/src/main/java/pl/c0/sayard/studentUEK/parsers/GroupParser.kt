package pl.c0.sayard.studentUEK.parsers

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import pl.c0.sayard.studentUEK.data.Group
import pl.c0.sayard.studentUEK.R
import java.io.InputStream
import java.net.URL

/**
 * Created by karol on 29.12.17.
 */
class GroupParser(@SuppressLint("StaticFieldLeak") private val activity: Activity, private val getLanguageGroups: Boolean, private val onTaskCompleted: OnTaskCompleted) : AsyncTask<Void, Void, List<Group>>() {

    private val GROUP_URL = "http://planzajec.uek.krakow.pl/index.php?xml&typ=G"
    private val RESOURCE_TAG = "zasob"
    private val NAME_ATTRIBUTE = "nazwa"
    private val ID_ATTRIBUTE = "id"
    private val SJO_PREFIX = "SJO"
    @SuppressLint("StaticFieldLeak")
    private var progressBar: ProgressBar? = null
    @SuppressLint("StaticFieldLeak")
    private var list: ListView? = null

    interface OnTaskCompleted{
        fun onTaskCompleted(result: List<Group>?, activity: Activity)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        if(getLanguageGroups){
            progressBar = activity.findViewById(R.id.second_step_progress_bar)
            list = activity.findViewById<ListView>(R.id.language_group_list_view)
        }else{
            progressBar = activity.findViewById(R.id.first_step_progress_bar)
            list = activity.findViewById<ListView>(R.id.group_list_view)
        }
        progressBar?.visibility = View.VISIBLE
        list?.visibility = View.GONE
    }

    override fun doInBackground(vararg p0: Void?): List<Group> {
        val groupList = mutableListOf<Group>()
        try {
            val url = URL(GROUP_URL)
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val xpp = factory.newPullParser()
            xpp.setInput(getInputStream(url), "UTF_8")
            var eventType = xpp.eventType
            while(eventType != XmlPullParser.END_DOCUMENT){
                if(eventType == XmlPullParser.START_TAG){
                    if(xpp.name.equals(RESOURCE_TAG, true)){
                        val name = xpp.getAttributeValue(null, NAME_ATTRIBUTE)
                        var predicate = !name.startsWith(SJO_PREFIX)
                        if(getLanguageGroups){
                            predicate = name.startsWith(SJO_PREFIX)
                        }
                        if(predicate){
                            val id = xpp.getAttributeValue(null, ID_ATTRIBUTE).toInt()
                            val group = Group(id, name)
                            groupList.add(group)
                        }
                    }
                }
                eventType = xpp.next()
            }
            return groupList
        }catch (e: Exception){
            Log.v("GROUP_PARSER_EXCEPTION", e.toString())
            return emptyList()
        }
    }

    override fun onPostExecute(result: List<Group>?) {
        super.onPostExecute(result)
        onTaskCompleted.onTaskCompleted(result, activity)
        progressBar?.visibility = View.GONE
        list?.visibility = View.VISIBLE
    }

    private fun getInputStream(url: URL): InputStream{
        return url.openConnection().getInputStream()
    }

}
