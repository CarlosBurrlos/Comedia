package com.purdue.comedia

import com.google.firebase.firestore.DocumentReference

class CommentModel {
    lateinit var content: String
    lateinit var parent: DocumentReference
    lateinit var poster: DocumentReference
}
