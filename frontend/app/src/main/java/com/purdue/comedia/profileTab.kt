package com.purdue.comedia

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.profile_tab.*

/**
 * A Fragment representing the profile Tab
 */
class profileTab : Fragment() {

    // 3. Declare Parameters here
    private var sampleVar2: String? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // 4. Initialize Parameters here from newInstance
            sampleVar2 = it.getString(ARG_PARAM1)
        }
    }

    var theLoginBtn: Button? = null

    // Change Sign In Button Text
    override fun onResume() {
        super.onResume()
        if (theLoginBtn != null && auth.currentUser != null) {
            theLoginBtn!!.text = "Sign Out"
        } else {
            theLoginBtn!!.text = "Sign In"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.profile_tab, container, false)

        /** Setup the elements of the view here **/

        // Login Button Functionality
        val loginBtn: Button = root.findViewById(R.id.btnToLoginPage)
        theLoginBtn = loginBtn
        if (auth.currentUser != null) {
            loginBtn.text = "Sign out"
        } else {
            loginBtn.text = "Sign In"
        }

        loginBtn.setOnClickListener {
            if (auth.currentUser == null) {
                // Go to sign up screen
                startActivity(Intent(view!!.context, SignUp::class.java))
            } else {
                // Sign Out
                auth.signOut()
                loginBtn.text = "Sign In"
            }
        }

        return root
    }

    companion object {

        // 1. Create Arguments Here
        private const val ARG_PARAM1 = "sampleVar2"

        // Return a new instance of the feedTab
        @JvmStatic
        fun newInstance(sampleVar2: String) =
            profileTab().apply {
                arguments = Bundle().apply {
                    // 2. Put parameters into arguments from step 1
                    putString(ARG_PARAM1, sampleVar2)
                }
            }
    }
}