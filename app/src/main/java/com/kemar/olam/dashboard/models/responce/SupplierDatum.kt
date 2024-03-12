package com.kemar.olam.dashboard.models.responce

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class SupplierDatum : Serializable {
    @SerializedName("optionValue")
    @Expose
    var optionValue: Int? = 0
    @SerializedName("optionName")
    @Expose
    var optionName: String? = null
    @SerializedName("optionValueString")
    @Expose
    var optionValueString: String? = null
    @SerializedName("bordereauNo")
    @Expose
    var bordereauNo: String? = null

    @SerializedName("finalDestination")
    @Expose
    var finalDestination: String? = null
}