package com.purdue.comedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CustomFeed : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_feed)

        supportActionBar?.title = intent.getStringExtra(MainAdapter.NAV_TITLE)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        // Setup Recycler View
        val recyclerView: RecyclerView = findViewById(R.id.customRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // Position to absolute pos.
        recyclerView.adapter = MainAdapter() // Setup table logic

    }

    // Allow back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return true
    }

}