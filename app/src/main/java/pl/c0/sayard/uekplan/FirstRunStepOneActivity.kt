package pl.c0.sayard.uekplan

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import pl.c0.sayard.uekplan.adapters.GroupListAdapter

class FirstRunStepOneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_run_step_one)
        val listView = findViewById<ListView>(R.id.group_list_view)
        listView.adapter = GroupListAdapter(this)
    }
}
