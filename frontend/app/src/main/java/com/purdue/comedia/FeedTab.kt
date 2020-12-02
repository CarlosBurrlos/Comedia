package com.purdue.comedia

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.DocumentReference
import kotlinx.android.synthetic.main.feed_tab.*


/**
 * A Fragment representing the Feed Tab
 */
class FeedTab : Fragment() {
    // 3. Declare Parameters here
    private lateinit var adapter: MainAdapter
    private lateinit var savedGenres: ArrayList<String>
    private lateinit var progress: ProgressDialog

    override fun onResume() {
        super.onResume()
        when {
            radioChronological.isChecked -> updateTableData()
            radioRelevance.isChecked -> updateTableDataWithRelevance()
            this::savedGenres.isInitialized -> updateTableWithGenres(savedGenres)
        }
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

        radioChronological.isChecked = MainActivity.onChronological
        radioRelevance.isChecked = MainActivity.onRelevance
        radioGenres.isChecked = MainActivity.onGenre

        if (radioChronological.isChecked) updateTableData()
        else if (radioRelevance.isChecked) updateTableDataWithRelevance()

        progress = ProgressDialog(context)

        radioChronological.setOnClickListener {
            progress.setMessage("Loading Main Feed")
            progress.setCancelable(false)
            progress.show()

            MainActivity.onChronological = true
            MainActivity.onRelevance = false
            MainActivity.onGenre = false
            updateTableData()
        }
        radioRelevance.setOnClickListener {
            progress.setMessage("Loading Relevance Feed")
            progress.setCancelable(false)
            progress.show()

            MainActivity.onChronological = false
            MainActivity.onRelevance = true
            MainActivity.onGenre = false
            updateTableDataWithRelevance()
        }
        radioGenres.setOnClickListener {
            MainActivity.onChronological = false
            MainActivity.onRelevance = false
            MainActivity.onGenre = true
            updateWithGenres()
        }

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
                builder.create().show()
            } else {
                val chosenGenres: ArrayList<String> = genres.toCollection(ArrayList())
                for (i in chosenGenres.indices.reversed()) {
                    if (!checkedItems[i]) chosenGenres.removeAt(i)
                }
                savedGenres = chosenGenres
                progress.setMessage("Loading Genre Feed")
                progress.setCancelable(false)
                progress.show()
                updateTableWithGenres(chosenGenres)
            }
        }.setNegativeButton("Cancel") { dialog, which ->
            if (MainActivity.onChronological) radioChronological.isChecked = true
            else radioRelevance.isChecked = true
            dialog.cancel()
        }.create().show()
    }

    // Called when screen is loaded to populate feed
    private fun updateTableData() {
        if (!this::adapter.isInitialized) return
        FirestoreUtility.queryMainFeed().addOnSuccessListener {
            progress.dismiss()
            adapter.updateTable(it)
        }
    }

    private fun updateTableWithGenres(chosenGenres: ArrayList<String>) {
        if (!this::adapter.isInitialized) return

        FirestoreUtility.queryMultiGenreFeed(chosenGenres).addOnSuccessListener {
            progress.dismiss()
            adapter.updateTable(it)
        }
    }

    private fun updateTableDataWithRelevance() {
        if (!this::adapter.isInitialized) return
        if (AuthUtility.uid() == null) return

        // Get volley queue and URL
        val queue = Volley.newRequestQueue(this.context)
        val url = "https://us-central1-comedia-6f804.cloudfunctions.net/calcRelevancy/relevantPosts?uid=${AuthUtility.uid()}"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                Log.w("E", response.toString())
                val array = response.getJSONArray("posts")
                val references = mutableListOf<DocumentReference>()
                for (i in 0 until array.length()) {
                    references.add(FirestoreUtility.postRefById(array[i] as String))
                }
                FirestoreUtility.resolvePostClientReferences(references)
                    .addOnSuccessListener {
                        progress.dismiss()
                        adapter.updateTable(it)
                    }
            },
            { error ->
                Log.w("ERROR", error.message)
            }
        )
        queue.add(jsonObjectRequest)
        queue.start()
    }

}