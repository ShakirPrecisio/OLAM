package com.kemar.olam.offlineData.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.kemar.olam.loading_wagons.fragment.LoadingWaDetailsFragment
import com.kemar.olam.loading_wagons.fragment.LoadingWagonsDeclarFragment
import com.kemar.olam.loading_wagons.fragment.LoadingWagonsHeaderFragment
import com.kemar.olam.loading_wagons.model.request.LoadingRequest
import com.kemar.olam.loading_wagons.model.request.WagonLogRequest
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.offlineData.adapter.WagonListAdapter
import com.kemar.olam.offlineData.requestbody.BorderauRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.*
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_loading_wagons_user_history.view.*
import kotlinx.android.synthetic.main.fragment_todays_history.view.*
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoadingWagonOfflineFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoadingWagonOfflineFragment : Fragment(), View.OnClickListener {

    lateinit var realm: Realm
    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var rvLogsUserHistory: RecyclerView
    lateinit var adapter: WagonListAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsTodaysHitoryLiting: ArrayList<LogsUserHistoryRes.BordereauRecordList?> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        realm = RealmHelper.getRealmInstance()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.fragment_loading_wagon_offline, container, false)
        initViews()
        return mView;
    }

    fun initViews() {
        mUtils = Utility()
        rvLogsUserHistory = mView.rvLogsUserHistory
        setToolbar()
        setupClickListner()

        offlineData()
        mView.swipeUserHistory.setOnRefreshListener {
            mView.swipeUserHistory.isRefreshing = false
            offlineData()
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
    
    
    
    fun offlineData(){

        realm.executeTransactionAsync ({ bgRealm ->
            var addLoadingBordereueHeaderReq: RealmResults<LoadingRequest> = bgRealm.where(LoadingRequest::class.java).sort("uniqueId", Sort.DESCENDING)
                    .equalTo("isUpload",false)
                    .or().equalTo("isFailed",true)
                    .or().equalTo("isLogUpload",false)
                    .findAll()
            Log.e("addLoadingReq","is "+addLoadingBordereueHeaderReq.size)

           // logsTodaysHitoryLiting.addAll(realm.copyFromRealm(addLoadingBordereueHeaderReq))
            logsTodaysHitoryLiting.clear()
            for(borderaurequest in addLoadingBordereueHeaderReq){
                var borderaurecord = LogsUserHistoryRes.BordereauRecordList()
                borderaurecord.loadingStatus="Draft"
                borderaurecord.isUpload= borderaurequest.isUpload!!
                borderaurecord.isFail= borderaurequest.isFailed!!
                borderaurecord.failreason= borderaurequest?.failedReason
                borderaurecord.headerStatus="Draft"
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
                borderaurecord.bordereauHeaderId=borderaurequest.headerID
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

                var borderauRequest: RealmResults<WagonLogRequest> = bgRealm.where(
                        WagonLogRequest::class.java).equalTo("forestuniqueId", borderaurequest.uniqueId, Case.SENSITIVE).findAll()
                borderaurecord.logQty=borderauRequest.size
                logsTodaysHitoryLiting.add(borderaurecord)

            }
        }, {
            Log.e("success","success")
            Log.e("logsTodaysHitoryLiting","is "+logsTodaysHitoryLiting.size)

            if(logsTodaysHitoryLiting.size==0){
                showNoNoDataView()
            }else{
                showContentView()
            }

            setupRecyclerViewNAdapter()

        }) {
            Log.e("faile","faile to update")
        }
        
    }
    fun setupClickListner() {
        mView.ivAddLoadingBodreu.setOnClickListener(this)
        mView.btnRetry.setOnClickListener(this)
        mView.txtDeclration.setOnClickListener(this)
    }



    fun setupRecyclerViewNAdapter() {
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvLogsUserHistory.layoutManager = mLinearLayoutManager
        adapter = WagonListAdapter(mView.context, logsTodaysHitoryLiting,true)
        mView.rvLogsUserHistory.adapter = adapter


        adapter.onHeaderClick = { modelData, position ->
            if (modelData.loadingStatus.equals(Constants.DRAFT, ignoreCase = true)!!) {
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
                bundle.putBoolean(
                        Constants.Offline,
                       true
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
                fragment.arguments = bundle
                myactivity?.replaceFragment(fragment, false)
            }
        }

        adapter.onReprintClick = { modelData, position ->
            if (modelData.loadingStatus?.equals(Constants.IN_TRANSIT, ignoreCase = true)!!) {
                callingRePrintInVoiceAPI(position)
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




    fun showNoInternetView() {
        mView.linContent.visibility = View.GONE
        mView.linDeclaration.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.VISIBLE
    }

    fun showNoNoDataView() {
        mView.linContent.visibility = View.GONE
        mView.linDeclaration.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.VISIBLE
    }

    fun showContentView() {
        mView.linContent.visibility = View.VISIBLE
        mView.linDeclaration.visibility = View.GONE
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
               offlineData()
            }
        }
    }
}


