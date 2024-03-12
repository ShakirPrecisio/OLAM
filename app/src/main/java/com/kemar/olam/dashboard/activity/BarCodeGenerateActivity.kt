package com.kemar.olam.dashboard.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.Writer
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kemar.olam.R
import com.kemar.olam.utility.Constants
import kotlinx.android.synthetic.main.activity_bar_code_generate.*
import java.util.*

class BarCodeGenerateActivity : AppCompatActivity() {
    lateinit var mContext: Context
    var RESULT_CODE = 103
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bar_code_generate)
        init()
    }

    private fun init(){
        mContext = this
        buttonGenerate?.setOnClickListener{
            buttonGenerate_onClick()
        }
        btnScan?.setOnClickListener{
            acessRuntimPermission()
        }

        btnDigitalSign.setOnClickListener{
            var i = Intent(this, DigitalSignatureActivity::class.java)
            startActivity(i)
        }

        btnGeneratePDF.setOnClickListener{
            var i = Intent(this, PrintActivity::class.java)
            startActivity(i)
        }
    }


    fun acessRuntimPermission() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                var intent = Intent(mContext,BarcodeScanActivity::class.java)
                startActivityForResult(intent,RESULT_CODE)
            }

            override fun onPermissionDenied(deniedPermissions: ArrayList<String>) {

            }
        }
        TedPermission.with(this)
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

      private fun buttonGenerate_onClick() {
        try {
            val productId = editTextProductId.text.toString()
            val hintMap: Hashtable<EncodeHintType, ErrorCorrectionLevel> =
                Hashtable<EncodeHintType, ErrorCorrectionLevel>()
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
            val codeWriter: Writer
            codeWriter = Code128Writer()
            val byteMatrix: BitMatrix =
                codeWriter.encode(productId, BarcodeFormat.CODE_128, 500, 200, hintMap)
            val width = byteMatrix.width
            val height = byteMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (i in 0 until width) {
                for (j in 0 until height) {
                    bitmap.setPixel(i, j, if (byteMatrix[i, j]) Color.BLACK else Color.WHITE)
                }
            }
            imageViewResult.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                txtBarcodeResult.text  =  "Scan Value "+data?.getStringExtra(Constants.scan_code).toString()
            }
        }

    }
}