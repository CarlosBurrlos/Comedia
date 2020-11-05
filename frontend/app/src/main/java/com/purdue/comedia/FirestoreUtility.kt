package com.purdue.comedia

import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.util.*
import kotlin.collections.ArrayList

class FirestoreUtility {
    companion object {
        private val firestore = FirebaseFirestore.getInstance()

        private val auth = FirebaseAuth.getInstance()
        private const val feedLimit = 100
        var currentUser: UserModelClient = UserModelClient().also { addListenerForCurrentUser() }
            private set
        var currentProfile: ProfileModel = ProfileModel()
            private set
        private var userListener: ListenerRegistration? = null
        private var profileListener: ListenerRegistration? = null
        private val currentUserSubscribers = mutableListOf<(UserModelClient) -> Unit>()
        private var subscribersEnabled = true

        fun addListenerForCurrentUser() {
            if (auth.uid == null) return
            userListener = userRefByUID(auth.uid!!)
                .addSnapshotListener { value, _ ->
                    if (value == null || value.data == null) return@addSnapshotListener
                    currentUser.reference = value.reference
                    currentUser.model = convertToUser(value)
                    if (profileListener == null) addListenerForCurrentUserProfile(currentUser.model)
                    notifySubscribers()
                }
        }

        private fun addListenerForCurrentUserProfile(user: UserModel) {
            profileListener = user.profile?.addSnapshotListener { value, _ ->
                if (value == null || value.data == null) return@addSnapshotListener
                currentProfile = convertToProfile(value)
                notifySubscribers()
            }
        }

        private fun notifySubscribers() {
            if (subscribersEnabled) currentUserSubscribers.forEach { it(currentUser) }
        }

        fun enableSubscribers() {
            subscribersEnabled = true
        }

        fun disableSubscribers() {
            subscribersEnabled = false
        }

        fun clearCurrentUserListener() {
            userListener?.remove().also { userListener = null }
            profileListener?.remove().also { profileListener = null }
            currentUser = UserModelClient()
            currentProfile = ProfileModel()
            notifySubscribers()
        }

        fun subscribeToUserChange(callback: (UserModelClient) -> Unit) {
            currentUserSubscribers.add(callback)
        }

        fun unsubscribeToUserChange(callback: (UserModelClient) -> Unit) {
            currentUserSubscribers.remove(callback)
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
            model.saveList = snapshot.get("saveList")!! as ArrayList<DocumentReference>
            model.isAnon = snapshot.get("anon")!! as Boolean
            model.poster = snapshot.get("poster")!! as DocumentReference
            model.title = snapshot.get("title")!! as String
            model.type = snapshot.get("type")!! as String
            model.genre = snapshot.get("genre")!! as String
            return model
        }

        @Suppress("UNCHECKED_CAST")
        private fun convertToPostClient(snapshot: DocumentSnapshot): PostModelClient {
            val model = PostModel()
            model.comments = snapshot.get("comments")!! as ArrayList<DocumentReference>
            model.content = snapshot.get("content")!! as String
            model.downvoteCount = snapshot.get("downvoteCount")!! as Long
            model.downvoteList = snapshot.get("downvoteList")!! as ArrayList<DocumentReference>
            model.upvoteCount = snapshot.get("upvoteCount")!! as Long
            model.upvoteList = snapshot.get("upvoteList")!! as ArrayList<DocumentReference>
            model.saveList = snapshot.get("saveList")!! as ArrayList<DocumentReference>
            model.isAnon = snapshot.get("anon")!! as Boolean
            model.poster = snapshot.get("poster")!! as DocumentReference
            model.title = snapshot.get("title")!! as String
            model.type = snapshot.get("type")!! as String
            model.genre = snapshot.get("genre")!! as String
            val client = PostModelClient()
            client.postID = snapshot.id
            client.reference = snapshot.reference
            client.created = snapshot.get("created")!! as Timestamp
            client.model = model
            return client
        }

        private fun convertToComment(snapshot: DocumentSnapshot): CommentModel {
            val model = CommentModel()
            model.content = snapshot.get("content")!! as String
            model.parent = snapshot.get("parent")!! as DocumentReference
            model.poster = snapshot.get("poster")!! as DocumentReference
            return model
        }

        fun resolveUserReferences(references: Collection<DocumentReference>): Task<List<UserModel>> {
            val resolutionTasks = references.map { resolveUserReference(it) }
            return Tasks.whenAll(resolutionTasks)
                .continueWith { resolutionTasks.map { it.result!! } }
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

        fun resolveCommentReferences(references: Collection<DocumentReference>): Task<List<CommentModel>> {
            val resolutionTasks = references.map { resolveCommentReference(it) }
            return Tasks.whenAll(resolutionTasks)
                .continueWith { resolutionTasks.map { it.result!! } }
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

        fun postRefById(postId: String): DocumentReference {
            return firestore.collection("posts")
                .document(postId)
        }

        fun queryForPostById(postId: String): Task<PostModel> {
            return postRefById(postId)
                .get()
                .continueWith(convertResult(::convertToPost))
        }

        fun queryForEmailByName(username: String): Task<String> {
            return queryForUserLookup(username)
                .continueWith { it.result!!.email }
        }

        fun updateUserProfile(
            uid: String,
            profileModel: PartialProfileModel
        ): Task<Void> {
            return queryForUserByUID(uid)
                .continueWithTask {
                    val profileReference = it.result!!.profile
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
                val timestamp = FieldValue.serverTimestamp()
                // Callback not needed
                newPost.update("created", timestamp)

                // Update the user object's createdPosts
                firestore.collection("users")
                    .document(uid)
                    .update(
                        "createdPosts",
                        FieldValue.arrayUnion(newPost)
                    )
            }
        }

        // Creates a comment on a post
        fun createComment(
            newComment: String,
            postId: String
        ): Task<Void> {
            val commentModel = CommentModel()
            val uid: String = FirebaseAuth.getInstance().uid!!
            commentModel.poster = firestore.collection("users").document(uid)
            commentModel.parent = firestore.collection("posts").document(postId)
            commentModel.content = newComment
            return firestore.collection("comments").add(commentModel).continueWithTask {
                val postTask = firestore.collection("posts").document(postId)
                    .update("comments", FieldValue.arrayUnion(it.result!!))
                val userTask = firestore.collection("users").document(uid)
                    .update("comments", FieldValue.arrayUnion(it.result!!))
                return@continueWithTask Tasks.whenAll(postTask, userTask)
            }
        }

        // Converts a returned query snapshot to a list of PostModelClients
        fun convertQueryToPosts(
            qs: QuerySnapshot
        ): List<PostModelClient> {
            val list = mutableListOf<PostModelClient>()
            qs.iterator().forEachRemaining {
                list.add(convertToPostClient(it))
            }
            return list
        }

        // Queries a group of posts posted by the given user id (sorted by time)
        fun queryProfileFeed(uid: String): Task<QuerySnapshot> {
            return firestore.collection("posts")
                .whereEqualTo(
                    "poster", firestore.collection("users").document(uid)
                )
                .whereEqualTo("anon", false)
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
            return queryForUserByUID(uid)
                .continueWithTask { user ->
                    val taskList: MutableList<Task<PostModelClient>> = mutableListOf()
                    user.result!!.savedPosts.forEach {
                        taskList.add(resolvePostClientReference(it))
                    }
                    val finalList: List<Task<PostModelClient>> =
                        Collections.unmodifiableList(taskList)
                    return@continueWithTask Tasks.whenAllComplete(finalList)
                }
                .continueWith { finished ->
                    val postList: MutableList<PostModelClient> = mutableListOf()
                    finished.result!!.forEach {
                        postList.add(it.result!! as PostModelClient)
                    }
                    return@continueWith postList
                }
        }

        // Queries posts by a given user's username
        fun queryUserFeed(username: String): Task<QuerySnapshot> {
            return queryForUserLookup(username)
                .continueWithTask { user ->
                    queryProfileFeed(user.result!!.uid).continueWithTask { it }
                }
        }

        // Queries the posts posted by the genres and users that a user is following
        // If they follow no users their own posts are shown
        fun queryMainFeed(uid: String? = FirebaseAuth.getInstance().uid): Task<List<PostModelClient>> {
            if (uid != null) {
                return queryForUserByUID(uid)
                    .continueWithTask { user ->
                        val taskList: MutableList<Task<List<PostModelClient>>> = mutableListOf()
                        val followedUsers = user.result!!.usersFollowing
                        val followedGenres = user.result!!.genresFollowing
                        if (followedUsers.isNotEmpty()) {
                            taskList.add(queryMultiUserFeed(followedUsers))
                        }
                        if (followedGenres.isNotEmpty()) {
                            taskList.add(queryMultiGenreFeed(followedGenres))
                        }
                        return@continueWithTask Tasks.whenAllComplete(taskList)
                    }
                    .continueWith { finished ->
                        val postListList: MutableList<List<PostModelClient>> = mutableListOf()
                        finished.result!!.forEach {
                            @Suppress("UNCHECKED_CAST")
                            postListList.add(it.result!! as List<PostModelClient>)
                        }
                        return@continueWith mergePostList(postListList)
                    }
            } else {
                // Return r/all
                return firestore.collection("posts")
                    .orderBy("created", Query.Direction.DESCENDING)
                    .limit(feedLimit.toLong())
                    .get()
                    .continueWith {
                        return@continueWith convertQueryToPosts(it.result!!)
                    }
            }
        }

        // Utility method for flattening and merging a multi-layer list
        fun mergePostList(posts: List<List<PostModelClient>>): List<PostModelClient> {
            return posts.flatten().distinctBy { it.postID }.sortedByDescending { it.created }
        }

        // Queries multiple users' non-anon posts, making multiple tasks for more than 10 users
        fun queryMultiUserFeed(
            users: List<DocumentReference>
        ): Task<List<PostModelClient>> {
            if (users.size > 10) {
                val taskList: MutableList<Task<List<PostModelClient>>> = mutableListOf()
                for (x in users.indices step 10) {
                    val max = (x + 9).coerceAtMost(users.size)  // Kotlin's way of doing Math.min
                    taskList.add(queryMultiUserFeedSimple(users.subList(x, max)))
                }
                return Tasks.whenAllComplete(taskList)
                    .continueWith { finished ->
                        val postListList: MutableList<List<PostModelClient>> = mutableListOf()
                        finished.result!!.forEach {
                            @Suppress("UNCHECKED_CAST")
                            postListList.add(it.result!! as List<PostModelClient>)
                        }
                        return@continueWith postListList.flatten()
                    }
            } else {
                return queryMultiUserFeedSimple(users)
            }
        }

        // Queries non-anon posts posted by a given set of users (up to 10)
        fun queryMultiUserFeedSimple(
            users: List<DocumentReference>
        ): Task<List<PostModelClient>> {
            return firestore.collection("posts")
                .whereIn("poster", users)
                .whereEqualTo("anon", false)
                .orderBy("created")
                .limit(feedLimit.toLong())
                .get()
                .continueWith {
                    return@continueWith convertQueryToPosts(it.result!!)
                }
        }

        // Queries multiple genres, making multiple tasks for more than 10 genres
        fun queryMultiGenreFeed(
            genres: List<String>
        ): Task<List<PostModelClient>> {
            if (genres.size > 10) {
                val taskList: MutableList<Task<List<PostModelClient>>> = mutableListOf()
                for (x in genres.indices step 10) {
                    val max = (x + 9).coerceAtMost(genres.size)   // Kotlin's way of doing Math.min
                    taskList.add(queryMultiGenreFeedSimple(genres.subList(x, max)))
                }
                return Tasks.whenAllComplete(taskList)
                    .continueWith { finished ->
                        val postListList: MutableList<List<PostModelClient>> = mutableListOf()
                        finished.result!!.forEach {
                            @Suppress("UNCHECKED_CAST")
                            postListList.add(it.result!! as List<PostModelClient>)
                        }
                        return@continueWith postListList.flatten()
                    }
            } else {
                return queryMultiGenreFeedSimple(genres)
            }
        }

        // Queries posts posted with a given set of genres (up to 10)
        fun queryMultiGenreFeedSimple(
            genres: List<String>
        ): Task<List<PostModelClient>> {
            return firestore.collection("posts")
                .whereIn("genre", genres)
                .orderBy("created", Query.Direction.DESCENDING)
                .limit(feedLimit.toLong())
                .get()
                .continueWith {
                    return@continueWith convertQueryToPosts(it.result!!)
                }
        }

        fun followGenre(
            genre: String
        ): Task<Void> {
            val uid: String = FirebaseAuth.getInstance().uid!!
            return firestore.collection("users")
                .document(uid)
                .update(
                    "genresFollowing",
                    FieldValue.arrayUnion(genre)
                )
        }

        fun unfollowGenre(
            genre: String
        ): Task<Void> {
            val uid: String = FirebaseAuth.getInstance().uid!!
            return firestore.collection("users")
                .document(uid)
                .update(
                    "genresFollowing",
                    FieldValue.arrayRemove(genre)
                )
        }

        fun followUser(username: String): Task<Void> {
            return queryForUserRefByName(username)
                .continueWithTask {
                    val addAsFollower =
                        it.result!!.update(
                            "followers",
                            FieldValue.arrayUnion(currentUser.reference)
                        )
                    val addToFollowing =
                        currentUser.reference.update(
                            "usersFollowing",
                            FieldValue.arrayUnion(it.result!!)
                        )
                    return@continueWithTask Tasks.whenAll(addAsFollower, addToFollowing)
                }
        }

        fun unfollowUser(username: String): Task<Void> {
            return queryForUserRefByName(username)
                .continueWithTask {
                    val addAsFollower =
                        it.result!!.update(
                            "followers",
                            FieldValue.arrayRemove(currentUser.reference)
                        )
                    val addToFollowing =
                        currentUser.reference.update(
                            "usersFollowing",
                            FieldValue.arrayRemove(it.result!!)
                        )
                    return@continueWithTask Tasks.whenAll(addAsFollower, addToFollowing)
                }
        }

        fun savePost(postID: String): Task<Void> {
            val post = postRefById(postID)
            val saveAsPost = post.update(
                "saveList",
                FieldValue.arrayUnion(currentUser.reference)
            )
            val saveToSavedPosts = currentUser.reference.update(
                "savedPosts",
                FieldValue.arrayUnion(post)
            )
            return Tasks.whenAll(saveAsPost, saveToSavedPosts)
        }

        fun unsavePost(postID: String): Task<Void> {
            val post = postRefById(postID)
            return unsavePost(post)
        }

        fun unsavePost(post: DocumentReference): Task<Void> {
            val unsaveAsPost = post.update(
                "saveList",
                FieldValue.arrayRemove(currentUser.reference)
            )
            val unsaveAPost = currentUser.reference.update(
                "savedPosts",
                FieldValue.arrayRemove(post)
            )
            return Tasks.whenAll(unsaveAsPost, unsaveAPost)
        }


        fun upvote(postID: String): Task<Void> {
            val post = postRefById(postID)
            val addAsUpvote =
                post.update(
                    "upvoteCount",
                    FieldValue.increment(1),
                    "upvoteList",
                    FieldValue.arrayUnion(currentUser.reference)
                )
            val addToUpvote = currentUser.reference.update(
                "upvotedPosts",
                FieldValue.arrayUnion(post)
            )
            return Tasks.whenAll(addAsUpvote, addToUpvote)
        }

        fun unupvote(postID: String): Task<Void> {
            val post = postRefById(postID)
            return unupvote(post)
        }

        fun unupvote(post: DocumentReference): Task<Void> {
            val addAsUpvote =
                post.update(
                    "upvoteCount",
                    FieldValue.increment(-1),
                    "upvoteList",
                    FieldValue.arrayRemove(currentUser.reference)
                )
            val addToUpvote = currentUser.reference.update(
                "upvotedPosts",
                FieldValue.arrayRemove(post)
            )
            return Tasks.whenAll(addAsUpvote, addToUpvote)
        }

        fun downvote(postID: String): Task<Void> {
            val post = postRefById(postID)
            val addAsDownvote = post.update(
                "downvoteCount",
                FieldValue.increment(1),
                "downvoteList",
                FieldValue.arrayUnion(currentUser.reference)
            )
            val addToUpvote = currentUser.reference.update(
                "downvotedPosts",
                FieldValue.arrayUnion(post)
            )
            return Tasks.whenAll(addAsDownvote, addToUpvote)
        }

        fun undownvote(postID: String): Task<Void> {
            val post = postRefById(postID)
            return undownvote(post)
        }

        fun undownvote(post: DocumentReference): Task<Void> {
            val addAsDownvote = post.update(
                "downvoteCount",
                FieldValue.increment(-1),
                "downvoteList",
                FieldValue.arrayRemove(currentUser.reference)
            )
            val addToUpvote = currentUser.reference.update(
                "downvotedPosts",
                FieldValue.arrayRemove(post)
            )
            return Tasks.whenAll(addAsDownvote, addToUpvote)
        }
    }
}