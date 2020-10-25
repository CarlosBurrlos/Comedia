package com.purdue.comedia

import android.app.Activity
import android.os.Bundle
import com.purdue.comedia.databinding.ActivityPostPageUiBinding

class PostPageUI : Activity() {

    private lateinit var binding: ActivityPostPageUiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPostPageUiBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}