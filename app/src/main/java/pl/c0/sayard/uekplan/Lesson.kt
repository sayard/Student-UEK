package pl.c0.sayard.uekplan

import android.content.Context


/**
 * Created by karol on 09.01.18.
 */
class Lesson(context: Context,
             var date: String,
             var weekDay: String,
             startHour: String,
             endHour: String,
             var subject: String,
             var type: String,
             var teacher: String,
             teacherId: String,
             var classroom: String,
             var comments: String?){

    var startDate: String? = null
    var endDate: String? = null
    var teacherIdParsed: Int? = null

    init{
        startDate = "$date $startHour"
        endDate = "$date $endHour"
        when(weekDay.toLowerCase()){
            "pn" -> weekDay = context.getString(R.string.monday)
            "wt" -> weekDay = context.getString(R.string.tuesday)
            "śr" -> weekDay = context.getString(R.string.wednesday)
            "cz" -> weekDay = context.getString(R.string.thursday)
            "pt" -> weekDay = context.getString(R.string.friday)
            "sb" -> weekDay = context.getString(R.string.saturday)
            "nd" -> weekDay = context.getString(R.string.sunday)
        }
        teacherIdParsed = teacherId.substring(1).toInt()
    }
}
