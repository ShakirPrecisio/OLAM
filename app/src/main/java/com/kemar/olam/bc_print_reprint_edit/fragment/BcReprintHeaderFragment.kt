package com.kemar.olam.bc_print_reprint_edit.fragment

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.gson.Gson
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.bc_print_reprint_edit.model.request.getBordereListByFilterReq
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.Utility
import com.kemar.olam.R
import com.kemar.olam.utility.SharedPref
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment__add_header.*
import kotlinx.android.synthetic.main.fragment__add_header.view.ivNext
import kotlinx.android.synthetic.main.fragment__add_header.view.linDate
import kotlinx.android.synthetic.main.fragment__add_header.view.linForest
import kotlinx.android.synthetic.main.fragment__add_header.view.swipeHeader
import kotlinx.android.synthetic.main.fragment__add_header.view.txtForest
import kotlinx.android.synthetic.main.fragment_bc_search.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class BcReprintHeaderFragment : Fragment(),DialogFragment.GetDialogListener,View.OnClickListener {
    lateinit var mView : View
    lateinit var mUtils : Utility
    lateinit var dialogFragment: DialogFragment
    lateinit var  presectedCalender : Calendar
    var forestList : ArrayList<SupplierDatum?>? = arrayListOf()
    var isDialogShowing:Boolean = false
    var selectedDate  :String=""
    var forestID : Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_bc_search, container, false)
        mUtils = Utility()
        init()
        return mView
    }

    private fun init(){

        presectedCalender  = Calendar.getInstance()
        setToolbar()
        setupClickListner()
        mView.swipeHeader.setOnRefreshListener{
            mView.swipeHeader.isRefreshing =  false
            getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
            mView.edtBordero_No.clearFocus()
        }

        mView.edt_log_2?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

            }


            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                try {
                   if(!mView.edt_log_2?.text.isNullOrEmpty()){
                       if(4 < mView.edt_log_2?.text.toString().toInt()){
                           mUtils.showAlert(activity, mView.resources.getString(R.string.after_slash_should_not_allow_more_than_four))
                       }
                   }
                } catch (e: Exception) {
                    e.toString()
                }
            }
        })

        getForestDataByLocation(SharedPref.readInt(Constants.user_location_id).toString())
    }
    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.bc_re_print_n_edit)
         (activity as HomeActivity).invisibleFilter()
    }
    fun setupClickListner(){
        mView.ivNext.setOnClickListener(this)
        mView.linForest.setOnClickListener(this)
        mView.linDate.setOnClickListener(this)

    }

    fun getForestByLocationRequest(userLocationID:String):CommonRequest{
        var request : CommonRequest =   CommonRequest()
        request.setUserLocationId(userLocationID)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    private fun getForestDataByLocation(userLocationID:String) =
        if (mUtils.checkInternetConnection(mView.context)) {
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val request  =   getForestByLocationRequest(userLocationID)
                val call_api: Call<List<SupplierDatum>> =
                    apiInterface.getForestDataByLocation(request)
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
                                forestList?.clear()
                                forestList?.addAll(responce)
                            }
                        }catch (e:Exception){
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

    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.linForest->{
                if(checkIsFragmentDialogAlreadyShowing())
                    showDialog(forestList,"forest")
            }
            R.id.linDate->{
                datePicker(txtDate)
            }
            R.id.ivNext ->{
                if(isValidateLogsNumber()) {
                    val request = getBordereListByFilterReq()
                    request.setBordereauDate(selectedDate)
                    request.setSupplier(forestID)
                    if(mView.edt_log_2.text.isNullOrEmpty() && mView.edt_log_.text.isNullOrEmpty()){
                        request.setLogNo("")
                    }else{
                        request.setLogNo(mView.edt_log_.text.toString() + "/" + mView.edt_log_2.text.toString())
                    }

                    request.setBordereauNo(mView.edtBordero_No.text.toString())
                    val fragment =
                        BcTodaysHistoryFragment()
                    val bundle = Bundle()
                    bundle.putString(
                        Constants.comming_from,
                        Constants.Bc_Header_screen
                    )
                    bundle.putSerializable(
                        Constants.badereuFilterModel,
                        request
                    )

                    fragment.arguments = bundle
                    (activity as HomeActivity).replaceFragment(fragment, false)
                }
            }

        }
    }

    fun isValidateLogsNumber():Boolean{
         if (!mView.edt_log_.text.isNullOrEmpty()) {
             if (mView.edt_log_2.text.isNullOrEmpty()) {
                 mUtils.showAlert(activity, mView.resources.getString(R.string.please_enter_valid_log_no))
                 return false
             }
        }else if(!mView.edt_log_2.text.isNullOrEmpty()) {
             if (mView.edt_log_.text.isNullOrEmpty()) {
                 mUtils.showAlert(activity, mView.resources.getString(R.string.please_enter_valid_log_no))
                 return false
             }
                else if(4 < mView.edt_log_2?.text.toString().toInt()){
                     mUtils.showAlert(activity, mView.resources.getString(R.string.after_slash_should_not_allow_more_than_four))
                      mView.edt_log_2.requestFocus()
                 }

         }
        return true
    }

    //Dialog content
     fun showDialog(countryListSearch:ArrayList<SupplierDatum?>?,action:String) {
        val fm: FragmentManager
        val bundle: Bundle
        fm = childFragmentManager
        dialogFragment = DialogFragment(this)
        bundle = Bundle()
        bundle.putSerializable("COUNTRY_LIST", countryListSearch)
        dialogFragment.setArguments(bundle)
        bundle.putBoolean("isCountryCode", false)
        bundle.putString("action", action)
        dialogFragment.show(fm, "COUNTRY_FRAGMENT")
        dialogFragment.isCancelable = false
        isDialogShowing = true
    }

    fun datePicker(txtView  : TextView){
        val datePickerDialog =
            DatePickerDialog(
                mView.context ,
                object :
                    DatePickerDialog.OnDateSetListener {
                    @SuppressLint("SetTextI18n")
                    override fun onDateSet(
                        view: DatePicker?, year: Int,
                        monthOfYear: Int, dayOfMonth: Int
                    ) {

                        presectedCalender.set(Calendar.YEAR,year)
                        presectedCalender.set(Calendar.MONTH,monthOfYear)
                        presectedCalender.set(Calendar.DAY_OF_MONTH,dayOfMonth)


                        txtView.setText(mUtils.dateFormater("dd-MM-yyyy","dd.MM.yyyy",dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year))
                        selectedDate= mUtils.dateFormater("dd-MM-yyyy","dd.MM.yyyy",dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)!!

                    }
                }, presectedCalender.get(Calendar.YEAR), presectedCalender.get(Calendar.MONTH),presectedCalender.get(
                    Calendar.DAY_OF_MONTH)
            )
        datePickerDialog.getDatePicker().setMaxDate(Date().time)
        datePickerDialog.show()
    }

    override fun onSubmitData(model: SupplierDatum?, isCountryCode: Boolean, action : String) {

        try {
            dialogFragment.dismiss()
            isDialogShowing = false
            mView.edtBordero_No.clearFocus()
        }catch (e:Exception){
            e.printStackTrace()
        }
        if (model != null) {
            try{
                when (action) {
                    "forest"->{
                        view?.txtForest?.text = model.optionName
                        forestID = model.optionValue!!
                        view?.txtForest?.requestFocus()
                    }
                }

            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun  checkIsFragmentDialogAlreadyShowing():Boolean{
        if(isDialogShowing){
            return false
        }
        return true
    }

    override fun onCancleDialog() {
        try {
            dialogFragment.dismiss()
            isDialogShowing = false
        }catch (e:Exception){
            e.printStackTrace()

        }
    }


}