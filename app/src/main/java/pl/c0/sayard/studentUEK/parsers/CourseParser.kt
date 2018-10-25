package pl.c0.sayard.studentUEK.parsers

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.google.common.io.ByteStreams
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.data.Course
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

class CourseParser(private val progressBar: ProgressBar, private val onTaskCompleted: OnTaskCompleted, private val parseFromUsos: Boolean, val prefs: SharedPreferences, val context: Context?): AsyncTask<String, Void, List<Course>>(){

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
        if(parseFromUsos) {
            val username = prefs.getString(context?.getString(R.string.PREFS_MOODLE_LOGIN), "")
            val password = prefs.getString(context?.getString(R.string.PREFS_MOODLE_PASSWORD), "")
            val urlString = "http://pigeon.dev.uek.krakow.pl:8000/moodleCoursesParser/$username/$password"
            val url = URL(urlString)
            val urlConnection = url.openConnection() as HttpURLConnection
            return try{
                val inputStream = BufferedInputStream(urlConnection.inputStream)
                val result = String(ByteStreams.toByteArray(inputStream), Charsets.UTF_8)
                urlConnection.disconnect()
                val mainObject = JSONObject(result)
                val coursesArray = mainObject.getJSONArray("courses")
                val courseList = mutableListOf<Course>()
                for (i in 0..(coursesArray.length() - 1)) {
                    val course = coursesArray.getJSONObject(i)
                    courseList.add(Course(course.getInt("id"), course.getString("name"), ""))
                }
                courseList
            }catch(e: UnknownHostException){
                emptyList()
            }catch(e: Exception){
                Log.v("USOS_COURSES_EXCEPTION", e.printStackTrace().toString())
                emptyList()
            }
        }else{
            val token = params[0]
            val urlString = "https://e-uczelnia.uek.krakow.pl/webservice/rest/server.php?wstoken=$token&wsfunction=mod_assign_get_assignments"
            val courseList = mutableListOf<Course>()
            return try {
                val url = URL(urlString)
                val factory = XmlPullParserFactory.newInstance()
                factory.isNamespaceAware = false
                val xpp = factory.newPullParser()
                xpp.setInput(getInputStream(url), "UTF_8")
                var eventType = xpp.eventType
                var idTemp: String? = null
                var fullNameTemp: String? = null
                var shortNameTemp: String? = null
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.name.equals(KEY_TAG, true)) {
                            when {
                                xpp.getAttributeValue(null, NAME_ATTRIBUTE) == ID_NAME_ATTRIBUTE_VALUE -> {
                                    repeat(2) {
                                        xpp.next()
                                    }
                                    idTemp = xpp.text
                                }
                                xpp.getAttributeValue(null, NAME_ATTRIBUTE) == FULLNAME_NAME_ATTRIBUTE_VALUE -> {
                                    repeat(2) {
                                        xpp.next()
                                    }
                                    fullNameTemp = xpp.text
                                }
                                xpp.getAttributeValue(null, NAME_ATTRIBUTE) == SHORTNAME_NAME_ATTRIBUTE_VALUE -> {
                                    repeat(2) {
                                        xpp.next()
                                    }
                                    shortNameTemp = xpp.text
                                }
                            }
                            if (idTemp != null && fullNameTemp != null && shortNameTemp != null) {
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
            } catch (e: Exception) {
                emptyList()
            }
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