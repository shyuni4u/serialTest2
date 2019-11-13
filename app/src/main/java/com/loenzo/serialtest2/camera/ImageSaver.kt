package com.loenzo.serialtest2.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.media.ExifInterface
import android.media.Image
import android.os.Environment
import android.os.Environment.DIRECTORY_DCIM
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.loenzo.serialtest2.util.APP_NAME
import com.loenzo.serialtest2.util.getRotateBitmap
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

/**
 * Saves a JPEG [Image] into the specified [File].
 */
internal class ImageSaver(
    /**
     * The JPEG image
     */
    private val image: Image,

    private val title: String,

    /**
     * For sendBroadcast
     */
    private val context: Context,

    private val cameraDirection: Int
) : Runnable {
    @SuppressLint("InlinedApi")
    override fun run() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        image.close()

        val cr = context.contentResolver

        val dir = File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM).absolutePath + File.separator + APP_NAME)
        /*
        if (dir == null) {
            //dir = context.getExternalFilesDir(DIRECTORY_DCIM)
            //dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        }
         */
        if (!dir.exists()) {
            dir.mkdirs()
        }

        var n = 1
        var file = File(dir, "$title.jpg")
        while(file.exists()) {
            file = File(dir, "$title($n).jpg")
            n++
        }

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, file.name)
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.DESCRIPTION, APP_NAME + "_$title")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            //put(MediaStore.Images.Media.IS_PENDING, 1)
            put(MediaStore.Images.Media.DATA, file.absolutePath)
            //put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }

        val url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        if (url != null) {
            try {
                val pdf = cr.openFileDescriptor(url, "w", null)
                if (pdf != null) {
                    val output = FileOutputStream(pdf.fileDescriptor)
                    try {
                        output.write(bytes)
                    } finally {
                        output.close()
                        pdf.close()
                    }
                }
            } catch (e: FileNotFoundException) {
                Log.e(TAG, "FileNotFoundException")
            } catch (e: IOException) {
                Log.e(TAG, "IOException")
            } finally {
                cr.update(url, values, null, null)
                values.clear()

                if (cameraDirection == CameraCharacteristics.LENS_FACING_FRONT) {
                    val exif = ExifInterface(file.absolutePath)
                    val rotate = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                        ExifInterface.ORIENTATION_ROTATE_270 -> 270
                        ExifInterface.ORIENTATION_ROTATE_180-> 180
                        ExifInterface.ORIENTATION_ROTATE_90-> 90
                        else -> 0
                    }
                    val flip = flipImage(getRotateBitmap(BitmapFactory.decodeFile(file.absolutePath), rotate))
                    val output = FileOutputStream(file).apply {
                        flip.compress(Bitmap.CompressFormat.JPEG, 100, this)
                    }
                    output.close()
                    cr.update(url, values, null, null)
                    values.clear()
                }

                (context as CameraActivity).refreshImages()
                CameraActivity.takingState = false

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                }
            }

            /*
            cr.openOutputStream(url).use { out ->
                src.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            image.close()
            values.clear()
            //values.put(MediaStore.Images.Media.IS_PENDING, 0)
            cr.update(url, values, null, null)
            */
        }
    }

    private fun flipImage(src: Bitmap, type: Int = FLIP_HORIZONTAL): Bitmap {
        val matrix = Matrix()

        if (type == FLIP_VERTICAL) {
            matrix.setScale(1.0F, -1.0F)
        } else if (type == FLIP_HORIZONTAL) {
            matrix.setScale(-1.0F, 1.0F)
        }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, false)
    }

    companion object {
        private const val TAG = "ImageSaver"
        private const val FLIP_VERTICAL = 0
        private const val FLIP_HORIZONTAL = 1
    }
}