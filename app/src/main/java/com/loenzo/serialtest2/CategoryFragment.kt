package com.loenzo.serialtest2

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

class CategoryFragment(private val _category: LastPicture) : Fragment () {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.row_category, container, false)
        val categoryImage: ImageView = view.findViewById(R.id.lastImage)
        val categoryName: TextView  = view.findViewById(R.id.categoryName)

        val btnAdd: ImageButton = view.findViewById(R.id.btnAdd)
        val btnList: ImageButton = view.findViewById(R.id.btnList)
        val btnCamera: ImageButton = view.findViewById(R.id.btnCamera)
        val btnVideo: ImageButton = view.findViewById(R.id.btnVideo)
        val btnOption: ImageButton = view.findViewById(R.id.btnOption)

        btnAdd.setOnClickListener {
            //Toast.makeText(context, "ADD BUTTON", Toast.LENGTH_SHORT).show()
            val addCategoryName = EditText(context)
            val builder = AlertDialog.Builder(context)

            builder.setTitle(resources.getString(R.string.add_category_title))
            builder.setView(addCategoryName)
            builder.setPositiveButton(resources.getString(R.string.add)
            ) { _, _ -> run {
                //  make category folder
                val newName = addCategoryName.text.toString()

                if (!MainActivity.categories.contains(newName)) {
                    MainActivity.categories.add(newName)
                    MainActivity.lasts.add(LastPicture("@@EMPTY@@", newName))
                }
                (context as MainActivity).makeCategoryFolder(newName)
                (context as MainActivity).makeViewPager(true)
            } }
            builder.setNegativeButton(resources.getString(R.string.cancel)
            ) { _, _ -> run {} }

            builder.show()
        }

        btnList.setOnClickListener {
            Toast.makeText(context, "LIST BUTTON", Toast.LENGTH_SHORT).show()
        }

        btnCamera.setOnClickListener {
            (context as MainActivity).openCamera()
        }

        btnVideo.setOnClickListener {
            Toast.makeText(context, "VIDEO BUTTON", Toast.LENGTH_SHORT).show()
        }

        btnOption.setOnClickListener {
            Toast.makeText(context, "OPTION BUTTON", Toast.LENGTH_SHORT).show()
        }

        categoryName.text = _category.strName
        val bitmap: Bitmap = when (_category.strUri == "@@EMPTY@@") {
            true -> BitmapFactory.decodeResource(resources, R.drawable.need_picture)
            false -> BitmapFactory.decodeFile(_category.strUri)
        }
        categoryImage.setImageBitmap(bitmap)

        return view
    }
}