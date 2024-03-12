package com.kemar.olam.bordereau.model.responce

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

class AddBodereuLogListingRes {

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
    @SerializedName("bordereauResponse")
    @Expose
    private var bordereauResponse: String? = null

    @SerializedName("pdfFilePath")
    @Expose
    private var pdfFilePath: String? = null


    fun getPdfFilePath(): String? {
        return pdfFilePath
    }

    fun setPdfFilePath(pdfFilePath: String?) {
        this.pdfFilePath = pdfFilePath
    }

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

    fun getBordereauResponse(): String? {
        return bordereauResponse
    }

    fun setBordereauResponse(bordereauResponse: String?) {
        this.bordereauResponse = bordereauResponse
    }

}