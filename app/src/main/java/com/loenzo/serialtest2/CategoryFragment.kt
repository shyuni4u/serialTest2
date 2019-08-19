package com.loenzo.serialtest2

import android.app.AlertDialog
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.io.File

class CategoryFragment : Fragment () {

    companion object {
        private const val TAG = "CategoryFrag: "
    }

    private lateinit var sf: SharedPreferences
    private lateinit var argName: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.row_category, container, false)

        val argName = arguments!!.getString("NAME")!!
        Log.i(TAG, "val : $argName")

        this.argName = argName

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageView: ImageView = view.findViewById(R.id.lastImage)
        val textView: TextView  = view.findViewById(R.id.categoryName)

        textView.text = argName

        val sdcard: String = Environment.getExternalStorageState()
        var rootDir: File = when (sdcard != Environment.MEDIA_MOUNTED) {
            true -> Environment.getRootDirectory()
            false -> Environment.getExternalStorageDirectory()
        }
        rootDir = File(rootDir.absolutePath + "/$APP_NAME/$argName/")
        if (!rootDir.exists())  rootDir.mkdirs()

        val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME}=?"
        val selectionArg = arrayOf(argName)
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val select = listOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA)
        val orderBy: String = MediaStore.Images.Media.DATE_TAKEN + " DESC LIMIT 1"

        val cursor: Cursor? = context!!.contentResolver.query(uri, select.toTypedArray(), selection, selectionArg, orderBy)

        val bitmap: Bitmap = when (cursor!!.count == 0) {
            true -> BitmapFactory.decodeResource(resources, R.drawable.need_picture)
            false -> {
                cursor.moveToNext()
                BitmapFactory.decodeFile(cursor.getString(cursor.getColumnIndex(select[1])))
            }
        }
        cursor.close()

        imageView.setImageBitmap(bitmap)

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
                //  make category folder
                val newName = addCategoryName.text.toString()

                sf = activity!!.getSharedPreferences(APP_NAME, MODE_PRIVATE)

                val categorySet = sf.getStringSet("CATEGORY_LIST", HashSet<String>())
                if (!categorySet!!.contains(newName)) {
                    categorySet.add(newName)

                    sf.edit().apply {
                        putStringSet("CATEGORY_LIST", categorySet).apply()
                    }

                    (activity as MainActivity).makeViewPager(true)
                }
            } }
            builder.setNegativeButton(resources.getString(R.string.cancel)
            ) { _, _ -> run {} }

            builder.show()
        }

        btnList.setOnClickListener {
            Toast.makeText(context, "LIST BUTTON", Toast.LENGTH_SHORT).show()
        }

        btnCamera.setOnClickListener {
            //(context as MainActivity).openCamera(_category)
        }

        btnVideo.setOnClickListener {
            //Toast.makeText(context, "VIDEO BUTTON", Toast.LENGTH_SHORT).show()
        }

        btnOption.setOnClickListener {
            Toast.makeText(context, "OPTION BUTTON", Toast.LENGTH_SHORT).show()
        }
    }
}