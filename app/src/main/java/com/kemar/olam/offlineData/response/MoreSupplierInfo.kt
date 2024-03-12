package com.kemar.olam.offlineData.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class MoreSupplierInfo :RealmObject() {

    @PrimaryKey
    var id:Int?=null


    @SerializedName("supplierName")
    @Expose
    var supplierName: String? = null

    @SerializedName("supplierId")
    @Expose
    var supplierId: Int? = null

    @SerializedName("rig_address")
    @Expose
    var rigAddress: String? = null

    @SerializedName("tracer_validity_to")
    @Expose
    var tracerValidityTo: String? = null

    @SerializedName("logger")
    @Expose
    var logger: String? = null

    @SerializedName("tracer_ref")
    @Expose
    var tracerRef: String? = null

    @SerializedName("final_destination")
    @Expose
    var finalDestination: String? = null

    @SerializedName("supplierCode")
    @Expose
    var supplierCode: String? = null

    @SerializedName("type")
    @Expose
    var type: String? = null

    @SerializedName("province")
    @Expose
    var province: String? = null

    @SerializedName("first_destination")
    @Expose
    var firstDestination: String? = null

    @SerializedName("supplierShortName")
    @Expose
    var supplierShortName: String? = null

    @SerializedName("tracer_validity_from")
    @Expose
    var tracerValidityFrom: String? = null
}