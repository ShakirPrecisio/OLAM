package com.kemar.olam.offlineData.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kemar.olam.R
import com.kemar.olam.utility.Constants

class LogOfflineActivity : AppCompatActivity() {

    var uniquie=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_offline)

        if(intent != null||intent.getExtras() != null) {
            uniquie = intent.getStringExtra(Constants.BR)!!


        }
    }
}