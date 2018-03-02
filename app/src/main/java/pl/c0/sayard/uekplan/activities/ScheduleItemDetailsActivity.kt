package pl.c0.sayard.uekplan.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.data.Building
import pl.c0.sayard.uekplan.data.ScheduleItem
import pl.c0.sayard.uekplan.db.ScheduleContract
import pl.c0.sayard.uekplan.db.ScheduleDbHelper
import java.text.SimpleDateFormat
import java.util.*

class ScheduleItemDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val hourFormat = SimpleDateFormat("HH:mm", Locale("pl", "PL"))
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("pl", "PL"))
    private var scheduleItem: ScheduleItem? = null
    private var idToDelete: Int? = null
    private val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
        when(which){
            DialogInterface.BUTTON_POSITIVE ->{
                try{
                    val dbHelper = ScheduleDbHelper(this)
                    val db = dbHelper.readableDatabase
                    val deleteCount = db.delete(ScheduleContract.UserAddedLessonEntry.TABLE_NAME,
                            "${ScheduleContract.UserAddedLessonEntry._ID} = $idToDelete",
                            null)
                    if(deleteCount > 0){
                        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
                        val editor = prefs.edit()
                        editor.putBoolean(getString(R.string.PREFS_REFRESH_SCHEDULE), true)
                        editor.apply()
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
                customId = intent.getIntExtra(getString(R.string.custom_id_extra), -1)
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

        val mapFragment = supportFragmentManager.findFragmentById(R.id.schedule_item_details_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if(scheduleItem?.classroom == ""){
            supportFragmentManager.findFragmentById(R.id.schedule_item_details_map).view?.visibility = View.GONE
        }
        if(scheduleItem != null && !scheduleItem!!.isCustom && scheduleItem?.teacherId != 0){
            val teacherPageButton = findViewById<Button>(R.id.schedule_item_details_teacher_page_button)
            teacherPageButton.visibility = View.VISIBLE
            teacherPageButton.setOnClickListener {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://e-uczelnia.uek.krakow.pl/course/view.php?id=${scheduleItem?.teacherId}"))
                startActivity(browserIntent)
            }
        }
    }

    override fun onMapReady(map: GoogleMap?) {
        val buildingInstance = Building(this)
        val markerLatLng = buildingInstance.getBuildingLatLng(scheduleItem?.classroom)
        if(markerLatLng != null){
            map?.addMarker(MarkerOptions().position(markerLatLng).title(scheduleItem?.classroom))
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
}
