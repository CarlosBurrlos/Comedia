package com.purdue.comedia

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.post_row.view.*
import kotlinx.android.synthetic.main.profile_tab.view.*

// Recycler View Manager
class MainAdapter : RecyclerView.Adapter<CustomViewHolder>() {
    lateinit var postArray: List<PostModelClient>

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
        val NAV_TITLE = "NAV_TITLE"
        val IS_GENRE = "IS_GENRE"
    }

    // Setup Row UI Elements
    override fun onBindViewHolder(holder: CustomViewHolder, rowIndex: Int) {
        val view = holder.view
        val context = holder.view.context

        val post = postArray[rowIndex]

        view.feedPostTitle.text = post.title
        view.feedPostBody.text = post.content
        view.feedPostGenre.text = post.genre

        if (post.isAnon) view.feedProfileAuthor.text = "Anonymous"
        else {
            FirestoreUtility.resolveUserReference(post.poster!!).addOnSuccessListener {
                view.feedProfileAuthor.text = it.username
            }
        }

        view.feedBtnUpvote.text = "Upvote (" + post.upvoteCount + ")"
        view.feedBtnDownvote.text = "Downvote (" + post.downvoteCount + ")"
        view.feedBtnComment.text = "Comments (" + post.comments.size + ")"

        // Setup Profile Page
        view.feedProfileImage.setOnClickListener {
            view.context.startActivity(Intent(context, ProfileView::class.java))
        }

        // Setup Tapping on Genre
        view.feedPostGenre.setOnClickListener {
            val intent = Intent(context, CustomFeed::class.java)
            intent.putExtra(NAV_TITLE, view.feedPostGenre.text.toString())
            view.context.startActivity(intent)
        }
    }

    fun updateTable(posts: List<PostModelClient>) {
        postArray = posts
        notifyDataSetChanged()
    }

}

class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {}