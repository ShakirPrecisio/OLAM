package com.kemar.olam.bordereau.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class ValidateBodereueNoReq {
    @SerializedName("bordereauNo")
    @Expose
    private var bordereauNo: String? = null
    @SerializedName("bordereauDate")
    @Expose
    private var bordereauDate: String? = null
    @SerializedName("supplier")
    @Expose
    private var supplier: Int? = null
    @SerializedName("logRecordDocNo")
    @Expose
    private var logRecordDocNo: String? = null
    @SerializedName("bordereauHeaderId")
    @Expose
    private var bordereauHeaderId: String? = null

    fun getLogRecordDocNo(): String? {
        return logRecordDocNo
    }

    fun setLogRecordDocNo(logRecordDocNo: String?) {
        this.logRecordDocNo = logRecordDocNo
    }

    fun getBordereauHeaderId(): String? {
        return bordereauHeaderId
    }

    fun setBordereauHeaderId(bordereauHeaderId: String?) {
        this.bordereauHeaderId = bordereauHeaderId
    }


    fun getBordereauNo(): String? {
        return bordereauNo
    }

    fun setBordereauNo(bordereauNo: String?) {
        this.bordereauNo = bordereauNo
    }

    fun getBordereauDate(): String? {
        return bordereauDate
    }

    fun setBordereauDate(bordereauDate: String?) {
        this.bordereauDate = bordereauDate
    }

    fun getSupplier(): Int? {
        return supplier
    }

    fun setSupplier(supplier: Int?) {
        this.supplier = supplier
    }
}