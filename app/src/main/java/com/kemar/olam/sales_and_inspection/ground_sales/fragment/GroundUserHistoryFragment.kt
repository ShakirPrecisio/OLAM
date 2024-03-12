package com.kemar.olam.sales_and_inspection.ground_sales.fragment
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
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.BoderueDeleteLogReq
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import com.kemar.olam.loading_wagons.fragment.LoadingWagonsLogsListingFragment
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.sales_and_inspection.ground_sales.adapter.GroundHistoryAdapter
import com.kemar.olam.sales_and_inspection.inspection.fragment.InnspectionHeaderFragment
import com.kemar.olam.sales_and_inspection.inspection.fragment.MainContainerFragment
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_ground_user_history.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class GroundUserHistoryFragment : Fragment(),View.OnClickListener {

    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var rvLogsUserHistory: RecyclerView
    lateinit var adapter: GroundHistoryAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsTodaysHitoryLiting: ArrayList<LogsUserHistoryRes.BordereauGroundList?> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_ground_user_history, container, false)
        initViews()
        return mView;
    }
    fun initViews() {
        mUtils = Utility()
        rvLogsUserHistory = mView.findViewById(R.id.rvLogsUserHistory)
        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()
        // callingSalesInspectionUserHistory()
        mView.swipeUserHistory.setOnRefreshListener {
            mView.swipeUserHistory.isRefreshing = false
            //callingSalesInspectionUserHistory()
            (parentFragment as MainContainerFragment).getGroundUserHistory(SharedPref.getUserId(Constants.user_id).toString())

            /* if((activity as HomeActivity).currentFragment is MainContainerFragment){
                 ((activity as HomeActivity).currentFragment as MainContainerFragment).getSalesUserHistory(SharedPref.getUserId(Constants.user_id).toString())
             }*/
        }
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.inspection_n_sales)
        (activity as HomeActivity).invisibleFilter()
    }

    fun setupClickListner() {
        mView.ivAddGroundBodreu.setOnClickListener(this)
        mView.btnRetry.setOnClickListener(this)
        mView.txtApprovalList.setOnClickListener(this)
    }

    /*fun callingSalesInspectionUserHistory() {
        getSalesUserHistory(SharedPref.getUserId(Constants.user_id).toString())
    }
*/
    fun setupRecyclerViewNAdapter() {
        logsTodaysHitoryLiting.clear()
        mLinearLayoutManager =
                LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvLogsUserHistory.layoutManager = mLinearLayoutManager
        adapter = GroundHistoryAdapter(mView.context, logsTodaysHitoryLiting, SharedPref.read(Constants.user_role).toString())
        mView.rvLogsUserHistory.adapter = adapter


        adapter.onRePrintInvoiceClick = { modelData, position ->
            if(!logsTodaysHitoryLiting?.get(position)?.inspectionNumber.isNullOrEmpty()) {
                callingRePrintInVoiceAPI(position)
            }else{
                mUtils.showAlert(
                        activity,
                        resources.getString(R.string.inspection_no_not_found)
                )
            }

        }

        adapter.onInspectionOrNoInspectionClick = { modelData, position, isSalesInpection ->
            if (isSalesInpection) {
                when(modelData?.inspectionFlag) {
                    null ->{
                        val myactivity = activity as HomeActivity
                        val fragment = GroundLogsListingFragment()
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
                    "S"->{
                        val myactivity = activity as HomeActivity
                        val fragment = GroundLogsListingFragment()
                        val bundle = Bundle()
                        bundle.putSerializable(
                                Constants.badereuModel,
                                modelData
                        );
                        bundle.putString(
                                Constants.comming_from,
                                mView.context.getString(R.string.user_history)
                        )
                        fragment.arguments = bundle
                        myactivity?.replaceFragment(fragment, false)
                    }
                }

            }else{
                //Noo Inspection api call
              //  callingNoInspectionAPI(position)
            }
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

 /*   fun generateNoInspectionRequest(position: Int): BoderueDeleteLogReq {

        var request : BoderueDeleteLogReq =   BoderueDeleteLogReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(logsTodaysHitoryLiting[position]?.bordereauHeaderId)
        return  request

    }*/


   /* private fun callingNoInspectionAPI(position:Int) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = generateNoInspectionRequest(position)
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
                                        //getSalesUserHistory(SharedPref.getUserId(Constants.user_id).toString())
                                        (parentFragment as MainContainerFragment).getSalesUserHistory(SharedPref.getUserId(Constants.user_id).toString())
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }
                            }else{
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
    }*/

    fun printPDF(pdfPath: String) {
        val printManager =
                mView.context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        /*val printAdapter =
            PdfDocumentAdapter(this@PrintActivity, "/storage/emulated/0/Download/test.pdf")*/
        val printAdapter =
                PdfDocumentAdapter(mView.context, pdfPath)
        printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
    }



  /*  fun downloadPDFFromURL(pdfPath: String) {
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
            request.setDescription("Downloading....")
            request.setTitle(" OLAM ");
            request.setDestinationUri(uri);
            val manager =
                    mView.context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
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
*/

    private fun generateInvoiceRequest(position: Int): BoderueDeleteLogReq {

        var request : BoderueDeleteLogReq =   BoderueDeleteLogReq()
        request.setInspectionNumber(logsTodaysHitoryLiting?.get(position)?.inspectionNumber)

        return  request

    }


    private fun callingRePrintInVoiceAPI(position: Int) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateInvoiceRequest(position)
                val call: Call<AddBodereuLogListingRes> =
                        apiInterface.reprintSalesInoive(request)
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
                                               /* downloadPDFFromURL(
                                                        it*/
                                                val base64Image = it.split(",")[0]
                                                SaveBitmapToInternalStorageAsyncTask().execute(base64Image)
                                                //)
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
            var filOutputStrem : FileOutputStream? = null
            val compressFile: File? = null
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

    fun setupUserHistoryDataForGroundInspection(historyDataList : ArrayList<LogsUserHistoryRes.BordereauGroundList?>, isInternetAvialable:Boolean){
        if(!isInternetAvialable){
            showNoInternetView()
        }else{
            showContentView()
            logsTodaysHitoryLiting.clear()
            logsTodaysHitoryLiting.addAll(historyDataList)
            if (logsTodaysHitoryLiting.size == 0) {
                showNoNoDataView()
            } else {
                showContentView()
            }
            adapter.notifyDataSetChanged()
        }
    }

    fun getSalesUserHistoryRequeest(userID: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setUserId(userID)
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setIsAdmin(false)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun getSalesUserHistory(userID: String) {
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

                val request  =   getSalesUserHistoryRequeest(userID)
                val call_api: Call<LogsUserHistoryRes> =
                    apiInterface.getBordereauHistoryForInspection(
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
                                logsTodaysHitoryLiting.clear()
                                responce.getBordereauGroundList()?.let {
                                    logsTodaysHitoryLiting.addAll(
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
        try {
            mView.linContent.visibility = View.GONE
            mView.linApprovalList.visibility = View.GONE
            mView.relvNoDataFound.visibility = View.GONE
            mView.relvNoInternet.visibility = View.VISIBLE
        }catch(e: Exception){
            e.printStackTrace()
        }
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
            R.id.ivAddGroundBodreu -> {
                /*var fragment = LoadingWagonsHeaderFragment()*/
                val fragment = GroundHeaderFragment()
                val bundle = Bundle()
                bundle.putString(
                        Constants.action,
                        Constants.action_non_edit
                );
                fragment.arguments = bundle
                (activity as HomeActivity).replaceFragment(fragment, false)
            }
            R.id.txtApprovalList -> {
                val fragment = GroundApprovalListingFragment()
                (activity as HomeActivity).replaceFragment(fragment, false)
            }

            R.id.btnRetry -> {
                /*  getSalesUserHistory(SharedPref.getUserId(Constants.user_id).toString())*/
                (parentFragment as MainContainerFragment).getGroundUserHistory(SharedPref.getUserId(Constants.user_id).toString())
            }
        }
    }
}

