package pl.c0.sayard.uekplan.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import pl.c0.sayard.uekplan.Utils

/**
 * Created by karol on 13.01.18.
 */
class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && context != null && intent.action == "android.intent.action.BOOT_COMPLETED") {
            Utils.startScheduleRefreshTask(context)
        }
    }
}
