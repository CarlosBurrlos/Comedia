package com.purdue.comedia

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_genres_and_user_list.*
import kotlinx.android.synthetic.main.genres_and_users_row.view.*


class GenresAndUserList : AppCompatActivity() {
    private var alphabetically = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genres_and_user_list)

        val isGenre = intent.extras!!.getBoolean(MainAdapter.IS_GENRE)
        if (isGenre) supportActionBar?.title = "Genres Following"
        else supportActionBar?.title = "Users Following"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        if (isGenre) {
            radioGroupUsers.alpha = 0F
            radioUserAlphabetical.isEnabled = false
            radioUserRelevance.isEnabled = false
            genresAndUsersRecyclerView.layoutParams.height = 1830
        } else {
            radioGroupUsers.alpha = 1F
            radioUserAlphabetical.isEnabled = true
            radioUserRelevance.isEnabled = true
            genresAndUsersRecyclerView.layoutParams.height = 1700
        }

        radioUserAlphabetical.isChecked = alphabetically
        radioUserRelevance.isChecked = !alphabetically

        val model = FirestoreUtility.currentUser.model

        // Setup Recycler View
        val recyclerView: RecyclerView = findViewById(R.id.genresAndUsersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // Position to absolute pos.

        // Re-fetch list
        radioUserAlphabetical.setOnClickListener {
            alphabetically = true
            fetchUsersAlphabetically(recyclerView, model, isGenre)
        }
        radioUserRelevance.setOnClickListener {
            alphabetically = false
            fetchUsersByRelevance(recyclerView, model, isGenre)
        }

        if (isGenre) {
            val stringArray: List<String> = model.genresFollowing
            recyclerView.adapter = GenresUsersAdapter(isGenre, stringArray) // Setup table logic
        } else {
            if (alphabetically) fetchUsersAlphabetically(recyclerView, model, isGenre)
            else fetchUsersByRelevance(recyclerView, model, isGenre)
        }
    }

    private fun fetchUsersAlphabetically(recyclerView: RecyclerView, model: UserModel, isGenre: Boolean) {
        // Todo: Ensure this sorts alphabetically
        FirestoreUtility.resolveUserReferences(model.usersFollowing)
            .addOnSuccessListener { resolvedUsers ->
                val usersFollowing = resolvedUsers.map { it.username }
                recyclerView.adapter = GenresUsersAdapter(isGenre, usersFollowing)
            }
    }

    private fun fetchUsersByRelevance(recyclerView: RecyclerView, model: UserModel, isGenre: Boolean) {
        // Todo: Fetch user list sorted by relevance

    }

    // Allow back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return true
    }

}

// Recycler View Manager
class GenresUsersAdapter(val isGenre: Boolean, var itemStrings: List<String>) :
    RecyclerView.Adapter<CustomViewHolder>() {

    override fun getItemCount(): Int {
        return itemStrings.size
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

        if (isGenre) {
            view.listGenreTextView.setOnClickListener {
                val intent = Intent(context, CustomFeed::class.java)
                intent.putExtra(MainAdapter.NAV_TITLE, view.listGenreTextView.text.toString())
                intent.putExtra(MainAdapter.GENRE, view.listGenreTextView.text.toString())
                view.context.startActivity(intent)
            }
        } else {
            view.listGenreTextView.setOnClickListener {
                val intent = Intent(context, ProfileView::class.java)
                intent.putExtra(MainAdapter.USERNAME, view.listGenreTextView.text.toString())
                view.context.startActivity(intent)
            }
        }

        // Setup unfollow button
        view.btnUnfollowGenreOrUser.setOnClickListener {
            val str = itemStrings[rowIndex]
            if (isGenre) {
                FirestoreUtility.unfollowGenre(str).addOnSuccessListener {
                    itemStrings = FirestoreUtility.currentUser.model.genresFollowing
                    notifyDataSetChanged()
                    Toast.makeText(context, "Unfollowed \"$str\"", Toast.LENGTH_SHORT).show()
                }
            } else {
                FirestoreUtility.unfollowUser(str).continueWithTask {
                    FirestoreUtility.resolveUserReferences(
                        FirestoreUtility.currentUser.model.usersFollowing
                    )
                        .addOnSuccessListener { resolvedUsers ->
                            itemStrings = resolvedUsers.map { it.username }
                            notifyDataSetChanged()
                            Toast.makeText(context, "Unfollowed \"$str\"", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
        }

    }

}