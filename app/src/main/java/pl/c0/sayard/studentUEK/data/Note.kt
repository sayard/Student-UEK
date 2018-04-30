package pl.c0.sayard.studentUEK.data


/**
 * Created by karol on 27.02.18.
 */
class Note(val id: Int, val title: String, val content: String, val dateStr:String, val hourStr: String){
    override fun equals(other: Any?): Boolean {
        val otherNote = other as Note
        return (id == otherNote.id &&
                title == otherNote.title &&
                content == otherNote.content &&
                dateStr == otherNote.dateStr &&
                hourStr == otherNote.hourStr)
    }

    override fun toString(): String {
        return "$id\n $title\n $content\n $dateStr\n $hourStr\n"
    }
}
