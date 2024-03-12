package com.kemar.olam.physicalcount.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class PhysicalCountModel :RealmObject() {
    var endDate: String? = null
    var details: RealmList<Detail>? = null
    var id: String? = null

    @PrimaryKey
    var uniqueId: String? = null

    var startDate: String? = null
    var logParkName: String? = null
    var status: String? = null
}