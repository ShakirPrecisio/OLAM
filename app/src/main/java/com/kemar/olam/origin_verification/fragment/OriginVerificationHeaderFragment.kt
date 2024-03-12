package com.kemar.olam.origin_verification.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kemar.olam.R
import com.kemar.olam.dashboard.activity.BarcodeScanActivity
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.origin_verification.model.request.OriginUserHistoryReq
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment__add_header.*
import kotlinx.android.synthetic.main.fragment__add_header.view.ivNext
import kotlinx.android.synthetic.main.fragment__add_header.view.linVehicleNo
import kotlinx.android.synthetic.main.fragment__add_header.view.txtForest
import kotlinx.android.synthetic.main.fragment__add_header.view.txtVehicleNo
import kotlinx.android.synthetic.main.fragment_origin_verification_header.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.Comparator

class OriginVerificationHeaderFragment : Fragment(), DialogFragment.GetDialogListener,
    View.OnClickListener {

    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var countryDialogFragment: DialogFragment
    var forestList: ArrayList<SupplierDatum?>? = arrayListOf()
    var commonForestMaster: GetForestDataRes? = GetForestDataRes()
    var isDialogShowing: Boolean = false
    var vehicleID: Int? = 0
    var forestID: Int = 0


    //Scan feature
    var RESULT_CODE = 103;
    val LOG_TAG = "DataCapture1"
    private val bRequestSendResult = false

    lateinit var searchBordereuList : ArrayList<String>
    lateinit var autoCompleteAdapter: ArrayAdapter<String>

    lateinit var  presectedCalender : Calendar
    var selectedDate :String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_origin_verification_header, container, false)
        mUtils = Utility()
        init()
        CreateProfile()
        setDecoderValues()
        return mView
    }


    private fun init() {
        presectedCalender = Calendar.getInstance()
        setToolbar()
        setupAutoCompleteListnerForBordereuNo()
        setupClickListner()
        mView.swipeOriginHeader.setOnRefreshListener {
            clearSelection()
            mView.swipeOriginHeader.isRefreshing = false
            getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
        }

        callingForestMasterAPI("0")
        getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
    }


    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.origin_verification)
        (activity as HomeActivity).invisibleFilter()
    }


    fun setupAutoCompleteListnerForBordereuNo(){

        mView.edt_Bordero_No.visibility = View.VISIBLE

        searchBordereuList = arrayListOf()

        //Creating the instance of ArrayAdapter containing list of language names
        autoCompleteAdapter = ArrayAdapter<String>(mView.context, R.layout.layout_autocomplete, searchBordereuList)

        mView.edt_Bordero_No.threshold = 2 //will start working from first character

        mView.edt_Bordero_No.setAdapter(autoCompleteAdapter) //setting the adapter data into the AutoCompleteTextView
        activity?.getWindow()?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        mView.edt_Bordero_No.setOnItemClickListener { parent, view, position, id ->


        }

    }


    fun setupClickListner() {
        mView.ivNext.setOnClickListener(this)
        mView.linOriginForest.setOnClickListener(this)
        /* mView.linOriginCFAT.setOnClickListener(this)
         mView.linDestination.setOnClickListener(this)
         mView.linTranporter.setOnClickListener(this)*/
        mView.cardScan.setOnClickListener(this)
        mView.linVehicleNo.setOnClickListener(this)
        mView.linDeclareDDate.setOnClickListener(this)

    }

    fun getForestByLocationRequest(userLocationID:String): CommonRequest {
        var request : CommonRequest =   CommonRequest()
        request.setUserLocationId(userLocationID)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    private fun getForestDataByLocation(userLocationID: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val request  =   getForestByLocationRequest(userLocationID)
                val call_api: Call<List<SupplierDatum>> =
                    apiInterface.getForestDataByLocation(request)

                call_api.enqueue(object :
                    Callback<List<SupplierDatum>> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                        call: Call<List<SupplierDatum>>,
                        response: Response<List<SupplierDatum>>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: List<SupplierDatum> =
                                response.body()!!
                            if (responce != null) {
                                forestList?.clear()
                                forestList?.addAll(responce)
                                /* if(action.equals(Constants.action_edit,ignoreCase = true)){
                                     bindHeaderData(headerModel,todaysHistoryModel,commigFrom)
                                 }*/
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



    fun getForestMasterRequest(forestId:String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setForestId(forestId)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }



    private fun callingForestMasterAPI(forestId: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = getForestMasterRequest(forestId)
                val call_api: Call<GetForestDataRes> =
                    apiInterface.GetDropDownDataForOriginVehicle(request)
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
                                        var count = 0
                                        if (!forestId.equals("0")) {
                                            commonForestMaster = responce
                                        }

                                        autoCompleteAdapter.clear()
                                        searchBordereuList.clear()

                                        responce!!.getVehicleData()!!.forEach {
                                            searchBordereuList.add(it!!.optionValueString!!)
                                            autoCompleteAdapter.insert(
                                                it!!.optionValueString!!,
                                                count
                                            )
                                            count++
                                        }
                                        autoCompleteAdapter.notifyDataSetChanged()


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
            mView.edt_Bordero_No.clearFocus()
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
                       mView.edt_Bordero_No?.setText("")
                        view?.edt_Bordero_No?.clearFocus()
                        callingForestMasterAPI(
                            forestID.toString()
                        )
                    }
                    /*  "origin"->{
                          view?.txtOriginCFAT?.text =   model.optionName
                          originID  = model.optionValue!!
                          view?.txtOriginCFAT?.requestFocus()
                      }

                      "transporter"->{
                          view?.txtTranporter?.text =   model.optionName
                          transporterID  = model.optionValue!!
                          view?.txtTranporter?.requestFocus()

                      }*/


                    "vehicle" -> {
                        view?.txtVehicleNo?.text = model.optionName
                        vehicleID = model.optionValue!!
                        view?.txtVehicleNo?.requestFocus()
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
            R.id.linOriginForest -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(forestList, "forest")
            }
            R.id.linDestination -> {
                /*  if(checkIsFragmentDialogAlreadyShowing())
                  showDialog(commonForestMaster?.getTransDistanceData() as ArrayList<SupplierDatum?>?,"destination")*/
            }

            R.id.cardScan -> {
                acessRuntimPermissionForCamera()
            }
            R.id.linVehicleNo -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(
                        commonForestMaster?.getVehicleData() as ArrayList<SupplierDatum?>?,
                        "vehicle"
                    )
            }
            R.id.linDeclareDDate ->{
                datePicker(mView.txtStockDeclaredDatte)
            }


            R.id.ivNext -> {

                setupFilterData(
                    forestID,
                    mView.txtVehicleNo.text.toString(),
                    mView.edt_Bordero_No.text.toString(),
                    "",
                    selectedDate
                )
            }

        }
    }

    fun datePicker(txtView  : TextView){
        val now = Calendar.getInstance()
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
                        selectedDate= mUtils.dateFormater("dd-MM-yyyy","dd.MM.yyyy",dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)!!

                    }
                }, presectedCalender.get(Calendar.YEAR), presectedCalender.get(Calendar.MONTH),presectedCalender.get(
                    Calendar.DAY_OF_MONTH)
            )
        datePickerDialog.getDatePicker().setMaxDate(Date().time)
        datePickerDialog.show()
    }


    fun setupFilterData(
        forestId: Int,
        wagonNumber: String,
        bordereuNumber: String,
        barcodeNumber: String,
        stockDeclaredDate:String
    ) {
        val request = OriginUserHistoryReq()
        request.setSupplier(forestId)
        if (wagonNumber.equals("Select", ignoreCase = true)) {
            request.setWagonNo("")
        } else {
            request.setWagonNo(wagonNumber)
        }

        request.setBarcodeNumber(barcodeNumber)
        request.setBordereauNo(bordereuNumber)
        request.setStockDeclaredDate(stockDeclaredDate)
        val fragment =
            OriginVeriUserHistoryFragment()
        val bundle = Bundle()
        bundle.putString(
            Constants.comming_from,
            Constants.Bc_Header_screen
        )
        bundle.putSerializable(
            Constants.badereuFilterModel,
            request
        )
        fragment.arguments = bundle
        (activity as HomeActivity).replaceFragment(fragment, false)

    }

    fun acessRuntimPermissionForCamera() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                var intent = Intent(mView.context, BarcodeScanActivity::class.java)
                startActivityForResult(intent, RESULT_CODE)
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
                Manifest.permission.CAMERA
            )
            .check()
    }

    fun clearSelection() {

        forestID = 0
        vehicleID = 0


        mView.txtForest.text = "Select"
       mView.txtVehicleNo?.setText("Select")

    }

    fun checkIsFragmentDialogAlreadyShowing(): Boolean {
        if (isDialogShowing) {
            return false
        }
        return true
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

    override fun onResume() {
        super.onResume()
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
                // mView.txtResult.text  =  "Scan Value "+data?.getStringExtra(Constants.scan_code).toString()
                setupFilterData(0, "", "", scannedValue,"")
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
        //getLogDataByBarCode(decodedData)
        setupFilterData(0, "", "", decodedData,"")
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