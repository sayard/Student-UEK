package pl.c0.sayard.studentUEK.jobs

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import com.evernote.android.job.Job
import com.evernote.android.job.JobRequest
import com.evernote.android.job.util.support.PersistableBundleCompat
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.activities.ScheduleItemDetailsActivity
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
        val intent = Intent(context, ScheduleItemDetailsActivity::class.java).apply {
            putExtra(context.getString(R.string.subject_extra), scheduleItem.subject)
            putExtra(context.getString(R.string.type_extra), scheduleItem.type)
            putExtra(context.getString(R.string.teacher_extra), scheduleItem.teacher)
            putExtra(context.getString(R.string.teacher_id_extra), scheduleItem.teacherId)
            putExtra(context.getString(R.string.classroom_extra), scheduleItem.classroom)
            putExtra(context.getString(R.string.comments_extra), scheduleItem.comments)
            putExtra(context.getString(R.string.date_extra), scheduleItem.dateStr)
            putExtra(context.getString(R.string.start_date_extra), scheduleItem.startDateStr)
            putExtra(context.getString(R.string.end_date_extra), scheduleItem.endDateStr)
            putExtra(context.getString(R.string.is_custom_extra), scheduleItem.isCustom)
            putExtra(context.getString(R.string.extra_custom_id), scheduleItem.customId)
            putExtra(context.getString(R.string.extra_note_id), scheduleItem.noteId)
            putExtra(context.getString(R.string.extra_note_content), scheduleItem.noteContent)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification = NotificationCompat.Builder(context, context.getString(R.string.SCHEDULE_ITEM_NOTIFICATION_CHANNEL))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("${scheduleItem.subject} - ${scheduleItem.type}")
                .setContentText("${scheduleItem.classroom} $hour")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(arrayOf<Long>(0, 200, 200, 200).toLongArray())
                .setLights(Color.RED, 500, 500)
                .setSound(alarmSound)
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