package com.kemar.olam.dashboard.adapter

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

class AppMenuAdapter (val context : Context, val dataList: ArrayList<MenusModel>) : RecyclerView.Adapter<AppMenuAdapter.ViewHolder>() {

    var onItemClick: ((MenusModel) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder?.txtCategoryName?.text = dataList[position].menuName
        holder?.ivIcon?.setBackgroundResource(dataList[position].imgResId!!)

        holder?.relvParent.setOnClickListener({
            onItemClick?.invoke(dataList[position])
        })
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup?.context).inflate(R.layout.row_app_menu_item, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtCategoryName = itemView.findViewById<TextView>(R.id.txtCategoryName)
        val ivIcon = itemView.findViewById<ImageView>(R.id.ivIcon)
        val relvParent = itemView.findViewById<RelativeLayout>(R.id.relvParent)

    }
}


