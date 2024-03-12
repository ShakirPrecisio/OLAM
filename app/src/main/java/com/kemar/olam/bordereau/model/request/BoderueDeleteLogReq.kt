package com.kemar.olam.bordereau.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class BoderueDeleteLogReq {
    @SerializedName("deleteDetailId")
    @Expose
    private var deleteDetailId: Int? = null

    //added for delete sales inspection logs
    @SerializedName("detailId")
    @Expose
    private var detailId: Int? = null

    //added for reprint sales inspection PDf
    @SerializedName("inspectionNumber")
    @Expose
    private var inspectionNumber: String? = null


    @SerializedName("userID")
    @Expose
    private var userID: Int? = null
    @SerializedName("bordereauHeaderId")
    @Expose
    private var bordereauHeaderId: Int? = null


    fun getInspectionNumber(): String? {
        return inspectionNumber
    }

    fun setInspectionNumber(inspectionNumber: String?) {
        this.inspectionNumber = inspectionNumber
    }


    fun getDetailId(): Int? {
        return detailId
    }

    fun setDetailId(detailId: Int?) {
        this.detailId = detailId
    }


    fun getDeleteDetailId(): Int? {
        return deleteDetailId
    }

    fun setDeleteDetailId(deleteDetailId: Int?) {
        this.deleteDetailId = deleteDetailId
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
}