package com.loenzo.serialtest2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.loenzo.serialtest2.MainActivity.Companion.lasts as g_lasts

class CategoryFragmentAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    private val list: ArrayList<CategoryFragment> = ArrayList()

    init {
        for (lastPicture in g_lasts) {
            list.add(CategoryFragment(lastPicture))
        }
    }

    override fun getItem(position: Int): Fragment {
        return list[position]
    }

    override fun getCount(): Int {
        return list.size
    }

}