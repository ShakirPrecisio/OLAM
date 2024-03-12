package com.kemar.olam.loading_wagons.model.responce

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class getBorderuaSerialNoRes {
    @SerializedName("status")
    @Expose
    private var status: String? = null
    @SerializedName("message")
    @Expose
    private var message: String? = null
    @SerializedName("severity")
    @Expose
    private var severity: Int? = null
    @SerializedName("errorMessage")
    @Expose
    private var errorMessage: String? = null
    @SerializedName("stackTrace")
    @Expose
    private var stackTrace: String? = null
    @SerializedName("bordereauCount")
    @Expose
    private var bordereauCount: Int? = null
    @SerializedName("bordereauResponse")
    @Expose
    private var bordereauResponse: String? = null
    @SerializedName("logDetail")
    @Expose
    private var logDetail: String? = null

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String?) {
        this.status = status
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }

    fun getSeverity(): Int? {
        return severity
    }

    fun setSeverity(severity: Int?) {
        this.severity = severity
    }

    fun getErrorMessage(): String? {
        return errorMessage
    }

    fun setErrorMessage(errorMessage: String?) {
        this.errorMessage = errorMessage
    }

    fun getStackTrace(): String? {
        return stackTrace
    }

    fun setStackTrace(stackTrace: String?) {
        this.stackTrace = stackTrace
    }

    fun getBordereauCount(): Int? {
        return bordereauCount
    }

    fun setBordereauCount(bordereauCount: Int?) {
        this.bordereauCount = bordereauCount
    }

    fun getBordereauResponse(): String? {
        return bordereauResponse
    }

    fun setBordereauResponse(bordereauResponse: String?) {
        this.bordereauResponse = bordereauResponse
    }

    fun getLogDetail(): String? {
        return logDetail
    }

    fun setLogDetail(logDetail: String?) {
        this.logDetail = logDetail
    }

}