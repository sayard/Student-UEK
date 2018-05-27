package pl.c0.sayard.studentUEK.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.anjlab.android.iab.v3.BillingProcessor
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import pl.c0.sayard.studentUEK.Utils.Companion.getTranslatedThemeName
import pl.c0.sayard.studentUEK.Utils.Companion.setSelectedTheme
import pl.c0.sayard.studentUEK.activities.ActivateGoogleCalendarIntegrationActivity
import pl.c0.sayard.studentUEK.activities.CreditsActivity
import pl.c0.sayard.studentUEK.activities.FirstRunStepOneActivity
import pl.c0.sayard.studentUEK.activities.MainActivity
import pl.c0.sayard.studentUEK.jobs.RefreshScheduleJob

class SettingsFragment : Fragment() {


    private val dialogClickListener = DialogInterface.OnClickListener { _, buttonClicked ->
        when(buttonClicked){
            DialogInterface.BUTTON_POSITIVE -> {
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                val editor = prefs.edit()
                editor.putBoolean(Utils.FIRST_RUN_SHARED_PREFS_KEY, true)
                editor.putBoolean(getString(R.string.PREFS_REFRESH_SCHEDULE), true)
                editor.apply()
                val intent = Intent(context, FirstRunStepOneActivity::class.java)
                activity.finish()
                startActivity(intent)
            }
            DialogInterface.BUTTON_NEGATIVE -> {}
        }
    }

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

        if(prefs.getBoolean(getString(R.string.PREFS_PREMIUM_PURCHASED), false)){
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
        }else{
            view.findViewById<LinearLayout>(R.id.gc_view).visibility = View.GONE
        }

        val notificationSwitch = view.findViewById<Switch>(R.id.settings_notification_switch)
        notificationSwitch.isChecked = prefs.getBoolean(getString(R.string.PREFS_ENABLE_NOTIFICATIONS), false)
        val notificationsSeekBar = view.findViewById<SeekBar>(R.id.notification_minutes_seek_bar)
        val notificationsDetailsView = view.findViewById<LinearLayout>(R.id.notification_settings_detail_view)
        val notificationMinutes = view.findViewById<TextView>(R.id.notification_settings_minutes)
        if(notificationSwitch.isChecked){
            notificationsSeekBar.visibility = View.VISIBLE
            notificationsDetailsView.visibility = View.VISIBLE
            notificationsSeekBar.progress = prefs.getInt(getString(R.string.PREFS_NOTIFICATION_TIME_THRESHOLD), 15)
            notificationMinutes.text = prefs.getInt(getString(R.string.PREFS_NOTIFICATION_TIME_THRESHOLD), 15).toString()
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

        notificationsSeekBar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{

            override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                notificationsSeekBar.progress = (progress/5)*5
                notificationMinutes.text = notificationsSeekBar.progress.toString()
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

        val currentTheme = view.findViewById<TextView>(R.id.currentTheme)
        val selectedThemeId = prefs.getInt(context.getString(R.string.PREFS_SELECTED_THEME), 0)
        currentTheme.append(" ${getTranslatedThemeName(selectedThemeId, context)}")

        val changeTheme = view.findViewById<LinearLayout>(R.id.change_theme)
        changeTheme.setOnClickListener {
            val themes = if(prefs.getBoolean(getString(R.string.PREFS_PREMIUM_PURCHASED), false)){
                arrayOf<CharSequence>(
                        context.getString(R.string.defaultTheme),
                        context.getString(R.string.darkTheme),
                        context.getString(R.string.premiumTheme)
                )
            }else{
                arrayOf<CharSequence>(
                        context.getString(R.string.defaultTheme),
                        context.getString(R.string.darkTheme)
                )
            }
            AlertDialog.Builder(context)
                    .setTitle(getString(R.string.choose_a_theme))
                    .setSingleChoiceItems(themes, selectedThemeId, null)
                    .setPositiveButton(getString(R.string.apply)) { dialog, _ ->
                        dialog?.dismiss()
                        val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                        setSelectedTheme(activity, selectedPosition)
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog?.dismiss()
                    }
                    .show()
        }

        val reconfigure = view.findViewById<LinearLayout>(R.id.reconfigure)
        reconfigure.setOnClickListener {

            val builder = AlertDialog.Builder(context)
            builder.setMessage(getString(R.string.groups_rechoosing_dialog_msg))
                    .setPositiveButton(getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(getString(R.string.no), dialogClickListener).show()
        }

        val buyPremium = view.findViewById<LinearLayout>(R.id.buy_premium)
        if(!prefs.getBoolean(getString(R.string.PREFS_PREMIUM_PURCHASED), false)){
            buyPremium.setOnClickListener{
                try{
                    val isIabAvailable = BillingProcessor.isIabServiceAvailable(context)
                    val isOneTimePurchaseSupported = MainActivity.bp?.isOneTimePurchaseSupported
                    if(isIabAvailable && isOneTimePurchaseSupported != null && isOneTimePurchaseSupported){
                        MainActivity.bp?.purchase(activity, getString(R.string.student_uek_premium_item_id))
                    }else{
                        Toast.makeText(context, getString(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
                    }
                }catch(e: NullPointerException){
                    Toast.makeText(context, getString(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            buyPremium.visibility = View.GONE
        }

        val credits = view.findViewById<LinearLayout>(R.id.credits)
        credits.setOnClickListener {
            val intent = Intent(context, CreditsActivity::class.java)
            startActivity(intent)
        }

        return view
    }

}
