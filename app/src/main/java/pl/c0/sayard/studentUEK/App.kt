package pl.c0.sayard.studentUEK

import android.app.Application
import com.evernote.android.job.JobManager
import pl.c0.sayard.studentUEK.jobs.ScheduleJobCreator

/**
 * Created by karol on 08.03.18.
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        JobManager.create(this).addJobCreator(ScheduleJobCreator())
    }
}