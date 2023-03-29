package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.singlerestaurant.user.R
import com.singlerestaurant.user.databinding.RowPaymentListBinding
import com.singlerestaurant.user.model.PaymentmethodsItem
import com.singlerestaurant.user.utils.Constants


class PaymentListAdapter(
    private val context: Activity,
    private val paymentOptionList: ArrayList<PaymentmethodsItem>,
    private val itemClick: (Int, String) -> Unit,
) : RecyclerView.Adapter<PaymentListAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(private val itemBinding: RowPaymentListBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(
            data: PaymentmethodsItem,
            context: Activity,
            position: Int,
            itemClick: (Int, String) -> Unit
        ) = with(itemBinding)
        {
            if (data.isSelect) {
                itemBinding.ivCheck.visibility = View.VISIBLE
            } else {
                itemBinding.ivCheck.visibility = View.GONE
            }


            Glide.with(itemView.context).load(data.image).into(itemBinding.ivPaymentProfile)
            itemBinding.tvPaymentName.text = data.paymentName
            itemView.setOnClickListener {
                itemClick(position, Constants.ItemClick)
                for (i in 0 until paymentOptionList.size) {
                    paymentOptionList[i].isSelect = false
                }
                data.isSelect = true
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = RowPaymentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(paymentOptionList[position], context, position, itemClick)
    }

    override fun getItemCount(): Int {
        return paymentOptionList.size
    }
}