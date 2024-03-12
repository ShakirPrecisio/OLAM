package com.kemar.olam.bordereau.fragment

import ViewPagerAdapter
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.tabs.TabLayout
import com.kemar.olam.R
import com.kemar.olam.dashboard.activity.HomeActivity
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.inspections_layout_tab.view.*

class InspectionsFragment : Fragment() {

    lateinit var mView : View
    val inStockOrderFragment = UnderMaintenaceFragment()
    val makeToOrderFragment = UnderMaintenaceFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.inspection_n_sales)
        (activity as HomeActivity).invisibleFilter()

        return inflater.inflate(R.layout.fragment_deleivery_management, container, false)
      //  mView= inflater.inflate(R.layout.inspections_layout_tab, container, false)
     /*   return inflater.inflate(R.layout.layout_inspection_header, container, false)*/

       /* setUpViewPager()
        mView.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                mView.viewPager.setCurrentItem(tab.getPosition())
                val view = tab?.customView
                if (view is AppCompatTextView) {
                    view.setTypeface(view.typeface, Typeface.BOLD)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val view = tab?.customView
                if (view is AppCompatTextView) {
                    view.setTypeface(view.typeface, Typeface.NORMAL)
                }
                *//*if (view is AppCompatTextView) {
                    val font = Typeface.createFromAsset(assets, "font/roboto_regular.ttf")
                    view.typeface = font
                }*//*
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        return  mView;*/

    }

  /*  private fun setUpViewPager() {
        val adapter = ViewPagerAdapter(childFragmentManager)
            adapter.addFragment(inStockOrderFragment, "Sales Inspection")
            adapter.addFragment(makeToOrderFragment, "Ground Sales")
        mView.viewPager.adapter = adapter
        mView.tabs.setupWithViewPager(mView.viewPager)
    }
*/

}