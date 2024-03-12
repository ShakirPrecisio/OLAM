package com.kemar.olam.origin_verification.fragment

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
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
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
import com.kemar.olam.loading_wagons.adapter.MultiLogsListAdapter
import com.kemar.olam.loading_wagons.fragment.LoadingWagonsHeaderFragment
import com.kemar.olam.loading_wagons.model.responce.GetLogDataByBarcodeRes
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.origin_verification.adapter.OriginVeri_LogsListAdapter
import com.kemar.olam.origin_verification.model.RemoveLogFromStocksReq
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
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
import kotlinx.android.synthetic.main.fragment_loading_wagons_logs_listing.view.linLoadingFooter
import kotlinx.android.synthetic.main.fragment_loading_wagons_logs_listing.view.linManual
import kotlinx.android.synthetic.main.fragment_loading_wagons_logs_listing.view.txtElecTronicBO_NO
import kotlinx.android.synthetic.main.fragment_loading_wagons_logs_listing.view.txtScan
import kotlinx.android.synthetic.main.fragment_origgin_log_detaiils.view.*
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
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Comparator
import kotlin.math.roundToInt


class OriginLogDetaiilsFragment : Fragment() ,View.OnClickListener, DialogFragment.GetDialogListener{

    // TODO: Rename and change types of parameters
    private val SPLASH_TIMEOUT = 400
    private var param1: String? = null
    private var param2: String? = null
    lateinit var mView: View
    var  FULL_PATH  : String = ""
    var logDialog: AlertDialog? = null
    lateinit var rvLogListing: RecyclerView
    lateinit var adapter: OriginVeri_LogsListAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsLiting: ArrayList<BodereuLogListing> = arrayListOf()
    var existinglogsLiting: ArrayList<BodereuLogListing> = arrayListOf()
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
    var forestID = 0
    var suplierID =""
    var originID : Int? = 0
    var bodereuNumber :String?= ""
    var speciesID = 0
    var aacID = 0
    var qualityId :Int?= 0
    var logCount=0
    var fscOrNonFsc :String = ""
    var supplierShortName :String? = ""
    var transporterName:String?=""
    var aacYear = ""
    var headerModel  : AddBodereuRes.BordereauResponse =  AddBodereuRes.BordereauResponse()
    var todaysHistoryModel  : LogsUserHistoryRes.BordereauRecordList =  LogsUserHistoryRes.BordereauRecordList()

    //Scan feature
    var RESULT_CODE = 103;
    val LOG_TAG = "DataCapture1"
    private val bRequestSendResult = false

    var LogModel: BodereuLogListing = BodereuLogListing()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_origgin_log_detaiils, container, false)
        initViews()
        ///CreateProfile()
       /// setDecoderValues()
        return mView;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun initViews() {
        commigFrom =
            arguments?.getString(Constants.comming_from).toString()
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
            if (commigFrom.equals(Constants.header, ignoreCase = true))
            else {
                var headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
                    arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
                if (headerDataModel != null) {
                    todaysHistoryModel = headerDataModel
                }
                /* headerDataModel?.supplierId?.toString()?.let { callingForestMasterAPI(it) }*/
                bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
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
                    bodereuNumber = headerDataModel?.eBordereauNo!!
                    mView.txtElecTronicBO_NO.text = headerDataModel?.eBordereauNo?.toString().toString()
                } else {
                    mView.linManual.visibility = View.VISIBLE
                    bodereuNumber = headerDataModel?.eBordereauNo!!
                    mView.txtElecTronicBO_NO.text = headerDataModel?.eBordereauNo?.toString().toString()
                    mView.txtBO_NO.text = headerDataModel?.bordereauNo?.toString().toString()

                }
                mView.txtForestWagonNo.text = headerDataModel?.wagonNo?.toString().toString()
                mView.txtBO.text = headerDataModel?.recordDocNo?.toString().toString()
                originID?.toString()?.let { callingLogMasterAPI(suplierID?.toString(), it) }
                getBodereuLogsByID(bodereuHeaderId.toString())
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
       mView.ivBOSearch.setOnClickListener(this)
       mView.txtSave.setOnClickListener(this)
       mView.txtSubmit.setOnClickListener(this)
       mView.ivHeaderEdit.setOnClickListener(this)
       mView.txtScan.setOnClickListener(this)

        mView.selectchk.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(
                buttonView: CompoundButton?,
                isChecked: Boolean
            ) {
                if(isChecked){
                    adapter.selecteAll()
                }else{
                    adapter.clearAll()
                }

            }
        }
        )
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setupRecyclerViewNAdapter() {
        logsLiting?.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        rvLogListing.layoutManager = mLinearLayoutManager
        adapter = OriginVeri_LogsListAdapter(mView.context, logsLiting)
        rvLogListing.adapter = adapter
        //Expand Collapse Action
       /* adapter.onMoreClick = { modelData, position, isExpanded ->
            if (isExpanded) {
                logsLiting?.get(position)?.isExpanded = true
            } else {
                logsLiting?.get(position)?.isExpanded = false
            }
            adapter.notifyDataSetChanged()
        }
*/
        adapter.onEditClick = { modelData, position ->
            showAlerDialog(true,position,mView
                .context.getString(R.string.are_you_sure_want_to_update_log),"update")
        }

        adapter.onDeleteClick = { modelData, position ->
            showAlerDialog(false,position,mView
                .context.getString(R.string.are_you_sure_want_to_delete_log),"delete")
        }

        adapter.onPrintClick = { modelData, position ->
            showAlerDialog(false,position,mView
                .context.getString(R.string.are_you_sure_want_to_print_log),"print")
        }
    }


    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.origin_verification)
        (activity as HomeActivity).invisibleFilter()
    }


    private fun setTotalNoOfLogs(count: Int) {
        if (count == 0) {
            mView.linLoadingFooter.visibility =  View.INVISIBLE
            mView.tvNoDataFound.visibility=View.VISIBLE
            mView.rvLogListing.visibility=View.GONE
            mView.txtTotalLogs.visibility =View.INVISIBLE
            mView.txtTotalLogs.text = getString(R.string.total_found,count)//"Total $count Found"
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
        setTotalNoOfLogs(logsLiting?.size)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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



        /*alertLayout.edtDia?.setOnFocusChangeListener { view, hasFocus ->
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
        }*/


        alertLayout.edtDia?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

            }


            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                /*try {
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
                            val df = DecimalFormat("###.###")
                            val finalResult = df.format(result)
                             val doublelCBM  = finalResult.toString().replace(",",".")
                             alertLayout.edtCBM.setText(doublelCBM)

                        }
                } catch (e: Exception) {
                    e.toString()
                }*/

                calculateDiaAverage()
            }
        })


        alertLayout.edtDia2?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

            }


            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                /*try {
                    var tempLong = 0.0
                    if(!alertLayout.edtLong?.text.toString().isNullOrEmpty()){
                        tempLong =  alertLayout.edtLong?.text.toString().toInt().toDouble()
                    }

                    if (250 < alertLayout.edtDia.text.toString().toDouble().roundToInt()) {
                        mUtils.showAlert(activity, mView.resources.getString(R.string.maximum_dia_value))

                    }*//*else if (tempLong.roundToInt()<alertLayout.edtDia.text.toString().toInt().toDouble().roundToInt()) {
                             mUtils.showAlert(activity, "Dia value should be smaller than Long")
                         }*//*

                } catch (e: Exception) {
                    e.toString()
                }*/
                calculateDiaAverage()
            }
        })

        alertLayout.edtDia3?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

            }


            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                /* try {
                     var tempLong = 0.0
                     if(!alertLayout.edtLong?.text.toString().isNullOrEmpty()){
                         tempLong =  alertLayout.edtLong?.text.toString().toInt().toDouble()
                     }

                     if (250 < alertLayout.edtDia.text.toString().toDouble().roundToInt()) {
                         mUtils.showAlert(activity, mView.resources.getString(R.string.maximum_dia_value))

                     }
                 } catch (e: Exception) {
                     e.toString()
                 }*/
                calculateDiaAverage()
            }
        })

        alertLayout.edtDia4?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

            }


            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                calculateDiaAverage()

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
               /* var tempDia = 0.0
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
                        val df = DecimalFormat("###.###")
                        val finalResult = df.format(result)
                        val doublelCBM  = finalResult.toString().replace(",",".")
                        alertLayout.edtCBM.setText(doublelCBM)
                        //alertLayout.edtCBM.setText(finalResult.toString())
                    }
                }catch (e:Exception){
                    e.toString()
                }*/
                calculateDiaAverage()
            }
        })

       /* alertLayout?.edtDia?.addTextChangedListener(object : TextWatcher {
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
                    if(!alertLayout.edtLong?.text.toString().isNullOrEmpty()){
                        tempLong =  alertLayout.edtLong?.text.toString().toInt().toDouble()
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
        })*/
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

        var multiLogsListAdapter= MultiLogsListAdapter(requireContext(), responce.getLogDetails()!!)

        alertLayout.ivCancel.setOnClickListener{
            logDialog?.dismiss()
        }

        alertLayout.headLabel.setText("Log Number"+ responce.getLogDetails()!!.get(0).getLogNo())

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
                if(!checkLogNoAlreadyExits(alertLayout.edt_log.text.toString()+"/"+alertLayout.edt_log2.text.toString())){
                    logDialog?.dismiss()
                    getLogDataByBarlogNumber(alertLayout.edt_log.text.toString()+"/"+alertLayout.edt_log2.text.toString())
                }
               // checkLogAACNLogNoAlreadyExits(alertLayout.edtAAC.text.toString(),alertLayout.edt_log.text.toString()+"/"+alertLayout.edt_log2.text.toString())
            }
        }


        alertLayout.ivLogCancel.setOnClickListener{
            logDialog?.dismiss()
        }


        logDialog = alert.create()
        logDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logDialog?.show()
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

    fun getLogDataByBarlogNumberRequest(logNumber:String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setLogNo(logNumber)
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setForestId(suplierID)
        //request.setloadingStatus("Loaded,In Transit")

        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    private fun getLogDataByBarlogNumber(logNumber:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val  request  =   getLogDataByBarlogNumberRequest(logNumber)
                val call_api: Call<GetLogDataByBarcodeRes> =
                        apiInterface.getLogDataByLogNoForOriginVerification(request)
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
                                            if (checkLogAACNLogNoAlreadyExits(
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
                                            showMultiLogDialog(responce)
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

    fun calculateDiaAverage(){

        try {

            var Dia1 = 0
            var Dia2 = 0
            var Dia3 = 0
            var Dia4 = 0
            var avgDia=0
            var long=0.0

            if(!alertView.edtDia?.text.toString().isNullOrEmpty()){
                Dia1 =  alertView.edtDia?.text.toString().toInt()
            }

            if(!alertView.edtDia2?.text.toString().isNullOrEmpty()){
                Dia2 =  alertView.edtDia2?.text.toString().toInt()
            }

            if(!alertView.edtDia3?.text.toString().isNullOrEmpty()){
                Dia3 =  alertView.edtDia3?.text.toString().toInt()
            }

            if(!alertView.edtDia4?.text.toString().isNullOrEmpty()){
                Dia4 =  alertView.edtDia4?.text.toString().toInt()
            }

            if(!alertView.edtLong?.text.toString().isNullOrEmpty()){
                long =  alertView.edtLong?.text.toString().toInt().toDouble()
            }

            val total=Dia1+Dia2+Dia3+Dia4

            val totalDia12=Dia1+Dia2
            val totalDia34=Dia3+Dia4


            var avgDia12= (totalDia12/2).toInt()
            var avgDia34= (totalDia34/2).toInt()

            var totalAvg=avgDia12+avgDia34
            avgDia=totalAvg/2.toInt()

            // avgDia= (total/4).roundToInt()

            val df = DecimalFormat("###.###")
            // val avgResult = df.format(avgDia)
            //  val doublelAvg  = avgResult.toString().replace(",",".")

            alertView.edtAvgDia.setText(avgDia.toString())

            val result =
                (long) / 100 * (avgDia) / 100 * (avgDia) / 100 * 0.7854
            val finalResult = df.format(result)
            val doublelCBM  = finalResult.toString().replace(",",".")
            alertView.edtCBM.setText(doublelCBM)

        }catch (e:Exception){
            e.printStackTrace()
        }
    }

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
        if(logsLiting.get(position).getDiamBdx1()!=null){
            alertLayout.edtDia.setText(logsLiting.get(position).getDiamBdx1().toString())
        }else{
            alertLayout.edtDia.setText(logsLiting.get(position).getDiamBdx().toString())
        }

        if(logsLiting.get(position).getDiamBdx2()!=null){
            alertLayout.edtDia2.setText(logsLiting.get(position).getDiamBdx2().toString())
        }else{
            alertLayout.edtDia2.setText(logsLiting.get(position).getDiamBdx().toString())
        }

        if(logsLiting.get(position).getDiamBdx3()!=null){
            alertLayout.edtDia3.setText(logsLiting.get(position).getDiamBdx3().toString())
        }else{
            alertLayout.edtDia3.setText(logsLiting.get(position).getDiamBdx().toString())
        }

        if(logsLiting.get(position).getDiamBdx4()!=null){
            alertLayout.edtDia4.setText(logsLiting.get(position).getDiamBdx4().toString())
        }else{
            alertLayout.edtDia4.setText(logsLiting.get(position).getDiamBdx().toString())
        }


        alertLayout.edtAvgDia.setText(logsLiting.get(position).getDiamBdx().toString())
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
                LogModel = BodereuLogListing()
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
                       // logsLiting?.get(position)?.setDiamBdx(alertLayout.edtDia.text?.toString()?.toInt())


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
                        //if it is from edit then directly add else need to validate  log number from database
                        if(isEditLog){
                            callingAddBordereauFoOriginAPI("Save")
                        }else{
                            LogModel.getLogNo()?.let { callingCheckLogNumberDuplication(it) }
                        }

                        //   buttonGenerate_onClick(LogModel)
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

    fun getCheckLogNumberDuplicatioRequest(logNumber:String): CommonRequest {

        var request : CommonRequest =   CommonRequest()
        request.setLogNo(logNumber)

        return  request

    }

    private fun callingCheckLogNumberDuplication(logNumber:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val  request  =  getCheckLogNumberDuplicatioRequest(logNumber)
                val call: Call<AddBodereuRes> =
                    apiInterface.checkLogNumberDuplication(request)
                call.enqueue(object :
                    Callback<AddBodereuRes> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                        call: Call<AddBodereuRes>,
                        response: Response<AddBodereuRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response != null) {
                                    if (response.body().getSeverity() == 200) {
                                        logsLiting?.add(LogModel)
                                        setTotalNoOfLogs(logsLiting?.size)
                                        callingAddBordereauFoOriginAPI("Save")
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
        } else if (alertLayout.edtLong.text.toString().toDouble().roundToInt()<alertLayout.edtAvgDia.text.toString().toDouble().roundToInt()) {
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
                                if( logsLiting?.get(position)?.getDetailId().equals("0")){
                                    deleteLogsEntry(position)
                                }else {
                                    callingdeleteBordereauLogAPI(position)
                                }
                            }
                            "update"->{
                                showAddLogDialog(isEditLog, position)
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
        var tempSubmitLogsLiting: ArrayList<BodereuLogListing> = arrayListOf()
        var tempSaveLogsLiting: ArrayList<BodereuLogListing> = arrayListOf()
        tempSubmitLogsLiting.clear()
        tempSaveLogsLiting.clear()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(bodereuHeaderId)
        request.setTimezoneId("Asia/Kolkata")
        if(action.equals("Submit",ignoreCase = true)) {
            for (model in logsLiting) {
                if (model.isSelected == true) {
                    model.setMode("Submit")
                    model.setMaterialDesc(0)
                    tempSubmitLogsLiting.add(model)
                }
            }
        }else{
            for (model in logsLiting) {
                if (model.isSelected == true) {
                    model.setMode("Submit")
                    model.setMaterialDesc(0)
                }
            }
            tempSaveLogsLiting.addAll(logsLiting)
        }
        if(action=="Save"){
            request.setBordereauLogList(tempSaveLogsLiting)
            request.setStatusMode("Save")
        }else{
            request.setBordereauLogList(tempSubmitLogsLiting)
            request.setStatusMode("Verified")
        }

        var json =  Gson().toJson(request)
        var test  = json
        return  request
    }

    fun generateRemoveLogFromStockRequest(): RemoveLogFromStocksReq {

        var bordereuDetailList  =  arrayListOf<Int>()
        var request : RemoveLogFromStocksReq =   RemoveLogFromStocksReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(bodereuHeaderId)
        for (model in logsLiting) {
            if(model.isSelected == false){
                model.getDetailId()?.toInt()?.let { bordereuDetailList.add(it) }
            }
        }
        request.setBordereauDetailIdList(bordereuDetailList)
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
                            }  else{
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



    private fun callingRemoveLogFromOriginAPI(action:String) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateRemoveLogFromStockRequest()
                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.removeLogListFromOrigin(request)
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
                                        //callingAddBordereauFoOriginAPI("Submit")

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


    private fun callingAddBordereauFoOriginAPI(action:String) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateAddBodereuLogListRequest(action)
                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.saveLogInOriginVerify(request)
                call.enqueue(object :
                    Callback<AddBodereuLogListingRes> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
                                            val fragment =
                                                OriginVeriUserHistoryFragment()
                                            val bundle = Bundle()
                                            bundle.putString(
                                                Constants.comming_from,
                                                Constants.Bc_main_screen)
                                            fragment.arguments = bundle

                                            (activity as HomeActivity).replaceFragment(fragment,true)
                                        }else{
                                            getBodereuLogsByID(bodereuHeaderId.toString())
                                            buttonGenerate_onClick(LogModel)
                                        }
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

    fun checkLogNoAlreadyExits(logNumber:String):Boolean{
        for((index,listdata)  in existinglogsLiting.withIndex()){
            if(!logNumber.isNullOrEmpty())
                if (listdata.getLogNo().toString().contains(logNumber) ) {

                    if(checkLogAACNLogNoAlreadyExits(listdata.getAACName().toString(),listdata.getLogNo().toString())) {
                        logsLiting.add(listdata)
                        adapter.selecteAll()
                        //adapter.notifyDataSetChanged()
                        setTotalNoOfLogs(logsLiting?.size)
                        if (logDialog != null)
                            logDialog?.dismiss()
                        return true
                    }
                }
        }
        if(logDialog!=null)
        logDialog?.dismiss()

        return false
    }

    fun checkBarcodeAlreadyExits(logNumber:String):Boolean{
        for((index,listdata)  in existinglogsLiting.withIndex()){
            if(!logNumber.isNullOrEmpty())
                if (listdata.getBarcodeNumber().toString().contains(logNumber) ) {

                    if(checkLogAACNLogNoAlreadyExits(listdata.getAACName().toString(),listdata.getLogNo().toString())) {
                        logsLiting.add(listdata)
                        adapter.selecteAll()
                        //adapter.notifyDataSetChanged()
                        setTotalNoOfLogs(logsLiting?.size)
                        if (logDialog != null)
                            logDialog?.dismiss()
                        return true
                    }
                }
        }
        if(logDialog!=null)
        logDialog?.dismiss()

        return false
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

        if(!checkBarcodeAlreadyExits(barcodeNumber)) {

            if (mUtils.checkInternetConnection(mView.context)) {
                try {
                    mUtils.showProgressDialog(mView.context)
                    val apiInterface: ApiEndPoints =
                        ApiClient.client.create(ApiEndPoints::class.java)

                    val request = getDeliveryBodereuLogsByDeliveryIDRequest(barcodeNumber)
                    val call_api: Call<GetLogDataByBarcodeRes> =
                        apiInterface.getLogDataByBarCodeInOrigin(request)

                    call_api.enqueue(object :
                        Callback<GetLogDataByBarcodeRes> {
                        override fun onResponse(
                            call: Call<GetLogDataByBarcodeRes>,
                            response: Response<GetLogDataByBarcodeRes>
                        ) {
                            mUtils.dismissProgressDialog()

                            try {
                                if (response.code() == 200) {
                                    if (response?.body().getSeverity() == 200) {
                                        val responce: GetLogDataByBarcodeRes =
                                            response.body()!!
                                        if (responce != null) {

                                            if (checkLogAACNLogNoAlreadyExits(
                                                    responce.getLogDetail()?.getAACName(),
                                                    responce.getLogDetail()?.getLogNo().toString()
                                                )
                                            ) {
                                                if (logsLiting?.size != 200) {
                                                    responce.getLogDetail()
                                                        ?.let { logsLiting?.add(it) }
                                                    adapter.selecteAll()
                                                    setTotalNoOfLogs(logsLiting?.size)
                                                }
                                            } else {
                                                mUtils.showAlert(
                                                    activity,
                                                    resources.getString(R.string.duplicate_log_found)
                                                )
                                            }
                                        }

                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showAlert(
                                            activity,
                                            response?.body().getMessage()
                                        )
                                    }
                                } else if (response.code() == 306) {
                                    mUtils.alertDialgSession(mView.context, activity)
                                } else {
                                    mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                                }
                            } catch (e: Exception) {
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
            } else {
                mUtils.showToast(mView.context, getString(R.string.no_internet))
            }
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
                                existinglogsLiting.clear()
                                logCount=responce.getBordereauLogList()?.size!!
                                responce.getBordereauLogList()?.let {
                                    existinglogsLiting?.addAll(
                                        it
                                    )
                                }

                               // setTotalNoOfLogs(responce.getBordereauLogList()?.size!!)
                                mView.selectchk.isChecked=true
                                adapter?.selecteAll()
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
            mView.txtbcDia.text = "AD : "+logModel.getDiamBdx().toString()+ " D1 : "+logModel.getDiamBdx1().toString()+ " D2 : "+logModel.getDiamBdx2().toString()+ " D3 : "+logModel.getDiamBdx3().toString()+ " D4 : "+logModel.getDiamBdx4().toString()

            mView.txtBCLong.text = "L : "+logModel.getLongBdx().toString()
            mView.txtBCCBM.text = logModel.getCbm().toString()
            mView.txtBCForest.text = supplierShortName
            var barcodeValue  = originName+"/"+logModel.getAACName().toString()+aacYear
            mView.txtBCTransporter.text=barcodeValue
            mView.txtBCOrigin.text = supplierLocationName
            mView.txtBCEssense.text= logModel.getLogSpeciesName()
            mView.txtBCDate.text = mUtils?.getCurrentDate()


            Handler().postDelayed({
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


   /* @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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
    }
*/
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
    fun isValidateForLogsSelectedForDeletion(): Boolean {
        var returnValue: Boolean = false
        var itemSelectedCount = 0
        for (listdata in logsLiting) {
            if (listdata?.isSelected) {
                itemSelectedCount++
            }
        }
        if (itemSelectedCount != 0) {
            returnValue = true
        } else {
            returnValue = false
        }
        return returnValue
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivBOAdd -> {
                //user should be add only 30 logs
                if(logsLiting?.size!=20) {
                    showAddLogDialog(false, 0)
                }

            }

            R.id.ivBOSearch -> {
                //user should be add only 30 logs
                if(logsLiting?.size!=20) {
                    showSearcByLogDialog( 0)
                }

            }


            R.id.txtSave -> {
                if(logsLiting?.size!=0) {
                    if (isValidateForLogsSelectedForDeletion()) {
                        callingAddBordereauFoOriginAPI(Constants.SAVE)
                    }else {
                        mUtils.showAlert(activity, resources.getString(R.string.please_select_logs))
                    }
                }
            }

            R.id.txtSubmit -> {
                if (logsLiting?.size != 0) {
                    if (isValidateForLogsSelectedForDeletion()) {
                        if(validateIfAllLogSelectedThenSkipDeletedLogAPI()==true){
                            if(validationCheckAllEqualLogs()){
                                if(validationCheckLogsLargerCount()){
                                    callingAddBordereauFoOriginAPI(Constants.SUBMIT)
                                }else{
                                        logLargeCountDialogue()
                                }
                            }else{
                                logCountEqualDialogue()
                            }
                        }else {
                            logDialogue()
                        }
                    } else {
                        mUtils.showAlert(activity, resources.getString(R.string.please_select_logs))
                    }
                }
            }

            R.id.txtScan -> {
                acessRuntimPermissionForCamera()
            }

            R.id.ivHeaderEdit ->{
                if(commigFrom.equals(Constants.header, ignoreCase = true)) {
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


    fun logCountEqualDialogue(){
        val dialogBuilder = AlertDialog.Builder(activity!!)
        dialogBuilder.setMessage("All log not added! Do you want to Continue..?")
                // if the dialog is cancelable
                .setCancelable(false)
                .setPositiveButton("yes ", DialogInterface.OnClickListener {
                    dialog, id ->

                    if(validationCheckLogsLargerCount()){
                        dialog.dismiss()
                        callingAddBordereauFoOriginAPI(Constants.SUBMIT)
                    }else{
                        logLargeCountDialogue()
                    }

                })
                .setNegativeButton("No ", DialogInterface.OnClickListener {
                    dialog, id ->
                    dialog.dismiss()

                })

        val alert = dialogBuilder.create()
        alert.setTitle("Log count?")
        alert.show()
    }
    fun logLargeCountDialogue(){
        val dialogBuilder = AlertDialog.Builder(activity!!)
        dialogBuilder.setMessage("New log added! Do you want to Continue..?")
                // if the dialog is cancelable
                .setCancelable(false)
                .setPositiveButton("yes ", DialogInterface.OnClickListener {
                    dialog, id ->
                    callingAddBordereauFoOriginAPI(Constants.SUBMIT)
                    dialog.dismiss()
                })
                .setNegativeButton("No ", DialogInterface.OnClickListener {
                    dialog, id ->
                    dialog.dismiss()

                })

        val alert = dialogBuilder.create()
        alert.setTitle("Log count?")
        alert.show()
    }

    fun logDialogue(){
        val builder1 = AlertDialog.Builder(context)
        builder1.setMessage("All the logs are not selected, Do you wish to continue")
        builder1.setCancelable(true)

        builder1.setPositiveButton(
                "Yes",
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id: Int) {

                        if(validationCheckAllEqualLogs()){
                            if(validationCheckLogsLargerCount()){
                                callingAddBordereauFoOriginAPI(Constants.SUBMIT)
                                dialog.dismiss()
                            }else{
                                dialog.dismiss()
                                logLargeCountDialogue()
                            }
                        }else{
                            dialog.dismiss()
                            logCountEqualDialogue()
                        }
                        dialog.cancel()
                    }
                })

        builder1.setNegativeButton(
                "No",
                object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface, id: Int) {
                        dialog.cancel()
                    }
                })

        val alert11 = builder1.create()
        alert11.show()
    }

    fun validateIfAllLogSelectedThenSkipDeletedLogAPI():Boolean{
        var isAllSelectedLog : Boolean =  false
        var counter=0
        for (model in logsLiting) {
            if(model.isSelected == true){
                counter++
            }
        }
        if(counter==logsLiting.size){
            isAllSelectedLog =  true
        }
        return isAllSelectedLog
    }

    //scanner Data

    fun validationCheckAllEqualLogs():Boolean{
        var i =0
        for((index,listdata)  in logsLiting.withIndex()){
            for((index,logData)  in existinglogsLiting.withIndex()){

                if (listdata.getLogNo().toString().contains(logData.getLogNo().toString()) ) {
                    i++
                }
            }
        }

        if(i==existinglogsLiting.size){
            return true
        }else{
            return false
        }
    }

    fun validationCheckLogsLargerCount():Boolean{
        var i =0
        for((index,listdata)  in logsLiting.withIndex()){
            for((index,logData)  in existinglogsLiting.withIndex()){

                if (listdata.getLogNo().toString().contains(logData.getLogNo().toString()) ) {
                    i++
                }
            }
        }

        if(logsLiting.size>i){
            return false
        }else{
            return true
        }
    }

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

   /* override fun onResume() {
        super.onResume()
        registerReceivers()
    }*/

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

   /* override fun onPause() {
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
*/
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


}
