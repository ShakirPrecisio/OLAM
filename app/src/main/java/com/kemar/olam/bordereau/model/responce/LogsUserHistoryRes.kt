package com.kemar.olam.bordereau.model.responce

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class LogsUserHistoryRes : Serializable{
    @SerializedName("bordereauRecordList")
    @Expose
    private var bordereauRecordList: List<BordereauRecordList?>? = null

    @SerializedName("bordereauGroundList")
    @Expose
    private var bordereauGroundList: List<BordereauGroundList?>? = null

    @SerializedName("count")
    @Expose
    private var count: Int? = null

    @SerializedName("severity")
    @Expose
    private var severity: Int? = null

    @SerializedName("message")
    @Expose
    private var message: String? = null
    fun getSeverity(): Int? {
        return severity
    }


    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }


    fun setSeverity(severity: Int?) {
        this.severity = severity
    }

    fun getBordereauGroundList(): List<BordereauGroundList?>? {
        return bordereauGroundList
    }

    fun setBordereauGroundList(bordereauRecordList: List<BordereauGroundList?>?) {
        this.bordereauGroundList = bordereauGroundList
    }


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


    class BordereauGroundList : Serializable {
        @SerializedName("verifiedDate")
        @Expose
        var verifiedDate: String? = null
        @SerializedName("inspectionDate")
        @Expose
        var inspectionDate: String? = null
        @SerializedName("verifiedDateStr")
        @Expose
        var verifiedDateStr: String? = null
        @SerializedName("inspectionDateStr")
        @Expose
        var inspectionDateStr: String? = null
        @SerializedName("requestReceivedDate")
        @Expose
        var requestReceivedDate: String? = null
        @SerializedName("refraction")
        @Expose
        var refraction: String? = null
        @SerializedName("wagonId")
        @Expose
        var wagonId: String? = null
        @SerializedName("invoiceBillPath")
        @Expose
        var invoiceBillPath: String? = null
        @SerializedName("transportMode")
        @Expose
        var transportMode: Int? = null
        @SerializedName("timezoneId")
        @Expose
        var timezoneId: String? = null
        @SerializedName("inspectionNumber")
        @Expose
        var inspectionNumber: String? = null
        @SerializedName("transporterName")
        @Expose
        var transporterName: String? = null
        @SerializedName("transporterId")
        @Expose
        var transporterId: String? = null
        @SerializedName("verifyFlag")
        @Expose
        var verifyFlag: String? = null
        @SerializedName("deliveryStatus")
        @Expose
        var deliveryStatus: String? = null
        @SerializedName("gradeName")
        @Expose
        var gradeName: String? = null
        @SerializedName("gradeId")
        @Expose
        val gradeId: Int? = null
        @SerializedName("customerName")
        @Expose
        var customerName: String? = null
        @SerializedName("graderName")
        @Expose
        var graderName: String? = null
        @SerializedName("parcName")
        @Expose
        var parcName: String? = null
        @SerializedName("inspectionFlag")
        @Expose
        var inspectionFlag: String? = null
        @SerializedName("inspectionId")
        @Expose
        var inspectionId: Int? = null
        @SerializedName("customerShortName")
        @Expose
        var customerShortName: String? = null

    }

    class BordereauRecordList :Serializable {

        var uniqueId: String?  = null


        var isUpload: Boolean  = false

        var isFail: Boolean  = false


        var isDeclare: Boolean?  = false


        var failreason: String?  = null

        @SerializedName("bordereauHeaderId")
        @Expose
        var bordereauHeaderId: Int? = 0
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
        var wagonId: Int? = 0
        @SerializedName("logQty")
        @Expose
        var logQty: Int? = null

        @SerializedName("logQty1")
        @Expose
        var logQty1: Int? = null

        @SerializedName("headerStatus")
        @Expose
        var headerStatus: String? = null
        @SerializedName("loadingStatus")
        @Expose
        var loadingStatus: String? = null
        @SerializedName("detailStatus")
        @Expose
        var detailStatus: String? = null
        @SerializedName("supplierId")
        @Expose
        var supplierId: Int? = 0
        @SerializedName("gsezCBM")
        @Expose
        var gsezCBM: String? = null
        @SerializedName("uniqueMaterial")
        @Expose
        var uniqueMaterial: String? = null
        @SerializedName("transportMode")
        @Expose
        var transportMode: Int? = 0
        @SerializedName("timezoneId")
        @Expose
        var timezoneId: String? = null
        @SerializedName("fscId")
        @Expose
        var fscId: Int? = 0
        @SerializedName("originId")
        @Expose
        var originId: Int? = 0
        @SerializedName("leauChargementId")
        @Expose
        var leauChargementId: Int? = 0
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
        @SerializedName("transporterId")
        @Expose
        var transporterId: Int? = null
        @SerializedName("transporterName")
        @Expose
        var transporterName: String? = ""
        @SerializedName("verifyFlag")
        @Expose
        var verifyFlag: String? = null
        //for sales n inspection stats
        @SerializedName("inspectionFlag")
        @Expose
        var inspectionFlag: String? = null

        //added for inspection reprint
        @SerializedName("inspectionNumber")
        @Expose
        var inspectionNumber: String? = null

        //addeed for Delivery Status
        @SerializedName("deliveryStatus")
        @Expose
        var deliveryStatus: String? = null

        //added for sales header Date
        @SerializedName("verifiedDateStr")
        @Expose
        var verifiedDateStr: String? = null

        //added for delivery header date
        @SerializedName("inspectionDateStr")
        @Expose
        var inspectionDateStr: String? = null

        //added for identify record from grond sales or sales inspections
        @SerializedName("module")
        @Expose
        var module: String? = null
        @SerializedName("inspectionId")
        @Expose
        var inspectionId: Int? = null


        @SerializedName("customerName")
        @Expose
        var customerName: String? = null

        @SerializedName("customerId")
        @Expose
        var customerId: Int? = 0

        @SerializedName("gradeName")
        @Expose
        var gradeName: String? = null

        @SerializedName("gradeId")
        @Expose
        val gradeId: Int? = null

        @SerializedName("customerShortName")
        @Expose
        var customerShortName: String? = null

        var index : Int? =null
        //added for origin filter
        @SerializedName("declaredDate")
        @Expose
        private var stockDeclaredDate: String? = null

        //added for deliivery history
        @SerializedName("deliveryNumber")
        @Expose
         var deliveryNumber: String? = null

        @SerializedName("truckName")
        @Expose
        var truckName: String? = null


        @SerializedName("truckNo")
        @Expose
        var truckNo: String? = null

        @SerializedName("deliveryId")
        @Expose
        var deliveryId: Int? = null

        @SerializedName("deliveryDate")
        @Expose
        var deliveryDate: String? = null

        @SerializedName("pdfFilePath")
        @Expose
        var pdfFilePath: String? = null



        fun getStockDeclaredDate(): String? {
            return stockDeclaredDate
        }

        fun setStockDeclaredDate(stockDeclaredDate: String?) {
            this.stockDeclaredDate = stockDeclaredDate
        }

    }
}