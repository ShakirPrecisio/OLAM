package com.kemar.olam.physicalcount.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class StockListRequest {
    @SerializedName("endDate")
    @Expose
    var endDate: String? = null

    @SerializedName("details")
    @Expose
    var details: List<Detail>? = null

    @SerializedName("id")
    @Expose
    var id: Any? = null

    @SerializedName("startDate")
    @Expose
    var startDate: String? = null

    @SerializedName("logParkName")
    @Expose
    var logParkName: Any? = null

    @SerializedName("username")
    @Expose
    var username: String=""

    @SerializedName("status")
    @Expose
    var status: Any? = null

    var isOffline: Boolean = false

    var uniqueId: String = ""
}