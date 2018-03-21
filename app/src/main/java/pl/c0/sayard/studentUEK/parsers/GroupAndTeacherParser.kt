package pl.c0.sayard.studentUEK.parsers

import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import pl.c0.sayard.studentUEK.Utils.Companion.isDeviceOnline
import pl.c0.sayard.studentUEK.data.Group
import pl.c0.sayard.studentUEK.fragments.SearchFragment
import java.io.InputStream
import java.net.URL

class GroupAndTeacherParser(private val fragment: SearchFragment, private val progressBar: ProgressBar, private val list: ListView, private val onTaskCompleted: OnTaskCompleted): AsyncTask<Void,Void,List<Group>>(){

    private val GROUP_URL = "http://planzajec.uek.krakow.pl/index.php?xml&typ=G"
    private val TEACHER_URL = "http://planzajec.uek.krakow.pl/index.php?xml&typ=N"
    private val RESOURCE_TAG = "zasob"
    private val NAME_ATTRIBUTE = "nazwa"
    private val ID_ATTRIBUTE = "id"
    interface OnTaskCompleted{
        fun onTaskCompleted(result: List<Group>?,fragment: SearchFragment)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        progressBar.visibility = View.VISIBLE
        list.visibility = View.GONE
    }

    override fun doInBackground(vararg p0: Void?): List<Group> {
        val groupAndTeacherList = mutableListOf<Group>()
        try{
            if(isDeviceOnline(fragment.context)){
                for(url in listOf(GROUP_URL, TEACHER_URL)){
                    val parserUrl = URL(url)
                    val factory = XmlPullParserFactory.newInstance()
                    factory.isNamespaceAware = false
                    val xpp = factory.newPullParser()
                    xpp.setInput(getInputStream(parserUrl), "UTF_8")
                    var eventType = xpp.eventType
                    while(eventType != XmlPullParser.END_DOCUMENT){
                        if(eventType == XmlPullParser.START_TAG){
                            if(xpp.name.equals(RESOURCE_TAG, true)){
                                val name = xpp.getAttributeValue(null, NAME_ATTRIBUTE)
                                val id = xpp.getAttributeValue(null, ID_ATTRIBUTE).toInt()
                                val group = if(url == TEACHER_URL){
                                    Group(id, name, "N")
                                }else{
                                    Group(id, name)
                                }
                                groupAndTeacherList.add(group)
                            }
                        }
                        eventType = xpp.next()
                    }
                }
            }
            return groupAndTeacherList
        }catch (e: Exception){
            Log.v("GT_PARSER_EXCEPTION", e.toString())
            return emptyList()
        }
    }

    override fun onPostExecute(result: List<Group>?) {
        super.onPostExecute(result)
        onTaskCompleted.onTaskCompleted(result, fragment)
        progressBar.visibility = View.GONE
        list.visibility = View.VISIBLE
    }

    private fun getInputStream(url: URL): InputStream{
        return url.openConnection().getInputStream()
    }

}
