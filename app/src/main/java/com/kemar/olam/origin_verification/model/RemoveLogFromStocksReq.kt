package com.kemar.olam.origin_verification.model

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class RemoveLogFromStocksReq {
    @SerializedName("userID")
    @Expose
    private var userID: Int? = null
    @SerializedName("bordereauHeaderId")
    @Expose
    private var bordereauHeaderId: Int? = null
    @SerializedName("bordereauDetailIdList")
    @Expose
    private var bordereauDetailIdList: List<Int?>? = null

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

    fun getBordereauDetailIdList(): List<Int?>? {
        return bordereauDetailIdList
    }

    fun setBordereauDetailIdList(bordereauDetailIdList: List<Int?>?) {
        this.bordereauDetailIdList = bordereauDetailIdList
    }
}