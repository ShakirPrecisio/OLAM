package com.kemar.olam.sales_and_inspection.ground_sales.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes

class GroundHistoryAdapter (val context : Context, val dataList: ArrayList<LogsUserHistoryRes.BordereauGroundList?>, val userRole:String) : RecyclerView.Adapter<GroundHistoryAdapter.ViewHolder>() {

    var onHeaderClick: ((LogsUserHistoryRes.BordereauGroundList, Int) -> Unit)? = null
    var onRePrintInvoiceClick: ((LogsUserHistoryRes.BordereauGroundList, Int) -> Unit)? = null
    var onInspectionOrNoInspectionClick: ((LogsUserHistoryRes.BordereauGroundList, Int, Boolean) -> Unit)? = null


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.txtHeaderDate?.text = dataList[position]?.inspectionDateStr
        holder.txtDate?.text =  dataList[position]?.inspectionDateStr
        holder?.txtGradeNamme.text = dataList[position]?.gradeName


        if(dataList[position]?.inspectionNumber.isNullOrEmpty()){
            holder.txtInspectionNo?.visibility =  View.GONE
        }else{
            holder.txtInspectionNo?.visibility =  View.VISIBLE
            holder.txtInspectionNo?.text = dataList[position]?.inspectionNumber
        }



        //status handle
        when(dataList[position]?.inspectionFlag) {
            /* inspectionFlag
                 null - Inspection not created
             S - header created
                     SA - header and log created and sent for admin approval
                     Y- Inspection done*/
            "S",null ->{
                //show user wise Functions
                when(userRole) {
                    "SuperUserApp", "Super User", "admin" -> {
                        holder.linInspection?.visibility = View.VISIBLE
                       /* holder.linNoInspection.visibility = View.VISIBLE*/
                        holder.linStatus?.visibility = View.GONE
                        holder.linReprint?.visibility= View.GONE
                    }
                    else -> {
                        holder.linInspection.visibility = View.VISIBLE
                      /*  holder.linNoInspection.visibility = View.GONE*/
                        holder.linStatus?.visibility = View.GONE
                        holder.linReprint?.visibility= View.GONE
                    }
                }
            }
            "SA" ->{
                holder.linStatus?.visibility = View.VISIBLE
                holder.linReprint?.visibility= View.GONE
                holder.linInspection?.visibility = View.GONE
              /*  holder.linNoInspection.visibility = View.GONE*/
                holder.linStatus?.setBackgroundColor(ContextCompat.getColor(context, R.color.loading_active_color))
                holder.txtStatus?.text = "Pending"
            }
            "Y" ->{
                holder.linStatus?.setBackgroundColor(ContextCompat.getColor(context, R.color.loading_active_color))
                holder.txtStatus?.text = "In Transit"
                holder.linReprint?.visibility= View.VISIBLE
                holder.linStatus?.visibility = View.VISIBLE
                holder.linInspection?.visibility = View.GONE
               /* holder.linNoInspection.visibility = View.GONE*/
            }

        }

        /*   if(!dataList[position]?.loadingStatus?.isNullOrEmpty()!!) {
               holder?.txtStatus?.text = dataList[position]?.loadingStatus

               if (dataList[position]?.loadingStatus?.equals("In Transit", ignoreCase = true)!!) {

                   holder?.linStatus?.setBackgroundColor(context.resources.getColor(R.color.loading_inactive_color))
               }
               else if (dataList[position]?.loadingStatus?.equals("Loaded", ignoreCase = true)!!) {
                   holder.linStatus?.setBackgroundColor(context.resources.getColor(R.color.loading_active_color))
               }
               else {
                   holder.linStatus?.setBackgroundColor(context.resources.getColor(R.color.loading_active_color))
               }
           }*/

        if(position==0)
        {
            holder.relvHeader?.visibility = View.VISIBLE
            holder.txttitle?.visibility = View.VISIBLE
        }else{
            holder.txttitle?.visibility = View.INVISIBLE
            if(dataList.get(position)?.inspectionDateStr != dataList.get(position-1)?.inspectionDateStr ) {
                holder.relvHeader?.visibility = View.VISIBLE
            }else{
                holder.relvHeader?.visibility = View.GONE
            }
        }


        holder.linInspection?.setOnClickListener{
            dataList[position]?.let { it1 -> onInspectionOrNoInspectionClick?.invoke(it1,position,true) }
        }

       /* holder?.linNoInspection.setOnClickListener{
            dataList[position]?.let { it1 -> onInspectionOrNoInspectionClick?.invoke(it1,position,false) }
        }*/


        holder.linReprint?.setOnClickListener{
            dataList[position]?.let { it1 -> onRePrintInvoiceClick?.invoke(it1,position) }
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_item_ground_insspections_history, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtInspectionNo = itemView.findViewById<TextView>(R.id.txtInspectionNo)
        val txttitle =  itemView.findViewById<TextView>(R.id.txttitle)
        val txtStatus =  itemView.findViewById<TextView>(R.id.txtStatus)
        val txtReprint =  itemView.findViewById<TextView>(R.id.txtReprint)
        val txtHeaderDate =  itemView.findViewById<TextView>(R.id.txtHeaderDate)
        val txtDate = itemView.findViewById<TextView>(R.id.txtDate)
        val txtGradeNamme = itemView.findViewById<TextView>(R.id.txtGradeNamme)
        val linInspection = itemView.findViewById<LinearLayout>(R.id.linInspection)
        val linReprint  =  itemView.findViewById<LinearLayout>(R.id.linReprint)
    /*    val linNoInspection = itemView.findViewById<LinearLayout>(R.id.linNoInspection)*/
        val linStatus = itemView.findViewById<LinearLayout>(R.id.linStatus)
        val linFooter = itemView.findViewById<LinearLayout>(R.id.linFooter)
        val relvHeader = itemView.findViewById<RelativeLayout>(R.id.relvHeader)

    }
}


