package com.kemar.olam.bc_print_reprint_edit.fragment

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
import com.kemar.olam.bc_print_reprint_edit.model.request.getBordereListByFilterReq
import com.kemar.olam.bordereau.adapter.LogsTodaysHistoryAdapter
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_todays_history.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class BcTodaysHistoryFragment : Fragment(),View.OnClickListener {
    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var rvLogsUserHistory: RecyclerView
    lateinit var adapter: LogsTodaysHistoryAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsTodaysHitoryLiting: ArrayList<LogsUserHistoryRes.BordereauRecordList?> = arrayListOf()
    var commigFrom:String?=""
    var forestID : Int? = 0
    var selectedDate  : String?=""
    var log_number :String?=""
    var borderue_no :String?=""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_bc_todays_history, container, false)
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
            arguments?.getString(Constants.comming_from)?.toString()

        if(commigFrom?.equals(Constants.Bc_Header_screen,ignoreCase = true)!!){
            (activity as HomeActivity)?.bcReprint_isFromFilter = true
            var dataModel: getBordereListByFilterReq? =
                arguments?.getSerializable(Constants.badereuFilterModel) as getBordereListByFilterReq
            forestID =
                dataModel?.getSupplier()
            selectedDate =
                dataModel?.getBordereauDate()
            borderue_no =
                dataModel?.getBordereauNo()
            log_number =
                dataModel?.getLogNo()
          callingUserHistoryByFilter()
        }else{
            (activity as HomeActivity)?.bcReprint_isFromFilter = false
            callingUserHistory()
        }
        mView.swipeUserHistory.setOnRefreshListener {
            mView.swipeUserHistory.isRefreshing = false
            if(commigFrom?.equals(Constants.Bc_Header_screen,ignoreCase = true)!!){
                callingUserHistoryByFilter()
            }else {
                callingUserHistory()
            }
        }
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.bc_re_print_n_edit)
        (activity as HomeActivity).visibleFilter()
         (activity as HomeActivity).ivHomeFilter.setOnClickListener(this)
    }


    fun setupClickListner() {
        mView.ivAddBodreu.setOnClickListener(this)
        mView.btnRetry.setOnClickListener(this)
    }

    fun callingUserHistory() {
        getBodreuLogsUserHistory(SharedPref.getUserId(Constants.user_id).toString())
    }

    fun callingUserHistoryByFilter(){
        getBordereauListFilter()
    }

    fun setupRecyclerViewNAdapter() {
        logsTodaysHitoryLiting.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvLogsUserHistory.layoutManager = mLinearLayoutManager
        adapter = LogsTodaysHistoryAdapter(mView.context, logsTodaysHitoryLiting,true,false)
        mView.rvLogsUserHistory.adapter = adapter

        adapter.onHeaderClick = { modelData, position ->
            if (modelData.headerStatus?.equals("At-Hub", ignoreCase = true)!!) {
                val myactivity = activity as HomeActivity
                var prductFragment = BCReprintLogsDeatilsFragment()
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.badereuModel,
                    modelData
                );
                prductFragment.arguments = bundle
                myactivity?.replaceFragment(prductFragment, false)
            }
        }

    }

    fun generateBordereauListFilterRequest(): getBordereListByFilterReq {

        var isAdmin :Boolean=false
        when(SharedPref.read(Constants.user_role).toString()){
            "SuperUserApp","Super User","admin"->{
                isAdmin = true
            }
            else->{
                isAdmin = false
            }
        }

        var request : getBordereListByFilterReq =   getBordereListByFilterReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id).toInt())
        request.setUserLocationID(SharedPref.readInt(Constants.user_location_id).toInt())
        request.setSupplier(forestID)
        request.setBordereauNo(borderue_no)
        request.setIsAdmin(isAdmin)
        request.setLogNo(log_number)
        request.setBordereauDate(selectedDate)
        var json =  Gson().toJson(request)
        var test  = json
        return  request

    }

    private fun getBordereauListFilter() {
        if (mUtils.checkInternetConnection(mView.context)) {
            showContentView()
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                   val request  = generateBordereauListFilterRequest()
                val call_api: Call<LogsUserHistoryRes> =
                    apiInterface.getBordereauListFilter(request)
                call_api.enqueue(object :
                    Callback<LogsUserHistoryRes> {
                    override fun onResponse(
                        call: Call<LogsUserHistoryRes>,
                        response: Response<LogsUserHistoryRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                val responce: LogsUserHistoryRes =
                                    response.body()!!
                                if (responce != null) {

                                    if (response.body().getSeverity() == 200) {
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
                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
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

    fun getBodreuLogsUserHistoryRequest(userID:String): CommonRequest {
        var request : CommonRequest =   CommonRequest()
        request.setUserId(userID)
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setIsAdmin(true)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun getBodreuLogsUserHistory(userID: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            showContentView()
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
               val request  =  getBodreuLogsUserHistoryRequest(userID)
                val call_api: Call<LogsUserHistoryRes> =
                    apiInterface.getBodereuLogsUserHistory(request)
                call_api.enqueue(object :
                    Callback<LogsUserHistoryRes> {
                    override fun onResponse(
                        call: Call<LogsUserHistoryRes>,
                        response: Response<LogsUserHistoryRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response.body()?.getSeverity() == 200) {
                            val responce: LogsUserHistoryRes =
                                response.body()!!
                            if (responce != null) {
                                logsTodaysHitoryLiting?.clear()
                                responce.getBordereauRecordList()?.let {
                                    logsTodaysHitoryLiting?.addAll(
                                        it
                                    )
                                }
                                if(logsTodaysHitoryLiting.size==0){
                                    showNoNoDataView()
                                }else{
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
            R.id.ivHomeFilter -> {
                var fragment = BcReprintHeaderFragment()
                if(activity!=null) {
                    (activity as HomeActivity)?.replaceFragment(fragment, false)
                }
            }

            R.id.btnRetry -> {
                getBodreuLogsUserHistory(SharedPref.getUserId(Constants.user_id).toString())
            }
        }
    }

}


