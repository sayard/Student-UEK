package pl.c0.sayard.uekplan.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.activities.ActivateGoogleCalendarIntegrationActivity
import pl.c0.sayard.uekplan.tasks.CalendarEventsTask

/**
 * Created by karol on 05.03.18.
 */
class GoogleCalendarTaskReceiver: BroadcastReceiver() {

    private val CALENDAR_ERROR_NOTIFICATION_ID = 200

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val credential = GoogleAccountCredential.usingOAuth2(
                    context.applicationContext, mutableListOf(CalendarScopes.CALENDAR))
                    .setBackOff(ExponentialBackOff())
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val accountName = prefs.getString(context.getString(R.string.PREFS_ACCOUNT_NAME), null)
            credential.selectedAccountName = accountName
            if(isDeviceOnline(context)){
                if(isGooglePlayServicesAvailable(context) && credential.selectedAccountName != null){
                    CalendarEventsTask(credential, null, context).execute()
                }
            }else{
                val gcIntent = Intent(context, ActivateGoogleCalendarIntegrationActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        gcIntent,
                        PendingIntent.FLAG_CANCEL_CURRENT
                )
                val notification = NotificationCompat.Builder(context, context.getString(R.string.GC_ERROR_CHANNEL))
                        .setSmallIcon(R.drawable.ic_error_grey)
                        .setContentTitle(context.getString(R.string.gc_items_adding_fail))
                        .setContentText(context.getString(R.string.click_here_to_try_again))
                        .setContentIntent(pendingIntent)
                        .build()
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(CALENDAR_ERROR_NOTIFICATION_ID, notification)
            }
        }
    }

    private fun isGooglePlayServicesAvailable(context: Context): Boolean{
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    private fun isDeviceOnline(context: Context): Boolean{
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return (networkInfo != null && networkInfo.isConnected)
    }
}