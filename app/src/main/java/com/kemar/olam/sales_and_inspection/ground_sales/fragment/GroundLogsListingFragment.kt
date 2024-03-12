package com.kemar.olam.sales_and_inspection.ground_sales.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.ContactsContract
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kemar.olam.R
import com.kemar.olam.bordereau.fragment.AddHeaderFragment
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.request.BoderueDeleteLogReq
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
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.retrofit.ApiClientMultiPart
import com.kemar.olam.sales_and_inspection.ground_sales.adapter.GroundLogsDetailsAdapter
import com.kemar.olam.sales_and_inspection.inspection.fragment.MainContainerFragment
import com.kemar.olam.utility.*
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.dialog_add_log_layout.view.*
import kotlinx.android.synthetic.main.fragment_ground_logs_listing.view.*
import kotlinx.android.synthetic.main.layout_add_sales_log.view.*
import kotlinx.android.synthetic.main.layout_add_sales_log.view.edtAAC
import kotlinx.android.synthetic.main.layout_add_sales_log.view.edtCBM
import kotlinx.android.synthetic.main.layout_add_sales_log.view.edtDia
import kotlinx.android.synthetic.main.layout_add_sales_log.view.edtEssence
import kotlinx.android.synthetic.main.layout_add_sales_log.view.edtLong
import kotlinx.android.synthetic.main.layout_add_sales_log.view.edtPlaque_no
import kotlinx.android.synthetic.main.layout_add_sales_log.view.edtQuality
import kotlinx.android.synthetic.main.layout_add_sales_log.view.edt_log
import kotlinx.android.synthetic.main.layout_add_sales_log.view.edt_log2
import kotlinx.android.synthetic.main.layout_add_sales_log.view.ivLogCancel
import kotlinx.android.synthetic.main.layout_add_sales_log.view.linLogAAC
import kotlinx.android.synthetic.main.layout_add_sales_log.view.linLogNumber
import kotlinx.android.synthetic.main.layout_add_sales_log.view.linLogNumber2
import kotlinx.android.synthetic.main.layout_digital_signature.view.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

class GroundLogsListingFragment : Fragment() ,View.OnClickListener, DialogFragment.GetDialogListener {

    private val DelayTime = 500
    lateinit var mUtils: Utility
    lateinit var mView: View
    var FULL_PATH: String = ""
    var logDialog: AlertDialog? = null
    var SignDialog: AlertDialog? = null
    lateinit var rvLogListing: RecyclerView
    lateinit var adapter: GroundLogsDetailsAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var groundLogsLiting: ArrayList<BodereuLogListing> = arrayListOf()

    //create local master based on list
    var isForestSelected : Boolean = false
    var forestLocalMaster :  ArrayList<SupplierDatum> = arrayListOf()
    var isLogNoSelected : Boolean = false
    var lognoLocalMaster   :  ArrayList<SupplierDatum> = arrayListOf()


    //listings
    var commonForestMaster: GetForestDataRes? = GetForestDataRes()
    var isDialogShowing: Boolean = false
    lateinit var commonDialogFragment: DialogFragment
    lateinit var alertView: View
    var essenceID: Int = 0
    var commigFrom = ""
    var supplierLocationName = ""
    var originName = ""
    var forestID = 0
    var suplierID = ""
    var originID: Int? = 0
    var bodereuNumber = ""
    var speciesID = 0
    var qualityId: Int? = 0
    var aacID = 0
    var fscOrNonFsc: String = ""
    var supplierShortName: String? = ""
    var transporterName: String? = ""
    var aacYear = ""
    var headerModel: AddBodereuRes.BordereauResponse = AddBodereuRes.BordereauResponse()
    var historyModel: LogsUserHistoryRes.BordereauGroundList =
        LogsUserHistoryRes.BordereauGroundList()
    var todaysHistoryModel: LogsUserHistoryRes.BordereauRecordList =
        LogsUserHistoryRes.BordereauRecordList()
    var countImgeUpload = 0
    var firstImagePath = ""
    var secodnImagepath = ""
    var inspectionId: Int = 0
    var inspectionNumber: String = ""
    lateinit var represemtativeBitmap: Bitmap
    lateinit var customerBitmap: Bitmap

    var headerGradeId: Int? = 0
    var headerGradeName: String = ""

    var gradeId: Int? = 0
    var gradeName: String = ""


    //Scan feature
    var RESULT_CODE = 103;
    val LOG_TAG = "DataCapture1"
    private val bRequestSendResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_ground_logs_listing, container, false)
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
        getDataFromRedirections()
        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()
        setupFooterButtonAsPerUserRole()

        mView.swipeLogListing.setOnRefreshListener {
            mView.swipeLogListing.isRefreshing = false
                mView.txtFilterLogNo.text = mView.resources.getString(R.string.select)
                mView.txtfilterForest.text = mView.resources.getString(R.string.select)

            callingGetMasterDataSalesInspectionAPI()
            getGroundsAllLogs()
        }

        //when comming from headerScreen & User History Screen
        //suplierID == forest
        //
        callingGetMasterDataSalesInspectionAPI()
        getGroundsAllLogs()

    }

    fun getDataFromRedirections(){

        if (commigFrom.equals( Constants.header, ignoreCase = true)) {
            var headerDataModel: AddBodereuRes.BordereauResponse? =
                    arguments?.getSerializable(Constants.badereuModel) as AddBodereuRes.BordereauResponse
            if (headerDataModel != null) {
                headerModel = headerDataModel
                inspectionId = headerModel.inspectionId!!
                inspectionNumber = headerModel.inspectionNO!!
                headerGradeName = headerModel?.gradeName.toString()
                headerGradeId = headerModel?.gradeId
                mView.txtInsspectionNo?.text = headerModel.inspectionNO?.toString()
                mView.txtCustName?.text = headerModel.customerShortName?.toString()
                mView.txtGradeName?.text = headerModel.gradeName?.toString()
            }
        } else {
            var headerDataModel: LogsUserHistoryRes.BordereauGroundList? =
                    arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauGroundList
            if (headerDataModel != null) {

                historyModel = headerDataModel
                headerGradeName = historyModel?.gradeName.toString()
                headerGradeId = historyModel?.gradeId
                inspectionId = historyModel.inspectionId!!
                inspectionNumber = historyModel.inspectionNumber!!
                mView.txtInsspectionNo?.text = historyModel.inspectionNumber?.toString()
                mView.txtCustName?.text = historyModel.customerShortName?.toString()
                mView.txtGradeName?.text = historyModel.gradeName?.toString()
            }

        }

    }


    fun setupFooterButtonAsPerUserRole() {
        when (SharedPref.read(Constants.user_role).toString()) {
            "SuperUserApp", "Super User", "admin" -> {
                mView.txtWaitingForBilling.visibility = View.VISIBLE
                // mView.txtRejectInspection.visibility = View.VISIBLE
                mView.txtWaitingForApproval.visibility = View.GONE

            }
            else -> {
                mView.txtWaitingForApproval.visibility = View.VISIBLE
                mView.txtWaitingForBilling.visibility = View.GONE
                //   mView.txtRejectInspection.visibility = View.GONE
            }
        }


    }
    /* fun setupTransportMode(transportMode:Int, context: Context) {
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
     }*/

    fun setupClickListner() {
       mView.ivBOAdd.setOnClickListener(this)
       mView.txtWaitingForApproval.setOnClickListener(this)
       mView.txtWaitingForBilling.setOnClickListener(this)
        //mView.txtRejectInspection.setOnClickListener(this)
       mView.ivHeaderEdit.setOnClickListener(this)
       mView.txtScan.setOnClickListener(this)
        mView.txtfilterForest.setOnClickListener(this)
        mView.txtFilterLogNo.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setupRecyclerViewNAdapter() {
        groundLogsLiting?.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        rvLogListing.layoutManager = mLinearLayoutManager
        adapter = GroundLogsDetailsAdapter(mView.context, groundLogsLiting, commigFrom)
        rvLogListing.adapter = adapter

        //Expand Collapse Action
        adapter.onMoreClick = { modelData, position, isExpanded ->
            /*if (isExpanded) {
                groundLogsLiting?.get(position)?.isExpanded = true
            } else {
                groundLogsLiting?.get(position)?.isExpanded = false
            }*/
            adapter.notifyDataSetChanged()
        }


        adapter.onCheckBoxClick = {
            selctedRecordMoveToUp()
            adapter.notifyDataSetChanged()
        }

        adapter.onEditClick = { modelData, position ->
            showAlerDialog(
                true, position, mView
                    .context.getString(R.string.are_you_sure_want_to_update_log), "update"
            )
        }

        adapter.onDeleteClick = { modelData, position ->
            showAlerDialog(
                false, position, mView
                    .context.getString(R.string.are_you_sure_want_to_delete_log), "delete"
            )
        }

        adapter.onAddClick = { modelData, position ->
            showAlerDialog(
                true, position, mView
                    .context.getString(R.string.are_you_sure_want_to_add_log_data), "add"
            )
        }
    }

    fun selctedRecordMoveToUp() {
    Collections.sort(groundLogsLiting,
    Comparator
    {
        firstRecord, secondRecord ->
        java.lang.Boolean.compare(
            secondRecord.isSelected,
            firstRecord.isSelected
        )
    })
        //setup index bcz index changed as per move to top
        for((index, listdata)  in groundLogsLiting.withIndex()){
            listdata.setIndex(index)
        }
}

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.inspection_n_sales)
        (activity as HomeActivity).invisibleFilter()
    }


    private fun setTotalNoOfLogs(count: Int) {
        if (count == 0) {
            mView.linFooter.visibility = View.INVISIBLE
            mView.tvNoDataFound.visibility = View.VISIBLE
            mView.rvLogListing.visibility = View.GONE
            mView.txtTotalLogs.visibility = View.INVISIBLE
            mView.txtTotalLogs.text = getString(R.string.total_found,count)//"Total $count Found"
        } else {
            mView.linFooter.visibility = View.VISIBLE
            mView.tvNoDataFound.visibility = View.GONE
            mView.rvLogListing.visibility = View.VISIBLE
            mView.txtTotalLogs.visibility = View.VISIBLE
            mView.txtTotalLogs.text = getString(R.string.total_found,count)//"Total $count Found"
        }

    }

    fun deleteLogsEntry(position: Int) {
        groundLogsLiting.removeAt(position)
        adapter.notifyDataSetChanged()
        //if all logs delete then it will redirect to user history

        if (groundLogsLiting.size == 0) {
            var fragment = MainContainerFragment()
            val bundle = Bundle()
            bundle.putString(
                Constants.comming_from,
                Constants.from_ground
            )
            fragment.arguments = bundle
            (activity as HomeActivity).replaceFragment(fragment, true)
        } else {
            setTotalNoOfLogs(groundLogsLiting.size)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun showAddLogDialog(isEditLog: Boolean, position: Int) {
        val inflater =
            mView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val alertLayout: View = inflater.inflate(R.layout.layout_add_sales_log, null)
        alertView = alertLayout
        val alert: AlertDialog.Builder =
            AlertDialog.Builder(mView.context, R.style.CustomDialog)
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout)
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false)

        if (isEditLog) {
            setEditDataOnAddLogDiolog(alertLayout, position)
        }
        alertLayout.linSaveOrUpdate.setOnClickListener {
            acessRuntimPermission(isEditLog, position, alertLayout)
        }

        alertLayout.edtDia?.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) try {
                var tempLong = 0.0
                if (!alertLayout.edtLong?.text.toString().isNullOrEmpty()) {
                    tempLong = alertLayout.edtLong?.text.toString().toInt().toDouble()
                    if (tempLong.roundToInt() < alertLayout.edtDia.text.toString().toInt().toDouble().roundToInt()) {
                        mUtils.showAlert(activity, "Dia value should be smaller than Long")
                    }
                }
            } catch (e: Exception) {
                e.toString()
            }
        }


        alertLayout.edtDia?.addTextChangedListener(object : TextWatcher {
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
                    var tempLong = 0.0
                    if (!alertLayout.edtLong?.text.toString().isNullOrEmpty()) {
                        tempLong = alertLayout.edtLong?.text.toString().toInt().toDouble()
                    }

                    if (250 < alertLayout.edtDia.text.toString().toDouble().roundToInt()) {
                        mUtils.showAlert(activity, "Maximum allowed dia value is 250")

                    }/*else if (tempLong.roundToInt()<alertLayout.edtDia.text.toString().toInt().toDouble().roundToInt()) {
                             mUtils.showAlert(activity, "Dia value should be smaller than Long")
                         }*/
                    else {
                        val result =
                            (tempLong) / 100 * (s.toString().toInt().toDouble()) / 100 * (s.toString().toInt().toDouble()) / 100 * 0.7854
                        val df = DecimalFormat("###.###")
                        val finalResult = df.format(result)
                        val doublelCBM  = finalResult.toString().replace(",",".")
                        alertLayout.edtCBM.setText(doublelCBM)
                        //alertLayout.edtCBM.setText(finalResult.toString())

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
                            mUtils.showAlert(activity, "Long value should be greater than diameter")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

        }



        alertLayout.edtLong?.addTextChangedListener(object : TextWatcher {
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
                var tempDia = 0.0
                if (!alertLayout.edtDia?.text.toString().isNullOrEmpty()) {
                    tempDia = alertLayout.edtDia?.text.toString().toInt().toDouble()
                }
                try {
                    if (2000 < alertLayout.edtLong.text.toString().toDouble().roundToInt()) {
                        mUtils.showAlert(activity, "Maximum allowed Long value is 2000")
                    }/*else if (alertLayout.edtLong.text.toString().toDouble().roundToInt()<tempDia.roundToInt()) {
                         mUtils.showAlert(activity, "Long value should be greater than diameter")
                     }*/
                    else {
                        val result =
                            (s.toString().toInt().toDouble()) / 100 * (tempDia) / 100 * (tempDia) / 100 * 0.7854
                        val df = DecimalFormat("###.###")
                        val finalResult = df.format(result)
                        val doublelCBM  = finalResult.toString().replace(",",".")
                        alertLayout.edtCBM.setText(doublelCBM)
                       // alertLayout.edtCBM.setText(finalResult.toString())
                    }
                } catch (e: Exception) {
                    e.toString()
                }
            }
        })

        alertLayout.edtEssence.setOnClickListener {
            makeIsSelectedForestNLogNoFalse()
            showDialog(commonForestMaster?.getSpecies() as ArrayList<SupplierDatum?>?, "essence")
        }

        alertLayout.edtQuality.setOnClickListener {
            makeIsSelectedForestNLogNoFalse()
            showDialog(
                commonForestMaster?.getQualityData() as ArrayList<SupplierDatum?>?,
                "quality"
            )
        }


        alertLayout.edtTGrade.setOnClickListener{
            makeIsSelectedForestNLogNoFalse()
            showDialog(commonForestMaster?.getGradeData() as ArrayList<SupplierDatum?>?,"grade")
        }

        alertLayout.edtAAC.setOnClickListener {
            makeIsSelectedForestNLogNoFalse()
            showDialog(commonForestMaster?.getAacList() as ArrayList<SupplierDatum?>?, "aac")
        }

        alertLayout.ivLogCancel.setOnClickListener {
            logDialog?.dismiss()
        }


        logDialog = alert.create()
        logDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logDialog?.show()
    }

    //when click on other search dialog
    fun makeIsSelectedForestNLogNoFalse(){
        isLogNoSelected = false
        isForestSelected = false
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setEditDataOnAddLogDiolog(alertLayout: View, position: Int) {
        if (groundLogsLiting?.get(position)?.getLogNo()?.contains("/")!!) {
            val tokens = StringTokenizer(groundLogsLiting?.get(position)?.getLogNo(), "/");
            val first = tokens?.nextToken()
            val second = tokens?.nextToken()
            alertLayout.edt_log.setText(first)
            alertLayout.edt_log2.setText(second)
        } else {
            alertLayout.edt_log.setText(groundLogsLiting?.get(position)?.getLogNo())
        }

        disableLogNonEditableFiled(alertLayout)


        if (groundLogsLiting.get(position).isEditable()!!) {
            alertLayout.txtSaveUpdate.setText(mView.context.getString(R.string.update))
        }
        alertLayout.edtPlaque_no.setText(groundLogsLiting.get(position).getPlaqNo())
        alertLayout.edtEssence.setText(groundLogsLiting.get(position).getLogSpeciesName())
        speciesID = groundLogsLiting.get(position).getLogSpecies()!!
        qualityId = groundLogsLiting.get(position).getQualityId()!!
        aacID = groundLogsLiting.get(position).getAAC()!!
        aacYear = groundLogsLiting.get(position).getAACYear()!!
        alertLayout.edtAAC.setText(groundLogsLiting.get(position).getAACName())
        alertLayout.edtQuality.setText(groundLogsLiting.get(position).getQuality().toString())
        alertLayout.edtDia.setText(groundLogsLiting.get(position).getDiamBdx().toString())
        alertLayout.edtLong.setText(groundLogsLiting.get(position).getLongBdx().toString())
        alertLayout.edtCBM.setText((groundLogsLiting.get(position).getCbm().toString()))
        //alertLayout.edtRefractionDia.setText((groundLogsLiting.get(position).getRefractionDiam()?.toString()))
        alertLayout.edtRefractionLong.setText((groundLogsLiting.get(position).getRefractionLength()?.toString()))
        gradeId = groundLogsLiting.get(position).getGradeId()
        gradeName = groundLogsLiting.get(position).getGradeName().toString()

        alertLayout.edtTGrade.setText((groundLogsLiting.get(position).getGradeName()?.toString()))
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun disableLogNonEditableFiled(alertLayout: View) {
        alertLayout.edt_log.isEnabled = false
        alertLayout.edt_log2.isEnabled = false
        alertLayout.edtAAC.isEnabled = false
        alertLayout.edtPlaque_no.isEnabled = false
        alertLayout.edtEssence.isEnabled = false

        alertLayout.edt_log.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it, R.color.gray_200) }
        alertLayout.edt_log2.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it, R.color.gray_200) }
        alertLayout.edtAAC.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it, R.color.gray_200) }
        alertLayout.edtPlaque_no.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it, R.color.gray_200) }
        alertLayout.edtEssence.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it, R.color.gray_200) }
        alertLayout?.linLogNumber.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout?.linLogNumber2.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout?.linLogAAC.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout?.linPlaque.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout?.linEssence.setBackgroundResource(R.drawable.bg_for_editext_non_editable)

    }

    fun acessRuntimPermission(isEditLog: Boolean, position: Int, alertLayout: View) {
        val permissionListener: PermissionListener = object : PermissionListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionGranted() {
                var isLogAlreadyExit: Boolean = false
                var LogModel: BodereuLogListing = BodereuLogListing();
                if (isValidateAddLog(alertLayout)) {
                    if (isEditLog) {
                        groundLogsLiting.get(position).setAAC(aacID)
                        groundLogsLiting.get(position).setAACYear(aacYear)
                        groundLogsLiting.get(position).setLogSpecies(speciesID)
                        groundLogsLiting.get(position).setQualityId(qualityId)
                        groundLogsLiting.get(position).setIsEditable(true)
                        groundLogsLiting.get(position).setGradeId(gradeId)
                        groundLogsLiting.get(position).setGradeName(gradeName)

                      /*  groundLogsLiting.get(position)
                            .setRefractionDiam(alertLayout.edtRefractionDia.text.toString()?.toInt())*/
                        if(!alertLayout.edtRefractionLong.text.toString().isNullOrEmpty()) {
                            groundLogsLiting.get(position)
                                .setRefractionLength(alertLayout.edtRefractionLong.text.toString()?.toInt())
                        }

                        LogModel = groundLogsLiting.get(position)

                    } else {
                        /*var request: BodereuLogListing = BodereuLogListing()
                        request.setLogNo(alertLayout.edt_log.text.toString()+"/"+alertLayout.edt_log2.text.toString())
                        request.setPlaqNo(alertLayout.edtPlaque_no.text.toString())
                        request.setLogSpeciesName(alertLayout.edtEssence.text.toString())
                        request.setLogSpecies(speciesID)
                        request.setQualityId(qualityId)
                        request.setAACName(alertLayout.edtAAC.text.toString())
                        request.setAAC(aacID)
                        request.setAACYear(aacYear)
                        request.setLogRecordDocNo(mUtils.createLogRecordDocsNumber(bodereuNumber,alertLayout.edt_log.text.toString()+"/"+alertLayout.edt_log2.text.toString(),alertLayout.edtDia.text.toString()))
                        request.setQuality(alertLayout.edtQuality?.text?.toString())
                        request.setDiamBdx(alertLayout.edtDia.text?.toString()?.toInt())
                        request.setLongBdx(alertLayout.edtLong.text?.toString()?.toInt())
                        request.setDetailId("")
                        request.setCbm(alertLayout.edtCBM.text?.toString()?.toDouble())
                        // request.setCbm(mUtils.intToDouble(alertLayout.edtCBM.text?.toString()?.toInt()))
                        request.setBarcodeNumber(mUtils.getRandomNumberString(""))

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
                        }*/
                    }
                    adapter.notifyDataSetChanged()
                    logDialog?.dismiss()
                    /* if(isLogAlreadyExit==false) {
                         buttonGenerate_onClick(LogModel)
                     }*/
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
        if (alertLayout.edtAAC?.text.toString() == getResources().getString(R.string.select)) {
            mUtils.showAlert(activity, "Please select AAC")
            return false
        } else if (alertLayout.edt_log.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, "Please enter log no")
            return false
        } else if (alertLayout.edt_log2.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, "Please enter valid log no")
            alertLayout.edt_log2.requestFocus()
            return false
        }
        /*else if (alertLayout.edtPlaque_no.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, "Please enter plaque no")
            return false
        }*/ else if (alertLayout.edtEssence?.text.toString() == getResources().getString(R.string.select)) {
            mUtils.showAlert(activity, "Please select essence")
            return false
        } else if (alertLayout.edtDia.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, "Please enter dia")
            return false
        } else if (250 < alertLayout.edtDia.text.toString().toDouble().roundToInt()) {
            mUtils.showAlert(activity, "Maximum allowed dia value is 250")
            return false
        } else if (alertLayout.edtLong.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, "Please enter long")
            return false
        } else if (2000 < alertLayout.edtLong.text.toString().toDouble().roundToInt()) {
            mUtils.showAlert(activity, "Maximum allowed Long value is 2000")
            return false
        } else if (alertLayout.edtLong.text.toString().toDouble().roundToInt() < alertLayout.edtDia.text.toString().toDouble().roundToInt()) {
            mUtils.showAlert(activity, "Long value should be greater than diameter")
            return false
        } else if (alertLayout.edtCBM.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, "Please enter CBM")
            return false
        } else if (alertLayout.edtQuality.text.toString() == getResources().getString(R.string.select)) {
            mUtils.showAlert(activity, "Please select quality")
            return false
        } else if (!alertLayout.edtRefractionLong.text.toString().isNullOrEmpty()) {
            if (500 < alertLayout.edtRefractionLong?.text.toString().toInt()) {
                mUtils.showAlert(activity, resources.getString(R.string.refraction_long_should_be_less_than_))
                return false
            }

        }

        return true;
    }


    fun checkLogAACNLogNoAlreadyExits(aacName: String?, logNumber: String): Boolean {
        for (listdata in groundLogsLiting) {
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun showAlerDialog(isEditLog: Boolean, position: Int, msg: String, action: String) {
        val alert: AlertDialog =
            AlertDialog.Builder(view?.context, R.style.CustomDialogWithBackground)
                //.setTitle(activity?.getString(R.string.app_name))
                .setMessage(msg)
                .setPositiveButton(
                    "Ok"
                ) { dialog, which ->
                    dialog.dismiss()
                    when (action) {
                        "delete" -> {
                            callingdeleteINspectionLogAPI(position)
                        }
                        "update" -> {
                            showAddLogDialog(isEditLog, position)
                        }
                        "add" -> {
                            showAddLogDialog(isEditLog, position)
                            //  buttonGenerate_onClick(logsLiting?.get(position))
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


    fun generateConfirmForBillingRequest(
        cuustomerSign: String,
        represetativeSign: String
    ): AddBoereuLogListingReq {
        var request: AddBoereuLogListingReq = AddBoereuLogListingReq()
        try {
            request.setCustomerSignBase(cuustomerSign)
            request.setRepresentativeSignBase(represetativeSign)
            request.setUserID(SharedPref.getUserId(Constants.user_id))
            request.setInspectionNumber(inspectionNumber)
            request.setInspectionId(inspectionId)
            request.setTimezoneId("Asia/Kolkata")
            request.setTotalLogs(groundLogsLiting.size)
            request.setResizedLogs(0)
            request.setRejectedLogs(0)
            request.setInspectionDate(mUtils.getCurrentDate())
            request.setSelectedLogs(getSelectedLogsListCount())
            val selectedLogsList = getSelectedLogsList()
            request.setBordereauLogList(selectedLogsList)
            var json = Gson().toJson(request)
            var test = json


        } catch (e: Exception) {
            e.printStackTrace()
        }
        return request
    }

    fun generateConfirmForGrroundApproveRequest(): AddBoereuLogListingReq {

        var request: AddBoereuLogListingReq = AddBoereuLogListingReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setInspectionNumber(inspectionNumber)
        request.setInspectionId(inspectionId)
        request.setTimezoneId("Asia/Kolkata")
        request.setTotalLogs(groundLogsLiting.size)
        request.setResizedLogs(0)
        request.setRejectedLogs(0)
        request.setSelectedLogs(getSelectedLogsListCount())
        val selectedLogsList = getSelectedLogsList()
        request.setBordereauLogList(selectedLogsList)
        var json = Gson().toJson(request)
        var test = json

        return request

    }

    fun getSelectedLogsListCount():Int{

        var selectedGroundLogsListCount : Int = 0
        for (listdata in groundLogsLiting) {
            if(listdata.isSelected){
                selectedGroundLogsListCount ++
            }
        }
        return selectedGroundLogsListCount
    }

    fun getSelectedLogsList():ArrayList<BodereuLogListing>{

        var selectedGroundLogsList : ArrayList<BodereuLogListing> = arrayListOf()
        for (listdata in groundLogsLiting) {
           if(listdata.isSelected){
               selectedGroundLogsList.add(listdata)
           }
        }
        return selectedGroundLogsList
    }


    private fun generateDeleteBodereuLogRequest(position: Int): BoderueDeleteLogReq {

        var request: BoderueDeleteLogReq = BoderueDeleteLogReq()
        //request.setUserID(SharedPref.getUserId(Constants.user_id))
        // request.setBordereauHeaderId(bodereuHeaderId)
        request.setDetailId(groundLogsLiting?.get(position)?.getDetailId()?.toInt())

        return request

    }


    /*private fun callingLogMasterAPI(forestId: String, originID: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val call_api: Call<GetForestDataRes> =
                    apiInterface.getLogsMaster(forestId, originID)
                call_api.enqueue(object :
                    Callback<GetForestDataRes> {
                    override fun onResponse(
                        call: Call<GetForestDataRes>,
                        response: Response<GetForestDataRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: GetForestDataRes =
                                response.body()!!
                            if (responce != null) {
                                if (responce.getSeverity() == 200) {
                                    commonForestMaster = responce
                                } else {

                                }
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
    }*/
    /*  private fun callingValidateBordereauNoAPI(logRecorNumber:String) {
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
  */

    private fun saveBitmapIntoStorageNew(bmp: Bitmap): File? {
        var f: File? = null
        try {
            val bytes = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 80, bytes)
            val d = Date()
            val s =
                DateFormat.format("MM-dd-yy hh-mm-ss", d.time)

            val cw =  ContextWrapper(mView.context)
            val directory = cw.getDir("OLAM", Context.MODE_PRIVATE)
            // Create imageDir
            f = File(directory, "SIGN" + s +".png")

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



    /*  private fun saveBitmapIntoStorage(bmp: Bitmap): File? {
        var f: File? = null
        try {
            val bytes = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 80, bytes)
            val d = Date()
            val s =
                DateFormat.format("MM-dd-yy hh-mm-ss", d.time)
            f = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + "IMAGE" + s + ".png"
            )
            f.createNewFile()
            val fo = FileOutputStream(f)
            fo.write(bytes.toByteArray())
            fo.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return f
    }
*/
    inner class SaveBitmapAsyncTask internal constructor() :
        AsyncTask<Bitmap?, String?, File?>() {

        override fun onPreExecute() {
            super.onPreExecute()
            mUtils.showProgressDialog(mView.context)
        }

        override fun onPostExecute(file: File?) {
            super.onPostExecute(file)
            mUtils.dismissProgressDialog()
            if (file != null) {
                try {
                    callingUploadImageApi(file)
                } catch (t: Throwable) {
                }
            }
        }


        override fun doInBackground(vararg p0: Bitmap?): File? {
            var file: File? = null
            val compressFile: File? = null
            try {
                val bmScreenShot = p0[0]
                file = bmScreenShot?.let { saveBitmapIntoStorageNew(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return file
        }


    }

    fun acessRuntimPermission(alertLayout: View) {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                //val representativeSign  = getRepresentativeBase64String(alertLayout)
                customerBitmap = alertLayout.customerSign.getTransparentSignatureBitmap(false)
                represemtativeBitmap =
                    alertLayout.representativeSign.getTransparentSignatureBitmap(false)
                //val customerSign  = getCustomerBase64String(alertLayout)
                SaveBitmapAsyncTask().execute(customerBitmap)
                //var request = generateConfirmForBillingRequest(representativeSign,customerSign)
                //  var test = generateJsonStringrequestForBasee64(representativeSign,customerSign)
                // callingConfirmForBillinngAPI(representativeSign,customerSign)
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {

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
                Manifest.permission.ACCESS_NETWORK_STATE
               /* Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE*/
            )
            .check()
    }


    private fun showDialogForDigitalSign() {
        val inflater =
            mView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val alertLayout: View = inflater.inflate(R.layout.layout_digital_signature, null)
        val alertView = alertLayout
        val alert: AlertDialog.Builder =
            AlertDialog.Builder(mView.context, R.style.CustomDialog)
        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout)
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(false)

        alertLayout.ivRepresentiveCancel?.setOnClickListener {
            alertLayout.representativeSign?.clear()
        }

        alertLayout.ivCustomerCancel?.setOnClickListener {
            alertLayout.customerSign?.clear()
        }

        alertLayout.txtReject?.setOnClickListener {
            SignDialog?.dismiss()
        }
        alertLayout.txtConfirm?.setOnClickListener {
            if (isValidateSign(alertView)) {
                acessRuntimPermission(alertView)
                //SignDialog?.dismiss()
            }

        }

        SignDialog = alert.create()
        SignDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        SignDialog?.show()


    }

    fun isValidateSign(alertLayout: View): Boolean {
        if (alertLayout.representativeSign.isEmpty) {
            mUtils.showAlert(
                activity,
                mView.context.resources.getString(R.string.representative_sign_error)
            )
            return false
        } else if (alertLayout.customerSign.isEmpty) {
            mUtils.showAlert(
                activity,
                mView.context.resources.getString(R.string.customer_sign_error)
            )
            return false
        }
        return true
    }


    /* fun getCustomerBase64String(dialog:View):String{
         var byteArray64String : String  = ""
         var customerSigbBitmap = dialog.customerSign.getTransparentSignatureBitmap(false)
         val byteArrayOutputStream =  ByteArrayOutputStream()
         customerSigbBitmap.compress(Bitmap.CompressFormat.PNG, 40, byteArrayOutputStream)
         val byteArray = byteArrayOutputStream.toByteArray()
         val test  = byteArray
         byteArray64String =  SharedPref.convertByArrayToBasse64Strinng(byteArray)
         //byteArray64String = Base64.getEncoder().encodeToString(byteArray);

         // byteArray64String = Base64.encodeToString(byteArray, Base64.DEFAULT);
         return  byteArray64String;
     }*/

    /*  fun getRepresentativeBase64String(dialog:View):String{
          var byteArray64String : String  = ""
          var reprensetativeSigbBitmap = dialog.representativeSign.getTransparentSignatureBitmap(false)
          val byteArrayOutputStream =  ByteArrayOutputStream()
          reprensetativeSigbBitmap.compress(Bitmap.CompressFormat.PNG, 40, byteArrayOutputStream)
          val byteArray = byteArrayOutputStream.toByteArray()
          val test  = byteArray
          byteArray64String = SharedPref.convertByArrayToBasse64Strinng(byteArray)
          //Base64.getEncoder().encodeToString(byteArray);

         // byteArray64String = Base64.encodeToString(byteArray, Base64.DEFAULT);
          return  byteArray64String;
      }*/

   /* fun generateNoInspectionRequest(): BoderueDeleteLogReq {

        var request: BoderueDeleteLogReq = BoderueDeleteLogReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(bodereuHeaderId)
        return request

    }*/

//    private fun callingRejectionAPI() {
//        if (mUtils?.checkInternetConnection(mView.context) == true) {
//            try {
//                mUtils.showProgressDialog(mView.context)
//                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
//                var request = generateNoInspectionRequest()
//                val call: Call<AddBodereuLogListingRes> =
//                    apiInterface.noInspection(request)
//                call.enqueue(object :
//                    Callback<AddBodereuLogListingRes> {
//                    override fun onResponse(
//                        call: Call<AddBodereuLogListingRes>,
//                        response: Response<AddBodereuLogListingRes>
//                    ) {
//                        mUtils.dismissProgressDialog()
//
//                        try {
//                            if (response.code() == 200) {
//                                if (response != null) {
//                                    if (response.body().getSeverity() == 200) {
//                                        mUtils.showToast(activity, response.body().getMessage())
//                                        var fragment = MainContainerFragment()
//                                        (activity as HomeActivity).replaceFragment(fragment, true)
//                                    } else {
//                                        mUtils.showToast(activity, response.body().getMessage())
//                                    }
//                                }
//                            } else {
//                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
//                            }
//                        } catch (e: Exception) {
//                            e.printStackTrace()
//                        }
//                    }
//
//                    override fun onFailure(
//                        call: Call<AddBodereuLogListingRes>,
//                        t: Throwable
//                    ) {
//                        mUtils.showToast(activity, Constants.SERVERTIMEOUT)
//                        mUtils.dismissProgressDialog()
//                    }
//                })
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        } else {
//            mUtils.showToast(view?.context, getString(R.string.no_internet))
//        }
//    }


    private fun callingConfirrmForGroundApprovalAPI() {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateConfirmForGrroundApproveRequest()
                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.sendForGroundApproval(request)
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
                                        var fragment = MainContainerFragment()
                                        val bundle = Bundle()
                                        bundle.putString(
                                            Constants.comming_from,
                                            Constants.from_ground
                                        )
                                        fragment.arguments = bundle
                                        (activity as HomeActivity).replaceFragment(fragment, true)
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


    private fun callingConfirmForBillinngAPI(represetativeSign: String, cuustomerSign: String) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = generateConfirmForBillingRequest(
                    represetativeSign,
                    cuustomerSign
                )

                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.sendForGroundConfirm(request)
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
                                        countImgeUpload = 0
                                        if (!response.body().getPdfFilePath().isNullOrEmpty()) {
                                            response.body().getPdfFilePath()?.let {
                                               /* downloadPDFFromURL(
                                                    it
                                                )*/
                                                val base64Image = it.split(",")[0]
                                                SaveBitmapToInternalStorageAsyncTask().execute(base64Image)
                                            }
                                        }
                                        SignDialog?.dismiss()
                                        // response.body().getPdfFilePath()?.let { c(it) }
                                        mUtils.showToast(activity, response.body().getMessage())
                                        var fragment = MainContainerFragment()
                                        val bundle = Bundle()
                                        bundle.putString(
                                            Constants.comming_from,
                                            Constants.from_ground
                                        )
                                        fragment.arguments = bundle
                                        (activity as HomeActivity).replaceFragment(fragment, true)
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
                val imageBytes = Base64.decode(bmScreenShot, Base64.DEFAULT)

                val d = Date()
                val s =
                    DateFormat.format("MM-dd-yy hh-mm-ss", d.time)

                val cw =  ContextWrapper(mView.context)
                val directory = cw.getDir("OLAM", Context.MODE_PRIVATE)
                // Create imageDir
                if (!directory.exists()) {
                    directory.mkdir()
                }
                file = File(directory, "Invoice" + s +".pdf")

                filOutputStrem =  FileOutputStream(file, false);
                filOutputStrem.write(imageBytes)
                filOutputStrem.flush()
                filOutputStrem.close()


                /*  val bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                  file = bitmapImage?.let { saveBitmapIntoStorageNew(it) }*/
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

    fun downloadPDFFromURL(pdfPath: String) {
        mUtils.showProgressDialog(mView.context)
        try {
            val mFileName = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
            var destination =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
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

    private fun callingdeleteINspectionLogAPI(position: Int) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateDeleteBodereuLogRequest(position)
                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.deleteInspectionLog(request)
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
                                    }  else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }
                            }  else if (response.code() == 306) {
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


    private fun callingUploadImageApi(file: File) {
        val photo: MultipartBody.Part
        val filename: RequestBody
        val photoContent: RequestBody
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                photoContent =
                    RequestBody.create(MediaType.parse("multipart/form-data"), file)
                photo = MultipartBody.Part.createFormData(
                    "files",
                    file.name.trim { it <= ' ' },
                    photoContent
                )
                filename = RequestBody.create(
                    MediaType.parse("text/plain"),
                    file.name.trim { it <= ' ' }
                )
                val apiInterface: ApiEndPoints = ApiClientMultiPart.client.create(ApiEndPoints::class.java)
                val call: Call<AddBodereuLogListingRes> =
                   // ApiServiceForMultipart.buildServiceMultipart(mView.context)
                    apiInterface.uploadSign(filename, photo)
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
                                        countImgeUpload++
                                        if (countImgeUpload == 1) {
                                            firstImagePath =
                                                response.body().getPdfFilePath().toString()
                                            val representativeSigbBitmap = represemtativeBitmap
                                            SaveBitmapAsyncTask().execute(representativeSigbBitmap)
                                        }
                                        if (countImgeUpload == 2) {
                                            secodnImagepath =
                                                response.body().getPdfFilePath().toString()
                                            callingConfirmForBillinngAPI(
                                                firstImagePath,
                                                secodnImagepath
                                            )
                                        }
                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }
                            }  else if (response.code() == 306) {
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

    //added for earlier this is get method with no request param
    fun getGroundsAllLogsRequest(): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    private fun getGroundsAllLogs() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
val request  =  getGroundsAllLogsRequest()
                val call_api: Call<GetBodereuLogByIdRes> =
                    apiInterface.getGroundLogsForInspection(request)
                call_api.enqueue(object :
                    Callback<GetBodereuLogByIdRes> {
                    override fun onResponse(
                        call: Call<GetBodereuLogByIdRes>,
                        response: Response<GetBodereuLogByIdRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                val responce: GetBodereuLogByIdRes =
                                    response.body()!!
                                if (responce != null) {
                                    if (responce.getSeverity() == 200) {
                                        var forestSupplierDatum: SupplierDatum
                                        groundLogsLiting.clear()
                                        forestLocalMaster.clear()
                                        lognoLocalMaster.clear()
                                        responce.getBordereauLogList()?.let {
                                            groundLogsLiting?.addAll(
                                                it
                                            )
                                        }
                                        for ((index, listdata) in groundLogsLiting.withIndex()) {
                                            listdata.setIndex(index)
                                            // for (listdata in groundLogsLiting) {
                                            //forestLocalMaster.add()
                                            var logSupplierDatum = SupplierDatum()
                                            logSupplierDatum.optionName = listdata.getLogNo()
                                            lognoLocalMaster.add(logSupplierDatum)

                                            if(listdata.getSupplierName() != null) {
                                                if (!listdata.getSupplierName()?.let {
                                                        validateForestAlreadyPresent(
                                                            it
                                                        )
                                                    }!!) {
                                                    forestSupplierDatum = SupplierDatum()
                                                    forestSupplierDatum.optionName =
                                                        listdata.getSupplierName()
                                                    forestLocalMaster.add(forestSupplierDatum)
                                                }
                                            }

                                        }

                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                    setTotalNoOfLogs(groundLogsLiting?.size)
                                    //when refresh list will remain as last filrter hence will set list with this method
                                    adapter.setValueToParentList(groundLogsLiting)

                                    adapter.notifyDataSetChanged()
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

    fun validateForestAlreadyPresent(forestName:String):Boolean{
       var isPresent : Boolean = false
        for (listdata in forestLocalMaster) {
            if(forestName.equals(listdata.optionName)){
                return  true
            }
        }
        return isPresent
    }


    //Dialog content
    open fun showDialog(countryListSearch: ArrayList<SupplierDatum?>?, action: String) {

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

                    "essence" -> {
                        alertView.edtEssence?.setText(model.optionName)
                        speciesID = model.optionValue!!
                        alertView.edtDia?.requestFocus()
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

                    "grade"->{
                        alertView.edtTGrade?.setText( model.optionName)
                        gradeId  = model.optionValue!!
                        gradeName = model.optionName!!
                        alertView.edtTGrade?.requestFocus()
                    }
                    "forest"->{
                        mView.txtfilterForest.text = model.optionName
                        mView.txtFilterLogNo.text = mView.resources.getString(R.string.select)
                        setForestNLogValuesToAdapter()

                    }

                    "logno"->{
                        mView.txtFilterLogNo.text = model.optionName
                        mView.txtfilterForest.text = mView.resources.getString(R.string.select)
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
        if(isLogNoSelected){
            mView.txtFilterLogNo.text = mView.resources.getString(R.string.select)
        }
        if(isForestSelected){
            mView.txtfilterForest.text = mView.resources.getString(R.string.select)
        }

        setForestNLogValuesToAdapter()

    }


    fun setForestNLogValuesToAdapter(){
        //if only forest selected
        if(mView.txtFilterLogNo.text.toString().equals("select",ignoreCase = true) && !mView.txtfilterForest.text.toString().equals("select",ignoreCase = true)){
            adapter.setLogAndForestValues(mView.txtfilterForest.text.toString(),"")
        }
        //if only log no selected
        else if(!mView.txtFilterLogNo.text.toString().equals("select",ignoreCase = true) && mView.txtfilterForest.text.toString().equals("select",ignoreCase = true)){
            adapter.setLogAndForestValues("",mView.txtFilterLogNo.text.toString())
        }
        //if forest & logNo selected
        else if(!mView.txtFilterLogNo.text.toString().equals("select",ignoreCase = true) && !mView.txtfilterForest.text.toString().equals("select",ignoreCase = true)){
            adapter.setLogAndForestValues(mView.txtfilterForest.text.toString(),mView.txtFilterLogNo.text.toString())
        }
        //if forest & logNo not selected
        else{
            adapter.setLogAndForestValues("","")
        }
        adapter.getFilter()?.filter("")
    }


    fun validatedLogSelection():Boolean{
        var isLogSelected:Boolean=false
        for (listdata in groundLogsLiting) {
            if(listdata.isSelected){
                isLogSelected = true
                return  isLogSelected
            }
        }
        return isLogSelected
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivBOAdd -> {
                //user should be add only 20 logs
                if (groundLogsLiting.size != 20) {
                    showAddLogDialog(false, 0)
                }
            }
            R.id.txtWaitingForApproval -> {
                if (groundLogsLiting.size != 0) {
                    if(validatedLogSelection()) {
                        callingConfirrmForGroundApprovalAPI()
                    }else{
                        mUtils.showAlert(activity,mView.resources.getString(R.string.please_select_logs))
                    }
                }
            }

            R.id.txtWaitingForBilling -> {
                if (groundLogsLiting.size != 0) {
                    if(validatedLogSelection()) {
                        showDialogForDigitalSign()
                    }else{
                        mUtils.showAlert(activity,mView.resources.getString(R.string.please_select_logs))
                    }

                }
            }

         /*   R.id.txtRejectInspection -> {
                callingRejectionAPI()
            }
*/
            R.id.ivHeaderEdit -> {
                if (commigFrom.equals( Constants.header, ignoreCase = true)) {
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

            R.id.txtScan -> {
                acessRuntimPermissionForCamera()
            }

            R.id.txtFilterLogNo ->{
                makeIsSelectedForestNLogNoFalse()
                isLogNoSelected = true
                showDialog(lognoLocalMaster as ArrayList<SupplierDatum?>?, "logno")
            }
            R.id.txtfilterForest ->{
                makeIsSelectedForestNLogNoFalse()
                isForestSelected = true
                showDialog(forestLocalMaster as ArrayList<SupplierDatum?>?, "forest")
            }

        }
    }

    fun acessRuntimPermissionForCamera() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                var intent = Intent(mView.context, BarcodeScanActivity::class.java)
                startActivityForResult(intent, RESULT_CODE)
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

    //function for validate Log when scan from barcode And Auto Select that log
    fun validateAndAutoSelectlog(logData: BodereuLogListing) {
        for (listdata in groundLogsLiting) {
            if (!logData.getDetailId().isNullOrEmpty() && !listdata.getLogRecordDocNo().isNullOrEmpty())
                if (listdata.getDetailId() == logData.getDetailId() && listdata.getLogRecordDocNo().toString().contains(
                        logData.getLogRecordDocNo().toString()
                    )
                ) {
                    listdata.isSelected = true
                    mUtils.showToast(
                        mView.context,
                        mView.context.getString(R.string.log_has_been_selected)
                    )

                }
        }
        selctedRecordMoveToUp()
        adapter.notifyDataSetChanged()
    }

    fun getMasterDataSalesInspectionRequest(): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        val json =  Gson().toJson(request)
        var test  = json
        return  request

    }

    private fun callingGetMasterDataSalesInspectionAPI() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val request =  getMasterDataSalesInspectionRequest()
                val call_api: Call<GetForestDataRes> =
                    apiInterface.getMasterDataSalesInspection(request)

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
                                    commonForestMaster =  responce

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

    fun getGgroundLogDataByBarCodeRequest(barcodeNumber: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setBarcodeNumber(barcodeNumber)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    private fun getGgroundLogDataByBarCode(barcodeNumber: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val request =  getGgroundLogDataByBarCodeRequest(barcodeNumber)
                val call_api: Call<GetBodereuLogByIdRes> =
                    apiInterface.getGroundLogDataByBarCode(request)

                call_api.enqueue(object :
                    Callback<GetBodereuLogByIdRes> {
                    override fun onResponse(
                        call: Call<GetBodereuLogByIdRes>,
                        response: Response<GetBodereuLogByIdRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                            val responce: GetBodereuLogByIdRes =
                                response.body()!!
                            if (responce != null) {

                                if (responce?.getSeverity() == 200) {
                                    var logsList: ArrayList<BodereuLogListing> = arrayListOf()
                                    logsList =
                                        responce.getBordereauLogList() as ArrayList<BodereuLogListing>
                                    if (logsList.size != 0) {
                                        val BodereuLogListing = logsList.get(0)
                                        validateAndAutoSelectlog(BodereuLogListing)
                                    }

                                    /* if (checkLogAACNLogNoAlreadyExits(
                                            responce.getLogDetail()?.getAACName(),
                                            responce.getLogDetail()?.getLogNo().toString()
                                        )
                                    ) {
                                        if(logsLiting?.size!=20) {
                                            responce.getLogDetail()?.let { logsLiting?.add(it) }
                                            adapter.notifyDataSetChanged()
                                            setTotalNoOfLogs(logsLiting?.size)
                                        }
                                    } else {
                                        mUtils.showAlert(
                                            activity,
                                            resources.getString(R.string.duplicate_log_found)
                                        )
                                    }*/
                                }
                                else if (response.body()?.getSeverity() == 306) {
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


    //scanner Data

    fun ToggleSoftScanTrigger() {
        sendDataWedgeIntentWithExtra(
            Constants.ACTION_DATAWEDGE,
            Constants.EXTRA_SOFT_SCAN_TRIGGER,
            "TOGGLE_SCANNING"
        )
    }

    fun setDecoderValues() {

        /* val checkCode128 = findViewById(R.id.chkCode128) as CheckBox*/
        val Code128Value: String = "true"/*setDecoder(checkCode128)*/

        /*   val checkCode39 = findViewById(R.id.chkCode39) as CheckBox*/
        val Code39Value: String = "true"/*setDecoder(checkCode39)*/

        /* val checkEAN13 = findViewById(R.id.chkEAN13) as CheckBox*/
        val EAN13Value: String = "true" /* setDecoder(checkEAN13)*/

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
        b.putString(Constants.EXTRA_KEY_APPLICATION_NAME, mView.context.packageName)
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

    override fun onResume() {
        super.onResume()
        registerReceivers()
    }

    override fun onPause() {
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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val scannedValue = data?.getStringExtra(Constants.scan_code).toString()
                //calling  getlogByBarcode
                getGgroundLogDataByBarCode(scannedValue)
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
        val decodedLabelType =
            initiatingIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_label_type))
        //calling  getlogByBarcode
        getGgroundLogDataByBarCode(decodedData)
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