package pl.c0.sayard.studentUEK.data

import java.text.SimpleDateFormat
import java.util.*

class Event(val name: String,
            val description: String,
            val startDate: Date,
            val endDate: Date,
            val fb: String,
            val promotionLevel: Int){

    fun startsOnDate(dateStr: String): Boolean{
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale("pl"))
        return dateFormat.format(startDate) == dateStr
    }

}