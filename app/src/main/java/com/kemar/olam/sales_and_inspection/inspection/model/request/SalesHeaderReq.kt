package com.kemar.olam.sales_and_inspection.inspection.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class SalesHeaderReq {
    @SerializedName("bordereauHeaderId")
    @Expose
    private var bordereauHeaderId: Int? = null
    @SerializedName("customerId")
    @Expose
    private var customerId: Int? = null
    @SerializedName("responsibleCustomerId")
    @Expose
    private var responsibleCustomerId: Int? = null
    @SerializedName("responsibleCustomer")
    @Expose
    private var responsibleCustomer: String? = null

    @SerializedName("gradeId")
    @Expose
    private var gradeId: Int? = null

    @SerializedName("parcId")
    @Expose
    private var parcId: Int? = null

    @SerializedName("graderId")
    @Expose
    private var graderId: Int? = null
    @SerializedName("tracerCherges")
    @Expose
    private var tracerCherges: String? = null
    @SerializedName("price")
    @Expose
    private var price: String? = null
    @SerializedName("refraction")
    @Expose
    private var refraction: String? = null
    @SerializedName("inspectionDate")
    @Expose
    private var inspectionDate: String? = null
    @SerializedName("inspectionNO")
    @Expose
    private var inspectionNO: String? = null
    @SerializedName("userID")
    @Expose
    private var userID: Int? = null
    @SerializedName("timezoneId")
    @Expose
    private var timezoneId: String? = null
    @SerializedName("userLocationID")
    @Expose
    private var userLocationID: Int? = null
    @SerializedName("isAdmin")
    @Expose
    private var isAdmin: Boolean? = null

    fun getBordereauHeaderId(): Int? {
        return bordereauHeaderId
    }

    fun setBordereauHeaderId(bordereauHeaderId: Int?) {
        this.bordereauHeaderId = bordereauHeaderId
    }

    fun getCustomerId(): Int? {
        return customerId
    }

    fun setCustomerId(customerId: Int?) {
        this.customerId = customerId
    }

    fun getResponsibleCustomerId(): Int? {
        return responsibleCustomerId
    }

    fun setResponsibleCustomerId(responsibleCustomerId: Int?) {
        this.responsibleCustomerId = responsibleCustomerId
    }

    fun getResponsibleCustomer(): String? {
        return responsibleCustomer
    }

    fun setResponsibleCustomer(responsibleCustomer: String?) {
        this.responsibleCustomer = responsibleCustomer
    }



    fun getGradeId(): Int? {
        return gradeId
    }

    fun setGradeId(gradeId: Int?) {
        this.gradeId = gradeId
    }


    fun getParcId(): Int? {
        return parcId
    }

    fun setParcId(parcId: Int?) {
        this.parcId = parcId
    }



    fun getGraderId(): Int? {
        return graderId
    }

    fun setGraderId(graderId: Int?) {
        this.graderId = graderId
    }

    fun getTracerCherges(): String? {
        return tracerCherges
    }

    fun setTracerCherges(tracerCherges: String?) {
        this.tracerCherges = tracerCherges
    }

    fun getPrice(): String? {
        return price
    }

    fun setPrice(price: String?) {
        this.price = price
    }

    fun getRefraction(): String? {
        return refraction
    }

    fun setRefraction(refraction: String?) {
        this.refraction = refraction
    }

    fun getInspectionDate(): String? {
        return inspectionDate
    }

    fun setInspectionDate(inspectionDate: String?) {
        this.inspectionDate = inspectionDate
    }

    fun getInspectionNO(): String? {
        return inspectionNO
    }

    fun setInspectionNO(inspectionNO: String?) {
        this.inspectionNO = inspectionNO
    }

    fun getUserID(): Int? {
        return userID
    }

    fun setUserID(userID: Int?) {
        this.userID = userID
    }

    fun getTimezoneId(): String? {
        return timezoneId
    }

    fun setTimezoneId(timezoneId: String?) {
        this.timezoneId = timezoneId
    }

    fun getUserLocationID(): Int? {
        return userLocationID
    }

    fun setUserLocationID(userLocationID: Int?) {
        this.userLocationID = userLocationID
    }

    fun getIsAdmin(): Boolean? {
        return isAdmin
    }

    fun setIsAdmin(isAdmin: Boolean?) {
        this.isAdmin = isAdmin
    }
}