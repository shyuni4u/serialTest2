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
@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.loenzo.serialtest2

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.graphics.scale
import com.google.gson.Gson
import java.io.File

const val APP_NAME = "MEMORIA"

// Permission code
const val PERMISSION_CODE = 1000
const val CAMERA_ACTIVITY_SUCCESS = 1001
const val GALLERY_ACTIVITY_SUCCESS = 1002
const val SELECT_MEDIA_SUCCESS = 1003

/**
 * convert & save json file
 * from [list]
 */
fun writeSetting(list: Array<LastPicture>) {
    val sdcard: String = Environment.getExternalStorageState()
    var rootDir: File = when (sdcard != Environment.MEDIA_MOUNTED) {
        true -> Environment.getRootDirectory()
        false -> Environment.getExternalStorageDirectory()
    }
    rootDir = File(rootDir.absolutePath + "/$APP_NAME/")
    if (!rootDir.exists()) {
        rootDir.mkdirs()
    }

    val settingFile = File(rootDir.absolutePath + "/setting.json")
    if (!settingFile.exists()) {
        settingFile.writeText(Gson().toJson(listOf(LastPicture("DEFAULT", ""))))
    }
    settingFile.writeText(Gson().toJson(list))
}

/**
 * read setting information
 * from setting.json file
 * if setting.json is not exist
 * make default info
 */
fun readSetting(): Array<LastPicture> {
    val sdcard: String = Environment.getExternalStorageState()
    var rootDir: File = when (sdcard != Environment.MEDIA_MOUNTED) {
        true -> Environment.getRootDirectory()
        false -> Environment.getExternalStorageDirectory()
    }
    rootDir = File(rootDir.absolutePath + "/$APP_NAME/")
    if (!rootDir.exists()) {
        rootDir.mkdirs()
    }

    val settingFile = File(rootDir.absolutePath + "/setting.json")
    if (!settingFile.exists()) {
        settingFile.writeText(Gson().toJson(listOf(LastPicture("DEFAULT", ""))))
    }
    val categoryInfoString = settingFile.bufferedReader().use { it.readText() }
    return Gson().fromJson(categoryInfoString, Array<LastPicture>::class.java)
}

/**
 * return file name
 * through cutting path
 */
fun getNameFromPath (path: String): String {
    val fullName = path.substringAfterLast("/")
    return fullName.substringBeforeLast(".")
}

/**
 * setting folder & image
 * from argument title info
 */
fun getRecentFilePathFromCategoryName (paramName: String, context: Context): String {
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
    val returnPath = when (cursor!!.count == 0) {
        true -> ""
        false -> {
            cursor.moveToNext()
            cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        }
    }

    cursor.close()
    return returnPath
}

/**
 * get image path list
 * from argument title info
 */
fun getRecentFilePathListFromCategoryName (paramName: String, context: Context): ArrayList<String> {
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
    val orderBy: String = MediaStore.Images.Media.DATE_TAKEN + " DESC"

    val cursor: Cursor? = context.contentResolver.query(uri, select.toTypedArray(), selection, selectionArg, orderBy)

    val returnArrayList = ArrayList<String>()

    while(cursor!!.moveToNext()) {
        returnArrayList.add(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)))
    }
    cursor.close()
    return returnArrayList
}

/**
 * remove directory tree
 * from argument path info
 */
fun setDirectoryEmpty(directoryPath: String) {
    val dir = File(directoryPath)
    val childFileList = dir.listFiles()

    if (dir.exists()) {
        Log.i("TEST", "dir exists")
        if (childFileList.isNotEmpty()) {
            Log.i("TEST", "childFileList size: ${childFileList.size}")
            for (childFile in childFileList) {
                if (childFile.isDirectory) {
                    Log.i("TEST", childFile.absolutePath)
                    //setDirectoryEmpty(childFile.absolutePath)
                } else {
                    childFile.delete()
                }
            }
        }
        dir.delete()
    }
}

/**
 * rotate bitmap if some device
 */
fun getRotateBitmap(bitmap: Bitmap, degree: Int, useScale: Boolean): Bitmap {
    val maxSize = when (bitmap.width > bitmap.height) {
        true -> bitmap.width
        false -> bitmap.height
    }
    var degreeSize = when {
        maxSize > 2000 -> 2
        //maxSize > 1000 -> 2
        else -> 1
    }

    if (useScale) {
        degreeSize = 1
    }

    val scaledBitmap = bitmap.scale(bitmap.width / degreeSize, bitmap.height / degreeSize, true)
    if (degree == 0) return scaledBitmap

    val m = Matrix()
    m.setRotate(degree.toFloat(), (scaledBitmap.width / 2).toFloat(), (scaledBitmap.height / 2).toFloat())

    return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, m, true)
}