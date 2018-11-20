package pl.c0.sayard.studentUEK.parsers

import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import com.google.common.io.ByteStreams
import org.json.JSONException
import org.json.JSONObject
import pl.c0.sayard.studentUEK.data.Event
import java.io.BufferedInputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class EventsParser(val progressBar: ProgressBar?, private val onTaskCompleted: OnTaskCompleted): AsyncTask<Void, Void, List<Event>?>() {

    private val EVENTS_URL = "https://pigeon.dev.uek.krakow.pl/getEvents"
    private val EVENTS_KEY = "events"
    private val EVENT_NAME_KEY = "name"
    private val EVENT_DESC_KEY = "description"
    private val EVENT_START_DATE_KEY = "startDate"
    private val EVENT_END_DATE_KEY = "endDate"
    private val EVENT_FB_KEY = "fb"
    private val EVENT_PROMOTION_LEVEL_KEY = "promotionLevel"

    interface OnTaskCompleted{
        fun onTaskCompleted(result: List<Event>?)
    }

    override fun onPreExecute() {
        progressBar?.visibility = View.VISIBLE
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: Void?): List<Event>? {
        val url = URL(EVENTS_URL)
        val urlConnection = url.openConnection() as HttpURLConnection
        return try{
            val inputStream = BufferedInputStream(urlConnection.inputStream)
            val result = String(ByteStreams.toByteArray(inputStream), Charsets.UTF_8)
            urlConnection.disconnect()
            parseJSON(result)
        }catch(e: Exception){
            Log.v("EVENT_PARSER_EXCEPTION", e.printStackTrace().toString())
            null
        }
    }

    override fun onPostExecute(result: List<Event>?) {
        super.onPostExecute(result)
        progressBar?.visibility = View.GONE
        onTaskCompleted.onTaskCompleted(result)
    }

    private fun parseJSON(jsonString: String): List<Event>{
        val mainObject = JSONObject(jsonString)
        return try{
            val eventsArray = mainObject.getJSONArray(EVENTS_KEY)
            val eventsList = mutableListOf<Event>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale("pl"))
            for (i in 0..(eventsArray.length() - 1)){
                val eventObject = eventsArray.getJSONObject(i)
                eventsList.add(
                        Event(
                                eventObject.getString(EVENT_NAME_KEY),
                                eventObject.getString(EVENT_DESC_KEY),
                                dateFormat.parse(eventObject.getString(EVENT_START_DATE_KEY)),
                                dateFormat.parse(eventObject.getString(EVENT_END_DATE_KEY)),
                                eventObject.getString(EVENT_FB_KEY),
                                eventObject.getInt(EVENT_PROMOTION_LEVEL_KEY)
                        )
                )
            }
            eventsList
        }catch(e: JSONException){
            emptyList()
        }
    }
}