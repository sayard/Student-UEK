package pl.c0.sayard.studentUEK.activities

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import pl.c0.sayard.studentUEK.tasks.CalendarEventsTask
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class ActivateGoogleCalendarIntegrationActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    class Constants{
        companion object {
            const val REQUEST_ACCOUNT_PICKER = 1000
            const val REQUEST_AUTHORIZATION = 1001
            const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
            const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
        }
    }

    private var credential: GoogleAccountCredential? = null

    private var PREFS_ACCOUNT_NAME = ""

    var progressBar:ProgressBar? = null
    var messageBox:TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.onActivityCreateSetTheme(this)
        PREFS_ACCOUNT_NAME = getString(R.string.PREFS_ACCOUNT_NAME)
        setContentView(R.layout.activity_activate_google_calendar_integration)
        progressBar = findViewById<ProgressBar>(R.id.activate_google_calendar_integration_progress_bar)
        messageBox = findViewById<TextView>(R.id.activate_google_calendar_integration_message_box)
        credential = GoogleAccountCredential.usingOAuth2(
                applicationContext, mutableListOf(CalendarScopes.CALENDAR))
                .setBackOff(ExponentialBackOff())
        configureApi()
    }

    private fun configureApi(){
        if(!isGooglePlayServicesAvailable()){
            acquireGooglePlayServices()
        }else if(credential?.selectedAccountName == null){
            chooseAccount()
        }else if(!isDeviceOnline()){
            messageBox?.text = getString(R.string.no_network_connection_message)
        }else{
            if(credential != null){
                CalendarEventsTask(credential!!, this, this).execute()
            }
        }
    }

    @AfterPermissionGranted(Constants.REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount(){
        if(EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)){
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            val accountName = prefs.getString(PREFS_ACCOUNT_NAME, null)
            if(accountName != null){
                credential?.selectedAccountName = accountName
                configureApi()
            }else{
                startActivityForResult(
                        credential?.newChooseAccountIntent(),
                        Constants.REQUEST_ACCOUNT_PICKER)
            }
        }else{
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.contacts_permission_rationale),
                    Constants.REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            Constants.REQUEST_GOOGLE_PLAY_SERVICES ->{
                if(resultCode != Activity.RESULT_OK){
                    messageBox?.text = getString(R.string.gp_services_required_msg)
                }else{
                    configureApi()
                }
            }
            Constants.REQUEST_ACCOUNT_PICKER ->{
                if(resultCode == Activity.RESULT_OK && data != null && data.extras != null){
                    val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if(accountName != null){
                        val settings = PreferenceManager.getDefaultSharedPreferences(this)
                        val editor = settings.edit()
                        editor.putString(PREFS_ACCOUNT_NAME, accountName)
                        editor.apply()
                        credential?.selectedAccountName = accountName
                        configureApi()
                    }
                }
            }
            Constants.REQUEST_AUTHORIZATION ->{
                if(resultCode == Activity.RESULT_OK){
                    configureApi()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>?) {
        finish()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>?) {
        //pass
    }

    private fun isDeviceOnline(): Boolean{
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return (networkInfo != null && networkInfo.isConnected)
    }

    private fun isGooglePlayServicesAvailable(): Boolean{
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this)
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    private fun acquireGooglePlayServices(){
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this)
        if(apiAvailability.isUserResolvableError(connectionStatusCode)){
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    private fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int){
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                Constants.REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }
}
