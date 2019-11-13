package com.loenzo.serialtest2.camera

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.loenzo.serialtest2.R
import com.loenzo.serialtest2.util.*

class MainMenuFragment : Fragment() {

    private lateinit var btnFlash: ImageButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.main_menu_fragment, container, false)

        val btnMenu = view.findViewById<ImageButton>(R.id.btnMenu)
        btnMenu.setOnClickListener {
            (context as CameraActivity).changeMenuFragment(CATEGORY_STATE)
        }

        btnFlash = view.findViewById(R.id.btnFlash)
        Handler(Looper.getMainLooper()).post {
            if (CameraActivity.prefsFlash) {
                btnFlash.setImageResource(R.drawable.flash_on)
            } else {
                btnFlash.setImageResource(R.drawable.flash_off)
            }
        }
        btnFlash.setOnClickListener {
            (context as CameraActivity).changeFlashSetting()
        }
        //val btnAlarm = view.findViewById<ImageButton>(R.id.btnAlarm)
        val btnPlaid = view.findViewById<ImageButton>(R.id.btnPlaid)
        btnPlaid.setOnClickListener {
            (context as CameraActivity).changePlaidSetting()
        }
        val btnAlpha = view.findViewById<ImageButton>(R.id.btnAlpha)
        btnAlpha.setOnClickListener {
            (context as CameraActivity).changeMenuFragment(ALPHA_STATE)
        }
        val btnExport = view.findViewById<ImageButton>(R.id.btnExport)
        btnExport.setOnClickListener {
            (context as CameraActivity).changeMenuFragment(EXPORT_STATE)
        }

        return view
    }
}