package com.kemar.olam.bordereau.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.adapter.LogsTodaysHistoryAdapter
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.offlineData.fragment.BorderauListFragment
import com.kemar.olam.offlineData.requestbody.BorderauRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.*
import com.lp.lpwms.ui.offline.response.SupplierDatum
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_todays_history.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TodaysHistoryFragment : Fragment(), View.OnClickListener {
    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var rvLogsUserHistory: RecyclerView
    lateinit var adapter: LogsTodaysHistoryAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsTodaysHitoryLiting: ArrayList<LogsUserHistoryRes.BordereauRecordList?> = arrayListOf()
    var FULL_PATH: String = ""

    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        realm = RealmHelper.getRealmInstance()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_todays_history, container, false)
        initViews()
        return mView;
    }

    fun initViews() {
        mUtils = Utility()
        rvLogsUserHistory = mView.findViewById(R.id.rvLogsUserHistory)
        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()
        callingUserHistory()
        mView.swipeUserHistory.setOnRefreshListener {
            mView.swipeUserHistory.isRefreshing = false
            callingUserHistory()
        }
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.barcode_entries)
        (activity as HomeActivity).invisibleFilter()
        /* (activity as HomeActivity).ivHomeFilter.setOnClickListener{
         }*/
    }

    /*  override fun onResume() {
          super.onResume()
          callingUserHistory()
      }*/

    fun setupClickListner() {
        mView.ivAddBodreu.setOnClickListener(this)
        mView.btnRetry.setOnClickListener(this)
        mView.linOffline.setOnClickListener(this)
    }

    fun callingUserHistory() {
        if (!mUtils.checkInternetConnection(mView.context)) {
            logsTodaysHitoryLiting.clear()
            realm.executeTransactionAsync ({ bgRealm ->
                var borderauRequest: RealmResults<BorderauRequest> = bgRealm.where(BorderauRequest::class.java).sort("uniqueId", Sort.DESCENDING).equalTo("isUpload", false).findAll()
                Log.e("borderauRequest","is "+borderauRequest.size)
                for(borderaurequest in borderauRequest){
                    var borderaurecord =LogsUserHistoryRes.BordereauRecordList()
                    borderaurecord.loadingStatus=borderaurequest.headerStatus
                    borderaurecord.headerStatus=borderaurequest.headerStatus
                    borderaurecord.supplierName=borderaurequest.forestName
                    borderaurecord.supplierId=borderaurequest.forestId
                    borderaurecord.transporterId=borderaurequest.transpoterId
                    borderaurecord.transporterName=borderaurequest.transpoterName
                    borderaurecord.uniqueId=borderaurequest.uniqueId
                    borderaurecord.originId=borderaurequest.originId
                    borderaurecord.originName=borderaurequest.originName
                    borderaurecord.bordereauNo=borderaurequest.bordereauNo
                    borderaurecord.leauChargementId=borderaurequest.leudechargementId
                    borderaurecord.leauChargementName=borderaurequest.chargementName
                    borderaurecord.bordereauDateString=borderaurequest.currentDate
                    borderaurecord.bordereauDate=borderaurequest.currentDate
                    borderaurecord.bordereauDate=borderaurequest.bordereauDate
                    borderaurecord.bordereauNo=borderaurequest.bordereauNo
                    borderaurecord.fscId=borderaurequest.fscId
                    borderaurecord.fscName=borderaurequest.fscName
                    borderaurecord.wagonId=borderaurequest.transpoterId
                    borderaurecord.distance=borderaurequest.distance
                    borderaurecord.destination=borderaurequest.destination
                    borderaurecord.timezoneId="Asia/Kolkata"
                    borderaurecord.truckNo=borderaurequest.wagonNo
                    borderaurecord.wagonNo= borderaurequest.wagonNo
                    borderaurecord.transportMode=borderaurequest.modeOfTransport
                    borderaurecord.recordDocNo=borderaurequest.recordDocNo
                    borderaurecord.supplierShortName=borderaurequest.forestName

                    var borderauRequest: RealmResults<BodereuLogListing> = bgRealm.where(BodereuLogListing::class.java).equalTo("forestuniqueId", borderaurequest.uniqueId, Case.SENSITIVE).findAll()
                    borderaurecord.logQty=borderauRequest.size
                    logsTodaysHitoryLiting.add(borderaurecord)

                }
            }, {
                Log.e("success","success")
                Log.e("logsTodaysHitoryLiting","is "+logsTodaysHitoryLiting.size)
                if(logsTodaysHitoryLiting.size>0){
                    showContentView()
                }else{
                    showNoNoDataView()
                }
                adapter = LogsTodaysHistoryAdapter(mView.context, logsTodaysHitoryLiting, false,false)
                mView.rvLogsUserHistory.adapter = adapter
                adapter.notifyDataSetChanged()

                adapter.onHeaderClick = { modelData, position ->
                    //    if(modelData.headerStatus?.equals("Draft",ignoreCase = true)!!) {
                    val myactivity = activity as HomeActivity
                    var fragment: Fragment? = null
                    if (modelData.headerStatus?.equals("Draft", ignoreCase = true)!!) {
                        fragment = LogsListingFragment()

                    } else {
                        fragment = BarcodeBordereuDetailsFragment()
                    }
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

            }) {
                Log.e("faile","faile to update")
            }
        }else {
            getBodreuLogsUserHistory(SharedPref.getUserId(Constants.user_id).toString())
        }
    }

    fun setupRecyclerViewNAdapter() {
        logsTodaysHitoryLiting.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvLogsUserHistory.layoutManager = mLinearLayoutManager
        adapter = LogsTodaysHistoryAdapter(mView.context, logsTodaysHitoryLiting, false,false)
        mView.rvLogsUserHistory.adapter = adapter

        adapter.onHeaderClick = { modelData, position ->
            //    if(modelData.headerStatus?.equals("Draft",ignoreCase = true)!!) {
            val myactivity = activity as HomeActivity
            var fragment: Fragment? = null
            if (modelData.headerStatus?.equals("Draft", ignoreCase = true)!!) {
                fragment = LogsListingFragment()

            } else {
                fragment = BarcodeBordereuDetailsFragment()
            }
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
        // }

    }


    fun getBodreuLogsUserHistoryRequest(userID:String): CommonRequest {
        var request : CommonRequest =   CommonRequest()
        request.setUserId(userID)
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setIsAdmin(false)
        val json =  Gson().toJson(request)
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
                    apiInterface.getBodereuLogsUserHistory(
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

    fun showNoInternetView() {
        mView.linContent.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.VISIBLE
    }

    fun showNoNoDataView() {
        mView.linContent.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.VISIBLE
    }

    fun showContentView() {
        mView.linContent.visibility = View.VISIBLE
        mView.relvNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
    }


    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivAddBodreu -> {
                var fragment = AddHeaderFragment()
                val bundle = Bundle()
                bundle.putString(
                    Constants.action,
                    Constants.action_non_edit
                );
                fragment.arguments = bundle
                (activity as HomeActivity).replaceFragment(fragment, false)
            }

            R.id.ivAddBodreu -> {
                var fragment = AddHeaderFragment()
                val bundle = Bundle()
                bundle.putString(
                        Constants.action,
                        Constants.action_non_edit
                );
                fragment.arguments = bundle
                (activity as HomeActivity).replaceFragment(fragment, false)
            }

            R.id.linOffline -> {
                var fragment = BorderauListFragment()
                (activity as HomeActivity).replaceFragment(fragment, false)
            }

            R.id.btnRetry -> {
                getBodreuLogsUserHistory(SharedPref.getUserId(Constants.user_id).toString())
            }
        }
    }


}