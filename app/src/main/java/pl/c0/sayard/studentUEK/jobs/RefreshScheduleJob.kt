package pl.c0.sayard.studentUEK.jobs

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import pl.c0.sayard.studentUEK.Utils.Companion.isDeviceOnline
import pl.c0.sayard.studentUEK.activities.ActivateGoogleCalendarIntegrationActivity
import pl.c0.sayard.studentUEK.data.Lesson
import pl.c0.sayard.studentUEK.db.ScheduleDbHelper
import pl.c0.sayard.studentUEK.parsers.ScheduleParser
import pl.c0.sayard.studentUEK.tasks.CalendarEventsTask
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Created by karol on 08.03.18.
 */
class RefreshScheduleJob: DailyJob() {


    override fun onRunDailyJob(params: Params): DailyJobResult {
        val countDownLatch = CountDownLatch(1)
        Thread{
            kotlin.run {
                refreshSchedule(context)
                countDownLatch.countDown()
            }
        }.start()
        try{
            countDownLatch.await()
        }catch (ignored: InterruptedException){}
        return DailyJobResult.SUCCESS
    }

    companion object {
        private val CALENDAR_ERROR_NOTIFICATION_ID = 200
        const val TAG = "REFRESH_SCHEDULE_TAG"
        fun schedule(){
            DailyJob.schedule(
                    JobRequest.Builder(TAG),
                    TimeUnit.HOURS.toMillis(1),
                    TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(30)
            )
        }

        fun refreshSchedule(context: Context){
            val dbHelper = ScheduleDbHelper(context)
            val db = dbHelper.readableDatabase
            val urls = mutableListOf<String>()
            val groups = Utils.getGroups(db)
            groups.mapTo(urls) { it.url }
            val languageGroups = Utils.getLanguageGroups(db)
            languageGroups.mapTo(urls) { it.url }
            val lessonList = ScheduleParser(context, null, null, null, null, null).execute(urls).get()
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val editor = prefs.edit()
            editor.putBoolean(context.getString(R.string.PREFS_REFRESH_SCHEDULE), true)
            editor.apply()
            dbHelper.close()
            db.close()
            if(prefs.getBoolean(context.getString(R.string.PREFS_ENABLE_GC), false)){
                exportScheduleToGC(context)
            }
            if(prefs.getBoolean(context.getString(R.string.PREFS_REFRESH_SCHEDULE), false)){
                val notificationTimeThreshold = prefs.getInt(context.getString(R.string.PREFS_NOTIFICATION_TIME_THRESHOLD), -1)
                if(notificationTimeThreshold != -1){
                    setScheduleNotifications(lessonList, notificationTimeThreshold, context)
                }else{
                    cancelScheduleNotifications()
                }

            }
        }

        private fun exportScheduleToGC(context: Context){
            val credential = GoogleAccountCredential.usingOAuth2(
                    context.applicationContext, mutableListOf(CalendarScopes.CALENDAR))
                    .setBackOff(ExponentialBackOff())
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val accountName = prefs.getString(context.getString(R.string.PREFS_ACCOUNT_NAME), null)
            credential.selectedAccountName = accountName
            if(isDeviceOnline(context) && isGooglePlayServicesAvailable(context) && credential.selectedAccountName != null){
                CalendarEventsTask(credential, null, context).execute()
            }
        }

        private fun isGooglePlayServicesAvailable(context: Context): Boolean{
            val apiAvailability = GoogleApiAvailability.getInstance()
            val connectionStatusCode =
                    apiAvailability.isGooglePlayServicesAvailable(context)
            return connectionStatusCode == ConnectionResult.SUCCESS
        }

        private fun setScheduleNotifications(lessonList: List<Lesson>?, notificationTimeThreshold:Int, context: Context){
            cancelScheduleNotifications()
            if(lessonList != null && lessonList.isNotEmpty()){
                for(i in 0 until lessonList.size){
                    val lesson = lessonList[i]
                    val time = lesson.startHour.split(':')
                    var hour = time[0].trim().toInt()
                    var minute = time[1].trim().toInt().minus(notificationTimeThreshold)
                    if(minute < 0){
                        hour = hour.minus(1)
                        minute = minute.plus(60)
                    }
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("PL", "pl"))
                    val date = dateFormat.parse(lesson.date)
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    if(calendar.timeInMillis > System.currentTimeMillis()){
                        val timeInMillis = Math.abs(calendar.timeInMillis - System.currentTimeMillis())
                        val extras = PersistableBundleCompat()
                        extras.putString(context.getString(R.string.EXTRA_NOTIFICATION_SUBJECT), lesson.subject)
                        extras.putString(context.getString(R.string.EXTRA_NOTIFICATION_CLASSROOM), lesson.classroom)
                        extras.putString(context.getString(R.string.EXTRA_NOTIFICATION_HOUR), lesson.startHour)
                        NotificationJob.schedule(timeInMillis, extras)
                    }
                }
            }
        }

        fun cancelScheduleNotifications(){
            JobManager.instance().cancelAllForTag(NotificationJob.TAG)
        }
    }
}