package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.singlerestaurant.user.R
import com.singlerestaurant.user.databinding.CellAddonsListBinding
import com.singlerestaurant.user.model.AddOnsDataModel
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference

class AddOnsViewAdapter(
    var context: Activity,
    private val mList: ArrayList<AddOnsDataModel>
) : RecyclerView.Adapter<AddOnsViewAdapter.AddOnsViewViewHolder>() {


    inner class AddOnsViewViewHolder(private val itemBinding: CellAddonsListBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        val tvaddons: TextView = itemView.findViewById(R.id.tvaddons)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddOnsViewViewHolder {
        val view = CellAddonsListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddOnsViewViewHolder(view)
    }

    override fun onBindViewHolder(holder: AddOnsViewViewHolder, position: Int) {

        val cuisinesItem = mList[position]
        val currency = SharePreference.getStringPref(context, SharePreference.isCurrancy) ?: ""
        val currencyPos =
            SharePreference.getStringPref(context, SharePreference.CurrencyPosition) ?: ""
        holder.tvPrice.text =
            Common.getPrices(currencyPos, currency, mList[position].price.toString())
        holder.tvaddons.text = cuisinesItem.name

    }

    override fun getItemCount(): Int {

        return mList.size
    }
}