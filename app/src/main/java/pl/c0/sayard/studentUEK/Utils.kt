package pl.c0.sayard.studentUEK

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.preference.PreferenceManager
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import com.evernote.android.job.util.support.PersistableBundleCompat
import pl.c0.sayard.studentUEK.data.FilteredLesson
import pl.c0.sayard.studentUEK.data.Group
import pl.c0.sayard.studentUEK.data.ScheduleItem
import java.util.*

/**
 * Created by Karol on 1/1/2018.
 */
class Utils {
    companion object {
        val FIRST_RUN_SHARED_PREFS_KEY = "firstRun"
        val AUTOMATIC_SCHEDULE_REFRESH_PREFS_KEY = "automaticScheduleRefresh"

        fun getGroupURL(group: Group, isLongSchedule: Boolean = false): String{
            return if(isLongSchedule){
                "http://planzajec.uek.krakow.pl/index.php?xml&typ=G&id=" +
                        group.id.toString() +
                        "&okres=2"
            }else{
                "http://planzajec.uek.krakow.pl/index.php?xml&typ=G&id=" +
                        group.id.toString() +
                        "&okres=1"
            }

        }

        fun getTeacherURL(group: Group?, isLongSchedule: Boolean = false): String {
            return if(isLongSchedule){
                "http://planzajec.uek.krakow.pl/index.php?xml&typ=N&id=" +
                        group?.id.toString() +
                        "&okres=2"
            }else{
                "http://planzajec.uek.krakow.pl/index.php?xml&typ=N&id=" +
                        group?.id.toString() +
                        "&okres=1"
            }
        }

        fun getTime(hourTv: TextView): Calendar{
            val startHour = hourTv.text.substring(0, 2).toInt()
            val startMinute = hourTv.text.substring(3).toInt()
            val startTime = Calendar.getInstance()
            startTime.set(Calendar.HOUR_OF_DAY, startHour)
            startTime.set(Calendar.MINUTE, startMinute)
            return startTime
        }

        fun getTitleBasedOnPosition(position: Int): Int{
            return when(position){
                0 -> R.string.courses
                1 -> R.string.search
                2 -> R.string.schedule
                3 -> R.string.notes
                4 -> R.string.settings
                else -> R.string.app_name
            }
        }

        fun isDeviceOnline(context: Context?): Boolean{
            val connMgr = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return (networkInfo != null && networkInfo.isConnected)
        }

        fun getTranslatedThemeName(codeThemeName: Int, context: Context): String{
            return when(codeThemeName){
                0 -> context.getString(R.string.defaultTheme)
                1 -> context.getString(R.string.darkTheme)
                2 -> context.getString(R.string.premiumTheme)
                else -> context.getString(R.string.defaultTheme)
            }
        }

        fun setSelectedTheme(activity: Activity, theme: Int){
            val prefs = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            val editor = prefs.edit()
            editor.putInt(activity.baseContext.getString(R.string.PREFS_SELECTED_THEME), theme).apply()
            activity.finish()
            activity.startActivity(Intent(activity, activity.javaClass))
        }

        fun onActivityCreateSetTheme(activity: Activity){
            val prefs = PreferenceManager.getDefaultSharedPreferences(activity.applicationContext)
            val themeId = prefs.getInt(activity.baseContext.getString(R.string.PREFS_SELECTED_THEME), 0)
            when(themeId){
                0 -> activity.setTheme(R.style.ThemeDefault)
                1 -> activity.setTheme(R.style.ThemeDark)
                2 -> activity.setTheme(R.style.ThemeGold)
            }
        }

        fun setFiltersUiState(dialogView: View, prefs: SharedPreferences, context: Context){
            val discoursesCheck = dialogView.findViewById<CheckBox>(R.id.filter_discourses)
            val exercisesCheck = dialogView.findViewById<CheckBox>(R.id.filter_exercises)
            val lecturesCheck = dialogView.findViewById<CheckBox>(R.id.filter_lectures)
            discoursesCheck.isChecked = prefs.getBoolean(context.getString(R.string.PREFS_DISCOURSES_VISIBLE), true)
            exercisesCheck.isChecked = prefs.getBoolean(context.getString(R.string.PREFS_EXERCISES_VISIBLE), true)
            lecturesCheck.isChecked = prefs.getBoolean(context.getString(R.string.PREFS_LECTURES_VISIBLE), true)
        }

        fun setFilters(dialogView: View, prefs:SharedPreferences, context: Context){
            val discoursesCheck = dialogView.findViewById<CheckBox>(R.id.filter_discourses)
            val exercisesCheck = dialogView.findViewById<CheckBox>(R.id.filter_exercises)
            val lecturesCheck = dialogView.findViewById<CheckBox>(R.id.filter_lectures)
            prefs.edit()
                    .putBoolean(context.getString(R.string.PREFS_DISCOURSES_VISIBLE), discoursesCheck.isChecked)
                    .putBoolean(context.getString(R.string.PREFS_EXERCISES_VISIBLE), exercisesCheck.isChecked)
                    .putBoolean(context.getString(R.string.PREFS_LECTURES_VISIBLE), lecturesCheck.isChecked)
                    .apply()
        }

         fun isInFilteredLessons(scheduleItem: ScheduleItem, filteredLessons: List<FilteredLesson>): Boolean{
            val calendar = Calendar.getInstance()
            calendar.time = scheduleItem.startDate
            val startHour = "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}"

            for(lesson in filteredLessons){
                if(lesson.subject == scheduleItem.subject
                && lesson.type == scheduleItem.type
                && lesson.teacher == scheduleItem.teacher
                && lesson.teacherId == scheduleItem.teacherId
                && lesson.dayOfWeek == scheduleItem.dayOfTheWeekStr
                && lesson.startHour == startHour){
                    return true
                }
            }
            return false
        }

        fun getScheduleItemExtras(context:Context, scheduleItem: ScheduleItem, hourStr: String): PersistableBundleCompat {
            return PersistableBundleCompat().apply {
                putString(context.getString(R.string.EXTRA_NOTIFICATION_SUBJECT), scheduleItem.subject)
                putString(context.getString(R.string.EXTRA_NOTIFICATION_TYPE), scheduleItem.type)
                putString(context.getString(R.string.EXTRA_NOTIFICATION_TEACHER), scheduleItem.teacher)
                putInt(context.getString(R.string.EXTRA_NOTIFICATION_TEACHER_ID), scheduleItem.teacherId)
                putString(context.getString(R.string.EXTRA_NOTIFICATION_CLASSROOM), scheduleItem.classroom)
                putString(context.getString(R.string.EXTRA_NOTIFICATION_COMMENTS), scheduleItem.comments)
                putString(context.getString(R.string.EXTRA_NOTIFICATION_DATE), scheduleItem.dateStr)
                putString(context.getString(R.string.EXTRA_NOTIFICATION_START_DATE), scheduleItem.startDateStr)
                putString(context.getString(R.string.EXTRA_NOTIFICATION_END_DATE), scheduleItem.endDateStr)
                putBoolean(context.getString(R.string.EXTRA_NOTIFICATION_IS_CUSTOM), scheduleItem.isCustom)
                putInt(context.getString(R.string.EXTRA_NOTIFICATION_CUSTOM_ID), scheduleItem.customId)
                putInt(context.getString(R.string.EXTRA_NOTIFICATION_NOTE_ID), scheduleItem.noteId)
                putString(context.getString(R.string.EXTRA_NOTIFICATION_NOTE_CONTENT), scheduleItem.noteContent)
                putString(context.getString(R.string.EXTRA_NOTIFICATION_HOUR), hourStr)
            }
        }
    }
}
