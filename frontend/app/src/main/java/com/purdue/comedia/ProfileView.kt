package com.purdue.comedia

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_profile_view.*

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

        // Hide follow button if viewing your own profile
        hideFollowButtonIfSelf(username, userbtnFollow)

        FirestoreUtility.queryForUserByName(username).addOnSuccessListener {
            FirestoreUtility.resolveProfileReference(it.profile!!).addOnSuccessListener {
                userProfileBio.text = it.biography
            }
        }

        FirestoreUtility.queryForUserRefByName(username)
            .addOnSuccessListener {
                if (it in FirestoreUtility.currentUser!!.model.usersFollowing) {
                    userbtnFollow.text =  "Unfollow"
                } else {
                    userbtnFollow.text = "Follow"
                }
            }


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

    private fun hideFollowButtonIfSelf(username: String, followButton: Button) {
        FirestoreUtility.queryForUserByUID(FirebaseAuth.getInstance().uid!!, {
            if (it.username == username) {
                followButton.alpha = 0F
                followButton.isClickable = false
                followButton.isFocusable = false
            }
        })
    }

    private fun setProfileImage(image: Bitmap?, toSetProfileImg: ImageView?) {
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
        val beginFollowing = (followBtn.text.toString().toLowerCase() == "follow")
        if (followBtn.text.toString().toLowerCase() == "follow") followBtn.text = "Unfollow"
        else followBtn.text = "Follow"

        if (beginFollowing) {
            FirestoreUtility.followUser(username)
        } else {
            FirestoreUtility.unfollowUser(username)
        }
    }

    // Allow back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return true
    }

}