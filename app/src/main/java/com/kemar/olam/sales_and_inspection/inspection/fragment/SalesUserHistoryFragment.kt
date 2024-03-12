package com.kemar.olam.sales_and_inspection.inspection.fragment

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.BoderueDeleteLogReq
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.loading_wagons.fragment.LoadingWagonsHeaderFragment
import com.kemar.olam.loading_wagons.fragment.LoadingWagonsLogsListingFragment
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.sales_and_inspection.inspection.adapter.SalesHistoryAdapter
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_sales_user_history.view.*
import kotlinx.android.synthetic.main.fragment_sales_user_history.view.linContent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.util.Base64
import kotlin.collections.ArrayList


class SalesUserHistoryFragment : Fragment() ,View.OnClickListener , DialogFragment.GetDialogListener {

    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var rvLogsUserHistory: RecyclerView
    lateinit var adapter: SalesHistoryAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsTodaysHitoryLiting: ArrayList<LogsUserHistoryRes.BordereauRecordList?> = arrayListOf()


    //create local master based on list
    var isWagonSelected : Boolean = false
    var wagonLocalMaster : ArrayList<SupplierDatum> = arrayListOf()
    var isBorrdereuSelected : Boolean = false
    var bordereuLocalMaster   : ArrayList<SupplierDatum> = arrayListOf()
    var isDialogShowing: Boolean = false
    lateinit var commonDialogFragment: DialogFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_sales_user_history, container, false)
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
                mView.txtfilterWagon.text = mView.resources.getString(R.string.select)
                mView.txtFilterBordereuNo.text = mView.resources.getString(R.string.select)
            //callingSalesInspectionUserHistory()
            (parentFragment as MainContainerFragment).getSalesUserHistory(SharedPref.getUserId(Constants.user_id).toString())

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
        mView.ivAddLoadingBodreu.setOnClickListener(this)
        mView.btnRetry.setOnClickListener(this)
        mView.txtApprovalList.setOnClickListener(this)

        mView.txtfilterWagon.setOnClickListener(this)
        mView.txtFilterBordereuNo.setOnClickListener(this)
    }

    /*fun callingSalesInspectionUserHistory() {
        getSalesUserHistory(SharedPref.getUserId(Constants.user_id).toString())
    }
*/

    fun setForestNLogValuesToAdapter(){
        //if only forest selected
        if(mView.txtfilterWagon.text.toString().equals("select",ignoreCase = true) && !mView.txtFilterBordereuNo.text.toString().equals("select",ignoreCase = true)){
            adapter.setLogAndForestValues("",mView.txtFilterBordereuNo.text.toString())
        }
        //if only log no selected
        else if(!mView.txtfilterWagon.text.toString().equals("select",ignoreCase = true) && mView.txtFilterBordereuNo.text.toString().equals("select",ignoreCase = true)){
            adapter.setLogAndForestValues(mView.txtfilterWagon.text.toString(),"")
        }
        //if forest & logNo not selected
        else{
            adapter.setLogAndForestValues("","")
        }
        adapter.getFilter()?.filter("")
    }

    fun setupRecyclerViewNAdapter() {
        logsTodaysHitoryLiting.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvLogsUserHistory.layoutManager = mLinearLayoutManager
        adapter = SalesHistoryAdapter(mView.context, logsTodaysHitoryLiting,SharedPref.read(Constants.user_role).toString())
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
                        val fragment = InnspectionHeaderFragment()
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
                        val fragment = SalesLogDetailsFragment()
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
                funShowAlertForNoInspection(position)
            }
        }

        adapter.onHeaderClick = { modelData, position ->
            if (modelData.loadingStatus.equals("Draft", ignoreCase = true)!!) {
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


    fun funShowAlertForNoInspection(position: Int){
        val alert: AlertDialog =
            android.app.AlertDialog.Builder(view?.context, R.style.CustomDialogWithBackground)
                //.setTitle(activity?.getString(R.string.app_name))
                .setMessage(mView.context.resources.getString(R.string.are_you_sure_you_want_to_add_data_for_no_inspection))
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()

                        //Noo Inspection api call
                        callingNoInspectionAPI(position)
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

    fun generateNoInspectionRequest(position: Int): BoderueDeleteLogReq {

        var request : BoderueDeleteLogReq =   BoderueDeleteLogReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(logsTodaysHitoryLiting[position]?.bordereauHeaderId)
        return  request

    }


    private fun callingNoInspectionAPI(position:Int) {
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

    fun printPDF(pdfPath: String) {
        val printManager =
            mView.context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        /*val printAdapter =
            PdfDocumentAdapter(this@PrintActivity, "/storage/emulated/0/Download/test.pdf")*/
        val printAdapter =
            PdfDocumentAdapter(mView.context, pdfPath)
        printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
    }



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
                                        }else{
                                            mUtils.showAlert(
                                                activity,
                                                resources.getString(R.string.pdf_not_found))
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




    fun setupUserHistoryDataForSalesInspection(historyDataList : ArrayList<LogsUserHistoryRes.BordereauRecordList?>,isInternetAvialable:Boolean,wagonDataList:ArrayList<SupplierDatum>,bordereuNoDataList:ArrayList<SupplierDatum>){
        if(!isInternetAvialable){
            showNoInternetView()
        }else{
            showContentView()
            logsTodaysHitoryLiting.clear()
            wagonLocalMaster.clear()
            bordereuLocalMaster.clear()
            wagonLocalMaster.addAll(wagonDataList)
            bordereuLocalMaster.addAll(bordereuNoDataList)
            logsTodaysHitoryLiting.addAll(historyDataList)
            if (logsTodaysHitoryLiting.size == 0) {
                showNoNoDataView()
            } else {
                showContentView()
            }
            //when refresh list will remain as last filrter hence will set list with this method
            adapter.setValueToParentList(logsTodaysHitoryLiting)

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

   /* private fun getSalesUserHistory(userID: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            showContentView()
            try {
               //var  isAdmin : Boolean = false
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

              *//*  when(SharedPref.read(Constants.user_role).toString()){
                    "SuperUserApp","Super User","admin"->{
                        isAdmin = false
                    }
                    else->{
                        isAdmin = false
                    }
                }*//*

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
                                responce.getBordereauRecordList()?.let {
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
*/

    fun showNoInternetView() {
        try {
            mView.linContent.visibility = View.GONE
            mView.linApprovalList.visibility = View.GONE
            mView.relvNoDataFound.visibility = View.GONE
            mView.relvNoInternet.visibility = View.VISIBLE
        }catch (e : Exception){
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

    //when click on other search dialog
    fun makeIsSelectedForestNLogNoFalse(){
        isWagonSelected = false
        isBorrdereuSelected = false
    }

    override fun onClick(view: View?) {
        when (requireView().id) {
            R.id.ivAddLoadingBodreu -> {
                /*var fragment = LoadingWagonsHeaderFragment()*/
                val fragment = LoadingWagonsHeaderFragment()
                val bundle = Bundle()
                bundle.putString(
                    Constants.action,
                    Constants.action_non_edit
                );
                fragment.arguments = bundle
                (activity as HomeActivity).replaceFragment(fragment, false)
            }
            R.id.txtApprovalList -> {
                val fragment = ApprovalListFragment()
                (activity as HomeActivity).replaceFragment(fragment, false)
            }

            R.id.btnRetry -> {
              /*  getSalesUserHistory(SharedPref.getUserId(Constants.user_id).toString())*/
                (parentFragment as MainContainerFragment).getSalesUserHistory(SharedPref.getUserId(Constants.user_id).toString())
            }

            R.id.txtfilterWagon ->{
                makeIsSelectedForestNLogNoFalse()
                isWagonSelected = true
                showDialog(wagonLocalMaster as java.util.ArrayList<SupplierDatum?>?, "wagon_no")
            }
            R.id.txtFilterBordereuNo ->{
                makeIsSelectedForestNLogNoFalse()
                isBorrdereuSelected = true
                showDialog(bordereuLocalMaster as java.util.ArrayList<SupplierDatum?>?, "bordereu_no")
            }
        }
    }


    //Dialog content
    open fun showDialog(countryListSearch: java.util.ArrayList<SupplierDatum?>?, action: String) {

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
        } catch (e: Exception) {
        }

        val fm: FragmentManager
        val bundle: Bundle
        fm = childFragmentManager
        commonDialogFragment = DialogFragment(AddHeaderFragment@ this)
        bundle = Bundle()
        bundle.putSerializable("COUNTRY_LIST", countryListSearch)
        commonDialogFragment.setArguments(bundle)
        bundle.putBoolean("isCountryCode", false)
        bundle.putString("action", action)
        commonDialogFragment.show(fm, "COUNTRY_FRAGMENT")
        commonDialogFragment.isCancelable = false
        isDialogShowing = true
    }

    override fun onSubmitData(model: SupplierDatum?, isCountryCode: Boolean, action: String) {

        try {
            commonDialogFragment.dismiss()
            isDialogShowing = false
            // mView.txtBordero_No.clearFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (model != null) {
            try {
                when (action) {
                    "wagon_no"->{
                        mView.txtfilterWagon.text = model.optionName
                        mView.txtFilterBordereuNo.text = mView.context.resources.getString(R.string.select)
                        setForestNLogValuesToAdapter()

                    }

                    "bordereu_no"->{
                        mView.txtFilterBordereuNo.text = model.optionName
                        mView.txtfilterWagon.text = mView.context.resources.getString(R.string.select)
                        setForestNLogValuesToAdapter()
                    }

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCancleDialog() {
        try {
            commonDialogFragment.dismiss()
            isDialogShowing = false
            clearFilterForForestNLogNo()
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    fun clearFilterForForestNLogNo(){
        if(isWagonSelected){
            mView.txtfilterWagon.text =     mView.resources.getString(R.string.select)
        }
        if(isBorrdereuSelected){
            mView.txtFilterBordereuNo.text = mView.resources.getString(R.string.select)
        }

        setForestNLogValuesToAdapter()

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
                    file  =  mUtils.writeDataIntoFileAndSavePDF(mView,"Invoice",bmScreenShot)
                }
                /*  val bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                  file = bitmapImage?.let { saveBitmapIntoStorageNew(it) }*/
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return file
        }


    }


   /* inner class  SaveBitmapToInternalStorageAsyncTask internal constructor():
        AsyncTask<String?, String?, File?>() {

        override fun onPreExecute() {
            super.onPreExecute()
            mUtils.showProgressDialog(mView.context)
        }

        override fun onPostExecute(file: File?) {
            super.onPostExecute(file)
            mUtils.dismissProgressDialog()
            if (file != null) {
                try {
                    printPDF(file.path)
                } catch (t: Throwable) {
                }
            }
        }



        override fun doInBackground(vararg p0: String?): File? {
            var file: File? = null
            val compressFile: File? = null
            try {

                val bmScreenShot = p0[0]
                val imageBytes = Base64.decode(bmScreenShot, 0)
                val bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                file = bitmapImage?.let { saveBitmapIntoStorageNew(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return file
        }


    }*/

    private fun saveBitmapIntoStorageNew(bmp: Bitmap): File? {
        var f: File? = null
        try {
            val bytes = ByteArrayOutputStream()
           // bmp.compress(Bitmap.CompressFormat., 80, bytes)
            val d = Date()
            val s =
                DateFormat.format("MM-dd-yy hh-mm-ss", d.time)

            val cw =  ContextWrapper(mView.context)
            val directory = cw.getDir("OLAM", Context.MODE_PRIVATE)
            // Create imageDir
            f = File(directory, "Invoice" + s +".pdf")

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


}

