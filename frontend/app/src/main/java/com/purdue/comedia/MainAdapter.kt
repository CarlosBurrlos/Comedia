package com.purdue.comedia

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.post_row.view.*

// Recycler View Manager
class MainAdapter: RecyclerView.Adapter<CustomViewHolder>() {
    lateinit var postArray: List<PostModelClient>

    override fun getItemCount(): Int {
        return 3 // Arbitrary placeholder
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

        view.feedPostTitle.text = "Joke Title. Row: $rowIndex"

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

class CustomViewHolder(val view: View): RecyclerView.ViewHolder(view) { }