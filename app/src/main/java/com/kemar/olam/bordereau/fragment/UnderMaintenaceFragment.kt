package com.kemar.olam.bordereau.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kemar.olam.R
import com.kemar.olam.dashboard.activity.HomeActivity
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.content_homes.view.*


class UnderMaintenaceFragment : Fragment() {
    lateinit var mView: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.dashboard)
        (activity as HomeActivity).invisibleFilter()
        mView = inflater.inflate(R.layout.fragment_under_maintenace, container, false)

        return mView
    }

}