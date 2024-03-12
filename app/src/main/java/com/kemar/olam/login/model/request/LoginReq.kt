package com.kemar.olam.login.model.request

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

class LoginReq {
    @SerializedName("username")
    @Expose
    private var username: String? = null
    @SerializedName("password")
    @Expose
    private var password: String? = null
    @SerializedName("isADLogin")
    @Expose
    private var isADLogin: Boolean? = false

    fun getUsername(): String? {
        return username
    }

    fun setIsADLogin(isADLogin: Boolean?) {
        this.isADLogin = isADLogin
    }

    fun isADLogin(): Boolean? {
        return isADLogin
    }

    fun setUsername(username: String?) {
        this.username = username
    }

    fun getPassword(): String? {
        return password
    }

    fun setPassword(password: String?) {
        this.password = password
    }
}