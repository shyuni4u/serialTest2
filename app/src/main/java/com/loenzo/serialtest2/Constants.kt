/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("Constants")

package com.loenzo.serialtest2

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.graphics.scale
import java.io.File

const val APP_NAME = "MEMORIA"

/**
 * setting folder & image
 * from argument title info
 */
fun getRecentFileFromCategoryName (paramName: String, context: Context): Bitmap? {
    val sdcard: String = Environment.getExternalStorageState()
    var rootDir: File = when (sdcard != Environment.MEDIA_MOUNTED) {
        true -> Environment.getRootDirectory()
        false -> Environment.getExternalStorageDirectory()
    }
    rootDir = File(rootDir.absolutePath + "/$APP_NAME/$paramName/")
    if (!rootDir.exists())  rootDir.mkdirs()

    val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME}=?"
    val selectionArg = arrayOf(paramName)
    val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val select = listOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA)
    val orderBy: String = MediaStore.Images.Media.DATE_TAKEN + " DESC LIMIT 1"

    val cursor: Cursor? = context.contentResolver.query(uri, select.toTypedArray(), selection, selectionArg, orderBy)

    val bitmap: Bitmap = when (cursor!!.count == 0) {
        true -> BitmapFactory.decodeStream(context.resources.assets.open("blank_canvas.png"))
        false -> {
            cursor.moveToNext()
            val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            val exif = ExifInterface(path)
            val rotate = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                ExifInterface.ORIENTATION_ROTATE_180-> 180
                ExifInterface.ORIENTATION_ROTATE_90-> 90
                else -> 0
            }
            getRotateBitmap(BitmapFactory.decodeFile(path), rotate)
        }
    }
    cursor.close()
    return bitmap
}

/**
 * rotate bitmap if some device
 */
fun getRotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
    val maxSize = when (bitmap.width > bitmap.height) {
        true -> bitmap.width
        false -> bitmap.height
    }
    val degreeSize = when {
        maxSize > 2000 -> 5
        maxSize > 1000 -> 2
        else -> 1
    }
    val scaledBitmap = bitmap.scale(bitmap.width / degreeSize, bitmap.height / degreeSize, true)
    if (degree == 0) return scaledBitmap

    val m = Matrix()
    m.setRotate(degree.toFloat(), (scaledBitmap.width / 2).toFloat(), (scaledBitmap.height / 2).toFloat())

    return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, m, true)
}