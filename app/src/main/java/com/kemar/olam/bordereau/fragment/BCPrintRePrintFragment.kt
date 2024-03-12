package com.kemar.olam.bordereau.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.utility.ApiEndPoints
import com.kemar.olam.R
import com.kemar.olam.bordereau.adapter.LogsTodaysHistoryAdapter
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.dashboard.activity.HomeActivity
import com.kemar.olam.dashboard.models.MenusModel
import com.kemar.olam.retrofit.ApiClient
import com.kemar.olam.utility.Constants
import com.kemar.olam.utility.SharedPref
import com.kemar.olam.utility.Utility
import kotlinx.android.synthetic.main.content_homes.*
import kotlinx.android.synthetic.main.fragment_todays_history.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BCPrintRePrintFragment : Fragment(),View.OnClickListener {

    lateinit var mView: View
    lateinit var mUtils : Utility
    lateinit var rvLogsUserHistory: RecyclerView
    lateinit var adapter: LogsTodaysHistoryAdapter
    lateinit var mLinearLayoutManager: LinearLayoutManager
    var logsTodaysHitoryLiting: ArrayList<LogsUserHistoryRes.BordereauRecordList?> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       /* mView =  inflater.inflate(R.layout.fragment_b_c_print_re_print, container, false)
        initViews()
        return mView;*/
        mView = inflater.inflate(R.layout.fragment_deleivery_management, container, false)
        //initViews()
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.title_bc_re_print)
        return mView;
    }

    fun initViews() {
        mUtils = Utility()
        rvLogsUserHistory = mView.findViewById(R.id.rvLogsUserHistory)
        setToolbar()
      /*  setupClickListner()
        setupRecyclerViewNAdapter()
        callingUserHistory()
        mView.swipeUserHistory.setOnRefreshListener{
            mView.swipeUserHistory.isRefreshing =  false
            callingUserHistory()
        }*/
    }
    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.title_bc_re_print)
            (activity as HomeActivity).invisibleFilter()
        /* (activity as HomeActivity).ivHomeFilter.setOnClickListener{

         }*/
    }

    /*  override fun onResume() {
          super.onResume()
          callingUserHistory()
      }*/
    fun setupClickListner(){
        mView.ivAddBodreu.setOnClickListener(this)
        mView.btnRetry.setOnClickListener(this)
    }

  /*  fun callingUserHistory(){
        getBodreuLogsUserHistory(SharedPref.getUserId(Constants.user_id).toString())
    }*/

    fun setupRecyclerViewNAdapter() {
        logsTodaysHitoryLiting.clear()
        mLinearLayoutManager =
            LinearLayoutManager(mView.context, LinearLayoutManager.VERTICAL, false)
        mView.rvLogsUserHistory.layoutManager = mLinearLayoutManager
        adapter = LogsTodaysHistoryAdapter(mView.context, logsTodaysHitoryLiting,true,false)
        mView.rvLogsUserHistory.adapter = adapter


        adapter.onHeaderClick = { modelData, position ->
            //  if(!modelData.headerStatus.equals("Submitted",ignoreCase = true)) {
            val myactivity = activity as HomeActivity
            var prductFragment = LogsListingFragment()
            val bundle = Bundle()
            bundle.putSerializable(
                Constants.badereuModel,
                modelData
            );
            bundle.putString(
                Constants.comming_from,
                "UserHistory"
            );
            prductFragment.arguments = bundle
            myactivity?.replaceFragment(prductFragment,false)
            // }
        }

    }


   /* private fun getBodreuLogsUserHistory(userID:String) {
        if (mUtils.checkInternetConnection(mView.context)) {
            hideNoInternetView()
            try {
                mUtils.showProgressDialog(mView.context)
                val apiInterface: ApiEndPoints = ApiClient.client.create(ApiEndPoints::class.java)

                val call_api: Call<LogsUserHistoryRes> =
                    apiInterface.getBodereuLogsUserHistory(userID,SharedPref.readInt(Constants.user_location_id).toString(),true)
                call_api.enqueue(object :
                    Callback<LogsUserHistoryRes> {
                    override fun onResponse(
                        call: Call<LogsUserHistoryRes>,
                        response: Response<LogsUserHistoryRes>
                    ) {
                        mUtils.dismissProgressDialog()

                        try {
                            val responce: LogsUserHistoryRes =
                                response.body()!!
                            if (responce != null) {
                                logsTodaysHitoryLiting?.clear()
                                responce.getBordereauRecordList()?.let {
                                    logsTodaysHitoryLiting?.addAll(
                                        it
                                    )
                                }
                                adapter.notifyDataSetChanged()
                            }
                        }catch (e:Exception){
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
        }else{
            mUtils.showToast(mView.context, getString(R.string.no_internet))
            showNoInternetView()
        }
    }*/


    fun showNoInternetView(){
        mView.linContent.visibility = View.GONE
        mView.relvNoInternet.visibility = View.VISIBLE
    }

    fun hideNoInternetView(){
        mView.linContent.visibility = View.VISIBLE
        mView.relvNoInternet.visibility = View.GONE
    }
    override fun onClick(view: View?) {
        val id = view!!.id
        when (id) {
            R.id.ivAddBodreu -> {
                var fragment = AddHeaderFragment()
                (activity as HomeActivity).replaceFragment(fragment,false)
            }

            R.id.btnRetry ->{
               // getBodreuLogsUserHistory(SharedPref.getUserId(Constants.user_id).toString())
            }
        }
    }





    fun <ArrayList> getAppMenuList(isSuperAdmin:Boolean): ArrayList {
        var appMenuList = ArrayList<MenusModel>()
        appMenuList.add(
            MenusModel(
                R.drawable.dashboard,
                resources.getString(R.string.dashboard)
            )
        )
        appMenuList.add(
            MenusModel(
                R.drawable.barcode,
                resources.getString(R.string.bordereau_module)
            )
        )
        appMenuList.add(
            MenusModel(
                R.drawable.bc_reprint,
                resources.getString(R.string.bc_re_print_n_edit)
            )
        )

        appMenuList.add(
            MenusModel(
                R.drawable.laoding_wagon,
                resources.getString(R.string.loading_wagons)
            )
        )

        appMenuList.add(
            MenusModel(
                R.drawable.origin_verfication,
                resources.getString(R.string.origin_verification)
            )
        )

        appMenuList.add(
            MenusModel(
                R.drawable.inspection_sales,
                resources.getString(R.string.inspection_n_sales)
            )
        )

        appMenuList.add(
            MenusModel(
                R.drawable.delivery,
                resources.getString(R.string.drawer_delivery_management)
            )
        )

        appMenuList.add(
            MenusModel(
                R.drawable.logout,
                resources.getString(R.string.logout)
            )
        )

        return appMenuList as ArrayList
    }


   /* fun initViews(){
        setToolbar()
       mView.rvBcPrintListing.layoutManager = LinearLayoutManager(
            view?.context,
            LinearLayoutManager.VERTICAL, false
        )
        mAdapter  =
           BcPrintRePrintAdapter(mView.context,getAppMenuList(true))
       mView.rvBcPrintListing.adapter = mAdapter
       mView.nestedScrollView?.smoothScrollTo(0,0)
    }

    fun setToolbar() {
        (activity as HomeActivity).txtHomeTitle.text = getString(R.string.title_bc_re_print)
    }*/

}