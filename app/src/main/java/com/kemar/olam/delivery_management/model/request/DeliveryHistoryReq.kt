package com.kemar.olam.delivery_management.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import java.io.Serializable


class DeliveryHistoryReq : Serializable {
    @SerializedName("supplierId")
    @Expose
    private var supplierId: Int? = null
    @SerializedName("userID")
    @Expose
    private var userID: Int? = null
    @SerializedName("userLocationID")
    @Expose
    private var userLocationID: Int? = null
    @SerializedName("customerId")
    @Expose
    private var customerId: Int? = null
    @SerializedName("isAdmin")
    @Expose
    private var isAdmin: Boolean? = null
    @SerializedName("bordereauNo")
    @Expose
    private var bordereauNo: String? = null


    fun getBordereauNo(): String? {
        return bordereauNo
    }

    fun setBordereauNo(bordereauNo: String?) {
        this.bordereauNo = bordereauNo
    }

    fun getSupplier(): Int? {
        return supplierId
    }

    fun setSupplier(supplierId: Int?) {
        this.supplierId = supplierId
    }

    fun getUserID(): Int? {
        return userID
    }

    fun setUserID(userID: Int?) {
        this.userID = userID
    }

    fun getUserLocationID(): Int? {
        return userLocationID
    }

    fun setUserLocationID(userLocationID: Int?) {
        this.userLocationID = userLocationID
    }

    fun getCustomerId(): Int? {
        return customerId
    }

    fun setCustomerId(customerId: Int?) {
        this.customerId = customerId
    }

    fun getIsAdmin(): Boolean? {
        return isAdmin
    }

    fun setIsAdmin(isAdmin: Boolean?) {
        this.isAdmin = isAdmin
    }

}