package com.singlerestaurant.user.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.singlerestaurant.user.R
import com.singlerestaurant.user.databinding.CustomStripeLayoutBinding
import com.singlerestaurant.user.utils.Common


class ActStripePayment : AppCompatActivity() {

    private lateinit var _binding:CustomStripeLayoutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = CustomStripeLayoutBinding.inflate(layoutInflater)
        _binding.ivClose.setOnClickListener {
            finish()
        }
        startCheckout()
        setContentView(_binding.root )
    }

    private fun startCheckout() {

        _binding.btnSubmit.setOnClickListener {
            if (_binding.edHolderName.text.isNullOrEmpty()) {
                Common.showErrorFullMsg(
                    this@ActStripePayment,
                    resources.getString(R.string.validation_all)
                )
            } else if (_binding.edCardNumber.text.isNullOrEmpty()) {
                Common.showErrorFullMsg(
                    this@ActStripePayment,
                    resources.getString(R.string.validation_all)
                )
            } else if ((_binding.edCardNumber.text?.length ?: 0) < 16) {
                Common.showErrorFullMsg(this@ActStripePayment, resources.getString(R.string.card_detail_error))
            } else if (_binding.etMonth.text.isNullOrEmpty()) {
                Common.showErrorFullMsg(
                    this@ActStripePayment,
                    resources.getString(R.string.validation_all)
                )
            } else if (_binding.etYear.text.isNullOrEmpty()) {
                Common.showErrorFullMsg(
                    this@ActStripePayment,
                    resources.getString(R.string.validation_all)
                )
            } else if (_binding.etCvv.text.isNullOrEmpty()) {
                Common.showErrorFullMsg(
                    this@ActStripePayment,
                    resources.getString(R.string.validation_all)
                )
            } else if ((_binding.etCvv.text?.length ?: 0) < 3) {
                Common.showErrorFullMsg(this@ActStripePayment, resources.getString(R.string.cvv_error))
            } else {
                val intent = Intent()
                intent.putExtra("card_number", _binding.edCardNumber.text.toString())
                intent.putExtra("exp_month", _binding.etMonth.text.toString())
                intent.putExtra("exp_year", _binding.etYear.text.toString())
                intent.putExtra("cvv", _binding.etCvv.text.toString())
                setResult(401, intent)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@ActStripePayment, false)
    }
}