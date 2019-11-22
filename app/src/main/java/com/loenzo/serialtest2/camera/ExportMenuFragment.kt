package com.loenzo.serialtest2.camera

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.loenzo.serialtest2.R
import com.loenzo.serialtest2.encoder.ParamVideo

class ExportMenuFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.export_menu_fragment, container, false)

        val btnGif = view.findViewById<Button>(R.id.btnGif)
        btnGif.setOnClickListener {
            val builder = AlertDialog.Builder(context)

            val etName = EditText(context)
            etName.hint = context!!.resources.getString(R.string.file_name)
            etName.text = etName.text.append((context as CameraActivity).getRecentName())

            val etFps = EditText(context)
            etFps.hint = context!!.resources.getString(R.string.gif_fps)
            etFps.inputType = InputType.TYPE_CLASS_NUMBER
            //etFps.text = etFps.text.append("8")

            val lay = LinearLayout(context)
            lay.orientation = LinearLayout.VERTICAL
            lay.addView(etName)
            lay.addView(etFps)
            builder.setView(lay)

            builder.setTitle(context!!.resources.getString(R.string.export_file))
            builder.setPositiveButton(context!!.resources.getString(R.string.apply)
            ) { _, _ -> run {
                if (etName.text.toString() != "") {
                    val fps = when(etFps.text.toString()) {
                        "" -> 0
                        else -> Integer.parseInt(etFps.text.toString())
                    }
                    (context as CameraActivity).exportVideo(ParamVideo(etName.text.toString(), fps, CameraActivity.GIF))
                }
            } }
            builder.setNegativeButton(context!!.resources.getString(R.string.cancel)
            ) { _, _ -> run {} }
            builder.show()
        }

        val btnVideo = view.findViewById<Button>(R.id.btnVideo)
        btnVideo.setOnClickListener {
            val builder = AlertDialog.Builder(context)

            val etName = EditText(context)
            etName.hint = context!!.resources.getString(R.string.file_name)
            etName.text = etName.text.append((context as CameraActivity).getRecentName())

            val etFps = EditText(context)
            etFps.hint = context!!.resources.getString(R.string.fps)
            etFps.inputType = InputType.TYPE_CLASS_NUMBER

            val lay = LinearLayout(context)
            lay.orientation = LinearLayout.VERTICAL
            lay.addView(etName)
            //lay.addView(etFps)
            builder.setView(lay)

            builder.setTitle(context!!.resources.getString(R.string.export_file))
            builder.setPositiveButton(context!!.resources.getString(R.string.apply)
            ) { _, _ -> run {
                if (etName.text.toString() != "") {
                    /*
                    val fps = when(etFps.text.toString()) {
                        "" -> 0
                        else -> Integer.parseInt(etFps.text.toString())
                    }
                     */
                    val fps = 8
                    (context as CameraActivity).exportVideo(ParamVideo(etName.text.toString(), fps, CameraActivity.MOVIE))
                }
            } }
            builder.setNegativeButton(context!!.resources.getString(R.string.cancel)
            ) { _, _ -> run {} }
            builder.show()
        }

        return view
    }
}