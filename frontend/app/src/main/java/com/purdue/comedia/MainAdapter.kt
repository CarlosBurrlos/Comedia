package com.purdue.comedia

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.post_row.view.*
import kotlinx.android.synthetic.main.profile_tab.view.*

// Recycler View Manager
class MainAdapter : RecyclerView.Adapter<CustomViewHolder>() {
    lateinit var postArray: List<PostModelClient>
    lateinit var contextVar: Context

    override fun getItemCount(): Int {
        return if (this::postArray.isInitialized) postArray.size
        else 0
    }

    // Initialize Row View
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(p0.context)
        val cellForRow = layoutInflater.inflate(R.layout.post_row, p0, false)
        return CustomViewHolder(cellForRow)
    }

    companion object {
        // Profile
        const val NAV_TITLE = "NAV_TITLE"
        const val IS_GENRE = "IS_GENRE"

        // Feed
        const val USERNAME = "USERNAME"
        const val GENRE = "GENRE"
        const val POST_ID = "POST_ID"
    }

    // Setup Row UI Elements
    override fun onBindViewHolder(holder: CustomViewHolder, rowIndex: Int) {
        val view = holder.view
        val context = holder.view.context

        val post = postArray[rowIndex]

        view.feedPostTitle.text = post.title
        view.feedPostBody.text = post.content
        view.feedPostGenre.text = post.genre
        contextVar = context

        if (post.isAnon) view.feedProfileAuthor.text = "Anonymous"
        else {
            FirestoreUtility.resolveUserReference(post.poster!!).addOnSuccessListener {
                view.feedProfileAuthor.text = it.username
                updateProfilePicture(view.feedProfileAuthor.text.toString(), view.feedProfileImage)
            }
        }

        view.feedBtnUpvote.text = "Upvote (" + post.upvoteCount + ")"
        view.feedBtnDownvote.text = "Downvote (" + post.downvoteCount + ")"
        view.feedBtnComment.text = "Comments (" + post.comments.size + ")"

        // Setup tapping on title to go to Post Page
        view.feedPostTitle.setOnClickListener {
            val intent = Intent(context, NewPostPageUI::class.java)
            intent.putExtra(POST_ID, post.postID)
            view.context.startActivity(intent)
        }

        // Setup tapping on Profile and username to go to Profile Page
        view.feedProfileImage.setOnClickListener { goToProfile(view) }
        view.feedProfileAuthor.setOnClickListener { goToProfile(view) }

        // Setup Tapping on Genre
        view.feedPostGenre.setOnClickListener {
            val intent = Intent(context, CustomFeed::class.java)
            intent.putExtra(NAV_TITLE, view.feedPostGenre.text.toString())
            intent.putExtra(GENRE, view.feedPostGenre.text.toString())
            view.context.startActivity(intent)
        }

        // Setup Downvote functionality
        view.feedBtnDownvote.setOnClickListener {
            performDownvote(post.postID)
        }

        // Setup Upvote functionality
        view.feedBtnUpvote.setOnClickListener {
            performUpvote(post.postID)
        }

    }

    private fun performDownvote(postID: String) {
        // Todo: Downvote post based on post id
    }

    private fun performUpvote(postID: String) {
        // Todo: Upvote post based on post id
    }

    private fun goToProfile(view: View) {
        val intent = Intent(contextVar, ProfileView::class.java)
        intent.putExtra(USERNAME, view.feedProfileAuthor.text.toString())
        view.context.startActivity(intent)
    }

    private fun updateProfilePicture(username: String, profileImage: ImageView) {
        FirestoreUtility.queryForUserByName(username).addOnSuccessListener {
            FirestoreUtility.resolveProfileReference(it.profile!!).addOnSuccessListener {
                ProfileTab.RetrieveImageTask(::setProfileImage, profileImage).execute(it.profileImage)
            }
        }
    }

    private fun setProfileImage(image: Bitmap?, toSetProfileImg: ImageView?) {
        val drawable: RoundedBitmapDrawable =
            RoundedBitmapDrawableFactory.create(contextVar.resources, image)
        drawable.isCircular = true
        if (toSetProfileImg != null) {
            toSetProfileImg.setImageBitmap(image)
            toSetProfileImg.setImageDrawable(drawable)
        }
    }

    fun updateTable(posts: List<PostModelClient>) {
        postArray = posts
        notifyDataSetChanged()
    }

}

class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {}