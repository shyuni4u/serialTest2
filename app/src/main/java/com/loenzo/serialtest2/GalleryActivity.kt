package com.loenzo.serialtest2

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GalleryActivity : AppCompatActivity() {
    private lateinit var mObject: LastPicture
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mAdapter: GalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_main)

        mObject = intent.getSerializableExtra("PARAM") as LastPicture

        val data = getRecentFilePathListFromCategoryName(mObject.title, this)
        Log.i("GalleryActivity", "data size: ${data.size}")

        mRecyclerView = findViewById(R.id.imgList)
        mRecyclerView.layoutManager = GridLayoutManager(this, 3)
        mRecyclerView.setHasFixedSize(true)

        mAdapter = GalleryAdapter(this, data)
        mRecyclerView.adapter = mAdapter
    }
}