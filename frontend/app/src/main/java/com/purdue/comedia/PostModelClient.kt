package com.purdue.comedia

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

class PostModelClient {
    lateinit var postID: String
    lateinit var reference: DocumentReference
    lateinit var created: Timestamp
    var model = PostModel()
}