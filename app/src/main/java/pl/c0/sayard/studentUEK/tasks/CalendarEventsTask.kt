package pl.c0.sayard.studentUEK.tasks

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.model.*
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import pl.c0.sayard.studentUEK.activities.ActivateGoogleCalendarIntegrationActivity
import pl.c0.sayard.studentUEK.data.Building
import pl.c0.sayard.studentUEK.db.ScheduleDbHelper
import pl.c0.sayard.studentUEK.jobs.RefreshScheduleJob
import java.io.IOException

/**
 * Created by karol on 05.03.18.
 */
class CalendarEventsTask(credential: GoogleAccountCredential, val instance: Activity?, val context: Context) : AsyncTask<Void, Void, Unit>() {

    private var service: Calendar? = null
    private var lastError: Exception? = null

    init {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        service = Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName(context.getString(R.string.app_name))
                .build()
    }

    override fun doInBackground(vararg params: Void?) {
        try{
            insertCalendarEvents()
        }catch (e: Exception){
            lastError = e
            cancel(true)
        }
        instance?.finish()
    }

    @Throws(IOException::class)
    private fun insertCalendarEvents(){
        val dbHelper = ScheduleDbHelper(context)
        val db = dbHelper.readableDatabase
        val cursor = Utils.getScheduleCursor(db)
        val scheduleList = Utils.getScheduleList(cursor, db)
        db.close()
        if(scheduleList.isNotEmpty()){
            for(i in 0 until scheduleList.size){
                val scheduleItem = scheduleList[i]
                val startDateTime = DateTime(scheduleItem.startDate)
                val endDateTime = DateTime(scheduleItem.endDate)
                if(isFree(startDateTime, endDateTime)){
                    val buildingInstance = Building(context)
                    val locationLatLng = buildingInstance.getBuildingLatLng(scheduleItem.classroom)
                    val event = Event()
                            .setSummary("${scheduleItem.subject} - ${scheduleItem.type}")
                            .setLocation("${locationLatLng?.latitude}, ${locationLatLng?.longitude}")
                            .setDescription("${context.getString(R.string.classroom)}: ${scheduleItem.classroom}\n${context.getString(R.string.teacher)}: ${scheduleItem.teacher}")
                    val start = EventDateTime()
                            .setDateTime(startDateTime)
                            .setTimeZone("Europe/Warsaw")
                    event.start = start
                    val end = EventDateTime()
                            .setDateTime(endDateTime)
                            .setTimeZone("Europe/Warsaw")
                    event.end = end
                    val reminderOverrides = arrayListOf<EventReminder>()
                    val reminders = Event.Reminders()
                            .setUseDefault(false)
                            .setOverrides(reminderOverrides)
                    event.reminders = reminders
                    val calendarId = "primary"
                    service?.events()?.insert(calendarId, event)?.execute()
                }
            }
        }
    }

    private fun isFree(startDateTime: DateTime, endDateTime: DateTime): Boolean{
        val request = FreeBusyRequest()
        request.timeMin = startDateTime
        request.timeMax = endDateTime
        request.timeZone = java.util.Calendar.getInstance().timeZone.toString()
        val item = FreeBusyRequestItem()
        item.id = "primary"
        val itemList = mutableListOf(item)
        request.items = itemList
        val query = service?.freebusy()?.query(request)
        val response = query?.execute()
        return (response?.calendars?.get("primary")?.get("busy") as List<*>).isEmpty()
    }

    override fun onCancelled() {
        super.onCancelled()
        if(lastError != null){
            when (lastError) {
                is GooglePlayServicesAvailabilityIOException -> {
                    val apiAvailability = GoogleApiAvailability.getInstance()
                    val dialog = apiAvailability.getErrorDialog(
                            instance,
                            (lastError as GooglePlayServicesAvailabilityIOException).connectionStatusCode,
                            ActivateGoogleCalendarIntegrationActivity.Constants.REQUEST_GOOGLE_PLAY_SERVICES)
                    dialog.show()
                }
                is UserRecoverableAuthIOException -> {
                    instance?.startActivityForResult(
                            (lastError as UserRecoverableAuthIOException).intent,
                            ActivateGoogleCalendarIntegrationActivity.Constants.REQUEST_AUTHORIZATION
                    )
                    Thread{
                        kotlin.run {
                            RefreshScheduleJob.refreshSchedule(context)
                        }
                    }.start()
                }
                else ->{} //fail silently
            }
        }else{
            //fail silently
        }
    }

}