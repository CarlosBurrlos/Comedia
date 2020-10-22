package com.purdue.comedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.genres_and_users_row.view.*
import kotlinx.android.synthetic.main.post_row.view.*

class GenresAndUserList : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genres_and_user_list)

        val isGenre = intent.extras!!.getBoolean(MainAdapter.IS_GENRE)
        var anStr = "Genre"

        if (isGenre) {
            supportActionBar?.title = "Genres Following"
        } else {
            supportActionBar?.title = "Users Following"
            anStr = "User"
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        // Setup Recycler View
        val recyclerView: RecyclerView = findViewById(R.id.genresAndUsersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // Position to absolute pos.
        var stringArray = grabGenresFollowing(isGenre)
        stringArray = Array(3) { i -> "$anStr $i" } // Remove once array has been populated
        recyclerView.adapter = GenresUsersAdapter(isGenre, stringArray) // Setup table logic

    }

    // ToDo: Return string array of genres or users following
    fun grabGenresFollowing(isGenre: Boolean): Array<String> {
        return Array(3) { i -> "$i" }
    }

    // Allow back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return true
    }

}

// Recycler View Manager
class GenresUsersAdapter(val isGenre: Boolean, val itemStrings: Array<String>) :
    RecyclerView.Adapter<CustomViewHolder>() {

    override fun getItemCount(): Int {
        return itemStrings.size // Arbitrary placeholder
    }

    // Initialize Row View
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(p0.context)
        val cellForRow = layoutInflater.inflate(R.layout.genres_and_users_row, p0, false)
        return CustomViewHolder(cellForRow)
    }

    // Setup Row UI Elements
    override fun onBindViewHolder(holder: CustomViewHolder, rowIndex: Int) {
        val view = holder.view
        val context = holder.view.context

        view.listGenreTextView.text = itemStrings.get(rowIndex)

        // Setup Profile Page
        view.btnUnfollowGenreOrUser.setOnClickListener {
            if (isGenre) {
                unfollowGenre(itemStrings.get(rowIndex))
            } else {
                unfollowUser(itemStrings.get(rowIndex))
            }
            Toast.makeText(context, "Unfollowed from row $rowIndex", Toast.LENGTH_SHORT).show()
        }

    }

    // ToDo: Unfollow genre by specified string
    fun unfollowGenre(genre: String) {

    }

    // ToDo: Unfollow user by specified username string
    fun unfollowUser(username: String) {

    }

}