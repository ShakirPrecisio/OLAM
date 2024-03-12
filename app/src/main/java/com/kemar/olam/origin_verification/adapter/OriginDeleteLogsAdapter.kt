package com.kemar.olam.origin_verification.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.BodereuLogListing

class OriginDeleteLogsAdapter(val context : Context, val dataList: ArrayList<BodereuLogListing?>) : RecyclerView.Adapter<OriginDeleteLogsAdapter.ViewHolder>() {

    //var onMoreClick: ((BodereuLogListing, Int, Boolean) -> Unit)? = null
    var onRightClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onPrintClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onEditClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onDeleteClick: ((BodereuLogListing, Int) -> Unit)? = null

    override fun onBindViewHolder(holder: OriginDeleteLogsAdapter.ViewHolder, position: Int) {
        var index  = position + 1
        holder?.txtSerial?.text = index.toString()//context.getString(R.string.logcount_log_no,index)//index.toString()+"."+"Log No : "

        holder?.txt_log_no?.text =  dataList[position]?.getLogNo()

        holder?.txtPlaque_no?.text =  dataList[position]?.getPlaqNo()
        holder?.txtEssence?.text = dataList[position]?.getLogSpeciesName().toString()
        /* var quality  = if (dataList[position].getQuality().isNullOrEmpty()) "" else  dataList[position].getQuality().toString()*/
        var cbm  = if (dataList[position]?.getCbm() ==null) "" else  dataList[position]?.getCbm()?.toString()
        holder?.txtQunatity?.text = cbm
        holder?.txtDia?.text = dataList[position]?.getDiamBdx().toString()
        holder?.txtLong?.text = dataList[position]?.getLongBdx().toString()
        holder?.txtCBM ?.text = dataList[position]?.getCbm().toString()
        holder?.txtBordereuNo ?.text = dataList[position]?.getEBordereauNo().toString()
        holder?.txtWagonNo ?.text = dataList[position]?.getWagonNo().toString()
        holder?.txtSupplierShortName ?.text = dataList[position]?.getSupplierShortName().toString()


        holder.chkBox?.isChecked = dataList[position]?.isSelected!!

        holder.chkBox?.setOnClickListener {
            dataList[position]?.isSelected = !dataList[position]?.isSelected!!
            notifyDataSetChanged()
        }

        if (dataList[position]?.isExpanded!!) {
            holder.linFooter.visibility = View.VISIBLE
        } else {
            holder.linFooter.visibility = View.GONE

        }

      /*  holder?.ivRight.setOnClickListener({
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
        })*/

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): OriginDeleteLogsAdapter.ViewHolder {
        val v = LayoutInflater.from(viewGroup?.context).inflate(R.layout.row_item_origin_deleted_logs_listing, viewGroup, false)
        return OriginDeleteLogsAdapter.ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtSerial =  itemView.findViewById<TextView>(R.id.txtSerial)
        val txt_log_no = itemView.findViewById<TextView>(R.id.txt_log_no)
        val txtPlaque_no =  itemView.findViewById<TextView>(R.id.txtPlaque_no)
        val txtEssence = itemView.findViewById<TextView>(R.id.txtEssence)
        val txtBordereuNo = itemView.findViewById<TextView>(R.id.txtBordereuNo)
        val txtWagonNo = itemView.findViewById<TextView>(R.id.txtWagonNo)
        val txtSupplierShortName = itemView.findViewById<TextView>(R.id.txtSupplierShortName)
        val txtQunatity =  itemView.findViewById<TextView>(R.id.txtQunatity)
        val txtDia =  itemView.findViewById<TextView>(R.id.txtDia)
        val txtLong = itemView.findViewById<TextView>(R.id.txtLong)
        val txtCBM =  itemView.findViewById<TextView>(R.id.txtCBM)
        val chkBox: CheckBox? = itemView.findViewById(R.id.chkBox)
        val linFooter = itemView.findViewById<LinearLayout>(R.id.linFooter)
        //  val ivMore = itemView.findViewById<ImageView>(R.id.ivMore)
        val  ivRight=  itemView.findViewById<ImageView>(R.id.ivRight)
        val  ivPrint=  itemView.findViewById<ImageView>(R.id.ivPrint)
        val  ivEdit=  itemView.findViewById<ImageView>(R.id.ivEdit)
        val  ivDelete=  itemView.findViewById<ImageView>(R.id.ivDelete)


    }
}


