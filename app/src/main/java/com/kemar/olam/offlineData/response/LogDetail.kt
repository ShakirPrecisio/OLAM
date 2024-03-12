package com.kemar.olam.offlineData.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class LogDetail :RealmObject(){

    @PrimaryKey
    var id: Int? = null


    var isUsed: Boolean = false

    @SerializedName("logNo")
    @Expose
    var logNo: String? = null

    @SerializedName("operationalDate")
    @Expose
    var operationalDate: String? = null

    @SerializedName("barcodeNumber")
    @Expose
    var barcodeNumber: String? = null

    @SerializedName("materialDesc")
    @Expose
    var materialDesc: Int? = null

    @SerializedName("inspectionNo")
    @Expose
    var inspectionNo: String? = null

    @SerializedName("detailId")
    @Expose
    var detailId: Int? = null

    @SerializedName("screen")
    @Expose
    var screen: String? = null

    @SerializedName("userID")
    @Expose
    var userID: String? = null

    @SerializedName("refLong")
    @Expose
    var refLong: String? = null

    @SerializedName("gradeStr")
    @Expose
    var gradeStr: String? = null

    @SerializedName("mode")
    @Expose
    var mode: String? = null

    @SerializedName("diam4Bdx")
    @Expose
    var diam4Bdx: Int? = null

    @SerializedName("longBdx")
    @Expose
    var longBdx: Int? = null

    @SerializedName("totalQuantity")
    @Expose
    var totalQuantity: String? = null

    @SerializedName("poDate")
    @Expose
    var poDate: String? = null

    @SerializedName("supplier")
    @Expose
    var supplier: Int? = null

    @SerializedName("logSpecies")
    @Expose
    var logSpecies: Int? = null

    @SerializedName("supplierShortName")
    @Expose
    var supplierShortName: String? = null

    @SerializedName("refCbm")
    @Expose
    var refCbm: String? = null

    @SerializedName("bordereauNo")
    @Expose
    var bordereauNo: String? = null

    @SerializedName("inspectionFlag")
    @Expose
    var inspectionFlag: String? = null

    @SerializedName("financeDate")
    @Expose
    var financeDate: String? = null

    @SerializedName("refDia")
    @Expose
    var refDia: String? = null

    @SerializedName("logRecordDocNo")
    @Expose
    var logRecordDocNo: String? = null

    @SerializedName("quality")
    @Expose
    var quality: String? = null

    @SerializedName("sawnAssignmentId")
    @Expose
    var sawnAssignmentId: String? = null

    @SerializedName("plaqNo")
    @Expose
    var plaqNo: String? = null

    @SerializedName("aacName")
    @Expose
    var aacName: String? = null

    @SerializedName("wagonNo")
    @Expose
    var wagonNo: String? = null

    @SerializedName("diam3Bdx")
    @Expose
    var diam3Bdx: Int? = null

    @SerializedName("logNo2")
    @Expose
    var logNo2: String? = null

    @SerializedName("aacId")
    @Expose
    var aacId: Int? = null

    @SerializedName("totaltonnage")
    @Expose
    var totaltonnage: String? = null

    @SerializedName("poNumber")
    @Expose
    var poNumber: String? = null

    @SerializedName("rejectionStatus")
    @Expose
    var rejectionStatus: String? = null

    @SerializedName("eBordereauNo")
    @Expose
    private var eBordereauNo: String? = null

    @SerializedName("diaType")
    @Expose
    var diaType: String? = null

    @SerializedName("diam2Bdx")
    @Expose
    var diam2Bdx: Int? = null

    @SerializedName("customerPurchasedFromForest")
    @Expose
    var customerPurchasedFromForest: String? = null

    @SerializedName("bordereaDetailStatus")
    @Expose
    var bordereaDetailStatus: String? = null

    @SerializedName("customerId")
    @Expose
    var customerId: String? = null

    @SerializedName("totalCBM")
    @Expose
    var totalCBM: String? = null

    @SerializedName("isBordereauInspectionCompleted")
    @Expose
    var isBordereauInspectionCompleted: String? = null

    @SerializedName("inspectionDate")
    @Expose
    var inspectionDate: String? = null

    @SerializedName("qualityId")
    @Expose
    var qualityId: Int? = null

    @SerializedName("supplierName")
    @Expose
    var supplierName: String? = null

    @SerializedName("diamBdx")
    @Expose
    var diamBdx: Int? = null

    @SerializedName("gradeName")
    @Expose
    var gradeName: String? = null

    @SerializedName("gradeId")
    @Expose
    var gradeId: String? = null

    @SerializedName("quantity")
    @Expose
    var quantity: String? = null

    @SerializedName("comments")
    @Expose
    var comments: String? = null

    @SerializedName("moduleFlag")
    @Expose
    var moduleFlag: String? = null

    @SerializedName("grnNo")
    @Expose
    var grnNo: String? = null

    @SerializedName("transactionStatus")
    @Expose
    var transactionStatus: String? = null

    @SerializedName("grnDate")
    @Expose
    var grnDate: String? = null

    @SerializedName("fscMode")
    @Expose
    var fscMode: String? = null

    @SerializedName("cbmQuantity")
    @Expose
    var cbmQuantity: String? = null

    @SerializedName("cbm")
    @Expose
    var cbm: Double? = null

    @SerializedName("inspectionId")
    @Expose
    var inspectionId: String? = null

    @SerializedName("bordereauHeaderId")
    @Expose
    var bordereauHeaderId: Int? = null

    @SerializedName("logSpeciesName")
    @Expose
    var logSpeciesName: String? = null

    @SerializedName("diam1Bdx")
    @Expose
    var diam1Bdx: Int? = null

    @SerializedName("aacYear")
    @Expose
    var aacYear: String? = null

    @SerializedName("loadingStatus")
    @Expose
    var loadingStatus: String? = null

    fun geteBordereauNo(): String? {
        return eBordereauNo
    }

    fun seteBordereauNo(eBordereauNo: String?) {
        this.eBordereauNo = eBordereauNo
    }
}