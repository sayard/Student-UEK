package pl.c0.sayard.studentUEK.parsers

import android.os.AsyncTask
import pl.c0.sayard.studentUEK.data.Note
import pl.c0.sayard.studentUEK.db.DatabaseManager
import pl.c0.sayard.studentUEK.fragments.NotesFragment

/**
 * Created by karol on 27.02.18.
 */
class NotesParser(private val fragment: NotesFragment, private val onTaskCompleted: OnTaskCompleted): AsyncTask<Void, Void, List<Note>>() {

    interface OnTaskCompleted{
        fun onTaskCompleted(result: List<Note>?)
    }

    override fun doInBackground(vararg p0: Void?): List<Note> {
        val notesList = mutableListOf<Note>()
        if(fragment.context != null){
            val dbManager = DatabaseManager(fragment.context)
            val notesCursor = dbManager.getNotesCursor()
            notesList.addAll(dbManager.getNotesListFromCursor(notesCursor))
        }
        return notesList
    }

    override fun onPostExecute(result: List<Note>?) {
        super.onPostExecute(result)
        onTaskCompleted.onTaskCompleted(result)
    }
}
