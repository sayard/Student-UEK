package pl.c0.sayard.uekplan.fragments

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes

import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.Utils
import pl.c0.sayard.uekplan.activities.ActivateGoogleCalendarIntegrationActivity


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
        googleCalendarIntegrationSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                val intent = Intent(context, ActivateGoogleCalendarIntegrationActivity::class.java)
                startActivity(intent)
            }else{
                editor.putString(getString(R.string.PREFS_ACCOUNT_NAME), null)
                editor.putBoolean(getString(R.string.PREFS_ENABLE_GC), false)
                editor.apply()
                val credential = GoogleAccountCredential.usingOAuth2(
                        activity.applicationContext, mutableListOf(CalendarScopes.CALENDAR)
                ).setBackOff(ExponentialBackOff())
                credential.selectedAccountName = null
                credential.selectedAccount = null
                Utils.stopGoogleCalendarIntegrationTask(context)
            }
        }

        return view
    }

}
