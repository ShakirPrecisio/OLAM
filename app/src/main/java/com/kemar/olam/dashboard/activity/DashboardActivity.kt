package com.kemar.olam.dashboard.activity

import android.content.*
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.AddBodereuReq
import com.kemar.olam.bordereau.model.request.AddBoereuLogListingReq
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.request.ValidateBodereueNoReq
import com.kemar.olam.bordereau.model.responce.AddBodereuLogListingRes
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.bordereau.model.responce.GetBodereuLogByIdRes
import com.kemar.olam.dashboard.models.responce.DestinationListDatum
import com.kemar.olam.forestry_management.model.offlineForestryMasterDatum
import com.kemar.olam.forestry_management.offlineModel.*
import com.kemar.olam.login.activity.LoginActivity
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.login.model.responce.LoginRes
import com.kemar.olam.offlineData.requestbody.BorderauRequest
import com.kemar.olam.offlineData.response.*
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.service.MyService
import com.kemar.olam.utility.*
import com.lp.lpwms.ui.offline.response.*
import io.realm.Case
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_dashboard.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.reflect.Type


class DashboardActivity : AppCompatActivity(),View.OnClickListener {
    lateinit var mUtils : Utility
    lateinit var context : Context
    var checkCount :Int =  0
    val pageSize = 400
    lateinit var  moduleconnections: List<LoginRes.AppModuleAccess>

    var BorderauRealResult: RealmResults<BorderauRequest>? = null


    var aacList= ArrayList<Aac>()
    var customerDatumlist= ArrayList<CustomerDatum>()
    var fsclist= ArrayList<FscMasterDatum>()
    var leauChargementDatumlist= ArrayList<LeauChargementDatum>()
    var leauChargementUserDatumlist= ArrayList<LeauChargementUserDatum>()
    var morelist= ArrayList<MoreSupplierInfo>()
    var destinationList= ArrayList<DestinationListDatum>()
    var originMasterlist= ArrayList<OriginMaster>()
    var qualityDatumlist= ArrayList<QualityDatum>()
    var vehicleDatum= ArrayList<VehicleDatum>()
    var specieslist= ArrayList<Species>()
    var supportlist= ArrayList<SupplierDatum>()
    var transportDatumlist= ArrayList<TransporterDatum>()
    var transportModeDatumlist= ArrayList<TransportModeDatum>()


    var aacForestryMaster = ArrayList<aacForestryMaster>()
    var concessionForestryMaster= ArrayList<concessionForestryMaster>()
    var cubeurForestryMaster= ArrayList<cubeurForestryMaster>()
    var harvestingCompanyForestryMaster= ArrayList<harvestingCompanyForestryMaster>()
    var loggingCompanyForestryMaster= ArrayList<loggingCompanyForestryMaster>()
    var loggingLocationForestryMaster= ArrayList<loggingLocationForestryMaster>()
    var ownerForestryMaster= ArrayList<ownerForestryMaster>()
    var pullingDriverForestryMaster= ArrayList<pullingDriverForestryMaster>()
    var pullingEquipmentForestryMaster= ArrayList<pullingEquipmentForestryMaster>()
    var qualityForestryMaster= ArrayList<qualityForestryMaster>()
    var speciesForestryMaster= ArrayList<speciesForestryMaster>()
    var subContractorForestryMaster= ArrayList<subContractorForestryMaster>()
    var treeCutterForestryMaster= ArrayList<treeCutterForestryMaster>()
    var ufaForestryMaster= ArrayList<ufaForestryMaster>()
    var ufgForestryMaster= ArrayList<ufgForestryMaster>()


    var logslist= ArrayList<LogDetail>()

    lateinit var realm: Realm

    var logsLiting: ArrayList<BodereuLogListing> = arrayListOf()

    var realmChangeListener: RealmChangeListener<*>? = null

    var borderauRequestList: List<BorderauRequest> = ArrayList<BorderauRequest>()

    // handler for received Intents for the "my-event" event
    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // Extract data included in the Intent
            val showDialog = intent.getBooleanExtra("showDialog", false)
            val dialogText = intent.getStringExtra("dialogText")

            if(!isFinishing) {
                if (showDialog) {
                    dialog =
                        dialogText?.let { mUtils.setProgressDialog(this@DashboardActivity, it) }
                    dialog?.show()
                } else {
                    if (dialog?.isShowing == true) {
                        dialog?.dismiss()
                    }
                }
            }
            Log.d("receiver", "Got message: $showDialog")
        }
    }

    var dialog : AlertDialog? = null

    var i=0
    var j=0

//    lateinit var mServiceIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mUtils = Utility()
        context = this
        mUtils.setupLocalization(context)
        setContentView(R.layout.activity_dashboard)
        setupClickListnerOnCard()
        setuplangToggleAndUserName()
//        writeLogToFile(context)

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
            IntentFilter("serviceEvent")
        )

        realm = RealmHelper.getRealmInstance()
        realmChangeListener = RealmChangeListener<String?> { }

        try {
            val connectionsJSONString=SharedPref.read(Constants.APPMODULE, "")
            //val connectionsJSONString = getPreferences(Context.MODE_PRIVATE).getString(Constants.APPMODULE, null)
            val type: Type = object : TypeToken<List<LoginRes.AppModuleAccess?>?>() {}.type
            moduleconnections = Gson().fromJson<List<LoginRes.AppModuleAccess>>(
                connectionsJSONString,
                type
            )
        }catch (e:Exception){
            e.printStackTrace()
        }

//        mServiceIntent = Intent(baseContext, MyService::class.java)
//
//        startService(mServiceIntent)

        getOfflineMasterData()
        getDestinationByLocation("-1")
        getOfflineForestryMasterData()
        getOfflineForestryMasterDataCategory2()
//        getMaxHeaderIdByBarcode()
        //getAllLogDataForLoadingWagons()

       /* if (mUtils?.checkInternetConnection(context)) {

            BorderauRealResult = realm.where(BorderauRequest::class.java).equalTo("isUpload",false).findAllAsync()
            borderauRequestList = realm.copyFromRealm(BorderauRealResult)
            if(borderauRequestList.size>i) {
                callingValidateBordereauNoAPI(borderauRequestList.get(i)!!)
            } else{
                uploadLogs()
            }
        }*/
    }

    fun uploadLogs(){
        BorderauRealResult = realm.where(BorderauRequest::class.java).equalTo("isUpload", true).findAllAsync()!!
        borderauRequestList = realm.copyFromRealm(BorderauRealResult)

        if(borderauRequestList.size>j) {
            var bodereuLogListing = realm.where(BodereuLogListing::class.java).equalTo("forestuniqueId", borderauRequestList.get(j)!!.uniqueId, Case.SENSITIVE).equalTo("isUploaded", false).findAll()

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
            if (bodereuLogListing.size > 0) {
                callingAddBordereauLogAPI()
            } else {
                j++
                uploadLogs()
            }
        }
    }


    private fun getDestinationByLocation(userLocationID: String) {
        if (mUtils.checkInternetConnection(context)) {
            try {
                mUtils.showProgressDialog(context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val request  =   getDestinationRequest(userLocationID)
                val call_api: Call<List<com.kemar.olam.dashboard.models.responce.SupplierDatum>> =
                    apiInterface.getGetDestinationList(request)
                call_api.enqueue(object :
                    Callback<List<com.kemar.olam.dashboard.models.responce.SupplierDatum>> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                        call: Call<List<com.kemar.olam.dashboard.models.responce.SupplierDatum>>,
                        response: Response<List<com.kemar.olam.dashboard.models.responce.SupplierDatum>>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: List<com.kemar.olam.dashboard.models.responce.SupplierDatum> =
                                response.body()!!
                            if (responce != null) {

                                realm.executeTransactionAsync({ bgRealm ->

                                    bgRealm.delete(MoreSupplierInfo::class.java)

                                    for (i in responce.indices) {
                                        val destinationListDatum = DestinationListDatum()
                                        destinationListDatum.id = i
                                        destinationListDatum.bordereauNo = responce[i].bordereauNo
                                        destinationListDatum.optionName = responce[i].optionName
                                        destinationListDatum.optionValue = responce[i].optionValue
                                        destinationListDatum.optionValueString = responce[i].optionValueString
                                        destinationListDatum.finalDestination = responce[i].finalDestination
                                        destinationList.add(destinationListDatum)
                                    }
                                    bgRealm.copyToRealmOrUpdate(destinationList)

                                }, {
                                    Log.e("Success", "Success")
                                }) {
                                    Log.e("faile", "faile")
                                }

                                /*if (action.equals(Constants.action_edit, ignoreCase = true)) {
                                    bindHeaderData(headerModel, todaysHistoryModel, commigFrom)
                                }else{
                                    mView.txtDate.text  =  mUtils.getCurrentDate()
                                    selectedDate = mUtils.getCurrentDate()
                                }*/
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        call: Call<List<com.kemar.olam.dashboard.models.responce.SupplierDatum>>,
                        t: Throwable
                    ) {
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            mUtils.showToast(context, getString(R.string.no_internet))
        }
    }


    private fun getMaxHeaderIdByBarcode() {
        if (mUtils.checkInternetConnection(this)) {
            try {
//                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var hashMap=HashMap<String,Any>()
                hashMap.put("logNo","1209/1,10801/2")
                hashMap.put("barcode","648051356117,648129968038")
                val call_api: Call<GetBodereuLogByIdRes> =
                    apiInterface.getBodereuLogsByID(hashMap)
                call_api.enqueue(object :
                    Callback<GetBodereuLogByIdRes> {
                    override fun onResponse(
                        call: Call<GetBodereuLogByIdRes>,
                        response: Response<GetBodereuLogByIdRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response.body()?.getSeverity() == 200) {
                                    val responce: GetBodereuLogByIdRes =
                                        response.body()!!
                                    if (responce != null) {
                                        logsLiting?.clear()
                                        responce.getBordereauLogList()?.let {
                                            logsLiting?.addAll(
                                                it
                                            )
                                        }
                                    }
                                } else if (response.body()?.getSeverity() == 306) {
//                                    mUtils.alertDialgSession(mView.context, activity)
                                } else {
//                                    mUtils.showToast(activity, response.body().getMessage())
                                }
                            } else if (response.code() == 306) {
//                                mUtils.alertDialgSession(mView.context, activity)
                            } else {
//                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        call: Call<GetBodereuLogByIdRes>,
                        t: Throwable
                    ) {
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
//            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }

    private fun getOfflineMasterData() {
        if (mUtils?.checkInternetConnection(context) == true) {
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

                                        for (i in offlineResponse.leauChargementUserData!!.indices!!) {
                                            offlineResponse.leauChargementUserData!!.get(i).id = i
                                            leauChargementUserDatumlist.add(offlineResponse.leauChargementUserData!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(leauChargementUserDatumlist)

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
        }else{
           // mUtils.showToast(this@DashboardActivity, getString(R.string.no_internet))
        }
    }

    private fun getOfflineForestryMasterData() {
        if (mUtils?.checkInternetConnection(context) == true) {
            try {
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val commonRequest=CommonRequest()
                val call: Call<offlineForestryMasterDatum> = apiInterface.getOfflineForestryMasterData(commonRequest)
                call.enqueue(object :
                        Callback<offlineForestryMasterDatum> {
                    override fun onResponse(
                        call: Call<offlineForestryMasterDatum>,
                        response: Response<offlineForestryMasterDatum>
                    ) {
                        try {
                            if (response.code() == 200) {

                                var offlineResponse = response.body()
                                if (offlineResponse != null) {

                                    aacForestryMaster.clear()
                                    subContractorForestryMaster.clear()
                                    ufaForestryMaster.clear()
                                    ufgForestryMaster.clear()
                                    ownerForestryMaster.clear()
                                    qualityForestryMaster.clear()
                                    speciesForestryMaster.clear()
                                    concessionForestryMaster.clear()


                                    realm.executeTransactionAsync({ bgRealm ->


                                        for (i in offlineResponse.aacList!!.indices!!) {
                                            offlineResponse.aacList!!.get(i).id = i

                                            aacForestryMaster.add(offlineResponse.aacList!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(aacForestryMaster)


                                        for (i in offlineResponse.subContractorList.indices) {
                                            offlineResponse.subContractorList.get(i).id = i
                                            subContractorForestryMaster.add(offlineResponse.subContractorList.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(subContractorForestryMaster)


                                        for (i in offlineResponse.ufaList!!.indices!!) {
                                            offlineResponse.ufaList!!.get(i).id = i
                                            ufaForestryMaster.add(offlineResponse.ufaList!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(ufaForestryMaster)


                                        for (i in offlineResponse.ownnerList!!.indices!!) {
                                            offlineResponse.ownnerList!!.get(i).id = i
                                            ownerForestryMaster.add(offlineResponse.ownnerList!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(ownerForestryMaster)


                                        for (i in offlineResponse.speciesList!!.indices!!) {
                                            offlineResponse.speciesList!!.get(i).id = i
                                            speciesForestryMaster.add(offlineResponse.speciesList!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(speciesForestryMaster)


                                        for (i in offlineResponse.concessionList!!.indices!!) {
                                            offlineResponse.concessionList!!.get(i).id = i
                                            concessionForestryMaster.add(offlineResponse.concessionList!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(concessionForestryMaster)

                                        for (i in offlineResponse.qualityList!!.indices!!) {
                                            offlineResponse.qualityList!!.get(i).id = i
                                            qualityForestryMaster.add(offlineResponse.qualityList!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(qualityForestryMaster)

                                        for (i in offlineResponse.ufgList!!.indices!!) {
                                            offlineResponse.ufgList!!.get(i).id = i
                                            ufgForestryMaster.add(offlineResponse.ufgList!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(ufgForestryMaster)

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
                        call: Call<offlineForestryMasterDatum>,
                        t: Throwable
                    ) {
                        //  mUtils.showToast(this@DashboardActivity, Constants.SERVERTIMEOUT)
                        // mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
           // mUtils.showToast(this@DashboardActivity, getString(R.string.no_internet))
        }
    }

    private fun getOfflineForestryMasterDataCategory2() {
        if (mUtils?.checkInternetConnection(context) == true) {
            try {
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val commonRequest=CommonRequest()
                val call: Call<offlineForestryMasterDatum> = apiInterface.getOfflineForestryMasterDataCategory2(commonRequest)
                call.enqueue(object :
                        Callback<offlineForestryMasterDatum> {
                    override fun onResponse(
                            call: Call<offlineForestryMasterDatum>,
                            response: Response<offlineForestryMasterDatum>
                    ) {
                        try {
                            if (response.code() == 200) {

                                var offlineResponse = response.body()
                                if (offlineResponse != null) {
                                    cubeurForestryMaster.clear()
                                    harvestingCompanyForestryMaster.clear()
                                    loggingCompanyForestryMaster.clear()
                                    loggingLocationForestryMaster.clear()
                                    pullingDriverForestryMaster.clear()
                                    pullingEquipmentForestryMaster.clear()
                                    treeCutterForestryMaster.clear()


                                    realm.executeTransactionAsync({ bgRealm ->


                                        for (i in offlineResponse.cubeurDtls!!.indices!!) {
                                            offlineResponse.cubeurDtls!!.get(i).id = i

                                            cubeurForestryMaster.add(offlineResponse.cubeurDtls!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(cubeurForestryMaster)


                                        for (i in offlineResponse.harvestingCompanyDtls!!.indices!!) {
                                            offlineResponse.harvestingCompanyDtls!!.get(i).id = i
                                            harvestingCompanyForestryMaster.add(offlineResponse.harvestingCompanyDtls!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(harvestingCompanyForestryMaster)


                                        for (i in offlineResponse.loggingCompanyDtls!!.indices!!) {
                                            offlineResponse.loggingCompanyDtls!!.get(i).id = i
                                            loggingCompanyForestryMaster.add(offlineResponse.loggingCompanyDtls!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(loggingCompanyForestryMaster)


                                        for (i in offlineResponse.loggingLocationDtls!!.indices!!) {
                                            offlineResponse.loggingLocationDtls!!.get(i).id = i
                                            loggingLocationForestryMaster.add(offlineResponse.loggingLocationDtls!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(loggingLocationForestryMaster)


                                        for (i in offlineResponse.pullingDriverDtls!!.indices!!) {
                                            offlineResponse.pullingDriverDtls!!.get(i).id = i
                                            pullingDriverForestryMaster.add(offlineResponse.pullingDriverDtls!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(pullingDriverForestryMaster)


                                        for (i in offlineResponse.pullingEquipmentDtls!!.indices!!) {
                                            offlineResponse.pullingEquipmentDtls!!.get(i).id = i
                                            pullingEquipmentForestryMaster.add(offlineResponse.pullingEquipmentDtls!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(pullingEquipmentForestryMaster)

                                        for (i in offlineResponse.treeCutterDtls!!.indices!!) {
                                            offlineResponse.treeCutterDtls!!.get(i).id = i
                                            treeCutterForestryMaster.add(offlineResponse.treeCutterDtls!!.get(i))
                                        }
                                        bgRealm.copyToRealmOrUpdate(treeCutterForestryMaster)

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
                            call: Call<offlineForestryMasterDatum>,
                            t: Throwable
                    ) {
                        //  mUtils.showToast(this@DashboardActivity, Constants.SERVERTIMEOUT)
                        // mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
           // mUtils.showToast(this@DashboardActivity, getString(R.string.no_internet))
        }
    }

    private fun getAllLogDataForLoadingWagons() {
        if (mUtils?.checkInternetConnection(context) == true) {
            try {
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val commonRequest=CommonRequest()
                commonRequest.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
                val call: Call<LogOfflineResponse> = apiInterface.getAllLogDataForLoadingWagons(commonRequest)
                call.enqueue(object :
                        Callback<LogOfflineResponse> {
                    override fun onResponse(
                            call: Call<LogOfflineResponse>,
                            response: Response<LogOfflineResponse>
                    ) {
                        try {
                            if (response.code() == 200) {

                                var offlineResponse = response.body()
                                if (offlineResponse != null) {
                                    logslist.clear()

                                    realm.executeTransactionAsync({ bgRealm ->

                                        bgRealm.delete(LogDetail::class.java)
                                    }, {


                                        Log.e("Success", "Success delete")

                                        realm.executeTransactionAsync({ bgRealm ->

                                            for (i in offlineResponse.logDetails!!.indices!!) {
                                                offlineResponse.logDetails!!.get(i).id = i

                                                logslist.add(offlineResponse.logDetails!!.get(i))
                                            }
                                            bgRealm.copyToRealmOrUpdate(logslist)

                                        }, {
                                            Log.e("Success", "Success logs")
                                        }) {
                                            Log.e("faile", "failed logs")
                                        }
                                    }) {
                                        Log.e("faile", "failed delte")
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
                            call: Call<LogOfflineResponse>,
                            t: Throwable
                    ) {
                        //  mUtils.showToast(this@DashboardActivity, Constants.SERVERTIMEOUT)
                        // mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            // mUtils.showToast(this@DashboardActivity, getString(R.string.no_internet))
        }
    }

    fun getDestinationRequest(userLocationID:String): CommonRequest {
        var request : CommonRequest =   CommonRequest()
        request.setUserLocationId(userLocationID)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    fun writeLogToFile(context: Context) {
        val fileName = "logcat.txt"
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString(), fileName)
        if (!file.exists()) file.createNewFile()
        val command = "logcat -f " + file.absolutePath
        Runtime.getRuntime().exec(command)
    }

    fun setuplangToggleAndUserName(){
        txtName.text = SharedPref.read(Constants.user_name)
       val language = mUtils.getLangSettingFromSharePre(context, "fr")
        if(language.equals("en", ignoreCase = true)){
            langSwitch.setChecked(true)
        }else{
            langSwitch.setChecked(false)
        }

        langSwitch.setOnCheckedChangeListener { compoundButton, b ->
                if(langSwitch.isChecked()){
                    mUtils.setLangSettingFromSharePre(
                            context,
                            resources.getString(R.string.english)
                    )
                }else{
                    mUtils.setLangSettingFromSharePre(context, resources.getString(R.string.french))
                }

                val i: Intent = Intent(this, DashboardActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i)
                finish()
            }
    }



    fun setupClickListnerOnCard(){
        card_Borderau.setOnClickListener(this)
        card_bc_reprint.setOnClickListener(this)
        card_loading_wagons.setOnClickListener(this)
        card_origin_verification.setOnClickListener(this)
        card_inspection_sales.setOnClickListener(this)
        card_delivery_management.setOnClickListener(this)
        card_physical_count.setOnClickListener(this)
        card_offlie.setOnClickListener(this)
        card_forestry_management.setOnClickListener(this)
        ivLogout.setOnClickListener(this)
        ivOfflineLogSync.setOnClickListener(this)
        //langSwitch.setOnClickListener(this)

    }

    fun gotToHomeScreen(commingFrom: String){
       var i = Intent(this, HomeActivity::class.java)
        i.putExtra(Constants.comming_from, commingFrom)
        startActivity(i)
       /* finish()*/
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {

            R.id.card_Borderau -> {
                if (moduleconnections.size == 0) {
                    mUtils.showAlert(this@DashboardActivity, "Module not accessible")
                } else {
                    for (i in moduleconnections.indices) {
                        if (module_barcode.text.toString()
                                        .equals(moduleconnections.get(i).moduleCode)
                        ) {
                            gotToHomeScreen(resources.getString(R.string.bordereau_module))
                            break
                        } else {
                            if (i == moduleconnections.size - 1) {
                                mUtils.showAlert(this@DashboardActivity, "Module not accessible")
                            }
                        }
                    }
                }
            }


            R.id.card_bc_reprint -> {
                // mUtils.showToast(context,"Comming Soon")
                when (SharedPref.read(Constants.user_role).toString()) {
                    "SuperUserApp", "Super User", "admin" -> {
                        //gotToHomeScreen(resources.getString(R.string.bc_re_print_n_edit))
                        if (moduleconnections.size == 0) {
                            mUtils.showAlert(this@DashboardActivity, "Module not accessible")
                        } else {
                            for (i in moduleconnections.indices) {
                                if (module_print.text.toString()
                                                .equals(moduleconnections.get(i).moduleCode)
                                ) {
                                    gotToHomeScreen(resources.getString(R.string.bc_re_print_n_edit))
                                    break
                                } else {
                                    if (i == moduleconnections.size - 1) {
                                        mUtils.showAlert(
                                                this@DashboardActivity,
                                                "Module not accessible"
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        mUtils.showAlert(
                                this@DashboardActivity,
                                resources.getString(R.string.you_are_not_a_super_user)
                        )
                    }
                }
            }
            R.id.card_loading_wagons -> {
                //gotToHomeScreen(resources.getString(R.string.loading_wagons))
                if (moduleconnections.size == 0) {
                    mUtils.showAlert(this@DashboardActivity, "Module not accessible")
                } else {
                    for (i in moduleconnections.indices) {
                        if (module_wagons.text.toString()
                                        .equals(moduleconnections.get(i).moduleCode)
                        ) {
                            gotToHomeScreen(resources.getString(R.string.loading_wagons))
                            break
                        } else {
                            if (i == moduleconnections.size - 1) {
                                mUtils.showAlert(this@DashboardActivity, "Module not accessible")
                            }
                        }
                    }
                }
                //   mUtils.showToast(context,"Comming Soon")

            }
            R.id.card_origin_verification -> {
                if (moduleconnections.size == 0) {
                    mUtils.showAlert(
                            this@DashboardActivity,
                            "Module not accessible"
                    )
                } else {
                    for (i in moduleconnections.indices) {
                        if (module_verification.text.toString()
                                        .equals(moduleconnections.get(i).moduleCode)
                        ) {
                            gotToHomeScreen(resources.getString(R.string.origin_verification))
                            break
                        } else {
                            if (i == moduleconnections.size - 1) {
                                mUtils.showAlert(this@DashboardActivity, "Module not accessible")
                            }
                        }
                    }
                }

                //gotToHomeScreen(resources.getString(R.string.origin_verification))
                //mUtils.showToast(context,"Comming Soon")
            }
            R.id.card_inspection_sales -> {


                if (moduleconnections.size == 0) {
                    mUtils.showAlert(this@DashboardActivity, "Module not accessible")
                } else {
                    for (i in moduleconnections.indices) {
                        if (module_sales.text.toString()
                                        .equals(moduleconnections.get(i).moduleCode)
                        ) {
                            gotToHomeScreen(resources.getString(R.string.inspection_n_sales))
                            break
                        } else {
                            if (i == moduleconnections.size - 1) {
                                mUtils.showAlert(this@DashboardActivity, "Module not accessible")
                            }
                        }
                    }
                }

                // gotToHomeScreen(resources.getString(R.string.inspection_n_sales))
                //mUtils.showToast(context,"Comming Soon")
            }
            R.id.card_forestry_management -> {

                if (moduleconnections.size == 0) {
                    mUtils.showAlert(this@DashboardActivity, "Module not accessible")
                } else {
                    for (i in moduleconnections.indices) {
                        if (module_forestryManagement.text.toString() == moduleconnections[i].moduleCode
                        ) {
                            gotToHomeScreen(resources.getString(R.string.forestry_management))
                            break
                        } else {
                            if (i == moduleconnections.size - 1) {
                                mUtils.showAlert(this@DashboardActivity, "Module not accessible")
                            }
                        }
                    }
                }

                // gotToHomeScreen(resources.getString(R.string.inspection_n_sales))
//                mUtils.showToast(context,"Comming Soon")
            }
            R.id.card_delivery_management -> {

                if (moduleconnections.size == 0) {
                    mUtils.showAlert(this@DashboardActivity, "Module not accessible")
                } else {
                    for (i in moduleconnections.indices) {
                        if (module_delivery.text.toString()
                                        .equals(moduleconnections.get(i).moduleCode)
                        ) {
                            gotToHomeScreen(resources.getString(R.string.drawer_delivery_management))
                            break
                        } else {
                            if (i == moduleconnections.size - 1) {
                                mUtils.showAlert(this@DashboardActivity, "Module not accessible")
                            }
                        }
                    }
                }


                //gotToHomeScreen(resources.getString(R.string.drawer_delivery_management))
                //mUtils.showToast(context,"Comming Soon")
            }
            R.id.ivLogout -> {
                alertDialogForLogoutFuntion()
            }
            R.id.ivOfflineLogSync -> {
                try {
                    val i = Intent(this@DashboardActivity, MyService::class.java)
                    i.action = Constants.RECALL
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(i)
                    } else{
                        startService(i)
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }
            R.id.langSwitch -> {
                if (langSwitch.isChecked()) {
                    langSwitch.setChecked(true)
                } else {
                    langSwitch.setChecked(false)
                }

                if (langSwitch.isChecked()) {
                    mUtils.setLangSettingFromSharePre(
                            context,
                            resources.getString(R.string.english)
                    )
                } else {
                    mUtils.setLangSettingFromSharePre(context, resources.getString(R.string.french))
                }

                val i: Intent = Intent(this, DashboardActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i)
                finish()

            }

            R.id.card_offlie -> {
                gotToHomeScreen(resources.getString(R.string.offline_mode))
            }

            R.id.card_physical_count->{
                gotToHomeScreen(resources.getString(R.string.physical_count))
            }
        }
    }

    fun logoutUser() {
        SharedPref.clearAllData()
        SharedPref.write(Constants.LOGIN, false)
        val i: Intent = Intent(this, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i)
        finish()
    }

    fun alertDialogForLogoutFuntion(){
        val alert: android.app.AlertDialog =
            android.app.AlertDialog.Builder(context, R.style.CustomDialogWithBackground)
                //.setTitle(activity?.getString(R.string.app_name))
                .setMessage(context.resources.getString(R.string.are_you_sure_you_want_to_logout))
                .setPositiveButton(
                        "Ok"
                ) { dialog, which ->
                    dialog.dismiss()
                    logoutUser()
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        alert.getButton(DialogInterface.BUTTON_NEGATIVE).setBackgroundColor(Color.WHITE)
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setBackgroundColor(Color.WHITE)

        val positive: Button =
            alert.getButton(DialogInterface.BUTTON_POSITIVE)
        positive.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))

        val negative: Button =
            alert.getButton(DialogInterface.BUTTON_NEGATIVE)
        negative.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
    }




    companion object {
        // Define the parameter keys:
        const val KEY_INPUT_url_ARG = "KEY_url_ARG"
        const val KEY_INPUT_filePath_ARG = "KEY_filePath_ARG"
        const val MATERIALID = "MATERIALID"

        const val KEY_STR_name_ARG = "KEY_STR_name_ARG"
        const val KEY_STR_url_ARG = "KEY_STR_url_ARG"
        const val KEY_STR_fileSize_ARG = "KEY_STR_fileSize_ARG"

        const val KEY_ARRAY_NAME = "KEY_ARRAY_NAME"
        const val KEY_ARRAY_URL = "KEY_ARRAY_URL"
        const val KEY_ARRAY_SIZES = "KEY_ARRAY_SIZES"

        // ...and the result key:
        const val KEY_RESULT = "result"
    }



    fun generateValidateBodereuNoRequest(borderauRequest: BorderauRequest): ValidateBodereueNoReq {

        var request : ValidateBodereueNoReq =   ValidateBodereueNoReq()
        request.setBordereauNo(borderauRequest.bordereauNo)
        request.setBordereauDate(borderauRequest.bordereauDate)
        request.setSupplier(borderauRequest.forestId)
        var json =  Gson().toJson(request)
        return  request
    }

    private fun callingValidateBordereauNoAPI(borderauRequest: BorderauRequest) {

        try {
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
            var request = generateValidateBodereuNoRequest(borderauRequest)
            val call: Call<AddBodereuRes> =
                    apiInterface.validateBordereauNo(request)
            call.enqueue(object :
                    Callback<AddBodereuRes> {
                override fun onResponse(
                        call: Call<AddBodereuRes>,
                        response: Response<AddBodereuRes>
                ) {

                    try {
                        if (response.code() == 200) {
                            if (response != null) {
                                if (response.body().getSeverity() == 200) {
                                    callingAddBordereauAPI(borderauRequest)
                                } else if (response.body()?.getSeverity() == 201) {
                                    val uniqueID = borderauRequest.uniqueId
                                    realm.executeTransaction { realm ->
                                        val realmResults: BorderauRequest = realm.where(BorderauRequest::class.java).equalTo("uniqueId", uniqueID).findFirst()!!
                                        realmResults!!.isFailed = true
                                        realm.copyToRealmOrUpdate(realmResults)
                                    }

                                    i++
                                    if (borderauRequestList.size > i) {
                                        callingValidateBordereauNoAPI(borderauRequestList.get(i))
                                    } else {
                                        uploadLogs()
                                    }
                                } else if (response.body()?.getSeverity() == 306) {

                                } else if (response.body()?.getSeverity() == 202) {
                                    i++
                                    if (borderauRequestList.size > i) {
                                        callingValidateBordereauNoAPI(borderauRequestList.get(i))
                                    } else {
                                        uploadLogs()
                                    }
                                }
                            }

                        } else if (response.code() == 306) {

                        } else {

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onFailure(
                        call: Call<AddBodereuRes>,
                        t: Throwable
                ) {
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
        request.setMode("Save")
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

    private fun callingAddBordereauAPI(borderauRequest: BorderauRequest) {
        try {
            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
            var request = generateAddBodereuRequest(borderauRequest)
            val call: Call<AddBodereuRes> =
                    apiInterface.addBordereau(request)
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
                                    val uniqueID = borderauRequest.uniqueId
                                    realm.executeTransaction { realm ->
                                        val realmResults: BorderauRequest = realm.where(BorderauRequest::class.java).equalTo("uniqueId", uniqueID).findFirst()!!
                                        realmResults!!.isUpload = true
                                        realmResults.headerID = response.body().getBordereauResponse()!!.bordereauHeaderId
                                        realm.copyToRealmOrUpdate(realmResults)
                                    }

                                    i++
                                    if (borderauRequestList.size > i) {
                                        callingValidateBordereauNoAPI(borderauRequestList.get(i))
                                    } else {
                                        uploadLogs()
                                    }

                                } else if (response.body()?.getSeverity() == 306) {

                                } else if (response.body()?.getSeverity() == 202) {

                                    val uniqueID = borderauRequest.uniqueId
                                    realm.executeTransaction { realm ->
                                        val realmResults: BorderauRequest = realm.where(BorderauRequest::class.java).equalTo("uniqueId", uniqueID).findFirst()!!
                                        realmResults!!.isFailed = true
                                        realmResults.failedReason = response.body().getMessage()
                                        realmResults.headerID = response.body().getBordereauResponse()!!.bordereauHeaderId
                                        realm.copyToRealmOrUpdate(realmResults)
                                    }

                                    i++
                                    if (borderauRequestList.size > i) {
                                        callingValidateBordereauNoAPI(borderauRequestList.get(i))
                                    } else {
                                        uploadLogs()
                                    }
                                } else {
                                }
                            }

                        } else if (response.code() == 306) {
                        } else {
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
                }
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun generateAddBodereuLogListRequest(): AddBoereuLogListingReq {

        var action="Save"
        var request : AddBoereuLogListingReq =   AddBoereuLogListingReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setBordereauHeaderId(BorderauRealResult!!.get(j)!!.headerID)
        request.setdiaType(logsLiting.get(0).getdiaType())


        request.setTimezoneId("Asia/Kolkata")
        for (model in logsLiting) {
            if(action=="Save"){
                model.setMode("Save")
            }else{
                model.setMode("Submit")
            }
            model.setMaterialDesc(0)
            /* model.setDetailId("")*/
        }
        request.setBordereauLogList(logsLiting)
        var json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun callingAddBordereauLogAPI() {
        if (mUtils?.checkInternetConnection(this@DashboardActivity) == true) {
            try {

                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request = generateAddBodereuLogListRequest()
                val call: Call<AddBodereuLogListingRes> =
                        apiInterface.addBordereauLogs(request)
                call.enqueue(object :
                        Callback<AddBodereuLogListingRes> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                            call: Call<AddBodereuLogListingRes>,
                            response: Response<AddBodereuLogListingRes>
                    ) {
                        mUtils.dismissProgressDialog()
                        try {
                            if (response.code() == 200) {
                                if (response != null) {
                                    if (response.body().getSeverity() == 200) {

                                        realm.executeTransaction { realm ->
                                            var bodereuLogListing = realm.where(BodereuLogListing::class.java).equalTo("forestuniqueId", borderauRequestList.get(j)!!.uniqueId).findAll()


                                            for (brder in bodereuLogListing) {
                                                brder!!.isUploaded = true
                                            }
                                            realm.copyToRealmOrUpdate(bodereuLogListing)
                                        }

                                        j++
                                        uploadLogs()
                                    } else if (response.body()?.getSeverity() == 306) {
                                        //  mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        j++
                                        uploadLogs()
                                        // mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }
                            } else if (response.code() == 306) {

                            } else {

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
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{

        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        super.onDestroy()
    }

    //    private fun getAllLogDataForLoadingWagons(pageCount : Int) {
//        try {
//
//            val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
//            val commonRequest= CommonRequest()
//            commonRequest.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
//            commonRequest.size = pageSize
//            commonRequest.page = pageCount
//            val call: Call<LogOfflineResponse> = apiInterface.getAllLogDataForLoadingWagons(commonRequest)
//            call.enqueue(object :
//                Callback<LogOfflineResponse> {
//                override fun onResponse(
//                    call: Call<LogOfflineResponse>,
//                    response: Response<LogOfflineResponse>
//                ) {
//                    try {
//                        if (response.code() == 200) {
//
//                            var offlineResponse = response.body()
//                            if (offlineResponse != null) {
//                                logslist.clear()
//
//                                realm.executeTransactionAsync({ bgRealm ->
//
//                                    if(pageCount == 0) {
//                                        bgRealm.delete(LogDetail::class.java)
//                                        Log.e("Success", "Success delete")
//                                    }
//                                }, {
//
//                                    realm.executeTransactionAsync({ bgRealm ->
//
//                                        for (i in offlineResponse.logDetails!!.indices!!) {
//                                            offlineResponse.logDetails!!.get(i).id = i + (pageCount * pageSize)
//
//                                            logslist.add(offlineResponse.logDetails!!.get(i))
//                                        }
//                                        Log.d("logslistcount", logslist.toString())
//                                        bgRealm.copyToRealmOrUpdate(logslist)
//
//                                    }, {
//                                        Log.e("Success", "Success logs")
//                                    }) {
//                                        Log.e("faile", "failed logs")
//                                    }
//                                }) {
//                                    Log.e("faile", "failed delte")
//                                }
//
//                                if(offlineResponse.logDetails!!.isNotEmpty()){
//                                    getAllLogDataForLoadingWagons(pageCount = (pageCount+1))
//                                } else {
//                                    if(dialog.isShowing)
//                                        dialog.dismiss()
//                                    mUtils.showAlert(this@DashboardActivity, "Offline Log Syncing Completed")
//                                }
//
//                            }
//
//                        } else if (response.code() == 306) {
//                            if(dialog.isShowing)
//                                dialog.dismiss()
//                             mUtils.alertDialgSession(this@DashboardActivity, this@DashboardActivity)
//                        } else {
//                            if(dialog.isShowing)
//                                dialog.dismiss()
//                             mUtils.showToast(this@DashboardActivity, Constants.SOMETHINGWENTWRONG)
//                        }
//                    } catch (e: Exception) {
//                        if(dialog.isShowing)
//                            dialog.dismiss()
//                        mUtils.showToast(this@DashboardActivity, Constants.SOMETHINGWENTWRONG)
//                        e.printStackTrace()
//                    }
//                }
//
//                override fun onFailure(
//                    call: Call<LogOfflineResponse>,
//                    t: Throwable
//                ) {
//                    if(dialog.isShowing)
//                        dialog.dismiss()
//                      mUtils.showToast(this@DashboardActivity, Constants.SERVERTIMEOUT)
//                }
//            })
//        } catch (e: Exception) {
//            if(dialog.isShowing)
//                dialog.dismiss()
//            mUtils.showToast(this@DashboardActivity, Constants.SOMETHINGWENTWRONG)
//            e.printStackTrace()
//        }
//    }

}