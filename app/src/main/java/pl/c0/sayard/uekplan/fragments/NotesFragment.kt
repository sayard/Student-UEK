package pl.c0.sayard.uekplan.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import pl.c0.sayard.uekplan.R


class NotesFragment : Fragment() {

    companion object {
        fun newInstance(): NotesFragment{
            return NotesFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_notes, container, false)
    }

}
