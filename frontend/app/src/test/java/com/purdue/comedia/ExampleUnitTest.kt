package com.purdue.comedia

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    lateinit var firestore: FirebaseFirestore

    @Before
    fun setupEmulators() {
        firestore = FirebaseFirestore.getInstance()
        firestore.useEmulator("localhost", 8080)

        addTestUser(firestore)
    }

    private fun addTestUser(firestoreInstance: FirebaseFirestore) {
        val user = firestoreInstance.collection("users").document("test")
        val profile = firestoreInstance.collection("profiles").document("test")

        val userModel = UserModel()
        userModel.username = "test"
        userModel.email = "test@test.com"
        userModel.profile = profile

        val profileModel = ProfileModel()
        profileModel.biography = "testing"
        profileModel.profileImage = "test.com/test.jpg"
        profileModel.user = user

        user.set(userModel)
        profile.set(profileModel)
    }

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}