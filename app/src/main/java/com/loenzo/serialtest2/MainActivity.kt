package com.loenzo.serialtest2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import android.Manifest.permission as _permission

class MainActivity : AppCompatActivity() {
    //  like static
    companion object {
        private const val APP_NAME = "MEMORIA"

        // image pick code
        private const val IMAGE_PICK_CODE = 1000

        //  make folder code
        private const val MAKE_FOLDER_CODE = 1001

        // Permission code
        private const val PERMISSION_CODE = 1002

        var lasts: ArrayList<LastPicture> = ArrayList()
        var categories: ArrayList<String> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        getUsePermission(this)
        initCategory(this)
        //makeRecyclerView(this)
    }
    private fun getUsePermission(context: Context) {
        // check runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(_permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                //permission denied
                val permissions = arrayOf(_permission.WRITE_EXTERNAL_STORAGE)
                //show popup to request runtime permission
                requestPermissions(permissions, PERMISSION_CODE)
            } else {
                //permission already granted
            }
        } else {
            //permission already granted
        }
    }

    private fun initCategory(context: Context) {
        val sdcard: String = Environment.getExternalStorageState()
        var rootDir: File = when (sdcard != Environment.MEDIA_MOUNTED) {
            true -> Environment.getRootDirectory()
            false -> Environment.getExternalStorageDirectory()
        }
        rootDir = File(rootDir.absolutePath + "/$APP_NAME/")
        val dir: String = rootDir.absolutePath
        var listFiles = rootDir.listFiles()

        if (listFiles.isEmpty()) {
            makeCategoryFolder("DEFAULT")
            listFiles = rootDir.listFiles()
        }

        categories.clear()
        lasts.clear()

        for (file in listFiles) {
            if (file.isDirectory) {
                val selection = "bucket_display_name=?"
                val selectionArg = arrayOf(file.name)
                val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val select = listOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA)
                val orderBy: String = MediaStore.Images.Media.DATE_ADDED + " DESC"

                val cursor: Cursor? = context.contentResolver.query(uri, select.toTypedArray(), selection, selectionArg, orderBy)

                if (cursor!!.count == 0) {
                    if (!categories.contains(file.name)) {
                        categories.add(file.name)
                        lasts.add(LastPicture("@@EMPTY@@", file.name))
                    }
                } else {
                    while (cursor.moveToNext()) {
                        val folderName: String = cursor.getString(cursor.getColumnIndex(select[0]))
                        val fileName: String = cursor.getString(cursor.getColumnIndex(select[1]))

                        if (fileName.startsWith(dir)) {
                            val file = File(fileName)
                            if (file.exists() && !categories.contains(folderName)) {
                                categories.add(folderName)
                                lasts.add(LastPicture(fileName, folderName))
                            }

                        }
                    }
                }

            }
        }
        makePagerView()
    }

    fun makePagerView(moveLast: Boolean = false) {
        lastImages.adapter = CategoryFragmentAdapter(supportFragmentManager)
        if (moveLast) {
            lastImages.currentItem = lasts.size
        }
    }

    private fun makeRecyclerView(context: Context) {
        //recyclerView.layoutManager = LinearLayoutManager(context, OrientationHelper.HORIZONTAL, false)
        //recyclerView.adapter = PostsAdapter(categories)
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    fun makeCategoryFolder(strCategoryName: String) {
        val sdcard: String = Environment.getExternalStorageState()
        var f: File?

        f = when (sdcard != Environment.MEDIA_MOUNTED) {
            true -> Environment.getRootDirectory()
            false -> Environment.getExternalStorageDirectory()
        }

        val dir: String = f!!.absolutePath + "/$APP_NAME/" + strCategoryName

        f = File(dir)
        if (!f.exists()) {
            f.mkdirs()
        }
        startActivityForResult(intent, MAKE_FOLDER_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup granted
                    //pickImageFromGallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            //lastImage.setImageURI(data?.data)
            Log.i(APP_NAME, "IMAGE_PICK_CODE")
        }

        if (resultCode == Activity.RESULT_OK && requestCode == MAKE_FOLDER_CODE) {
            Log.i(APP_NAME, "MAKE_FOLDER_CODE")
        }
    }

}

/*
https://recipes4dev.tistory.com/148
https://coding-factory.tistory.com/206
*/

