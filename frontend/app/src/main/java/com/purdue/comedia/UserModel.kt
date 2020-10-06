package com.purdue.comedia

import com.google.firebase.firestore.DocumentReference

class UserModel {
    lateinit var username: String
    lateinit var email: String
    var comments = ArrayList<DocumentReference>()
    var createdPosts = ArrayList<DocumentReference>()
    var followers = ArrayList<DocumentReference>()
    val genresFollowing = ArrayList<String>()
    val savedPosts = ArrayList<DocumentReference>()
    val usersFollowing = ArrayList<DocumentReference>()
    var profile: DocumentReference? = null
}
