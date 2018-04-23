package pl.c0.sayard.studentUEK.data


class FilteredLesson(
    val id: Int,
    val subject: String,
    val type: String,
    val teacher: String,
    val teacherId: Int,
    val dayOfWeek: String,
    val startHour: String
)