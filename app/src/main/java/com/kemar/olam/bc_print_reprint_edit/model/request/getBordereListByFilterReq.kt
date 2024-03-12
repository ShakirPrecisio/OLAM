package com.kemar.olam.bc_print_reprint_edit.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class getBordereListByFilterReq : Serializable{
    @SerializedName("supplier")
    @Expose
    private var supplier: Int? = 0
    @SerializedName("bordereauDate")
    @Expose
    private var bordereauDate: String? = ""
    @SerializedName("bordereauNo")
    @Expose
    private var bordereauNo: String? = ""
    @SerializedName("logNo")
    @Expose
    private var logNo: String? = ""
    @SerializedName("userID")
    @Expose
    private var userID: Int? = 0
    @SerializedName("isAdmin")
    @Expose
    private var isAdmin: Boolean? = false
    @SerializedName("userLocationID")
    @Expose
    private var userLocationID: Int? = 0

    fun getSupplier(): Int? {
        return supplier
    }

    fun setSupplier(supplier: Int?) {
        this.supplier = supplier
    }

    fun getBordereauDate(): String? {
        return bordereauDate
    }

    fun setBordereauDate(bordereauDate: String?) {
        this.bordereauDate = bordereauDate
    }

    fun getBordereauNo(): String? {
        return bordereauNo
    }

    fun setBordereauNo(bordereauNo: String?) {
        this.bordereauNo = bordereauNo
    }

    fun getLogNo(): String? {
        return logNo
    }

    fun setLogNo(logNo: String?) {
        this.logNo = logNo
    }

    fun getUserID(): Int? {
        return userID
    }

    fun setUserID(userID: Int?) {
        this.userID = userID
    }

    fun getIsAdmin(): Boolean? {
        return isAdmin
    }

    fun setIsAdmin(isAdmin: Boolean?) {
        this.isAdmin = isAdmin
    }

    fun getUserLocationID(): Int? {
        return userLocationID
    }

    fun setUserLocationID(userLocationID: Int?) {
        this.userLocationID = userLocationID
    }
}