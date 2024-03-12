package com.kemar.olam.sales_and_inspection.ground_sales.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.bordereau.model.request.BodereuLogListing
import com.kemar.olam.bordereau.model.responce.LogsUserHistoryRes

class GroundLogsDetailsAdapter (val context : Context, var dataList: ArrayList<BodereuLogListing>, val commingFrom:String) : RecyclerView.Adapter<GroundLogsDetailsAdapter.ViewHolder>(),Filterable {

    var mListFiltered = dataList
      var  mForestVal = ""
    var  mLogNo = ""
    var onMoreClick: ((BodereuLogListing, Int, Boolean) -> Unit)? = null
    var onAddClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onPrintClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onEditClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onDeleteClick: ((BodereuLogListing, Int) -> Unit)? = null
    var onCheckBoxClick: (() -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val index  = position + 1
        holder.txtSerial?.text =  "$index"
        //holder.txtLog?.text = "$index.Log No : "

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


        holder.chkBox?.isChecked = dataList[position].isSelected

        holder.chkBox?.setOnClickListener {
            dataList[position].isSelected = !dataList[position].isSelected
            notifyDataSetChanged()
        }
        holder.chkBox?.isChecked = dataList[position].isSelected

        holder.chkBox?.setOnClickListener {
            dataList[position].isSelected = !dataList[position].isSelected
            onCheckBoxClick?.invoke()
            /*notifyDataSetChanged()*/
        }


        holder.ivMore?.setOnClickListener{
            if(dataList[position].isExpanded){
                dataList[position].isExpanded = false
                onMoreClick?.invoke(dataList[position],position,false)
            }else{
                dataList[position].isExpanded = true
                onMoreClick?.invoke(dataList[position],position,true)
            }
        }
        holder.ivAdd?.setOnClickListener{
            dataList[position].getIndex()?.let { it1 ->
                onAddClick?.invoke(dataList[position],
                    it1
                )
            }
        }
        holder.ivPrint?.setOnClickListener{
            dataList[position].getIndex()?.let { it1 ->
                onPrintClick?.invoke(dataList[position],
                    it1
                )
            }
        }
        holder.ivEdit?.setOnClickListener{
            dataList[position].getIndex()?.let { it1 ->
                onEditClick?.invoke(dataList[position],
                    it1
                )
            }
        }
        holder.ivDelete?.setOnClickListener{
            dataList[position].getIndex()?.let { it1 ->
                onDeleteClick?.invoke(dataList[position],
                    it1
                )
            }
        }

    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_view_ground_logs_liisting, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtSerial : TextView? =  itemView.findViewById(R.id.txtSerial)
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

        val chkBox : CheckBox? =  itemView.findViewById(R.id.chkBox)



        val linFooter : LinearLayout? = itemView.findViewById(R.id.linFooter)
        val ivMore   : ImageView? = itemView.findViewById(R.id.ivMore)
        val  ivAdd  : ImageView? =  itemView.findViewById(R.id.ivAdd)
        val  ivPrint  : ImageView?  =  itemView.findViewById(R.id.ivPrint)
        val  ivEdit  : ImageView? =  itemView.findViewById(R.id.ivEdit)
        val  ivDelete : ImageView? =  itemView.findViewById(R.id.ivDelete)
    }


    fun setValueToParentList(mDataList: ArrayList<BodereuLogListing>){
        mListFiltered = mDataList
        dataList = mDataList
    }

    fun setLogAndForestValues(forestValue:String,logNoValue:String){
        this.mForestVal  = forestValue
        this.mLogNo  = logNoValue
    }
    override fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (mForestVal.isEmpty() && mLogNo.isEmpty()) {
                    dataList = mListFiltered
                } else {
                    val filteredData: ArrayList<BodereuLogListing> =
                        java.util.ArrayList<BodereuLogListing>()

                    for (row in mListFiltered)
                    {
                       //if both filter applied
                        if(!mForestVal.isEmpty() && !mLogNo.isEmpty()) {
                            // name match condition. this might differ depending on your requirement
                            if (row.getLogNo()?.toLowerCase()?.contains(mLogNo.toLowerCase())!! && row.getSupplierName()?.toLowerCase()?.contains(
                                    mForestVal.toLowerCase()
                                )!!
                            ) {
                                filteredData.add(row)
                            }
                        }
                        //if only forest filter applied
                        else if(!mForestVal.isEmpty() && mLogNo.isEmpty()){
                            // name match condition. this might differ depending on your requirement
                            if (row.getSupplierName()?.toLowerCase()?.contains(mForestVal.toLowerCase())!!
                            ) {
                                filteredData.add(row)
                            }
                        }
                        //if only log no filter applied
                        else{
                            if (row.getLogNo()?.toLowerCase()?.contains(mLogNo.toLowerCase())!!
                            ) {
                                filteredData.add(row)
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
                dataList = results.values as ArrayList<BodereuLogListing>
                /*isDataFound?.invoke(stringDeviceArrayList.size)*/
                notifyDataSetChanged()
            }
        }
    }

}


