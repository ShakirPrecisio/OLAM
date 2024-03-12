package com.kemar.olam.sales_and_inspection.inspection.fragment

import ViewPagerAdapter
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.login.model.responce.CommonRequest
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.sales_and_inspection.ground_sales.fragment.GroundUserHistoryFragment
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_main_container.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainContainerFragment : Fragment() {
    lateinit var mView: View
    lateinit var mUtils: Utility
    val inspectionUserHistory = SalesUserHistoryFragment()//InnspectionHeaderFragment()
    val groundHistoryFragment = GroundUserHistoryFragment()//GroundUserHistoryFragment()//DeleiveryManagementFragment()//SalesUserHistoryFragment()

    //for sales inspection
    var salesInspectionUserHistory: ArrayList<LogsUserHistoryRes.BordereauRecordList?> = arrayListOf()
    var wagonLocalMaster : java.util.ArrayList<SupplierDatum> = arrayListOf()
    var bordereuLocalMaster   : java.util.ArrayList<SupplierDatum> = arrayListOf()

    //for grounnd inspection
    var groundInspectionUserHistory: ArrayList<LogsUserHistoryRes.BordereauGroundList?> = arrayListOf()

    var commigFrom = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView =  inflater.inflate(R.layout.fragment_main_container, container, false)
        commigFrom =
            arguments?.getString(Constants.comming_from).toString()

        mUtils = Utility()
         setUpViewPager()
          setToolbar()
         mView.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
             override fun onTabSelected(tab: TabLayout.Tab) {
                 mView.viewPager.setCurrentItem(tab.getPosition())
                 val view = tab.customView
                 if (view is AppCompatTextView) {
                     view.setTypeface(view.typeface, Typeface.BOLD)
                 }
             }

             override fun onTabUnselected(tab: TabLayout.Tab) {
                 val view = tab.customView
                 if (view is AppCompatTextView) {
                     view.setTypeface(view.typeface, Typeface.NORMAL)
                 }
                 if (view is AppCompatTextView) {
                     val font = Typeface.createFromAsset(mView.context.assets, "font/poppins_regular.ttf")
                    view.typeface = font
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        getSalesUserHistory(SharedPref.getUserId(Constants.user_id).toString())
        getGroundUserHistory(SharedPref.getUserId(Constants.user_id).toString())
        return  mView
    }


    private fun setUpViewPager() {
        val adapter = ViewPagerAdapter(childFragmentManager)
        adapter.addFragment(inspectionUserHistory, mView.context.resources.getString(R.string.sales_inspection))
        adapter.addFragment(groundHistoryFragment,  mView.context.resources.getString(R.string.ground_sales))
        mView.viewPager.adapter = adapter
        mView.tabs.setupWithViewPager(mView.viewPager)
        if (commigFrom.equals(Constants.from_sales, ignoreCase = true)) {
            mView.viewPager.setCurrentItem(0)
        }else{
            mView.viewPager.setCurrentItem(1)
        }
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.inspection_n_sales)
        (activity as HomeActivity).invisibleFilter()
    }



    fun getGroundUserHistoryRequest(userID: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setUserId(userID)
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setIsAdmin(false)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }
    //for Ground Inspection User History
    fun getGroundUserHistory(userID: String) {
        groundInspectionUserHistory.clear()
        if (mUtils.checkInternetConnection(mView.context)) {
            /*  showContentView()*/
            try {
                //var  isAdmin : Boolean = false
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                /*  when(SharedPref.read(Constants.user_role).toString()){
                      "SuperUserApp","Super User","admin"->{
                          isAdmin = false
                      }
                      else->{
                          isAdmin = false
                      }
                  }*/

                val request  =   getGroundUserHistoryRequest(userID)
                val call_api: Call<LogsUserHistoryRes> =
                    apiInterface.getBordereauHistoryForGround(
                        request
                    )
                call_api.enqueue(object :
                    Callback<LogsUserHistoryRes> {
                    override fun onResponse(
                        call: Call<LogsUserHistoryRes>,
                        response: Response<LogsUserHistoryRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {

                                if (response.body().getSeverity() == 200) {
                                    val responce: LogsUserHistoryRes =
                                        response.body()!!
                                    groundInspectionUserHistory.clear()
                                    responce.getBordereauGroundList()?.let {
                                        groundInspectionUserHistory.addAll(
                                            it
                                        )
                                    }
                                    groundHistoryFragment.setupUserHistoryDataForGroundInspection(
                                        groundInspectionUserHistory,
                                        true
                                    )

                                }else if (response.body()?.getSeverity() == 306) {
                                    mUtils.alertDialgSession(mView.context, activity)
                                } else {
                                    mUtils.showToast(activity, response.body().getMessage())
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
                        call: Call<LogsUserHistoryRes>,
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
//            groundHistoryFragment.setupUserHistoryDataForGroundInspection(groundInspectionUserHistory,false)
        }
    }

    fun getSalesUserHistoryRequeest(userID: String): CommonRequest {
        val request : CommonRequest =   CommonRequest()
        request.setUserId(userID)
        request.setUserLocationId(SharedPref.readInt(Constants.user_location_id).toString())
        request.setIsAdmin(false)
        val json =  Gson().toJson(request)
        var test  = json

        return  request

    }


    //for sales Inspection User History
     fun getSalesUserHistory(userID: String) {
        salesInspectionUserHistory.clear()
        if (mUtils.checkInternetConnection(mView.context)) {
          /*  showContentView()*/
            try {
                //var  isAdmin : Boolean = false
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                /*  when(SharedPref.read(Constants.user_role).toString()){
                      "SuperUserApp","Super User","admin"->{
                          isAdmin = false
                      }
                      else->{
                          isAdmin = false
                      }
                  }*/
val request  =   getSalesUserHistoryRequeest(userID)
                val call_api: Call<LogsUserHistoryRes> =
                    apiInterface.getBordereauHistoryForInspection(
                        request
                    )
                call_api.enqueue(object :
                    Callback<LogsUserHistoryRes> {
                    override fun onResponse(
                        call: Call<LogsUserHistoryRes>,
                        response: Response<LogsUserHistoryRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            if (response.code() == 200) {

                                if (response.body().getSeverity() == 200) {
                                    salesInspectionUserHistory.clear()
                                    wagonLocalMaster.clear()
                                    bordereuLocalMaster.clear()
                                    val responce: LogsUserHistoryRes =
                                        response.body()!!
                                    responce.getBordereauRecordList()?.let {
                                        salesInspectionUserHistory.addAll(
                                            it
                                        )
                                    }

                                    for ((index, listdata) in salesInspectionUserHistory.withIndex()) {
                                        listdata?.index = index
                                        // for (listdata in groundLogsLiting) {
                                        //forestLocalMaster.add()
                                        val wagonSupplierDatum = SupplierDatum()
                                        wagonSupplierDatum.optionName = listdata?.wagonNo
                                        wagonLocalMaster.add(wagonSupplierDatum)


                                        val bordereuNoSupplierDatum = SupplierDatum()
                                        if (listdata?.bordereauNo.toString().isNullOrEmpty()) {
                                            bordereuNoSupplierDatum.optionName =
                                                listdata?.eBordereauNo
                                        } else {
                                            bordereuNoSupplierDatum.optionName =
                                                listdata?.bordereauNo
                                        }
                                        bordereuLocalMaster.add(bordereuNoSupplierDatum)


                                    }


                                    inspectionUserHistory.setupUserHistoryDataForSalesInspection(
                                        salesInspectionUserHistory,
                                        true,
                                        wagonLocalMaster,
                                        bordereuLocalMaster
                                    )

                                } else if (response.body()?.getSeverity() == 306) {
                                    mUtils.alertDialgSession(mView.context, activity)
                                } else {
                                    mUtils.showToast(activity, response.body().getMessage())
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
                        call: Call<LogsUserHistoryRes>,
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
//            inspectionUserHistory.setupUserHistoryDataForSalesInspection(salesInspectionUserHistory,false,wagonLocalMaster,bordereuLocalMaster)
        }
    }


}