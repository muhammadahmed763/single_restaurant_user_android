package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.singlerestaurant.user.R
import com.singlerestaurant.user.model.VariationItem
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.FilterClick
import com.singlerestaurant.user.utils.SharePreference

class VariationListAdapter (var context: Activity,
                            private val mList: ArrayList<VariationItem>,
                            private val onFilterClick: (Int,String) -> Unit) :RecyclerView.Adapter<VariationListAdapter.VariationListViewHolder>(),
    FilterClick {
    inner class VariationListViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
    {
        val tvVariation: TextView = itemView.findViewById(R.id.tvVariation)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val iv_check: ImageView = itemView.findViewById(R.id.ivCheck)
    }



    override fun onFilterClick(pos: Int, type: String) {
        onFilterClick.invoke(pos,type)
    }


    override fun getItemCount(): Int {
        return mList.size
    }

    override fun onBindViewHolder(holder: VariationListViewHolder, position: Int) {
        val cuisinesItem = mList[position]
        val currency = SharePreference.getStringPref(context, SharePreference.isCurrancy) ?: ""
        val currencyPos = SharePreference.getStringPref(context, SharePreference.CurrencyPosition) ?: ""

        holder.tvPrice.text = Common.getPrices(currencyPos,currency,mList[position].productPrice.toString())

        holder.tvVariation.text = cuisinesItem.variation
        if (cuisinesItem.isSelect==false) {
            holder.iv_check.setImageResource(R.drawable.ic_variationuncheck)
        } else {
            holder.iv_check.setImageResource(R.drawable.ic_variationcheck)
        }

        holder.itemView.setOnClickListener {
            onFilterClick(position,Constants.ItemClick)

                for(i in 0 until mList.size)
                {
                    mList[i].isSelect=false
                }
            mList[position].isSelect=true
            notifyDataSetChanged()


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariationListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_variation, parent, false)

        return VariationListViewHolder(view)
    }
}