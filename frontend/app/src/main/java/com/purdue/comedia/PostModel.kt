package com.purdue.comedia

import com.google.firebase.firestore.DocumentReference

class PostModel {
    var content: String = ""
    var title: String = ""
    var genre: String = ""
    var type: String = ""
    var isAnon: Boolean = false
    var upvoteCount: Number = 0
    var downvoteCount: Number = 0
    var poster: DocumentReference? = null
    var comments = ArrayList<DocumentReference>()
    var upvoteList = ArrayList<DocumentReference>()
    var downvoteList = ArrayList<DocumentReference>()
    var saveList = ArrayList<DocumentReference>()
}

