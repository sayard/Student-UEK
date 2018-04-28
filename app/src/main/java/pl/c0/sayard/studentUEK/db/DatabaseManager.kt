package pl.c0.sayard.studentUEK.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.preference.PreferenceManager
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import pl.c0.sayard.studentUEK.data.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashSet

class DatabaseManager(val context: Context) {

    private val dbHelper = ScheduleDbHelper(context)
    private val readableDb = dbHelper.readableDatabase
    private val writableDb = dbHelper.writableDatabase

    fun getScheduleList(): MutableList<ScheduleItem> {

        val scheduleList = mutableListOf<ScheduleItem>()
        val hiddenTypes = getHiddenTypes()
        val cursor = getScheduleCursor()
        cursor.moveToFirst()

        if(cursor.count > 0){
            do{
                val scheduleItem = getScheduleItemFromCursor(cursor)
                if(!hiddenTypes.contains(scheduleItem.type)){
                    scheduleList.add(scheduleItem)
                }
            }while(cursor.moveToNext())
        }else{
            return mutableListOf()
        }
        val pe = getPe()
        if(pe != null){
            val days = HashSet<String>(scheduleList.map { it.dateStr })
            val peDays = getPeDays(pe, days)
            peDays.mapTo(scheduleList) {
                ScheduleItem(
                        pe.name,
                        "P.E.",
                        "",
                        0,
                        "",
                        "",
                        it,
                        "$it ${pe.startHour}",
                        "$it ${pe.endHour}"
                )
            }
            scheduleList.sortWith(Comparator { p0, p1 -> p0?.startDate!!.compareTo(p1?.startDate) })
        }

        val filteredLessons = getFilteredLessons()
        val filteredList = getFilteredList(filteredLessons, scheduleList)

        for(i in 0 until filteredList.size){
            val scheduleItem = filteredList[i]
            if(i==0){
                scheduleItem.isFirstOnTheDay = true
            }else{
                val previousScheduleItem = filteredList[i-1]
                if(scheduleItem.dateStr != previousScheduleItem.dateStr){
                    scheduleItem.isFirstOnTheDay = true
                }
            }
            scheduleItem.setNotes(getLessonNoteCursor(scheduleItem))
        }
        return filteredList
    }

    fun getScheduleCursor(): Cursor{
        return readableDb.query(
                ScheduleContract.LessonEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                ScheduleContract.LessonEntry.START_DATE
        )
    }

    private fun getPe(): SchedulePE?{
        val cursor = getPeCursor()
        if(cursor.count > 0){
            cursor.moveToLast()
            val peName = cursor.getString(cursor.getColumnIndex(ScheduleContract.PeEntry.PE_NAME))
            val peDay = cursor.getInt(cursor.getColumnIndex(ScheduleContract.PeEntry.PE_DAY))
            val peStartHour = cursor.getString(cursor.getColumnIndex(ScheduleContract.PeEntry.PE_START_HOUR))
            val peEndHour = cursor.getString(cursor.getColumnIndex(ScheduleContract.PeEntry.PE_END_HOUR))
            cursor.close()
            return SchedulePE(peName, peDay, peStartHour, peEndHour)
        }
        cursor.close()
        return null
    }

    private fun getPeCursor(): Cursor{
        return readableDb.query(
                ScheduleContract.PeEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        )
    }

    fun clearPeTable(){
        writableDb.execSQL("DELETE FROM "+ScheduleContract.PeEntry.TABLE_NAME)
    }

    fun addPeToDb(name: String, day: Int, startHour: String, endHour: String){
        val contentValues = ContentValues()
        contentValues.put(ScheduleContract.PeEntry.PE_NAME, name)
        contentValues.put(ScheduleContract.PeEntry.PE_DAY, day)
        contentValues.put(ScheduleContract.PeEntry.PE_START_HOUR, startHour)
        contentValues.put(ScheduleContract.PeEntry.PE_END_HOUR, endHour)
        writableDb.insert(ScheduleContract.PeEntry.TABLE_NAME, null, contentValues)
    }

    private fun getPeDays(pe: SchedulePE, days: HashSet<String>): MutableList<String>{
        val peDays = mutableListOf<String>()
        val shortDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale("pl", "PL"))
        for(day in days){
            val date = shortDateFormat.parse(day)
            val calendar = Calendar.getInstance()
            calendar.time = date
            if(calendar.get(Calendar.DAY_OF_WEEK) == pe.day+2){
                peDays.add(day)
            }
        }
        return peDays
    }

    private fun getHiddenTypes(): MutableList<String>{
        val hiddenTypes = mutableListOf<String>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if(!prefs.getBoolean(context.getString(R.string.PREFS_DISCOURSES_VISIBLE), true)){
            hiddenTypes.add("wykład")
        }
        if(!prefs.getBoolean(context.getString(R.string.PREFS_EXERCISES_VISIBLE), true)){
            hiddenTypes.add("ćwiczenia")
        }
        if(!prefs.getBoolean(context.getString(R.string.PREFS_LECTURES_VISIBLE), true)){
            hiddenTypes.add("lektorat")
        }
        return hiddenTypes
    }

    private fun getScheduleItemFromCursor(cursor: Cursor): ScheduleItem{
        val dateStr = cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.DATE))
        val comments = cursor.getString(cursor.getColumnIndexOrThrow(ScheduleContract.LessonEntry.COMMENTS))
        var isCustom = false
        if(cursor.getInt(cursor.getColumnIndex(ScheduleContract.LessonEntry.IS_CUSTOM)) == 1){
            isCustom = true
        }
        return ScheduleItem(
                cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.SUBJECT)),
                cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.TYPE)),
                cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.TEACHER)),
                cursor.getInt(cursor.getColumnIndex(ScheduleContract.LessonEntry.TEACHER_ID)),
                cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.CLASSROOM)),
                comments,
                dateStr,
                cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.START_DATE)),
                cursor.getString(cursor.getColumnIndex(ScheduleContract.LessonEntry.END_DATE)),
                isCustom = isCustom,
                customId = cursor.getInt(cursor.getColumnIndex(ScheduleContract.LessonEntry.CUSTOM_ID))
        )
    }

    fun getFilteredLessons(): List<FilteredLesson>{
        val cursor = readableDb.query(
                ScheduleContract.FilteredLessonEntry.TABLE_NAME,
                arrayOf(
                        ScheduleContract.FilteredLessonEntry._ID,
                        ScheduleContract.FilteredLessonEntry.LESSON_SUBJECT,
                        ScheduleContract.FilteredLessonEntry.LESSON_TYPE,
                        ScheduleContract.FilteredLessonEntry.LESSON_TEACHER,
                        ScheduleContract.FilteredLessonEntry.LESSON_TEACHER_ID,
                        ScheduleContract.FilteredLessonEntry.LESSON_DAY_OF_WEEK,
                        ScheduleContract.FilteredLessonEntry.LESSON_START_HOUR
                ),
                null,
                null,
                null,
                null,
                null
        )

        val filteredLessonsList = mutableListOf<FilteredLesson>()

        try{
            cursor.moveToFirst()
            do{
                val filteredLesson = FilteredLesson(
                        cursor.getInt(cursor.getColumnIndex(ScheduleContract.FilteredLessonEntry._ID)),
                        cursor.getString(cursor.getColumnIndex(ScheduleContract.FilteredLessonEntry.LESSON_SUBJECT)),
                        cursor.getString(cursor.getColumnIndex(ScheduleContract.FilteredLessonEntry.LESSON_TYPE)),
                        cursor.getString(cursor.getColumnIndex(ScheduleContract.FilteredLessonEntry.LESSON_TEACHER)),
                        cursor.getInt(cursor.getColumnIndex(ScheduleContract.FilteredLessonEntry.LESSON_TEACHER_ID)),
                        cursor.getString(cursor.getColumnIndex(ScheduleContract.FilteredLessonEntry.LESSON_DAY_OF_WEEK)),
                        cursor.getString(cursor.getColumnIndex(ScheduleContract.FilteredLessonEntry.LESSON_START_HOUR))
                )
                filteredLessonsList.add(filteredLesson)
            }while(cursor.moveToNext())

            cursor.close()
            return filteredLessonsList
        }catch(e: CursorIndexOutOfBoundsException){
            return emptyList()
        }

    }

    private fun getFilteredList(filteredLessons: List<FilteredLesson>, scheduleList: MutableList<ScheduleItem>): MutableList<ScheduleItem> {
        val filteredList = mutableListOf<ScheduleItem>()
        for(i in 0 until scheduleList.size){
            val scheduleItem = scheduleList[i]
            if(i !=0 ){
                val previousScheduleItem = scheduleList[i-1]
                if(previousScheduleItem != scheduleItem && !Utils.isInFilteredLessons(scheduleItem, filteredLessons)){
                    filteredList.add(scheduleItem)
                }
            }else{
                if(!Utils.isInFilteredLessons(scheduleItem, filteredLessons)){
                    filteredList.add(scheduleItem)
                }
            }
        }
        return filteredList
    }

    fun removeFilteredLesson(id: Int){
        writableDb.delete(
                ScheduleContract.FilteredLessonEntry.TABLE_NAME,
                "${ScheduleContract.FilteredLessonEntry._ID}=?",
                arrayOf("$id")
        )

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(context.getString(R.string.PREFS_REFRESH_SCHEDULE), true).apply()
    }

    fun addLessonToFilteredLessons(scheduleItem: ScheduleItem){
        val calendar = Calendar.getInstance()
        calendar.time = scheduleItem.startDate

        val values = ContentValues().apply{
            put(ScheduleContract.FilteredLessonEntry.LESSON_SUBJECT, scheduleItem.subject)
            put(ScheduleContract.FilteredLessonEntry.LESSON_TYPE, scheduleItem.type)
            put(ScheduleContract.FilteredLessonEntry.LESSON_TEACHER, scheduleItem.teacher)
            put(ScheduleContract.FilteredLessonEntry.LESSON_TEACHER_ID, scheduleItem.teacherId)
            put(ScheduleContract.FilteredLessonEntry.LESSON_DAY_OF_WEEK, scheduleItem.dayOfTheWeekStr)
            put(ScheduleContract.FilteredLessonEntry.LESSON_START_HOUR, "${calendar.get(Calendar.HOUR_OF_DAY)}:${calendar.get(Calendar.MINUTE)}")
        }

        writableDb.insert(ScheduleContract.FilteredLessonEntry.TABLE_NAME, null, values)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean(context.getString(R.string.PREFS_REFRESH_SCHEDULE), true).apply()
    }

    private fun getLessonNoteCursor(scheduleItem: ScheduleItem): Cursor?{
        return readableDb.query(
                ScheduleContract.LessonNoteEntry.TABLE_NAME,
                arrayOf(ScheduleContract.LessonNoteEntry._ID, ScheduleContract.LessonNoteEntry.CONTENT),
                "${ScheduleContract.LessonNoteEntry.LESSON_SUBJECT} = ? AND " +
                        "${ScheduleContract.LessonNoteEntry.LESSON_TYPE} = ? AND " +
                        "${ScheduleContract.LessonNoteEntry.LESSON_TEACHER} = ? AND " +
                        "${ScheduleContract.LessonNoteEntry.LESSON_TEACHER_ID} = ? AND " +
                        "${ScheduleContract.LessonNoteEntry.LESSON_CLASSROOM} = ? AND " +
                        "${ScheduleContract.LessonNoteEntry.LESSON_DATE} = ? AND " +
                        "${ScheduleContract.LessonNoteEntry.LESSON_START_DATE} = ? AND " +
                        "${ScheduleContract.LessonNoteEntry.LESSON_END_DATE} = ? ",
                arrayOf(scheduleItem.subject,
                        scheduleItem.type,
                        scheduleItem.teacher,
                        "${scheduleItem.teacherId}",
                        scheduleItem.classroom,
                        scheduleItem.dateStr,
                        scheduleItem.startDateStr,
                        scheduleItem.endDateStr
                ),
                null,
                null,
                ScheduleContract.LessonNoteEntry._ID
        )
    }

    fun addLessonNoteToDb(scheduleItem: ScheduleItem?, noteContent: String): Long{
        val contentValues = ContentValues()
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_SUBJECT, scheduleItem?.subject)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_TYPE, scheduleItem?.type)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_TEACHER, scheduleItem?.teacher)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_TEACHER_ID, scheduleItem?.teacherId)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_CLASSROOM, scheduleItem?.classroom)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_DATE, scheduleItem?.dateStr)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_START_DATE, scheduleItem?.startDateStr)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_END_DATE, scheduleItem?.endDateStr)
        contentValues.put(ScheduleContract.LessonNoteEntry.CONTENT, noteContent)
        return writableDb.insert(
                ScheduleContract.LessonNoteEntry.TABLE_NAME,
                null,
                contentValues
        )
    }

    fun updateLessonNote(scheduleItem: ScheduleItem?, noteContent: String, noteId: Int?): Int{
        val contentValues = ContentValues()
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_SUBJECT, scheduleItem?.subject)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_TYPE, scheduleItem?.type)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_TEACHER, scheduleItem?.teacher)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_TEACHER_ID, scheduleItem?.teacherId)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_CLASSROOM, scheduleItem?.classroom)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_DATE, scheduleItem?.dateStr)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_START_DATE, scheduleItem?.startDateStr)
        contentValues.put(ScheduleContract.LessonNoteEntry.LESSON_END_DATE, scheduleItem?.endDateStr)
        contentValues.put(ScheduleContract.LessonNoteEntry.CONTENT, noteContent)
        return writableDb.update(
                ScheduleContract.LessonNoteEntry.TABLE_NAME,
                contentValues,
                "${ScheduleContract.LessonNoteEntry._ID} = $noteId",
                null
        )
    }

    fun removeLessonNote(noteId: Int?): Int{
        return writableDb.delete(
                ScheduleContract.LessonNoteEntry.TABLE_NAME,
                "${ScheduleContract.LessonNoteEntry._ID} = $noteId",
                null
        )
    }

    fun getGroups(): MutableList<ScheduleGroup> {
        val cursor = readableDb.query(
                ScheduleContract.GroupEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null)
        val groups = mutableListOf<ScheduleGroup>()
        while(cursor.moveToNext()){
            val groupName = cursor.getString(cursor.getColumnIndex(ScheduleContract.GroupEntry.GROUP_NAME))
            val groupURL = cursor.getString(cursor.getColumnIndex(ScheduleContract.GroupEntry.GROUP_URL))
            groups.add(ScheduleGroup(groupName, groupURL))
        }
        cursor.close()
        return groups
    }

    fun addGroupsToDb(groups: List<Group>){
        val contentValues = ContentValues()
        writableDb.execSQL("DELETE FROM " + ScheduleContract.GroupEntry.TABLE_NAME)
        groups.forEach({
            contentValues.put(
                    ScheduleContract.GroupEntry.GROUP_NAME,
                    it.name
            )
            contentValues.put(
                    ScheduleContract.GroupEntry.GROUP_URL,
                    Utils.getGroupURL(it)
            )
            writableDb.insert(ScheduleContract.GroupEntry.TABLE_NAME, null, contentValues)
        })
    }

    fun getLanguageGroups(): MutableList<ScheduleGroup> {
        val cursor = readableDb.query(
                ScheduleContract.LanguageGroupsEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null)
        val languageGroups = mutableListOf<ScheduleGroup>()
        while(cursor.moveToNext()){
            val languageGroupName = cursor.getString(cursor.getColumnIndex(ScheduleContract.LanguageGroupsEntry.LANGUAGE_GROUP_NAME))
            val languageGroupURL = cursor.getString(cursor.getColumnIndex(ScheduleContract.LanguageGroupsEntry.LANGUAGE_GROUP_URL))
            languageGroups.add(ScheduleGroup(languageGroupName, languageGroupURL))
        }
        cursor.close()
        return languageGroups
    }

    fun addLanguageGroupsToDb(selectedGroups: List<Group>) {
        val contentValues = ContentValues()
        writableDb.execSQL("DELETE FROM " + ScheduleContract.LanguageGroupsEntry.TABLE_NAME)
        selectedGroups.forEach({
            contentValues.put(
                    ScheduleContract.LanguageGroupsEntry.LANGUAGE_GROUP_NAME,
                    it.name
            )
            contentValues.put(
                    ScheduleContract.LanguageGroupsEntry.LANGUAGE_GROUP_URL,
                    Utils.getGroupURL(it)
            )
            writableDb.insert(ScheduleContract.LanguageGroupsEntry.TABLE_NAME, null, contentValues)
        })
    }

    fun getNotesCursor(): Cursor{
        return readableDb.query(
                ScheduleContract.NotesEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "${ScheduleContract.NotesEntry.DATE} ASC, ${ScheduleContract.NotesEntry.HOUR} ASC"
        )
    }

    fun getNotesCursorById(id: Int): Cursor{
        return readableDb.query(
                ScheduleContract.NotesEntry.TABLE_NAME,
                null,
                "${ScheduleContract.NotesEntry._ID} = $id",
                null,
                null,
                null,
                null
        )
    }

    fun getNotesListFromCursor(cursor: Cursor): List<Note>{
        val notesList = mutableListOf<Note>()
        cursor.moveToFirst()
        while(cursor.moveToNext()){
            notesList.add(Note(
                    cursor.getInt(cursor.getColumnIndex(ScheduleContract.NotesEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.NotesEntry.TITLE)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.NotesEntry.CONTENT)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.NotesEntry.DATE)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.NotesEntry.HOUR))
            ))
        }
        cursor.close()
        return notesList
    }

    fun addNoteToDb(id: Int, title:String, content: String, date: String, hour: String){
        val contentValues = ContentValues()
        contentValues.put(ScheduleContract.NotesEntry.TITLE, title)
        contentValues.put(ScheduleContract.NotesEntry.CONTENT, content)
        contentValues.put(ScheduleContract.NotesEntry.DATE, date)
        contentValues.put(ScheduleContract.NotesEntry.HOUR, hour)
        if(id == 0){
            writableDb.insert(ScheduleContract.NotesEntry.TABLE_NAME, null, contentValues)
        }else{
            writableDb.update(ScheduleContract.NotesEntry.TABLE_NAME, contentValues, "${ScheduleContract.NotesEntry._ID} = $id", null)
        }
    }

    fun deleteUnnecessaryEntries() {
        writableDb.delete(ScheduleContract.LessonEntry.TABLE_NAME, null, null)
        writableDb.delete(ScheduleContract.UserAddedLessonEntry.TABLE_NAME, "? < date('now')", arrayOf(ScheduleContract.UserAddedLessonEntry.DATE))
        writableDb.delete(ScheduleContract.LessonNoteEntry.TABLE_NAME, "? < date('now')", arrayOf(ScheduleContract.LessonNoteEntry.LESSON_DATE))
    }

    fun getUserLessonCursor(): Cursor {
        return readableDb.query(
                ScheduleContract.UserAddedLessonEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        )
    }

    fun getUserLessonsFromCursor(cursor: Cursor): List<Lesson> {
        val userLessonList = mutableListOf<Lesson>()
        cursor.moveToFirst()
        while(cursor.moveToNext()){
            val lesson = Lesson(
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.UserAddedLessonEntry.DATE)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.UserAddedLessonEntry.START_HOUR)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.UserAddedLessonEntry.END_HOUR)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.UserAddedLessonEntry.SUBJECT)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.UserAddedLessonEntry.TYPE)),
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.UserAddedLessonEntry.TEACHER)),
                    "-1",
                    cursor.getString(cursor.getColumnIndex(ScheduleContract.UserAddedLessonEntry.CLASSROOM)),
                    "",
                    true,
                    cursor.getInt(cursor.getColumnIndex(ScheduleContract.UserAddedLessonEntry._ID))
            )
            userLessonList.add(lesson)
        }
        return userLessonList
    }

    fun addUserLessonToDb(name: String,
                          type: String,
                          teacher: String,
                          buildingAbbreviation: String,
                          classroomVal:String,
                          date: String,
                          startHour: String,
                          endHour: String,
                          id: Int){
        val contentValues = ContentValues()
        contentValues.put(ScheduleContract.UserAddedLessonEntry.SUBJECT, name)
        contentValues.put(ScheduleContract.UserAddedLessonEntry.TYPE, type)
        contentValues.put(ScheduleContract.UserAddedLessonEntry.TEACHER, teacher)
        contentValues.put(ScheduleContract.UserAddedLessonEntry.CLASSROOM, "$buildingAbbreviation$classroomVal")
        contentValues.put(ScheduleContract.UserAddedLessonEntry.DATE, date)
        contentValues.put(ScheduleContract.UserAddedLessonEntry.START_HOUR, startHour)
        contentValues.put(ScheduleContract.UserAddedLessonEntry.END_HOUR, endHour)
        if(id == -1){
            writableDb.insert(ScheduleContract.UserAddedLessonEntry.TABLE_NAME, null, contentValues)
        }else{
            writableDb.update(ScheduleContract.UserAddedLessonEntry.TABLE_NAME,
                    contentValues,
                    "${ScheduleContract.UserAddedLessonEntry._ID} = $id",
                    null)
        }
    }

    fun removeUserLesson(idToDelete: Int?):Int{
        return writableDb.delete(ScheduleContract.UserAddedLessonEntry.TABLE_NAME,
                "${ScheduleContract.UserAddedLessonEntry._ID} = $idToDelete",
                null)
    }

    fun addLessonsToDb(lessons: List<Lesson>) {
        val contentValues = ContentValues()
        for(lesson in lessons){
            contentValues.put(ScheduleContract.LessonEntry.SUBJECT, lesson.subject)
            contentValues.put(ScheduleContract.LessonEntry.TYPE, lesson.type)
            contentValues.put(ScheduleContract.LessonEntry.TEACHER, lesson.teacher)
            contentValues.put(ScheduleContract.LessonEntry.TEACHER_ID, lesson.teacherIdParsed)
            contentValues.put(ScheduleContract.LessonEntry.CLASSROOM, lesson.classroom)
            contentValues.put(ScheduleContract.LessonEntry.COMMENTS, lesson.comments)
            contentValues.put(ScheduleContract.LessonEntry.DATE, lesson.date)
            contentValues.put(ScheduleContract.LessonEntry.START_DATE, lesson.startDate)
            contentValues.put(ScheduleContract.LessonEntry.END_DATE, lesson.endDate)
            contentValues.put(ScheduleContract.LessonEntry.IS_CUSTOM, lesson.isCustomLesson)
            contentValues.put(ScheduleContract.LessonEntry.CUSTOM_ID, lesson.customId)
            writableDb.insert(ScheduleContract.LessonEntry.TABLE_NAME, null, contentValues)
        }
    }

    fun getNotesDeleteCount(item: Any): Int {
        return writableDb.delete(ScheduleContract.NotesEntry.TABLE_NAME,
                "${ScheduleContract.NotesEntry._ID} = ${(item as Note).id}",
                null)
    }

}