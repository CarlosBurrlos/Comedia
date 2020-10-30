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
import com.google.firebase.auth.FirebaseAuth
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
        if (isGenre) {
            val stringArray = grabGenresFollowing()
            recyclerView.adapter = GenresUsersAdapter(isGenre, stringArray) // Setup table logic
        } else {
            FirestoreUtility.resolveUserReferences(
                FirestoreUtility.currentUser!!.model.usersFollowing
            )
                .addOnSuccessListener { resolvedUsers ->
                    val usersFollowing = resolvedUsers.map { it.username }
                    recyclerView.adapter = GenresUsersAdapter(isGenre, usersFollowing)
                }
        }
    }

    private fun grabGenresFollowing(): List<String> {
        return FirestoreUtility.currentUser!!.model.genresFollowing
    }

    // Allow back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return true
    }

}

// Recycler View Manager
class GenresUsersAdapter(val isGenre: Boolean, val itemStrings: List<String>) :
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

        view.listGenreTextView.text = itemStrings[rowIndex]

        // Setup unfollow button
        view.btnUnfollowGenreOrUser.setOnClickListener {
            if (isGenre) {
                unfollowGenre(itemStrings[rowIndex])
            } else {
                unfollowUser(itemStrings[rowIndex])
            }
            Toast.makeText(context, "Unfollowed.", Toast.LENGTH_SHORT).show()
        }

    }

    fun unfollowGenre(genre: String) {
        FirestoreUtility.unfollowGenre(genre)
    }

    fun unfollowUser(username: String) {
        FirestoreUtility.unfollowUser(username)
    }

}