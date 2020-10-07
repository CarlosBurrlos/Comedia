package com.purdue.comedia

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.webkit.URLUtil
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_create_post_page.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class CreatePostPage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post_page)

        auth = FirebaseAuth.getInstance()

        supportActionBar?.title = "Post a Joke"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnSubmitPost.setOnClickListener {
            createPost()
        }

        radioBtnImage.setOnClickListener { postBodyField.hint = "Enter a joke image URL" }
        radioBtnUrl.setOnClickListener { postBodyField.hint = "Enter a joke website URL" }
        radioBtnText.setOnClickListener { postBodyField.hint = "Joke Body" }

    }

    // Called when user presses the 'Post' button
    private fun createPost() {
        val postType = grabPostType() // 1: Text, 2: Image URL, 3: URL
        val title = postTitleField.text.toString()
        val body = postBodyField.text.toString()
        val genre = postGenreField.text.toString()
        val isAnonymous = postAnonymousSwitch.isActivated

        if (title.isEmpty()) {
            postTitleField.error = "Please enter a title"
            postTitleField.requestFocus()
            return
        }
        if (body.isEmpty()) {
            postBodyField.error = "A body is required"
            postBodyField.requestFocus()
            return
        }
        if ((postType == "img" || postType == "url") && !URLUtil.isValidUrl(body)) {
            postBodyField.error = "Please enter a valid URL"
            postBodyField.requestFocus()
            return
        }

        // Create post model
        val postModel = PostModel()
        postModel.type = postType
        postModel.title = title
        postModel.content = body
        postModel.genre = genre
        postModel.isAnon = isAnonymous
        postModel.poster = firestore.collection("users").document(auth.uid!!)

        // Add the post model to the database
        firestore.collection("posts").add(postModel).addOnSuccessListener {
            // On a newly created post, get the post reference (for references)
            newPost ->
            firestore.collection("users").document(auth.uid!!).get().addOnSuccessListener {
                // Add the newly created post to the created post list of the user
                user ->
                val postList = user.get("createdPosts") as ArrayList<DocumentReference>
                postList.add(newPost)
                firestore.collection("users").document(auth.uid!!).update("createdPosts",postList).addOnSuccessListener {
                    finish() // Dismisses the screen
                }
            }
        }
    }

    private fun grabPostType(): String {
        if (radioBtnText.isChecked) return "txt"
        if (radioBtnImage.isChecked) return "img"
        if (radioBtnUrl.isChecked) return "url"
        else return "txt"
    }

    /** Helper Functions **/

    // Allow back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        this.finish()
        return true
    }

    // Hide Keyboard on a non-text field screen tap
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun snack(str: String) {
        Snackbar.make(findViewById(android.R.id.content), str, Snackbar.LENGTH_LONG).show()
    }

    private fun toast(str: String) {
        Toast.makeText(baseContext, str, Toast.LENGTH_LONG).show()
    }
}