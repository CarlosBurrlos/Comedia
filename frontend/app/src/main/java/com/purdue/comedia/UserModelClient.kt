package com.purdue.comedia

import com.google.firebase.firestore.DocumentReference

class UserModelClient {
    lateinit var reference: DocumentReference
    var model: UserModel = UserModel()
}
