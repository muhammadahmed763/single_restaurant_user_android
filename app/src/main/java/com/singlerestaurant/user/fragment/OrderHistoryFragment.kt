package com.singlerestaurant.user.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singlerestaurant.user.R
import com.singlerestaurant.user.activity.OrderDetailActivity
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.base.BaseAdaptor
import com.singlerestaurant.user.base.BaseFragment
import com.singlerestaurant.user.databinding.FragmentOrderhistoryBinding
import com.singlerestaurant.user.model.OrderHistoryModel
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.alertErrorOrValidationDialog
import com.singlerestaurant.user.utils.Common.dismissLoadingProgress
import com.singlerestaurant.user.utils.Common.getDate
import com.singlerestaurant.user.utils.Common.isCheckNetwork
import com.singlerestaurant.user.utils.Common.showLoadingProgress
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.SharePreference.Companion.getStringPref
import com.singlerestaurant.user.utils.SharePreference.Companion.userId
import kotlinx.coroutines.launch


class OrderHistoryFragment : BaseFragment<FragmentOrderhistoryBinding>() {
    private var currentType = "1"
    var currency=""
    var currencyPos=""
    private lateinit var binding:FragmentOrderhistoryBinding

    override fun Init(view: View) {
        Common.getCurrentLanguage(requireActivity(), false)
        if (isCheckNetwork(requireActivity())) {
            callApiOrderHistory(currentType)
        } else {
            alertErrorOrValidationDialog(requireActivity(), resources.getString(R.string.no_internet))
        }
        setupTabIndicator(currentType)
        currency = getStringPref(requireActivity(), SharePreference.isCurrancy).toString()
        currencyPos = getStringPref(requireActivity(), SharePreference.CurrencyPosition).toString()
        initClickListeners()
       binding. swipeRefresh.setOnRefreshListener {
            if (isCheckNetwork(requireActivity())) {
                binding.swipeRefresh.isRefreshing = false

                callApiOrderHistory(currentType)
            } else {
                alertErrorOrValidationDialog(
                    requireActivity(),
                    resources.getString(R.string.no_internet)
                )
            }
        }
    }


    private fun initClickListeners() {
        binding.linearProcessing.setOnClickListener {
            currentType="1"
            setupTabIndicator(currentType)
            callApiOrderHistory(currentType)

        }
        binding.linearCompleted.setOnClickListener {
            currentType="2"
            setupTabIndicator(currentType)
            callApiOrderHistory(currentType)


        }
        binding.linearCancelled.setOnClickListener {
            currentType="3"
            setupTabIndicator(currentType)
            callApiOrderHistory(currentType)

        }
    }


    private fun setupTabIndicator(type: String) {
        when (type) {
            "1" -> {
                binding.view1.visibility = View.VISIBLE
                binding. view2.visibility = View.GONE
                binding.view3.visibility = View.GONE
                binding.view1.backgroundTintList= ColorStateList.valueOf(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.black,
                        null
                    )
                )

            }
            "2" -> {
                binding.view1.visibility = View.GONE
                binding.view2.visibility = View.VISIBLE
                binding.view3.visibility = View.GONE
                binding.view2.backgroundTintList= ColorStateList.valueOf(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.light_green,
                        null
                    )
                )
            }
            "3" -> {
                binding.view1.visibility = View.GONE
                binding.view2.visibility = View.GONE
                binding. view3.visibility = View.VISIBLE
                binding. view3.backgroundTintList= ColorStateList.valueOf(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.red,
                        null
                    )
                )
            }
        }
    }

    private fun callApiOrderHistory(type:String) {
        showLoadingProgress(requireActivity())
        val map = HashMap<String, String>()
        map["user_id"] = getStringPref(requireActivity(), userId)!!
        map["type"]=type
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().getOrderHistory(map)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()

                    val responseBody = response.body

                    if (responseBody.status == 1) {
                        binding.clOrderHistory.visibility=View.VISIBLE
                        if ((responseBody.data?.size ?: 0) > 0) {
                            binding.rvOrderHistory.visibility = View.VISIBLE
                            binding. tvNoDataFound.visibility = View.GONE
                            val foodCategoryList = responseBody.data!!
                            setFoodCategoryAdaptor(foodCategoryList)
                        } else {
                            binding.rvOrderHistory.visibility = View.GONE
                            binding.tvNoDataFound.visibility = View.VISIBLE
                        }
                    } else if (responseBody.status == 0) {
                        dismissLoadingProgress()
                        binding.rvOrderHistory.visibility = View.GONE
                        binding.tvNoDataFound.visibility = View.VISIBLE
                    }
                }
                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        requireActivity(),
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        requireActivity(),
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        requireActivity(),
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }

    private fun setFoodCategoryAdaptor(orderHistoryList: ArrayList<OrderHistoryModel>) {
        val orderHistoryAdapter =
            object : BaseAdaptor<OrderHistoryModel>(requireActivity(), orderHistoryList) {
                @SuppressLint("SetTextI18n", "NewApi", "UseCompatLoadingForDrawables")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: OrderHistoryModel,
                    position: Int
                ) {
                    val tvOrderNumber: TextView = holder!!.itemView.findViewById(R.id.tvOrderNumber)
                    val tvPrice: TextView = holder.itemView.findViewById(R.id.tvPrice)
                    val tvOrderStatus: TextView = holder.itemView.findViewById(R.id.tvOrderStatus)
                    val tvPaymentType: TextView = holder.itemView.findViewById(R.id.tvPaymentType)
                    val tvOrderDate: TextView = holder.itemView.findViewById(R.id.tvOrderDate)
                    val tvOrderType: TextView = holder.itemView.findViewById(R.id.tvOrderType)
//                        val rlOrder: RelativeLayout = holder.itemView.findViewById(R.id.rlOrder)


                    tvOrderNumber.text ="#${orderHistoryList[position].orderNumber.toString()}"
                    tvPrice.text = Common.getPrices(currencyPos,currency,orderHistoryList[position].totalPrice?:"0.00")


                    if (orderHistoryList[position].orderType == "2") {
                        tvOrderType.text = resources.getString(R.string.take_away)
                    } else if (orderHistoryList[position].orderType == "1") {
                        tvOrderType.text = resources.getString(R.string.delivery)
                    }

                    if (orderHistoryList[position].date == null) {
                        tvOrderDate.text = ""
                    } else {
                        tvOrderDate.text = getDate(orderHistoryList[position].date!!)
                    }

                    when {
                        orderHistoryList[position].paymentType!!.toInt() == 1 -> {
                            tvPaymentType.text = resources.getString(R.string.payment_type_)
                                .plus("  ${resources.getString(R.string.cash)}")
                        }
                        orderHistoryList[position].paymentType!!.toInt() == 3 -> {
                            tvPaymentType.text = resources.getString(R.string.payment_type_)
                                .plus("  ${resources.getString(R.string.razorpay)}")
                        }
                        orderHistoryList[position].paymentType!!.toInt() == 4 -> {
                            tvPaymentType.text = resources.getString(R.string.payment_type_)
                                .plus("  ${resources.getString(R.string.stripe)}")
                        }
                        orderHistoryList[position].paymentType!!.toInt() == 5 -> {
                            tvPaymentType.text = resources.getString(R.string.payment_type_)
                                .plus("  ${resources.getString(R.string.flutter_wave)}")
                        }
                        orderHistoryList[position].paymentType!!.toInt() == 6 -> {
                            tvPaymentType.text = resources.getString(R.string.payment_type_)
                                .plus("  ${resources.getString(R.string.paystack)}")
                        }
                        else -> {
                            tvPaymentType.text = resources.getString(R.string.payment_type_)
                                .plus("  ${resources.getString(R.string.wallet)}")
                        }
                    }

                    if (orderHistoryList[position].status == "7") {
                        tvOrderStatus.text = resources.getString(R.string.cancelled)
                        tvOrderStatus.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.status1, null))
                    } else if (orderHistoryList[position].status == "6") {
                        tvOrderStatus.text = resources.getString(R.string.cancelled)
                        tvOrderStatus.backgroundTintList = ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.status1, null))
                    } else {
                        if (orderHistoryList[position].orderType == "1") {
                            when (orderHistoryList[position].status) {
                                "1" -> {

                                    tvOrderStatus.text = resources.getString(R.string.order_place)
                                    tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.status1,
                                            null
                                        )
                                    )

                                }
                                "2" -> {

                                    tvOrderStatus.text = resources.getString(R.string.accepted)
                                    tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.status2,
                                            null
                                        )
                                    )

                                }
                                "3" -> {

                                    tvOrderStatus.text = resources.getString(R.string.order_ready)
                                    tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.dark_blue,
                                            null
                                        )
                                    )

                                }
                                "4" -> {

                                    tvOrderStatus.text = resources.getString(R.string.on_the_way)
                                    tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.blue,
                                            null
                                        )
                                    )

                                }
                                "5" -> {
                                    tvOrderStatus.text = resources.getString(R.string.order_completed)
                                    tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.light_green,
                                            null
                                        )
                                    )

                                }
                            }
                        } else {
                            when (orderHistoryList[position].status) {
                                "1" -> {

                                    tvOrderStatus.text = resources.getString(R.string.order_place)
                                    tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.status1,
                                            null
                                        )
                                    )

                                }
                                "2" -> {

                                    tvOrderStatus.text = resources.getString(R.string.accepted)
                                    tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.status2,
                                            null
                                        )
                                    )

                                }
                                "3" -> {

                                    tvOrderStatus.text = resources.getString(R.string.order_ready)
                                    tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.dark_blue,
                                            null
                                        )
                                    )

                                }
                                "4" -> {

                                    tvOrderStatus.text = resources.getString(R.string.waiting_for_pickup)
                                    tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.blue,
                                            null
                                        )
                                    )

                                }
                                "5" -> {

                                    tvOrderStatus.text = resources.getString(R.string.completed)
                                    tvOrderStatus.backgroundTintList = ColorStateList.valueOf(
                                        ResourcesCompat.getColor(
                                            resources,
                                            R.color.status4,
                                            null
                                        )
                                    )

                                }
                            }
                        }
                    }



                    holder.itemView.setOnClickListener {
                        startActivity(
                            Intent(
                                requireActivity(),
                                OrderDetailActivity::class.java
                            ).putExtra("order_id", orderHistoryList[position].id.toString())
                                .putExtra(
                                    "order_status",
                                    orderHistoryList[position].status.toString()
                                ).putExtra(
                                    "paymentType",
                                    orderHistoryList[position].paymentType.toString()
                                ).putExtra("orderDate", orderHistoryList[position].date.toString()).putExtra("fromHistory",true)
                        )
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.cell_order_history
                }

            }
        binding.rvOrderHistory.adapter = orderHistoryAdapter
        binding.rvOrderHistory.layoutManager = LinearLayoutManager(requireActivity())
        binding. rvOrderHistory.itemAnimator = DefaultItemAnimator()
        binding.rvOrderHistory.isNestedScrollingEnabled = true
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(requireActivity(), false)
        if (Common.isCancelledOrder) {
            if (isCheckNetwork(requireActivity())) {
                Common.isCancelledOrder = false
                callApiOrderHistory(currentType)
            } else {
                alertErrorOrValidationDialog(
                    requireActivity(),
                    resources.getString(R.string.no_internet)
                )
            }
        }
    }

    override fun getBinding(): FragmentOrderhistoryBinding {
        binding= FragmentOrderhistoryBinding.inflate(layoutInflater)
        return binding
    }
}