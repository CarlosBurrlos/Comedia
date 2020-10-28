package com.purdue.comedia

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_profile_view.*
import kotlinx.android.synthetic.main.profile_tab.*

/** To view profile of another user */
class ProfileView : AppCompatActivity() {
    private lateinit var adapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_view)

        val username = intent.getStringExtra(MainAdapter.USERNAME)!!

        supportActionBar?.title = username
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        userProfileUsername.text = username
        FirestoreUtility.queryForUserByName(username).addOnSuccessListener {
            FirestoreUtility.resolveProfileReference(it.profile!!).addOnSuccessListener {
                userProfileBio.text = it.biography
            }
        }

        // Todo: Set button based on if the current user is following this user or not
        userbtnFollow.text = "Follow"

        userbtnFollow.setOnClickListener {
            followOrUnfollowUser(username, userbtnFollow)
        }

        FirestoreUtility.queryForUserByName(username).addOnSuccessListener {
            FirestoreUtility.resolveProfileReference(it.profile!!).addOnSuccessListener {
                ProfileTab.RetrieveImageTask(::setProfileImage).execute(it.profileImage)
            }
        }

        // Setup Recycler View
        val recyclerView: RecyclerView = findViewById(R.id.userRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this) // Position to absolute pos.
        adapter = MainAdapter()
        recyclerView.adapter = adapter // Setup table logic
        updateTableData(username)

    }

    private fun setProfileImage(image: Bitmap?) {
        val drawable: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, image)
        drawable.isCircular = true
        userProfileImage.setImageBitmap(image)
        userProfileImage.setImageDrawable(drawable)
    }

    // Called when screen is loaded to populate genre feed
    private fun updateTableData(username: String) {
        if (!this::adapter.isInitialized) return
        FirestoreUtility.queryUserFeed(username).addOnSuccessListener {
            adapter.updateTable(FirestoreUtility.convertQueryToPosts(it))
        }
    }

    private fun followOrUnfollowUser(username: String, followBtn: Button) {
        // Toggle button text
        if (followBtn.text.toString().toLowerCase() == "follow") followBtn.text = "Unfollow"
        else followBtn.text = "Follow"

        // Todo: follow or unfollow the user with the specified username

    }

    // Allow back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return true
    }

}