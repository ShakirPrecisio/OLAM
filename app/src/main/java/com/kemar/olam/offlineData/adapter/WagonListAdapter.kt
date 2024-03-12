package com.kemar.olam.offlineData.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes
import com.kemar.olam.utility.Constants

class WagonListAdapter(val context : Context, val dataList: ArrayList<LogsUserHistoryRes.BordereauRecordList?>, var isOffline:Boolean) : RecyclerView.Adapter<WagonListAdapter.ViewHolder>() {

    var onHeaderClick: ((LogsUserHistoryRes.BordereauRecordList, Int) -> Unit)? = null
    var onReprintClick: ((LogsUserHistoryRes.BordereauRecordList, Int) -> Unit)? = null


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var index  = position + 1

        holder.txtDate?.text = dataList[position]?.bordereauDateString
        holder.txtForestWagonNo ?.text = dataList[position]?.wagonNo

        if(isOffline){
            holder.linSync.visibility= View.VISIBLE
            if(dataList.get(position)?.isUpload!!){
                holder.ivsync?.setBackgroundResource(R.drawable.iv_sync_done)
                holder.txtActionStatus?.text = "Sync Done"
                holder.linActionStatus.background=context.getDrawable(R.drawable.bg_curve_action_button)
                holder.tvsyncstatus.setText("Sync Done")
            }else if(dataList.get(position)?.isFail!!){
                holder.ivsync?.setBackgroundResource(R.drawable.ivcancel)
                holder.linActionStatus.background=context.getDrawable(R.drawable.bg_curve_red_action_button)
                holder.tvsyncstatus.setText("Sync Fail")
                holder.txtActionStatus?.text = "Sync Fail"
                holder.tvsyncstatus.setTextColor(Color.RED)
            }else{
                holder.ivsync?.setBackgroundResource(R.drawable.ivsync)
                holder.tvsyncstatus.text = if(position == 0) "Syncing" else "To Be Synced"
                holder.linActionStatus.background=context.getDrawable(R.drawable.bg_curve_action_button)
                holder.txtActionStatus?.text = if(position == 0) "Syncing" else "To Be Synced"
            }
        }else{
            holder.linSync.visibility= View.GONE
        }


        holder.ivsync.setOnClickListener {
            if(dataList.get(position)!!.isFail!!){
                Toast.makeText(context, dataList.get(position)!!.failreason, Toast.LENGTH_SHORT).show()
            }
        }



        if(dataList[position]?.bordereauNo.toString().isNullOrEmpty()){
            holder.linManual.visibility =  View.GONE
            holder.txtElectronicBO_NO.text =  dataList[position]?.eBordereauNo
        }else{
            holder.linManual.visibility =  View.VISIBLE
            holder.txtBO_NO?.text = dataList[position]?.bordereauNo
            holder.txtElectronicBO_NO.text =  dataList[position]?.eBordereauNo

        }

        if(dataList[position]?.logQty?.toString().isNullOrEmpty()){
            holder.txtNoOfLogs?.text ="0"
        }else{
            holder.txtNoOfLogs?.text = dataList[position]?.logQty?.toString()
        }


        try {
            if (dataList[position]?.loadingStatus != null) {
                holder.txtStatus?.text = dataList[position]?.loadingStatus
                //holder.txtActionStatus?.text = dataList[position]?.loadingStatus

                if (dataList[position]?.loadingStatus?.equals(Constants.IN_TRANSIT, ignoreCase = true)!!) {
                    holder.linStatus?.setBackgroundColor(context.resources.getColor(R.color.loading_active_color))
                    holder.linStatus?.visibility = View.GONE
                    holder.linActionStatus?.visibility = View.GONE
                    holder.linReprint?.visibility = View.GONE
                } else if (dataList[position]?.loadingStatus?.equals(
                                Constants.LOADED,
                                ignoreCase = true
                        )!!
                ) {
                    holder.linStatus?.setBackgroundColor(context.resources.getColor(R.color.loading_active_color))
                    holder.linStatus?.visibility = View.GONE
                    holder.linReprint?.visibility = View.GONE
                    holder.linActionStatus?.visibility = View.GONE
                } else {
                    holder.linStatus?.visibility = View.GONE
                    holder.linReprint?.visibility = View.GONE
                    holder.linActionStatus?.visibility = View.VISIBLE
                }
            }else{
                holder.linStatus?.visibility = View.GONE
                holder.linActionStatus?.visibility = View.VISIBLE
                holder.linReprint?.visibility = View.GONE
            }
        }catch (e:Exception){
            e.printStackTrace()
        }


        // dataList[position]?.transportMode?.let { setupTransportMode(it,holder,context) }
        if(position==0)
        {
            holder.relvHeader?.visibility = View.VISIBLE
            holder.txttitle?.visibility = View.INVISIBLE
        }else{
            holder.txttitle.visibility = View.INVISIBLE
            if(dataList.get(position)?.bordereauDateString != dataList.get(position-1)?.bordereauDateString ) {
                holder.relvHeader?.visibility = View.VISIBLE
            }else{
                holder.relvHeader?.visibility = View.GONE
            }
        }


        holder.linFooter?.setOnClickListener {
            dataList[position]?.let { it1 -> onHeaderClick?.invoke(it1,position) }
        }

        holder.linStatus?.setOnClickListener {
            dataList[position]?.let { it1 -> onHeaderClick?.invoke(it1,position) }
        }


        holder.linReprint?.setOnClickListener {
            dataList[position]?.let { it1 -> onReprintClick?.invoke(it1,position) }
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup?.context).inflate(R.layout.row_item_loading_wagons_offline_history, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

/*    fun setupTransportMode(transportMode:Int,holder:ViewHolder,context: Context){
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
        val linReprint= itemView.findViewById<LinearLayout>(R.id.linReprint)
        val txtBO_NO = itemView.findViewById<TextView>(R.id.txtBO_NO)
        val txtElectronicBO_NO = itemView.findViewById<TextView>(R.id.txtElectronicBO_NO)
        val txtNoOfLogs =  itemView.findViewById<TextView>(R.id.txtNoOfLogs)
        val txtStatus =  itemView.findViewById<TextView>(R.id.txtStatus)
        val txtActionStatus =  itemView.findViewById<TextView>(R.id.txtActionStatus)
        val txtReprint=  itemView.findViewById<TextView>(R.id.txtReprint)
        val relvParent = itemView.findViewById<RelativeLayout>(R.id.relvParent)
        val relvHeader = itemView.findViewById<RelativeLayout>(R.id.relvHeader)
        val ivTransportMode = itemView.findViewById<ImageView>(R.id.ivTransportMode)
        val linElectronic = itemView.findViewById<LinearLayout>(R.id.linElectronic)
        val linManual = itemView.findViewById<LinearLayout>(R.id.linManual)
        var ivsync=itemView.findViewById<ImageView>(R.id.ivsync)
        var linFooter=itemView.findViewById<LinearLayout>(R.id.linFooter)
        var linSync=itemView.findViewById<LinearLayout>(R.id.linSync)
        var tvsyncstatus=itemView.findViewById<TextView>(R.id.tvsyncstatus)


    }
}


