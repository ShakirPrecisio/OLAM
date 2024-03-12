package com.kemar.olam.origin_verification.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class OriginUserHistoryReq : Serializable  {
    @SerializedName("supplier")
    @Expose
    private var supplier: Int? = null
    @SerializedName("wagonNo")
    @Expose
    private var wagonNo: String? = null
    @SerializedName("bordereauNo")
    @Expose
    private var bordereauNo: String? = null

    @SerializedName("barcodeNumber")
    @Expose
    private var barcodeNumber: String? = null

    @SerializedName("userID")
    @Expose
    private var userID: Int? = null
    @SerializedName("isAdmin")
    @Expose
    private var isAdmin: Boolean? = null
    @SerializedName("userLocationID")
    @Expose
    private var userLocationID: Int? = null


    @SerializedName("declaredDate")
    @Expose
    private var stockDeclaredDate: String? = null


    fun getStockDeclaredDate(): String? {
        return stockDeclaredDate
    }

    fun setStockDeclaredDate(stockDeclaredDate: String?) {
        this.stockDeclaredDate = stockDeclaredDate
    }

    fun getSupplier(): Int? {
        return supplier
    }

    fun setSupplier(supplier: Int?) {
        this.supplier = supplier
    }

    fun getWagonNo(): String? {
        return wagonNo
    }

    fun setWagonNo(wagonNo: String?) {
        this.wagonNo = wagonNo
    }

    fun getBordereauNo(): String? {
        return bordereauNo
    }

    fun setBordereauNo(bordereauNo: String?) {
        this.bordereauNo = bordereauNo
    }

    fun getBarcodeNumber(): String? {
        return barcodeNumber
    }

    fun setBarcodeNumber(barcodeNumber: String?) {
        this.barcodeNumber = barcodeNumber
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