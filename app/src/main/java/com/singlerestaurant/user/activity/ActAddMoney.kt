package com.singlerestaurant.user.activity

import android.content.Intent
import android.view.View
import com.singlerestaurant.user.R
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActAddMoneyBinding
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference



class ActAddMoney : BaseActivity() {
    var currency: String = ""
    var currencyPosition: String = ""
    var walletAmount = ""
    private lateinit var binding:ActAddMoneyBinding
    override fun setLayout(): View = binding.root

    override fun InitView() {
        binding= ActAddMoneyBinding.inflate(layoutInflater)
        currencyPosition = SharePreference.getStringPref(this@ActAddMoney, SharePreference.CurrencyPosition) ?: ""
        currency=   SharePreference.getStringPref(this@ActAddMoney, SharePreference.isCurrancy) ?: ""
        walletAmount=   SharePreference.getStringPref(this@ActAddMoney, SharePreference.wallet) ?: "00"

        binding. tvTotalBalance.text=resources.getString(R.string.total_balance).plus(": ${Common.getPrices(currencyPosition,currency,walletAmount)}")

        if (SharePreference.getStringPref(
                this@ActAddMoney,
                SharePreference.SELECTED_LANGUAGE
            ).equals(
                resources.getString(R.string.language_hindi)
            )
        ) {
            binding. ivBack?.rotation = 180F
        } else {
            binding. ivBack?.rotation = 0F
        }
        binding.  ivBack?.setOnClickListener {
            finish()
        }



        binding. btnAddMoney?.setOnClickListener {
            if (binding. edAmount.text!!.isEmpty() || binding. edAmount.text.toString() == ".") {
                Common.showErrorFullMsg(this@ActAddMoney, resources.getString(R.string.enter_amount))
            } else if (!Common.isValidAmount(binding. edAmount.text.toString())) {
                Common.showErrorFullMsg(this@ActAddMoney, resources.getString(R.string.valid_amount))
            } else if (binding. edAmount.text.toString().toDouble().toInt() < 1) {
                Common.showErrorFullMsg(this@ActAddMoney, resources.getString(R.string.one_amount))
            }else
            {
                val intent=Intent(this@ActAddMoney,ActAddMoneyPaymentPay::class.java)
                intent.putExtra("amount",binding. edAmount.text.toString())
                intent.putExtra("paymentListType","wallet")
                startActivity(intent)
            }
        }


    }

}