package com.loenzo.serialtest2.room

import android.hardware.camera2.CameraCharacteristics
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "category")
data class LastPicture(@NonNull @PrimaryKey(autoGenerate = true) var id: Long,
                       @ColumnInfo(name = "title") var title: String,
                       @ColumnInfo(name = "camera_flash") var cameraFlash: Boolean,
                       @ColumnInfo(name = "camera_direction") var cameraDirection: Int,
                       @ColumnInfo(name = "camera_alpha") var cameraAlpha: Float,
                       @ColumnInfo(name = "alarm_state") var alarmState: Boolean,
                       @ColumnInfo(name = "flag_camera") var flagCamera: Boolean,
                       @ColumnInfo(name = "alarm_milliseconds") var alarmMilliseconds: Long) : Serializable {
    constructor(title: String): this(0, title, false, CameraCharacteristics.LENS_FACING_BACK, 0.4F, false, false, 0)
}