package com.example.htopstore.util.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity,private val listOfFragment: List<Fragment>): FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return  listOfFragment[position]
    }
    override fun getItemCount(): Int {
        return listOfFragment.size
    }
}