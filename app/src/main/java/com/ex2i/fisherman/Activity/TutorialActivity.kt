package com.ex2i.fisherman.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.ex2i.fisherman.Fragment.*
import com.ex2i.fisherman.R
import com.ex2i.fisherman.Util.PreferenceUtil
import kotlinx.android.synthetic.main.activity_tutorial.*
import me.relex.circleindicator.CircleIndicator


class TutorialActivity : AppCompatActivity() {
    var adapterViewPager: FragmentPagerAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)
        val vpPager = findViewById<View>(R.id.vpPager) as ViewPager

        adapterViewPager =
            MyPagerAdapter(
                supportFragmentManager
            )
        vpPager.adapter = adapterViewPager
        val indicator =
            findViewById<View>(R.id.indicator) as CircleIndicator
        indicator.setViewPager(vpPager)

        vpPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                if (position == 3) {
                    tv_start.visibility = View.VISIBLE
                    indicator.visibility = View.GONE
                } else {
                    tv_start.visibility = View.GONE
                    indicator.visibility = View.VISIBLE
                }
            }

        })

        tv_start.setOnClickListener {
            val intent = Intent(this, SeasonActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            PreferenceUtil.getInstance(this).putBooleanExtra("tutorial",true)
            finish()
        }

    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    class MyPagerAdapter(fragmentManager: FragmentManager?) :
        FragmentPagerAdapter(fragmentManager!!) {
        // Returns total number of pages
        override fun getCount(): Int {
            return NUM_ITEMS
        }

        // Returns the fragment to display for that page
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    FirstFragment.newInstance(0, "Page # 1")
                }
                1 -> {
                    SecondFragment.newInstance(1, "Page # 2")
                }
                2 -> {
                    ThirdFragment.newInstance(2, "Page # 3")
                }
                3 -> {
                    ForthFragment.newInstance(3, "Page # 3")
                }
                else -> null
            }!!
        }

        // Returns the page title for the top indicator
        override fun getPageTitle(position: Int): CharSequence? {
            return "Page $position"
        }

        companion object {
            private const val NUM_ITEMS = 4
        }
    }
}
