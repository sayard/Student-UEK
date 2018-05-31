package pl.c0.sayard.studentUEK.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import pl.c0.sayard.studentUEK.R
import pl.c0.sayard.studentUEK.adapters.EditGroupsAdapter
import pl.c0.sayard.studentUEK.db.DatabaseManager

class EditGroupsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_groups)
        val dbManager = DatabaseManager(this)
        val groups = dbManager.getGroups()
        groups.addAll(dbManager.getLanguageGroups())
        val editGroupsAdapter = EditGroupsAdapter(this, this, groups)
        val editGroupsListView = findViewById<ListView>(R.id.edit_groups_groups)
        editGroupsListView.adapter = editGroupsAdapter
    }
}
