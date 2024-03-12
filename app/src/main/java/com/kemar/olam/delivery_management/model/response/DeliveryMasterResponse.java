
package com.kemar.olam.delivery_management.model.response;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeliveryMasterResponse {

    @SerializedName("severity")
    @Expose
    private Integer severity;
    @SerializedName("errorMessage")
    @Expose
    private Object errorMessage;
    @SerializedName("destination")
    @Expose
    private List<Incharge> destination;
    @SerializedName("incharge")
    @Expose
    private List<Incharge> incharge = null;
    @SerializedName("location")
    @Expose
    private List<Location> location = null;
    @SerializedName("stackTrace")
    @Expose
    private Object stackTrace;
    @SerializedName("transport")
    @Expose
    private List<Transport> transport = null;
    @SerializedName("message")
    @Expose
    private Object message;
    @SerializedName("status")
    @Expose
    private Object status;
    @SerializedName("customer")
    @Expose
    private List<Customer> customer = null;

    public Integer getSeverity() {
        return severity;
    }

    public void setSeverity(Integer severity) {
        this.severity = severity;
    }

    public Object getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(Object errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<Incharge> getDestination() {
        return destination;
    }

    public void setDestination(List<Incharge> destination) {
        this.destination = destination;
    }

    public List<Incharge> getIncharge() {
        return incharge;
    }

    public void setIncharge(List<Incharge> incharge) {
        this.incharge = incharge;
    }

    public List<Location> getLocation() {
        return location;
    }

    public void setLocation(List<Location> location) {
        this.location = location;
    }

    public Object getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(Object stackTrace) {
        this.stackTrace = stackTrace;
    }

    public List<Transport> getTransport() {
        return transport;
    }

    public void setTransport(List<Transport> transport) {
        this.transport = transport;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public Object getStatus() {
        return status;
    }

    public void setStatus(Object status) {
        this.status = status;
    }

    public List<Customer> getCustomer() {
        return customer;
    }

    public void setCustomer(List<Customer> customer) {
        this.customer = customer;
    }




}
