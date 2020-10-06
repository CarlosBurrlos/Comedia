package com.purdue.comedia

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.profile_tab.*
import java.lang.Exception


/**
 * A Fragment representing the profile Tab
 */
class profileTab : Fragment() {

    // 3. Declare Parameters here
    private var sampleVar2: String? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var displayAccountUID: String? = null
    private var profile = UserProfileModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // 4. Initialize Parameters here from newInstance
            sampleVar2 = it.getString(ARG_PARAM1)
        }
    }

    private var theLoginBtn: Button? = null

    override fun onStart() {
        super.onStart()
        loadUserProfile(auth.uid)
    }

    private fun loadUserProfile(uid: String?) {
        if (uid == null) return
        queryForUser("example", ::updateProfileTabView) { e -> println(e) }
    }

    private fun queryForUser(uid: String, successCallback: ((DocumentSnapshot) -> Unit)? = null, failureCallback: ((Exception) -> Unit)? = null): Task<DocumentSnapshot> {
        return firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { successCallback?.invoke(it) }
            .addOnFailureListener { failureCallback?.invoke(it) }
    }

    private fun updateProfileTabView(snapshot: DocumentSnapshot) {
        (snapshot.get("profile") as DocumentReference?)
            ?.get()
            ?.addOnSuccessListener { updateProfileFields(it) }
            ?.addOnFailureListener { e -> println(e) }
        profileUsername.text = snapshot.get("username") as String? ?: "Unknown"
    }

    private fun updateProfileFields(snapshot: DocumentSnapshot) {
        val avatarUri = Uri.parse(snapshot.get("profileImage") as String?)
        if (avatarUri.query != null) {
            profileImage.setImageURI(avatarUri)
        }
        bioTextProfilePage.text = snapshot.get("biography") as String? ?: ""
    }

    // Gets run everytime the screen is presented
    override fun onResume() {
        super.onResume()

        // Change Sign In Button Text
        if (theLoginBtn != null && auth.currentUser != null) {
            theLoginBtn!!.text = "Sign Out"
        } else if (theLoginBtn != null) {
            theLoginBtn!!.text = "Sign In"
        }

        // Reload data from firebase each time profile page loads
        updateProfile()

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

        val btnEditProfile: Button = root.findViewById(R.id.btnEditProfile)
        btnEditProfile.setOnClickListener {
            textInputAlert() // Present alert to input the new text
        }

        return root
    }

    private fun textInputAlert() {
        val bioText = EditText(context)
        bioText.hint = "Bio"
        val profileUrl = EditText(context)
        profileUrl.hint = "Profile"

        val subTextInputLayout = TextInputLayout(context)
        subTextInputLayout.addView(bioText)

        val textInputLayout = TextInputLayout(context)
        textInputLayout.setPadding(
            resources.getDimensionPixelOffset(R.dimen.dp_19), 0,
            resources.getDimensionPixelOffset(R.dimen.dp_19), 0
        )

        textInputLayout.addView(profileUrl)
        textInputLayout.addView(subTextInputLayout)

        val alert = AlertDialog.Builder(context)
            .setTitle("Edit Profile")
            .setView(textInputLayout)
            .setMessage("Enter bio and Image URL for profile")
            .setPositiveButton("Confirm") { dialog, _ ->
                // Handle new profile information
                editProfileOntoFirebase(profileUrl.text.toString(), bioText.text.toString())
                dialog.cancel()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }.create()

        alert.show()

    }

    // Called once a user edits the profile. New strings are passed to this function.
    private fun editProfileOntoFirebase(profileImageUrl: String, bioText: String) {
        //Todo: Upload these two strings to firebase and refresh the page: updateProfile()
    }

    // Called when the profile page loads or when the profile has been edited
    private fun updateProfile() {
        loadUserProfile(auth.uid)
    }

    // Skeleton code to setup class
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

    private fun toast(str: String) {
        Toast.makeText(context, str, Toast.LENGTH_LONG).show()
    }

}

class UserProfileModel {
    var user = ""
    var bio = ""
    var profileImage = ""
}
