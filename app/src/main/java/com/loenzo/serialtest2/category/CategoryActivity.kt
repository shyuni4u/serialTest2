package com.loenzo.serialtest2.category

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.jobdispatcher.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.loenzo.serialtest2.room.LastPicture
import com.loenzo.serialtest2.R
import com.loenzo.serialtest2.alarm.AlarmBroadcastReceiver
import com.loenzo.serialtest2.camera.CameraActivity
import com.loenzo.serialtest2.encoder.SnapHelperOneByOne
import com.loenzo.serialtest2.gallery.GalleryActivity
import com.loenzo.serialtest2.help.HelpActivity
import com.loenzo.serialtest2.room.LastPictureDB
import com.loenzo.serialtest2.util.*
import kotlin.collections.ArrayList

class CategoryActivity : AppCompatActivity() {    //  like static
    companion object {
        private val permissionsRequired = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA)
    }

    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: CategoryAdapter

    private lateinit var mDispatcher: FirebaseJobDispatcher

    private var pictureDb: LastPictureDB? = null
    private var pictureList = listOf<LastPicture>()

    private var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)

        MobileAds.initialize(this) {}

        val mAdView = findViewById<AdView>(R.id.adView)
        val adRequest = AdRequest.Builder().build()

        mAdView.adListener = object: AdListener() {
            override fun onAdFailedToLoad(errorCode: Int) {
                super.onAdFailedToLoad(errorCode)
                Log.i("onAdFailedToLoad", "errorCode: $errorCode")
            }
        }
        mAdView.loadAd(adRequest)

        mDispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))

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

    /**
     * set notification
     */
    fun scheduleNotification(millisecond: Long, title: String, id: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(title, name, importance).apply {
                description = title
            }
            // Register the channel with the system
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        scheduleNotificationStop(title)

        val jobParameters = Bundle()
        jobParameters.putLong("millisecond", millisecond)
        jobParameters.putLong("id", id)
        jobParameters.putString("title", title)

        val job = mDispatcher.newJobBuilder()
            .setService(NotificationJobFireBaseService::class.java)
            .setTag(title)
            .setTrigger(Trigger.executionWindow(60 * 60 * 24, 60 * 60 * 24 + 10))
            //.setTrigger(Trigger.executionWindow(0, 60 * 60 * 24))
            .setRecurring(true)
            .setReplaceCurrent(true)
            .setLifetime(Lifetime.FOREVER)
            .setExtras(jobParameters)
            .setConstraints(Constraint.ON_ANY_NETWORK)
            .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
            .build()
        mDispatcher.mustSchedule(job)
    }

    fun scheduleNotificationStop(title: String) {
        mDispatcher.cancel(title)
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
                        resultObject.alarmMilliseconds = System.currentTimeMillis()
                        scheduleNotification(
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

    override fun onBackPressed() {
        val intent = Intent().apply {
            putExtra("RESULT_PARAM", "finish")
        }
        setResult(APPLICATION_SUCCESS, intent)
        super.onBackPressed()
    }

    override fun onDestroy() {
        LastPictureDB.destroyInstance()
        pictureDb = null
        super.onDestroy()
    }
}

class NotificationJobFireBaseService : JobService() {
    override fun onStopJob(job: JobParameters): Boolean {
        return false
    }

    override fun onStartJob(job: JobParameters): Boolean {
        val millisecond = job.extras!!.get("millisecond") as Long
        val id= job.extras!!.get("id") as Long
        val title = job.extras!!.get("title") as String

        val intent = Intent(this, AlarmBroadcastReceiver::class.java)
        intent.putExtra(AlarmBroadcastReceiver.ID, id)
        intent.putExtra(AlarmBroadcastReceiver.TITLE, title)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), pendingIntent)
        manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millisecond, pendingIntent)

        jobFinished(job, true)
        return false
    }

}
