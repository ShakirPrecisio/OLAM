package com.kemar.olam.dashboard.activity

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.zxing.BarcodeFormat
import com.kemar.olam.R
import com.kemar.olam.utility.Constants
import kotlinx.android.synthetic.main.activity_barcode_scan.*

class BarcodeScanActivity : AppCompatActivity() {
    private lateinit var codeScanner: CodeScanner
    //Scan feature
    var RESULT_CODE = 103;
    val LOG_TAG = "DataCapture1"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scan)
        setBarcodeScanner()
    }


    fun setBarcodeScanner(){
        codeScanner = CodeScanner(this, scannerView)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = listOf(BarcodeFormat.QR_CODE,
                BarcodeFormat.CODE_128,
                BarcodeFormat.CODE_39,
                BarcodeFormat.RSS_14,
                BarcodeFormat.AZTEC,
                BarcodeFormat.PDF_417,
                BarcodeFormat.MAXICODE,
                BarcodeFormat.EAN_13,
                BarcodeFormat.CODABAR,
                BarcodeFormat.EAN_8,
                BarcodeFormat.CODE_93,
                BarcodeFormat.ITF) // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {

                try {
                    val stringList = it.text.toString().split("&")
                    if(stringList.size>0){
                        var resultIntent : Intent = Intent();
                        resultIntent.putExtra(Constants.scan_code, stringList.get(0))
                        setResult(Activity.RESULT_OK,resultIntent)
                        finish()
                    }

                }catch (e :Exception){
                    e.printStackTrace()

                }
//                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
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
        registerReceiver(myBroadcastReceiver, filter)
    }

    // Unregister scanner status notification
    fun unRegisterScannerStatus() {
        Log.d(LOG_TAG, "unRegisterScannerStatus()")
        val b = Bundle()
        b.putString(Constants.EXTRA_KEY_APPLICATION_NAME,this.packageName)
        b.putString(
            Constants.EXTRA_KEY_NOTIFICATION_TYPE,
            Constants.EXTRA_KEY_VALUE_SCANNER_STATUS
        )
        val i = Intent()
        i.action = ContactsContract.Intents.Insert.ACTION
        i.putExtra(Constants.EXTRA_UNREGISTER_NOTIFICATION, b)
        sendBroadcast(i)
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
                runOnUiThread {

                    try {
                        val stringList = scannedValue.split("&")
                        if(stringList.size>0){
                            var resultIntent : Intent = Intent();
                            resultIntent.putExtra(Constants.scan_code, stringList.get(0))
                            setResult(Activity.RESULT_OK,resultIntent)
                            finish()
                        }

                    }catch (e :Exception){
                        e.printStackTrace()

                    }
//                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
                }
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
        runOnUiThread {

            try {
                val stringList = decodedData.split("&")
                if(stringList.size>0){
                    var resultIntent : Intent = Intent();
                    resultIntent.putExtra(Constants.scan_code, stringList.get(0))
                    setResult(Activity.RESULT_OK,resultIntent)
                    finish()
                }

            }catch (e :Exception){
                e.printStackTrace()

            }
//                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
        }
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
                            this@BarcodeScanActivity,
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


    override fun onResume() {
        super.onResume()
        try {
                codeScanner.startPreview()
            registerReceivers()
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
    }

    override fun onPause() {
        try {
                codeScanner.releaseResources()
            unregisterReceiver(myBroadcastReceiver)
            unRegisterScannerStatus()
        }catch (e :Exception){
            e.printStackTrace()
        }
        super.onPause()
    }
}