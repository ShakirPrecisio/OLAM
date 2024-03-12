package com.kemar.olam.loading_wagons.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.loading_wagons.model.responce.DeclrationBordereuListRes

class Loading_Declration_ListingAdapter(val context : Context) : RecyclerView.Adapter<Loading_Declration_ListingAdapter.ViewHolder>() {

    var onMoreClick: ((DeclrationBordereuListRes.BordereauRecordList, Int, Boolean) -> Unit)? = null
    var onHeaderClick: ((DeclrationBordereuListRes.BordereauRecordList, Int) -> Unit)? = null
    var onRemoveClick: ((DeclrationBordereuListRes.BordereauRecordList, Int) -> Unit)? = null
    var onTonnageUpdate: ((DeclrationBordereuListRes.BordereauRecordList, Int,Boolean) -> Unit)? = null
    val dataList: ArrayList<DeclrationBordereuListRes.BordereauRecordList?> = arrayListOf()

    var selectedPosition: Int? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var index  = position + 1
        holder?.txtSerial?.text =  index.toString().trim()

        holder?.txtSupplier?.text = dataList[position]?.supplierShortName?.toString()

        Log.d("declaration", "layout changed" + position)

        if(dataList[position]?.bordereauNo.toString().isNullOrEmpty()){
            holder?.txtElecTronicBO_NO?.text = dataList[position]?.eBordereauNo.toString()
            holder?.linManualBo.visibility =  View.GONE
            holder?.txtManualBO_NO?.text = ""
        }else{
            holder?.txtManualBO_NO?.text = dataList[position]?.bordereauNo.toString()
            holder?.txtElecTronicBO_NO?.text = dataList[position]?.eBordereauNo.toString()
            holder?.linManualBo.visibility =  View.VISIBLE
        }

        if (dataList[position]?.isExpanded!!) {
            holder.linFooter.visibility = View.VISIBLE
        } else {
            holder?.linFooter.visibility = View.GONE

        }

        if (dataList[position]?.isSelected!!) {
            holder.chkBox.isChecked = true
        } else {
            holder?.chkBox.isChecked = false

        }

        holder.chkBox.setOnClickListener {

            var isSelected : Boolean    = false
            if(dataList[position]?.isSelected!!){
                selectedPosition = null
                dataList[position]?.isSelected = false
                isSelected = false
            }else{
                resetAllCheckBox()
                selectedPosition = position
                dataList[position]?.isSelected = true
                isSelected = true
            }
            dataList[position]?.let { it1 -> onTonnageUpdate?.invoke(it1,position,isSelected) }

        }

        if(dataList[position]?.totaltonnage!=null){
            holder?.txtTonnage?.text = dataList[position]?.totaltonnage?.toString()
        }else{
            holder?.txtTonnage?.text = "0.0"
        }
        holder?.txtCBM?.text = dataList[position]?.gsezCBM.toString()
        holder?.txtVehicleNumber.text = dataList[position]?.wagonNo?.toString()
        dataList[position]?.transportMode?.let { setupTransportMode(it,holder,context) }


        holder?.ivMore.setOnClickListener {
            if(dataList[position]!!.isExpanded){
                dataList[position]?.let { it1 -> onMoreClick?.invoke(it1,position,false) }
            }else{
                dataList[position]?.let { it1 -> onMoreClick?.invoke(it1,position,true) }
            }
        }

        holder?.linParent.setOnClickListener {
            dataList[position]?.let { it1 -> onHeaderClick?.invoke(it1,position) }
        }

      /*  holder?.ivRemove.setOnClickListener {
            dataList[position]?.let { it1 -> onRemoveClick?.invoke(it1,position) }
        }*/


    }


    private fun resetAllCheckBox(){

        if(selectedPosition != null) {
            dataList[selectedPosition!!]?.isSelected = false
            notifyItemChanged(selectedPosition!!)
        }

    }

    fun addAllItems(dataList: ArrayList<DeclrationBordereuListRes.BordereauRecordList?>){
        for (data in dataList){
            addItem(data)
        }
    }

    fun addItem(data: DeclrationBordereuListRes.BordereauRecordList?) {
        dataList.add(data)
        notifyItemInserted(dataList.size - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeAllItem() {
        dataList.clear()
        notifyDataSetChanged()
    }

    fun removeItem(data: DeclrationBordereuListRes.BordereauRecordList?) {
        val position = dataList.indexOf(data)
        dataList.remove(data)
        notifyItemRemoved(position)
    }

    fun setupTransportMode(transportMode:Int, holder: ViewHolder, context: Context){
        when(transportMode){
            1 -> {
                holder?.txtVehicleHeader?.text = context.resources.getString(R.string._wagon_no)
            }
            17 -> {
                holder?.txtVehicleHeader?.text = context.resources.getString(R.string._barge_no)
            }
            3 -> {
                holder?.txtVehicleHeader?.text = context.resources.getString(R.string._truck_no)
            }else-> {

        }
        }
    }



    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup?.context).inflate(R.layout.row_item_lading_wagon_declare, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtTonnage =  itemView.findViewById<TextView>(R.id.txtTonnage)
        val txtCBM = itemView.findViewById<TextView>(R.id.txtCBM)
        val txtSupplier =  itemView.findViewById<TextView>(R.id.txtSupplier)
        val txtVehicleHeader =  itemView.findViewById<TextView>(R.id.txtVehicleHeader)
        val txtVehicleNumber = itemView.findViewById<TextView>(R.id.txtVehicleNumber)
        val txtManualBO_NO = itemView.findViewById<TextView>(R.id.txtManualBO_NO)
        val txtElecTronicBO_NO =  itemView.findViewById<TextView>(R.id.txtElecTronicBO_NO)
        val txtSerial =  itemView.findViewById<TextView>(R.id.txtSerial)
        val ivMore =  itemView.findViewById<ImageView>(R.id.ivMore)
      //  val ivRemove =  itemView.findViewById<ImageView>(R.id.ivRemove)
        val linFooter = itemView.findViewById<LinearLayout>(R.id.linFooter)
        val linManualBo = itemView.findViewById<LinearLayout>(R.id.linManualBo)
        val chkBox = itemView.findViewById<CheckBox>(R.id.chkBox)
        val linParent   = itemView.findViewById<LinearLayout>(R.id.linParent)


    }
}


