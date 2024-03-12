package com.kemar.olam.loading_wagons.fragment

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kemar.olam.R
import com.kemar.olam.dashboard.activity.BarcodeScanActivity
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.utility.Constants
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_loading_wagons.view.*
import java.util.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class LoadingWagonsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var mView : View
    var RESULT_CODE = 103;
    val LOG_TAG = "DataCapture1"
    private val bRequestSendResult = false

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
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.loading_wagons)
        (activity as HomeActivity).invisibleFilter()
        mView =  inflater.inflate(R.layout.fragment_loading_wagons, container, false)
        CreateProfile()
        setDecoderValues()
        mView.btnScanWithMob.setOnClickListener{
            mView.txtResult.text = "Scan Data"
            acessRuntimPermission()
        }

        mView.btnScanWitZebra.setOnClickListener{
            mView.txtResult.text = "Scan Data"
            ToggleSoftScanTrigger()
      /*      var intent = Intent(mView.context, ScanSDKActivity::class.java)
            startActivityForResult(intent,RESULT_CODE)*/
        }
        return  mView;
    }


    fun acessRuntimPermission() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                var intent = Intent(mView.context, BarcodeScanActivity::class.java)
                startActivityForResult(intent,RESULT_CODE)
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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                mView.txtResult.text  =  "Scan Value "+data?.getStringExtra(Constants.scan_code).toString()
            }
        }

    }



    //Scanner With Zebra Device
    fun setDecoder(decoder: CheckBox): String {
        val checkValue = decoder.isChecked
        var value = "false"
        return if (checkValue) {
            value = "true"
            value
        } else value
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
        mView.txtResult.text =  "Scan Value "+decodedData
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
            }else if (action == Constants.ACTION_RESULT) { // Register to receive the result code
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