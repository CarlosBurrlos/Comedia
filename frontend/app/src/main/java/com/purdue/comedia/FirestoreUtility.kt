package com.purdue.comedia

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreUtility {
    companion object {
        var firestore = FirebaseFirestore.getInstance()

        private fun reportError(error: java.lang.Exception) {
            println(error.message)
        }

        fun queryForUser(
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

        fun updateUserProfile(
            uid: String,
            profileModel: PartialProfileModel
        ): Task<Void> {
            return queryForUser(uid, failureCallback = ::reportError)
                .continueWithTask {
                    val profileReference = it.result?.get("profile") as DocumentReference?
                    val setOptions = SetOptions.merge()
                    return@continueWithTask profileReference!!.set(profileModel, setOptions)
                }
        }

        fun resolveReference(
            reference: DocumentReference,
            successCallback: ((DocumentSnapshot) -> Unit)? = null
        ): Task<DocumentSnapshot> {
            return reference.get()
                .addOnSuccessListener { successCallback?.invoke(it) }
                .addOnFailureListener { reportError(it) }
        }
    }
}