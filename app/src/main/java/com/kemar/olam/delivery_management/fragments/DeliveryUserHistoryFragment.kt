package com.kemar.olam.delivery_management.fragments

import android.app.DownloadManager
import android.content.*
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.format.DateFormat
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.BoderueDeleteLogReq
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import com.kemar.olam.delivery_management.adapter.DeliveryHistoryAdapter
import com.kemar.olam.delivery_management.model.request.DeliveryHistoryReq
import com.kemar.olam.delivery_management.model.response.LogsUserHistoryRes
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_delivery_user_history.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DeliveryUserHistoryFragment : Fragment()  ,View.OnClickListener {
    var commigFrom:String?=""
    var strBordereuNo : String?=""
    var forestID : Int?=0
    var customerId : Int?=0
    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var rvLogsUserHistory: RecyclerView
    lateinit var adapter: DeliveryHistoryAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsTodaysHitoryLiting: ArrayList<LogsUserHistoryRes.UserHist?> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView  = inflater.inflate(R.layout.fragment_delivery_user_history, container, false)
        initViews()
        return mView;
    }
    fun initViews() {
        mUtils = Utility()
        rvLogsUserHistory = mView.findViewById(R.id.rvLogsUserHistory)
        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()
        commigFrom =
            arguments?.getString(Constants.comming_from)
        if(commigFrom?.equals(Constants.Bc_Header_screen,ignoreCase = true)!!) {
            var dataModel: DeliveryHistoryReq? =
                arguments?.getSerializable(Constants.badereuFilterModel) as DeliveryHistoryReq
            forestID =
                dataModel?.getSupplier()
            customerId =
                dataModel?.getCustomerId()
            strBordereuNo =
                dataModel?.getBordereauNo()

        }
        callingDeliveryUserHistory()

        mView.swipeUserHistory.setOnRefreshListener {
            mView.swipeUserHistory.isRefreshing = false
            callingDeliveryUserHistory()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as HomeActivity).ivHomeFilter.setOnClickListener(this)

    }
    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.drawer_delivery_management)
        (activity as HomeActivity).visibleFilter()
    }

    fun setupClickListner() {
        mView.ivAddBBordereu.setOnClickListener(this)
        mView.btnRetry.setOnClickListener(this)
        mView.txtDeliverdList.setOnClickListener(this)
        if(activity!=null) {
            (activity as HomeActivity)?.ivHomeFilter.setOnClickListener(this)
        }
    }

    fun callingDeliveryUserHistory() {
        getDeliveryUserHistory()
    }

    fun setupRecyclerViewNAdapter() {
        logsTodaysHitoryLiting.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvLogsUserHistory.layoutManager = mLinearLayoutManager
        adapter = DeliveryHistoryAdapter(mView.context, logsTodaysHitoryLiting,
            SharedPref.read(Constants.user_role).toString())
        mView.rvLogsUserHistory.adapter = adapter


        adapter.onHeaderClick = { modelData, position ->
            val myactivity = activity as HomeActivity
            val fragment =
                DeliveryLogDetailsNewFragment()
            val bundle = Bundle()
            bundle.putSerializable(
                Constants.badereuModel,
                modelData
            )
            bundle.putString(
                Constants.comming_from,
                mView.context.getString(R.string.user_history)
            )
            fragment.arguments = bundle
            myactivity?.replaceFragment(fragment, false)

        }

        adapter.onInTransitClick = { modelData, position ->

            val myactivity = activity as HomeActivity
            val fragment =
                DeliveryBordereuDetails()
            val bundle = Bundle()
            bundle.putSerializable(
                Constants.badereuModel,
                modelData
            )
            bundle.putString(
                Constants.comming_from,
                mView.context.getString(R.string.user_history)
            )
            fragment.arguments = bundle
            myactivity?.replaceFragment(fragment, false)

        }

        adapter.onReprintClick = { modelData, position ->
            if(!logsTodaysHitoryLiting?.get(position)?.pdfFilePath.isNullOrEmpty()) {
                logsTodaysHitoryLiting?.get(position)?.pdfFilePath?.let {
                    /*downloadPDFFromURL(
                        it
                    )*/
                    callingRePrintInVoiceAPI(position)
                }
            }else{
                mUtils.showAlert(
                    activity,
                    resources.getString(R.string.pdf_not_found)
                )
            }

        }

    }

    fun generateNoInspectionRequest(position: Int): BoderueDeleteLogReq {
        val request : BoderueDeleteLogReq =   BoderueDeleteLogReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
       // request.setBordereauHeaderId(logsTodaysHitoryLiting[position]?.bordereauHeaderId)
        return  request

    }

    fun printPDF(pdfPath: String) {
        val printManager =
            mView.context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        /*val printAdapter =
            PdfDocumentAdapter(this@PrintActivity, "/storage/emulated/0/Download/test.pdf")*/
        val printAdapter =
            PdfDocumentAdapter(mView.context, pdfPath)
        printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
    }


    fun downloadPDFFromURL(pdfPath: String) {
        if(pdfPath.contains(".pdf"))
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

/*

    private fun generateInvoiceRequest(position: Int): BoderueDeleteLogReq {

        var request : BoderueDeleteLogReq =   BoderueDeleteLogReq()
        request.setInspectionNumber(logsTodaysHitoryLiting?.get(position)?.inspectionNumber)

        return  request
    }

*/


    private fun generateUserHistoryRequest(): DeliveryHistoryReq {

        var isAdmin :Boolean=false
          when(SharedPref.read(Constants.user_role).toString()){
                     "SuperUserApp","Super User","admin"->{
                         isAdmin = true
                     }
                     else->{
                         isAdmin = false
                     }
                 }
        var request : DeliveryHistoryReq =   DeliveryHistoryReq()
        request.setBordereauNo(strBordereuNo)
        request.setSupplier(forestID)
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setUserLocationID(SharedPref.readInt(Constants.user_location_id))
        request.setCustomerId(customerId)
        request.setIsAdmin(isAdmin)
        return  request

    }


    private fun generateInvoiceRequest(position: Int): CommonRequest {

        var request : CommonRequest =   CommonRequest()
        request.setPdfFilePath(logsTodaysHitoryLiting?.get(position)?.pdfFilePath)
        request.setDeliveryId(logsTodaysHitoryLiting?.get(position)?.deliveryId.toString())

        return  request

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

    private fun callingRePrintInVoiceAPI(position: Int) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateInvoiceRequest(position)
                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.reprintDeliveryInoive(request)
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
                                        if(!response.body().getPdfFilePath().isNullOrEmpty()) {
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


    private fun getDeliveryUserHistory() {
        if (mUtils.checkInternetConnection(mView.context)) {
            showContentView()
            try {
                //var  isAdmin : Boolean = false
                mUtils.showProgressDialog(mView.context)
                val request  = generateUserHistoryRequest()
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val call_api: Call<LogsUserHistoryRes> =
                    apiInterface.getDeliveryHistory()
                call_api.enqueue(object :
                    Callback<LogsUserHistoryRes> {
                    override fun onResponse(
                        call: Call<LogsUserHistoryRes>,
                        response: Response<LogsUserHistoryRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                var logsUserHistoryRes=response.body()
                                if (logsUserHistoryRes.severity == 200) {
                                    val responce: LogsUserHistoryRes =
                                        response.body()!!
                                    if (responce != null) {
                                        logsTodaysHitoryLiting?.clear()
                                        responce.userHistList?.let {
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
                                }else if (response.body()?.severity == 306) {
                                    mUtils.alertDialgSession(mView.context, activity)
                                } else {
                                    mUtils.showToast(activity, response.body().message)
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
        mView.linApprovalList.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.VISIBLE
    }

    fun showNoNoDataView() {
        mView.linContent.visibility = View.GONE
        mView.linApprovalList.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.VISIBLE
    }

    fun showContentView() {
        mView.linContent.visibility = View.VISIBLE
        mView.linApprovalList.visibility = View.VISIBLE
        mView.relvNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
    }


    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivHomeFilter -> {
                if(activity!=null) {
                    var fragment = DeliveryFilterFragment()
                    (activity as HomeActivity).replaceFragment(fragment, false)
                }
            }


            R.id.ivAddBBordereu -> {
                var fragment = DeliveryHeaderFragment()
                val bundle = Bundle()
                bundle.putString(
                    Constants.action,
                    Constants.action_non_edit
                )
                fragment.arguments = bundle
                (activity as HomeActivity).replaceFragment(fragment, false)
            }
            R.id.txtDeliverdList -> {
                var fragment = DeliverdBordereuHistoryFragment()
                (activity as HomeActivity).replaceFragment(fragment, false)
            }

            R.id.btnRetry -> {
                getDeliveryUserHistory()
            }

        }
    }
}

