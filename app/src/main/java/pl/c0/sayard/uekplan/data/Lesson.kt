package pl.c0.sayard.uekplan.data



/**
 * Created by karol on 09.01.18.
 */
class Lesson(var date: String,
             startHour: String,
             endHour: String,
             var subject: String,
             var type: String,
             var teacher: String,
             teacherId: String,
             var classroom: String,
             var comments: String,
             var isCustomLesson: Boolean = false,
             val customId: Int = -1){

    var startDate: String? = null
    var endDate: String? = null
    var teacherIdParsed: Int? = null

    init{
        startDate = "$date $startHour"
        endDate = "$date $endHour"
        teacherIdParsed = try{
            teacherId.substring(1).toInt()
        }catch (e: Exception) {
            0
        }
    }
}
