package com.kemar.olam.delivery_management.fragments


import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.itextpdf.text.pdf.PdfDocument
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.AddBodereuReq
import com.kemar.olam.bordereau.model.request.ValidateBodereueNoReq
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.delivery_management.model.response.*
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment__add_header.view.*
import kotlinx.android.synthetic.main.fragment_delivery_header_new.*
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.*
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.ivNext
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.linBordero_No
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.linDate
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.linDestination
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.linForest
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.linOriginCFAT
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.linTranporter
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.rgBordereu
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.swipeHeader
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.txtBoNumber
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.txtBordero_No
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.txtDate
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.txtDestination
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.txtForest
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.txtOriginCFAT
import kotlinx.android.synthetic.main.fragment_delivery_header_new.view.txtTranporter
import kotlinx.android.synthetic.main.fragment_loading_wagons_header.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.Comparator


class DeliveryHeaderFragment : Fragment(),DialogFragment.GetDialogListener,View.OnClickListener {

    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var countryDialogFragment: DialogFragment
    lateinit var presectedCalender: Calendar
    var forestID: Int = 0
    var headerID: Int = 0
    var originID: Int = 0
    var destinationID: Int = 0
    var customerID: Int = 0
    var transporterID: Int = 0
    var loading_location_id = 0
    var inchargeId = 0
    var vehicleID: Int? = 0
    var distionsID: Int = 0
    var selectedDate: String = ""
    var commigFrom = ""
    var action = ""
    var headerModel: AddBodereuRes.BordereauResponse = AddBodereuRes.BordereauResponse()
    var todaysHistoryModel: LogsUserHistoryRes.BordereauRecordList =
        LogsUserHistoryRes.BordereauRecordList()

    //listings
    var commonForestMaster: GetForestDataRes? = GetForestDataRes()
    var forestList: ArrayList<SupplierDatum?>? = arrayListOf()
    var supplierList: ArrayList<SupplierDatum?>? = arrayListOf()

    var customerList: ArrayList<Customer?>? = arrayListOf()
    var locationList: ArrayList<Location>? = arrayListOf()
    var destinationList: ArrayList<Incharge>? = arrayListOf()
    var transporterList: ArrayList<Transport>? = arrayListOf()
    var inChargeList: ArrayList<Incharge>? = arrayListOf()

    var isDialogShowing: Boolean = false
    var transporterFirstCount = 0

    var iselectronicBordereau: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_delivery_header_new, container, false)
        mUtils = Utility()

        getMasterDelivery()
        init()
        // (activity as HomeActivity).hideActionBar()
        return mView;
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun bindHeaderData(
        headerData: AddBodereuRes.BordereauResponse,
        todaysHistoryModel: LogsUserHistoryRes.BordereauRecordList,
        commingFrom: String
    ) {
        try {
           mView.linDate.isEnabled = false
           mView.txtDate.isEnabled = false
            mView.txtForest.isEnabled = false
            mView.linForest.isEnabled = false

            mView.linDate.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
           mView.linForest.setBackgroundResource(R.drawable.bg_for_editext_non_editable)

            if (commigFrom.equals(Constants.header, ignoreCase = true)) {
                mView.txtDate.setText(headerData?.bordereauDate.toString())
                headerID = headerData?.bordereauHeaderId!!
                selectedDate = headerData?.bordereauDate?.toString()!!
                mView.txtForest.text = headerData?.supplierName
                forestID = headerData?.supplier!!
                callingForestMasterAPI(
                    forestID.toString(),
                    SharedPref.getUserId(Constants.user_location_id).toString()
                )
                mView.txtOriginCFAT.text = headerData?.originName
                originID = headerData?.originID!!
                mView.txtLoadingLocation.text = "txtLoadingLocation"
                mView.txtDestination.text = headerData?.destination
                mView.txtTranporter.text = headerData?.transporterName
                transporterID = headerData?.transporterId!!
                vehicleID = headerData?.wagonId
                mView.txtTruckNo.setText(headerData?.wagonNo)
            } else {

                headerID = todaysHistoryModel?.bordereauHeaderId!!
                mView.txtDate.setText(todaysHistoryModel?.bordereauDateString)
                selectedDate = todaysHistoryModel?.bordereauDateString?.toString()!!
                mView.txtForest.text = todaysHistoryModel?.supplierName
                forestID = todaysHistoryModel?.supplierId!!
                callingForestMasterAPI(
                    forestID.toString(),
                    SharedPref.getUserId(Constants.user_location_id).toString()
                )
                mView.txtOriginCFAT.text = todaysHistoryModel?.originName
                originID = todaysHistoryModel?.originId!!
                mView.txtLoadingLocation.text = "txtLoadingLocation"
                mView.txtDestination.text = todaysHistoryModel?.destination
                mView.txtTranporter.text = todaysHistoryModel?.transporterName
                transporterID = todaysHistoryModel?.transporterId!!
                mView.txtTruckNo.setText(todaysHistoryModel?.wagonNo)
                vehicleID = todaysHistoryModel?.wagonId

            }
        } catch (e: Exception) {
            e.toString()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        action =
            arguments?.getString(Constants.action).toString()
        //mView.txtIncharge.text = SharedPref.read(Constants.user_name)
        commigFrom =
            arguments?.getString(Constants.comming_from).toString()
        if (action.equals(Constants.action_edit, ignoreCase = true)) {
            if (commigFrom.equals(Constants.header, ignoreCase = true)) {
                val headerDataModel: AddBodereuRes.BordereauResponse? =
                    arguments?.getSerializable(Constants.badereuModel) as AddBodereuRes.BordereauResponse
                if (headerDataModel != null) {
                    headerModel = headerDataModel
                }
            } else {
                val headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
                    arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
                if (headerDataModel != null) {
                    todaysHistoryModel = headerDataModel
                }
            }
        } else {
        }

        presectedCalender = Calendar.getInstance()
        setToolbar()

        mView.rgBordereu.setOnCheckedChangeListener { group, checkedId ->
            // checkedId is the RadioButton selected
            if (checkedId != -1) {
                val rb = mView.findViewById(checkedId) as RadioButton
                when (rb?.text) {
                    mView.resources.getString(R.string.electronic_bordereau) -> {
                       mView.linBordero_No.visibility = View.GONE
                        iselectronicBordereau = true
                       mView.txtBoNumber.visibility = View.GONE
                       mView.txtBordero_No.setText("")
                        /*   if (mView.txtForest.text.toString() == "Select") {
                               mUtils.showAlert(activity, "Please select forest")
                               mView.rgBordereu.clearCheck()

                           } else if (view?.txtDate?.text == "Select") {
                               mUtils.showAlert(activity, "Please select date")
                               mView.rgBordereu.clearCheck()
                           } else {
                               //creatting electronic borderiuea
                               if (action?.equals(Constants.action_non_edit, ignoreCase = true)) {
                                   if (!selectedDate.isNullOrEmpty() && forestID != 0) {
                                       callinGetBordereuSerialNumber()
                                   }
                               }
                           }*/
                    }
                    mView.resources.getString(R.string.manual_bordereau) -> {
                        iselectronicBordereau = false
                       mView.txtBoNumber.visibility = View.VISIBLE
                        mView.txtBordero_No.setText("")
                        enabledElectronicBorderueNo()

                    }


                }

            }
        }

        setupClickListner()
        mView.swipeHeader.setOnRefreshListener {
            ResetAllFeilds()
            mView.swipeHeader.isRefreshing = false
            getMasterDelivery()
            //getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
        }


       // getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun enabledElectronicBorderueNo(){
       mView.txtBordero_No.isEnabled = true
        mView.txtBordero_No.backgroundTintList =
                mView.context.resources.getColorStateList(R.color.white)
       mView.linBordero_No.visibility = View.VISIBLE
       mView.txtBoNumber.visibility = View.VISIBLE
       mView.linBordero_No.setBackgroundResource(R.drawable.bg_for_editest)
    }

    fun generateAddBodereuRequest(): AddBodereuReq {


        var request: AddBodereuReq = AddBodereuReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setCustomerId(customerID)
        request.setCustomerName(mView.txtCustomerName.text.toString())
        request.setTransporterID(transporterID)
        request.settranspoterName(mView.txtTranporter.text.toString())

        request.setLocationID(loading_location_id)
        request.setlocationName(mView.txtLoadingLocation.text.toString())
        request.setdestinationId(destinationID)
        request.setdestinationName(mView.txtDestination.text.toString())
        request.setinchargeId(inchargeId)
        request.setinchargeName(mView.txtIncharge.text.toString())

        request.settruckNo(mView.etTruckNo.text.toString())
        if(iselectronicBordereau){

            request.setborderearuType("ELECTRONIC")
            request.setBordereauNo("")
        }else {
            request.setborderearuType("MANUAL")
            request.setBordereauNo(mView.txtBordero_No.text.toString())
        }


        request.setDeliveryDate(selectedDate)
        request.setTimezoneId("IST")


     /*   request.setSupplier(forestID)
        request.setSupplierName(mView.txtForest.text.toString())
        request.setOriginID(originID)
        request.setOriginName(mView.txtOriginCFAT.text.toString())
        request.setTruckID(vehicleID)
        request.setTransporterID(transporterID)
        request.setCustomerId(destinationID)*/


        val json = Gson().toJson(request)
        var test = json

        return request

    }


    fun generateValidateBodereuNoRequest(): ValidateBodereueNoReq {

        var request: ValidateBodereueNoReq = ValidateBodereueNoReq()
        request.setBordereauDate(mView.txtDate.text.toString())
        request.setSupplier(forestID)


        var json = Gson().toJson(request)
        var test = json

        return request

    }

    fun setupClickListner() {
        mView.ivNext.setOnClickListener(this)
        mView.linCustomerName.setOnClickListener(this)
        mView.linLoadingLocation.setOnClickListener(this)
        mView.linForest.setOnClickListener(this)
        mView.linOriginCFAT.setOnClickListener(this)
        mView.linTranporter.setOnClickListener(this)
        mView.linDestination.setOnClickListener(this)
        mView.linIncharge.setOnClickListener(this)
        mView.linDate.setOnClickListener(this)

    }

    fun datePicker(txtView: TextView) {
        val now = Calendar.getInstance()
        val datePickerDialog =
            DatePickerDialog(
                mView.context,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    presectedCalender.set(Calendar.YEAR, year)
                    presectedCalender.set(Calendar.MONTH, monthOfYear)
                    presectedCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                    txtView.setText(
                        mUtils.dateFormater(
                            "dd-MM-yyyy",
                            "dd.MM.yyyy",
                            dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                        )
                    )
                    selectedDate = mUtils.dateFormater(
                        "dd-MM-yyyy",
                        "dd.MM.yyyy",
                        dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                    )!!
                },
                presectedCalender.get(Calendar.YEAR),
                presectedCalender.get(Calendar.MONTH),
                presectedCalender.get(
                    Calendar.DAY_OF_MONTH
                )
            )
        datePickerDialog.getDatePicker().setMaxDate(Date().time)
        datePickerDialog.show()
    }


    fun isValidateForm(): Boolean {

        if (view?.txtCustomerName?.text == "Select") {
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_customer_name))
            return false
        } else if (view?.txtLoadingLocation?.text == "Select") {
            mUtils.showAlert(
                activity,
                mView.resources.getString(R.string.please_select_loading_location)
            )
            return false
        }
        else if (view?.txtDestination?.text == "Select") {
            mUtils.showAlert(
                activity,
                mView.resources.getString(R.string.please_select_destination)
            )
            return false
        }

        else if (view?.txtTranporter?.text == "Select") {
            mUtils.showAlert(
                    activity,
                    mView.resources.getString(R.string.please_select_transporter)
            )
            return false
        }


        else  if (view?.txtIncharge?.text == "Select") {
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_incharge))
            return false
        }

        else  if (view?.txtDate?.text == "Select") {
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_date))
            return false
        }

        else if (view?.etTruckNo?.text.isNullOrEmpty()) {
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_vehicle_number))
            return false
        }

        else if (!isvalidTruckNumber(view?.etTruckNo?.text.toString())) {
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_enter_vehicle_number))
            return false
        }


        if(!iselectronicBordereau) {
            if (view?.txtBordero_No?.text.isNullOrEmpty()) {
                mUtils.showAlert(activity, mView.resources.getString(R.string.please_enter_bordereu_no))
                return false
            }else{
                return true
            }
        }

        return true
    }

    fun clearSelection() {

        forestID = 0
        originID = 0
        destinationID = 0
        vehicleID = 0
        distionsID = 0

        mView.txtForest.text = "Select"
        mView.txtOriginCFAT.text = "Select"
        mView.txtDestination.text = "Select"
        mView.txtLoadingLocation.text = "Log Park"
       mView.txtTruckNo?.setText("Select")

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun ResetAllFeilds() {
        forestID = 0
        originID = 0
        destinationID = 0
        vehicleID = 0
        distionsID = 0
        transporterID = 0
        headerID = 0

        mView.txtForest.text = "Select"
        mView.txtOriginCFAT.text = "Select"
        mView.txtDestination.text = "Select"
        mView.txtTranporter.text = "Select"
        mView.txtLoadingLocation.text = "Log Park"
        mView.txtTruckNo?.setText("Select")
        mView.txtDate.text =  mUtils.getCurrentDate()

        // forestList?.clear()
        //transporterList?.clear()
        //  commonForestMaster  = GetForestDataRes()
        enabledNonEditableField()

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun enabledNonEditableField() {
        mView.linDate.isEnabled = true
        mView.txtDate.isEnabled = true
        mView.txtForest.isEnabled = true
        mView.linForest.isEnabled = true

        mView.linDate.setBackgroundResource(R.drawable.bg_for_editest)
        mView.linForest.setBackgroundResource(R.drawable.bg_for_editest)
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.drawer_delivery_management)
        (activity as HomeActivity).invisibleFilter()
    }

    fun getForestMasterRequest(forestId: String, userLocationId: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setForestId(forestId)
        request.setUserLocationId(userLocationId)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun callingForestMasterAPI(forestId: String, userLocationId: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
val request  =  getForestMasterRequest(forestId,userLocationId)
                val call_api: Call<GetForestDataRes> =
                    apiInterface.getForestMasterForDelivery(request)
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
                                        if (!commonForestMaster?.getOriginMaster().isNullOrEmpty()) {
                                            if (commonForestMaster?.getOriginMaster()?.size == 1) {
                                                view?.txtOriginCFAT?.text =
                                                    commonForestMaster?.getOriginMaster()
                                                        ?.get(0)?.optionName
                                                originID =
                                                    commonForestMaster?.getOriginMaster()?.get(0)?.optionValue!!
                                                view?.txtOriginCFAT?.requestFocus()
                                            }
                                        }

                                        /*   if (!commonForestMaster?.getLeauChargementData().isNullOrEmpty()) {
                                        if (commonForestMaster?.getLeauChargementData()?.size == 1) {
                                            view?.txtLoadingLocation?.text =
                                                commonForestMaster?.getLeauChargementData()?.get(0)
                                                    ?.optionName
                                            leude_chargementID =
                                                commonForestMaster?.getLeauChargementData()?.get(0)?.optionValue!!
                                            view?.txtLoadingLocation?.requestFocus()
                                            view?.txtLoadingLocation?.requestFocus()
                                        }
                                    }*/

                                        if (!commonForestMaster?.getCCustomerData().isNullOrEmpty()) {
                                            if (commonForestMaster?.getCCustomerData()?.size == 1) {
                                                view?.txtDestination?.text =
                                                    commonForestMaster?.getCCustomerData()?.get(0)
                                                        ?.optionName
                                                destinationID =
                                                    commonForestMaster?.getCCustomerData()?.get(0)?.optionValue!!
                                                view?.txtDestination?.requestFocus()
                                                view?.txtDestination?.requestFocus()
                                            }
                                        }


                                    }else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }
                            }else{
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

    fun getForestDataByLocationRequest(userLocationID: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun getForestDataByLocation(userLocationID: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
val request  =  getForestDataByLocationRequest(userLocationID)
                val call_api: Call<List<SupplierDatum>> =
                    apiInterface.getForestDataByLocationForDelivery(request)
                call_api.enqueue(object :
                    Callback<List<SupplierDatum>> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                        call: Call<List<SupplierDatum>>,
                        response: Response<List<SupplierDatum>>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: List<SupplierDatum> = response.body()!!
                            if (responce != null) {
                                forestList?.clear()
                                commonForestMaster =  GetForestDataRes()
                                forestList?.addAll(responce)
                                if (action.equals(Constants.action_edit, ignoreCase = true)) {
                                    bindHeaderData(headerModel, todaysHistoryModel, commigFrom)
                                }else{
                                    mView.txtDate.text  =  mUtils.getCurrentDate()
                                    selectedDate = mUtils.getCurrentDate()
                                }
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

    private fun getMasterDelivery() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
               mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
               // val request  =  getForestDataByLocationRequest(userLocationID)
                val call_api: Call<DeliveryMasterResponse> =
                        apiInterface.getDeliveryMaster()
                call_api.enqueue(object :
                        Callback<DeliveryMasterResponse> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                            call: Call<DeliveryMasterResponse>,
                            response: Response<DeliveryMasterResponse>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            var responce:DeliveryMasterResponse = response.body()!!
                            if (responce.severity == 0) {

                                customerList?.clear()
                                customerList?.addAll(responce?.customer)

                                locationList?.clear()
                                locationList?.addAll(responce?.location)

                                destinationList?.clear()
                                destinationList?.addAll(responce?.destination)

                                transporterList?.clear()
                                transporterList?.addAll(responce?.transport)

                                inChargeList?.clear()
                                inChargeList?.addAll(responce?.incharge)

                                /*if (action.equals(Constants.action_edit, ignoreCase = true)) {
                                    bindHeaderData(headerModel, todaysHistoryModel, commigFrom)
                                }else{
                                    mView.txtDate.text  =  mUtils.getCurrentDate()
                                    selectedDate = mUtils.getCurrentDate()
                                }*/

                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                            call: Call<DeliveryMasterResponse>,
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

    private fun callingValidateBordereauNoAPI() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateValidateBodereuNoRequest()
                val call: Call<AddBodereuRes> =
                    apiInterface.validateBordereauNo(request)
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
                                        callingAddBordereauAPIForDelivery()

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
        } else {
            mUtils.showToast(view?.context, getString(R.string.no_internet))
        }
    }





    private fun callingAddBordereauAPIForDelivery() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateAddBodereuRequest()
                val call: Call<AddHeaderForDeliveryRes> =
                    apiInterface.addBordereauForDelivery(request)
                call.enqueue(object :
                    Callback<AddHeaderForDeliveryRes> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                        call: Call<AddHeaderForDeliveryRes>,
                        response: Response<AddHeaderForDeliveryRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response != null) {
                                    if (response.body().getSeverity() == 200) {
                                        mUtils.showToast(activity, response.body().getMessage())
                                        response.body().getCustomerId()
                                        val myactivity = activity as HomeActivity
                                        var prductFragment = DeliveryLogDetailsNewFragment()
                                        //response.body().supplierLocationName = txtOriginCFAT.text.toString()
                                        val bundle = Bundle()
                                        bundle.putSerializable(
                                            Constants.badereuModel,
                                            response.body()
                                        )
                                        bundle.putString(
                                            Constants.comming_from,
                                             Constants.header
                                        )
                                        prductFragment.arguments = bundle
                                        myactivity?.replaceFragment(prductFragment, false)
                                        ResetAllFeilds()
                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
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
                        call: Call<AddHeaderForDeliveryRes>,
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (model != null) {
            try {
                when (action) {
                    "forest" -> {
                        clearSelection()
                        view?.txtForest?.text = model.optionName
                        forestID = model.optionValue!!
                        view?.txtForest?.requestFocus()
                        callingForestMasterAPI(
                            forestID.toString(),
                            SharedPref.getUserId(Constants.user_location_id).toString()
                        )
                    }
                    "origin" -> {
                        view?.txtOriginCFAT?.text = model.optionName
                        originID = model.optionValue!!
                        view?.txtOriginCFAT?.requestFocus()
                    }

                    "transporter" -> {
                        view?.txtTranporter?.text = model.optionName
                        transporterID = model.optionValue!!
                        view?.txtTranporter?.requestFocus()

                    }
                    "customer" -> {
                        view!!.txtCustomerName?.text=model.optionName
                        customerID = model.optionValue!!
                        view?.txtCustomerName?.requestFocus()


                        view!!.txtDestination?.text=model.optionName
                        destinationID = model.optionValue!!

                    }

                    "loadinglocation" -> {
                        view!!.txtLoadingLocation?.text=model.optionName
                        loading_location_id = model.optionValue!!
                        view?.txtLoadingLocation?.requestFocus()

                    }

                    "destination"->{
                        view!!.txtDestination?.text=model.optionName
                        destinationID = model.optionValue!!
                        view?.txtDestination?.requestFocus()
                    }

                    "incharge" -> {
                        view!!.txtIncharge?.text=model.optionName
                        inchargeId = model.optionValue!!
                        view?.txtIncharge?.requestFocus()

                    }

                    "vehicle" -> {
                        view?.txtTruckNo?.text = model.optionName
                        vehicleID = model.optionValue!!
                        view?.txtTruckNo?.requestFocus()
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

            R.id.linForest -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(forestList, "forest")
            }

            R.id.linCustomerName -> {
                if (checkIsFragmentDialogAlreadyShowing()) {
                    supplierList?.clear()
                    for (customer in customerList!!) {
                        var supplierDatum = SupplierDatum()
                        supplierDatum.optionName = customer?.optionName
                        supplierDatum.optionValue = customer?.optionValue
                        supplierDatum.optionValueString = customer?.optionValueString
                        supplierList!!.add(supplierDatum)
                    }
                    showDialog(supplierList, "customer")
                }
            }


            R.id.linLoadingLocation->{
                if (checkIsFragmentDialogAlreadyShowing()) {
                    supplierList?.clear()
                    for (location in locationList!!) {
                        var supplierDatum = SupplierDatum()
                        supplierDatum.optionName = location?.optionName
                        supplierDatum.optionValue = location?.optionValue
                        supplierDatum.optionValueString = location?.optionValueString
                        supplierList!!.add(supplierDatum)
                    }
                    showDialog(supplierList, "loadinglocation")
                }
            }


            R.id.linOriginCFAT -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(
                        commonForestMaster?.getOriginMaster() as ArrayList<SupplierDatum?>?,
                        "origin"
                    )
            }

            R.id.linDestination -> {



                if (checkIsFragmentDialogAlreadyShowing()) {
                    supplierList?.clear()
                    for (destination in destinationList!!) {
                        var supplierDatum = SupplierDatum()
                        supplierDatum.optionName = destination?.optionName
                        supplierDatum.optionValue = destination?.optionValue
                        supplierDatum.optionValueString = destination?.optionValueString
                        supplierList!!.add(supplierDatum)
                    }
                    showDialog(supplierList, "destination")
                }
            }

            R.id.linTranporter -> {
                if (checkIsFragmentDialogAlreadyShowing()) {
                    supplierList?.clear()
                    for (transport in transporterList!!) {
                        var supplierDatum = SupplierDatum()
                        supplierDatum.optionName = transport?.optionName
                        supplierDatum.optionValue = transport?.optionValue
                        supplierDatum.optionValueString = transport?.optionValueString
                        supplierList!!.add(supplierDatum)
                    }
                    showDialog(supplierList, "transporter")
                }

            }

            R.id.linIncharge -> {
                if (checkIsFragmentDialogAlreadyShowing()) {
                    supplierList?.clear()
                    for (incharge in inChargeList!!) {
                        var supplierDatum = SupplierDatum()
                        supplierDatum.optionName = incharge?.optionName
                        supplierDatum.optionValue = incharge?.optionValue
                        supplierDatum.optionValueString = incharge?.optionValueString
                        supplierList!!.add(supplierDatum)
                    }
                    showDialog(supplierList, "incharge")
                }

            }

            R.id.linTruckNo -> {
               /* if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(
                        commonForestMaster?.getVehicleData() as ArrayList<SupplierDatum?>?,
                        "vehicle"
                    )*/
            }
          /*  R.id.linLoadingLocation -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(
                        commonForestMaster?.getLeauChargementData() as ArrayList<SupplierDatum?>?,
                        "chargement"
                    )
            }*/
            R.id.linDate -> {
                datePicker(mView.txtDate)
            }
            R.id.ivNext -> {
                if (isValidateForm()) {
                    callingAddBordereauAPIForDelivery()
                   /* if (action.equals(Constants.action_edit, ignoreCase = true)) {
                        callingAddBordereauAPI()
                    } else {
                        callingValidateBordereauNoAPI()
                    }*/
                }

            }


        }
    }

    fun isvalidTruckNumber(password: String):Boolean {

        val pattern: Pattern;
        val matcher: Matcher;

        val PASSWORD_PATTERN:String = "[a-zA-Z]{2,}[-]{1,}[0-9]{3}[-]{1,}[a-zA-Z]{2}"

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);
        var condition=matcher.matches()
        return condition

    }

    fun checkIsFragmentDialogAlreadyShowing(): Boolean {
        if (isDialogShowing) {
            return false
        }
        return true
    }

}