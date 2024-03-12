package com.kemar.olam.loading_wagons.fragment

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.datatransport.cct.internal.LogRequest
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.adapter.LogsTodaysHistoryAdapter
import com.kemar.olam.bordereau.fragment.BarcodeBordereuDetailsFragment
import com.kemar.olam.bordereau.fragment.LogsListingFragment
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import com.kemar.olam.loading_wagons.adapter.LoadingWagonsHistoryAdapter
import com.kemar.olam.loading_wagons.model.request.LoadingRequest
import com.kemar.olam.loading_wagons.model.request.WagonLogRequest
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.offlineData.activity.PdfCreatorExampleActivity
import com.kemar.olam.offlineData.fragment.LoadingWagonOfflineFragment
import com.kemar.olam.offlineData.requestbody.BorderauRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.service.MyService
import com.kemar.olam.utility.*
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_loading_wagons_user_history.view.*
import kotlinx.android.synthetic.main.fragment_todays_history.view.btnRetry
import kotlinx.android.synthetic.main.fragment_todays_history.view.linContent
import kotlinx.android.synthetic.main.fragment_todays_history.view.relvNoDataFound
import kotlinx.android.synthetic.main.fragment_todays_history.view.relvNoInternet
import kotlinx.android.synthetic.main.fragment_todays_history.view.rvLogsUserHistory
import kotlinx.android.synthetic.main.fragment_todays_history.view.swipeUserHistory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream


class LoadingWagonsUserHistoryFragment : Fragment(), View.OnClickListener {
    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var rvLogsUserHistory: RecyclerView
    lateinit var adapter: LoadingWagonsHistoryAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsTodaysHitoryLiting: ArrayList<LogsUserHistoryRes.BordereauRecordList?> = arrayListOf()


    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        realm = RealmHelper.getRealmInstance()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_loading_wagons_user_history, container, false)
        initViews()
        return mView;
    }

    fun initViews() {
        mUtils = Utility()
        rvLogsUserHistory = mView.rvLogsUserHistory
        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()
        callingLoadingWagonsUserHistory()
        mView.swipeUserHistory.setOnRefreshListener {
            mView.swipeUserHistory.isRefreshing = false
            callingLoadingWagonsUserHistory()
        }
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.loading_wagons)
        (activity as HomeActivity).invisibleFilter()
        /* (activity as HomeActivity).ivHomeFilter.setOnClickListener{

         }*/
    }

    /*  override fun onResume() {
          super.onResume()
          callingUserHistory()
      }*/
    fun setupClickListner() {
        mView.ivAddLoadingBodreu.setOnClickListener(this)
        mView.btnRetry.setOnClickListener(this)
        mView.txtDeclration.setOnClickListener(this)
        mView.linOffline.setOnClickListener(this)
        mView.linStartOnline.setOnClickListener(this)
    }

    fun callingLoadingWagonsUserHistory() {

        if (!mUtils.checkInternetConnection(mView.context)) {
            logsTodaysHitoryLiting.clear()
            realm.executeTransactionAsync ({ bgRealm ->
                var logRequest: RealmResults<LoadingRequest> = bgRealm.where(LoadingRequest::class.java).sort("uniqueId", Sort.DESCENDING).equalTo("isUpload", false).findAll()
                Log.e("logRequest","is "+logRequest.size)
                for(logrequest in logRequest){
                    var borderaurecord =LogsUserHistoryRes.BordereauRecordList()
//                    borderaurecord.loadingStatus="Draft"
//                    borderaurecord.headerStatus="Draft"

                    if(logrequest.action.equals("Submit") && logrequest.isDeclare == true){
                        borderaurecord.loadingStatus = Constants.IN_TRANSIT
                        borderaurecord.headerStatus = Constants.IN_TRANSIT
                    }else if(logrequest.action.equals("Submit")){
                        borderaurecord.loadingStatus = Constants.LOADED
                        borderaurecord.headerStatus = Constants.LOADED
                    }else  {
                        borderaurecord.loadingStatus = Constants.DRAFT
                        borderaurecord.headerStatus = Constants.DRAFT
                    }

                    borderaurecord.supplierName=logrequest.forestName
                    borderaurecord.supplierId=logrequest.forestId
                    borderaurecord.transporterId=logrequest.transpoterId
                    borderaurecord.transporterName=logrequest.transpoterName
                    borderaurecord.uniqueId=logrequest.uniqueId
                    borderaurecord.originId=logrequest.originId
                    borderaurecord.originName=logrequest.originName
                    borderaurecord.bordereauNo=logrequest.bordereauNo
                    borderaurecord.leauChargementId=logrequest.leudechargementId
                    borderaurecord.leauChargementName=logrequest.chargementName
                    borderaurecord.bordereauDateString=logrequest.currentDate
                    borderaurecord.bordereauDate=logrequest.currentDate
                    borderaurecord.bordereauDate=logrequest.bordereauDate
                    borderaurecord.bordereauNo=logrequest.bordereauNo
                    borderaurecord.eBordereauNo=logrequest.ebordereauNo
                    borderaurecord.fscId=logrequest.fscId
                    borderaurecord.fscName=logrequest.fscName
                    borderaurecord.wagonId=logrequest.transpoterId
                    borderaurecord.distance=logrequest.distance
                    borderaurecord.destination=logrequest.destination
                    borderaurecord.timezoneId="Asia/Kolkata"
                    borderaurecord.truckNo=logrequest.wagonNo
                    borderaurecord.wagonNo= logrequest.wagonNo
                    borderaurecord.transportMode=logrequest.modeOfTransport
                    borderaurecord.recordDocNo=logrequest.recordDocNo
                    borderaurecord.supplierShortName=logrequest.forestName
                    borderaurecord.isDeclare= logrequest.isDeclare

                    var wagonLogRequest: RealmResults<WagonLogRequest> = bgRealm.where(WagonLogRequest::class.java).equalTo("forestuniqueId", logrequest.uniqueId, Case.SENSITIVE).findAll()
                    borderaurecord.logQty=wagonLogRequest.size
                    logsTodaysHitoryLiting.add(borderaurecord)

                }
            }, {
                Log.e("success","success")
                Log.e("logsTodaysHitoryLiting","is "+logsTodaysHitoryLiting.size)


                adapter.notifyDataSetChanged()

                if(logsTodaysHitoryLiting.size>0){
                    showContentView()
                }else{
                    showNoNoDataView()
                }

            }) {
                Log.e("faile","faile to update")
            }
        }else {
            getLoadingWagonsUserHistory(SharedPref.getUserId(Constants.user_id).toString())
        }
    }

    fun setupRecyclerViewNAdapter() {
        logsTodaysHitoryLiting.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvLogsUserHistory.layoutManager = mLinearLayoutManager
        adapter = LoadingWagonsHistoryAdapter(mView.context, logsTodaysHitoryLiting,false)
        mView.rvLogsUserHistory.adapter = adapter


        adapter.onHeaderClick = { modelData, position ->
            if (modelData.loadingStatus.equals(Constants.DRAFT, ignoreCase = true)!! || modelData.loadingStatus.equals(Constants.LOADED, ignoreCase = true && !modelData.isDeclare!!)) {
                val myactivity = activity as HomeActivity
                val fragment = LoadingWagonsLogsListingFragment()
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.badereuModel,
                    modelData
                )
                bundle.putString(
                    Constants.comming_from,
                    Constants.USER_HISTORY
                )

                fragment.arguments = bundle
                myactivity?.replaceFragment(fragment, false)
            } else {
                val myactivity = activity as HomeActivity
                val fragment = LoadingWaDetailsFragment()
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.badereuModel,
                    modelData
                )
                bundle.putString(
                    Constants.comming_from,
                    Constants.USER_HISTORY
                )

                if (!mUtils.checkInternetConnection(mView.context)) {
                    bundle.putBoolean(
                        Constants.Offline,true
                    )
                }

                fragment.arguments = bundle
                myactivity?.replaceFragment(fragment, false)
            }
        }

        adapter.onReprintClick = { modelData, position ->
            if (modelData.loadingStatus?.equals(Constants.IN_TRANSIT, ignoreCase = true)!!) {
                if (mUtils?.checkInternetConnection(mView.context) == true) {
                    callingRePrintInVoiceAPI(position)
                }else{
                    var intent= Intent(requireActivity(), PdfCreatorExampleActivity::class.java)
                    intent.putExtra(Constants.UNIQUEID,modelData.uniqueId)
                    startActivity(intent)
                }
            }
        }
    }

    private fun generateInvoiceRequest(position: Int): CommonRequest {

        var request: CommonRequest = CommonRequest()
        request.setBordereauHeaderId(logsTodaysHitoryLiting?.get(position)?.bordereauHeaderId?.toString())

        return request

    }


    private fun callingRePrintInVoiceAPI(position: Int) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateInvoiceRequest(position)
                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.reprintDeclaredInoive(request)
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
                                        if (!response.body().getPdfFilePath().isNullOrEmpty()) {
                                            response.body().getPdfFilePath()?.let {
                                                /* downloadPDFFromURL(
                                                         it*/
                                                var base64Image = ""

                                                if(it.contains("data:application/pdf;base64")) {
                                                    base64Image = response.body().getPdfFilePath()!!.split(",")[1]
                                                }else{
                                                    base64Image = response.body().getPdfFilePath()!!
                                                }

                                                SaveBitmapToInternalStorageAsyncTask().execute(
                                                    base64Image
                                                )
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
                            }  else {
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

    inner class SaveBitmapToInternalStorageAsyncTask internal constructor() :
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
            var filOutputStrem: FileOutputStream? = null
            val compressFile: File? = null
            try {

                val bmScreenShot = p0[0]
                if (bmScreenShot != null) {
                    file = mUtils.writeDataIntoFileAndSavePDF(mView, "Invoice", bmScreenShot)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return file
        }


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

    fun getLoadingWagonsUserHistoryRequest(userID: String): CommonRequest {
        val request: CommonRequest = CommonRequest()
        request.setUserId(userID)
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setIsAdmin(false)

        val json = Gson().toJson(request)
        var test = json

        return request

    }

    private fun getLoadingWagonsUserHistory(userID: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            showContentView()
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = getLoadingWagonsUserHistoryRequest(userID)
                val call_api: Call<LogsUserHistoryRes> =
                    apiInterface.getLoadingWagonsUserHistory(request)
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
                                }else{
                                    mUtils.showToast(activity, response.body().getMessage())
                                }
                            } else {
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
        mView.linDeclaration.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.VISIBLE
    }

    fun showNoNoDataView() {
        mView.linContent.visibility = View.GONE
        mView.linDeclaration.visibility = View.VISIBLE
        mView.relvNoInternet.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.VISIBLE
    }

    fun showContentView() {
        mView.linContent.visibility = View.VISIBLE
        mView.linDeclaration.visibility = View.VISIBLE
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

            R.id.linOffline -> {
                /*var fragment = LoadingWagonsHeaderFragment()*/
                var fragment = LoadingWagonOfflineFragment()

                (activity as HomeActivity).replaceFragment(fragment, false)
            }

            R.id.linStartOnline -> {
                try {
                    val i = Intent(mView.context, MyService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        activity?.startForegroundService(i)
                    }else
                        activity?.startService(i)
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }


            R.id.txtDeclration -> {
                /*var fragment = LoadingWagonsHeaderFragment()*/
                var fragment = LoadingWagonsDeclarFragment()
                val bundle = Bundle()
                bundle.putString(
                    Constants.action,
                    Constants.action_non_edit
                );
                fragment.arguments = bundle
                (activity as HomeActivity).replaceFragment(fragment, false)
            }

            R.id.btnRetry -> {
                getLoadingWagonsUserHistory(SharedPref.getUserId(Constants.user_id).toString())
            }
        }
    }
}


