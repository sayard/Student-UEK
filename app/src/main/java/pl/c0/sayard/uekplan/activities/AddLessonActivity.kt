package pl.c0.sayard.uekplan.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.*
import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.Utils.Companion.getTime
import pl.c0.sayard.uekplan.data.Building
import pl.c0.sayard.uekplan.db.ScheduleContract
import pl.c0.sayard.uekplan.db.ScheduleDbHelper
import java.text.SimpleDateFormat
import java.util.*

class AddLessonActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener{

    private val dateCalendar: Calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")


    override fun onDateSet(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        dateCalendar.set(Calendar.YEAR, year)
        dateCalendar.set(Calendar.MONTH, monthOfYear)
        dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val dateTv = findViewById<TextView>(R.id.custom_lesson_date_tv)
        dateTv.text = dateFormat.format(dateCalendar.time)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_lesson)

        val dateTv = findViewById<TextView>(R.id.custom_lesson_date_tv)
        dateTv.text = dateFormat.format(dateCalendar.time)
        dateTv.setOnClickListener {
            val dpDialog = DatePickerDialog(
                    this@AddLessonActivity,
                    this@AddLessonActivity,
                    dateCalendar.get(Calendar.YEAR),
                    dateCalendar.get(Calendar.MONTH),
                    dateCalendar.get(Calendar.DAY_OF_MONTH)
            )
            dpDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            dpDialog.show()
        }

        val startHourTv = findViewById<TextView>(R.id.custom_lesson_start_hour_tv)
        startHourTv.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val hour = currentTime.get(Calendar.HOUR_OF_DAY)
            val minute = currentTime.get(Calendar.MINUTE)
            val timePicker = TimePickerDialog(
                    this@AddLessonActivity,
                    TimePickerDialog.OnTimeSetListener
                    { _, selectedHour, selectedMinute -> startHourTv.text = String.format("%02d:%02d", selectedHour, selectedMinute) },
                    hour,
                    minute,
                    true
            )
            timePicker.setTitle(getString(R.string.p_e_start))
            timePicker.show()
        }

        val endHourTv = findViewById<TextView>(R.id.custom_lesson_end_hour_tv)
        endHourTv.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val hour = currentTime.get(Calendar.HOUR_OF_DAY)
            val minute = currentTime.get(Calendar.MINUTE)
            val timePicker = TimePickerDialog(
                    this@AddLessonActivity,
                    TimePickerDialog.OnTimeSetListener
                    { _, selectedHour, selectedMinute -> endHourTv.text = String.format("%02d:%02d", selectedHour, selectedMinute) },
                    hour,
                    minute,
                    true
            )
            timePicker.setTitle(getString(R.string.p_e_end))
            timePicker.show()
        }

        val buildingSpinner = findViewById<Spinner>(R.id.custom_lesson_building)
        val buildingInstance = Building(this)
        val spinnerAdapter = ArrayAdapter<String>(
                this, R.layout.building_spinner_layout, R.id.building_spinner_layout_tv, buildingInstance.getBuildingList()
        )
        spinnerAdapter.setDropDownViewResource(R.layout.building_spinner_layout)
        buildingSpinner.adapter = spinnerAdapter

        val saveButton = findViewById<Button>(R.id.save_custom_lesson_button)
        saveButton.setOnClickListener {
            val nameTv = findViewById<EditText>(R.id.custom_lesson_name)
            if(nameTv.text.isEmpty()){
                Toast.makeText(this@AddLessonActivity, getString(R.string.custom_lesson_name_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val startTime = getTime(startHourTv)
            val endTime = getTime(endHourTv)
            if(startTime.timeInMillis > endTime.timeInMillis){
                Toast.makeText(this@AddLessonActivity, getString(R.string.hour_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var selectedBuilding = buildingSpinner.selectedItem.toString()
            val classroomTv = findViewById<TextView>(R.id.custom_lesson_classroom)
            val classroom = classroomTv.text.toString()
            var buildingAbbreviation = ""
            if(selectedBuilding == getString(R.string.building)){
                if(classroom.contains("30 koło") || classroom.contains("kortów")){
                    selectedBuilding = buildingInstance.MAIN_BUILDING
                    buildingAbbreviation = buildingInstance.getBuildingAbbreviation(selectedBuilding)
                }
            }else{
                buildingAbbreviation = buildingInstance.getBuildingAbbreviation(selectedBuilding)
            }

            val dbHelper = ScheduleDbHelper(this@AddLessonActivity)
            val db = dbHelper.readableDatabase
            val contentValues = ContentValues()
            contentValues.put(ScheduleContract.UserAddedLessonEntry.SUBJECT, nameTv.text.toString())
            val typeTv = findViewById<TextView>(R.id.custom_lesson_type)
            contentValues.put(ScheduleContract.UserAddedLessonEntry.TYPE, typeTv.text.toString())
            val teacherTv = findViewById<TextView>(R.id.custom_lesson_teacher)
            contentValues.put(ScheduleContract.UserAddedLessonEntry.TEACHER, teacherTv.text.toString())
            contentValues.put(ScheduleContract.UserAddedLessonEntry.CLASSROOM, "$buildingAbbreviation$classroom")
            contentValues.put(ScheduleContract.UserAddedLessonEntry.DATE, dateTv.text.toString())
            contentValues.put(ScheduleContract.UserAddedLessonEntry.START_HOUR, "${startHourTv.text}")
            contentValues.put(ScheduleContract.UserAddedLessonEntry.END_HOUR, "${endHourTv.text}")
            db.insert(ScheduleContract.UserAddedLessonEntry.TABLE_NAME, null, contentValues)
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@AddLessonActivity)
            val editor = prefs.edit()
            editor.putBoolean(getString(R.string.PREFS_REFRESH_SCHEDULE), true)
            editor.apply()
            val intent = Intent(this@AddLessonActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
