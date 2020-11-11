package com.purdue.comedia

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firestore.v1.FirestoreGrpc

/**
 * A Fragment representing the Feed Tab
 */
class FeedTab : Fragment() {
    // 3. Declare Parameters here
    private lateinit var adapter: MainAdapter

    override fun onResume() {
        super.onResume()
        updateTableData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.feed_tab, container, false)

        // Setup the elements of the view here

        // Setup Reload button
        val btnReloadFeed: Button = root.findViewById(R.id.btnReloadFeed)
        btnReloadFeed.setOnClickListener{
            (activity as MainActivity).recreate()
        }

        // Setup Recycler View
        val recyclerView: RecyclerView = root.findViewById(R.id.feedRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context) // Positions to absolute position
        adapter = MainAdapter()
        recyclerView.adapter = adapter // Setup table logic
        updateTableData()

        return root
    }

    // Called when screen is loaded to populate feed
    private fun updateTableData() {
        if (!this::adapter.isInitialized) return
        FirestoreUtility.queryMainFeed().addOnSuccessListener {
            adapter.updateTable(it)
        }
    }

}