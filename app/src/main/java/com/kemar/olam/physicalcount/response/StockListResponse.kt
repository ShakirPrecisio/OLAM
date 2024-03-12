package com.kemar.olam.physicalcount.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class StockListResponse {
    @SerializedName("severity")
    @Expose
    var severity: Int? = null

    @SerializedName("stockListRequest")
    @Expose
    var stockListRequest: StockListRequest? = null

    @SerializedName("data")
    @Expose
    var data: Any? = null

    @SerializedName("logDetail")
    @Expose
    var logDetail: Any? = null

    @SerializedName("errorMessage")
    @Expose
    var errorMessage: Any? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("logDetails")
    @Expose
    var logDetails: Any? = null

    @SerializedName("pdfFilePath")
    @Expose
    var pdfFilePath: Any? = null

    @SerializedName("inspectHeader")
    @Expose
    var inspectHeader: Any? = null

    @SerializedName("bordereauCount")
    @Expose
    var bordereauCount: Any? = null

    @SerializedName("stackTrace")
    @Expose
    var stackTrace: Any? = null

    @SerializedName("bordereauResponse")
    @Expose
    var bordereauResponse: Any? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("inspection")
    @Expose
    var inspection: Any? = null

    @SerializedName("stockListRequests")
    @Expose
    var stockListRequests: List<StockListRequest>? = null
}