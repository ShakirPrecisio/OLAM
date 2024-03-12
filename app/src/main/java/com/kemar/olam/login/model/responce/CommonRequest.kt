package com.kemar.olam.login.model.responce

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class CommonRequest {
    @SerializedName("userLocationID")
    @Expose
    private var userLocationID: String? = null

    @SerializedName("deleteDetailId")
    @Expose
    private var deleteDetailId: String? = null

    @SerializedName("transportModeID")
    @Expose
    private var transportModeID: String? = null

    @SerializedName("logNo")
    @Expose
    private var logNo: String? = null

    @SerializedName("userID")
    @Expose
    private var userID: String? = null

    @SerializedName("isAdmin")
    @Expose
    private var isAdmin: Boolean? = null


    @SerializedName("headerId")
    @Expose
    private var headerId: String? = null


    @SerializedName("loadingStatus")
    @Expose
    private var loadingStatus: String? = null


    @SerializedName("forestID")
    @Expose
    private var forestID: String? = null


    @SerializedName("originID")
    @Expose
    private var originID: String? = null


    @SerializedName("barcodeNumber")
    @Expose
    private var barcodeNumber: String? = null


    @SerializedName("bordereauHeaderId")
    @Expose
    private var bordereauHeaderId: String? = null


    @SerializedName("bordereauDate")
    @Expose
    private var bordereauDate: String? = null

    @SerializedName("inspectionId")
    @Expose
    private var inspectionId: String? = null

    @SerializedName("customerId")
    @Expose
    private var customerId: String? = null


    @SerializedName("deliveryId")
    @Expose
    private var deliveryId: String? = null


    @SerializedName("pdfFilePath")
    @Expose
    private var pdfFilePath: String? = null


    @SerializedName("leauChargementId")
    @Expose
    public var leauChargementId: Int? = null

    @SerializedName("username")
    @Expose
    public var userName: String? = null

    @SerializedName("size")
    @Expose
    public var size: Int? = null

    @SerializedName("page")
    @Expose
    public var page: Int? = null


    fun getPdfFilePath(): String? {
        return pdfFilePath
    }

    fun setPdfFilePath(pdfFilePath: String?) {
        this.pdfFilePath = pdfFilePath
    }


    fun getDeliveryId(): String? {
        return deliveryId
    }

    fun setDeliveryId(deliveryId: String?) {
        this.deliveryId = deliveryId
    }

    fun getCustomerId(): String? {
        return customerId
    }

    fun setCustomerId(customerId: String?) {
        this.customerId = customerId
    }


    fun getInspectionId(): String? {
        return inspectionId
    }

    fun setInspectionId(inspectionId: String?) {
        this.inspectionId = inspectionId
    }

    fun getBordereauDate(): String? {
        return bordereauDate
    }

    fun setBordereauDate(bordereauDate: String?) {
        this.bordereauDate = bordereauDate
    }


    fun getBordereauHeaderId(): String? {
        return bordereauHeaderId
    }

    fun setBordereauHeaderId(bordereauHeaderId: String?) {
        this.bordereauHeaderId = bordereauHeaderId
    }


    fun getBarcodeNumber(): String? {
        return barcodeNumber
    }

    fun setBarcodeNumber(barcodeNumber: String?) {
        this.barcodeNumber = barcodeNumber
    }


    fun getOriginId(): String? {
        return originID
    }

    fun setOriginId(originID: String?) {
        this.originID = originID
    }



    fun getForestId(): String? {
        return forestID
    }

    fun setForestId(forestID: String?) {
        this.forestID = forestID
    }




    fun getHeaderId(): String? {
        return headerId
    }

    fun setHeaderId(headerId: String?) {
        this.headerId = headerId
    }

    fun getloadingStatus(): String? {
        return loadingStatus
    }

    fun setloadingStatus(loadingStatus: String?) {
        this.loadingStatus = loadingStatus
    }


    fun getIsAdmin(): Boolean? {
        return isAdmin
    }

    fun setIsAdmin(isAdmin: Boolean?) {
        this.isAdmin = isAdmin
    }


    fun getUserId(): String? {
        return userID
    }

    fun setUserId(userID: String?) {
        this.userID = userID
    }



    fun getLogNo(): String? {
        return logNo
    }

    fun setLogNo(logNo: String?) {
        this.logNo = logNo
    }




    fun getDeleteDetailId(): String? {
        return deleteDetailId
    }

    fun setDeleteDetailId(deleteDetailId: String?) {
        this.deleteDetailId = deleteDetailId
    }

    fun getUserLocationId(): String? {
        return userLocationID
    }

    fun setUserLocationId(userLocationID: String?) {
        this.userLocationID = userLocationID
    }

    fun geTransportModeID(): String? {
        return transportModeID
    }

    fun seTransportModeID(transportModeID: String?) {
        this.transportModeID = transportModeID
    }


}