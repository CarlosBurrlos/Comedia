package com.purdue.comedia

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.klinker.android.link_builder.Link
import com.klinker.android.link_builder.applyLinks
import kotlinx.android.synthetic.main.activity_new_post_page_ui.*
import kotlinx.android.synthetic.main.post_row.view.*
import java.lang.Exception

class NewPostPageUI : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_post_page_ui)

        supportActionBar?.title = "View Post"
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button

        val postID = intent.getStringExtra(MainAdapter.POST_ID) ?: ""
        FirestoreUtility.queryForPostById(postID)
            .addOnSuccessListener {
                displayPost(it)
            }

        updateComments(postID)

        postPageBtnComment.setOnClickListener {
            if (FirebaseAuth.getInstance().currentUser != null) {
                postComment()
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "You must be signed in to post a comment", Snackbar.LENGTH_LONG
                ).setAction("Login") {
                    startActivity(Intent(baseContext, SignUp::class.java))
                }.show()
            }
        }
    }

    private fun updateComments(postID: String) {
        commentsString.text = ""
        FirestoreUtility.queryForPostById(postID).continueWithTask {
            FirestoreUtility.resolveCommentReferences(it.result!!.comments)
        }.addOnSuccessListener {
            for (comment in it) {
                commentsString.text = commentsString.text.toString() + comment.content + "\n"
            }
        }
    }

    private fun displayPost(post: PostModel) {
        if (post.isAnon) {
            postPageUsername.text = "Anonymous"
            postPageImage.setImageResource(R.drawable.anon_pic)
        } else {
            FirestoreUtility.resolveUserReference(post.poster!!).addOnSuccessListener {
                postPageUsername.text = it.username
                updateProfilePicture(it.username)

                postPageUsername.setOnClickListener {
                    val intent = Intent(baseContext, ProfileView::class.java)
                    intent.putExtra(MainAdapter.USERNAME, postPageUsername.text)
                    startActivity(intent)
                }

            }
        }

        postPageBody.text = post.content
        postPageTitle.text = post.title
        postPageGenre.text = post.genre

        if (post.type == "url") {
            val bodyUrl = post.content
            val link = Link(bodyUrl)
                .setTextColor(Color.BLUE)
                .setHighlightAlpha(.4F)
                .setUnderlined(true)
                .setBold(true)
                .setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(bodyUrl)
                    try { startActivity(intent) }
                    catch (e: Exception) {}
                }
            postPageBody.applyLinks(link)
        }

    }

    private fun postComment() {
        val inputText = EditText(this)
        inputText.hint = "Enter your comment"

        val textInputLayout = TextInputLayout(this)
        textInputLayout.setPadding(
            resources.getDimensionPixelOffset(R.dimen.dp_19), 0,
            resources.getDimensionPixelOffset(R.dimen.dp_19), 0
        )

        textInputLayout.addView(inputText)

        val alert = AlertDialog.Builder(this)
            .setTitle("Post Comment")
            .setView(textInputLayout)
            .setPositiveButton("Comment") { dialog, _ ->
                // Handle new profile information
                val commentStr = inputText.text.toString()
                if (commentStr.isEmpty()) dialog.cancel()
                else makeComment(commentStr,intent.getStringExtra(MainAdapter.POST_ID)!!)
                dialog.cancel()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }.create()

        alert.show()
    }

    private fun makeComment(commentStr: String, postID: String) {
        FirestoreUtility.createComment(commentStr, postID).addOnSuccessListener {
            Toast.makeText(baseContext, "Comment posted.", Toast.LENGTH_SHORT).show()
            updateComments(postID)
        }
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