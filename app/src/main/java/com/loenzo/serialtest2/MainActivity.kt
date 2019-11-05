package com.loenzo.serialtest2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.loenzo.serialtest2.camera.CameraActivity
import com.loenzo.serialtest2.help.ManualActivity
import com.loenzo.serialtest2.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private val permissionsRequired = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)
    }

    private var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getUsePermission()
        if (checkPermissions().isEmpty()) {
            applicationStart()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup granted
                    applicationStart()
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

        if (requestCode == APPLICATION_SUCCESS && data != null) {
            finishAffinity()
            moveTaskToBack(true)
        }
    }

    private fun applicationStart() {
        prefs = getSharedPreferences("com.lorenzo.sp", Context.MODE_PRIVATE)
        if (prefs!!.getBoolean("skip_manual", false)) {
            //prefs!!.edit().putBoolean("skip_manual", false).apply()
            val intent = Intent(this, CameraActivity::class.java)
            startActivityForResult(intent, APPLICATION_SUCCESS)
        } else {
            val intent = Intent(this, ManualActivity::class.java)
            startActivityForResult(intent, APPLICATION_SUCCESS)
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
}
