package pl.c0.sayard.uekplan.fragments

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch

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
        googleCalendarIntegrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                val intent = Intent(context, ActivateGoogleCalendarIntegrationActivity::class.java)
                startActivity(intent)
                editor.putBoolean(getString(R.string.PREFS_ENABLE_GC), true)
                editor.apply()
            }else{
                editor.putString(getString(R.string.PREFS_ACCOUNT_NAME), null)
                editor.putBoolean(getString(R.string.PREFS_ENABLE_GC), false)
                editor.apply()
            }
        }

        return view
    }

}
