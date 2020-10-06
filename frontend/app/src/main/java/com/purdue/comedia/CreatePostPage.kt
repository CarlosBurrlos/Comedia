package com.purdue.comedia

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class CreatePostPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post_page)

        supportActionBar?.title = "Post a Joke"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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