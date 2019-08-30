package com.loenzo.serialtest2

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.core.os.bundleOf
import kotlinx.android.synthetic.main.content_main.*
import com.google.gson.Gson
import java.io.File
import android.Manifest.permission as _permission

class MainActivity : AppCompatActivity() {
    //  like static
    companion object {
        private const val TAG = "MainActivity: "

        private val permissionsRequired = arrayOf(
            _permission.WRITE_EXTERNAL_STORAGE,
            _permission.READ_EXTERNAL_STORAGE,
            _permission.CAMERA)
    }

    private lateinit var settingFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)
        Log.i(TAG, "Call onCreate")

        getUsePermission()
        if (checkPermissions().isEmpty()) {
            initSettingFile()
            initCategory()
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
            }
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

    private fun initSettingFile() {
        val sdcard: String = Environment.getExternalStorageState()
        var rootDir: File = when (sdcard != Environment.MEDIA_MOUNTED) {
            true -> Environment.getRootDirectory()
            false -> Environment.getExternalStorageDirectory()
        }
        rootDir = File(rootDir.absolutePath + "/$APP_NAME/")
        if (!rootDir.exists()) {
            rootDir.mkdirs()
        }

        settingFile = File(rootDir.absolutePath + "/setting.json")
        if (!settingFile.exists()) {
            settingFile.writeText(Gson().toJson(listOf(LastPicture("DEFAULT", ""))))
        }
    }

    private fun initCategory() {
        // read external storage check ... XXX not yet
        lastImages.adapter = CategoryFragmentAdapter(supportFragmentManager, makeParamList())
    }

    private fun makeParamList() : ArrayList<CategoryFragment> {
        val categoryInfoString = settingFile.bufferedReader().use { it.readText() }
        val categoryInfoArray = Gson().fromJson(categoryInfoString, Array<LastPicture>::class.java)
        val list: ArrayList<CategoryFragment> = ArrayList()

        for (categoryInfo in categoryInfoArray) {
            //  setting array list for CategoryFragment
            CategoryFragment().apply {
                arguments = bundleOf("PARAM" to categoryInfo)
                list.add(this)
            }
        }
        return list
    }

    /**
     * add category
     * save refreshed info to setting.json
     */
    fun addCategoryFragment(newName: String) {
        val categoryInfoString = settingFile.bufferedReader().use { it.readText() }
        val categoryInfoArray = Gson().fromJson(categoryInfoString, Array<LastPicture>::class.java)

        // check newName exists
        for (info in categoryInfoArray) {
            if (info.title == newName) {
                Toast.makeText(this, resources.getString(R.string.duplicate_name), Toast.LENGTH_SHORT).apply {
                    setGravity(Gravity.BOTTOM, 0, 100)
                }.show()
                return
            }
        }

        val list: ArrayList<LastPicture> = categoryInfoArray.toCollection(ArrayList())
        list.add(LastPicture(newName, ""))

        settingFile.writeText(Gson().toJson(list))

        (lastImages.adapter as CategoryFragmentAdapter).addItem(CategoryFragment().apply {
            arguments = bundleOf("PARAM" to LastPicture(newName, ""))
        })
        lastImages.currentItem = (lastImages.adapter as CategoryFragmentAdapter).count
    }

    fun openCamera(categoryInfo: LastPicture) {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("PARAM", categoryInfo)
        startActivityForResult(intent, CAMERA_ACTIVITY_SUCCESS)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup granted
                    //pickImageFromGallery()
                    initSettingFile()
                    initCategory()
                } else {
                    //permission from popup denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).apply {
                        setGravity(Gravity.BOTTOM, 0, 100)
                    }.show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_FIRST_USER) {
            if (resultCode == CAMERA_ACTIVITY_SUCCESS) {
                val resultObject = data!!.getSerializableExtra("RESULT_PARAM") as LastPicture

                val categoryInfoString = settingFile.bufferedReader().use { it.readText() }
                val categoryInfoArray = Gson().fromJson(categoryInfoString, Array<LastPicture>::class.java)

                // find title
                for (info in categoryInfoArray) {
                    if (info.title == resultObject.title)   info.copy(resultObject)
                }

                settingFile.writeText(Gson().toJson(categoryInfoArray))
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

https://brunch.co.kr/@mystoryg/55
*/