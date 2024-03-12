package com.kemar.olam.physicalcount.requestbody

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class PhysicalCountRequest {
    @SerializedName("endDate")
    @Expose
    var endDate: String? = null

    @SerializedName("details")
    @Expose
    var details: List<Detail>? = null

    @SerializedName("id")
    @Expose
    var id: String? = null

    @SerializedName("startDate")
    @Expose
    var startDate: String? = null

    @SerializedName("logParkName")
    @Expose
    var logParkName: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null
}