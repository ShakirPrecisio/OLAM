package com.kemar.olam.delivery_management.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

class RevokeLogsReq {
    @SerializedName("comments")
    @Expose
    private var comments: String? = ""
    @SerializedName("detailIds")
    @Expose
    private var detailIds: List<Int?>? = null
    @SerializedName("userID")
    @Expose
    private var userID: Int? = null

    fun getComments(): String? {
        return comments
    }

    fun setComments(comments: String?) {
        this.comments = comments
    }

    fun getDetailIds(): List<Int?>? {
        return detailIds
    }

    fun setDetailIds(detailIds: List<Int?>?) {
        this.detailIds = detailIds
    }

    fun getUserID(): Int? {
        return userID
    }

    fun setUserID(userID: Int?) {
        this.userID = userID
    }
}