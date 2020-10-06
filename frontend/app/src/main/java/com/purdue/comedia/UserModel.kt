package com.purdue.comedia

import com.google.firebase.firestore.DocumentReference

class UserModel {
    lateinit var username: String
    lateinit var email: String
    var comments = arrayOf<DocumentReference>()
    var createdPosts = arrayOf<DocumentReference>()
    var followers = arrayOf<DocumentReference>()
    val genresFollowing = arrayOf<String>()
    val savedPosts = arrayOf<DocumentReference>()
    val usersFollowing = arrayOf<DocumentReference>()
    var profile: DocumentReference? = null
}
