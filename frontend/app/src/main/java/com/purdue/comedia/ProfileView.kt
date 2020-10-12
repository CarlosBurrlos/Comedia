package com.purdue.comedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/** To view profile of another user */
class ProfileView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_view)

        supportActionBar?.title = "Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        // Setup Recycler View
        val recyclerView: RecyclerView = findViewById(R.id.userRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // Position to absolute pos
        recyclerView.adapter = MainAdapter() // Setup table logic

    }


    // Allow back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return true
    }

}