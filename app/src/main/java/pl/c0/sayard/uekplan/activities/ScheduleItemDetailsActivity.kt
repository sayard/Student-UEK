package pl.c0.sayard.uekplan.activities

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.data.Building
import pl.c0.sayard.uekplan.data.ScheduleItem
import java.text.SimpleDateFormat
import java.util.*

class ScheduleItemDetailsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val hourFormat = SimpleDateFormat("HH:mm", Locale("pl", "PL"))
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("pl", "PL"))
    private var scheduleItem: ScheduleItem? = null

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
                isCustom = intent.getBooleanExtra(getString(R.string.is_custom_extra), false)
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
        if(scheduleItem != null && !scheduleItem!!.isCustom ){
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
}
