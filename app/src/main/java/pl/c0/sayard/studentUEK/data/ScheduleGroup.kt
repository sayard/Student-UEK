package pl.c0.sayard.studentUEK.data

/**
 * Created by karol on 13.01.18.
 */
class ScheduleGroup(var name: String, var url: String){
    override fun equals(other: Any?): Boolean {
        val otherScheduleGroup = other as ScheduleGroup
        return (name == otherScheduleGroup.name && url == otherScheduleGroup.url)
    }
}
