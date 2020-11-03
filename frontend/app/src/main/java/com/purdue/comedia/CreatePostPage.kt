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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_create_post_page.*

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

    public fun createPost() {
        val new_post = PostModel()
        new_post.type = grabPostType()
        new_post.title = postTitleField.text.toString()
        new_post.content = postBodyField.text.toString()
        new_post.genre = postGenreField.text.toString().toLowerCase()
        new_post.isAnon = postAnonymousSwitch.isChecked

        if (auth.uid == null) {
            postTitleField.error = "You must be logged in"
            postTitleField.requestFocus()
            return
        }

        if (new_post.title.isEmpty()) {
            postTitleField.error = "Please enter a title"
            postTitleField.requestFocus()
            return
        }
        if (new_post.content.isEmpty()) {
            postBodyField.error = "A body is required"
            postBodyField.requestFocus()
            return
        }
        if (new_post.type.isEmpty()) {
            return
        }
        if ((new_post.type == "img" || new_post.type == "url") && !URLUtil.isValidUrl(new_post.content)) {
            postBodyField.error = "Please enter a valid URL"
            postBodyField.requestFocus()
            return
        }

        FirestoreUtility.createPost(auth.uid, new_post)
        Toast.makeText(baseContext, "Post Created.", Toast.LENGTH_SHORT).show()
        finish()
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