package com.purdue.comedia

import com.google.firebase.firestore.DocumentReference

open class PartialProfileModel {
    lateinit var profileImage: String
    lateinit var biography: String
}

class ProfileModel : PartialProfileModel() {
    lateinit var user: DocumentReference
}
