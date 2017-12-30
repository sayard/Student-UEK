package pl.c0.sayard.uekplan

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ListView
import pl.c0.sayard.uekplan.adapters.GroupListAdapter

class FirstRunStepOneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_run_step_one)
        val adapter = GroupListAdapter(this)


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
    }
}
