package com.loenzo.serialtest2

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.view.Gravity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import android.Manifest.permission as _permission

class MainActivity : AppCompatActivity() {
    //  like static
    companion object {
        private val permissionsRequired = arrayOf(
            _permission.WRITE_EXTERNAL_STORAGE,
            _permission.READ_EXTERNAL_STORAGE,
            _permission.CAMERA)
    }

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        getUsePermission()
        if (checkPermissions().isEmpty()) {
            MobileAds.initialize(this) {}
            val mAdView = findViewById<AdView>(R.id.adView)
            val adRequest = AdRequest.Builder().build()
            mAdView.loadAd(adRequest)

            readSetting()
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

    private fun initCategory() {
        val snapHelper = SnapHelperOneByOne()
        mRecyclerView = findViewById(R.id.lastImages)
        mRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView.setHasFixedSize(true)
        snapHelper.attachToRecyclerView(mRecyclerView)

        mAdapter = CategoryAdapter(this, readSetting().toCollection(ArrayList()), mRecyclerView)
        mRecyclerView.adapter = mAdapter
    }

    fun openGallery(categoryInfo: LastPicture) {
        val intent = Intent(this, GalleryActivity::class.java)
        intent.putExtra("PARAM", categoryInfo)
        startActivityForResult(intent, CAMERA_ACTIVITY_SUCCESS)
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
                    readSetting()
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

        if (resultCode == Activity.RESULT_OK && requestCode == SELECT_MEDIA_SUCCESS && data != null) {
            //val selectedVideo = getRealPathFromURI(data.data!!, context!!)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                this.data = data.data
                this.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

        if (requestCode == CAMERA_ACTIVITY_SUCCESS && data != null) {
            val resultObject = data.getSerializableExtra("RESULT_PARAM") as LastPicture
            val categoryInfoArray = readSetting()
            var updateIndex = 0

            // find title
            for ((index, info) in categoryInfoArray.withIndex()) {
                if (info.title == resultObject.title) {
                    if (!resultObject.alarmState && !resultObject.flagCamera) {
                        resultObject.flagCamera = true
                        resultObject.alarmState = true
                        resultObject.alarmMilliseconds = System.currentTimeMillis()
                        scheduleNotification(this, resultObject.alarmMilliseconds, resultObject.title, resultObject.id)
                    }
                    info.copy(resultObject)
                    updateIndex = index
                }
            }
            writeSetting(categoryInfoArray)
            mAdapter.changeAlarmState(updateIndex)
            mAdapter.notifyItemChanged(updateIndex)
        }
    }
}