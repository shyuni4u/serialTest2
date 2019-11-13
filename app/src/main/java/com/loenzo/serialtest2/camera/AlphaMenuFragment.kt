package com.loenzo.serialtest2.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.loenzo.serialtest2.R

class AlphaMenuFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.alpha_menu_fragment, container, false)

        val barAlpha = view.findViewById<SeekBar>(R.id.barAlpha)

        barAlpha.progress = (context as CameraActivity).getTransparentAlpha()
        barAlpha.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, i: Int, b: Boolean) {
                (context as CameraActivity).setTransparentAlpha(i)
            }
        })
        return view
    }
}