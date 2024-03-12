package com.kemar.olam.sales_and_inspection.inspection.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class SalesInspectDataByVehicleNoReq {
    @SerializedName("wagonNo")
    @Expose
    private var wagonNo: String? = null
    @SerializedName("bordereauNo")
    @Expose
    private var bordereauNo: String? = null
    @SerializedName("eBordereauNo")
    @Expose
    private var eBordereauNo: String? = null
    @SerializedName("userLocationID")
    @Expose
    private var userLocationID: Int? = null

    fun getWagonNo(): String? {
        return wagonNo
    }

    fun setWagonNo(wagonNo: String?) {
        this.wagonNo = wagonNo
    }

    fun getBordereauNo(): String? {
        return bordereauNo
    }

    fun setBordereauNo(bordereauNo: String?) {
        this.bordereauNo = bordereauNo
    }

    fun getEBordereauNo(): String? {
        return eBordereauNo
    }

    fun setEBordereauNo(eBordereauNo: String?) {
        this.eBordereauNo = eBordereauNo
    }

    fun getUserLocationID(): Int? {
        return userLocationID
    }

    fun setUserLocationID(userLocationID: Int?) {
        this.userLocationID = userLocationID
    }
}