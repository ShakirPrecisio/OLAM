package com.kemar.olam.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.forestry_management.databaseModel.*
import com.kemar.olam.forestry_management.model.*
import com.kemar.olam.offlineData.response.LogDetail
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.RealmHelper
import com.kemar.olam.utility.SharedPref
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.HashMap

class ForestryService : Service() {

    var manager: NotificationManagerCompat? = null

    lateinit var realm: Realm
    var logsLiting: ArrayList<BodereuLogListing> = arrayListOf()

    var logsWagonLiting: ArrayList<BodereuLogListing> = arrayListOf()
    var i = 0
    var j = 0

    //wagon variable
    var k = 0

    var alreadyExecutedForestry = false
    var alreadyExecutedPistage = false
    var alreadyExecutedTreeFelling = false
    var alreadyExecutedTreePulling = false

    var treeDetailsRealResult: RealmResults<UploadTreeDetailsInventory>? = null
    var pistageResults: RealmResults<UploadPistageInventory>? = null
    var treeFellingResults: RealmResults<UploadTreeFellingInventory>? = null
    var treePullingResults: RealmResults<UploadTreePullingInventory>? = null

    var treeDetailsList = mutableListOf<UploadTreeDetailsInventory>()
    var pistageList = mutableListOf<UploadPistageInventory>()
    var treeFellingList = mutableListOf<UploadTreeFellingInventory>()
    var treePullingList = mutableListOf<UploadTreePullingInventory>()


    var logslist = ArrayList<LogDetail>()

    var mDatabase: DatabaseReference? = null

    lateinit var receiver: BroadcastReceiver
    var builder : NotificationCompat.Builder? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Let it continue running until it is stopped.

        try {
            realm = RealmHelper.getRealmInstance()

            Log.e("onStartCommand", "onStartCommand")

            mDatabase = FirebaseDatabase.getInstance().getReference()

            createNotificationChannel()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val builder: Notification.Builder = Notification.Builder(this, "com.kemar.olam1")
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("GSEZ Offline Sync Service")
                    .setAutoCancel(true)
                val notification: Notification = builder.build()
                startForeground(2, notification)
            } else {
                val builder = NotificationCompat.Builder(this)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText("GSEZ Offline Sync Service")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                val notification: Notification = builder.build()
                startForeground(2, notification)
            }

            val filter = IntentFilter()
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    if (CONNECTIVITY_CHANGE_ACTION == action) {
                        //check internet connection
                        if (!ConnectionHelper.isConnectedOrConnecting(context)) {
                            if (context != null) {
                                var show = false
                                if (ConnectionHelper.lastNoConnectionTs == -1L) { //first time
                                    show = true
                                    ConnectionHelper.lastNoConnectionTs = System.currentTimeMillis()
                                } else {
                                    if (System.currentTimeMillis() - ConnectionHelper.lastNoConnectionTs > 1000) {
                                        show = true
                                        ConnectionHelper.lastNoConnectionTs =
                                            System.currentTimeMillis()
                                    }
                                }
                                if (show && ConnectionHelper.isOnline) {
                                    ConnectionHelper.isOnline = false
                                    Log.e("NETWORK123", "Connection lost")

                                    alreadyExecutedForestry = false
                                    alreadyExecutedPistage = false
                                    alreadyExecutedTreeFelling = false
                                    alreadyExecutedTreePulling = false
                                    this@ForestryService.stopSelf()
                                    //manager.cancelAll();
                                }
                            }
                        } else {

                            builder = NotificationCompat.Builder(this@ForestryService, "CHANNEL_ID").apply {
                                setContentTitle("Offline Forestry Syncing")
                                setContentText("Offline Forestry Syncing In Progress")
                                setSmallIcon(R.mipmap.ic_launcher)
                                setDefaults(NotificationCompat.DEFAULT_ALL)
                                priority = NotificationCompat.PRIORITY_DEFAULT
                            }
                            val PROGRESS_MAX = 100
                            val PROGRESS_CURRENT = 0
                            manager = NotificationManagerCompat.from(this@ForestryService).apply {
                                // Issue the initial notification with zero progress
                                builder?.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, true)
                                builder?.build()?.let { notify(191, it) }

                                // Do the job here that tracks the progress.
                                // Usually, this should be in a
                                // worker thread
                                // To show progress, update PROGRESS_CURRENT and update the notification with:
                                // builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
                                // notificationManager.notify(notificationId, builder.build());

                            }

                            sendMessage(showDialog = true, "Forestry Data Syncing...\nPlease Wait")

                            Log.e("NETWORK123", "Connected")

                            //Forestry Sync
                            if (!alreadyExecutedForestry) {
                                //Tree Details Sync
                                treeDetailsRealResult =
                                    realm.where(UploadTreeDetailsInventory::class.java)
                                        .equalTo("isUpload", false)
                                        .equalTo("isFailed", false)
                                        .findAllAsync()
                                (treeDetailsRealResult as RealmResults<UploadTreeDetailsInventory>?)!!.addChangeListener(
                                    RealmChangeListener<RealmResults<UploadTreeDetailsInventory?>> { results ->
                                        treeDetailsList.clear()
                                        treeDetailsList = realm.copyFromRealm(treeDetailsRealResult)
                                        Log.e("treeDetailsList", " is" + treeDetailsList.size)
                                        if (treeDetailsList.size > 0) {
                                            builder?.setContentTitle("Offline Tree Details Syncing")?.setContentText("Offline Tree Details Syncing In Progress")
                                            builder?.build()?.let { manager?.notify(191, it) }
                                            callingAddTreeDetailsAPI(treeDetailsList[0])

                                        } else {
                                            // When done, update the notification one more time to remove the progress bar
                                            builder?.setContentText("Offline Tree Details Completed")
                                                ?.setProgress(0, 0, false)
                                            builder?.build()?.let { manager?.notify(191, it) }
                                            //Pistage Sync
                                            if (!alreadyExecutedPistage) {
                                                pistageResults =
                                                    realm.where(UploadPistageInventory::class.java)
                                                        .equalTo("isUpload", false)
                                                        .equalTo("isFailed", false).findAllAsync()
                                                (pistageResults as RealmResults<UploadPistageInventory>?)!!.addChangeListener(
                                                    RealmChangeListener<RealmResults<UploadPistageInventory?>> { results ->
                                                        pistageList.clear()
                                                        pistageList =
                                                            realm.copyFromRealm(pistageResults)
                                                        Log.e(
                                                            "pistageList",
                                                            " is" + pistageList.size
                                                        )
                                                        if (pistageList.size > 0) {
                                                            builder?.setContentTitle("Offline Tree Pistage Syncing")?.setContentText("Offline Tree Pistage Syncing In Progress")
                                                            builder?.build()?.let { manager?.notify(191, it) }
                                                            callingAddPistageDetailsAPI(
                                                                pistageList.get(
                                                                    0
                                                                )
                                                            )

                                                        } else {

                                                            // When done, update the notification one more time to remove the progress bar
                                                            builder?.setContentText("Offline Tree Pistage Completed")
                                                                ?.setProgress(0, 0, false)
                                                            builder?.build()?.let { manager?.notify(191, it) }
                                                            //Tree Felling
                                                            if (!alreadyExecutedTreeFelling) {
                                                                treeFellingResults =
                                                                    realm.where(UploadTreeFellingInventory::class.java)
                                                                        .equalTo("isUpload", false)
                                                                        .equalTo("isFailed", false)
                                                                        .findAllAsync()
                                                                (treeFellingResults as RealmResults<UploadTreeFellingInventory>?)!!.addChangeListener(
                                                                    RealmChangeListener<RealmResults<UploadTreeFellingInventory?>> { results ->
                                                                        treeFellingList.clear()
                                                                        treeFellingList =
                                                                            realm.copyFromRealm(
                                                                                treeFellingResults
                                                                            )

                                                                        Log.e(
                                                                            "treeFellingList",
                                                                            " is" + treeFellingList.size
                                                                        )

                                                                        if (treeFellingList.size > 0) {
                                                                            builder?.setContentTitle("Offline Tree Felling Syncing")?.setContentText("Offline Tree Felling Syncing In Progress")
                                                                            builder?.build()?.let { manager?.notify(191, it) }
                                                                            callingAddTreeFellingDetailsAPI(
                                                                                treeFellingList.get(
                                                                                    0
                                                                                )
                                                                            )

                                                                        } else {
                                                                            // When done, update the notification one more time to remove the progress bar
                                                                            builder?.setContentText("Offline Tree Felling Completed")
                                                                                ?.setProgress(0, 0, false)
                                                                            builder?.build()?.let { manager?.notify(191, it) }
                                                                            //Tree Pulling
                                                                            if (!alreadyExecutedTreePulling) {
                                                                                treePullingResults =
                                                                                    realm.where(
                                                                                        UploadTreePullingInventory::class.java
                                                                                    )
                                                                                        .equalTo(
                                                                                            "isUpload",
                                                                                            false
                                                                                        )
                                                                                        .equalTo(
                                                                                            "isFailed",
                                                                                            false
                                                                                        )
                                                                                        .findAllAsync()
                                                                                (treePullingResults as RealmResults<UploadTreePullingInventory>?)!!.addChangeListener(
                                                                                    RealmChangeListener<RealmResults<UploadTreePullingInventory?>> { results ->
                                                                                        treePullingList.clear()
                                                                                        treePullingList =
                                                                                            realm.copyFromRealm(
                                                                                                treePullingResults
                                                                                            )

                                                                                        Log.e(
                                                                                            "treePullingList",
                                                                                            " is" + treePullingList.size
                                                                                        )

                                                                                        if (treePullingList.size > 0) {
                                                                                            builder?.setContentTitle("Offline Tree Pulling Syncing")?.setContentText("Offline Tree Pulling Syncing In Progress")
                                                                                            builder?.build()?.let { manager?.notify(191, it) }
                                                                                            callingAddTreePullingDetailsAPI(
                                                                                                treePullingList.get(
                                                                                                    0
                                                                                                )
                                                                                            )
                                                                                        } else{
                                                                                            // When done, update the notification one more time to remove the progress bar
                                                                                            callTreeInventoryListApi()
                                                                                            builder?.setContentText("Offline Tree Pulling Completed")
                                                                                                ?.setProgress(0, 0, false)
                                                                                            builder?.build()?.let { manager?.notify(191, it) }
                                                                                        }

                                                                                    })

                                                                                alreadyExecutedTreePulling =
                                                                                    true

                                                                            }

                                                                        }

                                                                    })

                                                                alreadyExecutedTreeFelling = true

                                                            }

                                                        }
                                                    })
                                                alreadyExecutedPistage = true
                                            }
                                        }
                                    })
                                alreadyExecutedForestry = true
                            }

                        }
                    }
                }
            }

            if (::receiver.isInitialized)
                registerReceiver(receiver, filter)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = "channel_name"
            val description = "channel_description"
            val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("com.kemar.olam1", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getDateFromDateString(date: String?) : String{

        return date.takeIf { it != null }?.apply { SimpleDateFormat("dd.MM.yyyy")
            .format(SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(this)) } ?: ""

    }

    private fun callTreeInventoryListApi(pageIndex : Int = 0) {
            try {
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = HashMap<String, Int>()
                request["pageIndex"] = pageIndex
                request["pageSize"] = 200
                val call: Call<TreeInventoryListResponse> =
                    apiInterface.getTreeDetailList(request)
                call.enqueue(object :
                    Callback<TreeInventoryListResponse> {
                    override fun onResponse(
                        call: Call<TreeInventoryListResponse>,
                        response: Response<TreeInventoryListResponse>
                    ) {
//                        mUtils.dismissProgressDialog()
                        try {
                            if (response.code() == 200) {
                                if (response != null) {
                                    if (response.body().severity == 200) {
//                                        mUtils.showToast(activity, response.body().message)

                                        if(response.body().pageableTreeDetailRecordResponse.content.isNullOrEmpty().not()){

                                            realm.executeTransactionAsync({ bgRealm ->

//                                                bgRealm.delete(TreeDetailsInventory::class.java)
                                                for(data in response.body().pageableTreeDetailRecordResponse.content!!) {
                                                    if(data != null) {

                                                        var pistageId : Long = 0
                                                        var treeFellingId : Long = 0
                                                        var treePullingId : Long = 0

                                                        if(data.treePistageResponse != null) {

                                                            pistageId = System.currentTimeMillis()

                                                            val pistageInventory =
                                                                PistageInventory()
                                                            pistageInventory.uniqueId =
                                                                pistageId.toString()
                                                            pistageInventory.treeID =
                                                                data.treePistageResponse.treeID
                                                            pistageInventory.dia =
                                                                data.treePistageResponse.dia
                                                            pistageInventory.quality =
                                                                data.treePistageResponse.quality
                                                            pistageInventory.species =
                                                                data.treePistageResponse.species
                                                            pistageInventory.createdDate =
                                                                getDateFromDateString(data.treePistageResponse.createdDate)
                                                            pistageInventory.userId =
                                                                data.treePistageResponse.userId

                                                            bgRealm.copyToRealmOrUpdate(
                                                                pistageInventory
                                                            )
                                                        }

                                                        if(data.treeFellingScanResponse != null) {

                                                            treeFellingId = System.currentTimeMillis()

                                                            val treeFellingInventory = TreeFellingInventory()
                                                            treeFellingInventory.uniqueId = treeFellingId.toString()
                                                            treeFellingInventory.treeID = data.treeFellingScanResponse.treeID
                                                            treeFellingInventory.carnetNumber = data.treeFellingScanResponse.carnetNumber
                                                            treeFellingInventory.fellingID = data.treeFellingScanResponse.fellingID
                                                            treeFellingInventory.treeCutBy = data.treeFellingScanResponse.treeCutBy
                                                            treeFellingInventory.companyName = data.treeFellingScanResponse.companyName
                                                            treeFellingInventory.userId = data.treeFellingScanResponse.userId
                                                            treeFellingInventory.createdDate = getDateFromDateString(data.treeFellingScanResponse.createdDate)


                                                            bgRealm.copyToRealmOrUpdate(
                                                                treeFellingInventory
                                                            )
                                                        }

                                                        if(data.treePullingScanResponse != null) {

                                                            treePullingId = System.currentTimeMillis()

                                                            val treePullingInventory = TreePullingInventory()
                                                            treePullingInventory.uniqueId = treePullingId.toString()
                                                            treePullingInventory.treeID = data.treePullingScanResponse.treeID
                                                            treePullingInventory.userId = data.treePullingScanResponse.userId
                                                            treePullingInventory.length = data.treePullingScanResponse.length
                                                            treePullingInventory.equipmentID = data.treePullingScanResponse.equipmentID
                                                            treePullingInventory.dia = data.treePullingScanResponse.dia
                                                            treePullingInventory.driver = data.treePullingScanResponse.driver
                                                            treePullingInventory.createdDate = getDateFromDateString(data.treePullingScanResponse.createdDate)

                                                            bgRealm.copyToRealmOrUpdate(
                                                                treePullingInventory
                                                            )
                                                        }

                                                        var treeDetailsInventory =
                                                            TreeDetailsInventory()
                                                        treeDetailsInventory.uniqueId =
                                                            System.currentTimeMillis().toString()
                                                        treeDetailsInventory.treeID =
                                                            data.treeId
                                                        treeDetailsInventory.aac =
                                                            data.aac
                                                        treeDetailsInventory.ufg =
                                                            data.ufg
                                                        treeDetailsInventory.concession =
                                                            data.concession
                                                        treeDetailsInventory.dia =
                                                            data.dia
                                                        treeDetailsInventory.location =
                                                            data.location
                                                        treeDetailsInventory.parcelCount =
                                                            data.parcelCount
                                                        treeDetailsInventory.quality =
                                                            data.quality
                                                        treeDetailsInventory.species =
                                                            data.species
                                                        treeDetailsInventory.createdDate =
                                                            data.createdDate
                                                        treeDetailsInventory.userInfo =
                                                            data.userInfo
                                                        treeDetailsInventory.userId =
                                                            data.userId
                                                        treeDetailsInventory.pistageId =
                                                            pistageId
                                                        treeDetailsInventory.treeFellingId =
                                                            treeFellingId
                                                        treeDetailsInventory.treePullingId =
                                                            treePullingId

                                                        bgRealm.copyToRealmOrUpdate(
                                                            treeDetailsInventory
                                                        )

                                                    }
                                                }



                                            }, {
                                                Log.e("Success", "Success")
                                            }) {
                                                Log.e("faile", "failed delte")
                                            }
                                        }

                                        if(!response.body().pageableTreeDetailRecordResponse.last){
                                            callTreeInventoryListApi(pageIndex = pageIndex+1)
                                        } else {
                                            sendMessage(showDialog = false, "Forestry Data Syncing Complete")
                                        }

                                    } else if (response.body()?.severity == 306) {
//                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
//                                        mUtils.showToast(activity, response.body().message)
                                    }
                                }
                            } else if (response.code() == 306) {
//                                mUtils.alertDialgSession(mView.context, activity)
                            } else {
//                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                        } catch (e: Exception) {
                            sendMessage(showDialog = false, "Forestry Data Failed...\nPlease try again")
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        call: Call<TreeInventoryListResponse>,
                        t: Throwable
                    ) {
                        sendMessage(showDialog = false, "Forestry Data Failed...\nPlease try again")
//                        mUtils.showToast(activity, Constants.SERVERTIMEOUT)
//                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                sendMessage(showDialog = false, "Forestry Data Failed...\nPlease try again")
                e.printStackTrace()
            }
    }

    private fun callingAddTreeDetailsAPI(treeDetailsInventory: UploadTreeDetailsInventory) {
        try {
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
            var request = generateAddTreeDetailsRequest(treeDetailsInventory)
            val call: Call<AddBodereuLogListingRes> =
                apiInterface.addTreeDetails(request)


            val gson = Gson()
            val json = gson.toJson(request)
            var myJsonString = json.replace("\"", "");

            call.enqueue(object :
                Callback<AddBodereuLogListingRes> {
                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                override fun onResponse(
                    call: Call<AddBodereuLogListingRes>,
                    response: Response<AddBodereuLogListingRes>
                ) {
                    try {

                        if (response.code() == 200) {
                            if (response != null) {
                                if (response.body().getSeverity() == 200) {

                                    val uniqueID = treeDetailsInventory.uniqueId
                                    realm.executeTransaction { realm ->
                                        val realmResults: UploadTreeDetailsInventory? =
                                            realm.where(UploadTreeDetailsInventory::class.java)
                                                .equalTo("uniqueId", uniqueID).findFirst()
                                        if(realmResults != null) {
                                        realmResults.isUpload = true
                                        //realmResults.headerID = response.body().getBordereauResponse()!!.bordereauHeaderId
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }

                                } else if (response.body()?.getSeverity() == 306) {

                                } else if (response.body()?.getSeverity() == 202 ||
                                    response.body()?.getSeverity() == 201 ||
                                    response.body()?.getSeverity() == 500
                                ) {

                                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                                        .child("Tree Details Entry")
                                        .child(treeDetailsInventory.treeID!!.toString())
                                        .child("Request").setValue(myJsonString)
                                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                                        .child("Tree Details Entry")
                                        .child(treeDetailsInventory.treeID!!.toString())
                                        .child("Response").setValue(response.body())

                                    val uniqueID = treeDetailsInventory.uniqueId
                                    realm.executeTransaction() { realm ->
                                        val realmResults: UploadTreeDetailsInventory? =
                                            realm.where(UploadTreeDetailsInventory::class.java)
                                                .equalTo("uniqueId", uniqueID).findFirst()
                                        if(realmResults != null) {
                                            realmResults.isFailed = true
                                            realmResults.failedReason = "Severity : ${
                                                response.body()?.getSeverity()
                                            } ->" + response.body().getMessage()
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }

                                }
                            }

                        } else {
                            mDatabase!!.child(SharedPref.read(Constants.user_name))
                                .child("Tree Details Entry")
                                .child(treeDetailsInventory.treeID!!.toString()).child("Request")
                                .setValue(myJsonString)
                            mDatabase!!.child(SharedPref.read(Constants.user_name))
                                .child("Tree Details Entry")
                                .child(treeDetailsInventory.treeID!!.toString()).child("Response")
                                .setValue("Code : ${response.code()} -> " + response.body())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                    call: Call<AddBodereuLogListingRes>,
                    t: Throwable
                ) {
                    sendMessage(showDialog = false, "Forestry Data Failed...\n" +
                            "Please try again")
                    t.printStackTrace()
                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                        .child("Tree Details Entry").child(treeDetailsInventory.treeID!!.toString())
                        .child("Request").setValue(myJsonString)
                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                        .child("Tree Details Entry").child(treeDetailsInventory.treeID!!.toString())
                        .child("Response").setValue("OnFailure : ${t.printStackTrace()}")

                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun callingAddPistageDetailsAPI(pistageInventory: UploadPistageInventory) {
        try {
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
            var request = AddPistageDetailsRequest(
                userId = SharedPref.getUserId(Constants.user_id),
                treeID = pistageInventory.treeID.toString(),
                dia = pistageInventory.dia ?: "",
                pocketNumber = pistageInventory.pocketNo ?: 0,
                quality = pistageInventory.quality ?: "",
                species = pistageInventory.species ?: ""
            )
            val call: Call<AddBodereuLogListingRes> =
                apiInterface.addPistageDetails(request)

            val gson = Gson()
            val json = gson.toJson(request)
            var myJsonString = json.replace("\"", "");

            call.enqueue(object :
                Callback<AddBodereuLogListingRes> {
                override fun onResponse(
                    call: Call<AddBodereuLogListingRes>,
                    response: Response<AddBodereuLogListingRes>
                ) {
                    try {

                        if (response.code() == 200) {
                            if (response != null) {

                                if (response.body().getSeverity() == 200) {

                                    val uniqueID = pistageInventory.uniqueId
                                    realm.executeTransaction { realm ->
                                        val realmResults: TreeDetailsInventory? =
                                            realm.where(TreeDetailsInventory::class.java)
                                                .equalTo("treeID", pistageInventory.treeID)
                                                .findFirst()
                                        realmResults?.pistageId = uniqueID?.toLong()
                                        if (realmResults != null) {
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }
                                    realm.executeTransaction { realm ->
                                        val realmResults: UploadTreeDetailsInventory? =
                                            realm.where(UploadTreeDetailsInventory::class.java)
                                                .equalTo("treeID", pistageInventory.treeID)
                                                .findFirst()
                                        realmResults?.pistageId = uniqueID?.toLong()
                                        if (realmResults != null) {
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }
                                    realm.executeTransaction { realm ->
                                        val realmResults: UploadPistageInventory? =
                                            realm.where(UploadPistageInventory::class.java)
                                                .equalTo("uniqueId", uniqueID).findFirst()
                                        if(realmResults != null) {
                                            realmResults.isUpload = true
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }

                                } else if (response.body()?.getSeverity() == 306) {

                                } else if (response.body()?.getSeverity() == 202 ||
                                    response.body()?.getSeverity() == 201 ||
                                    response.body()?.getSeverity() == 500
                                ) {

                                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                                        .child("Pistage Entry")
                                        .child(pistageInventory.treeID!!.toString())
                                        .child("Request").setValue(myJsonString)
                                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                                        .child("Pistage Entry")
                                        .child(pistageInventory.treeID!!.toString())
                                        .child("Response").setValue(response.body())

                                    val uniqueID = pistageInventory.uniqueId
                                    realm.executeTransaction() { realm ->
                                        val realmResults: UploadPistageInventory? =
                                            realm.where(UploadPistageInventory::class.java)
                                                .equalTo("uniqueId", uniqueID).findFirst()
                                        if(realmResults != null) {
                                        realmResults.isFailed = true
                                        realmResults.failedReason = "Severity : ${
                                            response.body()?.getSeverity()
                                        } ->" + response.body().getMessage()
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }

                                }
                            }

                        } else {

                            mDatabase!!.child(SharedPref.read(Constants.user_name))
                                .child("Pistage Entry").child(pistageInventory.treeID!!.toString())
                                .child("Request").setValue(myJsonString)
                            mDatabase!!.child(SharedPref.read(Constants.user_name))
                                .child("Pistage Entry").child(pistageInventory.treeID!!.toString())
                                .child("Response")
                                .setValue("Code : ${response.code()} -> " + response.body())

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                    call: Call<AddBodereuLogListingRes>,
                    t: Throwable
                ) {
                    sendMessage(showDialog = false, "Forestry Data Failed...\n" +
                            "Please try again")
                    t.printStackTrace()
                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Pistage Entry")
                        .child(pistageInventory.treeID!!.toString()).child("Request")
                        .setValue(myJsonString)
                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Pistage Entry")
                        .child(pistageInventory.treeID!!.toString()).child("Response")
                        .setValue("OnFailure : ${t.printStackTrace()}")

                }
            })
        } catch (e: Exception) {
            sendMessage(showDialog = false, "Forestry Data Failed...\n" +
                    "Please try again")
            e.printStackTrace()
        }

    }

    private fun callingAddTreeFellingDetailsAPI(treeFellingInventory: UploadTreeFellingInventory) {
        try {
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
            var request = AddTreeFellingScanDetailsRequest(
                userId = SharedPref.getUserId(Constants.user_id),
                carnetNumber = treeFellingInventory.carnetNumber ?: "",
                companyName = treeFellingInventory.companyName ?: "",
                fellingID = treeFellingInventory.fellingID?.toString() ?: "",
                treeCutBy = treeFellingInventory.treeCutBy ?: "",
                treeID = treeFellingInventory.treeID.toString()
            )
            val call: Call<AddBodereuLogListingRes> =
                apiInterface.addTreeFellingScanDetails(request)

            val gson = Gson()
            val json = gson.toJson(request)
            var myJsonString = json.replace("\"", "");

            call.enqueue(object :
                Callback<AddBodereuLogListingRes> {
                override fun onResponse(
                    call: Call<AddBodereuLogListingRes>,
                    response: Response<AddBodereuLogListingRes>
                ) {
                    try {

                        if (response.code() == 200) {
                            if (response != null) {

                                if (response.body().getSeverity() == 200) {

                                    val uniqueID = treeFellingInventory.uniqueId
                                    realm.executeTransaction { realm ->
                                        val realmResults: TreeDetailsInventory? =
                                            realm.where(TreeDetailsInventory::class.java)
                                                .equalTo("treeID", treeFellingInventory.treeID)
                                                .findFirst()
                                        if(realmResults != null) {
                                            realmResults.treeFellingId = uniqueID?.toLong()
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }
                                    realm.executeTransaction { realm ->
                                        val realmResults: UploadTreeDetailsInventory? =
                                            realm.where(UploadTreeDetailsInventory::class.java)
                                                .equalTo("treeID", treeFellingInventory.treeID)
                                                .findFirst()
                                        if(realmResults != null) {
                                            realmResults.treeFellingId = uniqueID?.toLong()
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }
                                    realm.executeTransaction { realm ->
                                        val realmResults: UploadTreeFellingInventory? =
                                            realm.where(UploadTreeFellingInventory::class.java)
                                                .equalTo("uniqueId", uniqueID).findFirst()
                                        if(realmResults != null) {
                                            realmResults.isUpload = true
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }

                                } else if (response.body()?.getSeverity() == 306) {

                                } else if (response.body()?.getSeverity() == 202 ||
                                    response.body()?.getSeverity() == 201 ||
                                    response.body()?.getSeverity() == 500
                                ) {

                                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                                        .child("Tree Felling Entry")
                                        .child(treeFellingInventory.treeID!!.toString())
                                        .child("Request").setValue(myJsonString)
                                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                                        .child("Tree Felling Entry")
                                        .child(treeFellingInventory.treeID!!.toString())
                                        .child("Response").setValue(response.body())

                                    val uniqueID = treeFellingInventory.uniqueId
                                    realm.executeTransaction() { realm ->
                                        val realmResults: UploadTreeFellingInventory? =
                                            realm.where(UploadTreeFellingInventory::class.java)
                                                .equalTo("uniqueId", uniqueID).findFirst()
                                        if(realmResults != null) {
                                            realmResults!!.isFailed = true
                                            realmResults.failedReason = "Severity : ${
                                                response.body()?.getSeverity()
                                            } ->" + response.body().getMessage()
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }

                                }
                            }

                        } else {

                            mDatabase!!.child(SharedPref.read(Constants.user_name))
                                .child("Tree Felling Entry")
                                .child(treeFellingInventory.treeID!!.toString()).child("Request")
                                .setValue(myJsonString)
                            mDatabase!!.child(SharedPref.read(Constants.user_name))
                                .child("Tree Felling Entry")
                                .child(treeFellingInventory.treeID!!.toString()).child("Response")
                                .setValue("Code : ${response.code()} -> " + response.body())

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                    call: Call<AddBodereuLogListingRes>,
                    t: Throwable
                ) {
                    t.printStackTrace()
                    sendMessage(showDialog = false, "Forestry Data Failed...\n" +
                            "Please try again")
                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                        .child("Tree Felling Entry").child(treeFellingInventory.treeID!!.toString())
                        .child("Request").setValue(myJsonString)
                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                        .child("Tree Felling Entry").child(treeFellingInventory.treeID!!.toString())
                        .child("Response").setValue("OnFailure : ${t.printStackTrace()}")

                }
            })
        } catch (e: Exception) {
            sendMessage(showDialog = false, "Forestry Data Failed...\n" +
                    "Please try again")
            e.printStackTrace()
        }

    }

    private fun callingAddTreePullingDetailsAPI(treePullingInventory: UploadTreePullingInventory) {
        try {
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
            var request = AddTreePullingScanDetailsRequest(
                userId = SharedPref.getUserId(Constants.user_id),
                driver = treePullingInventory.driver ?: "",
                dia = treePullingInventory.dia ?: "",
                equipmentID = treePullingInventory.equipmentID ?: "",
                length = treePullingInventory.length ?: "",
                treeID = treePullingInventory.treeID.toString()
            )
            val call: Call<AddBodereuLogListingRes> =
                apiInterface.addTreePullingScanDetails(request)

            val gson = Gson()
            val json = gson.toJson(request)
            var myJsonString = json.replace("\"", "");

            call.enqueue(object :
                Callback<AddBodereuLogListingRes> {
                override fun onResponse(
                    call: Call<AddBodereuLogListingRes>,
                    response: Response<AddBodereuLogListingRes>
                ) {
                    try {

                        if (response.code() == 200) {
                            if (response != null) {

                                if (response.body().getSeverity() == 200) {

                                    val uniqueID = treePullingInventory.uniqueId
                                    realm.executeTransaction { realm ->
                                        val realmResults: TreeDetailsInventory? =
                                            realm.where(TreeDetailsInventory::class.java)
                                                .equalTo("treeID", treePullingInventory.treeID)
                                                .findFirst()
                                        if(realmResults != null) {
                                            realmResults.treePullingId = uniqueID?.toLong()
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }
                                    realm.executeTransaction { realm ->
                                        val realmResults: UploadTreeDetailsInventory? =
                                            realm.where(UploadTreeDetailsInventory::class.java)
                                                .equalTo("treeID", treePullingInventory.treeID)
                                                .findFirst()
                                        if(realmResults != null) {
                                            realmResults.treePullingId = uniqueID?.toLong()
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }
                                    realm.executeTransaction { realm ->
                                        val realmResults: UploadTreePullingInventory? =
                                            realm.where(UploadTreePullingInventory::class.java)
                                                .equalTo("uniqueId", uniqueID).findFirst()
                                        if(realmResults != null) {
                                        realmResults.isUpload = true
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }

                                } else if (response.body()?.getSeverity() == 306) {

                                } else if (response.body()?.getSeverity() == 202 ||
                                    response.body()?.getSeverity() == 201 ||
                                    response.body()?.getSeverity() == 500
                                ) {

                                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                                        .child("Tree Pulling Entry")
                                        .child(treePullingInventory.treeID!!.toString())
                                        .child("Request").setValue(myJsonString)
                                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                                        .child("Tree Pulling Entry")
                                        .child(treePullingInventory.treeID!!.toString())
                                        .child("Response").setValue(response.body())

                                    val uniqueID = treePullingInventory.uniqueId
                                    realm.executeTransaction() { realm ->
                                        val realmResults: UploadTreePullingInventory? =
                                            realm.where(UploadTreePullingInventory::class.java)
                                                .equalTo("uniqueId", uniqueID).findFirst()
                                        if(realmResults != null) {
                                            realmResults.isFailed = true
                                            realmResults.failedReason = "Severity : ${
                                                response.body()?.getSeverity()
                                            } ->" + response.body().getMessage()
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }

                                }
                            }

                        } else {

                            mDatabase!!.child(SharedPref.read(Constants.user_name))
                                .child("Tree Pulling Entry")
                                .child(treePullingInventory.treeID!!.toString()).child("Request")
                                .setValue(myJsonString)
                            mDatabase!!.child(SharedPref.read(Constants.user_name))
                                .child("Tree Pulling Entry")
                                .child(treePullingInventory.treeID!!.toString()).child("Response")
                                .setValue("Code : ${response.code()} -> " + response.body())

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                    call: Call<AddBodereuLogListingRes>,
                    t: Throwable
                ) {
                    t.printStackTrace()
                    sendMessage(showDialog = false, "Forestry Data Failed...\n" +
                            "Please try again")
                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                        .child("Tree Pulling Entry").child(treePullingInventory.treeID!!.toString())
                        .child("Request").setValue(myJsonString)
                    mDatabase!!.child(SharedPref.read(Constants.user_name))
                        .child("Tree Pulling Entry").child(treePullingInventory.treeID!!.toString())
                        .child("Response").setValue("OnFailure : ${t.printStackTrace()}")

                }
            })
        } catch (e: Exception) {
            sendMessage(showDialog = false, "Forestry Data Failed...\n" +
                    "Please try again")
            e.printStackTrace()
        }

    }

    fun generateAddTreeDetailsRequest(borderauRequest: UploadTreeDetailsInventory): AddTreeDetailsRequest {

        return AddTreeDetailsRequest(
            dia = borderauRequest.dia ?: "",
            treeCbm = borderauRequest.treeCbm?.toDouble() ?: 0.0,
            parcelCount = (borderauRequest.parcelCount ?: ""),
            quality = borderauRequest.quality ?: "",
            species = borderauRequest.species ?: "",
            userId = SharedPref.getUserId(Constants.user_id).toString(),
            concession = borderauRequest.concession ?: "",
            ufg = borderauRequest.ufg ?: "",
            aac = borderauRequest.aac ?: "",
            treeID = borderauRequest.treeID!!.toLong(),
            userInfo = borderauRequest.userInfo ?: "",
            location = borderauRequest.location ?: ""
        )

    }

    private fun sendMessage(showDialog : Boolean, dialogText : String) {
        val intent = Intent("serviceForestryEvent")
        // add data
        intent.putExtra("showDialog", showDialog)
        intent.putExtra("dialogText", dialogText)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        if(!showDialog){
            ConnectionHelper.isOnline = true
            stopForeground(true)
            stopSelf()
        }
    }

    override fun onDestroy() {
        if (::receiver.isInitialized)
            unregisterReceiver(receiver)
        super.onDestroy()
//        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show()
    }

    companion object {
        const val CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
    }


}