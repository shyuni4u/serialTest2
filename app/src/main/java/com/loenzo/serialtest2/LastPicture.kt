package com.loenzo.serialtest2

class LastPicture {
    var title: String? = null
    var uri: String? = null
    var option: String? = null
    var camera_direction: Int? = null
    var camera_transparent: Int? = null
    var camera_ratio: Int? = null

    constructor(title: String, uri: String) {
        this.title = title
        this.uri = uri
        this.option = ""
        this.camera_direction = 0
        this.camera_transparent = 40
        this.camera_ratio = 0
    }

    constructor(title: String, uri: String, option: String, camera_direction: Int, camera_transparent: Int, camera_ratio: Int) {
        this.title = title
        this.uri = uri
        this.option = option
        this.camera_direction = camera_direction
        this.camera_transparent = camera_transparent
        this.camera_ratio = camera_ratio
    }
}