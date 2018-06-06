package pl.c0.sayard.studentUEK.parsers

import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.edit
import com.google.common.io.ByteStreams
import org.json.JSONException
import org.json.JSONObject
import pl.c0.sayard.studentUEK.R
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException

class MoodleTokenParser(val context: Context?, val fragment: Fragment, val progressBar: ProgressBar, val loginButton: Button): AsyncTask<String, Void, Pair<Int, String?>>(){

    private val PARSE_RETURN_CODE_OK = 0
    private val PARSE_RETURN_CODE_CANT_CONNECT = 1
    private val PARSE_RETURN_CODE_OTHER_ERROR = 2
    
    private val TOKEN_KEY = "token"
    private val ERROR_CODE_KEY = "errorcode"
    private val INVALID_LOGIN_KEY = "invalidlogin"

    override fun onPreExecute() {
        progressBar.visibility = View.VISIBLE
        loginButton.visibility = View.GONE
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String?): Pair<Int, String?>{
        val login = params[0]
        val password = params[1]
        val tokenJsonUrl = "https://e-uczelnia.uek.krakow.pl/login/token.php?username=$login&password=$password&service=moodle_mobile_app"

        val url = URL(tokenJsonUrl)
        val urlConnection = url.openConnection() as HttpURLConnection
        return try{
            val inputStream = BufferedInputStream(urlConnection.inputStream)
            val result = String(ByteStreams.toByteArray(inputStream), Charsets.UTF_8)
            urlConnection.disconnect()
            Pair(PARSE_RETURN_CODE_OK, result)
        }catch(e: UnknownHostException){
            Pair(PARSE_RETURN_CODE_CANT_CONNECT, context?.getString(R.string.moodle_connection_error))
        }catch(e: Exception){
            Log.v("MOODLE_TOKEN_EXCEPTION", e.printStackTrace().toString())
            Pair(PARSE_RETURN_CODE_OTHER_ERROR, context?.getString(R.string.error_try_again_later))
        }
    }

    override fun onPostExecute(result: Pair<Int, String?>?) {
        when(result?.first){
            0-> result.second?.let { parseJSON(it) }
            1-> result.second?.let { displayError(it) }
            2-> result.second?.let { displayError(it) }
        }
        progressBar.visibility = View.GONE
        loginButton.visibility = View.VISIBLE
        super.onPostExecute(result)
    }

    private fun parseJSON(jsonString: String){
        val mainObject = JSONObject(jsonString)
        try{
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            prefs.edit {
                putString(context?.getString(R.string.pl_c0_sayard_StudentUEK_PREFS_MOODLE_TOKEN), mainObject.getString(TOKEN_KEY))
            }
            val fragmentManager = fragment.fragmentManager
            fragmentManager?.beginTransaction()
                    ?.detach(fragment)
                    ?.attach(fragment)
                    ?.commit()
        }catch(e: JSONException){
            val errorCode = mainObject.getString(ERROR_CODE_KEY)
            if(errorCode == INVALID_LOGIN_KEY){
                Toast.makeText(context, context?.getString(R.string.invalid_login_or_password), Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(context, context?.getString(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayError(errorString: String){
        Toast.makeText(context, errorString, Toast.LENGTH_SHORT).show()
    }

}