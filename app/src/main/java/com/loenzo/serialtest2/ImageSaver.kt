package com.loenzo.serialtest2

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.media.Image
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import android.widget.ImageView
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
    private val imageView: ImageView?
) : Runnable {

    override fun run() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
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
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                data = Uri.fromFile(file)
            }
            context.sendBroadcast(intent)
            Handler(Looper.getMainLooper()).post {
                //TODO: do something

                val exif = ExifInterface(file.absolutePath)
                val rotate = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    ExifInterface.ORIENTATION_ROTATE_180-> 180
                    ExifInterface.ORIENTATION_ROTATE_90-> 90
                    else -> 0
                }
                imageView!!.setImageBitmap(getRotateBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, null), rotate, true))
            }
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "ImageSaver"
    }
}