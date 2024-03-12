package com.lp.lpwms.ui.offline.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class LeauChargementDatum :RealmObject(){
    @PrimaryKey
    var id:Int?=null


    @SerializedName("uid")
    @Expose
    var uid: Int? = null

    @SerializedName("optionValue")
    @Expose
    var optionValue: Int? = null

    @SerializedName("optionName")
    @Expose
    var optionName: String? = null

    @SerializedName("optionValueString")
    @Expose
    var optionValueString: String? = null
}