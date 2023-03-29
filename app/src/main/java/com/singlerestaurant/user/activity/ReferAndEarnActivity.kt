package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.View
import com.singlerestaurant.user.R
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivityReferandearnBinding
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.SharePreference.Companion.getStringPref
import com.singlerestaurant.user.utils.SharePreference.Companion.userRefralAmount
import com.singlerestaurant.user.utils.SharePreference.Companion.userRefralCode



class ReferAndEarnActivity : BaseActivity() {
    var currency = ""
    var currencyPosition = ""
    private lateinit var binding:ActivityReferandearnBinding
    override fun setLayout(): View =binding.root

    @SuppressLint("SetTextI18n")
    override fun InitView() {
        binding=ActivityReferandearnBinding.inflate(layoutInflater)
        val price = getStringPref(this@ReferAndEarnActivity, userRefralAmount)


        currency = getStringPref(this@ReferAndEarnActivity, SharePreference.isCurrancy).toString()
        currencyPosition = getStringPref(this@ReferAndEarnActivity, SharePreference.CurrencyPosition).toString()

        val formattedPrice: String = Common.getPrices(currencyPosition,currency,price.toString())
        Common.getCurrentLanguage(this@ReferAndEarnActivity, false)
        binding.tvRefareAndEarn.text = "Share this code with a friend and you both \n could be eligible for $formattedPrice bonus amount \n under our Referral Program."
        binding.ivBack.setOnClickListener {
            finish()
        }
        if (getStringPref(this@ReferAndEarnActivity, SharePreference.SELECTED_LANGUAGE)
                        .equals(resources.getString(R.string.language_hindi))) {
            binding.ivBack.rotation = 180F
        } else {
            binding. ivBack.rotation = 0F
        }
        binding.  edPromocode.text = getStringPref(
                this@ReferAndEarnActivity,
                userRefralCode
        )
        val ios=SharePreference.getStringPref(this@ReferAndEarnActivity,SharePreference.ios)
        val android=SharePreference.getStringPref(this@ReferAndEarnActivity,SharePreference.android)
        Log.e("ios",ios.toString())
        Log.e("android",android.toString())
        binding.  tvBtnShare.setOnClickListener {
            val intent = Intent(Intent.ACTION_SEND)
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "Use this code ${getStringPref(this@ReferAndEarnActivity, userRefralCode)} to register with ${resources.getString(R.string.app_name)} & get bonus amount $formattedPrice  \n\n$android  \n\n"+ "$ios")
            startActivity(Intent.createChooser(intent, "choose one"))
        }
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@ReferAndEarnActivity, false)
    }
}