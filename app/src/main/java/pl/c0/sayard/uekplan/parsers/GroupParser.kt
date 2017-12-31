package pl.c0.sayard.uekplan.parsers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import org.w3c.dom.Element
import org.xml.sax.InputSource
import pl.c0.sayard.uekplan.R
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Created by karol on 29.12.17.
 */
class GroupParser(activity: Activity) : AsyncTask<Void, Void, List<String>>() {

    private val GROUP_URL = "http://planzajec.uek.krakow.pl/index.php?xml&typ=G"
    private val RESOURCE_TAG = "zasob"
    private val NAME_ATTRIBUTE = "nazwa"
    private val SJO_PREFIX = "SJO"
    @SuppressLint("StaticFieldLeak")
    private val progressBar = activity.findViewById<ProgressBar>(R.id.first_step_progress_bar)
    @SuppressLint("StaticFieldLeak")
    private val list = activity.findViewById<ListView>(R.id.group_list_view)
    @SuppressLint("StaticFieldLeak")

    override fun onPreExecute() {
        super.onPreExecute()
        progressBar.visibility = View.VISIBLE
        list.visibility = View.GONE
    }

    override fun doInBackground(vararg p0: Void?): List<String> {
        val groupList = mutableListOf<String>()
        try {
            val url = URL(GROUP_URL)
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()
            val document = documentBuilder.parse(InputSource(url.openStream()))
            document.documentElement.normalize()
            val nodeList = document.getElementsByTagName(RESOURCE_TAG)
            (0 until nodeList.length)
                    .map { nodeList.item(it) as Element }
                    .map { it.getAttribute(NAME_ATTRIBUTE) }
                    .filterNotTo(groupList) { it.startsWith(SJO_PREFIX) }
            return groupList
        }catch (e: Exception){
            Log.v("GROUP_PARSER_EXCEPTION", e.toString())
            progressBar.visibility = View.GONE
            list.visibility = View.GONE
            return emptyList()
        }
    }

    override fun onPostExecute(result: List<String>?) {
        super.onPostExecute(result)
        progressBar.visibility = View.GONE
        list.visibility = View.VISIBLE
    }

}
