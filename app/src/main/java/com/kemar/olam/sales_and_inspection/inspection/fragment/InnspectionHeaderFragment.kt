package com.kemar.olam.sales_and_inspection.inspection.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.R
import com.kemar.olam.bordereau.fragment.LogsListingFragment
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.sales_and_inspection.inspection.model.request.SalesHeaderReq
import com.kemar.olam.sales_and_inspection.inspection.model.request.SalesInspectDataByVehicleNoReq
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.dialog_add_log_layout.view.*
import kotlinx.android.synthetic.main.fragment__add_header.view.*
import kotlinx.android.synthetic.main.fragment_innspection_header.*
import kotlinx.android.synthetic.main.fragment_innspection_header.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.math.roundToInt


class InnspectionHeaderFragment : Fragment(), DialogFragment.GetDialogListener,
    View.OnClickListener {
    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var countryDialogFragment: DialogFragment
    lateinit var presectedCalender: Calendar
    var headerID: Int = 0
    var gradeID: Int = 0
    var parseDReceptionID: Int = 0
    var graderID: Int = 0
    var customerID: Int = 0
    var responsibleCustomerID: Int = 0
    var bordereauRecordNo: String = ""
    var inspectionDate: String = ""
    var tracerCharges:String=""
    var commigFrom = ""
    var action = ""
    var headerModel: AddBodereuRes.BordereauResponse = AddBodereuRes.BordereauResponse()
    var todaysHistoryModel: LogsUserHistoryRes.BordereauRecordList =
        LogsUserHistoryRes.BordereauRecordList()

    //listings
    var commonForestMaster: GetForestDataRes? = GetForestDataRes()
    var wagonList: ArrayList<SupplierDatum?>? = arrayListOf()
    var transporterList: ArrayList<SupplierDatum?>? = arrayListOf()
    var isDialogShowing: Boolean = false
    var iselectronicBordereau: Boolean = false
    var transporterFirstCount = 0
    var strBordereu1No :String? = ""
    var strEBordereuNo :String? = ""
    var strRecordNumber :String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_innspection_header, container, false)
        mUtils = Utility()
        initViews()
        return mView;
    }


    private fun callingAddSalesBordereauAPI() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateAddInspectionHeaderRequest()
                val call: Call<AddBodereuRes> =
                    apiInterface.addSalesInspectionBordereau(request)
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
                                        mUtils.showToast(activity, response.body().getMessage())
                                        val myactivity = activity as HomeActivity
                                        var fragment = SalesLogDetailsFragment()
                                      //set additional feild  which is not comming from api
                                        response.body().getInspectionHeader()?.eBordereauNo = strEBordereuNo
                                        response.body().getInspectionHeader()?.bordereauNo = strBordereu1No
                                        response.body().getInspectionHeader()?.bordereauHeaderId = headerID
                                        response.body().getInspectionHeader()?.bordereauRecordNo = bordereauRecordNo
                                        response.body().getInspectionHeader()?.wagonNo =  mView.txtWagonNo.text.toString()

                                        val bundle = Bundle()
                                        bundle.putSerializable(
                                            Constants.badereuModel,
                                            response.body().getInspectionHeader()
                                        )
                                        bundle.putString(
                                            Constants.comming_from,
                                            Constants.header
                                        )
                                        fragment.arguments = bundle
                                        myactivity?.replaceFragment(fragment,false)
                                      //  ResetAllFeilds()
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



    fun generateAddInspectionHeaderRequest(): SalesHeaderReq {

        var request : SalesHeaderReq =   SalesHeaderReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(headerID)
        request.setCustomerId(customerID)
        request.setResponsibleCustomerId(responsibleCustomerID)
        request.setResponsibleCustomer(mView.txtResCustName.text.toString())
        request.setGradeId(gradeID)
        request.setParcId(parseDReceptionID)
        request.setGraderId(graderID)
        request.setTracerCherges(tracerCharges)
        request.setPrice(mView.edtPrice.text.toString())
        request.setRefraction(mView.edtRefraction.text.toString())
        request.setInspectionDate(inspectionDate)
        request.setInspectionNO("")

        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setTimezoneId("Asia/Kolkata")//TimeZone.getDefault().getDisplayName()
        request.setUserLocationID(SharedPref.getUserId(Constants.user_location_id))
        request.setIsAdmin(false)



        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    fun initViews(){
        commigFrom =
            arguments?.getString(Constants.comming_from).toString();
        presectedCalender  = Calendar.getInstance()
        setToolbar()
        setupClickListner()
        callingGetMasterDataSalesInspectionAPI()
       /// callingGetWagonDataForInspectionAPI()
        if (commigFrom.equals( Constants.header, ignoreCase = true)) {
            var headerDataModel: AddBodereuRes.BordereauResponse? =
                arguments?.getSerializable(Constants.badereuModel) as AddBodereuRes.BordereauResponse
            if (headerDataModel != null) {
                headerModel = headerDataModel
                headerID = headerModel.bordereauHeaderId!!
                bordereauRecordNo = headerModel.bordereauRecordNo!!
            }
        } else{
            var headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
                arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
            if (headerDataModel != null) {
                todaysHistoryModel = headerDataModel
                headerID = todaysHistoryModel.bordereauHeaderId!!
                bordereauRecordNo =  todaysHistoryModel.recordDocNo!!
            }
        }
        getRedirectedData(headerModel,todaysHistoryModel,commigFrom)

        mView.swipeSalesHeader.setOnRefreshListener{
            mView.swipeSalesHeader.isRefreshing =  false
            callingGetMasterDataSalesInspectionAPI()
        }


        mView.rGroup.setOnCheckedChangeListener { group, checkedId ->
            // checkedId is the RadioButton selected
            if (checkedId != -1) {
                val rb = mView.findViewById(checkedId) as RadioButton
                when (rb?.text) {
                    mView.resources.getString(R.string.yes) -> {
                        tracerCharges = "Yes"
                    }
                    mView.resources.getString(R.string.No) -> {
                        tracerCharges = "No"
                    }
                }
            }
        }

    }
    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.inspection_n_sales)
        (activity as HomeActivity).invisibleFilter()
    }
    fun getRedirectedData(headerData : AddBodereuRes.BordereauResponse, todaysHistoryModel : LogsUserHistoryRes.BordereauRecordList, commingFrom:String){
        try {
            if (commigFrom.equals( Constants.header, ignoreCase = true)) {


            }else{
                mView.txtWagonNo.text = todaysHistoryModel?.wagonNo
                mView.txtForest_.text = todaysHistoryModel?.supplierShortName
                mView.txtDate_.text  =  mUtils.getCurrentDate()
                inspectionDate = mUtils.getCurrentDate()
                if(todaysHistoryModel?.bordereauNo.toString().isNullOrEmpty()){
                    mView.txtBorderoNo.text = todaysHistoryModel?.eBordereauNo
                    strBordereu1No = ""
                    strEBordereuNo = todaysHistoryModel?.eBordereauNo
                }else{
                    mView.txtBorderoNo.text = todaysHistoryModel?.bordereauNo
                    strBordereu1No =  todaysHistoryModel?.bordereauNo
                    strEBordereuNo = todaysHistoryModel?.eBordereauNo
                }
            }

            }
        catch (e:Exception){
            e.printStackTrace()
        }

    }

    fun setupClickListner() {
        mView.linListWagonNo.setOnClickListener(this)
        mView.linWagonNo.setOnClickListener(this)
        mView.linForest_.setOnClickListener(this)
        mView.linDate_.setOnClickListener(this)
        mView.linParcDeReception.setOnClickListener(this)
        mView.linCustName.setOnClickListener(this)
       // mView.linResCustName.setOnClickListener(this)
        mView.linGraderGSEZ.setOnClickListener(this)
        mView.linGrade.setOnClickListener(this)
        mView.ivNext_.setOnClickListener(this)
    }

    fun isValidateForm(): Boolean {
         if (view?.txtWagonNo?.text == "Select") {
             mUtils.showAlert(activity, mView.context.resources.getString(R.string.empty_wagon))
            return false
        }
         else if (view?.txtForest_?.text == "Select") {
             mUtils.showAlert(activity, mView.context.resources.getString(R.string.empty_forest))
             return false
         }
        else if (view?.txtBorderoNo?.text.isNullOrEmpty()) {
             mUtils.showAlert(activity, mView.context.resources.getString(R.string.empty_bordereau_no))
                return false
            }
        else if (view?.txtParseDeReception?.text == "Select") {
             mUtils.showAlert(activity, mView.context.resources.getString(R.string.please_select_parc_d_reception))
            return false
        } else if (view?.txtCustName?.text == "Select") {
             mUtils.showAlert(activity, mView.context.resources.getString(R.string.please_select_customer_name))
            return false
        } /*else if (view?.txtResCustName?.text == "Select") {
            mUtils.showAlert(activity, "Please select responsible customer name")
            return false
        }*/ else if (view?.txtGraderGSEZ?.text == "Select") {
             mUtils.showAlert(activity, mView.context.resources.getString(R.string.please_select_grader_gsez))
            return false
        } else if (view?.txtGrade?.text == "Select") {
             mUtils.showAlert(activity, mView.context.resources.getString(R.string.please_select_grade))
            return false
        }
        else if (view?.edtPrice?.text.toString().isNullOrEmpty()) {
             mUtils.showAlert(activity, mView.context.resources.getString(R.string.please_enter_price))
            return false
        }
        //not mandotory said by client
       /* else if(view?.edtRefraction?.text.toString().isNullOrEmpty()){
            mUtils.showAlert(activity, "Please enter refraction")
            return false
        }*/
         else if(!view?.edtRefraction?.text.toString().isNullOrEmpty()){
              if (10 < mView.edtRefraction?.text.toString().toInt()) {
                 mUtils.showAlert(activity, resources.getString(R.string.refraction_value_should_be_less_than_))
                 return false
             }
        }

        return true
    }


    fun resetSelection() {
        mView.txtForest_.text = "Select"
        mView.txtWagonNo.text = "Select"
        mView.txtBorderoNo.text = "Select"
        mView.txtResCustName.setText("")
        mView.txtParseDeReception.text = "Select"
        mView.txtCustName.text = "Select"
        mView.txtGraderGSEZ.text = "Select"
        mView.txtGrade.text = "Select"

    }

    fun clearSelection() {
        strBordereu1No = ""
        strEBordereuNo = ""
        mView.txtWagonNo.text = "Select"
        mView.txtForest_.text = "Select"
        mView.txtBorderoNo.text = "Select"

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
            mView.txtBorderoNo.clearFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (model != null) {
            try {
                when (action) {

                    "wagonlist" ->{
                        clearSelection()
                        view?.txtListWagonNo?.text =   model.optionName
                        view?.txtBorderoNo?.text = model.optionValueString
                        strEBordereuNo = model.optionValueString
                        strBordereu1No = model.bordereauNo
                        view?.txtListWagonNo?.requestFocus()
                        model.optionName?.let { callingGetSalesInspectDataByVehicleNoAPI(it) }
                       /* model.optionName?.let { model.optionValueString?.let { it1 ->
                            callingGetSalesInspectDataByVehicleNoAPI(it,
                                it1
                            )
                        } }*/
                    }
                    "forest" -> {

                    }
                    "parc_de_reception" ->{
                        view?.txtParseDeReception?.text =   model.optionName
                        parseDReceptionID = model.optionValue!!
                        view?.txtParseDeReception?.requestFocus()
                    }
                    "grade" -> {
                        view?.txtGrade?.text =   model.optionName
                        gradeID  = model.optionValue!!
                        view?.txtGrade?.requestFocus()
                    }
                    "grader" -> {
                        view?.txtGraderGSEZ?.text =   model.optionName
                        graderID  = model.optionValue!!
                        view?.txtGraderGSEZ?.requestFocus()
                    }


                    "customer_name" -> {
                        view?.txtCustName?.text =   model.optionName
                        customerID  = model.optionValue!!
                        view?.txtCustName?.requestFocus()
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


    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {

            R.id.linListWagonNo ->{
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(wagonList, "wagonlist")
            }

            R.id.linParcDeReception ->{
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog( commonForestMaster?.getParcData() as ArrayList<SupplierDatum?>?, "parc_de_reception")
            }

            R.id.linGrade -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(
                        commonForestMaster?.getGradeData() as ArrayList<SupplierDatum?>?,
                        "grade"
                    )
            }
            R.id.linGraderGSEZ -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(
                        commonForestMaster?.getGraderData() as ArrayList<SupplierDatum?>?,
                        "grader"
                    )
            }


            R.id.linCustName -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(
                        commonForestMaster?.getCCustomerData() as ArrayList<SupplierDatum?>?,
                        "customer_name"
                    )
            }

            R.id.linDate_->{
                datePicker(mView.txtDate_)
            }

            R.id.ivNext_ -> {
                if (isValidateForm()) {
                    callingAddSalesBordereauAPI()
                }

            }


        }
    }


    fun datePicker(txtView  : TextView){
        val datePickerDialog =
            DatePickerDialog(
                mView.context ,
                object :
                    DatePickerDialog.OnDateSetListener {
                    @SuppressLint("SetTextI18n")
                    override fun onDateSet(
                        view: DatePicker?, year: Int,
                        monthOfYear: Int, dayOfMonth: Int
                    ) {

                        presectedCalender.set(Calendar.YEAR,year);
                        presectedCalender.set(Calendar.MONTH,monthOfYear);
                        presectedCalender.set(Calendar.DAY_OF_MONTH,dayOfMonth)


                        txtView.setText(mUtils.dateFormater("dd-MM-yyyy","dd.MM.yyyy",dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year))
                        inspectionDate = mUtils.dateFormater("dd-MM-yyyy","dd.MM.yyyy",dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)!!

                    }
                }, presectedCalender.get(Calendar.YEAR), presectedCalender.get(Calendar.MONTH),presectedCalender.get(
                    Calendar.DAY_OF_MONTH)
            )
        datePickerDialog.getDatePicker().setMaxDate(Date().time)
        datePickerDialog.show()
    }


    fun checkIsFragmentDialogAlreadyShowing(): Boolean {
        if (isDialogShowing) {
            return false
        }
        return true
    }

   /* private fun callingGetWagonDataForInspectionAPI() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val call_api: Call<List<SupplierDatum>> =
                    apiInterface.getWagonDataForInspection(SharedPref.getUserId(Constants.user_location_id).toString())
                call_api.enqueue(object :
                    Callback<List<SupplierDatum>> {
                    override fun onResponse(
                        call: Call<List<SupplierDatum>>,
                        response: Response<List<SupplierDatum>>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: List<SupplierDatum> =
                                response.body()!!
                            if (responce != null) {

                                    wagonList?.clear()
                                wagonList?.addAll(responce)


                            }
                        }catch (e:Exception){
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
        }else{
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }
*/

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
                                        commonForestMaster = responce
                                        if (!commonForestMaster?.getGradeData().isNullOrEmpty()) {
                                            if (commonForestMaster?.getGradeData()?.size == 1) {
                                                view?.txtGrade?.text =
                                                    commonForestMaster?.getGradeData()?.get(0)
                                                        ?.optionName
                                                gradeID =
                                                    commonForestMaster?.getGradeData()?.get(0)?.optionValue!!
                                                view?.txtGrade?.requestFocus()
                                            }
                                        }
                                        if (!commonForestMaster?.getCCustomerData().isNullOrEmpty()) {
                                            if (commonForestMaster?.getCCustomerData()?.size == 1) {
                                                view?.txtCustName?.text =
                                                    commonForestMaster?.getCCustomerData()?.get(0)
                                                        ?.optionName
                                                customerID =
                                                    commonForestMaster?.getCCustomerData()?.get(0)?.optionValue!!
                                                view?.txtCustName?.requestFocus()
                                            }
                                        }

                                        if (!commonForestMaster?.getGraderData().isNullOrEmpty()) {
                                            if (commonForestMaster?.getGraderData()?.size == 1) {
                                                view?.txtGraderGSEZ?.text =
                                                    commonForestMaster?.getGraderData()?.get(0)
                                                        ?.optionName
                                                graderID =
                                                    commonForestMaster?.getGraderData()?.get(0)?.optionValue!!
                                                view?.txtGraderGSEZ?.requestFocus()
                                            }
                                        }

                                        if (!commonForestMaster?.getParcData().isNullOrEmpty()) {
                                            if (commonForestMaster?.getParcData()?.size == 1) {
                                                view?.txtParseDeReception?.text =
                                                    commonForestMaster?.getParcData()?.get(0)
                                                        ?.optionName
                                                graderID =
                                                    commonForestMaster?.getParcData()?.get(0)?.optionValue!!
                                                view?.txtParseDeReception?.requestFocus()
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

    fun generatealesInspectDataByVehicleNoRequest(wagonNo:String): SalesInspectDataByVehicleNoReq {

        var request : SalesInspectDataByVehicleNoReq =   SalesInspectDataByVehicleNoReq()
        request.setWagonNo(wagonNo)
        request.setEBordereauNo(strEBordereuNo)
        request.setUserLocationID(SharedPref.getUserId(Constants.user_location_id))
        request.setBordereauNo(strBordereu1No)

        var json =  Gson().toJson(request)

        var test  = json

        return  request

    }


    private fun callingGetSalesInspectDataByVehicleNoAPI(wagonNo:String) {
        if (mUtils?.checkInternetConnection(mView.context) == true) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generatealesInspectDataByVehicleNoRequest(wagonNo)
                val call: Call<AddBodereuRes> =
                    apiInterface.getSalesInspectDataByVehicleNo(request)
                call.enqueue(object :
                    Callback<AddBodereuRes> {
                    override fun onResponse(
                        call: Call<AddBodereuRes>,
                        response: Response<AddBodereuRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response.body() != null) {
                                    if (response.body()?.getSeverity() == 200) {
                                        val bordereauResponse = response.body()?.getBordereauResponse()
                                        mView.txtWagonNo.text = bordereauResponse?.wagonNo
                                        mView.txtForest_.text = bordereauResponse?.supplierName
                                        if(bordereauResponse?.bordereauNo.toString().isNullOrEmpty()){
                                            mView.txtBorderoNo.text = bordereauResponse?.eBordereauNo
                                            strEBordereuNo =  bordereauResponse?.bordereauNo
                                            strBordereu1No= ""

                                        }else{
                                            mView.txtBorderoNo.text = bordereauResponse?.bordereauNo
                                            strBordereu1No =  bordereauResponse?.bordereauNo
                                            strEBordereuNo = bordereauResponse?.eBordereauNo
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




}