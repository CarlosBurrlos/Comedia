package com.purdue.comedia

import com.google.firebase.firestore.DocumentReference

class GenreModelClient {
    var reference: DocumentReference? = null
    var model: GenreModel = GenreModel()
}

class GenreModel {
    var posts = mutableListOf<DocumentReference>()
}
