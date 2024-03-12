package com.kemar.olam.bordereau.fragment


import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.adapter.BoLogsDetailsAdapter
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.RealmHelper
import com.kemar.olam.utility.Utility
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.layout_logs_listing.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList


class BarcodeBordereuDetailsFragment : Fragment() {

    lateinit var realm: Realm
    lateinit var mView: View
    lateinit var rvLogListing: RecyclerView
    lateinit var adapter: BoLogsDetailsAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsLiting: ArrayList<BodereuLogListing> = arrayListOf()

    lateinit var  borderauList: RealmResults<BodereuLogListing>
    lateinit var mUtils: Utility
    //listings
    var commonForestMaster : GetForestDataRes? = GetForestDataRes()

    var commigFrom= ""
    var bodereuHeaderId = 0
    var forestUniqueId :String = ""

    var isOffline=false
    var todaysHistoryModel  : LogsUserHistoryRes.BordereauRecordList =  LogsUserHistoryRes.BordereauRecordList()


    override fun onCreate(savedInstanceState: Bundle?) {

        realm = RealmHelper.getRealmInstance()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_barcode_bordereu_details, container, false)
        initViews()
        return mView;
    }

    fun initViews() {
        commigFrom =
            arguments?.getString(Constants.comming_from).toString();
        isOffline = arguments?.getBoolean(Constants.Offline,false)!!
        rvLogListing = mView.findViewById(R.id.rvLogListing)
        mUtils = Utility()
        setToolbar()


        mView.swipeLogListing.setOnRefreshListener{
            mView.swipeLogListing.isRefreshing =  false
            if(!isOffline) {
                getBodereuLogsByID(bodereuHeaderId.toString())
            }
        }

        //when comming from headerScreen & User History Screen
        //suplierID == forest
        //
        if(commigFrom.equals("UserHistory", ignoreCase = true)) {
            val headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
                arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
            if (headerDataModel != null) {
                todaysHistoryModel =  headerDataModel
            }
            bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
            setupRecyclerViewNAdapter()
            try{
                forestUniqueId= headerDataModel?.uniqueId!!
                Log.e("forestUniqueId", "is " + forestUniqueId)
            }catch (e: Exception){
                e.printStackTrace()
            }

            headerDataModel?.transportMode?.let { setupTransportMode(it,mView.context) }
            mView.txtBO_NO.text = headerDataModel?.bordereauNo
            mView.txtForestWagonNo.text = headerDataModel?.wagonNo
            mView.txtBO.text = headerDataModel?.recordDocNo

            if (mUtils.checkInternetConnection(mView.context)) {
                getBodereuLogsByID(bodereuHeaderId.toString())
            }else{
                borderauList = realm.where(BodereuLogListing::class.java).equalTo("forestuniqueId", forestUniqueId, Case.SENSITIVE).findAll()
                Log.e("borderauList", "is " + borderauList.size)

                logsLiting.addAll(realm.copyFromRealm(borderauList))
                setupRecyclerViewOfflineNAdapter()
                adapter.notifyDataSetChanged()
                setTotalNoOfLogs(logsLiting.size)
            }
        }else{

            val headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
                arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
            if (headerDataModel != null) {
                todaysHistoryModel =  headerDataModel
            }
            bodereuHeaderId = headerDataModel?.bordereauHeaderId!!

            try{
                forestUniqueId= headerDataModel?.uniqueId!!
                Log.e("forestUniqueId", "is " + forestUniqueId)
            }catch (e: Exception){
                e.printStackTrace()
            }

            headerDataModel?.transportMode?.let { setupTransportMode(it,mView.context) }
            mView.txtBO_NO.text = headerDataModel?.bordereauNo
            mView.txtForestWagonNo.text = headerDataModel?.wagonNo
            mView.txtBO.text = headerDataModel?.recordDocNo

            borderauList = realm.where(BodereuLogListing::class.java).equalTo("forestuniqueId", forestUniqueId, Case.SENSITIVE).findAll()
            Log.e("borderauList", "is " + borderauList.size)

            logsLiting.addAll(realm.copyFromRealm(borderauList))



            setupRecyclerViewOfflineNAdapter()
            adapter.notifyDataSetChanged()

            setTotalNoOfLogs(logsLiting.size)
        }

    }


    fun setupTransportMode(transportMode:Int, context: Context) {
        when (transportMode) {
            1 -> {
               mView.txtTrasnporterTtile.text = context.resources.getString(R.string._wagon_no)
            }
            17 -> {
               mView.txtTrasnporterTtile.text = context.resources.getString(R.string._barge_no)
            }
            3 -> {
               mView.txtTrasnporterTtile.text = context.resources.getString(R.string._truck_no)
            }
            else -> {

            }
        }
    }




    fun setupRecyclerViewNAdapter() {
        logsLiting?.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        rvLogListing.layoutManager = mLinearLayoutManager
        adapter = BoLogsDetailsAdapter(mView.context, logsLiting,isOffline)
        rvLogListing.adapter = adapter
        //Expand Collapse Action
        adapter.onMoreClick = { modelData, position, isExpanded ->
            if (isExpanded) {
                logsLiting.get(position)?.isExpanded = true
            } else {
                logsLiting.get(position)?.isExpanded = false
            }
            adapter.notifyDataSetChanged()
        }

    }

    fun setupRecyclerViewOfflineNAdapter() {
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        rvLogListing.layoutManager = mLinearLayoutManager
        adapter = BoLogsDetailsAdapter(mView.context, logsLiting,isOffline)
        rvLogListing.adapter = adapter
        //Expand Collapse Action
        adapter.onMoreClick = { modelData, position, isExpanded ->
            if (isExpanded) {
                logsLiting.get(position)?.isExpanded = true
            } else {
                logsLiting.get(position)?.isExpanded = false
            }
            adapter.notifyDataSetChanged()
        }

    }


    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.bordereau_module)
        (activity as HomeActivity).invisibleFilter()
    }


    private fun setTotalNoOfLogs(count: Int) {
        if (count == 0) {
           // mView.linFooter.visibility =  View.INVISIBLE
            mView.tvNoDataFound.visibility=View.VISIBLE
            mView.rvLogListing.visibility=View.GONE
            mView.txtTotalLogs.visibility =View.INVISIBLE
            mView.txtTotalLogs.text = getString(R.string.total_found,count)//"Total $count Found"
        } else {
          //  mView.linFooter.visibility =  View.VISIBLE
            mView.tvNoDataFound.visibility=View.GONE
            mView.rvLogListing.visibility=View.VISIBLE
            mView.txtTotalLogs.visibility =View.VISIBLE
            mView.txtTotalLogs.text =getString(R.string.total_found,count) //"Total $count Found"
        }

    }

    fun getBodereuLogsByIDRequest(bodereuHeaderId:String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setHeaderId(bodereuHeaderId)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    private fun getBodereuLogsByID(bodereuHeaderId:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val request  =  getBodereuLogsByIDRequest(bodereuHeaderId)
                val call_api: Call<GetBodereuLogByIdRes> =
                    apiInterface.getBodereuLogsByID(request)
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
                                        setTotalNoOfLogs(logsLiting.size)
                                        adapter.notifyDataSetChanged()
                                    }
                                }else if (response.body()?.getSeverity() == 306) {
                                    mUtils.alertDialgSession(mView.context, activity)
                                } else {
                                    mUtils.showToast(activity, response.body().getMessage())
                                }

                            } else if (response.code() == 306) {
                                mUtils.alertDialgSession(mView.context, activity)
                            }
                            else{
                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                        }catch (e:Exception){
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
        }else{
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }




}
