package com.loenzo.serialtest2.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.media.ExifInterface
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_DCIM
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.loenzo.serialtest2.util.APP_NAME
import com.loenzo.serialtest2.util.getRotateBitmap
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception

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

    /**
     * PreviewImage: show image after image save
     */
    private val imageView: ImageView?,

    private val cameraDirection: Int
) : Runnable {
    @SuppressLint("InlinedApi")
    override fun run() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val src: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)

        val cr = context.contentResolver
        val mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val dir = File(mediaUri.path!!)
        if (!dir.exists()) {
            dir.mkdirs()
        }

        var n = 1
        var file = File(dir, "$title.jpg")
        while(file.exists()) {
            file = File(dir, "$title($n).jpg")
            n++
        }
        //val copy: Bitmap = getRotateBitmap(src, 0, false)

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, file.name)
            //put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            //put(MediaStore.Images.Media.DESCRIPTION, APP_NAME + "_$title")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/$APP_NAME")
            put(MediaStore.Images.Media.IS_PENDING, 1)
            //put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            //put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }
        Log.e(TAG, "mediaUri: ${mediaUri.path}")
        val url = cr.insert(mediaUri, values)
        Log.e(TAG, "url: ${url!!.path}")

        try {
            val pdf = cr.openFileDescriptor(url, "w", null)
            if (pdf == null) {
                Log.e(TAG, "TEST")
            } else {
                val output = FileOutputStream(pdf.fileDescriptor)
                try {
                    output.write(bytes)
                } finally {
                    image.close()
                    output.close()
                    pdf.close()
                }
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "FileNotFoundException")
        } catch (e: IOException) {
            Log.e(TAG, "IOException")
        } finally {
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            Log.e(TAG, "finally url: ${url.path}")
            cr.update(url, values, null, null)

            Handler(Looper.getMainLooper()).post {
                /*
                    Glide.with(context)
                        .load(file.absolutePath)
                        .apply(RequestOptions().circleCrop())
                        .thumbnail(0.1F)
                        .override(100, 100)
                        .into(imageView!!)
                */
                //Log.e(TAG, "file.name: ${file.name}, file.path: ${file.parent}")
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
            }
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