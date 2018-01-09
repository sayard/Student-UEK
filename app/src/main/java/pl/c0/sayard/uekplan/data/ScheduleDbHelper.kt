package pl.c0.sayard.uekplan.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by Karol on 1/1/2018.
 */
class ScheduleDbHelper(context: Context): SQLiteOpenHelper(context, "ScheduleUEK.db", null, 3) {

    private val SQL_CREATE_GROUP = "CREATE TABLE IF NOT EXISTS " +
            ScheduleContract.GroupEntry.TABLE_NAME + "( " +
            ScheduleContract.GroupEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ScheduleContract.GroupEntry.GROUP_NAME + " TEXT NOT NULL, " +
            ScheduleContract.GroupEntry.GROUP_URL + " TEXT NOT NULL)"


    private val SQL_DELETE_GROUP = "DROP TABLE IF EXISTS " + ScheduleContract.GroupEntry.TABLE_NAME

    private val SQL_CREATE_LANGUAGE_GROUP = "CREATE TABLE IF NOT EXISTS " +
            ScheduleContract.LanguageGroupsEntry.TABLE_NAME + "( " +
            ScheduleContract.LanguageGroupsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ScheduleContract.LanguageGroupsEntry.LANGUAGE_GROUP_NAME + " TEXT NOT NULL, " +
            ScheduleContract.LanguageGroupsEntry.LANGUAGE_GROUP_URL + " TEXT NOT NULL)"

    private val SQL_DELETE_LANGUAGE_GROUP = "DROP TABLE IF EXISTS " +
            ScheduleContract.LanguageGroupsEntry.TABLE_NAME

    private val SQL_CREATE_PE = "CREATE TABLE IF NOT EXISTS " +
            ScheduleContract.PeEntry.TABLE_NAME + "( " +
            ScheduleContract.PeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ScheduleContract.PeEntry.PE_NAME + " TEXT NOT NULL, " +
            ScheduleContract.PeEntry.PE_DAY + " INTEGER NOT NULL, " +
            ScheduleContract.PeEntry.PE_START_HOUR + " TEXT NOT NULL, " +
            ScheduleContract.PeEntry.PE_END_HOUR + " TEXT NOT NULL)"

    private val SQL_DELETE_PE = "DROP TABLE IF EXISTS " + ScheduleContract.PeEntry.TABLE_NAME

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_GROUP)
        db.execSQL(SQL_CREATE_LANGUAGE_GROUP)
        db.execSQL(SQL_CREATE_PE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_GROUP)
        db.execSQL(SQL_DELETE_LANGUAGE_GROUP)
        db.execSQL(SQL_DELETE_PE)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
}
