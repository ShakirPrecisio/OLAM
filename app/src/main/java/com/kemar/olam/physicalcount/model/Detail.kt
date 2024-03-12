package com.kemar.olam.physicalcount.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class Detail : RealmObject(){
    var barcodeNumber: String? = null
    @PrimaryKey
    var uniqueId: Int? = null

    var id: Int? = null
}