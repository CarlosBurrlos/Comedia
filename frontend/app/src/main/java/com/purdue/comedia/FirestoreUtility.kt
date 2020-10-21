package com.purdue.comedia

import android.webkit.URLUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_create_post_page.*

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

        fun createPost(
            uid: String?,
            new_post: PostModel
        ) {
            new_post.poster = firestore.collection("users").document(uid!!)

            // Add the post model to the database
            firestore.collection("posts").add(new_post).addOnSuccessListener {
                // On a newly created post, get the post reference (for references)
                    newPost ->
                val timestamp = com.google.firebase.firestore.FieldValue.serverTimestamp()
                // Callback not needed
                newPost.update("created", timestamp);

                // Update the user object's createdPosts
                firestore.collection("users")
                    .document(uid)
                    .update(
                        "createdPosts",
                        com.google.firebase.firestore.FieldValue.arrayUnion(newPost)
                    )
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

        // Queries posts posted by a given set of users (up to 10)
        fun queryMultiUserFeed(
            users: List<DocumentReference>,
            successCallback: ((QuerySnapshot) -> Unit)? = null,
            failureCallback: ((Exception) -> Unit)? = null
        ) {
            if (users.size > 10) {
                return
            }
            firestore.collection("posts")
                .whereIn("poster",users)
                .orderBy("created")
                .limit(feedLimit.toLong())
                .get()
                .addOnSuccessListener { successCallback?.invoke(it) }
                .addOnFailureListener { failureCallback?.invoke(it) }
        }

        // Queries posts posted with a given set of genres (up to 10)
        fun queryMultiGenreFeed(
            genres: List<String>,
            successCallback: ((QuerySnapshot) -> Unit)? = null,
            failureCallback: ((Exception) -> Unit)? = null
        ) {
            if (genres.size > 10) {
                return
            }
            firestore.collection("posts")
                .whereIn("genre",genres)
                .orderBy("created")
                .limit(feedLimit.toLong())
                .get()
                .addOnSuccessListener { successCallback?.invoke(it) }
                .addOnFailureListener { failureCallback?.invoke(it) }
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