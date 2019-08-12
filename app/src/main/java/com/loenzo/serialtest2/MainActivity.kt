package com.loenzo.serialtest2

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import androidx.fragment.app.Fragment
import android.Manifest.permission as _permission

class MainActivity : AppCompatActivity() {
    //  like static
    companion object {
        private const val TAG = "MainActivity: "
        private const val APP_NAME = "MEMORIA"

        // Permission code
        private const val PERMISSION_CODE = 1000

        private val permissionsRequired = arrayOf(
            _permission.WRITE_EXTERNAL_STORAGE,
            _permission.READ_EXTERNAL_STORAGE,
            _permission.CAMERA)

        var lasts: ArrayList<LastPicture> = ArrayList()
        var categories: ArrayList<String> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        getUsePermission()

        if (checkPermissions().isEmpty()) {
            initCategory(this)
        }
    }

    private fun getUsePermission() {
        // check runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // check external storage
            val requests = checkPermissions()
            if (requests.isNotEmpty()) {
                //permission denied
                //show popup to request runtime permission
                val arr = arrayOfNulls<String>(requests.size)
                requestPermissions(requests.toArray(arr), PERMISSION_CODE)
            } else {
                //permission already granted
            }
        } else {
            //permission already granted
        }
    }

    private fun checkPermissions() : ArrayList<String> {
        val requests : ArrayList<String> = ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissionsRequired) {
                if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                    requests.add(permission)
                }
            }
        }
        return requests
    }

    @SuppressLint("InlinedApi", "Recycle")
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

        //  load folder list
        for (file in listFiles) {
            if (file.isDirectory) {
                val selection = "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME}=?"
                val selectionArg = arrayOf(file.name)
                val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val select = listOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA)
                val orderBy: String = MediaStore.Images.Media.DATE_TAKEN + " DESC"

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
                            val tempFile = File(fileName)
                            if (tempFile.exists() && !categories.contains(folderName)) {
                                categories.add(folderName)
                                lasts.add(LastPicture(fileName, folderName))
                            }

                        }
                    }
                }

            }
        }
        makeViewPager()
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }

    private fun addFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }

    fun makeViewPager(moveLast: Boolean = false) {
        lastImages.adapter = CategoryFragmentAdapter(supportFragmentManager)
        if (moveLast) {
            lastImages.currentItem = lasts.size
        }
    }

    fun openCamera(param: LastPicture) {
        replaceFragment(PreviewFragment.newInstance(param))
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
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup granted
                    //pickImageFromGallery()
                    initCategory(this)
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}

/**
private fun makeRecyclerView(context: Context) {
    //recyclerView.layoutManager = LinearLayoutManager(context, OrientationHelper.HORIZONTAL, false)
    //recyclerView.adapter = PostsAdapter(categories)
}

private fun pickImageFromGallery() {
    //Intent to pick image
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
}
*/