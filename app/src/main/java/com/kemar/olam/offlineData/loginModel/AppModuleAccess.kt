package com.kemar.olam.offlineData.loginModel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open class AppModuleAccess : RealmObject() {
    @SerializedName("moduleCode")
    @Expose
    var moduleCode: String? = null

    @SerializedName("moduleName")
    @Expose
    var moduleName: String? = null

    @PrimaryKey
    @SerializedName("moduleId")
    @Expose
    var moduleId: Int? = null

}