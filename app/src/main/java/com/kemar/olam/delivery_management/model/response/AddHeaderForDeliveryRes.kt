package com.kemar.olam.delivery_management.model.response

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import java.io.Serializable


class AddHeaderForDeliveryRes  : Serializable{
    @SerializedName("userID")
    @Expose
    private var userID: Int? = null
    @SerializedName("deliveryId")
    @Expose
    private var deliveryId: Int? = null
    @SerializedName("deliveryNumber")
    @Expose
    private var deliveryNumber: String? = null
    @SerializedName("supplierId")
    @Expose
    private var supplierId: String? = null
    @SerializedName("originId")
    @Expose
    private var originId: String? = null
    @SerializedName("truckId")
    @Expose
    private var truckId: Int? = null
    @SerializedName("transporterId")
    @Expose
    private var transporterId: Int? = null
    @SerializedName("customerId")
    @Expose
    private var customerId: Int? = null
    @SerializedName("supplierName")
    @Expose
    private var supplierName: String? = null
    @SerializedName("originName")
    @Expose
    private var originName: String? = null
    @SerializedName("truckName")
    @Expose
    private var truckName: String? = null


    @SerializedName("truckNo")
    @Expose
    private var truckNo: String? = null

    @SerializedName("transporterName")
    @Expose
    private var transporterName: String? = null
    @SerializedName("customerShortName")
    @Expose
    private var customerShortName: String? = null
    @SerializedName("customerName")
    @Expose
    private var customerName: String? = null
    @SerializedName("deliveryDate")
    @Expose
    private var deliveryDate: String? = null
    @SerializedName("timezoneId")
    @Expose
    private var timezoneId: String? = null
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

    fun getUserID(): Int? {
        return userID
    }

    fun setUserID(userID: Int?) {
        this.userID = userID
    }

    fun getDeliveryId(): Int? {
        return deliveryId
    }

    fun setDeliveryId(deliveryId: Int?) {
        this.deliveryId = deliveryId
    }

    fun getDeliveryNumber(): String? {
        return deliveryNumber
    }

    fun setDeliveryNumber(deliveryNumber: String?) {
        this.deliveryNumber = deliveryNumber
    }

    fun getSupplierId(): String? {
        return supplierId
    }

    fun setSupplierId(supplierId: String?) {
        this.supplierId = supplierId
    }

    fun getOriginId(): String? {
        return originId
    }

    fun setOriginId(originId: String?) {
        this.originId = originId
    }

    fun getTruckId(): Int? {
        return truckId
    }

    fun setTruckId(truckId: Int?) {
        this.truckId = truckId
    }

    fun getTransporterId(): Int? {
        return transporterId
    }

    fun setTransporterId(transporterId: Int?) {
        this.transporterId = transporterId
    }

    fun getCustomerId(): Int? {
        return customerId
    }

    fun setCustomerId(customerId: Int?) {
        this.customerId = customerId
    }

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

    fun getTruckName(): String? {
        return truckName
    }

    fun setTruckName(truckName: String?) {
        this.truckName = truckName
    }

    fun gettruckNo(): String? {
        return truckNo
    }

    fun settruckNo(truckNo: String?) {
        this.truckNo = truckNo
    }



    fun getTransporterName(): String? {
        return transporterName
    }

    fun setTransporterName(transporterName: String?) {
        this.transporterName = transporterName
    }

    fun getCustomerShortName(): String? {
        return customerShortName
    }

    fun setCustomerShortName(customerShortName: String?) {
        this.customerShortName = customerShortName
    }

    fun getCustomerName(): String? {
        return customerName
    }

    fun setCustomerName(customerName: String?) {
        this.customerName = customerName
    }

    fun getDeliveryDate(): String? {
        return deliveryDate
    }

    fun setDeliveryDate(deliveryDate: String?) {
        this.deliveryDate = deliveryDate
    }

    fun getTimezoneId(): String? {
        return timezoneId
    }

    fun setTimezoneId(timezoneId: String?) {
        this.timezoneId = timezoneId
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

}