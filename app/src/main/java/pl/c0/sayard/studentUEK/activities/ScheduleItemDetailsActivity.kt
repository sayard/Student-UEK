package pl.c0.sayard.studentUEK.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import pl.c0.sayard.studentUEK.data.Building
import pl.c0.sayard.studentUEK.data.ScheduleItem
import pl.c0.sayard.studentUEK.db.DatabaseManager
import pl.c0.sayard.studentUEK.jobs.RefreshScheduleJob
import java.text.SimpleDateFormat
import java.util.*

class ScheduleItemDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val hourFormat = SimpleDateFormat("HH:mm", Locale("pl", "PL"))
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("pl", "PL"))
    private var scheduleItem: ScheduleItem? = null
    private var idToDelete: Int? = null
    private val dialogClickListener = DialogInterface.OnClickListener { _, which ->
        when(which){
            DialogInterface.BUTTON_POSITIVE ->{
                try{
                    val dbManager = DatabaseManager(this)
                    val deleteCount = dbManager.removeUserLesson(idToDelete)
                    if(deleteCount > 0){
                        Thread{
                            kotlin.run {
                                RefreshScheduleJob.refreshSchedule(this)
                            }
                        }.start()
                        val mainActivityIntent = Intent(this, MainActivity::class.java)
                        startActivity(mainActivityIntent)
                    }else{
                        Toast.makeText(this, getString(R.string.custom_lesson_delete_error), Toast.LENGTH_SHORT).show()
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(this, getString(R.string.custom_lesson_delete_error), Toast.LENGTH_SHORT).show()
                }

                return@OnClickListener
            }
            DialogInterface.BUTTON_NEGATIVE -> return@OnClickListener
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.onActivityCreateSetTheme(this)
        setContentView(R.layout.activity_schedule_item_details)
        scheduleItem = ScheduleItem(
                intent.getStringExtra(getString(R.string.subject_extra)),
                intent.getStringExtra(getString(R.string.type_extra)),
                intent.getStringExtra(getString(R.string.teacher_extra)),
                intent.getIntExtra(getString(R.string.teacher_id_extra), 0),
                intent.getStringExtra(getString(R.string.classroom_extra)),
                intent.getStringExtra(getString(R.string.comments_extra)),
                intent.getStringExtra(getString(R.string.date_extra)),
                intent.getStringExtra(getString(R.string.start_date_extra)),
                intent.getStringExtra(getString(R.string.end_date_extra)),
                isCustom = intent.getBooleanExtra(getString(R.string.is_custom_extra), false),
                customId = intent.getIntExtra(getString(R.string.custom_id_extra), -1),
                noteId = intent.getIntExtra(getString(R.string.extra_note_id), -1),
                noteContent = intent.getStringExtra(getString(R.string.extra_note_content))
        )
        val subjectTv = findViewById<TextView>(R.id.schedule_item_details_subject)
        subjectTv.text = scheduleItem?.subject
        val typeTv = findViewById<TextView>(R.id.schedule_item_details_type)
        typeTv.text = scheduleItem?.type
        val teacherTv = findViewById<TextView>(R.id.schedule_item_details_teacher)
        teacherTv.text = scheduleItem?.teacher
        val dateAndHourTv = findViewById<TextView>(R.id.schedule_item_details_date_and_hour)
        val date = dateFormat.format(scheduleItem?.startDate)
        val startHour = hourFormat.format(scheduleItem?.startDate)
        val endHour = hourFormat.format(scheduleItem?.endDate)
        val dateAndHourStr = "${scheduleItem?.dayOfTheWeekStr} $date $startHour-$endHour"
        dateAndHourTv.text = dateAndHourStr
        val classroomTv = findViewById<TextView>(R.id.schedule_item_details_classroom)
        classroomTv.text = scheduleItem?.classroom

        val noteContentTextView = findViewById<TextView>(R.id.schedule_item_details_note_content)
        noteContentTextView.text = scheduleItem?.noteContent

        val addOrEditLessonNoteButton = findViewById<Button>(R.id.add_or_edit_lesson_note)
        if(scheduleItem?.noteId == -1 && scheduleItem?.noteContent == ""){
            addOrEditLessonNoteButton.text = getString(R.string.add_note)
        }else{
            addOrEditLessonNoteButton.text = getString(R.string.edit_note)
            noteContentTextView.visibility = View.VISIBLE
        }
        addOrEditLessonNoteButton.visibility = View.VISIBLE
        addOrEditLessonNoteButton.setOnClickListener {
            val noteId = scheduleItem?.noteId
            showNoteEditDialog(noteId, scheduleItem?.noteContent)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.schedule_item_details_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if(scheduleItem != null && !scheduleItem!!.isCustom && scheduleItem?.teacherId != 0){
            val teacherPageButton = findViewById<Button>(R.id.schedule_item_details_teacher_page_button)
            teacherPageButton.visibility = View.VISIBLE
            teacherPageButton.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, "https://e-uczelnia.uek.krakow.pl/course/view.php?id=${scheduleItem?.teacherId}".toUri())
                startActivity(browserIntent)
            }
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        val buildingInstance = Building(this)
        val markerLatLng = buildingInstance.getBuildingLatLng(scheduleItem?.classroom)
        if(markerLatLng != null){
            val marker = map
                            ?.addMarker(MarkerOptions()
                            .position(markerLatLng)
                            .title(scheduleItem?.classroom))
            marker?.snippet = getString(R.string.schedule_details_marker_snippet)
            marker?.showInfoWindow()
            map?.setOnMarkerClickListener { clickedMarker ->
                if(clickedMarker == marker){
                    val gmmIntentUri = "geo:0,0?q=${markerLatLng.latitude},${markerLatLng.longitude}(${scheduleItem?.classroom})".toUri()
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.`package` = "com.google.android.apps.maps"
                    startActivity(mapIntent)
                }
                true
            }
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng, 16.5f))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(scheduleItem != null && scheduleItem!!.isCustom){
            menuInflater.inflate(R.menu.schedule_item_details_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.custom_schedule_item_edit -> {
                val buildingInstance = Building(this)
                val intent = Intent(this, AddLessonActivity::class.java).apply {
                    putExtra(getString(R.string.extra_custom_lesson_name), scheduleItem?.subject)
                    putExtra(getString(R.string.extra_custom_lesson_type), scheduleItem?.type)
                    putExtra(getString(R.string.extra_custom_lesson_teacher), scheduleItem?.teacher)
                    putExtra(getString(R.string.extra_custom_lesson_building), buildingInstance.getBuildingFromAbbreviation(scheduleItem?.classroom))
                    putExtra(getString(R.string.extra_custom_lesson_classroom), scheduleItem?.classroom)
                    putExtra(getString(R.string.extra_custom_lesson_date), dateFormat.format(scheduleItem?.startDate))
                    putExtra(getString(R.string.extra_custom_lesson_start_hour), hourFormat.format(scheduleItem?.startDate))
                    putExtra(getString(R.string.extra_custom_lesson_end_hour), hourFormat.format(scheduleItem?.endDate))
                    putExtra(getString(R.string.extra_custom_id), scheduleItem?.customId)
                }
                startActivity(intent)
            }
            R.id.custom_schedule_item_delete -> {
                idToDelete = scheduleItem?.customId
                val builder = AlertDialog.Builder(this)
                builder.setMessage(getString(R.string.custom_lesson_delete_message))
                        .setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener)
                        .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showNoteEditDialog(noteId:Int?, dbNoteContent: String?){
        val dialogBuilder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.schedule_item_note_edit_dialog, null)
        dialogBuilder.setView(dialogView)

        val noteContentField = dialogView.findViewById<EditText>(R.id.lesson_note_edit_field)

        val noteContentTextView = findViewById<TextView>(R.id.schedule_item_details_note_content)
        val addOrEditLessonNoteButton = findViewById<Button>(R.id.add_or_edit_lesson_note)

        val dbManager = DatabaseManager(this)
        if(noteId == -1){
            dialogBuilder.setTitle(getString(R.string.add_note))
            dialogBuilder.setPositiveButton(getString(R.string.save)) { _, _ ->
                val insertCount = dbManager.addLessonNoteToDb(scheduleItem, noteContentField.text.toString())
                val noteContent = noteContentField.text.toString()
                if(insertCount > 0){
                    noteContentTextView.text = noteContent
                    noteContentTextView.visibility = View.VISIBLE
                    addOrEditLessonNoteButton.text = getString(R.string.edit_note)
                }else{
                    Toast.makeText(this, getString(R.string.note_adding_error), Toast.LENGTH_SHORT).show()
                }
            }
        }else{
            dialogBuilder.setTitle(getString(R.string.edit_note))
            noteContentField.setText(dbNoteContent, TextView.BufferType.EDITABLE)
            dialogBuilder.setPositiveButton(getString(R.string.save)){_, _ ->
                val updateCount = dbManager.updateLessonNote(scheduleItem, noteContentField.text.toString(), noteId)
                val noteContent = noteContentField.text.toString()
                if(updateCount > 0){
                    noteContentTextView.text = noteContent
                }else{
                    Toast.makeText(this, getString(R.string.note_update_error), Toast.LENGTH_SHORT).show()
                }
            }
            dialogBuilder.setNeutralButton(getString(R.string.delete)){_,_ ->
                val deleteDialogBuilder = AlertDialog.Builder(this)
                deleteDialogBuilder.setTitle(getString(R.string.note_delete_message))
                deleteDialogBuilder.setPositiveButton(getString(R.string.yes)){_,_ ->
                    val deleteCount = dbManager.removeLessonNote(noteId)
                    if(deleteCount > 0){
                        noteContentTextView.text = ""
                        noteContentTextView.visibility = View.GONE
                        addOrEditLessonNoteButton.text = getString(R.string.add_note)
                    }else{
                        Toast.makeText(this, getString(R.string.note_delete_error), Toast.LENGTH_SHORT).show()
                    }
                }
                deleteDialogBuilder.setNegativeButton(getString(R.string.no)){_,_ ->
                    //pass
                }
                deleteDialogBuilder.create().show()
            }
        }
        dialogBuilder.setNegativeButton(getString(R.string.cancel)) {_, _ ->
            //pass
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit {
            putBoolean(getString(R.string.PREFS_REFRESH_SCHEDULE), true)
        }
        dialogBuilder.create().show()
    }
}
