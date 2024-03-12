package com.kemar.olam.login.model.responce

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class ShipmentSearchRes {
    @SerializedName("\$id")
    @Expose
    private var `$id`: String? = null
    @SerializedName("StatusCode")
    @Expose
    private var statusCode: Int? = null
    @SerializedName("Status")
    @Expose
    private var status: String? = null
    @SerializedName("MessageId")
    @Expose
    private var messageId: Int? = null
    @SerializedName("Message")
    @Expose
    private var message: String? = null
    @SerializedName("ds")
    @Expose
    private var ds: Ds? = null

    fun `get$id`(): String? {
        return `$id`
    }

    fun `set$id`(`$id`: String?): Unit {
        this.`$id` = `$id`
    }

    fun getStatusCode(): Int? {
        return statusCode
    }

    fun setStatusCode(statusCode: Int?): Unit {
        this.statusCode = statusCode
    }

    fun getStatus(): String? {
        return status
    }

    fun setStatus(status: String?): Unit {
        this.status = status
    }

    fun getMessageId(): Int? {
        return messageId
    }

    fun setMessageId(messageId: Int?): Unit {
        this.messageId = messageId
    }

    fun getMessage(): String? {
        return message
    }

    fun setMessage(message: String?): Unit {
        this.message = message
    }

    fun getDs(): Ds? {
        return ds
    }

    fun setDs(ds: Ds?): Unit {
        this.ds = ds
    }

    class Ds {
        @SerializedName("Table")
        @Expose
        var table: List<Table>? =
            null

    }

    class Table {
        @SerializedName("JobType")
        @Expose
        var jobType: String? = null
        @SerializedName("JobNo")
        @Expose
        var jobNo: String? = null
        @SerializedName("JobDate")
        @Expose
        var jobDate: String? = null
        @SerializedName("ShipperName")
        @Expose
        var shipperName: String? = null
        @SerializedName("ShiplineName")
        @Expose
        var shiplineName: String? = null
        @SerializedName("POL")
        @Expose
        var pOL: String? = null
        @SerializedName("POD")
        @Expose
        var pOD: String? = null
        @SerializedName("MasterNo")
        @Expose
        var masterNo: String? = null
        @SerializedName("HouseNo")
        @Expose
        var houseNo: String? = null
        @SerializedName("SBType")
        @Expose
        var sBType: String? = null
        @SerializedName("GroupID")
        @Expose
        var groupID: Int? = null

    }

}