package pl.c0.sayard.studentUEK.parsers

import android.os.AsyncTask
import pl.c0.sayard.studentUEK.data.Note
import pl.c0.sayard.studentUEK.db.ScheduleContract
import pl.c0.sayard.studentUEK.db.ScheduleDbHelper
import pl.c0.sayard.studentUEK.fragments.NotesFragment

/**
 * Created by karol on 27.02.18.
 */
class NotesParser(private val fragment: NotesFragment, private val onTaskCompleted: OnTaskCompleted): AsyncTask<Void, Void, List<Note>>() {

    interface OnTaskCompleted{
        fun onTaskCompleted(result: List<Note>?, fragment: NotesFragment)
    }

    override fun doInBackground(vararg p0: Void?): List<Note> {
        val notesList = mutableListOf<Note>()
        if(fragment.context != null){
            val dbHelper = ScheduleDbHelper(fragment.context)
            val db = dbHelper.readableDatabase
            val notesCursor = db.query(
                    ScheduleContract.NotesEntry.TABLE_NAME,
                    null,
                    null,
                    null,
                    null,
                    null,
                    "${ScheduleContract.NotesEntry.DATE} ASC, ${ScheduleContract.NotesEntry.HOUR} ASC"
            )
            while(notesCursor.moveToNext()){
                notesList.add(Note(
                        notesCursor.getInt(notesCursor.getColumnIndex(ScheduleContract.NotesEntry._ID)),
                        notesCursor.getString(notesCursor.getColumnIndex(ScheduleContract.NotesEntry.TITLE)),
                        notesCursor.getString(notesCursor.getColumnIndex(ScheduleContract.NotesEntry.CONTENT)),
                        notesCursor.getString(notesCursor.getColumnIndex(ScheduleContract.NotesEntry.DATE)),
                        notesCursor.getString(notesCursor.getColumnIndex(ScheduleContract.NotesEntry.HOUR))
                ))
            }
            notesCursor.close()
        }
        return notesList
    }

    override fun onPostExecute(result: List<Note>?) {
        super.onPostExecute(result)
        onTaskCompleted.onTaskCompleted(result, fragment)
    }
}
