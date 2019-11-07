package com.loenzo.serialtest2.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.loenzo.serialtest2.*
import com.loenzo.serialtest2.category.CategoryAdapter
import com.loenzo.serialtest2.room.LastPicture
import com.loenzo.serialtest2.room.LastPictureDB
import com.loenzo.serialtest2.util.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.system.exitProcess

class CameraActivity : AppCompatActivity () {

    private var pictureDb: LastPictureDB? = null
    private var pictureList = listOf<LastPicture>()

    private lateinit var list: ArrayList<LastPicture>

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: CategoryAdapter

    private var previousTime = 0L
    private var menuState = 0
    private var categoryPosition = 0

    private var x1 = 0F
    private var x2 = 0F

    fun changeMenuFragment(state: Int) {
        val fragment = when (state) {
            MAIN_STATE -> MainMenuFragment()
            CATEGORY_STATE -> CategoryMenuFragment()
            ALPHA_STATE -> AlphaMenuFragment()
            else -> MainMenuFragment()
        }
        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .addToBackStack(null)
            .replace(R.id.camera_top_menu, fragment)
            .commit()
        menuState = state
    }

    fun addRecyclerViewItem(newName: String) {
        var isDuplicated = false
        for (info in pictureList) {
            if (info.title == newName) {
                Toast.makeText(this, resources.getString(R.string.duplicate_name), Toast.LENGTH_SHORT).apply {
                    setGravity(Gravity.BOTTOM, 0, 100)
                }.show()
                isDuplicated = true
            }
        }

        if (!isDuplicated) {
            daoThread {
                val temp = LastPicture(newName)
                pictureDb?.lastPictureDao()?.insert(temp)!!   //  return row number
                pictureList = pictureDb?.lastPictureDao()?.getAll()!!

                list.add(temp)
                Handler(Looper.getMainLooper()).post {
                    mAdapter.notifyItemInserted(pictureList.size)
                    mRecyclerView.smoothScrollToPosition(pictureList.size)
                }
            }
        }
    }

    fun removeRecyclerViewItem(isChecked: Boolean) {
        if (pictureList.size > 1) {
            daoThread {
                var delItem: LastPicture? = null
                for (item in pictureList) {
                    if (item.title == list[categoryPosition].title) {
                        delItem = item
                    }
                }
                pictureDb?.lastPictureDao()?.delete(delItem!!)
                pictureList = pictureDb?.lastPictureDao()?.getAll()!!

                list.remove(list[categoryPosition])
                Handler(Looper.getMainLooper()).post {
                    mAdapter.notifyItemRemoved(categoryPosition)
                }
            }
            if (isChecked) {
                val sdcard: String = Environment.getExternalStorageState()
                val rootDir: File? = when (sdcard != Environment.MEDIA_MOUNTED) {
                    true -> Environment.getRootDirectory()
                    false -> getExternalFilesDir(null)
                }
                if (rootDir != null) {
                    setDirectoryEmpty(rootDir.absolutePath + "/$APP_NAME/${list[categoryPosition].title}")
                }
            }
        } else {
            Toast.makeText(this, resources.getString(R.string.warning_category), Toast.LENGTH_SHORT).show()
        }
    }

    fun getRecentName(): String {
        return list[categoryPosition].title
    }

    fun changeFlashSetting() {
        val prefs = getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE)
        prefsFlash = when (prefsFlash) {
            true -> false
            false -> true
        }
        prefs!!.edit().putBoolean("flash", prefsFlash).apply()
        Handler(Looper.getMainLooper()).post {
            val btnFlash = findViewById<ImageButton>(R.id.btnFlash)
            if (prefsFlash) {
                btnFlash.setImageResource(R.drawable.flash_on)
            } else {
                btnFlash.setImageResource(R.drawable.flash_off)
            }
        }
        closeCamera()
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    fun changePlaidSetting() {
        val prefs = getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE)
        prefsPlaid = when (prefsPlaid) {
            true -> false
            false -> true
        }
        prefs!!.edit().putBoolean("plaid", prefsPlaid).apply()
        Handler(Looper.getMainLooper()).post {
            if (prefsPlaid) {
                imgPlaid.background = resources.getDrawable(R.drawable.plaid, null)
            } else {
                imgPlaid.background = null
            }
        }
    }

    private fun getScreenWidth(): Int {
        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)

        return size.x
    }

    private fun itemWidth(): Int {
        // convert dp to pixel
        // 100 = category_main.text_view_category.width = 100dp
        return (100 * resources.displayMetrics.density + 0.5F).toInt()
    }

    inner class CustomLayoutManager(context: Context?, orientation: Int, reverseLayout: Boolean, private var parentWidth: Int, private var itemWidth: Int): LinearLayoutManager(context, orientation, reverseLayout) {

        private val millisecondsPerInch = 150F

        override fun getPaddingLeft(): Int {
            return (parentWidth / 2f - itemWidth / 2).roundToInt()
        }

        override fun getPaddingRight(): Int {
            return paddingLeft
        }

        override fun smoothScrollToPosition(
            recyclerView: RecyclerView?,
            state: RecyclerView.State?,
            position: Int
        ) {
            val linearSmoothScroller = object: LinearSmoothScroller(recyclerView!!.context) {
                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                    return millisecondsPerInch / displayMetrics!!.densityDpi
                }
            }
            linearSmoothScroller.targetPosition = position
            startSmoothScroll(linearSmoothScroller)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.camera_main)

        MobileAds.initialize(this) {}
        val adView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        changeMenuFragment(MAIN_STATE)

        val prefs = getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE)
        prefsFlash = prefs!!.getBoolean("flash", true)
        prefsPlaid = prefs.getBoolean("plaid", true)

        val snapHelper = LinearSnapHelper()
        val layoutManager = CustomLayoutManager(this, LinearLayoutManager.HORIZONTAL, false, getScreenWidth(), itemWidth())
        mRecyclerView = findViewById(R.id.recycler_view_categories)
        mRecyclerView.layoutManager = layoutManager
        mRecyclerView.setHasFixedSize(true)
        snapHelper.attachToRecyclerView(mRecyclerView)
        mRecyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                //super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val centerView = snapHelper.findSnapView(layoutManager)!!
                    val pos = layoutManager.getPosition(centerView)
                    categoryPosition = pos
                    Log.e("Snapped Item: ", "pos: $pos")
                }
            }
        })

        pictureDb = LastPictureDB.getInstance(this)
        daoThread {
            pictureList = pictureDb?.lastPictureDao()?.getAll()!!

            list = pictureList.toCollection(ArrayList())
            mAdapter = CategoryAdapter(this, list)
            mRecyclerView.adapter = mAdapter

            mObject = pictureList.first()
        }

        textureView = findViewById(R.id.textureView)

        /**
         * recover top menu state [MAIN_STATE]
         */
        textureView.setOnClickListener {
            if (menuState != MAIN_STATE) changeMenuFragment(MAIN_STATE)
        }
        /**
         * when swiping [textureView] then move position of [mRecyclerView]
         */
        textureView.setOnTouchListener { _, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> x1 = event.x
                MotionEvent.ACTION_UP -> {
                    x2 = event.x
                    val deltaX = x2 - x1
                    val snapView = snapHelper.findSnapView(layoutManager)!!
                    val snapPosition = layoutManager.getPosition(snapView)
                    if (abs(deltaX) > MIN_DISTANCE && deltaX > 0) {
                        if (snapPosition > 0) {
                            mRecyclerView.smoothScrollToPosition(snapPosition - 1)
                        }
                    } else if (abs(deltaX) > MIN_DISTANCE && deltaX < 0) {
                        if (snapPosition < pictureList.size) {
                            mRecyclerView.smoothScrollToPosition(snapPosition + 1)
                        }
                    }
                }
            }
            super.onTouchEvent(event)
        }

        val sdcard: String = Environment.getExternalStorageState()
        val f = when (sdcard != Environment.MEDIA_MOUNTED) {
            true -> Environment.getRootDirectory()
            false -> getExternalFilesDir(Environment.DIRECTORY_DCIM)
        }
        FILE_PATH = f!!.absolutePath + "/$APP_NAME/"

        imgBackground = findViewById(R.id.imgBack)

        /**
         * setting [imgPlaid] background
         */
        imgPlaid = findViewById(R.id.imgPlaid)
        if (prefsPlaid) {
            imgPlaid.background = resources.getDrawable(R.drawable.plaid, null)
        } else {
            imgPlaid.background = null
        }
        //val barAlpha: SeekBar = this.findViewById(R.id.barAlpha)
        //val imgRecent: ImageView = this.findViewById(R.id.imgRecent)
        val btnCapture: Button = this.findViewById(R.id.btnCapture)
        val btnChange: ImageButton = this.findViewById(R.id.btnChange)

        /*
        val recentFilePath = getRecentFilePathFromCategoryName(mObject.title, this)
        if (recentFilePath == null) {
            barAlpha.visibility = View.INVISIBLE
            imgBackground.background = null
        } else {
            barAlpha.visibility = View.VISIBLE
            Glide.with(this)
                .load(recentFilePath)
                .thumbnail(0.1F)
                .into(imgBackground)
            imgBackground.alpha = mObject.cameraAlpha
        }

        //barAlpha.progress = (mObject.cameraAlpha * 100).toInt()
        barAlpha.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            override fun onProgressChanged(seekBar: SeekBar?, i: Int, b: Boolean) {
                imgBackground.alpha = (i * 0.01).toFloat()
                //mObject.cameraAlpha = (i * 0.01).toFloat()
            }
        })

        Glide.with(this)
            .load(
                getRecentFilePathFromCategoryName(
                    mObject.title,
                    this
                )
            )
            .apply(RequestOptions().circleCrop())
            .thumbnail(0.1F)
            .into(imgRecent)
        */

        btnCapture.setOnClickListener {
            lockFocus()
        }

        btnChange.setOnClickListener {
            closeCamera()
            if (textureView.isAvailable) {
                /*
                mObject.cameraDirection = when (mObject.cameraDirection == CameraCharacteristics.LENS_FACING_BACK) {
                    true -> CameraCharacteristics.LENS_FACING_FRONT
                    false -> CameraCharacteristics.LENS_FACING_BACK
                }
                 */
                openCamera(textureView.width, textureView.height)
            } else {
                textureView.surfaceTextureListener = surfaceTextureListener
            }
        }
    }

    override fun onBackPressed() {
        if (menuState == MAIN_STATE) {
            val currentTime = System.currentTimeMillis()
            if ((currentTime - previousTime) <= CLOSE_INTERVAL_TIME) {
                closeCamera()
                finishAffinity()
                moveTaskToBack(true)
                exitProcess(0)
            } else {
                previousTime = currentTime
                Toast.makeText(this, getString(R.string.close_app), Toast.LENGTH_SHORT).show()
            }
        } else {
            menuState = MAIN_STATE
            changeMenuFragment(MAIN_STATE)
        }
    }

    override fun onDestroy() {
        closeCamera()
        super.onDestroy()
    }

    /**
     * [TextureView.SurfaceTextureListener] handles several lifecycle events on a
     * [TextureView].
     */
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onSurfaceTextureAvailable(texture: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }

        override fun onSurfaceTextureSizeChanged(texture: SurfaceTexture, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureDestroyed(texture: SurfaceTexture) = true

        override fun onSurfaceTextureUpdated(texture: SurfaceTexture) = Unit
    }

    /**
     * ID of the current [CameraDevice].
     */
    private lateinit var cameraId: String

    /**
     * An [AutoFitTextureView] for camera preview.
     */
    private lateinit var textureView: AutoFitTextureView

    /**
     * An [ImageView] for background
     */
    private lateinit var imgBackground: AutoFitImageView
    private lateinit var imgPlaid: ImageView

    /**
     * A [CameraCaptureSession] for camera preview.
     */
    private var captureSession: CameraCaptureSession? = null

    /**
     * A reference to the opened [CameraDevice].
     */
    private var cameraDevice: CameraDevice? = null

    /**
     * The [android.util.Size] of camera preview.
     */
    private lateinit var previewSize: Size

    /**
     * [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.
     */
    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            this@CameraActivity.cameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@CameraActivity.cameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            onDisconnected(cameraDevice)
            this@CameraActivity.finish()
        }

    }

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private var backgroundThread: HandlerThread? = null

    /**
     * A [Handler] for running tasks in the background.
     */
    private var backgroundHandler: Handler? = null

    /**
     * An [ImageReader] that handles still image capture.
     */
    private var imageReader: ImageReader? = null

    /**
     * This is the output file for our picture.
     */

    /**
     * This a callback object for the [ImageReader]. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    @SuppressLint("SimpleDateFormat")
    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {
        val sdf = SimpleDateFormat("yyMMddhhmmss")
        val currentDate = sdf.format(Date())

        val dir = File(FILE_PATH + list[categoryPosition].title)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, mObject.title + "_$currentDate.jpg")

        backgroundHandler?.post(
            ImageSaver(
                it.acquireNextImage(),
                file,
                this,
                findViewById(R.id.imgRecent),
                CameraCharacteristics.LENS_FACING_FRONT
                //mObject.cameraDirection
            )
        )
    }

    /**
     * [CaptureRequest.Builder] for the camera preview
     */
    private lateinit var previewRequestBuilder: CaptureRequest.Builder

    /**
     * [CaptureRequest] generated by [.previewRequestBuilder]
     */
    private lateinit var previewRequest: CaptureRequest

    /**
     * The current state of camera state for taking pictures.
     *
     * @see .captureCallback
     */
    private var state = STATE_PREVIEW

    /**
     * A [Semaphore] to prevent the app from exiting before closing the camera.
     */
    private val cameraOpenCloseLock = Semaphore(1)

    /**
     * Whether the current camera device supports Flash or not.
     */
    private var flashSupported = false

    /**
     * Orientation of the camera sensor
     */
    private var sensorOrientation = 0

    /**
     * A [CameraCaptureSession.CaptureCallback] that handles events related to JPEG capture.
     */
    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (state) {
                STATE_PREVIEW -> Unit // Do nothing when the camera preview is working normally.
                STATE_WAITING_LOCK -> capturePicture(result)
                STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                        aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                        aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        state =
                            STATE_WAITING_NON_PRECAPTURE
                    }
                }
                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        state =
                            STATE_PICTURE_TAKEN
                        captureStillPicture()
                    }
                }
            }
        }

        private fun capturePicture(result: CaptureResult) {
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                captureStillPicture()
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                // CONTROL_AE_STATE can be null on some devices
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    state =
                        STATE_PICTURE_TAKEN
                    captureStillPicture()
                } else {
                    runPrecaptureSequence()
                }
            }
        }

        override fun onCaptureProgressed(session: CameraCaptureSession,
                                         request: CaptureRequest,
                                         partialResult: CaptureResult
        ) {
            process(partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession,
                                        request: CaptureRequest,
                                        result: TotalCaptureResult
        ) {
            process(result)
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Log.e(TAG, "childFragmentManager: FRAGMENT_DIALOG")
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.size != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "childFragmentManager: FRAGMENT_DIALOG")
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val manager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
                /*
                if (cameraDirection != null && cameraDirection != mObject.cameraDirection) {
                    continue
                }
                 */

                val map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP) ?: continue

                // XXX: image size
                // For still image captures, we use the largest available size.
                val largest = Collections.max(
                    listOf(*map.getOutputSizes(ImageFormat.JPEG)),
                    CompareSizesByArea()
                )
                imageReader = ImageReader.newInstance(largest.width, largest.height,
                    ImageFormat.JPEG, /*maxImages*/ 2).apply {
                    setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
                }

                // Find out if we need to swap dimension to get the preview size relative to sensor
                // coordinate.
                val displayRotation = this.windowManager.defaultDisplay.rotation

                // XXX: image rotation
                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!
                val swappedDimensions = areDimensionsSwapped(displayRotation)

                val displaySize = Point()
                this.windowManager.defaultDisplay.getSize(displaySize)
                val rotatedPreviewWidth = if (swappedDimensions) height else width
                val rotatedPreviewHeight = if (swappedDimensions) width else height
                var maxPreviewWidth = if (swappedDimensions) displaySize.y else displaySize.x
                var maxPreviewHeight = if (swappedDimensions) displaySize.x else displaySize.y

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth =
                    MAX_PREVIEW_WIDTH
                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight =
                    MAX_PREVIEW_HEIGHT

                // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                previewSize =
                    chooseOptimalSize(
                        map.getOutputSizes(SurfaceTexture::class.java),
                        rotatedPreviewWidth, rotatedPreviewHeight,
                        maxPreviewWidth, maxPreviewHeight,
                        largest
                    )

                // We fit the aspect ratio of TextureView to the size of preview we picked.
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.width, previewSize.height)
                    imgBackground.setAspectRatio(previewSize.width, previewSize.height, imgPlaid)
                } else {
                    textureView.setAspectRatio(previewSize.height, previewSize.width)
                    imgBackground.setAspectRatio(previewSize.height, previewSize.width, imgPlaid)
                }

                // Check if the flash is supported.
                flashSupported =
                    characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                this.cameraId = cameraId

                // We've found a viable camera and finished setting up member variables,
                // so we don't need to iterate through other available cameras.
                return
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            Log.e(TAG, "childFragmentManager: FRAGMENT_DIALOG")
        }

    }

    /**
     * Determines if the dimensions are swapped given the phone's current rotation.
     *
     * @param displayRotation The current rotation of the display
     *
     * @return true if the dimensions are swapped, false otherwise.
     */
    private fun areDimensionsSwapped(displayRotation: Int): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    swappedDimensions = true
                }
            }
            else -> {
                Log.e(TAG, "Display rotation is invalid: $displayRotation")
            }
        }
        return swappedDimensions
    }

    /**
     * Opens the camera specified by [CameraActivity.cameraId].
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private fun openCamera(width: Int, height: Int) {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
            return
        }
        setUpCameraOutputs(width, height)
        configureTransform(width, height)
        val manager = this.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            // Wait for camera to open - 2.5 seconds is sufficient
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    /**
     * Closes the current [CameraDevice].
     */
    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader?.close()
            imageReader = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread!!.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     * Creates a new [CameraCaptureSession] for camera preview.
     */
    private fun createCameraPreviewSession() {
        try {
            val texture = textureView.surfaceTexture

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(previewSize.width, previewSize.height)

            // This is the output Surface we need to start preview.
            val surface = Surface(texture)

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )
            previewRequestBuilder.addTarget(surface)

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice?.createCaptureSession(
                listOf(surface, imageReader?.surface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (cameraDevice == null) return

                        // When the session is ready, we start displaying the preview.
                        captureSession = cameraCaptureSession
                        try {
                            // Auto focus should be continuous for camera preview.
                            previewRequestBuilder.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                            // Flash is automatically enabled when necessary.
                            setAutoFlash(previewRequestBuilder)

                            // Finally, we start displaying the camera preview.
                            previewRequest = previewRequestBuilder.build()
                            captureSession?.setRepeatingRequest(previewRequest,
                                captureCallback, backgroundHandler)
                        } catch (e: CameraAccessException) {
                            Log.e(TAG, e.toString())
                        }

                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Log.e(TAG, "onConfigureFailed")
                    }
                }, null)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     * Configures the necessary [android.graphics.Matrix] transformation to `textureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `textureView` is fixed.
     *
     * @param viewWidth  The width of `textureView`
     * @param viewHeight The height of `textureView`
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val rotation = this.windowManager.defaultDisplay.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            val scale = max(viewHeight.toFloat() / previewSize.height, viewWidth.toFloat() / previewSize.width)
            with(matrix) {
                setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        textureView.setTransform(matrix)
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private fun lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START)
            // Tell #captureCallback to wait for the lock.
            state = STATE_WAITING_LOCK
            captureSession?.capture(previewRequestBuilder.build(), captureCallback,
                backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in [.captureCallback] from [.lockFocus].
     */
    private fun runPrecaptureSequence() {
        try {
            // This is how to tell the camera to trigger.
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START)
            // Tell #captureCallback to wait for the precapture sequence to be set.
            state = STATE_WAITING_PRECAPTURE
            captureSession?.capture(previewRequestBuilder.build(), captureCallback,
                backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * [.captureCallback] from both [.lockFocus].
     */
    private fun captureStillPicture() {
        try {
            if (cameraDevice == null) return
            val rotation = this.windowManager.defaultDisplay.rotation

            // This is the CaptureRequest.Builder that we use to take a picture.
            val captureBuilder = cameraDevice?.createCaptureRequest(
                CameraDevice.TEMPLATE_STILL_CAPTURE)?.apply {
                addTarget(imageReader?.surface!!)

                // Sensor orientation is 90 for most devices, or 270 for some devices (eg. Nexus 5X)
                // We have to take that into account and rotate JPEG properly.
                // For devices with orientation of 90, we return our mapping from ORIENTATIONS.
                // For devices with orientation of 270, we need to rotate the JPEG 180 degrees.
                set(
                    CaptureRequest.JPEG_ORIENTATION,
                    (ORIENTATIONS.get(rotation) + sensorOrientation + 270) % 360)

                // Use the same AE and AF modes as the preview.
                set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
            }?.also { setAutoFlash(it) }
            val captureCallback = object : CameraCaptureSession.CaptureCallback() {

                override fun onCaptureCompleted(session: CameraCaptureSession,
                                                request: CaptureRequest,
                                                result: TotalCaptureResult
                ) {
                    unlockFocus()
                }
            }

            captureSession?.apply {
                stopRepeating()
                abortCaptures()
                capture(captureBuilder!!.build(), captureCallback, null)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
            setAutoFlash(previewRequestBuilder)
            captureSession?.capture(previewRequestBuilder.build(), captureCallback,
                backgroundHandler)
            // After this, the camera will go back to the normal state of preview.
            state = STATE_PREVIEW
            captureSession?.setRepeatingRequest(previewRequest, captureCallback,
                backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported && prefsFlash) {
            requestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
        }
    }

    companion object {
        var prefsFlash = true
        var prefsPlaid = true

        /**
         * Conversion from screen rotation to JPEG orientation.
         */
        private val ORIENTATIONS = SparseIntArray()
        //private val FRAGMENT_DIALOG = "dialog"

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        /**
         * Tag for the [Log].
         */
        private const val TAG = "CameraActivity"

        private lateinit var FILE_PATH: String

        private lateinit var mObject: LastPicture

        /**
         * Camera state: Showing camera preview.
         */
        private const val STATE_PREVIEW = 0

        /**
         * Camera state: Waiting for the focus to be locked.
         */
        private const val STATE_WAITING_LOCK = 1

        /**
         * Camera state: Waiting for the exposure to be precapture state.
         */
        private const val STATE_WAITING_PRECAPTURE = 2

        /**
         * Camera state: Waiting for the exposure state to be something other than precapture.
         */
        private const val STATE_WAITING_NON_PRECAPTURE = 3

        /**
         * Camera state: Picture was taken.
         */
        private const val STATE_PICTURE_TAKEN = 4

        /**
         * Max preview width that is guaranteed by Camera2 API
         */
        private const val MAX_PREVIEW_WIDTH = 1920

        /**
         * Max preview height that is guaranteed by Camera2 API
         */
        private const val MAX_PREVIEW_HEIGHT = 1080

        private const val REQUEST_CAMERA_PERMISSION = 1

        /**
         * Given `choices` of `Size`s supported by a camera, choose the smallest one that
         * is at least as large as the respective texture view size, and that is at most as large as
         * the respective max size, and whose aspect ratio matches with the specified value. If such
         * size doesn't exist, choose the largest one that is at most as large as the respective max
         * size, and whose aspect ratio matches with the specified value.
         *
         * @param choices           The list of sizes that the camera supports for the intended
         *                          output class
         * @param textureViewWidth  The width of the texture view relative to sensor coordinate
         * @param textureViewHeight The height of the texture view relative to sensor coordinate
         * @param maxWidth          The maximum width that can be chosen
         * @param maxHeight         The maximum height that can be chosen
         * @param aspectRatio       The aspect ratio
         * @return The optimal `Size`, or an arbitrary one if none were big enough
         */
        @JvmStatic private fun chooseOptimalSize(
            choices: Array<Size>,
            textureViewWidth: Int,
            textureViewHeight: Int,
            maxWidth: Int,
            maxHeight: Int,
            aspectRatio: Size
        ): Size {

            // Collect the supported resolutions that are at least as big as the preview Surface
            val bigEnough = ArrayList<Size>()
            // Collect the supported resolutions that are smaller than the preview Surface
            val notBigEnough = ArrayList<Size>()
            val w = aspectRatio.width
            val h = aspectRatio.height
            for (option in choices) {
                if (option.width <= maxWidth && option.height <= maxHeight &&
                    option.height == option.width * h / w) {
                    if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                        bigEnough.add(option)
                    } else {
                        notBigEnough.add(option)
                    }
                }
            }

            // Pick the smallest of those big enough. If there is no one big enough, pick the
            // largest of those not big enough.
            return when {
                bigEnough.size > 0 -> Collections.min(bigEnough,
                    CompareSizesByArea()
                )
                notBigEnough.size > 0 -> Collections.max(notBigEnough,
                    CompareSizesByArea()
                )
                else -> {
                    Log.e(TAG, "Couldn't find any suitable preview size")
                    choices[0]
                }
            }
        }
    }
}