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
    private var sampleVar1: String? = null
    private lateinit var adapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // 4. Initialize Parameters here from newInstance
            sampleVar1 = it.getString(ARG_PARAM1)
        }
    }

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
        val textView: TextView = root.findViewById(R.id.fragment_one_text)
        textView.text = "User Feed"

        // Setup Recycler View
        val recyclerView: RecyclerView = root.findViewById(R.id.feedRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context) // Positions to absolute position
        adapter = MainAdapter()
        updateTableData()
        recyclerView.adapter = adapter // Setup table logic

        return root
    }

    private fun updateTableData() {
        if (!this::adapter.isInitialized) return
        FirestoreUtility.queryProfileFeed(FirebaseAuth.getInstance().uid!!,{
            adapter.updateTable(FirestoreUtility.convertQueryToPosts(it))
        })
    }

    companion object {

        // 1. Create Arguments Here
        private const val ARG_PARAM1 = "sampleVar1"

        // Return a new instance of the feedTab
        @JvmStatic
        fun newInstance(sampleVar1: String) =
            FeedTab().apply {
                arguments = Bundle().apply {
                    // 2. Put parameters into arguments from step 1
                    putString(ARG_PARAM1, sampleVar1)
                }
            }
    }
}