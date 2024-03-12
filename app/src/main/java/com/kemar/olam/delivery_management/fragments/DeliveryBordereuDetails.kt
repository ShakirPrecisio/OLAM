package com.kemar.olam.delivery_management.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.delivery_management.adapter.DeliveryBordereuInfoAdapter
import com.kemar.olam.delivery_management.model.response.AddHeaderForDeliveryRes
import com.kemar.olam.delivery_management.model.response.LogsUserHistoryRes
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.dialog_layouut_add_transporter_n_vehicle.view.*
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.rvLogListing
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.swipeLogListing
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.tvNoDataFound
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.txtBO
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.txtCustomerName
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.txtForestWagonNo
import kotlinx.android.synthetic.main.fragment_delivery_log_details.view.txtTotalLogs
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.Comparator


class DeliveryBordereuDetails : Fragment() , DialogFragment.GetDialogListener {

    lateinit var mView: View
    lateinit var rvLogListing: RecyclerView
    lateinit var adapter: DeliveryBordereuInfoAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsLiting: ArrayList<BodereuLogListing> = arrayListOf()
    lateinit var mUtils: Utility
    //listings
    var commonForestMaster : GetForestDataRes? = GetForestDataRes()
    var isDialogShowing:Boolean = false
    lateinit var countryDialogFragment: DialogFragment
    lateinit var transporterNVehicleAlertView : View
    var essenceID:Int=0
    var commigFrom= ""
    var originName = ""
    var bodereuHeaderId = 0
    var forestID = 0
    var transporterID = 0
    var vehicleID =  0
    //var suplierID =""
    var customerID  :Int? = 0
    var deliveryId:Int?=0
    var headerModel  : AddHeaderForDeliveryRes =  AddHeaderForDeliveryRes()
    var LogsListingResponce = GetBodereuLogByIdRes()
    var todaysHistoryModel  : LogsUserHistoryRes.UserHist =  LogsUserHistoryRes.UserHist()
    var transporterVehicleDialog: AlertDialog? = null

    lateinit var alertView : View
    //Scan feature
    var RESULT_CODE = 103;
    val LOG_TAG = "DataCapture1"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_delivery_bordereu_details, container, false)
        initViews()
        return mView

    }

    fun initViews() {
        commigFrom =
            arguments?.getString(Constants.comming_from).toString();
        rvLogListing = mView.findViewById(R.id.rvLogListing)
        mUtils = Utility()
        setToolbar()
        setupRecyclerViewNAdapter()
        // setupFooterButtonAsPerUserRole()

        mView.swipeLogListing.setOnRefreshListener{
            mView.swipeLogListing.isRefreshing =  false
            callingDeliveryMasterAPI()
            getDeliveryBodereuLogsByDeliveryID(deliveryId.toString())
        }

        //when comming from headerScreen & User History Screen
        //suplierID == forest
        //
        if(commigFrom.equals( Constants.header, ignoreCase = true)) {
            var headerDataModel: AddHeaderForDeliveryRes? =
                arguments?.getSerializable(Constants.badereuModel) as AddHeaderForDeliveryRes
            if (headerDataModel != null) {
                headerModel =  headerDataModel
            }
            //bodereuHeaderId = headerDataModel?.bordereauHeaderId!!
            customerID = headerDataModel?.getCustomerId()
            //suplierID = headerDataModel?.getSupplierId().toString()
            deliveryId= headerDataModel?.getDeliveryId()
            // headerDataModel?.modeOfTransport?.let { setupTransportMode(it,mView.context) }

            //need to add bordreu no
            /*  if (headerDataModel?.bordereauNo?.toString().isNullOrEmpty()) {
                  mView.txtBO_NO.text = headerDataModel?.eBordereauNo?.toString().toString()
              } else {
                  mView.txtBO_NO.text = headerDataModel?.bordereauNo?.toString().toString()

              }*/
            mView.txtCustomerName.text = headerDataModel?.getCustomerName()?.toString()
            mView.txtForestWagonNo.text = headerDataModel?.getTruckName()?.toString()
            mView.txtBO.text = headerDataModel?.getDeliveryNumber()?.toString()

            callingDeliveryMasterAPI()
            getDeliveryBodereuLogsByDeliveryID(deliveryId.toString())
        }else{
            val headerDataModel: LogsUserHistoryRes.UserHist? =
                arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.UserHist
            if (headerDataModel != null) {
                todaysHistoryModel =  headerDataModel
            }
            //suplierID = headerDataModel?.supplierId.toString()
            deliveryId = headerDataModel?.deliveryId
            //headerDataModel?.transportMode?.let { setupTransportMode(it,mView.context) }

            customerID = headerDataModel?.customerId

            /*  if(headerDataModel?.eBordereauNo!=null || headerDataModel?.bordereauNo!=null){
                  mView.lin_Bordero_No?.visibility =  View.VISIBLE
                  if (headerDataModel?.bordereauNo?.isNullOrEmpty()!!) {
                      mView.txtBO_NO.text = headerDataModel?.eBordereauNo
                  } else {
                      mView.txtBO_NO.text = headerDataModel?.bordereauNo

                  }
              }else{
                  mView.lin_Bordero_No?.visibility =  View.GONE
              }*/

            mView.txtCustomerName.text = headerDataModel?.customerShortName?.toString()
            mView.txtForestWagonNo.text = headerDataModel?.truckName?.toString()
            mView.txtBO.text = headerDataModel?.deliveryNumber?.toString()

            callingDeliveryMasterAPI()
            getDeliveryBodereuLogsByDeliveryID(deliveryId.toString())
        }

    }


    fun setupRecyclerViewNAdapter() {
        logsLiting?.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        rvLogListing.layoutManager = mLinearLayoutManager
        adapter = DeliveryBordereuInfoAdapter(mView.context, logsLiting,commigFrom)
        rvLogListing.adapter = adapter
        //Expand Collapse Action
        adapter.onMoreClick = { modelData, position, isExpanded ->
            logsLiting.get(position).isExpanded = isExpanded
            adapter.notifyDataSetChanged()
        }

    }


    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.drawer_delivery_management)
        (activity as HomeActivity).invisibleFilter()
    }


    private fun setTotalNoOfLogs(count: Int) {
        if (count == 0) {
            mView.tvNoDataFound.visibility=View.VISIBLE
            mView.rvLogListing.visibility=View.GONE
            mView.txtTotalLogs.visibility =View.INVISIBLE
            mView.txtTotalLogs.text = getString(R.string.total_found,count)//"Total $count Found"
        } else {
            mView.tvNoDataFound.visibility=View.GONE
            mView.rvLogListing.visibility=View.VISIBLE
            mView.txtTotalLogs.visibility =View.VISIBLE
            mView.txtTotalLogs.text =getString(R.string.total_found,count)// "Total $count Found"
        }

    }

    fun deleteLogsEntry(position: Int) {
        logsLiting.removeAt(position)
        adapter.notifyDataSetChanged()
        setTotalNoOfLogs(logsLiting.size)
    }


    fun callingDeliveryMasterAPIRequest(): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun callingDeliveryMasterAPI() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val request  =  callingDeliveryMasterAPIRequest()
                val call_api: Call<GetForestDataRes> =
                    apiInterface.getMasterDataForDelivery(request)

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

    fun getDeliveryBodereuLogsByDeliveryIDRequest(deliveryId:String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setDeliveryId(deliveryId)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    private fun getDeliveryBodereuLogsByDeliveryID(deliveryId:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val request =  getDeliveryBodereuLogsByDeliveryIDRequest(deliveryId)
                val call_api: Call<GetBodereuLogByIdRes> =
                    apiInterface.getLogByDeliveryId(request)

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
                                    LogsListingResponce = GetBodereuLogByIdRes()
                                    logsLiting?.clear()
                                    if (responce.getSeverity() == 200) {
                                        LogsListingResponce = responce
                                        responce.getBordereauLogList()?.let {
                                            logsLiting?.addAll(
                                                it
                                            )
                                        }
                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                    setTotalNoOfLogs(logsLiting?.size)
                                    adapter.notifyDataSetChanged()
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
            // mView.txtBordero_No.clearFocus()
        }catch (e:Exception){
            e.printStackTrace()
        }
        if (model != null) {
            try{
                when (action) {

                    "transporter"->{
                        transporterNVehicleAlertView.txtTransporter?.setText( model.optionName)
                        transporterID  = model.optionValue!!
                        transporterNVehicleAlertView.txtTransporter?.requestFocus()
                    }
                    "vehicle"->{
                        transporterNVehicleAlertView.txtDVehicleNo?.setText( model.optionName)
                        vehicleID  = model.optionValue!!
                        transporterNVehicleAlertView.txtDVehicleNo?.requestFocus()
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


    fun isValidateTransporterNVehicleData():Boolean{
        if(transporterNVehicleAlertView.txtTransporter?.text=="Select"){
            mUtils.showAlert(activity, mView.context.getString(R.string.select_transporter))
            return false
        }
        else  if(transporterNVehicleAlertView.txtDVehicleNo?.text=="Select"){
            mUtils.showAlert(activity, mView.context.getString(R.string.select_vehicle_no))
            return false
        }
        return  true
    }




}
