package com.kemar.olam.physicalcount.fragment

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.dashboard.activity.BarcodeScanActivity
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.physicalcount.adapter.PhysicalBarcodeAdapter
import com.kemar.olam.physicalcount.model.PhysicalCountModel
import com.kemar.olam.physicalcount.requestbody.Detail
import com.kemar.olam.physicalcount.requestbody.PhysicalCountRequest
import com.kemar.olam.physicalcount.response.StockListResponse
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.RealmHelper
import com.kemar.olam.utility.Utility
import io.realm.Realm
import io.realm.RealmList
import io.realm.RealmResults
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_physical_scan.*
import kotlinx.android.synthetic.main.fragment_physical_scan.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PhysicalScanFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PhysicalScanFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var mView: View
    lateinit var mUtils: Utility

    lateinit var realm: Realm
    var barcodelist= ArrayList<Detail>()

    val LOG_TAG = "DataCapture1"
    //Scan feature
    var RESULT_CODE = 103;

    var isOffline=false

    lateinit var physicalBarcodeAdapter: PhysicalBarcodeAdapter
    lateinit var physicalCountModel: PhysicalCountModel

    lateinit var   stockListResponse:  StockListResponse
    var isSubmit=false

    var stockId=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mUtils = Utility()

        stockId = arguments?.getString(Constants.action,"").toString();
        isOffline = arguments?.getBoolean(Constants.Offline,false)!!


        realm = RealmHelper.getRealmInstance()
        mView =inflater.inflate(R.layout.fragment_physical_scan, container, false)
        setupClickListner()
        barcodelist.clear()
        setToolbar()

        if (mUtils.checkInternetConnection(mView.context)) {
            if(!stockId.isNullOrEmpty()) {
                getStockListInfo()
            }

            try{
                physicalCountModel = realm.where(PhysicalCountModel::class.java).equalTo("uniqueId", stockId).findFirst()!!

                for (barcode in physicalCountModel.details!!){
                    var deat = Detail()
                    deat.barcodeNumber = barcode.barcodeNumber
                    deat.uniqueId = barcode.uniqueId
                    barcodelist.add(deat)
                }
            }catch (e:java.lang.Exception){
                e.printStackTrace()
            }

        }else {

        }

        setRecycler()

        return mView
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.physical_count)
    }

    fun setupClickListner() {
       mView.txtScan.setOnClickListener {
            acessRuntimPermissionForCamera()
        }

        mView.txtSave.setOnClickListener {

            Log.e("SAve","SAve")
            if (mUtils.checkInternetConnection(mView.context)) {
                callScanLog(getString(R.string.save))
            }else {
                val date: String =
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

                realm.executeTransactionAsync({ bgRealm ->

                    var physicalCountModel = PhysicalCountModel()
                    physicalCountModel.uniqueId=System.currentTimeMillis().toString()
                    physicalCountModel.startDate = date
                    physicalCountModel.endDate = ""
                    var details = RealmList<com.kemar.olam.physicalcount.model.Detail>()

                    for (detail in barcodelist) {
                        var detai = com.kemar.olam.physicalcount.model.Detail()
                        detai.barcodeNumber = detail.barcodeNumber
                        detai.uniqueId = detail.uniqueId
                        details.add(detai)
                    }
                    physicalCountModel.details = details
                    bgRealm.copyToRealmOrUpdate(physicalCountModel)

                }, {
                    mUtils.showToast(requireActivity(), "Save successful")
                    activity!!.onBackPressed()
                }) {
                    mUtils.showAlert(requireActivity(), "Save failure")
                }
            }
        }

        mView.txtSubmit.setOnClickListener {
            Log.e("Submit","Submit")
            if (mUtils.checkInternetConnection(mView.context)) {
                callScanLog(getString(R.string.submit))
            }else{

            }
        }
    }

    fun setRecycler(){
        physicalBarcodeAdapter= PhysicalBarcodeAdapter(barcodelist, requireContext() ,isSubmit)
        mView.rvPhysical!!.layoutManager = LinearLayoutManager(
            requireActivity(),
            RecyclerView.VERTICAL,
            false
        )
        mView.rvPhysical.adapter=physicalBarcodeAdapter

        physicalBarcodeAdapter!!.onItemClick = { data, position, holder ->
            showRemoveDiagloue(data, position)
        }

        setData()
    }

    fun showRemoveDiagloue(data: Detail, position: Int) {

        val builder = AlertDialog.Builder(requireActivity())

        builder.setTitle("Remove")
        builder.setMessage("Are you sure you want to remove barcode?")

        builder.setPositiveButton("YES") { dialog, which -> // Do nothing but close the dialog
            dialog.dismiss()
            barcodelist.remove(data)
            physicalBarcodeAdapter.notifyDataSetChanged()
            setData()
        }

        builder.setNegativeButton("NO") { dialog, which -> // Do nothing
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()
    }

    fun setData(){
        if(barcodelist.size>0){
            mView.rvPhysical.visibility=View.VISIBLE
            mView.noDataFound.visibility=View.GONE
        }else{
            mView.noDataFound.visibility=View.VISIBLE
            mView.rvPhysical.visibility=View.GONE
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PhysicalScanFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PhysicalScanFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun callScanLog(status: String) {
        try {
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
            val date: String = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

            var physicalCountRequest= PhysicalCountRequest()
            physicalCountRequest.startDate=date
            if(!stockId.isNullOrEmpty()) {
                physicalCountRequest.id = stockId
            }
            physicalCountRequest.endDate=date
            physicalCountRequest.status=status


            if(status.equals(getString(R.string.submit))){
                if(!stockId.isNullOrEmpty()) {
                    physicalCountRequest.id = stockId
                }
            }

            var listdetail=ArrayList<Detail>()

            for(details in barcodelist){
                var detail=Detail()
                detail.barcodeNumber=details.barcodeNumber
                detail.id=details.id
                listdetail.add(detail)
            }
            physicalCountRequest.details=listdetail

            val call: Call<AddBodereuRes> = apiInterface.physicalLogScan(physicalCountRequest)
            call.enqueue(object :
                Callback<AddBodereuRes> {
                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onResponse(
                    call: Call<AddBodereuRes>,
                    response: Response<AddBodereuRes>
                ) {
                    try {
                        if (response.code() == 200) {
                            if (response != null) {
                                if (response.body().getSeverity() == 200) {

                                    mUtils.showToast(requireActivity(),response.body().getMessage())

                                    activity!!.onBackPressed()


                                } else if (response.body()?.getSeverity() == 306) {
                                    mUtils.alertDialgSession(mView.context, activity)
                                } else if (response.body()?.getSeverity() == 202) {

                                } else if (response.body()?.getSeverity() == 201) {
                                    mUtils.showAlert(requireActivity(),response.body().getMessage())

                                } else {
                                    mUtils.showToast(activity, response.body().getMessage())
                                }
                            }

                        } else if (response.code() == 306) {
                            mUtils.alertDialgSession(mView.context, activity)
                        } else {
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                    call: Call<AddBodereuRes>,
                    t: Throwable
                ) {
                    t.printStackTrace()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun getStockListInfo() {
        try {
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

            var hashMap=HashMap<String,Any>()
            hashMap.put("stockListId",stockId)

            val call: Call<StockListResponse> = apiInterface.getStockListInfo(hashMap)
            call.enqueue(object :
                Callback<StockListResponse> {
                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onResponse(
                    call: Call<StockListResponse>,
                    response: Response<StockListResponse>
                ) {
                    try {
                        if (response.code() == 200) {
                            if (response != null) {
                                 stockListResponse=response.body()
                                if (stockListResponse.severity == 200) {

                                    barcodelist.clear()
                                    for(det in stockListResponse.stockListRequest?.details!!){
                                        var deat=Detail()
                                        deat.barcodeNumber= det.barcode.toString()
                                        deat.id=det.id
                                        deat.uniqueId=barcodelist.size
                                        barcodelist.add(deat)
                                    }
                                    visibleScan()

                                } else if (stockListResponse.severity == 306) {
                                    mUtils.alertDialgSession(mView.context, activity)
                                } else if (stockListResponse.severity == 202) {

                                } else if (stockListResponse.severity == 201) {
                                    mUtils.showAlert(requireActivity(),stockListResponse.message)

                                } else {
                                    mUtils.showToast(activity, stockListResponse.message)
                                }
                            }

                        } else if (response.code() == 306) {
                            mUtils.alertDialgSession(mView.context, activity)
                        } else {
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                    call: Call<StockListResponse>,
                    t: Throwable
                ) {
                    t.printStackTrace()
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun visibleScan(){
        if(stockListResponse.stockListRequest!!.status?.equals(getString(R.string.submit))!!){
            mView.lineScan.visibility=View.GONE
            mView.txtSave.visibility=View.GONE
            mView.txtSubmit.visibility=View.GONE
            mView.linLoadingFooter.visibility=View.GONE
            isSubmit=true
        }else{
            mView.lineScan.visibility=View.VISIBLE
            mView.txtSave.visibility=View.VISIBLE
            mView.txtSubmit.visibility=View.VISIBLE
            mView.linLoadingFooter.visibility=View.VISIBLE
            isSubmit=false
        }
        setRecycler()
        setData()
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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val scannedValue  =  data?.getStringExtra(Constants.scan_code).toString()
                Log.e("scannedValue", scannedValue)

                if(!isSubmit) {

                    if (checkBarcodeDuplicate(scannedValue)) {
                        var barcodeModel = Detail()
                        barcodeModel.barcodeNumber = scannedValue
                        barcodeModel.uniqueId = barcodelist.size + 1
                        barcodelist.add(barcodeModel)

                        setData()
                        physicalBarcodeAdapter.notifyDataSetChanged()
                    } else {
                        mUtils.showAlert(requireActivity(), "Duplicate Barcode ")
                    }
                }
            }
        }
    }

    fun checkBarcodeDuplicate(barcode: String):Boolean{
        for((index, listdata)  in barcodelist.withIndex()) {
            if (listdata.barcodeNumber?.contains(barcode)!!) {
                return false
            }
        }
        return true
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

        if(!isSubmit) {
            if (checkBarcodeDuplicate(scannedValue)) {
                var barcodeModel = Detail()
                barcodeModel.barcodeNumber = scannedValue
                barcodeModel.uniqueId = barcodelist.size + 1
                barcodelist.add(barcodeModel)

                setData()
                physicalBarcodeAdapter.notifyDataSetChanged()
            } else {
                mUtils.showAlert(requireActivity(), "Duplicate Barcode ")
            }
        }

       /* if (mUtils.checkInternetConnection(mView.context)) {
           // getLogDataByBarCode(scannedValue)
        } else {

        }*/

        Log.e("decodedData", scannedValue)
        //mView.txtResult.text =  "Scan Value "+decodedData
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