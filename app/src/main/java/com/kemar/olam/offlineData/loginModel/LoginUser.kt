package com.kemar.olam.offlineData.loginModel

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
open  class LoginUser : RealmObject(){

    var uniqueId: String? = null

    @PrimaryKey
    var username: String? = null

    var password: String? = null

    var token: String? = null

    var user_name: String? = null


    var profile_path: String? = null


    var user_id: String? = null


    var user_location_id: String? = null


    var user_token: String? = null


    var user_login_id: String? = null

    var user_role: String? = null

    var appmodule: String? = null




}