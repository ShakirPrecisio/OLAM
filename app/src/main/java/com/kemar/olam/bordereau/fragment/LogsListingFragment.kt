package com.kemar.olam.bordereau.fragment
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
import android.print.PrintAttributes
import android.print.PrintManager
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
import androidx.core.content.ContextCompat
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
import com.kemar.olam.bluetooth_printer.fragment.ReceiptDemo
import com.kemar.olam.bordereau.adapter.BoLogsListAdapter
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.request.BoderueDeleteLogReq
import com.kemar.olam.bordereau.model.request.ValidateBodereueNoReq
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.loading_wagons.model.request.LoadingRequest
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.offlineData.requestbody.BorderauRequest
import com.kemar.olam.offlineData.response.LogDetail
import com.kemar.olam.offlineData.response.LogOfflineResponse
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.*
import com.lp.lpwms.ui.offline.response.*
import io.realm.Case
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.dialog_add_log_layout.view.*
import kotlinx.android.synthetic.main.fragment__add_header.view.*
import kotlinx.android.synthetic.main.layout_logs_listing.view.*
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


class LogsListingFragment : Fragment(),View.OnClickListener, DialogFragment.GetDialogListener {

    private val DelayTime = 500
    lateinit var mView: View
    var  FULL_PATH  : String = ""
    var logDialog: AlertDialog? = null
    lateinit var rvLogListing: RecyclerView
    lateinit var adapter: BoLogsListAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsLiting: ArrayList<BodereuLogListing> = arrayListOf()
    var logsDetailsLiting: ArrayList<LogDetail> = arrayListOf()
    lateinit var  borderauList: RealmResults<BodereuLogListing>
    lateinit var mUtils: Utility
    var diatype="Under bark"
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
    var suplierID ="0"
    var originID : Int? = 0
    var bodereuNumber = ""
    var speciesID = 0
    var qualityId :Int?= 0
    var customerId :Int?= 0
    var aacID = 0
    var fscOrNonFsc :String = ""
    var uniqueId :String = ""
    var forestuniqueId :String = ""
    var forestUniqueId :String = ""
    var supplierShortName :String? = ""
    var transporterName:String?=""
    var aacYear = ""
    var headerModel  : AddBodereuRes.BordereauResponse =  AddBodereuRes.BordereauResponse()
    var todaysHistoryModel  :LogsUserHistoryRes.BordereauRecordList =  LogsUserHistoryRes.BordereauRecordList()
    var LogModel : BodereuLogListing = BodereuLogListing()


    var logslist= ArrayList<LogDetail>()

    lateinit var realm: Realm


    var aaclist : ArrayList<SupplierDatum?>? = arrayListOf()
    var essenceList : ArrayList<SupplierDatum?>? = arrayListOf()
    var customerList : ArrayList<SupplierDatum?>? = arrayListOf()
    var qualityList : ArrayList<SupplierDatum?>? = arrayListOf()

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
        mView = inflater.inflate(R.layout.layout_logs_listing, container, false)
        initViews()
        return mView;
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

        mView.swipeLogListing.setOnRefreshListener{
            mView.swipeLogListing.isRefreshing =  false
           /* if(!commigFrom.equals( Constants.header, ignoreCase = true)){
                getBodereuLogsByID(bodereuHeaderId.toString())
            }*/
            //callingLogMasterAPI(suplierID)
            if (mUtils.checkInternetConnection(mView.context)) {
                callingLogMasterAPI(suplierID.toString(), originID.toString())
            }
        }

        //when comming from headerScreen & User History Screen
        //suplierID == forest
        //
        if(commigFrom.equals(Constants.header, ignoreCase = true)) {
            val headerDataModel: AddBodereuRes.BordereauResponse? =
                arguments?.getSerializable(Constants.badereuModel) as AddBodereuRes.BordereauResponse
            if (headerDataModel != null) {
                headerModel =  headerDataModel
            }
            supplierLocationName = headerDataModel?.supplierName.toString()
            bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
            bodereuNumber = headerDataModel?.bordereauNo!!
            suplierID = headerDataModel?.supplier.toString()

            try{
                forestUniqueId= headerDataModel?.unqiueId!!
                Log.e("forestUniqueId", "is " + forestUniqueId)
            }catch (e: Exception){
                e.printStackTrace()
            }


            originID = headerDataModel?.originID
            originName= headerDataModel?.originName.toString()
            supplierShortName = headerDataModel?.supplierShortName
            transporterName= headerDataModel?.transporterName
            fscOrNonFsc = headerDataModel?.fscName?.toString().toString()
            headerDataModel?.modeOfTransport?.let { setupTransportMode(it, mView.context) }
            mView.txtBO_NO.text = headerDataModel?.bordereauNo?.toString().toString()
            mView.txtForestWagonNo.text = headerDataModel?.wagonNo?.toString().toString()
            mView.txtBO.text = headerDataModel?.bordereauRecordNo?.toString().toString()

          //  setTotalNoOfLogs(logsLiting?.size)
           /* headerDataModel?.supplier?.toString()?.let { callingForestMasterAPI(it) }*/

            if (mUtils.checkInternetConnection(mView.context)) {
                callingLogMasterAPI(suplierID.toString(), originID.toString())
            }



            if (!mUtils.checkInternetConnection(mView.context)) {
                 borderauList = realm.where(BodereuLogListing::class.java).equalTo("forestuniqueId", forestUniqueId,Case.SENSITIVE).findAll()
                Log.e("borderauList", "is " + borderauList.size)

                logsLiting.addAll(realm.copyFromRealm(borderauList))

                /*for(border in borderauList){
                    var bordereauGroundList = BodereuLogListing()
                    bordereauGroundList.isUploaded=border.isUploaded
                    bordereauGroundList.setBarcodeNumber(border.getBarcodeNumber())
                    bordereauGroundList.setDetailId(border.getDetailId())
                    bordereauGroundList.setQualityId(border.getQualityId())
                    bordereauGroundList.setQuality(border.getQuality())
                    bordereauGroundList.setDiamBdx1(border.getDiamBdx1())
                    bordereauGroundList.setDiamBdx2(border.getDiamBdx2())
                    bordereauGroundList.setDiamBdx3(border.getDiamBdx3())
                    bordereauGroundList.setDiamBdx4(border.getDiamBdx4())
                    bordereauGroundList.setdiaType(border.getdiaType())
                    bordereauGroundList.setLongBdx(border.getLongBdx())
                    bordereauGroundList.setAvrageBdx(border.getaverageBdx())
                    bordereauGroundList.setLongBdx(border.getLongBdx())
                    bordereauGroundList.setAAC(border.getAAC())
                    bordereauGroundList.setAACName(border.getAACName())
                    bordereauGroundList.setAACYear(border.getAACYear())
                    bordereauGroundList.setPlaqNo(border.getPlaqNo())
                    bordereauGroundList.setLogSpeciesName(border.getLogSpeciesName())
                    bordereauGroundList.setLogSpecies(border.getLogSpecies())
                    bordereauGroundList.setBordereaDetailStatus(border.getBordereaDetailStatus())
                    bordereauGroundList.uniqueId=border.uniqueId
                    bordereauGroundList.forestuniqueId=border.forestuniqueId
                    bordereauGroundList.setdiaType(border.getdiaType())
                    bordereauGroundList.setCbm(border.getCbm())
                    bordereauGroundList.setCbmQuantity(border.getCbmQuantity())
                    bordereauGroundList.setTotalCBM(border.getTotalCBM())

                    logsLiting.add(bordereauGroundList)
                }*/

                adapter.notifyDataSetChanged()

                setTotalNoOfLogs(logsLiting.size)
            }else{
                getBodereuLogsByID(bodereuHeaderId.toString())
            }
        }
        else{
            var headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
                arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
                if (headerDataModel != null) {
                    todaysHistoryModel =  headerDataModel
                }
           /* headerDataModel?.supplierId?.toString()?.let { callingForestMasterAPI(it) }*/
            bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
            supplierLocationName = headerDataModel?.supplierName.toString()

            originID = headerDataModel?.originId

            try{
                forestUniqueId= headerDataModel?.uniqueId!!
                Log.e("forestUniqueId", "is " + forestUniqueId)
            }catch (e: Exception){
                e.printStackTrace()
            }

            bodereuNumber = headerDataModel?.bordereauNo!!
            originName= headerDataModel?.originName.toString()
            supplierShortName = headerDataModel?.supplierShortName
            suplierID = headerDataModel?.supplierId.toString()
            transporterName= headerDataModel?.transporterName
            fscOrNonFsc = headerDataModel?.fscName?.toString().toString()
            headerDataModel?.transportMode?.let { setupTransportMode(it, mView.context) }
            mView.txtBO_NO.text = headerDataModel?.bordereauNo?.toString()
            mView.txtForestWagonNo.text = headerDataModel?.wagonNo?.toString()
            mView.txtBO.text = headerDataModel?.recordDocNo?.toString()
            originID?.toString()?.let {
                if (mUtils.checkInternetConnection(mView.context)) {
                    callingLogMasterAPI(suplierID.toString(), it)
                }
            }


            if (!mUtils.checkInternetConnection(mView.context)) {
                realm.executeTransaction {
                    borderauList= realm.where(BodereuLogListing::class.java).equalTo("forestuniqueId", forestUniqueId).findAll()
                    Log.e("borderauList", "is " + borderauList.size)

                    logsLiting.addAll(realm.copyFromRealm(borderauList))
                    adapter.notifyDataSetChanged()
                    setTotalNoOfLogs(logsLiting.size)
                }
            }else{
                getBodereuLogsByID(bodereuHeaderId.toString())
            }
        }
    }


    fun setupTransportMode(transportMode: Int, context: Context) {
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
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setupRecyclerViewNAdapter() {
        logsLiting?.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        rvLogListing.layoutManager = mLinearLayoutManager
        adapter = BoLogsListAdapter(mView.context, logsLiting)
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

        adapter.onEditClick = { modelData, position ->
            showAlerDialog(true, position, mView
                    .context.getString(R.string.are_you_sure_want_to_update_log), "update")
        }

        adapter.onDeleteClick = { modelData, position ->
            showAlerDialog(false, position, mView
                    .context.getString(R.string.are_you_sure_want_to_delete_log), "delete")
        }

        adapter.onPrintClick = { modelData, position ->
            showAlerDialog(false, position, mView
                    .context.getString(R.string.are_you_sure_want_to_print_log), "print")
        }
    }


    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.bordereau_module)
        (activity as HomeActivity).invisibleFilter()
    }


    private fun setTotalNoOfLogs(count: Int) {
        if (count == 0) {
            mView.linFooter.visibility =  View.INVISIBLE
            mView.tvNoDataFound.visibility=View.VISIBLE
            mView.rvLogListing.visibility=View.GONE
            mView.txtTotalLogs.visibility =View.INVISIBLE
            mView.txtTotalLogs.text = getString(R.string.total_found, count)//"Total $count Found"
        } else {
            mView.linFooter.visibility =  View.VISIBLE
            mView.tvNoDataFound.visibility=View.GONE
            mView.rvLogListing.visibility=View.VISIBLE
            mView.txtTotalLogs.visibility =View.VISIBLE
            mView.txtTotalLogs.text =getString(R.string.total_found, count) //"Total $count Found"
        }

    }

    fun deleteLogsEntry(position: Int) {
        logsLiting.removeAt(position)
        adapter.notifyDataSetChanged()
        setTotalNoOfLogs(logsLiting.size)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showAddLogDialog(isEditLog: Boolean, position: Int) {
        diatype="Under bark"
        val inflater =
            mView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        //val alertLayout: View = inflater.inflate(R.layout.dialog_add_log_layout, null)
        val alertLayout: View = inflater.inflate(R.layout.dialog_add_log_layout, null)
        alertView = alertLayout
        val alert: AlertDialog.Builder =
            AlertDialog.Builder(mView.context, R.style.CustomDialog)
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout)
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false)

        alertLayout.linearCustomer.visibility=View.VISIBLE

        offlineForestSelection()
        if (isEditLog) {
            setEditDataOnAddLogDiolog(alertLayout, position)
        }
        alertLayout.linConfirmNPrint.setOnClickListener {
            if (mUtils?.checkInternetConnection(mView.context) == true) {
                acessRuntimPermission(isEditLog, position, alertLayout)
            }else{
                acessRunofflinePermission(isEditLog, position, alertLayout)
            }
        }

        alertLayout.radioGp.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(radioGroup: RadioGroup, i: Int) {
                when (i) {

                    R.id.under_bark -> {
                        Log.e("under_bark", "under_bark")
                        diatype = "Under bark"
                    }

                    R.id.under_sap -> {
                        Log.e("under_sap", "under_sap")
                        diatype = "UnderSap"
                    }

                }
            }
        })

        alertLayout.edt_log2?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

            }


            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                try {
                    if (!alertLayout.edt_log2?.text.isNullOrEmpty()) {
                        if (4 < alertLayout.edt_log2?.text.toString().toInt()) {
                            mUtils.showAlert(activity, mView.resources.getString(R.string.after_slash_should_not_allow_more_than_four))
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
                            mUtils.showAlert(activity, mView.resources.getString(R.string.long_value_greater_than_dia))
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
        alertLayout.edtEssence.setOnClickListener{
            if (mUtils.checkInternetConnection(mView.context)) {
                showDialog(commonForestMaster?.getSpecies() as java.util.ArrayList<SupplierDatum?>?, "essence")
            }else{
                showDialog(essenceList, "essence")
            }
        }

        alertLayout.linCustomerName.setOnClickListener{
             if (mUtils.checkInternetConnection(mView.context)) {
                 showDialog(commonForestMaster?.getCCustomerData() as java.util.ArrayList<SupplierDatum?>?, "customer")
            }else{
                 showDialog(customerList, "customer")
            }

        }

        alertLayout.edtQuality.setOnClickListener{

         if (mUtils.checkInternetConnection(mView.context)) {
             showDialog(commonForestMaster?.getQualityData() as java.util.ArrayList<SupplierDatum?>?, "quality")
            }else{
             showDialog(qualityList, "quality")
            }
        }

        alertLayout.edtAAC.setOnClickListener{

             if (mUtils.checkInternetConnection(mView.context)) {
                 showDialog(commonForestMaster?.getAacList() as java.util.ArrayList<SupplierDatum?>?, "aac")
            }else{
                 showDialog(aaclist, "aac")
            }
        }

        alertLayout.ivLogCancel.setOnClickListener{
            logDialog?.dismiss()
        }


        logDialog = alert.create()
        logDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logDialog?.show()
    }

    fun offlineForestSelection(){
        aaclist!!.clear()
        essenceList!!.clear()
        customerList!!.clear()
        qualityList!!.clear()


        realm.executeTransactionAsync { bgRealm ->
            val aacMaster: RealmResults<Aac> = bgRealm.where(Aac::class.java).equalTo("uid", originID).findAll()
            Log.e("OriginMaster", "is " + aacMaster.size)
            for(transpoter in aacMaster){
                var transporterDatum=SupplierDatum()
                transporterDatum.optionValueString=transpoter.optionValueString
                transporterDatum.optionValue=transpoter.optionValue
                transporterDatum.optionName=transpoter.optionName
                aaclist?.add(transporterDatum)
            }
        }


        realm.executeTransactionAsync { bgRealm ->
            val speciesMaster: RealmResults<Species> = bgRealm.where(Species::class.java).findAll()
            Log.e("FscMasterDatum", "is " + speciesMaster.size)
            for(transpoter in speciesMaster){
                var transporterDatum=SupplierDatum()
                transporterDatum.optionValueString=transpoter.optionValueString
                transporterDatum.optionValue=transpoter.optionValue
                transporterDatum.optionName=transpoter.optionName
                essenceList?.add(transporterDatum)
            }
        }

        realm.executeTransactionAsync { bgRealm ->
            val qualityDatum: RealmResults<QualityDatum> = bgRealm.where(QualityDatum::class.java).findAll()
            Log.e("leauChargementDatum", "is " + qualityDatum.size)
            for(transpoter in qualityDatum){
                var transporterDatum=SupplierDatum()
                transporterDatum.optionValueString=transpoter.optionValueString
                transporterDatum.optionValue=transpoter.optionValue
                transporterDatum.optionName=transpoter.optionName
                qualityList?.add(transporterDatum)
            }
        }

        realm.executeTransactionAsync { bgRealm ->
            val customerDatum: RealmResults<CustomerDatum> = bgRealm.where(CustomerDatum::class.java).findAll()
            Log.e("leauChargementDatum", "is " + customerDatum.size)
            for(transpoter in customerDatum){
                var transporterDatum=SupplierDatum()
                transporterDatum.optionValueString=transpoter.optionValueString
                transporterDatum.optionValue=transpoter.optionValue
                transporterDatum.optionName=transpoter.optionName
                customerList?.add(transporterDatum)
            }
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
            val doublelCBM  = finalResult.toString().replace(",", ".")
            alertView.edtCBM.setText(doublelCBM)

        }catch (e: Exception){
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

        alertLayout.edtPlaque_no.setText(logsLiting.get(position).getPlaqNo())
        alertLayout.edtEssence.setText(logsLiting.get(position).getLogSpeciesName())

        if(logsLiting.get(position).getLogSpeciesName().equals("OKOUME")){
            diatype=""
            alertLayout.linear_data.visibility=View.GONE

        }else{
            try {
                diatype= logsLiting?.get(position)?.getdiaType()!!
            }catch (e: java.lang.Exception){
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
        qualityId= logsLiting.get(position).getQualityId()!!
        aacID =  logsLiting.get(position).getAAC()!!
        try {
            uniqueId = logsLiting.get(position).getUniqueId()!!
            forestuniqueId = logsLiting.get(position).getForestUnqiueId()!!
        }catch (e:Exception)
        {
            e.printStackTrace()
        }
        aacYear =   logsLiting.get(position).getAACYear()!!
        alertLayout.edtAAC.setText(logsLiting.get(position).getAACName())
        alertLayout.edtQuality.setText(logsLiting.get(position).getQuality().toString())

        if(logsLiting.get(position).getcustomerPurchasedFromForest()!=null) {
            alertLayout.txtCustomerName.setText(logsLiting.get(position).getcustomerPurchasedFromForest())
        }

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
        alertLayout.edtCBM.setText((logsLiting.get(position).getCbm().toString()))
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun disableLogNonEditableFiled(alertLayout: View){
        alertLayout.edt_log.isEnabled = false
        alertLayout.edt_log2.isEnabled = false
        alertLayout.edtAAC.isEnabled = false
        alertLayout.edt_log.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it, R.color.gray_200) }
        alertLayout.edt_log2.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it, R.color.gray_200) }
        alertLayout.edtAAC.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it, R.color.gray_200) }
        alertLayout.linLogNumber.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout.linLogNumber2.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout.linLogAAC.setBackgroundResource(R.drawable.bg_for_editext_non_editable)

    }

    fun acessRuntimPermission(isEditLog: Boolean, position: Int, alertLayout: View) {
        val permissionListener: PermissionListener = object : PermissionListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionGranted() {
                var isLogAlreadyExit : Boolean = false
                 LogModel  = BodereuLogListing()
                if (isValidateAddLog(alertLayout)) {
                    if (isEditLog) {
                        logsLiting.get(position).setLogNo(alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString())
                        logsLiting.get(position).setPlaqNo(alertLayout.edtPlaque_no.text.toString())
                        logsLiting.get(position).setLogSpeciesName(alertLayout.edtEssence.text.toString())
                        logsLiting.get(position).setAACName(alertLayout.edtAAC.text.toString())
                        logsLiting.get(position).setdiaType(diatype)
                        logsLiting.get(position).setAAC(aacID)
                        logsLiting.get(position).setAACYear(aacYear)
                        logsLiting.get(position).setLogSpecies(speciesID)
                        logsLiting.get(position).setQualityId(qualityId)
                        logsLiting.get(position).setLogRecordDocNo(mUtils.createLogRecordDocsNumber(bodereuNumber, alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString(), alertLayout.edtDia.text.toString()))
                        logsLiting.get(position).setQuality(alertLayout.edtQuality.text?.toString())

                        logsLiting.get(position).setcustomerPurchasedFromForest(alertLayout.txtCustomerName.text.toString())


                        logsLiting.get(position).setDiamBdx1(alertLayout.edtDia.text?.toString()?.toInt())
                        logsLiting.get(position).setDiamBdx2(alertLayout.edtDia2.text?.toString()?.toInt())
                        logsLiting.get(position).setDiamBdx3(alertLayout.edtDia3.text?.toString()?.toInt())
                        logsLiting.get(position).setDiamBdx4(alertLayout.edtDia4.text?.toString()?.toInt())

                        logsLiting.get(position).setDiamBdx(Math.round(alertLayout.edtAvgDia.text?.toString()?.toDouble()!!).toInt())


                       // logsLiting.get(position).fscMode=alertLayout.txtFSC.text.toString()

                        /* logsLiting?.get(position)?.setDetailId("")*/
                        logsLiting.get(position).setLongBdx(alertLayout.edtLong?.text?.toString()?.toInt())
                        logsLiting.get(position).setCbm(alertLayout.edtCBM.text?.toString()?.toDouble())
                     /*   logsLiting.get(position).setBarcodeNumber(mUtils.getRandomNumberString(""))*/
                        logsLiting.get(position).setBarcodeNumber(logsLiting.get(position).getBarcodeNumber())
                        LogModel = logsLiting.get(position)

                    } else {
                        var request: BodereuLogListing = BodereuLogListing()
                        request.setLogNo(alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString())
                        request.setPlaqNo(alertLayout.edtPlaque_no.text.toString())
                        request.setLogSpeciesName(alertLayout.edtEssence.text.toString())
                        request.setLogSpecies(speciesID)
                        request.setdiaType(diatype)
                        request.setQualityId(qualityId)
                        request.setAACName(alertLayout.edtAAC.text.toString())
                        request.setAAC(aacID)
                        request.setAACYear(aacYear)
                        request.setLogRecordDocNo(mUtils.createLogRecordDocsNumber(bodereuNumber, alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString(), alertLayout.edtDia.text.toString()))
                        request.setQuality(alertLayout.edtQuality?.text?.toString())

                        if(alertLayout.txtCustomerName.text.toString().equals("Gesz")){
                            request.setcustomerPurchasedFromForest("")
                        }else{
                            request.setcustomerPurchasedFromForest(alertLayout.txtCustomerName.text.toString())
                        }

                        request.setDiamBdx1(alertLayout.edtDia.text?.toString()?.toInt())
                        request.setDiamBdx2(alertLayout.edtDia2.text?.toString()?.toInt())
                        request.setDiamBdx3(alertLayout.edtDia3.text?.toString()?.toInt())
                        request.setDiamBdx4(alertLayout.edtDia4.text?.toString()?.toInt())
                        request.setDiamBdx(Math.round(alertLayout.edtAvgDia.text?.toString()?.toDouble()!!).toInt())

                        //request.fscMode=alertLayout.txtFSC.text.toString()

                        request.setLongBdx(alertLayout.edtLong.text?.toString()?.toInt())
                        request.setDetailId("0")
                        request.setCbm(alertLayout.edtCBM.text?.toString()?.toDouble())
                       // request.setCbm(mUtils.intToDouble(alertLayout.edtCBM.text?.toString()?.toInt()))

                        try {
                            var supplierid:Int=suplierID.toInt()

                            /*if (text.length() > 3) {
                                d = text.substring(4);
                            }*/

                            var str = String.format("%03d", supplierid)
                            var uniqString = str.toString() + alertLayout.edt_log.text.toString() + alertLayout.edt_log2.text.toString()

                            Log.e("barcode", "is" + uniqString)
                            request.setBarcodeNumber(mUtils.getRandomNumberString(uniqString))

                        }catch (e: java.lang.Exception){
                            e.printStackTrace()
                        }

                        if(checkLogAACNLogNoAlreadyExits(alertLayout.edtAAC.text.toString(), alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString())) {
                            LogModel = request
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
                            callingAddBordereauAPI("Save", true)
                        }else{
                            //LogModel.getLogNo()?.let { callingCheckLogNumberDuplication(it) }
                            logsLiting?.add(LogModel)
                            setTotalNoOfLogs(logsLiting.size)
                            callingAddBordereauAPI("Save", true)
                           // callingAddBordereauAPI("Save",true)
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

    fun acessRunofflinePermission(isEditLog: Boolean, position: Int, alertLayout: View) {
        val permissionListener: PermissionListener = object : PermissionListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionGranted() {
                var isLogAlreadyExit : Boolean = false
                LogModel  = BodereuLogListing()
                if (isValidateAddLog(alertLayout)) {
                    if (isEditLog) {

                        try {
                            logsLiting.get(position).setUniqueId(uniqueId)
                            logsLiting.get(position).setForestUnqiueId(forestuniqueId)
                        }catch (e:Exception){
                            e.printStackTrace()
                        }


                        logsLiting.get(position).fscMode=fscOrNonFsc
                        logsLiting.get(position).setBordereauNo(bodereuNumber)
                        logsLiting.get(position).setSupplierName(supplierLocationName)


                        logsLiting.get(position).setLogNo(alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString())
                        logsLiting.get(position).setPlaqNo(alertLayout.edtPlaque_no.text.toString())
                        logsLiting.get(position).setLogSpeciesName(alertLayout.edtEssence.text.toString())
                        logsLiting.get(position).setAACName(alertLayout.edtAAC.text.toString())
                        logsLiting.get(position).setdiaType(diatype)
                        logsLiting.get(position).setAAC(aacID)
                        logsLiting.get(position).setAACYear(aacYear)
                        logsLiting.get(position).setLogSpecies(speciesID)
                        logsLiting.get(position).setQualityId(qualityId)
                        logsLiting.get(position).setLogRecordDocNo(mUtils.createLogRecordDocsNumber(bodereuNumber, alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString(), alertLayout.edtDia.text.toString()))
                        logsLiting.get(position).setQuality(alertLayout.edtQuality.text?.toString())
                        logsLiting.get(position).setcustomerPurchasedFromForest(alertLayout.txtCustomerName.text.toString())
                        logsLiting.get(position).setDiamBdx1(alertLayout.edtDia.text?.toString()?.toInt())
                        logsLiting.get(position).setDiamBdx2(alertLayout.edtDia2.text?.toString()?.toInt())
                        logsLiting.get(position).setDiamBdx3(alertLayout.edtDia3.text?.toString()?.toInt())
                        logsLiting.get(position).setDiamBdx4(alertLayout.edtDia4.text?.toString()?.toInt())

                        logsLiting.get(position).setDiamBdx(Math.round(alertLayout.edtAvgDia.text?.toString()?.toDouble()!!).toInt())
                        logsLiting.get(position).setAvrageBdx(Math.round(alertLayout.edtAvgDia.text?.toString()?.toDouble()!!).toInt())


                        /* logsLiting?.get(position)?.setDetailId("")*/
                        logsLiting.get(position).setLongBdx(alertLayout.edtLong?.text?.toString()?.toInt())
                        logsLiting.get(position).setCbm(alertLayout.edtCBM.text?.toString()?.toDouble())
                        /*   logsLiting.get(position).setBarcodeNumber(mUtils.getRandomNumberString(""))*/
                        logsLiting.get(position).setBarcodeNumber(logsLiting.get(position).getBarcodeNumber())
                        LogModel = logsLiting.get(position)

                    } else {
                        var request: BodereuLogListing = BodereuLogListing()
                        request.uniqueId=System.currentTimeMillis().toString()
                        Log.e("forestUniqueId", "is " + forestUniqueId)
                        request.forestuniqueId=forestUniqueId
                        request.fscMode=fscOrNonFsc
                        request.setBordereauNo(bodereuNumber)
                        request.setSupplierName(supplierLocationName)
                        request.setLogNo(alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString())
                        request.setPlaqNo(alertLayout.edtPlaque_no.text.toString())
                        request.setLogSpeciesName(alertLayout.edtEssence.text.toString())
                        request.setLogSpecies(speciesID)
                        request.setdiaType(diatype)
                        request.setQualityId(qualityId)
                        request.setAACName(alertLayout.edtAAC.text.toString())
                        request.setAAC(aacID)
                        request.setAACYear(aacYear)
                        request.setBordereauHeaderId(bodereuHeaderId)
                        request.setLogRecordDocNo(mUtils.createLogRecordDocsNumber(bodereuNumber, alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString(), alertLayout.edtDia.text.toString()))
                        request.setQuality(alertLayout.edtQuality?.text?.toString())

                        if(alertLayout.txtCustomerName.text.toString().equals("Gesz")){
                            request.setcustomerPurchasedFromForest("")
                        }else{
                            request.setcustomerPurchasedFromForest(alertLayout.txtCustomerName.text.toString())
                        }

                        request.setDiamBdx1(alertLayout.edtDia.text?.toString()?.toInt())
                        request.setDiamBdx2(alertLayout.edtDia2.text?.toString()?.toInt())
                        request.setDiamBdx3(alertLayout.edtDia3.text?.toString()?.toInt())
                        request.setDiamBdx4(alertLayout.edtDia4.text?.toString()?.toInt())
                        request.setDiamBdx(Math.round(alertLayout.edtAvgDia.text?.toString()?.toDouble()!!).toInt())
                        request.setAvrageBdx(Math.round(alertLayout.edtAvgDia.text?.toString()?.toDouble()!!).toInt())

                        request.setLongBdx(alertLayout.edtLong.text?.toString()?.toInt())
                        request.setDetailId("0")

                        try {
                            request.setCbm(alertLayout.edtCBM.text?.toString()?.toDouble())
                        }catch (e:java.lang.Exception){
                            e.printStackTrace()
                        }

                        // request.setCbm(mUtils.intToDouble(alertLayout.edtCBM.text?.toString()?.toInt()))

                        try {
                            var supplierid:Int=suplierID.toInt()

                            request.supplier=supplierid

                            /*if (text.length() > 3) {
                                d = text.substring(4);
                            }*/

                            var str = String.format("%03d", supplierid)
                            var uniqString = str.toString() + alertLayout.edt_log.text.toString() + alertLayout.edt_log2.text.toString()

                            Log.e("barcode", "is" + uniqString)
                            request.setBarcodeNumber(mUtils.getRandomNumberString(uniqString))

                        }catch (e: java.lang.Exception){
                            e.printStackTrace()
                        }

                        Log.e("requestforestUniqueId", "is " + request.forestuniqueId + LogModel.forestuniqueId)

                        if(checkLogAACNLogNoAlreadyExits(alertLayout.edtAAC.text.toString(), alertLayout.edt_log.text.toString() + "/" + alertLayout.edt_log2.text.toString())) {
                            LogModel = request

                            realm.executeTransactionAsync({ bgRealm ->

                                var borderauRequest = bgRealm.where(BorderauRequest::class.java).equalTo("uniqueId", forestUniqueId).findFirst()
                                var logcount = borderauRequest?.logCount
                                borderauRequest!!.logCount = logcount!!+1
                                bgRealm.copyToRealmOrUpdate(borderauRequest)

                            }, {
                                Log.e("Success", "Success logs update")
                                realm.executeTransactionAsync { bgRealm ->
                                    bgRealm.copyToRealmOrUpdate(LogModel)
                                }
                            }) {
                                Log.e("faile", "failed logs")
                            }


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
                            realm.executeTransactionAsync({ bgRealm ->
                                bgRealm.copyToRealmOrUpdate(LogModel)
                            })
                           // callingAddBordereauAPI("Save", true)
                        }else{
                            //LogModel.getLogNo()?.let { callingCheckLogNumberDuplication(it) }
                            logsLiting?.add(LogModel)
                            setTotalNoOfLogs(logsLiting.size)
                          //  callingAddBordereauAPI("Save", true)
                            // callingAddBordereauAPI("Save",true)

                        }
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
        }*/ else if (alertLayout.edtEssence?.text.toString()==getResources().getString(R.string.select)) {
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


    fun checkLogAACNLogNoAlreadyExits(aacName: String?, logNumber: String):Boolean{
        for(listdata in logsLiting){
            if(!aacName.isNullOrEmpty() && !logNumber.isNullOrEmpty())
                if (listdata.getLogNo().toString().contains(logNumber) && listdata.getAACName().toString().contains(aacName!!)) {
                    return false
                }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun showAlerDialog(isEditLog: Boolean, position: Int, msg: String, action: String){
    val alert: AlertDialog =
        AlertDialog.Builder(view?.context, R.style.CustomDialogWithBackground)
            //.setTitle(activity?.getString(R.string.app_name))
            .setMessage(msg)
            .setPositiveButton("Ok"
            ) { dialog, which ->
                dialog.dismiss()
                when(action){
                    "delete" -> {
                        if (logsLiting?.get(position)?.getDetailId().equals("0")) {
                            if (mUtils?.checkInternetConnection(mView.context) == false) {
                                realm.executeTransaction({
                                    borderauList.get(position)!!.deleteFromRealm()
                                    deleteLogsEntry(position)
                                })

                            }else{
                                deleteLogsEntry(position)
                            }
                        } else {
                            callingdeleteBordereauLogAPI(position)
                        }
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
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    alert.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE)
    alert.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE)

    val positive: Button =
        alert.getButton(DialogInterface.BUTTON_POSITIVE)
    positive.setTextColor(ContextCompat.getColor(mView.context, R.color.colorPrimaryDark))

    val negative: Button =
        alert.getButton(DialogInterface.BUTTON_NEGATIVE)
    negative.setTextColor(ContextCompat.getColor(mView.context, R.color.colorPrimaryDark))
}



    fun generateValidateLogRecorNumberRequest(logRecordDocNo: String): ValidateBodereueNoReq {

        val request  =   ValidateBodereueNoReq()
        request.setLogRecordDocNo(logRecordDocNo)
        request.setBordereauHeaderId(bodereuHeaderId.toString())


        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }



    fun generateAddBodereuLogListRequest(action: String): AddBoereuLogListingReq {

        var request : AddBoereuLogListingReq =   AddBoereuLogListingReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(bodereuHeaderId)

        request.setdiaType(diatype)


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
                                        callingAddBordereauAPI("Save", true)
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



    fun getLogMasterRequest(forestId: String, originID: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setForestId(forestId)
        request.setOriginId(originID)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    private fun callingLogMasterAPI(forestId: String, originID: String) {
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
        }else{
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }
    private fun callingValidateBordereauNoAPI(logRecorNumber: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = generateValidateLogRecorNumberRequest(logRecorNumber)
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


    private fun callingAddBordereauAPI(action: String, isSinglePrint: Boolean) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = generateAddBodereuLogListRequest(action)
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
                                        if (action.equals("Submit", ignoreCase = true)) {
                                            getAllLogDataForLoadingWagons()
                                            var fragment = TodaysHistoryFragment()
                                            (activity as HomeActivity).replaceFragment(fragment, true)
                                        } else {
                                            getBodereuLogsByID(bodereuHeaderId.toString())
                                            //if it is save from popup then go to print barcode
                                            if (isSinglePrint) {
                                                buttonGenerate_onClick(LogModel)
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
        }else{
            mUtils.showToast(view?.context, getString(R.string.no_internet))
        }
    }

    private fun getAllLogDataForLoadingWagons() {
        try {
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
            val commonRequest= CommonRequest()
            commonRequest.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
            val call: Call<LogOfflineResponse> = apiInterface.getAllLogDataForLoadingWagons(commonRequest)
            call.enqueue(object :
                Callback<LogOfflineResponse> {
                override fun onResponse(
                    call: Call<LogOfflineResponse>,
                    response: Response<LogOfflineResponse>
                ) {
                    try {
                        if (response.code() == 200) {

                            var offlineResponse = response.body()
                            if (offlineResponse != null) {
                                logslist.clear()

                                realm.executeTransactionAsync({ bgRealm ->

                                    bgRealm.delete(LogDetail::class.java)
                                }, {


                                    Log.e("Success", "Success delete")

                                    realm.executeTransactionAsync({ bgRealm ->

                                        for (i in offlineResponse.logDetails!!.indices!!) {
                                            offlineResponse.logDetails!!.get(i).id = i

                                            logslist.add(offlineResponse.logDetails!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(logslist)

                                    }, {
                                        Log.e("Success", "Success logs")
                                    }) {
                                        Log.e("faile", "failed logs")
                                    }
                                }) {
                                    Log.e("faile", "failed delte")
                                }
                            }

                        } else if (response.code() == 306) {
                            // mUtils.alertDialgSession(this@DashboardActivity, this@DashboardActivity)
                        } else {
                            // mUtils.showToast(this@DashboardActivity, Constants.SOMETHINGWENTWRONG)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                    call: Call<LogOfflineResponse>,
                    t: Throwable
                ) {
                    //  mUtils.showToast(this@DashboardActivity, Constants.SERVERTIMEOUT)
                    // mUtils.dismissProgressDialog()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun callingdeleteBordereauLogAPI(position: Int) {
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
        }else{
            mUtils.showToast(view?.context, getString(R.string.no_internet))
        }
    }


    fun getBodereuLogsByIDRequest(bodereuHeaderId: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setHeaderId(bodereuHeaderId)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun getBodereuLogsByID(bodereuHeaderId: String) {
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
                                if (response.body().getSeverity() == 200) {
                                    val responce: GetBodereuLogByIdRes =
                                            response.body()!!
                                    if (responce != null) {
                                        logsLiting?.clear()
                                        responce.getBordereauLogList()?.let {
                                            logsLiting?.addAll(
                                                    it
                                            )
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
        }else{
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }


    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun  buttonGenerate_onClick(logModel: BodereuLogListing) {
        try {

            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            if (!mBluetoothAdapter.isEnabled) {
                mBluetoothAdapter.enable()
            }

//            mView.chkBCNoFSC.isChecked =  false
//            mView.chkFSC.isChecked =  false`
            if (!fscOrNonFsc.isNullOrEmpty()){
                if(fscOrNonFsc.equals("NO FSC", ignoreCase = true)){
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

            mView.parentView.setDrawingCacheEnabled(true)
            mView.parentView.buildDrawingCache()
            val bitmap = mView.parentView.getDrawingCache()
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageInByte: ByteArray = baos.toByteArray()
            val image = Image.getInstance(imageInByte)
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
            //Toast.makeText(mView.context, "$mFileName.pdf\nis saved to\n$FULL_PATH", Toast.LENGTH_SHORT).show()
            printPDF(FULL_PATH)
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

            mView.parentView.setDrawingCacheEnabled(true)
            mView.parentView.buildDrawingCache()
            val bitmap = mView.parentView.getDrawingCache()
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageInByte: ByteArray = baos.toByteArray()
            val image = Image.getInstance(imageInByte)

            //add author of the document (metadata)
            mDoc.addAuthor("OLAM")

            val scaler = ((mDoc.getPageSize().getWidth() - mDoc.leftMargin()
                    - mDoc.rightMargin() - 0) / image.getWidth()) * 100;
            image.scalePercent(scaler);
            image.alignment = Image.ALIGN_CENTER*//* | Image.ALIGN_TOP);*//*

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

    fun printPDF(pdfPath: String){
        val printManager =
            mView.context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        /*val printAdapter =
            PdfDocumentAdapter(this@PrintActivity, "/storage/emulated/0/Download/test.pdf")*/
        val printAdapter =
            PdfDocumentAdapter(mView.context, pdfPath)
        printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
    }

    fun  checkIsFragmentDialogAlreadyShowing():Boolean{
        if(isDialogShowing){
            return false
        }
        return true
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
        }catch (e: Exception){
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
          //  mView.txtBordero_No.clearFocus()
        }catch (e: Exception){
            e.printStackTrace()
        }
        if (model != null) {
            try{
                when (action) {

                    "essence" -> {
                        alertView.edtEssence?.setText(model.optionName)
                        speciesID = model.optionValue!!
                        alertView.edtDia?.requestFocus()

                        if (model.optionName.equals("OKOUME")) {
                            alertView.linear_data.visibility = View.GONE
                            diatype = ""
                        } else {
                            alertView.linear_data.visibility = View.VISIBLE
                        }
                    }
                    "aac" -> {
                        alertView.edtAAC?.setText(model.optionName)
                        aacID = model.optionValue!!
                        aacYear = model.optionValueString!!
                        alertView.edt_log?.requestFocus()
                    }
                    "quality" -> {
                        alertView.edtQuality?.setText(model.optionName)
                        qualityId = model.optionValue!!
                        alertView.edtQuality?.requestFocus()
                    }

                    "customer" -> {
                        alertView.txtCustomerName?.setText(model.optionName)
                        customerId = model.optionValue!!
                        alertView.txtCustomerName?.requestFocus()
                    }
                }

            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    override fun onCancleDialog() {
        try {
            countryDialogFragment.dismiss()
            isDialogShowing = false
        }catch (e: Exception){
            e.printStackTrace()

        }
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivBOAdd -> {
                //user should be add only 20 logs
                if (logsLiting.size != 20) {
                    showAddLogDialog(false, 0)
                }
            }
            R.id.txtSave -> {
                if (logsLiting.size != 0) {
                    callingAddBordereauAPI("Save", false)
                }
            }

            R.id.txtSubmit -> {
                if (logsLiting.size != 0) {
                    if (mUtils?.checkInternetConnection(mView.context) == true) {
                        callingAddBordereauAPI("Submit", false)
                    }else{

                        var logDetailResult = realm.where(LogDetail::class.java).findAll()
                        var id=logDetailResult.size


                        logsDetailsLiting.clear()
                        var uniqid = id
                        for (loglist in logsLiting){
                            var logDetail=LogDetail()
                            uniqid=uniqid+1
                            logDetail.id=uniqid
                            logDetail.logNo=loglist.logNo
                            logDetail.barcodeNumber=loglist.getBarcodeNumber()
                            logDetail.plaqNo=loglist.getPlaqNo()
                            logDetail.longBdx=loglist.getLongBdx()
                            logDetail.materialDesc=loglist.getMaterialDesc()
                            logDetail.detailId= loglist.getDetailId()!!.toInt()
                            logDetail.supplier=loglist.supplier
                            logDetail.supplierName=loglist.getSupplierName()
                            logDetail.logSpecies=loglist.logSpecies
                            logDetail.logRecordDocNo=loglist.logRecordDocNo
                            logDetail.quality=loglist.getQuality()
                            logDetail.aacName=loglist.aacName
                            logDetail.wagonNo=loglist.getWagonNo()
                            logDetail.aacId=loglist.aacId
                            logDetail.diaType=loglist.diaType
                            logDetail.qualityId=loglist.getQualityId()
                            logDetail.fscMode=loglist.fscMode
                            logDetail.cbm=loglist.cbm
                            logDetail.bordereauHeaderId=loglist.getBordereauHeaderId()
                            logDetail.logSpeciesName=loglist.getLogSpeciesName()
                            logDetail.diam1Bdx=loglist.getDiamBdx1()
                            logDetail.diam2Bdx=loglist.getDiamBdx2()
                            logDetail.diam3Bdx=loglist.getDiamBdx3()
                            logDetail.diam4Bdx=loglist.getDiamBdx4()
                            logDetail.diamBdx=loglist.getDiamBdx()
                            logDetail.aacYear=loglist.getAACYear()
                            logDetail.logSpeciesName=loglist.getLogSpeciesName()
                            logDetail.loadingStatus= "Loaded"
                            logsDetailsLiting.add(logDetail)
                        }

                       /* realm.executeTransaction {

                        }*/

                        realm.executeTransactionAsync({ bgRealm ->
                            var borderauRequest = bgRealm.where(BorderauRequest::class.java).equalTo("uniqueId", forestUniqueId).findFirst()
                            borderauRequest!!.mode=Constants.SUBMIT
                            borderauRequest.headerStatus="At-Hub"
                            bgRealm.copyToRealmOrUpdate(borderauRequest)
                            bgRealm.copyToRealmOrUpdate(logsDetailsLiting)
                        }, {
                            mUtils.showAlert(requireActivity(),getString(R.string.bordereau_submit_successful))
                            requireActivity().onBackPressed()
                            Log.e("Success","Success")
                        }) {
                            Log.e("failed","failed")
                        }


                        val handler = Handler()
                        handler.postDelayed(Runnable { // Do something after 5s = 5000ms

                        }, 1000)
                    }
                }
            }
            R.id.ivHeaderEdit -> {
                if (commigFrom.equals(Constants.header, ignoreCase = true)) {
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
                    (activity as HomeActivity).replaceFragment(fragment, false)

                } else {
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
                    (activity as HomeActivity).replaceFragment(fragment, false)
                }
            }
        }
    }

}
