package pl.c0.sayard.studentUEK.activities

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import java.util.*

class CreditsActivity : AppCompatActivity() {

    private var eeCounter = 10
    private val eeList = mutableListOf<Int>()
    private val toastTexts = arrayOf(
            "All your course retake fees are belong to us",
            "Cowabunga!",
            "The dopefish lives",
            "It's a trap",
            "One app to rule them all",
            "Toasty!",
            "Battle city jest super",
            "Nobody expects the Spanish Inquisition",
            "Przestańcie przebiegać przez mój pawilon",
            "42",
            "Don't forget your multipass",
            "Can I pass this semester? LET'S FIND OUT!"
    )
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.onActivityCreateSetTheme(this)
        setContentView(R.layout.activity_credits)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        eeCounter = 10
        val androidName = findViewById<TextView>(R.id.credits_android_name)
        val iosName = findViewById<TextView>(R.id.credits_ios_name)
        val eeButtons = findViewById<ConstraintLayout>(R.id.ee_buttons)
        androidName.setOnClickListener{
            vibrator?.vibrate(100)
            eeCounter-=1
            if(eeCounter==0){
                eeButtons.visibility = View.VISIBLE
            }
        }
        iosName.setOnClickListener {
            vibrator?.vibrate(100)
            eeCounter-=1
            if(eeCounter==0){
                eeButtons.visibility = View.VISIBLE
            }
        }
        val eeUp = findViewById<Button>(R.id.ee_up)
        eeUp.setOnClickListener {checkKC(0)}
        val eeDown = findViewById<Button>(R.id.ee_down)
        eeDown.setOnClickListener {checkKC(1)}
        val eeLeft= findViewById<Button>(R.id.ee_left)
        eeLeft.setOnClickListener {checkKC(2)}
        val eeRight = findViewById<Button>(R.id.ee_right)
        eeRight.setOnClickListener {checkKC(3)}
        val eeA = findViewById<Button>(R.id.ee_a)
        eeA.setOnClickListener {checkKC(4)}
        val eeB = findViewById<Button>(R.id.ee_b)
        eeB.setOnClickListener {checkKC(5)}
    }

    private fun checkKC(buttonIndex: Int){
        vibrator?.vibrate(100)
        eeList.add(buttonIndex)
        if(eeList.size > 10){
            eeList.removeAt(0)
        }
        Log.v("TAG", eeList.toString())
        if(eeList.joinToString("") == "0011232354"){
            val random = Random().nextInt(toastTexts.size)
            Toast.makeText(this, toastTexts[random], Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        eeCounter = 10
        findViewById<ConstraintLayout>(R.id.ee_buttons).visibility = View.GONE
    }
}
