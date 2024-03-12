package com.kemar.olam.dashboard.models.responce

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class DestinationListDatum : RealmObject() {
    @PrimaryKey
    var id:Int?=null
    @SerializedName("optionValue")
    @Expose
    var optionValue: Int? = 0
    @SerializedName("optionName")
    @Expose
    var optionName: String? = null
    @SerializedName("optionValueString")
    @Expose
    var optionValueString: String? = null
    @SerializedName("bordereauNo")
    @Expose
    var bordereauNo: String? = null

    @SerializedName("finalDestination")
    @Expose
    var finalDestination: String? = null
}