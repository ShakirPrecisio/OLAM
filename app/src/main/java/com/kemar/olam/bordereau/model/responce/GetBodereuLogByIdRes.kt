package com.kemar.olam.bordereau.model.responce

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import com.kemar.olam.bordereau.model.request.BodereuLogListing


class GetBodereuLogByIdRes {

    @SerializedName("bordereauHeaderIdList")
    @Expose
    private var bordereauHeaderIdList: String? = null

    @SerializedName("timezoneId")
    @Expose
    private val timezoneId: String? = null
    @SerializedName("statusMode")
    @Expose
    private val statusMode: String? = null

    @SerializedName("bordereauLogList")
    @Expose
    private var bordereauLogList: List<BodereuLogListing>? = null
    @SerializedName("userID")
    @Expose
    private var userID: Int? = null
    @SerializedName("bordereauHeaderId")
    @Expose
    private var bordereauHeaderId: Int? = null
    @SerializedName("deleteDetailId")
    @Expose
    private var deleteDetailId: String? = null

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
    @SerializedName("selectedLogs")
    @Expose
    private var selectedLogs: Int? = null
    @SerializedName("resizedLogs")
    @Expose
    private var resizedLogs: Int? = null
    @SerializedName("rejectedLogs")
    @Expose
    private var rejectedLogs: Int? = null
    @SerializedName("totalLogs")
    @Expose
    private var totalLogs: Int? = null
    @SerializedName("inspectionDate")
    @Expose
    private var inspectionDate: String? = null
    @SerializedName("inspectionNumber")
    @Expose
    private var inspectionNumber: String? = null
    @SerializedName("customerId")
    @Expose
    private var customerId: String? = null
    @SerializedName("gradeId")
    @Expose
    private var gradeId: String? = null


    @SerializedName("customerPurchasedFromForest")
    @Expose
    private var customerPurchasedFromForest: String? = null

    fun getBordereauLogList(): List<BodereuLogListing>? {
        return bordereauLogList
    }

    fun setBordereauLogList(bordereauLogList: List<BodereuLogListing>?) {
        this.bordereauLogList = bordereauLogList
    }

    fun getUserID(): Int? {
        return userID
    }

    fun setUserID(userID: Int?) {
        this.userID = userID
    }

    fun getBordereauHeaderId(): Int? {
        return bordereauHeaderId
    }

    fun setBordereauHeaderId(bordereauHeaderId: Int?) {
        this.bordereauHeaderId = bordereauHeaderId
    }

    fun getDeleteDetailId(): String? {
        return deleteDetailId
    }

    fun setDeleteDetailId(deleteDetailId: String?) {
        this.deleteDetailId = deleteDetailId
    }

    fun getBordereauHeaderIdList(): String? {
        return bordereauHeaderIdList
    }

    fun setBordereauHeaderIdList(bordereauHeaderIdList: String?) {
        this.bordereauHeaderIdList = bordereauHeaderIdList
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

    fun getSelectedLogs(): Int? {
        return selectedLogs
    }

    fun setSelectedLogs(selectedLogs: Int?) {
        this.selectedLogs = selectedLogs
    }

    fun getResizedLogs(): Int? {
        return resizedLogs
    }

    fun setResizedLogs(resizedLogs: Int?) {
        this.resizedLogs = resizedLogs
    }

    fun getRejectedLogs(): Int? {
        return rejectedLogs
    }

    fun setRejectedLogs(rejectedLogs: Int?) {
        this.rejectedLogs = rejectedLogs
    }

    fun getTotalLogs(): Int? {
        return totalLogs
    }

    fun setTotalLogs(totalLogs: Int?) {
        this.totalLogs = totalLogs
    }

    fun getInspectionDate(): String? {
        return inspectionDate
    }

    fun setInspectionDate(inspectionDate: String?) {
        this.inspectionDate = inspectionDate
    }

    fun getInspectionNumber(): String? {
        return inspectionNumber
    }

    fun setInspectionNumber(inspectionNumber: String?) {
        this.inspectionNumber = inspectionNumber
    }

    fun getCustomerId(): String? {
        return customerId
    }

    fun setCustomerId(customerId: String?) {
        this.customerId = customerId
    }

    fun getGradeId(): String? {
        return gradeId
    }

    fun setGradeId(gradeId: String?) {
        this.gradeId = gradeId
    }

    fun getcustomerPurchasedFromForest(): String? {
        return customerPurchasedFromForest
    }

    fun setcustomerPurchasedFromForest(customerPurchasedFromForest: String?) {
        this.customerPurchasedFromForest = customerPurchasedFromForest
    }
}