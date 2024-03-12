package com.kemar.olam.bordereau.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.AddBodereuReq
import com.kemar.olam.bordereau.model.request.ValidateBodereueNoReq
import com.kemar.olam.bordereau.model.responce.AddBodereuRes
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.offlineData.requestbody.BorderauRequest
import com.kemar.olam.offlineData.response.MoreSupplierInfo
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.*
import com.lp.lpwms.ui.offline.response.*
import io.realm.Case
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmResults
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment__add_header.*
import kotlinx.android.synthetic.main.fragment__add_header.view.*
import org.spongycastle.asn1.pkcs.PKCSObjectIdentifiers.data
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class AddHeaderFragment : Fragment(),DialogFragment.GetDialogListener,View.OnClickListener{

    lateinit var mView : View
    lateinit var mUtils : Utility
    lateinit var countryDialogFragment: DialogFragment
    lateinit var  presectedCalender : Calendar
    var forestID : Int = 0
    var headerID:Int = 0
    var  aacID: Int = 0
    var originID:Int=0
    var  fscID: Int = 0
    var  uniqueId: String = ""
    var  destinationID: Int = 0
    var transporterID:Int=0
    var vehicleID:Int?=0
    var  distionsID: Int = 0
    var leude_chargementID : Int = 0
    var transportModeID : Int =  0
    var selectedDate  :String=""
    var commigFrom= ""
    var action =""
    var headerModel  : AddBodereuRes.BordereauResponse =  AddBodereuRes.BordereauResponse()
    var todaysHistoryModel  :LogsUserHistoryRes.BordereauRecordList =  LogsUserHistoryRes.BordereauRecordList()

    lateinit var realm: Realm
    //listings
    var commonForestMaster : GetForestDataRes? = GetForestDataRes()
    var vehicleMaster : ArrayList<SupplierDatum?>? = arrayListOf()
    var forestList : ArrayList<SupplierDatum?>? = arrayListOf()
    var fscList : ArrayList<SupplierDatum?>? = arrayListOf()
    var lechargemetList : ArrayList<SupplierDatum?>? = arrayListOf()
    var moreSupplierList : ArrayList<MoreSupplierInfo?>? = arrayListOf()
    var destinationList : ArrayList<SupplierDatum?>? = arrayListOf()
    var originList : ArrayList<SupplierDatum?>? = arrayListOf()
    var transporterList : ArrayList<SupplierDatum?>? = arrayListOf()
    var isDialogShowing:Boolean = false
    var transporterFirstCount  = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        realm = RealmHelper.getRealmInstance()
        super.onCreate(savedInstanceState)
    }

    //Origin =  Supplier Location
    //Forest  = Supplier
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView=inflater.inflate(R.layout.fragment__add_header, container, false)
        mUtils = Utility()
        init()
       // (activity as HomeActivity).hideActionBar()

        return mView;
    }


    fun offlineData(){

        forestList!!.clear()
        transporterList?.clear()
        val locationid=SharedPref.getUserId(Constants.user_location_id)
        realm.executeTransactionAsync { bgRealm ->
            val supplierDatum: RealmResults<com.lp.lpwms.ui.offline.response.SupplierDatum> = bgRealm.where(com.lp.lpwms.ui.offline.response.SupplierDatum::class.java).equalTo("uid",locationid ).findAll()
            Log.e("supplierdatum","is "+supplierDatum.size)
            for(supplierdatum in supplierDatum){
                var supplierDatum=SupplierDatum()
                supplierDatum.optionValueString=supplierdatum.optionValueString
                supplierDatum.optionValue=supplierdatum.optionValue
                supplierDatum.optionName=supplierdatum.optionName
                forestList!!.add(supplierDatum)
            }
        }

        realm.executeTransactionAsync { bgRealm ->
            val transporterDatum: RealmResults<TransportModeDatum> = bgRealm.where(TransportModeDatum::class.java).equalTo("uid",transportModeID ).findAll()
            Log.e("TransportModeDatum","is "+transporterDatum.size)
            for(transpoter in transporterDatum){
                var transporterDatum=SupplierDatum()
                transporterDatum.optionValueString=transpoter.optionValueString
                transporterDatum.optionValue=transpoter.optionValue
                transporterDatum.optionName=transpoter.optionName
                transporterList?.add(transporterDatum)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun  bindHeaderData(headerData: AddBodereuRes.BordereauResponse, todaysHistoryModel: LogsUserHistoryRes.BordereauRecordList, commingFrom: String){
        try {
            mView.txtBordero_No.isEnabled = false
            mView.linDate.isEnabled = false
            mView.txtDate.isEnabled = false
            mView.txtForest.isEnabled = false
            mView.linForest.isEnabled = false

            mView.txtBordero_No.backgroundTintList = mView.context.resources.getColorStateList(R.color.gray_200)
            mView.linDate.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
            mView.linBordero_No.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
            mView.linForest.setBackgroundResource(R.drawable.bg_for_editext_non_editable)

            if (commigFrom.equals(Constants.header, ignoreCase = true)) {
                when (headerData.modeOfTransport) {
                    1 -> {
                        mView.chkWagon.isChecked = true
                        transportModeID = headerData.modeOfTransport!!
                    }
                    17 -> {
                        mView.chkBarge.isChecked = true
                        transportModeID = headerData.modeOfTransport!!
                    }
                    3 -> {
                        mView.chkTruck.isChecked = true
                        transportModeID = headerData.modeOfTransport!!
                    }
                }
                mView.txtDate.setText(headerData?.bordereauDate.toString())
                headerID = headerData?.bordereauHeaderId!!
                mView.txtBordero_No.setText(headerData?.bordereauNo)
                selectedDate = headerData?.bordereauDate?.toString()!!
                mView.txtForest.text = headerData?.supplierName
                forestID = headerData?.supplier!!
                callingForestMasterAPI(
                        forestID.toString(),
                        SharedPref.getUserId(Constants.user_location_id).toString()
                )
                mView.txtOriginCFAT.text = headerData?.originName
                originID = headerData?.originID!!
                mView.txtFSC.text = headerData?.fscName
                fscID = headerData?.fscId!!
                mView.txtLeude_chargement.text = headerData?.leauChargementName
                leude_chargementID = headerData?.leauChargementId!!
                mView.txtDestination.text = headerData?.destination
                mView.txtTranporter.text = headerData?.transporterName
                transporterID = headerData?.transporterId!!
                mView.txtDistance.text = headerData?.distance
                vehicleID = headerData?.wagonId
                mView.txtVehicleNo.setText(headerData?.wagonNo)
            } else {
                when (todaysHistoryModel.transportMode) {
                    1 -> {
                        mView.chkWagon.isChecked = true
                        transportModeID = todaysHistoryModel.transportMode!!
                    }
                    17 -> {
                        mView.chkBarge.isChecked = true
                        transportModeID = todaysHistoryModel.transportMode!!
                    }
                    3 -> {
                        mView.chkTruck.isChecked = true
                        transportModeID = todaysHistoryModel.transportMode!!
                    }
                }
                headerID = todaysHistoryModel?.bordereauHeaderId!!
                mView.txtDate.setText(todaysHistoryModel?.bordereauDateString)
                mView.txtBordero_No.setText(todaysHistoryModel?.bordereauNo)
                selectedDate = todaysHistoryModel?.bordereauDateString?.toString()!!
                mView.txtForest.text = todaysHistoryModel?.supplierName
                forestID = todaysHistoryModel?.supplierId!!

                if (mUtils.checkInternetConnection(mView.context)) {
                    callingForestMasterAPI(forestID.toString(), SharedPref.getUserId(Constants.user_location_id).toString())
                }else{
                   mView.txtBordero_No.isEnabled = true
                    mView.linBordero_No.setBackgroundResource(R.drawable.bg_for_editest)
                    mView.txtBordero_No.backgroundTintList = mView.context.resources.getColorStateList(R.color.white)
                    offlineForestSelection(true)
                }
                uniqueId= todaysHistoryModel.uniqueId.toString()
                mView.txtOriginCFAT.text = todaysHistoryModel?.originName
                originID = todaysHistoryModel?.originId!!
                mView.txtFSC.text = todaysHistoryModel?.fscName
                fscID = todaysHistoryModel?.fscId!!
                mView.txtLeude_chargement.text = todaysHistoryModel?.leauChargementName
                leude_chargementID = todaysHistoryModel?.leauChargementId!!
                mView.txtDestination.text = todaysHistoryModel?.destination
                mView.txtTranporter.text = todaysHistoryModel?.transporterName
                transporterID = todaysHistoryModel?.transporterId!!
                if (!mUtils.checkInternetConnection(mView.context)) {
                    offlineData()
                }
                mView.txtDistance.text = todaysHistoryModel?.distance
                mView.txtVehicleNo.setText(todaysHistoryModel?.wagonNo)
                vehicleID = todaysHistoryModel?.wagonId

            }
        }catch (e: Exception){
            e.printStackTrace()
        }

    }

     @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
     private fun init(){


         action =
             arguments?.getString(Constants.action).toString();
         commigFrom =
             arguments?.getString(Constants.comming_from).toString();
         if(action.equals(Constants.action_edit, ignoreCase = true)) {
             if (commigFrom.equals(Constants.header, ignoreCase = true)) {
                 val headerDataModel: AddBodereuRes.BordereauResponse? =
                     arguments?.getSerializable(Constants.badereuModel) as AddBodereuRes.BordereauResponse
                 if (headerDataModel != null) {
                     headerModel = headerDataModel
                 }
             } else {
                 val headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
                     arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
                 if (headerDataModel != null) {
                     todaysHistoryModel = headerDataModel
                 }
             }
         }else{
             mView.chkTruck?.isChecked =  true
             transportModeID = 3
             getTranspoterByTransportMode(transportModeID.toString())
         }

         presectedCalender = Calendar.getInstance()
    setToolbar()
    mView.radioGroup.setOnCheckedChangeListener { group, checkedId ->
        // checkedId is the RadioButton selected
        if(checkedId!=-1) {
            mView.txtTranporter.text =
               "Select"
            transporterID = 0
            val rb = mView.findViewById(checkedId) as RadioButton
            when (rb.text) {
                mView.resources.getString(R.string.truck) -> {
                    transportModeID = 3
                }
                mView.resources.getString(R.string.wagon) -> {
                    transportModeID = 1
                }
                mView.resources.getString(R.string.barge) -> {
                    transportModeID = 17
                }

            }

            vehicleID = 0
            mView.txtVehicleNo.text =  "Select"
            if (mUtils.checkInternetConnection(mView.context)) {
                getTranspoterByTransportMode(transportModeID.toString())
            }else{
                if (transporterList?.size != 0) {
                                    if (transporterList?.size == 1) {
                                        view?.txtTranporter?.text =
                                                transporterList?.get(0)?.optionName
                                        transporterID = transporterList?.get(0)?.optionValue!!
                                        view?.txtTranporter?.requestFocus()
                                        transporterFirstCount++
                                        if (transporterFirstCount == 1) {
                                            if (action.equals(
                                                            Constants.action_edit,
                                                            ignoreCase = true
                                                    )
                                            ) {
                                                if (commigFrom.equals(
                                                                Constants.header,
                                                                ignoreCase = true
                                                        )
                                                ) {
                                                    mView.txtTranporter.text =
                                                            headerModel.transporterName
                                                    transporterID = headerModel?.transporterId!!
                                                } else {
                                                    mView.txtTranporter.text =
                                                            todaysHistoryModel?.transporterName
                                                    transporterID =
                                                            todaysHistoryModel?.transporterId!!
                                                }
                                            }
                                        }
                                    }
                                }
            }

        }
    }
    setupClickListner()
    mView.swipeHeader.setOnRefreshListener{
        if (mUtils.checkInternetConnection(mView.context)) {
            ResetAllFeilds()
            mView.swipeHeader.isRefreshing = false
            getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
            mView.txtBordero_No.clearFocus()
        }else{
            ResetAllFeilds()
            offlineData()
            mView.swipeHeader.isRefreshing = false
            mView.txtBordero_No.clearFocus()
        }
    }

   /*mView.txtVehicleNo?.addTextChangedListener(object : TextWatcher {

        override fun afterTextChanged(s: Editable) {}

        override fun beforeTextChanged(s: CharSequence, start: Int,
                                       count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int,
                                   before: Int, count: Int) {
            if(!s.isNullOrEmpty()) {
               mView.txtVehicleNo?.setText(mView?.txtVehicleNo?.getText().toString().toUpperCase());
            }
        }
    })*/
         if (mUtils.checkInternetConnection(mView.context)) {
             getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
         }else{
            if (action.equals(Constants.action_edit, ignoreCase = true)) {
                    bindHeaderData(headerModel, todaysHistoryModel, commigFrom)
            }
         }

         if (!mUtils.checkInternetConnection(mView.context)) {
             offlineData()
         }


}

    fun generateAddBodereuRequest():AddBodereuReq{

       var request : AddBodereuReq =   AddBodereuReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id).toInt())
        request.setSupplier(forestID)
        request.setBordereauHeaderId(headerID)

        Log.e("headerID","is "+headerID)
        request.setSupplierLocation(SharedPref.getUserId(Constants.user_location_id).toInt())
        request.setModeOfTransport(transportModeID)
        request.setTransporterID(transporterID)
        request.setBordereauNo(txtBordero_No.text.toString())
        request.setBordereauDate(selectedDate)
        request.setLeauChargementId(leude_chargementID)
        request.setMode("Save")
        request.setOriginID(originID)
        request.setDestination(mView.txtDestination.text.toString())
        //request.setAacId(aacID)
        request.setFscId(fscID)
        request.setSpeciesId(0)
        request.setTimezoneId("Asia/Kolkata")//TimeZone.getDefault().getDisplayName()
        request.setWagonNo(txtVehicleNo.text.toString()?.toUpperCase())
        request.setWagonId(vehicleID)
        request.setRequestReceivedDate(mUtils?.getCurrentDate())

        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    fun generateValidateBodereuNoRequest(): ValidateBodereueNoReq {

        var request : ValidateBodereueNoReq =   ValidateBodereueNoReq()
        request.setBordereauNo(mView.txtBordero_No.text.toString())
        request.setBordereauDate(mView.txtDate.text.toString())
        request.setSupplier(forestID)


        var json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    fun setupClickListner(){
        mView.ivNext.setOnClickListener(this)
        mView.linForest.setOnClickListener(this)
        mView.linOriginCFAT.setOnClickListener(this)
        mView.linDestination.setOnClickListener(this)
        mView.linTranporter.setOnClickListener(this)
        mView.linAAC.setOnClickListener(this)
      //  mView.linDistance.setOnClickListener(this)
        mView.linFSC.setOnClickListener(this)
        mView.linVehicleNo.setOnClickListener(this)
        mView.linLeude_chargement.setOnClickListener(this)
        mView.linDate.setOnClickListener(this)

    }

    fun datePicker(txtView: TextView){
        val now = Calendar.getInstance()
        val datePickerDialog =
            DatePickerDialog(
                    mView.context,
                    object :
                            DatePickerDialog.OnDateSetListener {
                        @SuppressLint("SetTextI18n")
                        override fun onDateSet(
                                view: DatePicker?, year: Int,
                                monthOfYear: Int, dayOfMonth: Int
                        ) {

                            presectedCalender.set(Calendar.YEAR, year);
                            presectedCalender.set(Calendar.MONTH, monthOfYear);
                            presectedCalender.set(Calendar.DAY_OF_MONTH, dayOfMonth)


                            txtView.setText(mUtils.dateFormater("dd-MM-yyyy", "dd.MM.yyyy", dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year))
                            selectedDate = mUtils.dateFormater("dd-MM-yyyy", "dd.MM.yyyy", dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)!!

                        }
                    }, presectedCalender.get(Calendar.YEAR), presectedCalender.get(Calendar.MONTH), presectedCalender.get(
                    Calendar.DAY_OF_MONTH)
            )
        datePickerDialog.getDatePicker().setMaxDate(Date().time)
        datePickerDialog.show()
    }


    fun isValidateForm():Boolean{
        if(transportModeID==0){
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_transport_mode))
            return false
        }
        else if(view?.txtDate?.text=="Select"){
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_date))
            return false
        }
        else if(view?.txtBordero_No?.text.isNullOrEmpty()){
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_enter_bordereu_no))
            return false
        }
       else  if(view?.txtForest?.text=="Select"){
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_forest))
            return false
        }else if(view?.txtOriginCFAT?.text=="Select"){
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_origin))
            return false
        }
        else if(view?.txtFSC?.text=="Select"){
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_fsc))
            return false
        }
        else if(view?.txtLeude_chargement?.text=="Select"){
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_leuchargement))
            return false
        }
        else if(view?.txtDestination?.text=="Select"){
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_destination))
            return false
        }
        else if(view?.txtTranporter?.text=="Select"){
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_transporter))
            return false
        }
        /*else if(view?.txtAAC?.text=="Select"){
            mUtils.showAlert(activity, "Please select AAC")
            return false
        }*/

        else if(view?.txtDistance?.text=="Select"){
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_distance))
            return false
        }

       /* else if(view?.txtVehicleNo?.text=="Select"){
            mUtils.showAlert(activity, "Please select vehicle number")
            return false
        }*/
        else if(view?.txtVehicleNo?.text=="Select"){
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_vehicle_number))
            return false
        }

      return true;
    }
    fun clearSelection(){

         forestID  = 0
          aacID = 0
         originID=0
          fscID = 0
          destinationID = 0
         vehicleID=0
          distionsID= 0
         leude_chargementID  = 0

        mView.txtForest.text = "Select"
        mView.txtOriginCFAT.text = "Select"
        mView.txtDestination.text = "Select"
        mView.txtAAC.text = "Select"
        mView.txtDistance.text = "Select"
        mView.txtFSC.text = "Select"
        mView.txtLeude_chargement.text = "Select"
       mView.txtVehicleNo?.setText("Select")

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun ResetAllFeilds(){
        forestID  = 0
        aacID = 0
        originID=0
        fscID = 0
        destinationID = 0
        vehicleID=0
        distionsID= 0
        transporterID = 0
        leude_chargementID  = 0
        headerID = 0
        mView.txtForest.text = "Select"
        mView.txtOriginCFAT.text = "Select"
        mView.txtDestination.text = "Select"
        mView.txtAAC.text = "Select"
        mView.txtDistance.text = "Select"
        mView.txtTranporter.text = "Select"
        mView.txtFSC.text = "Select"
        mView.txtLeude_chargement.text = "Select"
        mView.txtVehicleNo?.setText("Select")
        mView.txtDate.text = "Select"
        mView.txtBordero_No.setText("")
        mView.radioGroup?.clearCheck()
        mView.chkTruck.isChecked=true
        transportModeID = 3
       // forestList?.clear()
        transporterList?.clear()
      //  commonForestMaster  = GetForestDataRes()
        enabledNonEditableField()

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun enabledNonEditableField(){
        mView.txtBordero_No.isEnabled = true
        mView.linDate.isEnabled = true
        mView.txtDate.isEnabled = true
        mView.txtForest.isEnabled = true
        mView.linForest.isEnabled = true

        mView.txtBordero_No.backgroundTintList =
            mView.context.resources.getColorStateList(R.color.white)
        mView.linDate.setBackgroundResource(R.drawable.bg_for_editest)
        mView.linBordero_No.setBackgroundResource(R.drawable.bg_for_editest)
        mView.linForest.setBackgroundResource(R.drawable.bg_for_editest)
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.bordereau_module)
        (activity as HomeActivity).invisibleFilter()
    }



    fun getForestMasterReqquest(forestId: String, userLocationId: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setForestId(forestId)
        request.setUserLocationId(userLocationId)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }

    fun getForestMasterReqquest(leauChargementId: Int): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.leauChargementId = leauChargementId
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun callingForestMasterAPI(forestId: String, userLocationId: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
               val request  =  getForestMasterReqquest(forestId, userLocationId)
                val call_api: Call<GetForestDataRes> =
                    apiInterface.getMasterDataByForestID(request)
                call_api.enqueue(object :
                        Callback<GetForestDataRes> {
                    override fun onResponse(
                            call: Call<GetForestDataRes>,
                            response: Response<GetForestDataRes>
                    ) {
                        mUtils.dismissProgressDialog()
                        try {
                            if (response.code() == 200) {
                                val responce: GetForestDataRes =
                                        response.body()!!
                                if (responce != null) {
                                    if (responce.getSeverity() == 200) {
                                        commonForestMaster = responce
                                        if (!commonForestMaster?.getOriginMaster().isNullOrEmpty()) {
                                            if (commonForestMaster?.getOriginMaster()?.size == 1) {
                                                originList!!.clear()
                                                originList!!.addAll((commonForestMaster?.getOriginMaster() as ArrayList<SupplierDatum?>?)!!)

                                                view?.txtOriginCFAT?.text =
                                                        commonForestMaster?.getOriginMaster()
                                                                ?.get(0)?.optionName
                                                originID =
                                                        commonForestMaster?.getOriginMaster()?.get(0)?.optionValue!!
                                                view?.txtOriginCFAT?.requestFocus()
                                            }else{
                                                originList!!.clear()
                                                originList!!.addAll((commonForestMaster?.getOriginMaster() as ArrayList<SupplierDatum?>?)!!)


                                            }
                                        }
                                        if (!commonForestMaster?.getFscMasterData().isNullOrEmpty()) {
                                            if (commonForestMaster?.getFscMasterData()?.size == 1) {

                                                fscList!!.clear()
                                                fscList!!.addAll((commonForestMaster?.getFscMasterData() as ArrayList<SupplierDatum?>?)!!)

                                                view?.txtFSC?.text =
                                                        commonForestMaster?.getFscMasterData()?.get(0)
                                                                ?.optionName
                                                fscID = commonForestMaster?.getFscMasterData()?.get(0)?.optionValue!!
                                                view?.txtFSC?.requestFocus()
                                            }else{
                                                fscList!!.clear()
                                                fscList!!.addAll((commonForestMaster?.getFscMasterData() as ArrayList<SupplierDatum?>?)!!)

                                            }
                                        }
                                        if (!commonForestMaster?.getLeauChargementData().isNullOrEmpty()) {
                                            if (commonForestMaster?.getLeauChargementData()?.size == 1) {

                                                lechargemetList!!.clear()
                                                lechargemetList!!.addAll((commonForestMaster?.getLeauChargementData() as ArrayList<SupplierDatum?>?)!!)

                                                view?.txtLeude_chargement?.text = commonForestMaster?.getLeauChargementData()
                                                        ?.get(0)
                                                        ?.optionName
                                                leude_chargementID =
                                                        commonForestMaster?.getLeauChargementData()?.get(
                                                                0
                                                        )?.optionValue!!
                                                view?.txtDistance?.text =
                                                        commonForestMaster?.getLeauChargementData()?.get(
                                                                0
                                                        )?.optionValueString!!
                                                view?.txtLeude_chargement?.requestFocus()
                                                view?.txtLeude_chargement?.requestFocus()
                                            }else{
                                                lechargemetList!!.clear()
                                                lechargemetList!!.addAll((commonForestMaster?.getLeauChargementData() as ArrayList<SupplierDatum?>?)!!)

                                            }
                                        }

                                        if (commonForestMaster?.getSupplierData() != null) {
                                            // mView.txtDestination.text  = commonForestMaster?.getSupplierData()?.finalDestination
                                            mView.txtDestination.text =
                                                    commonForestMaster?.getSupplierData()
                                                            ?.finalDestination
                                        }

                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }
                            } else if (response.code() == 306) {
                                mUtils.alertDialgSession(mView.context, activity)
                            } else {
                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                            call: Call<GetForestDataRes>,
                            t: Throwable
                    ) {
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }

    private fun getVehicleMasterAPI(leauChargementId: Int) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
               val request  =  getForestMasterReqquest(leauChargementId)
                val call_api: Call<List<SupplierDatum?>?> =
                    apiInterface.getMasterDataForVehicle(request)
                call_api.enqueue(object :
                        Callback<List<SupplierDatum?>?> {
                    override fun onResponse(
                            call: Call<List<SupplierDatum?>?>,
                            response: Response<List<SupplierDatum?>?>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                val responce: List<SupplierDatum?>? =
                                        response.body()!!
                                if (responce != null) {
                                    if (responce != null) {
                                        vehicleMaster!!.addAll( responce)
                                    }

                                    if(responce.size==0){
                                        view?.txtVehicleNo?.setText("Select")
                                    }
                                }
                            } else {
                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                            call: Call<List<SupplierDatum?>?>,
                            t: Throwable
                    ) {
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }

    fun getTranspoterByTransportModeReequest(transportMode: String):CommonRequest{
        var request : CommonRequest =   CommonRequest()
        request.seTransportModeID(transportMode)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun getTranspoterByTransportMode(transportMode: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request  = getTranspoterByTransportModeReequest(transportMode)
                val call_api: Call<List<SupplierDatum>> =
                    apiInterface.GetTransporterDataByMode(request)
                call_api.enqueue(object :
                        Callback<List<SupplierDatum>> {
                    override fun onResponse(
                            call: Call<List<SupplierDatum>>,
                            response: Response<List<SupplierDatum>>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: List<SupplierDatum> =
                                    response.body()!!
                            if (responce != null) {
                                transporterList?.clear()
                                transporterList?.addAll(responce)
                                if (transporterList?.size != 0) {
                                    if (transporterList?.size == 1) {
                                        view?.txtTranporter?.text =
                                                transporterList?.get(0)?.optionName
                                        transporterID = transporterList?.get(0)?.optionValue!!
                                        view?.txtTranporter?.requestFocus()
                                        transporterFirstCount++
                                        if (transporterFirstCount == 1) {
                                            if (action.equals(
                                                            Constants.action_edit,
                                                            ignoreCase = true
                                                    )
                                            ) {
                                                if (commigFrom.equals(
                                                                Constants.header,
                                                                ignoreCase = true
                                                        )
                                                ) {
                                                    mView.txtTranporter.text =
                                                            headerModel.transporterName
                                                    transporterID = headerModel?.transporterId!!
                                                } else {
                                                    mView.txtTranporter.text =
                                                            todaysHistoryModel?.transporterName
                                                    transporterID =
                                                            todaysHistoryModel?.transporterId!!
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                            call: Call<List<SupplierDatum>>,
                            t: Throwable
                    ) {
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }


    fun getForestByLocationRequest(userLocationID: String):CommonRequest{
        var request : CommonRequest =   CommonRequest()
        request.setUserLocationId(userLocationID)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }
    private fun getForestDataByLocation(userLocationID: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val request  =   getForestByLocationRequest(userLocationID)
                val call_api: Call<List<SupplierDatum>> =
                    apiInterface.getForestDataByLocation(request)
                call_api.enqueue(object :
                        Callback<List<SupplierDatum>> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                            call: Call<List<SupplierDatum>>,
                            response: Response<List<SupplierDatum>>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: List<SupplierDatum> =
                                    response.body()!!
                            if (responce != null) {
                                forestList?.clear()
                                forestList?.addAll(responce)
                                if (action.equals(Constants.action_edit, ignoreCase = true)) {
                                    bindHeaderData(headerModel, todaysHistoryModel, commigFrom)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                            call: Call<List<SupplierDatum>>,
                            t: Throwable
                    ) {
                        t.printStackTrace()
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }

    private fun callingValidateBordereauNoAPI() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateValidateBodereuNoRequest()
                val call: Call<AddBodereuRes> =
                    apiInterface.validateBordereauNo(request)
                call.enqueue(object :
                        Callback<AddBodereuRes> {
                    override fun onResponse(
                            call: Call<AddBodereuRes>,
                            response: Response<AddBodereuRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response != null) {
                                    if (response.body().getSeverity() == 200) {
                                        callingAddBordereauAPI()

                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }

                            } else if (response.code() == 306) {
                                mUtils.alertDialgSession(mView.context, activity)
                            } else {
                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                            call: Call<AddBodereuRes>,
                            t: Throwable
                    ) {
                        mUtils.showToast(activity, Constants.SERVERTIMEOUT)
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            mUtils.showToast(view?.context, getString(R.string.no_internet))
        }
    }

    private fun callingAddBordereauAPI() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateAddBodereuRequest()
                val call: Call<AddBodereuRes> =
                    apiInterface.addBordereau(request)
                call.enqueue(object :
                        Callback<AddBodereuRes> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                            call: Call<AddBodereuRes>,
                            response: Response<AddBodereuRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {
                                if (response != null) {
                                    if (response.body().getSeverity() == 200) {
                                        mUtils.showToast(activity, response.body().getMessage())
                                        val myactivity = activity as HomeActivity
                                        var prductFragment = LogsListingFragment()
                                        /*    response.body().getBordereauResponse()?.supplierLocationName = txtOriginCFAT.text.toString()*/
                                        val bundle = Bundle()
                                        bundle.putSerializable(
                                                Constants.badereuModel,
                                                response.body().getBordereauResponse()
                                        );
                                        bundle.putString(
                                                Constants.comming_from,
                                                Constants.header
                                        );
                                        prductFragment.arguments = bundle
                                        myactivity?.replaceFragment(prductFragment, false)
                                        ResetAllFeilds()
                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }

                            } else if (response.code() == 306) {
                                mUtils.alertDialgSession(mView.context, activity)
                            } else {
                                mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                            call: Call<AddBodereuRes>,
                            t: Throwable
                    ) {
                        mUtils.showToast(activity, Constants.SERVERTIMEOUT)
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }else{
            mUtils.showToast(view?.context, getString(R.string.no_internet))
        }
    }


//    private fun callingGetTransporterDataByMode(transportModeID:String) {
//        if (mUtils.checkInternetConnection(mView.context)) {
//            try {
//                mUtils.showProgressDialog(mView.context)
//                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
//
//                val call_api: Call<List<SupplierDatum>> =
//                    apiInterface.getTransporterDataByMode("3")
//                call_api.enqueue(object :
//                    Callback<List<SupplierDatum>> {
//                    override fun onResponse(
//                        call: Call<List<SupplierDatum>>,
//                        response: Response<List<SupplierDatum>>
//                    ) {
//                        mUtils.dismissProgressDialog()
//
//                        try {
//                            val responce: List<SupplierDatum> =
//                                response.body()!!
//                            if (responce != null) {
//
//                            }
//                        }catch (e:Exception){
//                            e.printStackTrace()
//                        }
//                    }
//
//                    override fun onFailure(
//                        call: Call<List<SupplierDatum>>,
//                        t: Throwable
//                    ) {
//                        mUtils.dismissProgressDialog()
//                    }
//                })
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }else{
//            mUtils.showToast(mView.context, getString(R.string.no_internet))
//        }
//    }


    //Dialog content
    open fun showDialog(countryListSearch: ArrayList<SupplierDatum?>?, action: String) {

        try {
            if (!countryListSearch.isNullOrEmpty()) {
                Collections.sort(
                        countryListSearch,
                        Comparator<SupplierDatum?> { contactOne, contactSecond ->
                            contactOne?.optionName!!.toLowerCase().compareTo(
                                    contactSecond?.optionName!!.toLowerCase()
                            )
                        })
            }
        }catch (e: Exception){
        }


        val fm: FragmentManager
        val bundle: Bundle
        fm = childFragmentManager
        countryDialogFragment = DialogFragment(AddHeaderFragment@ this)
        bundle = Bundle()
        bundle.putSerializable("COUNTRY_LIST", countryListSearch)
        countryDialogFragment.setArguments(bundle)
        bundle.putBoolean("isCountryCode", false)
        bundle.putString("action", action)
        countryDialogFragment.show(fm, "COUNTRY_FRAGMENT")
        countryDialogFragment.isCancelable = false
        isDialogShowing = true
    }


    override fun onSubmitData(model: SupplierDatum?, isCountryCode: Boolean, action: String) {

        try {
            countryDialogFragment.dismiss()
            isDialogShowing = false
            mView.txtBordero_No.clearFocus()
        }catch (e: Exception){
            e.printStackTrace()
        }
        if (model != null) {
            try{
                when (action) {
                    "forest" -> {
                        clearSelection()
                        view?.txtForest?.text = model.optionName
                        forestID = model.optionValue!!
                        view?.txtForest?.requestFocus()
                        if (!mUtils.checkInternetConnection(mView.context)) {
                            offlineForestSelection(false)
                        }else {
                            callingForestMasterAPI(forestID.toString(), SharedPref.getUserId(Constants.user_location_id).toString())
                        }
                    }
                    "origin" -> {
                        view?.txtOriginCFAT?.text = model.optionName
                        originID = model.optionValue!!
                        view?.txtOriginCFAT?.requestFocus()
                    }
                    "destination" -> {
                        /*  view?.txtDestination?.text =   model.optionName
                        destinationID  = model.optionValue!!*/

                    }
                    "transporter" -> {
                        view?.txtTranporter?.text = model.optionName
                        transporterID = model.optionValue!!
                        view?.txtTranporter?.requestFocus()

                    }
                    "aac" -> {
                        view?.txtAAC?.text = model.optionName
                        aacID = model.optionValue!!
                        view?.txtAAC?.requestFocus()

                    }
                    "fsc" -> {
                        view?.txtFSC?.text = model.optionName
                        fscID = model.optionValue!!
                        view?.txtFSC?.requestFocus()
                    }
                    "vehicle" -> {
                        view?.txtVehicleNo?.text = model.optionName
                        vehicleID = model.optionValue!!
                        view?.txtVehicleNo?.requestFocus()
                    }
                    "chargement" -> {
                        view?.txtLeude_chargement?.text = model.optionName
                        leude_chargementID = model.optionValue!!
                        view?.txtDistance?.text = model.optionValueString
                        view?.txtLeude_chargement?.requestFocus()

                        vehicleMaster!!.clear()
                        if (!mUtils.checkInternetConnection(mView.context)) {
                            realm.executeTransactionAsync { bgRealm ->
                                val vehicleDatum: RealmResults<VehicleDatum> = bgRealm.where(VehicleDatum::class.java).equalTo("uid",leude_chargementID ).findAll()
                                Log.e("LeauChargementDatum","is "+ vehicleDatum.size)
                                for(transpoter in vehicleDatum){
                                    var transporterDatum=SupplierDatum()
                                    transporterDatum.optionValueString=transpoter.optionValueString
                                    transporterDatum.optionValue=transpoter.optionValue
                                    transporterDatum.optionName=transpoter.optionName
                                    vehicleMaster!!.add(transporterDatum)
                                }

                            }
                            view?.txtVehicleNo?.setText("Select")
                        }else {
                            getVehicleMasterAPI(leude_chargementID)
                        }

                    }
                }

            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }


    fun offlineForestSelection(isEdit:Boolean){
        fscList!!.clear()
        lechargemetList!!.clear()
        originList!!.clear()
        destinationList!!.clear()
        moreSupplierList!!.clear()
        val locationid=SharedPref.getUserId(Constants.user_location_id)

        realm.executeTransactionAsync { bgRealm ->
            val originMaster: RealmResults<OriginMaster> = bgRealm.where(OriginMaster::class.java).equalTo("uid",forestID).findAll()
            Log.e("OriginMaster","is "+originMaster.size)
            for(transpoter in originMaster){
                var transporterDatum=SupplierDatum()
                transporterDatum.optionValueString=transpoter.optionValueString
                transporterDatum.optionValue=transpoter.optionValue
                transporterDatum.optionName=transpoter.optionName
                originList?.add(transporterDatum)
            }
            if(!isEdit) {
                if (originList!!.size == 1) {
                    view?.txtOriginCFAT?.text =
                        originList?.get(0)
                            ?.optionName
                    originID = originList?.get(0)?.optionValue!!
                    view?.txtOriginCFAT?.requestFocus()
                }
            }
        }

        realm.executeTransactionAsync { bgRealm ->
            val fscMasterDatum: RealmResults<FscMasterDatum> = bgRealm.where(FscMasterDatum::class.java).equalTo("uid",forestID ).findAll()
            Log.e("FscMasterDatum","is "+fscMasterDatum.size)
            for(transpoter in fscMasterDatum){
                var transporterDatum=SupplierDatum()
                transporterDatum.optionValueString=transpoter.optionValueString
                transporterDatum.optionValue=transpoter.optionValue
                transporterDatum.optionName=transpoter.optionName
                fscList?.add(transporterDatum)
            }
            if(!isEdit) {
                if (fscList!!.size == 1) {
                    view?.txtFSC?.text =
                        fscList?.get(0)
                            ?.optionName
                    fscID = fscList?.get(0)?.optionValue!!
                    view?.txtFSC?.requestFocus()
                }
            }
        }

        realm.executeTransactionAsync { bgRealm ->
            val leauChargementDatum: RealmResults<LeauChargementDatum> = bgRealm.where(LeauChargementDatum::class.java).equalTo("uid",locationid ).findAll()
            Log.e("leauChargementDatum","is "+leauChargementDatum.size)
            for(transpoter in leauChargementDatum){
                var transporterDatum=SupplierDatum()
                transporterDatum.optionValueString=transpoter.optionValueString
                transporterDatum.optionValue=transpoter.optionValue
                transporterDatum.optionName=transpoter.optionName
                lechargemetList?.add(transporterDatum)
            }
            if(!isEdit) {
                if (lechargemetList!!.size == 1) {
                    view?.txtLeude_chargement?.text = lechargemetList?.get(0)
                        ?.optionName
                    leude_chargementID =
                        lechargemetList?.get(
                            0
                        )?.optionValue!!
                    view?.txtDistance?.text =
                        lechargemetList?.get(
                            0
                        )?.optionValueString!!
                    view?.txtLeude_chargement?.requestFocus()
                    view?.txtLeude_chargement?.requestFocus()
                }
            }
        }

        val moreSupplierInfo: MoreSupplierInfo = realm.where(MoreSupplierInfo::class.java).equalTo("supplierId",forestID ).findFirst()!!
        mView.txtDestination.text = moreSupplierInfo!!.finalDestination

    }

    override fun onCancleDialog() {
        try {
            countryDialogFragment.dismiss()
            isDialogShowing = false
        }catch (e: Exception){
            e.printStackTrace()

        }
    }


    @SuppressLint("UseRequireInsteadOfGet")
    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.linForest -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(forestList, "forest")
            }
            R.id.linOriginCFAT -> {
                if (checkIsFragmentDialogAlreadyShowing()) {
                    showDialog(originList, "origin")
                }
            }
            R.id.linDestination -> {
                /*  if(checkIsFragmentDialogAlreadyShowing())
                showDialog(commonForestMaster?.getTransDistanceData() as ArrayList<SupplierDatum?>?,"destination")*/
            }
            R.id.linTranporter -> {
                if (transporterList?.size != 0) {
                    if (checkIsFragmentDialogAlreadyShowing())
                        showDialog(transporterList, "transporter")
                } else {
                    mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_transport_mode))
                }
            }
            R.id.linAAC -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(commonForestMaster?.getAacList() as ArrayList<SupplierDatum?>?, "aac")
            }
           /* R.id.linDistance->{
               // showDialog(supplierLocationDataList,"distance")
            }*/
            R.id.linFSC -> {
                showDialog(fscList, "fsc")
            }
            R.id.linVehicleNo -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(vehicleMaster, "vehicle")
            }
            R.id.linLeude_chargement -> {
                if (checkIsFragmentDialogAlreadyShowing()) {

                    showDialog(lechargemetList, "chargement")

                }
            }
            R.id.linDate -> {
                datePicker(txtDate)
            }
            R.id.ivNext -> {
                if (isValidateForm()) {

                        if (action.equals(Constants.action_edit, ignoreCase = true)) {
                            if (mUtils.checkInternetConnection(mView.context)) {
                                callingAddBordereauAPI()
                            }else{
                                var borderauRequest=BorderauRequest()
                                borderauRequest.uniqueId=uniqueId
                                borderauRequest.bordereauDate=mView.txtDate.text.toString()
                                borderauRequest.bordereauNo=mView.txtBordero_No.text.toString()
                                borderauRequest.forestId=forestID
                                borderauRequest.forestName=mView.txtForest.text.toString()
                                borderauRequest.originId=originID
                                borderauRequest.originName=mView.txtOriginCFAT.text.toString()
                                borderauRequest.fscId=fscID
                                borderauRequest.fscName=mView.txtFSC.text.toString()
                                borderauRequest.leudechargementId=leude_chargementID
                                borderauRequest.chargementName=mView.txtLeude_chargement.text.toString()
                                borderauRequest.transpoterId=transportModeID
                                borderauRequest.transpoterName=mView.txtTranporter.text.toString()

                                borderauRequest.distance=mView.txtDistance.text.toString()
                                borderauRequest.headerID=headerID
                                borderauRequest.userID=SharedPref.getUserId(Constants.user_id)
                                borderauRequest.supplierLocation=SharedPref.getUserId(Constants.user_location_id)
                                borderauRequest.modeOfTransport=transportModeID
                                borderauRequest.mode="Save"
                                borderauRequest.destination=mView.txtDestination.text.toString()
                                borderauRequest.speciesId=0
                                borderauRequest.wagonNo=txtVehicleNo.text.toString()?.toUpperCase()
                                borderauRequest.vehicalId=vehicleID
                                borderauRequest.currentDate=mUtils.getCurrentDate()

                                realm.executeTransactionAsync({ bgRealm ->
                                    bgRealm.copyToRealmOrUpdate(borderauRequest)
                                }, {
                                    mUtils.showAlert(requireActivity(),getString(R.string.bordereau_update_successful))
                                    requireActivity().onBackPressed()
                                    Log.e("Success","Success")
                                }) {
                                    Log.e("faile","faile")
                                }
                            }
                        } else {
                            if (mUtils.checkInternetConnection(mView.context)) {
                                callingValidateBordereauNoAPI()
                            }else{
                                var borderauRequest=BorderauRequest()
                                borderauRequest.uniqueId=System.currentTimeMillis().toString()
                                borderauRequest.headerStatus="Draft"
                                borderauRequest.bordereauDate=mView.txtDate.text.toString()
                                borderauRequest.bordereauNo=mView.txtBordero_No.text.toString()
                                borderauRequest.forestId=forestID
                                borderauRequest.forestName=mView.txtForest.text.toString()
                                borderauRequest.originId=originID
                                borderauRequest.originName=mView.txtOriginCFAT.text.toString()
                                borderauRequest.fscId=fscID
                                borderauRequest.fscName=mView.txtFSC.text.toString()
                                borderauRequest.leudechargementId=leude_chargementID
                                borderauRequest.chargementName=mView.txtLeude_chargement.text.toString()
                                borderauRequest.transpoterId=transportModeID
                                borderauRequest.transpoterName=mView.txtTranporter.text.toString()
                                borderauRequest.distance=mView.txtDistance.text.toString()
                                borderauRequest.headerID=headerID
                                borderauRequest.userID=SharedPref.getUserId(Constants.user_id)
                                borderauRequest.supplierLocation=SharedPref.getUserId(Constants.user_location_id)
                                borderauRequest.modeOfTransport=transportModeID
                                borderauRequest.mode="Save"
                                borderauRequest.destination=mView.txtDestination.text.toString()
                                borderauRequest.speciesId=0
                                borderauRequest.wagonNo=txtVehicleNo.text.toString()?.toUpperCase()
                                borderauRequest.vehicalId=vehicleID
                                borderauRequest.currentDate=mUtils.getCurrentDate()
                                borderauRequest.recordDocNo=mView.txtForest.text.toString()+" "+mView.txtBordero_No.text.toString()

                                realm.executeTransactionAsync({ bgRealm ->
                                    bgRealm.copyToRealmOrUpdate(borderauRequest)
                                }, {
                                    mUtils.showAlert(requireActivity(),getString(R.string.bordereau_successful))
                                    requireActivity().onBackPressed()
                                    Log.e("Success","Success")
                                }) {
                                    Log.e("faile","faile")
                                }
                            }


                        }
                }

            }



        }
    }

    fun  checkIsFragmentDialogAlreadyShowing():Boolean{
           if(isDialogShowing){
               return false
           }
        return true
    }


}
