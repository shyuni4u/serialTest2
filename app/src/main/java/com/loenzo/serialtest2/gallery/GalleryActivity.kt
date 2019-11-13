package com.loenzo.serialtest2.gallery

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.loenzo.serialtest2.room.LastPicture
import com.loenzo.serialtest2.R
import com.loenzo.serialtest2.room.LastPictureDB
import com.loenzo.serialtest2.util.daoThread
import com.loenzo.serialtest2.util.getRecentFilePathListFromCategoryName

class GalleryActivity : AppCompatActivity() {
    enum class GalleryState { CAMERA, GIF, VIDEO }

    private lateinit var selectedTitle: String

    private fun refreshViewPager() {
        //Log.e("TEST", "refreshViewPager selectedTitle: $selectedTitle")

        val fragmentList = ArrayList<GalleryCameraFragment>()
        fragmentList.add(GalleryCameraFragment(GalleryState.CAMERA, selectedTitle))
        fragmentList.add(GalleryCameraFragment(GalleryState.GIF, selectedTitle))
        fragmentList.add(GalleryCameraFragment(GalleryState.VIDEO, selectedTitle))

        val viewPager = findViewById<ViewPager>(R.id.galleryViewpager)
        val viewPagerAdapter = GalleryCameraFragmentAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, fragmentList)

        Handler(Looper.getMainLooper()).post {
            viewPager.adapter = viewPagerAdapter

            val tabLayout = findViewById<TabLayout>(R.id.galleryTab)
            tabLayout.setupWithViewPager(viewPager, true)
            tabLayout.getTabAt(0)!!.setText(R.string.camera)
            tabLayout.getTabAt(1)!!.setText(R.string.gif)
            tabLayout.getTabAt(2)!!.setText(R.string.video)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_main)

        selectedTitle = intent.getStringExtra("selected")!!

        findViewById<TextView>(R.id.galleryTitle).apply {
            text = selectedTitle
        }

        /*
        pictureDb = LastPictureDB.getInstance(this)
        daoThread {
            pictureList = pictureDb?.lastPictureDao()?.getAll()!!

            var selectedIdx = 0
            val titles = ArrayList<String>()
            for ((idx, item) in pictureList.withIndex()) {
                titles.add(item.title)
                if (selectedTitle == item.title) {
                    selectedIdx = idx
                }
            }

            list = pictureList.toCollection(ArrayList())

            val spinnerAdapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, titles)
            val spinner = findViewById<Spinner>(R.id.gallerySpinner)
            spinnerAdapter.setDropDownViewResource(R.layout.spinner_normal_dropdown)
            spinner.adapter = spinnerAdapter
            spinner.setSelection(selectedIdx)
            spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedTitle = titles[position]
                    refreshViewPager()
                }
            }
        }
        */

        refreshViewPager()
    }

    inner class GalleryCameraFragmentAdapter(fm: FragmentManager, behavior: Int, private var list: ArrayList<GalleryCameraFragment>): FragmentPagerAdapter(fm, behavior) {
        override fun getItemPosition(`object`: Any): Int {
            return PagerAdapter.POSITION_NONE
        }
        override fun getItem(position: Int): Fragment {
            return list[position]
        }
        override fun getCount(): Int {
            return list.size
        }
    }

    data class GalleryCameraFragment(private val state: GalleryState, private val title: String): Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.gallery_camera, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            if (state == GalleryState.CAMERA) {
                val files = getRecentFilePathListFromCategoryName(title, view.context)
                val recyclerView = view.findViewById<RecyclerView>(R.id.imgList)
                recyclerView.layoutManager = GridLayoutManager(view.context, 3)
                recyclerView.setHasFixedSize(true)
                val adapter = GalleryAdapter(view.context, files)
                recyclerView.adapter = adapter
            }
            //adapter.notifyDataSetChanged()
            /*
            recyclerView.background = when (state) {
                GalleryState.CAMERA -> resources.getDrawable(R.drawable.plaid_on)
                GalleryState.GIF -> resources.getDrawable(R.drawable.alarm_off)
                else -> resources.getDrawable(R.drawable.alarm_on)
            }
            */
        }
    }
}