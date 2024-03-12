package com.kemar.olam.offlineData.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kemar.olam.R
import com.kemar.olam.bordereau.fragment.AddHeaderFragment
import com.kemar.olam.bordereau.fragment.TodaysHistoryFragment
import com.kemar.olam.dashboard.activity.DashboardActivity
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.loading_wagons.fragment.LoadingWagonsUserHistoryFragment
import com.kemar.olam.offlineData.activity.BorderauListActivity
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_offline_main.*
import kotlinx.android.synthetic.main.activity_offline_main.langSwitch
import kotlinx.android.synthetic.main.fragment_offline_mode.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OfflineModeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OfflineModeFragment : Fragment() {
    lateinit var mView: View
    lateinit var mUtils: Utility


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_offline_mode, container, false)

        mView.card_Borderau.setOnClickListener {
            var fragment = BorderauListFragment()
            (activity as HomeActivity).replaceFragment(fragment, false)
        }

        mView.card_loading_wagons.setOnClickListener {
            val fragment = LoadingWagonOfflineFragment()
            (activity as HomeActivity).replaceFragment(fragment, false)
        }
        return mView
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OfflineModeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OfflineModeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}