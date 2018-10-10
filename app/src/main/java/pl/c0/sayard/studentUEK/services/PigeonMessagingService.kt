package pl.c0.sayard.studentUEK.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pl.c0.sayard.studentUEK.data.Message
import pl.c0.sayard.studentUEK.db.DatabaseManager

class PigeonMessagingService : FirebaseMessagingService() {

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
        }
        //TODO notification
    }

}
