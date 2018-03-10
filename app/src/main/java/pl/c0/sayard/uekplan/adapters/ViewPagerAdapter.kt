package pl.c0.sayard.uekplan.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class ViewPagerAdapter(supportFragmentManager: FragmentManager): FragmentPagerAdapter(supportFragmentManager) {

    private val fragmentList = mutableListOf<Fragment>()

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }

    fun addFragment(fragment: Fragment){
        fragmentList.add(fragment)
    }
}