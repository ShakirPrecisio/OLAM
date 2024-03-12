package com.kemar.olam.delivery_management.model.response

import com.google.gson.annotations.SerializedName
import com.google.gson.annotations.Expose
import com.kemar.olam.delivery_management.model.response.LogsUserHistoryRes.UserHist
import java.io.Serializable

class LogsUserHistoryRes : Serializable {
    @SerializedName("severity")
    @Expose
    var severity: Int? = null

    @SerializedName("errorMessage")
    @Expose
    var errorMessage: String? = null

    @SerializedName("count")
    @Expose
    var count: Int? = null

    @SerializedName("stackTrace")
    @Expose
    var stackTrace: String? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("userHistList")
    @Expose
    var userHistList: List<UserHist>? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    class UserHist: Serializable  {
        @SerializedName("pdfFilePath")
        @Expose
        var pdfFilePath: String? = null

        @SerializedName("customerShortName")
        @Expose
        var customerShortName: String? = null

        @SerializedName("deliveryId")
        @Expose
        var deliveryId: Int? = null


        @SerializedName("customerId")
        @Expose
        var customerId: Int? = null

        @SerializedName("truckName")
        @Expose
        var truckName: String? = null

        @SerializedName("deliveryDate")
        @Expose
        var deliveryDate: String? = null

        @SerializedName("deliveryStatus")
        @Expose
        var deliveryStatus: String? = null

        @SerializedName("deliveryNumber")
        @Expose
        var deliveryNumber: String? = null
    }
}