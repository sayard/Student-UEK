package pl.c0.sayard.uekplan.parsers

import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import org.w3c.dom.Element
import org.xml.sax.InputSource
import pl.c0.sayard.uekplan.Utils
import pl.c0.sayard.uekplan.data.Group
import pl.c0.sayard.uekplan.data.ScheduleItem
import pl.c0.sayard.uekplan.fragments.SearchedScheduleFragment
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

class SearchedScheduleParser(val progressBar: ProgressBar, val errorMessage: TextView, val fragment: SearchedScheduleFragment, val onTaskCompleted: OnTaskCompleted): AsyncTask<Group, Void, List<ScheduleItem>>(){
    private val CLASSES_TAG = "zajecia"
    private val DATE_TAG = "termin"
    private val START_HOUR_TAG = "od-godz"
    private val END_HOUR_TAG = "do-godz"
    private val SUBJECT_TAG = "przedmiot"
    private val TYPE_TAG = "typ"
    private val TEACHER_TAG = "nauczyciel"
    private val GROUP_TAG = "grupa"
    private val MOODLE_TAG = "moodle"
    private val CLASSROOM_TAG = "sala"
    private val COMMENTS_TAG = "uwagi"
    private val SCHEDULE_TAG = "plan-zajec"
    private val IDCEL_TAG = "idcel"

    interface OnTaskCompleted{
        fun onTaskCompleted(result: List<ScheduleItem>, fragment: SearchedScheduleFragment)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        progressBar.visibility = View.VISIBLE
        errorMessage.visibility = View.GONE
    }

    override fun doInBackground(vararg groups: Group?): List<ScheduleItem> {
        val scheduleItemList = mutableListOf<ScheduleItem>()
        try{
            groups.map {
                val url = if(it?.type == "G"){
                    Utils.getGroupURL(it)
                }else{
                    Utils.getTeacherURL(it)
                }
                val parsingUrl = URL(url)
                val dbf = DocumentBuilderFactory.newInstance()
                val db = dbf.newDocumentBuilder()
                val document = db.parse(InputSource(parsingUrl.openStream()))
                document.documentElement.normalize()
                var nodeList = document.getElementsByTagName(SCHEDULE_TAG)
                var idCel: Int? = null
                if(it?.type == "N"){
                    val scheduleElem = nodeList.item(0) as Element
                    val idCelStr = scheduleElem.getAttribute(IDCEL_TAG)
                    idCel = idCelStr.substring(1).toInt()
                }
                nodeList = document.getElementsByTagName(CLASSES_TAG)
                for(i in 0 until nodeList.length){
                    val element = nodeList.item(i) as Element
                    val subject = element.getElementsByTagName(SUBJECT_TAG).item(0).textContent
                    val type = element.getElementsByTagName(TYPE_TAG).item(0).textContent
                    val classroom = element.getElementsByTagName(CLASSROOM_TAG).item(0).textContent
                    var comments = ""
                    if(element.getElementsByTagName(COMMENTS_TAG).length > 0){
                        comments = element.getElementsByTagName(COMMENTS_TAG).item(0).textContent
                    }
                    val date = element.getElementsByTagName(DATE_TAG).item(0).textContent
                    val startHour = element.getElementsByTagName(START_HOUR_TAG).item(0).textContent
                    val endHour = element.getElementsByTagName(END_HOUR_TAG).item(0).textContent
                    if(it?.type == "G"){
                        val teacher = element.getElementsByTagName(TEACHER_TAG).item(0).textContent
                        var teacherId = (element.getElementsByTagName(TEACHER_TAG).item(0) as Element).getAttribute(MOODLE_TAG)
                        if(teacherId == ""){
                            teacherId = "0"
                        }
                        scheduleItemList.add(ScheduleItem(subject, type, teacher, teacherId.toInt(), classroom, comments, date, "$date $startHour", "$date $endHour"))
                    }else{
                        val group = element.getElementsByTagName(GROUP_TAG).item(0).textContent
                        var teacherId = it?.id
                        if(idCel != null){
                            teacherId = idCel
                        }
                        scheduleItemList.add(ScheduleItem(subject, type, group, teacherId!!, classroom, comments, date, "$date $startHour", "$date $endHour"))
                    }
                }
            }
            return scheduleItemList
        }catch (e: Exception){
            Log.v("SEARCHED_SCHEDULE_P_E", e.toString())
            return emptyList()
        }
    }

    override fun onPostExecute(result: List<ScheduleItem>?) {
        super.onPostExecute(result)
        progressBar.visibility = View.GONE
        if(result != null){
            onTaskCompleted.onTaskCompleted(result, fragment)
        }else{
            errorMessage.visibility = View.VISIBLE
        }
    }
}