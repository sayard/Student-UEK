package pl.c0.sayard.studentUEK.data

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by karol on 12.01.18.
 */
class ScheduleItem(
        val subject: String,
        val type: String,
        val teacher: String,
        val teacherId: Int,
        val classroom: String,
        val comments: String,
        val dateStr: String,
        val startDateStr: String,
        val endDateStr: String,
        var isFirstOnTheDay: Boolean = false,
        var isCustom: Boolean = false,
        val customId: Int = -1,
        var noteId: Int = -1,
        var noteContent: String = ""){

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale("pl", "PL"))
    private val dateFormatShort = SimpleDateFormat("yyyy-MM-dd", Locale("pl", "PL"))
    var startDate: Date? = null
    var endDate: Date? = null
    private var date: Date? = null
    private val dayOfTheWeekFormat = SimpleDateFormat("EEEE")
    val calendar = Calendar.getInstance()!!
    var dayOfTheWeekStr: String? = null
    var dayOfTheWeek: Int? = null

    init {
        try{
            startDate = dateFormat.parse(startDateStr)
            endDate = dateFormat.parse(endDateStr)
            date = dateFormatShort.parse(dateStr)
            calendar.time = date
            dayOfTheWeekStr = dayOfTheWeekFormat.format(calendar.time)
            dayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK)
        }catch (e: ParseException) {
            e.printStackTrace()
        }
    }

    override fun toString(): String {
        return "$subject\n $type\n $teacher\n $teacherId\n $classroom\n comments:$comments\n $dateStr\n $isFirstOnTheDay\n $isCustom"
    }

    override fun equals(other: Any?): Boolean {
        val otherScheduleItem = other as ScheduleItem
        return (subject == otherScheduleItem.subject
                && type == otherScheduleItem.type
                && teacher == otherScheduleItem.teacher
                && teacherId == otherScheduleItem.teacherId
                && classroom == otherScheduleItem.classroom
                && comments == otherScheduleItem.comments
                && startDateStr == otherScheduleItem.startDateStr)
    }
}
