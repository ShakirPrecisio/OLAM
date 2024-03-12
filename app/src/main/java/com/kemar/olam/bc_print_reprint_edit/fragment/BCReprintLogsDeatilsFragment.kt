package com.kemar.olam.bc_print_reprint_edit.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
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
import com.kemar.olam.bc_print_reprint_edit.adapter.BcBoLogsListAdapter
import com.kemar.olam.bc_print_reprint_edit.model.request.BorderueLogDeleteReq
import com.kemar.olam.bluetooth_printer.fragment.ReceiptDemo
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.*
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.dialog_add_log_layout.view.*
import kotlinx.android.synthetic.main.fragment_b_c_print_re_print.view.*
import kotlinx.android.synthetic.main.fragment_b_c_print_re_print.view.txtBO_NO
import kotlinx.android.synthetic.main.fragment_b_c_print_re_print.view.txtForestWagonNo
import kotlinx.android.synthetic.main.layout_logs_listing.view.*
import kotlinx.android.synthetic.main.layout_logs_listing.view.ivBOAdd
import kotlinx.android.synthetic.main.layout_logs_listing.view.swipeLogListing
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
import kotlin.math.roundToInt


class BCReprintLogsDeatilsFragment : Fragment(), View.OnClickListener,
    DialogFragment.GetDialogListener {
    lateinit var mView: View
    private val DelayTime = 500
    lateinit var rvLogListing: RecyclerView
    lateinit var adapter: BcBoLogsListAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logDialog: AlertDialog? = null
    var isDialogShowing: Boolean = false
    lateinit var countryDialogFragment: DialogFragment
    lateinit var alertView: View
    var logsLiting: ArrayList<BodereuLogListing> = arrayListOf()
    var commonForestMaster: GetForestDataRes? = GetForestDataRes()
    lateinit var mUtils: Utility
    var supplierLocationName = ""
    var bodereuHeaderId = 0
    var suplierID = ""
    var bodereuNumber = ""
    var speciesID = 0
    var qualityId: Int? = 0
    var aacID = 0
    var aacYear = ""
    var FULL_PATH: String = ""
    var isAllLogSelectedForDeletion: Boolean = false
    var selectedLogsCount: Int = 0;
    var originID: Int? = 0
    var fscOrNonFsc: String = ""
    var supplierShortName: String? = ""
    var transporterName: String? = ""
    var originName: String? = ""
    var LogModel: BodereuLogListing = BodereuLogListing()
    var printer: PrinterClass? = null
    var diatype="Under bark"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_b_c_print_re_print, container, false)
        initViews()
        return mView
    }


    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.bc_re_print_n_edit)
        (activity as HomeActivity).invisibleFilter()
    }

    fun setupClickListner() {
        mView.ivBOAdd.setOnClickListener(this)
        mView.txtUpdate.setOnClickListener(this)
        mView.txtDeleteAll.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun initViews() {
        //  (activity as HomeActivity).showActionBar()
        rvLogListing = mView.findViewById(R.id.rvLogListing)
        mUtils = Utility()
        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()
        printer = PrinterClass(mView.context)

        mView.swipeLogListing.setOnRefreshListener {
            mView.swipeLogListing.isRefreshing = false
            /* if(!commigFrom.equals( Constants.header, ignoreCase = true)){
                 getBodereuLogsByID(bodereuHeaderId.toString())
             }*/
            callingLogMasterAPI(suplierID.toString(), originID.toString())
        }

        var headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
            arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
        //headerDataModel?.supplierId?.toString()?.let { callingForestMasterAPI(it) }
        bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
        bodereuNumber = headerDataModel?.bordereauNo!!
        supplierLocationName = headerDataModel?.supplierName.toString()
        originID = headerDataModel?.originId
        supplierShortName = headerDataModel?.supplierShortName
        transporterName = headerDataModel?.transporterName
        suplierID = headerDataModel?.supplierId.toString()
        originName = headerDataModel?.originName.toString()
        headerDataModel?.transportMode?.let { setupTransportMode(it, mView.context) }
        fscOrNonFsc = headerDataModel?.fscName?.toString().toString()
        originID?.toString()?.let { callingLogMasterAPI(suplierID.toString(), it) }
        getBodereuLogsByID(bodereuHeaderId.toString())
        mView.txtBO_NO.text = headerDataModel?.bordereauNo?.toString().toString()
        mView.txtBOtext.text=headerDataModel.recordDocNo
        mView.txtForestWagonNo.text = headerDataModel?.wagonNo?.toString().toString()

    }


    fun setupTransportMode(transportMode: Int, context: Context) {
        when (transportMode) {
            1 -> {
                mView.txtBTrasnporterTtile?.text = context.resources.getString(R.string._wagon_no)
            }
            17 -> {
                mView.txtBTrasnporterTtile?.text = context.resources.getString(R.string._barge_no)
            }
            3 -> {
                mView.txtBTrasnporterTtile?.text = context.resources.getString(R.string._truck_no)
            }
            else -> {

            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setupRecyclerViewNAdapter() {
        logsLiting.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        rvLogListing.layoutManager = mLinearLayoutManager
        adapter = BcBoLogsListAdapter(mView.context, logsLiting)
        rvLogListing.adapter = adapter
        //Expand Collapse Action
        adapter.onMoreClick = { modelData, position, isExpanded ->
            if (isExpanded) {
                logsLiting.get(position).isExpanded = true
            } else {
                logsLiting.get(position).isExpanded = false
            }
            adapter.notifyDataSetChanged()
        }

        adapter.onEditClick = { modelData, position ->
            showAlerDialog(
                true, position, mView
                    .context.getString(R.string.are_you_sure_want_to_update_log), "update", false
            )
        }

        adapter.onDeleteClick = { modelData, position ->
            showAlerDialog(
                false, position, mView
                    .context.getString(R.string.are_you_sure_want_to_delete_log), "delete", false
            )
        }

        adapter.onPrintClick = { modelData, position ->
            showAlerDialog(
                false, position, mView
                    .context.getString(R.string.are_you_sure_want_to_print_log), "print", false
            )
        }
    }


    fun getLogMasterRequest(forestId: String, originID: String): CommonRequest {
        val request: CommonRequest = CommonRequest()
        request.setForestId(forestId)
        request.setOriginId(originID)
        val json = Gson().toJson(request)
        var test = json

        return request

    }


    private fun callingLogMasterAPI(forestId: String, originID: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = getLogMasterRequest(forestId, originID)
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
                            } else {
                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                        } catch (e: Exception) {
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
        } else {
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }


    fun getBodereuLogsByIDRequest(bodereuHeaderId: String): CommonRequest {
        var request: CommonRequest = CommonRequest()
        request.setHeaderId(bodereuHeaderId)
        val json = Gson().toJson(request)
        var test = json

        return request

    }

    private fun getBodereuLogsByID(bodereuHeaderId: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = getBodereuLogsByIDRequest(bodereuHeaderId)
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
                                            logsLiting.addAll(it)
                                        }
                                        setTotalNoOfLogs(logsLiting.size)
                                        adapter.notifyDataSetChanged()
                                    }
                                } else if (response.body()?.getSeverity() == 306) {
                                    mUtils.alertDialgSession(mView.context, activity)
                                } else {
                                    mUtils.showToast(activity, response.body().getMessage())
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
                        call: Call<GetBodereuLogByIdRes>,
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


    fun deleteLogsEntry(position: Int) {
        logsLiting.removeAt(position)
        adapter.notifyDataSetChanged()
        setTotalNoOfLogs(logsLiting.size)
    }

    private fun setTotalNoOfLogs(count: Int) {
        if (count == 0) {
            mView.linBCFooter.visibility = View.INVISIBLE
            mView.txtBCTotalLogs.text = getString(R.string.total_found, count)//"Total $count Found"
        } else {
            mView.linBCFooter.visibility = View.VISIBLE
            mView.txtBCTotalLogs.text = getString(R.string.total_found, count)//"Total $count Found"
        }

    }

    fun isValidateForLogsSelectedForDeletion(): Boolean {
        var returnValue: Boolean = false
        var itemSelectedCount = 0
        for (listdata in logsLiting) {
            if (listdata.isSelected) {
                itemSelectedCount++
            }
        }
        returnValue = itemSelectedCount != 0
        return returnValue
    }

    fun generateDeleteBodereuLogRequest(
        position: Int,
        isMultipleSelect: Boolean
    ): BorderueLogDeleteReq {
        val logList: ArrayList<BorderueLogDeleteReq.LogDatum> = arrayListOf()
        val request: BorderueLogDeleteReq = BorderueLogDeleteReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id).toInt())
        if (isMultipleSelect) {
            val logRequest: BorderueLogDeleteReq.LogDatum = BorderueLogDeleteReq.LogDatum()
            for (listdata in logsLiting) {
                if (listdata.isSelected) {
                    selectedLogsCount++
                    logRequest.deleteDetailId = logsLiting.get(position).getDetailId()?.toInt()
                    logRequest.bordereauHeaderId = bodereuHeaderId
                    logList.add(logRequest)
                }
            }
            //check whether all logs selected if yes then with the all logs borderuee header also deleted
            if (logsLiting?.size == selectedLogsCount) {
                isAllLogSelectedForDeletion = true
            }
        } else {
            val logRequest: BorderueLogDeleteReq.LogDatum = BorderueLogDeleteReq.LogDatum()
            logRequest.deleteDetailId = logsLiting.get(position).getDetailId()?.toInt()
            logRequest.bordereauHeaderId = bodereuHeaderId
            logList.add(logRequest)
            //check whether loglist having only one log if yes then with the all logs borderuee header also deleted
            if (logsLiting?.size == 1) {
                isAllLogSelectedForDeletion = true
            }
        }
        request.setLogData(logList)
        return request

    }


    fun generateAddBodereuLogListRequest(): AddBoereuLogListingReq {

        val request: AddBoereuLogListingReq = AddBoereuLogListingReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id).toInt())
        request.setBordereauHeaderId(bodereuHeaderId)

        request.setdiaType(diatype)

        request.setTimezoneId("Asia/Kolkata")
        for (model in logsLiting) {
            model.setMode("Submit")
            model.setMaterialDesc(0)
            /* model.setDetailId("")*/
        }
        request.setBordereauLogList(logsLiting)
        val json = Gson().toJson(request)
        var test = json

        return request

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun showAlerDialog(
        isEditLog: Boolean,
        position: Int,
        msg: String,
        action: String,
        isMultipleSelect: Boolean
    ) {
        val alert: AlertDialog =
            android.app.AlertDialog.Builder(view?.context, R.style.CustomDialogWithBackground)
                //.setTitle(activity?.getString(R.string.app_name))
                .setMessage(msg)
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        when (action) {
                            "delete" -> {
                                callingdeleteBordereauLogAPI(position, isMultipleSelect)
                            }
                            "deleteALL" -> {
                                callingdeleteBordereauLogAPI(position, isMultipleSelect)
                            }
                            "update" -> {
                                showAddLogDialog(isEditLog, position)
                            }
                            "print" -> {
                                buttonGenerate_onClick(logsLiting.get(position))
                            }
                            "right" -> {

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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showAddLogDialog(isEditLog: Boolean, position: Int) {

        diatype="Under bark"
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
        alertLayout.linConfirmNPrint.setOnClickListener {
            acessRuntimPermission(isEditLog, position, alertLayout)
        }


        alertLayout.edt_log2?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

            }


            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                try {
                    if (!alertLayout.edt_log2?.text.isNullOrEmpty()) {
                        if (4 < alertLayout.edt_log2?.text.toString().toInt()) {
                            mUtils.showAlert(
                                activity,
                                mView.resources.getString(R.string.after_slash_should_not_allow_more_than_four)
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.toString()
                }
            }
        })


        /*alertLayout.edtDia?.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus)  try {
                var tempLong = 0.0
                if(!alertLayout.edtLong?.text.toString().isNullOrEmpty()){
                    tempLong =  alertLayout.edtLong?.text.toString().toInt().toDouble()
                    if (tempLong.roundToInt()<alertLayout.edtDia.text.toString().toInt().toDouble().roundToInt()) {
                        mUtils.showAlert(
                            activity,
                            mView.resources.getString(R.string.dia_smaller_than_long)
                        )
                    }
                }
            } catch (e: Exception) {
                e.toString()
            }
        }*/

        alertLayout.radioGp.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(radioGroup: RadioGroup, i: Int) {
                when (i) {

                    R.id.under_bark -> {
                        Log.e("under_bark","under_bark")
                        diatype="Under bark"
                    }

                    R.id.under_sap -> {
                        Log.e("under_sap","under_sap")
                        diatype="UnderSap"
                    }

                }
            }
        })




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
                try {


                    /*var tempLong = 0.0
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

                    }*/
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
                            mUtils.showAlert(
                                activity,
                                mView.resources.getString(R.string.long_value_greater_than_dia)
                            )
                        }
                    }
                }catch (e: Exception){
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

                         calculateDiaAverage()
                         val result =
                             (s.toString().toInt().toDouble()) / 100 * (tempDia) / 100 * (tempDia) / 100 * 0.7854
                         val df = DecimalFormat("###.###")
                         val finalResult = df.format(result)
                         val doublelCBM  = finalResult.toString().replace(",",".")
                         alertLayout.edtCBM.setText(doublelCBM)
                     }
                }catch (e:Exception){
                  e.toString()
                }*/
                calculateDiaAverage()
            }
        })


        alertLayout.edtEssence.setOnClickListener {
            showDialog(commonForestMaster?.getSpecies() as ArrayList<SupplierDatum?>?, "essence")
        }
        alertLayout?.edtQuality?.setOnClickListener {
            showDialog(
                commonForestMaster?.getQualityData() as ArrayList<SupplierDatum?>?,
                "quality"
            )
        }

        alertLayout.edtAAC.setOnClickListener {
            showDialog(commonForestMaster?.getAacList() as ArrayList<SupplierDatum?>?, "aac")
        }

        alertLayout.ivLogCancel.setOnClickListener {
            logDialog?.dismiss()
        }


        logDialog = alert.create()
        logDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logDialog?.show()
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

    private fun callingdeleteBordereauLogAPI(position: Int, isMultipleSelect: Boolean) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = generateDeleteBodereuLogRequest(position, isMultipleSelect)
                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.deleteBcPrintBordereauLog(request)
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
                                        if (isAllLogSelectedForDeletion) {
                                            var fragment =
                                                BcTodaysHistoryFragment()
                                            val bundle = Bundle()
                                            bundle.putString(
                                                Constants.comming_from,
                                                Constants.Bc_main_screen
                                            );
                                            fragment.arguments = bundle
                                            (activity as HomeActivity).replaceFragment(
                                                fragment,
                                                true
                                            )
                                        } else {
                                            getBodereuLogsByID(bodereuHeaderId.toString())
                                        }
                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
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

    private fun callingAddBordereauAPI(isSinglePrint: Boolean) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = generateAddBodereuLogListRequest()
                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.addBordereauLogs(request)
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
                                        if (isSinglePrint) {
                                            getBodereuLogsByID(bodereuHeaderId.toString())
                                            buttonGenerate_onClick(LogModel)
                                        } else {
                                            var fragment =
                                                BcTodaysHistoryFragment()
                                            val bundle = Bundle()
                                            bundle.putString(
                                                Constants.comming_from,
                                                Constants.Bc_main_screen
                                            );
                                            fragment.arguments = bundle
                                            (activity as HomeActivity).replaceFragment(
                                                fragment,
                                                true
                                            )
                                        }


                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
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
        else if(4 < alertLayout.edt_log2?.text.toString().toInt()){
            mUtils.showAlert(activity, mView.resources.getString(R.string.after_slash_should_not_allow_more_than_four))
            alertLayout.edt_log2.requestFocus()
            return false
        }
        /*else if (alertLayout.edtPlaque_no.text.isNullOrEmpty()) {
           mUtils.showAlert(activity, "Please enter plaque no")
           return false
       }*/

        else if (alertLayout.edtEssence?.text.toString()==getResources().getString(R.string.select)) {
            mUtils.showAlert(activity, resources.getString(R.string.please_select_essesnce))
            return false
        } else if (alertLayout.edtDia.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, resources.getString(R.string.please_enter_dia))
            return false
        } else if (250 < alertLayout.edtDia.text.toString().toDouble().roundToInt()) {
            mUtils.showAlert(activity, resources.getString(R.string.maximum_dia_value))
            return false
        }else if (alertLayout.edtDia2.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, resources.getString(R.string.please_enter_dia))
            return false
        } else if (250 < alertLayout.edtDia2.text.toString().toDouble().roundToInt()) {
            mUtils.showAlert(activity, resources.getString(R.string.maximum_dia_value))
            return false
        }else if (alertLayout.edtDia3.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, resources.getString(R.string.please_enter_dia))
            return false
        } else if (250 < alertLayout.edtDia3.text.toString().toDouble().roundToInt()) {
            mUtils.showAlert(activity, resources.getString(R.string.maximum_dia_value))
            return false
        }else if (alertLayout.edtDia4.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, resources.getString(R.string.please_enter_dia))
            return false
        } else if (250 < alertLayout.edtDia4.text.toString().toDouble().roundToInt()) {
            mUtils.showAlert(activity, resources.getString(R.string.maximum_dia_value))
            return false
        }
        else if (alertLayout.edtLong.text.isNullOrEmpty()) {
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
        } else if (alertLayout.edtQuality.text.toString()==getResources().getString(R.string.select)) {
            mUtils.showAlert(activity, resources.getString(R.string.please_select_quality))
            return false
        }

        return true;
    }

    fun acessRuntimPermission(isEditLog: Boolean, position: Int, alertLayout: View) {
        val permissionListener: PermissionListener = object : PermissionListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionGranted() {
                var isLogAlreadyExit: Boolean = false
                LogModel = BodereuLogListing()
                if (isValidateAddLog(alertLayout)) {
                    if (isEditLog) {
                        logsLiting.get(position)
                            .setLogNo(alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString())
                        logsLiting.get(position).setPlaqNo(alertLayout.edtPlaque_no.text.toString())
                        logsLiting.get(position)
                            .setLogSpeciesName(alertLayout.edtEssence.text.toString())
                        logsLiting.get(position).setLogSpecies(speciesID)
                        logsLiting.get(position).setdiaType(diatype)
                        logsLiting.get(position).setQualityId(qualityId)
                        logsLiting.get(position).setAACYear(aacYear)
                        logsLiting.get(position).setAACName(alertLayout.edtAAC.text.toString())
                        logsLiting.get(position).setAAC(aacID)
                        logsLiting.get(position).setLogRecordDocNo(
                            mUtils.createLogRecordDocsNumber(
                                bodereuNumber,
                                alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString(),
                                alertLayout.edtDia.text.toString()
                            )
                        )
                        logsLiting.get(position).setQuality(alertLayout.edtQuality.text?.toString())

                        logsLiting.get(position).setDiamBdx1(alertLayout.edtDia.text?.toString()?.toInt())
                        logsLiting.get(position).setDiamBdx2(alertLayout.edtDia2.text?.toString()?.toInt())
                        logsLiting.get(position).setDiamBdx3(alertLayout.edtDia3.text?.toString()?.toInt())
                        logsLiting.get(position).setDiamBdx4(alertLayout.edtDia4.text?.toString()?.toInt())

                        logsLiting.get(position).setDiamBdx(Math.round(alertLayout.edtAvgDia.text?.toString()?.toDouble()!!).toInt())


                        /* logsLiting.get(position).setDetailId("")*/
                        logsLiting.get(position)
                            .setLongBdx(alertLayout.edtLong?.text?.toString()?.toInt())
                        logsLiting.get(position)
                            .setCbm(alertLayout.edtCBM.text?.toString()?.toDouble())
                        /* logsLiting.get(position).setBarcodeNumber(mUtils.getRandomNumberString(""))*/
                        logsLiting.get(position)
                            .setBarcodeNumber(logsLiting.get(position).getBarcodeNumber())
                        LogModel = logsLiting.get(position)

                    } else {
                        val request: BodereuLogListing = BodereuLogListing()
                        request.setLogNo(alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString())
                        request.setPlaqNo(alertLayout.edtPlaque_no.text.toString())
                        request.setLogSpeciesName(alertLayout.edtEssence.text.toString())
                        request.setLogSpecies(speciesID)
                        request.setQualityId(qualityId)
                        request.setAACYear(aacYear)
                        request.setdiaType(diatype)
                        request.setAACName(alertLayout.edtAAC.text.toString())
                        request.setAAC(aacID)
                        request.setLogRecordDocNo(
                            mUtils.createLogRecordDocsNumber(
                                bodereuNumber,
                                alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString(),
                                alertLayout.edtDia.text.toString()
                            )
                        )
                        request.setQuality(alertLayout.edtQuality?.text?.toString())
                        //request.setDiamBdx(alertLayout.edtDia.text?.toString()?.toInt())

                        request.setDiamBdx1(alertLayout.edtDia.text?.toString()?.toInt())
                        request.setDiamBdx2(alertLayout.edtDia2.text?.toString()?.toInt())
                        request.setDiamBdx3(alertLayout.edtDia3.text?.toString()?.toInt())
                        request.setDiamBdx4(alertLayout.edtDia4.text?.toString()?.toInt())
                        request.setDiamBdx(Math.round(alertLayout.edtAvgDia.text?.toString()?.toDouble()!!).toInt())

                        request.setLongBdx(alertLayout.edtLong.text?.toString()?.toInt())
                        request.setDetailId("0")
                        request.setCbm(alertLayout.edtCBM.text?.toString()?.toDouble())
                        request.setBarcodeNumber(mUtils.getRandomNumberString(""))

                        if (checkLogAACNLogNoAlreadyExits(
                                alertLayout.edtAAC.text.toString(),
                                alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString()
                            )
                        ) {
                            //logsLiting.add(request)
                            LogModel = request
                           // setTotalNoOfLogs(logsLiting.size)
                            isLogAlreadyExit = false
                        } else {
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
                            callingAddBordereauAPI(true)
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

    fun getCheckLogNumberDuplicatioRequest(logNumber: String): CommonRequest {

        var request : CommonRequest =   CommonRequest()
        request.setLogNo(logNumber)

        return  request

    }

    private fun callingCheckLogNumberDuplication(logNumber: String) {
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
                                        setTotalNoOfLogs(logsLiting.size)
                                        callingAddBordereauAPI(true)
                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
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


    fun checkLogAACNLogNoAlreadyExits(aacName: String?, logNumber: String): Boolean {
        for ((index, listdata) in logsLiting.withIndex()) {
            if (!aacName.isNullOrEmpty() && !logNumber.isNullOrEmpty())
                if (listdata.getLogNo().toString().contains(logNumber) && listdata.getAACName().toString().contains(
                        aacName!!
                    )
                ) {
                    return false
                }
        }
        return true
    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun  buttonGenerate_onClick(logModel:BodereuLogListing) {
        try {

            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            if (!mBluetoothAdapter.isEnabled) {
                mBluetoothAdapter.enable()
            }

//            mView.chkBCNoFSC.isChecked =  false
//            mView.chkFSC.isChecked =  false`
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
            } else {
                fscOrNonFsc = "FSC"
            }
            val productId = logModel.getBarcodeNumber()
            //Generate Barcoode with product ID
            val bitmap = mUtils.generatebarcodeBitmap(productId)
            mView.ivBarcode.setImageBitmap(bitmap)
            mView.txtBardCodeId.text  = logModel.getBarcodeNumber().toString()
            mView.txtBCLog.text  =  logModel.getLogNo().toString()
            mView.txtBCAAC.text = logModel.getAACName().toString()
            mView.txtBCCBM.text = logModel.getCbm().toString()

            val totalDia12=logModel.getDiamBdx1()!!  + logModel.getDiamBdx2()!!
            val totalDia34=logModel.getDiamBdx3()!!  + logModel.getDiamBdx4()!!


            var avgDia12= (totalDia12/2).toInt()
            var avgDia34= (totalDia34/2).toInt()


            mView.txtbcDia.text =  " D1 : "+avgDia12.toString()+ " D2 : "+avgDia34.toString()+"<b>" +" AD : "+logModel.getDiamBdx().toString() + "</b> "
            mView.txtBCLong.text ="<b>"+ "L : "+logModel.getLongBdx().toString()+ "m"+Character.toString('\u221B')+"</b> "
            mView.txtBCForest.text = supplierShortName
            val barcodeValue  = originName+"/"+logModel.getAACName().toString()
            mView.txtBCTransporter.text=barcodeValue
            /*removed as per client changees*/
            //mView.txtBCOrigin.text = supplierLocationName
            mView.txtBCEssense.text= logModel.getLogSpeciesName()
            mView.txtBCDate.text = mUtils?.getCurrentDate()

            val intent = Intent(mView.context, ReceiptDemo::class.java)
            intent.putExtra("Supplier_Name", supplierShortName)
            intent.putExtra("LOG_DATA", Gson().toJson(logModel))
            intent.putExtra("FSC_NO_FSC", fscOrNonFsc)
            intent.putExtra("BARCODE_Value", barcodeValue)
            startActivity(intent)

//            Handler().postDelayed({
//               // savePdf()
//                savePdfNew()
//            },DelayTime.toLong())

        } catch (e: Exception) {
            Toast.makeText(view?.context, e.message, Toast.LENGTH_LONG).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setEditDataOnAddLogDiolog(alertLayout: View, position: Int) {
        if (logsLiting?.get(position)?.getLogNo()?.contains("/")!!) {
            val tokens = StringTokenizer(logsLiting?.get(position)?.getLogNo(), "/");
            val first = tokens?.nextToken()
            val second = tokens?.nextToken()
            alertLayout.edt_log.setText(first)
            alertLayout.edt_log2.setText(second)
        } else {
            alertLayout.edt_log.setText(logsLiting?.get(position)?.getLogNo())
        }

        disableLogNonEditableFiled(alertLayout)

        alertLayout.edtPlaque_no.setText(logsLiting.get(position).getPlaqNo())
        alertLayout.edtEssence.setText(logsLiting.get(position).getLogSpeciesName())

        if(logsLiting.get(position).getLogSpeciesName().equals("OKOUME")){
            diatype=""
            alertLayout.linear_data.visibility=View.GONE

        }else{
            try {
                diatype= logsLiting?.get(position)?.getdiaType()!!
            }catch (e:java.lang.Exception){
                e.printStackTrace()
            }
            alertLayout.linear_data.visibility=View.VISIBLE
            if(diatype.equals("Under bark")){
                alertLayout.under_bark.isChecked=true
            }else{
                alertLayout.under_sap.isChecked=true

            }

        }

        speciesID = logsLiting.get(position).getLogSpecies()!!
        qualityId = logsLiting?.get(position)?.getQualityId()!!
        aacID = logsLiting.get(position).getAAC()!!
        aacYear = logsLiting.get(position).getAACYear()!!
        alertLayout.edtAAC.setText(logsLiting.get(position).getAACName())
        alertLayout.edtQuality.setText(logsLiting.get(position).getQuality().toString())

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

        alertLayout.edtLong.setText(logsLiting.get(position).getLongBdx().toString())
        alertLayout.edtCBM.setText(logsLiting.get(position).getCbm().toString())
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun disableLogNonEditableFiled(alertLayout: View) {
        alertLayout.edt_log.isEnabled = false
        alertLayout.edt_log2.isEnabled = false
        alertLayout.edtAAC.isEnabled = false
        alertLayout.edt_log.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it, R.color.gray_200) }
        alertLayout.edt_log2.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it, R.color.gray_200) }
        alertLayout.edtAAC.backgroundTintList =
            AppCompatResources.getColorStateList(mView.context, R.color.gray_200)
        alertLayout?.linLogNumber?.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout?.linLogNumber2?.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout?.linLogAAC?.setBackgroundResource(R.drawable.bg_for_editext_non_editable)

    }


    private fun savePdfNew(bitmap: Bitmap) {
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
        val mypath= File(directory, bodereuNumber + mFileName + ".pdf")

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
            Toast.makeText(
                mView.context,
                "$mFileName.pdf\nis saved to\n$FULL_PATH",
                Toast.LENGTH_SHORT
            ).show()

//            printPDFoverBluetooth(requireActivity(), FULL_PATH).execute()
//            printPDF(bitmap)
//            printPDF(FULL_PATH)
        }
        catch (e: Exception){
            //if Stringthing goes wrong causing exception, get and show exception message
            Toast.makeText(mView.context, e.message, Toast.LENGTH_SHORT).show()
        }
    }

  /*  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun savePdf() {
        //create object of Document class
        val mDoc = Document()
        //pdf file name
        val mFileName = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(System.currentTimeMillis())
        //pdf file path
        val mFilePath =
            Environment.getExternalStorageDirectory().toString() + "/" + mFileName + ".pdf"
        FULL_PATH = mFilePath
        try {
            //create instance of PdfWriter class
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))

            //open the document for writing
            mDoc.open()

            //get text from EditText i.e. textEt
            val mText = "Test PDF"

            mView.parentView.setDrawingCacheEnabled(true);
            mView.parentView.buildDrawingCache();
            var bitmap = parentView.getDrawingCache();
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageInByte: ByteArray = baos.toByteArray()
            var image = Image.getInstance(imageInByte);

            //add author of the document (metadata)
            mDoc.addAuthor("OLAM")

            var scaler = ((mDoc.getPageSize().getWidth() - mDoc.leftMargin()
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
            Toast.makeText(
                mView.context,
                "$mFileName.pdf\nis saved to\n$mFilePath",
                Toast.LENGTH_SHORT
            ).show()
            printPDF(FULL_PATH)
        } catch (e: Exception) {
            //if Stringthing goes wrong causing exception, get and show exception message
            Toast.makeText(mView.context, e.message, Toast.LENGTH_SHORT).show()
        }
    }*/

    fun printPDF(pdfPath: Bitmap) {

        printer!!.printData(mView.context, pdfPath)

//        val printManager =
//            mView.context.getSystemService(Context.PRINT_SERVICE) as PrintManager
//        /*val printAdapter =
//            PdfDocumentAdapter(this@PrintActivity, "/storage/emulated/0/Download/test.pdf")*/
//        val printAdapter =
//            PdfDocumentAdapter(mView.context, pdfPath)
//        printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
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
            mView.txtBO_NO.clearFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (model != null) {
            try {
                when (action) {

                    "essence" -> {
                        alertView?.edtEssence?.setText(model.optionName)
                        speciesID = model.optionValue!!
                        alertView?.edtQuality?.requestFocus()

                        if(model.optionName.equals("OKOUME")){
                            alertView.linear_data.visibility=View.GONE
                            diatype=""
                        }else{
                            alertView.linear_data.visibility=View.VISIBLE
                        }

                    }
                    "aac" -> {
                        alertView?.edtAAC?.setText(model.optionName)
                        aacID = model.optionValue!!
                        aacYear = model.optionValueString!!
                        alertView?.edt_log?.requestFocus()
                    }
                    "quality" -> {
                        alertView?.edtQuality?.setText(model.optionName)
                        qualityId = model.optionValue!!
                        alertView?.edtQuality?.requestFocus()
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
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(view: View?) {
        val id = view?.id
        when (id) {
            R.id.ivBOAdd -> {
                showAddLogDialog(false, 0)
            }
            R.id.txtDeleteAll -> {
                if (logsLiting.size != 0) {
                    if (isValidateForLogsSelectedForDeletion()) {
                        selectedLogsCount = 0
                        isAllLogSelectedForDeletion = false
                        showAlerDialog(
                            false,
                            0,
                            resources.getString(R.string.are_you_sure_want_to_delete_all_selected_log),
                            "deleteALL",
                            true
                        )
                    } else {
                        mUtils.showAlert(activity, resources.getString(R.string.please_select_logs))
                    }
                }
            }

            R.id.txtUpdate -> {
                if (logsLiting.size != 0) {
                    callingAddBordereauAPI(false)
                }
            }
        }
    }

}