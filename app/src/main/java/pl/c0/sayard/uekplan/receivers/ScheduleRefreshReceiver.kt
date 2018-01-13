package pl.c0.sayard.uekplan.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import pl.c0.sayard.uekplan.Utils
import pl.c0.sayard.uekplan.Utils.Companion.getLanguageGroups
import pl.c0.sayard.uekplan.db.ScheduleDbHelper
import pl.c0.sayard.uekplan.parsers.ScheduleParser


/**
 * Created by karol on 13.01.18.
 */
class ScheduleRefreshReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            val dbHelper = ScheduleDbHelper(context)
            val db = dbHelper.readableDatabase
            val group = Utils.getGroup(db)
            val languageGroups = getLanguageGroups(db)
            val urls = mutableListOf(group.url)
            languageGroups.mapTo(urls) { it.url }
            ScheduleParser(context, null, null, null, null, null).execute(urls)
        }
    }
}
