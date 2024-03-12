package com.kemar.olam.bordereau.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

class AddBodereuReq {
    @SerializedName("userID")
    @Expose
    private var userID: Int? = 0
    @SerializedName("supplier")
    @Expose
    private var supplier: Int? = 0
    @SerializedName("supplierLocation")
    @Expose
    private var supplierLocation: Int? = 0
    @SerializedName("modeOfTransport")
    @Expose
    private var modeOfTransport: Int? = 0
    @SerializedName("mode")
    @Expose
    private var mode: String? = null


    @SerializedName("customerName")
    @Expose
    private var customerName: String? = null


    @SerializedName("transporterId")
    @Expose
    private var transporterId: Int? = 0

    @SerializedName("transporterName")
    @Expose
    private var transporterName: String? = null


    @SerializedName("locationId")
    @Expose
    private var locationId: Int? = 0

    @SerializedName("locationName")
    @Expose
    private var locationName: String? = null

    @SerializedName("destinationId")
    @Expose
    private var destinationId: Int? = 0

    @SerializedName("destinationName")
    @Expose
    private var destinationName: String? = null


    @SerializedName("inchargeId")
    @Expose
    private var inchargeId: Int? = 0

    @SerializedName("inchargeName")
    @Expose
    private var inchargeName: String? = null



    @SerializedName("truckNo")
    @Expose
    private var truckNo: String? = null


    @SerializedName("borderearuType")
    @Expose
    private var borderearuType: String? = null


    @SerializedName("bordereauNo")
    @Expose
    private var bordereauNo: String? = null

    @SerializedName("bordereauDate")
    @Expose
    private var bordereauDate: String? = null

    @SerializedName("deliveryDate")
    @Expose
    private var deliveryDate: String? = null

    @SerializedName("timezoneId")
    @Expose
    private var timezoneId: String? = null


  /*  @SerializedName("aac_id")
    @Expose
    private var aacId: Int? = 0*/
    @SerializedName("fscId")
    @Expose
    private var fscId: Int? = 0
    @SerializedName("leauChargementId")
    @Expose
    private var leauChargementId: Int? = 0
    @SerializedName("speciesId")
    @Expose
    private var speciesId: Int? = 0
    @SerializedName("wagonId")
    @Expose
    private var wagonId: Int? = 0
    @SerializedName("wagonNo")
    @Expose
    private var wagonNo: String? = null
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

    //added for delivery management
    @SerializedName("truckId")
    @Expose
    private var truckId: Int? = null

    @SerializedName("customerId")
    @Expose
    private var customerId: Int? = null


    @SerializedName("originName")
    @Expose
    private var originName: String? = null

    @SerializedName("supplierName")
    @Expose
    private var supplierName: String? = null


    fun getSupplierName(): String? {
        return supplierName
    }

    fun setSupplierName(supplierName: String?) {
        this.supplierName = supplierName
    }

    fun getOriginName(): String? {
        return originName
    }

    fun setOriginName(originName: String?) {
        this.originName = originName
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

    fun getLeauChargementId(): Int? {
        return leauChargementId;
    }

    fun setLeauChargementId(leauChargementId: Int?) {
        this.leauChargementId = leauChargementId
    }



    fun getdeliveryDatee(): String? {
        return deliveryDate;
    }

    fun setdeliveryDate(deliveryDate: String?) {
        this.deliveryDate = deliveryDate
    }

    fun getBordereauDate(): String? {
        return bordereauDate;
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



    fun getCustomerName(): String? {
        return customerName
    }

    fun setCustomerName(customerName: String?) {
        this.customerName = customerName
    }

    fun gettransporterName(): String? {
        return transporterName
    }

    fun settranspoterName(transporterName: String?) {
        this.transporterName = transporterName
    }

    fun getLocationId(): Int? {
        return locationId
    }

    fun setLocationID(locationId: Int?) {
        this.locationId = locationId
    }

    fun getlocationName(): String? {
        return locationName
    }

    fun setlocationName(locationName: String?) {
        this.locationName = locationName
    }

    fun getdestinationId(): Int? {
        return destinationId
    }

    fun setdestinationId(destinationId: Int?) {
        this.destinationId = destinationId
    }


    fun getdestinationName(): String? {
        return destinationName
    }

    fun setdestinationName(destinationName: String?) {
        this.destinationName = destinationName
    }



    fun getinchargeId(): Int? {
        return inchargeId
    }

    fun setinchargeId(inchargeId: Int?) {
        this.inchargeId = inchargeId
    }

    fun getinchargeName(): String? {
        return inchargeName
    }

    fun setinchargeName(inchargeName: String?) {
        this.inchargeName = inchargeName
    }

    fun gettruckNo(): String? {
        return truckNo
    }

    fun settruckNo(truckNo: String?) {
        this.truckNo = truckNo
    }

    fun getborderearuType(): String? {
        return borderearuType
    }

    fun setborderearuType(borderearuType: String?) {
        this.borderearuType = borderearuType
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

    fun getTruckID(): Int? {
        return truckId
    }

    fun setTruckID(truckId: Int?) {
        this.truckId = truckId
    }


    fun getCustomerId(): Int? {
        return customerId
    }

    fun setCustomerId(customerId: Int?) {
        this.customerId = customerId
    }

    fun getDeliveryDate(): String? {
        return deliveryDate
    }

    fun setDeliveryDate(deliveryDate: String?) {
        this.deliveryDate = deliveryDate
    }


}