package com.kemar.olam.bordereau.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes

class LogsTodaysHistoryAdapter(val context : Context, val dataList: ArrayList<LogsUserHistoryRes.BordereauRecordList?>,val isBcRePrint:Boolean,val isOffline:Boolean) : RecyclerView.Adapter<LogsTodaysHistoryAdapter.ViewHolder>() {

    var onHeaderClick: ((LogsUserHistoryRes.BordereauRecordList, Int) -> Unit)? = null


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.txtDate?.text = dataList[position]?.bordereauDateString
        holder.txtForestWagonNo?.text = dataList[position]?.wagonNo

        holder.txtBO_NO?.text = dataList[position]?.bordereauNo
        if(dataList[position]?.logQty?.toString().isNullOrEmpty()){
            holder.txtNoOfLogs?.text ="0"
        }else{
            holder.txtNoOfLogs?.text = dataList[position]?.logQty?.toString()
        }

        if(isOffline){
            holder.ivsync.visibility=View.VISIBLE

            if(dataList[position]?.isUpload!!){
                holder.ivsync?.setBackgroundResource(R.drawable.iv_sync_done)
            }else if(dataList.get(position)?.isFail!!){
                holder.ivsync?.setBackgroundResource(R.drawable.ivcancel)
            }else{
                holder.ivsync?.setBackgroundResource(R.drawable.ivsync)
            }


        }else{
            holder.ivsync.visibility=View.GONE
        }



        holder.ivsync.setOnClickListener {
            if(dataList.get(position)!!.isFail!!){
                Toast.makeText(context, dataList.get(position)!!.failreason, Toast.LENGTH_SHORT).show()
            }
        }


        holder.txtActionStatus?.text = dataList[position]?.headerStatus
        holder.txtStatus?.text = dataList[position]?.headerStatus
        if(isBcRePrint){
            if (dataList[position]?.headerStatus?.equals("At-Hub", ignoreCase = true)!!) {
                holder.linStatus?.visibility = View.GONE
                holder.linActionStatus?.visibility = View.VISIBLE
                holder.linStatus?.setBackgroundColor(ContextCompat.getColor(context,R.color.loading_active_color))
            } else {
                holder.linStatus?.visibility = View.VISIBLE
                holder.linActionStatus?.visibility = View.GONE
                holder.linStatus?.setBackgroundColor(ContextCompat.getColor(context,R.color.loading_active_color))
            }
        }else{
            if (dataList[position]?.headerStatus?.equals("At-Hub", ignoreCase = true)!!) {
                holder.linStatus?.visibility = View.VISIBLE
                holder.linActionStatus?.visibility = View.GONE
                holder.linStatus?.setBackgroundColor(ContextCompat.getColor(context,R.color.loading_active_color))
            } else {
                holder.linStatus?.visibility = View.GONE
                holder.linActionStatus?.visibility = View.VISIBLE
                holder.linStatus?.setBackgroundColor(ContextCompat.getColor(context,R.color.loading_active_color))
            }

        }

        dataList[position]?.transportMode?.let { setupTransportMode(it,holder,context) }

        if(position==0)
        {
            holder.relvHeader?.visibility = View.VISIBLE
            holder.txttitle?.visibility = View.VISIBLE
        }else{
            holder.txttitle?.visibility = View.INVISIBLE
            if(dataList.get(position)?.bordereauDateString != dataList.get(position-1)?.bordereauDateString ) {
                holder.relvHeader?.visibility = View.VISIBLE
            }else{
                holder.relvHeader?.visibility = View.GONE
            }
        }


        holder.linStatus?.setOnClickListener{
            dataList[position]?.let { it1 -> onHeaderClick?.invoke(it1,position) }
        }


        holder.linActionStatus?.setOnClickListener{
            dataList[position]?.let { it1 -> onHeaderClick?.invoke(it1,position) }
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_item_bodreu_history, viewGroup, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setupTransportMode(transportMode:Int,holder:ViewHolder,context: Context){
        when(transportMode){
            1 -> {
                holder.txtTrasnporterTtile?.text = context.resources.getString(R.string._wagon_no)
                holder.ivTransportMode?.setImageResource(R.drawable.ic_wagon)
            }
            17 -> {
                holder.txtTrasnporterTtile?.text = context.resources.getString(R.string._barge_no)
                holder.ivTransportMode?.setImageResource(R.drawable.ic_barge)
            }
            3 -> {
                holder.txtTrasnporterTtile?.text = context.resources.getString(R.string._truck_no)
                holder.ivTransportMode?.setImageResource(R.drawable.ic_truck)
            }else-> {

            }
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txttitle: TextView? =  itemView.findViewById(R.id.txttitle)
        val txtTrasnporterTtile: TextView? =  itemView.findViewById(R.id.txtTrasnporterTtile)
        val txtDate: TextView? = itemView.findViewById(R.id.txtDate)
        val txtForestWagonNo: TextView? =  itemView.findViewById(R.id.txtForestWagonNo)
        val linStatus: LinearLayout? = itemView.findViewById(R.id.linStatus)
        val txtBO_NO: TextView? = itemView.findViewById(R.id.txtBO_NO)
        val txtNoOfLogs: TextView? =  itemView.findViewById(R.id.txtNoOfLogs)
        val txtStatus: TextView? =  itemView.findViewById(R.id.txtStatus)
        val relvParent: RelativeLayout? = itemView.findViewById(R.id.relvParent)
        val relvHeader: RelativeLayout? = itemView.findViewById(R.id.relvHeader)
        val ivTransportMode: ImageView? = itemView.findViewById(R.id.ivTransportMode)
        var ivsync:ImageView=itemView.findViewById(R.id.ivsync)

        val linActionStatus = itemView.findViewById<LinearLayout>(R.id.linActionStatus)
        val txtActionStatus =  itemView.findViewById<TextView>(R.id.txtActionStatus)

    }
}


