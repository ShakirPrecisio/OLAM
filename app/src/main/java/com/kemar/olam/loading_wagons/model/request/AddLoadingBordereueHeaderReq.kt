package com.kemar.olam.loading_wagons.model.request

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class AddLoadingBordereueHeaderReq :RealmObject() {

    @PrimaryKey
    var uniqueId: String? = null

    var isUpload: Boolean? = false



    @SerializedName("userID")
    @Expose
    private var userID: Int? = 0
    @SerializedName("supplier")
    @Expose
    private var supplier: Int? = null
    @SerializedName("supplierLocation")
    @Expose
    private var supplierLocation: Int? = 0
    @SerializedName("modeOfTransport")
    @Expose
    private var modeOfTransport: Int? = 0
    @SerializedName("transporterId")
    @Expose
    private var transporterId: Int? = 0
    @SerializedName("bordereauNo")
    @Expose
    private var bordereauNo: String? = ""
    @SerializedName("eBordereauNo")
    @Expose
    var eBordereauNo: String? = ""
    @SerializedName("bordereauDate")
    @Expose
    private var bordereauDate: String? = null
    @SerializedName("mode")
    @Expose
    private var mode: String? = null
    /*  @SerializedName("aac_id")
      @Expose
      private var aacId: Int? = 0*/
    @SerializedName("fscId")
    @Expose
    private var fscId: Int? = null
    @SerializedName("leauChargementId")
    @Expose
    private var leauChargementId: Int? = 0
    @SerializedName("speciesId")
    @Expose
    private var speciesId: Int? = 0
    @SerializedName("timezoneId")
    @Expose
    private var timezoneId: String? = null
    @SerializedName("wagonId")
    @Expose
    private var wagonId: Int? = 0
    @SerializedName("wagonNo")
    @Expose
    private var wagonNo :String? = null
    @SerializedName("requestReceivedDate")
    @Expose
    private var requestReceivedDate: String? = null

    @SerializedName("originID")
    @Expose
    private var originID: Int? = null

    @SerializedName("destination")
    @Expose
    private var destination: String? = null

    @SerializedName("bordereauHeaderId")
    @Expose
    private var bordereauHeaderId: Int? = null

    @SerializedName("supplierShortName")
    @Expose
    private var supplierShortName: String? = null
    @SerializedName("electronicBordereau")
    @Expose
    private var electronicBordereau: Boolean? = false

    fun getSupplierShortName(): String? {
        return supplierShortName
    }

    fun setSupplierShortName(supplierShortName: String?) {
        this.supplierShortName = supplierShortName
    }

    fun getElectronicBordereau(): Boolean? {
        return electronicBordereau
    }

    fun setElectronicBordereau(electronicBordereau: Boolean?) {
        this.electronicBordereau = electronicBordereau
    }

    fun getBordereauHeaderId(): Int? {
        return bordereauHeaderId
    }

    fun setBordereauHeaderId(bordereauHeaderId: Int?) {
        this.bordereauHeaderId = bordereauHeaderId
    }


    fun getUserID(): Int? {
        return userID
    }

    fun setUserID(userID: Int?) {
        this.userID = userID
    }

    fun getSupplier(): Int? {
        return supplier
    }

    fun setSupplier(supplier: Int?) {
        this.supplier = supplier
    }

    fun getSupplierLocation(): Int? {
        return supplierLocation
    }

    fun setSupplierLocation(supplierLocation: Int?) {
        this.supplierLocation = supplierLocation
    }

    fun getModeOfTransport(): Int? {
        return modeOfTransport
    }

    fun setModeOfTransport(modeOfTransport: Int?) {
        this.modeOfTransport = modeOfTransport
    }

    fun getTransporterID(): Int? {
        return transporterId
    }

    fun setTransporterID(transporterId: Int?) {
        this.transporterId = transporterId
    }

    fun getBordereauNo(): String? {
        return bordereauNo
    }

    fun setBordereauNo(bordereauNo: String?) {
        this.bordereauNo = bordereauNo
    }

    fun getE_BordereauNo(): String? {
        return eBordereauNo
    }

    fun setE_BordereauNo(eBordereauNo: String?) {
        this.eBordereauNo = eBordereauNo
    }

    fun getBordereauDate(): String? {
        return bordereauDate
    }

    fun setBordereauDate(bordereauDate: String?) {
        this.bordereauDate = bordereauDate
    }

    fun getMode(): String? {
        return mode
    }

    fun setMode(mode: String?) {
        this.mode = mode
    }

//    fun getAacId(): Int? {
//        return aacId
//    }
//
//    fun setAacId(aacId: Int?) {
//        this.aacId = aacId
//    }

    fun getLeauChargementId(): Int? {
        return leauChargementId
    }

    fun setLeauChargementId(leauChargementId: Int?) {
        this.leauChargementId = leauChargementId
    }



    fun getFscId(): Int? {
        return fscId
    }

    fun setFscId(fscId: Int?) {
        this.fscId = fscId
    }

    fun getSpeciesId(): Int? {
        return speciesId
    }

    fun setSpeciesId(speciesId: Int?) {
        this.speciesId = speciesId
    }

    fun getTimezoneId(): String? {
        return timezoneId
    }

    fun setTimezoneId(timezoneId: String?) {
        this.timezoneId = timezoneId
    }

    fun getWagonNo(): String? {
        return wagonNo
    }

    fun setWagonNo(wagonNo: String?) {
        this.wagonNo = wagonNo
    }

    fun getWagonId(): Int? {
        return wagonId
    }

    fun setWagonId(wagonId: Int?) {
        this.wagonId = wagonId
    }

    fun getRequestReceivedDate(): String? {
        return requestReceivedDate
    }

    fun setRequestReceivedDate(requestReceivedDate: String?) {
        this.requestReceivedDate = requestReceivedDate
    }

    fun getDestination(): String? {
        return destination
    }

    fun setDestination(destination: String?) {
        this.destination = destination
    }


    fun getOriginID(): Int? {
        return originID
    }

    fun setOriginID(originID: Int?) {
        this.originID = originID
    }
}