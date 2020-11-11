package com.purdue.comedia

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

        val radioChronological: RadioButton = root.findViewById(R.id.radioChronological)
        val radioRelevance: RadioButton = root.findViewById(R.id.radioRelevance)

        if (radioChronological.isChecked) updateTableData()
        else updateTableDataWithRelevance()

        radioChronological.setOnClickListener { updateTableData() }
        radioRelevance.setOnClickListener { updateTableDataWithRelevance() }

        return root
    }

    // Called when screen is loaded to populate feed
    private fun updateTableData() {
        if (!this::adapter.isInitialized) return
        FirestoreUtility.queryMainFeed().addOnSuccessListener {
            adapter.updateTable(it)
        }
    }

    private fun updateTableDataWithRelevance() {
        if (!this::adapter.isInitialized) return
        // Todo: Update adapter with posts sorted by relevance
    }

}