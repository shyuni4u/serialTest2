package com.loenzo.serialtest2

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

@Suppress("CAST_NEVER_SUCCEEDS")
class CategoryFragment : Fragment() {

    companion object {
        private const val TAG = "CategoryFrag: "
    }

    private lateinit var imageView: ImageView
    private lateinit var argObject: LastPicture

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.row_category, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "Call onViewCreated")

        val textView: TextView  = view.findViewById(R.id.categoryName)
        imageView = view.findViewById(R.id.lastImage)
        argObject = arguments!!.get("PARAM") as LastPicture
        textView.text = argObject.title

        //imageView.setImageBitmap(getRecentFileFromCategoryName(argObject.title, context!!))
        Glide.with(context!!)
            .load(getRecentFilePathFromCategoryName(argObject.title, context!!))
            .thumbnail(0.1F)
            .into(imageView)

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
            (context as MainActivity).openGallery(argObject)
        }

        btnCamera.setOnClickListener {
            (context as MainActivity).openCamera(argObject)
        }

        btnVideo.setOnClickListener {
            //Toast.makeText(context, "VIDEO BUTTON", Toast.LENGTH_SHORT).show()
        }

        btnOption.setOnClickListener {
            Toast.makeText(context, "OPTION BUTTON", Toast.LENGTH_SHORT).show()
        }
    }
}