package pl.c0.sayard.uekplan

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import pl.c0.sayard.uekplan.data.ScheduleContract
import pl.c0.sayard.uekplan.data.ScheduleDbHelper
import java.util.*

class FirstRunStepThreeActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_run_step_three)
        val switch = findViewById<Switch>(R.id.pe_switch)
        val peName = findViewById<EditText>(R.id.pe_name)
        val dayOfWeekSpinner = findViewById<Spinner>(R.id.day_of_week_spinner)
        val peHoursHeader = findViewById<LinearLayout>(R.id.pe_hours_header)
        val peHours = findViewById<LinearLayout>(R.id.pe_hours)
        switch.setOnCheckedChangeListener { p0, isChecked ->
            if(isChecked){
                peName.visibility = View.VISIBLE
                dayOfWeekSpinner.visibility = View.VISIBLE
                peHoursHeader.visibility = View.VISIBLE
                peHours.visibility = View.VISIBLE
            }else{
                peName.visibility = View.GONE
                dayOfWeekSpinner.visibility = View.GONE
                peHoursHeader.visibility = View.GONE
                peHours.visibility = View.GONE
            }
        }
        val startHourTv = findViewById<TextView>(R.id.start_hour_tv)
        startHourTv.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val hour = currentTime.get(Calendar.HOUR_OF_DAY)
            val minute = currentTime.get(Calendar.MINUTE)
            val timePicker = TimePickerDialog(
                    this@FirstRunStepThreeActivity,
                    TimePickerDialog.OnTimeSetListener
                    { _, selectedHour, selectedMinute -> startHourTv.text = String.format("%02d:%02d", selectedHour, selectedMinute) },
                    hour,
                    minute,
                    true
            )
            timePicker.setTitle(getString(R.string.p_e_start))
            timePicker.show()
        }
        val endHourTv = findViewById<TextView>(R.id.end_hour_tv)
        endHourTv.setOnClickListener {
            val currentTime = Calendar.getInstance()
            val hour = currentTime.get(Calendar.HOUR_OF_DAY)
            val minute = currentTime.get(Calendar.MINUTE)
            val timePicker = TimePickerDialog(
                    this@FirstRunStepThreeActivity,
                    TimePickerDialog.OnTimeSetListener
                    { _, selectedHour, selectedMinute -> endHourTv.text = String.format("%02d:%02d", selectedHour, selectedMinute) },
                    hour,
                    minute,
                    true
            )
            timePicker.setTitle(getString(R.string.p_e_end))
            timePicker.show()
        }
        val nextStepButton = findViewById<Button>(R.id.next_step_button_three)
        nextStepButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                val dbHelper = ScheduleDbHelper(this@FirstRunStepThreeActivity)
                val db = dbHelper.readableDatabase
                db.execSQL("DELETE FROM "+ScheduleContract.PeEntry.TABLE_NAME)
                if(switch.isChecked){
                    if(peName.text.isEmpty()){
                        Toast.makeText(this@FirstRunStepThreeActivity, getString(R.string.pe_name_field_error), Toast.LENGTH_SHORT).show()
                        return
                    }
                    val startTime = getTime(startHourTv)
                    val endTime = getTime(endHourTv)
                    if(startTime.timeInMillis > endTime.timeInMillis){
                        Toast.makeText(this@FirstRunStepThreeActivity, getString(R.string.pe_hour_error), Toast.LENGTH_SHORT).show()
                        return
                    }
                    val contentValues = ContentValues()
                    contentValues.put(ScheduleContract.PeEntry.PE_NAME, peName.text.toString())
                    contentValues.put(ScheduleContract.PeEntry.PE_DAY, dayOfWeekSpinner.selectedItemPosition)
                    contentValues.put(ScheduleContract.PeEntry.PE_START_HOUR, startHourTv.text.toString())
                    contentValues.put(ScheduleContract.PeEntry.PE_END_HOUR, endHourTv.text.toString())
                    db.insert(ScheduleContract.PeEntry.TABLE_NAME, null, contentValues)
                    val prefs = getSharedPreferences("pl.c0.sayard.uekplan", Context.MODE_PRIVATE)
                    prefs.edit()?.putBoolean("firstRun", false)?.apply()
                    val intent = Intent(this@FirstRunStepThreeActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        })
    }

    private fun getTime(hourTv: TextView): Calendar{
        val startHour = hourTv.text.substring(0, 2).toInt()
        val startMinute = hourTv.text.substring(3).toInt()
        val startTime = Calendar.getInstance()
        startTime.set(Calendar.HOUR_OF_DAY, startHour)
        startTime.set(Calendar.MINUTE, startMinute)
        return startTime
    }
}
