package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.android.SphericalUtil
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.api.RestResponse
import com.singlerestaurant.user.api.RestSummaryResponse
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.base.BaseAdaptor
import com.singlerestaurant.user.databinding.ActivityYoursorderdetailBinding
import com.singlerestaurant.user.fragment.FragAddons
import com.singlerestaurant.user.model.*
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.alertErrorOrValidationDialog
import com.singlerestaurant.user.utils.Common.dismissLoadingProgress
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.FieldSelector
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.SharePreference.Companion.getStringPref
import com.singlerestaurant.user.utils.SharePreference.Companion.isCurrancy
import com.singlerestaurant.user.utils.SharePreference.Companion.isMaximum
import com.singlerestaurant.user.utils.SharePreference.Companion.isMiniMum
import com.singlerestaurant.user.utils.SharePreference.Companion.mapKey
import com.singlerestaurant.user.utils.SharePreference.Companion.userId
import kotlinx.coroutines.launch
import java.util.*

class OrderSummeryActivity : BaseActivity() {
    var summaryModel = SummaryModel()
    var promoCodeList: ArrayList<PromocodeModel>? = null
    var discountAmount = "0.00"
    var discountPer = "0"
    var promoCodePrice: Float = 0.0F
    var lat: Double = 0.0
    var lon: Double = 0.0
    var addressType = 0
    var area = ""
    var address = ""
    var houseNo = ""
    var selectDelivery = 1
    var deliveryCharge = "0.00"
    var currency = ""
    var currencyPosition = ""
    var totalQty = 0
    var fieldSelector: FieldSelector? = null
    private lateinit var binding: ActivityYoursorderdetailBinding
    override fun setLayout(): View {
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun InitView() {
        binding = ActivityYoursorderdetailBinding.inflate(layoutInflater)
        promoCodeList = ArrayList()
        val mapKey = getStringPref(this@OrderSummeryActivity, mapKey)
        mapKey?.let { Places.initialize(applicationContext, it) }
        fieldSelector = FieldSelector()
        binding.rlOffer.visibility = View.GONE
        if (Common.isCheckNetwork(this@OrderSummeryActivity)) {
            callApiOrderSummary()
        } else {
            alertErrorOrValidationDialog(this@OrderSummeryActivity, resources.getString(R.string.no_internet))
        }

        currency = getStringPref(this@OrderSummeryActivity, isCurrancy).toString()
        currencyPosition =
            getStringPref(this@OrderSummeryActivity, SharePreference.CurrencyPosition).toString()

        if (getStringPref(this@OrderSummeryActivity, SharePreference.SELECTED_LANGUAGE).equals(resources.getString(R.string.language_hindi))) {
            binding.ivBack.rotation = 180F
        } else {
            binding.ivBack.rotation = 0F
        }
        if (intent.getStringExtra("DeliveryType")?.equals("2") == true) {
            binding.llAddress.visibility = View.GONE
            binding.llAddressLayout.visibility = View.GONE
        } else {
            binding.llAddress.visibility = View.VISIBLE
            binding.llAddressLayout.visibility = View.VISIBLE
        }
        selectDelivery = intent.getStringExtra("DeliveryType")?.toInt() ?: 0
        binding.tvSelectAddress.setOnClickListener {
            val intent = Intent(this@OrderSummeryActivity, GetAddressActivity::class.java)
            intent.putExtra("isComeFromSelectAddress", true)
            activityResult.launch(intent)
        }

        binding.edAddress.setOnClickListener {
            getLocation()
        }


        binding.tvProceedToPaymnet.setOnClickListener {
            isOpenOrClose()
        }
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.tvbtnPromocode.setOnClickListener {
            if (Common.isCheckNetwork(this@OrderSummeryActivity)) {
                callApiPromoCode()
            } else {
                alertErrorOrValidationDialog(
                    this@OrderSummeryActivity, resources.getString(R.string.no_internet))
            }
        }

        binding.tvApply.setOnClickListener {
            if (binding.tvApply.text.toString() == resources.getString(R.string.apply)) {


                if (binding.edPromocode.text.toString() != "") {
                    if (summaryModel.getOrder_total()?.toDouble() ?: 0.00 < getStringPref(
                            this@OrderSummeryActivity,
                            isMiniMum
                        )?.toDouble() ?: 0.00
                    ) {
                        Common.showErrorFullMsg(
                            this@OrderSummeryActivity,
                            resources.getString(R.string.offer_error)
                        )
                    } else {
                        callApiCheckPromoCode()
                    }

                }
            } else if (binding.tvApply.text.toString() == resources.getString(R.string.remove)) {
                binding.tvbtnPromocode.visibility = View.VISIBLE
                binding.tvPromoCodeApply.text = ""
                binding.tvDiscountOffer.text = ""
                binding.edPromocode.text = ""
                binding.tvApply.text = resources.getString(R.string.apply)
                binding.rlOffer.visibility = View.GONE
                if (selectDelivery == 1) {

                    binding.tvOrderTotalPrice.text = Common.getPrices(
                        currencyPosition,
                        currency,
                        summaryModel.getOrder_total() ?: "0.00"
                    )



                    binding.tvOrderTaxPrice.text = Common.getPrices(
                        currencyPosition,
                        currency,
                        summaryModel.getTax() ?: "0.00"
                    )



                    binding.tvOrderDeliveryCharge.text =
                        Common.getPrices(currencyPosition, currency, deliveryCharge ?: "0.00")


                    val totalprice = summaryModel.getOrder_total()!!
                        .toDouble() + summaryModel.getTax()
                        ?.toDouble()!! + deliveryCharge.toDouble()
                    binding.tvOrderTotalCharge.text = Common.getPrices(
                        currencyPosition, currency,
                        totalprice.toString()
                    )
                    discountAmount = "0.00"
                } else {

                    binding.tvOrderTotalPrice.text = Common.getPrices(
                        currencyPosition, currency,
                        summaryModel.getOrder_total().toString()
                    )
                    binding.tvOrderTaxPrice.text = Common.getPrices(
                        currencyPosition, currency,
                        summaryModel.getTax().toString()
                    )


                    binding.tvOrderDeliveryCharge.text = Common.getPrices(
                        currencyPosition, currency,
                        "0.00"
                    )

                    val totalprice =
                        summaryModel.getOrder_total()!!.toDouble() + summaryModel.getTax()
                            ?.toDouble()!! + 0.00
                    binding.tvOrderTotalCharge.text =
                        Common.getPrices(currencyPosition, currency, totalprice.toString())

                    discountAmount = "0.00"
                }

            }
        }

    }

    override fun onBackPressed() {
        finish()
    }


    private fun paymentNavigation() {
        if (totalQty <= getStringPref(
                this@OrderSummeryActivity,
                SharePreference.isMiniMumQty
            )?.toInt() ?: 0
        ) {


            if (selectDelivery == 1) {
                if (binding.edAddress.text.toString() == "") {
                    alertErrorOrValidationDialog(
                        this@OrderSummeryActivity,
                        resources.getString(R.string.address_error)
                    )
                } else {

                    if (summaryModel.getOrder_total()!!.toDouble() >= getStringPref(
                            this@OrderSummeryActivity,
                            isMiniMum
                        )!!.toDouble() && summaryModel.getOrder_total()!!
                            .toDouble() <= getStringPref(
                            this@OrderSummeryActivity,
                            isMaximum
                        )!!.toDouble()
                    ) {
                        val intent =
                            Intent(this@OrderSummeryActivity, PaymentPayActivity::class.java)
                        val strTotalCharge = binding.tvOrderTotalCharge.text.toString()
                            .replace(getStringPref(this@OrderSummeryActivity, isCurrancy)!!, "")
                        val strActuleCharge = strTotalCharge.replace(",", "")
                        intent.putExtra(
                            "getAmount",
                            String.format(Locale.US, "%.2f", strActuleCharge.toDouble())
                        )
                        intent.putExtra("getAddress", binding.edAddress.text.toString())
                        intent.putExtra("getTax", summaryModel.getTax())
                        intent.putExtra(
                            "getTaxAmount",
                            String.format(Locale.US, "%.2f", summaryModel.getTax()!!.toFloat())
                        )
                        intent.putExtra(
                            "delivery_charge",
                            String.format(Locale.US, "%.2f", deliveryCharge.toDouble())
                        )
                        intent.putExtra("promocode", binding.tvPromoCodeApply.text.toString())
                        intent.putExtra("discount_amount", discountAmount)
                        intent.putExtra("order_notes", binding.edNotes.text.toString())
                        intent.putExtra("lat", lat.toString())
                        intent.putExtra("lon", lon.toString())
                        intent.putExtra("order_type", "1")
                        intent.putExtra("address_type", addressType.toString())
                        intent.putExtra("address", address)
                        intent.putExtra("area", area)
                        intent.putExtra("house_no", houseNo)
                        intent.putExtra("paymentListType", "order")
                        startActivity(intent)
                    } else {
                        alertErrorOrValidationDialog(
                            this@OrderSummeryActivity,
                            "Order amount must be between ${
                                getStringPref(
                                    this@OrderSummeryActivity,
                                    isCurrancy
                                ) + getStringPref(this@OrderSummeryActivity, isMiniMum)
                            } and ${
                                getStringPref(
                                    this@OrderSummeryActivity,
                                    isCurrancy
                                ) + getStringPref(this@OrderSummeryActivity, isMaximum)
                            }"
                        )
                    }
                }
            } else if (selectDelivery == 2) {
                if (summaryModel.getOrder_total()!!.toDouble() >= getStringPref(
                        this@OrderSummeryActivity,
                        isMiniMum
                    )!!.toDouble() && summaryModel.getOrder_total()!!.toDouble() <= getStringPref(
                        this@OrderSummeryActivity,
                        isMaximum
                    )!!.toDouble()
                ) {
                    val intent = Intent(this@OrderSummeryActivity, PaymentPayActivity::class.java)
                    val strTotalCharge = binding.tvOrderTotalCharge.text.toString()
                        .replace(getStringPref(this@OrderSummeryActivity, isCurrancy)!!, "")
                    val strActuleCharge = strTotalCharge.replace(",", "")
                    val orderTax: Float =
                        (summaryModel.getOrder_total()!!.toFloat() * summaryModel.getTax()!!
                            .toFloat()) / 100
                    intent.putExtra(
                        "getAmount",
                        String.format(Locale.US, "%.2f", strActuleCharge.toDouble())
                    )
                    intent.putExtra("getAddress", "")
                    intent.putExtra("getTax", summaryModel.getTax())
                    intent.putExtra(
                        "getTaxAmount",
                        String.format(Locale.US, "%.2f", summaryModel.getTax()!!.toFloat())
                    )
                    intent.putExtra("delivery_charge", "0.00")
                    intent.putExtra("promocode", binding.tvPromoCodeApply.text.toString())
                    intent.putExtra("discount_amount", discountAmount)
                    intent.putExtra("order_notes", binding.edNotes.text.toString())
                    intent.putExtra("order_type", "2")
                    intent.putExtra("address_type", "")
                    intent.putExtra("address", "")
                    intent.putExtra("area", "")
                    intent.putExtra("house_no", "")
                    intent.putExtra("lat", "")
                    intent.putExtra("lon", "")
                    intent.putExtra("paymentListType", "order")
                    startActivity(intent)
                } else {
                    alertErrorOrValidationDialog(
                        this@OrderSummeryActivity,
                        "${resources.getString(R.string.order_must_between)} ${
                            getStringPref(
                                this@OrderSummeryActivity,
                                isCurrancy
                            ) + getStringPref(this@OrderSummeryActivity, isMiniMum)
                        } and ${
                            getStringPref(
                                this@OrderSummeryActivity,
                                isCurrancy
                            ) + getStringPref(this@OrderSummeryActivity, isMaximum)
                        }"
                    )
                }
            }

        } else {
            alertErrorOrValidationDialog(
                this@OrderSummeryActivity,
                "Item Quantity count must be less than ${
                    getStringPref(
                        this@OrderSummeryActivity,
                        SharePreference.isMiniMumQty
                    )
                }"
            )
        }
    }

    private fun callApiOrderSummary() {
        Common.showLoadingProgress(this@OrderSummeryActivity)
        val map = HashMap<String, String>()
        map["user_id"] = getStringPref(this@OrderSummeryActivity, userId)!!

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setSummary(map)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()
                    val restResponse: RestSummaryResponse = response.body
                    if (restResponse.getStatus().equals("1")) {
                        binding.rlSummery?.visibility = View.VISIBLE
                        if (restResponse.getData().size > 0) {
                            binding.rvOrderItemFood.visibility = View.VISIBLE
                            val foodCategoryList = restResponse.getData()
                            val summary = restResponse.getSummery()
                            setFoodCategoryAdaptor(foodCategoryList, summary)
                        } else {
                            binding.rvOrderItemFood.visibility = View.GONE
                        }
                    } else if (restResponse.getStatus().equals("0")) {
                        dismissLoadingProgress()
                        binding.rvOrderItemFood.visibility = View.GONE
                    }
                }
                is NetworkResponse.ApiError -> {

                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }


    private fun callApiPromoCode() {
        Common.showLoadingProgress(this@OrderSummeryActivity)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().getPromoCodeList()) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()
                    val restResponse: ListResponse<PromocodeModel> = response.body
                    if (restResponse.status == 1) {
                        if (restResponse.data!!.size > 0) {
                            promoCodeList = restResponse.data
                            openDialogPromoCode()
                        }
                    }
                }
                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }


    @SuppressLint("SetTextI18n")
    private fun callApiCheckPromoCode() {
        Common.showLoadingProgress(this@OrderSummeryActivity)
        val map = HashMap<String, String>()
        map["user_id"] = getStringPref(this@OrderSummeryActivity, userId)!!
        map["offer_code"] = binding.edPromocode.text.toString()
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setApplyPromocode(map)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()
                    val restResponse: RestResponse<PromoCodeData> = response.body
                    if (restResponse.getStatus().equals("1")) {
                        binding.tvbtnPromocode.visibility = View.GONE
                        binding.rlOffer.visibility = View.VISIBLE
                        var subtotalCharge = 0.00
                        if (restResponse.getData()?.offerType == "2") {
                            binding.tvDiscountOffer.text =
                                restResponse.getData()!!.offerAmount + "%"
                            subtotalCharge = (summaryModel.getOrder_total()!!
                                .toFloat() * restResponse.getData()!!.offerAmount!!
                                .toFloat() / 100).toDouble()
                        } else {
                            subtotalCharge = restResponse.getData()?.offerAmount?.toDouble()!!
                            binding.tvDiscountOffer.text = restResponse.getData()!!.offerAmount

                        }
                        binding.tvPromoCodeApply.text = restResponse.getData()!!.offerCode
                        binding.tvApply.text = resources.getString(R.string.remove)
                        if (selectDelivery == 1) {

                            val total = summaryModel.getOrder_total()!!.toFloat() - subtotalCharge

                            val mainTotal =
                                summaryModel.getTax()!!
                                    .toDouble() + total + deliveryCharge.toFloat()
                            binding.tvDiscountOffer.text = Common.getPrices(
                                currencyPosition,
                                currency,
                                subtotalCharge.toString()
                            )
                            Log.e("total", total.toString())
                            Log.e("subTotalCharge", subtotalCharge.toString())


                            binding.tvOrderTotalCharge.text =
                                Common.getPrices(currencyPosition, currency, mainTotal.toString())

                            discountAmount = subtotalCharge.toString()
                        } else {

                            val total = summaryModel.getOrder_total()!!.toFloat() - subtotalCharge

                            Log.e("total", total.toString())
                            Log.e("subTotalCharge", subtotalCharge.toString())

                            val mainTotal = summaryModel.getTax()!!.toDouble() + total + 0.00

                            binding.tvDiscountOffer.text = Common.getPrices(
                                currencyPosition,
                                currency,
                                subtotalCharge.toString()
                            )

                            binding.tvOrderTotalCharge.text =
                                Common.getPrices(currencyPosition, currency, mainTotal.toString())
                            discountAmount = subtotalCharge.toString()
                        }
                    } else if (restResponse.getStatus().equals("0")) {
                        dismissLoadingProgress()
                        binding.edPromocode.text = ""
                        binding.rlOffer.visibility = View.GONE
                        binding.tvApply.text = resources.getString(R.string.apply)
                        alertErrorOrValidationDialog(
                            this@OrderSummeryActivity,
                            restResponse.getMessage()
                        )
                    }
                }
                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }


    @SuppressLint("SetTextI18n")
    private fun setFoodCategoryAdaptor(
        foodCategoryList: ArrayList<OrderSummaryModel>,
        summary: SummaryModel?
    ) {
        if (foodCategoryList.size > 0) {
            setFoodCategoryAdaptor(foodCategoryList)
            totalQty = 0
            totalQty = foodCategoryList.sumOf { it.qty?.toInt() ?: 0 }
        }
        summaryModel = summary!!
        binding.tvOrderTotalPrice.text =
            Common.getPrices(currencyPosition, currency, summary.getOrder_total().toString())


        binding.tvOrderTaxPrice.text =
            Common.getPrices(currencyPosition, currency, summary.getTax().toString())

        binding.tvTitleTex.text = "Tax"
        binding.tvOrderDeliveryCharge.text =
            Common.getPrices(currencyPosition, currency, deliveryCharge)

        val totalprice = summary.getOrder_total()!!.toFloat() + summary.getTax()
            ?.toFloat()!! + deliveryCharge.toFloat()
        binding.tvOrderTotalCharge.text =
            Common.getPrices(currencyPosition, currency, totalprice.toString())

    }

    private fun setFoodCategoryAdaptor(orderHistoryList: ArrayList<OrderSummaryModel>) {
        val orderHistoryAdapter =
            object : BaseAdaptor<OrderSummaryModel>(this@OrderSummeryActivity, orderHistoryList) {
                @SuppressLint("SetTextI18n", "NewApi", "UseCompatLoadingForDrawables")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: OrderSummaryModel,
                    position: Int
                ) {
                    val tvOrderFoodName: TextView = holder!!.itemView.findViewById(R.id.tvFoodName)
                    val tvPrice: TextView = holder.itemView.findViewById(R.id.tvPrice)
                    val tvVariation: TextView = holder.itemView.findViewById(R.id.tvVariation)
                    val tvAddOns: TextView = holder.itemView.findViewById(R.id.tvAddons)
                    val tvQtyNumber: TextView = holder.itemView.findViewById(R.id.tvQty)
                    val ivVeg: AppCompatImageView = holder.itemView.findViewById(R.id.ivVeg)
                    val ivFoodCart: AppCompatImageView =
                        holder.itemView.findViewById(R.id.ivFoodCart)


                    if (`val`.itemType == 2) {
                        ivVeg.setImageResource(R.drawable.ic_nonveg)
                    } else {
                        ivVeg.setImageResource(R.drawable.ic_vegetarian)
                    }

                    tvOrderFoodName.text = orderHistoryList[position].itemName.toString()
                    tvQtyNumber.text = "QTY : ${orderHistoryList[position].qty}"


                    val itemPrice = orderHistoryList[position].itemPrice?.toDouble()
                        ?.plus(orderHistoryList[position].addons_total_price?.toDouble() ?: 0.00)

                    Log.e("itemImage", orderHistoryList[position].itemImage.toString())
                    Glide.with(this@OrderSummeryActivity).load(orderHistoryList[position].itemImage)
                        .transition(DrawableTransitionOptions.withCrossFade(500))
                        .into(ivFoodCart)

                    tvPrice.text = Common.getPrices(
                        currencyPosition,
                        currency,
                        itemPrice.toString()
                    )


                    if(orderHistoryList[position].variation?.isEmpty() == true ||orderHistoryList[position].variation=="null")
                    {
                        tvVariation.text="-"
                    }else
                    {
                        tvVariation.text = orderHistoryList[position].variation

                    }


                    if (orderHistoryList[position].addonsName?.isEmpty() == true || orderHistoryList[position].addonsName == "null") {
                        tvAddOns.text = "-"
                        tvAddOns.isEnabled = false
                        tvAddOns.isClickable = false
                    } else {
                        tvAddOns.text = "Add-ons>>"
                        tvAddOns.isEnabled = true
                        tvAddOns.isClickable = true
                    }

                    tvAddOns.setOnClickListener {
                        val fragAddOns = FragAddons()
                        val bundle = Bundle()
                        bundle.putString("addOnsName", orderHistoryList[position].addonsName)
                        bundle.putString("addOnsPrice", orderHistoryList[position].addonsPrice)
                        fragAddOns.arguments = bundle
                        fragAddOns.show(supportFragmentManager, FragAddons::class.java.name)

                    }








                }

                override fun setItemLayout(): Int {
                    return R.layout.cell_order_summery_item
                }

            }
        binding.rvOrderItemFood.adapter = orderHistoryAdapter
        binding.rvOrderItemFood.layoutManager = LinearLayoutManager(this@OrderSummeryActivity)
        binding.rvOrderItemFood.itemAnimator = DefaultItemAnimator()
        binding.rvOrderItemFood.isNestedScrollingEnabled = true
    }


    private fun isOpenOrClose() {
        Common.showLoadingProgress(this@OrderSummeryActivity)
        val request = java.util.HashMap<String, String>()
        request["user_id"] = getStringPref(this@OrderSummeryActivity, userId) ?: ""
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().isOpenClose(request)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()

                    if (response.body.status == 1) {

                        if (response.body.isEmptyCart == "0") {
                            paymentNavigation()
                        } else {
                            val intent = Intent(
                                this@OrderSummeryActivity,
                                DashboardActivity::class.java
                            ).putExtra("pos", "1")
                            startActivity(intent)
                            finish()
                            finishAffinity()
                        }
                    } else if (response.body.status == 0) {
                        alertErrorOrValidationDialog(
                            this@OrderSummeryActivity,
                            resources.getString(R.string.close_restaurant_error)
                        )
                    }
                }

                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }

    private fun openDialogPromoCode() {
        val dialog = Dialog(this@OrderSummeryActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val lp = WindowManager.LayoutParams()
        lp.windowAnimations = R.style.DialogAnimation
        dialog.window!!.attributes = lp
        dialog.setContentView(R.layout.dlg_procode)
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val ivCancel = dialog.findViewById<ImageView>(R.id.ivCancel)
        val rvPromoCode = dialog.findViewById<RecyclerView>(R.id.rvPromoCode)
        val tvNoDataFound = dialog.findViewById<TextView>(R.id.tvNoDataFound)
        if (promoCodeList!!.size > 0) {
            rvPromoCode.visibility = View.VISIBLE
            tvNoDataFound.visibility = View.GONE
            setPromoCodeAdaptor(promoCodeList!!, rvPromoCode, dialog)
        } else {
            rvPromoCode.visibility = View.GONE
            tvNoDataFound.visibility = View.VISIBLE
        }

        ivCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setPromoCodeAdaptor(
        promoCodeList: ArrayList<PromocodeModel>,
        rvPromoCode: RecyclerView,
        dialog: Dialog
    ) {
        val orderHistoryAdapter =
            object : BaseAdaptor<PromocodeModel>(this@OrderSummeryActivity, promoCodeList) {
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: PromocodeModel,
                    position: Int
                ) {
                    val tvTitleOrderNumber: AppCompatTextView =
                        holder!!.itemView.findViewById(R.id.tvTitleOrderNumber)
                    val tvPromoCode: AppCompatTextView =
                        holder.itemView.findViewById(R.id.tvPromocode)
                    val tvPromoCodeDescription: TextView =
                        holder.itemView.findViewById(R.id.tvPromocodeDescription)
                    val tvCopyCode: AppCompatTextView =
                        holder.itemView.findViewById(R.id.tvCopyCode)

                    tvTitleOrderNumber.text = promoCodeList[position].getOffer_code()
                    tvPromoCode.text = promoCodeList[position].getOffer_name()
                    tvPromoCodeDescription.text = promoCodeList[position].getDescription()

                    tvCopyCode.setOnClickListener {
                        dialog.dismiss()
                        promoCodePrice = promoCodeList[position].getOffer_amount()!!.toFloat()
                        binding.edPromocode.text = promoCodeList[position].getOffer_code()
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_promocode
                }

            }
        rvPromoCode.adapter = orderHistoryAdapter
        rvPromoCode.layoutManager = LinearLayoutManager(this@OrderSummeryActivity)
        rvPromoCode.itemAnimator = DefaultItemAnimator()
        rvPromoCode.isNestedScrollingEnabled = true
    }

    private fun getLocation() {
        val autocompleteIntent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN,
            fieldSelector!!.allFields
        ).build(this@OrderSummeryActivity)

        activityResult.launch(autocompleteIntent)
    }


    private var activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                AutocompleteActivity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(result.data!!)
                    binding.edAddress.text = place.address
                    val latLng: String = place.latLng.toString()
                    val tempArray =
                        latLng.substring(latLng.indexOf("(") + 1, latLng.lastIndexOf(")"))
                            .split(",")
                            .toTypedArray()
                    lat = tempArray[0].toDouble()
                    lon = tempArray[1].toDouble()

                }
                AutocompleteActivity.RESULT_ERROR -> {
                    val status: Status = Autocomplete.getStatusFromIntent(result.data!!)
                    Common.showErrorFullMsg(
                        this@OrderSummeryActivity,
                        resources.getString(R.string.invalid_key)
                    )
                }
                AutocompleteActivity.RESULT_CANCELED -> {
                    Common.getLog("Nice", " RESULT_CANCELED : AutoComplete Places")
                }

                500 -> {
                    val data = result.data
                    binding.llAddress.visibility = View.VISIBLE
                    binding.edAddress.visibility = View.VISIBLE
                    binding.edAddress.text = data?.getStringExtra("Address")
                        .plus(",")
                        .plus(data?.getStringExtra("houseNumber"))
                        .plus(", ${data?.getStringExtra("area")}")
                    area = data?.getStringExtra("area") ?: ""
                    address = data?.getStringExtra("Address") ?: ""
                    houseNo = data?.getStringExtra("houseNumber") ?: ""
                    addressType = data?.getStringExtra("addressType")?.toInt() ?: 0
                    lat = data?.getStringExtra("lat")?.toDouble() ?: 0.00
                    lon = data?.getStringExtra("long")?.toDouble() ?: 0.00
                    val resLat = getStringPref(this@OrderSummeryActivity, SharePreference.resLat)
                    val resLong = getStringPref(this@OrderSummeryActivity, SharePreference.resLang)

                    if (ApiClient.System_environment == Constants.SendBox) {
                        deliveryCharge = getStringPref(
                            this@OrderSummeryActivity,
                            SharePreference.kmCharge
                        )?.toDouble().toString()
                    } else {
                        val distance = SphericalUtil.computeDistanceBetween(
                            LatLng(lat, lon),
                            LatLng(resLat?.toDouble() ?: 0.00, resLong?.toDouble() ?: 0.00)
                        )

                        val kmCharge = ((distance / 1000) * getStringPref(
                            this@OrderSummeryActivity,
                            SharePreference.kmCharge
                        )?.toDouble()!!)

                        deliveryCharge = kmCharge.toString()

                        Log.e("deliveryCharge", deliveryCharge.toString())
                        Log.e("kmCharge", kmCharge.toString())
                        Log.e("User", "$lat,$lon")
                        Log.e("Restaurant", "$resLat,$resLong")
                        Log.e("distance", (distance / 1000).toString())

                    }



                    binding.tvOrderDeliveryCharge.text =
                        Common.getPrices(currencyPosition, currency, deliveryCharge ?: "0.00")


                    val totalprice =
                        summaryModel.getOrder_total()!!.toDouble() + summaryModel.getTax()
                            ?.toFloat()!! + deliveryCharge.toDouble()
                    binding.tvOrderTotalCharge.text =
                        Common.getPrices(currencyPosition, currency, totalprice.toString())
                    val addressTypeArray = arrayOf("Home", "Work", "Other")

                    when (addressType) {
                        1 -> {
                            binding.tvType.text = addressTypeArray[0]
                        }
                        2 -> {
                            binding.tvType.text = addressTypeArray[1]
                        }
                        3 -> {
                            binding.tvType.text = addressTypeArray[2]
                        }
                    }
                }
            }
        }

}