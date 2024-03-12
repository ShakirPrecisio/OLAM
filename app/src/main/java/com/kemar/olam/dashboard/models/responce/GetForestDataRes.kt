package com.kemar.olam.dashboard.models.responce

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class GetForestDataRes {
    @SerializedName("supplierData")
    @Expose
    private var supplierData: SupplierData? = null

    @SerializedName("supplierLocationData")
    @Expose
    private var supplierLocationData: List<SupplierDatum?>? = null
    @SerializedName("transportModeData")
    @Expose
    private var transportModeData: String? = null
    @SerializedName("transporterData")
    @Expose
    private var transporterData: List<SupplierDatum?>? = null
    @SerializedName("forestData")
    @Expose
    private var forestData: String? = null
    @SerializedName("species")
    @Expose
    private var species: List<SupplierDatum?>? = null

    @SerializedName("originMaster")
    @Expose
    private var originMaster: List<SupplierDatum?>? = null

    @SerializedName("destinationList")
    @Expose
    var destinationList: List<SupplierDatum?>? = null

    @SerializedName("qualityData")
    @Expose
    private var qualityData: List<SupplierDatum?>? = null

    @SerializedName("vehicleData")
    @Expose
    private var vehicleData: List<SupplierDatum?>? = null

    @SerializedName("transDistanceData")
    @Expose
    private var transDistanceData: List<SupplierDatum?>? = null

    @SerializedName("aacList")
    @Expose
    private var aacList: List<SupplierDatum?>? = null

    @SerializedName("leauChargement")
    @Expose
    private var leauChargementData: List<SupplierDatum?>? = null

    @SerializedName("leauChargementUser")
    @Expose
    private var leauChargementUserData: List<SupplierDatum?>? = null

    @SerializedName("gradeData")
    @Expose
    private var gradeData: List<SupplierDatum?>? = null


    @SerializedName("graderData")
    @Expose
    private var graderData: List<SupplierDatum?>? = null


    @SerializedName("parcData")
    @Expose
    private var parcData: List<SupplierDatum?>? = null



    @SerializedName("customerData")
    @Expose
    private var customerData: List<SupplierDatum?>? = null


    @SerializedName("fscMasterData")
    @Expose
    private var fscMasterData: List<SupplierDatum?>? = null

    @SerializedName("status")
    @Expose
    private var status: String? = null
    @SerializedName("message")
    @Expose
    private var message: String? = null
    @SerializedName("severity")
    @Expose
    private var severity: Int? = null
    @SerializedName("errorMessage")
    @Expose
    private var errorMessage: String? = null
    @SerializedName("stackTrace")
    @Expose
    private var stackTrace: String? = null

    //added for delivery Header
    @SerializedName("supplier")
    @Expose
    private var supplier: List<SupplierDatum?>? = null



    fun getSupplier(): List<SupplierDatum?>? {
        return supplier
    }

    fun setSupplier(supplier: List<SupplierDatum?>?) {
        this.supplier = supplier
    }



    fun getSupplierData(): SupplierData? {
        return supplierData
    }

    fun setSupplierData(supplierData: SupplierData?) {
        this.supplierData = supplierData
    }

    fun getSupplierLocationData(): List<SupplierDatum?>? {
        return supplierLocationData
    }

    fun setSupplierLocationData(supplierLocationData: List<SupplierDatum?>?) {
        this.supplierLocationData = supplierLocationData
    }

    fun getTransportModeData(): String? {
        return transportModeData
    }

    fun setTransportModeData(transportModeData: String?) {
        this.transportModeData = transportModeData
    }

    fun getTransporterData(): List<SupplierDatum?>? {
        return transporterData
    }

    fun setTransporterData(transporterData:  List<SupplierDatum?>?) {
        this.transporterData = transporterData
    }

    fun getForestData(): String? {
        return forestData
    }

    fun setForestData(forestData: String?) {
        this.forestData = forestData
    }



    fun getFscMasterData(): List<SupplierDatum?>? {
        return fscMasterData
    }

    fun setFscMasterData(fscMasterData: List<SupplierDatum?>?) {
        this.fscMasterData = fscMasterData
    }


    fun getLeauChargementData(): List<SupplierDatum?>? {
        return leauChargementData
    }

    fun setLeauChargementData(leauChargementData: List<SupplierDatum?>?) {
        this.leauChargementData = leauChargementData
    }


    fun getLeauChargementUserData(): List<SupplierDatum?>? {
        return leauChargementUserData
    }

    fun setLeauChargementUserData(leauChargementUserData: List<SupplierDatum?>?) {
        this.leauChargementUserData = leauChargementUserData
    }

    fun getGradeData(): List<SupplierDatum?>? {
        return gradeData
    }

    fun setGradeData(gradeData: List<SupplierDatum?>?) {
        this.gradeData = gradeData
    }

    fun getGraderData(): List<SupplierDatum?>? {
        return graderData
    }

    fun setGraderData(gradeData: List<SupplierDatum?>?) {
        this.graderData = graderData
    }

    fun getParcData(): List<SupplierDatum?>? {
        return parcData
    }

    fun setParcData(parcData: List<SupplierDatum?>?) {
        this.parcData = parcData
    }


    fun getCCustomerData(): List<SupplierDatum?>? {
        return customerData
    }

    fun setCCustomerData(customerData: List<SupplierDatum?>?) {
        this.customerData = customerData
    }




    fun getTransDistanceData(): List<SupplierDatum?>? {
        return transDistanceData
    }

    fun setTransDistanceData(transDistanceData: List<SupplierDatum?>?) {
        this.transDistanceData = transDistanceData
    }


    fun getVehicleData(): List<SupplierDatum?>? {
        return vehicleData
    }

    fun setVehicleData(vehicleData: List<SupplierDatum?>?) {
        this.vehicleData = vehicleData
    }

    fun getOriginMaster(): List<SupplierDatum?>? {
        return originMaster
    }

    fun setOriginMaste(specieoriginMasters: List<SupplierDatum?>?) {
        this.originMaster = originMaster
    }

    fun getQualityData(): List<SupplierDatum?>? {
        return qualityData
    }

    fun setQualityData(qualityData: List<SupplierDatum?>?) {
        this.qualityData = qualityData
    }
    fun getSpecies(): List<SupplierDatum?>? {
        return species
    }

    fun setSpecies(species: List<SupplierDatum?>?) {
        this.species = species
    }

    fun getAacList(): List<SupplierDatum?>? {
        return aacList
    }

    fun setAacList(aacList: List<SupplierDatum?>?) {
        this.aacList = aacList
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String?) {
        this.status = status
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?) {
        this.message = message
    }

    fun getSeverity(): Int? {
        return severity
    }

    fun setSeverity(severity: Int?) {
        this.severity = severity
    }

    fun getErrorMessage(): String? {
        return errorMessage
    }

    fun setErrorMessage(errorMessage: String?) {
        this.errorMessage = errorMessage
    }

    fun getStackTrace(): String? {
        return stackTrace
    }

    fun setStackTrace(stackTrace: String?) {
        this.stackTrace = stackTrace
    }


    class SupplierData {
        @SerializedName("supplierId")
        @Expose
        var supplierId: Int? = null
        @SerializedName("supplierShortName")
        @Expose
        var supplierShortName: String? = null
        @SerializedName("supplierName")
        @Expose
        var supplierName: String? = null
        @SerializedName("supplierCode")
        @Expose
        var supplierCode: String? = null
        @SerializedName("tracer_validity_to")
        @Expose
        var tracerValidityTo: String? = null
        @SerializedName("tracer_validity_from")
        @Expose
        var tracerValidityFrom: String? = null
        @SerializedName("tracer_ref")
        @Expose
        var tracerRef: String? = null
        @SerializedName("type")
        @Expose
        var type: String? = null
        @SerializedName("rig_address")
        @Expose
        var rigAddress: String? = null
        @SerializedName("final_destination")
        @Expose
        var finalDestination: String? = null
        @SerializedName("first_destination")
        @Expose
        var firstDestination: String? = null
        @SerializedName("logger")
        @Expose
        var logger: String? = null
        @SerializedName("province")
        @Expose
        var province: String? = null

    }

}