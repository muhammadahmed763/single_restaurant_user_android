package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.api.SingleResponsee
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.base.BaseAdaptor
import com.singlerestaurant.user.databinding.ActivityOrderdetailBinding
import com.singlerestaurant.user.databinding.DlgConfomationBinding
import com.singlerestaurant.user.fragment.FragAddons
import com.singlerestaurant.user.model.OrderDetailModel
import com.singlerestaurant.user.model.OrderDetailResponse
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.alertErrorOrValidationDialog
import com.singlerestaurant.user.utils.Common.showLoadingProgress
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.SharePreference.Companion.getStringPref
import kotlinx.coroutines.launch

class OrderDetailActivity : BaseActivity() {
    var orderStatus = ""
    var paymentType = ""
    var currency = ""
    var currencyPosition = ""
    private lateinit var binding: ActivityOrderdetailBinding
    override fun setLayout(): View {
        return binding.root
    }

    override fun InitView() {
        binding= ActivityOrderdetailBinding.inflate(layoutInflater)


        if (SharePreference.getBooleanPref(this@OrderDetailActivity,SharePreference.isLogin)) {

            if (Common.isCheckNetwork(this@OrderDetailActivity)) {
                callApiOrderDetail()
            } else {
                alertErrorOrValidationDialog(
                    this@OrderDetailActivity,
                    resources.getString(R.string.no_internet)
                )
            }
        }else
        {
            openActivity(LoginActivity::class.java)
            finish()
            finishAffinity()
        }
        binding.ivBack.setOnClickListener {
            if(intent.getBooleanExtra(Constants.FromNotification,false)) {
                val i = Intent(this@OrderDetailActivity, DashboardActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
                finish()
            }else if(intent.getBooleanExtra("fromOrderSuccess",false))
            {
                val i = Intent(this@OrderDetailActivity, DashboardActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
                finish()
            }
            else
            {
                finish()
            }

        }


        currency = getStringPref(this@OrderDetailActivity, SharePreference.isCurrancy).toString()
        currencyPosition = getStringPref(this@OrderDetailActivity, SharePreference.CurrencyPosition).toString()

        if (getStringPref(this@OrderDetailActivity, SharePreference.SELECTED_LANGUAGE).equals(resources.getString(R.string.language_hindi)))
        {
            binding.ivBack.rotation = 180F
        } else {
            binding.ivBack.rotation = 0F

        }



        binding.tvProceedToPaymnet.setOnClickListener {
            mCancelOrderDialog(intent.getStringExtra("order_id")!!)
        }

    }

    override fun onBackPressed() {
        if(intent.getBooleanExtra(Constants.FromNotification,false)) {
            val i = Intent(this@OrderDetailActivity, DashboardActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
            finish()
        }else if(intent.getBooleanExtra("fromOrderSuccess",false))
        {
            val i = Intent(this@OrderDetailActivity, DashboardActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
            finish()
        }
        else
        {
            finish()

        }
    }


    @SuppressLint("NewApi")
    private fun orderInfo(response: OrderDetailResponse) {
        paymentType = response.summery?.transactionType.toString()
        orderStatus = response.summery?.status.toString()

        when {
            paymentType.toInt() == 1 -> {
                binding.orderInfo.tvPaymentType.text = resources.getString(R.string.payment_type_)
                    .plus("  ${resources.getString(R.string.cash)}")
            }
            paymentType.toInt() == 3 -> {
                binding.orderInfo.tvPaymentType.text = resources.getString(R.string.payment_type_)
                    .plus("  ${resources.getString(R.string.razorpay)}")
            }
            paymentType.toInt() == 4 -> {
                binding.orderInfo.tvPaymentType.text = resources.getString(R.string.payment_type_)
                    .plus("  ${resources.getString(R.string.stripe)}")
            }
            paymentType.toInt() == 5 -> {
                binding.orderInfo.tvPaymentType.text = resources.getString(R.string.payment_type_)
                    .plus("  ${resources.getString(R.string.flutter_wave)}")
            }
            paymentType.toInt() == 6 -> {
                binding.orderInfo.tvPaymentType.text = resources.getString(R.string.payment_type_)
                    .plus("  ${resources.getString(R.string.paystack)}")
            }

            else -> {
                binding.orderInfo.tvPaymentType.text = resources.getString(R.string.payment_type_)
                    .plus("  ${resources.getString(R.string.wallet)}")
            }
        }
        if (response.summery?.orderType == "2") {
            binding.orderInfo.tvOrderType.text = resources.getString(R.string.take_away)
        } else if (response.summery?.orderType == "1") {
            binding.orderInfo.tvOrderType.text = resources.getString(R.string.delivery)
        }
        binding.orderInfo.tvOrderDate.text = response.summery?.date
        binding.orderInfo.tvOrderNumber?.text = "#${response.summery?.orderNumber}"



        binding.orderInfo.tvPrice.text = Common.getPrices(currencyPosition, currency, response.summery?.grandTotal ?: "0.00")
        if (orderStatus == "6") {
            binding.cvDeliveryAddress.visibility=View.GONE
            binding.orderInfo.tvOrderStatus.text = resources.getString(R.string.cancelled)
            binding.orderInfo.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                ResourcesCompat.getColor(
                    resources,
                    R.color.status1,
                    null
                )
            )
        } else if (orderStatus == "7") {
            binding.cvDeliveryAddress.visibility=View.GONE

            binding.orderInfo.tvOrderStatus.text = resources.getString(R.string.cancelled)
            binding.orderInfo.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                ResourcesCompat.getColor(
                    resources,
                    R.color.status1,
                    null
                )
            )
        } else {
            if (response.summery?.orderType == "1") {
                when (orderStatus) {
                    "1" -> {
                        binding.orderInfo.tvOrderStatus.text =
                            resources.getString(R.string.order_place)
                        binding.orderInfo.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.status1,
                                null
                            )
                        )
                    }
                    "2" -> {

                        binding.orderInfo.tvOrderStatus.text =
                            resources.getString(R.string.accepted)
                        binding.orderInfo.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.status2,
                                null
                            )
                        )
                    }
                    "3" -> {
                        binding.orderInfo.tvOrderStatus.text =
                            resources.getString(R.string.order_ready)
                        binding.orderInfo.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.dark_blue,
                                null
                            )
                        )
                    }
                    "4" -> {

                        binding.orderInfo.tvOrderStatus.text =
                            resources.getString(R.string.on_the_way)
                        binding.orderInfo.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.blue,
                                null
                            )
                        )
                    }
                    "5" -> {
                        binding.orderInfo.tvOrderStatus.text =
                            resources.getString(R.string.order_completed)
                        binding.orderInfo.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.light_green,
                                null
                            )
                        )
                    }
                }
            } else {
                when (orderStatus) {
                    "1" -> {

                        binding.orderInfo.tvOrderStatus.text = resources.getString(R.string.order_place)
                        binding.orderInfo.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.status1,
                                null
                            )
                        )
                    }
                    "2" -> {

                        binding.orderInfo.tvOrderStatus.text = resources.getString(R.string.order_ready)
                        binding.orderInfo.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.status2,
                                null
                            )
                        )
                    }
                    "3" -> {

                        binding.orderInfo.tvOrderStatus.text = resources.getString(R.string.order_ready)
                        binding.orderInfo.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.dark_blue,
                                null
                            )
                        )
                    }
                    "4" -> {

                        binding.orderInfo.tvOrderStatus.text =
                            resources.getString(R.string.waiting_for_pickup)
                        binding.orderInfo.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.blue,
                                null
                            )
                        )
                    }
                    "5" -> {
                        binding.orderInfo.tvOrderStatus.text = resources.getString(R.string.order_completed)
                        binding.orderInfo.tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.light_green,
                                null
                            )
                        )

                    }
                }
            }
        }
    }

    private fun callApiOrderDetail() {
        showLoadingProgress(this@OrderDetailActivity)
        val map = HashMap<String, String>()
        map["order_id"] = intent.getStringExtra("order_id")!!

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setGetOrderDetail(map)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    binding.rlOrderDetail.visibility = View.VISIBLE

                    val responseBody = response.body
                    if (responseBody.status?.equals("1") == true) {
                        if ((responseBody.data?.size ?: 0) > 0) {
                            binding.rvOrderItemFood.visibility = View.VISIBLE
                            setFoodDetailData(responseBody)
                        } else {
                            binding.rvOrderItemFood.visibility = View.GONE
                        }
                    } else if (responseBody.status.equals("0")) {
                        Common.dismissLoadingProgress()
                        binding.rvOrderItemFood.visibility = View.GONE
                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderDetailActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderDetailActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderDetailActivity,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setFoodDetailData(response: OrderDetailResponse) {
        if ((response.data?.size ?: 0) > 0) {
            response.data?.let { setFoodCategoryAdaptor(it) }
        }

        if (response.summery?.status?.toInt()!! > 1) {
            binding.tvProceedToPaymnet.visibility = View.GONE
        } else {
            binding.tvProceedToPaymnet.visibility = View.VISIBLE
        }


        if (response.summery.orderType?.equals("2") == true) {
            binding.cvDeliveryAddress.visibility = View.GONE
            binding.cvDriverInformation.visibility = View.GONE
            if (response.summery.offerCode.toString() == "") {
                binding.rlDiscount.visibility = View.GONE
                if (response.summery.orderNotes == "") {
                    binding.tvNotes.text = ""
                    binding.cvOrderNote.visibility = View.GONE
                } else {
                    binding.cvOrderNote.visibility = View.VISIBLE
                    binding.tvNotes.text = response.summery.orderNotes
                }


                binding.tvOrderAddress.text = response.summery.address
                    .plus(", ${response.summery.houseNo}")
                    .plus(", ${response.summery.area}")

                binding.tvOrderTotalPrice.text = Common.getPrices(
                    currencyPosition, currency,
                    response.summery.orderTotal ?: "0.00"
                )


                binding.tvOrderTaxPrice.text = Common.getPrices(
                    currencyPosition, currency,
                    response.summery.tax ?: "0.00"
                )

                binding.tvOrderDeliveryCharge.text =
                    Common.getPrices(currencyPosition, currency, "0.00")


                binding.tvTitleTex.text = "Tax"
                binding.tvOrderTaxPrice.text =
                    Common.getPrices(currencyPosition, currency, response.summery.tax ?: "0.00")

                binding.tvOrderTotalCharge.text = Common.getPrices(currencyPosition, currency, response.summery.grandTotal ?: "0.00")
            } else {
                binding.rlDiscount.visibility = View.VISIBLE
                if (response.summery.orderNotes == "") {
                    binding.tvNotes.text = ""
                    binding.cvOrderNote.visibility = View.GONE
                } else {
                    binding.cvOrderNote.visibility = View.VISIBLE
                    binding.tvNotes.text = response.summery.orderNotes
                }

                binding.tvOrderAddress.text = response.summery.address
                    .plus(", ${response.summery.houseNo}")
                    .plus(", ${response.summery.area}")



                binding.tvOrderTotalPrice.text = Common.getPrices(
                    currencyPosition,
                    currency,
                    response.summery.orderTotal ?: "0.00"
                )

                binding.tvOrderTaxPrice.text =
                    Common.getPrices(currencyPosition, currency, response.summery.tax ?: "0.00")

                binding.tvOrderDeliveryCharge.text =
                    Common.getPrices(currencyPosition, currency, "0.00")

                binding.tvTitleTex.text = "Tax"
                binding.tvOrderTaxPrice.text =
                    Common.getPrices(currencyPosition, currency, response.summery.tax ?: "0.00")

                binding.tvDiscountOffer.text = Common.getPrices(
                    currencyPosition,
                    currency,
                    response.summery.discountAmount ?: "0.00"
                )


                binding.tvPromoCodeApply.text = response.summery.offerCode



                binding.tvOrderTotalCharge.text =
                    Common.getPrices(currencyPosition, currency, response.summery.grandTotal ?: "0.00")

            }
        } else {
            binding.cvDeliveryAddress.visibility = View.VISIBLE

            if (response.summery.status == "4" || response.summery.status == "5") {

                if(response.driverInfo?.id?.isNotEmpty() == true) {


                    binding.cvDriverInformation.visibility = View.VISIBLE
                    binding.llCall.setOnClickListener {
                        val call: Uri = Uri.parse("tel:${response.driverInfo.mobile}")
                        val surf = Intent(Intent.ACTION_DIAL, call)
                        startActivity(surf)
                    }
                    binding.tvUserName.text = response.driverInfo.name
                    binding.tvDriverEmail.text = response.driverInfo.email.toString()
                    Glide.with(this@OrderDetailActivity)
                        .load(response.driverInfo.profile_image)
                        .apply(RequestOptions.circleCropTransform())
                        .transition(DrawableTransitionOptions.withCrossFade(500)).centerCrop()
                        .into(binding.ivUserDetail)
                }else
                {
                    binding.cvDriverInformation.visibility = View.GONE
                }
            } else {
                binding.cvDriverInformation.visibility = View.GONE
            }
            binding.tvOrderAddress.text = response.summery.address
                .plus(", ${response.summery.houseNo}")
                .plus(", ${response.summery.area}")
            if (response.summery.offerCode == null) {
                binding.rlDiscount.visibility = View.GONE
                if (response.summery.orderNotes == "") {
                    binding.tvNotes.text = ""
                    binding.cvOrderNote.visibility = View.GONE
                } else {
                    binding.cvOrderNote.visibility = View.VISIBLE
                    binding.tvNotes.text = response.summery.orderNotes
                }


                binding.tvOrderTotalPrice.text = Common.getPrices(
                    currencyPosition,
                    currency,
                    response.summery.orderTotal ?: "0.00"
                )

                binding.tvOrderTaxPrice.text =
                    Common.getPrices(currencyPosition, currency, response.summery.tax ?: "0.00")

                binding.tvOrderDeliveryCharge.text = Common.getPrices(currencyPosition, currency, response.summery.deliveryCharge ?: "0.00")


                binding.tvTitleTex.text = "Tax"


                binding.tvOrderTotalCharge.text =
                    Common.getPrices(currencyPosition, currency,response.summery.grandTotal.toString()?:"0.00")

            } else {
                binding.rlDiscount.visibility = View.VISIBLE
                if (response.summery.orderNotes == "") {
                    binding.tvNotes.text = ""
                    binding.cvOrderNote.visibility = View.GONE
                } else {
                    binding.cvOrderNote.visibility = View.VISIBLE
                    binding.tvNotes.text = response.summery.orderNotes
                }
                binding.tvOrderAddress.text = response.summery.address.toString()
                    .plus(", ${response.summery.houseNo.toString()}")
                    .plus(", ${response.summery.area.toString()}")

                binding.tvOrderTotalPrice.text = Common.getPrices(
                    currencyPosition,
                    currency,
                    response.summery.orderTotal ?: "0.00"
                )

                binding.tvOrderTaxPrice.text =
                    Common.getPrices(currencyPosition, currency, response.summery.tax ?: "0.00")

                binding.tvOrderDeliveryCharge.text = Common.getPrices(
                    currencyPosition,
                    currency,
                    response.summery.deliveryCharge ?: "0.00"
                )

                binding.tvTitleTex.text = "Tax"


                binding.tvDiscountOffer.text = Common.getPrices(
                    currencyPosition,
                    currency,
                    response.summery.discountAmount ?: "0.00"
                )



                binding.tvPromoCodeApply.text = response.summery.offerCode


                binding.tvOrderTotalCharge.text = Common.getPrices(currencyPosition, currency, response.summery.grandTotal ?: "0.00")

            }
        }


        orderInfo(response)

    }

    private fun setFoodCategoryAdaptor(orderHistoryList: ArrayList<OrderDetailModel>) {
        val orderHistoryAdapter =
            object : BaseAdaptor<OrderDetailModel>(this@OrderDetailActivity, orderHistoryList) {
                @SuppressLint("SetTextI18n", "NewApi")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: OrderDetailModel,
                    position: Int
                ) {


                    val tvOrderFoodName: AppCompatTextView =
                        holder!!.itemView.findViewById(R.id.tvFoodName)
                    val tvPrice: AppCompatTextView = holder.itemView.findViewById(R.id.tvPrice)
                    val tvVariation: AppCompatTextView = holder.itemView.findViewById(R.id.tvVariation)
                    val tvAddOns: AppCompatTextView = holder.itemView.findViewById(R.id.tvAddons)
                    val tvQtyNumber: AppCompatTextView = holder.itemView.findViewById(R.id.tvQty)
                    val ivVeg: AppCompatImageView = holder.itemView.findViewById(R.id.ivVeg)
                    val ivFoodCart: AppCompatImageView = holder.itemView.findViewById(R.id.ivFoodCart)


                    if (`val`.itemType == 2) {
                        ivVeg.setImageResource(R.drawable.ic_nonveg)
                    } else {
                        ivVeg.setImageResource(R.drawable.ic_vegetarian)
                    }

                    if(orderHistoryList[position].variation?.isEmpty() == true ||orderHistoryList[position].variation=="null")
                    {
                        tvVariation.text="-"
                    }else
                    {
                        tvVariation.text = orderHistoryList[position].variation

                    }

                    if(orderHistoryList[position].addonsName?.isEmpty() == true ||orderHistoryList[position].addonsName=="null")
                    {
                        tvAddOns.text="-"
                        tvAddOns.isEnabled=false
                        tvAddOns.isClickable=false
                    }else
                    {
                        tvAddOns.text="Add-ons>>"
                        tvAddOns.isEnabled=true
                        tvAddOns.isClickable=true
                    }

                    tvAddOns.setOnClickListener {
                        val fragAddOns=FragAddons()
                        val bundle=Bundle()
                        bundle.putString("addOnsName",orderHistoryList[position].addonsName)
                        bundle.putString("addOnsPrice",orderHistoryList[position].addonsPrice)
                        fragAddOns.arguments=bundle
                        fragAddOns.show(supportFragmentManager,FragAddons::class.java.name)

                    }


//                    tvAddOns.text = orderHistoryList[position].addonsName
                    tvOrderFoodName.text = orderHistoryList[position].itemName
                    val itemPrice=orderHistoryList[position].itemPrice?.toDouble()?.plus(orderHistoryList[position].addons_total_price?.toDouble()?:0.00)

                    tvPrice.text = Common.getPrices(currencyPosition, currency, itemPrice.toString() ?: "0.00")

                    Glide.with(this@OrderDetailActivity).load(orderHistoryList[position].itemImage)
                        .transition(DrawableTransitionOptions.withCrossFade(500))
                        .into(ivFoodCart)
                    tvQtyNumber.text = resources.getString(R.string.qty) + " ${orderHistoryList[position].qty}"





                }

                override fun setItemLayout(): Int {
                    return R.layout.cell_order_summery_item
                }
            }
        binding.rvOrderItemFood.adapter = orderHistoryAdapter
        binding.rvOrderItemFood.layoutManager = LinearLayoutManager(this@OrderDetailActivity)
        binding.rvOrderItemFood.itemAnimator = DefaultItemAnimator()
        binding.rvOrderItemFood.isNestedScrollingEnabled = true
    }

    @SuppressLint("InflateParams")
    fun mCancelOrderDialog(strOrderId: String) {
        var dialog: Dialog? = null
        try {
            dialog?.dismiss()
            dialog = Dialog(this@OrderDetailActivity, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            val mInflater = LayoutInflater.from(this@OrderDetailActivity)
            val mView = DlgConfomationBinding.inflate(layoutInflater)
            mView.tvDesc.text = resources.getString(R.string.wallet_description)
            val finalDialog: Dialog = dialog
            mView.tvYes.setOnClickListener {
                finalDialog.dismiss()
                val map = HashMap<String, String>()
                map["order_id"] = strOrderId
                if (Common.isCheckNetwork(this@OrderDetailActivity)) {
                    callApiOrder(map)
                } else {
                    alertErrorOrValidationDialog(
                        this@OrderDetailActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
            }
            mView.tvNo.setOnClickListener {
                finalDialog.dismiss()
            }
            dialog.setContentView(mView.root)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }


    private fun callApiOrder(map: HashMap<String, String>) {
        showLoadingProgress(this@OrderDetailActivity)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setOrderCancel(map)) {
                is NetworkResponse.Success -> {

                    Common.dismissLoadingProgress()

                    val restResponse: SingleResponsee = response.body
                    if (restResponse.status == 1) {
                        Common.isCancelledOrder = true
                        finish()
                    } else if (restResponse.status == 0) {
                        alertErrorOrValidationDialog(
                            this@OrderDetailActivity,
                            restResponse.message
                        )
                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderDetailActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderDetailActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OrderDetailActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }



}