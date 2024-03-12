package com.kemar.olam.dialog_fragment.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.dashboard.models.responce.SupplierDatum
import com.kemar.olam.dialog_fragment.fragment.DialogFragment
import java.lang.Exception
import java.util.*

class DialogAdapter(
    val context: Context, val countryList: ArrayList<SupplierDatum?>?,
    val countryDialogFragment: DialogFragment,
    val isCountryCode:Boolean) : RecyclerView.Adapter<DialogAdapter.MyHolder>() {
    var listener: ((position : Int, SupplierDatum) -> Unit)? = null

    interface DialogListener {
        fun GetSelectedPosition(pos: Int, model: SupplierDatum?)
    }


    class MyHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var tvName: TextView
       // var cardHeader: CardView

        init {
            tvName = itemView.findViewById(R.id.spinner_textView)
          //  cardHeader = itemView.findViewById(R.id.cardHeader)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): MyHolder {
        val view: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_country_dialog_adapter, viewGroup, false)
        return MyHolder(view)
    }


    override fun onBindViewHolder(myHolder: MyHolder, i: Int) {
        if (!TextUtils.isEmpty(countryList!![i]?.optionName.toString()))
            if (isCountryCode) {
                myHolder.tvName.text =
                    "(" + "+" + countryList[i]!!.optionName + ")" + " " + countryList[i]!!.optionName
            } else {
                myHolder.tvName.setText(countryList[i]!!.optionName)
            }
        myHolder.tvName.setOnClickListener {
            try {
                countryList!![i]?.let { it1 -> listener?.invoke(i, it1) }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount(): Int {
        return countryList!!.size
    }


}
