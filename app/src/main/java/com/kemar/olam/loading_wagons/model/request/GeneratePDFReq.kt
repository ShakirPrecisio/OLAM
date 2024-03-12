package com.kemar.olam.loading_wagons.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

class GeneratePDFReq {
    @SerializedName("bordereauHeaderIdList")
    @Expose
    private var bordereauHeaderIdList: List<Int?>? = null

    @SerializedName("isGSEBOrForestDeclaration")
    @Expose
    private var isGSEBOrForestDeclaration:Boolean = false

    @SerializedName("bordereauHeaderId")
    @Expose
    private var bordereauHeaderId: String? = null

    @SerializedName("intOfflineFlag")
    @Expose
    private var intOfflineFlag: Int? = null


    fun getBordereauHeaderId(): String? {
        return bordereauHeaderId
    }

    fun setBordereauHeaderId(bordereauHeaderId: String) {
        this.bordereauHeaderId = bordereauHeaderId
    }


    fun isGSEBOrForestDeclaration():Boolean {
        return isGSEBOrForestDeclaration
    }

    fun setIsGSEBOrForestDeclaration(isGSEBOrForestDeclaration: Boolean) {
        this.isGSEBOrForestDeclaration = isGSEBOrForestDeclaration
    }

    fun getBordereauHeaderIdList(): List<Int?>? {
        return bordereauHeaderIdList
    }

    fun setBordereauHeaderIdList(bordereauHeaderIdList: List<Int?>?) {
        this.bordereauHeaderIdList = bordereauHeaderIdList
    }

    fun getIntOfflineFlag(): Int? {
        return intOfflineFlag
    }

    fun setIntOfflineFlag(intOfflineFlag: Int?) {
        this.intOfflineFlag = intOfflineFlag
    }

}