
package com.kemar.olam.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.DEFAULT_ALL
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.AddBodereuReq
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.dashboard.activity.DashboardActivity
import com.kemar.olam.loading_wagons.model.request.AddLoadingBordereueHeaderReq
import com.kemar.olam.loading_wagons.model.request.GeneratePDFReq
import com.kemar.olam.loading_wagons.model.request.LoadingRequest
import com.kemar.olam.loading_wagons.model.request.WagonLogRequest
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.offlineData.requestbody.BorderauHeaderLogRequest
import com.kemar.olam.offlineData.requestbody.BorderauRequest
import com.kemar.olam.offlineData.requestbody.LoadingHeaderLogRequest
import com.kemar.olam.offlineData.response.LogDetail
import com.kemar.olam.offlineData.response.LogOfflineResponse
import com.kemar.olam.offlineData.response.MoreSupplierInfo
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.*
import com.lp.lpwms.ui.offline.response.*
import io.realm.Case
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyService : Service() {
    var manager: NotificationManagerCompat? = null

    lateinit var realm: Realm
    var logsLiting: ArrayList<BodereuLogListing> = arrayListOf()

    var logsWagonLiting: ArrayList<BodereuLogListing> = arrayListOf()
    var i=0
    var j=0

    //wagon variable
    var k=0

    var alreadyExecuted=false
    var alreadyExecutedLoading=false
    var alreadyExecutedDeclartion=false
    var isCalled=false
    var isLogDataloaded=false

    var isGetLogsApiRunning = false

    var BorderauRealResult: RealmResults<BorderauRequest>? = null
    var loadingRequestResults: RealmResults<LoadingRequest>? = null
    var loadingRequestDeclarationResults: RealmResults<LoadingRequest>? = null
    var addLoadingBordereueHeaderReq: RealmResults<AddLoadingBordereueHeaderReq>? = null

    //var borderauRequestList: List<BorderauRequest> = ArrayList<BorderauRequest>()
    var borderauRequestList = mutableListOf<BorderauRequest>()
    var loadingRequestList = mutableListOf<LoadingRequest>()
    var loadingRequestDeclarationList = mutableListOf<LoadingRequest>()


    var logslist= ArrayList<LogDetail>()

    var  mDatabase: DatabaseReference? = null

    val pageSize = 400


    var aacList= ArrayList<Aac>()
    var customerDatumlist= ArrayList<CustomerDatum>()
    var fsclist= ArrayList<FscMasterDatum>()
    var leauChargementDatumlist= ArrayList<LeauChargementDatum>()
    var morelist= ArrayList<MoreSupplierInfo>()
    var originMasterlist= ArrayList<OriginMaster>()
    var qualityDatumlist= ArrayList<QualityDatum>()
    var vehicleDatum= ArrayList<VehicleDatum>()
    var specieslist= ArrayList<Species>()
    var supportlist= ArrayList<SupplierDatum>()
    var transportDatumlist= ArrayList<TransporterDatum>()
    var transportModeDatumlist= ArrayList<TransportModeDatum>()

    lateinit var receiver: BroadcastReceiver

    var builder : NotificationCompat.Builder? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Let it continue running until it is stopped.

        realm = RealmHelper.getRealmInstance()

        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val builder: Notification.Builder = Notification.Builder(this, "CHANNEL_ID")
                .setContentTitle(getString(R.string.app_name))
                .setContentText("GSEZ Offline Sync Service")
                .setAutoCancel(true)
            val notification: Notification = builder.build()
            startForeground(1, notification)
        } else {
            val builder = NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("GSEZ Offline Sync Service")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
            val notification: Notification = builder.build()
            startForeground(1, notification)
        }

        if (intent?.action != null && intent.action.equals(Constants.RECALL)) {

            builder = NotificationCompat.Builder(this, "CHANNEL_ID").apply {
                setContentTitle("Offline Log Syncing")
                setContentText("Offline Log Syncing In Progress")
                setSmallIcon(R.mipmap.ic_launcher)
                setDefaults(DEFAULT_ALL)
                priority = NotificationCompat.PRIORITY_DEFAULT
                setFullScreenIntent(PendingIntent.getActivity(this@MyService, 0, Intent(), PendingIntent.FLAG_UPDATE_CURRENT), true)
            }

            manager = NotificationManagerCompat.from(this).apply {
                // Issue the initial notification with zero progress
                builder?.setProgress(100, 0, true)
                builder?.build()?.let { notify(190, it) }

                // Do the job here that tracks the progress.
                // Usually, this should be in a
                // worker thread
                // To show progress, update PROGRESS_CURRENT and update the notification with:
                // builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
                // notificationManager.notify(notificationId, builder.build());

            }
            builder?.setContentTitle("Offline Log Syncing")?.setContentText("Offline Log Syncing In Progress")
            builder?.build()?.let { manager?.notify(190, it) }
            if(!isGetLogsApiRunning) {
                sendMessage(showDialog = true, "Logs Syncing...\nPlease Wait")
                isGetLogsApiRunning = true
                getAllLogDataForLoadingWagons(pageCount = 0) // here you invoke the service method
            }
        } else {

            try{

                Log.e("onStartCommand", "onStartCommand")

                mDatabase = FirebaseDatabase.getInstance().getReference()

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
                                            ConnectionHelper.lastNoConnectionTs = System.currentTimeMillis()
                                        }
                                    }
                                    if (show && ConnectionHelper.isOnline) {
                                        ConnectionHelper.isOnline = false
                                        Log.e("NETWORK123", "Connection lost")

                                        alreadyExecuted = false
                                        alreadyExecutedLoading=false
                                        alreadyExecutedDeclartion=false
                                        isCalled=false
                                        //manager.cancelAll();
                                    }
                                }
                            } else {
                                Log.e("NETWORK123", "Connected")
                                builder = NotificationCompat.Builder(this@MyService, "CHANNEL_ID").apply {
                                    setContentTitle("Offline Log Syncing")
                                    setContentText("Offline Log Syncing In Progress")
                                    setSmallIcon(R.mipmap.ic_launcher)
                                    setDefaults(DEFAULT_ALL)
                                    priority = NotificationCompat.PRIORITY_DEFAULT
                                }
                                manager = NotificationManagerCompat.from(this@MyService).apply {
                                    // Issue the initial notification with zero progress
                                    builder?.setProgress(100, 0, true)
                                    builder?.build()?.let { notify(190, it) }

                                    // Do the job here that tracks the progress.
                                    // Usually, this should be in a
                                    // worker thread
                                    // To show progress, update PROGRESS_CURRENT and update the notification with:
                                    // builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
                                    // notificationManager.notify(notificationId, builder.build());

                                }

                                sendMessage(showDialog = true, "Data Syncing...\nPlease Wait")

                                if(!alreadyExecuted) {
                                    BorderauRealResult = realm.where(BorderauRequest::class.java).greaterThan("logCount", 0).equalTo("isUpload", false).equalTo("isFailed", false).findAllAsync()
                                    (BorderauRealResult as RealmResults<BorderauRequest>?)!!.addChangeListener(RealmChangeListener<RealmResults<BorderauRequest?>> { results ->
                                        borderauRequestList.clear()
                                        borderauRequestList = realm.copyFromRealm(BorderauRealResult)
                                        Log.e("borderauRequestList", " is" + borderauRequestList.size)
                                        if (borderauRequestList.size > 0) {
                                            builder?.setContentTitle("Offline Barcode Syncing")?.setContentText("Offline Barcode Syncing In Progress")
                                            builder?.build()?.let { manager?.notify(190, it) }
                                            callingAddHeaderLogsBordereauAPI(borderauRequestList.get(0)!!)
                                        } else {
                                            // When done, update the notification one more time to remove the progress bar
                                            builder?.setContentText("Offline Barcode Syncing Completed")
                                                ?.setProgress(0, 0, false)
                                            builder?.build()?.let { manager?.notify(190, it) }
                                            // uploadLogs()5
                                            if(!alreadyExecutedLoading) {
                                                loadingRequestResults = realm.where(LoadingRequest::class.java).greaterThan("logCount", 0).equalTo("isUpload", false).equalTo("isFailed", false).findAllAsync()
                                                (loadingRequestResults as RealmResults<LoadingRequest>?)!!.addChangeListener(RealmChangeListener<RealmResults<LoadingRequest?>> { results ->
                                                    loadingRequestList.clear()
                                                    loadingRequestList = realm.copyFromRealm(loadingRequestResults)
                                                    Log.e("loadingRequestList", " is" + loadingRequestList.size)
                                                    if (loadingRequestList.size > 0) {
                                                        builder?.setContentTitle("Offline Loading Wagon Syncing")?.setContentText("Offline Loading Wagon Syncing In Progress")
                                                        builder?.build()?.let { manager?.notify(190, it) }
                                                        callingLoadingWagonAddHeaderLogsAPI(loadingRequestList.get(0))

                                                    } else {
                                                        // When done, update the notification one more time to remove the progress bar
                                                        builder?.setContentText("Offline Loading Wagon Syncing Completed")
                                                            ?.setProgress(0, 0, false)
                                                        builder?.build()?.let { manager?.notify(190, it) }
                                                        //uploadWagonLogs()
                                                        if(!alreadyExecutedDeclartion) {
                                                            loadingRequestDeclarationResults = realm.where(LoadingRequest::class.java)
                                                                .equalTo("isLogUpload", true)
                                                                .equalTo("isDeclare", true)
                                                                .equalTo("isDeclareDone", false)
                                                                .equalTo("isDeclareDoneFail",false)
                                                                .greaterThan("headerID", 0)
                                                                .findAllAsync()
                                                            (loadingRequestDeclarationResults as RealmResults<LoadingRequest>?)!!.addChangeListener(RealmChangeListener<RealmResults<LoadingRequest?>> { results ->
                                                                loadingRequestDeclarationList.clear()
                                                                loadingRequestDeclarationList = realm.copyFromRealm(loadingRequestDeclarationResults)

                                                                Log.e("loadingRequestList", " is" + loadingRequestDeclarationList.size)

                                                                if (loadingRequestDeclarationList.size > 0) {
                                                                    builder?.setContentTitle("Offline Declaration Syncing")?.setContentText("Offline Declaration Syncing In Progress")
                                                                    builder?.build()?.let { manager?.notify(190, it) }
                                                                    callingDeclarePostAPI(loadingRequestDeclarationList.get(0))
                                                                } else{
                                                                    // When done, update the notification one more time to remove the progress bar
                                                                    builder?.setContentText("Offline Declaration Syncing Completed")
                                                                        ?.setProgress(0, 0, false)
                                                                    builder?.build()?.let { manager?.notify(190, it) }
                                                                }

                                                            })

                                                            alreadyExecutedDeclartion = true

                                                        }

                                                    }
                                                })
                                                alreadyExecutedLoading = true
                                            }
                                        }
                                    })
                                    alreadyExecuted = true
                                }
                                ConnectionHelper.isOnline = true
                                sendMessage(showDialog = false, "Data Syncing...\nPlease Wait")
                                stopForeground(true)
                                stopSelf()
                            }
                        }
                    }
                }

                if(::receiver.isInitialized)
                    registerReceiver(receiver, filter)
            }catch (e:Exception){
                e.printStackTrace()
            }
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
            val channel = NotificationChannel("CHANNEL_ID", name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getAllLogDataForLoadingWagons(pageCount: Int) {
        try {
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
            val commonRequest= CommonRequest()
            commonRequest.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
            commonRequest.size = pageSize
            commonRequest.page = pageCount
            val call: Call<LogOfflineResponse> = apiInterface.getAllLogDataForLoadingWagons(commonRequest)
            call.enqueue(object :
                Callback<LogOfflineResponse> {
                override fun onResponse(
                    call: Call<LogOfflineResponse>,
                    response: Response<LogOfflineResponse>
                ) {
                    try {
                        if (response.code() == 200) {

                            Log.d("ResponseRaw", Gson().toJson(response.raw()))

                            var offlineResponse = response.body()
                            if (offlineResponse != null) {
                                logslist.clear()

                                realm.executeTransactionAsync({ bgRealm ->

                                    if(pageCount == 0) {
                                        bgRealm.delete(LogDetail::class.java)
                                        Log.e("Success", "Success delete")
                                    }
                                }, {

                                    realm.executeTransactionAsync({ bgRealm ->

                                        for (i in offlineResponse.logDetails!!.indices!!) {
                                            offlineResponse.logDetails!!.get(i).id = i + (pageCount * pageSize)

                                            logslist.add(offlineResponse.logDetails!!.get(i))
                                        }
                                        Log.d("logslistcount", logslist.toString())
                                        bgRealm.copyToRealmOrUpdate(logslist)

                                    }, {
                                        Log.e("Success", "Success logs")
                                    }) {
                                        Log.e("faile", "failed logs")
                                    }
                                }) {
                                    Log.e("faile", "failed delte")
                                }

                                if(offlineResponse.logDetails!!.isNotEmpty()){
                                    isGetLogsApiRunning = false
                                    getAllLogDataForLoadingWagons(
                                        pageCount = (pageCount+1)
                                    )
                                } else {
                                    // When done, update the notification one more time to remove the progress bar
                                    builder?.setContentText("Offline Log Syncing Completed")
                                        ?.setProgress(0, 0, false)
                                    builder?.build()?.let { manager?.notify(190, it) }
                                    sendMessage(showDialog = false, "Logs Syncing...\nPlease Wait")
                                    stopForeground(true)
                                    stopSelf()
                                }

                            }

                        } else if (response.code() == 306) {
                            isGetLogsApiRunning = false
                            builder?.setContentText("Syncing Failed - Please Logout from app")
                                ?.setProgress(0, 0, false)
                            builder?.build()?.let { manager?.notify(190, it) }
                            sendMessage(showDialog = false, "Logs Syncing...\nPlease Wait")
                        } else {
                            isGetLogsApiRunning = false
                            mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Log Syncing").child(System.currentTimeMillis().toString()).child("ResponseRawData").setValue(Gson().toJson(response.raw()))
                            mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Log Syncing").child(System.currentTimeMillis().toString()).child("Request").setValue(Gson().toJson(commonRequest))
                            mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Log Syncing").child(System.currentTimeMillis().toString()).child("Response").setValue(Gson().toJson(response.body()))

                            builder?.setContentText("Offline Log Syncing Failed")
                                ?.setProgress(0, 0, false)
                            builder?.build()?.let { manager?.notify(190, it) }
                            sendMessage(showDialog = false, "Logs Syncing...\nPlease Wait")
                        }
                    } catch (e: Exception) {
                        isGetLogsApiRunning = false
                        builder?.setContentText("Offline Log Syncing Failed")
                            ?.setProgress(0, 0, false)
                        builder?.build()?.let { manager?.notify(190, it) }
                        e.printStackTrace()
                        sendMessage(showDialog = false, "Logs Syncing...\nPlease Wait")
                    }
                }

                override fun onFailure(
                    call: Call<LogOfflineResponse>,
                    t: Throwable
                ) {
                    isGetLogsApiRunning = false
                    try {
                        mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Log Syncing")
                            .child(System.currentTimeMillis().toString()).child("Request")
                            .setValue(Gson().toJson(commonRequest))
                        mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Log Syncing")
                            .child(System.currentTimeMillis().toString()).child("Response")
                            .setValue(Gson().toJson("onFailure -> ${t.printStackTrace()}"))
                    }catch (e:Exception){
                        e.printStackTrace()
                    }
                    sendMessage(showDialog = false, "Logs Syncing...\nPlease Wait")
                        builder?.setContentText("Offline Log Syncing Failed")
                        ?.setProgress(0, 0, false)
                    builder?.build()?.let { manager?.notify(190, it) }
                }
            })
        } catch (e: Exception) {
            sendMessage(showDialog = false, "Logs Syncing...\nPlease Wait")
            isGetLogsApiRunning = false
            builder?.setContentText("Offline Log Syncing Failed")
                ?.setProgress(0, 0, false)
            builder?.build()?.let { manager?.notify(190, it) }
            e.printStackTrace()
            mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Log Syncing").child(System.currentTimeMillis().toString()).child("Response").setValue(Gson().toJson("onCatch -> ${e.printStackTrace()}"))

        }
    }

    private fun getOfflineMasterData() {

            try {
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val commonRequest=CommonRequest()
                val call: Call<OfflineResponse> = apiInterface.getOfflineMasterData(commonRequest)
                call.enqueue(object :
                    Callback<OfflineResponse> {
                    override fun onResponse(
                        call: Call<OfflineResponse>,
                        response: Response<OfflineResponse>
                    ) {
                        try {
                            if (response.code() == 200) {

                                var offlineResponse = response.body()
                                if (offlineResponse != null) {
                                    originMasterlist.clear()
                                    supportlist.clear()
                                    transportModeDatumlist.clear()
                                    aacList.clear()

                                    customerDatumlist.clear()
                                    fsclist.clear()
                                    leauChargementDatumlist.clear()
                                    qualityDatumlist.clear()
                                    specieslist.clear()
                                    vehicleDatum.clear()
                                    transportDatumlist.clear()
                                    morelist.clear()


                                    realm.executeTransactionAsync({ bgRealm ->


                                        for (i in offlineResponse.originMaster!!.indices!!) {
                                            offlineResponse.originMaster!!.get(i).id = i

                                            originMasterlist.add(offlineResponse.originMaster!!.get(i))
                                        }

                                        bgRealm.copyToRealmOrUpdate(originMasterlist)


                                        for (i in offlineResponse.supplierData!!.indices!!) {
                                            offlineResponse.supplierData!!.get(i).id = i
                                            supportlist.add(offlineResponse.supplierData!!.get(i))
                                        }


                                        bgRealm.copyToRealmOrUpdate(supportlist)


                                        for (i in offlineResponse.transportModeData!!.indices!!) {
                                            offlineResponse.transportModeData!!.get(i).id = i
                                            transportModeDatumlist.add(offlineResponse.transportModeData!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(transportModeDatumlist)


                                        for (i in offlineResponse.aacList!!.indices!!) {
                                            offlineResponse.aacList!!.get(i).id = i
                                            aacList.add(offlineResponse.aacList!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(aacList)


                                        for (i in offlineResponse.customerData!!.indices!!) {
                                            offlineResponse.customerData!!.get(i).id = i
                                            customerDatumlist.add(offlineResponse.customerData!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(customerDatumlist)


                                        for (i in offlineResponse.fscMasterData!!.indices!!) {
                                            offlineResponse.fscMasterData!!.get(i).id = i
                                            fsclist.add(offlineResponse.fscMasterData!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(fsclist)

                                        for (i in offlineResponse.leauChargementData!!.indices!!) {
                                            offlineResponse.leauChargementData!!.get(i).id = i
                                            leauChargementDatumlist.add(offlineResponse.leauChargementData!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(leauChargementDatumlist)

                                        for (i in offlineResponse.qualityData!!.indices!!) {
                                            offlineResponse.qualityData!!.get(i).id = i
                                            qualityDatumlist.add(offlineResponse.qualityData!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(qualityDatumlist)

                                        for (i in offlineResponse.species!!.indices) {
                                            offlineResponse.species!!.get(i).id = i
                                            specieslist.add(offlineResponse.species!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(specieslist)


                                        for (i in offlineResponse.vehicleData!!.indices!!) {
                                            offlineResponse.vehicleData!!.get(i).id = i
                                            vehicleDatum.add(offlineResponse.vehicleData!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(vehicleDatum)


                                        for (i in offlineResponse.transporterData!!.indices!!) {
                                            offlineResponse.transporterData!!.get(i).id = i
                                            transportDatumlist.add(offlineResponse.transporterData!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(transportDatumlist)


                                        for (i in offlineResponse.moreSupplierInfo!!.indices!!) {
                                            offlineResponse.moreSupplierInfo!!.get(i).id = i
                                            morelist.add(offlineResponse.moreSupplierInfo!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(morelist)

                                    }, {
                                        Log.e("Success", "Success")
                                    }) {
                                        Log.e("faile", "faile")
                                    }
                                }

                            } else if (response.code() == 306) {
                                // mUtils.alertDialgSession(this@DashboardActivity, this@DashboardActivity)
                            } else {
                                // mUtils.showToast(this@DashboardActivity, Constants.SOMETHINGWENTWRONG)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        call: Call<OfflineResponse>,
                        t: Throwable
                    ) {
                        //  mUtils.showToast(this@DashboardActivity, Constants.SERVERTIMEOUT)
                        // mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }

    }


    fun generateAddBodereuRequest(borderauRequest: BorderauRequest): AddBodereuReq {

        var request : AddBodereuReq =   AddBodereuReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id).toInt())
        request.setSupplier(borderauRequest.forestId)
        request.setBordereauHeaderId(borderauRequest.headerID)
        request.setSupplierLocation(SharedPref.getUserId(Constants.user_location_id).toInt())
        request.setModeOfTransport(borderauRequest.modeOfTransport)
        request.setTransporterID(borderauRequest.transpoterId)
        request.setBordereauNo(borderauRequest.bordereauNo)
        request.setBordereauDate(borderauRequest.bordereauDate)
        request.setLeauChargementId(borderauRequest.leudechargementId)
        request.setMode(borderauRequest.mode)
        request.setOriginID(borderauRequest.originId)
        request.setDestination("")
        //request.setAacId(aacID)
        request.setFscId(borderauRequest.fscId)
        request.setSpeciesId(0)
        request.setTimezoneId("Asia/Kolkata")//TimeZone.getDefault().getDisplayName()
        request.setWagonNo(borderauRequest.wagonNo)
        request.setWagonId(borderauRequest.vehicalId)
        request.setRequestReceivedDate(borderauRequest.currentDate)

        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    fun generateAddBodereuLogListRequest(borderauRequest: BorderauRequest): AddBoereuLogListingReq {

        var action="Save"

        var request : AddBoereuLogListingReq =   AddBoereuLogListingReq()

        if(logsLiting.size>0) {

            request.setUserID(SharedPref.getUserId(Constants.user_id))
            request.setBordereauHeaderId(borderauRequest!!.headerID)
            request.setdiaType(logsLiting.get(0).getdiaType())
            request.setTimezoneId("Asia/Kolkata")

            for (model in logsLiting) {
                if (action == "Save") {
                    model.setMode("Save")
                } else {
                    model.setMode("Submit")
                }
                model.setMaterialDesc(0)
                //* model.setDetailId("")*//*
            }
            request.setBordereauLogList(logsLiting)
            var json = Gson().toJson(request)
            var test = json
        }

        return  request

    }

    fun generateAddHeaderLogsRequest(borderauRequest: BorderauRequest): BorderauHeaderLogRequest {

            var request  =   BorderauHeaderLogRequest()

            request.setAddBodereuReq(generateAddBodereuRequest(borderauRequest))

            var bodereuLogListing = realm.where(BodereuLogListing::class.java)
                                    .equalTo("forestuniqueId", borderauRequest.uniqueId, Case.SENSITIVE)
                                    .equalTo("isUploaded", false)
                                    .findAll()

            logsLiting.clear()

            for (brder in bodereuLogListing) {
                val bordereauGroundList = BodereuLogListing()
                bordereauGroundList.setDetailId(brder.getDetailId())
                bordereauGroundList.aacId = brder.aacId
                bordereauGroundList.aacName = brder.aacName
                bordereauGroundList.aacYear = brder.aacYear
                bordereauGroundList.setBarcodeNumber(brder.getBarcodeNumber())
                bordereauGroundList.setdiaType(brder.getdiaType())
                bordereauGroundList.setLogNo(brder.getLogNo())
                bordereauGroundList.setLogRecordDocNo(brder.getLogRecordDocNo())
                bordereauGroundList.setLogSpecies(brder.getLogSpecies())
                bordereauGroundList.setLogSpeciesName(brder.getLogSpeciesName())
                bordereauGroundList.setPlaqNo(brder.getPlaqNo())
                bordereauGroundList.setCbm(brder.getCbm())
                bordereauGroundList.setLongBdx(brder.getLongBdx())
                bordereauGroundList.setDiamBdx(brder.getDiamBdx())
                bordereauGroundList.setDiamBdx1(brder.getDiamBdx1())
                bordereauGroundList.setDiamBdx2(brder.getDiamBdx2())
                bordereauGroundList.setDiamBdx3(brder.getDiamBdx3())
                bordereauGroundList.setDiamBdx4(brder.getDiamBdx4())
                bordereauGroundList.setQuality(brder.getQuality())
                bordereauGroundList.setQualityId(brder.getQualityId())
                bordereauGroundList.setDetailId(brder.getDetailId())

                logsLiting.add(bordereauGroundList)
            }

            request.setAddBoereuLogListingReq(generateAddBodereuLogListRequest(borderauRequest))

        return  request

    }

    fun callingAddHeaderLogsBordereauAPI(borderauRequest: BorderauRequest) {
        try {
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
            var request = generateAddHeaderLogsRequest(borderauRequest)
            val call: Call<AddBodereuRes> =
                    apiInterface.syncBordereauModule(request)



            val gson = Gson()
            val json = gson.toJson(request)
            var myJsonString=json.replace("\"","");

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

                                    var addBorderRe = response.body()

                                    val uniqueID = borderauRequest.uniqueId
                                    realm.executeTransaction { realm ->
                                        val realmResults: BorderauRequest = realm.where(BorderauRequest::class.java).equalTo("uniqueId", uniqueID).findFirst()!!
                                        realmResults!!.isUpload = true
                                        //realmResults.headerID = response.body().getBordereauResponse()!!.bordereauHeaderId
                                        realm.copyToRealmOrUpdate(realmResults)
                                    }

                                    Log.d("ResponseRaw", Gson().toJson(response.raw()))

                                    for (brder in addBorderRe.getData()?.addedLogs!!) {
                                        var logno = brder?.replace("/", "/")
                                        Log.e("Log No", "is " + logno)
                                        realm.executeTransaction { realm ->
                                            var borderauLog = realm.where(BodereuLogListing::class.java).equalTo("forestuniqueId", borderauRequest!!.uniqueId).equalTo("logNo", logno).findFirst()
                                            borderauLog?.isUploaded = true
                                            realm.copyToRealmOrUpdate(borderauLog)
                                        }
                                    }

                                    /*if( addBorderRe.getData()?.duplicateLogs.isNullOrEmpty()){
                                        realm.executeTransaction { realm ->
                                            val realmResults: BorderauRequest = realm.where(BorderauRequest::class.java).equalTo("uniqueId", uniqueID).findFirst()!!
                                            realmResults!!.isLogUpload = true
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }*/

                                    /*for (brder in addBorderRe.getData()?.duplicateLogs!!) {
                                        var logno = brder?.replace("/", "/")
                                        Log.e("Log No", "is " + logno)
                                        realm.executeTransaction { realm ->
                                            var borderauLog = realm.where(BodereuLogListing::class.java).equalTo("forestuniqueId", borderauRequest!!.uniqueId).equalTo("logNo", logno).findFirst()
                                            borderauLog?.isFailure = true
                                            realm.copyToRealmOrUpdate(borderauLog)
                                        }
                                    }*/

//                                    getAllLogDataForLoadingWagons(pageCount = 0)

                                } else if (response.body()?.getSeverity() == 306) {

                                } else if (response.body()?.getSeverity() == 201 ||
                                    response.body()?.getSeverity() == 202||
                                    response.body()?.getSeverity() == 500) {

                                    Log.d("ResponseRaw", Gson().toJson(response.raw()))
                                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Barcode Entry").child(borderauRequest.bordereauNo!!).child("ResponseRawData").setValue(Gson().toJson(response.raw()))
                                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Barcode Entry").child(borderauRequest.bordereauNo!!).child("Request").setValue(myJsonString)
                                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Barcode Entry").child(borderauRequest.bordereauNo!!).child("Response").setValue(response.body())


                                    val uniqueID = borderauRequest.uniqueId
                                    realm.executeTransaction() { realm ->
                                        val realmResults: BorderauRequest = realm.where(BorderauRequest::class.java).equalTo("uniqueId", uniqueID).findFirst()!!
                                        realmResults!!.isFailed = true
                                        realmResults.failedReason = response.body().getMessage()
                                        realm.copyToRealmOrUpdate(realmResults)
                                    }

                                }
                            }

                        } else {
                            mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Barcode Entry").child(borderauRequest.bordereauNo!!).child("ResponseRawData").setValue(Gson().toJson(response.raw()))
                            mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Barcode Entry").child(borderauRequest.bordereauNo!!).child("Request").setValue(myJsonString)
                            mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Barcode Entry").child(borderauRequest.bordereauNo!!).child("Response").setValue("Code : ${response.code()} -> " + response.body())
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
                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Barcode Entry").child(borderauRequest.bordereauNo!!).child("Request").setValue(myJsonString)
                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Barcode Entry").child(borderauRequest.bordereauNo!!).child("Response").setValue("onFailure -> ${t.printStackTrace()}")
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun generateLoadingWagonAddHeaderLogsRequest(loadingRequest: LoadingRequest): LoadingHeaderLogRequest {

        var loadingHeaderLogRequest=LoadingHeaderLogRequest()

        var request: AddLoadingBordereueHeaderReq = AddLoadingBordereueHeaderReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
//        request.setSupplier(loadingRequest.forestId)
        request.setSupplier(null)
        loadingRequest.supplierShortName = null
        if(loadingRequest.headerID==0){

            request.setBordereauHeaderId(0)
            if(loadingRequest.iselectronicBordereau!!){
                request.setElectronicBordereau(true)
                request.setSupplierShortName(loadingRequest.supplierShortName)
            }else{
                request.setSupplierShortName(loadingRequest.supplierShortName)
                request.setElectronicBordereau(false)
            }

        }else{
            request.setElectronicBordereau(false)
            request.setSupplierShortName(null)
            request.setBordereauHeaderId(loadingRequest.headerID)

        }
        request.setSupplierLocation(SharedPref.getUserId(Constants.user_location_id))
        request.setModeOfTransport(loadingRequest.modeOfTransport)
        request.setTransporterID(loadingRequest.transpoterId)
        //check if it is comming from edit or norml
        //if from edit then what we recieved will send
        //if from norml then if eBordereu select then both eBo n Manuial Bo send Empty else manual Bo send only

        if(loadingRequest.iselectronicBordereau!!){
            request.setBordereauNo("")
            request.setE_BordereauNo(loadingRequest.ebordereauNo)
           // request.setE_BordereauNo("GAWTEJAS")

            /*try {
                Log.e("barcode", "is" + getRandomNumberString(loadingRequest))
            }catch (e: java.lang.Exception){
                e.printStackTrace()
            }*/

        }else{
            request.setBordereauNo(loadingRequest.bordereauNo)
            //request.setE_BordereauNo(getRandomNumberString(loadingRequest))
            request.setE_BordereauNo(loadingRequest.ebordereauNo)
           // request.setE_BordereauNo("GAWTEJAS")
        }

        request.setBordereauDate(loadingRequest.currentDate)
        request.setLeauChargementId(loadingRequest.leudechargementId)

        request.setMode(loadingRequest.action)
        request.setOriginID(loadingRequest.originId)
        request.setDestination(loadingRequest.destination)
        //request.setAacId(aacID)
        request.setFscId(loadingRequest.fscId)
        request.setSpeciesId(0)
        request.setTimezoneId("Asia/Kolkata")//TimeZone.getDefault().getDisplayName()
        request.setWagonNo(loadingRequest.wagonNo)
        request.setWagonId(loadingRequest.vehicalId)
        request.setRequestReceivedDate(loadingRequest.currentDate)

        var json = Gson().toJson(request)
        var test = json

        loadingHeaderLogRequest.setAddBodereuReq(request)

        var bodereuLogListing = realm.where(WagonLogRequest::class.java)
                .equalTo("forestuniqueId", loadingRequest.uniqueId, Case.SENSITIVE)
                .equalTo("isUploaded", false)
                .findAll()

        logsWagonLiting.clear()


        for (brder in bodereuLogListing) {
            val bordereauGroundList = BodereuLogListing()
            bordereauGroundList.setDetailId(brder.getDetailId())
            bordereauGroundList.aacId = brder.aacId
            bordereauGroundList.aacName = brder.aacName
            bordereauGroundList.aacYear = brder.aacYear
            bordereauGroundList.setBarcodeNumber(brder.getBarcodeNumber())
            bordereauGroundList.setdiaType(brder.getdiaType())
            bordereauGroundList.setLogNo(brder.getLogNo())
            bordereauGroundList.setLogRecordDocNo(brder.getLogRecordDocNo())
            bordereauGroundList.setLogSpecies(brder.getLogSpecies())
            bordereauGroundList.setLogSpeciesName(brder.getLogSpeciesName())
            bordereauGroundList.setPlaqNo(brder.getPlaqNo())
            bordereauGroundList.setCbm(brder.getCbm())
            bordereauGroundList.setLongBdx(brder.getLongBdx())
            bordereauGroundList.setDiamBdx(brder.getDiamBdx())
            bordereauGroundList.setDiamBdx1(brder.getDiamBdx1())
            bordereauGroundList.setDiamBdx2(brder.getDiamBdx2())
            bordereauGroundList.setDiamBdx3(brder.getDiamBdx3())
            bordereauGroundList.setDiamBdx4(brder.getDiamBdx4())
            bordereauGroundList.setQuality(brder.getQuality())
            bordereauGroundList.setQualityId(brder.getQualityId())
            bordereauGroundList.setDetailId(brder.getDetailId())
            bordereauGroundList.setdiaType(brder.getdiaType())
//            bordereauGroundList.fsc(brder.getdiaType())


            logsWagonLiting.add(bordereauGroundList)
        }

        var action="Save"
        var addBoereuLogListingReq : AddBoereuLogListingReq =   AddBoereuLogListingReq()
        addBoereuLogListingReq.setTransporterId(loadingRequest.transpoterId)
        addBoereuLogListingReq.setUserID(SharedPref.getUserId(Constants.user_id))
        //addBoereuLogListingReq.setBordereauHeaderId(loadingRequest.headerID)
        if(logsWagonLiting.size>0) {
            addBoereuLogListingReq.setdiaType(logsWagonLiting.get(0).getdiaType())
        }else{
            addBoereuLogListingReq.setdiaType("")
        }

        if(loadingRequest.headerID==0) {
            addBoereuLogListingReq.setBordereauHeaderId(0)
        }else{
            addBoereuLogListingReq.setBordereauHeaderId(loadingRequest.headerID)
        }


        addBoereuLogListingReq.setTimezoneId("Asia/Kolkata")
        for (model in logsWagonLiting) {
            if(action=="Save"){
                model.setMode("Save")
            }else{
                model.setMode("Submit")
            }
            model.setMaterialDesc(0)
            /* model.setDetailId("")*/
        }

        addBoereuLogListingReq.setBordereauLogList(logsWagonLiting)

        loadingHeaderLogRequest.setAddBoereuLogListingReq(addBoereuLogListingReq)

        //var json =  Gson().toJson(loadingHeaderLogRequest)

        return  loadingHeaderLogRequest

    }

    /*fun getRandomNumberString(loadingRequest: LoadingRequest):String {

        // It will generate 6 digit random Number.
        // from 0 to 999999

        try{
        var rnd =  Random();
        var number = rnd.nextInt(99)
        val currentTimestamp = System.currentTimeMillis()
        var uniquebarcode=currentTimestamp.toString()
        val date = SimpleDateFormat("ddMM", Locale.getDefault()).format(Date())
        var uniquecode=loadingRequest.forestName+date+uniquebarcode

        //val uniqueBarcode = uniquebarcode.substring(0, 12)
        //var uniquebarcode=bodreuNumber+String.format("%04d", number)

        Log.e("uniqueBarcode", "is" + uniquecode)
        val last12: String
        if (uniquecode == null || uniquecode.length < 12) {
            val uniqueBarcode = uniquecode+number
            last12 = uniqueBarcode.substring(uniqueBarcode.length - 12);
        } else {
            last12 = uniquebarcode.substring(uniquebarcode.length - 12);
        }
        // this will convert String number sequence into 6 character.
        Log.e("last12", "is" + last12)
        return last12

        }catch (e: java.lang.Exception){
            e.printStackTrace()
            return ""
        }
    }*/




    fun callingLoadingWagonAddHeaderLogsAPI(loadingRequest: LoadingRequest) {
        try {
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
            var request = generateLoadingWagonAddHeaderLogsRequest(loadingRequest)

            val gson = Gson()
            val json = gson.toJson(request)
            var myJsonString=json.replace("\"","");

            val call: Call<AddBodereuRes> = apiInterface.syncLoadingWagonModule(request)
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
                                    Log.d("ResponseRaw", Gson().toJson(response.raw()))
                                    var addBorderRe = response.body()
                                    val uniqueID = loadingRequest.uniqueId
                                    realm.executeTransaction { realm ->
                                        val realmResults: LoadingRequest = realm.where(LoadingRequest::class.java).equalTo("uniqueId", uniqueID).findFirst()!!
                                        realmResults!!.isUpload = true
                                        realmResults.headerID= response.body().getData()?.headerId?.toInt()
                                        //realmResults.headerID = response.body().getBordereauResponse()!!.bordereauHeaderId
                                        realm.copyToRealmOrUpdate(realmResults)
                                    }

                                    for (brder in addBorderRe.getData()?.addedLogs!!) {
                                        var logno = brder?.replace("/", "/")
                                        Log.e("Log No", "is " + logno)
                                        realm.executeTransaction { realm ->
                                            var wagonLogRequest = realm.where(WagonLogRequest::class.java).equalTo("forestuniqueId", uniqueID).equalTo("logNo", logno).findFirst()
                                            wagonLogRequest?.isUploaded = true
                                            realm.copyToRealmOrUpdate(wagonLogRequest)
                                        }
                                    }

                                    if(addBorderRe.getData()!!.duplicateLogs.isNullOrEmpty()){
                                        realm.executeTransaction { realm ->
                                            val realmResults: LoadingRequest = realm.where(LoadingRequest::class.java).equalTo("uniqueId", uniqueID).findFirst()!!
                                            realmResults!!.isLogUpload = true
                                            //realmResults.headerID = response.body().getBordereauResponse()!!.bordereauHeaderId
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }

                                    /*if (addBorderRe.getData()!!.duplicateLogs!!.size == 0) {
                                        realm.executeTransaction { realm ->
                                            val realmResults: LoadingRequest = realm.where(LoadingRequest::class.java).equalTo("uniqueId", uniqueID).findFirst()!!
                                            realmResults!!.isLogUpload = true
                                            //realmResults.headerID = response.body().getBordereauResponse()!!.bordereauHeaderId
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }*/

                                    if(addBorderRe.getData()!!.duplicateLogs!=null) {
                                        for (brder in addBorderRe.getData()?.duplicateLogs!!) {
                                            var logno = brder?.replace("/", "/")
                                            Log.e("Log No", "is " + logno)
                                            realm.executeTransaction { realm ->
                                                var wagonLogRequest =
                                                    realm.where(WagonLogRequest::class.java)
                                                        .equalTo("forestuniqueId", uniqueID)
                                                        .equalTo("logNo", logno).findFirst()
                                                wagonLogRequest?.isFailure = true
                                                realm.copyToRealmOrUpdate(wagonLogRequest)
                                            }
                                        }
                                    }

                                } else if (response.body()?.getSeverity() == 306) {

                                } else if (response.body()?.getSeverity() == 201 ||
                                    response.body()?.getSeverity() == 202 ||
                                    response.body()?.getSeverity() == 500) {
                                    Log.d("ResponseRaw", Gson().toJson(response.raw()))
                                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Loading Wagon").child(loadingRequest.wagonNo!!).child("ResponseRawData").setValue(Gson().toJson(response.raw()))
                                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Loading Wagon").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("Request").setValue(myJsonString)
                                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Loading Wagon").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("Response").setValue(response.body())

                                    val uniqueID = loadingRequest.uniqueId
                                    realm.executeTransaction() { realm ->
                                        val realmResults: LoadingRequest = realm.where(LoadingRequest::class.java).equalTo("uniqueId", uniqueID).findFirst()!!
                                        realmResults!!.isFailed = true
                                        realmResults.failedReason = response.body().getMessage()
                                        realm.copyToRealmOrUpdate(realmResults)
                                    }

                                }
                            }

                        } else {
                            mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Loading Wagon").child(loadingRequest.wagonNo!!).child("ResponseRawData").setValue(Gson().toJson(response.raw()))
                            mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Loading Wagon").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("Request").setValue(myJsonString)
                            mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Loading Wagon").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("Response").setValue("Code : ${response.code()} -> " + response.body())

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
                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Loading Wagon").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("Request").setValue(myJsonString)
                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Loading Wagon").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("Response").setValue("onFailure -> ${t.printStackTrace()}")

                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    //Declaration Log
    fun generateBordereuPDFRequest(loadingRequest: LoadingRequest): GeneratePDFReq {

        var request = GeneratePDFReq()
        request.setBordereauHeaderId(loadingRequest.headerID.toString())
        request.setIsGSEBOrForestDeclaration(true)
        request.setIntOfflineFlag(0) //for offline = 1, online = 0

        return request
    }

    private fun callingDeclarePostAPI(loadingRequest: LoadingRequest) {
            try {

                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateBordereuPDFRequest(loadingRequest)

                val gson = Gson()
                val json = gson.toJson(request)
                var myJsonString=json.replace("\"","")


                val call_api: Call<AddBodereuLogListingRes> =
                    apiInterface.declareLoadedBordereau(request)
                call_api.enqueue(object :
                    Callback<AddBodereuLogListingRes> {
                    override fun onResponse(
                        call: Call<AddBodereuLogListingRes>,
                        response: Response<AddBodereuLogListingRes>
                    ) {
                        try {

                            if (response.code() == 200) {
                                val responce: AddBodereuLogListingRes =
                                    response.body()!!
                                if (responce.getSeverity() == 200) {
                                    if (responce != null) {

                                        realm.executeTransaction { realm ->
                                            val realmResults: LoadingRequest = realm.where(LoadingRequest::class.java).equalTo("uniqueId", loadingRequest.uniqueId).findFirst()!!
                                            realmResults.isDeclareDone= true
                                            realm.copyToRealmOrUpdate(realmResults)
                                        }
                                    }
                                } else if (response.body()?.getSeverity() == 306) {

                                } else {

                                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Declaration").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("ResponseRawData").setValue(Gson().toJson(response.raw()))
                                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Declaration").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("Request").setValue(myJsonString)
                                    mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Declaration").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("Response").setValue(response.body())

                                    realm.executeTransaction { realm ->
                                        val realmResults: LoadingRequest = realm.where(LoadingRequest::class.java).equalTo("uniqueId", loadingRequest.uniqueId).findFirst()!!
                                        realmResults.isDeclareDoneFail= true
                                        realm.copyToRealmOrUpdate(realmResults)
                                    }
                                }
                            } else{
                                mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Declaration").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("Request").setValue(myJsonString)
                                mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Declaration").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("Response").setValue("Code : ${response.code()} -> " + response.body())
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        call: Call<AddBodereuLogListingRes>,
                        t: Throwable
                    ) {
                        mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Declaration").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("Request").setValue(myJsonString)
                        mDatabase!!.child(SharedPref.read(Constants.user_name)).child("Declaration").child(loadingRequest.wagonNo!!+" "+ loadingRequest.bordereauDate!!.replace(".","")).child("Response").setValue("onFailure -> ${t.printStackTrace()}")

                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }

    }

    private fun sendMessage(showDialog : Boolean, dialogText : String) {
        val intent = Intent("serviceEvent")
        // add data
        intent.putExtra("showDialog", showDialog)
        intent.putExtra("dialogText", dialogText)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        if(::receiver.isInitialized)
            unregisterReceiver(receiver)
        super.onDestroy()
//        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show()
    }

    companion object {
        const val CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
    }


}