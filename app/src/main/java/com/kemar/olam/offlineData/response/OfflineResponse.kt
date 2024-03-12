package com.lp.lpwms.ui.offline.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.kemar.olam.offlineData.response.LeauChargementUserDatum
import com.kemar.olam.offlineData.response.MoreSupplierInfo

class OfflineResponse  {
    @SerializedName("severity")
    @Expose
    var severity: Int? = null

    @SerializedName("transportModeData")
    @Expose
    var transportModeData: List<TransportModeDatum>? = null

    @SerializedName("supplierData")
    @Expose
    var supplierData: List<SupplierDatum>? = null

    @SerializedName("vehicleData")
    @Expose
    var vehicleData: List<VehicleDatum>? = null

    @SerializedName("errorMessage")
    @Expose
    var errorMessage: String? = null

    @SerializedName("originMaster")
    @Expose
    var originMaster: List<OriginMaster>? = null

    @SerializedName("customerData")
    @Expose
    var customerData: List<CustomerDatum>? = null

    @SerializedName("message")
    @Expose
    var message: String? = null

    @SerializedName("transporterData")
    @Expose
    var transporterData: List<TransporterDatum>? = null

    @SerializedName("leauChargementData")
    @Expose
    var leauChargementData: List<LeauChargementDatum>? = null

    @SerializedName("leauChargementDataUser")
    @Expose
    var leauChargementUserData: List<LeauChargementUserDatum>? = null

    @SerializedName("fscMasterData")
    @Expose
    var fscMasterData: List<FscMasterDatum>? = null

    @SerializedName("qualityData")
    @Expose
    var qualityData: List<QualityDatum>? = null

    @SerializedName("species")
    @Expose
    var species: List<Species>? = null


    @SerializedName("moreSupplierInfo")
    @Expose
    var moreSupplierInfo: List<MoreSupplierInfo>? = null

    @SerializedName("aacList")
    @Expose
    var aacList: List<Aac>? = null

    @SerializedName("stackTrace")
    @Expose
    var stackTrace: String? = null

    @SerializedName("status")
    @Expose
    var status: String? = null
}