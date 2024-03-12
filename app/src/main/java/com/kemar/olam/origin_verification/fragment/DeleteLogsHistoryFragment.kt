package com.kemar.olam.origin_verification.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.origin_verification.adapter.OriginDeleteLogsAdapter
import com.kemar.olam.origin_verification.model.RemoveLogFromStocksReq
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_delete_logs_history.view.*
import kotlinx.android.synthetic.main.fragment_delete_logs_history.view.relvNoInternet
import kotlinx.android.synthetic.main.fragment_loading_wagons_declar.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DeleteLogsHistoryFragment : Fragment(),View.OnClickListener {

    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var rvDeleteLogHistory: RecyclerView
    lateinit var adapter: OriginDeleteLogsAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsLiting: ArrayList<BodereuLogListing?> = arrayListOf()
    var commigFrom: String? = ""
    var forestID: Int? = 0
    var vehicleName: String? = ""
    var borderue_no: String? = ""
    var barcoddeNumber: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_delete_logs_history, container, false)
        initViews()
        return mView
    }

    fun initViews() {
        mUtils = Utility()
        rvDeleteLogHistory = mView.findViewById(R.id.rvDeleteLogHistory)
        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()

        mView.swipeUserHistory.setOnRefreshListener {
            mView.swipeUserHistory.isRefreshing = false
            callingUserHistory()
        }
        callingUserHistory()
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.origin_verification)
        (activity as HomeActivity).invisibleFilter()
        (activity as HomeActivity).ivHomeFilter.setOnClickListener(this)
    }


    fun setupClickListner() {
        mView.btnLogRetry.setOnClickListener(this)
        mView.linFooter.setOnClickListener(this)
    }

    fun callingUserHistory() {
        getOriginVerificationUserHistory()
    }


    fun setupRecyclerViewNAdapter() {
        logsLiting.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvDeleteLogHistory.layoutManager = mLinearLayoutManager
        adapter = OriginDeleteLogsAdapter(mView.context, logsLiting)
        mView.rvDeleteLogHistory.adapter = adapter
        mView.rvDeleteLogHistory.isNestedScrollingEnabled = true


    }

    fun generatconfirmRemovedLogListRequest(): RemoveLogFromStocksReq{
        var request : RemoveLogFromStocksReq =   RemoveLogFromStocksReq()
        var tempList: ArrayList<Int?> = arrayListOf()
        for (model in logsLiting)
    {
        if (model?.isSelected == true) {
            tempList.add(model?.getDetailId()?.toInt())
        }
    }
        request.setBordereauDetailIdList(tempList)
        return  request
}


    private fun saveDeleteLogFromOrigin() {
        if (mUtils.checkInternetConnection(mView.context)) {
            showContentView()
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = generatconfirmRemovedLogListRequest()
                val call_api: Call<GetBodereuLogByIdRes> =
                    apiInterface.saveDeleteLogFromOrigin(request)
                call_api.enqueue(object :
                    Callback<GetBodereuLogByIdRes> {
                    override fun onResponse(
                        call: Call<GetBodereuLogByIdRes>,
                        response: Response<GetBodereuLogByIdRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response.body().getSeverity() == 200) {
                                    callingUserHistory()

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
                        call: Call<GetBodereuLogByIdRes>,
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





    fun generateAddBodereuLogListRequest(): CommonRequest {
        val request: CommonRequest = CommonRequest()
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        val json = Gson().toJson(request)
        var test = json

        return request

    }


    private fun getOriginVerificationUserHistory() {
        if (mUtils.checkInternetConnection(mView.context)) {
            showContentView()
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = generateAddBodereuLogListRequest()
                val call_api: Call<GetBodereuLogByIdRes> =
                    apiInterface.getDeleteLogFromOrigin(request)
                call_api.enqueue(object :
                    Callback<GetBodereuLogByIdRes> {
                    override fun onResponse(
                        call: Call<GetBodereuLogByIdRes>,
                        response: Response<GetBodereuLogByIdRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response.body().getSeverity() == 200) {

                                    val responce: GetBodereuLogByIdRes =
                                        response.body()!!
                                    if (responce != null) {
                                        logsLiting?.clear()
                                        responce.getBordereauLogList()?.let {
                                            logsLiting?.addAll(
                                                it
                                            )
                                        }
                                        if (logsLiting.size == 0) {
                                            showNoNoDataView()
                                        } else {
                                            showContentView()
                                        }
                                        adapter.notifyDataSetChanged()
                                    }
                                    setTotalNoOfLogs(logsLiting.size)
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
                        call: Call<GetBodereuLogByIdRes>,
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


    private fun setTotalNoOfLogs(count: Int) {
        if (count == 0) {
            mView.linFooter.visibility =  View.INVISIBLE
            mView.tvNoDataFound.visibility=View.VISIBLE
            mView.rvDeleteLogHistory.visibility=View.GONE
        } else {
            mView.linFooter.visibility =  View.VISIBLE
            mView.tvNoDataFound.visibility=View.GONE
            mView.rvDeleteLogHistory.visibility=View.VISIBLE
        }

    }

    fun showNoNoDataView() {
        mView.linContent.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.VISIBLE
    }

    fun showNoInternetView() {
        mView.linContent.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.VISIBLE
    }

    fun showContentView() {
        mView.linContent.visibility = View.VISIBLE
        mView.relvNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
    }

    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.btnLogRetry -> {
                callingUserHistory()
            }

            R.id.linFooter -> {
                saveDeleteLogFromOrigin()
            }
        }
    }
}

