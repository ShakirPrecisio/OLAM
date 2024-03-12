package com.kemar.olam.dialog_fragment.fragment

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.adapter.DialogAdapter
import java.util.*
import java.util.regex.Pattern


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DialogFragment(val fragment : Fragment) : DialogFragment(), DialogAdapter.DialogListener{

    lateinit var mContext  : Context
    var recyclerView: RecyclerView? = null
    var tvTitle: TextView? = null
    var searchView: SearchView? = null
    lateinit var bundleList: ArrayList<SupplierDatum?>
    var countryList:java.util.ArrayList<SupplierDatum?>? = null
    var countryListSearch:java.util.ArrayList<SupplierDatum?>? = null
    var countryAdapter: DialogAdapter? = null

    private var linCancle: LinearLayout? = null
    private var linSeach:LinearLayout? = null
    var ivCancel: ImageView? = null
    var isCountryCode = false
    var action=""


    var listener: GetDialogListener? = null

    var selectedCountry: SupplierDatum? = null

    interface GetDialogListener {
        fun onSubmitData(model: SupplierDatum?, isCountryCode :Boolean, action : String)
        fun onCancleDialog() //void onCancleDialog();
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.mContext = context.applicationContext
        try {
         /*   listener = context as GetCountryListener*/
            listener = fragment as GetDialogListener
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog)
        setCancelable(false)
        if (getArguments() != null) {
            countryList = ArrayList<SupplierDatum?>()
            countryListSearch = ArrayList<SupplierDatum?>()
            try {
                bundleList =
                    getArguments()?.getSerializable("COUNTRY_LIST") as ArrayList<SupplierDatum?>
                isCountryCode = requireArguments().getBoolean("isCountryCode")
                action  = requireArguments().getString("action").toString()
                if (bundleList != null) {
                    countryList!!.addAll(bundleList)
                    countryListSearch!!.addAll(bundleList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.country_dialog_spinner, container, false)
        searchView =
            view.findViewById<View>(R.id.searchCountry) as SearchView
        (searchView!!.findViewById<View>(androidx.appcompat.R.id.search_src_text) as EditText).setHintTextColor(
            getResources().getColor(R.color.white)
        )
        recyclerView = view.findViewById<View>(R.id.recyclerview) as RecyclerView
        tvTitle = view.findViewById<View>(R.id.spinner_textView) as TextView
        linCancle = view.findViewById<View>(R.id.linCancle) as LinearLayout
        ivCancel = view.findViewById<View>(R.id.ivCancel) as ImageView
        linSeach = view.findViewById<View>(R.id.linSeach) as LinearLayout
        if(isCountryCode){
            tvTitle!!.text = "Select"
        }else{
            tvTitle!!.text = "Select"
        }
        countryAdapter = context?.let { DialogAdapter(it, countryList, this,isCountryCode) }
        val manager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        recyclerView!!.layoutManager = manager
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = countryAdapter
        countryAdapter?.listener ={ position, value ->
            if (value != null) {
                selectedCountry = value
                listener?.onSubmitData(selectedCountry,isCountryCode,action)
            }
        }

        searchView!!.setOnClickListener{
            searchView!!.isIconified = false
            requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        /* ivCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancleDialog();
            }
        });*/

        linSeach!!.setOnClickListener { v: View? ->  searchView!!.isIconified = false }


      /*  linSeach!!.setOnClickListener(View.OnClickListener {
            searchView!!.isIconified = false }
        )*/
        ivCancel!!.setOnClickListener { v: View? ->
            requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            listener?.onCancleDialog() }
        searchView!!.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                countryList?.clear()
                val numberRegex : Pattern =  Pattern.compile("\\d+")
                try {
                    if (TextUtils.isEmpty(newText)) { //cityList = cityListSearch;
                        countryListSearch?.let { countryList?.addAll(it) }
                    } else {
                        for (model : SupplierDatum? in countryListSearch!!) {
                            if (model?.optionName?.toLowerCase()?.contains(newText.toLowerCase())!!) {
                                countryList!!.add(model)
                            }
                            //search base on country code also
                            if(isCountryCode){
                                if(numberRegex.matcher(newText).find()){
                                if(model?.optionName?.toLowerCase()?.contains(newText.toLowerCase())!!) {
                                    countryList!!.add(model)
                                }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.d("CountryDialog--->", e.toString())
                    e.printStackTrace()
                }
                countryAdapter!!.notifyDataSetChanged()
                return false
            }
        })
        return view
    }

    /* Get country after selection of cityList */
    override fun GetSelectedPosition(pos: Int, model: SupplierDatum?) {
        if (model != null) {
            selectedCountry = model
            listener?.onSubmitData(selectedCountry,isCountryCode,action)
        }
    }
}
