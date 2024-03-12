package com.kemar.olam.loading_wagons.fragment


import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.request.BoderueDeleteLogReq
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.loading_wagons.adapter.LodingWa_BoDetailsAdapter
import com.kemar.olam.loading_wagons.model.request.WagonLogRequest
import com.kemar.olam.loading_wagons.model.responce.DeclrationBordereuListRes
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.*
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_loading_wagons_logs_listing.view.*
import kotlinx.android.synthetic.main.layout_logs_listing.view.rvLogListing
import kotlinx.android.synthetic.main.layout_logs_listing.view.swipeLogListing
import kotlinx.android.synthetic.main.layout_logs_listing.view.tvNoDataFound
import kotlinx.android.synthetic.main.layout_logs_listing.view.txtBO
import kotlinx.android.synthetic.main.layout_logs_listing.view.txtBO_NO
import kotlinx.android.synthetic.main.layout_logs_listing.view.txtForestWagonNo
import kotlinx.android.synthetic.main.layout_logs_listing.view.txtTotalLogs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class LoadingWaDetailsFragment : Fragment() {

    lateinit var mView: View
    lateinit var rvLogListing: RecyclerView
    lateinit var adapter: LodingWa_BoDetailsAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsLiting: ArrayList<BodereuLogListing> = arrayListOf()
    lateinit var mUtils: Utility
    //listings
    var commonForestMaster : GetForestDataRes? = GetForestDataRes()
    var isDialogShowing:Boolean = false
    lateinit var countryDialogFragment: DialogFragment
    lateinit var alertView : View
    var essenceID:Int=0
    var commigFrom= ""
    var offline= false
    var supplierLocationName  = ""
    var originName = ""
    var bodereuHeaderId = 0
    var forestUniqueId :String = ""
    var forestID = 0
    var suplierID =""
    var originID : Int? = 0
    var bodereuNumber :String?= ""
    var speciesID = 0
    var aacID = 0
    var qualityId :Int?= 0
    var fscOrNonFsc :String = ""
    var supplierShortName :String? = ""
    var transporterName:String?=""
    var aacYear = ""
    var todaysHistoryModel  : DeclrationBordereuListRes.BordereauRecordList =  DeclrationBordereuListRes.BordereauRecordList()
    var todaysHistoryModel_   : LogsUserHistoryRes.BordereauRecordList =  LogsUserHistoryRes.BordereauRecordList()

    lateinit var realm: Realm
    var isFromUserHistory = false
    lateinit var  wagonLogRequestlist: RealmResults<WagonLogRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        realm = RealmHelper.getRealmInstance()
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_loading_wa_details, container, false)
        initViews()
        return mView
    }

    fun initViews() {
        commigFrom =
            arguments?.getString(Constants.comming_from).toString()
        isFromUserHistory = commigFrom.equals(Constants.USER_HISTORY,ignoreCase = true)
        offline = arguments?.getBoolean(Constants.Offline,false)!!
        isFromUserHistory = commigFrom.equals(Constants.USER_HISTORY,ignoreCase = true)
        //  (activity as HomeActivity).showActionBar()
        rvLogListing = mView.findViewById(R.id.rvLogListing)
        mUtils = Utility()
        setToolbar()
        setupRecyclerViewNAdapter()

        mView.swipeLogListing.setOnRefreshListener {
            mView.swipeLogListing.isRefreshing = false
        }

        //when comming from headerScreen & User History Screen
        //suplierID == forest
        //
        try{
            if (commigFrom.equals(Constants.DECLARATION, ignoreCase = true)) {
                var headerDataModel: DeclrationBordereuListRes.BordereauRecordList? =
                    arguments?.getSerializable(Constants.badereuModel) as DeclrationBordereuListRes.BordereauRecordList
                if (headerDataModel != null) {
                    todaysHistoryModel = headerDataModel
                }
                /* headerDataModel?.supplierId?.toString()?.let { callingForestMasterAPI(it) }*/
                bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
                supplierLocationName = headerDataModel?.supplierName.toString()
                originID = headerDataModel?.originId
                originName = headerDataModel?.originName.toString()
                supplierShortName = headerDataModel?.supplierShortName
                suplierID = headerDataModel?.supplierId.toString()
                transporterName = headerDataModel?.transporterName
                fscOrNonFsc = headerDataModel?.fscName?.toString().toString()
                if (headerDataModel?.bordereauNo?.toString().isNullOrEmpty()) {
                    mView.linManual.visibility = GONE
                    bodereuNumber = headerDataModel?.eBordereauNo!!
                    mView.txtElecTronicBO_NO.text =
                        headerDataModel?.eBordereauNo?.toString().toString()
                } else {
                    mView.linManual.visibility = VISIBLE
                    bodereuNumber = headerDataModel?.eBordereauNo!!
                    mView.txtElecTronicBO_NO.text =
                        headerDataModel?.eBordereauNo?.toString().toString()
                    mView.txtBO_NO.text = headerDataModel?.bordereauNo?.toString().toString()

                }
                mView.txtForestWagonNo.text = headerDataModel?.wagonNo?.toString().toString()
                mView.txtBO.text = headerDataModel?.recordDocNo?.toString().toString()
                getBodereuLogsByID(bodereuHeaderId.toString())

            }else{

                val headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
                    arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
                if (headerDataModel != null) {
                    todaysHistoryModel_ = headerDataModel
                }
                try{
                    forestUniqueId= headerDataModel?.uniqueId!!
                    Log.e("forestUniqueId", "is " + forestUniqueId)
                }catch (e: Exception){
                    e.printStackTrace()
                }

                /* headerDataModel?.supplierId?.toString()?.let { callingForestMasterAPI(it) }*/
                bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
                supplierLocationName = headerDataModel?.supplierName.toString()
                originID = headerDataModel?.originId
                originName = headerDataModel?.originName.toString()
                supplierShortName = headerDataModel?.supplierShortName
                suplierID = headerDataModel?.supplierId.toString()
                transporterName = headerDataModel?.transporterName
                fscOrNonFsc = headerDataModel?.fscName?.toString().toString()
                if (headerDataModel?.bordereauNo?.toString().isNullOrEmpty()) {
                    mView.linManual.visibility = View.GONE
                    if (!headerDataModel?.eBordereauNo.isNullOrEmpty()) {
                        bodereuNumber = headerDataModel?.eBordereauNo!!
                        mView.txtElecTronicBO_NO.text = headerDataModel?.eBordereauNo?.toString().toString()
                    }
                } else {
                    mView.linManual.visibility = View.VISIBLE
                    if (!headerDataModel?.eBordereauNo.isNullOrEmpty()) {
                        bodereuNumber = headerDataModel?.eBordereauNo!!
                        mView.txtElecTronicBO_NO.text = headerDataModel?.eBordereauNo?.toString().toString()
                        mView.txtBO_NO.text = headerDataModel?.bordereauNo?.toString().toString()
                    }

                }
                mView.txtForestWagonNo.text = headerDataModel?.wagonNo?.toString().toString()
                mView.txtBO.text = headerDataModel?.recordDocNo?.toString().toString()

                if(offline){
                    realm.executeTransaction {
                        wagonLogRequestlist= realm.where(WagonLogRequest::class.java).equalTo("forestuniqueId", forestUniqueId, Case.SENSITIVE).findAll()
                        Log.e("wagonlogList", "is " + wagonLogRequestlist.size)
                        logsLiting.clear()
                        for (wagon in wagonLogRequestlist){
                            var bordereuLoglist=BodereuLogListing()
                            bordereuLoglist.setBarcodeNumber(wagon.getBarcodeNumber())
                            bordereuLoglist.setQualityId(wagon.getQualityId())
                            bordereuLoglist.isUploaded=wagon.isUploaded
                            bordereuLoglist.isFailure=wagon.isFailure
                            bordereuLoglist.setQuality(wagon.getQuality())
                            bordereuLoglist.setLogNo2(wagon.getLogNo2())
                            bordereuLoglist.setLogNo(wagon.getLogNo())
                            bordereuLoglist.setLogRecordDocNo(wagon.getLogRecordDocNo())
                            bordereuLoglist.setSupplierShortName(wagon.getSupplierShortName())
                            bordereuLoglist.setSupplierName(wagon.getSupplierName())
                            bordereuLoglist.setDetailId(wagon.getDetailId())
                            bordereuLoglist.setMaterialDesc(wagon.getMaterialDesc())
                            bordereuLoglist.setDiamBdx(wagon.getDiamBdx())
                            bordereuLoglist.setDiamBdx1(wagon.getDiamBdx1())
                            bordereuLoglist.setDiamBdx2(wagon.getDiamBdx2())
                            bordereuLoglist.setDiamBdx3(wagon.getDiamBdx3())
                            bordereuLoglist.setDiamBdx4(wagon.getDiamBdx4())
                            bordereuLoglist.setCbmQuantity(wagon.getCbmQuantity())
                            bordereuLoglist.setCbm(wagon.getCbm())
                            bordereuLoglist.setLongBdx(wagon.getLongBdx())
                            bordereuLoglist.setAvrageBdx(wagon.getaverageBdx())
                            bordereuLoglist.setBordereauHeaderId(wagon.getBordereauHeaderId())
                            bordereuLoglist.setBordereaDetailStatus(wagon.getBordereaDetailStatus())
                            bordereuLoglist.setBordereauNo(wagon.getBordereauNo())
                            bordereuLoglist.setAAC(wagon.getAAC())
                            bordereuLoglist.setAACYear(wagon.getAACYear())
                            bordereuLoglist.setAACName(wagon.getAACName())
                            bordereuLoglist.setLogSpecies(wagon.getLogSpecies())
                            bordereuLoglist.setLogSpeciesName(wagon.getLogSpeciesName())
                            bordereuLoglist.setPlaqNo(wagon.getPlaqNo())
                            logsLiting.add(bordereuLoglist)
                        }
                        adapter.notifyDataSetChanged()
                        setTotalNoOfLogs(logsLiting.size)
                    }
                }else {
                    getBodereuLogsByID(bodereuHeaderId.toString())
                }


            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun setupRecyclerViewNAdapter() {
        logsLiting?.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        rvLogListing.layoutManager = mLinearLayoutManager
        adapter = LodingWa_BoDetailsAdapter(mView.context, logsLiting,isFromUserHistory,offline)
        rvLogListing.adapter = adapter
        //Expand Collapse Action
        adapter.onMoreClick = { modelData, position, isExpanded ->
            logsLiting[position].isExpanded = isExpanded
            adapter.notifyDataSetChanged()
        }

        adapter.onRemoveClick = { modelData, position ->
            funShowAlertForRemoveLog(bodereuHeaderId,modelData.getDetailId())

        }

    }

    fun funShowAlertForRemoveLog(bodereuHeaderId: Int?,detaileId:String?){
        val alert: AlertDialog =
                android.app.AlertDialog.Builder(view?.context, R.style.CustomDialogWithBackground)
                        //.setTitle(activity?.getString(R.string.app_name))
                        .setMessage(mView.context.resources.getString(R.string.are_you_sure_want_to_delete_log))
                        .setPositiveButton("Ok",
                                DialogInterface.OnClickListener { dialog, which ->
                                    dialog.dismiss()

                                    removeLogFromLoadedBordereau(bodereuHeaderId,detaileId)
                                })
                        .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        })
                        .show()
        alert.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE)
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE)

        val positive: Button =
                alert.getButton(DialogInterface.BUTTON_POSITIVE)
        positive.setTextColor(resources.getColor(R.color.colorPrimaryDark))

        val negative: Button =
                alert.getButton(DialogInterface.BUTTON_NEGATIVE)
        negative.setTextColor(resources.getColor(R.color.colorPrimaryDark))
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.loading_wagons)
        (activity as HomeActivity).invisibleFilter()
    }


    private fun setTotalNoOfLogs(count: Int) {
        if (count == 0) {
            mView.tvNoDataFound.visibility= VISIBLE
            mView.rvLogListing.visibility= GONE
            mView.txtTotalLogs.visibility = INVISIBLE
            mView.txtTotalLogs.text =getString(R.string.total_found,count) //"Total $count Found"
        } else {
            mView.tvNoDataFound.visibility= GONE
            mView.rvLogListing.visibility= VISIBLE
            mView.txtTotalLogs.visibility = VISIBLE
            mView.txtTotalLogs.text = getString(R.string.total_found,count)//"Total $count Found"
        }

    }

    fun deleteLogsEntry(position: Int) {
        logsLiting.removeAt(position)
        adapter.notifyDataSetChanged()
        mUtils.showToast(activity, mView.resources.getString(R.string.log_deleted_successfully))
        setTotalNoOfLogs(logsLiting.size)
    }




    fun generateDeleteBodereuLogRequest(position: Int): BoderueDeleteLogReq {

        var request : BoderueDeleteLogReq =   BoderueDeleteLogReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(bodereuHeaderId)
        request.setDeleteDetailId(logsLiting.get(position)?.getDetailId()?.toInt())

        return  request

    }

    fun generateRemoeLogRequest(bodereuHeaderId: Int?,detaileId:String?): CommonRequest {

        var request : CommonRequest =   CommonRequest()
        request.setBordereauHeaderId(bodereuHeaderId.toString())
        request.setDeleteDetailId(detaileId)

        return  request

    }

    private fun removeLogFromLoadedBordereau(bodereuHeaderId: Int?,detaileId:String?) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request  =  generateRemoeLogRequest(bodereuHeaderId,detaileId)
                val call_api: Call<AddBodereuRes> =
                    apiInterface.deleteLogFromDeclare(request)
                call_api.enqueue(object :
                    Callback<AddBodereuRes> {
                    override fun onResponse(
                        call: Call<AddBodereuRes>,
                        response: Response<AddBodereuRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                val responce: AddBodereuRes =
                                    response.body()!!
                                if (responce != null) {

                                    if (responce.getSeverity() == 200) {
                                        getBodereuLogsByID(bodereuHeaderId.toString())
                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showAlert(
                                            activity,
                                            responce.getMessage()
                                        )
                                    }
                                }
                            } else if (response.code() == 306) {
                                mUtils.alertDialgSession(mView.context, activity)
                            } else{
                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        call: Call<AddBodereuRes>,
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


    fun checkLogAACNLogNoAlreadyExits(aacName:String?,logNumber:String):Boolean{
        for((index,listdata)  in logsLiting.withIndex()){
            if(!aacName.isNullOrEmpty() && !logNumber.isNullOrEmpty())
                if (listdata.getLogNo().toString().contains(logNumber) && listdata.getAACName().toString().contains(aacName!!)) {
                    return false
                }
        }
        return true
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
                                if (response.body()?.getSeverity() == 200) {
                            val responce: GetBodereuLogByIdRes =
                                response.body()!!
                            if (responce != null) {
                                logsLiting?.clear()
                                responce.getBordereauLogList()?.let {
                                    logsLiting?.addAll(
                                        it
                                    )
                                }
                                setTotalNoOfLogs(logsLiting?.size)
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
