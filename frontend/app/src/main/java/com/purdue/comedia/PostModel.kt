package com.purdue.comedia

import com.google.firebase.firestore.DocumentReference

class PostModel {
    lateinit var content: String
    lateinit var title: String
    lateinit var genre: String
    lateinit var type: String
    var isAnon: Boolean = false
    var upvoteCount: Number = 0
    var downvoteCount: Number = 0
    var poster: DocumentReference? = null
    var comments = ArrayList<DocumentReference>()
    var upvoteList = ArrayList<DocumentReference>()
    var downvoteList = ArrayList<DocumentReference>()
}