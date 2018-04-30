package pl.c0.sayard.studentUEK.data

/**
 * Created by Karol on 1/1/2018.
 */
class Group(var id: kotlin.Int, var name: String, var type:String="G"){
    override fun equals(other: Any?): Boolean {
        val otherGroup = other as Group
        return(id == otherGroup.id &&
                name == otherGroup.name &&
                type == otherGroup.type)
    }

    override fun toString(): String {
        return "$id\n $name\n $type\n"
    }
}
