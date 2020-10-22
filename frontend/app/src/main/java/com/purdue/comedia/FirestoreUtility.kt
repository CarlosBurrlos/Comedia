package com.purdue.comedia

import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class FirestoreUtility {
    companion object {
        private val firestore = FirebaseFirestore.getInstance()
        private val auth = FirebaseAuth.getInstance()
        private const val feedLimit = 100

        fun userRefByUID(uid: String): DocumentReference {
            return firestore.collection("users")
                .document(uid)
        }

        fun queryForUserByUID(
            uid: String,
            successCallback: ((UserModel) -> Unit)? = null,
            failureCallback: ((Exception) -> Unit) = ::reportError
        ): Task<UserModel> {
            return userRefByUID(uid)
                .get()
                .continueWith(convertResult(::convertDocumentSnapshotToUser))
                .addOnSuccessListener { successCallback?.invoke(it) }
                .addOnFailureListener { failureCallback(it) }
        }

        fun queryForUserRefByName(username: String): Task<DocumentReference> {
            return firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .continueWith(convertResult(::convertQuerySnapshotToUserRef))
        }

        fun queryForUserByName(
            username: String,
            successCallback: ((UserModel) -> Unit)? = null,
            failureCallback: ((Exception) -> Unit) = ::reportError
        ): Task<UserModel> {
            return firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .continueWith(convertResult(::convertQuerySnapshotToUser))
                .addOnSuccessListener { successCallback?.invoke(it) }
                .addOnFailureListener { failureCallback(it) }
        }

        private fun <TResult, TContinuationResult> convertResult(
            conversionFunction: (TResult) -> TContinuationResult
        ): Continuation<TResult, TContinuationResult> {
            return Continuation {
                if (it.exception != null) throw it.exception!!
                return@Continuation conversionFunction(it.result!!)
            }
        }

        @Suppress("UNCHECKED_CAST")
        private fun convertDocumentSnapshotToUser(snapshot: DocumentSnapshot): UserModel {
            val model = UserModel()
            model.username = snapshot.get("username")!! as String
            model.email = snapshot.get("email")!! as String
            model.comments = snapshot.get("comments")!! as ArrayList<DocumentReference>
            model.followers = snapshot.get("followers")!! as ArrayList<DocumentReference>
            model.usersFollowing = snapshot.get("usersFollowing")!! as ArrayList<DocumentReference>
            model.genresFollowing = snapshot.get("genresFollowing")!! as ArrayList<String>
            model.createdPosts = snapshot.get("createdPosts")!! as ArrayList<DocumentReference>
            model.savedPosts = snapshot.get("savedPosts")!! as ArrayList<DocumentReference>
            model.upvotedPosts = snapshot.get("upvotedPosts")!! as ArrayList<DocumentReference>
            model.downvotedPosts = snapshot.get("downvotedPosts")!! as ArrayList<DocumentReference>
            model.profile = snapshot.get("profile")!! as DocumentReference
            println("USER BUILT")
            return model
        }

        private fun convertQuerySnapshotToUsers(snapshot: QuerySnapshot): List<UserModel> {
            val users = ArrayList<UserModel>()
            for (document in snapshot.documents) {
                val model = convertDocumentSnapshotToUser(document)
                users.add(model)
            }
            return users
        }

        private fun convertQuerySnapshotToUser(snapshot: QuerySnapshot): UserModel {
            return convertDocumentSnapshotToUser(snapshot.documents[0])
        }

        private fun convertQuerySnapshotToUserRef(snapshot: QuerySnapshot): DocumentReference {
            return snapshot.documents[0].reference
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
            return queryForUserByUID(uid)
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

        fun followUser(
            username: String
        ): Task<Void> {
            val currentUser = userRefByUID(auth.uid!!)
            return queryForUserRefByName(username)
                .continueWithTask {
                    val addAsFollower =
                        it.result!!.update("followers", FieldValue.arrayUnion(currentUser))
                    val addToFollowing =
                        currentUser.update("followers", FieldValue.arrayUnion(it.result!!))
                    return@continueWithTask Tasks.whenAll(addAsFollower, addToFollowing)
                }
        }

        fun unfollowUser(
            username: String
        ): Task<Void> {
            val currentUser = userRefByUID(auth.uid!!)
            return queryForUserRefByName(username)
                .continueWithTask {
                    val addAsFollower =
                        it.result!!.update("followers", FieldValue.arrayRemove(currentUser))
                    val addToFollowing =
                        currentUser.update("followers", FieldValue.arrayRemove(it.result!!))
                    return@continueWithTask Tasks.whenAll(addAsFollower, addToFollowing)
                }
        }
    }
}