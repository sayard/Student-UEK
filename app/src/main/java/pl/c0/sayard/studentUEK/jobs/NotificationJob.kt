package pl.c0.sayard.studentUEK.jobs

import android.app.NotificationManager
import android.content.Context
import android.support.v4.app.NotificationCompat
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.data.ScheduleItem

/**
 * Created by karol on 08.03.18.
 */
class NotificationJob: Job() {

    private val SCHEDULE_ITEM_NOFITICATION_ID = 666

    override fun onRunJob(params: Params): Result {
        val extras = params.extras
        val scheduleItem = ScheduleItem(
                extras.getString(context.getString(R.string.EXTRA_NOTIFICATION_SUBJECT), ""),
                extras.getString(context.getString(R.string.EXTRA_NOTIFICATION_TYPE), ""),
                extras.getString(context.getString(R.string.EXTRA_NOTIFICATION_TEACHER), ""),
                extras.getInt(context.getString(R.string.EXTRA_NOTIFICATION_TEACHER_ID), 0),
                extras.getString(context.getString(R.string.EXTRA_NOTIFICATION_CLASSROOM), ""),
                extras.getString(context.getString(R.string.EXTRA_NOTIFICATION_COMMENTS), ""),
                extras.getString(context.getString(R.string.EXTRA_NOTIFICATION_DATE), ""),
                extras.getString(context.getString(R.string.EXTRA_NOTIFICATION_START_DATE), ""),
                extras.getString(context.getString(R.string.EXTRA_NOTIFICATION_END_DATE), ""),
                false,
                extras.getBoolean(context.getString(R.string.EXTRA_NOTIFICATION_IS_CUSTOM), false),
                extras.getInt(context.getString(R.string.EXTRA_NOTIFICATION_CUSTOM_ID), -1),
                extras.getInt(context.getString(R.string.EXTRA_NOTIFICATION_NOTE_ID), -1),
                extras.getString(context.getString(R.string.EXTRA_NOTIFICATION_NOTE_CONTENT), "")
        )
        val hour = extras.getString(context.getString(R.string.EXTRA_NOTIFICATION_HOUR), "")
        val notification = NotificationCompat.Builder(context, context.getString(R.string.SCHEDULE_ITEM_NOTIFICATION_CHANNEL))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("${scheduleItem.subject} - ${scheduleItem.type}")
                .setContentText("${scheduleItem.classroom} $hour")
                .build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(SCHEDULE_ITEM_NOFITICATION_ID, notification)
        return Result.SUCCESS
    }

    companion object {
        val TAG = "SCHEDULE_NOTIFICATION"
        fun schedule(timeInMillis: Long, extras:PersistableBundleCompat){
            JobRequest.Builder(TAG)
                    .setExact(timeInMillis)
                    .setUpdateCurrent(false)
                    .addExtras(extras)
                    .build()
                    .schedule()
        }
    }
}