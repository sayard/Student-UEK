package pl.c0.sayard.uekplan

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import pl.c0.sayard.uekplan.adapters.GroupListAdapter

class FirstRunStepOneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_run_step_one)
        val retryButton = findViewById<Button>(R.id.group_retry_button)
        val nextStepButton = findViewById<Button>(R.id.next_step_button)
        val adapter = getAdapter(this, this)
        if(adapter.count <= 0){
            Toast.makeText(this, getText(R.string.error_try_again_later), Toast.LENGTH_SHORT).show()
            retryButton.visibility = View.VISIBLE
            nextStepButton.visibility = View.GONE
        }else{
            retryButton.visibility = View.GONE
            nextStepButton.visibility = View.VISIBLE
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
    }

    private fun getAdapter(context: Context, activity: Activity): GroupListAdapter {
        return GroupListAdapter(context, activity)
    }
}
