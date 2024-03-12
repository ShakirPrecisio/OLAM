package com.kemar.olam.loading_wagons.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.loading_wagons.model.responce.GetLogDataByBarcodeRes

class MultiLogsListAdapter (val context : Context, val dataList: List<BodereuLogListing>) : RecyclerView.Adapter<MultiLogsListAdapter.ViewHolder>() {

    var onItemClick: ((BodereuLogListing) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var data= dataList.get(position)

        holder.tvDia.setText(data.getDiamBdx().toString())
        holder.tvCBM.setText(data.getCbm().toString())
        holder.tvLong.setText(data.getLongBdx().toString())
        holder.tvForest.setText(data.getcustomerPurchasedFromForest().toString())
        holder.tvlogSpecies.setText(data.getLogSpeciesName().toString())



        holder.linear_data.setOnClickListener{
            holder.checkbox.isChecked=true
            onItemClick!!.invoke(data)
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup?.context).inflate(R.layout.row_item__multi_logs, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return dataList?.size!!
    }



    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val tvDia =  itemView.findViewById<TextView>(R.id.tvDia)
        val tvCBM = itemView.findViewById<TextView>(R.id.tvCBM)
        val tvLong =  itemView.findViewById<TextView>(R.id.tvLong)
        val tvForest =  itemView.findViewById<TextView>(R.id.tvForest)
        val tvlogSpecies = itemView.findViewById<TextView>(R.id.tvlogSpecies)
        val checkbox = itemView.findViewById<CheckBox>(R.id.checkbox)
        var linear_data=itemView.findViewById<LinearLayout>(R.id.linear_data)
    }
}


