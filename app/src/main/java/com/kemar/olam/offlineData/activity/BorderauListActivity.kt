package com.kemar.olam.offlineData.activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.kemar.olam.R
import com.kemar.olam.bordereau.adapter.LogsTodaysHistoryAdapter
import com.kemar.olam.bordereau.fragment.BarcodeBordereuDetailsFragment
import com.kemar.olam.bordereau.fragment.LogsListingFragment
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.offlineData.adapter.BorderauListAdapter
import com.kemar.olam.offlineData.requestbody.BorderauRequest
import com.kemar.olam.service.MyService
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.RealmHelper
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_borderau_list.*

class BorderauListActivity : AppCompatActivity() {

    lateinit var realm:Realm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_borderau_list)

        realm = RealmHelper.getRealmInstance()

        var borderauRequest: RealmResults<BorderauRequest> = realm.where(BorderauRequest::class.java).findAllAsync()
        borderauRequest!!.addChangeListener(RealmChangeListener<RealmResults<BorderauRequest?>> { results ->
           // var adapter = BorderauListAdapter(this, borderauRequest, false)
            val linearLayoutManager =
                LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
            recycler_view.setLayoutManager(linearLayoutManager)
            recycler_view.setHasFixedSize(true)
           // recycler_view.adapter=adapter


           /* adapter.onHeaderClick = { modelData, position ->
                val i: Intent = Intent(this, LogOfflineActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.putExtra(Constants.BR,modelData.uniqueId)
                startActivity(i)
                finish()
            }*/
        })

        startsync.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(baseContext, MyService::class.java))
            }else
                startService(Intent(baseContext, MyService::class.java))
        }

    }
}