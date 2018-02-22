package pl.c0.sayard.uekplan.db

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

    class LanguageGroupsEntry : BaseColumns{
        companion object {
            val TABLE_NAME: String = "language_groups"
            val _ID: String = BaseColumns._ID
            val LANGUAGE_GROUP_NAME: String = "language_group_name"
            val LANGUAGE_GROUP_URL: String = "language_group_url"
        }
    }

    class PeEntry: BaseColumns{
        companion object {
            val TABLE_NAME: String = "pe"
            val _ID: String = BaseColumns._ID
            val PE_NAME: String = "pe_name"
            val PE_DAY: String = "pe_day"
            val PE_START_HOUR: String = "pe_start_hour"
            val PE_END_HOUR: String = "pe_end_hour"
        }
    }

    class LessonEntry: BaseColumns{
        companion object {
            val TABLE_NAME: String = "lesson"
            val _ID: String = BaseColumns._ID
            val SUBJECT: String = "subject"
            val TYPE: String = "type"
            val TEACHER: String = "teacher"
            val TEACHER_ID: String = "teacher_id"
            val CLASSROOM: String = "classroom"
            val COMMENTS: String = "comments"
            val DATE: String = "date"
            val START_DATE: String = "start_date"
            val END_DATE: String = "end_date"
        }
    }

    class UserAddedLessonEntry: BaseColumns{
        companion object {
            val TABLE_NAME: String = "user_added_lesson"
            val _ID: String = BaseColumns._ID
            val SUBJECT: String = "subject"
            val TYPE: String = "type"
            val TEACHER: String = "teacher"
            val CLASSROOM: String = "classroom"
            val DATE: String = "date"
            val START_DATE: String = "start_date"
            val END_DATE: String = "end_date"
        }
    }
}
