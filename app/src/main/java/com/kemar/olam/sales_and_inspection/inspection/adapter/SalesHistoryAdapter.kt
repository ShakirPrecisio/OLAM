package com.kemar.olam.sales_and_inspection.inspection.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes

/*


inspectionFlag
null - Inspection not created
S - header created
SA - header and log created and sent for admin approval
Y- Inspection done


*/


class SalesHistoryAdapter (val context : Context, var dataList: ArrayList<LogsUserHistoryRes.BordereauRecordList?>, val userRole:String) : RecyclerView.Adapter<SalesHistoryAdapter.ViewHolder>(),
    Filterable {

    var onHeaderClick: ((LogsUserHistoryRes.BordereauRecordList, Int) -> Unit)? = null
    var onRePrintInvoiceClick: ((LogsUserHistoryRes.BordereauRecordList, Int) -> Unit)? = null
    var onInspectionOrNoInspectionClick: ((LogsUserHistoryRes.BordereauRecordList, Int,Boolean) -> Unit)? = null
    var  mListFiltered = dataList
    var  mWagonValue:String=""
    var  mBordereuValue:String=""

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var index  = position + 1

        holder?.txtDate?.text = dataList[position]?.verifiedDateStr
        holder?.txtBordereuDate.text =  dataList[position]?.bordereauDateString
        holder?.txtWagonNo ?.text = dataList[position]?.wagonNo
        holder?.txtForest?.text = dataList[position]?.supplierShortName
        if(dataList[position]?.inspectionNumber.isNullOrEmpty()){
            holder?.txtInspectionNo?.visibility =  View.GONE
        }else{
            holder?.txtInspectionNo?.visibility =  View.VISIBLE
            holder?.txtInspectionNo?.text = dataList[position]?.inspectionNumber
        }


        if(dataList[position]?.bordereauNo.toString().isNullOrEmpty()){
            holder?.txtBordereuNo.text =  dataList[position]?.eBordereauNo
        }else{
            holder?.txtBordereuNo?.text = dataList[position]?.bordereauNo

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
                        holder.linInspection.visibility = View.VISIBLE
                        holder.linNoInspection.visibility = View.VISIBLE
                        holder.linStatus.visibility = View.GONE
                        holder.linReprint.visibility=View.GONE
                    }
                    else -> {
                        holder.linInspection.visibility = View.VISIBLE
                        holder.linNoInspection.visibility = View.GONE
                        holder.linStatus.visibility = View.GONE
                        holder.linReprint.visibility=View.GONE
                    }
                }
            }
            "SA" ->{
                holder.linStatus.visibility = View.VISIBLE
                holder.linReprint.visibility=View.GONE
                holder.linInspection.visibility = View.GONE
                holder.linNoInspection.visibility = View.GONE
                holder?.linStatus?.setBackgroundColor(ContextCompat.getColor(context,R.color.loading_active_color))
                holder?.txtStatus.text = "Pending"
            }
            "Y" ->{
                holder?.linStatus?.setBackgroundColor(ContextCompat.getColor(context,R.color.loading_active_color))
                holder?.txtStatus.text = "In Transit"
                holder.linReprint.visibility=View.VISIBLE
                holder.linStatus.visibility = View.VISIBLE
                holder.linInspection.visibility = View.GONE
                holder.linNoInspection.visibility = View.GONE
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
            holder.relvHeader.visibility = View.VISIBLE
            holder?.txttitle.visibility = View.VISIBLE
        }else{
            holder?.txttitle.visibility = View.INVISIBLE
            if(dataList.get(position)?.verifiedDateStr != dataList.get(position-1)?.verifiedDateStr ) {
                holder.relvHeader.visibility = View.VISIBLE
            }else{
                holder.relvHeader.visibility = View.GONE
            }
        }


        holder?.linInspection.setOnClickListener{
            dataList[position]?.let { it1 -> dataList[position]?.index?.let { it2 ->
                onInspectionOrNoInspectionClick?.invoke(it1,
                    it2,true)
            } }
        }

        holder?.linNoInspection.setOnClickListener{
            dataList[position]?.let { it1 -> dataList[position]?.index?.let { it2 ->
                onInspectionOrNoInspectionClick?.invoke(it1,
                    it2,false)
            } }
        }


        holder?.linReprint.setOnClickListener{
            dataList[position]?.let { it1 -> dataList[position]?.index?.let { it2 ->
                onRePrintInvoiceClick?.invoke(it1,
                    it2
                )
            } }
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup?.context).inflate(R.layout.row_item_sales_n_insspections_history, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtWagonNo =  itemView.findViewById<TextView>(R.id.txtWagonNo)
        val txtInspectionNo = itemView.findViewById<TextView>(R.id.txtInspectionNo)
        val txttitle =  itemView.findViewById<TextView>(R.id.txttitle)
        val txtStatus =  itemView.findViewById<TextView>(R.id.txtStatus)
        val txtReprint =  itemView.findViewById<TextView>(R.id.txtReprint)
        val txtDate =  itemView.findViewById<TextView>(R.id.txtDate)
        val txtBordereuDate = itemView.findViewById<TextView>(R.id.txtBordereuDate)
        val txtBordereuNo = itemView.findViewById<TextView>(R.id.txtBordereuNo)
        val txtForest =  itemView.findViewById<TextView>(R.id.txtForest)
        val linInspection = itemView.findViewById<LinearLayout>(R.id.linInspection)
        val linReprint  =  itemView.findViewById<LinearLayout>(R.id.linReprint)
        val linNoInspection = itemView.findViewById<LinearLayout>(R.id.linNoInspection)
        val linStatus = itemView.findViewById<LinearLayout>(R.id.linStatus)
        val linFooter = itemView.findViewById<LinearLayout>(R.id.linFooter)
        val relvHeader = itemView.findViewById<RelativeLayout>(R.id.relvHeader)

    }

    fun setValueToParentList(mDataList: ArrayList<LogsUserHistoryRes.BordereauRecordList?>){
          mListFiltered = mDataList
            dataList = mDataList
    }
    fun setLogAndForestValues(wagonValue:String,bordereuValue:String){
        this.mWagonValue  = wagonValue
        this.mBordereuValue  = bordereuValue
    }


    override fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (mWagonValue.isEmpty() && mBordereuValue.isEmpty()) {
                    dataList = mListFiltered
                } else {
                    val filteredData: ArrayList<LogsUserHistoryRes.BordereauRecordList?> =
                        java.util.ArrayList<LogsUserHistoryRes.BordereauRecordList?>()
                    for (row in mListFiltered)
                    {
                        //if only wagon filter applied
                         if(!mWagonValue.isEmpty() && mBordereuValue.isEmpty()){
                            // name match condition. this might differ depending on your requiremen
                            if (row?.wagonNo?.toLowerCase()?.contains(mWagonValue.toLowerCase())!!
                            ) {
                                filteredData.add(row)
                            }
                        }
                        //if only Bordereue no filter applied
                        else{
                             //check if electronic bordereu no present then show else manual bordereu no show
                             if(row?.bordereauNo.toString().isNullOrEmpty()){
                                 if (row?.eBordereauNo?.toLowerCase()?.contains(mBordereuValue.toLowerCase())!!) {
                                     filteredData.add(row)
                                 }
                             }else{
                                 if (row?.bordereauNo?.toLowerCase()?.contains(mBordereuValue.toLowerCase())!!) {
                                     filteredData.add(row)
                                 }

                             }


                        }
                    }
                    dataList = filteredData
                }
                val filterResults = FilterResults()
                filterResults.values = dataList
                return filterResults
            }

            override fun publishResults(
                constraint: CharSequence,
                results: FilterResults
            ) {
                dataList = results.values as ArrayList<LogsUserHistoryRes.BordereauRecordList?>
                /*isDataFound?.invoke(stringDeviceArrayList.size)*/
                notifyDataSetChanged()
            }
        }
    }

}


