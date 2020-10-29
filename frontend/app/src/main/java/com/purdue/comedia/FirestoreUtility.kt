package com.purdue.comedia

import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.util.*
import kotlin.collections.ArrayList

class FirestoreUtility {
    companion object {
        private val firestore = FirebaseFirestore.getInstance()
        private val auth = FirebaseAuth.getInstance()
        private const val feedLimit = 100

        fun resolveReference(
            reference: DocumentReference,
            successCallback: ((DocumentSnapshot) -> Unit)? = null,
            failureCallback: ((Exception) -> Unit) = ::reportError
        ): Task<DocumentSnapshot> {
            return reference.get()
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

        private fun reportError(error: java.lang.Exception) {
            println(error.message)
        }

        private fun convertToUserLookup(snapshot: DocumentSnapshot): UserLookupModel {
            val model = UserLookupModel()
            model.email = snapshot.get("email")!! as String
            model.uid = snapshot.get("uid")!! as String
            return model
        }

        @Suppress("UNCHECKED_CAST")
        private fun convertToUser(snapshot: DocumentSnapshot): UserModel {
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
            return model
        }

        private fun convertToProfile(snapshot: DocumentSnapshot): ProfileModel {
            val model = ProfileModel()
            model.user = snapshot.get("user")!! as DocumentReference
            model.profileImage = snapshot.get("profileImage")!! as String
            model.biography = snapshot.get("biography")!! as String
            return model
        }

        @Suppress("UNCHECKED_CAST")
        private fun convertToPost(snapshot: DocumentSnapshot): PostModel {
            val model = PostModel()
            model.comments = snapshot.get("comments")!! as ArrayList<DocumentReference>
            model.content = snapshot.get("content")!! as String
            model.downvoteCount = snapshot.get("downvoteCount")!! as Long
            model.downvoteList = snapshot.get("downvoteList")!! as ArrayList<DocumentReference>
            model.upvoteCount = snapshot.get("upvoteCount")!! as Long
            model.upvoteList = snapshot.get("upvoteList")!! as ArrayList<DocumentReference>
            model.isAnon = snapshot.get("anon")!! as Boolean
            model.poster = snapshot.get("poster")!! as DocumentReference
            model.title = snapshot.get("title")!! as String
            model.type = snapshot.get("type")!! as String
            model.genre = snapshot.get("genre")!! as String
            return model
        }

        @Suppress("UNCHECKED_CAST")
        private fun convertToPostClient(snapshot: DocumentSnapshot): PostModelClient {
            val model = PostModelClient()
            model.comments = snapshot.get("comments")!! as ArrayList<DocumentReference>
            model.content = snapshot.get("content")!! as String
            model.downvoteCount = snapshot.get("downvoteCount")!! as Long
            model.downvoteList = snapshot.get("downvoteList")!! as ArrayList<DocumentReference>
            model.upvoteCount = snapshot.get("upvoteCount")!! as Long
            model.upvoteList = snapshot.get("upvoteList")!! as ArrayList<DocumentReference>
            model.isAnon = snapshot.get("anon")!! as Boolean
            model.poster = snapshot.get("poster")!! as DocumentReference
            model.title = snapshot.get("title")!! as String
            model.type = snapshot.get("type")!! as String
            model.genre = snapshot.get("genre")!! as String
            model.postID = snapshot.id
            return model
        }

        private fun convertToComment(snapshot: DocumentSnapshot): CommentModel {
            val model = CommentModel()
            model.content = snapshot.get("content")!! as String
            model.parent = snapshot.get("parent")!! as DocumentReference
            model.poster = snapshot.get("poster")!! as DocumentReference
            return model
        }

        fun resolveUserReference(reference: DocumentReference): Task<UserModel> {
            return reference.get().continueWith(convertResult(::convertToUser))
        }

        fun resolveProfileReference(reference: DocumentReference): Task<ProfileModel> {
            return reference.get().continueWith(convertResult(::convertToProfile))
        }

        fun resolvePostReference(reference: DocumentReference): Task<PostModel> {
            return reference.get().continueWith(convertResult(::convertToPost))
        }

        fun resolvePostClientReference(reference: DocumentReference): Task<PostModelClient> {
            return reference.get().continueWith(convertResult(::convertToPostClient))
        }

        fun resolveCommentReference(reference: DocumentReference): Task<CommentModel> {
            return reference.get().continueWith(convertResult(::convertToComment))
        }

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
                .continueWith(convertResult(::convertToUser))
                .addOnSuccessListener { successCallback?.invoke(it) }
                .addOnFailureListener { failureCallback(it) }
        }

        fun queryForUserRefByName(username: String): Task<DocumentReference> {
            return queryForUserLookup(username)
                .continueWith {
                    userRefByUID(it.result!!.uid)
                }
        }

        fun queryForUserLookup(username: String): Task<UserLookupModel> {
            return firestore.collection("userLookup")
                .document(username)
                .get()
                .continueWith(convertResult(::convertToUserLookup))
        }

        fun queryForUserByName(username: String): Task<UserModel> {
            return queryForUserLookup(username)
                .continueWithTask {
                    queryForUserByUID(it.result!!.uid)
                }
        }

        fun queryForEmailByName(username: String): Task<String> {
            return queryForUserLookup(username)
                .continueWith { it.result!!.email }
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

        // Returns a series of PostModelClient objects representing a user's main feed
        fun compileMainFeed(
            uid: String
        ): List<PostModelClient> {

            return emptyList()
        }

        // Converts a returned query snapshot to a list of PostModelClients
        fun convertQueryToPosts(
            qs: QuerySnapshot
        ): List<PostModelClient> {
            var list : MutableList<PostModelClient> = mutableListOf()
            qs.iterator().forEachRemaining {
                //var post: PostModelClient = convertToPost(it) as PostModelClient
                val post = PostModelClient()
                post.comments = it.get("comments")!! as ArrayList<DocumentReference>
                post.content = it.get("content")!! as String
                post.downvoteCount = it.get("downvoteCount")!! as Long
                post.downvoteList = it.get("downvoteList")!! as ArrayList<DocumentReference>
                post.upvoteCount = it.get("upvoteCount")!! as Long
                post.upvoteList = it.get("upvoteList")!! as ArrayList<DocumentReference>
                post.isAnon = it.get("anon")!! as Boolean
                post.poster = it.get("poster")!! as DocumentReference
                post.title = it.get("title")!! as String
                post.type = it.get("type")!! as String
                post.genre = it.get("genre")!! as String
                post.postID = it.id
                list.add(post)
            }
            return list
        }

        // Queries a group of posts posted by the given user id (sorted by time)
        fun queryProfileFeed(uid: String): Task<QuerySnapshot> {
            return firestore.collection("posts")
                .whereEqualTo(
                    "poster", firestore.collection("users").document(uid)
                )
                .orderBy("created", Query.Direction.DESCENDING)
                .limit(feedLimit.toLong())
                .get()
        }

        // Queries a group of posts posted by the given genre
        fun queryGenreFeed(genre: String): Task<QuerySnapshot> {
            return firestore.collection("posts")
                .whereEqualTo(
                    "genre", genre.toLowerCase()
                )
                .orderBy("created", Query.Direction.DESCENDING)
                .limit(feedLimit.toLong())
                .get()
        }

        // Queries posts saved by the current user
        fun querySavedPostsFeed(uid: String = FirebaseAuth.getInstance().uid!!)
                : Task<List<PostModelClient>> {
            // Todo: @TOM
            return queryForUserByUID(uid)
                .continueWithTask { user ->
                    val taskList: MutableList<Task<PostModelClient>> = mutableListOf()
                    user.result!!.savedPosts.forEach {
                        taskList.add(resolvePostClientReference(it))
                    }
                    val finalList: List<Task<PostModelClient>> = Collections.unmodifiableList(taskList)
                    return@continueWithTask Tasks.whenAllComplete(finalList)
                }
                .continueWith{
                    val postList: MutableList<PostModelClient> = mutableListOf()
                    it.result!!.forEach {
                        postList.add(it.result!! as PostModelClient)
                    }
                    return@continueWith postList
                }
        }

        fun foo(bar: Number): Number {
            return bar
        }

        // Queries posts saved by the current user
        fun queryMainFeed(uid: String = FirebaseAuth.getInstance().uid!!): Task<QuerySnapshot> {
            // Todo: @TOM (Currently returns all posts)
            return firestore.collection("posts")
                //.whereEqualTo()
                .orderBy("created", Query.Direction.DESCENDING)
                .limit(feedLimit.toLong())
                .get()
        }

        // Queries posts by a given user's username
        fun queryUserFeed(username: String): Task<QuerySnapshot> {
            return queryForUserLookup(username).continueWithTask {
                return@continueWithTask queryProfileFeed(it.result!!.uid).continueWithTask {
                    return@continueWithTask it
                }
            }
        }

        // Queries posts posted by a given set of users (up to 10)
        fun queryMultiUserFeed(
            users: List<DocumentReference>,
            successCallback: ((QuerySnapshot) -> Unit)? = null,
            failureCallback: ((Exception) -> Unit)? = null
        ): Task<QuerySnapshot> {
            return firestore.collection("posts")
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
        ): Task<QuerySnapshot> {
            return firestore.collection("posts")
                .whereIn("genre", genres)
                .orderBy("created")
                .limit(feedLimit.toLong())
                .get()
                .addOnSuccessListener { successCallback?.invoke(it) }
                .addOnFailureListener { failureCallback?.invoke(it) }
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