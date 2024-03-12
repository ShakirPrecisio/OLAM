package com.kemar.olam.dashboard.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.gcacace.signaturepad.views.SignaturePad
import com.kemar.olam.R
import kotlinx.android.synthetic.main.activity_digital_signature.*


class DigitalSignatureActivity : AppCompatActivity() {
    lateinit var mSignaturePad : SignaturePad;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_digital_signature)
        mSignaturePad =  findViewById(R.id.signature_pad);
        btnClr.setOnClickListener{
            mSignaturePad.clear()
        }
        mSignaturePad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() { //Event triggered when the pad is touched
            }

            override fun onSigned() {
                //Event triggered when the pad is signed
            }

            override fun onClear() { //Event triggered when the pad is cleared
            }
        })
    }
}