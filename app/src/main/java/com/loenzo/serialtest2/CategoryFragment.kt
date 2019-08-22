package com.loenzo.serialtest2

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class CategoryFragment : Fragment () {

    companion object {
        private const val TAG = "CategoryFrag: "
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.row_category, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "Call onViewCreated")

        val imageView: ImageView = view.findViewById(R.id.lastImage)
        val textView: TextView  = view.findViewById(R.id.categoryName)
        val argName = arguments!!.getString("TITLE")!!
        textView.text = argName

        imageView.setImageBitmap(getRecentFileFromCategoryName(argName, context!!))

        /**
         * set button click listener
         */
        val btnAdd: ImageButton = view.findViewById(R.id.btnAdd)
        val btnList: ImageButton = view.findViewById(R.id.btnList)
        val btnCamera: ImageButton = view.findViewById(R.id.btnCamera)
        val btnVideo: ImageButton = view.findViewById(R.id.btnVideo)
        val btnOption: ImageButton = view.findViewById(R.id.btnOption)

        btnAdd.setOnClickListener {
            val addCategoryName = EditText(context)
            val builder = AlertDialog.Builder(context)

            builder.setTitle(resources.getString(R.string.add_category_title))
            builder.setView(addCategoryName)
            builder.setPositiveButton(resources.getString(R.string.add)
            ) { _, _ -> run {
                (context as MainActivity).addCategoryFragment(addCategoryName.text.toString())
            } }
            builder.setNegativeButton(resources.getString(R.string.cancel)
            ) { _, _ -> run {} }

            builder.show()
        }

        btnList.setOnClickListener {
            Toast.makeText(context, "LIST BUTTON", Toast.LENGTH_SHORT).show()
        }

        btnCamera.setOnClickListener {
            (context as MainActivity).openCamera(argName)
        }

        btnVideo.setOnClickListener {
            //Toast.makeText(context, "VIDEO BUTTON", Toast.LENGTH_SHORT).show()
        }

        btnOption.setOnClickListener {
            Toast.makeText(context, "OPTION BUTTON", Toast.LENGTH_SHORT).show()
        }
    }
}