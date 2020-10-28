package com.purdue.comedia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CustomFeed : AppCompatActivity() {
    private lateinit var adapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_feed)

        supportActionBar?.title = intent.getStringExtra(MainAdapter.NAV_TITLE)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        // Setup Recycler View
        val recyclerView: RecyclerView = findViewById(R.id.customRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // Position to absolute pos.
        adapter = MainAdapter()
        recyclerView.adapter = adapter // Setup table logic

        val isGenre = intent.extras!!.containsKey(MainAdapter.GENRE)

        if (isGenre) {
            val genre = intent.getStringExtra(MainAdapter.GENRE)
            updateTableDataWithGenre(genre!!)
        } else {
            updateTableDataWithSavedPosts()
        }

    }

    // Called when screen is loaded to populate genre feed
    private fun updateTableDataWithGenre(genre: String) {
        if (!this::adapter.isInitialized) return
        FirestoreUtility.queryGenreFeed(genre).addOnSuccessListener {
            adapter.updateTable(FirestoreUtility.convertQueryToPosts(it))
        }
    }

    // Called when screen is loaded to saved posts feed
    private fun updateTableDataWithSavedPosts() {
        if (!this::adapter.isInitialized) return
        FirestoreUtility.querySavedPostsFeed().addOnSuccessListener {
            adapter.updateTable(FirestoreUtility.convertQueryToPosts(it))
        }
    }

    // Allow back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return true
    }

}