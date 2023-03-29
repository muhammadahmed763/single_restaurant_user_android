package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.singlerestaurant.user.R
import com.singlerestaurant.user.databinding.RowTransactionhistoryBinding
import com.singlerestaurant.user.model.TransactionsItem
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import kotlin.collections.ArrayList

class TransactionHistoryAdapter(
    var context: Activity,
    private val itemList: ArrayList<TransactionsItem>,
    private val itemClick: (Int, String) -> Unit
) : RecyclerView.Adapter<TransactionHistoryAdapter.TransactionHistoryViewHolder>() {


    inner class TransactionHistoryViewHolder(var binding: RowTransactionhistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindItems(
            context: Activity,
            data: TransactionsItem,
            itemClick: (Int, String) -> Unit,
            position: Int
        ) = with(binding.root)
        {

            if (data.date == null) {
                binding.tvWalletDate.text = ""
            } else {
                binding.tvWalletDate.text = Common.getDate(data.date)
            }

            binding.tvOrderNumber.text = "Order Id : " + data.orderNumber.toString()
            val currencyPos = SharePreference.getStringPref(context, SharePreference.CurrencyPosition)
            val currency = SharePreference.getStringPref(context, SharePreference.isCurrancy)
            binding.tvWalletAmount.text = Common.getPrices(
                currencyPos.toString(),
                currency.toString(),
                data.amount.toString()
            )


            when (data.transactionType) {
                1 -> {
                    binding.tvTransactionName.text =
                        context.resources.getString(R.string.walletorder)
                    binding.tvOrderNumber.text =
                        resources.getString(R.string.orderid).plus(data.orderNumber)
                    binding.ivCheck.setImageResource(R.drawable.ic_up)
                    binding.tvWalletAmount.setTextColor(
                        ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.red,
                                null
                            )
                        )
                    )
                }
                2 -> {
                    binding.tvTransactionName.text =
                        context.resources.getString(R.string.ordercancelled)
                    binding.tvOrderNumber.text =
                        resources.getString(R.string.orderid).plus(data.orderNumber)
                    binding.ivCheck.setImageResource(R.drawable.ic_down)
                    binding.tvWalletAmount.setTextColor(
                        ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.light_green,
                                null
                            )
                        )
                    )

                }
                3 -> {
                    binding.tvTransactionName.text = resources.getString(R.string.walletrecharge)
                    binding.tvOrderNumber.text = resources.getString(R.string.ptrazorpay)
                    binding.ivCheck.setImageResource(R.drawable.ic_down)
                    binding.tvWalletAmount.setTextColor(
                        ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.light_green,
                                null
                            )
                        )
                    )
                }
                4 -> {
                    binding.tvTransactionName.text = resources.getString(R.string.walletrecharge)
                    binding.tvOrderNumber.text = resources.getString(R.string.ptstripe)
                    binding.ivCheck.setImageResource(R.drawable.ic_down)
                    binding.tvWalletAmount.setTextColor(
                        ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.light_green,
                                null
                            )
                        )
                    )
                }
                5 -> {
                    binding.tvTransactionName.text = resources.getString(R.string.walletrecharge)
                    binding.tvOrderNumber.text = resources.getString(R.string.ptflutterwave)
                    binding.ivCheck.setImageResource(R.drawable.ic_down)
                    binding.tvWalletAmount.setTextColor(
                        ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.light_green,
                                null
                            )
                        )
                    )
                }
                6 -> {
                    binding.tvTransactionName.text = resources.getString(R.string.walletrecharge)
                    binding.tvOrderNumber.text = resources.getString(R.string.ptstack)
                    binding.ivCheck.setImageResource(R.drawable.ic_down)
                    binding.tvWalletAmount.setTextColor(
                        ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.light_green,
                                null
                            )
                        )
                    )
                }
                7 -> {
                    binding.tvTransactionName.text = resources.getString(R.string.referral_amount)
                    binding.ivCheck.setImageResource(R.drawable.ic_down)
                    binding.tvOrderNumber.text = data.username.toString()
                    binding.tvWalletAmount.setTextColor(
                        ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.light_green,
                                null
                            )
                        )
                    )
                }
                8 -> {

                    binding.tvTransactionName.text = resources.getString(R.string.amount_added_by_admin)
                    binding.ivCheck.setImageResource(R.drawable.ic_down)
                    binding.tvOrderNumber.text = ""
                    binding.tvWalletAmount.setTextColor(
                        ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.light_green,
                                null
                            )
                        )
                    )

                }
                9 -> {
                    binding.tvTransactionName.text = resources.getString(R.string.amount_deduct_by_admin)
                    binding.ivCheck.setImageResource(R.drawable.ic_up)
                    binding.tvOrderNumber.text = ""
                    binding.tvWalletAmount.setTextColor(
                        ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.red,
                                null
                            )
                        )
                    )

                }
            }

        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TransactionHistoryViewHolder {
        val view = RowTransactionhistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionHistoryViewHolder(view)

    }

    override fun onBindViewHolder(holder: TransactionHistoryViewHolder, position: Int) {
        holder.bindItems(context, itemList[position], itemClick, position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}