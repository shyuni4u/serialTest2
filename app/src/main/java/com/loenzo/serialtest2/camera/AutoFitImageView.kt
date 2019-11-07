package com.loenzo.serialtest2.camera

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import android.widget.ImageView

/**
 * A [TextureView] that can be adjusted to a specified aspect ratio.
 */
class AutoFitImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ImageView(context, attrs, defStyle) {

    private var imagePlaid: ImageView? = null

    private var ratioWidth = 0
    private var ratioHeight = 0

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    fun setAspectRatio(width: Int, height: Int, temp: ImageView? = null) {
        require(!(width < 0 || height < 0)) { "Size cannot be negative." }
        ratioWidth = width
        ratioHeight = height
        imagePlaid = temp
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        if (ratioWidth == 0 || ratioHeight == 0) {
            setMeasuredDimension(width, height)
            if (imagePlaid != null) {
                imagePlaid!!.layoutParams.width = width
                imagePlaid!!.layoutParams.height = height
            }
        } else {
            if (width < height * ratioWidth / ratioHeight) {
                setMeasuredDimension(width, width * ratioHeight / ratioWidth)
                if (imagePlaid != null) {
                    imagePlaid!!.layoutParams.width = width
                    imagePlaid!!.layoutParams.height = width * ratioHeight / ratioWidth
                }
            } else {
                setMeasuredDimension(height * ratioWidth / ratioHeight, height)
                if (imagePlaid != null) {
                    imagePlaid!!.layoutParams.width = height * ratioWidth / ratioHeight
                    imagePlaid!!.layoutParams.height = height
                }
            }
        }
        if (imagePlaid != null) {
            imagePlaid!!.requestLayout()
        }
    }

}