package pl.c0.sayard.studentUEK.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Toast
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.adapters.GroupListAdapter
import pl.c0.sayard.studentUEK.data.Group
import pl.c0.sayard.studentUEK.parsers.AllGroupsParser

class AddGroupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)

        val searchBox = findViewById<EditText>(R.id.add_group_search_box)
        val progressBar = findViewById<ProgressBar>(R.id.all_groups_progress_bar)
        val listView = findViewById<ListView>(R.id.all_groups_list_view)

        progressBar.visibility = View.VISIBLE
        listView.visibility = View.GONE

        AllGroupsParser(object: AllGroupsParser.OnTaskCompleted{

            override fun onTaskCompleted(result: List<Group>?) {
                if(result == null || result.isEmpty()){
                    Toast.makeText(this@AddGroupActivity, getString(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
                    finish()
                }else{
                    progressBar.visibility = View.GONE
                    listView.visibility = View.VISIBLE

                    val adapter = GroupListAdapter(this@AddGroupActivity, result, false)
                    searchBox.addTextChangedListener(object: TextWatcher{
                        override fun afterTextChanged(p0: Editable?) {
                        }

                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        }

                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                            adapter.filter.filter(p0.toString())
                        }

                    })
                    listView.adapter = adapter
                }
            }

        }).execute()
    }
}
