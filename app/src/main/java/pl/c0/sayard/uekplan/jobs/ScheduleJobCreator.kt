package pl.c0.sayard.uekplan.jobs

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

/**
 * Created by karol on 08.03.18.
 */
class ScheduleJobCreator: JobCreator {
    override fun create(tag: String): Job? {
        return when(tag){
            RefreshScheduleJob.TAG -> RefreshScheduleJob()
            NotificationJob.TAG -> NotificationJob()
            else -> null
        }
    }
}