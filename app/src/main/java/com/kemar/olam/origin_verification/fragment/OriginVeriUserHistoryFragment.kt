package com.kemar.olam.origin_verification.fragment

import android.opengl.Visibility
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.origin_verification.adapter.OriginTodaysHistoryAdapter
import com.kemar.olam.origin_verification.model.request.OriginUserHistoryReq
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_origin_veri_user_history.view.*
import kotlinx.android.synthetic.main.fragment_todays_history.view.btnRetry
import kotlinx.android.synthetic.main.fragment_todays_history.view.ivAddBodreu
import kotlinx.android.synthetic.main.fragment_todays_history.view.linContent
import kotlinx.android.synthetic.main.fragment_todays_history.view.relvNoDataFound
import kotlinx.android.synthetic.main.fragment_todays_history.view.relvNoInternet
import kotlinx.android.synthetic.main.fragment_todays_history.view.rvLogsUserHistory
import kotlinx.android.synthetic.main.fragment_todays_history.view.swipeUserHistory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OriginVeriUserHistoryFragment : Fragment(),View.OnClickListener {

    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var rvLogsUserHistory: RecyclerView
    lateinit var adapter: OriginTodaysHistoryAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsTodaysHitoryLiting: ArrayList<LogsUserHistoryRes.BordereauRecordList?> = arrayListOf()
    var commigFrom:String?=""
    var forestID :Int? = 0
    var vehicleName :String?=""
    var borderue_no :String?=""
    var barcoddeNumber :String?=""
    var stockDeclaredDate :String?=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_origin_veri_user_history, container, false)
        initViews()
        return  mView
    }
    fun initViews() {
        mUtils = Utility()
        rvLogsUserHistory = mView.findViewById(R.id.rvLogsUserHistory)
        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()
        commigFrom =
            arguments?.getString(Constants.comming_from)

        if(commigFrom?.equals(Constants.Bc_Header_screen,ignoreCase = true)!!){
            var dataModel: OriginUserHistoryReq? =
                arguments?.getSerializable(Constants.badereuFilterModel) as OriginUserHistoryReq
            forestID =
                dataModel?.getSupplier()
            vehicleName =
                dataModel?.getWagonNo()
            borderue_no =
                dataModel?.getBordereauNo()
            barcoddeNumber = dataModel?.getBarcodeNumber()

            stockDeclaredDate = dataModel?.getStockDeclaredDate()
            callingUserHistory()
        }else{
            callingUserHistory()
        }
        mView.swipeUserHistory.setOnRefreshListener {
            mView.swipeUserHistory.isRefreshing = false
            if(commigFrom?.equals(Constants.Bc_Header_screen,ignoreCase = true)!!){
                callingUserHistory()
            }else {
                callingUserHistory()
            }
        }
        showHideDeletedLogsButton()
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.origin_verification)
        (activity as HomeActivity).invisibleFilter()
       // (activity as HomeActivity).ivHomeFilter.setOnClickListener(this)
    }


    fun setupClickListner() {
        mView.ivAddBodreu.setOnClickListener(this)
        mView.btnRetry.setOnClickListener(this)
        mView.linDeleteLogList.setOnClickListener(this)
    }

    fun callingUserHistory() {
        getOriginVerificationUserHistory()
    }

    fun showHideDeletedLogsButton() {
        when (SharedPref.read(Constants.user_role).toString()) {
            "SuperUserApp", "Super User", "admin" -> {
                mView.linDeleteLogList.visibility = View.VISIBLE
            }
            else -> {
                mView.linDeleteLogList.visibility = View.GONE
            }
        }
    }

    fun setupRecyclerViewNAdapter() {
        logsTodaysHitoryLiting.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvLogsUserHistory.layoutManager = mLinearLayoutManager
        adapter = OriginTodaysHistoryAdapter(mView.context, logsTodaysHitoryLiting)
        mView.rvLogsUserHistory.adapter = adapter


        adapter.onHeaderClick = { modelData, position ->
            if(modelData.verifyFlag.isNullOrEmpty()){
                val myactivity = activity as HomeActivity
                val prductFragment = OriginLogDetaiilsFragment()
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.badereuModel,
                    modelData
                )
                prductFragment.arguments = bundle
                myactivity?.replaceFragment(prductFragment, false)
            }
        /*    if (modelData.headerStatus?.equals("Submitted", ignoreCase = true)!!) {
                val myactivity = activity as HomeActivity
                var prductFragment = OriginLogDetaiilsFragment()
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.badereuModel,
                    modelData
                );
                prductFragment.arguments = bundle
                myactivity?.replaceFragment(prductFragment, false)
            }*/
        }

    }


    fun generateAddBodereuLogListRequest(): OriginUserHistoryReq {
        val request : OriginUserHistoryReq =   OriginUserHistoryReq()
        request.setWagonNo(vehicleName)
        request.setBordereauNo(borderue_no)
        request.setSupplier(forestID)
        request.setBarcodeNumber(barcoddeNumber)
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setUserLocationID(SharedPref.readInt(Constants.user_location_id))
        request.setStockDeclaredDate(stockDeclaredDate)
        request.setIsAdmin(true)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun getOriginVerificationUserHistory() {
        if (mUtils.checkInternetConnection(mView.context)) {
            showContentView()
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request= generateAddBodereuLogListRequest()
                val call_api: Call<LogsUserHistoryRes> =
                    apiInterface.getOriginFilteredList(request)
                call_api.enqueue(object :
                    Callback<LogsUserHistoryRes> {
                    override fun onResponse(
                        call: Call<LogsUserHistoryRes>,
                        response: Response<LogsUserHistoryRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response.body().getSeverity() == 200) {
                                    val responce: LogsUserHistoryRes =
                                        response.body()!!
                                    if (responce != null) {
                                        logsTodaysHitoryLiting?.clear()
                                        responce.getBordereauRecordList()?.let {
                                            logsTodaysHitoryLiting?.addAll(
                                                it
                                            )
                                        }
                                        if (logsTodaysHitoryLiting.size == 0) {
                                            showNoNoDataView()
                                        } else {
                                            showContentView()
                                        }
                                        adapter.notifyDataSetChanged()
                                    }
                                } else if (response.body()?.getSeverity() == 306) {
                                    mUtils.alertDialgSession(mView.context, activity)
                                } else {
                                    mUtils.showToast(activity, response.body().getMessage())
                                }
                            } else if (response.code() == 306) {
                                mUtils.alertDialgSession(mView.context, activity)
                            } else{
                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        call: Call<LogsUserHistoryRes>,
                        t: Throwable
                    ) {
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            mUtils.showToast(mView.context, getString(R.string.no_internet))
            showNoInternetView()
        }
    }

    fun showNoNoDataView(){
        mView.linContent.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
        mView.linDeleteLogList.visibility = View.VISIBLE
        mView.relvNoDataFound.visibility = View.VISIBLE
    }

    fun showNoInternetView() {
        mView.linContent.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.GONE
        mView.linDeleteLogList.visibility = View.GONE
        mView.relvNoInternet.visibility = View.VISIBLE
    }

    fun showContentView() {
        mView.linContent.visibility = View.VISIBLE
        mView.relvNoDataFound.visibility = View.GONE
       // mView.linDeleteLogList.visibility = View.VISIBLE
        mView.relvNoInternet.visibility = View.GONE
    }

    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivHomeFilter -> {
               /* var fragment = OriginVerificationHeaderFragment()
                (activity as HomeActivity).replaceFragment(fragment,false)*/
            }

            R.id.btnRetry -> {
                callingUserHistory()
            }
            R.id.ivAddBodreu->{
                var fragment = OriginVerificationHeaderFragment()
                (activity as HomeActivity).replaceFragment(fragment,false)
            }

            R.id.linDeleteLogList  ->{
                var fragment = DeleteLogsHistoryFragment()
                (activity as HomeActivity).replaceFragment(fragment,false)
            }
        }
    }

}