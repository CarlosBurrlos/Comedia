package com.purdue.comedia

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * A Fragment representing the Feed Tab
 */
class feedTab : Fragment() {
    // 3. Declare Parameters here
    private var sampleVar1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // 4. Initialize Parameters here from newInstance
            sampleVar1 = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.feed_tab, container, false)

        // Setup the elements of the view here
        val textView: TextView = root.findViewById(R.id.fragment_one_text)
        textView.text = "This will be the feed"

        return root
    }

    companion object {

        // 1. Create Arguments Here
        private const val ARG_PARAM1 = "sampleVar1"

        // Return a new instance of the feedTab
        @JvmStatic
        fun newInstance(sampleVar1: String) =
            feedTab().apply {
                arguments = Bundle().apply {
                    // 2. Put parameters into arguments from step 1
                    putString(ARG_PARAM1, sampleVar1)
                }
            }
    }
}