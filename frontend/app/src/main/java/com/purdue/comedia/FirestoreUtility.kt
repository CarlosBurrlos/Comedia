package com.purdue.comedia

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*

class FirestoreUtility {
    companion object {
        var firestore = FirebaseFirestore.getInstance()
        const val feedLimit = 100

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

        // Queries a group of posts posted by the given user id (sorted by time)
        fun queryProfileFeed(
            uid: String,
            successCallback: ((QuerySnapshot) -> Unit)? = null,
            failureCallback: ((Exception) -> Unit)? = null
        ): Task<DocumentSnapshot> {
            return queryForUser(uid, {
                user->
                firestore.collection("posts")
                    .whereEqualTo("poster",user)
                    .orderBy("created")
                    .limit(feedLimit.toLong())
                    .get()
                    .addOnSuccessListener { successCallback?.invoke(it) }
                    .addOnFailureListener { failureCallback?.invoke(it) }
            })
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