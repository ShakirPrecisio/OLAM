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


class GApprovalListAdapter(val context : Context, val dataList: ArrayList<LogsUserHistoryRes.BordereauGroundList?>, val userRole:String) : RecyclerView.Adapter<GApprovalListAdapter.ViewHolder>() {

    var onHeaderClick: ((LogsUserHistoryRes.BordereauGroundList, Int) -> Unit)? = null
    var onInspectionOrNoInspectionClick: ((LogsUserHistoryRes.BordereauGroundList, Int, Boolean) -> Unit)? = null
    var onWaitingForApprovalClick: ((LogsUserHistoryRes.BordereauGroundList, Int) -> Unit)? = null


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var index = position + 1

        holder.txtHeaderDate?.text = dataList[position]?.inspectionDateStr
        holder.txtDate?.text =  dataList[position]?.inspectionDateStr
        holder.txtGradeNamme.text = dataList[position]?.gradeName

        if(dataList[position]?.inspectionNumber.isNullOrEmpty()){
            holder.txtInspectionNo?.visibility =  View.GONE
        }else{
            holder.txtInspectionNo?.visibility =  View.VISIBLE
            holder.txtInspectionNo?.text = dataList[position]?.inspectionNumber
        }


        //status handle
        when (dataList[position]?.inspectionFlag) {
            /* inspectionFlag
                 null - Inspection not created
             S - header created
                     SA - header and log created and sent for admin approval
                     Y- Inspection done*/
            "S", null -> {
                //show user wise Functions
                when (userRole) {
                    "SuperUserApp", "Super User", "admin" -> {
                        holder.linInspection.visibility = View.VISIBLE
                        holder.linNoInspection.visibility = View.VISIBLE
                        holder.linStatus.visibility = View.GONE
                    }
                    else -> {
                        holder.linInspection.visibility = View.VISIBLE
                        holder.linNoInspection.visibility = View.GONE
                        holder.linStatus.visibility = View.GONE
                    }
                }
            }
            "SA" -> {

                when (userRole) {
                    "SuperUserApp", "Super User", "admin" -> {
                        holder.linStatus.visibility = View.GONE
                        holder.linActionStatus.visibility = View.VISIBLE
                        holder.linInspection.visibility = View.GONE
                        holder.linNoInspection.visibility = View.GONE
                        holder?.linStatus?.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.loading_active_color
                            )
                        )
                        holder?.txtActionStatus.text =
                            context.resources.getString(R.string.wating_for_approval)
                    }
                    else -> {
                        holder.linActionStatus.visibility = View.GONE
                        holder.linStatus.visibility = View.VISIBLE
                        holder.linInspection.visibility = View.GONE
                        holder.linNoInspection.visibility = View.GONE
                        holder?.linStatus?.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.loading_active_color
                            )
                        )
                        holder?.txtStatus.text =
                            context.resources.getString(R.string.wating_for_approval)
                    }
                }
            }
            "Y" -> {
                holder?.linStatus?.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.loading_active_color
                    )
                )
                holder?.txtStatus.text = "In Transit"
                holder.linStatus.visibility = View.VISIBLE
                holder.linInspection.visibility = View.GONE
                holder.linNoInspection.visibility = View.GONE
            }

        }


        if (position == 0) {
            holder.relvHeader.visibility = View.VISIBLE
            holder?.txttitle.visibility = View.VISIBLE
        } else {
            holder?.txttitle.visibility = View.INVISIBLE
            if (dataList.get(position)?.inspectionDateStr != dataList.get(position - 1)?.inspectionDateStr) {
                holder.relvHeader.visibility = View.VISIBLE
            } else {
                holder.relvHeader.visibility = View.GONE
            }
        }


        holder?.linInspection.setOnClickListener {
            dataList[position]?.let { it1 ->
                onInspectionOrNoInspectionClick?.invoke(
                    it1,
                    position,
                    true
                )
            }
        }

        holder?.linNoInspection.setOnClickListener {
            dataList[position]?.let { it1 ->
                onInspectionOrNoInspectionClick?.invoke(
                    it1,
                    position,
                    false
                )
            }
        }

        holder?.linActionStatus.setOnClickListener {
            when (dataList[position]?.inspectionFlag) {
                "SA" -> {
                    when (userRole) {
                        "SuperUserApp", "Super User", "admin" -> {
                            dataList[position]?.let { it1 ->
                                onWaitingForApprovalClick?.invoke(
                                    it1,
                                    position
                                )
                            }
                        }
                    }

                }

            }

        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup?.context).inflate(R.layout.row_ground_approval_listing, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtHeaderDate =  itemView.findViewById<TextView>(R.id.txtHeaderDate)
        val txtDate = itemView.findViewById<TextView>(R.id.txtDate)
        val txtGradeNamme = itemView.findViewById<TextView>(R.id.txtGradeNamme)
        val txtInspectionNo = itemView.findViewById<TextView>(R.id.txtInspectionNo)

        val txttitle =  itemView.findViewById<TextView>(R.id.txttitle)
        val txtStatus =  itemView.findViewById<TextView>(R.id.txtStatus)
        val txtActionStatus =  itemView.findViewById<TextView>(R.id.txtActionStatus)
        val txtBordereuDate =  itemView.findViewById<TextView>(R.id.txtBordereuDate)

        val txtForest =  itemView.findViewById<TextView>(R.id.txtForest)
        val linInspection = itemView.findViewById<LinearLayout>(R.id.linInspection)
        val linNoInspection = itemView.findViewById<LinearLayout>(R.id.linNoInspection)
        val linStatus = itemView.findViewById<LinearLayout>(R.id.linStatus)
        val linActionStatus = itemView.findViewById<LinearLayout>(R.id.linActionStatus)
        val linFooter = itemView.findViewById<LinearLayout>(R.id.linFooter)
        val relvHeader = itemView.findViewById<RelativeLayout>(R.id.relvHeader)

    }
}


