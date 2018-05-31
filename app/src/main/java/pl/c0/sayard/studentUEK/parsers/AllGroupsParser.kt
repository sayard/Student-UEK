package pl.c0.sayard.studentUEK.parsers

import android.os.AsyncTask
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import pl.c0.sayard.studentUEK.data.Group
import java.net.URL

class AllGroupsParser(private val onTaskCompleted: OnTaskCompleted): AsyncTask<Void, Void, List<Group>>() {

    private val GROUP_URL = "http://planzajec.uek.krakow.pl/index.php?xml&typ=G"
    private val RESOURCE_TAG = "zasob"
    private val NAME_ATTRIBUTE = "nazwa"
    private val ID_ATTRIBUTE = "id"

    interface OnTaskCompleted{
        fun onTaskCompleted(result: List<Group>?)
    }

    override fun doInBackground(vararg p0: Void?): List<Group> {
        val groupList = mutableListOf<Group>()
        try {
            val url = URL(GROUP_URL)
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val xpp = factory.newPullParser()
            xpp.setInput(url.openConnection().getInputStream(), "UTF_8")
            var eventType = xpp.eventType
            while(eventType != XmlPullParser.END_DOCUMENT){
                if(eventType == XmlPullParser.START_TAG){
                    if(xpp.name.equals(RESOURCE_TAG, true)){
                        val name = xpp.getAttributeValue(null, NAME_ATTRIBUTE)
                        val id = xpp.getAttributeValue(null, ID_ATTRIBUTE).toInt()
                        val group = Group(id, name)
                        groupList.add(group)
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
        onTaskCompleted.onTaskCompleted(result)
    }
}