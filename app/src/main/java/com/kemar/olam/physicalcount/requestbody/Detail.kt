package com.kemar.olam.physicalcount.requestbody

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Detail {
    @SerializedName("barcode")
    @Expose
    var barcodeNumber: String? = null

    @SerializedName("id")
    @Expose
    var id: Int? = null


    var uniqueId: Int? = null
}