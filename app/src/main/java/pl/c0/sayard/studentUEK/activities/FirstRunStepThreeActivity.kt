package pl.c0.sayard.studentUEK.activities

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import pl.c0.sayard.studentUEK.Utils.Companion.AUTOMATIC_SCHEDULE_REFRESH_PREFS_KEY
import pl.c0.sayard.studentUEK.Utils.Companion.FIRST_RUN_SHARED_PREFS_KEY
import pl.c0.sayard.studentUEK.Utils.Companion.getTime
import pl.c0.sayard.studentUEK.db.DatabaseManager
import pl.c0.sayard.studentUEK.jobs.RefreshScheduleJob
import java.util.*

class FirstRunStepThreeActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.onActivityCreateSetTheme(this)
        setContentView(R.layout.activity_first_run_step_three)
        val switch = findViewById<Switch>(R.id.pe_switch)
        val peName = findViewById<EditText>(R.id.pe_name)
        val dayOfWeekSpinner = findViewById<Spinner>(R.id.day_of_week_spinner)
        dayOfWeekSpinner.adapter = ArrayAdapter<String>(
                this,
                R.layout.day_of_week_spinner_layout,
                R.id.day_of_week_spinner_layout_tv,
                resources.getStringArray(R.array.days_of_week))
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
        nextStepButton.setOnClickListener(View.OnClickListener {
            val dbManager = DatabaseManager(this)
            dbManager.clearPeTable()
            if(switch.isChecked){
                if(peName.text.isEmpty()){
                    Toast.makeText(this@FirstRunStepThreeActivity, getString(R.string.pe_name_field_error), Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }
                val startTime = getTime(startHourTv)
                val endTime = getTime(endHourTv)
                if(startTime.timeInMillis > endTime.timeInMillis){
                    Toast.makeText(this@FirstRunStepThreeActivity, getString(R.string.hour_error), Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                }
                dbManager.addPeToDb(
                        peName.text.toString(),
                        dayOfWeekSpinner.selectedItemPosition,
                        startHourTv.text.toString(),
                        endHourTv.text.toString()
                )
            }
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            if(prefs.getBoolean(FIRST_RUN_SHARED_PREFS_KEY, true)){
                RefreshScheduleJob.schedule()
                prefs.edit()?.putBoolean(AUTOMATIC_SCHEDULE_REFRESH_PREFS_KEY, true)?.apply()
            }
            prefs.edit()?.putBoolean(FIRST_RUN_SHARED_PREFS_KEY, false)?.apply()
            val intent = Intent(this@FirstRunStepThreeActivity, MainActivity::class.java)
            startActivity(intent)
        })
    }
}
