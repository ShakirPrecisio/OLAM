package com.kemar.olam.login.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName




class CommonEncyrptRequest {
    @SerializedName("request")
    @Expose
    private var request: String? = null

    fun getRequest(): String? {
        return request
    }

    fun setRequest(request: String?) {
        this.request = request
    }

}