package com.purdue.comedia

import com.google.firebase.firestore.DocumentReference

class UserModel {
    var username: String = ""
    var email: String = ""
    var comments = ArrayList<DocumentReference>()
    var followers = ArrayList<DocumentReference>()
    var usersFollowing = ArrayList<DocumentReference>()
    var genresFollowing = ArrayList<String>()
    var createdPosts = ArrayList<DocumentReference>()
    var savedPosts = ArrayList<DocumentReference>()
    var upvotedPosts = ArrayList<DocumentReference>()
    var downvotedPosts = ArrayList<DocumentReference>()
    var profile: DocumentReference? = null
}
