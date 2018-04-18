package pl.c0.sayard.studentUEK.activities

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
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

class FirstRunStepOneActivity : AppCompatActivity() {

    private var nextStepButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.onActivityCreateSetTheme(this)
        setContentView(R.layout.activity_first_run_step_one)
        val retryButton = findViewById<Button>(R.id.group_retry_button)
        nextStepButton = findViewById(R.id.next_step_button)
        GroupParser(this, false, object: GroupParser.OnTaskCompleted{
            override fun onTaskCompleted(result: List<Group>?, activity: Activity) {
                val adapter = getAdapter(activity, result!!)
                if(adapter.count <= 0){
                    Toast.makeText(activity, getText(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
                    retryButton.visibility = View.VISIBLE
                    nextStepButton?.visibility = View.GONE
                }else{
                    retryButton.visibility = View.GONE
                    nextStepButton?.visibility = View.VISIBLE
                }

                retryButton.setOnClickListener {
                    activity.recreate()
                }

                val searchBox = findViewById<EditText>(R.id.step_one_search_box)
                searchBox.addTextChangedListener(object: TextWatcher{
                    override fun afterTextChanged(p0: Editable?) {
                    }

                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    }

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        adapter.filter.filter(p0.toString())
                    }

                })

                val listView = findViewById<ListView>(R.id.group_list_view)
                listView.adapter = adapter
                val selectedGroups = mutableListOf<Group>()
                listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                    val group = parent.getItemAtPosition(position) as Group
                    if(!selectedGroups.remove(group) && selectedGroups.count()<5){
                        selectedGroups.add(group)
                    }
                    updateSelectedGroupsAndActivateNextButton(selectedGroups)
                }
                val selectedGroupsET = findViewById<EditText>(R.id.selected_group_s_edit_text)
                selectedGroupsET.setOnTouchListener(View.OnTouchListener { view, event->
                    val DRAWABLE_RIGHT = 2
                    if(event?.action == MotionEvent.ACTION_UP){
                        if(event.rawX >= (selectedGroupsET.right - selectedGroupsET.compoundDrawables[DRAWABLE_RIGHT].bounds.width())){
                            selectedGroups.clear()
                            updateSelectedGroupsAndActivateNextButton(selectedGroups)
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

    private fun updateSelectedGroupsAndActivateNextButton(groups: List<Group>){
        val selectedGroupsET = findViewById<EditText>(R.id.selected_group_s_edit_text)
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
        nextStepButton?.isClickable = true
        val ta = theme.obtainStyledAttributes(R.styleable.Style)
        nextStepButton?.setBackgroundColor(ta.getColor(R.styleable.Style_colorPrimary, ContextCompat.getColor(this, R.color.colorPrimaryDefault)))
        nextStepButton?.setOnClickListener {
            val dbHelper = ScheduleDbHelper(this)
            val db = dbHelper.readableDatabase
            val contentValues = ContentValues()
            db.execSQL("DELETE FROM " + ScheduleContract.GroupEntry.TABLE_NAME)
            groups.forEach({
                contentValues.put(
                        ScheduleContract.GroupEntry.GROUP_NAME,
                        it.name
                )
                contentValues.put(
                        ScheduleContract.GroupEntry.GROUP_URL,
                        Utils.getGroupURL(it)
                )
                db.insert(ScheduleContract.GroupEntry.TABLE_NAME, null, contentValues)
            })
            val intent = Intent(this, FirstRunStepTwoActivity::class.java)
            startActivity(intent)
        }
    }
}
