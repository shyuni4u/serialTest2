package com.loenzo.serialtest2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class CategoryFragmentAdapter(fm: FragmentManager, list: ArrayList<CategoryFragment>) : FragmentPagerAdapter(fm) {

    private val mList: ArrayList<CategoryFragment> = list

    override fun getItem(position: Int): Fragment {
        return mList[position]
    }

    override fun getCount(): Int {
        return mList.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    fun addItem(newItem: CategoryFragment) {
        mList.add(newItem)
        notifyDataSetChanged()
    }
}