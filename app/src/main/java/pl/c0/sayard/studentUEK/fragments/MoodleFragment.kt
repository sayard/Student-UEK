package pl.c0.sayard.studentUEK.fragments

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pl.c0.sayard.studentUEK.R

class MoodleFragment : Fragment() {

    companion object {
        fun newInstance(): MoodleFragment{
            return MoodleFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_moodle, container, false)

        return view
    }

}