package com.loenzo.serialtest2

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.content_main.*
import androidx.fragment.app.Fragment
import android.Manifest.permission as _permission

class MainActivity : AppCompatActivity() {
    //  like static
    companion object {
        private const val TAG = "MainActivity: "

        // Permission code
        private const val PERMISSION_CODE = 1000

        private val permissionsRequired = arrayOf(
            _permission.WRITE_EXTERNAL_STORAGE,
            _permission.READ_EXTERNAL_STORAGE,
            _permission.CAMERA)
    }

    private lateinit var sf: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        getUsePermission()

        if (checkPermissions().isEmpty()) {
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

    private fun initCategory() {
        // read external storage check ... XXX not yet
        sf = getSharedPreferences(APP_NAME, MODE_PRIVATE)

        val categorySet = sf.getStringSet("CATEGORY_LIST", HashSet<String>())
        if (categorySet.isNullOrEmpty() && !categorySet!!.contains("DEFAULT")) {
            categorySet.add("DEFAULT")

            val editor: SharedPreferences.Editor = sf.edit()
            //editor.clear()
            editor.putStringSet("CATEGORY_LIST", categorySet)
            editor.apply()
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
        sf = getSharedPreferences(APP_NAME, MODE_PRIVATE)

        lastImages.adapter = CategoryFragmentAdapter(supportFragmentManager, sf)
        if (moveLast) {
            val categorySet = sf.getStringSet("CATEGORY_LIST", HashSet<String>())
            lastImages.currentItem = categorySet!!.size
        }
    }

    fun openCamera(param: LastPicture) {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("URI", param.strUri)
        intent.putExtra("NAME", param.strName)
        startActivityForResult(intent, 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup granted
                    //pickImageFromGallery()
                    initCategory()
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

https://brunch.co.kr/@mystoryg/55
*/