package com.kemar.olam.bordereau.model.responce

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class Data {
    @SerializedName("duplicateLogs")
    @Expose
    var duplicateLogs: List<String>? = null

    @SerializedName("addedLogs")
    @Expose
    var addedLogs: List<String>? = null


    @SerializedName("headerId")
    @Expose
    var headerId: String? = null


}