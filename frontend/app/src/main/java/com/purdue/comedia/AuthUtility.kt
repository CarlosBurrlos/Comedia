package com.purdue.comedia

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AuthUtility {
    companion object {
        private val auth = FirebaseAuth.getInstance()
        private val firestore = FirebaseFirestore.getInstance()

        fun deleteAccount(): Task<Unit> {
            return FirestoreUtility.queryForUserByUID(auth.uid!!)
                .continueWithTask { removeInteractions(it.result!!) }
                .continueWithTask { deletePosts(it.result!!) }
                .continueWithTask { removeFromFollowLists(it.result!!) }
                .continueWithTask { deleteUser(it.result!!) }
                .continueWith { auth.signOut() }
        }

        private fun removeInteractions(user: UserModel): Task<UserModel> {
            return Tasks.whenAll(
                deleteComments(user.comments),
                revertVoteCounts(user)
            ).continueWith { user }
        }

        private fun deleteComments(comments: List<DocumentReference>): Task<Void> {
            val commentTasks = ArrayList<Task<Void>>()
            for (comment in comments) {
                commentTasks.add(FirestoreUtility.resolveCommentReference(comment)
                    .continueWithTask {
                        Tasks.whenAll(
                            it.result!!.parent.update(
                                "comments",
                                FieldValue.arrayRemove(comment)
                            ),
                            comment.delete()
                        )
                    })
            }
            return Tasks.whenAll(commentTasks)
        }

        private fun revertVoteCounts(user: UserModel): Task<Void> {
            val reversionTasks = ArrayList<Task<Void>>()
            val userRef = FirestoreUtility.userRefByUID(auth.uid!!)
            for (post in user.upvotedPosts) {
                reversionTasks.add(
                    Tasks.whenAll(
                        post.update("upvoteCount", FieldValue.increment(-1)),
                        post.update("upvoteList", FieldValue.arrayRemove(userRef))
                    )
                )
            }
            for (post in user.downvotedPosts) {
                reversionTasks.add(
                    Tasks.whenAll(
                        post.update("downvoteCount", FieldValue.increment(-1)),
                        post.update("downvoteList", FieldValue.arrayRemove(userRef))
                    )
                )
            }
            return Tasks.whenAll(reversionTasks)
        }

        private fun deletePosts(user: UserModel): Task<UserModel> {
            val deleteTasks = ArrayList<Task<Void>>()
            for (post in user.createdPosts) {
                deleteTasks.add(
                    FirestoreUtility.resolvePostReference(post)
                        .continueWithTask {
                            deletePost(post, it.result!!)
                        }
                )
            }
            return Tasks.whenAll(deleteTasks).continueWith { user }
        }

        private fun deletePost(post: DocumentReference, postModel: PostModel): Task<Void> {
            val postRemovalTasks = ArrayList<Task<Void>>()
            for (userRef in postModel.upvoteList) {
                postRemovalTasks.add(
                    userRef.update("upvotedPosts", FieldValue.arrayRemove(post))
                )
            }
            for (userRef in postModel.downvoteList) {
                postRemovalTasks.add(
                    userRef.update("downvotedPosts", FieldValue.arrayRemove(post))
                )
            }
            postRemovalTasks.add(deleteComments(postModel.comments))
            postRemovalTasks.add(post.delete())

            return Tasks.whenAll(postRemovalTasks)
        }

        private fun removeFromFollowLists(user: UserModel): Task<UserModel> {
            val removalTasks = ArrayList<Task<Void>>()
            val userRef = FirestoreUtility.userRefByUID(auth.uid!!)

            for (follower in user.followers) {
                removalTasks.add(
                    follower.update("usersFollowing", FieldValue.arrayRemove(userRef))
                )
            }
            for (followed in user.usersFollowing) {
                removalTasks.add(
                    followed.update("followers", FieldValue.arrayRemove(userRef))
                )
            }

            return Tasks.whenAll(removalTasks).continueWith { user }
        }

        private fun deleteUser(userModel: UserModel): Task<Void> {
            val deleteUser = firestore.collection("users")
                .document(auth.uid!!)
                .delete()
            val deleteUserLookup = firestore.collection("userLookup")
                .document(userModel.username)
                .delete()
            return Tasks.whenAll(deleteUser, deleteUserLookup)
        }
    }
}
