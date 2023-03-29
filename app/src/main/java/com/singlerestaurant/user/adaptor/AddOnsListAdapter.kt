package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.singlerestaurant.user.R
import com.singlerestaurant.user.model.AddOnsDataModel
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference

class AddOnsListAdapter(
    var context: Activity,
    private val mList: ArrayList<AddOnsDataModel>,
) : RecyclerView.Adapter<AddOnsListAdapter.AddOnsListViewHolder>() {

    inner class AddOnsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvaddons: TextView = itemView.findViewById(R.id.tvaddons)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val iv_check: ImageView = itemView.findViewById(R.id.ivCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddOnsListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_itemaddons, parent, false)
        return AddOnsListViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddOnsListViewHolder, position: Int) {


        val cuisinesItem = mList[position]
        val currency = SharePreference.getStringPref(context, SharePreference.isCurrancy) ?: ""
        val currencyPos =
            SharePreference.getStringPref(context, SharePreference.CurrencyPosition) ?: ""
        holder.tvPrice.text =
            Common.getPrices(currencyPos, currency, mList[position].price.toString())
        holder.tvaddons.text = cuisinesItem.name


        if (!cuisinesItem.isAddOnsSelect) {
            holder.iv_check.setImageResource(R.drawable.ic_addonsuncheck)
        } else {
            holder.iv_check.setImageResource(R.drawable.ic_addonscheck)
        }

        holder.itemView.setOnClickListener {
            if (!cuisinesItem.isAddOnsSelect) {

                holder.iv_check.setImageResource(R.drawable.ic_addonscheck)
                mList[position].isAddOnsSelect = true
            } else {
                holder.iv_check.setImageResource(R.drawable.ic_addonsuncheck)
                mList[position].isAddOnsSelect = false
            }
        }


    }

    override fun getItemCount(): Int {
        return mList.size
    }
}