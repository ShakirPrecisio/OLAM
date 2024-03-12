package com.kemar.olam.loading_wagons.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class getBorderuaSerialNoReq {
    @SerializedName("bordereauDate")
    @Expose
    private var bordereauDate: String? = null
    @SerializedName("supplier")
    @Expose
    private var supplier: Int? = null

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