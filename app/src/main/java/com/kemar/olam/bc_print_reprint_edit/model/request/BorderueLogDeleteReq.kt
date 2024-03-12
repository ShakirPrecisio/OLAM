package com.kemar.olam.bc_print_reprint_edit.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class BorderueLogDeleteReq {
    @SerializedName("userID")
    @Expose
    private var userID: Int? = null
    @SerializedName("logData")
    @Expose
    private var logData: List<LogDatum?>? = null

    fun getUserID(): Int? {
        return userID
    }

    fun setUserID(userID: Int?) {
        this.userID = userID
    }

    fun getLogData(): List<LogDatum?>? {
        return logData
    }

    fun setLogData(logData: List<LogDatum?>?) {
        this.logData = logData
    }

    class LogDatum {
        @SerializedName("deleteDetailId")
        @Expose
        var deleteDetailId: Int? = null
        @SerializedName("bordereauHeaderId")
        @Expose
        var bordereauHeaderId: Int? = null

    }

}