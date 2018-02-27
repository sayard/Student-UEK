package pl.c0.sayard.uekplan.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by Karol on 1/1/2018.
 */
class ScheduleDbHelper(context: Context): SQLiteOpenHelper(context, "ScheduleUEK.db", null, 7) {

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

    private val SQL_CREATE_LESSON = "CREATE TABLE IF NOT EXISTS " +
            ScheduleContract.LessonEntry.TABLE_NAME + "( " +
            ScheduleContract.LessonEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ScheduleContract.LessonEntry.SUBJECT + " TEXT NOT NULL, " +
            ScheduleContract.LessonEntry.TYPE + " TEXT NOT NULL, " +
            ScheduleContract.LessonEntry.TEACHER + " TEXT NOT NULL, " +
            ScheduleContract.LessonEntry.TEACHER_ID + " INTEGER NOT NULL, " +
            ScheduleContract.LessonEntry.CLASSROOM + " TEXT NOT NULL, " +
            ScheduleContract.LessonEntry.COMMENTS + " TEXT NOT NULL, " +
            ScheduleContract.LessonEntry.DATE + " TEXT ," +
            ScheduleContract.LessonEntry.START_DATE + " TEXT NOT NULL, " +
            ScheduleContract.LessonEntry.END_DATE + " TEXT NOT NULL)"

    private val SQL_DELETE_LESSON = "DROP TABLE IF EXISTS " + ScheduleContract.LessonEntry.TABLE_NAME

    private val SQL_CREATE_USER_ADDED_LESSON = "CREATE TABLE IF NOT EXISTS " +
            ScheduleContract.UserAddedLessonEntry.TABLE_NAME + "( " +
            ScheduleContract.UserAddedLessonEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ScheduleContract.UserAddedLessonEntry.SUBJECT + " TEXT NOT NULL, " +
            ScheduleContract.UserAddedLessonEntry.TYPE + " TEXT DEFAULT \"\", " +
            ScheduleContract.UserAddedLessonEntry.TEACHER + " TEXT DEFAULT \"\", " +
            ScheduleContract.UserAddedLessonEntry.CLASSROOM + " TEXT DEFAULT \"\"," +
            ScheduleContract.UserAddedLessonEntry.DATE + " TEXT NOT NULL, " +
            ScheduleContract.UserAddedLessonEntry.START_HOUR + " TEXT NOT NULL, " +
            ScheduleContract.UserAddedLessonEntry.END_HOUR + " TEXT NOT NULL) "

    private val SQL_DELETE_USER_ADDED_LESSON = "DROP TABLE IF EXISTS " + ScheduleContract.UserAddedLessonEntry.TABLE_NAME

    private val SQL_CREATE_NOTES = "CREATE TABLE IF NOT EXISTS " +
            ScheduleContract.NotesEntry.TABLE_NAME + "( " +
            ScheduleContract.NotesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            ScheduleContract.NotesEntry.TITLE + " TEXT DEFAULT \"\", " +
            ScheduleContract.NotesEntry.CONTENT + " TEXT DEFAULT \"\", " +
            ScheduleContract.NotesEntry.DATE + " TEXT NOT NULL, " +
            ScheduleContract.NotesEntry.HOUR + " TEXT NOT NULL)"

    private val SQL_DELETE_NOTES = "DROP TABLE IF EXISTS " + ScheduleContract.NotesEntry.TABLE_NAME

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_GROUP)
        db.execSQL(SQL_CREATE_LANGUAGE_GROUP)
        db.execSQL(SQL_CREATE_PE)
        db.execSQL(SQL_CREATE_LESSON)
        db.execSQL(SQL_CREATE_USER_ADDED_LESSON)
        db.execSQL(SQL_CREATE_NOTES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_GROUP)
        db.execSQL(SQL_DELETE_LANGUAGE_GROUP)
        db.execSQL(SQL_DELETE_PE)
        db.execSQL(SQL_DELETE_LESSON)
        db.execSQL(SQL_DELETE_USER_ADDED_LESSON)
        db.execSQL(SQL_DELETE_NOTES)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
}
