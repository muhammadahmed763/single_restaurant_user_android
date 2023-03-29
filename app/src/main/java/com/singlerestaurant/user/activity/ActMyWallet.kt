package com.singlerestaurant.user.activity

import android.content.Intent
import android.view.View
import com.singlerestaurant.user.R
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActMyWalletBinding
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference

class ActMyWallet : BaseActivity() {
    private lateinit var binding:ActMyWalletBinding
    override fun setLayout(): View = binding.root
    var currency: String = ""
    var currencyPosition: String = ""
    var walletAmount = ""
    var fromPaymentPay=false
    override fun InitView() {
        binding=ActMyWalletBinding.inflate(layoutInflater)
        currencyPosition = SharePreference.getStringPref(this@ActMyWallet, SharePreference.CurrencyPosition) ?: ""
         currency=   SharePreference.getStringPref(this@ActMyWallet, SharePreference.isCurrancy) ?: ""
         walletAmount=   SharePreference.getStringPref(this@ActMyWallet, SharePreference.wallet) ?: "00"
        fromPaymentPay= intent.getBooleanExtra("AddMoneyPay",false)
        initClickListeners()
        binding.tvtotalbalance.text=Common.getPrices(currencyPosition,currency,walletAmount)

        if (SharePreference.getStringPref(
                this@ActMyWallet,
                SharePreference.SELECTED_LANGUAGE
            ).equals(
                resources.getString(R.string.language_hindi)
            )
        ) {
            binding.ivBack.rotation = 180F
        } else {
            binding.ivBack.rotation = 0F
        }
    }

    private fun initClickListeners()
    {
        binding.tvAddMoney.setOnClickListener {
            startActivity(Intent(this@ActMyWallet,ActAddMoney::class.java))
        }

        binding.ivBack.setOnClickListener {
            if(fromPaymentPay)
            {
                startActivity(Intent(this@ActMyWallet,DashboardActivity::class.java).putExtra("pos","5"))
                finish()
            }else
            {
                finish()

            }
        }

        binding.ivwalletlist.setOnClickListener {
            startActivity(Intent(this@ActMyWallet,ActTransactionHistory::class.java))
        }
    }

}