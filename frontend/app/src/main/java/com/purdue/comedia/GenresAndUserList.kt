package com.purdue.comedia

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import kotlinx.android.synthetic.main.activity_create_post_page.*
import kotlinx.android.synthetic.main.activity_genres_and_user_list.*
import kotlinx.android.synthetic.main.genres_and_users_row.view.*


class GenresAndUserList : AppCompatActivity() {
    private var alphabetically = true
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_genres_and_user_list)


        val isGenre = intent.extras!!.getBoolean(MainAdapter.IS_GENRE)
        val isUsersFollowing = intent.extras!!.getBoolean(MainAdapter.IS_VIEW_FOLLOWING)
        if (isGenre) supportActionBar?.title = "Genres Following"
        else if (isUsersFollowing) supportActionBar?.title = "Users You Follow"
        else supportActionBar?.title = "Your Followers"
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
            fetchUsersAlphabetically(recyclerView, model, isGenre, isUsersFollowing)
        }
        radioUserRelevance.setOnClickListener {
            alphabetically = false
            fetchUsersByRelevance(recyclerView, model, isGenre, isUsersFollowing)
        }

        if (isGenre) {
            val stringArray: List<String> = model.genresFollowing
            recyclerView.adapter = GenresUsersAdapter(isGenre, isUsersFollowing, stringArray) // Setup table logic
        } else {
            if (alphabetically) fetchUsersAlphabetically(recyclerView, model, isGenre, isUsersFollowing)
            else fetchUsersByRelevance(recyclerView, model, isGenre, isUsersFollowing)
        }
    }

    private fun fetchUsersAlphabetically(recyclerView: RecyclerView, model: UserModel, isGenre: Boolean, isUsersFollowing: Boolean) {
        if (isUsersFollowing) {
            FirestoreUtility.resolveUserReferences(model.usersFollowing).addOnSuccessListener { resolvedUsers ->
                val usersFollowing = resolvedUsers.map { it.username }.sorted()
                recyclerView.adapter = GenresUsersAdapter(isGenre, isUsersFollowing, usersFollowing)
            }
        } else {
            FirestoreUtility.resolveUserReferences(model.followers).addOnSuccessListener { resolvedUsers ->
                val usersFollowing = resolvedUsers.map { it.username }.sorted()
                recyclerView.adapter = GenresUsersAdapter(isGenre, isUsersFollowing, usersFollowing)
            }
        }
    }

    private fun fetchUsersByRelevance(recyclerView: RecyclerView, model: UserModel, isGenre: Boolean, isUsersFollowing: Boolean) {
        // Get volley queue and URL
        val queue = Volley.newRequestQueue(this)
        var url = "https://us-central1-comedia-6f804.cloudfunctions.net/calcRelevancy/relevantUsers?uid=${auth.uid}&mode="

        // Decide between followers and following
        if (isUsersFollowing) {
            url += "following";
        } else {
            url += "followers";
        }

        val jsonObjectRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                Log.w("HTTPS",response.toString())
                val ref_list = mutableListOf<DocumentReference>()
                for (i in 0 until response.length()) {
                    ref_list.add(FirestoreUtility.userRefByUID(response.getString(i)))
                    Log.w("HTTPS",response.getString(i))
                }
                FirestoreUtility.resolveUserReferences(ref_list).addOnSuccessListener {
                    resolvedUsers ->
                    val usersFollowing = resolvedUsers.map { it.username }
                    recyclerView.adapter = GenresUsersAdapter(isGenre, isUsersFollowing, usersFollowing)
                }
            },
            { error ->
                Log.w("ERROR",error.message)
            }
        )
        queue.add(jsonObjectRequest)
    }

    // Allow back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return true
    }

}

// Recycler View Manager
class GenresUsersAdapter(private val isGenre: Boolean, val isUsersFollowing: Boolean, var itemStrings: List<String>) :
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

        if (isUsersFollowing) {
            view.btnUnfollowGenreOrUser.isEnabled = true
            view.btnUnfollowGenreOrUser.alpha = 1F

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
        } else {
            view.btnUnfollowGenreOrUser.isEnabled = false
            view.btnUnfollowGenreOrUser.alpha = 0F
        }

    }

}