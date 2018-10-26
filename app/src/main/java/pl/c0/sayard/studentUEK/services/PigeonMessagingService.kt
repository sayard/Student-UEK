package pl.c0.sayard.studentUEK.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.activities.MainActivity
import pl.c0.sayard.studentUEK.data.Message
import pl.c0.sayard.studentUEK.db.DatabaseManager

class PigeonMessagingService : FirebaseMessagingService() {

    val PIGEON_NOFITICATION_ID = 420

    override fun onMessageReceived(rm: RemoteMessage?) {
        if(rm != null){
            val message = Message(
                    rm.data["title"]!!,
                    rm.data["body"]!!,
                    rm.data["author"]!!,
                    rm.data["date"]!!,
                    rm.data["groups"]!!
            )
            val dbManager = DatabaseManager(this)
            dbManager.addMessageToDb(message)
            val intent = Intent(this, MainActivity::class.java).apply{
                putExtra("EXTRA_OPEN_MESSAGES", true)
            }
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notification = NotificationCompat.Builder(this, getString(R.string.PIGEON_NOTIFICATION_CHANNEL))
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle("${message.title} - ${message.author}")
                    .setContentText(message.body)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(arrayOf<Long>(200, 150, 200, 150, 400, 200, 150, 200).toLongArray())
                    .setLights(Color.BLUE, 500, 500)
                    .setSound(alarmSound)
                    .build()
            val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(PIGEON_NOFITICATION_ID, notification)
        }
    }

}
