    package com.kemar.olam.loading_wagons.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DownloadManager
import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.loading_wagons.adapter.Loading_Declration_ListingAdapter
import com.kemar.olam.loading_wagons.adapter.MonthAdapter
import com.kemar.olam.loading_wagons.model.request.GeneratePDFReq
import com.kemar.olam.loading_wagons.model.request.LoadingRequest
import com.kemar.olam.loading_wagons.model.request.WagonLogRequest
import com.kemar.olam.loading_wagons.model.responce.DeclrationBordereuListRes
import com.kemar.olam.loading_wagons.model.responce.GetLogDataByBarcodeRes
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.offlineData.activity.PdfCreatorExampleActivity
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.*
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.dialog_month_layout.view.*
import kotlinx.android.synthetic.main.fragment__add_header.view.*
import kotlinx.android.synthetic.main.fragment_loading_wagons_declar.view.*
import kotlinx.android.synthetic.main.fragment_loading_wagons_declar.view.txtDate
import kotlinx.android.synthetic.main.fragment_loading_wagons_declar.view.txtForest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.Context.BIND_AUTO_CREATE
import android.os.*
import androidx.core.widget.NestedScrollView

    class LoadingWagonsDeclarFragment : Fragment(), View.OnClickListener,
        DialogFragment.GetDialogListener {

    var monthDialog: AlertDialog? = null
    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var rvDeclareListing: RecyclerView
    lateinit var adapter: Loading_Declration_ListingAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    lateinit var countryDialogFragment: DialogFragment
    var bordereauListing: ArrayList<DeclrationBordereuListRes.BordereauRecordList?> =
            arrayListOf()
    var forestList: java.util.ArrayList<SupplierDatum?>? = arrayListOf()
    lateinit var presectedCalender: Calendar
    var isDialogShowing: Boolean = false
    var forestID: String = "0"
    var bordereauDate: String = "00"
    var isGSEBOrForestDeclaration :  Boolean = false
    var isLoading = false


    var totalTonnes = 0.0
    var cubantOkumeSum = 0.0
    var othercubantSum = 0.0

    var OKOUME = "OKOUME"

    var wagonLogRequests: RealmResults<WagonLogRequest>? = null

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
        mView = inflater.inflate(R.layout.fragment_loading_wagons_declar, container, false)
        initViews()
        return mView
    }

    fun initViews() {
        mUtils = Utility()

        rvDeclareListing = mView.findViewById(R.id.rvDeclareListing)
        presectedCalender = Calendar.getInstance()

        mView.radioGp.setOnCheckedChangeListener { group, checkedId ->
            // checkedId is the RadioButton selected
            if(checkedId!=-1) {
                val rb = mView.findViewById(checkedId) as RadioButton
                when (rb?.text) {
                    mView.resources.getString(R.string.gsez__decleration) -> {
                        isGSEBOrForestDeclaration = true
                    }
                    mView.resources.getString(R.string.forester_declaration) -> {
                        isGSEBOrForestDeclaration = false
                    }
                }

            }
        }


        mView.chkBoxSelectAll.setOnClickListener{
            if (mView.chkBoxSelectAll.isChecked) {
                for (listdata in bordereauListing) {
                    listdata?.isSelected = true
                }
                setSelectionValidation()
//                adapter.notifyDataSetChanged()
            } else {
                if (bordereauListing.size != 0) {
                    for (listdata in bordereauListing) {
                        listdata?.isSelected = false
                    }
                    setSelectionValidation()
//                    adapter.notifyDataSetChanged()
                }
            }
        }

        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()
        /*getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
        callingDeclarationList()*/

        if (mUtils?.checkInternetConnection(mView.context) == true) {
            getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
            callingDeclarationList()
        }else{
             realm.executeTransactionAsync ({ bgRealm ->
                 var logRequest: RealmResults<LoadingRequest> = bgRealm.where(LoadingRequest::class.java)
                     .sort("uniqueId", Sort.DESCENDING)
                     .equalTo("isLogUpload", false)
                     .equalTo("isDeclare", false)
                     .equalTo("isDeclareDone", false)
                     .equalTo("isDeclareDoneFail", false)
                     .equalTo("action",Constants.SUBMIT).findAll()
                 Log.e("logRequest","is "+logRequest.size)

                 for(logrequest in logRequest) {
                     var bordereauRecordList = com.kemar.olam.loading_wagons.model.responce.DeclrationBordereuListRes.BordereauRecordList()

                     bordereauRecordList.uniqueId = logrequest.uniqueId
                     bordereauRecordList.bordereauNo = logrequest.bordereauNo
                     bordereauRecordList.bordereauDate=logrequest.bordereauDate
                     bordereauRecordList.bordereauHeaderId=logrequest.headerID
                     bordereauRecordList.destination=logrequest.destination
                     bordereauRecordList.eBordereauNo=logrequest.ebordereauNo
                     bordereauRecordList.wagonNo=logrequest.wagonNo
                     bordereauRecordList.supplierShortName=logrequest.supplierShortName
                     bordereauRecordList.supplierName=logrequest.supplierShortName




                     wagonLogRequests = bgRealm.where(WagonLogRequest::class.java).equalTo("forestuniqueId", logrequest.uniqueId).findAll()

                     for(i in (wagonLogRequests as RealmResults<WagonLogRequest>?)?.indices!!) {

                         if (OKOUME == (wagonLogRequests as RealmResults<WagonLogRequest>?)?.get(i)!!.getLogSpeciesName()) {
                             cubantOkumeSum += (wagonLogRequests as RealmResults<WagonLogRequest>?)?.get(i)!!.getCbm()!!
                         } else {
                             othercubantSum += (wagonLogRequests as RealmResults<WagonLogRequest>?)?.get(i)!!.getCbm()!!
                         }
                     }

                     bordereauRecordList.gsezCBM= mUtils.convertDoubleToRoundedValue(cubantOkumeSum + othercubantSum).toDoubleOrNull()


                     totalTonnes = 0.6 * cubantOkumeSum + 0.8 * othercubantSum

                     bordereauRecordList.totaltonnage=mUtils.convertDoubleToRoundedValue(totalTonnes).toDoubleOrNull()


                     bordereauListing.add(bordereauRecordList)
                     adapter.addItem(bordereauRecordList)

                 }

//                 adapter.notifyDataSetChanged()

             }, {
                 Log.e("success","success")
                 Log.e("logsTodaysHitoryLiting","is ")



//                if(logRequest.size>0){
//                    showContentView()
//                }else{
//                    showNoNoDataView()
//                }

            }) {
                Log.e("failed","failed to update")
            }
        }

        mView.swipDeclareListing.setOnRefreshListener {
            mView.swipDeclareListing.isRefreshing = false
            forestID = "0"
            bordereauDate = "00"
            view?.txtForest?.text = mView.context.resources.getString(R.string.forest)
            view?.txtDate?.text = mView.context.resources.getString(R.string.date_)
            mView.chkBoxSelectAll.isChecked = false
           /* getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
            callingDeclarationList()*/

            if (mUtils?.checkInternetConnection(mView.context) == true) {
                getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
                callingDeclarationList()
            }else{
                bordereauListing.clear()
                adapter.removeAllItem()
                totalTonnes=0.0
                cubantOkumeSum = 0.0
                othercubantSum = 0.0

                realm.executeTransactionAsync ({ bgRealm ->
                    var logRequest: RealmResults<LoadingRequest> = bgRealm.where(LoadingRequest::class.java)
                        .sort("uniqueId", Sort.DESCENDING)
                        .equalTo("isLogUpload", false)
                        .equalTo("isDeclare", false)
                        .equalTo("isDeclareDone", false)
                        .equalTo("isDeclareDoneFail", false)
                        .equalTo("action",Constants.SUBMIT).findAll()
                    Log.e("logRequest","is "+logRequest.size)
                    for(logrequest in logRequest) {
                        var bordereauRecordList = com.kemar.olam.loading_wagons.model.responce.DeclrationBordereuListRes.BordereauRecordList()

                        bordereauRecordList.uniqueId = logrequest.uniqueId
                        bordereauRecordList.bordereauNo = logrequest.bordereauNo
                        bordereauRecordList.bordereauDate=logrequest.bordereauDate
                        bordereauRecordList.bordereauHeaderId=logrequest.headerID
                        bordereauRecordList.destination=logrequest.destination
                        bordereauRecordList.eBordereauNo=logrequest.ebordereauNo
                        bordereauRecordList.wagonNo=logrequest.wagonNo
                        bordereauRecordList.supplierShortName=logrequest.supplierShortName
                        bordereauRecordList.supplierName=logrequest.supplierShortName



                        wagonLogRequests = bgRealm.where(WagonLogRequest::class.java).equalTo("forestuniqueId", logrequest.uniqueId).findAll()

                        for(i in (wagonLogRequests as RealmResults<WagonLogRequest>?)?.indices!!) {

                            if (OKOUME == (wagonLogRequests as RealmResults<WagonLogRequest>?)?.get(i)!!.getLogSpeciesName()) {
                                cubantOkumeSum += (wagonLogRequests as RealmResults<WagonLogRequest>?)?.get(i)!!.getCbm()!!
                            } else {
                                othercubantSum += (wagonLogRequests as RealmResults<WagonLogRequest>?)?.get(i)!!.getCbm()!!
                            }
                        }

                        bordereauRecordList.gsezCBM= mUtils.convertDoubleToRoundedValue(cubantOkumeSum + othercubantSum).toDoubleOrNull()

                        totalTonnes = 0.6 * cubantOkumeSum + 0.8 * othercubantSum

                        bordereauRecordList.totaltonnage=mUtils.convertDoubleToRoundedValue(totalTonnes).toDoubleOrNull()

                        bordereauListing.add(bordereauRecordList)
                        adapter.addItem(bordereauRecordList)

                    }

//                    adapter.notifyDataSetChanged()

                }, {
                    Log.e("success","success")
                    Log.e("logsTodaysHitoryLiting","is ")

//                if(logRequest.size>0){
//                    showContentView()
//                }else{
//                    showNoNoDataView()
//                }

                }) {
                    Log.e("failed","failed to update")
                }
            }
        }
    }

    fun setupRecyclerViewNAdapter() {
        bordereauListing.clear()

        mLinearLayoutManager =
                LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvDeclareListing.layoutManager = mLinearLayoutManager
        adapter = Loading_Declration_ListingAdapter(mView.context)
//        mView.rvDeclareListing.setHasFixedSize(true)
        mView.rvDeclareListing.adapter = adapter

        mView.nestedScrollViewDecl.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->

            if(scrollY - oldScrollY > 0 )
            if (!isLoading) {
                if (mLinearLayoutManager != null && mLinearLayoutManager.findLastCompletelyVisibleItemPosition() !=
                    bordereauListing.size - 1) {
                    //bottom of list!
                    loadMore()
                    isLoading = true
                }
            }
        })

        adapter.onMoreClick = { modelData, position, isExpanded ->
            bordereauListing.get(position)?.isExpanded = isExpanded
//            adapter.notifyDataSetChanged()
        }

        adapter.onTonnageUpdate = { modelData, position, isSelected ->
            if (mView.chkBoxSelectAll.isChecked) {
                if (!isSelected) {
                    mView.chkBoxSelectAll.isChecked = false
                }
            }
            setSelectionValidation()
        }

        adapter.onHeaderClick = { modelData, position ->
            /*CR Point*/
            val myactivity = activity as HomeActivity
            val fragment = LoadingWaDetailsFragment()
            val bundle = Bundle()
            bundle.putSerializable(
                Constants.badereuModel,
                modelData
            )
            bundle.putString(
                Constants.comming_from,
                Constants.DECLARATION
            )
            fragment.arguments = bundle
            myactivity?.replaceFragment(fragment, false)

        }

        adapter.onRemoveClick = { modelData, position ->
            deleteLoadedBordereau(modelData.bordereauHeaderId)
        }

    }

    private fun loadMore() {
        val handler = Handler()
        handler.postDelayed(Runnable {
//            rowsArrayList.removeAt(rowsArrayList.size − 1)
            val scrollPosition = adapter.itemCount
//            recyclerViewAdapter.notifyItemRemoved(scrollPosition)
            var currentSize = scrollPosition
            val nextLimit = currentSize + 20
            while (currentSize - 1 < nextLimit) {
                if(currentSize < bordereauListing.size)
            adapter.addItem(bordereauListing[currentSize])
            currentSize++
        }
//            recyclerViewAdapter.notifyDataSetChanged()
            isLoading = false
        }, 2000)
    }


    fun setupClickListner() {
        mView.linPrint.setOnClickListener(this)
        mView.linDeclare.setOnClickListener(this)
        mView.cardvwForest.setOnClickListener(this)
        mView.cardvwDate.setOnClickListener(this)
        mView.btnRetry.setOnClickListener(this)
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.loading_n_declration_)
        (activity as HomeActivity).invisibleFilter()
    }

    fun generateBordereuPDFRequest(): GeneratePDFReq {

        var request: GeneratePDFReq = GeneratePDFReq()
        for (listdata in bordereauListing) {
            if (listdata?.isSelected!!) {
                listdata.bordereauHeaderId?.toString()?.let { request.setBordereauHeaderId(it) }
            }
        }
        request.setIsGSEBOrForestDeclaration(isGSEBOrForestDeclaration)
        request.setIntOfflineFlag(1) //for offline = 1, online = 0
        //request.setBordereauHeaderIdList(sselectedHeaderList)
        return request
    }

    fun setClearTonnage() {
        mView.txtTotalTonage.text = mView.context.getString(R.string.total_tonnage)+"0.0"
    }

    fun setSelectionValidation() {
        var totalSize = 0
        var totalTonnage: Double = 0.0
        for (listdata in bordereauListing) {
            if (listdata?.isSelected!!) {
                totalSize++
                if(listdata?.totaltonnage!=null){
                    totalTonnage += listdata?.totaltonnage!!
                }
            }
        }
        if (totalSize == bordereauListing.size) {
            mView.chkBoxSelectAll.isChecked = true
        }
        val roundValue =  Math.floor(totalTonnage * 100) / 100;
        mView.txtTotalTonage.text = mView.context.getString(R.string.total_tonnage)+ roundValue.toString()
    }

    fun setTonnage() {
        var totalTonnage: Double = 0.0
        for (listdata in bordereauListing) {
            if(listdata?.totaltonnage!=null){
                totalTonnage += listdata?.totaltonnage!!
            }
        }
        val roundValue =  Math.floor(totalTonnage * 100) / 100;
        mView.txtTotalTonage.text =  mView.context.getString(R.string.total_tonnage)+ roundValue.toString()
    }

    fun isValidateSelection(): Boolean {
        var isValidate: Boolean = false
        for (listdata in bordereauListing) {
            if (listdata?.isSelected!!) {
                isValidate = true
            }
        }
        return isValidate
    }

    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.linPrint -> {
                if (isValidateSelection()) {
                    if (mUtils?.checkInternetConnection(mView.context) == true) {
                        acessRuntimPermission()
                    }else{
                        var uniqueId=""

                        for (listdata in bordereauListing) {
                            if (listdata?.isSelected!!) {
                                uniqueId= listdata.uniqueId?.toString()!!
                            }
                        }

                        var intent=Intent(requireActivity(), PdfCreatorExampleActivity::class.java)
                        intent.putExtra(Constants.UNIQUEID,uniqueId)
                        intent.putExtra(Constants.isGESZ,isGSEBOrForestDeclaration)
                        startActivity(intent)
                    }
                } else {
                    mUtils.showAlert(activity, mView.context.resources.getString(R.string.select_bordereau_for_generating_pdf))
                }
            }

            R.id.linDeclare -> {

                if (isValidateSelection()) {
                    if (mUtils?.checkInternetConnection(mView.context) == true) {
                        callingDeclarePostAPI()
                    }else{


                        for (listdata in bordereauListing) {
                            if (listdata?.isSelected!!) {

                                realm.executeTransactionAsync({ bgRealm ->
                                    var loadingRequest = bgRealm.where(LoadingRequest::class.java).sort("uniqueId", Sort.DESCENDING).equalTo("wagonNo", listdata.wagonNo).findFirst()
                                    loadingRequest!!.isDeclare=true
                                    bgRealm.copyToRealmOrUpdate(loadingRequest)
                                }, {
                                    Log.e("Success","Success")

                                    mUtils.showToast(requireActivity(),getString(R.string.loading_wagon_declaration_successful))
                                    requireActivity().onBackPressed()

                                   /* var uniqueId=""

                                    for (listdata in bordereauListing) {
                                        if (listdata?.isSelected!!) {
                                            uniqueId= listdata.uniqueId?.toString()!!
                                        }
                                    }

                                    var intent=Intent(requireActivity(), PdfCreatorExampleActivity::class.java)
                                    intent.putExtra(Constants.UNIQUEID,uniqueId)
                                    startActivity(intent)*/

                                }) {
                                    Log.e("failed","failed")
                                }
                            }
                        }


                    }
                } else {
                    mUtils.showAlert(activity, mView.context.resources.getString(R.string.select_bordereau_for_declare))
                }


            }
            R.id.btnRetry -> {
                getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
                callingDeclarationList()
            }

            R.id.cardvwForest -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(forestList, "forest")
            }

            R.id.cardvwDate -> {
                //datePicker(mView?.txtDate)
                showMonthDialog()
            }
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



    private fun callingetPDFFileForLoaded() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateBordereuPDFRequest()
                val call_api: Call<AddBodereuLogListingRes> =
                        apiInterface.getPDFFileForLoaded(request)
                call_api.enqueue(object :
                        Callback<AddBodereuLogListingRes> {
                    override fun onResponse(
                        call: Call<AddBodereuLogListingRes>,
                        response: Response<AddBodereuLogListingRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response.body().getSeverity() == 200) {
                                val responce: AddBodereuLogListingRes =
                                    response.body()!!
                                if (responce != null) {
                                        responce.getPdfFilePath()?.let {
                                           /* downloadPDFFromURL(it) */
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
                        call: Call<AddBodereuLogListingRes>,
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
        }
    }


    private fun deleteLoadedBordereau(bodereuHeaderId: Int?) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request  =  deleteLoadedBordereauRequest(bodereuHeaderId.toString())
                val call_api: Call<GetLogDataByBarcodeRes> =
                    apiInterface.deleteLoadedBordereau(request)
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
                                        callingDeclarationList()

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


    fun deleteLoadedBordereauRequest(bodereuHeaderId:String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setHeaderId(bodereuHeaderId)
        request.setUserId(SharedPref.getUserId(Constants.user_id).toString())
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }



    private fun callingDeclarePostAPI() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateBordereuPDFRequest()
                val call_api: Call<AddBodereuLogListingRes> =
                        apiInterface.declareLoadedBordereau(request)
                call_api.enqueue(object :
                        Callback<AddBodereuLogListingRes> {
                    override fun onResponse(
                        call: Call<AddBodereuLogListingRes>,
                        response: Response<AddBodereuLogListingRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                val responce: AddBodereuLogListingRes =
                                    response.body()!!
                                if (responce.getSeverity() == 200) {

                                    if (responce != null) {
                                        responce.getPdfFilePath()?.let {
                                            /* downloadPDFFromURL(it) */
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

                                        var fragment = LoadingWagonsUserHistoryFragment()
                                        (activity as HomeActivity).replaceFragment(fragment, true)
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
                        call: Call<AddBodereuLogListingRes>,
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
        }
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

    fun acessRuntimPermission() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                callingetPDFFileForLoaded()
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
                        Manifest.permission.INTERNET,
                                Manifest.permission.ACCESS_NETWORK_STATE/*,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE*/
                )
                .check()
    }

    fun checkIsFragmentDialogAlreadyShowing(): Boolean {
        if (isDialogShowing) {
            return false
        }
        return true
    }

    fun printPDF(pdfPath: String) {
        val printManager =
                mView.context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        /*val printAdapter =
            PdfDocumentAdapter(this@PrintActivity, "/storage/emulated/0/Download/test.pdf")*/
        val printAdapter =
                PdfDocumentAdapter(mView.context, pdfPath)
        printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
//
//        MyService.getAllLogDataForLoadingWagons
    }



    fun datePicker(txtView: TextView) {
        val now = Calendar.getInstance()
        val datePickerDialog =
                DatePickerDialog(
                        mView.context,
                        object :
                                DatePickerDialog.OnDateSetListener {
                            @SuppressLint("SetTextI18n")
                            override fun onDateSet(
                                    view: DatePicker?, year: Int,
                                    monthOfYear: Int, dayOfMonth: Int
                            ) {

                                presectedCalender.set(Calendar.YEAR, year);
                                presectedCalender.set(Calendar.MONTH, monthOfYear);
                                presectedCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                                txtView.setText(
                                        mUtils.dateFormater(
                                                "dd-MM-yyyy",
                                                "dd.MM.yyyy",
                                                dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                                        )
                                )
                                bordereauDate = mUtils.dateFormater(
                                        "dd-MM-yyyy",
                                        "dd.MM.yyyy",
                                        dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                                )!!

                                callingDeclarationList()

                            }

                        },
                        presectedCalender.get(Calendar.YEAR),
                        presectedCalender.get(Calendar.MONTH),
                        presectedCalender.get(
                                Calendar.DAY_OF_MONTH
                        )


                )
        datePickerDialog.datePicker.setMaxDate(Date().time)
        datePickerDialog.show()
    }

    //Dialog content
    open fun showDialog(countryListSearch: ArrayList<SupplierDatum?>?, action: String) {

        try {
            if (!countryListSearch.isNullOrEmpty()) {
                Collections.sort(
                        countryListSearch,
                        Comparator<SupplierDatum?> { contactOne, contactSecond ->
                            contactOne.optionName!!.toLowerCase().compareTo(
                                    contactSecond?.optionName!!.toLowerCase()
                            )
                        })
            }
        } catch (e: Exception) {
        }


        val fm: FragmentManager
        val bundle: Bundle
        fm = childFragmentManager
        countryDialogFragment = DialogFragment(AddHeaderFragment@ this)
        bundle = Bundle()
        bundle.putSerializable("COUNTRY_LIST", countryListSearch)
        countryDialogFragment.setArguments(bundle)
        bundle.putBoolean("isCountryCode", false)
        bundle.putString("action", action)
        countryDialogFragment.show(fm, "COUNTRY_FRAGMENT")
        countryDialogFragment.isCancelable = false
        isDialogShowing = true
    }

    override fun onSubmitData(model: SupplierDatum?, isCountryCode: Boolean, action: String) {

        try {
            countryDialogFragment.dismiss()
            isDialogShowing = false
            mView.txtBordero_No.clearFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (model != null) {
            try {
                when (action) {
                    "forest" -> {
                        view?.txtForest?.text = model.optionName
                        forestID = model.optionValue!!.toString()
                        view?.txtForest?.requestFocus()
                        callingDeclarationList()
                    }


                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCancleDialog() {
        try {
            countryDialogFragment.dismiss()
            isDialogShowing = false
            view?.txtForest?.text = mView.context.resources.getString(R.string.forest)
            forestID = "0"
            view?.txtForest?.requestFocus()
            callingDeclarationList()
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    fun callingDeclrationHistory(){


    }
    fun getDeclarationListRequest(): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setForestId(forestID)
        request.setBordereauDate(bordereauDate)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }



    fun callingDeclarationList(
    ) {
        if (mUtils.checkInternetConnection(mView.context)) {
            showContentView()
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request  =   getDeclarationListRequest()
                val call_api: Call<DeclrationBordereuListRes> =
                        apiInterface.getBordereauListDeclaration(request)
                call_api.enqueue(object :
                        Callback<DeclrationBordereuListRes> {
                    override fun onResponse(
                            call: Call<DeclrationBordereuListRes>,
                            response: Response<DeclrationBordereuListRes>
                    ) {

                        try {

                            if (response.code() == 200) {
                                val responce: DeclrationBordereuListRes =
                                    response.body()!!
                                if (responce != null) {
                                    bordereauListing.clear()
                                    adapter.removeAllItem()
                                    if (responce.getSeverity() == 200) {
                                        responce.getBordereauRecordList()?.let {
                                            bordereauListing.addAll(
                                                it
                                            )

                                            adapter.addAllItems(ArrayList(bordereauListing.take(20)))
                                        }
                                    } else if (response.body()?.getSeverity() == 306) {
                                    mUtils.alertDialgSession(mView.context, activity)
                                } else {
                                    mUtils.showToast(activity, response.body().getMessage())
                                }
                                    setClearTonnage()
                                    if (bordereauListing.size == 0) {
                                        showNoNoDataView()
                                    } else {
                                        // setTonnage()
                                        showContentView()
                                    }
//                                    adapter.notifyDataSetChanged()
                                }
                            } else if (response.code() == 306) {
                                mUtils.alertDialgSession(mView.context, activity)
                            } else{
                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                            val handler = Handler()
                            handler.postDelayed({ mUtils.dismissProgressDialog() }, 5000)

                        } catch (e: Exception) {
                            mUtils.dismissProgressDialog()
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                            call: Call<DeclrationBordereuListRes>,
                            t: Throwable
                    ) {
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            mUtils.dismissProgressDialog()
            mUtils.showToast(mView.context, getString(R.string.no_internet))
            showNoInternetView()
        }
    }


    fun getForestByLocationRequest(userLocationID:String): CommonRequest {
        var request : CommonRequest =   CommonRequest()
        request.setUserLocationId(userLocationID)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    private fun getForestDataByLocation(userLocationID: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request  =   getForestByLocationRequest(userLocationID)
                val call_api: Call<List<SupplierDatum>> =
                    apiInterface.getForestDataByLocation(request)
                call_api.enqueue(object :
                        Callback<List<SupplierDatum>> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                            call: Call<List<SupplierDatum>>,
                            response: Response<List<SupplierDatum>>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: List<SupplierDatum> =
                                    response.body()!!
                            if (responce != null) {
                                forestList?.clear()
                                forestList?.addAll(responce)

                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                            call: Call<List<SupplierDatum>>,
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
        }
    }

    fun showNoNoDataView() {
        mView.rvDeclareListing.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
        mView.txtNoDataFound.visibility = View.VISIBLE
    }

    fun showNoInternetView() {
        mView.rvDeclareListing.visibility = View.GONE
        mView.txtNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.VISIBLE
    }

    fun showContentView() {
        mView.rvDeclareListing.visibility = View.VISIBLE
        mView.txtNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
    }


    private fun showMonthDialog() {
        val inflater =
            mView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val alertLayout: View = inflater.inflate(R.layout.dialog_month_layout, null)
        val alert: AlertDialog.Builder =
           AlertDialog.Builder(mView.context, R.style.CustomDialog)
        alert.setView(alertLayout)
        alert.setCancelable(false)
        alertLayout.ivCancel.setOnClickListener{
            monthDialog?.dismiss()
            view?.txtDate?.text = mView.context.resources.getString(R.string.date_)
            bordereauDate = "00"
            view?.txtDate?.requestFocus()
            callingDeclarationList()
        }

        val monthList = setupMonth()
        val mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        alertLayout.recycMonth.layoutManager = mLinearLayoutManager
        val adapter = MonthAdapter(mView.context, monthList)
        alertLayout.recycMonth.adapter = adapter

        adapter.onMonthClick = { month, position ->
            monthDialog?.dismiss()
           mView.txtDate?.text = month
            bordereauDate = mUtils.convertNumberIntoDecimalFormat(position+1)
            callingDeclarationList()
        }
        monthDialog = alert.create()
        monthDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        monthDialog?.show()
    }

    fun setupMonth():ArrayList<String>{
   var  months =  ArrayList<String>()
    for (i in 0..11) {
        val cal = Calendar.getInstance()
        val month_date =  SimpleDateFormat("MMMM")
        cal.set(Calendar.MONTH, i)
        val month_name = month_date.format(cal.getTime())
        months.add(month_name)
    }
        return  months
}


}