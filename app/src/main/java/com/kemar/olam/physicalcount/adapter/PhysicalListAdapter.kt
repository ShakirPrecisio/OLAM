package com.kemar.olam.physicalcount.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.physicalcount.response.StockListRequest
import kotlinx.android.synthetic.main.item_physical_code.view.*

class PhysicalListAdapter (val list: kotlin.collections.List<StockListRequest>, val context: Context?)  : RecyclerView.Adapter<PhysicalListAdapter.ViewHolder>(){

    var onRightsSupe: ((StockListRequest, Int, ViewHolder) -> Unit)? = null
    var onItemClick: ((StockListRequest, Int, ViewHolder) -> Unit)? = null



    lateinit var alertView: View
    var truckLoadedDialog: AlertDialog? = null


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val startDate = view.startDate
        val endDate = view.endDate
        var physicalid=view.physicalid
        var status=view.status
        var card=view.card
        var mode=view.mode
        var txtUserName=view.txtUserName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val  v= LayoutInflater.from(context).inflate(R.layout.item_physical_code,parent,false)
        return ViewHolder(
            v
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var data=list.get(position)


        holder.startDate.setText(data.startDate)
        holder.endDate.setText(data.endDate)

        holder.physicalid.setText(data.id?.toString())

        holder.txtUserName.setText(data.username)

        holder.status.setText(data.status.toString())

        if(data.isOffline){
            holder.mode.setText("Offline")
            holder.mode.setTextColor(Color.RED)
        }else{
            holder.mode.setText("Online")
            holder.mode.setTextColor(Color.GREEN)
        }


        holder.card.setOnClickListener {
            onItemClick!!.invoke(data,position,holder)
        }


    }

}