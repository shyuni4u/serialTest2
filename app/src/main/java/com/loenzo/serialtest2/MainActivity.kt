package com.loenzo.serialtest2

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
import android.hardware.camera2.*
import android.view.Surface
import androidx.fragment.app.Fragment
import java.util.concurrent.locks.ReentrantLock
import android.Manifest.permission as _permission

class MainActivity : AppCompatActivity() {
    //  like static
    companion object {
        private const val APP_NAME = "MEMORIA"

        // image pick code
        private const val IMAGE_PICK_CODE = 1001

        //  make folder code
        private const val MAKE_FOLDER_CODE = 1002

        // Permission code
        private const val PERMISSION_CODE = 1000

        private val permissionsRequired = arrayOf(
            _permission.WRITE_EXTERNAL_STORAGE,
            _permission.READ_EXTERNAL_STORAGE,
            _permission.CAMERA)

        private val sharedPermissionLock = ReentrantLock()

        var lasts: ArrayList<LastPicture> = ArrayList()
        var categories: ArrayList<String> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        getUsePermission(this)

        if (checkPermissions().isEmpty()) {
            initCategory(this)
        }
    }

    private fun getUsePermission(context: Context) {
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
        makeViewPager()
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragmentContainer, fragment)
        fragmentTransaction.commit()
    }

    fun makeViewPager(moveLast: Boolean = false) {
        lastImages.adapter = CategoryFragmentAdapter(supportFragmentManager)
        if (moveLast) {
            lastImages.currentItem = lasts.size
        }
    }

    fun openCamera() {
        replaceFragment(PreviewFragment.newInstance())
        /*
        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        if (cameraManager.cameraIdList.isEmpty())   return

        val frontCamera = cameraManager.cameraIdList[LENS_FACING_FRONT]

        cameraManager.openCamera(frontCamera, object: CameraDevice.StateCallback () {
            override fun onDisconnected(cameraDevice: CameraDevice) {
                Log.i("CAMERA", "onDisconnected")
            }

            override fun onError(cameraDevice: CameraDevice, p1: Int) {
                Log.i("CAMERA", "onError")
            }

            override fun onOpened(cameraDevice: CameraDevice) {
                Log.i("CAMERA", "onOpened")
                // use the camera
                val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
                cameraCharacteristics[CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP]?.let {
                    streamConfigurationMap -> streamConfigurationMap.getOutputSizes(ImageFormat.YUV_420_888)?.let {
                    yuvSizes -> val previewSize = yuvSizes.last()

                    //  cont.
                    val displayRotation = windowManager.defaultDisplay.rotation

                    val swappedDimensions = areDimensionsSwapped(displayRotation, cameraCharacteristics)

                    val rotatedPreviewWidth = if (swappedDimensions) previewSize.height
                    else previewSize.width
                    val rotatedPreviewHeight = if (swappedDimensions) previewSize.width
                    else previewSize.height

                    surfaceView.holder.setFixedSize(rotatedPreviewWidth, rotatedPreviewHeight)
                    }
                }
            }
        }, Handler {true} )
        */
    }

    private fun areDimensionsSwapped(displayRotation: Int, cameraCharacteristics: CameraCharacteristics): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 90 || cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 0 || cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
                // invalid display rotation
            }
        }
        return swappedDimensions
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