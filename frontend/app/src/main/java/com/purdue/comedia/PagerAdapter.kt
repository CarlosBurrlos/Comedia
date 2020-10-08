package com.purdue.comedia

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                FeedTab.newInstance("A Sample Parameter")
            }
            else -> {
                return profileTab.newInstance("Another Sample Parameter")
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> {
                "Feed"
            }
            else -> {
                return "Profile"
            }
        }
    }

}