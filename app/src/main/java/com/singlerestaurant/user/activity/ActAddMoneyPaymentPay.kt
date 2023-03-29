package com.singlerestaurant.user.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.flutterwave.raveandroid.RavePayActivity
import com.flutterwave.raveandroid.RaveUiManager
import com.flutterwave.raveandroid.rave_java_commons.RaveConstants
import com.singlerestaurant.user.R
import com.singlerestaurant.user.adaptor.PaymentListAdapter
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.ActAddMoneyPaymentPayBinding
import com.singlerestaurant.user.model.FlutterWaveResponse
import com.singlerestaurant.user.model.PaymentmethodsItem
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import com.google.gson.Gson
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import kotlin.math.round
import kotlin.math.roundToInt

class ActAddMoneyPaymentPay : AppCompatActivity(), PaymentResultListener {
    private var strGetData = ""
    private var logoimg = ""
    private var strRazorPayKey = ""
    private var payStackKey = ""
    private var flutterWaveKey = ""
    private var encryptionKey = ""
    private var stripekey = ""
    private var paymentAdapter: PaymentListAdapter? = null
    private var walletAmount = 0.00
    private var paymentList = ArrayList<PaymentmethodsItem>()
    var addAmount = "0.00"


    private lateinit var binding: ActAddMoneyPaymentPayBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActAddMoneyPaymentPayBinding.inflate(layoutInflater)
        setContentView(binding.root)


        addAmount = intent.getStringExtra("amount").toString()

        Common.getCurrentLanguage(this@ActAddMoneyPaymentPay, false)
        Checkout.preload(this@ActAddMoneyPaymentPay)

        binding.ivBack.setOnClickListener {
            finish()
        }
        if (SharePreference.getStringPref(
                this@ActAddMoneyPaymentPay,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))
        ) {
            binding.ivBack.rotation = 180F
        } else {
            binding.ivBack.rotation = 0F
        }
        setupAdapter()

        if (SharePreference.getBooleanPref(this@ActAddMoneyPaymentPay,SharePreference.isLogin)) {
            if (Common.isCheckNetwork(this@ActAddMoneyPaymentPay)) {
                callApiPayment()
            } else {
                Common.alertErrorOrValidationDialog(
                    this@ActAddMoneyPaymentPay,
                    resources.getString(R.string.no_internet)
                )
            }
        }else
        {
            startActivity(Intent(this@ActAddMoneyPaymentPay,LoginActivity::class.java))
            finish()
            finishAffinity()
        }
        binding.tvPaynow.setOnClickListener {
            when (strGetData) {
                "RazorPay" -> {
                    Common.showLoadingProgress(this@ActAddMoneyPaymentPay)
                    razorPayPayment()
                }
                "Stripe" -> {
                    val intent = Intent(this@ActAddMoneyPaymentPay, ActStripePayment::class.java)
                    intent.putExtra("stripeKey", stripekey)
                    activityResult.launch(intent)
                }
                "Flutterwave" -> {
                    flutterWavePayment()
                }
                "Paystack" -> {
                    val amount = intent.getStringExtra("amount") ?: "0.00"
                    val totalAmount = round(amount.toDouble()).roundToInt()
                    val i = Intent(this@ActAddMoneyPaymentPay, ActPayStack::class.java)
                    i.putExtra(
                        "email",
                        SharePreference.getStringPref(
                            this@ActAddMoneyPaymentPay,
                            SharePreference.userEmail
                        )
                    )
                    i.putExtra("public_key", payStackKey)
                    i.putExtra("amount", totalAmount.toString())
                    activityResult.launch(i)
                }
                "" -> {
                    Common.showErrorFullMsg(
                        this@ActAddMoneyPaymentPay,
                        resources.getString(R.string.payment_type_selection_error)
                    )
                }
            }
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            when (resultCode) {
                RavePayActivity.RESULT_SUCCESS -> {
                    Toast.makeText(this, "Transaction Successful", Toast.LENGTH_SHORT).show()
                    val message: String? = data.getStringExtra("response")
                    Log.e("message", message.toString())
                    val json = Gson().fromJson(message, FlutterWaveResponse::class.java)
                    val dataValue = json.data
                    val id = dataValue?.id.toString()
                    callAddMoney("5", id, "", "", "", "")
                }
                RavePayActivity.RESULT_ERROR -> {
                    Toast.makeText(this, "An Error Occur", Toast.LENGTH_SHORT).show()
                }
                RavePayActivity.RESULT_CANCELLED -> {
                    Toast.makeText(this, "Transaction Canceled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun razorPayPayment() {

        val activity: Activity = this
        val co = Checkout()
        try {
            co.setKeyID(strRazorPayKey)
            val amount = addAmount.toDouble().times(100)
            Common.getLog("test", amount.toString())
            val options = JSONObject()
            options.put("name", resources.getString(R.string.app_name))
            options.put("description", resources.getString(R.string.order_payment))
            options.put("image", logoimg)
            options.put("currency", "INR")
            options.put("amount", String.format(Locale.US, "%d", amount.toLong()))
            val prefill = JSONObject()
            prefill.put(
                "email",
                SharePreference.getStringPref(this@ActAddMoneyPaymentPay, SharePreference.userEmail)
            )
            prefill.put(
                "contact",
                SharePreference.getStringPref(
                    this@ActAddMoneyPaymentPay,
                    SharePreference.userMobile
                )
            )
            options.put("prefill", prefill)
            val theme = JSONObject()
            theme.put("color", "#366ed4")
            options.put("theme", theme)
            co.open(activity, options)

        } catch (e: Exception) {

            Common.dismissLoadingProgress()
            Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()

        }
    }


    private fun flutterWavePayment() {

        RaveUiManager(this).setAmount(addAmount.toDouble())
            .setEmail(
                SharePreference.getStringPref(
                    this@ActAddMoneyPaymentPay,
                    SharePreference.userEmail
                )
            )
            .setfName(
                SharePreference.getStringPref(
                    this@ActAddMoneyPaymentPay,
                    SharePreference.userName
                )
            )
            .setlName(
                SharePreference.getStringPref(
                    this@ActAddMoneyPaymentPay,
                    SharePreference.userName
                )
            )
            .setPublicKey(flutterWaveKey)
            .setEncryptionKey(encryptionKey)
            .setCountry("US")
            .setCurrency("NGN")
            .setTxRef(System.currentTimeMillis().toString() + "Ref")
            .setPhoneNumber(
                SharePreference.getStringPref(
                    this@ActAddMoneyPaymentPay,
                    SharePreference.userMobile
                ), false
            )
            .acceptMpesaPayments(true)
            .acceptBankTransferPayments(true, true)
            .acceptAccountPayments(true)
            .acceptSaBankPayments(true)
            .acceptBankTransferPayments(true)
            .acceptCardPayments(true)
            .onStagingEnv(false)
            .withTheme(R.style.DefaultPayTheme)
            .allowSaveCardFeature(true, false)
            .initialize()

    }


    private var activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == 401) {
                val cardNumber = result.data?.getStringExtra("card_number").toString()
                val expMonth = result.data?.getStringExtra("exp_month").toString()
                val expYear = result.data?.getStringExtra("exp_year").toString()
                val cvv = result.data?.getStringExtra("cvv").toString()
                callAddMoney("4", "", cardNumber, expMonth, expYear, cvv)
            } else if (result.resultCode == RESULT_OK) {
                val id = result.data?.getStringExtra("id") ?: ""
                callAddMoney("6", id, "", "", "", "")
                setResult(RESULT_OK)
            } else if (result.resultCode == RaveConstants.RESULT_SUCCESS && result.data != null) {
                when (result.resultCode) {
                    RavePayActivity.RESULT_SUCCESS -> {
                        Toast.makeText(this, "Transaction Successful", Toast.LENGTH_SHORT).show()
                        val message: String? = result.data?.getStringExtra("response")
                        Log.e("message", message.toString())
                        val json = Gson().fromJson(message, JSONObject::class.java)
                        val dataValue = json.getJSONObject("data")
                        Log.e("dataValue", dataValue.toString())
                        val id = dataValue.getString("id")
                        callAddMoney("5", id, "", "", "", "")
                    }
                    RavePayActivity.RESULT_ERROR -> {
                        Toast.makeText(this, "An Error Occur", Toast.LENGTH_SHORT).show()
                    }
                    RavePayActivity.RESULT_CANCELLED -> {
                        Toast.makeText(this, "Transaction Canceled", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (result.resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, "An Error Occur", Toast.LENGTH_SHORT).show()
            }
        }


    private fun callApiPayment() {
        Common.showLoadingProgress(this@ActAddMoneyPaymentPay)
        val map = HashMap<String, String>()
        map["user_id"] = SharePreference.getStringPref(
            this@ActAddMoneyPaymentPay,
            SharePreference.userId
        )!!
        map["type"] = intent.getStringExtra("paymentListType") ?: "order"
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().getPaymentType(map)) {
                is NetworkResponse.Success -> {
                    val restResponse = response.body

                    if (restResponse.status == 1) {
                        Common.dismissLoadingProgress()
                        restResponse.paymentmethods?.let { paymentList.addAll(it) }
                        paymentAdapter?.notifyDataSetChanged()
                        if (restResponse.totalWallet != "null") {
                            walletAmount = restResponse.totalWallet?.toDouble() ?: 0.00
                        }
                        setupKeyList()

                    } else {
                        Common.showErrorFullMsg(
                            this@ActAddMoneyPaymentPay,
                            restResponse.message.toString()
                        )

                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActAddMoneyPaymentPay,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActAddMoneyPaymentPay,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActAddMoneyPaymentPay,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }

    private fun setupAdapter() {
        paymentAdapter =
            PaymentListAdapter(this@ActAddMoneyPaymentPay, paymentList) { i: Int, s: String ->
                if (s == Constants.ItemClick) {
                    strGetData = paymentList[i].paymentName.toString()
                }
            }
        binding.rvPayment.apply {
            layoutManager = LinearLayoutManager(this@ActAddMoneyPaymentPay)
            itemAnimator = DefaultItemAnimator()
            adapter = paymentAdapter
        }
    }


    private fun setupKeyList() {
        for (i in 0 until paymentList.size) {
            when (paymentList[i].paymentName) {
                "RazorPay" -> {
                    strRazorPayKey = paymentList[i].publicKey!!
                }
                "Stripe" -> {
                    stripekey = paymentList[i].publicKey.toString()
                }
                "Paystack" -> {
                    payStackKey = paymentList[i].publicKey!!
                }
                "Flutterwave" -> {
                    flutterWaveKey = paymentList[i].publicKey!!
                    encryptionKey = paymentList[i].encryptionKey ?: ""
                }
            }
        }
    }


    private fun callAddMoney(
        tType: String,
        tId: String,
        cardNumber: String,
        cMonth: String,
        cYear: String,
        cCvc: String
    ) {
        val amount=    intent.getStringExtra("amount") ?: "0.00"

        Common.showLoadingProgress(this@ActAddMoneyPaymentPay)
        val map = HashMap<String, String>()
        map["user_id"] = SharePreference.getStringPref(
            this@ActAddMoneyPaymentPay,
            SharePreference.userId
        )!!
        map["amount"] =   String.format(Locale.US, "%.02f", amount.toDouble())
        map["transaction_type"] = tType
        map["transaction_id"] = tId
        map["card_number"] = cardNumber
        map["card_exp_month"] = cMonth
        map["card_exp_year"] = cYear
        map["card_cvc"] = cCvc

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().addMoney(map)) {
                is NetworkResponse.Success -> {
                    val restResponse = response.body
                    Common.dismissLoadingProgress()
                    if (restResponse.status == 1) {

                        Common.showSuccessFullMsg(
                            this@ActAddMoneyPaymentPay,
                            response.body.message.toString()
                        )
                        finish()
                        val intent = Intent(this@ActAddMoneyPaymentPay, ActMyWallet::class.java)
                        SharePreference.setStringPref(
                            this@ActAddMoneyPaymentPay,
                            SharePreference.wallet,
                            response.body.totalWallet.toString() ?: "00"
                        )
                        intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        intent.putExtra("walletAmount", response.body.totalWallet.toString())
                        intent.putExtra("AddMoneyPay",true)
                        startActivity(intent)
                    } else {
                        Common.showErrorFullMsg(
                            this@ActAddMoneyPaymentPay,
                            restResponse.message.toString()
                        )
                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActAddMoneyPaymentPay,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActAddMoneyPaymentPay,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActAddMoneyPaymentPay,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }

    }

    override fun onPaymentSuccess(razorPayId: String?) {
        Common.dismissLoadingProgress()
        razorPayId?.let { callAddMoney("3",it, "", "", "", "") }
    }

    override fun onPaymentError(error: Int, response: String?) {
        try {
            Toast.makeText(this, "$response", Toast.LENGTH_LONG).show()
            Common.dismissLoadingProgress()
        } catch (e: Exception) {
            Log.e("Exception", e.message, e)
        }
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this, false)
    }


}