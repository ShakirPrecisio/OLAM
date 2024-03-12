package com.kemar.olam.sales_and_inspection.inspection.fragment
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DownloadManager
import android.content.*
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import com.kemar.olam.R
import com.kemar.olam.bordereau.fragment.AddHeaderFragment
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.request.BoderueDeleteLogReq
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.login.activity.ShowPDFPreview
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.retrofit.ApiClientMultiPart
import com.kemar.olam.sales_and_inspection.inspection.adapter.SalesLogsListingAdapter
import com.kemar.olam.utility.*
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.dialog_add_log_layout.view.*
import kotlinx.android.synthetic.main.fragment_sales_log_details.view.*
import kotlinx.android.synthetic.main.fragment_sales_log_details.view.ivBOAdd
import kotlinx.android.synthetic.main.fragment_sales_log_details.view.ivHeaderEdit
import kotlinx.android.synthetic.main.fragment_sales_log_details.view.linFooter
import kotlinx.android.synthetic.main.fragment_sales_log_details.view.rvLogListing
import kotlinx.android.synthetic.main.fragment_sales_log_details.view.swipeLogListing
import kotlinx.android.synthetic.main.fragment_sales_log_details.view.tvNoDataFound
import kotlinx.android.synthetic.main.fragment_sales_log_details.view.txtBO
import kotlinx.android.synthetic.main.fragment_sales_log_details.view.txtBO_NO
import kotlinx.android.synthetic.main.fragment_sales_log_details.view.txtForestWagonNo
import kotlinx.android.synthetic.main.fragment_sales_log_details.view.txtTotalLogs
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
import org.json.JSONObject
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

class SalesLogDetailsFragment : Fragment() ,View.OnClickListener, DialogFragment.GetDialogListener {
    private val DelayTime = 500
    lateinit var mView: View
    var  FULL_PATH  : String = ""
    var logDialog: AlertDialog? = null
    var SignDialog: AlertDialog? = null
    lateinit var rvLogListing: RecyclerView
    lateinit var adapter: SalesLogsListingAdapter
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
    var forestID = 0
    var suplierID =""
    var originID : Int? = 0
    var bodereuNumber = ""
    var speciesID = 0
    var qualityId :Int?= 0
    var aacID = 0
    var fscOrNonFsc :String = ""
    var supplierShortName :String? = ""
    var transporterName:String?=""
    var aacYear = ""
    var headerModel  : AddBodereuRes.BordereauResponse =  AddBodereuRes.BordereauResponse()
    var LogsListingResponce = GetBodereuLogByIdRes()
    var todaysHistoryModel  :LogsUserHistoryRes.BordereauRecordList =  LogsUserHistoryRes.BordereauRecordList()
    var countImgeUpload = 0
    var firstImagePath = ""
    var secodnImagepath = ""
    //for header
    var headerGradeId :Int?= 0
    var headerGradeName : String =""

    var gradeId :Int?= 0
    var gradeName : String =""

    lateinit  var represemtativeBitmap : Bitmap
    lateinit var  customerBitmap : Bitmap

    var PDF_PREVIEW_REQUEST_CODE = 104
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_sales_log_details, container, false)
        initViews()
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
        setupFooterButtonAsPerUserRole()

        mView.swipeLogListing.setOnRefreshListener{
            mView.swipeLogListing.isRefreshing =  false
           // callingLogMasterAPI(suplierID.toString(),originID.toString())
            callingGetMasterDataSalesInspectionAPI()
        }

        //when comming from headerScreen & User History Screen
        //suplierID == forest
        //
        if(commigFrom.equals( Constants.header, ignoreCase = true)) {
            var headerDataModel: AddBodereuRes.BordereauResponse? =
                arguments?.getSerializable(Constants.badereuModel) as AddBodereuRes.BordereauResponse
            if (headerDataModel != null) {
                headerModel =  headerDataModel
            }
            supplierLocationName = headerDataModel?.supplierName.toString()
            bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
            bodereuNumber = headerDataModel?.bordereauNo!!
            suplierID = headerDataModel?.supplier.toString()
            originID = headerDataModel?.originID
            originName= headerDataModel?.originName.toString()
            headerGradeName= headerDataModel?.gradeName.toString()
            headerGradeId  = headerDataModel?.gradeId
            supplierShortName = headerDataModel?.supplierShortName
            transporterName= headerDataModel?.transporterName
            fscOrNonFsc = headerDataModel?.fscName?.toString().toString()
           // headerDataModel?.modeOfTransport?.let { setupTransportMode(it,mView.context) }

            if (headerDataModel?.bordereauNo?.toString().isNullOrEmpty()) {
                mView.txtBO_NO.text = headerDataModel?.eBordereauNo?.toString()
            } else {
                mView.txtBO_NO.text = headerDataModel?.bordereauNo?.toString()

            }


            mView.txtForestWagonNo.text = headerDataModel?.wagonNo?.toString()
            mView.txtBO.text = headerDataModel?.bordereauRecordNo?.toString()

            //  setTotalNoOfLogs(logsLiting?.size)
            /* headerDataModel?.supplier?.toString()?.let { callingForestMasterAPI(it) }*/
          //  callingLogMasterAPI(suplierID.toString(),originID.toString())
            callingGetMasterDataSalesInspectionAPI()
            getSalesBodereuLogsByID(bodereuHeaderId.toString())
        }else{
            var headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
                arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
            if (headerDataModel != null) {
                todaysHistoryModel =  headerDataModel
            }
            /* headerDataModel?.supplierId?.toString()?.let { callingForestMasterAPI(it) }*/
            bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
            supplierLocationName = headerDataModel?.supplierName.toString()
            originID = headerDataModel?.originId
            bodereuNumber = headerDataModel?.bordereauNo!!
            originName= headerDataModel?.originName.toString()
            supplierShortName = headerDataModel?.supplierShortName
            suplierID = headerDataModel?.supplierId.toString()
            transporterName= headerDataModel?.transporterName
            fscOrNonFsc = headerDataModel?.fscName?.toString().toString()

            headerGradeName = headerDataModel?.gradeName.toString()
            headerGradeId  = headerDataModel?.gradeId

           // headerDataModel?.transportMode?.let { setupTransportMode(it,mView.context) }

            if (headerDataModel?.bordereauNo?.isNullOrEmpty()!!) {
                mView.txtBO_NO.text = headerDataModel?.eBordereauNo
            } else {
                mView.txtBO_NO.text = headerDataModel?.bordereauNo

            }

            mView.txtForestWagonNo.text = headerDataModel?.wagonNo?.toString()
            mView.txtBO.text = headerDataModel?.recordDocNo?.toString().toString()
           // originID?.toString()?.let { callingLogMasterAPI(suplierID?.toString(), it) }
            callingGetMasterDataSalesInspectionAPI()
            getSalesBodereuLogsByID(bodereuHeaderId.toString())
        }

    }


    fun setupFooterButtonAsPerUserRole(){
        when(SharedPref.read(Constants.user_role).toString()){
            "SuperUserApp","Super User","admin"->{
                mView.txtWaitingForBilling.visibility =  View.VISIBLE
                mView.txtRejectInspection.visibility =  View.VISIBLE
                mView.txtWaitingForApproval.visibility =  View.GONE

            }else->{
            mView.txtWaitingForApproval.visibility =  View.VISIBLE
            mView.txtWaitingForBilling.visibility =  View.GONE
            mView.txtRejectInspection.visibility =  View.GONE
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
       mView.txtRejectInspection.setOnClickListener(this)
       mView.ivHeaderEdit.setOnClickListener(this)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setupRecyclerViewNAdapter() {
        logsLiting?.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        rvLogListing.layoutManager = mLinearLayoutManager
        adapter = SalesLogsListingAdapter(mView.context, logsLiting,commigFrom)
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

        adapter.onAddClick = { modelData, position ->
            showAlerDialog(true, position, mView
                .context.getString(R.string.are_you_sure_want_to_add_log_data), "add")
        }
    }


    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.inspection_n_sales)
        (activity as HomeActivity).invisibleFilter()
    }


    private fun setTotalNoOfLogs(count: Int) {
        if (count == 0) {
            mView.linFooter.visibility =  View.INVISIBLE
            mView.tvNoDataFound.visibility=View.VISIBLE
            mView.rvLogListing.visibility=View.GONE
            mView.txtTotalLogs.visibility =View.INVISIBLE
            mView.txtTotalLogs.text = getString(R.string.total_found,count)//"Total $count Found"
        } else {
            mView.linFooter.visibility =  View.VISIBLE
            mView.tvNoDataFound.visibility=View.GONE
            mView.rvLogListing.visibility=View.VISIBLE
            mView.txtTotalLogs.visibility =View.VISIBLE
            mView.txtTotalLogs.text = getString(R.string.total_found,count)//"Total $count Found"
        }

    }

    fun deleteLogsEntry(position: Int) {
        logsLiting.removeAt(position)
        adapter.notifyDataSetChanged()
        //if all logs delete then it will redirect to user history

        if(logsLiting.size == 0){
            var fragment  = MainContainerFragment()
            val bundle = Bundle()
            bundle.putString(
                Constants.comming_from,
                Constants.from_sales
            )
            fragment.arguments = bundle
            (activity as HomeActivity).replaceFragment(fragment,true)
        }else {
            setTotalNoOfLogs(logsLiting.size)
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
            acessRuntimPermission(isEditLog,position,alertLayout)
        }

        alertLayout.edtDia?.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus)  try {
                var tempLong = 0.0
                if(!alertLayout.edtLong?.text.toString().isNullOrEmpty()){
                    tempLong =  alertLayout.edtLong?.text.toString().toInt().toDouble()
                    if (tempLong.roundToInt()<alertLayout.edtDia.text.toString().toInt().toDouble().roundToInt()) {
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
                      // alertLayout.edtCBM.setText(finalResult.toString())

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
                }catch (e:Exception){
                    e.toString()
                }
            }
        })

        alertLayout.edtTGrade.setOnClickListener{
            showDialog(commonForestMaster?.getGradeData() as ArrayList<SupplierDatum?>?,"grade")
        }

        alertLayout.edtEssence.setOnClickListener{
            showDialog(commonForestMaster?.getSpecies() as ArrayList<SupplierDatum?>?,"essence")
        }

        alertLayout.edtQuality.setOnClickListener{
            showDialog(commonForestMaster?.getQualityData() as ArrayList<SupplierDatum?>?,"quality")
        }

        alertLayout.edtAAC.setOnClickListener{
            showDialog(commonForestMaster?.getAacList() as ArrayList<SupplierDatum?>?,"aac")
        }

        alertLayout.ivLogCancel.setOnClickListener{
            logDialog?.dismiss()
        }


        logDialog = alert.create()
        logDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        logDialog?.show()
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


        if(logsLiting.get(position).isEditable()!!){
            alertLayout.txtSaveUpdate.setText(mView.context.getString(R.string.update))
        }
        alertLayout.edtPlaque_no.setText(logsLiting.get(position).getPlaqNo())
        alertLayout.edtEssence.setText(logsLiting.get(position).getLogSpeciesName())
        speciesID = logsLiting.get(position).getLogSpecies()!!
        qualityId= logsLiting.get(position).getQualityId()!!
        aacID =  logsLiting.get(position).getAAC()!!
        aacYear =    logsLiting.get(position).getAACYear()!!
        alertLayout.edtAAC.setText(logsLiting.get(position).getAACName())
        alertLayout.edtQuality.setText(logsLiting.get(position).getQuality().toString())
        alertLayout.edtDia.setText(logsLiting.get(position).getDiamBdx().toString())
        alertLayout.edtLong.setText(logsLiting.get(position).getLongBdx().toString())
        alertLayout.edtCBM.setText((logsLiting.get(position).getCbm().toString()))
        //alertLayout.edtRefractionDia.setText((logsLiting.get(position).getRefractionDiam()?.toString()))
        alertLayout.edtRefractionLong.setText((logsLiting.get(position).getRefractionLength()?.toString()))
       /* logsLiting.get(position).setGrade(gradeNBame)
        logsLiting.get(position).setGradeId(gradeId)*/
         gradeId = logsLiting.get(position).getGradeId()
         gradeName = logsLiting.get(position).getGradeName().toString()
        alertLayout.edtTGrade.setText((logsLiting.get(position).getGradeName()?.toString()))
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun disableLogNonEditableFiled(alertLayout: View){
        alertLayout.edt_log.isEnabled = false
        alertLayout.edt_log2.isEnabled = false
        alertLayout.edtAAC.isEnabled = false
        alertLayout.edtPlaque_no.isEnabled = false
        alertLayout.edtEssence.isEnabled = false

        alertLayout.edt_log.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it,R.color.gray_200) }
        alertLayout.edt_log2.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it,R.color.gray_200) }
        alertLayout.edtAAC.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it,R.color.gray_200) }
        alertLayout.edtPlaque_no.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it,R.color.gray_200) }
        alertLayout.edtEssence.backgroundTintList =
            view?.context?.let { AppCompatResources.getColorStateList(it,R.color.gray_200) }
        alertLayout?.linLogNumber.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout?.linLogNumber2.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout?.linLogAAC.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout?.linPlaque.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
        alertLayout?.linEssence.setBackgroundResource(R.drawable.bg_for_editext_non_editable)

    }

    fun acessRuntimPermission(isEditLog: Boolean, position: Int,alertLayout : View ) {
        val permissionListener: PermissionListener = object : PermissionListener {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onPermissionGranted() {
                var isLogAlreadyExit : Boolean = false
                var LogModel : BodereuLogListing = BodereuLogListing();
                if (isValidateAddLog(alertLayout)) {
                    if (isEditLog) {
                        logsLiting.get(position).setAAC(aacID)
                        logsLiting.get(position).setAACYear(aacYear)
                        logsLiting.get(position).setLogSpecies(speciesID)
                        logsLiting.get(position).setQualityId(qualityId)
                        logsLiting.get(position).setIsEditable(true)
                        logsLiting.get(position).setGradeId(gradeId)
                        logsLiting.get(position).setGradeName(gradeName)
                        //logsLiting.get(position).setRefractionDiam(alertLayout.edtRefractionDia.text.toString()?.toInt())
                        if(!alertLayout.edtRefractionLong.text.toString().isNullOrEmpty()) {
                            logsLiting.get(position)
                                .setRefractionLength(alertLayout.edtRefractionLong.text.toString()?.toInt())
                        }
                        LogModel = logsLiting.get(position)

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
        } else if (alertLayout.edtQuality.text.toString()==getResources().getString(R.string.select)) {
            mUtils.showAlert(activity, resources.getString(R.string.please_select_quality))
            return false
        }
        else if (!alertLayout.edtRefractionLong.text.toString().isNullOrEmpty()) {
            if (500 < alertLayout.edtRefractionLong?.text.toString().toInt()) {
                mUtils.showAlert(activity, resources.getString(R.string.refraction_long_should_be_less_than_))
                return false
            }

        }
        return true
    }


    fun checkLogAACNLogNoAlreadyExits(aacName:String?,logNumber:String):Boolean{
        for(listdata in logsLiting){
            if(!aacName.isNullOrEmpty() && !logNumber.isNullOrEmpty())
                if (listdata.getLogNo().toString().contains(logNumber) && listdata.getAACName().toString().contains(aacName!!)) {
                    return false
                }
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun showAlerDialog(isEditLog: Boolean, position: Int, msg:String, action:String){
        val alert: AlertDialog =
            AlertDialog.Builder(view?.context, R.style.CustomDialogWithBackground)
                //.setTitle(activity?.getString(R.string.app_name))
                .setMessage(msg)
                .setPositiveButton("Ok"
                ) { dialog, which ->
                    dialog.dismiss()
                    when(action){
                        "delete"->{
                                callingdeleteINspectionLogAPI(position)
                        }
                        "update"->{
                            showAddLogDialog(isEditLog, position)
                        }
                        "add"->{
                            showAddLogDialog(isEditLog, position)
                          //  buttonGenerate_onClick(logsLiting?.get(position))
                        }
                        "right"->{

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
        positive.setTextColor(ContextCompat.getColor(mView.context,R.color.colorPrimaryDark))

        val negative: Button =
            alert.getButton(DialogInterface.BUTTON_NEGATIVE)
        negative.setTextColor(ContextCompat.getColor(mView.context,R.color.colorPrimaryDark))
    }



    fun generateConfirmForApproveRequest(): AddBoereuLogListingReq {

        var request : AddBoereuLogListingReq =   AddBoereuLogListingReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(bodereuHeaderId)
        request.setTimezoneId("Asia/Kolkata")
        request.setInspectionDate(mUtils.getCurrentDate())
        /*for (model in logsLiting) {
            if(action=="Save"){
                model.setMode("Save")
            }else{
                model.setMode("Submit")
            }
            model.setMaterialDesc(0)
            *//* model.setDetailId("")*//*
        }*/
        request.setBordereauLogList(logsLiting)
        var json =  Gson().toJson(request)
        var test  = json

        return  request

    }

  /* fun  generateJsonStringrequestForBasee64(represetativeSign:String,cuustomerSign:String):JSONObject
    {
        val paramObject =  JSONObject()
        try {

        paramObject.put("representativeSignBase", represetativeSign)
        paramObject.put("customerSignBase", cuustomerSign)
        paramObject.put("userID", SharedPref.getUserId(Constants.user_id).toString())
        paramObject.put("bordereauHeaderId", bodereuHeaderId)
        paramObject.put("timezoneId", "Asia/Kolkata")
        paramObject.put("inspectionNumber", LogsListingResponce.getInspectionNumber())
        paramObject.put("inspectionDate", mUtils.getCurrentDate())
        paramObject.put("totalLogs", LogsListingResponce.getTotalLogs())
        paramObject.put("rejectedLogs", LogsListingResponce.getResizedLogs())
        paramObject.put("resizedLogs", LogsListingResponce.getResizedLogs())
        paramObject.put("selectedLogs", LogsListingResponce.getSelectedLogs())

            for (listdata in logsLiting) {
                listdata.setRejectionStatus("N")
            }

            var json1 = Gson().toJson(logsLiting)
            var test1 = json1
            paramObject.put("bordereauLogList", json1)
            var finalValue  =  Gson().toJson(paramObject).toString()
            var   test  = finalValue
        }catch (e:Exception){
            e.printStackTrace()
        }

            return paramObject
    }*/

    fun generateConfirmForBillingRequest(cuustomerSign:String,represetativeSign:String): AddBoereuLogListingReq {
         var request: AddBoereuLogListingReq = AddBoereuLogListingReq()
        try{
            for (listdata in logsLiting) {
                listdata.setRejectionStatus("N")
            }

              request.setUserID(SharedPref.getUserId(Constants.user_id))
            request.setBordereauHeaderId(bodereuHeaderId)
            request.setTimezoneId("Asia/Kolkata")
             request.setCustomerSignBase(cuustomerSign)
             request.setRepresentativeSignBase(represetativeSign)
              request.setInspectionNumber(LogsListingResponce.getInspectionNumber())
            request.setSelectedLogs(LogsListingResponce.getSelectedLogs())
            request.setRejectedLogs(LogsListingResponce.getRejectedLogs())
            request.setResizedLogs(LogsListingResponce.getResizedLogs())
            request.setTotalLogs(LogsListingResponce.getTotalLogs())
            request.setInspectionDate(mUtils.getCurrentDate())
            request.setBordereauLogList(logsLiting)
            var json = Gson().toJson(request)
            var test = json


        }catch (e:Exception){
            e.printStackTrace()
        }
        return request
    }


    private fun generateDeleteBodereuLogRequest(position: Int): BoderueDeleteLogReq {

        var request : BoderueDeleteLogReq =   BoderueDeleteLogReq()
        //request.setUserID(SharedPref.getUserId(Constants.user_id))
       // request.setBordereauHeaderId(bodereuHeaderId)
        request.setDetailId(logsLiting?.get(position)?.getDetailId()?.toInt())

        return  request

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
                                if (response.body().getSeverity() == 200) {

                                    val responce: GetForestDataRes =
                                        response.body()!!
                                    if (responce != null) {
                                        commonForestMaster = responce

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
                            val responce: GetForestDataRes =
                                response.body()!!
                            if (responce != null) {
                                if (responce.getSeverity() == 200) {
                                    commonForestMaster =  responce
                                } else {

                                }
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
            bmp.compress(CompressFormat.PNG, 80, bytes)
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



   /* private fun saveBitmapIntoStorage(bmp: Bitmap): File? {
        var f: File? = null
        try {
            val bytes = ByteArrayOutputStream()
            bmp.compress(CompressFormat.PNG, 80, bytes)
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
    }*/

    inner class  SaveBitmapAsyncTask internal constructor():
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
    fun acessRuntimPermission(alertLayout:View) {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                //val representativeSign  = getRepresentativeBase64String(alertLayout)
                customerBitmap = alertLayout.customerSign.getTransparentSignatureBitmap(false)
                represemtativeBitmap = alertLayout.representativeSign.getTransparentSignatureBitmap(false)
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

        alertLayout.ivRepresentiveCancel?.setOnClickListener{
            alertLayout.representativeSign?.clear()
        }

        alertLayout.ivCustomerCancel?.setOnClickListener{
            alertLayout.customerSign?.clear()
        }

        alertLayout.txtReject?.setOnClickListener {
            SignDialog?.dismiss()
        }
        alertLayout.txtConfirm?.setOnClickListener {
            if(isValidateSign(alertView)) {
                acessRuntimPermission(alertView)
                //SignDialog?.dismiss()
            }

        }

        SignDialog = alert.create()
        SignDialog?.getWindow()?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        SignDialog?.show()


    }

    fun isValidateSign(alertLayout:View):Boolean{
        if (alertLayout.representativeSign.isEmpty) {
            mUtils.showAlert(activity, mView.context.resources.getString(R.string.representative_sign_error))
            return false
        }
        else if(alertLayout.customerSign.isEmpty) {
            mUtils.showAlert(activity, mView.context.resources.getString(R.string.customer_sign_error))
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

    fun generateNoInspectionRequest(): BoderueDeleteLogReq {
        var request : BoderueDeleteLogReq =   BoderueDeleteLogReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(bodereuHeaderId)
        return  request

    }

    fun funShowAlertForNoInspection(){
        val alert: AlertDialog =
            android.app.AlertDialog.Builder(view?.context, R.style.CustomDialogWithBackground)
                //.setTitle(activity?.getString(R.string.app_name))
                .setMessage(mView.context.resources.getString(R.string.are_you_sure_you_want_to_reject_inspection))
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        //Noo Inspection api call
                        callingRejectionAPI()
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


    private fun callingRejectionAPI() {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateNoInspectionRequest()
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
                                        var fragment  = MainContainerFragment()
                                        val bundle = Bundle()
                                        bundle.putString(
                                            Constants.comming_from,
                                            Constants.from_sales
                                        )
                                        fragment.arguments = bundle
                                        (activity as HomeActivity).replaceFragment(fragment,true)
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



    private fun callingShowPreviewSalesInvoiceAPI(represetativeSign:String,cuustomerSign:String) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request =  generateConfirmForBillingRequest(represetativeSign,cuustomerSign)//generateJsonStringrequestForBasee64(represetativeSign,cuustomerSign)

                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.pdfPreviewSalesInvoice(request)
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
                                        SignDialog?.dismiss()
                                        if(!response.body().getPdfFilePath().isNullOrEmpty()){

                                            var base64Image = ""
                                            if(response.body().getPdfFilePath()?.length!! > 100) {
                                                if(response.body().getPdfFilePath()!!.contains("data:application/pdf;base64")) {
                                                    base64Image = response.body().getPdfFilePath()!!.split(",")[1]
                                                }else{
                                                    base64Image = response.body().getPdfFilePath()!!
                                                }
                                               /* SaveBitmapToInternalStorageAsyncTask().execute(
                                                    base64Image
                                                )*/
                                            }

                                            val pdfPreviewIntent: Intent =
                                                Intent(
                                                    context,
                                                    ShowPDFPreview::class.java
                                                )
                                            pdfPreviewIntent.putExtra(Constants.PDF_PATH,base64Image)
                                           startActivityForResult(pdfPreviewIntent, PDF_PREVIEW_REQUEST_CODE);
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



    private fun callingConfirrmForApprovalAPI() {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateConfirmForApproveRequest()
                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.addSalesBordereauLogs(request)
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
                                        val fragment  = MainContainerFragment()
                                        val bundle = Bundle()
                                        bundle.putString(
                                            Constants.comming_from,
                                            Constants.from_sales
                                        )
                                        fragment.arguments = bundle
                                        (activity as HomeActivity).replaceFragment(fragment,true)
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


    private fun callingConfirmForBillinngAPI(represetativeSign:String,cuustomerSign:String) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request =  generateConfirmForBillingRequest(represetativeSign,cuustomerSign)//generateJsonStringrequestForBasee64(represetativeSign,cuustomerSign)

                val call: Call<AddBodereuLogListingRes> =
                    apiInterface.confirmForBilling(request)
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
                                        if(!response.body().getPdfFilePath().isNullOrEmpty()){
                                            response.body().getPdfFilePath()?.let {
                                               /* downloadPDFFromURL(
                                                    it
                                                )*/
                                                var base64Image = ""

                                                if(it.contains("data:application/pdf;base64")) {
                                                    base64Image = response.body().getPdfFilePath()!!.split(",")[1]
                                                }else{
                                                    base64Image = response.body().getPdfFilePath()!!
                                                }

                                                SaveBitmapToInternalStorageAsyncTask().execute(base64Image)
                                            }
                                        }
                                        //SignDialog?.dismiss()
                                       // response.body().getPdfFilePath()?.let { c(it) }
                                        mUtils.showToast(activity, response.body().getMessage())
                                        var fragment = MainContainerFragment()
                                        val bundle = Bundle()
                                        bundle.putString(
                                            Constants.comming_from,
                                            Constants.from_sales
                                        )
                                        fragment.arguments = bundle
                                        (activity as HomeActivity).replaceFragment(fragment,true)
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



        @SuppressLint("WrongThread")
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
            var destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/";
            var fileName = "$mFileName.pdf";
            destination += fileName;
            var uri = Uri.parse("file://" + destination);
            val pdfURL = pdfPath

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

    private fun callingdeleteINspectionLogAPI(position:Int) {
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



    private fun callingUploadImageApi(file:File) {
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
                    apiInterface.uploadSign(filename,photo)
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
                                      countImgeUpload ++
                                        if(countImgeUpload==1){
                                            firstImagePath = response.body().getPdfFilePath().toString()
                                            val representativeSigbBitmap = represemtativeBitmap
                                            SaveBitmapAsyncTask().execute(representativeSigbBitmap)
                                        }
                                        if(countImgeUpload==2){
                                            secodnImagepath = response.body().getPdfFilePath().toString()
                                            callingShowPreviewSalesInvoiceAPI(firstImagePath,secodnImagepath)
                                          /*  callingConfirmForBillinngAPI(firstImagePath,secodnImagepath)*/
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
                                countImgeUpload = 0
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
                        countImgeUpload = 0
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


    fun getSalesBodereuLogsByIDRequest(bodereuHeaderId: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setBordereauHeaderId(bodereuHeaderId)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }



    private fun getSalesBodereuLogsByID(bodereuHeaderId:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
val reqquest  =  getSalesBodereuLogsByIDRequest(bodereuHeaderId)
                val call_api: Call<GetBodereuLogByIdRes> =
                    apiInterface.getSalesBodereuLogsByID(reqquest)
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
                                        LogsListingResponce = responce
                                        logsLiting?.clear()
                                        responce.getBordereauLogList()?.let {
                                            logsLiting?.addAll(
                                                it
                                            )
                                        }
                                    }
                                    setTotalNoOfLogs(logsLiting?.size)
                                    adapter.notifyDataSetChanged()
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


   /* @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun buttonGenerate_onClick(logModel: BodereuLogListing) {
        try {
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
            }
            val productId = logModel.getBarcodeNumber()
            //Generate Barcoode with product ID
            val bitmap = mUtils.generatebarcodeBitmap(productId)
            mView.ivBarcode.setImageBitmap(bitmap)
            mView.txtBardCodeId.text  = logModel.getBarcodeNumber().toString()
            mView.txtBCLog.text  =  logModel.getLogNo().toString()
            mView.txtBCAAC.text = logModel.getAACName().toString()
            mView.txtBCCBM.text = logModel.getCbm().toString()
            mView.txtbcDia.text = "D : "+logModel.getDiamBdx().toString()
            mView.txtBCLong.text = "L : "+logModel.getLongBdx().toString()
            mView.txtBCForest.text = supplierShortName
            val barcodeValue  = originName+"/"+logModel.getAACName().toString()+aacYear
            mView.txtBCTransporter.text=barcodeValue
            mView.txtBCOrigin.text = supplierLocationName
            mView.txtBCEssense.text= logModel.getLogSpeciesName()
            mView.txtBCDate.text = mUtils?.getCurrentDate()

            Handler().postDelayed({
                savePdf()
            },DelayTime.toLong())

        } catch (e: Exception) {
            Toast.makeText(view?.context, e.message, Toast.LENGTH_LONG).show()
        }
    }*/



 /*   @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
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

/*    fun printPDF(pdfPath:String){
        val printManager =
            mView.context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        *//*val printAdapter =
            PdfDocumentAdapter(this@PrintActivity, "/storage/emulated/0/Download/test.pdf")*//*
        val printAdapter =
            PdfDocumentAdapter(mView.context, pdfPath)
        printManager.print("Document",printAdapter, PrintAttributes.Builder().build())
    }

    fun  checkIsFragmentDialogAlreadyShowing():Boolean{
        if(isDialogShowing){
            return false
        }
        return true
    }*/

    //Dialog content
    open fun showDialog(countryListSearch:ArrayList<SupplierDatum?>?, action:String) {

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
           // mView.txtBordero_No.clearFocus()
        }catch (e:Exception){
            e.printStackTrace()
        }
        if (model != null) {
            try{
                when (action) {

                    "essence"->{
                        alertView.edtEssence?.setText( model.optionName)
                        speciesID  = model.optionValue!!
                        alertView.edtDia?.requestFocus()
                    }
                    "aac"->{
                        alertView.edtAAC?.setText( model.optionName)
                        aacID  = model.optionValue!!
                        aacYear = model.optionValueString!!
                        alertView.edt_log?.requestFocus()
                    }
                    "quality"->{
                        alertView.edtQuality?.setText( model.optionName)
                        qualityId  = model.optionValue!!
                        alertView.edtQuality?.requestFocus()
                    }
                    "grade"->{
                        alertView.edtTGrade?.setText( model.optionName)
                        gradeId  = model.optionValue!!
                        gradeName = model.optionName!!
                        alertView.edtTGrade?.requestFocus()
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
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivBOAdd -> {
                //user should be add only 20 logs
                if(logsLiting.size !=20) {
                    showAddLogDialog(false, 0)
                }
            }
            R.id.txtWaitingForApproval -> {
                if(logsLiting.size !=0) {
                    callingConfirrmForApprovalAPI()
                }
            }

            R.id.txtWaitingForBilling -> {
                if(logsLiting.size !=0) {
                    showDialogForDigitalSign()

                }
            }

            R.id.txtRejectInspection -> {
                funShowAlertForNoInspection()
            }

            R.id.ivHeaderEdit ->{
                if(commigFrom.equals( Constants.header, ignoreCase = true)) {
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
                    (activity as HomeActivity).replaceFragment(fragment,false)

                }else{
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
                    (activity as HomeActivity).replaceFragment(fragment,false)
                }
            }
        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PDF_PREVIEW_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                callingConfirmForBillinngAPI(firstImagePath,secodnImagepath)
            }
        }

    }

}
