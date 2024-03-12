package com.kemar.olam.loading_wagons.model.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class WagonLogRequest : RealmObject() {

    @PrimaryKey
    var uniqueId: String? = null

    var isUploaded: Boolean=false

    var isFailure: Boolean=false

    var forestuniqueId: String? = null


    @SerializedName("fscmode")
    @Expose
    private var fscMode: String? = null

    @SerializedName("aacId")
    @Expose
    var aacId: Int? = 0

    @SerializedName("aacName")
    @Expose
    var aacName: String? = ""

    @SerializedName("aacYear")
    @Expose
    var aacYear: String? = ""

    var isSelect: Boolean? = false

    @SerializedName("customerPurchsedFromForest")
    @Expose
    var customerPurchsedFromForest: String? = ""

    @SerializedName("diaType")
    @Expose
    private var diaType: String? = null

    @SerializedName("logNo")
    @Expose
    private var logNo: String? = null
    @SerializedName("logRecordDocNo")
    @Expose
    private var logRecordDocNo: String? = null
    @SerializedName("logSpecies")
    @Expose
    private var logSpecies: Int? = 0
    @SerializedName("logSpeciesName")
    @Expose
    private var logSpeciesName: String? = ""
    @SerializedName("materialDesc")
    @Expose
    private var materialDesc: Int? =  0
    @SerializedName("plaqNo")
    @Expose
    private var plaqNo: String? = null
    @SerializedName("longBdx")
    @Expose
    private var longBdx: Int? = 0
    @SerializedName("diamBdx")
    @Expose
    private var diamBdx: Int? = 0

    @SerializedName("diam1Bdx")
    @Expose
    private var diam1Bdx: Int? = 0

    @SerializedName("diam2Bdx")
    @Expose
    private var diam2Bdx: Int? = 0

    @SerializedName("diam3Bdx")
    @Expose
    private var diam3Bdx: Int? = 0

    @SerializedName("diam4Bdx")
    @Expose
    private var diam4Bdx: Int? = 0

    @SerializedName("avgDiamBdx")
    @Expose
    private var avgDiamBdx: Int? = 0

    @SerializedName("cbm")
    @Expose
    private var cbm: Double? = 0.0
    @SerializedName("quality")
    @Expose
    private var quality: String? = ""
    @SerializedName("qualityId")
    @Expose
    private var qualityId: Int? = 0
    @SerializedName("comments")
    @Expose
    private var comments: String? = ""
    @SerializedName("detailId")
    @Expose
    private var detailId: String? = null
    @SerializedName("mode")
    @Expose
    private var mode: String? = null

    @SerializedName("logNo2")
    @Expose
    private var logNo2: String? = null


    @SerializedName("sawnAssignmentId")
    @Expose
    private var sawnAssignmentId: String? = null
    @SerializedName("bordereauHeaderId")
    @Expose
    private var bordereauHeaderId: Int? = null
    @SerializedName("bordereauNo")
    @Expose
    private var bordereauNo: String? = null
    @SerializedName("eBordereauNo")
    @Expose
    private var eBordereauNo: String? = null
    @SerializedName("wagonNo")
    @Expose
    private var wagonNo: String? = null

    @SerializedName("bordereaDetailStatus")
    @Expose
    private var bordereaDetailStatus: String? = null

    @SerializedName("totalQuantity")
    @Expose
    private var totalQuantity: String? = null
    @SerializedName("totalCBM")
    @Expose
    private var totalCBM: String? = null
    @SerializedName("cbmQuantity")
    @Expose
    private var cbmQuantity: String? = null
    @SerializedName("inspectionFlag")
    @Expose
    private var inspectionFlag: String? = null
    @SerializedName("moduleFlag")
    @Expose
    private var moduleFlag: String? = null
    @SerializedName("poDate")
    @Expose
    private var poDate: String? = null
    @SerializedName("poNumber")
    @Expose
    private var poNumber: String? = null
    @SerializedName("grnNo")
    @Expose
    private var grnNo: String? = null
    @SerializedName("grnDate")
    @Expose
    private var grnDate: String? = null
    @SerializedName("operationalDate")
    @Expose
    private var operationalDate: String? = null
    @SerializedName("financeDate")
    @Expose
    private var financeDate: String? = null
    @SerializedName("transactionStatus")
    @Expose
    private var transactionStatus: String? = null
    @SerializedName("isBordereauInspectionCompleted")
    @Expose
    private var isBordereauInspectionCompleted: String? = null
    @SerializedName("screen")
    @Expose
    private var screen: String? = null
    @SerializedName("userID")
    @Expose
    private var userID: String? = null

    private var barcodeNumber: String = ""
    @SerializedName("refDia")
    @Expose
    private var refractionDiam: Int? = 0

    @SerializedName("refLong")
    @Expose
    private var refractionLength: Int? = 0

    @SerializedName("grade")
    @Expose
    private var grade: String? = ""

    @SerializedName("gradeStr")
    @Expose
    private var gradeStr: String? = ""

    //aaded for confirm for billing
    @SerializedName("rejectionStatus")
    @Expose
    private var rejectionStatus: String? = "N"

    //added for Grade changes in sales inspection & ground inspection
    @SerializedName("gradeId")
    @Expose
    private var gradeId: Int? = null

    @SerializedName("gradeName")
    @Expose
    private var gradeName: String? = null

    //added for ground sales filter
    @SerializedName("supplierName")
    @Expose
    private var supplierName: String? = null

    @SerializedName("supplierShortName")
    @Expose
    private var supplierShortName: String? = null


    @SerializedName("customerPurchasedFromForest")
    @Expose
    private var customerPurchasedFromForest: String? = null


    @SerializedName("essence")
    @Expose
    private var essence: String? = null



    private var index: Int? = null

    fun getIndex(): Int? {
        return index
    }

    fun setIndex(index: Int?) {
        this.index = index
    }



    fun getGradeId(): Int? {
        return gradeId
    }

    fun setGradeId(gradeId: Int?) {
        this.gradeId = gradeId
    }

    fun getGradeName(): String? {
        return gradeName
    }

    fun setGradeName(gradeName: String?) {
        this.gradeName = gradeName
    }


    fun getSupplierName(): String? {
        return supplierName
    }

    fun setSupplierName(supplierName: String?) {
        this.supplierName = supplierName
    }

    fun getdiaType(): String? {
        return diaType
    }

    fun setdiaType(diaType: String?) {
        this.diaType = diaType
    }

    fun getSupplierShortName(): String? {
        return supplierShortName
    }

    fun setSupplierShortName(supplierShortName: String?) {
        this.supplierShortName = supplierShortName
    }

    fun getcustomerPurchasedFromForest(): String? {
        return customerPurchasedFromForest
    }

    fun setcustomerPurchasedFromForest(customerPurchasedFromForest: String?) {
        this.customerPurchasedFromForest = customerPurchasedFromForest
    }


    fun getessence(): String? {
        return essence
    }

    fun setessence(essence: String?) {
        this.essence = essence
    }


    var isExpanded:Boolean = false
    var isSelected:Boolean = false
    var isEditable :Boolean=  false


    fun isEditable(): Boolean? {
        return isEditable
    }

    fun setIsEditable(isEditable : Boolean) {
        this.isEditable = isEditable
    }

    fun isSelected(): Boolean? {
        return isSelected
    }

    fun setIsSelected(isSelected : Boolean) {
        this.isSelected = isSelected
    }


    fun isExpanded(): Boolean? {
        return isExpanded
    }

    fun setIsExpanded(isExpanded : Boolean) {
        this.isExpanded = isExpanded
    }




    fun getLogNo(): String? {
        return logNo
    }

    fun setLogNo(logNo: String?) {
        this.logNo = logNo
    }

    fun getLogRecordDocNo(): String? {
        return logRecordDocNo
    }

    fun setLogRecordDocNo(logRecordDocNo: String?) {
        this.logRecordDocNo = logRecordDocNo
    }

    fun getLogSpecies(): Int? {
        return logSpecies
    }

    fun setLogSpecies(logSpecies: Int?) {
        this.logSpecies = logSpecies
    }

    fun getAAC(): Int? {
        return aacId
    }

    fun setAAC(aacId: Int?) {
        this.aacId = aacId
    }


    fun getAACName(): String? {
        return aacName
    }

    fun setAACName(aacName: String?) {
        this.aacName = aacName
    }

    fun getAACYear(): String? {
        return aacYear
    }

    fun setAACYear(aacYear: String?) {
        this.aacYear = aacYear
    }


    fun getLogSpeciesName(): String? {
        return logSpeciesName
    }

    fun setLogSpeciesName(logSpeciesName: String?) {
        this.logSpeciesName = logSpeciesName
    }
    fun getMaterialDesc(): Int? {
        return materialDesc
    }

    fun setMaterialDesc(materialDesc: Int?) {
        this.materialDesc = materialDesc
    }

    fun getPlaqNo(): String? {
        return plaqNo
    }

    fun setPlaqNo(plaqNo: String?) {
        this.plaqNo = plaqNo
    }

    fun getLongBdx(): Int? {
        return longBdx
    }

    fun setLongBdx(longBdx: Int?) {
        this.longBdx = longBdx
    }

    fun getGrade(): String? {
        return grade
    }

    fun setGrade(grade: String?) {
        this.grade = grade
    }

    fun getGradeStr(): String? {
        return gradeStr
    }

    fun setGradeStr(gradeStr: String?) {
        this.gradeStr = gradeStr
    }



    fun getRejectionStatus(): String? {
        return rejectionStatus
    }

    fun setRejectionStatus(rejectionStatus: String?) {
        this.rejectionStatus = rejectionStatus
    }


    fun getRefractionLength(): Int? {
        return refractionLength
    }

    fun setRefractionLength(refractionLength: Int?) {
        this.refractionLength = refractionLength
    }


    fun getRefractionDiam(): Int? {
        return refractionDiam
    }

    fun setRefractionDiam(refractionDiam: Int?) {
        this.refractionDiam = refractionDiam
    }

    fun getDiamBdx(): Int? {
        return diamBdx
    }

    fun setDiamBdx(diamBdx: Int?) {
        this.diamBdx = diamBdx
    }

    fun getDiamBdx1(): Int? {
        return diam1Bdx
    }

    fun setDiamBdx1(diamBdx1: Int?) {
        this.diam1Bdx = diamBdx1
    }

    fun getDiamBdx2(): Int? {
        return diam2Bdx
    }

    fun setDiamBdx2(diamBdx2: Int?) {
        this.diam2Bdx = diamBdx2
    }

    fun getDiamBdx3(): Int? {
        return diam3Bdx
    }

    fun setDiamBdx3(diamBdx3: Int?) {
        this.diam3Bdx = diamBdx3
    }

    fun getDiamBdx4(): Int? {
        return diam4Bdx
    }

    fun setDiamBdx4(diamBdx4: Int?) {
        this.diam4Bdx = diamBdx4
    }

    fun getaverageBdx(): Int? {
        return avgDiamBdx
    }

    fun setAvrageBdx(avgDiamBdx: Int?) {
        this.avgDiamBdx = avgDiamBdx
    }

    fun getCbm(): Double? {
        return cbm
    }

    fun setCbm(cbm: Double?) {
        this.cbm = cbm
    }

    fun getQuality(): String? {
        return quality
    }

    fun setQuality(quality: String?) {
        this.quality = quality
    }


    fun getQualityId(): Int? {
        return qualityId
    }

    fun setQualityId(qualityId: Int?) {
        this.qualityId = qualityId
    }

    fun getComments(): String? {
        return comments
    }

    fun setComments(comments: String?) {
        this.comments = comments
    }

    fun getDetailId(): String? {
        return detailId
    }

    fun setDetailId(detailId: String?) {
        this.detailId = detailId
    }



    fun getMode(): String? {
        return mode
    }

    fun setMode(mode: String?) {
        this.mode = mode
    }

    fun getLogNo2(): String? {
        return logNo2
    }

    fun setLogNo2(logNo2: String?) {
        this.logNo2 = logNo2
    }
    fun getBarcodeNumber(): String {
        return barcodeNumber
    }

    fun setBarcodeNumber(barcodeNumber: String) {
        this.barcodeNumber = barcodeNumber
    }

    fun getSawnAssignmentId(): String? {
        return sawnAssignmentId
    }

    fun setSawnAssignmentId(sawnAssignmentId: String?) {
        this.sawnAssignmentId = sawnAssignmentId
    }


    fun getBordereauHeaderId(): Int? {
        return bordereauHeaderId
    }

    fun setBordereauHeaderId(bordereauHeaderId: Int?) {
        this.bordereauHeaderId = bordereauHeaderId
    }

    fun getBordereauNo(): String? {
        return bordereauNo
    }

    fun setBordereauNo(bordereauNo: String?) {
        this.bordereauNo = bordereauNo
    }

    fun getEBordereauNo(): String? {
        return eBordereauNo
    }

    fun setEBordereauNo(eBordereauNo: String?) {
        this.eBordereauNo = eBordereauNo
    }

    fun getWagonNo(): String? {
        return wagonNo
    }

    fun setWagonNo(wagonNo: String?) {
        this.wagonNo = wagonNo
    }

    fun getBordereaDetailStatus(): String? {
        return bordereaDetailStatus
    }

    fun setBordereaDetailStatus(bordereaDetailStatus: String?) {
        this.bordereaDetailStatus = bordereaDetailStatus
    }


    fun getTotalQuantity(): String? {
        return totalQuantity
    }

    fun setTotalQuantity(totalQuantity: String?) {
        this.totalQuantity = totalQuantity
    }

    fun getTotalCBM(): String? {
        return totalCBM
    }

    fun setTotalCBM(totalCBM: String?) {
        this.totalCBM = totalCBM
    }

    fun getCbmQuantity(): String? {
        return cbmQuantity
    }

    fun setCbmQuantity(cbmQuantity: String?) {
        this.cbmQuantity = cbmQuantity
    }

    fun getInspectionFlag(): String? {
        return inspectionFlag
    }

    fun setInspectionFlag(inspectionFlag: String?) {
        this.inspectionFlag = inspectionFlag
    }

    fun getModuleFlag(): String? {
        return moduleFlag
    }

    fun setModuleFlag(moduleFlag: String?) {
        this.moduleFlag = moduleFlag
    }

    fun getPoDate(): String? {
        return poDate
    }

    fun setPoDate(poDate: String?) {
        this.poDate = poDate
    }

    fun getPoNumber(): String? {
        return poNumber
    }

    fun setPoNumber(poNumber: String?) {
        this.poNumber = poNumber
    }

    fun getGrnNo(): String? {
        return grnNo
    }

    fun setGrnNo(grnNo: String?) {
        this.grnNo = grnNo
    }

    fun getGrnDate(): String? {
        return grnDate
    }

    fun setGrnDate(grnDate: String?) {
        this.grnDate = grnDate
    }

    fun getOperationalDate(): String? {
        return operationalDate
    }

    fun setOperationalDate(operationalDate: String?) {
        this.operationalDate = operationalDate
    }

    fun getFinanceDate(): String? {
        return financeDate
    }

    fun setFinanceDate(financeDate: String?) {
        this.financeDate = financeDate
    }

    fun getTransactionStatus(): String? {
        return transactionStatus
    }

    fun setTransactionStatus(transactionStatus: String?) {
        this.transactionStatus = transactionStatus
    }

    fun getIsBordereauInspectionCompleted(): String? {
        return isBordereauInspectionCompleted
    }

    fun setIsBordereauInspectionCompleted(isBordereauInspectionCompleted: String?) {
        this.isBordereauInspectionCompleted = isBordereauInspectionCompleted
    }

    fun getScreen(): String? {
        return screen
    }

    fun setScreen(screen: String?) {
        this.screen = screen
    }

    fun getUserID(): String? {
        return userID
    }

    fun setUserID(userID: String?) {
        this.userID = userID
    }

    fun getcustomerPurchsedFromForest(): String? {
        return customerPurchsedFromForest
    }

    fun setcustomerPurchsedFromForest(customerPurchsedFromForest: String?) {
        this.customerPurchsedFromForest = customerPurchsedFromForest
    }

    fun getIsSelect(): Boolean? {
        return isSelect
    }

    fun setIsSelect(isSelect: Boolean?) {
        this.isSelect = isSelect
    }

    @JvmName("getUniqueId1")
    fun getUniqueId(): String? {
        return uniqueId
    }

    @JvmName("setUniqueId1")
    fun setUniqueId(uniqueId: String?) {
        this.uniqueId = uniqueId
    }

    fun getForestUnqiueId(): String? {
        return forestuniqueId
    }

    fun setForestUnqiueId(forestuniqueId: String?) {
        this.forestuniqueId = forestuniqueId
    }


    fun getFSCMode(): String? {
        return fscMode
    }

    fun setFSCMode(fscMode: String?) {
        this.fscMode = fscMode
    }






}