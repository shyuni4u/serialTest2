package com.loenzo.serialtest2

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.camera2.CameraCharacteristics
import android.media.ExifInterface
import android.media.Image
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File
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

    /**
     * The file we save the image into.
     */
    private val file: File,

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

    override fun run() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        //val src: Bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null)
        //val copy: Bitmap = getRotateBitmap(src, 0, false)

        var output: FileOutputStream? = null
        try {
            /*
            output = FileOutputStream(file).apply {
                copy.compress(Bitmap.CompressFormat.JPEG, 100, this)
            }
            */
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
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = Uri.fromFile(file)
            }
            context.sendBroadcast(intent)

            if (cameraDirection == CameraCharacteristics.LENS_FACING_FRONT) {
                val exif = ExifInterface(file.absolutePath)
                val rotate = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    ExifInterface.ORIENTATION_ROTATE_180-> 180
                    ExifInterface.ORIENTATION_ROTATE_90-> 90
                    else -> 0
                }
                val flip = flipImage(getRotateBitmap(BitmapFactory.decodeFile(file.absolutePath), rotate, false))
                try {
                    output = FileOutputStream(file).apply {
                        flip.compress(Bitmap.CompressFormat.JPEG, 100, this)
                    }
                } catch(e: IOException) {
                    Log.e(TAG, e.toString())
                } finally {
                    output?.let {
                        try {
                            it.close()
                        } catch (e: IOException) {
                            Log.e(TAG, e.toString())
                        }
                    }
                }
            }

            Handler(Looper.getMainLooper()).post {
                Glide.with(context)
                    .load(file.absolutePath)
                    .apply(RequestOptions().circleCrop())
                    .thumbnail(0.1F)
                    .override(100, 100)
                    .into(imageView!!)
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