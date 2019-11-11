package com.loenzo.serialtest2.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.Image
import android.os.Environment
import android.os.Environment.DIRECTORY_DCIM
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import com.loenzo.serialtest2.util.APP_NAME
import java.io.File

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

        var dir = context.getExternalFilesDir(DIRECTORY_DCIM)
        if (dir == null) {
            dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        }
        if (!dir!!.exists()) {
            dir.mkdirs()
        }

        var n = 1
        var file = File(dir, "$title.jpg")
        while(file.exists()) {
            file = File(dir, "$title($n).jpg")
            n++
        }

        /*
        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(file).apply {
                write(bytes)
            }
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        } finally {
            image.close()
            output?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    Log.e(TAG, e.toString())
                }
            }
        }
         */

        val values = ContentValues().apply {
            //put(MediaStore.Images.Media.TITLE, file.name)
            put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
            put(MediaStore.Images.Media.DESCRIPTION, APP_NAME + "_$title")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.IS_PENDING, 1)
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM")
            //put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis())
            //put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
        }
        val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        Log.e(TAG, "path: ${collection.path}")

        val url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        if (url == null) {
            Log.e(TAG, "url is null")
        } else {
            Log.e(TAG, "url is not null")
            cr.openOutputStream(url).use { out ->
                src.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            cr.update(url, values, null, null)
        }

        /*
        try {
            val pdf = cr.openFileDescriptor(url, "w", null)
            if (pdf == null) {
                Log.e(TAG, "TEST")
            } else {
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
         */
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