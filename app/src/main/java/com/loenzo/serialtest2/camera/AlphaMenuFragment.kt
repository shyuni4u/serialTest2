package com.loenzo.serialtest2.camera

import androidx.appcompat.widget.AppCompatSeekBar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.loenzo.serialtest2.R
import com.loenzo.serialtest2.help.ManualActivity

class AlphaMenuFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.alpha_menu_fragment, container, false)

        val barAlpha = view.findViewById<AppCompatSeekBar>(R.id.barAlpha)
        return view
    }
}