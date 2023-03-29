package com.singlerestaurant.user.activity

import android.content.Intent
import android.view.View
import com.singlerestaurant.user.R
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivitySuccesspaymentBinding

class PaymentSuccessfulActivity : BaseActivity() {
    private lateinit var binding:ActivitySuccesspaymentBinding
    override fun setLayout(): View = binding.root

    override fun InitView() {
        binding=ActivitySuccesspaymentBinding.inflate(layoutInflater)
        binding.tvProceed.setOnClickListener {
            startActivity(Intent(this@PaymentSuccessfulActivity,OrderDetailActivity::class.java).putExtra("fromOrderSuccess",true).putExtra("order_id",intent.getStringExtra("order_id")))
            finish()
            finishAffinity()
        }
    }

    override fun onBackPressed() {
        return
    }
}