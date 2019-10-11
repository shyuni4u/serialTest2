package com.loenzo.serialtest2.gallery

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.loenzo.serialtest2.room.LastPicture
import com.loenzo.serialtest2.R
import com.loenzo.serialtest2.util.CAMERA_ACTIVITY_SUCCESS
import com.loenzo.serialtest2.util.getRecentFilePathListFromCategoryName

class GalleryActivity : AppCompatActivity() {
    private lateinit var mObject: LastPicture
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: GalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_main)

        mObject = intent.getSerializableExtra("PARAM") as LastPicture

        val data =
            getRecentFilePathListFromCategoryName(mObject.title, this)

        //val snapHelper = SnapHelperOneByOne()
        mRecyclerView = findViewById(R.id.imgList)
        mRecyclerView.layoutManager = GridLayoutManager(this, 3)
        mRecyclerView.setHasFixedSize(true)
        //snapHelper.attachToRecyclerView(mRecyclerView)

        mAdapter = GalleryAdapter(this, data)
        mRecyclerView.adapter = mAdapter
    }

    override fun onBackPressed() {
        val intent = Intent().apply {
            putExtra("RESULT_PARAM", mObject)
        }
        setResult(CAMERA_ACTIVITY_SUCCESS, intent)
        super.onBackPressed()
    }
}