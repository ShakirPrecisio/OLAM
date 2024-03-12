package com.kemar.olam.sales_and_inspection.inspection.adapter

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

class SalesLogsListingAdapter  (val context : Context, val dataList: ArrayList<BodereuLogListing>,val commingFrom:String) : RecyclerView.Adapter<SalesLogsListingAdapter.ViewHolder>() {

    var onMoreClick: ((BodereuLogListing, Int, Boolean) -> Unit)? = null
    var onAddClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onPrintClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onEditClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onDeleteClick: ((BodereuLogListing, Int) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val index  = position + 1
        holder.txtLog?.text = context.getString(R.string.logcount_log_no,index)//"$index.Log No : "

        holder.txt_log_no?.text =  dataList[position].getLogNo()

        holder.txtPlaque_no?.text =  dataList[position].getPlaqNo()
        holder.txtEssence?.text = dataList[position].getLogSpeciesName().toString()
        holder.txtRefraDia?.text = dataList[position].getRefractionDiam()?.toString()
        holder.txtRefraLength?.text = dataList[position].getRefractionLength()?.toString()
        holder.txtGrade?.text = dataList[position].getGradeName()?.toString()
        val quality  = if (dataList[position].getQuality().isNullOrEmpty()) "" else  dataList[position].getQuality().toString()
        holder.txtQunatity?.text = quality
        holder.txtDia?.text = dataList[position].getDiamBdx().toString()
        holder.txtLong?.text = dataList[position].getLongBdx().toString()
        holder.txtCBM?.text = dataList[position].getCbm().toString()


        //check if comming from approval list then add option hide
        //if comming from header & user history then first time add option display 2nd time update option display
        if(commingFrom.equals(context.getString(R.string.approval_history), ignoreCase = true)!!){
            holder.ivEdit?.visibility = View.VISIBLE
            holder.ivAdd?.visibility = View.GONE
        }else {
            if (dataList[position].isEditable) {
                holder.ivEdit?.visibility = View.VISIBLE
                holder.ivAdd?.visibility = View.GONE
            } else {
                holder.ivAdd?.visibility = View.VISIBLE
                holder.ivEdit?.visibility = View.GONE

            }
        }

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
        holder.ivAdd?.setOnClickListener{
            onAddClick?.invoke(dataList[position],position)
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

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_view_sales_logs_liisting, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtLog : TextView? =  itemView.findViewById(R.id.txtLog)
        val txt_log_no : TextView? = itemView.findViewById(R.id.txt_log_no)
        val txtPlaque_no : TextView?  =  itemView.findViewById(R.id.txtPlaque_no)
        val txtEssence : TextView? = itemView.findViewById(R.id.txtEssence)
        val txtQunatity : TextView?  =  itemView.findViewById(R.id.txtQunatity)
        val txtDia : TextView? =  itemView.findViewById(R.id.txtDia)
        val txtLong : TextView? = itemView.findViewById(R.id.txtLong)
        val txtCBM : TextView? =  itemView.findViewById(R.id.txtCBM)

        val txtRefraDia : TextView? =  itemView.findViewById(R.id.txtRefraDia)
        val txtRefraLength : TextView? = itemView.findViewById(R.id.txtRefraLength)
        val txtGrade : TextView? =  itemView.findViewById(R.id.txtGrade)


        val linFooter : LinearLayout? = itemView.findViewById(R.id.linFooter)
        val ivMore   : ImageView? = itemView.findViewById(R.id.ivMore)
        val  ivAdd  : ImageView? =  itemView.findViewById(R.id.ivAdd)
        val  ivPrint  : ImageView?  =  itemView.findViewById(R.id.ivPrint)
        val  ivEdit  : ImageView? =  itemView.findViewById(R.id.ivEdit)
        val  ivDelete : ImageView? =  itemView.findViewById(R.id.ivDelete)
    }
}


