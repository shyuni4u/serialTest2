package com.loenzo.serialtest2

import android.content.Context
import android.graphics.*
import android.widget.ImageView
import android.graphics.Color.parseColor
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet


class CircularImageView(context: Context, attributeSet: AttributeSet) :
    ImageView(context, attributeSet) {

    override fun onDraw(canvas: Canvas) {
        val drawable = drawable ?: return

        if (width == 0 || height == 0) {
            return
        }
        val b = (drawable as BitmapDrawable).bitmap
        val bitmap = b.copy(Bitmap.Config.ARGB_8888, true)

        val w = width

        val roundBitmap = getRoundedCroppedBitmap(bitmap, w)
        canvas.drawBitmap(roundBitmap, 0F, 0F, null)

    }

    companion object {

        fun getRoundedCroppedBitmap(bitmap: Bitmap, radius: Int): Bitmap {
            val finalBitmap: Bitmap = if (bitmap.width != radius || bitmap.height != radius)
                Bitmap.createScaledBitmap(
                    bitmap, radius, radius,
                    false
                )
            else
                bitmap
            val output = Bitmap.createBitmap(
                finalBitmap.width,
                finalBitmap.height, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(output)

            val paint = Paint()
            val rect = Rect(
                0, 0, finalBitmap.width,
                finalBitmap.height
            )

            paint.isAntiAlias = true
            paint.isFilterBitmap = true
            paint.isDither = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = parseColor("#BAB399")
            canvas.drawCircle(
                finalBitmap.width / 2 + 0.7f,
                finalBitmap.height / 2 + 0.7f,
                finalBitmap.width / 2 + 0.1f, paint
            )
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(finalBitmap, rect, rect, paint)

            return output
        }
    }
}