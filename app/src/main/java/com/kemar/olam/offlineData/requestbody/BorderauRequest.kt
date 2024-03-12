package com.kemar.olam.offlineData.requestbody

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class BorderauRequest :RealmObject(){

    @PrimaryKey
    var uniqueId: String? = null


    var bordereauNo: String? = null

    var bordereauDate: String? = null

    var forestId: Int? = null



    var forestName: String? = null


    var originId: Int? = null
    var originName: String? = null


    var fscId: Int? = null
    var fscName: String? = null


    var leudechargementId: Int? = null

    var chargementName: String? = null


    var transpoterId: Int? = null


    var transpoterName: String? = null

    var vehicalId: Int? = null


    var destinationName: String? = null

    var distance: String? = null

    var headerID:Int?=null

    var recordDocNo: String? = null

    var userID: Int? = 0
    var supplierLocation: Int? = 0
    var modeOfTransport: Int? = 0
    var mode: String? = null

    var headerStatus: String? = null

    var destination: String? = null

    var speciesId: Int? = 0

    var wagonNo: String? = null
    var currentDate: String? = null

    var isUpload: Boolean?=false

    var isFailed: Boolean?=false

    var failedReason: String? = null

    var logCount: Int? = 0

//    var isLogUpload: Boolean?=false
}