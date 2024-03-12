package com.kemar.olam.origin_verification.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes


class OriginTodaysHistoryAdapter(val context : Context, val dataList: ArrayList<LogsUserHistoryRes.BordereauRecordList?>) : RecyclerView.Adapter<OriginTodaysHistoryAdapter.ViewHolder>() {

    var onHeaderClick: ((LogsUserHistoryRes.BordereauRecordList, Int) -> Unit)? = null


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var index  = position + 1

        holder?.txtDate?.text = dataList[position]?.bordereauDateString
        holder?.txtForestWagonNo ?.text = dataList[position]?.wagonNo
        holder?.txtForest ?.text = dataList[position]?.supplierShortName

        holder?.layoutForest ?.visibility = View.GONE

        if(dataList[position]?.bordereauNo.toString().isNullOrEmpty()){
            holder?.linManual.visibility =  View.GONE
            holder?.txtElectronicBO_NO.text =  dataList[position]?.eBordereauNo
        }else{
            holder?.linManual.visibility =  View.VISIBLE
            holder?.txtBO_NO?.text = dataList[position]?.bordereauNo
            holder?.txtElectronicBO_NO.text =  dataList[position]?.eBordereauNo

        }

        if(dataList[position]?.logQty?.toString().isNullOrEmpty()){
            holder?.txtNoOfLogs ?.text ="0"
        }else{
            holder?.txtNoOfLogs ?.text = dataList[position]?.logQty?.toString()
        }

            if (dataList[position]?.verifyFlag.isNullOrEmpty()){
                holder?.linStatus?.visibility = View.GONE
                holder?.linActionStatus?.visibility = View.VISIBLE
                holder?.txtActionStatus?.text  = "In Transit"
            }else{
                holder?.linStatus?.visibility = View.VISIBLE
                holder?.linActionStatus?.visibility = View.GONE
                holder?.txtStatus?.text  = context.getString(R.string.recieved)
                holder?.linStatus?.setBackgroundColor(context.resources.getColor(R.color.loading_active_color))
            }



      //  dataList[position]?.transportMode?.let { setupTransportMode(it,holder,context) }
        if(position==0)
        {
            holder.relvHeader.visibility = View.VISIBLE
            holder?.txttitle.visibility = View.VISIBLE
        }else{
            holder?.txttitle.visibility = View.INVISIBLE
            if(dataList.get(position)?.bordereauDateString != dataList.get(position-1)?.bordereauDateString ) {
                holder.relvHeader.visibility = View.VISIBLE
            }else{
                holder.relvHeader.visibility = View.GONE
            }
        }


        holder?.linActionStatus.setOnClickListener{
            dataList[position]?.let { it1 -> onHeaderClick?.invoke(it1,position) }
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup?.context).inflate(R.layout.row_item_origin_veriification_history, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

 /*   fun setupTransportMode(transportMode:Int,holder:ViewHolder,context: Context){
        when(transportMode){
            1 -> {
                holder?.txtTrasnporterTtile.text = context.resources.getString(R.string._wagon_no)
                holder?.ivTransportMode.setImageResource(R.drawable.ic_wagon)
            }
            17 -> {
                holder?.txtTrasnporterTtile.text = context.resources.getString(R.string._barge_no)
                holder?.ivTransportMode.setImageResource(R.drawable.ic_barge)
            }
            3 -> {
                holder?.txtTrasnporterTtile.text = context.resources.getString(R.string._truck_no)
                holder?.ivTransportMode.setImageResource(R.drawable.ic_truck)
            }else-> {

        }
        }
    }*/


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txttitle =  itemView.findViewById<TextView>(R.id.txttitle)
        val txtTrasnporterTtile =  itemView.findViewById<TextView>(R.id.txtTrasnporterTtile)
        val txtDate = itemView.findViewById<TextView>(R.id.txtDate)
        val txtForestWagonNo =  itemView.findViewById<TextView>(R.id.txtForestWagonNo)
        val linStatus = itemView.findViewById<LinearLayout>(R.id.linStatus)
        val linActionStatus = itemView.findViewById<LinearLayout>(R.id.linActionStatus)
        val txtActionStatus =  itemView.findViewById<TextView>(R.id.txtActionStatus)
        val txtBO_NO = itemView.findViewById<TextView>(R.id.txtBO_NO)
        val txtForest = itemView.findViewById<TextView>(R.id.txtForest)
        val txtElectronicBO_NO = itemView.findViewById<TextView>(R.id.txtElectronicBO_NO)
        val txtNoOfLogs =  itemView.findViewById<TextView>(R.id.txtNoOfLogs)
        val txtStatus =  itemView.findViewById<TextView>(R.id.txtStatus)
        val relvParent = itemView.findViewById<RelativeLayout>(R.id.relvParent)
        val relvHeader = itemView.findViewById<RelativeLayout>(R.id.relvHeader)
        val ivTransportMode = itemView.findViewById<ImageView>(R.id.ivTransportMode)
        val linElectronic = itemView.findViewById<LinearLayout>(R.id.linElectronic)
        val linManual = itemView.findViewById<LinearLayout>(R.id.linManual)
        val layoutForest = itemView.findViewById<LinearLayout>(R.id.layoutForest)


    }
}


