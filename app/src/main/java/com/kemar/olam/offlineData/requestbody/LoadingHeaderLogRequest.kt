package com.kemar.olam.offlineData.requestbody

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.loading_wagons.model.request.AddLoadingBordereueHeaderReq

class LoadingHeaderLogRequest {

    @SerializedName("bordereauHeader")
    @Expose
    private var addLoadingBordereueHeaderReq: AddLoadingBordereueHeaderReq? = null



    @SerializedName("createLogRequest")
    @Expose
    private var addBoereuLogListingReq: AddBoereuLogListingReq? = null


    fun getAddBodereuReq(): AddLoadingBordereueHeaderReq? {
        return addLoadingBordereueHeaderReq
    }

    fun setAddBodereuReq(addBodereuReq: AddLoadingBordereueHeaderReq?) {
        this.addLoadingBordereueHeaderReq = addBodereuReq
    }

    fun getAddBoereuLogListingReq(): AddBoereuLogListingReq? {
        return addBoereuLogListingReq
    }

    fun setAddBoereuLogListingReq(addBoereuLogListingReq:  AddBoereuLogListingReq?) {
        this.addBoereuLogListingReq = addBoereuLogListingReq
    }





}