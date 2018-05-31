package pl.c0.sayard.studentUEK.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.adapters.EditGroupsAdapter
import pl.c0.sayard.studentUEK.db.DatabaseManager

class EditGroupsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_groups)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.edit_groups_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.add_group->{
                startActivity(Intent(this, AddGroupActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val dbManager = DatabaseManager(this)
        val groups = dbManager.getGroups()
        groups.addAll(dbManager.getLanguageGroups())
        val editGroupsAdapter = EditGroupsAdapter(this, this, groups)
        val editGroupsListView = findViewById<ListView>(R.id.edit_groups_groups)
        editGroupsListView.adapter = editGroupsAdapter
    }
}
