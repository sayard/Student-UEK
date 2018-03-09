package pl.c0.sayard.uekplan.fragments

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.evernote.android.job.JobRequest

import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.Utils
import pl.c0.sayard.uekplan.activities.ActivateGoogleCalendarIntegrationActivity
import pl.c0.sayard.uekplan.jobs.RefreshScheduleJob

class SettingsFragment : Fragment() {

    companion object {
        fun newInstance(): SettingsFragment{
            return SettingsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_settings, container, false)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = prefs.edit()

        val googleCalendarIntegrationSwitch = view.findViewById<Switch>(R.id.settings_calendar_switch)
        googleCalendarIntegrationSwitch.isChecked = prefs.getBoolean(getString(R.string.PREFS_ENABLE_GC), false)
        googleCalendarIntegrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                val intent = Intent(context, ActivateGoogleCalendarIntegrationActivity::class.java)
                startActivity(intent)
                editor.putBoolean(getString(R.string.PREFS_ENABLE_GC), true)
                editor.apply()
                Thread{
                    kotlin.run {
                        RefreshScheduleJob.refreshSchedule(context)
                    }
                }.start()
            }else{
                editor.putString(getString(R.string.PREFS_ACCOUNT_NAME), null)
                editor.putBoolean(getString(R.string.PREFS_ENABLE_GC), false)
                editor.apply()
                Thread{
                    kotlin.run {
                        RefreshScheduleJob.refreshSchedule(context)
                    }
                }.start()
            }
        }

        val notificationSwitch = view.findViewById<Switch>(R.id.settings_notification_switch)
        notificationSwitch.isChecked = prefs.getBoolean(getString(R.string.PREFS_ENABLE_NOTIFICATIONS), false)
        val notificationsSeekBar = view.findViewById<SeekBar>(R.id.notification_minutes_seek_bar)
        val notificationsDetailsView = view.findViewById<LinearLayout>(R.id.notification_settings_detail_view)
        if(notificationSwitch.isChecked){
            notificationsSeekBar.visibility = View.VISIBLE
            notificationsDetailsView.visibility = View.VISIBLE
            notificationsSeekBar.progress = prefs.getInt(getString(R.string.PREFS_NOTIFICATION_TIME_THRESHOLD), 15)
        }
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                notificationsSeekBar.visibility = View.VISIBLE
                notificationsDetailsView.visibility = View.VISIBLE
                editor.putBoolean(getString(R.string.PREFS_ENABLE_NOTIFICATIONS), true)
                editor.putInt(getString(R.string.PREFS_NOTIFICATION_TIME_THRESHOLD), notificationsSeekBar.progress)
                editor.putBoolean(getString(R.string.PREFS_REFRESH_SCHEDULE), true)
                editor.apply()
                notificationsSeekBar.progress = prefs.getInt(getString(R.string.PREFS_NOTIFICATION_TIME_THRESHOLD), 15)
                Thread{
                    kotlin.run {
                        RefreshScheduleJob.refreshSchedule(context)
                    }
                }.start()
            }else{
                notificationsSeekBar.visibility = View.GONE
                notificationsDetailsView.visibility = View.GONE
                editor.putBoolean(getString(R.string.PREFS_ENABLE_NOTIFICATIONS), false)
                editor.putInt(getString(R.string.PREFS_NOTIFICATION_TIME_THRESHOLD), -1)
                editor.apply()
                Thread{
                    kotlin.run {
                        RefreshScheduleJob.refreshSchedule(context)
                    }
                }.start()
            }
        }

        val notificationMinutes = view.findViewById<TextView>(R.id.notification_settings_minutes)

        notificationsSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{

            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                notificationMinutes.text = progress.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if(seekBar != null){
                    editor.putInt(getString(R.string.PREFS_NOTIFICATION_TIME_THRESHOLD), seekBar.progress)
                    editor.putBoolean(getString(R.string.PREFS_REFRESH_SCHEDULE), true)
                }else{
                    editor.putInt(getString(R.string.PREFS_NOTIFICATION_TIME_THRESHOLD), -1)
                }
                editor.apply()
                RefreshScheduleJob.cancelScheduleNotifications()
                Thread{
                    kotlin.run {
                        RefreshScheduleJob.refreshSchedule(context)
                    }
                }.start()
            }

        })

        return view
    }

}
