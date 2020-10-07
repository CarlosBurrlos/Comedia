package com.purdue.comedia

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.profile_tab.*
import java.io.BufferedInputStream
import java.lang.Exception
import java.net.URL


/**
 * A Fragment representing the profile Tab
 */
class profileTab : Fragment() {

    // 3. Declare Parameters here
    private var sampleVar2: String? = null
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var savedProfileUrl = ""

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
        updateProfile()
    }

    private fun loadUserProfile(uid: String?) {
        if (uid == null) return
        queryForUser(uid, ::loadProfileTabView) { e -> println(e) }
    }

    private fun queryForUser(
        uid: String,
        successCallback: ((DocumentSnapshot) -> Unit)? = null,
        failureCallback: ((Exception) -> Unit)? = null
    ): Task<DocumentSnapshot> {
        return firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { successCallback?.invoke(it) }
            .addOnFailureListener { failureCallback?.invoke(it) }
    }

    private fun loadProfileTabView(snapshot: DocumentSnapshot) {
        (snapshot.get("profile") as DocumentReference?)
            ?.get()
            ?.addOnSuccessListener { loadProfileFields(it) }
            ?.addOnFailureListener { e -> println(e) }
        profileUsername.text = snapshot.get("username") as String? ?: "Unknown"
    }

    private fun loadProfileFields(snapshot: DocumentSnapshot) {
        val url = (snapshot.get("profileImage") as String?) ?: ""
        savedProfileUrl = url
        RetrieveImageTask(::setProfileImage).execute(url)
        bioTextProfilePage.text = snapshot.get("biography") as String? ?: ""
    }

    private class RetrieveImageTask(imageCallback: (Bitmap?) -> Unit) :
        AsyncTask<String, Void, Bitmap?>() {
        val setImageView: (Bitmap?) -> Unit = imageCallback

        override fun doInBackground(vararg urlArgs: String?): Bitmap? {
            return downloadImage(urlArgs[0] ?: "")
        }

        private fun downloadImage(url: String): Bitmap? {
            return try {
                val avatarUrl = URL(url)
                val connection = avatarUrl.openConnection()
                connection.connect()
                val stream = BufferedInputStream(connection.getInputStream())
                BitmapFactory.decodeStream(stream)
            } catch (e: Exception) {
                println(e)
                null
            }
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            this.setImageView(result)
        }
    }

    private fun setProfileImage(image: Bitmap?) {
        profileImage.setImageBitmap(image)
    }

    // Gets run everytime the screen is presented
    override fun onResume() {
        super.onResume()

        // Change Sign In Button Text
        if (theLoginBtn != null && auth.currentUser != null) {
            theLoginBtn!!.text = "Sign Out"

            if (savedProfileUrl.isEmpty() && bioTextProfilePage.text.toString() == "No bio yet!") {
                textInputAlert()
            }

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
            if (auth.currentUser != null) {
                // Sign Out
                auth.signOut()
                loginBtn.text = "Sign In"
            }
            startActivity(Intent(view!!.context, SignUp::class.java))
        }

        val btnEditProfile: Button = root.findViewById(R.id.btnEditProfile)
        btnEditProfile.setOnClickListener {
            if (auth.currentUser != null) {
                textInputAlert() // Present alert to input the new text
            } else {
                // User not signed in, present alert
                Snackbar.make(root.findViewById(R.id.profileScreen),
                    "You must be signed in to edit your profile", Snackbar.LENGTH_LONG)
                    .setAction("Login") {
                        startActivity(Intent(view!!.context, SignUp::class.java))
                    }.show()
            }
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
                if (bioText.text.isEmpty()) bioText.setText(bioTextProfilePage.text)
                if (profileUrl.text.isEmpty()) profileUrl.setText(savedProfileUrl)
                editProfileOntoFirebase(profileUrl.text.toString(), bioText.text.toString())
                updateProfile()
                dialog.cancel()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }.create()

        alert.show()

    }

    // Called once a user edits the profile. New strings are passed to this function.
    private fun editProfileOntoFirebase(profileImageUrl: String = "", bioText: String) {
        if (auth.uid == null) return
        if (profileImageUrl.isEmpty() && bioText.isEmpty()) return
        val profileModel = PartialProfileModel()
        profileModel.profileImage = profileImageUrl
        profileModel.biography = bioText
        saveUserProfile(auth.uid as String, profileModel)
    }

    private fun saveUserProfile(uid: String, profileModel: PartialProfileModel) {
        queryForUser(uid, {
            val profileReference = it.get("profile") as DocumentReference?
            val setOptions = SetOptions.merge()
            profileReference?.set(profileModel, setOptions)
        })
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
