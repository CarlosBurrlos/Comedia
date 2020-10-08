package com.purdue.comedia

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
//    companion object {
//        private lateinit var firestore: FirebaseFirestore
//        private lateinit var userModel: UserModel
//        private lateinit var profileModel: ProfileModel
//
//        @BeforeClass
//        fun setupEmulators() {
//            FirebaseApp.initializeApp(getApplicationContext())
//            firestore = FirebaseFirestore.getInstance()
//            firestore.useEmulator("localhost", 8080)
//
//            addTestUser(firestore)
//        }
//
//        private fun addTestUser(firestoreInstance: FirebaseFirestore) {
//            val user = firestoreInstance.collection("users").document("test")
//            val profile = firestoreInstance.collection("profiles").document("test")
//
//            userModel = UserModel()
//            userModel.username = "test"
//            userModel.email = "test@test.com"
//            userModel.profile = profile
//
//            profileModel = ProfileModel()
//            profileModel.biography = "testing"
//            profileModel.profileImage = "test.com/test.jpg"
//            profileModel.user = user
//
//            user.set(userModel)
//            profile.set(profileModel)
//        }
//
//        private fun createTestPost(firestoreInstance: FirebaseFirestore) {
//            val uid = "test"
//            val postModel = PostModel()
//            postModel.title = "Test Post"
//            postModel.content = "This is a test post."
//            postModel.genre = "test"
//            postModel.type = "txt"
//            postModel.isAnon = false
//            val cpp = CreatePostPage()
//            cpp.createPost(uid, postModel, firestoreInstance)
//        }
//
//
//    }
//
//    @Before
//    fun setupFirebaseUtility() {
//        FirestoreUtility.firestore = firestore
//    }
//
//    @Test
//    fun test_query_for_user_gives_correct_result_on_success() {
//        FirestoreUtility.queryForUser("test", {
//            assertEquals(it.data, userModel)
//        })
//    }
}