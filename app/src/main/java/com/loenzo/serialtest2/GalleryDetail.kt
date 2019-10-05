package com.loenzo.serialtest2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide

class GalleryDetail : AppCompatActivity() {
    class SectionPagerAdapter(fm: FragmentManager, private val mList: ArrayList<PlaceholderFragment>) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int): Fragment {
            return mList[position]
        }

        override fun getCount(): Int {
            return mList.size
        }

    }

    class PlaceholderFragment : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View? = inflater.inflate(R.layout.gallery_detail_item, container, false)

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val imageView: ImageView = view.findViewById(R.id.imgGalleryDetail)
            val url = arguments!!.get("PARAM") as String

            Glide.with(context!!)
                .load(url)
                .thumbnail(0.1F)
                .into(imageView)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gallery_detail)

        val mToolbar:Toolbar = findViewById(R.id.toolbarDetail)
        setSupportActionBar(mToolbar)

        val data = intent.getStringArrayListExtra("PARAM")
        val pos = intent.getIntExtra("POSITION", 0)
        val list: ArrayList<PlaceholderFragment> = ArrayList()

        for (temp in data) {
            PlaceholderFragment().apply {
                arguments = bundleOf("PARAM" to temp)
                list.add(this)
            }
        }

        title = getNameFromPath(data[pos])

        val mSectionsPagerAdapter = SectionPagerAdapter(supportFragmentManager, list)

        val mViewPager: ViewPager = findViewById(R.id.viewPagerDetail)

        mViewPager.adapter = mSectionsPagerAdapter
        mViewPager.currentItem = pos

        mViewPager.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                title = getNameFromPath(data[position])
            }

        })
    }
}