package com.purdue.comedia

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.applyLinks
import kotlinx.android.synthetic.main.post_row.view.*

// Recycler View Manager
class MainAdapter : RecyclerView.Adapter<CustomViewHolder>() {
    lateinit var postArray: List<PostModelClient>
    lateinit var contextVar: Context
    private val anonImages = hashSetOf<ImageView>()

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
        const val IS_VIEW_FOLLOWING = "IS_VIEW_FOLLOWING"

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

        view.feedPostTitle.text = post.model.title
        if (post.model.type != "img") view.feedPostBody.text = post.model.content
        view.feedPostGenre.text = post.model.genre
        contextVar = context

        if (post.model.isAnon) {
            view.feedProfileAuthor.text = "Anonymous"
            view.feedProfileImage.setImageResource(R.drawable.anon_pic)
            anonImages.add(view.feedProfileImage)
        } else if (post.model.poster != null) {
            FirestoreUtility.resolveUserReference(post.model.poster!!).addOnSuccessListener {
                view.feedProfileAuthor.text = it.username
                updateProfilePicture(view.feedProfileAuthor.text.toString(), view.feedProfileImage)
            }
        }

        view.feedBtnUpvote.isClickable = true
        view.feedBtnUpvote.isEnabled = true
        view.feedBtnDownvote.isClickable = true
        view.feedBtnDownvote.isEnabled = true

        view.feedBtnUpvote.text = "Upvote (" + post.model.upvoteCount + ")"
        view.feedBtnDownvote.text = "Downvote (" + post.model.downvoteCount + ")"
        view.feedBtnComment.text = "Comments (" + post.model.comments.size + ")"

        val title = view.feedPostTitle

        // Set correct title view height
        var bounds = Rect()
        title.paint.getTextBounds(title.text, 0, title.text.length, bounds)
        var width = bounds.width().toFloat()
        title.layoutParams.height = (kotlin.math.ceil(width/622) * 98).toInt()

        if (post.model.type == "img") {
            view.feedImageView.alpha = 1F
            view.feedPostBody.alpha = 0F
            view.feedImageView.setImageResource(R.drawable.loading_indicator)
            ProfileTab.RetrieveImageTask(::setFeedImage, view.feedImageView).execute(post.model.content)
        } else {
            view.feedImageView.alpha = 0F
            view.feedPostBody.alpha = 1F
            view.feedImageView.layoutParams.height = 0

            val body = view.feedPostBody

            // Set correct body view height
            bounds = Rect()
            body.paint.getTextBounds(body.text, 0, body.text.length, bounds)
            width = bounds.width().toFloat()
            body.layoutParams.height = (kotlin.math.ceil(width/982) * 78).toInt()

            // Show text as hyperlink
            if (post.model.type == "url") {
                val bodyUrl = post.model.content
                val link = Link(bodyUrl).setTextColor(Color.BLUE).setHighlightAlpha(.4F)
                    .setUnderlined(true).setBold(true).setOnClickListener {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(bodyUrl)
                        try { view.context.startActivity(intent) }
                        catch (e: Exception) {}
                    }
                body.applyLinks(link)
            }
        }

        // Setup tapping on title and comments button to go to Post Page
        view.feedPostTitle.setOnClickListener { goToPost(post, view) }
        view.feedBtnComment.setOnClickListener { goToPost(post, view) }

        // Setup tapping on Profile and username to go to Profile Page
        view.feedProfileImage.setOnClickListener { goToProfile(view) }
        view.feedProfileAuthor.setOnClickListener { goToProfile(view) }

        if (FirestoreUtility.postRefById(post.postID) in FirestoreUtility.currentUser.model.savedPosts) {
            view.feedBtnSave.text = "Unsave"
        } else {
            view.feedBtnSave.text = "Save"
        }

        // Setup Tapping on save post button
        view.feedBtnSave.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null) {
                handlePostSave(post.postID, view.feedBtnSave)
            } else {
                Toast.makeText(context, "Sign in to save this post", Toast.LENGTH_SHORT).show()
            }
        }

        // Setup Tapping on Genre
        view.feedPostGenre.setOnClickListener {
            val intent = Intent(context, CustomFeed::class.java)
            intent.putExtra(NAV_TITLE, view.feedPostGenre.text.toString())
            intent.putExtra(GENRE, view.feedPostGenre.text.toString())
            view.context.startActivity(intent)
        }

        if (FirebaseAuth.getInstance().currentUser != null) {
            if (FirestoreUtility.currentUser.reference in post.model.downvoteList) {
                var text = view.feedBtnDownvote.text.toString().toLowerCase()
                text = text.replace("downvote", "Downvoted")
                view.feedBtnDownvote.text = text
            }
            if (FirestoreUtility.currentUser.reference in post.model.upvoteList) {
                var text = view.feedBtnUpvote.text.toString().toLowerCase()
                text = text.replace("upvote", "Upvoted")
                view.feedBtnUpvote.text = text
            }
        }

        // Setup Downvote functionality
        view.feedBtnDownvote.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null) {
                view.feedBtnUpvote.isClickable = false
                view.feedBtnDownvote.isClickable = false
                view.feedBtnDownvote.isEnabled = false
                if (FirestoreUtility.currentUser.reference in post.model.downvoteList) {
                    removeDownvote(post, true)
                } else {
                    if (FirestoreUtility.currentUser.reference in post.model.upvoteList) {
                        removeUpvote(post, false)
                            .continueWithTask {
                                addDownvote(post)
                            }
                    }
                    else addDownvote(post)
                }
            } else {
                Toast.makeText(context, "Sign in to downvote post", Toast.LENGTH_SHORT).show()
            }
        }

        // Setup Upvote functionality
        view.feedBtnUpvote.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null) {
                view.feedBtnUpvote.isClickable = false
                view.feedBtnDownvote.isClickable = false
                view.feedBtnUpvote.isEnabled = false
                if (FirestoreUtility.currentUser.reference in post.model.upvoteList) {
                    removeUpvote(post, true)
                } else {
                    if (FirestoreUtility.currentUser.reference in post.model.downvoteList) {
                        removeDownvote(post, false)
                            .continueWithTask {
                                addUpvote(post)
                            }
                    }
                    else addUpvote(post)
                }
            } else {
                Toast.makeText(context, "Sign in to upvote post", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun setFeedImage(image: Bitmap?, toSetProfileImg: ImageView?) {
        toSetProfileImg?.setImageBitmap(image)
        toSetProfileImg?.alpha = 1F
    }

    private fun addDownvote(post: PostModelClient): Task<PostModel> {
        return FirestoreUtility.downvote(post.postID)
            .continueWithTask {
                FirestoreUtility.resolvePostReference(post.reference)
            }
            .addOnSuccessListener {
                post.model = it
                notifyDataSetChanged()
            }
    }

    private fun removeDownvote(post: PostModelClient, onlyDownvote: Boolean): Task<PostModel> {
        return FirestoreUtility.undownvote(post.postID)
            .continueWithTask {
                FirestoreUtility.resolvePostReference(post.reference)
            }
            .addOnSuccessListener {
                post.model = it
                if (onlyDownvote) notifyDataSetChanged()
            }
    }

    private fun addUpvote(post: PostModelClient): Task<PostModel> {
        return FirestoreUtility.upvote(post.postID)
            .continueWithTask {
                FirestoreUtility.resolvePostReference(post.reference)
            }
            .addOnSuccessListener {
                post.model = it
                notifyDataSetChanged()
            }
    }

    private fun removeUpvote(post: PostModelClient, onlyUpvote: Boolean): Task<PostModel> {
        return FirestoreUtility.unupvote(post.postID)
            .continueWithTask {
                FirestoreUtility.resolvePostReference(post.reference)
            }
            .addOnSuccessListener {
                post.model = it
                if (onlyUpvote) notifyDataSetChanged()
            }
    }

    private fun goToPost(post: PostModelClient, view: View) {
        val intent = Intent(contextVar, NewPostPageUI::class.java)
        intent.putExtra(POST_ID, post.postID)
        view.context.startActivity(intent)
    }

    private fun handlePostSave(postID: String, saveBtn: Button) {
        val beginSave = (saveBtn.text.toString().toLowerCase() == "save")

        if (beginSave) {
            FirestoreUtility.savePost(postID)
                .addOnSuccessListener {
                    saveBtn.text = "unsave"
                }
        } else {
            FirestoreUtility.unsavePost(postID)
                .addOnSuccessListener {
                    saveBtn.text = "save"
                }
        }
    }

    private fun goToProfile(view: View) {
        if (view.feedProfileAuthor.text.toString().toLowerCase() != "anonymous") {
            val intent = Intent(contextVar, ProfileView::class.java)
            intent.putExtra(USERNAME, view.feedProfileAuthor.text.toString())
            view.context.startActivity(intent)
        }
    }

    private fun updateProfilePicture(username: String, profileImage: ImageView) {
        if (username.toLowerCase() == "anonymous") return
        FirestoreUtility.queryForUserByName(username)
            .continueWithTask {
                FirestoreUtility.resolveProfileReference(it.result!!.profile!!)
            }
            .addOnSuccessListener {
                ProfileTab.RetrieveImageTask(::setProfileImage, profileImage).execute(it.profileImage)
            }
    }

    private fun setProfileImage(image: Bitmap?, toSetProfileImg: ImageView?) {
        val drawable: RoundedBitmapDrawable =
            RoundedBitmapDrawableFactory.create(contextVar.resources, image)
        drawable.isCircular = true
        if (toSetProfileImg != null && !anonImages.contains(toSetProfileImg)) {
            toSetProfileImg.setImageBitmap(image)
            toSetProfileImg.setImageDrawable(drawable)
        }
    }

    fun updateTable(posts: List<PostModelClient>) {
        postArray = posts
        anonImages.clear()
        notifyDataSetChanged()
    }

}

class CustomViewHolder(val view: View) : RecyclerView.ViewHolder(view) {}