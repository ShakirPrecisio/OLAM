package com.kemar.olam.dashboard.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kemar.olam.R
import com.kemar.olam.utility.Constants
import kotlinx.android.synthetic.main.activity_scan_s_d_k.*

class ScanSDKActivity : AppCompatActivity() {
    // private variables
    private val bRequestSendResult = false
    val LOG_TAG = "DataCapture1"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_s_d_k)
        btnSetDecoders.setOnClickListener{
            setDecoderValues()
        }
        btnCreateProfile.performClick()
        btnSetDecoders.performClick()
    }

    fun setDecoder(decoder: CheckBox): String {
        val checkValue = decoder.isChecked
        var value = "false"
        return if (checkValue) {
            value = "true"
            value
        } else value
    }

    fun ToggleSoftScanTrigger(view: View?) {
        sendDataWedgeIntentWithExtra(
            Constants.ACTION_DATAWEDGE,
            Constants.EXTRA_SOFT_SCAN_TRIGGER,
            "TOGGLE_SCANNING"
        )
    }

    fun setDecoderValues(){

        val checkCode128 = findViewById(R.id.chkCode128) as CheckBox
        val Code128Value: String = setDecoder(checkCode128)

        val checkCode39 = findViewById(R.id.chkCode39) as CheckBox
        val Code39Value: String = setDecoder(checkCode39)

        val checkEAN13 = findViewById(R.id.chkEAN13) as CheckBox
        val EAN13Value: String = setDecoder(checkEAN13)

        val checkUPCA = findViewById(R.id.chkUPCA) as CheckBox
        val UPCAValue: String = setDecoder(checkUPCA)
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
        appConfig.putString("PACKAGE_NAME", packageName)
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
        this.sendBroadcast(dwIntent)
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
        this.sendBroadcast(dwIntent)
    }

    // Unregister scanner status notification
    fun unRegisterScannerStatus() {
        Log.d(LOG_TAG, "unRegisterScannerStatus()")
        val b = Bundle()
        b.putString(Constants.EXTRA_KEY_APPLICATION_NAME, packageName)
        b.putString(
            Constants.EXTRA_KEY_NOTIFICATION_TYPE,
            Constants.EXTRA_KEY_VALUE_SCANNER_STATUS
        )
        val i = Intent()
        i.action = ContactsContract.Intents.Insert.ACTION
        i.putExtra(Constants.EXTRA_UNREGISTER_NOTIFICATION, b)
        this.sendBroadcast(i)
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
        registerReceiver(myBroadcastReceiver, filter)
    }

    override fun onResume() {
        super.onResume()
        registerReceivers()
    }

    // Create profile from UI onClick() event
    fun CreateProfile(view: View?) {
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
        appConfig.putString("PACKAGE_NAME", packageName)
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
        Toast.makeText(
            applicationContext,
            "Created profile.  Check DataWedge app UI.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(myBroadcastReceiver)
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
            initiatingIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_data))
        // store decoder type
        val decodedLabelType =
            initiatingIntent.getStringExtra(resources.getString(R.string.datawedge_intent_key_label_type))
        val lblScanData = findViewById(R.id.lblScanData) as TextView
        val lblScanLabelType = findViewById(R.id.lblScanDecoder) as TextView
        lblScanData.text = decodedData
        lblScanLabelType.text = decodedLabelType
        var resultIntent : Intent = Intent();
        resultIntent.putExtra(Constants.scan_code, decodedData)
        setResult(Activity.RESULT_OK,resultIntent)
        finish()
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
                val txtDWVersion = findViewById(R.id.txtGetDWVersion) as TextView
                txtDWVersion.text = DWVersion
                Log.i(LOG_TAG, "DataWedge Version: $DWVersion")
            }
            if (action == resources.getString(R.string.activity_intent_filter_action)) { //  Received a barcode scan
                try {
                    displayScanResult(intent, "via Broadcast")
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
                            applicationContext,
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
                                val lblScannerStatus =
                                    findViewById(R.id.lblScannerStatus) as TextView
                                lblScannerStatus.text = displayScannerStatusText
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