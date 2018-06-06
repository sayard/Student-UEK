package pl.c0.sayard.studentUEK.jobs

import android.content.Context
import android.preference.PreferenceManager
import androidx.core.content.edit
import com.evernote.android.job.DailyJob
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import pl.c0.sayard.studentUEK.Utils.Companion.isDeviceOnline
import pl.c0.sayard.studentUEK.db.DatabaseManager
import pl.c0.sayard.studentUEK.parsers.ScheduleParser
import pl.c0.sayard.studentUEK.tasks.CalendarEventsTask
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
        const val TAG = "REFRESH_SCHEDULE_TAG"
        fun schedule(){
            DailyJob.schedule(
                    JobRequest.Builder(TAG),
                    TimeUnit.HOURS.toMillis(1),
                    TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(30)
            )
        }

        fun refreshSchedule(context: Context){
            val dbManager = DatabaseManager(context)
            val urls = mutableListOf<String>()
            val groups = dbManager.getGroups()
            groups.mapTo(urls) { it.url }
            val languageGroups = dbManager.getLanguageGroups()
            languageGroups.mapTo(urls) { it.url }
            ScheduleParser(context, null, null, null, null,null, null).execute(urls).get()
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit {
                putBoolean(context.getString(R.string.PREFS_REFRESH_SCHEDULE), true)
            }
            if(prefs.getBoolean(context.getString(R.string.PREFS_ENABLE_GC), false)){
                exportScheduleToGC(context)
            }
            if(prefs.getBoolean(context.getString(R.string.PREFS_REFRESH_SCHEDULE), false)){
                val notificationTimeThreshold = prefs.getInt(context.getString(R.string.PREFS_NOTIFICATION_TIME_THRESHOLD), -1)
                if(notificationTimeThreshold != -1){
                    setScheduleNotifications(notificationTimeThreshold, context)
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

        private fun setScheduleNotifications(notificationTimeThreshold:Int, context: Context){
            cancelScheduleNotifications()
            val dbManager = DatabaseManager(context)
            val scheduleList = dbManager.getScheduleList()
            if(scheduleList.isNotEmpty()){
                for(i in 0 until scheduleList.size){
                    val scheduleItem = scheduleList[i]
                    val calendar = Calendar.getInstance()
                    calendar.time = scheduleItem.startDate
                    val hourStr = String.format("%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
                    calendar.add(Calendar.MINUTE, notificationTimeThreshold*-1)
                    if(calendar.timeInMillis > System.currentTimeMillis()){
                        val timeInMillis = Math.abs(calendar.timeInMillis - System.currentTimeMillis())
                        val extras = Utils.getScheduleItemExtras(context, scheduleItem, hourStr)
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