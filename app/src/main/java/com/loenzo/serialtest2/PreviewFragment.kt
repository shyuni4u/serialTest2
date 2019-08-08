package com.loenzo.serialtest2

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_preview.*
import java.lang.IllegalArgumentException

class PreviewFragment : Fragment() {

    companion object {
        private val TAG = PreviewFragment::class.qualifiedName
        private const val STR_URI = "CATEGORY_RECENT_FILE"
        private const val STR_NAME = "CATEGORY_NAME"

        private const val MAX_WIDTH = 1280
        private const val MAX_HEIGHT = 720

        @JvmStatic
        //fun newInstance(param: LastPicture) = PreviewFragment()
        fun newInstance(param: LastPicture) = PreviewFragment().apply {
            arguments = bundleOf(
                STR_URI to param.strUri,
                STR_NAME to param.strName
            )
        }
    }

    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder

    private lateinit var cameraDevice: CameraDevice
    private val deviceStateCallback = object: CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            Log.d(TAG, "camera device opened")
            cameraDevice = camera
            previewSession()
        }

        override fun onDisconnected(camera: CameraDevice) {
            Log.d(TAG, "camera device disconnected")
            camera.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            Log.d(TAG, "camera device error")
            this@PreviewFragment.activity?.finish()
        }

    }
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler

    private val cameraManager by lazy {
        activity?.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private fun previewSession() {
        val surfaceTexture = previewTextureView.surfaceTexture
        surfaceTexture.setDefaultBufferSize(MAX_WIDTH, MAX_HEIGHT)
        val surface = Surface(surfaceTexture)

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        cameraDevice.createCaptureSession(listOf(surface),
            object: CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "creating capture session failed!")
                }

                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                    captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                }

            }, null)
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera2 Kotlin").also { it.start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun <T> cameraCharacteristics(cameraId: String, key: CameraCharacteristics.Key<T>) : T? {
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        return when (key) {
            CameraCharacteristics.LENS_FACING -> characteristics.get(key)
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP -> characteristics.get(key)
            else -> throw IllegalArgumentException("Key not recognized")
        }
    }

    private fun cameraId(lens: Int) : String {
        var deviceId = listOf<String>()
        try {
            val cameraIdList = cameraManager.cameraIdList
            deviceId = cameraIdList.filter { lens == cameraCharacteristics(it, CameraCharacteristics.LENS_FACING) }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
        return deviceId[0]
    }

    @SuppressLint("MissingPermission")
    private fun connectCamera() {
        val deviceId = cameraId(CameraCharacteristics.LENS_FACING_BACK)
        Log.d(TAG, "deviceId: $deviceId")
        try {
            cameraManager.openCamera(deviceId, deviceStateCallback, backgroundHandler)
        } catch (e: InterruptedException) {
            Log.e(TAG, "Open camera device interrupted while opened")
        }
    }

    private val surfaceListener = object: TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture?, p1: Int, p2: Int) {}

        override fun onSurfaceTextureUpdated(p0: SurfaceTexture?) = Unit

        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture?) = true

        override fun onSurfaceTextureAvailable(p0: SurfaceTexture?, width: Int, height: Int) {
            Log.d(TAG, "textureSurface width: $width, height: $height")
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun openCamera() {
        connectCamera()
    }

    private fun closeCamera() {
        if (this::captureSession.isInitialized)
            captureSession.close()
        if (this::cameraDevice.isInitialized)
            cameraDevice.close()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_preview, container, false)
        val imgBack: ImageView = view.findViewById(R.id.imgBack)
        val btnChange: Button = view.findViewById(R.id.btnChange)
        val barAlpha: SeekBar = view.findViewById(R.id.barAlpha)
        val btnCapture: Button = view.findViewById(R.id.btnCapture)

        btnChange.setOnClickListener {
            Log.i(TAG, "CHANGE BUTTON")
        }

        barAlpha.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, i: Int, b: Boolean) {
                imgBack.alpha = (i * 0.01).toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        btnCapture.setOnClickListener {
            Log.i(TAG, "CAPTURE BUTTON")
        }

        Log.i(TAG, "strUri: ${arguments?.getString(STR_URI)}, strName: ${arguments?.getString(STR_NAME)}")
        val bitmap: Bitmap = BitmapFactory.decodeFile(arguments?.getString(STR_URI))
        imgBack.setImageBitmap(bitmap)
        imgBack.alpha = 0.4F

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }

    override fun onResume() {
        super.onResume()

        startBackgroundThread()
        if (previewTextureView.isAvailable)
            openCamera()
        else
            previewTextureView.surfaceTextureListener = surfaceListener
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }
}