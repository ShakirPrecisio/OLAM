package com.kemar.olam.delivery_management.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class AddLogReq {
    @SerializedName("deliveryId")
    @Expose
    private var deliveryId: Int? = null
    @SerializedName("bordereauDetailId")
    @Expose
    private var bordereauDetailId: Int? = null

    fun getDeliveryId(): Int? {
        return deliveryId
    }

    fun setDeliveryId(deliveryId: Int?) {
        this.deliveryId = deliveryId
    }

    fun getBordereauDetailId(): Int? {
        return bordereauDetailId
    }

    fun setBordereauDetailId(bordereauDetailId: Int?) {
        this.bordereauDetailId = bordereauDetailId
    }
}