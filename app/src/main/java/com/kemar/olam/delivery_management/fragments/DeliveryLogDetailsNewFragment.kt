package com.kemar.olam.delivery_management.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.ContactsContract
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.dashboard.activity.BarcodeScanActivity
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.delivery_management.adapter.DeliveryLogsListingAdapter
import com.kemar.olam.delivery_management.model.request.AddLogReq
import com.kemar.olam.delivery_management.model.response.AddHeaderForDeliveryRes
import com.kemar.olam.delivery_management.model.response.LogsUserHistoryRes
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.loading_wagons.adapter.MultiLogsListAdapter
import com.kemar.olam.loading_wagons.model.responce.GetLogDataByBarcodeRes
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.retrofit.ApiClientMultiPart
import com.kemar.olam.utility.*
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.dialog_layouut_add_transporter_n_vehicle.view.*
import kotlinx.android.synthetic.main.dialog_layouut_add_transporter_n_vehicle.view.ivLogCancel
import kotlinx.android.synthetic.main.dialog_multi_log_no_layout.view.*
import kotlinx.android.synthetic.main.dialog_search_by_log_no_layout.view.*
import kotlinx.android.synthetic.main.dialog_search_by_log_no_layout.view.edt_log
import kotlinx.android.synthetic.main.dialog_search_by_log_no_layout.view.edt_log2
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.ivBOAdd
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.ivHeaderEdit
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.linFooter
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.rvLogListing
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.swipeLogListing
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.tvNoDataFound
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.txtBO
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.txtConfirmForDelivery
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.txtCustomerName
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.txtForestWagonNo
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.txtTotalLogs
import kotlinx.android.synthetic.main.fragment_delivery_log_details_new.view.*
import kotlinx.android.synthetic.main.layout_digital_signature.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator

class DeliveryLogDetailsNewFragment : Fragment() ,View.OnClickListener, DialogFragment.GetDialogListener {
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
    var originName = ""
    var bodereuHeaderId = 0
    var forestID = 0
    var transporterID = 0
    var vehicleID =  0
    //var suplierID =""
    var customerID  :Int? = 0
    var deliveryId:Int?=0
    var headerModel  : AddHeaderForDeliveryRes =  AddHeaderForDeliveryRes()
    var LogsListingResponce = GetBodereuLogByIdRes()
    var todaysHistoryModel  : LogsUserHistoryRes.UserHist =  LogsUserHistoryRes.UserHist()
    var transporterVehicleDialog: AlertDialog? = null

    lateinit var alertView : View
    //Scan feature
    var RESULT_CODE = 103;
    val LOG_TAG = "DataCapture1"
    private val bRequestSendResult = false
    var addLogDialog: AlertDialog? = null

    //sign functionality
    var SignDialog: AlertDialog? = null
    lateinit var represemtativeBitmap: Bitmap
    lateinit var customerBitmap: Bitmap

    //pdf saved values
    var countImgeUpload = 0
    var firstImagePath = ""
    var secodnImagepath = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_delivery_log_details_new, container, false)
        initViews()
        CreateProfile()
        setDecoderValues()
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
            //callingDeliveryMasterAPI()
            getDeliveryBodereuLogsByDeliveryID(deliveryId.toString())
        }

        //when comming from headerScreen & User History Screen
        //suplierID == forest
        //
        if(commigFrom.equals( Constants.header, ignoreCase = true)) {
            var headerDataModel: AddHeaderForDeliveryRes? =
                    arguments?.getSerializable(Constants.badereuModel) as AddHeaderForDeliveryRes
            if (headerDataModel != null) {
                headerModel =  headerDataModel
            }
            //bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
            customerID = headerDataModel?.getCustomerId()
            // suplierID = headerDataModel?.getSupplierId().toString()
            deliveryId= headerDataModel?.getDeliveryId()
            // headerDataModel?.modeOfTransport?.let { setupTransportMode(it,mView.context) }

            //need to add bordreu no
            /*  if (headerDataModel?.bordereauNo?.toString().isNullOrEmpty()) {
                  mView.txtBO_NO.text = headerDataModel?.eBordereauNo?.toString().toString()
              } else {
                  mView.txtBO_NO.text = headerDataModel?.bordereauNo?.toString().toString()

              }*/
            mView.txtCustomerName.text = headerDataModel?.getCustomerName()?.toString()
            mView.txtForestWagonNo.text = headerDataModel?.gettruckNo()?.toString()
            mView.txtBO.text = headerDataModel?.getDeliveryNumber()?.toString()

            // callingDeliveryMasterAPI()
            getDeliveryBodereuLogsByDeliveryID(deliveryId.toString())
        }else{
            val headerDataModel: LogsUserHistoryRes.UserHist? =
                    arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.UserHist
            if (headerDataModel != null) {
                todaysHistoryModel =  headerDataModel
            }
            // suplierID = headerDataModel?.supplierId.toString()
            deliveryId = headerDataModel?.deliveryId
            //headerDataModel?.transportMode?.let { setupTransportMode(it,mView.context) }

            customerID = headerDataModel?.customerId

            /*  if(headerDataModel?.eBordereauNo!=null || headerDataModel?.bordereauNo!=null){
                  mView.lin_Bordero_No?.visibility =  View.VISIBLE
                  if (headerDataModel?.bordereauNo?.isNullOrEmpty()!!) {
                      mView.txtBO_NO.text = headerDataModel?.eBordereauNo
                  } else {
                      mView.txtBO_NO.text = headerDataModel?.bordereauNo

                  }
              }else{
                  mView.lin_Bordero_No?.visibility =  View.GONE
              }*/

            mView.txtCustomerName.text = headerDataModel?.customerShortName?.toString()
            mView.txtForestWagonNo.text = headerDataModel?.truckName?.toString()
            mView.txtBO.text = headerDataModel?.deliveryNumber?.toString()

            //  callingDeliveryMasterAPI()
            getDeliveryBodereuLogsByDeliveryID(deliveryId.toString())
        }

    }


    fun setupClickListner() {
        mView.ivBOAdd.setOnClickListener(this)
        mView.ivHeaderEdit.setOnClickListener(this)
        mView.txtConfirmForDelivery.setOnClickListener(this)
        mView.txtDScan.setOnClickListener(this)

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
                //callingConfirmForDeliveryAPI()
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
            modelData.getDetailId()?.toInt()?.let { callingRemoveLogBordereauAForDelivery(it) }
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
                    mUtils.printPDF(mView.context,finalDestination)

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

    inner class  SaveBitmapToInternalStorageAsyncTask internal constructor():
            AsyncTask<String?, String?, File?>() {

        override fun onPreExecute() {
            super.onPreExecute()

        }

        override fun onPostExecute(file: File?) {
            super.onPostExecute(file)
            if (file != null) {
                try {
                    printPDF(file.path)
                } catch (t: Throwable) {
                }
            }
        }



        override fun doInBackground(vararg p0: String?): File? {
            var file: File? = null
            try {

                val bmScreenShot = p0[0]
                if (bmScreenShot != null) {
                    file =   mUtils.writeDataIntoFileAndSavePDF(mView,"Invoice",bmScreenShot)
                }

                /*  val bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                  file = bitmapImage?.let { saveBitmapIntoStorageNew(it) }*/
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return file
        }


    }

    private fun callingConfirmForDeliveryAPI(represetativeSign:String,cuustomerSign:String) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request =  generateConfirmForBillingRequest(represetativeSign,cuustomerSign)

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
                                        countImgeUpload = 0
                                        if(!response.body().getPdfFilePath().isNullOrEmpty()){
                                            response.body().getPdfFilePath()?.let {
                                                /* downloadPDFFromURLNew(
                                                     it*/
                                                var base64Image = ""
                                                if(response.body().getPdfFilePath()?.length!! > 100) {
                                                    if(it.contains("data:application/pdf;base64")) {
                                                        base64Image = response.body().getPdfFilePath()!!.split(",")[1]
                                                    }else{
                                                        base64Image = response.body().getPdfFilePath()!!
                                                    }
                                                    SaveBitmapToInternalStorageAsyncTask().execute(
                                                            base64Image
                                                    )
                                                }

                                            }
                                        }
                                        SignDialog?.dismiss()

                                        var fragment  = DeliveryUserHistoryFragment()
                                        val bundle = Bundle()
                                        bundle.putString(
                                                Constants.comming_from,
                                                Constants.Bc_main_screen)
                                        fragment.arguments = bundle
                                        (activity as HomeActivity).replaceFragment(fragment,true)
                                    }
                                    else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        countImgeUpload = 0
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



    fun generateConfirmForBillingRequest(cuustomerSign:String,represetativeSign:String): AddBoereuLogListingReq {
        var request: AddBoereuLogListingReq = AddBoereuLogListingReq()
        try{
            for (listdata in logsLiting) {
                listdata.setRejectionStatus("N")
                listdata.setComments("")
            }
            request.setUserID(SharedPref.getUserId(Constants.user_id))
            request.setBordereauHeaderId(bodereuHeaderId)
            request.setTimezoneId("Asia/Kolkata")
            request.setCustomerSignBase(cuustomerSign)
            request.setRepresentativeSignBase(represetativeSign)
            request.setDeliveryId(deliveryId)
            /// request.setInspectionNumber(LogsListingResponce.getInspectionNumber())
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

    fun acessRuntimPermissionForCamera() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                var intent = Intent(mView.context, BarcodeScanActivity::class.java)
                startActivityForResult(intent,RESULT_CODE)
            }

            override fun onPermissionDenied(deniedPermissions: java.util.ArrayList<String>) {

            }
        }
        TedPermission.with(view?.context)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getString(R.string.Please_give_permission_for_app_functionality))
                .setDeniedMessage(
                        getString(R.string.If_you_reject_permission_you_can_not_use_this_service) + "\n\n" + getString(
                                R.string.Please_turn_on_permissions_at
                        )
                )
                .setGotoSettingButtonText("setting")
                .setPermissions(
                        Manifest.permission.CAMERA
                )
                .check()
    }

    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivBOAdd -> {
                //user should be add only 20 logs
                if(logsLiting.size !=20) {
                    showSearcByLogDialog(0)
                }
            }

            R.id.txtConfirmForDelivery ->{
                //  showTransporterNVehicleDialog()
                if(logsLiting.size !=0) {
                    showDialogForDigitalSign()
                }
            }

            R.id.txtDScan -> {
                acessRuntimPermissionForCamera()
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

    private fun showMultiLogDialog(responce: GetLogDataByBarcodeRes) {
        val inflater =
                mView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val alertLayout: View = inflater.inflate(R.layout.dialog_multi_log_no_layout, null)
        alertView = alertLayout
        val alert: android.app.AlertDialog.Builder =
                android.app.AlertDialog.Builder(mView.context, R.style.CustomDialog)
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout)
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false)

        alertLayout.ivCancel.setOnClickListener{
            addLogDialog?.dismiss()
        }

        alertLayout.headLabel.setText("Log Number"+ responce.getLogDetails()!!.get(0).getLogNo())

        var multiLogsListAdapter= MultiLogsListAdapter(requireContext(), responce.getLogDetails()!!)

        multiLogsListAdapter.onItemClick = { modelData ->

            if (checkLogAACNLogNoAlreadyExits(
                            modelData.getAACName(),
                            modelData.getLogNo().toString()
                    )
            ) {
                if (logsLiting?.size != 20) {
                    modelData.getDetailId()?.toInt()
                            ?.let {
                                callingAddLogBordereauAForDelivery(
                                        it
                                )
                            }
                   /* logsLiting.add(modelData)
                    adapter.notifyDataSetChanged()
                    setTotalNoOfLogs(logsLiting?.size)
                    addLogDialog?.dismiss()*/
                }
            } else {
                addLogDialog?.dismiss()
                mUtils.showAlert(
                        activity,
                        resources.getString(R.string.duplicate_log_found)
                )
            }
        }
        mLinearLayoutManager =
                LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        alertLayout.logslistrecy.layoutManager = mLinearLayoutManager
        alertLayout.logslistrecy.adapter=multiLogsListAdapter
        addLogDialog = alert.create()
        addLogDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addLogDialog?.show()
    }

    private fun showSearcByLogDialog(position: Int) {
        val inflater =
                mView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val alertLayout: View = inflater.inflate(R.layout.dialog_search_by_log_forest, null)
        alertView = alertLayout
        val alert: android.app.AlertDialog.Builder =
                android.app.AlertDialog.Builder(mView.context, R.style.CustomDialog)
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout)
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false)


        alertLayout?.linSearchByLogNo.setOnClickListener {
            if(isValidateLogsNumber(alertLayout)) {
                addLogDialog?.dismiss()
                getLogDataByBarlogNumber(alertLayout.edt_log.text.toString()+"/"+alertLayout.edt_log2.text.toString())
            }
        }


        alertLayout.ivLogCancel.setOnClickListener{
            addLogDialog?.dismiss()
        }


        addLogDialog = alert.create()
        addLogDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addLogDialog?.show()
    }


    fun isValidateLogsNumber(alertLayout: View): Boolean {
        if (alertLayout.edt_log.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, resources.getString(R.string.Please_enter_log_no))
            return false
        }
        else if (alertLayout.edt_log2.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, resources.getString(R.string.please_enter_valid_log_no))
            alertLayout.edt_log2.requestFocus()
            return false
        }
        return  true
    }

    //digital sign data
    private fun showDialogForDigitalSign() {
        val inflater =
                mView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val alertLayout: View = inflater.inflate(R.layout.layout_digital_signature, null)
        val alertView = alertLayout
        val alert: AlertDialog.Builder =
                AlertDialog.Builder(mView.context, R.style.CustomDialog)
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout)
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false)

        alertLayout.ivRepresentiveCancel?.setOnClickListener {
            alertLayout.representativeSign?.clear()
        }

        alertLayout.ivCustomerCancel?.setOnClickListener {
            alertLayout.customerSign?.clear()
        }

        alertLayout.txtReject?.setOnClickListener {
            SignDialog?.dismiss()
        }
        alertLayout.txtConfirm?.setOnClickListener {
            if (isValidateSign(alertView)) {
                acessRuntimPermissionForStorage(alertView)
                //SignDialog?.dismiss()
            }

        }

        SignDialog = alert.create()
        SignDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        SignDialog?.show()


    }

    fun isValidateSign(alertLayout: View): Boolean {
        if (alertLayout.representativeSign.isEmpty) {
            mUtils.showAlert(
                    activity,
                    mView.context.resources.getString(R.string.representative_sign_error)
            )
            return false
        } /*else if (alertLayout.customerSign.isEmpty) {
            mUtils.showAlert(
                activity,
                mView.context.resources.getString(R.string.customer_sign_error)
            )
            return false
        }*/
        return true
    }

    fun getLogDataByBarlogNumberRequest(logNumber: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setCustomerId(customerID.toString())
        request.setLogNo(logNumber)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    private fun getLogDataByBarlogNumber(logNumber:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request  =   getLogDataByBarlogNumberRequest(logNumber)
                val call_api: Call<GetLogDataByBarcodeRes> =
                        apiInterface.getLogDataByLogNoDelivery(request)
                call_api.enqueue(object :
                        Callback<GetLogDataByBarcodeRes> {
                    override fun onResponse(
                            call: Call<GetLogDataByBarcodeRes>,
                            response: Response<GetLogDataByBarcodeRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                val responce: GetLogDataByBarcodeRes =
                                        response.body()!!
                                if (responce != null) {

                                    if(responce?.getSeverity()==200) {
                                        if(responce.getLogDetails()!!.size==1) {
                                            if (checkLogAACNLogNoAlreadyExits(
                                                            responce.getLogDetails()!!.get(0)?.getAACName(),
                                                            responce.getLogDetails()!!.get(0)?.getLogNo().toString()
                                                    )
                                            ) {
                                                if(logsLiting?.size!=20) {
                                                    /*  responce.getLogDetail()?.setDetailId("")
                                                      responce.getLogDetail()?.let { logsLiting?.add(it) }
                                                      adapter.notifyDataSetChanged()
                                                      setTotalNoOfLogs(logsLiting?.size)*/
                                                    responce.getLogDetails()?.get(0)!!.getDetailId()?.toInt()?.let {
                                                        callingAddLogBordereauAForDelivery(
                                                                it
                                                        )
                                                    }
                                                }
                                            } else {
                                                mUtils.showAlert(
                                                        activity,
                                                        resources.getString(R.string.duplicate_log_found)
                                                )
                                            }
                                        }else{
                                            showMultiLogDialog(responce)
                                        }
                                    }
                                    else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showAlert(
                                                activity,
                                                responce?.getMessage())
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
                            call: Call<GetLogDataByBarcodeRes>,
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


    inner class  SaveBitmapAsyncTask internal constructor():
            AsyncTask<Bitmap?, String?, File?>() {

        override fun onPreExecute() {
            super.onPreExecute()
            mUtils.showProgressDialog(mView.context)
        }

        override fun onPostExecute(file: File?) {
            super.onPostExecute(file)
            mUtils.dismissProgressDialog()
            if (file != null) {
                try {
                    callingUploadImageApi(file)
                } catch (t: Throwable) {
                }
            }
        }



        override fun doInBackground(vararg p0: Bitmap?): File? {
            var file: File? = null
            val compressFile: File? = null
            try {
                val bmScreenShot = p0[0]
                file = bmScreenShot?.let { saveBitmapIntoStorageNew(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return file
        }


    }


    private fun callingUploadImageApi(file:File) {
        val photo: MultipartBody.Part
        val filename: RequestBody
        val photoContent: RequestBody
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                photoContent =
                        RequestBody.create(MediaType.parse("multipart/form-data"), file)
                photo = MultipartBody.Part.createFormData(
                        "files",
                        file.name.trim { it <= ' ' },
                        photoContent
                )
                filename = RequestBody.create(
                        MediaType.parse("text/plain"),
                        file.name.trim { it <= ' ' }
                )
                val apiInterface: ApiEndPoints = ApiClientMultiPart.client.create(ApiEndPoints::class.java)
                val call: Call<AddBodereuLogListingRes> =
                        apiInterface.uploadSign(filename,photo)
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
                                        countImgeUpload ++
                                        if(countImgeUpload==1){
                                            firstImagePath = response.body().getPdfFilePath().toString()
                                            val representativeSigbBitmap = represemtativeBitmap
                                            SaveBitmapAsyncTask().execute(representativeSigbBitmap)
                                        }
                                        if(countImgeUpload==2){
                                            secodnImagepath = response.body().getPdfFilePath().toString()
                                            callingConfirmForDeliveryAPI(firstImagePath,secodnImagepath)
                                        }
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

    private fun saveBitmapIntoStorageNew(bmp: Bitmap): File? {
        var f: File? = null
        try {
            val bytes = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 80, bytes)
            val d = Date()
            val s =
                    DateFormat.format("MM-dd-yy hh-mm-ss", d.time)

            val cw =  ContextWrapper(mView.context)
            val directory = cw.getDir("OLAM", Context.MODE_PRIVATE)
            // Create imageDir
            f = File(directory, "SIGN" + s +".png")

            /* f = File(
                 Environment.getExternalStorageDirectory()
                     .toString() + File.separator + "IMAGE" + s + ".png"
             )*/

            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return f
    }

    /* private fun saveBitmapIntoStorage(bmp: Bitmap): File? {
         var f: File? = null
         try {
             val bytes = ByteArrayOutputStream()
             bmp.compress(Bitmap.CompressFormat.PNG, 80, bytes)
             val d = Date()
             val s =
                 DateFormat.format("MM-dd-yy hh-mm-ss", d.time)
             f = File(
                 Environment.getExternalStorageDirectory()
                     .toString() + File.separator + "IMAGE" + s + ".png"
             )
             f.createNewFile()
             val fo = FileOutputStream(f)
             fo.write(bytes.toByteArray())
             fo.close()
         } catch (e: Exception) {
             e.printStackTrace()
         }
         return f
     }*/


    fun acessRuntimPermissionForStorage(alertLayout: View) {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                //val representativeSign  = getRepresentativeBase64String(alertLayout)
                customerBitmap = alertLayout.customerSign.getTransparentSignatureBitmap(false)
                represemtativeBitmap =
                        alertLayout.representativeSign.getTransparentSignatureBitmap(false)
                SaveBitmapAsyncTask().execute(customerBitmap)
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {

            }
        }
        TedPermission.with(view?.context)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getString(R.string.Please_give_permission_for_app_functionality))
                .setDeniedMessage(
                        getString(R.string.If_you_reject_permission_you_can_not_use_this_service) + "\n\n" + getString(
                                R.string.Please_turn_on_permissions_at
                        )
                )
                .setGotoSettingButtonText("setting")
                .setPermissions(
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_NETWORK_STATE
                        /*  Manifest.permission.WRITE_EXTERNAL_STORAGE,
                          Manifest.permission.READ_EXTERNAL_STORAGE*/
                )
                .check()
    }


    //scanner Data
    fun ToggleSoftScanTrigger() {
        sendDataWedgeIntentWithExtra(
                Constants.ACTION_DATAWEDGE,
                Constants.EXTRA_SOFT_SCAN_TRIGGER,
                "TOGGLE_SCANNING"
        )
    }

    fun setDecoderValues(){

        /* val checkCode128 = findViewById(R.id.chkCode128) as CheckBox*/
        val Code128Value: String = "true"/*setDecoder(checkCode128)*/

        /*   val checkCode39 = findViewById(R.id.chkCode39) as CheckBox*/
        val Code39Value: String = "true"/*setDecoder(checkCode39)*/

        /* val checkEAN13 = findViewById(R.id.chkEAN13) as CheckBox*/
        val EAN13Value: String ="true" /* setDecoder(checkEAN13)*/

        /*val checkUPCA = findViewById(R.id.chkUPCA) as CheckBox*/
        val UPCAValue: String = "true"/*setDecoder(checkUPCA)*/
        // Main bundle properties
        // Main bundle properties
        val profileConfig = Bundle()
        profileConfig.putString("PROFILE_NAME", Constants.EXTRA_PROFILENAME)
        profileConfig.putString("PROFILE_ENABLED", "true")
        profileConfig.putString("CONFIG_MODE", "UPDATE") // Update specified settings in profile


        // PLUGIN_CONFIG bundle properties
        // PLUGIN_CONFIG bundle properties
        val barcodeConfig = Bundle()
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
        barcodeConfig.putString("RESET_CONFIG", "true")

        // PARAM_LIST bundle properties
        // PARAM_LIST bundle properties
        val barcodeProps = Bundle()
        barcodeProps.putString("scanner_selection", "auto")
        barcodeProps.putString("scanner_input_enabled", "true")
        barcodeProps.putString("decoder_code128", Code128Value)
        barcodeProps.putString("decoder_code39", Code39Value)
        barcodeProps.putString("decoder_ean13", EAN13Value)
        barcodeProps.putString("decoder_upca", UPCAValue)

        // Bundle "barcodeProps" within bundle "barcodeConfig"
        // Bundle "barcodeProps" within bundle "barcodeConfig"
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
        // Place "barcodeConfig" bundle within main "profileConfig" bundle
        // Place "barcodeConfig" bundle within main "profileConfig" bundle
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)

        // Create APP_LIST bundle to associate app with profile
        // Create APP_LIST bundle to associate app with profile
        val appConfig = Bundle()
        appConfig.putString("PACKAGE_NAME", mView.context.packageName)
        appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))
        profileConfig.putParcelableArray("APP_LIST", arrayOf(appConfig))
        sendDataWedgeIntentWithExtra(
                Constants.ACTION_DATAWEDGE,
                Constants.EXTRA_SET_CONFIG,
                profileConfig
        )
        /*Toast.makeText(
            applicationContext,
            "In profile " + Constants.EXTRA_PROFILENAME + " the selected decoders are being set: \nCode128=" + Code128Value + "\nCode39="
                    + Code39Value + "\nEAN13=" + EAN13Value + "\nUPCA=" + UPCAValue,
            Toast.LENGTH_LONG
        ).show()*/

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
    fun getLogDataByBarCodeRequest(barcodeNumber: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setCustomerId(customerID.toString())
        request.setBarcodeNumber(barcodeNumber)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun getLogDataByBarCode(barcodeNumber:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = getLogDataByBarCodeRequest(barcodeNumber)
                val call_api: Call<GetLogDataByBarcodeRes> =
                        apiInterface.getLogDataByBarCodeDeliver(request)
                call_api.enqueue(object :
                        Callback<GetLogDataByBarcodeRes> {
                    override fun onResponse(
                            call: Call<GetLogDataByBarcodeRes>,
                            response: Response<GetLogDataByBarcodeRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                val responce: GetLogDataByBarcodeRes =
                                        response.body()!!
                                if (responce != null) {

                                    if (responce?.getSeverity() == 200) {
                                        if (checkLogAACNLogNoAlreadyExits(
                                                responce.getLogDetail()?.getAACName(),
                                                responce.getLogDetail()?.getLogNo().toString()
                                            )
                                        ) {
                                            if (logsLiting?.size != 20) {
                                                /*  responce.getLogDetail()?.setDetailId("")
                                            responce.getLogDetail()?.let { logsLiting?.add(it) }
                                            adapter.notifyDataSetChanged()
                                            setTotalNoOfLogs(logsLiting?.size)*/
                                                responce.getLogDetail()?.getDetailId()?.toInt()
                                                    ?.let {
                                                        callingAddLogBordereauAForDelivery(
                                                            it
                                                        )
                                                    }
                                            }
                                        } else {
                                            mUtils.showAlert(
                                                activity,
                                                resources.getString(R.string.duplicate_log_found)
                                            )
                                        }
                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showAlert(
                                                activity,
                                                responce?.getMessage()
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
                            call: Call<GetLogDataByBarcodeRes>,
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

    private fun sendDataWedgeIntentWithExtra(
            action: String,
            extraKey: String,
            extraValue: String
    ) {
        val dwIntent = Intent()
        dwIntent.action = action
        dwIntent.putExtra(extraKey, extraValue)
        if (bRequestSendResult) dwIntent.putExtra(
                Constants.EXTRA_SEND_RESULT,
                "true"
        )
        mView.context.sendBroadcast(dwIntent)
    }
    private fun sendDataWedgeIntentWithExtra(
            action: String,
            extraKey: String,
            extras: Bundle
    ) {
        val dwIntent = Intent()
        dwIntent.action = action
        dwIntent.putExtra(extraKey, extras)
        if (bRequestSendResult) dwIntent.putExtra(
                Constants.EXTRA_SEND_RESULT,
                "true"
        )
        mView.context.sendBroadcast(dwIntent)
    }

    // Unregister scanner status notification
    fun unRegisterScannerStatus() {
        Log.d(LOG_TAG, "unRegisterScannerStatus()")
        val b = Bundle()
        b.putString(Constants.EXTRA_KEY_APPLICATION_NAME,  mView.context.packageName)
        b.putString(
                Constants.EXTRA_KEY_NOTIFICATION_TYPE,
                Constants.EXTRA_KEY_VALUE_SCANNER_STATUS
        )
        val i = Intent()
        i.action = ContactsContract.Intents.Insert.ACTION
        i.putExtra(Constants.EXTRA_UNREGISTER_NOTIFICATION, b)
        mView.context.sendBroadcast(i)
    }

    // Create filter for the broadcast intent
    private fun registerReceivers() {
        Log.d(LOG_TAG, "registerReceivers()")
        val filter = IntentFilter()
        filter.addAction(Constants.ACTION_RESULT_NOTIFICATION) // for notification result
        filter.addAction(Constants.ACTION_RESULT) // for error code result
        filter.addCategory(Intent.CATEGORY_DEFAULT) // needed to get version info
        // register to received broadcasts via DataWedge scanning
        filter.addAction(resources.getString(R.string.activity_intent_filter_action))
        filter.addAction(resources.getString(R.string.activity_action_from_service))
        filter.addAction(resources.getString(R.string.activity_action_from_nautiz))
        mView.context.registerReceiver(myBroadcastReceiver, filter)
    }

    override fun onResume() {
        super.onResume()
        registerReceivers()
    }

    // Create profile from UI onClick() event
    fun CreateProfile() {
        val profileName: String = Constants.EXTRA_PROFILENAME
        // Send DataWedge intent with extra to create profile
// Use CREATE_PROFILE: http://techdocs.zebra.com/datawedge/latest/guide/api/createprofile/
        sendDataWedgeIntentWithExtra(
                Constants.ACTION_DATAWEDGE,
                Constants.EXTRA_CREATE_PROFILE,
                profileName
        )
        // Configure created profile to apply to this app
        val profileConfig = Bundle()
        profileConfig.putString("PROFILE_NAME", Constants.EXTRA_PROFILENAME)
        profileConfig.putString("PROFILE_ENABLED", "true")
        profileConfig.putString(
                "CONFIG_MODE",
                "CREATE_IF_NOT_EXIST"
        ) // Create profile if it does not exist
        // Configure barcode input plugin
        val barcodeConfig = Bundle()
        barcodeConfig.putString("PLUGIN_NAME", "BARCODE")
        barcodeConfig.putString("RESET_CONFIG", "true") //  This is the default
        val barcodeProps = Bundle()
        barcodeConfig.putBundle("PARAM_LIST", barcodeProps)
        profileConfig.putBundle("PLUGIN_CONFIG", barcodeConfig)
        // Associate profile with this app
        val appConfig = Bundle()
        appConfig.putString("PACKAGE_NAME", mView.context.packageName)
        appConfig.putStringArray("ACTIVITY_LIST", arrayOf("*"))
        profileConfig.putParcelableArray("APP_LIST", arrayOf(appConfig))
        profileConfig.remove("PLUGIN_CONFIG")
        // Apply configs
// Use SET_CONFIG: http://techdocs.zebra.com/datawedge/latest/guide/api/setconfig/
        sendDataWedgeIntentWithExtra(
                Constants.ACTION_DATAWEDGE,
                Constants.EXTRA_SET_CONFIG,
                profileConfig
        )
        // Configure intent output for captured data to be sent to this app
        val intentConfig = Bundle()
        intentConfig.putString("PLUGIN_NAME", "INTENT")
        intentConfig.putString("RESET_CONFIG", "true")
        val intentProps = Bundle()
        intentProps.putString("intent_output_enabled", "true")
        intentProps.putString("intent_action", "com.zebra.datacapture1.ACTION")
        intentProps.putString("intent_delivery", "2")
        intentConfig.putBundle("PARAM_LIST", intentProps)
        profileConfig.putBundle("PLUGIN_CONFIG", intentConfig)
        sendDataWedgeIntentWithExtra(
                Constants.ACTION_DATAWEDGE,
                Constants.EXTRA_SET_CONFIG,
                profileConfig
        )
        /* Toast.makeText(
             mView.context,
             "Created profile.  Check DataWedge app UI.",
             Toast.LENGTH_LONG
         ).show()*/
    }

    override fun onPause() {
        super.onPause()
        mView.context.unregisterReceiver(myBroadcastReceiver)
        unRegisterScannerStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val scannedValue  =  data?.getStringExtra(Constants.scan_code).toString()
                // mView.txtResult.text  =  "Scan Value "+data?.getStringExtra(Constants.scan_code).toString()
                getLogDataByBarCode(scannedValue)
            }
        }

    }


    private fun displayScanResult(
            initiatingIntent: Intent,
            howDataReceived: String
    ) { // store decoded data
        val decodedData =
            if(howDataReceived == "zebra")
                initiatingIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_data))
            else
                initiatingIntent.getStringExtra("value")
        // store decoder type
        val decodedLabelType =
                initiatingIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_label_type))
        /*val lblScanData = findViewById(R.id.lblScanData) as TextView
        val lblScanLabelType = findViewById(R.id.lblScanDecoder) as TextView
        lblScanData.text = decodedData
        lblScanLabelType.text = decodedLabelType*/
        getLogDataByBarCode(decodedData)
        //mView.txtResult.text =  "Scan Value "+decodedData
    }

    private val myBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            val b = intent.extras
            Log.d(LOG_TAG, "DataWedge Action:$action")
            // Get DataWedge version info
            if (intent.hasExtra(Constants.EXTRA_RESULT_GET_VERSION_INFO)) {
                val versionInfo =
                        intent.getBundleExtra(Constants.EXTRA_RESULT_GET_VERSION_INFO)
                val DWVersion = versionInfo.getString("DATAWEDGE")
                /* val txtDWVersion = findViewById(R.id.txtGetDWVersion) as TextView
                 txtDWVersion.text = DWVersion
                 Log.i(LOG_TAG, "DataWedge Version: $DWVersion")*/
            }
            if (action == resources.getString(R.string.activity_intent_filter_action)) { //  Received a barcode scan
                try {
                    displayScanResult(intent, "zebra")
                } catch (e: Exception) { //  Catch error if the UI does not exist when we receive the broadcast...
                }
            } else if(action == resources.getString(R.string.activity_action_from_nautiz)){
                try {
                    displayScanResult(intent, "nautiz")
                } catch (e: Exception) { //  Catch error if the UI does not exist when we receive the broadcast...
                }
            } else if (action == Constants.ACTION_RESULT) { // Register to receive the result code
                if (intent.hasExtra(Constants.EXTRA_RESULT) && intent.hasExtra(
                                Constants.EXTRA_COMMAND
                        )
                ) {
                    val command =
                            intent.getStringExtra(Constants.EXTRA_COMMAND)
                    val result =
                            intent.getStringExtra(Constants.EXTRA_RESULT)
                    var info = ""
                    if (intent.hasExtra(Constants.EXTRA_RESULT_INFO)) {
                        val result_info =
                                intent.getBundleExtra(Constants.EXTRA_RESULT_INFO)
                        val keys = result_info.keySet()
                        for (key in keys) {
                            val `object` = result_info[key]
                            if (`object` is String) {
                                info += "$key: $`object`\n"
                            } else if (`object` is Array<*>) {
                                for (code in `object`) {
                                    info += "$key: $code\n"
                                }
                            }
                        }
                        Log.d(
                                LOG_TAG, "Command: " + command + "\n" +
                                "Result: " + result + "\n" +
                                "Result Info: " + info + "\n"
                        )
                        Toast.makeText(
                                mView.context,
                                "Error Resulted. Command:$command\nResult: $result\nResult Info: $info",
                                Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else if (action == Constants.ACTION_RESULT_NOTIFICATION) {
                if (intent.hasExtra(Constants.EXTRA_RESULT_NOTIFICATION)) {
                    val extras =
                            intent.getBundleExtra(Constants.EXTRA_RESULT_NOTIFICATION)
                    val notificationType =
                            extras.getString(Constants.EXTRA_RESULT_NOTIFICATION_TYPE)
                    if (notificationType != null) {
                        when (notificationType) {
                            Constants.EXTRA_KEY_VALUE_SCANNER_STATUS -> {
                                // Change in scanner status occurred
                                val displayScannerStatusText =
                                        extras.getString(Constants.EXTRA_KEY_VALUE_NOTIFICATION_STATUS) +
                                                ", profile: " + extras.getString(Constants.EXTRA_KEY_VALUE_NOTIFICATION_PROFILE_NAME)
                                //Toast.makeText(getApplicationContext(), displayScannerStatusText, Toast.LENGTH_SHORT).show();
                                /*   val lblScannerStatus =
                                       findViewById(R.id.lblScannerStatus) as TextView
                                   lblScannerStatus.text = displayScannerStatusText*/
                                Log.i(
                                        LOG_TAG,
                                        "Scanner status: $displayScannerStatusText"
                                )
                            }
                            Constants.EXTRA_KEY_VALUE_PROFILE_SWITCH -> {
                            }
                            Constants.EXTRA_KEY_VALUE_CONFIGURATION_UPDATE -> {
                            }
                        }
                    }
                }
            }
        }
    }


    fun generateAddNRemoveBodereuRequest(logDetaileId:Int): AddLogReq {


        var request: AddLogReq = AddLogReq()
        request.setBordereauDetailId(logDetaileId)
        request.setDeliveryId(deliveryId!!)
        val json = Gson().toJson(request)
        var test = json

        return request

    }




    /*     add & Remove Log From Header      */
    private fun callingAddLogBordereauAForDelivery(logDetaileId: Int) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateAddNRemoveBodereuRequest(logDetaileId)
                val call: Call<AddHeaderForDeliveryRes> =
                        apiInterface.addLogToDelivery(request)
                call.enqueue(object :
                        Callback<AddHeaderForDeliveryRes> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                            call: Call<AddHeaderForDeliveryRes>,
                            response: Response<AddHeaderForDeliveryRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        if(addLogDialog!=null)
                        addLogDialog?.dismiss()


                        try {
                            if (response.code() == 200) {
                                if (response != null) {
                                    if (response.body().getSeverity() == 200) {
                                        mUtils.showToast(activity, response.body().getMessage())
                                        getDeliveryBodereuLogsByDeliveryID(deliveryId.toString())
                                    }  else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }

                            }  else if (response.code() == 306) {
                                mUtils.alertDialgSession(mView.context, activity)
                            } else {
                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                            call: Call<AddHeaderForDeliveryRes>,
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


    private fun callingRemoveLogBordereauAForDelivery(logDetaileId: Int) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateAddNRemoveBodereuRequest(logDetaileId)
                val call: Call<AddHeaderForDeliveryRes> =
                        apiInterface.removeLogFromDelivery(request)
                call.enqueue(object :
                        Callback<AddHeaderForDeliveryRes> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                            call: Call<AddHeaderForDeliveryRes>,
                            response: Response<AddHeaderForDeliveryRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response != null) {
                                    if (response.body().getSeverity() == 200) {
                                        mUtils.showToast(activity, response.body().getMessage())
                                        getDeliveryBodereuLogsByDeliveryID(deliveryId.toString())
                                    }  else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }

                            } else if (response.code() == 306) {
                                mUtils.alertDialgSession(mView.context, activity)
                            }  else {
                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                            call: Call<AddHeaderForDeliveryRes>,
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



}
