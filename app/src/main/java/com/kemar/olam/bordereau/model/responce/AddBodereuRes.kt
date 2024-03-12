package com.kemar.olam.bordereau.model.responce

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import java.io.Serializable


class AddBodereuRes : Serializable{
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
    private var bordereauResponse: BordereauResponse? = null


    @SerializedName("data")
    @Expose
    private var data: Data? = null

    //Inspection Header Res
    @SerializedName("inspectHeader")
    @Expose
    private var inspectHeader: BordereauResponse? = null

  /*  //Ground Header Res
    @SerializedName("inspection")
    @Expose
    private var inspection: BordereauResponse? = null*/


    fun getData(): Data? {
        return data
    }

    fun setData(data: Data?) {
        this.data = data
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

    fun getBordereauResponse(): BordereauResponse? {
        return bordereauResponse
    }

    fun setBordereauResponse(bordereauResponse: BordereauResponse?) {
        this.bordereauResponse = bordereauResponse
    }

    fun getInspectionHeader(): BordereauResponse? {
        return inspectHeader
    }

    fun setInspectionHeader(inspectHeader: BordereauResponse?) {
        this.inspectHeader = inspectHeader
    }


    class BordereauResponse : Serializable {


        var unqiueId: String? = null



        @SerializedName("bordereauHeaderId")
        @Expose
        var bordereauHeaderId: Int? = null
        @SerializedName("bordereauRecordNo")
        @Expose
        var bordereauRecordNo: String? = null

        @SerializedName("recordDocNo")
        @Expose
        var recordDocNo: String? = ""


        @SerializedName("supplier")
        @Expose
        var supplier: Int? = null
        @SerializedName("supplierLocation")
        @Expose
        var supplierLocation: String? = null
        @SerializedName("modeOfTransport")
        @Expose
        var modeOfTransport: Int? = null
        @SerializedName("transporterId")
        @Expose
        var transporterId: Int? = null
        @SerializedName("transporterName")
        @Expose
        var transporterName: String? = ""
        @SerializedName("bordereauNo")
        @Expose
        var bordereauNo: String? = null
        @SerializedName("eBordereauNo")
        @Expose
        var eBordereauNo: String? = null
        @SerializedName("bordereauDate")
        @Expose
        var bordereauDate: String? = null
        @SerializedName("receiptDate")
        @Expose
        var receiptDate: String? = null
        @SerializedName("requestReceivedDate")
        @Expose
        var requestReceivedDate: String? = null
        @SerializedName("wagonNo")
        @Expose
        var wagonNo: String? = null
        @SerializedName("wagonId")
        @Expose
        var wagonId: Int? = 0
        @SerializedName("transactionStatus")
        @Expose
        var transactionStatus: String? = null
        @SerializedName("errorMessage")
        @Expose
        var errorMessage: String? = null
        @SerializedName("mode")
        @Expose
        var mode: String? = null
        @SerializedName("logQty")
        @Expose
        var logQty: String? = null
        @SerializedName("ecmHeaderNumber")
        @Expose
        var ecmHeaderNumber: String? = null
        @SerializedName("timezoneId")
        @Expose
        var timezoneId: String? = null
        @SerializedName("bordereauDetails")
        @Expose
        var bordereauDetails: String? = null
        @SerializedName("bordereauDateString")
        @Expose
        var bordereauDateString: String? = null
        @SerializedName("fscId")
        @Expose
        var fscId: Int? = null

        @SerializedName("leauChargementId")
        @Expose
        var leauChargementId: Int? = null
        @SerializedName("destination")
        @Expose
        var destination: String? = null
        @SerializedName("leauChargementName")
        @Expose
        var leauChargementName: String? = null
        @SerializedName("distance")
        @Expose
        var distance: String? = null
        @SerializedName("originName")
        @Expose
        var originName: String? = null
        @SerializedName("fscName")
        @Expose
        var fscName: String? = null
        @SerializedName("userID")
        @Expose
        var userID: String? = null
        @SerializedName("speciesId")
        @Expose
        var speciesId: String? = null
        @SerializedName("originID")
        @Expose
        var originID: Int? = null
        @SerializedName("supplierShortName")
        @Expose
        var supplierShortName: String? = null
        @SerializedName("headerStatus")
        @Expose
        var headerStatus: String? = null
        @SerializedName("supplierName")
        @Expose
        var supplierName: String? = null
        @SerializedName("cbmsupplierGiven")
        @Expose
        var cbmsupplierGiven: String? = null
        @SerializedName("cbmreceived")
        @Expose
        var cbmreceived: String? = null


        //additional feild for inspection
        @SerializedName("customerId")
        @Expose
        private val customerId: Int? = null
        @SerializedName("responsibleCustomerId")
        @Expose
        private val responsibleCustomerId: Int? = null
        @SerializedName("gradeId")
        @Expose
         val gradeId: Int? = null
        @SerializedName("graderId")
        @Expose
        private val graderId: Int? = null
        @SerializedName("tracerCherges")
        @Expose
        private val tracerCherges: String? = null
        @SerializedName("price")
        @Expose
        private val price: String? = null
        @SerializedName("refraction")
        @Expose
        private val refraction: String? = null
        @SerializedName("inspectionDate")
        @Expose
        private val inspectionDate: String? = null
        @SerializedName("inspectionNO")
        @Expose
        val inspectionNO: String? = null

        @SerializedName("userLocationID")
        @Expose
        private val userLocationID: Int? = null
        @SerializedName("userId")
        @Expose
        private val userId: String? = null
        @SerializedName("admin")
        @Expose
        private val admin: Boolean? = null


        //Additional feild for ground inspection
        @SerializedName("inspectionId")
        @Expose
         val inspectionId: Int? = null

        @SerializedName("parcId")
        @Expose
        val parcId: Int? = null
        @SerializedName("customerName")
        @Expose
         val customerName: String? = null
        @SerializedName("graderName")
        @Expose
         val graderName: String? = null
        @SerializedName("parcName")
        @Expose
         val parcName: String? = null
        @SerializedName("gradeName")
        @Expose
         val gradeName: String? = null

        @SerializedName("customerShortName")
        @Expose
        var customerShortName: String? = null

    }

}