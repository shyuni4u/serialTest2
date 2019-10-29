package com.loenzo.serialtest2

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.*
import android.view.Gravity
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.*
import com.loenzo.serialtest2.camera.CameraActivity
import com.loenzo.serialtest2.encoder.SnapHelperOneByOne
import com.loenzo.serialtest2.gallery.GalleryActivity
import com.loenzo.serialtest2.help.HelpActivity
import com.loenzo.serialtest2.room.LastPicture
import com.loenzo.serialtest2.room.LastPictureDB
import com.loenzo.serialtest2.util.*
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

    private var pictureDb: LastPictureDB? = null
    private var pictureList = listOf<LastPicture>()

    private var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        MobileAds.initialize(this) {}

        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        getUsePermission()
        if (checkPermissions().isEmpty()) {
            initSetting()
        }
    }

    override fun onResume() {
        super.onResume()
        if (prefs != null) {
            if (prefs!!.getBoolean("firstRun", true)) {
                openHelp()
                prefs!!.edit().putBoolean("firstRun", false).apply()
            }
        }
    }

    private fun initSetting() {
        var newName = resources.getString(R.string.temp)
        prefs = getSharedPreferences("com.loenzo.overcam", Context.MODE_PRIVATE)

        if (prefs!!.getBoolean("firstRun", true)) {
            val addCategoryName = EditText(this)
            val builder = AlertDialog.Builder(this)

            builder.setTitle(resources.getString(R.string.add_category_title))
            builder.setView(addCategoryName)
            builder.setPositiveButton(resources.getString(R.string.add)) { _, _ -> run {
                    newName = addCategoryName.text.toString()
                    pictureDb = LastPictureDB.getInstance(this, newName)
                    DaoThread {
                        pictureList = pictureDb?.lastPictureDao()?.getAll()!!
                        initCategory()
                    }
                }
            }
            builder.setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> run {
                pictureDb = LastPictureDB.getInstance(this, newName)
                DaoThread {
                    pictureList = pictureDb?.lastPictureDao()?.getAll()!!
                    initCategory()
                }
            } }
            builder.setCancelable(false).create().show()
        } else {
            pictureDb = LastPictureDB.getInstance(this, newName)
            DaoThread {
                pictureList = pictureDb?.lastPictureDao()?.getAll()!!
                initCategory()
            }
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
                requestPermissions(requests.toArray(arr),
                    PERMISSION_CODE
                )
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
        Handler(Looper.getMainLooper()).post {
            val snapHelper = SnapHelperOneByOne()
            mRecyclerView = findViewById(R.id.lastImages)
            mRecyclerView.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            mRecyclerView.setHasFixedSize(true)
            snapHelper.attachToRecyclerView(mRecyclerView)

            mAdapter = CategoryAdapter(
                this,
                pictureList.toCollection(ArrayList()),
                mRecyclerView,
                pictureDb
            )
            mRecyclerView.adapter = mAdapter
        }
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

    fun openHelp() {
        val intent = Intent(this, HelpActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup granted
                    initSetting()
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
            DaoThread { pictureList = pictureDb?.lastPictureDao()?.getAll()!! }
            var updateIndex = 0

            // find title
            for ((index, info) in pictureList.withIndex()) {
                if (info.title == resultObject.title) {
                    if (!resultObject.alarmState && !resultObject.flagCamera) {
                        resultObject.flagCamera = true
                        resultObject.alarmState = true
                        resultObject.alarmMilliseconds = System.currentTimeMillis() - 5 * 60 * 1000
                        scheduleNotification(
                            this,
                            resultObject.alarmMilliseconds,
                            resultObject.title,
                            resultObject.id
                        )
                        mAdapter.changeAlarmState(index)
                    }
                    updateIndex = index
                }
            }
            DaoThread { pictureDb?.lastPictureDao()?.update(resultObject) }
            mAdapter.notifyItemChanged(updateIndex)
        }
    }

    override fun onDestroy() {
        LastPictureDB.destroyInstance()
        pictureDb = null
        super.onDestroy()
    }
}