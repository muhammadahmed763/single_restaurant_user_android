package com.singlerestaurant.user.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.exceptions.ExpiredAccessCodeException
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.singlerestaurant.user.R
import com.singlerestaurant.user.databinding.ActPayStackBinding
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.CreditCardTextFormatter
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.Utility

import org.json.JSONException
import java.util.*

class ActPayStack : AppCompatActivity() {

    private var transaction: Transaction? = null
    private var charge: Charge? = null
    private var amount = ""
    private var publicKey = ""
    private var email = ""
    private lateinit var binding: ActPayStackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActPayStackBinding.inflate(layoutInflater)
        val view = binding.root
        amount = intent.getStringExtra("amount") ?: ""
        publicKey = intent.getStringExtra("public_key") ?: ""
        email = intent.getStringExtra("email") ?: ""
        setContentView(view)
        initViews()

    }



    private fun initViews() {
        addTextWatcherToEditText()
        binding.btnPay.text = application.getString(R.string.pay_amount, amount)
        handleClicks()
    }

    private fun addTextWatcherToEditText() {
        //Make button UnClickable for the first time
        binding.btnPay.isEnabled = false
        binding.btnPay.background = ContextCompat.getDrawable(this, R.drawable.btn_round_opaque)
        //make the button clickable after detecting changes in input field
        val watcher: TextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val s1 = binding.etCardNumber.text.toString()
                val s2 = binding.etExpiry.text.toString()
                val s3 = binding.etCvv.text.toString()
                if (s1.isEmpty() || s2.isEmpty() || s3.isEmpty()) {
                    binding.btnPay.isEnabled = false
                    binding.btnPay.background = ContextCompat.getDrawable(
                        this@ActPayStack,
                        R.drawable.btn_round_opaque
                    )
                }
                if (s1.length >= 16 && s2.length == 5 && s3.length == 3) {
                    binding.btnPay.isEnabled = true
                    binding.btnPay.background = ContextCompat.getDrawable(
                        this@ActPayStack,
                        R.drawable.btn_border_blue_bg
                    )
                }
                if (s1.length < 16 || s2.length < 5 || s3.length < 3) {
                    binding.btnPay.isEnabled = false
                    binding.btnPay.background = ContextCompat.getDrawable(
                        this@ActPayStack,
                        R.drawable.btn_round_opaque
                    )
                }
                if (s2.length == 2) {
                    if (start == 2 && before == 1 && !s2.contains("/")) {
                        binding.etExpiry.setText(
                            application.getString(
                                R.string.expiry_space,
                                s2[0]
                            )
                        )
                        binding.etExpiry.setSelection(1)
                    } else {
                        binding.etExpiry.setText(application.getString(R.string.expiry_slash, s2))
                        binding.etExpiry.setSelection(3)
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
            }
        }
        binding.etCardNumber.addTextChangedListener(CreditCardTextFormatter())
        binding.etExpiry.addTextChangedListener(watcher)
        binding.etCvv.addTextChangedListener(watcher)
    }

    private fun handleClicks() {
        binding.btnPay.setOnClickListener {
            if (Utility.isNetworkAvailable(applicationContext)) {
                binding.loadingPayOrder.visibility = View.VISIBLE
                binding.btnPay.visibility = View.GONE
                    doPayment()
            } else {
                Toast.makeText(this, "Please check your internet", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun doPayment() {
        //set public key
        PaystackSdk.setPublicKey(publicKey)
        // initialize the charge
        charge = Charge()
        charge!!.card = loadCardFromForm()
        charge!!.amount = amount.toInt()
        charge!!.currency = "GHS"
        charge!!.email = email
        charge!!.reference = "payment" + Calendar.getInstance().timeInMillis
        try {
            charge!!.putCustomField("Charged From", "Android SDK")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        doChargeCard()
    }

    private fun loadCardFromForm(): Card {

        //validate fields
        val cardNumber = binding.etCardNumber.text.toString().trim()
        val expiryDate = binding.etExpiry.text.toString().trim()
        val cvc = binding.etCvv.text.toString().trim()

        //formatted values
        val cardNumberWithoutSpace = cardNumber.replace(" ", "")
        val monthValue = expiryDate.substring(0, expiryDate.length.coerceAtMost(2))
        val yearValue = expiryDate.takeLast(2)

        //build card object with ONLY the number, update the other fields later
        val card: Card = Card.Builder(cardNumberWithoutSpace, 0, 0, "").build()

        //update the cvc field of the card
        card.cvc = cvc

        //validate expiry month;
        val sMonth: String = monthValue
        var month = 0
        try {
            month = sMonth.toInt()
        } catch (ignored: Exception) {
        }

        card.expiryMonth = month

        //validate expiry year
        val sYear: String = yearValue
        var year = 0
        try {
            year = sYear.toInt()
        } catch (ignored: Exception) {
        }
        card.expiryYear = year
        return card
    }

    /**
     * DO charge and receive call backs - successful and failed payment
     */

    private fun doChargeCard() {
        transaction = null
        PaystackSdk.chargeCard(this, charge, object : Paystack.TransactionCallback {
            // This is called only after transaction is successful
            override fun onSuccess(transaction: Transaction) {
                //hide loading
                binding.loadingPayOrder.visibility = View.GONE
                binding.btnPay.visibility = View.VISIBLE

                Toast.makeText(this@ActPayStack, "Payment was successful", Toast.LENGTH_LONG).show()
                val intent = Intent()
                intent.putExtra("id", transaction.reference.toString())
                setResult(RESULT_OK, intent)
                finish()
                this@ActPayStack.transaction = transaction
            }

            override fun beforeValidate(transaction: Transaction) {
                this@ActPayStack.transaction = transaction
            }

            override fun onError(error: Throwable, transaction: Transaction) {
                //stop loading
                binding.loadingPayOrder.visibility = View.GONE
                binding.btnPay.visibility = View.VISIBLE

                // If an access code has expired, simply ask your server for a new one
                // and restart the charge instead of displaying error

                this@ActPayStack.transaction = transaction
                if (error is ExpiredAccessCodeException) {
                    this@ActPayStack.doChargeCard()
                    return
                }

                if (transaction.reference != null) {
                    Toast.makeText(this@ActPayStack, error.message ?: "", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@ActPayStack, error.message ?: "", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@ActPayStack, false)
    }

    override fun onPause() {
        super.onPause()
        binding.loadingPayOrder.visibility = View.GONE
    }
}