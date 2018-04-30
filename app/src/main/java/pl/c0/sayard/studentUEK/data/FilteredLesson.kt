package pl.c0.sayard.studentUEK.data


class FilteredLesson(
    val id: Int,
    val subject: String,
    val type: String,
    val teacher: String,
    val teacherId: Int,
    val dayOfWeek: String,
    val startHour: String
){
    override fun equals(other: Any?): Boolean{
        val otherFilteredLesson = other as FilteredLesson
        return (id == otherFilteredLesson.id &&
                subject == otherFilteredLesson.subject &&
                type == otherFilteredLesson.type &&
                teacher == otherFilteredLesson.teacher &&
                teacherId == otherFilteredLesson.teacherId &&
                dayOfWeek == otherFilteredLesson.dayOfWeek &&
                startHour == otherFilteredLesson.startHour)
    }

    override fun toString(): String {
        return "$id\n $subject\n $type\n $teacher\n $teacherId\n $dayOfWeek\n $startHour\n"
    }
}