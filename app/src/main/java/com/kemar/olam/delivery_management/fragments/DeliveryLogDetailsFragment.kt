package com.kemar.olam.delivery_management.fragments

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
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
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.delivery_management.adapter.DeliveryLogsListingAdapter
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.*
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.dialog_layouut_add_transporter_n_vehicle.view.*
import kotlinx.android.synthetic.main.dialog_layouut_add_transporter_n_vehicle.view.linVehicleNo
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

class DeliveryLogDetailsFragment : Fragment() ,View.OnClickListener, DialogFragment.GetDialogListener {

    lateinit var mView: View
    lateinit var rvLogListing: RecyclerView
    lateinit var adapter: DeliveryLogsListingAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsLiting: ArrayList<BodereuLogListing> = arrayListOf()
    lateinit var mUtils: Utility
    //listings
    var commonForestMaster : GetForestDataRes? = GetForestDataRes()
    var isDialogShowing:Boolean = false
    lateinit var countryDialogFragment: DialogFragment
    lateinit var transporterNVehicleAlertView : View
    var essenceID:Int=0
    var commigFrom= ""
    var supplierLocationName  = ""
    var originName = ""
    var bodereuHeaderId = 0
    var forestID = 0
    var transporterID = 0
    var vehicleID =  0
    var suplierID =""
    var originID : Int? = 0
    var bodereuNumber = ""
    var fscOrNonFsc :String = ""
    var supplierShortName :String? = ""
    var transporterName:String?=""
    var inspectionID:String?=""
    var headerModel  : AddBodereuRes.BordereauResponse =  AddBodereuRes.BordereauResponse()
    var LogsListingResponce = GetBodereuLogByIdRes()
    var todaysHistoryModel  : LogsUserHistoryRes.BordereauRecordList =  LogsUserHistoryRes.BordereauRecordList()
    var transporterVehicleDialog: AlertDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_delivery_log_details, container, false)
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
            callingDeliveryMasterAPI()
            getDeliveryBodereuLogsByID(bodereuHeaderId.toString())
        }

        //when comming from headerScreen & User History Screen
        //suplierID == forest
        //
        if(commigFrom.equals( Constants.header, ignoreCase = true)) {
            var headerDataModel: AddBodereuRes.BordereauResponse? =
                arguments?.getSerializable(Constants.badereuModel) as AddBodereuRes.BordereauResponse
            if (headerDataModel != null) {
                headerModel =  headerDataModel
            }
            supplierLocationName = headerDataModel?.supplierName.toString()
            bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
            bodereuNumber = headerDataModel?.bordereauNo!!
            suplierID = headerDataModel?.supplier.toString()
            originID = headerDataModel?.originID
            originName= headerDataModel?.originName.toString()
            supplierShortName = headerDataModel?.supplierShortName
            transporterName= headerDataModel?.transporterName
            inspectionID = headerDataModel?.inspectionId.toString()
            fscOrNonFsc = headerDataModel?.fscName?.toString().toString()
           // headerDataModel?.modeOfTransport?.let { setupTransportMode(it,mView.context) }

            if (headerDataModel?.bordereauNo?.toString().isNullOrEmpty()) {
                mView.txtBO_NO.text = headerDataModel?.eBordereauNo?.toString().toString()
            } else {
                mView.txtBO_NO.text = headerDataModel?.bordereauNo?.toString().toString()

            }

            mView.txtGradername.text = headerDataModel?.gradeName?.toString().toString()
            mView.txtCustomerName.text = headerDataModel?.customerShortName?.toString().toString()
            mView.txtForestWagonNo.text = headerDataModel?.wagonNo?.toString().toString()
            mView.txtBO.text = headerDataModel?.inspectionNO?.toString().toString()

            callingDeliveryMasterAPI()
            getDeliveryBodereuLogsByID(inspectionID.toString())
        }else{
            var headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
                arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
            if (headerDataModel != null) {
                todaysHistoryModel =  headerDataModel
            }
            /* headerDataModel?.supplierId?.toString()?.let { callingForestMasterAPI(it) }*/
            bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
            supplierLocationName = headerDataModel?.supplierName.toString()
            originID = headerDataModel?.originId
            bodereuNumber = headerDataModel?.bordereauNo!!
            originName= headerDataModel?.originName.toString()
            supplierShortName = headerDataModel?.supplierShortName
            suplierID = headerDataModel?.supplierId.toString()
            transporterName= headerDataModel?.transporterName
            fscOrNonFsc = headerDataModel?.fscName?.toString().toString()
            inspectionID = headerDataModel?.inspectionId.toString()
            //headerDataModel?.transportMode?.let { setupTransportMode(it,mView.context) }



            if(headerDataModel?.module.isNullOrEmpty()){
                mView.lin_Bordero_No?.visibility =  View.VISIBLE
                if (headerDataModel?.bordereauNo?.isNullOrEmpty()!!) {
                    mView.txtBO_NO.text = headerDataModel?.eBordereauNo
                } else {
                    mView.txtBO_NO.text = headerDataModel?.bordereauNo

                }
            }else{
                mView.lin_Bordero_No?.visibility =  View.GONE
            }

            mView.txtGradername.text = headerDataModel?.gradeName?.toString().toString()
            mView.txtCustomerName.text = headerDataModel?.customerShortName?.toString().toString()
            mView.txtForestWagonNo.text = headerDataModel?.wagonNo?.toString().toString()
            mView.txtBO.text = headerDataModel?.inspectionNumber?.toString().toString()
            callingDeliveryMasterAPI()
            //originID?.toString()?.let { callingLogMasterAPI(suplierID?.toString(), it) }
            getDeliveryBodereuLogsByID(inspectionID.toString())
        }

    }


    fun setupClickListner() {
        mView.ivBOAdd.setOnClickListener(this)
        mView.ivHeaderEdit.setOnClickListener(this)
        mView.txtConfirmForDelivery.setOnClickListener(this)
    }



    private fun showTransporterNVehicleDialog() {
        val inflater =
            mView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val alertLayout: View = inflater.inflate(R.layout.dialog_layouut_add_transporter_n_vehicle, null)
        transporterNVehicleAlertView = alertLayout
        val alert: AlertDialog.Builder =
            AlertDialog.Builder(mView.context, R.style.CustomDialog)
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout)
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false)


        transporterNVehicleAlertView.linTransporter.setOnClickListener {
            showDialog(commonForestMaster?.getTransporterData() as ArrayList<SupplierDatum?>?,"transporter")
        }

        transporterNVehicleAlertView.linVehicleNo.setOnClickListener {
            showDialog(commonForestMaster?.getVehicleData() as ArrayList<SupplierDatum?>?,"vehicle")
        }
        transporterNVehicleAlertView.txtConfirmDelivery.setOnClickListener {
            if(isValidateTransporterNVehicleData()) {
                transporterVehicleDialog?.dismiss()
                callingConfirmForDeliveryAPI()
            }
        }


        alertLayout.ivLogCancel.setOnClickListener{
            transporterVehicleDialog?.dismiss()
        }

        transporterVehicleDialog = alert.create()
        transporterVehicleDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        transporterVehicleDialog?.show()
    }


    fun setupRecyclerViewNAdapter() {
        logsLiting?.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        rvLogListing.layoutManager = mLinearLayoutManager
        adapter = DeliveryLogsListingAdapter(mView.context, logsLiting,commigFrom)
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
            mView.txtTotalLogs.text =getString(R.string.total_found,count)// "Total $count Found"
        }

    }

    fun deleteLogsEntry(position: Int) {
        logsLiting.removeAt(position)
        adapter.notifyDataSetChanged()
        setTotalNoOfLogs(logsLiting.size)
    }



  /*  private fun callingLogMasterAPI(forestId:String,originID:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val call_api: Call<GetForestDataRes> =
                    apiInterface.getLogsMaster(forestId,originID)
                call_api.enqueue(object :
                    Callback<GetForestDataRes> {
                    override fun onResponse(
                        call: Call<GetForestDataRes>,
                        response: Response<GetForestDataRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: GetForestDataRes =
                                response.body()!!
                            if (responce != null) {
                                if (responce.getSeverity() == 200) {
                                    commonForestMaster =  responce
                                } else {

                                }
                            }
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        call: Call<GetForestDataRes>,
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

    private fun callingConfirmForDeliveryAPI() {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request =  generateConfirmForBillingRequest()

                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.confirmDelivery(request)
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



    fun generateConfirmForBillingRequest(): AddBoereuLogListingReq {
        var request: AddBoereuLogListingReq = AddBoereuLogListingReq()
        try{
            for (listdata in logsLiting) {
                listdata.setRejectionStatus("N")
                listdata.setComments("")
            }

            request.setUserID(SharedPref.getUserId(Constants.user_id))
            request.setBordereauHeaderId(bodereuHeaderId)
            request.setTimezoneId("Asia/Kolkata")
            request.setInspectionId(inspectionID?.toInt())

              /*  request.setCustomerSignBase(cuustomerSign)
                request.setRepresentativeSignBase(represetativeSign)*/
            request.setInspectionNumber(LogsListingResponce.getInspectionNumber())
            request.setTransporterId(transporterID)
            request.setTruckNo(vehicleID)
          /*  request.setSelectedLogs(LogsListingResponce.getSelectedLogs())
            request.setRejectedLogs(LogsListingResponce.getRejectedLogs())
            request.setResizedLogs(LogsListingResponce.getResizedLogs())
           */
            request.setTotalLogs(LogsListingResponce.getTotalLogs())
            request.setInspectionDate(mUtils.getCurrentDate())
            request.setBordereauLogList(logsLiting)
            var json = Gson().toJson(request)
            var test = json


        }catch (e:Exception){
            e.printStackTrace()
        }
        return request
    }


    fun callingDeliveryMasterAPIRequest(): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun callingDeliveryMasterAPI() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
val request  =  callingDeliveryMasterAPIRequest()
                val call_api: Call<GetForestDataRes> =
                    apiInterface.getMasterDataForDelivery(request)
                call_api.enqueue(object :
                    Callback<GetForestDataRes> {
                    override fun onResponse(
                        call: Call<GetForestDataRes>,
                        response: Response<GetForestDataRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                val responce: GetForestDataRes =
                                    response.body()!!
                                if (responce != null) {
                                    if (responce.getSeverity() == 200) {
                                        commonForestMaster = responce

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
                        call: Call<GetForestDataRes>,
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

    fun getDeliveryBodereuLogsByIDRequest(bodereuHeaderId: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setInspectionId(bodereuHeaderId)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }



    private fun getDeliveryBodereuLogsByID(bodereuHeaderId:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
val request  =  getDeliveryBodereuLogsByIDRequest(bodereuHeaderId)
                val call_api: Call<GetBodereuLogByIdRes> =
                    apiInterface.getDeliveryBodereuLogsByID(request)
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
                                    if (responce.getSeverity() == 200) {
                                        LogsListingResponce = responce
                                        logsLiting?.clear()
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
    //Dialog content
    open fun showDialog(countryListSearch:ArrayList<SupplierDatum?>?, action:String) {

        try {
            if (!countryListSearch.isNullOrEmpty()) {
                Collections.sort(
                    countryListSearch,
                    Comparator<SupplierDatum?> { contactOne, contactSecond ->
                        contactOne?.optionName!!.toLowerCase().compareTo(
                            contactSecond?.optionName!!.toLowerCase()
                        )
                    })
            }
        }catch (e:Exception){
        }

        val fm: FragmentManager
        val bundle: Bundle
        fm = childFragmentManager
        countryDialogFragment = DialogFragment(this)
        bundle = Bundle()
        bundle.putSerializable("COUNTRY_LIST", countryListSearch)
        countryDialogFragment.setArguments(bundle)
        bundle.putBoolean("isCountryCode", false)
        bundle.putString("action", action)
        countryDialogFragment.show(fm, "COUNTRY_FRAGMENT")
        countryDialogFragment.isCancelable = false
        isDialogShowing = true
    }

    override fun onSubmitData(model: SupplierDatum?, isCountryCode: Boolean, action : String) {

        try {
            countryDialogFragment.dismiss()
            isDialogShowing = false
            // mView.txtBordero_No.clearFocus()
        }catch (e:Exception){
            e.printStackTrace()
        }
        if (model != null) {
            try{
                when (action) {

                    "transporter"->{
                        transporterNVehicleAlertView.txtTransporter?.setText( model.optionName)
                        transporterID  = model.optionValue!!
                        transporterNVehicleAlertView.txtTransporter?.requestFocus()
                    }
                    "vehicle"->{
                        transporterNVehicleAlertView.txtDVehicleNo?.setText( model.optionName)
                        vehicleID  = model.optionValue!!
                        transporterNVehicleAlertView.txtDVehicleNo?.requestFocus()
                    }
                }

            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onCancleDialog() {
        try {
            countryDialogFragment.dismiss()
            isDialogShowing = false
        }catch (e:Exception){
            e.printStackTrace()

        }
    }


    fun isValidateTransporterNVehicleData():Boolean{
        if(transporterNVehicleAlertView.txtTransporter?.text=="Select"){
            mUtils.showAlert(activity, mView.context.getString(R.string.select_transporter))
            return false
        }
       else  if(transporterNVehicleAlertView.txtDVehicleNo?.text=="Select"){
            mUtils.showAlert(activity, mView.context.getString(R.string.select_vehicle_no))
            return false
        }
        return  true
    }
    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivBOAdd -> {
                //user should be add only 20 logs
                if(logsLiting.size !=20) {

                }
            }

            R.id.txtConfirmForDelivery ->{
                showTransporterNVehicleDialog()
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

}
