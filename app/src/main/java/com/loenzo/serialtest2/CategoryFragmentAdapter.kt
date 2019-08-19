package com.loenzo.serialtest2

import android.content.SharedPreferences
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class CategoryFragmentAdapter(fm: FragmentManager, sf: SharedPreferences) : FragmentPagerAdapter(fm) {

    private val list: ArrayList<CategoryFragment> = ArrayList()

    init {
        val categorySet = sf.getStringSet("CATEGORY_LIST", HashSet<String>())

        list.clear()
        for (s in categorySet!!) {
            val cf = CategoryFragment().apply {
                arguments = bundleOf("NAME" to s)
            }
            Log.i("CategoryFragmentAdapter", "TEST: $s")
            list.add(cf)
        }
    }

    override fun getItem(position: Int): Fragment {
        return list[position]
    }

    override fun getCount(): Int {
        return list.size
    }
}