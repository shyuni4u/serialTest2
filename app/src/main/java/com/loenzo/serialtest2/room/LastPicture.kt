package com.loenzo.serialtest2.room

import android.hardware.camera2.CameraCharacteristics
import java.io.Serializable

class LastPicture(var title: String, var uri: String) : Serializable {
    var id: Long = System.currentTimeMillis()
    var option: String
    var cameraFlash: Boolean
    var cameraDirection: Int
    var cameraAlpha: Float
    var cameraRatio: Int
    var alarmState: Boolean
    var flagCamera: Boolean
    var alarmMilliseconds: Long

    init {
        this.option = ""
        this.cameraFlash = false
        this.cameraDirection = CameraCharacteristics.LENS_FACING_BACK
        this.cameraAlpha = 0.4F
        this.cameraRatio = 0
        this.alarmState = false
        this.flagCamera = false
        this.alarmMilliseconds = 0
    }

    fun copy (copy: LastPicture) {
        this.title = copy.title
        this.uri = copy.uri
        this.option = copy.option
        this.cameraFlash = copy.cameraFlash
        this.cameraDirection = copy.cameraDirection
        this.cameraAlpha = copy.cameraAlpha
        this.cameraRatio = copy.cameraRatio
        this.alarmState = copy.alarmState
        this.flagCamera = copy.flagCamera
        this.alarmMilliseconds = copy.alarmMilliseconds
    }
}