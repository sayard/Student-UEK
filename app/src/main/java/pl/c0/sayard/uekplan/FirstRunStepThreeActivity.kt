package pl.c0.sayard.uekplan

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
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
                    { _, selectedHour, selectedMinute -> startHourTv.text = "$selectedHour:$selectedMinute" },
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
                    { _, selectedHour, selectedMinute -> endHourTv.text = "$selectedHour:$selectedMinute" },
                    hour,
                    minute,
                    true
            )
            timePicker.setTitle(getString(R.string.p_e_end))
            timePicker.show()
        }
    }
}
