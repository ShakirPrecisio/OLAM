package com.kemar.olam.physicalcount.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
class Detail {
    @SerializedName("id")
    @Expose
    var id: Int? = null

    @SerializedName("barcode")
    @Expose
    var barcode: String? = null
}