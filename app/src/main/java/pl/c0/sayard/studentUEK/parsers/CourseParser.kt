package pl.c0.sayard.studentUEK.parsers

import android.os.AsyncTask
import android.view.View
import android.widget.ProgressBar
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import pl.c0.sayard.studentUEK.data.Course
import java.io.InputStream
import java.net.URL

class CourseParser(private val progressBar: ProgressBar, private val onTaskCompleted: OnTaskCompleted): AsyncTask<String, Void, List<Course>>(){

    private val KEY_TAG = "KEY"
    private val NAME_ATTRIBUTE = "name"
    private val ID_NAME_ATTRIBUTE_VALUE = "id"
    private val FULLNAME_NAME_ATTRIBUTE_VALUE = "fullname"
    private val SHORTNAME_NAME_ATTRIBUTE_VALUE = "shortname"

    interface OnTaskCompleted{
        fun onTaskCompleted(result: List<Course>?)
    }

    override fun onPreExecute() {
        progressBar.visibility = View.VISIBLE
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String?): List<Course> {
        val token = params[0]
        val urlString = "https://e-uczelnia.uek.krakow.pl/webservice/rest/server.php?wstoken=$token&wsfunction=mod_assign_get_assignments"
        val courseList = mutableListOf<Course>()
        return try{
            val url = URL(urlString)
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val xpp = factory.newPullParser()
            xpp.setInput(getInputStream(url), "UTF_8")
            var eventType = xpp.eventType
            var idTemp:String? = null
            var fullNameTemp: String? = null
            var shortNameTemp: String? = null
            while(eventType != XmlPullParser.END_DOCUMENT){
                if(eventType == XmlPullParser.START_TAG){
                    if(xpp.name.equals(KEY_TAG, true)){
                        when {
                            xpp.getAttributeValue(null, NAME_ATTRIBUTE) == ID_NAME_ATTRIBUTE_VALUE -> {
                                repeat(2){
                                    xpp.next()
                                }
                                idTemp = xpp.text
                            }
                            xpp.getAttributeValue(null, NAME_ATTRIBUTE) == FULLNAME_NAME_ATTRIBUTE_VALUE -> {
                                repeat(2){
                                    xpp.next()
                                }
                                fullNameTemp = xpp.text
                            }
                            xpp.getAttributeValue(null, NAME_ATTRIBUTE) == SHORTNAME_NAME_ATTRIBUTE_VALUE -> {
                                repeat(2){
                                    xpp.next()
                                }
                                shortNameTemp = xpp.text
                            }
                        }
                        if(idTemp != null && fullNameTemp != null && shortNameTemp != null){
                            courseList.add(Course(idTemp.toInt(), fullNameTemp, shortNameTemp))
                            idTemp = null
                            fullNameTemp = null
                            shortNameTemp = null
                        }
                    }
                }
                eventType = xpp.next()
            }

            return courseList
        }catch(e: Exception){
            emptyList()
        }

    }

    override fun onPostExecute(result: List<Course>?) {
        super.onPostExecute(result)
        progressBar.visibility = View.GONE
        onTaskCompleted.onTaskCompleted(result)
    }

    private fun getInputStream(url: URL): InputStream {
        return url.openConnection().getInputStream()
    }
}