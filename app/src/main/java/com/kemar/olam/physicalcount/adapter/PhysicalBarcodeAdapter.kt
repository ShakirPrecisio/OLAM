package com.kemar.olam.physicalcount.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.physicalcount.requestbody.Detail
import kotlinx.android.synthetic.main.item_barcode_physical.view.*

class PhysicalBarcodeAdapter (val list: kotlin.collections.List<Detail>, val context: Context?,var isSubmit:Boolean)  : RecyclerView.Adapter<PhysicalBarcodeAdapter.ViewHolder>(){

    var onRightsSupe: ((Detail, Int, ViewHolder) -> Unit)? = null
    var onItemClick: ((Detail, Int, ViewHolder) -> Unit)? = null



    lateinit var alertView: View
    var truckLoadedDialog: AlertDialog? = null


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val barcodeName = view.barcodeName
        val ivcross = view.ivcross
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val  v= LayoutInflater.from(context).inflate(R.layout.item_barcode_physical,parent,false)
        return ViewHolder(
            v
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

         var data=list.get(position)


        holder.barcodeName.setText(data.barcodeNumber)

        if(isSubmit){
            holder.ivcross.visibility=View.GONE
        }else{
            holder.ivcross.visibility=View.VISIBLE
        }

        holder.ivcross.setOnClickListener {
            onItemClick?.invoke(data,position,holder)
        }


    }

}