package com.kemar.olam.offlineData.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.kemar.olam.R
import com.kemar.olam.dashboard.activity.DashboardActivity
import kotlinx.android.synthetic.main.activity_offline_main.*

class OfflineMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_offline_main)


        card_Borderau.setOnClickListener {
            val i: Intent = Intent(this, BorderauListActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i)
            finish()
        }

        card_loading_wagons.setOnClickListener {
            val i: Intent = Intent(this, BorderauListActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i)
            finish()
        }
    }
}