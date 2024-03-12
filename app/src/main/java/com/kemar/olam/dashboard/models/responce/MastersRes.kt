package com.kemar.olam.dashboard.models.responce

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class MastersRes {
    @SerializedName("supplierData")
    @Expose
    private var supplierData: List<SupplierDatum?>? = null
    @SerializedName("supplierLocationData")
    @Expose
    private var supplierLocationData: List<SupplierDatum?>? = null
    @SerializedName("transportModeData")
    @Expose
    private var transportModeData: List<SupplierDatum?>? = null
    @SerializedName("forestData")
    @Expose
    private var forestData: List<ForestDatum?>? = null

    @SerializedName("transporterData")
    @Expose
    private var transporterData: List<SupplierDatum?>? = null

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

    fun getSupplierData(): List<SupplierDatum?>? {
        return supplierData
    }

    fun setSupplierData(supplierData: List<SupplierDatum?>?) {
        this.supplierData = supplierData
    }

    fun getSupplierLocationData(): List<SupplierDatum?>? {
        return supplierLocationData
    }

    fun setSupplierLocationData(supplierLocationData: List<SupplierDatum?>?) {
        this.supplierLocationData = supplierLocationData
    }

    fun getTransportModeData(): List<SupplierDatum?>? {
        return transportModeData
    }

    fun setTransportModeData(transportModeData: List<SupplierDatum?>?) {
        this.transportModeData = transportModeData
    }

    fun getForestData(): List<ForestDatum?>? {
        return forestData
    }

    fun setForestData(forestData: List<ForestDatum?>?) {
        this.forestData = forestData
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

    class ForestDatum {
        @SerializedName("forestId")
        @Expose
        var forestId: Int? = null
        @SerializedName("forestName")
        @Expose
        var forestName: String? = null
        @SerializedName("shortName")
        @Expose
        var shortName: String? = null
        @SerializedName("sapCode")
        @Expose
        var sapCode: String? = null
        @SerializedName("rigAddress")
        @Expose
        var rigAddress: String? = null
        @SerializedName("type")
        @Expose
        var type: String? = null
        @SerializedName("tracerRef")
        @Expose
        var tracerRef: String? = null
        @SerializedName("fscRef")
        @Expose
        var fscRef: String? = null
        @SerializedName("tracerValidity")
        @Expose
        var tracerValidity: String? = null
        @SerializedName("fscValidity")
        @Expose
        var fscValidity: String? = null
        @SerializedName("province")
        @Expose
        var province: String? = null
        @SerializedName("origin")
        @Expose
        var origin: String? = null
        @SerializedName("concession")
        @Expose
        var concession: String? = null
        @SerializedName("aac")
        @Expose
        var aac: String? = null
        @SerializedName("aacYear")
        @Expose
        var aacYear: String? = null
        @SerializedName("volumeAnnual")
        @Expose
        var volumeAnnual: String? = null
        @SerializedName("firstDestination")
        @Expose
        var firstDestination: String? = null
        @SerializedName("finalDestination")
        @Expose
        var finalDestination: String? = null
        @SerializedName("logger")
        @Expose
        var logger: String? = null
        @SerializedName("transporter")
        @Expose
        var transporter: String? = null
        @SerializedName("species")
        @Expose
        var species: String? = null

    }

}