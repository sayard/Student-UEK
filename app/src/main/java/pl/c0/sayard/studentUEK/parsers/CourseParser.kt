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

class CourseParser(private val progressBar: ProgressBar, private val onTaskCompleted: OnTaskCompleted, val prefs: SharedPreferences, val context: Context?): AsyncTask<String, Void, List<Course>>(){


    interface OnTaskCompleted{
        fun onTaskCompleted(result: List<Course>?)
    }

    override fun onPreExecute() {
        progressBar.visibility = View.VISIBLE
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String?): List<Course> {
        val username = prefs.getString(context?.getString(R.string.PREFS_MOODLE_LOGIN), "")
        val password = prefs.getString(context?.getString(R.string.PREFS_MOODLE_PASSWORD), "")
        val urlString = "http://35.246.253.130:8000/moodleCoursesParser/$username/$password"
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
    }

    override fun onPostExecute(result: List<Course>?) {
        super.onPostExecute(result)
        progressBar.visibility = View.GONE
        onTaskCompleted.onTaskCompleted(result)
    }

}