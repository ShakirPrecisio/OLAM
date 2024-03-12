package com.kemar.olam.delivery_management.fragments

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.delivery_management.adapter.DeliverdBordereuLogsAdapter
import com.kemar.olam.delivery_management.model.request.RevokeLogsReq
import com.kemar.olam.delivery_management.model.response.AddHeaderForDeliveryRes
import com.kemar.olam.delivery_management.model.response.LogsUserHistoryRes
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.dialog_layouut_add_transporter_n_vehicle.view.ivLogCancel
import kotlinx.android.synthetic.main.dialog_view_comment.view.*
import kotlinx.android.synthetic.main.fragment_deliveerd_logs_details.view.*
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.ivBOAdd
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.ivHeaderEdit
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.linFooter
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.rvLogListing
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.swipeLogListing
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.tvNoDataFound
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.txtBO
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.txtForestWagonNo
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.txtTotalLogs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class DeliveredLogsDetailsFragment : Fragment() ,View.OnClickListener{

    lateinit var mView: View
    lateinit var rvLogListing: RecyclerView
    lateinit var adapter: DeliverdBordereuLogsAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsLiting: ArrayList<BodereuLogListing> = arrayListOf()
    lateinit var mUtils: Utility
    //listings
    var commonForestMaster : GetForestDataRes? = GetForestDataRes()
    var isDialogShowing:Boolean = false
    lateinit var countryDialogFragment: DialogFragment
    lateinit var transporterNVehicleAlertView : View
    var commigFrom= ""
    var supplierLocationName  = ""
    var forestID = 0
    var headerModel  : AddHeaderForDeliveryRes =  AddHeaderForDeliveryRes()
    var LogsListingResponce = GetBodereuLogByIdRes()
    var todaysHistoryModel  : LogsUserHistoryRes.UserHist =  LogsUserHistoryRes.UserHist()
    var transporterVehicleDialog: AlertDialog? = null


    var customerID  :Int? = 0
    var deliveryId:Int?=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_deliveerd_logs_details, container, false)
        initViews()
        return mView;
    }


    fun initViews() {
        commigFrom =
            arguments?.getString(Constants.comming_from).toString();
        rvLogListing = mView.findViewById(R.id.rvLogListing)
        mUtils = Utility()
        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()
        // setupFooterButtonAsPerUserRole()

        mView.swipeLogListing.setOnRefreshListener{
            mView.swipeLogListing.isRefreshing =  false
            getDeliveryBodereuLogsByDeliveryID(deliveryId.toString())
        }

        //when comming from headerScreen & User History Screen
        //suplierID == forest
        //
        if(commigFrom.equals( Constants.header, ignoreCase = true)) {

        }else{
            val headerDataModel: LogsUserHistoryRes.UserHist? =
                arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.UserHist
            if (headerDataModel != null) {
                todaysHistoryModel =  headerDataModel
            }
            /* headerDataModel?.supplierId?.toString()?.let { callingForestMasterAPI(it) }*/
           // supplierLocationName = headerDataModel?.supplierName.toString()
            deliveryId = headerDataModel?.deliveryId
            //headerDataModel?.transportMode?.let { setupTransportMode(it,mView.context) }

           /* if ( headerDataModel?.bordereauNo?.isNullOrEmpty() || !headerDataModel?.eBordereauNo?.isNullOrEmpty()){
                mView.lin_Bordero_No_?.visibility =  View.VISIBLE
                if (headerDataModel?.bordereauNo?.isNullOrEmpty()!!) {
                    mView.txtBO_NO.text = headerDataModel?.eBordereauNo
                } else {
                    mView.txtBO_NO.text = headerDataModel?.bordereauNo

                }
            }else{
                mView.lin_Bordero_No_?.visibility =  View.GONE
            }*/

          /*  if (headerDataModel?.bordereauNo?.isNullOrEmpty()!!) {
                mView.txtBO_NO.text = headerDataModel?.eBordereauNo
            } else {
                mView.txtBO_NO.text = headerDataModel?.bordereauNo

            }*/

          //  mView.txtGradername_.text = headerDataModel?.gradeName?.toString()
            mView.txtTruckNo.text=headerDataModel?.truckName
            mView.txtCustomerName_.text = headerDataModel?.customerShortName?.toString()
            mView.txtForestWagonNo.text = headerDataModel?.truckName?.toString()
            mView.txtBO.text = headerDataModel?.deliveryNumber?.toString()

            getDeliveryBodereuLogsByDeliveryID(deliveryId.toString())
        }

    }


    fun setupClickListner() {
        mView.ivBOAdd.setOnClickListener(this)
        mView.ivHeaderEdit.setOnClickListener(this)
        mView.txtDRevoke.setOnClickListener(this)
    }

    fun isValidateForLogsSelectedForRevoke():Boolean{
        var returnValue:Boolean = false
        var itemSelectedCount = 0
        for(listdata  in logsLiting){
            if(listdata?.isSelected){
                itemSelectedCount++
            }
        }
        if(itemSelectedCount!=0){
            returnValue = true
        }else{
            returnValue = false
        }
        return returnValue
    }

    fun setupRecyclerViewNAdapter() {
        logsLiting?.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        rvLogListing.layoutManager = mLinearLayoutManager
        adapter = DeliverdBordereuLogsAdapter(mView.context, logsLiting,commigFrom)
        rvLogListing.adapter = adapter
        //Expand Collapse Action
        adapter.onMoreClick = { modelData, position, isExpanded ->
            logsLiting.get(position).isExpanded = isExpanded
            adapter.notifyDataSetChanged()
        }

        adapter.onEditClick = { modelData, position ->

        }

        adapter.onDeleteClick = { modelData, position ->
        }

        adapter.onAddClick = { modelData, position ->

        }
    }


    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.drawer_delivery_management)
        (activity as HomeActivity).invisibleFilter()
    }


    private fun setTotalNoOfLogs(count: Int) {
        if (count == 0) {
            mView.linFooter.visibility =  View.INVISIBLE
            mView.tvNoDataFound.visibility=View.VISIBLE
            mView.rvLogListing.visibility=View.GONE
            mView.txtTotalLogs.visibility =View.INVISIBLE
            mView.txtTotalLogs.text = getString(R.string.total_found,count)//"Total $count Found"
        } else {
            mView.linFooter.visibility =  View.VISIBLE
            mView.tvNoDataFound.visibility=View.GONE
            mView.rvLogListing.visibility=View.VISIBLE
            mView.txtTotalLogs.visibility =View.VISIBLE
            mView.txtTotalLogs.text = getString(R.string.total_found,count)//"Total $count Found"
        }

    }

    fun deleteLogsEntry(position: Int) {
        logsLiting.removeAt(position)
        adapter.notifyDataSetChanged()
        setTotalNoOfLogs(logsLiting.size)
    }





    fun printPDF(pdfPath: String) {
        val printManager =
            mView.context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter =
            PdfDocumentAdapter(mView.context, pdfPath)
        printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
    }

    fun downloadPDFFromURL(pdfPath: String) {
        mUtils.showProgressDialog(mView.context)
        try {
            val mFileName = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
            var destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
            var fileName = "$mFileName.pdf";
            destination += fileName;
            var uri = Uri.parse("file://" + destination);
            val pdfURL = pdfPath;

            var request = DownloadManager.Request(Uri.parse(pdfURL));
            request.setDescription("Downloading....");
            request.setTitle(" OLAM ");
            request.setDestinationUri(uri);
            val manager =
                mView.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager;
            manager.enqueue(request);
            val finalDestination = destination

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    mView.context.unregisterReceiver(this)
                    mUtils.dismissProgressDialog()
                    printPDF(finalDestination)

                }
            }
            mView.context.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            );
        } catch (e: Exception) {
            mUtils.dismissProgressDialog()
            e.printStackTrace()
        }

    }

    private fun callingRevokeAPI(comment:String) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request =  generateRevokeRequest(comment)

                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.revokeDeliveredLogsList(request)
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
                                        var fragment  = DeliveryUserHistoryFragment()
                                        val bundle = Bundle()
                                        bundle.putString(
                                            Constants.comming_from,
                                            Constants.Bc_main_screen)
                                        fragment.arguments = bundle
                                        (activity as HomeActivity).replaceFragment(fragment,true)
                                    }else if (response.body()?.getSeverity() == 306) {
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
                        }catch (e:Exception){
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
        }else{
            mUtils.showToast(view?.context, getString(R.string.no_internet))
        }
    }



    fun generateRevokeRequest(comment: String): RevokeLogsReq {
        var request: RevokeLogsReq = RevokeLogsReq()
         var detailIdsList : ArrayList<Int> =  arrayListOf()
        try{
            for (listdata in logsLiting) {
                if(listdata.isSelected) {
                    listdata.getDetailId()?.toInt()?.let { detailIdsList.add(it) }
                }
            }

            request.setUserID(SharedPref.getUserId(Constants.user_id))
            request.setDetailIds(detailIdsList)
             request.setComments(comment)
            var json = Gson().toJson(request)
            var test = json

        }catch (e:Exception){
            e.printStackTrace()
        }
        return request
    }

    fun getDeliveryBodereuLogsByDeliveryIDRequest(deliveryId:String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setDeliveryId(deliveryId)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun getDeliveryBodereuLogsByDeliveryID(deliveryId:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
val request =  getDeliveryBodereuLogsByDeliveryIDRequest(deliveryId)
                val call_api: Call<GetBodereuLogByIdRes> =
                    apiInterface.getLogByDeliveryId(request)
                call_api.enqueue(object :
                    Callback<GetBodereuLogByIdRes> {
                    override fun onResponse(
                        call: Call<GetBodereuLogByIdRes>,
                        response: Response<GetBodereuLogByIdRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                val responce: GetBodereuLogByIdRes =
                                    response.body()!!
                                if (responce != null) {
                                    LogsListingResponce = GetBodereuLogByIdRes()
                                    logsLiting?.clear()
                                    if (responce.getSeverity() == 200) {
                                        LogsListingResponce = responce
                                        responce.getBordereauLogList()?.let {
                                            logsLiting?.addAll(
                                                it
                                            )
                                        }
                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                    setTotalNoOfLogs(logsLiting?.size)
                                    adapter.notifyDataSetChanged()
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


/*
    private fun getDeliveryBodereuLogsByID(bodereuHeaderId:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val call_api: Call<GetBodereuLogByIdRes> =
                    apiInterface.getDeliveryBodereuLogsByID(bodereuHeaderId)
                call_api.enqueue(object :
                    Callback<GetBodereuLogByIdRes> {
                    override fun onResponse(
                        call: Call<GetBodereuLogByIdRes>,
                        response: Response<GetBodereuLogByIdRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: GetBodereuLogByIdRes =
                                response.body()!!
                            if (responce != null) {
                                if(responce.getSeverity()==200) {
                                    LogsListingResponce = responce
                                    logsLiting?.clear()
                                    responce.getBordereauLogList()?.let {
                                        logsLiting?.addAll(
                                            it
                                        )
                                    }
                                }
                                setTotalNoOfLogs(logsLiting?.size)
                                adapter.notifyDataSetChanged()
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
*/


    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivBOAdd -> {
                //user should be add only 20 logs
                if(logsLiting.size !=20) {

                }
            }

            R.id.txtDRevoke ->{
                if(isValidateForLogsSelectedForRevoke()) {
                    showCommentDialog()
                }else{
                    mUtils.showAlert(activity, resources.getString(R.string.please_select_logs))
                }
            }


            R.id.ivHeaderEdit ->{
                /*if(commigFrom.equals( Constants.header, ignoreCase = true)) {
                    val fragment = AddHeaderFragment()
                    val bundle = Bundle()
                    bundle.putSerializable(
                        Constants.badereuModel,
                        headerModel
                    )
                    bundle.putString(
                        Constants.action,
                        Constants.action_edit
                    )
                    bundle.putString(
                        Constants.comming_from,
                        Constants.header
                    )
                    fragment.arguments = bundle
                    (activity as HomeActivity).replaceFragment(fragment,false)

                }else{
                    val fragment = AddHeaderFragment()
                    val bundle = Bundle()
                    bundle.putSerializable(
                        Constants.badereuModel,
                        todaysHistoryModel
                    )
                    bundle.putString(
                        Constants.action,
                        Constants.action_edit
                    )
                    bundle.putString(
                        Constants.comming_from,
                        Constants.todays_history
                    )
                    fragment.arguments = bundle
                    (activity as HomeActivity).replaceFragment(fragment,false)
                }*/
            }
        }
    }


    private fun showCommentDialog() {
        val inflater =
            mView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val alertLayout: View = inflater.inflate(R.layout.dialog_view_comment, null)
        transporterNVehicleAlertView = alertLayout
        val alert: AlertDialog.Builder =
            AlertDialog.Builder(mView.context, R.style.CustomDialog)
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout)
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false)


        transporterNVehicleAlertView.linRevoke.setOnClickListener {
                transporterVehicleDialog?.dismiss()
                callingRevokeAPI(transporterNVehicleAlertView.txtComment.text.toString())
        }


        alertLayout.ivLogCancel.setOnClickListener{
            transporterVehicleDialog?.dismiss()
        }

        transporterVehicleDialog = alert.create()
        transporterVehicleDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        transporterVehicleDialog?.show()
    }


}
