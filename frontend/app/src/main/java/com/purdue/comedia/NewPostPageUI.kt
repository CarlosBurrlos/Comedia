package com.purdue.comedia

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import kotlinx.android.synthetic.main.activity_new_post_page_ui.*
import kotlinx.android.synthetic.main.post_row.view.*

class NewPostPageUI : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post_page_ui)

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        val postID = intent.getStringExtra(MainAdapter.POST_ID)
        val post: PostModel = grabPostObject(postID)

//        FirestoreUtility.resolveUserReference(post.poster!!).addOnSuccessListener {
//            postPageUsername.text = it.username
//            updateProfilePicture(it.username)
//        }
//
//        postPageBody.text = post.content
//        postPageTitle.text = post.title
//        postPageGenre.text = post.genre

    }

    private fun grabPostObject(postID: String?): PostModel {
        // Todo: return post model with all the information from postID then uncomment lines above
        return PostModel()
    }

    private fun updateProfilePicture(username: String) {
        FirestoreUtility.queryForUserByName(username).addOnSuccessListener {
            FirestoreUtility.resolveProfileReference(it.profile!!).addOnSuccessListener {
                ProfileTab.RetrieveImageTask(::setProfileImage).execute(it.profileImage)
            }
        }
    }

    private fun setProfileImage(image: Bitmap?, toSetProfileImg: ImageView? = null) {
        val drawable: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources, image)
        drawable.isCircular = true
        postPageImage.setImageBitmap(image)
        postPageImage.setImageDrawable(drawable)
    }

    // Allow back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return true
    }

}