package pl.c0.sayard.studentUEK

import android.preference.PreferenceManager
import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.c0.sayard.studentUEK.db.DatabaseManager
import org.junit.Assert.*
import pl.c0.sayard.studentUEK.Utils.Companion.getGroupURL
import pl.c0.sayard.studentUEK.data.*
import pl.c0.sayard.studentUEK.db.ScheduleDbHelper

@RunWith(AndroidJUnit4::class)
@LargeTest
class DatabaseManagerInstrumentedTest {

    private var dbManager: DatabaseManager? = null
    private val calculusExercises = ScheduleItem("Analiza matematyczna i algebra liniowa", "ćwiczenia", "dr Jakub Bielawski", 540, "Paw.F 102", "", "2018-05-07", "2018-05-07 07:50", "2018-05-07 09:30", true)
    private val economicsDiscourse = ScheduleItem("Ekonomia", "wykład", "dr Barbara Kieniewicz", 356, "Paw.C Nowa Aula", "", "2018-05-09", "2018-05-09 11:20", "2018-05-09 13:00", true)
    private val calculusReschedule = ScheduleItem("Analiza matematyczna i algebra liniowa", "Przeniesienie zajęć", "dr Jakub Bielawski", 540, "", "na 7 V", "2018-05-09", "2018-05-09 14:50", "2018-05-09 16:25", false)
    private val customScheduleItem = ScheduleItem("Custom lesson subject", "lektorat", "Custom lesson teacher", -1, "Custom lesson classroom", "", "2018-05-10", "2018-05-10 12:00", "2018-05-10 13:00", true, true)
    private val customScheduleItemWithNote = ScheduleItem("Custom lesson subject", "lektorat", "Custom lesson teacher", -1, "Custom lesson classroom", "", "2018-05-10", "2018-05-10 12:00", "2018-05-10 13:00", true, true, noteId=1, noteContent = "lesson note test")

    @Before
    fun setUp(){
        instantiateNewDatabase()
    }

    private fun instantiateNewDatabase(){
        val context = InstrumentationRegistry.getTargetContext()
        InstrumentationRegistry.getTargetContext().deleteDatabase("TestDb.db")
        dbManager = DatabaseManager(context, ScheduleDbHelper(context, "TestDb.db", 1))
        val lessonList = listOf(
                Lesson("2018-05-07", "07:50", "09:30", "Analiza matematyczna i algebra liniowa", "ćwiczenia", "dr Jakub Bielawski", "-540", "Paw.F 102", ""),
                Lesson("2018-05-09", "11:20", "13:00", "Ekonomia", "wykład", "dr Barbara Kieniewicz", "-356", "Paw.C Nowa Aula", ""),
                Lesson("2018-05-09", "14:50", "16:25", "Analiza matematyczna i algebra liniowa", "Przeniesienie zajęć", "dr Jakub Bielawski", "-540", "", "na 7 V"),
                Lesson("2018-05-10", "12:00", "13:00", "Custom lesson subject", "lektorat", "Custom lesson teacher", "--1", "Custom lesson classroom", "", true, 1)
        )
        dbManager!!.addLessonsToDb(lessonList)
    }

    @Test
    fun testPreConditions(){
        assertNotNull(dbManager)
    }

    @Test
    fun getOrdinaryScheduleListTest(){

        val expectedScheduleList = mutableListOf(
                calculusExercises,
                economicsDiscourse,
                calculusReschedule,
                customScheduleItem
        )

        val scheduleList = dbManager!!.getScheduleList()
        assertEquals(expectedScheduleList, scheduleList)

    }

    @Test
    fun discoursesFilterTest(){

        val context = InstrumentationRegistry.getTargetContext()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val originalDiscoursesPref = prefs.getBoolean(context.getString(R.string.PREFS_DISCOURSES_VISIBLE), true)
        val expectedScheduleList = listOf(
                calculusExercises,
                calculusReschedule,
                customScheduleItem
        )

        prefs.edit().putBoolean(context.getString(R.string.PREFS_DISCOURSES_VISIBLE), false).apply()
        instantiateNewDatabase()
        val scheduleList = dbManager!!.getScheduleList()
        assertEquals(expectedScheduleList, scheduleList)
        prefs.edit().putBoolean(context.getString(R.string.PREFS_DISCOURSES_VISIBLE), originalDiscoursesPref).apply()

    }

    @Test
    fun exercisesFilterTest(){

        val context = InstrumentationRegistry.getTargetContext()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val originalExercisesPref = prefs.getBoolean(context.getString(R.string.PREFS_EXERCISES_VISIBLE), true)
        val expectedScheduleList = listOf(
                economicsDiscourse,
                calculusReschedule,
                customScheduleItem
        )

        prefs.edit().putBoolean(context.getString(R.string.PREFS_EXERCISES_VISIBLE), false).apply()
        instantiateNewDatabase()
        val scheduleList = dbManager!!.getScheduleList()
        assertEquals(expectedScheduleList, scheduleList)
        prefs.edit().putBoolean(context.getString(R.string.PREFS_EXERCISES_VISIBLE), originalExercisesPref).apply()

    }

    @Test
    fun lecturesFilterTest(){

        val context = InstrumentationRegistry.getTargetContext()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val originalLecturesPref = prefs.getBoolean(context.getString(R.string.PREFS_LECTURES_VISIBLE), true)
        val expectedScheduleList = listOf(
                calculusExercises,
                economicsDiscourse,
                calculusReschedule
        )

        prefs.edit().putBoolean(context.getString(R.string.PREFS_LECTURES_VISIBLE), false).apply()
        instantiateNewDatabase()
        val scheduleList = dbManager!!.getScheduleList()
        assertEquals(expectedScheduleList, scheduleList) // test lectures filter
        prefs.edit().putBoolean(context.getString(R.string.PREFS_LECTURES_VISIBLE), originalLecturesPref).apply()
    }

    @Test
    fun addLessonNoteToDbTest(){
        val expectedScheduleList = listOf(
                calculusExercises,
                economicsDiscourse,
                calculusReschedule,
                customScheduleItemWithNote
        )
        instantiateNewDatabase()
        dbManager!!.addLessonNoteToDb(customScheduleItem, "lesson note test")
        val scheduleList = dbManager!!.getScheduleList()
        assertEquals(expectedScheduleList, scheduleList)
    }

    @Test
    fun updateLessonNoteTest(){
        val expectedScheduleList = listOf(
                calculusExercises,
                economicsDiscourse,
                calculusReschedule,
                customScheduleItemWithNote
        )
        instantiateNewDatabase()
        dbManager!!.addLessonNoteToDb(customScheduleItem, "lesson note pre-update")
        var scheduleList = dbManager!!.getScheduleList()
        assertNotEquals(expectedScheduleList, scheduleList)
        val updateCount = dbManager!!.updateLessonNote(customScheduleItem, "lesson note test", 1)
        scheduleList = dbManager!!.getScheduleList()
        assertEquals(1, updateCount)
        assertEquals(expectedScheduleList, scheduleList)
    }

    @Test
    fun removeLessonNoteTest(){
        val expectedScheduleList = listOf(
                calculusExercises,
                economicsDiscourse,
                calculusReschedule,
                customScheduleItem
        )
        instantiateNewDatabase()
        dbManager!!.addLessonNoteToDb(customScheduleItem, "lesson note test")
        val deleteCount = dbManager!!.removeLessonNote(1)
        val scheduleList = dbManager!!.getScheduleList()
        assertEquals(1, deleteCount)
        assertEquals(expectedScheduleList, scheduleList)
    }

    @Test
    fun addLessonToFilteredLessonsTest(){
        val expectedScheduleList = listOf(
                economicsDiscourse,
                calculusReschedule,
                customScheduleItem
        )
        instantiateNewDatabase()
        dbManager!!.addLessonToFilteredLessons(calculusExercises)
        val scheduleList = dbManager!!.getScheduleList()
        assertEquals(expectedScheduleList, scheduleList)
    }

    @Test
    fun removeFilteredLessonTest(){
        val expectedScheduleList = listOf(
                calculusExercises,
                economicsDiscourse,
                calculusReschedule,
                customScheduleItem
        )
        instantiateNewDatabase()
        dbManager!!.addLessonToFilteredLessons(calculusExercises)
        dbManager!!.removeFilteredLesson(1)
        val scheduleList = dbManager!!.getScheduleList()
        assertEquals(expectedScheduleList, scheduleList)
    }

    @Test
    fun getFilteredLessonsTest(){
        instantiateNewDatabase()
        assertEquals(mutableListOf<FilteredLesson>(), dbManager!!.getFilteredLessons())
        dbManager!!.addLessonToFilteredLessons(calculusExercises)
        val expectedFilteredList = listOf(
                FilteredLesson(1, "Analiza matematyczna i algebra liniowa", "ćwiczenia", "dr Jakub Bielawski", 540, "poniedziałek", "7:50")
        )
        assertEquals(expectedFilteredList, dbManager!!.getFilteredLessons())
    }

    @Test
    fun addAndGetGroupsTest(){
        val expectedGroups = listOf(
                ScheduleGroup("testGroupOne", getGroupURL(Group(84721, "testGroupOne"))),
                ScheduleGroup("testGroupTwo", getGroupURL(Group(84751, "testGroupTwo")))
        )
        instantiateNewDatabase()
        dbManager!!.addGroupsToDb(listOf(
                Group(84721, "testGroupOne"),
                Group(84751, "testGroupTwo")
        ))
        val groupList = dbManager!!.getGroups()
        assertEquals(expectedGroups, groupList)
    }

    @Test
    fun addAndGetLanguageGroupsTest(){
        val expectedGroups = listOf(
                ScheduleGroup("testLanguageGroupOne", getGroupURL(Group(12345, "testLanguageGroupOne"))),
                ScheduleGroup("testLanguageGroupOne", getGroupURL(Group(12346, "testLanguageGroupOne")))
        )
        instantiateNewDatabase()
        dbManager!!.addLanguageGroupsToDb(listOf(
                Group(12345, "testLanguageGroupOne"),
                Group(12346, "testLanguageGroupOne")
        ))
        val groupList = dbManager!!.getLanguageGroups()
        assertEquals(expectedGroups, groupList)
    }

    @Test
    fun addAndGetNoteTest(){
        val expectedNoteList = listOf(
                Note(1, "testNoteTitle1", "testNoteContent1", "2018-05-07", "12:00"),
                Note(2, "testNoteTitle2", "testNoteContent2", "2018-05-08", "13:00")
        )
        instantiateNewDatabase()
        for(note in expectedNoteList){
            dbManager!!.addNoteToDb(0, note.title, note.content, note.dateStr, note.hourStr)
        }
        val notesCursor = dbManager!!.getNotesCursor()
        val notesList = dbManager!!.getNotesListFromCursor(notesCursor)
        assertEquals(expectedNoteList, notesList)
    }

    @Test
    fun getNotesCursorByIdTest(){
        val expectedNoteList = listOf(
                Note(2, "testNoteTitle2", "testNoteContent2", "2018-05-08", "13:00")
        )
        instantiateNewDatabase()
        dbManager!!.addNoteToDb(0, "testNoteTitle1", "testNoteContent1", "2018-05-07", "12:00")
        dbManager!!.addNoteToDb(0, "testNoteTitle2", "testNoteContent2", "2018-05-08", "13:00")
        var notesCursor = dbManager!!.getNotesCursorById(2)
        var notesList = dbManager!!.getNotesListFromCursor(notesCursor)
        assertEquals(expectedNoteList, notesList)
        notesCursor = dbManager!!.getNotesCursorById(3)
        notesList = dbManager!!.getNotesListFromCursor(notesCursor)
        assertEquals(listOf<Note>(), notesList)
    }

    @Test
    fun removeNotesFromDbTest(){
        val note1 = Note(1, "testNoteTitle1", "testNoteContent1", "2018-05-07", "12:00")
        val note2 = Note(2, "testNoteTitle2", "testNoteContent2", "2018-05-08", "13:00")
        val expectedNoteList = listOf(
                note2
        )
        instantiateNewDatabase()
        dbManager!!.addNoteToDb(0, note1.title, note1.content, note1.dateStr, note1.hourStr)
        dbManager!!.addNoteToDb(0, note2.title, note2.content, note2.dateStr, note2.hourStr)
        val deleteCount = dbManager!!.getNotesDeleteCount(note1)
        assertEquals(1, deleteCount)
        assertEquals(expectedNoteList, dbManager!!.getNotesListFromCursor(dbManager!!.getNotesCursor()))
    }

}