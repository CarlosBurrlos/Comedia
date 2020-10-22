package com.purdue.comedia

import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*

class FirestoreUtility {
    companion object {
        var firestore = FirebaseFirestore.getInstance()
        private const val feedLimit = 100

        fun queryForUser(
            uid: String,
            successCallback: ((UserModel) -> Unit)? = null,
            failureCallback: ((Exception) -> Unit) = ::reportError
        ): Task<UserModel> {
            return firestore.collection("users")
                .document(uid)
                .get()
                .continueWith(convertToModel(::convertToUser))
                .addOnSuccessListener { successCallback?.invoke(it) }
                .addOnFailureListener { failureCallback(it) }
        }

        private fun <TContinuationResult> convertToModel(
            conversionFunction: (DocumentSnapshot) -> TContinuationResult
        ): Continuation<DocumentSnapshot, TContinuationResult> {
            return Continuation {
                if (it.exception != null) throw it.exception!!
                return@Continuation conversionFunction(it.result!!)
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun convertToUser(it: DocumentSnapshot): UserModel {
            val model = UserModel()
            println("BUILDING USER")
            model.username = it.get("username")!! as String
            model.email = it.get("email")!! as String
            model.comments = it.get("comments")!! as ArrayList<DocumentReference>
            model.followers = it.get("followers")!! as ArrayList<DocumentReference>
            model.usersFollowing = it.get("usersFollowing")!! as ArrayList<DocumentReference>
            model.genresFollowing = it.get("genresFollowing")!! as ArrayList<String>
            model.createdPosts = it.get("createdPosts")!! as ArrayList<DocumentReference>
            model.savedPosts = it.get("savedPosts")!! as ArrayList<DocumentReference>
            model.upvotedPosts = it.get("upvotedPosts")!! as ArrayList<DocumentReference>
            model.downvotedPosts = it.get("downvotedPosts")!! as ArrayList<DocumentReference>
            model.profile = it.get("profile")!! as DocumentReference
            println("USER BUILT")
            return model
        }

        private fun reportError(error: java.lang.Exception) {
            println(error.message)
        }

        fun updateUserProfile(
            uid: String,
            profileModel: PartialProfileModel,
            successCallback: (() -> Unit)? = null,
            failureCallback: (Exception) -> Unit = ::reportError
        ): Task<Void> {
            return queryForUser(uid)
                .continueWithTask {
                    val profileReference = it.result!!.profile!!
                    val setOptions = SetOptions.merge()
                    return@continueWithTask profileReference.set(profileModel, setOptions)
                }
                .addOnSuccessListener { successCallback?.invoke() }
                .addOnFailureListener(failureCallback)
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
        ): Task<QuerySnapshot> {
            return firestore.collection("posts")
                .whereEqualTo(
                    "poster", firestore.collection("users").document(uid)
                )
                .orderBy("created")
                .limit(feedLimit.toLong())
                .get()
                .addOnSuccessListener { successCallback?.invoke(it) }
                .addOnFailureListener { failureCallback?.invoke(it) }
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
                .whereIn("poster", users)
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
                .whereIn("genre", genres)
                .orderBy("created")
                .limit(feedLimit.toLong())
                .get()
                .addOnSuccessListener { successCallback?.invoke(it) }
                .addOnFailureListener { failureCallback?.invoke(it) }
        }

        fun resolveReference(
            reference: DocumentReference,
            successCallback: ((DocumentSnapshot) -> Unit)? = null,
            failureCallback: ((Exception) -> Unit) = ::reportError
        ): Task<DocumentSnapshot> {
            return reference.get()
                .addOnSuccessListener { successCallback?.invoke(it) }
                .addOnFailureListener { failureCallback(it) }
        }
    }
}