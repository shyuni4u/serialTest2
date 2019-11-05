package com.loenzo.serialtest2.help

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.loenzo.serialtest2.R
import com.loenzo.serialtest2.camera.CameraActivity
import com.loenzo.serialtest2.util.APPLICATION_SUCCESS

class ManualActivity : AppCompatActivity() {

    private var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manual_main)

        val list = ArrayList<ManualFragment>()

        prefs = getSharedPreferences("com.lorenzo.sp", Context.MODE_PRIVATE)
        list.add(ManualFragment(R.layout.manual_app_info))
        list.add(ManualFragment(R.layout.manual_camera_info))
        list.add(ManualFragment(R.layout.manual_category_info))
        if (!prefs!!.getBoolean("skip_manual", false)) {
            list.add(ManualFragment(R.layout.manual_first_info))
        }

        val viewPager = findViewById<ViewPager>(R.id.manual_viewpager)
        val viewPagerAdapter = ManualFragmentAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, list)

        viewPager.adapter = viewPagerAdapter

        val tabLayout = findViewById<TabLayout>(R.id.manual_tab_layout)
        tabLayout.setupWithViewPager(viewPager, true)
    }

    override fun onBackPressed() {
        val intent = Intent().apply {
            putExtra("RESULT_PARAM", "finish")
        }
        setResult(APPLICATION_SUCCESS, intent)
        super.onBackPressed()
    }

    inner class ManualFragmentAdapter(fm: FragmentManager, behavior: Int, private var list: ArrayList<ManualFragment>): FragmentPagerAdapter(fm, behavior) {
        override fun getItem(position: Int): ManualFragment {
            return list[position]
        }

        override fun getCount(): Int {
            return list.size
        }
    }

    data class ManualFragment(private val rId: Int): Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(rId, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val requestOptions = RequestOptions().transform(CenterCrop(), RoundedCorners(16))
            if (rId == R.layout.manual_app_info) {
                val imageView = view.findViewById<ImageView>(R.id.image_manual_app_info)
                Glide.with(this)
                    .load(resources.getDrawable(R.drawable.manual_app_info, null))
                    .thumbnail(0.1F)
                    .apply(requestOptions)
                    .into(imageView)
            } else if(rId == R.layout.manual_first_info) {
                //prefs!!.edit().putBoolean("skip_manual", false).apply()
            }
        }
    }

}

