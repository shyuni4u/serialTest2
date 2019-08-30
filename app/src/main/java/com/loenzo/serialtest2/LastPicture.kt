package com.loenzo.serialtest2

import android.hardware.camera2.CameraCharacteristics
import java.io.Serializable

class LastPicture : Serializable {
    var title: String
    var uri: String?
    var option: String?
    var camera_flash: Boolean?
    var camera_direction: Int
    var camera_alpha: Float
    var camera_ratio: Int?

    constructor(title: String, uri: String) {
        this.title = title
        this.uri = uri
        this.option = ""
        this.camera_flash = false
        this.camera_direction = CameraCharacteristics.LENS_FACING_BACK
        this.camera_alpha = 0.4F
        this.camera_ratio = 0
    }

    constructor(title: String, uri: String, option: String, camera_flash: Boolean, camera_direction: Int, camera_alpha: Float, camera_ratio: Int) {
        this.title = title
        this.uri = uri
        this.option = option
        this.camera_flash = camera_flash
        this.camera_direction = camera_direction
        this.camera_alpha = camera_alpha
        this.camera_ratio = camera_ratio
    }

    fun copy (copy: LastPicture) {
        this.title = copy.title
        this.uri = copy.uri
        this.option = copy.option
        this.camera_flash = copy.camera_flash
        this.camera_direction = copy.camera_direction
        this.camera_alpha = copy.camera_alpha
        this.camera_ratio = copy.camera_ratio
    }
}