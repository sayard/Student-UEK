package pl.c0.sayard.uekplan.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by Karol on 1/1/2018.
 */
class ScheduleDbHelper(context: Context): SQLiteOpenHelper(context, "ScheduleUEK.db", null, 1) {

    private val SQL_CREATE_GROUP = "CREATE TABLE IF NOT EXISTS " +
            ScheduleContract.GroupEntry.TABLE_NAME + "( " +
            ScheduleContract.GroupEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ScheduleContract.GroupEntry.GROUP_NAME + " TEXT NOT NULL, " +
            ScheduleContract.GroupEntry.GROUP_URL + " TEXT NOT NULL)"


    private val SQL_DELETE_GROUP = "DROP TABLE IF EXISTS " + ScheduleContract.GroupEntry.TABLE_NAME

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_GROUP)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_GROUP)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
}
