package pl.c0.sayard.uekplan.parsers

import android.os.AsyncTask
import android.util.Log
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Created by karol on 29.12.17.
 */
class GroupParser: AsyncTask<Void, Void, List<String>>() {

    private val GROUP_URL = "http://planzajec.uek.krakow.pl/index.php?xml&typ=G"
    private val RESOURCE_TAG = "zasob"
    private val NAME_ATTRIBUTE = "nazwa"
    private val SJO_PREFIX = "SJO"

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
            return emptyList()
        }
    }

}
