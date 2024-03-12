package com.kemar.olam.login.model.responce

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class LoginRes {
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

    @SerializedName("userLocationID")
    @Expose
    private var userLocationID: Int? = 0

    @SerializedName("tokenKey")
    @Expose
    private var tokenKey: String? = null

    @SerializedName("role")
    @Expose
    private var role: Role? = null

    @SerializedName("userProfile")
    @Expose
    private var userProfile: UserProfile? = null

    @SerializedName("appModuleAccess")
    @Expose
    private var appModuleAccess: List<AppModuleAccess>? = null


    fun getTokenKey(): String? {
        return tokenKey
    }

    fun setTokenKey(tokenKey: String?) {
        this.tokenKey = tokenKey
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

    fun getUserLocationID(): Int? {
        return userLocationID
    }

    fun setUserLocationID(userLocationID: Int?) {
        this.userLocationID = userLocationID
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

    fun getRole(): Role? {
        return role
    }

    fun setRole(role: Role?) {
        this.role = role
    }

    fun getUserProfile(): UserProfile? {
        return userProfile
    }

    fun setUserProfile(userProfile: UserProfile?) {
        this.userProfile = userProfile
    }

    fun getAppModuleAccess(): List<AppModuleAccess?>? {
        return appModuleAccess
    }

    fun setAppModuleAccess(appModuleAccess: List<AppModuleAccess?>?) {
        this.appModuleAccess = appModuleAccess as List<AppModuleAccess>?
    }

    class Role {
        @SerializedName("id")
        @Expose
        var id: Int? = null
        @SerializedName("isAdmin")
        @Expose
        var isAdmin: String? = null
        @SerializedName("isReadOnly")
        @Expose
        var isReadOnly: String? = null
        @SerializedName("transactionStatus")
        @Expose
        var transactionStatus: String? = null
        @SerializedName("errorMessage")
        @Expose
        var errorMessage: String? = null
        @SerializedName("priority")
        @Expose
        var priority: Int? = null
        @SerializedName("roleDescription")
        @Expose
        var roleDescription: String? = null
        @SerializedName("roleName")
        @Expose
        var roleName: String? = null

    }

    class UserProfile {
        @SerializedName("userId")
        @Expose
        var userId: Int? = null
        @SerializedName("active")
        @Expose
        var active: String? = null
        @SerializedName("activeDate")
        @Expose
        var activeDate: String? = null
        @SerializedName("dateCreated")
        @Expose
        var dateCreated: String? = null
        @SerializedName("dateModified")
        @Expose
        var dateModified: String? = null
        @SerializedName("dateOfBirth")
        @Expose
        var dateOfBirth: String? = null
        @SerializedName("emailId")
        @Expose
        var emailId: String? = null
        @SerializedName("firstName")
        @Expose
        var firstName: String? = null
        @SerializedName("gender")
        @Expose
        var gender: String? = null
        @SerializedName("isApplicationAuthentication")
        @Expose
        var isApplicationAuthentication: String? = null
        @SerializedName("image")
        @Expose
        var image: String? = ""
        @SerializedName("lastName")
        @Expose
        var lastName: String? = null
        @SerializedName("loginId")
        @Expose
        var loginId: String? = null
        @SerializedName("middleName")
        @Expose
        var middleName: String? = null
        @SerializedName("password")
        @Expose
        var password: String? = null
        @SerializedName("secretAnswer")
        @Expose
        var secretAnswer: String? = null
        @SerializedName("secretQuestion")
        @Expose
        var secretQuestion: String? = null
        @SerializedName("userCreated")
        @Expose
        var userCreated: String? = null
        @SerializedName("userModified")
        @Expose
        var userModified: String? = null
        @SerializedName("userType")
        @Expose
        var userType: String? = null
        @SerializedName("validFrom")
        @Expose
        var validFrom: String? = null
        @SerializedName("validTill")
        @Expose
        var validTill: String? = null
        @SerializedName("roles")
        @Expose
        var roles: String? = null
        @SerializedName("statusName")
        @Expose
        var statusName: String? = null
        @SerializedName("isReadOnly")
        @Expose
        var isReadOnly: String? = null
        @SerializedName("deleteStatus")
        @Expose
        var deleteStatus: String? = null

        @SerializedName("tokenKey")
        @Expose
        var tokenKey: String? = null

    }


    class AppModuleAccess {
        @SerializedName("moduleCode")
        @Expose
        var moduleCode: String? = null

        @SerializedName("moduleName")
        @Expose
        var moduleName: String? = null

        @SerializedName("moduleId")
        @Expose
        var moduleId: Int? = null

    }

}