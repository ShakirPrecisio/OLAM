package com.kemar.olam.bordereau.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

class AddBoereuLogListingReq() {

    @SerializedName("userID")
    @Expose
    private var userID: Int? = null

    @SerializedName("userLocationID")
    @Expose
    var userLocationID: Int? = null


    @SerializedName("supplier")
    @Expose
    private var supplier: Int? = null

    @SerializedName("timezoneId")
    @Expose
    private var timezoneId: String? = ""

    @SerializedName("bordereauHeaderId")
    @Expose
    private var bordereauHeaderId: Int? = null

    @SerializedName("statusMode")
    @Expose
    private var statusMode: String? = null

    @SerializedName("diaType")
    @Expose
    private var diaType: String? = null

    @SerializedName("bordereauLogList")
    @Expose
    private var bordereauLogList: List<BodereuLogListing?>? = null

    //added for Confirm Billing
    @SerializedName("customerSignBase")
    @Expose
    private var customerSignBase: String? = null

    @SerializedName("representativeSignBase")
    @Expose
    private var representativeSignBase: String? = null

    ///Start FOR Confirm Billing Requesst

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

    //Added for confirm delivery
    @SerializedName("transporterId")
    @Expose
    private var transporterId: Int? = 0

    @SerializedName("truckNo")
    @Expose
    private var truckNo: Int? = 0


    //added for ground approval
    @SerializedName("inspectionId")
    @Expose
    private var inspectionId: Int? = 0

    //added for delivery confirm
    @SerializedName("deliveryId")
    @Expose
    private var deliveryId: Int? = 0





    fun getdiaType(): String? {
        return diaType
    }

    fun setdiaType(diaType: String?) {
        this.diaType = diaType
    }

    fun getDeliveryId(): Int? {
        return deliveryId
    }

    fun setDeliveryId(deliveryId: Int?) {
        this.deliveryId = deliveryId
    }

    fun getInspectionId(): Int? {
        return inspectionId
    }

    fun setInspectionId(inspectionId: Int?) {
        this.inspectionId = inspectionId
    }


    fun getTruckNo(): Int? {
        return truckNo
    }

    fun setTruckNo(truckNo: Int?) {
        this.truckNo = truckNo
    }


    fun getTransporterId(): Int? {
        return transporterId
    }

    fun setTransporterId(transporterId: Int?) {
        this.transporterId = transporterId
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

    fun getSupplier(): Int? {
        return supplier
    }

    fun setSupplier(supplier: Int?) {
        this.supplier = supplier
    }

    fun getGradeId(): String? {
        return gradeId
    }

    fun setGradeId(gradeId: String?) {
        this.gradeId = gradeId
    }

    ///END FOR Confirm Billing


    fun getCustomerSignBase(): String? {
        return customerSignBase
    }

    fun setCustomerSignBase(customerSignBase: String?) {
        this.customerSignBase = customerSignBase
    }


    fun getRepresentativeSignBase(): String? {
        return representativeSignBase
    }

    fun setRepresentativeSignBase(representativeSignBase: String?) {
        this.representativeSignBase = representativeSignBase
    }

    fun getUserID(): Int? {
        return userID
    }

    fun setUserID(userID: Int?) {
        this.userID = userID
    }


    fun getUTimezoneId(): String? {
        return timezoneId
    }

    fun setTimezoneId(timezoneId: String?) {
        this.timezoneId = timezoneId
    }



    fun getBordereauHeaderId(): Int? {
        return bordereauHeaderId
    }

    fun setBordereauHeaderId(bordereauHeaderId: Int?) {
        this.bordereauHeaderId = bordereauHeaderId
    }

    fun getBordereauLogList(): List<BodereuLogListing?>? {
        return bordereauLogList
    }

    fun setBordereauLogList(bordereauLogList: List<BodereuLogListing?>?) {
        this.bordereauLogList = bordereauLogList
    }

    fun getStatusMode(): String? {
        return statusMode;
    }

    fun setStatusMode(statusMode: String?) {
        this.statusMode = statusMode
    }



}