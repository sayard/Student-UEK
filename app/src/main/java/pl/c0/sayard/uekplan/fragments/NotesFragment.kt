package pl.c0.sayard.uekplan.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*

import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.activities.AddNoteActivity


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
        setHasOptionsMenu(true)
        return inflater!!.inflate(R.layout.fragment_notes, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.notes_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.new_note_item -> {
                val newNoteIntent = Intent(context, AddNoteActivity::class.java)
                startActivity(newNoteIntent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        activity.title = getString(R.string.notes)
    }

}
