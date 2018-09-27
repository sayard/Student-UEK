package pl.c0.sayard.studentUEK

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.c0.sayard.studentUEK.Utils.Companion.getGroupURL
import pl.c0.sayard.studentUEK.Utils.Companion.getTeacherURL
import pl.c0.sayard.studentUEK.Utils.Companion.getTitleBasedOnPosition
import pl.c0.sayard.studentUEK.Utils.Companion.isInFilteredLessons
import pl.c0.sayard.studentUEK.data.FilteredLesson
import pl.c0.sayard.studentUEK.data.Group
import pl.c0.sayard.studentUEK.data.ScheduleItem
import java.text.SimpleDateFormat
import java.util.*

class UtilsUnitTest {
    @Test
    fun getGroupUrlTest(){
        val testGroup = Group(123, "testName")
        assertEquals("http://planzajec.uek.krakow.pl/index.php?xml&typ=G&id=123&okres=4",
                getGroupURL(testGroup, true))
        assertEquals("http://planzajec.uek.krakow.pl/index.php?xml&typ=G&id=123&okres=1",
                getGroupURL(testGroup, false))
    }

    @Test
    fun getTeacherUrlTest(){
        var testGroup: Group? = Group(123, "testName")
        assertEquals("http://planzajec.uek.krakow.pl/index.php?xml&typ=N&id=123&okres=4",
                getTeacherURL(testGroup, true))
        assertEquals("http://planzajec.uek.krakow.pl/index.php?xml&typ=N&id=123&okres=1",
                getTeacherURL(testGroup, false))
        testGroup = null
        assertEquals("http://planzajec.uek.krakow.pl/index.php?xml&typ=N&id=null&okres=4",
                getTeacherURL(testGroup, true))
        assertEquals("http://planzajec.uek.krakow.pl/index.php?xml&typ=N&id=null&okres=1",
                getTeacherURL(testGroup, false))
    }

    @Test
    fun getTitleBasedOnPositionTest(){
        assertEquals(R.string.courses, getTitleBasedOnPosition(0))
        assertEquals(R.string.search, getTitleBasedOnPosition(1))
        assertEquals(R.string.schedule, getTitleBasedOnPosition(2))
        assertEquals(R.string.notes, getTitleBasedOnPosition(3))
        assertEquals(R.string.settings, getTitleBasedOnPosition(4))
        assertEquals(R.string.app_name, getTitleBasedOnPosition(5))
    }

    @Test
    fun isInFilteredLessonsTest(){
        val testScheduleItem = ScheduleItem("testSubject",
                "testType",
                "testTeacher",
                0,
                "testClassroom",
                "",
                "2018-04-26",
                "2018-04-26 9:00",
                "2018-04-26 13:00",
                false,
                false)
        val dateFormatShort = SimpleDateFormat("yyyy-MM-dd", Locale("pl", "PL"))
        val dayOfTheWeekFormat = SimpleDateFormat("EEEE")
        val date = dateFormatShort.parse("2018-04-26")
        val calendar = Calendar.getInstance()
        calendar.time = testScheduleItem.startDate
        val filteredLessonList = mutableListOf(
                FilteredLesson(1,
                        "testSubject1",
                        "testType1",
                        "testTeacher1",
                        1,
                        dayOfTheWeekFormat.format(date),
                        "9:00")
        )
        assertEquals(false, isInFilteredLessons(testScheduleItem, filteredLessonList))
        filteredLessonList.add(
                FilteredLesson(2,
                        "testSubject",
                        "testType",
                        "testTeacher",
                        0,
                        dayOfTheWeekFormat.format(date),
                        "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}")
        )
        assertEquals(true, isInFilteredLessons(testScheduleItem, filteredLessonList))
    }
}