package com.loenzo.serialtest2.util

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import java.io.File
import kotlin.collections.ArrayList

const val APP_NAME = "OverCam"

// Shared Preference
const val SHARED_NAME = "CAM_INFO"

// BackButton interval time
const val CLOSE_INTERVAL_TIME = 1000 * 2

// Menu state
const val MAIN_STATE = 0
const val CATEGORY_STATE = 1
const val ALPHA_STATE = 2
const val EXPORT_STATE = 3

// Plaid state
const val PLAID_NONE_STATE = 0
const val PLAID_THREE_STATE = 1
const val PLAID_NINE_STATE = 2

// Minimum swipe distance
const val MIN_DISTANCE = 150

// Permission code
const val APPLICATION_SUCCESS = 9999
const val PERMISSION_CODE = 1000
const val CAMERA_ACTIVITY_SUCCESS = 1001
const val SELECT_MEDIA_SUCCESS = 1002

enum class FileType { JPEG, GIF, MP4 }

/**
 * return file name
 * through cutting path
 */
fun getNameFromPath (path: String): String {
    val fullName = path.substringAfterLast("/")
    return fullName.substringBeforeLast(".")
}

/**
 * get image path list
 * from argument title info
 */
@SuppressLint("InlinedApi")
fun getRecentFilePathListFromCategoryName (paramName: String, context: Context, fileType: FileType = FileType.JPEG): ArrayList<String> {
    val env = when(fileType) {
        FileType.JPEG -> Environment.DIRECTORY_DCIM
        else -> Environment.DIRECTORY_PICTURES
    }
    val dir = File(Environment.getExternalStoragePublicDirectory(env).absolutePath + File.separator + APP_NAME)
    if (!dir.exists())  dir.mkdirs()

    val selection = "${MediaStore.Images.Media.DESCRIPTION}=?"
    val selectionArg = arrayOf(APP_NAME + "_$paramName")
    val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val select = listOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA)
    val orderBy: String = MediaStore.Images.Media.DATE_TAKEN + " DESC"

    val cursor: Cursor? = context.contentResolver.query(uri, select.toTypedArray(), selection, selectionArg, orderBy)

    val returnArrayList = ArrayList<String>()

    while(cursor!!.moveToNext()) {
        val temp = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        if (fileType == FileType.JPEG && temp.substring(temp.lastIndexOf(".") + 1) == "jpg") {
            returnArrayList.add(temp)
        } else {
            returnArrayList.add(temp)
        }
    }
    cursor.close()
    return returnArrayList
}

/**
 * remove directory tree
 * from argument path info
 */
fun setRemoveImages(list: ArrayList<String>) {
    for (path in list) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }
}

/**
 * rotate bitmap if some device
 */
fun getRotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
    val scaledBitmap = bitmap.scale(bitmap.width, bitmap.height, true)
    if (degree == 0) return scaledBitmap

    val m = Matrix()
    m.setRotate(degree.toFloat(), (scaledBitmap.width / 2).toFloat(), (scaledBitmap.height / 2).toFloat())

    return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, m, true)
}

/**
 * rotate image in file info
 */
fun autoRotateFile(filePath: String): Bitmap {
    val exif = ExifInterface(filePath)
    val rotate = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
        ExifInterface.ORIENTATION_ROTATE_270 -> 270
        ExifInterface.ORIENTATION_ROTATE_180-> 180
        ExifInterface.ORIENTATION_ROTATE_90-> 90
        else -> 0
    }

    val origin = getRotateBitmap(BitmapFactory.decodeFile(filePath), rotate)
    return if (rotate == 90 || rotate == 270) {
        origin.scale(720, 1280, false)
    } else {
        origin.scale(1280, 720, false)
    }
}