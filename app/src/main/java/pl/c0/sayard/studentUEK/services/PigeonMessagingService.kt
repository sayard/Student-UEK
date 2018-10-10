package pl.c0.sayard.studentUEK.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PigeonMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(p0: RemoteMessage?) {
        Log.v("PIGEON", p0?.data.toString())
    }

}
