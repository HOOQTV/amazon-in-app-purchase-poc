package com.dimpossitorus.sample.amazoniap.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dimpossitorus.sample.amazoniap.R
import com.dimpossitorus.sample.amazoniap.plan.Sku
import kotlinx.android.synthetic.main.item_sku.view.*

class SkuListAdapter(val skus: List<Sku>, val onClickItemListener: OnItemClickListener) :
    RecyclerView.Adapter<SkuListAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sku, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return skus.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(skus.get(position), onClickItemListener)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(sku: Sku, itemClickListener: OnItemClickListener) {
            itemView.skuName.text = sku.name
            itemView.skuDescription.text = sku.description
            itemView.skuPrice.text = "%.0f".format(sku.price)
            itemView.setOnClickListener {
                itemClickListener.onClick(sku)
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(sku: Sku)
    }

}