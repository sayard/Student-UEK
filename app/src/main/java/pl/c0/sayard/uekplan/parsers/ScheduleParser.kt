package pl.c0.sayard.uekplan.parsers

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.support.v4.widget.SwipeRefreshLayout
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import org.w3c.dom.Element
import org.xml.sax.InputSource
import pl.c0.sayard.uekplan.Utils
import pl.c0.sayard.uekplan.adapters.ScheduleAdapter
import pl.c0.sayard.uekplan.data.Lesson
import pl.c0.sayard.uekplan.db.ScheduleContract
import pl.c0.sayard.uekplan.db.ScheduleDbHelper
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Created by karol on 09.01.18.
 */
class ScheduleParser(@SuppressLint("StaticFieldLeak") val context: Context,
                     val activity: Activity?,
                     val progressBar: ProgressBar?,
                     val errorMessage: TextView?,
                     val adapter: ScheduleAdapter?,
                     val scheduleSwipe: SwipeRefreshLayout?) : AsyncTask<List<String>, Void, List<Lesson>>() {

    private val CLASSES_TAG = "zajecia"
    private val DATE_TAG = "termin"
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
        progressBar?.visibility = View.VISIBLE
        val dbHelper = ScheduleDbHelper(context)
        val db = dbHelper.writableDatabase
        db.execSQL("DELETE FROM " + ScheduleContract.LessonEntry.TABLE_NAME)
        dbHelper.close()
    }

    override fun doInBackground(vararg groupUrls: List<String>?): List<Lesson>? {
        val lessonList = mutableListOf<Lesson>()
        try{
            for(i in 0 until groupUrls[0]!!.size){
                val url = URL(groupUrls[0]!![i])
                val dbf = DocumentBuilderFactory.newInstance()
                val db = dbf.newDocumentBuilder()
                val document = db.parse(InputSource(url.openStream()))
                document.documentElement.normalize()
                val nodeList = document.getElementsByTagName(CLASSES_TAG)
                for(j in 0 until nodeList.length){
                    val node = nodeList.item(j)
                    val element = node as Element
                    val type = element.getElementsByTagName(TYPE_TAG).item(0).textContent
                    val classroom = element.getElementsByTagName(CLASSROOM_TAG).item(0).textContent
                    if(!(type == LECTURESHIP && classroom.isEmpty())){
                        val date = element.getElementsByTagName(DATE_TAG).item(0).textContent
                        val startHour = element.getElementsByTagName(START_HOUR_TAG).item(0).textContent
                        val endHour = element.getElementsByTagName(END_HOUR_TAG).item(0).textContent
                        val subject = element.getElementsByTagName(SUBJECT_TAG).item(0).textContent
                        val teacher = element.getElementsByTagName(TEACHER_TAG).item(0).textContent
                        val teacherId = (element.getElementsByTagName(TEACHER_TAG).item(0) as Element).getAttribute(MOODLE_TAG)
                        var comments = ""
                        if(element.getElementsByTagName(COMMENTS_TAG).length > 0){
                            comments = element.getElementsByTagName(COMMENTS_TAG).item(0).textContent
                        }
                        val lesson = Lesson(date, startHour, endHour, subject, type, teacher, teacherId, classroom, comments)
                        lessonList.add(lesson)
                    }
                }
            }
            return lessonList
        }catch (e: Exception){
            Log.v("SCHEDULE_PARS_EXCEPTION", e.toString())
            return emptyList()
        }
    }

    override fun onPostExecute(lessons: List<Lesson>?) {
        super.onPostExecute(lessons)
        if (lessons != null) {
            val dbHelper = ScheduleDbHelper(context)
            val db = dbHelper.writableDatabase
            val contentValues = ContentValues()
            for(lesson in lessons){
                contentValues.put(ScheduleContract.LessonEntry.SUBJECT, lesson.subject)
                contentValues.put(ScheduleContract.LessonEntry.TYPE, lesson.type)
                contentValues.put(ScheduleContract.LessonEntry.TEACHER, lesson.teacher)
                contentValues.put(ScheduleContract.LessonEntry.TEACHER_ID, lesson.teacherIdParsed)
                contentValues.put(ScheduleContract.LessonEntry.CLASSROOM, lesson.classroom)
                contentValues.put(ScheduleContract.LessonEntry.COMMENTS, lesson.comments)
                contentValues.put(ScheduleContract.LessonEntry.DATE, lesson.date)
                contentValues.put(ScheduleContract.LessonEntry.START_DATE, lesson.startDate)
                contentValues.put(ScheduleContract.LessonEntry.END_DATE, lesson.endDate)
                db.insert(ScheduleContract.LessonEntry.TABLE_NAME, null, contentValues)
            }
            if(adapter != null){
                val cursor = Utils.getScheduleCursor(db)
                val scheduleList = Utils.getScheduleList(cursor, db)
                adapter.changeAdapterData(scheduleList)
                adapter.notifyDataSetChanged()
                scheduleSwipe?.isRefreshing = false
            }
            dbHelper.close()
            activity?.recreate()
        }else{
            progressBar?.visibility = View.GONE
            errorMessage?.visibility = View.VISIBLE
        }
    }
}
