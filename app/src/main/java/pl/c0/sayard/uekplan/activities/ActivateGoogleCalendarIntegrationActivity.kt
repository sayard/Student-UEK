package pl.c0.sayard.uekplan.activities

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import pl.c0.sayard.uekplan.R
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException

class ActivateGoogleCalendarIntegrationActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private class Constants{
        companion object {
            const val REQUEST_ACCOUNT_PICKER = 1000
            const val REQUEST_AUTHORIZATION = 1001
            const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
            const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
        }
    }

    private var credential: GoogleAccountCredential? = null

    private val PREF_ACCOUNT_NAME = "accountName"

    var progressBar:ProgressBar? = null
    var messageBox:TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            Log.v("TAG", "1")
            acquireGooglePlayServices()
        }else if(credential?.selectedAccountName == null){
            Log.v("TAG", "2")
            chooseAccount()
        }else if(!isDeviceOnline()){
            Log.v("TAG", "3")
            messageBox?.text = getString(R.string.no_network_connection_message)
        }else{
            Log.v("TAG", "4")
            if(credential != null){
                Log.v("TAG", "5")
                MakeRequestTask(credential!!, this).execute()
            }
        }
    }

    @AfterPermissionGranted(Constants.REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount(){
        if(EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)){
            val accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null)
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
                        val settings = getPreferences(Context.MODE_PRIVATE)
                        val editor = settings.edit()
                        editor.putString(PREF_ACCOUNT_NAME, accountName)
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

    private class MakeRequestTask(credential: GoogleAccountCredential, val instance: Activity) : AsyncTask<Void, Void, List<String>?>() {

        private var service: Calendar? = null
        private var lastError: Exception? = null

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            service = Calendar.Builder(transport, jsonFactory, credential)
                    .setApplicationName("Student UEK")
                    .build()
        }

        override fun doInBackground(vararg params: Void?): List<String>? {
            return try{
                getDataFromApi()
            }catch (e: Exception){
                lastError = e
                cancel(true)
                null
            }
        }

        @Throws(IOException::class)
        private fun getDataFromApi(): List<String>{
            val now = DateTime(System.currentTimeMillis())
            val eventStrings = mutableListOf<String>()
            val events = service?.events()?.list("primary")
                    ?.setMaxResults(10)
                    ?.setTimeMin(now)
                    ?.setOrderBy("startTime")
                    ?.setSingleEvents(true)
                    ?.execute()
            val items = events?.items
            if (items != null) {
                for(event in items){
                    var start = event.start.dateTime
                    if(start == null){
                        start = event.start.date
                    }
                    eventStrings.add("${event.summary} $start")
                }
            }
            return eventStrings
        }

        override fun onPostExecute(result: List<String>?) {
            if(result == null || result.isEmpty()){
                Log.v("TAG", "no results")
            }else{
                Log.v("TAG", TextUtils.join("\n", result))
            }
        }

        override fun onCancelled() {
            super.onCancelled()
            if(lastError != null){
                when (lastError) {
                    is GooglePlayServicesAvailabilityIOException -> {
                        val apiAvailability = GoogleApiAvailability.getInstance()
                        val dialog = apiAvailability.getErrorDialog(
                                instance,
                                (lastError as GooglePlayServicesAvailabilityIOException).connectionStatusCode,
                                Constants.REQUEST_GOOGLE_PLAY_SERVICES)
                        dialog.show()
                    }
                    is UserRecoverableAuthIOException -> instance.startActivityForResult(
                            (lastError as UserRecoverableAuthIOException).intent,
                            ActivateGoogleCalendarIntegrationActivity.Constants.REQUEST_AUTHORIZATION
                    )
                    else -> Log.v("TAG", lastError!!.message)
                }
            }else{
                Log.v("TAG", "cancelled")
            }
        }

    }
}
