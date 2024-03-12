package com.kemar.olam.loading_wagons.adapter

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

class LodingWa_LogsListAdapter (val context : Context, val dataList: ArrayList<BodereuLogListing>) : RecyclerView.Adapter<LodingWa_LogsListAdapter.ViewHolder>() {

    var onMoreClick: ((BodereuLogListing, Int, Boolean) -> Unit)? = null
    var onRightClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onPrintClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onEditClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onDeleteClick: ((BodereuLogListing, Int) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var index  = position + 1
        holder?.txtLog?.text =  index.toString()+"."+"Log No : "

        holder?.txt_log_no?.text =  dataList[position].getLogNo()

        holder?.txtPlaque_no?.text =  dataList[position].getPlaqNo()
        holder?.txtEssence?.text = dataList[position].getLogSpeciesName().toString()
        var quality  = if (dataList[position].getQuality().isNullOrEmpty()) "" else  dataList[position].getQuality().toString()
        holder?.txtQunatity?.text = quality
        holder?.txtDia?.text = dataList[position].getDiamBdx().toString()
        holder?.txtLong?.text = dataList[position].getLongBdx().toString()
        holder?.txtCBM ?.text = dataList[position].getCbm().toString()

        try{
            val totalDia12= dataList[position].getDiamBdx1()!! + dataList[position]?.getDiamBdx2()!!
            val totalDia34= dataList[position]?.getDiamBdx3()!! + dataList[position]?.getDiamBdx4()!!


            var avgDia12= (totalDia12/2).toInt()
            var avgDia34= (totalDia34/2).toInt()


            holder.txtDia1?.text =avgDia12.toString()
            holder.txtDia2?.text = avgDia34.toString()

        }catch (e:Exception){
            e.printStackTrace()
        }




     /*   holder.txtDia1?.text = dataList[position]?.getDiamBdx1().toString()
        holder.txtDia2?.text = dataList[position]?.getDiamBdx2().toString()
        holder.txtDia3?.text = dataList[position]?.getDiamBdx3().toString()
        holder.txtDia4?.text = dataList[position]?.getDiamBdx4().toString()*/


        if (dataList[position].isExpanded) {
            holder.linFooter.visibility = View.VISIBLE
        } else {
            holder?.linFooter.visibility = View.GONE

        }


        holder?.ivMore.setOnClickListener({
            if(dataList[position].isExpanded){
                onMoreClick?.invoke(dataList[position],position,false)
            }else{
                onMoreClick?.invoke(dataList[position],position,true)
            }
        })
        holder?.ivRight.setOnClickListener({
            onRightClick?.invoke(dataList[position],position)
        })
        holder?.ivPrint.setOnClickListener({
            onPrintClick?.invoke(dataList[position],position)
        })
        holder?.ivEdit.setOnClickListener({
            onEditClick?.invoke(dataList[position],position)
        })
        holder?.ivDelete.setOnClickListener({
            onDeleteClick?.invoke(dataList[position],position)
        })

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup?.context).inflate(R.layout.row_item_loading_listing, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtLog =  itemView.findViewById<TextView>(R.id.txtLog)
        val txt_log_no = itemView.findViewById<TextView>(R.id.txt_log_no)
        val txtPlaque_no =  itemView.findViewById<TextView>(R.id.txtPlaque_no)
        val txtEssence = itemView.findViewById<TextView>(R.id.txtEssence)
        val txtQunatity =  itemView.findViewById<TextView>(R.id.txtQunatity)
        val txtDia =  itemView.findViewById<TextView>(R.id.txtDia)
        val txtLong = itemView.findViewById<TextView>(R.id.txtLong)
        val txtCBM =  itemView.findViewById<TextView>(R.id.txtCBM)

        val linFooter = itemView.findViewById<LinearLayout>(R.id.linFooter)
        val ivMore = itemView.findViewById<ImageView>(R.id.ivMore)
        val  ivRight=  itemView.findViewById<ImageView>(R.id.ivRight)
        val  ivPrint=  itemView.findViewById<ImageView>(R.id.ivPrint)
        val  ivEdit=  itemView.findViewById<ImageView>(R.id.ivEdit)
        val  ivDelete=  itemView.findViewById<ImageView>(R.id.ivDelete)


        var txtDia1:TextView? =itemView.findViewById(R.id.txtDia1)
        var txtDia2:TextView? =itemView.findViewById(R.id.txtDia2)
        var txtDia3:TextView? =itemView.findViewById(R.id.txtDia3)
        var txtDia4:TextView? =itemView.findViewById(R.id.txtDia4)
    }
}


