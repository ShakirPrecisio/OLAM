package com.kemar.olam.offlineData.requestbody

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.kemar.olam.bordereau.model.request.AddBodereuReq
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.bordereau.model.request.BodereuLogListing

class BorderauHeaderLogRequest() {

    @SerializedName("bordereauHeader")
    @Expose
    private var addBodereuReq: AddBodereuReq? = null

    @SerializedName("createLogRequest")
    @Expose
    private var addBoereuLogListingReq: AddBoereuLogListingReq? = null

    constructor(addBodereuReq: AddBodereuReq?, addBoereuLogListingReq: AddBoereuLogListingReq?) : this() {
        this.addBodereuReq = addBodereuReq
        this.addBoereuLogListingReq = addBoereuLogListingReq
    }



    fun getAddBodereuReq(): AddBodereuReq? {
        return addBodereuReq
    }

    fun setAddBodereuReq(addBodereuReq: AddBodereuReq?) {
        this.addBodereuReq = addBodereuReq
    }

    fun getAddBoereuLogListingReq(): AddBoereuLogListingReq? {
        return addBoereuLogListingReq
    }

    fun setAddBoereuLogListingReq(addBoereuLogListingReq:  AddBoereuLogListingReq?) {
        this.addBoereuLogListingReq = addBoereuLogListingReq
    }






}