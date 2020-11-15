package com.purdue.comedia

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.feed_tab.*

/**
 * A Fragment representing the Feed Tab
 */
class FeedTab : Fragment() {
    // 3. Declare Parameters here
    private lateinit var adapter: MainAdapter
    var onChronological = true

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
        val radioGenres: RadioButton = root.findViewById(R.id.radioGenres)
        onChronological = radioChronological.isChecked

        if (radioChronological.isChecked) updateTableData()
        else updateTableDataWithRelevance()

        radioChronological.setOnClickListener {
            onChronological = true
            updateTableData()
        }
        radioRelevance.setOnClickListener {
            onChronological = false
            updateTableDataWithRelevance()
        }
        radioGenres.setOnClickListener { updateWithGenres() }

        return root
    }

    private fun updateWithGenres() {
        if (!this::adapter.isInitialized) return
        // Setup Genre Filter Alert
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select genres to view your genre-only timeline")

        // Add a checkbox list
        val genres: Array<String> = FirestoreUtility.currentUser.model.genresFollowing.toTypedArray().copyOf()
        val checkedItems = BooleanArray(genres.size) {true}
        builder.setMultiChoiceItems(genres, checkedItems) { dialog, index, isChecked ->
            // The user checked or unchecked a box
            checkedItems[index] = isChecked
        }.setPositiveButton("OK") { dialog, which ->
            if (!checkedItems.contains(true)) {
                Toast.makeText(context, "You must include at least 1 genre", Toast.LENGTH_SHORT).show()
                if (onChronological) radioChronological.isChecked = true
                else radioRelevance.isChecked = true
                builder.create().show()
            } else {
                val chosenGenres: ArrayList<String> = genres.toCollection(ArrayList())
                for (i in chosenGenres.indices.reversed()) {
                    if (!checkedItems[i]) chosenGenres.removeAt(i)
                }
                updateTableWithGenres(chosenGenres)
            }
        }.setNegativeButton("Cancel") {  dialog, which ->
            dialog.cancel()
        }.create().show()
    }

    // Called when screen is loaded to populate feed
    private fun updateTableData() {
        if (!this::adapter.isInitialized) return
        FirestoreUtility.queryMainFeed().addOnSuccessListener {
            adapter.updateTable(it)
        }
    }

    private fun updateTableWithGenres(chosenGenres: ArrayList<String>) {
        if (!this::adapter.isInitialized) return
        // Todo: Update table with the selected genres
    }

    private fun updateTableDataWithRelevance() {
        if (!this::adapter.isInitialized) return
        // Todo: Update adapter with posts sorted by relevance
    }

}