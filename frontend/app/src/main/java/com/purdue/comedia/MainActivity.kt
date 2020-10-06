package com.purdue.comedia

import android.content.Intent
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

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
            startActivity(Intent(view!!.context, CreatePostPage::class.java))
        }

    }
}