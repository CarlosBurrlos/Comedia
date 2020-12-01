package com.purdue.comedia

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    companion object {
        var onChronological = true
        var onRelevance = false
        var onGenre = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup Tabs through 'PagerAdapter' class
        val sectionsPagerAdapter = PagerAdapter(supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)

        // Setup Add Post Button
        val newPostBtn: FloatingActionButton = findViewById(R.id.btnCreatePost)
        newPostBtn.setOnClickListener { view ->
            // Check if signed in
            if (FirebaseAuth.getInstance().currentUser != null) {
                startActivity(Intent(view!!.context, CreatePostPage::class.java))
            } else {
                Snackbar.make(
                    findViewById(android.R.id.content),
                    "You must be signed in to create a post", Snackbar.LENGTH_LONG)
                    .setAction("Login") {
                        startActivity(Intent(view!!.context, SignUp::class.java))
                    }.show()
            }
        }

    }
}