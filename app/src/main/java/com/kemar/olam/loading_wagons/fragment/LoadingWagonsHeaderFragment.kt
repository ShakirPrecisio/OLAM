package com.kemar.olam.loading_wagons.fragment

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
import com.kemar.olam.dashboard.models.responce.DestinationListDatum
import com.kemar.olam.dashboard.models.responce.GetForestDataRes
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.loading_wagons.model.request.AddLoadingBordereueHeaderReq
import com.kemar.olam.loading_wagons.model.request.LoadingRequest
import com.kemar.olam.loading_wagons.model.request.getBorderuaSerialNoReq
import com.kemar.olam.loading_wagons.model.responce.getBorderuaSerialNoRes
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.offlineData.requestbody.BorderauRequest
import com.kemar.olam.offlineData.response.LeauChargementUserDatum
import com.kemar.olam.offlineData.response.MoreSupplierInfo
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.*
import com.lp.lpwms.ui.offline.response.*
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_loading_wagons_header.*
import kotlinx.android.synthetic.main.fragment_loading_wagons_header.view.*
import kotlinx.android.synthetic.main.item_physical_code.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class LoadingWagonsHeaderFragment : Fragment(), DialogFragment.GetDialogListener,
    View.OnClickListener {

    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var countryDialogFragment: DialogFragment
    lateinit var presectedCalender: Calendar
    var forestID: Int = 0
    var headerID: Int = 0
    var aacID: Int = 0
    var originID: Int = 0
    var fscID: Int = 0
    var destinationID: Int = 0
    var transporterID: Int = 0
    var vehicleID:Int?=0
    var distionsID: Int = 0
    var leude_chargementID: Int = 0
    var transportModeID: Int = 0
    var selectedDate: String = ""
    var commigFrom = ""
    var action = ""
    var finalDestination=""
    var headerModel: AddBodereuRes.BordereauResponse = AddBodereuRes.BordereauResponse()
    var todaysHistoryModel: LogsUserHistoryRes.BordereauRecordList =
        LogsUserHistoryRes.BordereauRecordList()

    //listings
//    var commonForestMaster: GetForestDataRes? = GetForestDataRes()
//    var forestList: java.util.ArrayList<SupplierDatum?>? = arrayListOf()
//    var transporterList: java.util.ArrayList<SupplierDatum?>? = arrayListOf()
    var isDialogShowing: Boolean = false
    var iselectronicBordereau: Boolean = false
    var transporterFirstCount = 0
    var strBordereuNo :String? = ""
    var strEBordereuNo :String? = ""



    lateinit var realm: Realm
    //listings
    var commonForestMaster : GetForestDataRes? = GetForestDataRes()
    var vehicleMaster : ArrayList<SupplierDatum?>? = arrayListOf()
    var forestList : ArrayList<SupplierDatum?>? = arrayListOf()
    var finalDestinationList : ArrayList<SupplierDatum?>? = arrayListOf()
    var fscList : ArrayList<SupplierDatum?>? = arrayListOf()
    var lechargemetList : ArrayList<SupplierDatum?>? = arrayListOf()
    var moreSupplierList : ArrayList<MoreSupplierInfo?>? = arrayListOf()
    var destinationList : ArrayList<SupplierDatum?>? = arrayListOf()
    var originList : ArrayList<SupplierDatum?>? = arrayListOf()
    var transporterList : ArrayList<SupplierDatum?>? = arrayListOf()


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
        mView = inflater.inflate(R.layout.fragment_loading_wagons_header, container, false)
        mUtils = Utility()
        init()
        return mView;
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun enabledNonEditableField(){
       mView.txtBordero_No.isEnabled = true
       mView.linDate.isEnabled = true
       mView.txtDate.isEnabled = true
        mView.txtForest.isEnabled = true
        mView.linForest.isEnabled = true
        mView.linBorderoType.visibility =  View.VISIBLE
        mView.txtBordero_No.backgroundTintList = mView.context.resources.getColorStateList(R.color.white)
       mView.linDate.setBackgroundResource(R.drawable.bg_for_editest)
       mView.linBordero_No.setBackgroundResource(R.drawable.bg_for_editest)
       mView.linForest.setBackgroundResource(R.drawable.bg_for_editest)
       mView.txtBoNumber.visibility = View.VISIBLE
       mView.txtBordero_No.visibility =  View.VISIBLE
       mView.linBordero_No.visibility =  View.VISIBLE
    }


    fun clearAllList(){
        forestList!!.clear()
        transporterList?.clear()
        vehicleMaster!!.clear()
        forestList!!.clear()
        fscList!!.clear()
        lechargemetList!!.clear()
        moreSupplierList!!.clear()
        destinationList!!.clear()
        transporterList!!.clear()

        ResetAllFeilds()
        offlineData()
        mView.swipeHeader.isRefreshing = false
        mView.txtBordero_No.clearFocus()
    }
    fun offlineData(){

        forestList!!.clear()
        transporterList?.clear()
        val locationid=SharedPref.getUserId(Constants.user_location_id)
        realm.executeTransactionAsync { bgRealm ->
            val supplierDatum: RealmResults<com.lp.lpwms.ui.offline.response.SupplierDatum> = bgRealm.where(com.lp.lpwms.ui.offline.response.SupplierDatum::class.java).equalTo("uid",locationid ).findAll()
            Log.e("forestList","is "+supplierDatum.size)
            for(supplierdatum in supplierDatum){
                var supplierDatum=SupplierDatum()
                supplierDatum.optionValueString=supplierdatum.optionValueString
                supplierDatum.optionValue=supplierdatum.optionValue
                supplierDatum.optionName=supplierdatum.optionName
                supplierDatum.finalDestination=supplierdatum.finalDestination
                forestList!!.add(supplierDatum)
                this.forestList = forestList?.distinctBy { it!!.optionValue } as java.util.ArrayList<SupplierDatum?>

            }
        }

        realm.executeTransactionAsync { bgRealm ->
            val transporterDatum: RealmResults<TransportModeDatum> = bgRealm.where(
                TransportModeDatum::class.java).equalTo("uid",transportModeID ).findAll()
            Log.e("TransportModeDatum","is "+transporterDatum.size)
            for(transpoter in transporterDatum){
                var transporterDatum=SupplierDatum()
                transporterDatum.optionValueString=transpoter.optionValueString
                transporterDatum.optionValue=transpoter.optionValue
                transporterDatum.optionName=transpoter.optionName
                transporterList?.add(transporterDatum)
                this.transporterList = transporterList?.distinctBy { it!!.optionValue } as java.util.ArrayList<SupplierDatum?>

            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun bindHeaderData(
        headerData: AddBodereuRes.BordereauResponse,
        todaysHistoryModel: LogsUserHistoryRes.BordereauRecordList,
        commingFrom: String
    ) {
        try {
           mView.txtBordero_No.isEnabled = false
           mView.linDate.isEnabled = false
           mView.txtDate.isEnabled = false
            mView.txtForest.isEnabled = false
            mView.linForest.isEnabled = false

            mView.txtBordero_No.backgroundTintList =
                mView.context.resources.getColorStateList(R.color.gray_200)
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
                if(headerData?.bordereauNo.isNullOrEmpty()){
                   mView.txtBordero_No.setText(headerData?.eBordereauNo)
                    strEBordereuNo = headerData?.eBordereauNo
                }else{
                    strBordereuNo = headerData?.bordereauNo
                   mView.txtBordero_No.setText(headerData?.bordereauNo)
                }
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
            //   mView.txtBordero_No.setText(todaysHistoryModel?.bordereauNo)

                if(todaysHistoryModel?.bordereauNo.isNullOrEmpty()){
                   mView.txtBordero_No.setText(todaysHistoryModel?.eBordereauNo)
                    strEBordereuNo = todaysHistoryModel?.eBordereauNo
                }else{
                    strBordereuNo = todaysHistoryModel?.bordereauNo
                   mView.txtBordero_No.setText(todaysHistoryModel?.bordereauNo)
                }

                selectedDate = todaysHistoryModel?.bordereauDateString?.toString()!!
               mView.txtForest.text = todaysHistoryModel?.supplierName
                forestID = todaysHistoryModel?.supplierId!!
               /* callingForestMasterAPI(
                    forestID.toString(),
                    SharedPref.getUserId(Constants.user_location_id).toString()
                )*/
                if (mUtils.checkInternetConnection(mView.context)) {
                    callingForestMasterAPI(forestID.toString(), SharedPref.getUserId(Constants.user_location_id).toString())
                }else{
                    offlineForestSelection(true)
                }

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
        } catch (e: Exception) {
            e.toString()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        action =
            arguments?.getString(Constants.action).toString();
        commigFrom =
            arguments?.getString(Constants.comming_from).toString();

        mView.txthintvehicleNo.text=getString(R.string.wagon_no)

        if (action.equals(Constants.action_edit, ignoreCase = true)) {
           mView.linBorderoType.visibility = View.GONE
            if (commigFrom.equals(Constants.header, ignoreCase = true)) {
                var headerDataModel: AddBodereuRes.BordereauResponse? =
                    arguments?.getSerializable(Constants.badereuModel) as AddBodereuRes.BordereauResponse
                if (headerDataModel != null) {
                    headerModel = headerDataModel
                }
            } else {
                var headerDataModel: LogsUserHistoryRes.BordereauRecordList? =
                    arguments?.getSerializable(Constants.badereuModel) as LogsUserHistoryRes.BordereauRecordList
                if (headerDataModel != null) {
                    todaysHistoryModel = headerDataModel
                }
            }
        } else {
           mView.linBorderoType.visibility = View.VISIBLE
           mView.chkWagon?.isChecked = true
            transportModeID = 1
            getTranspoterByTransportMode(transportModeID.toString())
        }

        presectedCalender = Calendar.getInstance()
        setToolbar()
        mView.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            // checkedId is the RadioButton selected
            if (checkedId != -1) {
               mView.txtTranporter.text = "Select"
                transporterID = 0
                val rb = mView.findViewById(checkedId) as RadioButton
                when (rb?.text) {
                    mView.resources.getString(R.string.truck) -> {
                        transportModeID = 3
                        mView.txthintvehicleNo.text=getString(R.string.truck_no)
                    }
                    mView.resources.getString(R.string.wagon) -> {
                        transportModeID = 1
                        mView.txthintvehicleNo.text=getString(R.string.wagon_no)
                    }
                    mView.resources.getString(R.string.barge) -> {
                        transportModeID = 17
                        mView.txthintvehicleNo.text=getString(R.string._barge_no)
                    }

                }

                vehicleID = 0
            //    mView.txtVehicleNo.text = "Select"
                if (mUtils.checkInternetConnection(mView.context)) {
                    getTranspoterByTransportMode(transportModeID.toString())
                }else {
                    offlineData()
                }
            }
        }

        mView.rgBordereu.setOnCheckedChangeListener { group, checkedId ->
            // checkedId is the RadioButton selected
            if (checkedId != -1) {
                val rb = mView.findViewById(checkedId) as RadioButton
                when (rb?.text) {
                    mView.resources.getString(R.string.electronic_bordereau) -> {
                       mView.linBordero_No.visibility = View.GONE
                        iselectronicBordereau = true
                       mView.txtBoNumber.visibility = View.GONE
                       mView.txtBordero_No.setText("")
                        /*   if (mView.txtForest.text.toString() == "Select") {
                               mUtils.showAlert(activity, "Please select forest")
                               mView.rgBordereu.clearCheck()

                           } else if (view?.txtDate?.text == "Select") {
                               mUtils.showAlert(activity, "Please select date")
                               mView.rgBordereu.clearCheck()
                           } else {
                               //creatting electronic borderiuea
                               if (action?.equals(Constants.action_non_edit, ignoreCase = true)) {
                                   if (!selectedDate.isNullOrEmpty() && forestID != 0) {
                                       callinGetBordereuSerialNumber()
                                   }
                               }
                           }*/
                    }
                    mView.resources.getString(R.string.manual_bordereau) -> {
                        iselectronicBordereau = false
                       mView.txtBoNumber.visibility = View.VISIBLE
                        mView.txtBordero_No.setText("")
                        enabledElectronicBorderueNo()

                    }


                }

            }
        }
        setupClickListner()
        mView.swipeHeader.setOnRefreshListener {
           /* ResetAllFeilds()
            mView.swipeHeader.isRefreshing = false
            getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
            mView.txtBordero_No.clearFocus()*/


            if (mUtils.checkInternetConnection(mView.context)) {
                ResetAllFeilds()
                mView.swipeHeader.isRefreshing = false
                getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
                getDestinationByLocation("-1")
                mView.txtBordero_No.clearFocus()
            }else{
                clearAllList()
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

        //getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())

        if (mUtils.checkInternetConnection(mView.context)) {
            getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
            getDestinationByLocation("-1")
        }else{
            if (action.equals(Constants.action_edit, ignoreCase = true)) {
                bindHeaderData(headerModel, todaysHistoryModel, commigFrom)
            }
        }

        if (!mUtils.checkInternetConnection(mView.context)) {
            offlineData()
        }

        if (mUtils.checkInternetConnection(mView.context)) {
            callingForestMasterAPI("", SharedPref.getUserId(Constants.user_location_id).toString())
        }else{
            offlineForestSelection(true)
        }
    }

    fun generateAddBodereuRequest(): AddLoadingBordereueHeaderReq {

        var request: AddLoadingBordereueHeaderReq = AddLoadingBordereueHeaderReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
//        request.setSupplier(forestID)
        request.setBordereauHeaderId(headerID)
        if(headerID==0){
            if(iselectronicBordereau){
                request.setElectronicBordereau(true)
//                request.setSupplierShortName(getSupplierShortName(mView.txtForest.text.toString()))
            }else{
//                request.setSupplierShortName(getSupplierShortName(mView.txtForest.text.toString()))
                request.setElectronicBordereau(false)
            }

        }else{
            request.setElectronicBordereau(false)
            request.setSupplierShortName("")
        }
        request.setSupplierLocation(SharedPref.getUserId(Constants.user_location_id))
        request.setModeOfTransport(transportModeID)
        request.setTransporterID(transporterID)
        //check if it is comming from edit or norml
        //if from edit then what we recieved will send
        //if from norml then if eBordereu select then both eBo n Manuial Bo send Empty else manual Bo send only
        if (action.equals(Constants.action_edit, ignoreCase = true)) {
            request.setBordereauNo(strBordereuNo)
            request.setE_BordereauNo(strEBordereuNo)
        }else {
            if(iselectronicBordereau){
                request.setBordereauNo("")
                request.setE_BordereauNo("")
            }else{
                request.setBordereauNo(mView.txtBordero_No.text.toString())
                request.setE_BordereauNo("")
            }
        }
        request.setBordereauDate(selectedDate)
        request.setLeauChargementId(leude_chargementID)
        request.setMode("Save")
//        request.setOriginID(originID)
        request.setDestination(mView.txtDestination.text.toString())
        //request.setAacId(aacID)
//        request.setFscId(fscID)
        request.setSpeciesId(0)
        request.setTimezoneId("Asia/Kolkata")//TimeZone.getDefault().getDisplayName()
        request.setWagonNo(txtVehicleNo.text.toString()?.toUpperCase())
        request.setWagonId(vehicleID)
        request.setRequestReceivedDate(mUtils?.getCurrentDate())

        var json = Gson().toJson(request)
        var test = json

        return request

    }

    fun generateGetSerialNumberRequest(): getBorderuaSerialNoReq {

        var request: getBorderuaSerialNoReq = getBorderuaSerialNoReq()
        request.setSupplier(forestID)
        request.setBordereauDate(selectedDate)
        var json = Gson().toJson(request)
        var test = json

        return request

    }


    fun generateValidateBodereuNoRequest(): ValidateBodereueNoReq {

        var request: ValidateBodereueNoReq = ValidateBodereueNoReq()
        request.setBordereauNo(mView.txtBordero_No.text.toString())
        request.setBordereauDate(mView.txtDate.text.toString())
        request.setSupplier(forestID)


        var json = Gson().toJson(request)
        var test = json

        return request

    }

    fun setupClickListner() {
        mView.ivNext.setOnClickListener(this)
        mView.linForest.setOnClickListener(this)
        mView.linOriginCFAT.setOnClickListener(this)
        mView.linDestination.setOnClickListener(this)
        mView.linTranporter.setOnClickListener(this)
        mView.linAAC.setOnClickListener(this)
        //  mView.linDistance.setOnClickListener(this)
        mView.linFSC.setOnClickListener(this)
        mView.linDestination.setOnClickListener(this)
       // mView.linVehicleNo.setOnClickListener(this)
        mView.linLeude_chargement.setOnClickListener(this)
        mView.linDate.setOnClickListener(this)

    }

    fun datePicker(txtView: TextView) {
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


                        txtView.setText(
                            mUtils.dateFormater(
                                "dd-MM-yyyy",
                                "dd.MM.yyyy",
                                dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                            )
                        )
                        selectedDate = mUtils.dateFormater(
                            "dd-MM-yyyy",
                            "dd.MM.yyyy",
                            dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year
                        )!!
                        /* if(action?.equals(Constants.action_non_edit, ignoreCase = true)) {
                             if (!selectedDate.isNullOrEmpty() && forestID != 0) {
                                 callinGetBordereuSerialNumber()
                             }
                         }*/
                    }
                },
                presectedCalender.get(Calendar.YEAR),
                presectedCalender.get(Calendar.MONTH),
                presectedCalender.get(
                    Calendar.DAY_OF_MONTH
                )
            )
        datePickerDialog.getDatePicker().setMaxDate(Date().time)
        datePickerDialog.show()
    }


    fun isValidateForm(): Boolean {
        if (transportModeID == 0) {
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_transport_mode))
            return false
        } else if (view?.txtDate?.text == "Select") {
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_date))
            return false
        }
        else if(!iselectronicBordereau){
                if (view?.txtBordero_No?.text.isNullOrEmpty()) {
                  mUtils.showAlert(activity, mView.resources.getString(R.string.please_enter_bordereu_no))
                return false
//                }else if (view?.txtForest?.text == "Select") {
//                    mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_forest))
//                    return false
//                } else if (view?.txtOriginCFAT?.text == "Select") {
//                    mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_origin))
//                    return false
//                } else if (view?.txtFSC?.text == "Select") {
//                    mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_fsc))
//                    return false
                } else if (view?.txtLeude_chargement?.text == "Select") {
                    mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_leuchargement))
                    return false
                } else if (view?.txtDestination?.text == "Select") {
                    mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_destination))
                    return false
                } else if (view?.txtTranporter?.text == "Select") {
                    mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_transporter))
                    return false
                }
                /*else if(view?.txtAAC?.text=="Select"){
                    mUtils.showAlert(activity, "Please select AAC")
                    return false
                }*/
                else if (view?.txtDistance?.text == "Select") {
                    mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_distance))
                    return false
                }

                /* else if(view?.txtVehicleNo?.text=="Select"){
                     mUtils.showAlert(activity, "Please select vehicle number")
                     return false
                 }*/
                else if (view?.txtVehicleNo?.text.isNullOrEmpty()) {
                    if(transportModeID==3){
                        mUtils.showAlert(activity, mView.resources.getString(R.string.please_enter_truck_number))

                    }else if(transportModeID==1){
                        mUtils.showAlert(activity, mView.resources.getString(R.string.please_enter_wagon_number))

                    }else if(transportModeID==17){
                        mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_barge_number))

                    }else{
                        mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_vehicle_number))
                    }
                    return false
                }

                else if (!mUtils.isvalidTruckNumber(view?.txtVehicleNo?.text.toString())) {
                    mUtils.showAlert(activity, mView.resources.getString(R.string.please_enter_vehicle_number))
                    return false
                }

//        }
//        else if (view?.txtForest?.text == "Select") {
//            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_forest))
//            return false
//        } else if (view?.txtOriginCFAT?.text == "Select") {
//            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_origin))
//            return false
//        } else if (view?.txtFSC?.text == "Select") {
//            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_fsc))
//            return false
        } else if (view?.txtLeude_chargement?.text == "Select") {
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_leuchargement))
            return false
        } else if (view?.txtDestination?.text == "Select") {
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_destination))
            return false
        } else if (view?.txtTranporter?.text == "Select") {
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_transporter))
            return false
        }
        /*else if(view?.txtAAC?.text=="Select"){
            mUtils.showAlert(activity, "Please select AAC")
            return false
        }*/
        else if (view?.txtDistance?.text == "Select") {
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_distance))
            return false
        }

        /* else if(view?.txtVehicleNo?.text=="Select"){
             mUtils.showAlert(activity, "Please select vehicle number")
             return false
         }*/
        else if (view?.txtVehicleNo?.text.isNullOrEmpty()) {

            //mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_vehicle_number))

            if(transportModeID==3){
                mUtils.showAlert(activity, mView.resources.getString(R.string.please_enter_truck_number))

            }else if(transportModeID==1){
                mUtils.showAlert(activity, mView.resources.getString(R.string.please_enter_wagon_number))

            }else if(transportModeID==17){
                mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_barge_number))

            }else{
                mUtils.showAlert(activity, mView.resources.getString(R.string.please_select_vehicle_number))
            }
            return false
        }

        else if (!mUtils.isvalidTruckNumber(view?.txtVehicleNo?.text.toString())) {
            mUtils.showAlert(activity, mView.resources.getString(R.string.please_enter_vehicle_number))
            return false
        }

      /*  else if(view?.txtVehicleNo?.text=="Select"){
            mUtils.showAlert(activity, "Please select vehicle number")
            return false
        }*/


        return true;
    }

    fun clearSelection() {
        forestID = 0
        aacID = 0
        originID = 0
        fscID = 0
        destinationID = 0
        vehicleID = 0
        distionsID = 0
        leude_chargementID = 0
       // mView.rgBordereu.clearCheck()
       // mView.txtBordero_No.setText("")
        mView.txtForest.text = "Select"
        mView.txtOriginCFAT.text = "Select"
        mView.txtDestination.text = "Select"
        mView.txtAAC.text = "Select"
        mView.txtDistance.text = "Select"
        mView.txtFSC.text = "Select"
        mView.txtLeude_chargement.text = "Select"
      /* mView.txtVehicleNo?.setText("Select")*/
       mView.txtVehicleNo?.setText("")
        //mView?.linBordero_No.setBackgroundResource(R.drawable.bg_for_editest)

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun ResetAllFeilds() {
        forestID = 0
        aacID = 0
        originID = 0
        fscID = 0
        destinationID = 0
        vehicleID = 0
        distionsID = 0
        transporterID = 0
        leude_chargementID = 0
        headerID = 0
        iselectronicBordereau = false
        strBordereuNo = ""
        strEBordereuNo = ""
        mView.rgBordereu.clearCheck()
        mView.txtBordero_No.setText("")
       mView.linBordero_No.setBackgroundResource(R.drawable.bg_for_editest)
       mView.radioGroup?.clearCheck()
        mView.txtForest.text = "Select"
        mView.txtOriginCFAT.text = "Select"
        mView.txtDestination.text = "Select"
        mView.txtAAC.text = "Select"
        mView.txtDistance.text = "Select"
        mView.txtTranporter.text = "Select"
        mView.txtFSC.text = "Select"
        mView.txtLeude_chargement.text = "Select"
      /* mView.txtVehicleNo?.setText("Select")*/
       mView.txtVehicleNo?.setText("")
        mView.txtDate.text = "Select"
       // forestList?.clear()
        transporterList?.clear()

        mView.chkWagon?.isChecked = true
        transportModeID = 1
        //commonForestMaster = GetForestDataRes()
        enabledNonEditableField()

    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.loading_wagons)
        (activity as HomeActivity).invisibleFilter()
    }


    fun getSupplierShortName(supplierName: String): String {
        var supplierThreeDigit = ""
        try {
            if (supplierName.trim().length > 3) {
                supplierThreeDigit = supplierName?.substring(0, 3).trim()
            } else {
                supplierThreeDigit = supplierName?.trim()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return supplierThreeDigit
    }

    fun creatingElectronicBordereau(supplier: String, serialNumber: Int): String {
        var finalValue = ""
        try {
            var incrementSerialNo  = serialNumber
            val year = Calendar.getInstance()[Calendar.YEAR]
            var month = Calendar.getInstance()[Calendar.MONTH]
            month++
            incrementSerialNo++
            val twoDigitYear =
                year.toString().substring(year.toString().length - 2, year.toString().length)
            val twoDigitMonth = String.format("%02d", month)
            val serialNumber = String.format("%04d",incrementSerialNo)
            var supplierThreeDigit=""
            if(supplier.length>3){
                supplierThreeDigit  = supplier?.substring(0, 3).trim()
            }else{
                 supplierThreeDigit = supplier?.trim()
            }

             finalValue = supplierThreeDigit + twoDigitYear + twoDigitMonth + serialNumber
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return finalValue
    }

    fun getForestMasterReqquest(forestId:String,userLocationId:String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setForestId(forestId)
        request.setUserLocationId(userLocationId)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


   /* private fun callingForestMasterAPI(forestId: String, userLocationId: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val request  =  getForestMasterReqquest(forestId,userLocationId)
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
                                                view?.txtOriginCFAT?.text =
                                                    commonForestMaster?.getOriginMaster()
                                                        ?.get(0)?.optionName
                                                originID =
                                                    commonForestMaster?.getOriginMaster()?.get(0)?.optionValue!!
                                                view?.txtOriginCFAT?.requestFocus()
                                            }
                                        }
                                        if (!commonForestMaster?.getFscMasterData().isNullOrEmpty()) {
                                            if (commonForestMaster?.getFscMasterData()?.size == 1) {
                                                view?.txtFSC?.text =
                                                    commonForestMaster?.getFscMasterData()?.get(0)
                                                        ?.optionName
                                                fscID =
                                                    commonForestMaster?.getFscMasterData()?.get(0)?.optionValue!!
                                                view?.txtFSC?.requestFocus()
                                            }
                                        }
                                        if (!commonForestMaster?.getLeauChargementUserData().isNullOrEmpty()) {
                                            if (commonForestMaster?.getLeauChargementUserData()?.size == 1) {
                                                view?.txtLeude_chargement?.text =
                                                    commonForestMaster?.getLeauChargementUserData()
                                                        ?.get(0)
                                                        ?.optionName
                                                leude_chargementID =
                                                    commonForestMaster?.getLeauChargementUserData()?.get(
                                                        0
                                                    )?.optionValue!!
                                                view?.txtDistance?.text =
                                                    commonForestMaster?.getLeauChargementUserData()?.get(
                                                        0
                                                    )?.optionValueString!!
                                                view?.txtLeude_chargement?.requestFocus()
                                                view?.txtLeude_chargement?.requestFocus()
                                            }
                                        }

                                        if (commonForestMaster?.getSupplierData() != null) {
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
                            } else{
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
        } else {
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }*/


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
                                        if (!commonForestMaster?.getLeauChargementUserData().isNullOrEmpty()) {
                                            if (commonForestMaster?.getLeauChargementUserData()?.size == 1) {

                                                lechargemetList!!.clear()
                                                lechargemetList!!.addAll((commonForestMaster?.getLeauChargementUserData() as ArrayList<SupplierDatum?>?)!!)

                                                view?.txtLeude_chargement?.text = commonForestMaster?.getLeauChargementUserData()
                                                    ?.get(0)
                                                    ?.optionName
                                                leude_chargementID =
                                                    commonForestMaster?.getLeauChargementUserData()?.get(
                                                        0
                                                    )?.optionValue!!
                                                view?.txtDistance?.text =
                                                    commonForestMaster?.getLeauChargementUserData()?.get(
                                                        0
                                                    )?.optionValueString!!
                                                view?.txtLeude_chargement?.requestFocus()
                                                view?.txtLeude_chargement?.requestFocus()
                                            }else{
                                                lechargemetList!!.clear()
                                                lechargemetList!!.addAll((commonForestMaster?.getLeauChargementUserData() as ArrayList<SupplierDatum?>?)!!)

                                            }
                                        }

                                        if (commonForestMaster?.getSupplierData() != null) {
                                            // mView.txtDestination.text  = commonForestMaster?.getSupplierData()?.finalDestination
                                            //mView.txtDestination.text = commonForestMaster?.getSupplierData()?.finalDestination
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

    fun getTranspoterByTransportModeReequest(transportMode:String):CommonRequest{
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
        } else {
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }


    fun getForestByLocationRequest(userLocationID:String): CommonRequest {
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
                                }else{
                                    mView.txtDate.text  =  mUtils.getCurrentDate()
                                    selectedDate = mUtils.getCurrentDate()
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
        } else {
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }

    fun getDestinationRequest(userLocationID:String): CommonRequest {
        var request : CommonRequest =   CommonRequest()
        request.setUserLocationId(userLocationID)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun getDestinationByLocation(userLocationID: String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val request  =   getDestinationRequest(userLocationID)
                val call_api: Call<List<SupplierDatum>> =
                    apiInterface.getGetDestinationList(request)
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
                                finalDestinationList?.clear()
                                finalDestinationList?.addAll(responce)
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
                        call: Call<List<SupplierDatum>>,
                        t: Throwable
                    ) {
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
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

                                    }else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }

                            }  else if (response.code() == 306) {
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
        } else {
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
                    apiInterface.addBordereauforLoadingWagons(request)
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
                                        var fragment = LoadingWagonsLogsListingFragment()
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
                                        fragment.arguments = bundle
                                        myactivity?.replaceFragment(fragment, false)
                                        ResetAllFeilds()
                                    } else if (response.body()?.getSeverity() == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().getMessage())
                                    }
                                }

                            } else if (response.code() == 306) {
                                mUtils.alertDialgSession(mView.context, activity)
                            }  else {
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
        } else {
            mUtils.showToast(view?.context, getString(R.string.no_internet))
        }
    }


    private fun callinGetBordereuSerialNumber() {
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                var request = generateGetSerialNumberRequest()
                val call_api: Call<getBorderuaSerialNoRes> =
                    apiInterface.getBordereuSerialNumber(request)
                call_api.enqueue(object :
                    Callback<getBorderuaSerialNoRes> {
                    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                    override fun onResponse(
                        call: Call<getBorderuaSerialNoRes>,
                        response: Response<getBorderuaSerialNoRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: getBorderuaSerialNoRes =
                                response.body()!!
                            if (responce != null) {
                                if (responce.getSeverity() == 200) {
                                    val bordereuNo =
                                        responce.getBordereauCount()?.let {
                                            creatingElectronicBordereau(
                                                mView.txtForest.text.toString(),
                                                it
                                            )
                                        }
                                    mView.txtBordero_No.setText(bordereuNo)
                                    disabledElectronicBorderueNo()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onFailure(
                        call: Call<getBorderuaSerialNoRes>,
                        t: Throwable
                    ) {
                        mUtils.dismissProgressDialog()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            mUtils.showToast(mView.context, getString(R.string.no_internet))
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun disabledElectronicBorderueNo(){
       mView.txtBordero_No.isEnabled = false
        mView.txtBordero_No.backgroundTintList =
            mView.context.resources.getColorStateList(R.color.gray_200)
       mView.linBordero_No.setBackgroundResource(R.drawable.bg_for_editext_non_editable)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun enabledElectronicBorderueNo(){
       mView.txtBordero_No.isEnabled = true
        mView.txtBordero_No.backgroundTintList =
            mView.context.resources.getColorStateList(R.color.white)
       mView.linBordero_No.visibility = View.VISIBLE
       mView.txtBoNumber.visibility = View.VISIBLE
       mView.linBordero_No.setBackgroundResource(R.drawable.bg_for_editest)
    }

    //Dialog content
    open fun showDialog(countryListSearch: java.util.ArrayList<SupplierDatum?>?, action: String) {

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
        } catch (e: Exception) {
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (model != null) {
            try {
                when (action) {
                    "forest" -> {
                        clearSelection()
                        view?.txtForest?.text = model.optionName
                        forestID = model.optionValue!!
                        if(!model.finalDestination.isNullOrBlank()) {
                            finalDestination = model.finalDestination!!
                        }
                        view?.txtForest?.requestFocus()
                        /* if(action?.equals(Constants.action_non_edit, ignoreCase = true)) {
                                 if (!selectedDate.isNullOrEmpty() && forestID != 0) {
                                     callinGetBordereuSerialNumber()
                                 }
                             }*/
                       /* callingForestMasterAPI(
                            forestID.toString(),
                            SharedPref.getUserId(Constants.user_location_id).toString()
                        )*/

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
                          view?.txtDestination?.text =   model.optionName
                          destinationID  = model.optionValue!!

                    }

                    "finalDestinationList" -> {
                          view?.txtDestination?.text =   model.optionName
                          destinationID  = model.optionValue!!

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
                            view?.txtVehicleNo?.setText( model.optionName)
                            vehicleID =  model.optionValue!!
                            view?.txtVehicleNo?.requestFocus()
                    }
                    "chargement" -> {
                        if(model.optionName!!.equals("PARK RUPTURE")){
                            view?.chkTruck?.isEnabled  =false
                            view?.chkBarge?.isEnabled  =false
                        }else{
                            view?.chkTruck?.isEnabled  =true
                            view?.chkBarge?.isEnabled =true
                        }
                        view?.txtLeude_chargement?.text = model.optionName
                        leude_chargementID = model.optionValue!!
                        view?.txtDistance?.text = model.optionValueString
                        view?.txtLeude_chargement?.requestFocus()


                    }
                }

            } catch (e: Exception) {
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
                this.originList = originList?.distinctBy { it!!.optionValue } as java.util.ArrayList<SupplierDatum?>

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
                this.fscList = fscList?.distinctBy { it!!.optionValue } as java.util.ArrayList<SupplierDatum?>

            }
        }

        realm.executeTransactionAsync { bgRealm ->
            val leauChargementUserDatum: RealmResults<LeauChargementUserDatum> = bgRealm.where(
                LeauChargementUserDatum::class.java).findAll()
            Log.e("leauChargementUserDatum","is "+leauChargementUserDatum.size)
            for(transpoter in leauChargementUserDatum){
                var transporterDatum=SupplierDatum()
                transporterDatum.optionValueString=transpoter.optionValueString
                transporterDatum.optionValue=transpoter.optionValue
                transporterDatum.optionName=transpoter.optionName
                lechargemetList?.add(transporterDatum)
                this.lechargemetList = lechargemetList?.distinctBy { it!!.optionValue } as java.util.ArrayList<SupplierDatum?>

            }
        }

        realm.executeTransactionAsync { bgRealm ->
            val destinationListDatum: RealmResults<DestinationListDatum> = bgRealm.where(
                DestinationListDatum::class.java).findAll()
            Log.e("DestinationListDatum","is "+destinationListDatum.size)
            for(destination in destinationListDatum){
                var destinationDatum=SupplierDatum()
                destinationDatum.optionValueString=destination.optionValueString
                destinationDatum.optionValue=destination.optionValue
                destinationDatum.optionName=destination.optionName
                finalDestinationList?.add(destinationDatum)
                this.finalDestinationList = finalDestinationList?.distinctBy { it!!.optionValue } as java.util.ArrayList<SupplierDatum?>

            }
        }

//        val moreSupplierInfo: MoreSupplierInfo = realm.where(MoreSupplierInfo::class.java).findFirst()!!
//        mView.txtDestination.text = moreSupplierInfo!!.finalDestination

        if(!isEdit) {
            if (fscList!!.size == 1) {
                view?.txtFSC?.text =
                    fscList?.get(0)
                        ?.optionName
                fscID = fscList?.get(0)?.optionValue!!
                view?.txtFSC?.requestFocus()
            }

            if (originList!!.size == 1) {
                view?.txtOriginCFAT?.text =
                    originList?.get(0)
                        ?.optionName
                originID = originList?.get(0)?.optionValue!!
                view?.txtOriginCFAT?.requestFocus()
            }

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

    override fun onCancleDialog() {
        try {
            countryDialogFragment.dismiss()
            isDialogShowing = false
        } catch (e: Exception) {
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

            R.id.linDestination -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(finalDestinationList, "finalDestinationList")
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
             /*   if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(
                        commonForestMaster?.getVehicleData() as ArrayList<SupplierDatum?>?,
                        "vehicle"
                    )*/
                showDialog(vehicleMaster, "vehicle")
            }
            R.id.linLeude_chargement -> {
                if (checkIsFragmentDialogAlreadyShowing())
                    showDialog(
                       lechargemetList,                        "chargement"
                    )
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
                            var loadingRequest= LoadingRequest()
                            loadingRequest.uniqueId=System.currentTimeMillis().toString()
                            loadingRequest.bordereauDate=mView.txtDate.text.toString()
                            loadingRequest.bordereauNo=mView.txtBordero_No.text.toString()
//                            loadingRequest.forestId=forestID
                            loadingRequest.finalDestination=finalDestination
//                            loadingRequest.forestName=mView.txtForest.text.toString()
//                            loadingRequest.originId=originID
//                            loadingRequest.originName=mView.txtOriginCFAT.text.toString()
//                            loadingRequest.fscId=fscID
//                            loadingRequest.supplierShortName=mView.txtForest.text.toString()
//                            loadingRequest.fscName=mView.txtFSC.text.toString()
                            loadingRequest.leudechargementId=leude_chargementID
                            loadingRequest.chargementName=mView.txtLeude_chargement.text.toString()
                            loadingRequest.transpoterId=transporterID
                            loadingRequest.transpoterName=mView.txtTranporter.text.toString()
                            loadingRequest.distance=mView.txtDistance.text.toString()
                            loadingRequest.headerID=headerID
                            loadingRequest.userID=SharedPref.getUserId(Constants.user_id)
                            loadingRequest.supplierLocation=SharedPref.getUserId(Constants.user_location_id)
                            loadingRequest.modeOfTransport=transportModeID
                            loadingRequest.mode="Save"
                            loadingRequest.destination=mView.txtDestination.text.toString()
                            loadingRequest.speciesId=0
                            loadingRequest.wagonNo=txtVehicleNo.text.toString()?.toUpperCase()
                            loadingRequest.vehicalId=vehicleID
                            loadingRequest.currentDate=mUtils.getCurrentDate()
                            loadingRequest.recordDocNo=mView.txtForest.text.toString()+" "+mView.txtBordero_No.text.toString()

                            var eBorderauNo=getRandomNumberString(loadingRequest)
                            Log.e("Eborder","is"+eBorderauNo)
                            loadingRequest.ebordereauNo=eBorderauNo

                            realm.executeTransactionAsync({ bgRealm ->
                                bgRealm.copyToRealmOrUpdate(loadingRequest)
                            }, {
                                mUtils.showAlert(requireActivity(),getString(R.string.loading_wagon_update_successful))
                                requireActivity().onBackPressed()
                                Log.e("Success","Success")
                            }) {
                                Log.e("failed","faile")
                            }
                        }
                    } else {
                        if(iselectronicBordereau){
                            if (mUtils.checkInternetConnection(mView.context)) {
                                callingAddBordereauAPI()
                            }else{
                                var loadingRequest= LoadingRequest()
                                loadingRequest.uniqueId=System.currentTimeMillis().toString()
                                loadingRequest.bordereauDate=mView.txtDate.text.toString()
                                loadingRequest.bordereauNo=mView.txtBordero_No.text.toString()
                                loadingRequest.forestId=forestID
                                loadingRequest.forestName=mView.txtForest.text.toString()

                                loadingRequest.finalDestination=finalDestination
                                loadingRequest.supplierShortName=mView.txtForest.text.toString()
                                loadingRequest.originId=originID
                                loadingRequest.iselectronicBordereau=iselectronicBordereau
                                loadingRequest.originName=mView.txtOriginCFAT.text.toString()
                                loadingRequest.fscId=fscID
                                loadingRequest.fscName=mView.txtFSC.text.toString()
                                loadingRequest.leudechargementId=leude_chargementID
                                loadingRequest.chargementName=mView.txtLeude_chargement.text.toString()
                                loadingRequest.transpoterId=transporterID
                                loadingRequest.transpoterName=mView.txtTranporter.text.toString()
                                loadingRequest.distance=mView.txtDistance.text.toString()
                                loadingRequest.headerID=headerID
                                loadingRequest.userID=SharedPref.getUserId(Constants.user_id)
                                loadingRequest.supplierLocation=SharedPref.getUserId(Constants.user_location_id)
                                loadingRequest.modeOfTransport=transportModeID
                                loadingRequest.mode="Save"
                                loadingRequest.destination=mView.txtDestination.text.toString()
                                loadingRequest.speciesId=0
                                loadingRequest.wagonNo=txtVehicleNo.text.toString()?.toUpperCase()
                                loadingRequest.vehicalId=vehicleID
                                loadingRequest.currentDate=mUtils.getCurrentDate()
                                loadingRequest.recordDocNo=mView.txtForest.text.toString()+" "+mView.txtBordero_No.text.toString()
                                var eBorderauNo=getRandomNumberString(loadingRequest)
                                Log.e("Eborder","is"+eBorderauNo)
                                loadingRequest.ebordereauNo=eBorderauNo
                                realm.executeTransactionAsync({ bgRealm ->
                                    bgRealm.copyToRealmOrUpdate(loadingRequest)
                                }, {
                                    mUtils.showAlert(requireActivity(),getString(R.string.loading_wagon_save_successful))
                                    requireActivity().onBackPressed()
                                    Log.e("Success","Success")
                                }) {
                                    Log.e("faile","faile")
                                }

                            }
                        }else {
                            if (mUtils.checkInternetConnection(mView.context)) {
                                callingValidateBordereauNoAPI()
                            }else
                            {

                                var loadingRequest= LoadingRequest()
                                loadingRequest.uniqueId=System.currentTimeMillis().toString()
                                loadingRequest.bordereauDate=mView.txtDate.text.toString()
                                loadingRequest.bordereauNo=mView.txtBordero_No.text.toString()
                                loadingRequest.forestId=forestID
                                loadingRequest.forestName=mView.txtForest.text.toString()
                                loadingRequest.originId=originID
                                loadingRequest.iselectronicBordereau=iselectronicBordereau
                                loadingRequest.supplierShortName=mView.txtForest.text.toString()
                                loadingRequest.originName=mView.txtOriginCFAT.text.toString()
                                loadingRequest.fscId=fscID
                                loadingRequest.fscName=mView.txtFSC.text.toString()
                                loadingRequest.leudechargementId=leude_chargementID
                                loadingRequest.chargementName=mView.txtLeude_chargement.text.toString()
                                loadingRequest.transpoterId=transporterID
                                loadingRequest.transpoterName=mView.txtTranporter.text.toString()
                                loadingRequest.distance=mView.txtDistance.text.toString()
                                loadingRequest.headerID=headerID
                                loadingRequest.userID=SharedPref.getUserId(Constants.user_id)
                                loadingRequest.supplierLocation=SharedPref.getUserId(Constants.user_location_id)
                                loadingRequest.modeOfTransport=transportModeID
                                loadingRequest.mode="Save"
                                loadingRequest.destination=mView.txtDestination.text.toString()
                                loadingRequest.speciesId=0
                                loadingRequest.wagonNo=txtVehicleNo.text.toString()?.toUpperCase()
                                loadingRequest.vehicalId=vehicleID
                                loadingRequest.currentDate=mUtils.getCurrentDate()
                                loadingRequest.recordDocNo=mView.txtForest.text.toString()+" "+mView.txtBordero_No.text.toString()
                                var eBorderauNo=getRandomNumberString(loadingRequest)
                                Log.e("Eborder","is"+eBorderauNo)
                                loadingRequest.ebordereauNo=eBorderauNo
                                realm.executeTransactionAsync({ bgRealm ->
                                    bgRealm.copyToRealmOrUpdate(loadingRequest)
                                }, {
                                    mUtils.showAlert(requireActivity(),getString(R.string.loading_wagon_save_successful))
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
    }

    fun getRandomNumberString(loadingRequest: LoadingRequest):String {

        // It will generate 6 digit random Number.
        // from 0 to 999999

        try{
//            var rnd =  Random()
//            var number = rnd.nextInt(99)
//            val currentTimestamp = System.currentTimeMillis()
//            var uniquebarcode=currentTimestamp.toString()
            val date = SimpleDateFormat("yyMMddssSSS", Locale.getDefault()).format(Date())
            var uniquecode= loadingRequest.destination!!.substring(0,3)+date
//            var uniquecode= loadingRequest.destination!!.substring(0,3)+number+date

            //val uniqueBarcode = uniquebarcode.substring(0, 12)
            //var uniquebarcode=bodreuNumber+String.format("%04d", number)

            Log.e("uniqueBarcode", "is" + uniquecode)
//            val last12: String
//            if (uniquecode == null || uniquecode.length < 12) {
//                val uniqueBarcode = uniquecode+number
//                last12 = uniqueBarcode.substring(uniqueBarcode.length - 12);
//            } else {
//                last12 = uniquecode.substring(0,12);
//            }
            // this will convert String number sequence into 6 character.
//            Log.e("last12", "is" + last12)
            return uniquecode

        }catch (e: java.lang.Exception){
            e.printStackTrace()
            return ""
        }
    }

    fun checkIsFragmentDialogAlreadyShowing(): Boolean {
        if (isDialogShowing) {
            return false
        }
        return true
    }


}
