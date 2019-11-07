package com.loenzo.serialtest2.room

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "category", indices = [Index(value = ["title"], unique = true)])
data class LastPicture(@NonNull @PrimaryKey(autoGenerate = true) var id: Long,
                       @ColumnInfo(name = "title") var title: String,
                       @ColumnInfo(name = "alarm_state") var alarmState: Boolean,
                       @ColumnInfo(name = "alarm_milliseconds") var alarmMilliseconds: Long) : Serializable {
    constructor(title: String): this(0, title, false, 0)
}