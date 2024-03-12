package com.kemar.olam.loading_wagons.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kemar.olam.R


class MonthAdapter(val context : Context, val dataList: ArrayList<String>) : RecyclerView.Adapter<MonthAdapter.ViewHolder>() {

    var onMonthClick: ((String, Int) -> Unit)? = null

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder?.spinner_textView ?.text = dataList[position]

        holder?.spinner_textView.setOnClickListener({
            onMonthClick?.invoke(dataList[position],position)
        })
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup?.context).inflate(R.layout.item_country_dialog_adapter, viewGroup, false)
        return ViewHolder(v);
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val spinner_textView =  itemView.findViewById<TextView>(R.id.spinner_textView)

    }
}


