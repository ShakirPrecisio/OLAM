package com.kemar.olam.physicalcount.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.physicalcount.adapter.PhysicalListAdapter
import com.kemar.olam.physicalcount.model.PhysicalCountModel
import com.kemar.olam.physicalcount.requestbody.PhysicalCountRequest
import com.kemar.olam.physicalcount.response.StockListRequest
import com.kemar.olam.physicalcount.response.StockListResponse
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.RealmHelper
import com.kemar.olam.utility.Utility
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_physical_list.view.*
import kotlinx.android.synthetic.main.fragment_physical_list.view.noDataFound
import kotlinx.android.synthetic.main.fragment_physical_list.view.rvPhysical
import kotlinx.android.synthetic.main.fragment_physical_scan.view.*
import kotlinx.android.synthetic.main.fragment_todays_history.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PhysicalListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PhysicalListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var mView: View
    lateinit var mUtils: Utility


    var stockRequest= ArrayList<StockListRequest>()
    lateinit var realm: Realm

    lateinit var physicalCountModel: RealmResults<PhysicalCountModel>
    lateinit  var physicalListAdapter : PhysicalListAdapter
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

        realm = RealmHelper.getRealmInstance()
        mUtils = Utility()
        mView= inflater.inflate(R.layout.fragment_physical_list, container, false)
        setToolbar()

       mView.txtNew.setOnClickListener {
            var fragment = PhysicalScanFragment()
            val bundle = Bundle()
            bundle.putString(
                    Constants.action,
                    ""
            )

            bundle.putBoolean(
                    Constants.Offline,
                    false
            )
            fragment.arguments = bundle
            (activity as HomeActivity).replaceFragment(fragment, false)
        }



        stockRequest.clear()
        physicalCountModel = realm.where(PhysicalCountModel::class.java).findAll()
        if(physicalCountModel.size>0) {
            for(physic in physicalCountModel) {
                var stockListRequest = StockListRequest()
                stockListRequest.uniqueId= physic.uniqueId.toString()
                stockListRequest.startDate = physic.startDate
                stockListRequest.isOffline = true
                stockListRequest.status = "Offine Save"
                stockRequest.add(stockListRequest)
            }
        }

        mView.swipeLogListing.setOnRefreshListener {
            mView.swipeLogListing.isRefreshing = false
            getStockList()
        }

        Log.e("Count","is"+stockRequest.size)

        setRecycler()
        return mView
    }
    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.physical_count)
    }

    fun setRecycler(){
        physicalListAdapter= PhysicalListAdapter(stockRequest, requireContext())
        mView.rvPhysical!!.layoutManager = LinearLayoutManager(
            requireActivity(),
            RecyclerView.VERTICAL,
            false
        )
        mView.rvPhysical.adapter=physicalListAdapter

        physicalListAdapter!!.onItemClick = { data, position, holder ->
            var fragment = PhysicalScanFragment()
            val bundle = Bundle()

            if(data.isOffline){

                bundle.putString(
                        Constants.action,
                        data.uniqueId
                )

                bundle.putBoolean(
                        Constants.Offline,
                        data.isOffline
                )
            }else {
                bundle.putString(
                        Constants.action,
                        data.id.toString()
                )
                bundle.putBoolean(
                        Constants.Offline,
                        data.isOffline
                )
            }
            fragment.arguments = bundle
            (activity as HomeActivity).replaceFragment(fragment, false)
        }

        setData()
    }

    fun setData(){
        if(stockRequest.size>0){
            mView.rvPhysical.visibility=View.VISIBLE
            mView.noDataFound.visibility=View.GONE
        }else{
            mView.noDataFound.visibility=View.VISIBLE
            mView.rvPhysical.visibility=View.GONE
        }
    }

    override fun onResume() {
        stockRequest.clear()
        physicalCountModel = realm.where(PhysicalCountModel::class.java).findAll()
        if(physicalCountModel.size>0) {
            for(physic in physicalCountModel) {
                var stockListRequest = StockListRequest()
                stockListRequest.startDate = physic.startDate
                stockListRequest.isOffline = true
                stockListRequest.status = "Offine Save"
                stockRequest.add(stockListRequest)
            }

        }
        Log.e("onResume","onResume")
        getStockList()
        setData()
        super.onResume()
    }

    fun getStockList() {
        try {

            mUtils.showProgressDialog(mView.context)
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
            val date: String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

            var physicalCountRequest= PhysicalCountRequest()
            physicalCountRequest.startDate=date
            var hashMap=HashMap<String,Any>()

            val call: Call<StockListResponse> = apiInterface.getStockLists(hashMap)
            call.enqueue(object :
                Callback<StockListResponse> {
                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onResponse(
                    call: Call<StockListResponse>,
                    response: Response<StockListResponse>
                ) {
                    try {

                        mUtils.dismissProgressDialog()
                        if (response.code() == 200) {
                            if (response != null) {
                                var stockListResponse=response.body()
                                if (stockListResponse.severity == 200) {

                                    stockRequest.clear()
                                    stockRequest.addAll(stockListResponse.stockListRequests!!)
                                    physicalListAdapter.notifyDataSetChanged()

                                    setData()

                                   // mUtils.showToast(requireActivity(),response.body().getMessage())

                                } else if (stockListResponse.severity == 306) {
                                    mUtils.alertDialgSession(mView.context, activity)
                                } else if (stockListResponse.severity == 202) {

                                } else if (stockListResponse.severity == 201) {
                                   // mUtils.showAlert(requireActivity(),response.body().getMessage())

                                } else {
                                    //mUtils.showToast(activity, response.body().getMessage())
                                }
                            }

                        } else if (response.code() == 306) {
                            mUtils.alertDialgSession(mView.context, activity)
                        } else {
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                    call: Call<StockListResponse>,
                    t: Throwable
                ) {

                    mUtils.dismissProgressDialog()
                    t.printStackTrace()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PhysicalListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PhysicalListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}