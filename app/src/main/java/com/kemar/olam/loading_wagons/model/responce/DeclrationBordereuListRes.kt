package com.kemar.olam.loading_wagons.model.responce

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


class DeclrationBordereuListRes {
    @SerializedName("bordereauRecordList")
    @Expose
    private var bordereauRecordList: List<BordereauRecordList?>? = null
    @SerializedName("count")
    @Expose
    private var count: Int? = null
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

    fun getBordereauRecordList(): List<BordereauRecordList?>? {
        return bordereauRecordList
    }

    fun setBordereauRecordList(bordereauRecordList: List<BordereauRecordList?>?) {
        this.bordereauRecordList = bordereauRecordList
    }

    fun getCount(): Int? {
        return count
    }

    fun setCount(count: Int?) {
        this.count = count
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

    class BordereauRecordList : Serializable {


        @SerializedName("uniqueId")
        @Expose
        var uniqueId: String? = null


        @SerializedName("bordereauHeaderId")
        @Expose
        var bordereauHeaderId: Int? = null
        @SerializedName("bordereauNo")
        @Expose
        var bordereauNo: String? = null
        @SerializedName("eBordereauNo")
        @Expose
        var eBordereauNo: String? = null
        @SerializedName("recordDocNo")
        @Expose
        var recordDocNo: String? = null
        @SerializedName("supplierName")
        @Expose
        var supplierName: String? = null
        @SerializedName("supplierShortName")
        @Expose
        var supplierShortName: String? = null
        @SerializedName("bordereauDate")
        @Expose
        var bordereauDate: String? = null
        @SerializedName("requestReceivedDate")
        @Expose
        var requestReceivedDate: String? = null
        @SerializedName("bordereauDateString")
        @Expose
        var bordereauDateString: String? = null
        @SerializedName("requestReceivedDateString")
        @Expose
        var requestReceivedDateString: String? = null
        @SerializedName("wagonNo")
        @Expose
        var wagonNo: String? = null
        @SerializedName("wagonId")
        @Expose
        var wagonId: Int? = null
        @SerializedName("logQty")
        @Expose
        var logQty: Int? = null
        @SerializedName("headerStatus")
        @Expose
        var headerStatus: String? = null
        @SerializedName("detailStatus")
        @Expose
        var detailStatus: String? = null
        @SerializedName("inspectionFlag")
        @Expose
        var inspectionFlag: String? = null
        @SerializedName("supplierId")
        @Expose
        var supplierId: Int? = null
        @SerializedName("gsezCBM")
        @Expose
        var gsezCBM: Double? = null
        @SerializedName("uniqueMaterial")
        @Expose
        var uniqueMaterial: String? = null
        @SerializedName("transportMode")
        @Expose
        var transportMode: Int? = null
        @SerializedName("timezoneId")
        @Expose
        var timezoneId: String? = null
        @SerializedName("fscId")
        @Expose
        var fscId: Int? = null
        @SerializedName("originId")
        @Expose
        var originId: Int? = null
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
        @SerializedName("transporterName")
        @Expose
        var transporterName: String? = null
        @SerializedName("transporterId")
        @Expose
        var transporterId: Int? = null
        @SerializedName("totaltonnage")
        @Expose
        var totaltonnage: Double? = 0.0

        var isExpanded:Boolean = false
        var isSelected:Boolean = false
    }

  /*  class BordereauRecordList {
        @SerializedName("bordereauHeaderId")
        @Expose
        var bordereauHeaderId: Int? = null
        @SerializedName("bordereauNo")
        @Expose
        var bordereauNo: String? = null
        @SerializedName("eBordereauNo")
        @Expose
        var eBordereauNo: String? = null
        @SerializedName("recordDocNo")
        @Expose
        var recordDocNo: String? = null
        @SerializedName("supplierName")
        @Expose
        var supplierName: String? = null
        @SerializedName("supplierShortName")
        @Expose
        var supplierShortName: String? = null
        @SerializedName("bordereauDate")
        @Expose
        var bordereauDate: String? = null
        @SerializedName("requestReceivedDate")
        @Expose
        var requestReceivedDate: String? = null
        @SerializedName("bordereauDateString")
        @Expose
        var bordereauDateString: String? = null
        @SerializedName("requestReceivedDateString")
        @Expose
        var requestReceivedDateString: String? = null
        @SerializedName("wagonNo")
        @Expose
        var wagonNo: String? = null
        @SerializedName("wagonId")
        @Expose
        var wagonId: String? = null
        @SerializedName("logQty")
        @Expose
        var logQty: Int? = null
        @SerializedName("headerStatus")
        @Expose
        var headerStatus: String? = null
        @SerializedName("detailStatus")
        @Expose
        var detailStatus: String? = null
        @SerializedName("inspectionFlag")
        @Expose
        var inspectionFlag: String? = null
        @SerializedName("supplierId")
        @Expose
        var supplierId: Int? = null
        @SerializedName("gsezCBM")
        @Expose
        var gsezCBM: Double? = 0.0
        @SerializedName("uniqueMaterial")
        @Expose
        var uniqueMaterial: String? = null
        @SerializedName("transportMode")
        @Expose
        var transportMode: Int? = null
        @SerializedName("timezoneId")
        @Expose
        var timezoneId: String? = null
        @SerializedName("fscId")
        @Expose
        var fscId: Int? = null
        @SerializedName("originId")
        @Expose
        var originId: Int? = null
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
        @SerializedName("transporterName")
        @Expose
        var transporterName: String? = null
        @SerializedName("transporterId")
        @Expose
        var transporterId: Int? = null

        var isExpanded:Boolean = false
        var isSelected:Boolean = false

    }*/

}