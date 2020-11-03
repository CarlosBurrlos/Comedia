package com.purdue.comedia

import com.google.firebase.firestore.DocumentReference

open class PartialProfileModel {
    var profileImage: String = ""
    var biography: String = ""
}

class ProfileModel : PartialProfileModel() {
    var user: DocumentReference? = null
}
