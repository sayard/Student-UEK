package pl.c0.sayard.uekplan.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.*

import pl.c0.sayard.uekplan.R
import pl.c0.sayard.uekplan.activities.AddNoteActivity
import pl.c0.sayard.uekplan.adapters.NotesAdapter
import pl.c0.sayard.uekplan.data.Note
import pl.c0.sayard.uekplan.parsers.NotesParser


class NotesFragment : Fragment() {

    companion object {
        fun newInstance(): NotesFragment{
            return NotesFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater!!.inflate(R.layout.fragment_notes, container, false)
        val notesMessage = view.findViewById<TextView>(R.id.notes_message)
        val listView = view.findViewById<ListView>(R.id.notes_list_view)
        val progressBar = view.findViewById<ProgressBar>(R.id.notes_progress_bar)
        progressBar.visibility = View.VISIBLE

        executeNotesParser(progressBar, notesMessage, listView)

        setHasOptionsMenu(true)
        return view
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
        if(view != null){
            val progressBar = view!!.findViewById<ProgressBar>(R.id.notes_progress_bar)
            val listView = view!!.findViewById<ListView>(R.id.notes_list_view)
            val notesMessage = view!!.findViewById<TextView>(R.id.notes_message)
            executeNotesParser(progressBar, notesMessage, listView)
        }
        activity.title = getString(R.string.notes)
    }

    private fun executeNotesParser(progressBar: ProgressBar, notesMessage: TextView, listView: ListView){
        NotesParser(this, object: NotesParser.OnTaskCompleted{

            override fun onTaskCompleted(result: List<Note>?, fragment: NotesFragment) {
                progressBar.visibility = View.GONE
                if(result == null){
                    notesMessage.visibility = View.VISIBLE
                }else{
                    val adapter = NotesAdapter(context, result.toMutableList())
                    listView.adapter = adapter
                    listView.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
                        Toast.makeText(context, "ASD", Toast.LENGTH_SHORT).show()
                        val note = parent.getItemAtPosition(position) as Note
                        val intent = Intent(context, AddNoteActivity::class.java)
                        intent.putExtra(getString(R.string.note_id_extra), note.id)
                        startActivity(intent)
                    }
                }
            }

        }).execute()
    }

}
