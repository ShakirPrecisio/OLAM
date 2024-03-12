package com.kemar.olam.bordereau.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R
import com.kemar.olam.dashboard.models.MenusModel

class BcPrintRePrintAdapter (val context : Context, val dataList: ArrayList<MenusModel>) : RecyclerView.Adapter<BcPrintRePrintAdapter.ViewHolder>() {

    var onItemClick: ((MenusModel) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.txtLog?.text = "Log No"
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup?.context).inflate(R.layout.row_item_bordereau_listing, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtLog = itemView.findViewById<TextView>(R.id.txtLog)

    }
}


