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
import kotlin.math.abs

class GalleryDetail : AppCompatActivity() {
    class SectionPagerAdapter(fm: FragmentManager, private val mList: ArrayList<PlaceholderFragment>) : FragmentPagerAdapter(fm) {
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

    class DepthPageTransformer : ViewPager.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            val min = 0.75F

            view.apply {
                val pageWidth = width
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 0 -> { // [-1,0]
                        // Use the default slide transition when moving to the left page
                        alpha = 1f
                        translationX = 0f
                        scaleX = 1f
                        scaleY = 1f
                    }
                    position <= 1 -> { // (0,1]
                        // Fade the page out.
                        alpha = 1 - position

                        // Counteract the default slide transition
                        translationX = pageWidth * -position

                        // Scale the page down (between MIN_SCALE and 1)
                        val scaleFactor = (min + (1 - min) * (1 - abs(position)))
                        scaleX = scaleFactor
                        scaleY = scaleFactor
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
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

        mViewPager.setPageTransformer(true, DepthPageTransformer())
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