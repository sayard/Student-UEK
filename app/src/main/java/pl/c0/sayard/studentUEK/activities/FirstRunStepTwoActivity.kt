package pl.c0.sayard.studentUEK.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.*
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.Utils
import pl.c0.sayard.studentUEK.adapters.GroupListAdapter
import pl.c0.sayard.studentUEK.data.Group
import pl.c0.sayard.studentUEK.db.ScheduleContract
import pl.c0.sayard.studentUEK.db.ScheduleDbHelper
import pl.c0.sayard.studentUEK.parsers.GroupParser

class FirstRunStepTwoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.onActivityCreateSetTheme(this)
        setContentView(R.layout.activity_first_run_step_two)
        val retryButton = findViewById<Button>(R.id.language_group_retry_button)
        val nextStepButton = findViewById<Button>(R.id.next_step_button_two)
        GroupParser(this, true, object: GroupParser.OnTaskCompleted{
            @SuppressLint("ClickableViewAccessibility")
            override fun onTaskCompleted(result: List<Group>?, activity: Activity) {
                val groupListOriginal = result!!
                val adapter = getAdapter(activity, groupListOriginal)
                if(adapter.count <= 0){
                    Toast.makeText(activity, getText(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
                    retryButton.visibility = View.VISIBLE
                    nextStepButton.visibility = View.GONE
                }else{
                    retryButton.visibility = View.GONE
                    nextStepButton.visibility = View.VISIBLE
                }

                retryButton.setOnClickListener{
                    activity.recreate()
                }

                val searchBox = findViewById<EditText>(R.id.step_two_search_box)
                searchBox.addTextChangedListener(object: TextWatcher{
                    override fun afterTextChanged(p0: Editable?) {
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        adapter.filter.filter(p0.toString())
                    }

                })

                val listView = findViewById<ListView>(R.id.language_group_list_view)
                listView.adapter = adapter
                val selectedGroups = mutableListOf<Group>()
                listView.onItemClickListener = AdapterView.OnItemClickListener{ parent, view, position, id ->
                    val group = parent.getItemAtPosition(position) as Group
                    if(!selectedGroups.remove(group) && selectedGroups.count()<5){
                        selectedGroups.add(group)
                    }
                    updateSelectedGroups(selectedGroups)
                }
                nextStepButton.setOnClickListener{
                    val dbHelper = ScheduleDbHelper(activity)
                    val db = dbHelper.readableDatabase
                    val contentValues = ContentValues()
                    db.execSQL("DELETE FROM " + ScheduleContract.LanguageGroupsEntry.TABLE_NAME)
                    selectedGroups.forEach({
                        contentValues.put(
                                ScheduleContract.LanguageGroupsEntry.LANGUAGE_GROUP_NAME,
                                it.name
                        )
                        contentValues.put(
                                ScheduleContract.LanguageGroupsEntry.LANGUAGE_GROUP_URL,
                                Utils.getGroupURL(it)
                        )
                        db.insert(ScheduleContract.LanguageGroupsEntry.TABLE_NAME, null, contentValues)
                    })
                    val intent = Intent(activity, FirstRunStepThreeActivity::class.java)
                    startActivity(intent)
                }
                val selectedGroupsET = findViewById<EditText>(R.id.selected_language_group_s_edit_text)
                selectedGroupsET.setOnTouchListener(View.OnTouchListener { view, event ->
                    val DRAWABLE_RIGHT = 2
                    if(event?.action == MotionEvent.ACTION_UP){
                        if(event.rawX >= (selectedGroupsET.right - selectedGroupsET.compoundDrawables[DRAWABLE_RIGHT].bounds.width())){
                            selectedGroups.clear()
                            updateSelectedGroups(selectedGroups)
                            return@OnTouchListener true
                        }
                    }
                    false
                })
            }
        }).execute()
    }

    private fun getAdapter(context: Context, groupListOriginal: List<Group>): GroupListAdapter {
        return GroupListAdapter(context, groupListOriginal)
    }

    private fun updateSelectedGroups(groups: List<Group>){
        val selectedGroupsET = findViewById<EditText>(R.id.selected_language_group_s_edit_text)
        if(selectedGroupsET.visibility == View.GONE){
            selectedGroupsET.visibility = View.VISIBLE
        }else if(groups.isEmpty()){
            selectedGroupsET.visibility = View.GONE
        }
        val groupNames = mutableListOf<String>()
        groups.forEach({
            groupNames.add(it.name)
        })
        selectedGroupsET.setText(groupNames.joinToString(", "))
    }

    override fun onRestart() {
        super.onRestart()
        val intent = Intent(this, FirstRunStepOneActivity::class.java)
        startActivity(intent)
    }
}
