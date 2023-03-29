package com.singlerestaurant.user.activity

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.View
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
import com.singlerestaurant.user.api.SingleResponsee
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivityPaymentBinding
import com.singlerestaurant.user.model.FlutterWaveResponse
import com.singlerestaurant.user.model.PaymentmethodsItem
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.alertErrorOrValidationDialog
import com.singlerestaurant.user.utils.Common.dismissLoadingProgress
import com.singlerestaurant.user.utils.Common.isCheckNetwork
import com.singlerestaurant.user.utils.Common.showErrorFullMsg
import com.singlerestaurant.user.utils.Common.showLoadingProgress
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.SharePreference.Companion.getStringPref
import com.singlerestaurant.user.utils.SharePreference.Companion.userEmail
import com.singlerestaurant.user.utils.SharePreference.Companion.userId
import com.singlerestaurant.user.utils.SharePreference.Companion.userMobile
import com.singlerestaurant.user.utils.SharePreference.Companion.userName
import com.google.gson.Gson
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.*
import kotlin.math.round
import kotlin.math.roundToInt


class PaymentPayActivity : BaseActivity(), PaymentResultListener {
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
    private lateinit var binding:ActivityPaymentBinding
    override fun setLayout(): View {
        return binding.root
    }

    override fun InitView() {
        binding=ActivityPaymentBinding.inflate(layoutInflater)
        Common.getCurrentLanguage(this@PaymentPayActivity, false)
        Checkout.preload(this@PaymentPayActivity)

        binding.ivBack.setOnClickListener {
            finish()
        }

        if (getStringPref(this@PaymentPayActivity, SharePreference.SELECTED_LANGUAGE).equals(
                resources.getString(R.string.language_hindi)
            )
        ) {
            binding. ivBack.rotation = 180F
        } else {
            binding.ivBack.rotation = 0F
        }
        setupAdapter()
        if (isCheckNetwork(this@PaymentPayActivity)) {
            callApiPayment()
        } else {
            alertErrorOrValidationDialog(
                this@PaymentPayActivity,
                resources.getString(R.string.no_internet)
            )
        }
        binding. tvPaynow.setOnClickListener {
            if (isCheckNetwork(this@PaymentPayActivity)) {
                isOpenOrClose()
            } else {
                alertErrorOrValidationDialog(
                    this@PaymentPayActivity,
                    resources.getString(R.string.no_internet)
                )
            }

        }


    }


    private fun payment() {
        if (strGetData == "Wallet") {
            if (walletAmount >= intent.getStringExtra("getAmount")!!.toDouble()) {
                if (isCheckNetwork(this@PaymentPayActivity)) {
                    showLoadingProgress(this@PaymentPayActivity)
                    callApiOrder("", "2")
                } else {
                    alertErrorOrValidationDialog(
                        this@PaymentPayActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
            } else {
                showErrorFullMsg(
                    this@PaymentPayActivity,
                    resources.getString(R.string.wallet_sufficient_amount)
                )
            }
        } else if (strGetData == "COD") {
            if (isCheckNetwork(this@PaymentPayActivity)) {
                showLoadingProgress(this@PaymentPayActivity)
                callApiOrder("", "1")
            } else {
                alertErrorOrValidationDialog(
                    this@PaymentPayActivity,
                    resources.getString(R.string.no_internet)
                )
            }
        } else if (strGetData == "RazorPay") {
            showLoadingProgress(this@PaymentPayActivity)
            startPayment()
        } else if (strGetData == "Stripe") {
            val intent = Intent(this@PaymentPayActivity, ActStripePayment::class.java)
            intent.putExtra("stripeKey", stripekey)
            activityResult.launch(intent)
        } else if (strGetData == "Flutterwave") {
            flutterWavePayment()
        } else if (strGetData == "Paystack") {
            val amount = intent.getStringExtra("getAmount") ?: "0.00"
            val totalAmount = round(amount.toDouble()).roundToInt()
            val i = Intent(this@PaymentPayActivity, ActPayStack::class.java)
            i.putExtra("email", getStringPref(this@PaymentPayActivity, userEmail))
            i.putExtra("public_key", payStackKey)
            i.putExtra("amount", totalAmount.toString())
            activityResult.launch(i)
        } else if (strGetData == "") {
            showErrorFullMsg(
                this@PaymentPayActivity,
                resources.getString(R.string.payment_type_selection_error)
            )
        }
    }


    private fun isOpenOrClose() {
        Common.showLoadingProgress(this@PaymentPayActivity)

        lifecycleScope.launch {
            val request= HashMap<String, String>()
            request["user_id"]=getStringPref(this@PaymentPayActivity,userId)?:""
            when (val response = ApiClient.getClient().isOpenClose(request)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()
                    if (response.body.status == 1) {

                        if(response.body.isEmptyCart=="0"){
                            payment()
                        }else
                        {
                            val intent = Intent(this@PaymentPayActivity, DashboardActivity::class.java).putExtra("pos", "1")
                            startActivity(intent)
                            finish()
                            finishAffinity()
                        }
                    } else if (response.body.status == 0) {
                        alertErrorOrValidationDialog(
                            this@PaymentPayActivity,
                            resources.getString(R.string.close_restaurant_error)
                        )
                    }
                }

                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@PaymentPayActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@PaymentPayActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@PaymentPayActivity,
                        resources.getString(R.string.error_msg)
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
                    callApiOrder(id, "5")
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

    private fun flutterWavePayment() {
        RaveUiManager(this).setAmount(intent.getStringExtra("getAmount")!!.toDouble())
            .setEmail(getStringPref(this@PaymentPayActivity, userEmail))
            .setfName(getStringPref(this@PaymentPayActivity, userName))
            .setlName(getStringPref(this@PaymentPayActivity, userName))
            .setPublicKey(flutterWaveKey)
            .setEncryptionKey(encryptionKey)
            .setCountry("US")
            .setCurrency("NGN")
            .setTxRef(System.currentTimeMillis().toString() + "Ref")
            .setPhoneNumber(getStringPref(this@PaymentPayActivity, userMobile), false)
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

            Log.e("result", result.resultCode.toString())
            if (result.resultCode == 401) {
                val cardNumber = result.data?.getStringExtra("card_number").toString()
                val expMonth = result.data?.getStringExtra("exp_month").toString()
                val expYear = result.data?.getStringExtra("exp_year").toString()
                val cvv = result.data?.getStringExtra("cvv").toString()
                callStripePayment(cardNumber, expMonth, expYear, cvv)
            } else if (result.resultCode == RESULT_OK) {
                val id = result.data?.getStringExtra("id") ?: ""
                callApiOrder(id, "6")
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
                        callApiOrder(id, "5")
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


    private fun setupAdapter() {
        paymentAdapter =
            PaymentListAdapter(this@PaymentPayActivity, paymentList) { i: Int, s: String ->
                if (s == Constants.ItemClick) {
                    strGetData = paymentList[i].paymentName.toString()
                }
            }
        binding. rvPayment.apply {
            layoutManager = LinearLayoutManager(this@PaymentPayActivity)
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

    private fun callApiPayment() {
        showLoadingProgress(this@PaymentPayActivity)
        val map = HashMap<String, String>()
        map["user_id"] = getStringPref(this@PaymentPayActivity, userId)!!
        map["type"] = intent.getStringExtra("paymentListType") ?: "order"
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().getPaymentType(map)) {
                is NetworkResponse.Success -> {
                    val restResponse = response.body

                    if (restResponse.status == 1) {
                        dismissLoadingProgress()

                        restResponse.paymentmethods?.let { paymentList.addAll(it) }
                        paymentAdapter?.notifyDataSetChanged()
                        if (restResponse.totalWallet != "null") {
                            walletAmount = restResponse.totalWallet?.toDouble() ?: 0.00
                        }
                        setupKeyList()
                        Log.e("stripeKey",stripekey.toString())

                    } else {
                        showErrorFullMsg(
                            this@PaymentPayActivity,
                            restResponse.message.toString()
                        )

                    }
                }
                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@PaymentPayActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@PaymentPayActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@PaymentPayActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }


    override fun onBackPressed() {
        finish()
    }


    private fun startPayment() {
        Common.getLog("test", intent.getStringExtra("getAmount").toString())
        val activity: Activity = this
        val co = Checkout()
        try {
            co.setKeyID(strRazorPayKey)
            val amount = intent.getStringExtra("getAmount")!!.toDouble() * 100
            Common.getLog("test", amount.toString())
            val options = JSONObject()
            options.put("name", resources.getString(R.string.app_name))
            options.put("description", resources.getString(R.string.order_payment))
            options.put("image", logoimg)
            options.put("currency", "INR")
            options.put("amount", String.format(Locale.US, "%d", amount.toLong()))
            val prefill = JSONObject()
            prefill.put("email", getStringPref(this@PaymentPayActivity, userEmail))
            prefill.put("contact", getStringPref(this@PaymentPayActivity, userMobile))
            options.put("prefill", prefill)
            val theme = JSONObject()
            theme.put("color", "#366ed4")
            options.put("theme", theme)
            co.open(activity, options)
        } catch (e: Exception) {
            dismissLoadingProgress()

            Toast.makeText(activity, e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


    override fun onPaymentError(errorCode: Int, response: String?) {
        try {
            Toast.makeText(this, "$response", Toast.LENGTH_LONG).show()
            dismissLoadingProgress()
        } catch (e: Exception) {
            Log.e("Exception", e.message, e)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        try {
            callApiOrder(razorpayPaymentId.toString(), "3")
        } catch (e: Exception) {
            Log.e("Exception", "Exception in onPaymentSuccess", e)
        }
    }

    private fun  callApiOrder(transactionId: String, transactionType: String) {
        showLoadingProgress(this@PaymentPayActivity)


        val hashmap = HashMap<String, String>()

        hashmap["user_id"] = getStringPref(this@PaymentPayActivity, userId)!!
        hashmap["grand_total"] = String.format(Locale.US, "%.02f",intent.getStringExtra("getAmount")?.toDouble() ?: "0.00")
        hashmap["transaction_id"] = transactionId
        hashmap["transaction_type"] = transactionType
        hashmap["offer_code"] = intent.getStringExtra("promocode") ?: ""
        hashmap["discount_amount"] = String.format(Locale.US, "%.02f",intent.getStringExtra("discount_amount")?.toDouble() ?: "0.00")
        hashmap["discount_pr"] = intent.getStringExtra("discount_pr") ?: ""
        hashmap["tax"] = intent.getStringExtra("getTax") ?: ""
        hashmap["tax_amount"] = String.format(Locale.US, "%.02f",intent.getStringExtra("getTaxAmount")?.toDouble() ?: "0.00")
        hashmap["delivery_charge"] = String.format(Locale.US, "%.02f",intent.getStringExtra("delivery_charge")?.toDouble() ?: "0.00")
        hashmap["order_from"] = "android"
        hashmap["order_type"] = intent.getStringExtra("order_type") ?: ""
        hashmap["order_notes"] = intent.getStringExtra("order_notes") ?: ""
        hashmap["address"] = intent.getStringExtra("address") ?: ""
        hashmap["address_type"] = intent.getStringExtra("address_type") ?: ""
        hashmap["area"] = intent.getStringExtra("area") ?: ""
        hashmap["house_no"] = intent.getStringExtra("house_no") ?: ""
        hashmap["lat"] = intent.getStringExtra("lat") ?: ""
        hashmap["lang"] = intent.getStringExtra("lon") ?: ""

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setOrderPayment(hashmap)) {
                is NetworkResponse.Success -> {

                    val restResponse: SingleResponsee = response.body
                    if (restResponse.status == 1) {
                        SharePreference.setStringPref(this@PaymentPayActivity,SharePreference.BadgesCount,"0")
                        dismissLoadingProgress()

                        startActivity(Intent(this@PaymentPayActivity,PaymentSuccessfulActivity::class.java).putExtra("order_id",response.body.orderId.toString()))

                    } else if (restResponse.status == 0) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            this@PaymentPayActivity,
                            restResponse.message.toString()
                        )
                    }
                }
                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@PaymentPayActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@PaymentPayActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@PaymentPayActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }


        }


    }


    private fun callStripePayment(
        cardNumber: String,
        expMonth: String,
        expYear: String,
        cvv: String
    ) {
        showLoadingProgress(this@PaymentPayActivity)

        val hashmap = HashMap<String, String>()
        hashmap["user_id"] = getStringPref(this@PaymentPayActivity, userId)!!
        hashmap["transaction_id"] = ""
        hashmap["transaction_type"] = "4"
        hashmap["offer_code"] = intent.getStringExtra("promocode") ?: ""
        hashmap["discount_amount"] = String.format(Locale.US, "%.02f",intent.getStringExtra("discount_amount")?.toDouble() ?: "0.00")
        hashmap["delivery_charge"] =String.format(Locale.US, "%.02f",intent.getStringExtra("delivery_charge")?.toDouble() ?: "0.00")
        hashmap["grand_total"] = String.format(Locale.US, "%.02f",intent.getStringExtra("getAmount")?.toDouble() ?: "0.00")
        hashmap["tax_amount"] = String.format(Locale.US, "%.02f",intent.getStringExtra("getTaxAmount")?.toDouble() ?: "0.00")

        hashmap["discount_pr"] = intent.getStringExtra("discount_pr") ?: ""
        hashmap["tax"] = intent.getStringExtra("getTax") ?: ""
        hashmap["order_from"] = "android"
        hashmap["order_type"] = intent.getStringExtra("order_type") ?: ""
        hashmap["order_notes"] = intent.getStringExtra("order_notes") ?: ""
        hashmap["address"] = intent.getStringExtra("address") ?: ""
        hashmap["address_type"] = intent.getStringExtra("address_type") ?: ""
        hashmap["area"] = intent.getStringExtra("area") ?: ""
        hashmap["house_no"] = intent.getStringExtra("house_no") ?: ""
        hashmap["lat"] = intent.getStringExtra("lat") ?: ""
        hashmap["lang"] = intent.getStringExtra("lon") ?: ""
        hashmap["card_number"] = cardNumber
        hashmap["card_exp_month"] = expMonth
        hashmap["card_exp_year"] = expYear
        hashmap["card_cvc"] = cvv

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setOrderPayment(hashmap)) {
                is NetworkResponse.Success -> {

                    val restResponse: SingleResponsee = response.body
                    if (restResponse.status == 1) {
                        dismissLoadingProgress()
                        startActivity(Intent(this@PaymentPayActivity,PaymentSuccessfulActivity::class.java).putExtra("order_id",response.body.orderId.toString()))

                    } else if (restResponse.status == 0) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            this@PaymentPayActivity,
                            restResponse.message
                        )
                    }
                }
                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@PaymentPayActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@PaymentPayActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    showErrorFullMsg(
                        this@PaymentPayActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }


        }


    }


    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@PaymentPayActivity, false)
    }


}