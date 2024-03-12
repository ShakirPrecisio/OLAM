package com.kemar.olam.delivery_management.fragments

import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.activity.PdfDocumentAdapter
import com.kemar.olam.delivery_management.adapter.DeliverdUserHistoryAdapter
import com.kemar.olam.delivery_management.model.request.DeliveryHistoryReq
import com.kemar.olam.delivery_management.model.response.LogsUserHistoryRes
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_deliverd_bordereu.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList


class DeliverdBordereuHistoryFragment : Fragment() ,View.OnClickListener {

    lateinit var mView: View
    lateinit var mUtils: Utility
    lateinit var rvLogsUserHistory: RecyclerView
    lateinit var adapter: DeliverdUserHistoryAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var DeliverdBordereuHitoryLiting: ArrayList<LogsUserHistoryRes.UserHist?> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView  = inflater.inflate(R.layout.fragment_deliverd_bordereu, container, false)
        initViews()
        return mView
    }
    fun initViews() {
        mUtils = Utility()
        rvLogsUserHistory = mView.findViewById(R.id.rvLogsUserHistory)
        setToolbar()
        setupClickListner()
        setupRecyclerViewNAdapter()
        callingDeliveredUserHistory()
        mView.swipeDeliverd.setOnRefreshListener {
            mView.swipeDeliverd.isRefreshing = false
            callingDeliveredUserHistory()
        }
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.drawer_delivery_management)
        (activity as HomeActivity).invisibleFilter()
    }

    fun setupClickListner() {
        mView.btnRetry.setOnClickListener(this)
    }

    fun callingDeliveredUserHistory() {
        getDeliveredUserHistory()
    }

    fun setupRecyclerViewNAdapter() {
        DeliverdBordereuHitoryLiting.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvLogsUserHistory.layoutManager = mLinearLayoutManager
        adapter = DeliverdUserHistoryAdapter(mView.context, DeliverdBordereuHitoryLiting,
            SharedPref.read(Constants.user_role).toString())
        mView.rvLogsUserHistory.adapter = adapter


        adapter.onHeaderClick = { modelData, position ->
            val myactivity = activity as HomeActivity
            val fragment =
                DeliveredLogsDetailsFragment()
            val bundle = Bundle()
            bundle.putSerializable(
                Constants.badereuModel,
                modelData
            )
            bundle.putString(
                Constants.comming_from,
                mView.context.getString(R.string.user_history)
            )
            fragment.arguments = bundle
            myactivity?.replaceFragment(fragment, false)

        }


    }


    fun printPDF(pdfPath: String) {
        val printManager =
            mView.context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val printAdapter =
            PdfDocumentAdapter(mView.context, pdfPath)
        printManager.print("Document", printAdapter, PrintAttributes.Builder().build())
    }


    private fun generateDeliveredHistoryRequest(): DeliveryHistoryReq {
        var request : DeliveryHistoryReq =   DeliveryHistoryReq()
        request.setUserID(SharedPref.getUserId(Constants.user_id))
        request.setUserLocationID(SharedPref.readInt(Constants.user_location_id))
        return  request

    }



    private fun getDeliveredUserHistory() {
        if (mUtils.checkInternetConnection(mView.context)) {
            showContentView()
            try {
                //var  isAdmin : Boolean = false
                mUtils.showProgressDialog(mView.context)
                val request  = generateDeliveredHistoryRequest()
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)
                val call_api: Call<LogsUserHistoryRes> =
                    apiInterface.getDeliveredLogList(request)
                call_api.enqueue(object :
                    Callback<LogsUserHistoryRes> {
                    override fun onResponse(
                        call: Call<LogsUserHistoryRes>,
                        response: Response<LogsUserHistoryRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: LogsUserHistoryRes = response.body()!!
                            if (responce != null) {
                                if (response.code() == 200) {
                                    if (responce.severity == 200) {
                                        DeliverdBordereuHitoryLiting?.clear()
                                        responce.userHistList?.let {
                                            DeliverdBordereuHitoryLiting?.addAll(
                                                it
                                            )
                                        }
                                        if (DeliverdBordereuHitoryLiting.size == 0) {
                                            showNoNoDataView()
                                        } else {
                                            showContentView()
                                        }
                                        adapter.notifyDataSetChanged()
                                    }else if (response.body()?.severity == 306) {
                                        mUtils.alertDialgSession(mView.context, activity)
                                    } else {
                                        mUtils.showToast(activity, response.body().message)
                                    }
                                } else if (response.code() == 306) {
                                    mUtils.alertDialgSession(mView.context, activity)
                                } else{
                                    mUtils.showToast(activity, Constants.SOMETHINGWENTWRONG)
                                }
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
            showNoInternetView()
        }
    }


    fun showNoInternetView() {
        mView.linContent.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.VISIBLE
    }

    fun showNoNoDataView() {
        mView.linContent.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
        mView.relvNoDataFound.visibility = View.VISIBLE
    }

    fun showContentView() {
        mView.linContent.visibility = View.VISIBLE
        mView.relvNoDataFound.visibility = View.GONE
        mView.relvNoInternet.visibility = View.GONE
    }


    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.btnRetry -> {
                getDeliveredUserHistory()
            }
        }
    }
}