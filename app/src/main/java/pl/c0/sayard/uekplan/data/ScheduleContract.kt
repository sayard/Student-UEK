package pl.c0.sayard.uekplan.data

import android.provider.BaseColumns

/**
 * Created by Karol on 1/1/2018.
 */
class ScheduleContract private constructor(){

    class GroupEntry : BaseColumns{
        companion object {
            val TABLE_NAME: String = "major_group"
            val _ID: String = BaseColumns._ID
            val GROUP_NAME: String = "group_name"
            val GROUP_URL: String = "group_url"
        }
    }
}
