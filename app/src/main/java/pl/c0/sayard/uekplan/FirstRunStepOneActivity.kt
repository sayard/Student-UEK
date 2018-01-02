package pl.c0.sayard.uekplan

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import pl.c0.sayard.uekplan.adapters.GroupListAdapter
import pl.c0.sayard.uekplan.data.ScheduleContract
import pl.c0.sayard.uekplan.data.ScheduleDbHelper

class FirstRunStepOneActivity : AppCompatActivity() {

    private var nextStepButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_run_step_one)
        val retryButton = findViewById<Button>(R.id.group_retry_button)
        nextStepButton = findViewById(R.id.next_step_button)
        val adapter = getAdapter(this, this)
        if(adapter.count <= 0){
            Toast.makeText(this, getText(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
            retryButton.visibility = View.VISIBLE
            nextStepButton!!.visibility = View.GONE
        }else{
            retryButton.visibility = View.GONE
            nextStepButton!!.visibility = View.VISIBLE
        }

        retryButton.setOnClickListener {
            this.recreate()
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
        var selectedGroup: Group?
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            selectedGroup = parent.getItemAtPosition(position) as Group
            updateSelectedGroupTvAndActivateNextButton(selectedGroup!!)
        }
    }

    private fun getAdapter(context: Context, activity: Activity): GroupListAdapter {
        return GroupListAdapter(context, activity, false)
    }

    private fun updateSelectedGroupTvAndActivateNextButton(group: Group){
        val selectedGroupTV = findViewById<TextView>(R.id.selected_group_text_view)
        if(selectedGroupTV.visibility == View.GONE){
            selectedGroupTV.visibility = View.VISIBLE
        }
        selectedGroupTV.text = group.name
        nextStepButton!!.isClickable = true
        nextStepButton!!.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        nextStepButton!!.setOnClickListener {
            val dbHelper = ScheduleDbHelper(this)
            val db = dbHelper.readableDatabase
            val contentValues = ContentValues()
            contentValues.put(ScheduleContract.GroupEntry.GROUP_NAME, group.name)
            contentValues.put(ScheduleContract.GroupEntry.GROUP_URL, Utils.getGroupURL(group))
            val cursor = db.rawQuery("SELECT COUNT(*) FROM " + ScheduleContract.GroupEntry.TABLE_NAME, null)
            cursor.moveToFirst()
            if(cursor.getInt(0)==0){
                db.insert(ScheduleContract.GroupEntry.TABLE_NAME, null, contentValues)
            }else{
                db.update(
                        ScheduleContract.GroupEntry.TABLE_NAME,
                        contentValues,
                        ScheduleContract.GroupEntry._ID+"=1",
                        null
                )
            }
            cursor.close()
            val intent = Intent(this, FirstRunStepTwoActivity::class.java)
            startActivity(intent)
        }
    }
}
