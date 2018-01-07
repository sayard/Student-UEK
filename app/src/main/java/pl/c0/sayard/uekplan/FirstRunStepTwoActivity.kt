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

class FirstRunStepTwoActivity : AppCompatActivity() {

    private var nextStepButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_run_step_two)
        val retryButton = findViewById<Button>(R.id.language_group_retry_button)
        nextStepButton = findViewById(R.id.next_step_button_two)
        val adapter = getAdapter(this, this)
        if(adapter.count <= 0){
            Toast.makeText(this, getText(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
            retryButton.visibility = View.VISIBLE
            nextStepButton!!.visibility = View.GONE
        }else{
            retryButton.visibility = View.GONE
            nextStepButton!!.visibility = View.VISIBLE
        }

        retryButton.setOnClickListener{
            this.recreate()
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
            if(!selectedGroups.remove(group) && selectedGroups.count()<2){
                selectedGroups.add(group)
            }
            updateSelectedGroups(selectedGroups)
        }
        nextStepButton!!.setOnClickListener{
            val dbHelper = ScheduleDbHelper(this)
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
            val intent = Intent(this, FirstRunStepThreeActivity::class.java)
            startActivity(intent)
        }
    }

    private fun getAdapter(context: Context, activity: Activity): GroupListAdapter {
        return GroupListAdapter(context, activity, true)
    }

    private fun updateSelectedGroups(groups: List<Group>){
        val selectedGroupsTV = findViewById<TextView>(R.id.selected_language_group_s_text_view)
        if(selectedGroupsTV.visibility == View.GONE){
            selectedGroupsTV.visibility = View.VISIBLE
        }else if(groups.isEmpty()){
            selectedGroupsTV.visibility = View.GONE
        }
        val groupNames = mutableListOf<String>()
        groups.forEach({
            groupNames.add(it.name)
        })
        selectedGroupsTV.text = groupNames.joinToString()
    }
}
