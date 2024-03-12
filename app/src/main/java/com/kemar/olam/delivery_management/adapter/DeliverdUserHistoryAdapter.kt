package com.kemar.olam.delivery_management.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.delivery_management.model.response.LogsUserHistoryRes

class DeliverdUserHistoryAdapter (val context : Context, val dataList: ArrayList<LogsUserHistoryRes.UserHist?>, val userRole:String) : RecyclerView.Adapter<DeliverdUserHistoryAdapter.ViewHolder>() {

    var onHeaderClick: ((LogsUserHistoryRes.UserHist, Int) -> Unit)? = null


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var index  = position + 1


        holder?.txtDate?.text = dataList[position]?.deliveryDate
        holder?.txtWagonNo ?.text = dataList[position]?.truckName
     //   holder?.txtForest?.text = dataList[position]?.supplierShortName
        holder?.txtBordereuDate?.text = dataList[position]?.deliveryDate
      //  holder?.txtGradeName_?.text = dataList[position]?.gradeName
        holder?.txtCustomerName_?.text = dataList[position]?.customerShortName
        holder?.txtDeliveryNo?.text = dataList[position]?.deliveryNumber
        holder?.txtTruckNo?.text = dataList[position]?.truckName


       /* if(dataList[position]?.module.isNullOrEmpty()){
            holder?.linBoDate_.visibility = View.VISIBLE
            holder?.linBoNo_.visibility = View.VISIBLE

        }else{
            holder?.linBoDate_.visibility = View.GONE
            holder?.linBoNo_.visibility = View.GONE
        }*/


      /*  if(dataList[position]?.bordereauNo.toString().isNullOrEmpty()){
            holder?.txtBordereuNo.text =  dataList[position]?.eBordereauNo
        }else{
            holder?.txtBordereuNo?.text = dataList[position]?.bordereauNo

        }*/

        holder.linStatus.visibility = View.VISIBLE
        holder.txtStatus.text = context.getString(R.string.delivered)


        if(position==0)
        {
            holder.relvHeader.visibility = View.VISIBLE
            holder?.txttitle.visibility = View.VISIBLE
        }else{
            holder?.txttitle.visibility = View.INVISIBLE
            if(dataList.get(position)?.deliveryDate != dataList.get(position-1)?.deliveryDate ) {
                holder.relvHeader.visibility = View.VISIBLE
            }else{
                holder.relvHeader.visibility = View.GONE
            }
        }

        holder?.linStatus.setOnClickListener{
            dataList[position]?.let { it1 -> onHeaderClick?.invoke(it1,position) }
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup?.context).inflate(R.layout.row_item_delivered_list_history, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtWagonNo =  itemView.findViewById<TextView>(R.id.txtWagonNo)
        val txttitle =  itemView.findViewById<TextView>(R.id.txttitle)
        val txtStatus =  itemView.findViewById<TextView>(R.id.txtStatus)
        val txtDate =  itemView.findViewById<TextView>(R.id.txtDate)
        val txtBordereuNo = itemView.findViewById<TextView>(R.id.txtBordereuNo)
        val txtForest =  itemView.findViewById<TextView>(R.id.txtForest)
        val txtBordereuDate =  itemView.findViewById<TextView>(R.id.txtBordereuDate)
        val txtTruckNo =  itemView.findViewById<TextView>(R.id.txtTruckNo)
        val txtDeliveryNo =  itemView.findViewById<TextView>(R.id.txtDeliveryNo)
        val txtCustomerName_ =  itemView.findViewById<TextView>(R.id.txtCustomerName_)
        val txtGradeName_ =  itemView.findViewById<TextView>(R.id.txtGradeName_)


        val linStatus = itemView.findViewById<LinearLayout>(R.id.linStatus)
        val linFooter = itemView.findViewById<LinearLayout>(R.id.linFooter)
        val relvHeader = itemView.findViewById<RelativeLayout>(R.id.relvHeader)

        val linBoDate_ = itemView.findViewById<LinearLayout>(R.id.linBoDate_)
        val linBoNo_ = itemView.findViewById<LinearLayout>(R.id.linBoNo_)
        val linWagonNo_ = itemView.findViewById<LinearLayout>(R.id.linWagonNo_)

    }
}


