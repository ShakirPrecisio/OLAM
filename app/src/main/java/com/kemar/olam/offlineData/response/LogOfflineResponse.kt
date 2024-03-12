package com.kemar.olam.offlineData.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class LogOfflineResponse {
    @SerializedName("severity")
    @Expose
    var severity: Int? = null

    @SerializedName("pdfFilePath")
    @Expose
    var pdfFilePath: Any? = null

    @SerializedName("inspectHeader")
    @Expose
    var inspectHeader: Any? = null

    @SerializedName("logDetail")
    @Expose
    var logDetail: Any? = null

    @SerializedName("errorMessage")
    @Expose
    var errorMessage: Any? = null

    @SerializedName("bordereauCount")
    @Expose
    var bordereauCount: Any? = null

    @SerializedName("stackTrace")
    @Expose
    var stackTrace: Any? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("bordereauResponse")
    @Expose
    var bordereauResponse: Any? = null

    @SerializedName("logDetails")
    @Expose
    var logDetails: List<LogDetail>? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("inspection")
    @Expose
    var inspection: Any? = null
}