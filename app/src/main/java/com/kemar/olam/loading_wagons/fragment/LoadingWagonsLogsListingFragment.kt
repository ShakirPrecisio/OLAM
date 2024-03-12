package com.kemar.olam.loading_wagons.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.itextpdf.text.pdf.PdfWriter
import com.kemar.olam.R
import com.kemar.olam.bluetooth_printer.inter.ScannerInterface
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.request.BoderueDeleteLogReq
import com.kemar.olam.bordereau.model.request.ValidateBodereueNoReq
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.BarcodeScanActivity
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.loading_wagons.adapter.LodingWa_LogsListAdapter
import com.kemar.olam.loading_wagons.adapter.MultiLogsListAdapter
import com.kemar.olam.loading_wagons.model.request.LoadingRequest
import com.kemar.olam.loading_wagons.model.request.WagonLogRequest
import com.kemar.olam.loading_wagons.model.responce.GetLogDataByBarcodeRes
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.offlineData.response.LogDetail
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.*
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.dialog_add_log_layout.view.*
import kotlinx.android.synthetic.main.dialog_add_log_layout.view.edt_log
import kotlinx.android.synthetic.main.dialog_add_log_layout.view.edt_log2
import kotlinx.android.synthetic.main.dialog_add_log_layout.view.ivLogCancel
import kotlinx.android.synthetic.main.dialog_add_log_layout.view.linLogNumber
import kotlinx.android.synthetic.main.dialog_add_log_layout.view.linLogNumber2
import kotlinx.android.synthetic.main.dialog_multi_log_no_layout.view.*
import kotlinx.android.synthetic.main.dialog_search_by_log_no_layout.view.*
import kotlinx.android.synthetic.main.fragment__add_header.view.*
import kotlinx.android.synthetic.main.fragment_loading_wagons_logs_listing.view.*
import kotlinx.android.synthetic.main.layout_logs_listing.view.ivBOAdd
import kotlinx.android.synthetic.main.layout_logs_listing.view.ivHeaderEdit
import kotlinx.android.synthetic.main.layout_logs_listing.view.rvLogListing
import kotlinx.android.synthetic.main.layout_logs_listing.view.swipeLogListing
import kotlinx.android.synthetic.main.layout_logs_listing.view.tvNoDataFound
import kotlinx.android.synthetic.main.layout_logs_listing.view.txtBO
import kotlinx.android.synthetic.main.layout_logs_listing.view.txtBO_NO
import kotlinx.android.synthetic.main.layout_logs_listing.view.txtForestWagonNo
import kotlinx.android.synthetic.main.layout_logs_listing.view.txtSave
import kotlinx.android.synthetic.main.layout_logs_listing.view.txtSubmit
import kotlinx.android.synthetic.main.layout_logs_listing.view.txtTotalLogs
import kotlinx.android.synthetic.main.layout_logs_listing.view.txtTrasnporterTtile
import kotlinx.android.synthetic.main.row_print_reciept.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class LoadingWagonsLogsListingFragment : Fragment(),View.OnClickListener, DialogFragment.GetDialogListener {

    // TODO: Rename and change types of parameters
    private val SPLASH_TIMEOUT = 400
    lateinit var mView: View
    var  FULL_PATH  : String = ""
    var logDialog: AlertDialog? = null
    lateinit var rvLogListing: RecyclerView
    lateinit var adapter: LodingWa_LogsListAdapter
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
    var headerModel  : AddBodereuRes.BordereauResponse =  AddBodereuRes.BordereauResponse()
    var todaysHistoryModel  : LogsUserHistoryRes.BordereauRecordList =  LogsUserHistoryRes.BordereauRecordList()

    var scanner: ScannerInterface? = null


    lateinit var  wagonLogRequestlist: RealmResults<WagonLogRequest>

    var wagonlogList: ArrayList<WagonLogRequest> = arrayListOf()


    //Scan feature
    var RESULT_CODE = 103;
    val LOG_TAG = "DataCapture1"
    private val bRequestSendResult = false
    lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        realm = RealmHelper.getRealmInstance()
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_loading_wagons_logs_listing, container, false)
        initViews()
        CreateProfile()
        setDecoderValues()
        return mView
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun initViews() {
        commigFrom =
            arguments?.getString(Constants.comming_from).toString();


        //  (activity as HomeActivity).showActionBar()
        rvLogListing = mView.findViewById(R.id.rvLogListing)
        mUtils = Utility()
        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()



        mView.swipeLogListing.setOnRefreshListener {
            mView.swipeLogListing.isRefreshing = false
            /* if(!commigFrom.equals( Constants.header, ignoreCase = true)){
                 getBodereuLogsByID(bodereuHeaderId.toString())
             }*/
            //callingLogMasterAPI(suplierID)

            callingLogMasterAPI(suplierID.toString(), originID.toString())

        }

        //when comming from headerScreen & User History Screen
        //suplierID == forest
        //
        try{
        if (commigFrom.equals( Constants.header, ignoreCase = true)) {
            var headerDataModel: AddBodereuRes.BordereauResponse? =
                arguments?.getSerializable(Constants.badereuModel) as AddBodereuRes.BordereauResponse
            if (headerDataModel != null) {
                headerModel = headerDataModel
            }
            supplierLocationName = headerDataModel?.supplierName.toString()
            bodereuHeaderId = headerDataModel?.bordereauHeaderId!!

            try{
                forestUniqueId= headerDataModel?.unqiueId!!
                Log.e("forestUniqueId", "is " + forestUniqueId)
            }catch (e: Exception){
                e.printStackTrace()
            }


//            suplierID = headerDataModel?.supplier.toString()
//            originID = headerDataModel?.originID
//            originName = headerDataModel?.originName.toString()
//            supplierShortName = headerDataModel?.supplierShortName
            transporterName = headerDataModel?.transporterName
//            fscOrNonFsc = headerDataModel?.fscName?.toString().toString()
            headerDataModel?.modeOfTransport?.let { setupTransportMode(it, mView.context) }
            if (headerDataModel?.bordereauNo?.toString().isNullOrEmpty()) {
                mView.linManual.visibility = View.GONE
                bodereuNumber = headerDataModel?.eBordereauNo!!
                mView.txtElecTronicBO_NO.text = headerDataModel?.eBordereauNo?.toString().toString()
            } else {
                mView.linManual.visibility = View.VISIBLE
                bodereuNumber = headerDataModel?.eBordereauNo!!
                mView.txtElecTronicBO_NO.text = headerDataModel?.eBordereauNo?.toString().toString()
                mView.txtBO_NO.text = headerDataModel?.bordereauNo?.toString().toString()

            }
            mView.txtForestWagonNo.text = headerDataModel?.wagonNo?.toString().toString()
            mView.txtBO.text = headerDataModel?.bordereauRecordNo?.toString().toString()

            //setTotalNoOfLogs(logsLiting?.size)
            /* headerDataModel?.supplier?.toString()?.let { callingForestMasterAPI(it) }*/

            if (mUtils.checkInternetConnection(mView.context)) {
                callingLogMasterAPI(suplierID.toString(), originID.toString())
            }


            //getBodereuLogsByID(bodereuHeaderId.toString())

            if (!mUtils.checkInternetConnection(mView.context)) {
                realm.executeTransaction {
                    wagonLogRequestlist= realm.where(WagonLogRequest::class.java).equalTo("forestuniqueId", forestUniqueId,Case.SENSITIVE).findAll()
                    Log.e("wagonlogList", "is " + wagonlogList.size)
                    logsLiting.clear()
                    for (wagon in wagonLogRequestlist){
                        var bordereuLoglist=BodereuLogListing()
                        bordereuLoglist.setBarcodeNumber(wagon.getBarcodeNumber())
                        bordereuLoglist.setQualityId(wagon.getQualityId())
                        bordereuLoglist.setQuality(wagon.getQuality())
                        bordereuLoglist.setLogRecordDocNo(wagon.getLogRecordDocNo())
                        bordereuLoglist.setLogNo(wagon.getLogNo())
                        bordereuLoglist.setLogNo2(wagon.getLogNo2())
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
                        bordereuLoglist.setLogSpeciesName(wagon.getLogSpeciesName())
                        bordereuLoglist.setLogSpecies(wagon.getLogSpecies())
                        bordereuLoglist.setPlaqNo(wagon.getPlaqNo())
                        logsLiting.add(bordereuLoglist)
                    }
                    adapter.notifyDataSetChanged()
                    setTotalNoOfLogs(logsLiting.size)
                }
            }else{
                getBodereuLogsByID(bodereuHeaderId.toString())
            }


        } else {
            var headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
                arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
            if (headerDataModel != null) {
                todaysHistoryModel = headerDataModel
            }
            /* headerDataModel?.supplierId?.toString()?.let { callingForestMasterAPI(it) }*/
            bodereuHeaderId = headerDataModel?.bordereauHeaderId!!

            try{
                forestUniqueId= headerDataModel?.uniqueId!!
                Log.e("forestUniqueId", "is " + forestUniqueId)
            }catch (e: Exception){
                e.printStackTrace()
            }

            supplierLocationName = headerDataModel?.supplierName.toString()
            originID = headerDataModel?.originId
            originName = headerDataModel?.originName.toString()
            supplierShortName = headerDataModel?.supplierShortName
            suplierID = headerDataModel?.supplierId.toString()
            transporterName = headerDataModel?.transporterName
            fscOrNonFsc = headerDataModel?.fscName?.toString().toString()
            headerDataModel?.transportMode?.let { setupTransportMode(it, mView.context) }
            if (headerDataModel?.bordereauNo?.toString().isNullOrEmpty()) {
                mView.linManual.visibility = View.GONE
                if (!headerDataModel?.eBordereauNo.isNullOrEmpty()) {
                    bodereuNumber = headerDataModel?.eBordereauNo
                    mView.txtElecTronicBO_NO.text = headerDataModel?.eBordereauNo
                }
            } else {
                mView.linManual.visibility = View.VISIBLE
                if (!headerDataModel?.eBordereauNo.isNullOrEmpty()) {
                    bodereuNumber = headerDataModel?.eBordereauNo!!
                    mView.txtElecTronicBO_NO.text = headerDataModel?.eBordereauNo?.toString().toString()
                }

                mView.txtBO_NO.text = headerDataModel?.bordereauNo?.toString().toString()

            }
            mView.txtForestWagonNo.text = headerDataModel?.wagonNo?.toString().toString()
            mView.txtBO.text = headerDataModel?.recordDocNo?.toString().toString()


            if (mUtils.checkInternetConnection(mView.context)) {
                callingLogMasterAPI(suplierID.toString(), originID.toString())
            }

            if (!mUtils.checkInternetConnection(mView.context)) {
                realm.executeTransaction {
                    wagonLogRequestlist= realm.where(WagonLogRequest::class.java).equalTo("forestuniqueId", forestUniqueId).findAll()
                    Log.e("wagonlogList", "is " + wagonLogRequestlist.size)
                    logsLiting.clear()
                    for (wagon in wagonLogRequestlist){
                        var bordereuLoglist=BodereuLogListing()
                        bordereuLoglist.setBarcodeNumber(wagon.getBarcodeNumber())
                        bordereuLoglist.setQualityId(wagon.getQualityId())
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
            }else{

                getBodereuLogsByID(bodereuHeaderId.toString())

            }
        }

    }catch (e:Exception){
            e.printStackTrace()
        }
    }


    fun setupTransportMode(transportMode:Int, context: Context) {
        when (transportMode) {
            1 -> {
               mView.txtTrasnporterTtile.text = context.resources.getString(R.string._wagon_no)
            }
            17 -> {
               mView.txtTrasnporterTtile.text = context.resources.getString(R.string._barge_no)
            }
            3 -> {
               mView.txtTrasnporterTtile.text = context.resources.getString(R.string._truck_no)
            }
            else -> {
            }
        }
    }

    fun setupClickListner() {
       mView.ivBOAdd.setOnClickListener(this)
       mView.txtSave.setOnClickListener(this)
       mView.txtSubmit.setOnClickListener(this)
       mView.ivHeaderEdit.setOnClickListener(this)
       mView.txtScan.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setupRecyclerViewNAdapter() {
        logsLiting?.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        rvLogListing.layoutManager = mLinearLayoutManager
        adapter = LodingWa_LogsListAdapter(mView.context, logsLiting)
        rvLogListing.adapter = adapter
        //Expand Collapse Action
        adapter.onMoreClick = { modelData, position, isExpanded ->
            if (isExpanded) {
                logsLiting?.get(position)?.isExpanded = true
            } else {
                logsLiting?.get(position)?.isExpanded = false
            }
            adapter.notifyDataSetChanged()
        }

        /*adapter.onEditClick = { modelData, position ->
            showAlerDialog(true,position,"Are you sure want to update log?","update")
        }*/

        adapter.onDeleteClick = { modelData, position ->
            showAlerDialog(false,position,"Do you want to remove Log ?","delete")
        }

        /*adapter.onPrintClick = { modelData, position ->
            showAlerDialog(false,position,"Are you sure want to print log?","print")
        }*/
    }


    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.loading_wagons)
        (activity as HomeActivity).invisibleFilter()
    }


    private fun setTotalNoOfLogs(count: Int) {
        if (count == 0) {
            mView.linLoadingFooter.visibility =  View.INVISIBLE
            mView.tvNoDataFound.visibility=View.VISIBLE
            mView.rvLogListing.visibility=View.GONE
            mView.txtTotalLogs.visibility =View.INVISIBLE
            mView.txtTotalLogs.text =getString(R.string.total_found,count) //"Total $count Found"
        } else {
            mView.linLoadingFooter.visibility =  View.VISIBLE
            mView.tvNoDataFound.visibility=View.GONE
            mView.rvLogListing.visibility=View.VISIBLE
            mView.txtTotalLogs.visibility =View.VISIBLE
            mView.txtTotalLogs.text = getString(R.string.total_found,count)//"Total $count Found"
        }

    }

    fun deleteLogsEntry(position: Int) {
        logsLiting?.removeAt(position)
        adapter.notifyDataSetChanged()
        mUtils.showToast(activity, mView.resources.getString(R.string.log_deleted_successfully))
        setTotalNoOfLogs(logsLiting?.size)
    }

    private fun showMultiLogDialog( dataList: List<BodereuLogListing>) {
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
            logDialog?.dismiss()
        }

        alertLayout.headLabel.setText("Log Number"+ dataList.get(0).getLogNo())

        var multiLogsListAdapter= MultiLogsListAdapter(requireContext(), dataList)

        multiLogsListAdapter.onItemClick = { modelData ->

            if (checkLogAACNLogNoAlreadyExits(
                            modelData.getAACName(),
                            modelData.getLogNo().toString()
                )
            ) {
                if (logsLiting?.size != 20) {
                    modelData.setDetailId("0")
                    logsLiting.add(modelData)
                    adapter.notifyDataSetChanged()
                    setTotalNoOfLogs(logsLiting?.size)
                    logDialog?.dismiss()


                    if (!mUtils.checkInternetConnection(mView.context)) {
                        realm.executeTransactionAsync({ bgRealm ->
                        var wagonLogRequest=WagonLogRequest()
                        wagonLogRequest.uniqueId=System.currentTimeMillis().toString()
                        wagonLogRequest.forestuniqueId=forestUniqueId
                        wagonLogRequest.setCbm(modelData.getCbm())
                        wagonLogRequest.setdiaType(modelData.getdiaType())
                        wagonLogRequest.setTotalCBM(modelData.getTotalCBM())
                        wagonLogRequest.setLogSpecies(modelData.getLogSpecies())
                        wagonLogRequest.setLogNo(modelData.getLogNo())
                        wagonLogRequest.setPlaqNo(modelData.getPlaqNo())
                        wagonLogRequest.setAACYear(modelData.getAACYear())
                        wagonLogRequest.setLongBdx(modelData.getLongBdx())
                        wagonLogRequest.setCbmQuantity(modelData.getCbmQuantity())
                        wagonLogRequest.setDiamBdx1(modelData.getDiamBdx1())
                        wagonLogRequest.setDiamBdx2(modelData.getDiamBdx2())
                        wagonLogRequest.setDiamBdx3(modelData.getDiamBdx3())
                        wagonLogRequest.setDiamBdx4(modelData.getDiamBdx4())
                        wagonLogRequest.setDiamBdx(modelData.getDiamBdx())
                        wagonLogRequest.setLogSpeciesName(modelData.getLogSpeciesName())

                        if(!modelData.getBordereauNo().isNullOrEmpty()) {
                            wagonLogRequest.setBarcodeNumber(modelData.getBordereauNo()!!)
                        }

                        wagonLogRequest.setBarcodeNumber(modelData.getBarcodeNumber())
                        wagonLogRequest.setMaterialDesc(modelData.getMaterialDesc())
                        wagonLogRequest.setDetailId("0")
                        wagonLogRequest.setSupplierName(modelData.getSupplierName())
                        wagonLogRequest.setSupplierShortName(modelData.getSupplierShortName())
                        wagonLogRequest.setSupplierName(modelData.getSupplierName())
                        wagonLogRequest.setLogRecordDocNo(modelData.getLogRecordDocNo())
                        wagonLogRequest.setQuality(modelData.getQuality())
                        wagonLogRequest.setQualityId(modelData.getQualityId())
                        wagonLogRequest.aacId= modelData.aacId
                        wagonLogRequest.setDetailId("0")
                        bgRealm.copyToRealmOrUpdate(wagonLogRequest)
                    }, {
                        Log.e("Success", "Success")

                            realm.executeTransactionAsync({ bgRealm ->

                                        var logdetail= bgRealm.where(LogDetail::class.java)
                                                .equalTo("barcodeNumber", modelData.getBarcodeNumber(),Case.SENSITIVE)
//                                                .equalTo("supplier",suplierID.toInt())
                                                .findFirst()
                                        logdetail!!.isUsed=true

                                        bgRealm.copyToRealmOrUpdate(logdetail)

                                        }, {
                                            Log.e("Success", "Success used logs")
                                        }) {
                                            Log.e("faile", "failed used logs")
                                        }


                    }) {
                        Log.e("faile", "faile")
                    }
                    }

                }
            } else {
                logDialog?.dismiss()
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
        logDialog = alert.create()
        logDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logDialog?.show()
    }

    private fun showSearcByLogDialog(position: Int) {
        val inflater =
            mView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val alertLayout: View = inflater.inflate(R.layout.dialog_search_by_log_no_layout, null)
        alertView = alertLayout
        val alert: android.app.AlertDialog.Builder =
            android.app.AlertDialog.Builder(mView.context, R.style.CustomDialog)
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout)
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false)


        alertLayout?.linSearchByLogNo.setOnClickListener {
            if(isValidateLogsNumber(alertLayout)) {
                logDialog?.dismiss()

                if (mUtils.checkInternetConnection(mView.context)) {
                    getLogDataByBarlogNumber(alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString())

                } else {
                    var isSingle=false
                    var logslisting: ArrayList<BodereuLogListing> = arrayListOf()
                    realm.executeTransactionAsync ({ bgRealm ->
                        var logDetail= bgRealm.where(LogDetail::class.java)
                                .equalTo("logNo", alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString(),Case.SENSITIVE)
//                                .equalTo("supplier",suplierID.toInt())
                                .equalTo("isUsed",false)
                                .equalTo("loadingStatus","Loaded")
                                .findAll()

                        Log.e("logDetail","is "+logDetail.size)

                        if(logDetail!!.size==1 && logDetail?.size>0) {
                            if (checkLogAACNLogNoAlreadyExits(
                                            logDetail!!.get(0)?.aacName,
                                            logDetail!!.get(0)?.logNo.toString()
                                    )
                            ) {
                                if (logsLiting?.size != 20) {

                                    var borderLogListing=BodereuLogListing()
                                    borderLogListing.setCbm(logDetail.get(0)!!.cbm)
                                    borderLogListing.setdiaType(logDetail.get(0)!!.diaType)
                                    borderLogListing.setTotalCBM(logDetail.get(0)!!.totalCBM)
                                    borderLogListing.setLogSpecies(logDetail.get(0)!!.logSpecies)
                                    borderLogListing.setLogSpeciesName(logDetail.get(0)!!.logSpeciesName)
                                    borderLogListing.setLogNo(logDetail.get(0)!!.logNo)
                                    borderLogListing.setPlaqNo(logDetail.get(0)!!.plaqNo)
                                    borderLogListing.setAACName(logDetail.get(0)!!.aacName)
                                    borderLogListing.setAACYear(logDetail.get(0)!!.aacYear)
                                    borderLogListing.setLongBdx(logDetail.get(0)!!.longBdx)
                                    borderLogListing.setCbmQuantity(logDetail.get(0)!!.cbmQuantity)
                                    borderLogListing.setDiamBdx1(logDetail.get(0)!!.diam1Bdx)
                                    borderLogListing.setDiamBdx2(logDetail.get(0)!!.diam2Bdx)
                                    borderLogListing.setDiamBdx3(logDetail.get(0)!!.diam3Bdx)
                                    borderLogListing.setDiamBdx4(logDetail.get(0)!!.diam4Bdx)
                                    borderLogListing.setDiamBdx(logDetail.get(0)!!.diamBdx)
                                    if(!logDetail.get(0)!!.bordereauNo.isNullOrEmpty()) {
                                        borderLogListing.setBordereauNo(logDetail.get(0)!!.bordereauNo!!)
                                    }

                                    if(!logDetail.get(0)!!.barcodeNumber.isNullOrEmpty()) {
                                        borderLogListing.setBarcodeNumber(logDetail.get(0)!!.barcodeNumber!!)
                                    }

                                    if(!logDetail.get(0)!!.plaqNo.isNullOrEmpty()) {
                                        borderLogListing.setPlaqNo(logDetail.get(0)!!.plaqNo!!)
                                    }

                                    if(!logDetail.get(0)!!.plaqNo.isNullOrEmpty()) {
                                        borderLogListing.setPlaqNo(logDetail.get(0)!!.plaqNo!!)
                                    }

                                    borderLogListing.setMaterialDesc(logDetail.get(0)!!.materialDesc)
                                    borderLogListing.setDetailId("0")
                                    borderLogListing.setSupplierName(logDetail.get(0)!!.supplierName)
                                    borderLogListing.setSupplierShortName(logDetail.get(0)!!.supplierShortName)
                                    borderLogListing.setSupplierName(logDetail.get(0)!!.supplierName)
                                    borderLogListing.setLogRecordDocNo(logDetail.get(0)!!.logRecordDocNo)
                                    borderLogListing.setQuality(logDetail.get(0)!!.quality)
                                    borderLogListing.setQualityId(logDetail.get(0)!!.qualityId)
                                    borderLogListing.aacId= logDetail.get(0)!!.aacId
                                    borderLogListing.setDetailId("0")

                                    borderLogListing.let { logsLiting?.add(it) }
                                    isSingle=true

                                    var logdetail= bgRealm.where(LogDetail::class.java)
                                            .equalTo("logNo", alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString(),Case.SENSITIVE)
//                                            .equalTo("supplier",suplierID.toInt())
                                            .equalTo("isUsed",false)
                                            .findFirst()
                                    logdetail!!.isUsed=true

                                    bgRealm.copyToRealmOrUpdate(logdetail)

                                    var wagonLogRequest=WagonLogRequest()
                                    wagonLogRequest.uniqueId=System.currentTimeMillis().toString()
                                    wagonLogRequest.forestuniqueId=forestUniqueId

                                    if(!logdetail.aacName.isNullOrEmpty())
                                    wagonLogRequest.aacName=logdetail.aacName

                                    wagonLogRequest.setdiaType(logdetail.diaType)
                                    wagonLogRequest.setCbm(logdetail.cbm)
                                    wagonLogRequest.setdiaType(logdetail.diaType)
                                    wagonLogRequest.setTotalCBM(logdetail.totalCBM)
                                    wagonLogRequest.setLogSpecies(logdetail.logSpecies)
                                    wagonLogRequest.setLogNo(logdetail.logNo)
                                    wagonLogRequest.setPlaqNo(logdetail.plaqNo)
                                    wagonLogRequest.setAACYear(logdetail.aacYear)
                                    wagonLogRequest.setLongBdx(logdetail.longBdx)
                                    wagonLogRequest.setCbmQuantity(logdetail.cbmQuantity)
                                    wagonLogRequest.setDiamBdx1(logdetail.diam1Bdx)
                                    wagonLogRequest.setDiamBdx2(logdetail.diam2Bdx)
                                    wagonLogRequest.setDiamBdx3(logdetail.diam3Bdx)
                                    wagonLogRequest.setDiamBdx4(logdetail.diam4Bdx)
                                    wagonLogRequest.setDiamBdx(logdetail.diamBdx)

                                    if(!logdetail.fscMode.isNullOrEmpty())
                                        wagonLogRequest.setFSCMode(logdetail.fscMode)

                                    if(!logdetail.wagonNo.isNullOrEmpty())
                                    wagonLogRequest.setWagonNo(logdetail.wagonNo)

                                    if(!logdetail.supplierName.isNullOrEmpty())
                                    wagonLogRequest.setSupplierName(logdetail.supplierName)


                                    if(!logdetail.supplierShortName.isNullOrEmpty())
                                        wagonLogRequest.setSupplierName(logdetail.supplierShortName)

                                    if(!logdetail.bordereauNo.isNullOrEmpty()) {
                                        wagonLogRequest.setBordereauNo(logdetail.bordereauNo!!)
                                    }

                                    if(!logdetail.barcodeNumber.isNullOrEmpty()) {
                                        wagonLogRequest.setBarcodeNumber(logdetail.barcodeNumber!!)
                                    }

                                    wagonLogRequest.setMaterialDesc(logdetail.materialDesc)
                                    wagonLogRequest.setDetailId("0")
                                    wagonLogRequest.setSupplierName(logdetail.supplierName)
                                    wagonLogRequest.setSupplierShortName(logdetail.supplierShortName)
                                    wagonLogRequest.setSupplierName(logdetail.supplierName)
                                    wagonLogRequest.setLogRecordDocNo(logdetail.logRecordDocNo)
                                    wagonLogRequest.setQuality(logdetail.quality)
                                    wagonLogRequest.setQualityId(logdetail.qualityId)
                                    wagonLogRequest.aacId= logdetail.aacId
                                    wagonLogRequest.setLogSpeciesName(logdetail.logSpeciesName)
                                    wagonLogRequest.setDetailId("0")
                                    bgRealm.copyToRealmOrUpdate(wagonLogRequest)


                                }
                            } else {
                                mUtils.showAlert(
                                        activity,
                                        resources.getString(R.string.duplicate_log_found)
                                )
                            }
                        }else if(logDetail.size>1){
                            for(i in logDetail.indices){
                                var borderLogListing=BodereuLogListing()
                                borderLogListing.setCbm(logDetail.get(i)?.cbm)
                                borderLogListing.setdiaType(logDetail.get(i)?.diaType)
                                borderLogListing.setTotalCBM(logDetail.get(i)?.totalCBM)
                                borderLogListing.setLogSpecies(logDetail.get(i)?.logSpecies)
                                borderLogListing.setLogNo(logDetail.get(i)?.logNo)
                                borderLogListing.setPlaqNo(logDetail.get(i)?.plaqNo)
                                borderLogListing.setAACYear(logDetail.get(i)?.aacYear)
                                borderLogListing.setLongBdx(logDetail.get(i)?.longBdx)
                                borderLogListing.setCbmQuantity(logDetail.get(i)?.cbmQuantity)
                                borderLogListing.setDiamBdx1(logDetail.get(i)?.diam1Bdx)
                                borderLogListing.setDiamBdx2(logDetail.get(i)?.diam2Bdx)
                                borderLogListing.setDiamBdx3(logDetail.get(i)?.diam3Bdx)
                                borderLogListing.setDiamBdx4(logDetail.get(i)?.diam4Bdx)
                                borderLogListing.setDiamBdx(logDetail.get(i)?.diamBdx)

                                if(!logDetail.get(i)!!.bordereauNo.isNullOrEmpty()) {
                                    borderLogListing.setBordereauNo(logDetail.get(i)!!.bordereauNo!!)
                                }

                                if(!logDetail.get(i)!!.barcodeNumber.isNullOrEmpty()) {
                                    borderLogListing.setBarcodeNumber(logDetail.get(i)!!.barcodeNumber!!)
                                }

                                borderLogListing.setMaterialDesc(logDetail.get(i)?.materialDesc)
                                borderLogListing.setDetailId("0")
                                borderLogListing.setSupplierName(logDetail.get(i)?.supplierName)
                                borderLogListing.setSupplierShortName(logDetail.get(i)?.supplierShortName)
                                borderLogListing.setLogSpeciesName(logDetail.get(i)!!.logSpeciesName)
                                borderLogListing.setSupplierName(logDetail.get(i)?.supplierName)
                                borderLogListing.setLogRecordDocNo(logDetail.get(i)?.logRecordDocNo)
                                borderLogListing.setQuality(logDetail.get(i)?.quality)
                                borderLogListing.setQualityId(logDetail.get(i)?.qualityId)
                                borderLogListing.aacId= logDetail.get(i)?.aacId
                                borderLogListing.aacId= logDetail.get(i)?.aacId
                                borderLogListing.aacId= logDetail.get(i)?.aacId
                                borderLogListing.setDetailId("0")

                                logslisting.add(borderLogListing)


                            }
                        }/*else
                        {

                            var borderauLog= bgRealm.where(BodereuLogListing::class.java)
                                .equalTo("logNo", alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString(),Case.SENSITIVE)
                                .equalTo("supplier",suplierID.toInt())
                                .equalTo("isUsed",false)
                                .findAll()

                                bgRealm.executeTransactionAsync({ bgRealm ->

                                }, {
                                    Log.e("Success", "Success logs")

                                    if(borderauLog!!.size==1 && borderauLog?.size>0) {
                                        if (    checkLogAACNLogNoAlreadyExits(
                                                borderauLog!!.get(0)?.aacName,
                                                borderauLog!!.get(0)?.logNo.toString()
                                            )
                                        ) {
                                            if (logsLiting?.size != 20) {

                                                var borderLogListing=BodereuLogListing()
                                                borderLogListing.setCbm(borderauLog.get(0)!!.cbm)
                                                borderLogListing.setdiaType(borderauLog.get(0)!!.diaType)
                                                borderLogListing.setTotalCBM(borderauLog.get(0)!!.totalCBM)
                                                borderLogListing.setLogSpecies(borderauLog.get(0)!!.logSpecies)
                                                borderLogListing.setLogNo(borderauLog.get(0)!!.logNo)
                                                borderLogListing.setPlaqNo(borderauLog.get(0)!!.plaqNo)
                                                borderLogListing.setAACName(logDetail.get(0)!!.aacName)
                                                borderLogListing.setAACYear(borderauLog.get(0)!!.aacYear)
                                                borderLogListing.setLongBdx(borderauLog.get(0)!!.longBdx)
                                                borderLogListing.setCbmQuantity(borderauLog.get(0)!!.cbmQuantity)
                                                borderLogListing.setDiamBdx1(borderauLog.get(0)!!.diam1Bdx)
                                                borderLogListing.setDiamBdx2(borderauLog.get(0)!!.diam2Bdx)
                                                borderLogListing.setDiamBdx3(borderauLog.get(0)!!.diam3Bdx)
                                                borderLogListing.setDiamBdx4(borderauLog.get(0)!!.diam4Bdx)
                                                borderLogListing.setDiamBdx(borderauLog.get(0)!!.diamBdx)
                                                if(!borderauLog.get(0)!!.bordereauNo.isNullOrEmpty()) {
                                                    borderLogListing.setBordereauNo(borderauLog.get(0)!!.bordereauNo!!)
                                                }

                                                if(!borderauLog.get(0)!!.barcodeNumber.isNullOrEmpty()) {
                                                    borderLogListing.setBarcodeNumber(borderauLog.get(0)!!.barcodeNumber!!)
                                                }

                                                if(!borderauLog.get(0)!!.plaqNo.isNullOrEmpty()) {
                                                    borderLogListing.setPlaqNo(borderauLog.get(0)!!.plaqNo!!)
                                                }

                                                if(!borderauLog.get(0)!!.plaqNo.isNullOrEmpty()) {
                                                    borderLogListing.setPlaqNo(borderauLog.get(0)!!.plaqNo!!)
                                                }

                                                borderLogListing.setMaterialDesc(borderauLog.get(0)!!.materialDesc)
                                                borderLogListing.setDetailId("0")
                                                borderLogListing.setSupplierName(borderauLog.get(0)!!.supplierName)
                                                borderLogListing.setSupplierShortName(borderauLog.get(0)!!.supplierShortName)
                                                borderLogListing.setSupplierName(borderauLog.get(0)!!.supplierName)
                                                borderLogListing.setLogRecordDocNo(borderauLog.get(0)!!.logRecordDocNo)
                                                borderLogListing.setQuality(borderauLog.get(0)!!.quality)
                                                borderLogListing.setQualityId(borderauLog.get(0)!!.qualityId)
                                                borderLogListing.aacId= borderauLog.get(0)!!.aacId
                                                borderLogListing.setDetailId("0")

                                                borderLogListing.let { logsLiting?.add(it) }
                                                isSingle=true

                                                var borderLog= bgRealm.where(BodereuLogListing::class.java)
                                                    .equalTo("logNo", alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString(),Case.SENSITIVE)
                                                    .equalTo("supplier",suplierID.toInt())
                                                    .equalTo("isUsed",false)
                                                    .findFirst()
                                                borderLog!!.isUsed=true

                                                bgRealm.copyToRealmOrUpdate(borderLog)

                                                var wagonLogRequest=WagonLogRequest()
                                                wagonLogRequest.uniqueId=System.currentTimeMillis().toString()
                                                wagonLogRequest.forestuniqueId=forestUniqueId

                                                if(!borderLog.aacName.isNullOrEmpty())
                                                    wagonLogRequest.aacName=borderLog.aacName

                                                wagonLogRequest.setdiaType(borderLog.diaType)
                                                wagonLogRequest.setCbm(borderLog.cbm)
                                                wagonLogRequest.setdiaType(borderLog.diaType)
                                                wagonLogRequest.setTotalCBM(borderLog.totalCBM)
                                                wagonLogRequest.setLogSpecies(borderLog.logSpecies)
                                                wagonLogRequest.setLogNo(borderLog.logNo)
                                                wagonLogRequest.setPlaqNo(borderLog.plaqNo)
                                                wagonLogRequest.setAACYear(borderLog.aacYear)
                                                wagonLogRequest.setLongBdx(borderLog.longBdx)
                                                wagonLogRequest.setCbmQuantity(borderLog.cbmQuantity)
                                                wagonLogRequest.setDiamBdx1(borderLog.diam1Bdx)
                                                wagonLogRequest.setDiamBdx2(borderLog.diam2Bdx)
                                                wagonLogRequest.setDiamBdx3(borderLog.diam3Bdx)
                                                wagonLogRequest.setDiamBdx4(borderLog.diam4Bdx)
                                                wagonLogRequest.setDiamBdx(borderLog.diamBdx)

                                                if(!borderLog.fscMode.isNullOrEmpty())
                                                    wagonLogRequest.setFSCMode(borderLog.fscMode)

                                                if(!borderLog.wagonNo.isNullOrEmpty())
                                                    wagonLogRequest.setWagonNo(borderLog.wagonNo)

                                                if(!borderLog.supplierName.isNullOrEmpty())
                                                    wagonLogRequest.setSupplierName(borderLog.supplierName)


                                                if(!borderLog.supplierShortName.isNullOrEmpty())
                                                    wagonLogRequest.setSupplierName(borderLog.supplierShortName)

                                                if(!borderLog.bordereauNo.isNullOrEmpty()) {
                                                    wagonLogRequest.setBordereauNo(borderLog.bordereauNo!!)
                                                }

                                                if(!borderLog.barcodeNumber.isNullOrEmpty()) {
                                                    wagonLogRequest.setBarcodeNumber(borderLog.barcodeNumber!!)
                                                }

                                                wagonLogRequest.setMaterialDesc(borderLog.materialDesc)
                                                wagonLogRequest.setDetailId("0")
                                                wagonLogRequest.setSupplierName(borderLog.supplierName)
                                                wagonLogRequest.setSupplierShortName(borderLog.supplierShortName)
                                                wagonLogRequest.setSupplierName(borderLog.supplierName)
                                                wagonLogRequest.setLogRecordDocNo(borderLog.logRecordDocNo)
                                                wagonLogRequest.setQuality(borderLog.quality)
                                                wagonLogRequest.setQualityId(borderLog.qualityId)
                                                wagonLogRequest.aacId= borderLog.aacId
                                                wagonLogRequest.setLogSpeciesName(borderLog.logSpeciesName)
                                                wagonLogRequest.setDetailId("0")
                                                bgRealm.copyToRealmOrUpdate(wagonLogRequest)


                                            }
                                        } else {
                                            mUtils.showAlert(
                                                activity,
                                                resources.getString(R.string.duplicate_log_found)
                                            )
                                        }
                                    }else if(borderLog.size>1){
                                        for(i in logDetail.indices){
                                            var borderLogListing=BodereuLogListing()
                                            borderLogListing.setCbm(logDetail.get(i)?.cbm)
                                            borderLogListing.setdiaType(logDetail.get(i)?.diaType)
                                            borderLogListing.setTotalCBM(logDetail.get(i)?.totalCBM)
                                            borderLogListing.setLogSpecies(logDetail.get(i)?.logSpecies)
                                            borderLogListing.setLogNo(logDetail.get(i)?.logNo)
                                            borderLogListing.setPlaqNo(logDetail.get(i)?.plaqNo)
                                            borderLogListing.setAACYear(logDetail.get(i)?.aacYear)
                                            borderLogListing.setLongBdx(logDetail.get(i)?.longBdx)
                                            borderLogListing.setCbmQuantity(logDetail.get(i)?.cbmQuantity)
                                            borderLogListing.setDiamBdx1(logDetail.get(i)?.diam1Bdx)
                                            borderLogListing.setDiamBdx2(logDetail.get(i)?.diam2Bdx)
                                            borderLogListing.setDiamBdx3(logDetail.get(i)?.diam3Bdx)
                                            borderLogListing.setDiamBdx4(logDetail.get(i)?.diam4Bdx)
                                            borderLogListing.setDiamBdx(logDetail.get(i)?.diamBdx)

                                            if(!logDetail.get(i)!!.bordereauNo.isNullOrEmpty()) {
                                                borderLogListing.setBordereauNo(logDetail.get(i)!!.bordereauNo!!)
                                            }

                                            if(!logDetail.get(i)!!.barcodeNumber.isNullOrEmpty()) {
                                                borderLogListing.setBarcodeNumber(logDetail.get(i)!!.barcodeNumber!!)
                                            }

                                            borderLogListing.setMaterialDesc(logDetail.get(i)?.materialDesc)
                                            borderLogListing.setDetailId("0")
                                            borderLogListing.setSupplierName(logDetail.get(i)?.supplierName)
                                            borderLogListing.setSupplierShortName(logDetail.get(i)?.supplierShortName)
                                            borderLogListing.setSupplierName(logDetail.get(i)?.supplierName)
                                            borderLogListing.setLogRecordDocNo(logDetail.get(i)?.logRecordDocNo)
                                            borderLogListing.setQuality(logDetail.get(i)?.quality)
                                            borderLogListing.setQualityId(logDetail.get(i)?.qualityId)
                                            borderLogListing.aacId= logDetail.get(i)?.aacId
                                            borderLogListing.aacId= logDetail.get(i)?.aacId
                                            borderLogListing.aacId= logDetail.get(i)?.aacId
                                            borderLogListing.setDetailId("0")

                                            logslisting.add(borderLogListing)


                                        }
                                    }
                                }) {
                                    Log.e("faile", "failed logs")
                                }

                        }*/
                    }, {
                        if(isSingle) {
                            adapter.notifyDataSetChanged()
                            setTotalNoOfLogs(logsLiting?.size)
                        }else  if(logslisting.size>1) {
                            showMultiLogDialog(logslisting)
                        }else{
                            mUtils.showAlert(
                                    activity,
                                    "Log not found "
                            )
                        }
                        Log.e("success","success wagon logslisting")

                        realm.executeTransactionAsync({ bgRealm ->
                            var loadingRequest = bgRealm.where(LoadingRequest::class.java).equalTo("uniqueId", forestUniqueId).findFirst()
                            var logcount = loadingRequest?.logCount
                            loadingRequest!!.logCount = logcount!!+1
                            bgRealm.copyToRealmOrUpdate(loadingRequest)
                        }, {
                            Log.e("Success", "Success logs count update")
                        }) {
                            Log.e("failed", "failed count add logs")
                        }
                    }) {
                        Log.e("failed","failed to update logslisting")
                    }
                }
            }
        }


        alertLayout.ivLogCancel.setOnClickListener{
            logDialog?.dismiss()
        }


        logDialog = alert.create()
        logDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logDialog?.show()
    }


  /*  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showAddLogDialog(isEditLog: Boolean, position: Int) {
        val inflater =
            mView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val alertLayout: View = inflater.inflate(R.layout.dialog_add_log_layout, null)
        alertView = alertLayout
        val alert: android.app.AlertDialog.Builder =
            android.app.AlertDialog.Builder(mView.context, R.style.CustomDialog)
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout)
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false)

        if (isEditLog) {
            setEditDataOnAddLogDiolog(alertLayout, position)
        }
        alertLayout?.linConfirmNPrint.setOnClickListener {
            acessRuntimPermission(isEditLog,position,alertLayout)
        }


        alertLayout.edtDia?.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus)  try {
                var tempLong = 0.0
                if(!alertLayout.edtLong?.text.toString().isNullOrEmpty()){
                    tempLong =  alertLayout.edtLong?.text.toString().toInt().toDouble()
                    if (tempLong.roundToInt()<alertLayout.edtDia.text.toString().toInt().toDouble().roundToInt()) {
                        mUtils.showAlert(activity, mView.resources.getString(R.string.dia_smaller_than_long))
                    }
                }
            } catch (e: Exception) {
                e.toString()
            }
        }


        alertLayout.edtDia?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

            }


            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                try {
                    var tempLong = 0.0
                    if(!alertLayout.edtLong?.text.toString().isNullOrEmpty()){
                        tempLong =  alertLayout.edtLong?.text.toString().toInt().toDouble()
                    }

                    if (250 < alertLayout.edtDia.text.toString().toDouble().roundToInt()) {
                        mUtils.showAlert(activity, mView.resources.getString(R.string.maximum_dia_value))

                    }*//*else if (tempLong.roundToInt()<alertLayout.edtDia.text.toString().toInt().toDouble().roundToInt()) {
                             mUtils.showAlert(activity, "Dia value should be smaller than Long")
                         }*//*
                    else {
                        val result =
                            (tempLong) / 100 * (s.toString().toInt().toDouble()) / 100 * (s.toString().toInt().toDouble()) / 100 * 0.7854
                        val df = DecimalFormat("###.###");
                        val finalResult = df.format(result)
                        alertLayout.edtCBM.setText(finalResult.toString())

                    }
                } catch (e: Exception) {
                    e.toString()
                }
            }
        })


        alertLayout.edtLong?.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                try {
                    var tempDia = 0.0
                    if (!alertLayout.edtDia?.text.toString().isNullOrEmpty()) {
                        tempDia = alertLayout.edtDia?.text.toString().toInt().toDouble()
                        if (alertLayout.edtLong.text.toString().toDouble().roundToInt() < tempDia.roundToInt()) {
                            mUtils.showAlert(activity, mView.resources.getString(R.string.long_value_greater_than_dia))
                        }
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

        }



        alertLayout.edtLong?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

            }


            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                var tempDia = 0.0
                if(!alertLayout.edtDia?.text.toString().isNullOrEmpty()){
                    tempDia =  alertLayout.edtDia?.text.toString().toInt().toDouble()
                }
                try {
                    if (2000 < alertLayout.edtLong.text.toString().toDouble().roundToInt()) {
                        mUtils.showAlert(activity, mView.resources.getString(R.string.maximum_long_value))
                    }*//*else if (alertLayout.edtLong.text.toString().toDouble().roundToInt()<tempDia.roundToInt()) {
                         mUtils.showAlert(activity, "Long value should be greater than diameter")
                     }*//*
                    else {
                        val result =
                            (s.toString().toInt().toDouble()) / 100 * (tempDia) / 100 * (tempDia) / 100 * 0.7854
                        val df = DecimalFormat("###.###");
                        val finalResult = df.format(result)
                        alertLayout.edtCBM.setText(finalResult.toString())
                    }
                }catch (e:Exception){
                    e.toString()
                }
            }
        })

       *//* alertLayout?.edtDia?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

            }


            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                try {
                    var tempLong = 0.0
                    if(!alertLayout?.edtLong?.text.toString().isNullOrEmpty()){
                        tempLong =  alertLayout?.edtLong?.text.toString().toInt().toDouble()
                    }


                    if (250 < alertLayout.edtDia.text.toString().toDouble().roundToInt()) {
                        mUtils.showAlert(activity, "Maximum allowed dia value is 250")

                    }else if (tempLong.roundToInt()<alertLayout.edtDia.text.toString().toInt().toDouble().roundToInt()) {
                        mUtils.showAlert(activity, "Dia value should be smaller than Long")
                    }
                    else {
                        var result =
                            (tempLong.toDouble()) / 100 * (s.toString().toInt().toDouble()) / 100 * (s.toString().toInt().toDouble()) / 100 * 0.7854
                        val df = DecimalFormat("###.###");
                        var finalResult = df.format(result)
                        alertLayout?.edtCBM.setText(finalResult.toString())
                    }

                } catch (e: Exception) {
                    e.toString()
                }
            }
        })

        alertLayout?.edtLong?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

            }


            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                var tempDia = 0.0
                if(!alertLayout?.edtDia?.text.toString().isNullOrEmpty()){
                    tempDia =  alertLayout?.edtDia?.text.toString().toInt().toDouble()
                }
                try {
                    if (2000 < alertLayout.edtLong.text.toString().toDouble().roundToInt()) {
                        mUtils.showAlert(activity, "Maximum allowed Long value is 2000")
                    }else if (alertLayout.edtLong.text.toString().toDouble().roundToInt()<tempDia.roundToInt()) {
                        mUtils.showAlert(activity, "Long value should be greater than diameter")
                    }
                    else {
                        var result =
                            (s.toString().toInt().toDouble()) / 100 * (tempDia) / 100 * (tempDia) / 100 * 0.7854
                        val df = DecimalFormat("###.###");
                        var finalResult = df.format(result)
                        alertLayout?.edtCBM.setText(finalResult.toString())
                    }
                }catch (e:Exception){
                    e.toString()
                }
            }
        })*//*
        alertLayout?.edtEssence.setOnClickListener{
            showDialog(commonForestMaster?.getSpecies() as java.util.ArrayList<SupplierDatum?>?,"essence")
        }

        alertLayout?.edtAAC.setOnClickListener{
            showDialog(commonForestMaster?.getAacList() as java.util.ArrayList<SupplierDatum?>?,"aac")
        }
        alertLayout?.edtQuality.setOnClickListener{
            showDialog(commonForestMaster?.getQualityData() as java.util.ArrayList<SupplierDatum?>?,"quality")
        }

        alertLayout.ivLogCancel.setOnClickListener{
            logDialog?.dismiss()
        }


        logDialog = alert.create()
        logDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logDialog?.show()
    }*/

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setEditDataOnAddLogDiolog(alertLayout: View, position: Int) {
        if(logsLiting?.get(position)?.getLogNo()?.contains("/")!!){
         val tokens =  StringTokenizer(logsLiting?.get(position)?.getLogNo(), "/");
          val first = tokens?.nextToken()
          val second = tokens?.nextToken()
            alertLayout.edt_log.setText(first)
            alertLayout.edt_log2.setText(second)
        }else {
            alertLayout.edt_log.setText(logsLiting?.get(position)?.getLogNo())
        }

        disableLogNonEditableFiled(alertLayout)

        alertLayout.edtPlaque_no.setText(logsLiting?.get(position)?.getPlaqNo())
        alertLayout.edtEssence.setText(logsLiting?.get(position)?.getLogSpeciesName())
        speciesID = logsLiting?.get(position)?.getLogSpecies()!!
        aacID =  logsLiting?.get(position)?.getAAC()!!
        qualityId= logsLiting?.get(position)?.getQualityId()!!
        aacYear =  logsLiting.get(position).getAACYear()!!
        alertLayout.edtAAC.setText(logsLiting?.get(position)?.getAACName())
        alertLayout.edtQuality.setText(logsLiting?.get(position)?.getQuality().toString())
        alertLayout.edtDia.setText(logsLiting?.get(position)?.getDiamBdx().toString())
        alertLayout.edtLong.setText(logsLiting?.get(position)?.getLongBdx().toString())
        alertLayout.edtCBM.setText((logsLiting?.get(position)?.getCbm().toString()))
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun disableLogNonEditableFiled(alertLayout: View){
        alertLayout.edt_log.isEnabled = false
        alertLayout.edt_log2.isEnabled = false
        alertLayout.edtAAC.isEnabled = false
        alertLayout.edt_log.backgroundTintList =
            mView.context.resources.getColorStateList(R.color.gray_200)
        alertLayout.edt_log2.backgroundTintList =
            mView.context.resources.getColorStateList(R.color.gray_200)
        alertLayout.edtAAC.backgroundTintList =
            mView.context.resources.getColorStateList(R.color.gray_200)
        alertLayout?.linLogNumber.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout?.linLogNumber2.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout?.linLogAAC.setBackgroundResource(R.drawable.bg_for_editext_non_editable)

    }

    fun acessRuntimPermission(isEditLog: Boolean, position: Int,alertLayout : View ) {
        val permissionListener: PermissionListener = object : PermissionListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionGranted() {
                var isLogAlreadyExit : Boolean = false
                var LogModel : BodereuLogListing = BodereuLogListing();
                if (isValidateAddLog(alertLayout)) {
                    if (isEditLog) {
                        logsLiting?.get(position)?.setLogNo(alertLayout.edt_log.text.toString()+"/"+alertLayout.edt_log2.text.toString())
                        logsLiting?.get(position)?.setPlaqNo(alertLayout.edtPlaque_no.text.toString())
                        logsLiting?.get(position)?.setLogSpeciesName(alertLayout.edtEssence.text.toString())
                        logsLiting?.get(position)?.setAACName(alertLayout.edtAAC.text.toString())
                        logsLiting?.get(position)?.setAAC(aacID)
                        logsLiting.get(position).setAACYear(aacYear)
                        logsLiting?.get(position)?.setLogSpecies(speciesID)
                        logsLiting?.get(position)?.setQualityId(qualityId)
                        logsLiting?.get(position)?.setLogRecordDocNo(mUtils.createLogRecordDocsNumber(bodereuNumber,alertLayout.edt_log.text.toString()+"/"+alertLayout.edt_log2.text.toString(),alertLayout.edtDia.text.toString()))
                        logsLiting?.get(position)?.setQuality(alertLayout.edtQuality.text?.toString())

                        logsLiting.get(position).setDiamBdx1(alertLayout.edtDia.text?.toString()?.toInt())
                        logsLiting.get(position).setDiamBdx2(alertLayout.edtDia2.text?.toString()?.toInt())
                        logsLiting.get(position).setDiamBdx3(alertLayout.edtDia3.text?.toString()?.toInt())
                        logsLiting.get(position).setDiamBdx4(alertLayout.edtDia4.text?.toString()?.toInt())

                        logsLiting.get(position).setDiamBdx(Math.round(alertLayout.edtAvgDia.text?.toString()?.toDouble()!!).toInt())


                        /* logsLiting?.get(position)?.setDetailId("")*/
                        logsLiting?.get(position)?.setLongBdx(alertLayout.edtLong?.text?.toString()?.toInt())
                        logsLiting?.get(position)?.setCbm(alertLayout.edtCBM.text?.toString()?.toDouble())
                        logsLiting?.get(position)?.setBarcodeNumber(mUtils.getRandomNumberString(""))
                        LogModel = logsLiting?.get(position)

                    } else {
                        var request: BodereuLogListing = BodereuLogListing()
                        request.setLogNo(alertLayout.edt_log.text.toString()+"/"+alertLayout.edt_log2.text.toString())
                        request.setPlaqNo(alertLayout.edtPlaque_no.text.toString())
                        request.setLogSpeciesName(alertLayout.edtEssence.text.toString())
                        request.setLogSpecies(speciesID)
                        request.setAACName(alertLayout.edtAAC.text.toString())
                        request.setAAC(aacID)
                        request.setAACYear(aacYear)
                        request.setQualityId(qualityId)
                        request.setLogRecordDocNo(mUtils.createLogRecordDocsNumber(bodereuNumber,alertLayout.edt_log.text.toString()+"/"+alertLayout.edt_log2.text.toString(),alertLayout.edtDia.text.toString()))
                        request.setQuality(alertLayout.edtQuality?.text?.toString())

                        request.setDiamBdx1(alertLayout.edtDia.text?.toString()?.toInt())
                        request.setDiamBdx2(alertLayout.edtDia2.text?.toString()?.toInt())
                        request.setDiamBdx3(alertLayout.edtDia3.text?.toString()?.toInt())
                        request.setDiamBdx4(alertLayout.edtDia4.text?.toString()?.toInt())
                        request.setDiamBdx(Math.round(alertLayout.edtAvgDia.text?.toString()?.toDouble()!!).toInt())

                        request.setLongBdx(alertLayout.edtLong.text?.toString()?.toInt())
                        request.setDetailId("0")
                        request.setCbm(alertLayout.edtCBM.text?.toString()?.toDouble())
                        // request.setCbm(mUtils.intToDouble(alertLayout.edtCBM.text?.toString()?.toInt()))
                        request.setBarcodeNumber(mUtils.getRandomNumberString(""))

                        //Check condition for log is already exits
                        if(checkLogAACNLogNoAlreadyExits(alertLayout.edtAAC.text.toString(),alertLayout.edt_log.text.toString()+"/"+alertLayout.edt_log2.text.toString())) {
                            logsLiting?.add(request)
                            LogModel = request
                            setTotalNoOfLogs(logsLiting?.size)
                            isLogAlreadyExit = false
                        }else{
                            isLogAlreadyExit = true
                            mUtils.showAlert(
                                activity,
                                resources.getString(R.string.duplicate_log_found)
                            )
                        }
                    }
                    adapter.notifyDataSetChanged()
                    logDialog?.dismiss()
                    if(isLogAlreadyExit==false) {
                        buttonGenerate_onClick(LogModel)
                    }
                }

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
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE*/
            )
            .check()
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
    fun isValidateAddLog(alertLayout: View): Boolean {
        if (alertLayout.edtAAC?.text.toString()==getResources().getString(R.string.select)) {
            mUtils.showAlert(activity, resources.getString(R.string.please_select_aac))
            return false
        }
        else if (alertLayout.edt_log.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, resources.getString(R.string.Please_enter_log_no))
            return false
        }
        else if (alertLayout.edt_log2.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, resources.getString(R.string.please_enter_valid_log_no))
            alertLayout.edt_log2.requestFocus()
            return false
        }
        /*else if (alertLayout.edtPlaque_no.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, "Please enter plaque no")
            return false
        }*/ else if (alertLayout.edtEssence?.text.toString()==getResources().getString(R.string.select)) {
            mUtils.showAlert(activity, resources.getString(R.string.please_select_essesnce))
            return false
        } else if (alertLayout.edtDia.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, resources.getString(R.string.please_enter_dia))
            return false
        } else if (250 < alertLayout.edtDia.text.toString().toDouble().roundToInt()) {
            mUtils.showAlert(activity, resources.getString(R.string.maximum_dia_value))
            return false
        } else if (alertLayout.edtLong.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, resources.getString(R.string.please_enter_long))
            return false
        } else if (2000 < alertLayout.edtLong.text.toString().toDouble().roundToInt()) {
            mUtils.showAlert(activity, resources.getString(R.string.maximum_long_value))
            return false
        } else if (alertLayout.edtLong.text.toString().toDouble().roundToInt()<alertLayout.edtDia.text.toString().toDouble().roundToInt()) {
            mUtils.showAlert(activity, resources.getString(R.string.long_value_greater_than_dia))
            return false
        } else if (alertLayout.edtCBM.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, resources.getString(R.string.please_enter_cbm))
            return false
        }else if (alertLayout.edtQuality.text.toString()==getResources().getString(R.string.select)) {
            mUtils.showAlert(activity, resources.getString(R.string.please_select_quality))
            return false
        }
        return true;
    }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun showAlerDialog(isEditLog: Boolean, position: Int, msg:String, action:String){
        var alert: AlertDialog =
            android.app.AlertDialog.Builder(view?.context, R.style.CustomDialogWithBackground)
                //.setTitle(activity?.getString(R.string.app_name))
                .setMessage(msg)
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        when(action){
                            "delete"->{
                                if (mUtils?.checkInternetConnection(mView.context) == true) {
                                    if (logsLiting?.get(position)?.getDetailId().equals("0")) {
                                        deleteLogsEntry(position)
                                    } else {
                                        callingdeleteBordereauLogAPI(position)
                                    }
                                }else{
                                    deleteOfflineLog(position)
                                }
                                //deleteLogsEntry(position)
                            }
                            "update"->{
                                //showAddLogDialog(isEditLog, position)
                            }
                            "print"->{
                                buttonGenerate_onClick(logsLiting?.get(position))
                            }
                            "right"->{

                            }
                        }
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


    fun deleteOfflineLog(position: Int){
        var isSingle=false
            realm.executeTransactionAsync ({ bgRealm ->

                var wagonLogReques= bgRealm.where(WagonLogRequest::class.java)
                    .equalTo("barcodeNumber", logsLiting.get(position).getBarcodeNumber(),Case.SENSITIVE)
                    .findFirst()
                wagonLogReques?.deleteFromRealm()

            }, {



                realm.executeTransactionAsync({ bgRealm ->

                    var loadingRequest = bgRealm.where(LoadingRequest::class.java).equalTo("uniqueId", forestUniqueId).findFirst()
                    var logcount = loadingRequest?.logCount
                    loadingRequest!!.logCount = logcount!!-1
                    bgRealm.copyToRealmOrUpdate(loadingRequest)

                    var logDetail= bgRealm.where(LogDetail::class.java)
                        .equalTo("barcodeNumber", logsLiting.get(position).getBarcodeNumber(),Case.SENSITIVE)
                        .findFirst()
                    logDetail!!.isUsed=false

                    bgRealm.copyToRealmOrUpdate(logDetail)

                    Log.e("logDetail","is "+ logDetail!!.barcodeNumber)

                }, {
                    logsLiting.remove(logsLiting.get(position))
                    adapter.notifyDataSetChanged()
                    Log.e("Success", "Success logs count update")

                }) {
                    Log.e("failed", "failed count add logs")
                }




            }) {
                Log.e("faile","faile to update logslisting")
            }
    }



    fun generateValidateLogRecorNumberRequest(logRecordDocNo:String): ValidateBodereueNoReq {

        var request : ValidateBodereueNoReq =   ValidateBodereueNoReq()
        request.setLogRecordDocNo(logRecordDocNo)
        request.setBordereauHeaderId(bodereuHeaderId.toString())


        var json =  Gson().toJson(request)
        var test  = json

        return  request

    }



    fun generateAddBodereuLogListRequest(action:String): AddBoereuLogListingReq {

        var request : AddBoereuLogListingReq =   AddBoereuLogListingReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.userLocationID = SharedPref.readInt(Constants.user_location_id)
        request.setBordereauHeaderId(bodereuHeaderId)
        request.setTimezoneId("Asia/Kolkata")
        for (model in logsLiting) {
            if(action=="Save"){
                model.setMode("Save")
            }else{
                model.setMode("Submit")
            }
            model.setMaterialDesc(0)
            /* model.setDetailId("")*/
        }
        request.setBordereauLogList(logsLiting)
        var json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    fun generateDeleteBodereuLogRequest(position: Int): BoderueDeleteLogReq {

        var request : BoderueDeleteLogReq =   BoderueDeleteLogReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(bodereuHeaderId)
        request.setDeleteDetailId(logsLiting?.get(position)?.getDetailId()?.toInt())

        return  request

    }

    fun getLogMasterRequest(forestId: String, originID: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setForestId(forestId)
        request.setOriginId(originID)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    private fun callingLogMasterAPI(forestId:String,originID:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val request  = getLogMasterRequest(forestId, originID)
                val call_api: Call<GetForestDataRes> =
                    apiInterface.getLogsMaster(request)

                call_api.enqueue(object :
                    Callback<GetForestDataRes> {
                    override fun onResponse(
                        call: Call<GetForestDataRes>,
                        response: Response<GetForestDataRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response.body().getSeverity() == 200) {
                                val responce: GetForestDataRes =
                                    response.body()!!
                                if (responce != null) {

                                        commonForestMaster = responce
                                    }
                                }else if (response.body()?.getSeverity() == 306) {
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
    private fun callingValidateBordereauNoAPI(logRecorNumber:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateValidateLogRecorNumberRequest(logRecorNumber)
                val call: Call<AddBodereuRes> =
                    apiInterface.validateLogRecorNumber(request)
                call.enqueue(object :
                    Callback<AddBodereuRes> {
                    override fun onResponse(
                        call: Call<AddBodereuRes>,
                        response: Response<AddBodereuRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response != null) {
                                    if (response.body().getSeverity() == 200) {


                                    } else {
                                        mUtils.showAlert(activity, response.body().getMessage())
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
                        call: Call<AddBodereuRes>,
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


    private fun callingAddBordereauFoWagonsAPI(action:String) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateAddBodereuLogListRequest(action)
                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.addBordereauLogsForLoading(request)
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
                                        if(action.equals("Submit", ignoreCase = true)){
                                            var fragment  = LoadingWagonsUserHistoryFragment()
                                            (activity as HomeActivity).replaceFragment(fragment,true)
                                        }else{

                                            getBodereuLogsByID(bodereuHeaderId.toString())
                                        }
                                    }  else if (response.body()?.getSeverity() == 306) {
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


    private fun callingdeleteBordereauLogAPI(position:Int) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateDeleteBodereuLogRequest(position)
                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.deleteBordereauLog(request)
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
                                        deleteLogsEntry(position)
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



    fun checkLogAACNLogNoAlreadyExits(aacName:String?,logNumber:String):Boolean{
        for((index,listdata)  in logsLiting.withIndex()){
            if(!aacName.isNullOrEmpty() && !logNumber.isNullOrEmpty())
                if (listdata.getLogNo().toString().contains(logNumber) && listdata.getAACName().toString().contains(aacName!!)) {
                  return false
                }
        }
        return true
    }

    fun getLogDataByBarlogNumberRequest(logNumber:String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setLogNo(logNumber)
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setForestId(suplierID)
        //request.setloadingStatus("Loaded")
        val json =  Gson().toJson(request)
        var test  = json
        return  request
    }

    private fun getLogDataByBarlogNumber(logNumber:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val  request  = getLogDataByBarlogNumberRequest(logNumber)
                val call_api: Call<GetLogDataByBarcodeRes> = apiInterface.getLogDataByLogNoForLoadingWagons(request)
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
                                        if(responce.getLogDetails()!!.size==1) {
                                            if (    checkLogAACNLogNoAlreadyExits(
                                                    responce.getLogDetails()!!.get(0)?.getAACName(),
                                                            responce.getLogDetails()!!.get(0)?.getLogNo().toString()
                                                )
                                            ) {
                                                if (logsLiting?.size != 20) {
                                                    responce.getLogDetails()!!.get(0)?.setDetailId("0")
                                                    responce.getLogDetails()!!.get(0)?.let { logsLiting?.add(it) }
                                                    adapter.notifyDataSetChanged()
                                                    setTotalNoOfLogs(logsLiting?.size)
                                                }
                                            } else {
                                                mUtils.showAlert(
                                                    activity,
                                                    resources.getString(R.string.duplicate_log_found)
                                                )
                                            }
                                        }else{
                                            showMultiLogDialog(responce.getLogDetails()!!)
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
                            }  else{
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


    fun getDeliveryBodereuLogsByDeliveryIDRequest(barcodeNumber:String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setBarcodeNumber(barcodeNumber)
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setForestId(suplierID)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    private fun getLogDataByBarCode(barcodeNumber:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
val request  =  getDeliveryBodereuLogsByDeliveryIDRequest(barcodeNumber)
                val call_api: Call<GetLogDataByBarcodeRes> =
                    apiInterface.getLogDataByBarCode(request)
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
                                                responce.getLogDetail()?.setDetailId("0")
                                                responce.getLogDetail()?.let { logsLiting?.add(it) }
                                                adapter.notifyDataSetChanged()
                                                setTotalNoOfLogs(logsLiting?.size)
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


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun buttonGenerate_onClick(logModel:BodereuLogListing) {
        try {
            if (!fscOrNonFsc.isNullOrEmpty()){
                if(fscOrNonFsc.equals("NO FSC",ignoreCase = true)){
                    mView.chkBCNoFSC.visibility  = View.VISIBLE
                    mView.chkBCNoFSCClear.visibility  = View.GONE
                    mView.chkFSC.visibility  = View.GONE
                    mView.chkFSCClear.visibility  = View.VISIBLE

                }else{
                    mView.chkBCNoFSC.visibility  = View.GONE
                    mView.chkBCNoFSCClear.visibility  = View.VISIBLE
                    mView.chkFSC.visibility  = View.VISIBLE
                    mView.chkFSCClear.visibility  = View.GONE
                }
            }

            val productId = logModel.getBarcodeNumber()
            //Geenerate Barcoode with product ID
            val bitmap = mUtils.generatebarcodeBitmap(productId)
            mView.ivBarcode.setImageBitmap(bitmap)
            mView.txtBardCodeId.text  = logModel.getBarcodeNumber().toString()
            mView.txtBCLog.text  =  logModel.getLogNo().toString()
            mView.txtBCAAC.text = logModel.getAACName().toString()
            mView.txtbcDia.text = "D : "+logModel.getDiamBdx().toString()
            mView.txtBCLong.text = "L : "+logModel.getLongBdx().toString()
            mView.txtBCCBM.text = logModel.getCbm().toString()
            mView.txtBCForest.text = supplierShortName
            var barcodeValue  = originName+"/"+logModel.getAACName().toString()+aacYear
            mView.txtBCTransporter.text=barcodeValue
            mView.txtBCOrigin.text = supplierLocationName
            mView.txtBCEssense.text= logModel.getLogSpeciesName()
            mView.txtBCDate.text = mUtils?.getCurrentDate()

            Handler().postDelayed({
               // savePdf()
                savePdfNew()
            },SPLASH_TIMEOUT.toLong())

        } catch (e: Exception) {
            Toast.makeText(view?.context, e.message, Toast.LENGTH_LONG).show()
        }
    }


    private fun savePdfNew() {
        //create object of Document class
        val mDoc = Document()
        //pdf file name
        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        //pdf file path

        val cw =  ContextWrapper(mView.context)
        val directory = cw.getDir("OLAM", Context.MODE_PRIVATE)
        if (directory.exists()) {
            directory.deleteRecursively()
        }
        if (!directory.exists()) {
            directory.mkdir()
        }
        // Create imageDir
        val mypath= File(directory, bodereuNumber+mFileName +".pdf")

        //val mFilePath = Environment.getExternalStorageDirectory().toString() + "/" + mFileName +".pdf"
        FULL_PATH = mypath.path
        try {
            //create instance of PdfWriter class
            PdfWriter.getInstance(mDoc, FileOutputStream(FULL_PATH))

            //open the document for writing
            mDoc.open()

            //get text from EditText i.e. textEt
            val mText ="Test PDF"

            mView.parentView.setDrawingCacheEnabled(true);
            mView.parentView.buildDrawingCache();
            val bitmap = mView.parentView.getDrawingCache();
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageInByte: ByteArray = baos.toByteArray()
            val image = Image.getInstance(imageInByte);

            //add author of the document (metadata)
            mDoc.addAuthor("OLAM")

            val scaler = ((mDoc.getPageSize().getWidth() - mDoc.leftMargin()
                    - mDoc.rightMargin() - 0) / image.getWidth()) * 100;
            image.scalePercent(scaler);
            image.alignment = Image.ALIGN_CENTER/* | Image.ALIGN_TOP);*/

            //add paragraph to the document
            //  mDoc.add(Paragraph(mText))
            //add Image to the document
            mDoc.add(image)
            //close document
            mDoc.close()

            //show file saved message with file name and path
            Toast.makeText(mView.context, "$mFileName.pdf\nis saved to\n$FULL_PATH", Toast.LENGTH_SHORT).show()
            printPDF(FULL_PATH)
        }
        catch (e: Exception){
            //if Stringthing goes wrong causing exception, get and show exception message
            Toast.makeText(mView.context, e.message, Toast.LENGTH_SHORT).show()
        }
    }


    /*@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun savePdf() {
        //create object of Document class
        val mDoc = Document()
        //pdf file name
        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())
        //pdf file path
        val mFilePath = Environment.getExternalStorageDirectory().toString() + "/" + mFileName +".pdf"
        FULL_PATH = mFilePath
        try {
            //create instance of PdfWriter class
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))

            //open the document for writing
            mDoc.open()

            //get text from EditText i.e. textEt
            val mText ="Test PDF"

            mView.parentView.setDrawingCacheEnabled(true);
            mView.parentView.buildDrawingCache();
            var bitmap = mView.parentView.getDrawingCache();
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageInByte: ByteArray = baos.toByteArray()
            var  image = Image.getInstance(imageInByte);

            //add author of the document (metadata)
            mDoc.addAuthor("OLAM")

            var  scaler = ((mDoc.getPageSize().getWidth() - mDoc.leftMargin()
                    - mDoc.rightMargin() - 0) / image.getWidth()) * 100;
            image.scalePercent(scaler);
            image.setAlignment(Image.ALIGN_CENTER)*//* | Image.ALIGN_TOP);*//*

            //add paragraph to the document
            //  mDoc.add(Paragraph(mText))
            //add Image to the document
            mDoc.add(image)
            //close document
            mDoc.close()

            //show file saved message with file name and path
            Toast.makeText(mView.context, "$mFileName.pdf\nis saved to\n$mFilePath", Toast.LENGTH_SHORT).show()
            printPDF(FULL_PATH)
        }
        catch (e: Exception){
            //if Stringthing goes wrong causing exception, get and show exception message
            Toast.makeText(mView.context, e.message, Toast.LENGTH_SHORT).show()
        }
    }*/

    fun printPDF(pdfPath:String){
        val printManager =
            mView.context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        /*val printAdapter =
            PdfDocumentAdapter(this@PrintActivity, "/storage/emulated/0/Download/test.pdf")*/
        val printAdapter =
            PdfDocumentAdapter(mView.context, pdfPath)
        printManager.print("Document",printAdapter, PrintAttributes.Builder().build())
    }

    fun  checkIsFragmentDialogAlreadyShowing():Boolean{
        if(isDialogShowing){
            return false
        }
        return true
    }

    //Dialog content
    open fun showDialog(countryListSearch:java.util.ArrayList<SupplierDatum?>?, action:String) {

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
        countryDialogFragment = DialogFragment(AddHeaderFragment@this)
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
            mView.txtBordero_No.clearFocus()
        }catch (e:Exception){
            e.printStackTrace()
        }
        if (model != null) {
            try{
                when (action) {

                    "essence"->{
                        alertView?.edtEssence?.setText( model.optionName)
                        speciesID  = model.optionValue!!
                        alertView?.edtDia?.requestFocus()
                    }
                    "aac"->{
                        alertView?.edtAAC?.setText( model.optionName)
                        aacID  = model.optionValue!!
                        aacYear = model.optionValueString!!
                        alertView?.edt_log?.requestFocus()
                    }
                    "quality"->{
                        alertView?.edtQuality?.setText( model.optionName)
                        qualityId  = model.optionValue!!
                        alertView?.edtQuality?.requestFocus()
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

    fun acessRuntimPermissionForCamera() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                singleScan()
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivBOAdd -> {
                //user should be add only 30 logs
                if(logsLiting?.size!=20) {
                   // showAddLogDialog(false, 0)
                    showSearcByLogDialog(0)
                }
            }

            R.id.txtSave -> {
                if(logsLiting?.size!=0) {
                    callingAddBordereauFoWagonsAPI("Save")
                }
            }

            R.id.txtSubmit -> {
                if(logsLiting?.size!=0) {

                    if (mUtils?.checkInternetConnection(mView.context) == true) {

                        callingAddBordereauFoWagonsAPI(Constants.SUBMIT)
                    }else{

                        realm.executeTransactionAsync({ bgRealm ->
                            var loadingRequest = bgRealm.where(LoadingRequest::class.java).equalTo("uniqueId", forestUniqueId).findFirst()
                            loadingRequest!!.action=Constants.SUBMIT
                            bgRealm.copyToRealmOrUpdate(loadingRequest)
                        }, {
                            mUtils.showAlert(requireActivity(),getString(R.string.loading_wagon_submit_successful))
                            requireActivity().onBackPressed()
                            Log.e("Success","Success")
                        }) {
                            Log.e("failed","failed")
                        }
                    }
                }
            }

            R.id.txtScan -> {
                acessRuntimPermissionForCamera()
            }

            R.id.ivHeaderEdit ->{
                if(commigFrom.equals( Constants.header, ignoreCase = true)) {
                    var fragment = LoadingWagonsHeaderFragment()
                    val bundle = Bundle()
                    bundle.putSerializable(
                        Constants.badereuModel,
                        headerModel
                    );
                    bundle.putString(
                        Constants.action,
                        Constants.action_edit
                    );
                    bundle.putString(
                        Constants.comming_from,
                        Constants.header
                    );
                    fragment.arguments = bundle
                    (activity as HomeActivity).replaceFragment(fragment,false)

                }else{
                    var fragment = LoadingWagonsHeaderFragment()
                    val bundle = Bundle()
                    bundle.putSerializable(
                        Constants.badereuModel,
                        todaysHistoryModel
                    );
                    bundle.putString(
                        Constants.action,
                        Constants.action_edit
                    );
                    bundle.putString(
                        Constants.comming_from,
                        Constants.todays_history
                    );
                    fragment.arguments = bundle
                    (activity as HomeActivity).replaceFragment(fragment,false)
                }
            }
        }
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


    private fun initScanner() {
        scanner = ScannerInterface(mView.context)
        scanner!!.open()
        scanner!!.enablePlayBeep(true);
        //		scanner.enableFailurePlayBeep(false);
        scanner!!.enablePlayVibrate(true);
        //		scanner.enablShowAPPIcon(false);
        //		scanner.enablShowNoticeIcon(false);
        //		scanner.enableAddKeyValue(1);
        //		scanner.timeOutSet(2);
        //		scanner.intervalSet(10);
        //		scanner.lightSet(false);
        //		scanner.enablePower(true);
        //		scanner.addSuffix("BBB");
        //		scanner.interceptTrimleft(2);
        //		scanner.interceptTrimright(3);
        //		scanner.filterCharacter("R");
        //		scanner.SetErrorBroadCast(true);
        //		scanner.lockScanKey();
        scanner!!.setOutputMode(1)
        //		scanner.resultScan();
    }

    fun singleScan() {
        try {
            if (scanner != null)
                scanner!!.scan_start()
        }catch (e : Exception){
            e.printStackTrace()
        }
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
        initScanner()
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
        finishScanner()
        unRegisterScannerStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun finishScanner() {
        try {
            if (scanner != null) {
                scanner!!.scan_stop()
                scanner!!.continuousScan(false)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
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

                if (mUtils.checkInternetConnection(mView.context)) {
                    getLogDataByBarCode(scannedValue)
                } else {
                    var isSingle=false
                    var logslisting: ArrayList<BodereuLogListing> = arrayListOf()
                    realm.executeTransactionAsync ({ bgRealm ->
                        var logDetail= bgRealm.where(LogDetail::class.java)
                                .equalTo("barcodeNumber", scannedValue,Case.SENSITIVE)
//                                .equalTo("supplier",suplierID.toInt())
                                .equalTo("isUsed",false)
                                .findAll()

                        Log.e("logDetail","is "+logDetail.size)

                        if(logDetail!!.size==1 && logDetail?.size>0) {
                            if (    checkLogAACNLogNoAlreadyExits(
                                            logDetail!!.get(0)?.aacName,
                                            logDetail!!.get(0)?.logNo.toString()
                                    )
                            ) {
                                if (logsLiting?.size != 20) {
                                    var borderLogListing=BodereuLogListing()
                                    borderLogListing.setCbm(logDetail.get(0)!!.cbm)
                                    borderLogListing.setdiaType(logDetail.get(0)!!.diaType)
                                    borderLogListing.setTotalCBM(logDetail.get(0)!!.totalCBM)
                                    borderLogListing.setLogSpecies(logDetail.get(0)!!.logSpecies)
                                    borderLogListing.setLogNo(logDetail.get(0)!!.logNo)
                                    borderLogListing.setPlaqNo(logDetail.get(0)!!.plaqNo)
                                    borderLogListing.setAACYear(logDetail.get(0)!!.aacYear)
                                    borderLogListing.setLongBdx(logDetail.get(0)!!.longBdx)
                                    borderLogListing.setCbmQuantity(logDetail.get(0)!!.cbmQuantity)
                                    borderLogListing.setDiamBdx1(logDetail.get(0)!!.diam1Bdx)
                                    borderLogListing.setDiamBdx2(logDetail.get(0)!!.diam2Bdx)
                                    borderLogListing.setDiamBdx3(logDetail.get(0)!!.diam3Bdx)
                                    borderLogListing.setDiamBdx4(logDetail.get(0)!!.diam4Bdx)
                                    borderLogListing.setDiamBdx(logDetail.get(0)!!.diamBdx)

                                    borderLogListing.setLogSpeciesName(logDetail.get(0)!!.logSpeciesName)



                                    if(!logDetail.get(0)!!.bordereauNo.isNullOrEmpty()) {
                                        borderLogListing.setBordereauNo(logDetail.get(0)!!.bordereauNo!!)
                                    }

                                    if(!logDetail.get(0)!!.barcodeNumber.isNullOrEmpty()) {
                                        borderLogListing.setBarcodeNumber(logDetail.get(0)!!.barcodeNumber!!)
                                    }



                                    borderLogListing.setMaterialDesc(logDetail.get(0)!!.materialDesc)
                                    borderLogListing.setDetailId("0")
                                    borderLogListing.setSupplierName(logDetail.get(0)!!.supplierName)
                                    borderLogListing.setSupplierShortName(logDetail.get(0)!!.supplierShortName)
                                    borderLogListing.setSupplierName(logDetail.get(0)!!.supplierName)
                                    borderLogListing.setLogRecordDocNo(logDetail.get(0)!!.logRecordDocNo)
                                    borderLogListing.setQuality(logDetail.get(0)!!.quality)
                                    borderLogListing.setQualityId(logDetail.get(0)!!.qualityId)
                                    borderLogListing.aacId= logDetail.get(0)!!.aacId
                                    borderLogListing.setDetailId("0")

                                    borderLogListing.let { logsLiting?.add(it) }
                                    isSingle=true

                                    var logdetail= bgRealm.where(LogDetail::class.java)
                                            .equalTo("barcodeNumber", scannedValue,Case.SENSITIVE)
//                                            .equalTo("supplier",suplierID.toInt())
                                            .equalTo("isUsed",false)
                                            .findFirst()
                                    logdetail!!.isUsed=true

                                    bgRealm.copyToRealmOrUpdate(logdetail)

                                    var wagonLogRequest=WagonLogRequest()
                                    wagonLogRequest.uniqueId=System.currentTimeMillis().toString()
                                    wagonLogRequest.forestuniqueId=forestUniqueId
                                    wagonLogRequest.setCbm(logdetail.cbm)
                                    wagonLogRequest.setdiaType(logdetail.diaType)
                                    wagonLogRequest.setTotalCBM(logdetail.totalCBM)
                                    wagonLogRequest.setLogSpecies(logdetail.logSpecies)
                                    wagonLogRequest.setLogNo(logdetail.logNo)
                                    wagonLogRequest.setPlaqNo(logdetail.plaqNo)
                                    wagonLogRequest.setAACYear(logdetail.aacYear)
                                    wagonLogRequest.setLongBdx(logdetail.longBdx)
                                    wagonLogRequest.setCbmQuantity(logdetail.cbmQuantity)
                                    wagonLogRequest.setDiamBdx1(logdetail.diam1Bdx)
                                    wagonLogRequest.setDiamBdx2(logdetail.diam2Bdx)
                                    wagonLogRequest.setDiamBdx3(logdetail.diam3Bdx)
                                    wagonLogRequest.setDiamBdx4(logdetail.diam4Bdx)
                                    wagonLogRequest.setDiamBdx(logdetail.diamBdx)

                                    if(!logdetail.bordereauNo.isNullOrEmpty()) {
                                        wagonLogRequest.setBordereauNo(logdetail.bordereauNo!!)
                                    }

                                    if(!logdetail.barcodeNumber.isNullOrEmpty()) {
                                        wagonLogRequest.setBarcodeNumber(logdetail.barcodeNumber!!)
                                    }


                                    wagonLogRequest.setMaterialDesc(logdetail.materialDesc)
                                    wagonLogRequest.setDetailId("0")
                                    wagonLogRequest.setSupplierName(logdetail.supplierName)
                                    wagonLogRequest.setSupplierShortName(logdetail.supplierShortName)
                                    wagonLogRequest.setSupplierName(logdetail.supplierName)
                                    wagonLogRequest.setLogRecordDocNo(logdetail.logRecordDocNo)
                                    wagonLogRequest.setQuality(logdetail.quality)
                                    wagonLogRequest.setQualityId(logdetail.qualityId)
                                    wagonLogRequest.aacId= logdetail.aacId
                                    wagonLogRequest.setDetailId("0")
                                    bgRealm.copyToRealmOrUpdate(wagonLogRequest)



                                }
                            } else {
                                mUtils.showAlert(
                                        activity,
                                        resources.getString(R.string.duplicate_log_found)
                                )
                            }
                        }else if(logDetail.size>1){
                            for(i in logDetail.indices){
                                var borderLogListing=BodereuLogListing()
                                borderLogListing.setCbm(logDetail.get(i)?.cbm)
                                borderLogListing.setdiaType(logDetail.get(i)?.diaType)
                                borderLogListing.setTotalCBM(logDetail.get(i)?.totalCBM)
                                borderLogListing.setLogSpecies(logDetail.get(i)?.logSpecies)
                                borderLogListing.setLogNo(logDetail.get(i)?.logNo)
                                borderLogListing.setPlaqNo(logDetail.get(i)?.plaqNo)
                                borderLogListing.setAACYear(logDetail.get(i)?.aacYear)
                                borderLogListing.setLongBdx(logDetail.get(i)?.longBdx)
                                borderLogListing.setCbmQuantity(logDetail.get(i)?.cbmQuantity)
                                borderLogListing.setDiamBdx1(logDetail.get(i)?.diam1Bdx)
                                borderLogListing.setDiamBdx2(logDetail.get(i)?.diam2Bdx)
                                borderLogListing.setDiamBdx3(logDetail.get(i)?.diam3Bdx)
                                borderLogListing.setDiamBdx4(logDetail.get(i)?.diam4Bdx)
                                borderLogListing.setDiamBdx(logDetail.get(i)?.diamBdx)

                                if(!logDetail.get(i)!!.bordereauNo.isNullOrEmpty()) {
                                    borderLogListing.setBordereauNo(logDetail.get(i)!!.bordereauNo!!)
                                }

                                if(!logDetail.get(i)!!.barcodeNumber.isNullOrEmpty()) {
                                    borderLogListing.setBordereauNo(logDetail.get(i)!!.barcodeNumber!!)
                                }


                                borderLogListing.setMaterialDesc(logDetail.get(i)?.materialDesc)
                                borderLogListing.setDetailId("0")
                                borderLogListing.setSupplierName(logDetail.get(i)?.supplierName)
                                borderLogListing.setSupplierShortName(logDetail.get(i)?.supplierShortName)
                                borderLogListing.setSupplierName(logDetail.get(i)?.supplierName)
                                borderLogListing.setLogRecordDocNo(logDetail.get(i)?.logRecordDocNo)
                                borderLogListing.setQuality(logDetail.get(i)?.quality)
                                borderLogListing.setQualityId(logDetail.get(i)?.qualityId)
                                borderLogListing.aacId= logDetail.get(i)?.aacId
                                borderLogListing.aacId= logDetail.get(i)?.aacId
                                borderLogListing.aacId= logDetail.get(i)?.aacId
                                borderLogListing.setDetailId("0")

                                logslisting.add(borderLogListing)
                            }
                        }
                    }, {
                        if(isSingle) {
                            adapter.notifyDataSetChanged()
                            setTotalNoOfLogs(logsLiting?.size)
                        }else  if(logslisting.size>1) {
                            showMultiLogDialog(logslisting)
                        }else{
                            mUtils.showAlert(
                                    activity,
                                    "Log not found "
                            )
                        }
                        Log.e("success","success wagon logslisting")

                        realm.executeTransactionAsync({ bgRealm ->

                            var loadingRequest = bgRealm.where(LoadingRequest::class.java).equalTo("uniqueId", forestUniqueId).findFirst()
                            var logcount = loadingRequest?.logCount
                            loadingRequest!!.logCount = logcount!!+1
                            bgRealm.copyToRealmOrUpdate(loadingRequest)

                        }, {
                            Log.e("Success", "Success logs count update")

                        }) {
                            Log.e("failed", "failed count add logs")
                        }


                    }) {
                        Log.e("faile","faile to update logslisting")
                    }
                }
            }
        }

    }


    private fun displayScanResult(
        initiatingIntent: Intent,
        howDataReceived: String
    ) { // store decoded data
        val scannedValue =
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
        //getLogDataByBarCode(scannedValue)

        if (mUtils.checkInternetConnection(mView.context)) {
            getLogDataByBarCode(scannedValue)
        } else {
            var isSingle=false
            var logslisting: ArrayList<BodereuLogListing> = arrayListOf()
            realm.executeTransactionAsync ({ bgRealm ->
                var logDetail= bgRealm.where(LogDetail::class.java)
                    .equalTo("barcodeNumber", scannedValue,Case.SENSITIVE)
//                    .equalTo("supplier",suplierID.toInt())
                    .equalTo("isUsed",false)
                    .findAll()

                Log.e("logDetail","is "+logDetail.size)

                if(logDetail!!.size==1 && logDetail?.size>0) {
                    if (    checkLogAACNLogNoAlreadyExits(
                            logDetail!!.get(0)?.aacName,
                            logDetail!!.get(0)?.logNo.toString()
                        )
                    ) {
                        if (logsLiting?.size != 20) {
                            var borderLogListing=BodereuLogListing()
                            borderLogListing.setCbm(logDetail.get(0)!!.cbm)
                            borderLogListing.setdiaType(logDetail.get(0)!!.diaType)
                            borderLogListing.setTotalCBM(logDetail.get(0)!!.totalCBM)
                            borderLogListing.setLogSpecies(logDetail.get(0)!!.logSpecies)
                            borderLogListing.setLogSpeciesName(logDetail.get(0)!!.logSpeciesName)
                            borderLogListing.setLogNo(logDetail.get(0)!!.logNo)
                            borderLogListing.setPlaqNo(logDetail.get(0)!!.plaqNo)
                            borderLogListing.setAACYear(logDetail.get(0)!!.aacYear)
                            borderLogListing.setLongBdx(logDetail.get(0)!!.longBdx)
                            borderLogListing.setCbmQuantity(logDetail.get(0)!!.cbmQuantity)
                            borderLogListing.setDiamBdx1(logDetail.get(0)!!.diam1Bdx)
                            borderLogListing.setDiamBdx2(logDetail.get(0)!!.diam2Bdx)
                            borderLogListing.setDiamBdx3(logDetail.get(0)!!.diam3Bdx)
                            borderLogListing.setDiamBdx4(logDetail.get(0)!!.diam4Bdx)
                            borderLogListing.setDiamBdx(logDetail.get(0)!!.diamBdx)


                            if(!logDetail.get(0)!!.bordereauNo.isNullOrEmpty()) {
                                borderLogListing.setBordereauNo(logDetail.get(0)!!.bordereauNo!!)
                            }

                            if(!logDetail.get(0)!!.barcodeNumber.isNullOrEmpty()) {
                                borderLogListing.setBarcodeNumber(logDetail.get(0)!!.barcodeNumber!!)
                            }



                            borderLogListing.setMaterialDesc(logDetail.get(0)!!.materialDesc)
                            borderLogListing.setDetailId("0")
                            borderLogListing.setSupplierName(logDetail.get(0)!!.supplierName)
                            borderLogListing.setSupplierShortName(logDetail.get(0)!!.supplierShortName)
                            borderLogListing.setSupplierName(logDetail.get(0)!!.supplierName)
                            borderLogListing.setLogRecordDocNo(logDetail.get(0)!!.logRecordDocNo)
                            borderLogListing.setQuality(logDetail.get(0)!!.quality)
                            borderLogListing.setQualityId(logDetail.get(0)!!.qualityId)
                            borderLogListing.aacId= logDetail.get(0)!!.aacId
                            borderLogListing.setDetailId("0")

                            borderLogListing.let { logsLiting?.add(it) }
                            isSingle=true

                            var logdetail= bgRealm.where(LogDetail::class.java)
                                .equalTo("barcodeNumber", scannedValue,Case.SENSITIVE)
//                                .equalTo("supplier",suplierID.toInt())
                                .equalTo("isUsed",false)
                                .findFirst()
                            logdetail!!.isUsed=true

                            bgRealm.copyToRealmOrUpdate(logdetail)

                            var wagonLogRequest=WagonLogRequest()
                            wagonLogRequest.uniqueId=System.currentTimeMillis().toString()
                            wagonLogRequest.forestuniqueId=forestUniqueId
                            wagonLogRequest.setCbm(logdetail.cbm)
                            wagonLogRequest.setdiaType(logdetail.diaType)
                            wagonLogRequest.setTotalCBM(logdetail.totalCBM)
                            wagonLogRequest.setLogSpecies(logdetail.logSpecies)
                            wagonLogRequest.setLogNo(logdetail.logNo)
                            wagonLogRequest.setPlaqNo(logdetail.plaqNo)
                            wagonLogRequest.setAACYear(logdetail.aacYear)
                            wagonLogRequest.setLongBdx(logdetail.longBdx)
                            wagonLogRequest.setCbmQuantity(logdetail.cbmQuantity)
                            wagonLogRequest.setDiamBdx1(logdetail.diam1Bdx)
                            wagonLogRequest.setDiamBdx2(logdetail.diam2Bdx)
                            wagonLogRequest.setDiamBdx3(logdetail.diam3Bdx)
                            wagonLogRequest.setDiamBdx4(logdetail.diam4Bdx)
                            wagonLogRequest.setDiamBdx(logdetail.diamBdx)

                            if(!logdetail.bordereauNo.isNullOrEmpty()) {
                                wagonLogRequest.setBordereauNo(logdetail.bordereauNo!!)
                            }

                            if(!logdetail.barcodeNumber.isNullOrEmpty()) {
                                wagonLogRequest.setBarcodeNumber(logdetail.barcodeNumber!!)
                            }


                            wagonLogRequest.setMaterialDesc(logdetail.materialDesc)
                            wagonLogRequest.setDetailId("0")
                            wagonLogRequest.setSupplierName(logdetail.supplierName)
                            wagonLogRequest.setSupplierShortName(logdetail.supplierShortName)
                            wagonLogRequest.setSupplierName(logdetail.supplierName)
                            wagonLogRequest.setLogRecordDocNo(logdetail.logRecordDocNo)
                            wagonLogRequest.setQuality(logdetail.quality)
                            wagonLogRequest.setQualityId(logdetail.qualityId)
                            wagonLogRequest.aacId= logdetail.aacId
                            wagonLogRequest.setDetailId("0")
                            bgRealm.copyToRealmOrUpdate(wagonLogRequest)


                        }
                    } else {
                        mUtils.showAlert(
                            activity,
                            resources.getString(R.string.duplicate_log_found)
                        )
                    }
                }else if(logDetail.size>1){
                    for(i in logDetail.indices){
                        var borderLogListing=BodereuLogListing()
                        borderLogListing.setCbm(logDetail.get(i)?.cbm)
                        borderLogListing.setdiaType(logDetail.get(i)?.diaType)
                        borderLogListing.setTotalCBM(logDetail.get(i)?.totalCBM)
                        borderLogListing.setLogSpecies(logDetail.get(i)?.logSpecies)
                        borderLogListing.setLogNo(logDetail.get(i)?.logNo)
                        borderLogListing.setPlaqNo(logDetail.get(i)?.plaqNo)
                        borderLogListing.setAACYear(logDetail.get(i)?.aacYear)
                        borderLogListing.setLongBdx(logDetail.get(i)?.longBdx)
                        borderLogListing.setCbmQuantity(logDetail.get(i)?.cbmQuantity)
                        borderLogListing.setDiamBdx1(logDetail.get(i)?.diam1Bdx)
                        borderLogListing.setDiamBdx2(logDetail.get(i)?.diam2Bdx)
                        borderLogListing.setDiamBdx3(logDetail.get(i)?.diam3Bdx)
                        borderLogListing.setDiamBdx4(logDetail.get(i)?.diam4Bdx)
                        borderLogListing.setDiamBdx(logDetail.get(i)?.diamBdx)

                        if(!logDetail.get(i)!!.bordereauNo.isNullOrEmpty()) {
                            borderLogListing.setBordereauNo(logDetail.get(i)!!.bordereauNo!!)
                        }

                        if(!logDetail.get(i)!!.barcodeNumber.isNullOrEmpty()) {
                            borderLogListing.setBordereauNo(logDetail.get(i)!!.barcodeNumber!!)
                        }


                        borderLogListing.setMaterialDesc(logDetail.get(i)?.materialDesc)
                        borderLogListing.setDetailId("0")
                        borderLogListing.setSupplierName(logDetail.get(i)?.supplierName)
                        borderLogListing.setSupplierShortName(logDetail.get(i)?.supplierShortName)
                        borderLogListing.setSupplierName(logDetail.get(i)?.supplierName)
                        borderLogListing.setLogRecordDocNo(logDetail.get(i)?.logRecordDocNo)
                        borderLogListing.setQuality(logDetail.get(i)?.quality)
                        borderLogListing.setQualityId(logDetail.get(i)?.qualityId)
                        borderLogListing.aacId= logDetail.get(i)?.aacId
                        borderLogListing.aacId= logDetail.get(i)?.aacId
                        borderLogListing.aacId= logDetail.get(i)?.aacId
                        borderLogListing.setDetailId("0")

                        logslisting.add(borderLogListing)
                    }
                }
            }, {
                if(isSingle) {
                    adapter.notifyDataSetChanged()
                    setTotalNoOfLogs(logsLiting?.size)
                }else  if(logslisting.size>1) {
                    showMultiLogDialog(logslisting)
                }else{
                    mUtils.showAlert(
                        activity,
                        "Log not found "
                    )
                }
                Log.e("success","success wagon logslisting")

                realm.executeTransactionAsync({ bgRealm ->

                    var loadingRequest = bgRealm.where(LoadingRequest::class.java).equalTo("uniqueId", forestUniqueId).findFirst()
                    var logcount = loadingRequest?.logCount
                    loadingRequest!!.logCount = logcount!!+1
                    bgRealm.copyToRealmOrUpdate(loadingRequest)

                }, {
                    Log.e("Success", "Success logs count update")

                }) {
                    Log.e("failed", "failed count add logs")
                }


            }) {
                Log.e("faile","faile to update logslisting")
            }
        }
        Log.e("decodedData",scannedValue)
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
            }else if (action == Constants.ACTION_RESULT) { // Register to receive the result code
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


}
