package pl.c0.sayard.uekplan.parsers

import android.os.AsyncTask
import android.util.Log
import org.w3c.dom.Element
import org.xml.sax.InputSource
import pl.c0.sayard.uekplan.Lesson
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Created by karol on 09.01.18.
 */
class ScheduleParser : AsyncTask<String, Void, List<Lesson>>() {

    private val CLASSES_TAG = "zajecia"
    private val DATE_TAG = "termin"
    private val WEEK_DAY_TAG = "dzien"
    private val START_HOUR_TAG = "od-godz"
    private val END_HOUR_TAG = "do-godz"
    private val SUBJECT_TAG = "przedmiot"
    private val TYPE_TAG = "typ"
    private val TEACHER_TAG = "nauczyciel"
    private val MOODLE_TAG = "moodle"
    private val CLASSROOM_TAG = "sala"
    private val COMMENTS_TAG = "uwagi"
    private val LECTURESHIP = "lektorat"

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg groupUrl: String?): List<Lesson> {
        val lessonList = mutableListOf<Lesson>()
        try{
            val url = URL(groupUrl[0])
            val dbf = DocumentBuilderFactory.newInstance()
            val db = dbf.newDocumentBuilder()
            val document = db.parse(InputSource(url.openStream()))
            document.documentElement.normalize()
            val nodeList = document.getElementsByTagName(CLASSES_TAG)
            for(i in 0 until nodeList.length){
                val node = nodeList.item(i)
                val element = node as Element
                val type = element.getElementsByTagName(TYPE_TAG).item(0).textContent
                if(type != LECTURESHIP){
                    val date = element.getElementsByTagName(DATE_TAG).item(0).textContent
                    val weekDay = element.getElementsByTagName(WEEK_DAY_TAG).item(0).textContent
                    val startHour = element.getElementsByTagName(START_HOUR_TAG).item(0).textContent
                    val endHour = element.getElementsByTagName(END_HOUR_TAG).item(0).textContent
                    val subject = element.getElementsByTagName(SUBJECT_TAG).item(0).textContent
                    val teacher = element.getElementsByTagName(TEACHER_TAG).item(0).textContent
                    val teacherId = (element.getElementsByTagName(TEACHER_TAG).item(0) as Element).getAttribute(MOODLE_TAG)
                    val classroom = element.getElementsByTagName(CLASSROOM_TAG).item(0).textContent
                    var comments = ""
                    if(element.getElementsByTagName(COMMENTS_TAG).length > 0){
                        comments = element.getElementsByTagName(COMMENTS_TAG).item(0).textContent
                    }
                    val lesson = Lesson(date, weekDay, startHour, endHour, subject, type, teacher, teacherId, classroom, comments)
                    lessonList.add(lesson)
                }
            }
            return lessonList
        }catch (e: Exception){
            Log.v("SCHEDULE_PARS_EXCEPTION", e.toString())
            return emptyList()
        }
    }

    override fun onPostExecute(result: List<Lesson>?) {
        super.onPostExecute(result)
    }
}