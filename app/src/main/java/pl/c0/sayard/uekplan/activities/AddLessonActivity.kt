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

        var nameText = ""
        var typeText = ""
        var teacherText = ""
        var buildingText = ""
        var classroomText = ""
        var dateText = dateFormat.format(dateCalendar.time)
        var startHourText = "12:00"
        var endHourText = "13:00"

        val intent = intent
        try{
            nameText = intent.getStringExtra(getString(R.string.extra_custom_lesson_name))
            typeText = intent.getStringExtra(getString(R.string.extra_custom_lesson_type))
            teacherText = intent.getStringExtra(getString(R.string.extra_custom_lesson_teacher))
            buildingText = intent.getStringExtra(getString(R.string.extra_custom_lesson_building))
            val buildingInstance = Building(this)
            classroomText = buildingInstance.getClassroomWithoutBuilding(intent.getStringExtra(getString(R.string.extra_custom_lesson_classroom)))
            dateText = dateFormat.format(intent.getStringExtra(getString(R.string.extra_custom_lesson_date)))
            startHourText = intent.getStringExtra(getString(R.string.extra_custom_lesson_start_hour))
            endHourText = intent.getStringExtra(getString(R.string.extra_custom_lesson_end_hour))
        }catch (e: Exception){
            //fail silently
        }

        val dateTv = findViewById<TextView>(R.id.custom_lesson_date_tv)
        dateTv.text = dateText
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
        startHourTv.text = startHourText
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
        endHourTv.text = endHourText
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
        if(buildingText != ""){
            val spinnerPosition = spinnerAdapter.getPosition(buildingText)
            buildingSpinner.setSelection(spinnerPosition)
        }

        val name = findViewById<EditText>(R.id.custom_lesson_name)
        name.setText(nameText, TextView.BufferType.EDITABLE)
        val classroom = findViewById<TextView>(R.id.custom_lesson_classroom)
        classroom.setText(classroomText, TextView.BufferType.EDITABLE)
        val teacher = findViewById<TextView>(R.id.custom_lesson_teacher)
        teacher.setText(teacherText, TextView.BufferType.EDITABLE)
        val type = findViewById<TextView>(R.id.custom_lesson_type)
        type.setText(typeText, TextView.BufferType.EDITABLE)

        val saveButton = findViewById<Button>(R.id.save_custom_lesson_button)
        saveButton.setOnClickListener {
            if(name.text.isEmpty()){
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
            val classroomVal = classroom.text.toString()
            var buildingAbbreviation = ""
            if(selectedBuilding == getString(R.string.building)){
                if(classroomVal.contains("30 koło") || classroomVal.contains("kortów")){
                    selectedBuilding = buildingInstance.MAIN_BUILDING
                    buildingAbbreviation = buildingInstance.getBuildingAbbreviation(selectedBuilding)
                }
            }else{
                buildingAbbreviation = buildingInstance.getBuildingAbbreviation(selectedBuilding)
            }

            val dbHelper = ScheduleDbHelper(this@AddLessonActivity)
            val db = dbHelper.readableDatabase
            val contentValues = ContentValues()
            contentValues.put(ScheduleContract.UserAddedLessonEntry.SUBJECT, name.text.toString())
            contentValues.put(ScheduleContract.UserAddedLessonEntry.TYPE, type.text.toString())
            contentValues.put(ScheduleContract.UserAddedLessonEntry.TEACHER, teacher.text.toString())
            contentValues.put(ScheduleContract.UserAddedLessonEntry.CLASSROOM, "$buildingAbbreviation$classroomVal")
            contentValues.put(ScheduleContract.UserAddedLessonEntry.DATE, dateTv.text.toString())
            contentValues.put(ScheduleContract.UserAddedLessonEntry.START_HOUR, "${startHourTv.text}")
            contentValues.put(ScheduleContract.UserAddedLessonEntry.END_HOUR, "${endHourTv.text}")
            val id = intent.getIntExtra(getString(R.string.extra_custom_id), -1)
            if(id == -1){
                db.insert(ScheduleContract.UserAddedLessonEntry.TABLE_NAME, null, contentValues)
            }else{
                db.update(ScheduleContract.UserAddedLessonEntry.TABLE_NAME,
                        contentValues,
                        "${ScheduleContract.UserAddedLessonEntry._ID} = $id",
                        null)
            }
            val prefs = PreferenceManager.getDefaultSharedPreferences(this@AddLessonActivity)
            val editor = prefs.edit()
            editor.putBoolean(getString(R.string.PREFS_REFRESH_SCHEDULE), true)
            editor.apply()
            val mainActivityIntent = Intent(this@AddLessonActivity, MainActivity::class.java)
            startActivity(mainActivityIntent)
        }
    }
}
