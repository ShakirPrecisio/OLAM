package com.kemar.olam.sales_and_inspection.ground_sales.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.loading_wagons.fragment.LoadingWagonsHeaderFragment
import com.kemar.olam.loading_wagons.fragment.LoadingWagonsLogsListingFragment
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.sales_and_inspection.ground_sales.adapter.GApprovalListAdapter
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_ground_approval_listing.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GroundApprovalListingFragment : Fragment() , View.OnClickListener {

    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var rvLogsUserHistory: RecyclerView
    lateinit var adapter: GApprovalListAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsTodaysHitoryLiting: ArrayList<LogsUserHistoryRes.BordereauGroundList?> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_ground_approval_listing, container, false)
        initViews()
        return mView;
    }

    fun initViews() {
        mUtils = Utility()
        rvLogsUserHistory = mView.findViewById(R.id.rvLogsUserHistory)
        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()
        callingGroundUserHistory()
        mView.swipeUserHistory.setOnRefreshListener {
            mView.swipeUserHistory.isRefreshing = false
            callingGroundUserHistory()
        }
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.inspection_n_sales)
        (activity as HomeActivity).invisibleFilter()
    }

    fun setupClickListner() {
        mView.ivAddLoadingBodreu.setOnClickListener(this)
        mView.btnRetry.setOnClickListener(this)
        //  mView.txtApprovalList.setOnClickListener(this)
    }

    fun callingGroundUserHistory() {
        getGroundUserHistory(SharedPref.getUserId(Constants.user_id).toString())
    }

    fun setupRecyclerViewNAdapter() {
        logsTodaysHitoryLiting.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvLogsUserHistory.layoutManager = mLinearLayoutManager
        adapter = GApprovalListAdapter(
            mView.context,
            logsTodaysHitoryLiting,
            SharedPref.read(Constants.user_role).toString()
        )
        mView.rvLogsUserHistory.adapter = adapter

        adapter.onInspectionOrNoInspectionClick = { modelData, position, isSalesInpection ->
            if (isSalesInpection) {
                when (modelData?.inspectionFlag) {
                    null -> {
                        val myactivity = activity as HomeActivity
                        val fragment = GroundHeaderFragment()
                        val bundle = Bundle()
                        bundle.putSerializable(
                            Constants.badereuModel,
                            modelData
                        );
                        bundle.putString(
                            Constants.comming_from,
                            "UserHistory"
                        );
                        fragment.arguments = bundle
                        myactivity?.replaceFragment(fragment, false)
                    }
                    "S" -> {
                        val myactivity = activity as HomeActivity
                        val fragment = GroundApprovalListingFragment()
                        val bundle = Bundle()
                        bundle.putSerializable(
                            Constants.badereuModel,
                            modelData
                        );
                        bundle.putString(
                            Constants.comming_from,
                            "UserHistory"
                        );
                        fragment.arguments = bundle
                        myactivity?.replaceFragment(fragment, false)
                    }
                }

            } else {
                //Noo Inspection api call
                //callingNoInspectionAPI(position)
            }
        }

        adapter.onWaitingForApprovalClick = { modelData, position ->
            val myactivity = activity as HomeActivity
            val fragment = GroundApprovalLogsDeatilsFragment()
            val bundle = Bundle()
            bundle.putSerializable(
                Constants.badereuModel,
                modelData
            )
            bundle.putString(
                Constants.comming_from,
                mView.context.getString(R.string.approval_history)
            )
            fragment.arguments = bundle
            myactivity?.replaceFragment(fragment, false)

        }
        adapter.onHeaderClick = { modelData, position ->
            if (modelData.inspectionFlag.equals("Draft", ignoreCase = true)!!) {
                val myactivity = activity as HomeActivity
                val fragment = LoadingWagonsLogsListingFragment()
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.badereuModel,
                    modelData
                )
                bundle.putString(
                    Constants.comming_from,
                    "UserHistory"
                )
                fragment.arguments = bundle
                myactivity?.replaceFragment(fragment, false)
            }
        }

    }

    /*fun generateNoInspectionRequest(position: Int): BoderueDeleteLogReq {

        var request: BoderueDeleteLogReq = BoderueDeleteLogReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(logsTodaysHitoryLiting[position]?.bordereauHeaderId)
        return request

    }*/


   /* private fun callingNoInspectionAPI(position: Int) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateNoInspectionRequest(position)
                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.noInspection(request)
                call.enqueue(object :
                    Callback<AddBodereuLogListingRes> {
                    override fun onResponse(
                        call: Call<AddBodereuLogListingRes>,
                        response: Response<AddBodereuLogListingRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response != null) {
                                    if (response.body().getSeverity() == 200) {
                                        mUtils.showToast(activity, response.body().getMessage())
                                        getLoadingSalesUserHistory(SharedPref.getUserId(Constants.user_id).toString())
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }
                            } else {
                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        call: Call<AddBodereuLogListingRes>,
                        t: Throwable
                    ) {
                        mUtils.showToast(activity, Constants.SERVERTIMEOUT)
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            mUtils.showToast(view?.context, getString(R.string.no_internet))
        }
    }
*/

    fun getGroundUserHistoryRequest(userID: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setUserId(userID)
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setIsAdmin(true)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    private fun getGroundUserHistory(userID: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            showContentView()
            try {
                //var  isAdmin : Boolean = false
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                /*  when(SharedPref.read(Constants.user_role).toString()){
                      "SuperUserApp","Super User","admin"->{
                          isAdmin = false
                      }
                      else->{
                          isAdmin = false
                      }
                  }*/

                //is admin true bc only sent for approval data come to this screen
                val request  =   getGroundUserHistoryRequest(userID)
                val call_api: Call<LogsUserHistoryRes> =
                    apiInterface.getBordereauHistoryForGround(
                        request
                    )
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
                                        responce.getBordereauGroundList()?.let {
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
                                }else if (response.body()?.getSeverity() == 306) {
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



    fun showNoInternetView() {
        mView.linContent.visibility = View.GONE
        // mView.linApprovalList.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.VISIBLE
    }

    fun showNoNoDataView() {
        mView.linContent.visibility = View.GONE
        //  mView.linApprovalList.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.VISIBLE
    }

    fun showContentView() {
        mView.linContent.visibility = View.VISIBLE
        //mView.linApprovalList.visibility = View.VISIBLE
        mView.relvNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
    }


    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivAddLoadingBodreu -> {
                /*var fragment = LoadingWagonsHeaderFragment()*/
                var fragment = LoadingWagonsHeaderFragment()
                val bundle = Bundle()
                bundle.putString(
                    Constants.action,
                    Constants.action_non_edit
                );
                fragment.arguments = bundle
                (activity as HomeActivity).replaceFragment(fragment, false)
            }
            /* R.id.txtApprovalList -> {
                 var fragment = ApprovalListFragment()
                 (activity as HomeActivity).replaceFragment(fragment, false)
             }*/

            R.id.btnRetry -> {
                getGroundUserHistory(SharedPref.getUserId(Constants.user_id).toString())
            }
        }
    }
}

