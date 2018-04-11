package pl.c0.sayard.studentUEK.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import pl.c0.sayard.studentUEK.R

class MoodleFragment : Fragment() {

    companion object {
        fun newInstance(): MoodleFragment{
            return MoodleFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_moodle, container, false)

        val coursesView = view.findViewById<ScrollView>(R.id.moodle_courses_view)
        val loginView = view.findViewById<ConstraintLayout>(R.id.moodle_login_view)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if(prefs.getString(getString(R.string.pl_c0_sayard_StudentUEK_PREFS_MOODLE_TOKEN), null) != null){
            coursesView.visibility = View.VISIBLE
            loginView.visibility = View.GONE
        }else{
            coursesView.visibility = View.GONE
            loginView.visibility = View.VISIBLE

            val login = view.findViewById<EditText>(R.id.moodle_login)
            val password = view.findViewById<EditText>(R.id.moodle_password)
            val loginButton = view.findViewById<Button>(R.id.mooodle_login_button)
            val loginProgressBar = view.findViewById<ProgressBar>(R.id.moodle_login_progress)

            loginButton.setOnClickListener{
                Toast.makeText(context, "Login: ${login.text} Password: ${password.text}", Toast.LENGTH_SHORT).show()
            }
            password.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if((event?.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)){
                    loginButton.performClick()
                    return@OnKeyListener true
                }
                false
            })
        }

        return view
    }

}