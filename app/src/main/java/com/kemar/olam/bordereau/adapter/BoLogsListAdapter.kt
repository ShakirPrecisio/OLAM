package com.kemar.olam.bordereau.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.utility.RealmHelper
import io.realm.Realm
import java.lang.Exception

class BoLogsListAdapter (val context : Context, val dataList: ArrayList<BodereuLogListing>) : RecyclerView.Adapter<BoLogsListAdapter.ViewHolder>() {

    var onMoreClick: ((BodereuLogListing,Int,Boolean) -> Unit)? = null
    var onRightClick: ((BodereuLogListing,Int) -> Unit)? = null
    var onPrintClick: ((BodereuLogListing,Int) -> Unit)? = null
    var onEditClick: ((BodereuLogListing,Int) -> Unit)? = null
    var onDeleteClick: ((BodereuLogListing,Int) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val index  = position + 1

        holder.txtLog?.text = context.getString(R.string.logcount_log_no,index)//"$index.Log No : "

        holder.txt_log_no?.text =  dataList[position].getLogNo()

        holder.txtPlaque_no?.text =  dataList[position].getPlaqNo()
        holder.txtEssence?.text = dataList[position].getLogSpeciesName().toString()
        val quality  = if (dataList[position].getQuality().isNullOrEmpty()) "" else  dataList[position].getQuality().toString()
        holder.txtQunatity?.text = quality
        holder.txtDia?.text = dataList[position].getDiamBdx().toString()

        try {
            val totalDia12= dataList[position]?.getDiamBdx1()!! + dataList[position]?.getDiamBdx2()!!
            val totalDia34= dataList[position]?.getDiamBdx3()!! + dataList[position]?.getDiamBdx4()!!

            var avgDia12= (totalDia12/2).toInt()
            var avgDia34= (totalDia34/2).toInt()

            holder.txtDia1?.text =avgDia12.toString()
            holder.txtDia2?.text = avgDia34.toString()

            holder.txtDia3?.text = dataList[position]?.getDiamBdx3().toString()
            holder.txtDia4?.text = dataList[position]?.getDiamBdx4().toString()

        }catch (e:Exception){
            e.printStackTrace()
        }


        holder.txtLong?.text = dataList[position].getLongBdx().toString()
        holder.txtCBM?.text = dataList[position].getCbm().toString()


        if (dataList[position].isExpanded) {
            holder.linFooter?.visibility = View.VISIBLE
        } else {
            holder.linFooter?.visibility = View.GONE
        }


        holder.ivMore?.setOnClickListener{
            if(dataList[position].isExpanded){
                onMoreClick?.invoke(dataList[position],position,false)
            }else{
                onMoreClick?.invoke(dataList[position],position,true)
            }
        }
        holder.ivRight?.setOnClickListener{
            onRightClick?.invoke(dataList[position],position)
        }
        holder.ivPrint?.setOnClickListener{
            onPrintClick?.invoke(dataList[position],position)
        }
        holder.ivEdit?.setOnClickListener{
            onEditClick?.invoke(dataList[position],position)
        }
        holder.ivDelete?.setOnClickListener{
            onDeleteClick?.invoke(dataList[position],position)
        }

        if(dataList.get(0).isUploaded){
            holder.ivsync?.setBackgroundResource(R.drawable.iv_sync_done)
        }else{
            holder.ivsync?.setBackgroundResource(R.drawable.ivsync)
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_item_bordereau_listing, viewGroup, false)
       // val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_view_sales_logs_liisting, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtLog :TextView? =  itemView.findViewById(R.id.txtLog)
        val txt_log_no :TextView? = itemView.findViewById(R.id.txt_log_no)
        val txtPlaque_no :TextView?  =  itemView.findViewById(R.id.txtPlaque_no)
        val txtEssence :TextView? = itemView.findViewById(R.id.txtEssence)
        val txtQunatity :TextView?  =  itemView.findViewById(R.id.txtQunatity)
        val txtDia :TextView? =  itemView.findViewById(R.id.txtDia)
        val txtLong :TextView? = itemView.findViewById(R.id.txtLong)
        val txtCBM :TextView? =  itemView.findViewById(R.id.txtCBM)

        val linFooter : LinearLayout? = itemView.findViewById(R.id.linFooter)
        val ivMore   :ImageView? = itemView.findViewById(R.id.ivMore)
        val  ivRight  :ImageView? =  itemView.findViewById(R.id.ivRight)
        val  ivPrint  :ImageView?  =  itemView.findViewById(R.id.ivPrint)
        val  ivEdit  :ImageView? =  itemView.findViewById(R.id.ivEdit)
        val  ivDelete :ImageView? =  itemView.findViewById(R.id.ivDelete)
        val  ivsync :ImageView? =  itemView.findViewById(R.id.ivsync)

        var txtDia1:TextView? =itemView.findViewById(R.id.txtDia1)
        var txtDia2:TextView? =itemView.findViewById(R.id.txtDia2)
        var txtDia3:TextView? =itemView.findViewById(R.id.txtDia3)
        var txtDia4:TextView? =itemView.findViewById(R.id.txtDia4)
    }
}


