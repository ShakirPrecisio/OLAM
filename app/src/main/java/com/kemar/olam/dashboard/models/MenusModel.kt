package com.kemar.olam.dashboard.models

class MenusModel {
    var imgResId : Int? = 0
    var menuName: String? = null
    var subtitle:String?=null

    constructor(imgResId: Int, menuName: String) {
        this.imgResId = imgResId
        this.menuName = menuName

    }

    constructor(imgResId: Int, menuName: String, subtitle: String) {
        this.imgResId = imgResId
        this.menuName = menuName
        this.subtitle = subtitle

    }


}