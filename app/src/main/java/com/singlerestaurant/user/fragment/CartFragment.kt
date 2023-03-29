package com.singlerestaurant.user.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.activity.DashboardActivity
import com.singlerestaurant.user.activity.LoginActivity
import com.singlerestaurant.user.activity.OrderSummeryActivity
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.api.SingleResponsee
import com.singlerestaurant.user.base.BaseAdaptor
import com.singlerestaurant.user.base.BaseFragment
import com.singlerestaurant.user.databinding.FragmentCartBinding
import com.singlerestaurant.user.model.CartItemModel
import com.singlerestaurant.user.model.ListResponse
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.alertErrorOrValidationDialog
import com.singlerestaurant.user.utils.Common.dismissLoadingProgress
import com.singlerestaurant.user.utils.Common.isCheckNetwork
import com.singlerestaurant.user.utils.Common.showLoadingProgress
import com.singlerestaurant.user.utils.SharePreference
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.singlerestaurant.user.model.OrderSummaryModel

import kotlinx.coroutines.launch
import java.util.*


class CartFragment : BaseFragment<FragmentCartBinding>() {
    var cartItemAdapter: BaseAdaptor<OrderSummaryModel>? = null
    var cartItem = ArrayList<OrderSummaryModel>()
    var type = ""
    var deliveryOption = 1
    private lateinit var binding: FragmentCartBinding
    var currencyPos = ""
    var currency = ""

    override fun Init(view: View) {
        Common.getCurrentLanguage(requireActivity(), false)
        if (SharePreference.getBooleanPref(requireActivity(),SharePreference.isLogin)) {
        if (isCheckNetwork(requireActivity())) {
            callApiCart(false)
        } else {
            alertErrorOrValidationDialog(requireActivity(), resources.getString(R.string.no_internet))
        }}else
        {
            startActivity(Intent(requireActivity(), LoginActivity::class.java))
           requireActivity().finish()
            requireActivity().finishAffinity()
        }
        currency =
            SharePreference.getStringPref(requireActivity(), SharePreference.isCurrancy) ?: ""
        currencyPos =
            SharePreference.getStringPref(requireActivity(), SharePreference.CurrencyPosition) ?: ""

        iniClickListener()
    }

    private fun iniClickListener() {
        binding.tvCheckout.setOnClickListener {
            if (isCheckNetwork(requireActivity())) {
                isOpenOrClose()
            } else {
                alertErrorOrValidationDialog(
                    requireActivity(),
                    resources.getString(R.string.no_internet)
                )
            }
        }

    }


    private fun selectDeliveryOptionDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        val view: View = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_select_dilevery_option, null, false)
        val linearDelivery = view.findViewById<LinearLayoutCompat>(R.id.linearDelivery)
        val linearPickup = view.findViewById<LinearLayoutCompat>(R.id.linearTakeAway)
        val btnCancel = view.findViewById<TextView>(R.id.btnCancel)
        val btnContinue = view.findViewById<TextView>(R.id.tvContinue)

        linearPickup.setOnClickListener {
            deliveryOption = 2
            linearPickup.background =
                ResourcesCompat.getDrawable(resources, R.drawable.bg_green_corner_10, null)
            linearDelivery.background = null
        }

        linearDelivery.setOnClickListener {
            deliveryOption = 1
            linearPickup.background = null
            linearDelivery.background =
                ResourcesCompat.getDrawable(resources, R.drawable.bg_green_corner_10, null)
        }

        btnCancel.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        btnContinue.setOnClickListener {
            bottomSheetDialog.dismiss()

            val intent = Intent(requireActivity(), OrderSummeryActivity::class.java)
            intent.putExtra("DeliveryType", deliveryOption.toString())
            startActivity(intent)

        }



        bottomSheetDialog.setContentView(view)

        bottomSheetDialog.show()
    }


    private fun callApiCart(isQty: Boolean) {

        if (!isQty) {
            showLoadingProgress(requireActivity())
        }
        val map = HashMap<String, String>()
        map["user_id"] = SharePreference.getStringPref(
            requireActivity(),
            SharePreference.userId
        )!!
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setSummary(map)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()
                    val restResponse = response.body
                    if (response.body.getStatus() == "1") {
                        binding.mainLayout.visibility = View.VISIBLE
                        if (restResponse.getData()?.size ?: 0 > 0) {
                            binding.tvNoDataFound.visibility = View.GONE
                            binding.tvCheckout.visibility = View.VISIBLE

                            cartItem.clear()
                            cartItem.addAll(restResponse.getData())
                            setFoodCartAdaptor(cartItem)
                        } else {
                            binding.rvCartFoodList.visibility = View.GONE
                            binding.tvCheckout.visibility = View.GONE

                            binding.tvNoDataFound.visibility = View.VISIBLE
                        }
                    } else {
                        binding.rvCartFoodList.visibility = View.GONE
                        binding.tvNoDataFound.visibility = View.VISIBLE
                        binding.tvCheckout.visibility = View.GONE
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


    @SuppressLint("ResourceType", "NewApi")
    private fun setFoodCartAdaptor(cartItemList: ArrayList<OrderSummaryModel>) {
        cartItemAdapter = object : BaseAdaptor<OrderSummaryModel>(requireActivity(), cartItem) {
            @SuppressLint("SetTextI18n")
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: OrderSummaryModel,
                position: Int
            ) {

                val tvFoodName = holder?.itemView?.findViewById<AppCompatTextView>(R.id.tvFoodName)
                val tvType = holder?.itemView?.findViewById<AppCompatTextView>(R.id.tvType)
                val tvItemName = holder?.itemView?.findViewById<AppCompatTextView>(R.id.tvItemName)
                val tvFoodPrice =
                    holder?.itemView?.findViewById<AppCompatTextView>(R.id.tvFoodPrice)
                val tvFoodQty = holder?.itemView?.findViewById<AppCompatTextView>(R.id.tvFoodQty)
                val ivFoodCart = holder?.itemView?.findViewById<AppCompatImageView>(R.id.ivFoodCart)
                val ivveg = holder?.itemView?.findViewById<AppCompatImageView>(R.id.ivveg)
                val ivMinus = holder?.itemView?.findViewById<AppCompatImageView>(R.id.ivMinus)
                val ivPlus = holder?.itemView?.findViewById<AppCompatImageView>(R.id.ivPlus)

                tvFoodName?.text = cartItem[position].itemName


                if (cartItem[position].variation == null || cartItemList[position].variation
                        ?.isEmpty() == true
                ) {
                    tvType?.text = "-"
                } else {
                    tvType?.text = cartItem[position].variation

                }


            /*    if (cartItem[position].addonsName== null || cartItemList[position].addonsName
                        ?.isEmpty() == true
                ) {
                    tvItemName?.visibility = View.GONE
                } else {
                    tvItemName?.visibility = View.VISIBLE

                    tvItemName?.text = cartItem[position].addonsName

                }*/
                val itemPrice=cartItem[position].itemPrice?.toDouble()?.plus(cartItem[position].addons_total_price?.toDouble()?:0.00)


                if((cartItem[position].addonsName?.isEmpty() ?: "") == true || cartItem[position].addonsName == "null"
                )
                {
                    tvItemName?.text="-"
                    tvItemName?.isEnabled=false
                    tvItemName?.isClickable=false
                }else
                {
                    tvItemName?.text="Add-ons>>"
                    tvItemName?.isEnabled=true
                    tvItemName?.isClickable=true
                }


                tvItemName?.setOnClickListener {

                    val fragAddOns=FragAddons()
                    val bundle= Bundle()
                    bundle.putString("addOnsName",cartItem[position].addonsName)
                    bundle.putString("addOnsPrice",cartItem[position].addonsPrice)
                    fragAddOns.arguments=bundle
                    fragAddOns.show(requireActivity().supportFragmentManager,FragAddons::class.java.name)
                }

                tvFoodPrice?.text = Common.getPrices(currencyPos,currency,itemPrice.toString())
                tvFoodQty?.text = cartItem[position].qty
                if (cartItem[position].itemType == 2) {
                    ivveg?.setImageResource(R.drawable.ic_nonveg)
                } else {
                    ivveg?.setImageResource(R.drawable.ic_vegetarian)
                }


                Glide.with(requireActivity()).load(cartItem[position].itemImage)
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .into(ivFoodCart!!)




                ivMinus?.setOnClickListener {
                    if (cartItem[position].qty!!.toInt() > 1) {
                        ivMinus.isClickable = true
                        type = "decreaseValue"
                        Common.getLog("Qty>>", cartItem[position].qty.toString())
                        if (isCheckNetwork(requireActivity())) {
                            callApiCartQTYUpdate(cartItemList[position], type, false)
                        } else {
                            alertErrorOrValidationDialog(
                                requireActivity(),
                                resources.getString(R.string.no_internet)
                            )
                        }
                    } else {
                        callApiCartItemDelete(cartItemList[position].id.toString(), position)
                    }
                }
                ivPlus?.setOnClickListener {
                    if (cartItem[position].qty!!.toInt() < SharePreference.getStringPref(
                            requireActivity(),
                            SharePreference.isMiniMumQty
                        )!!.toInt()
                    ) {
                        type = "increaseValue"
                        if (isCheckNetwork(requireActivity())) {
                            callApiCartQTYUpdate(cartItemList[position], type, true)
                        } else {
                            alertErrorOrValidationDialog(
                                requireActivity(),
                                resources.getString(R.string.no_internet)
                            )
                        }
                    } else {
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            resources.getString(R.string.max_qty)
                        )
                    }
                }
            }

            override fun setItemLayout(): Int {
                return R.layout.row_cartlist
            }

        }
        binding.rvCartFoodList.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvCartFoodList.itemAnimator = DefaultItemAnimator()
        binding.rvCartFoodList.isNestedScrollingEnabled = true
        binding.rvCartFoodList.adapter = cartItemAdapter

    }


    private fun callApiCartQTYUpdate(
        cartModel: OrderSummaryModel,
        type: String,
        isPlus: Boolean

    ) {

        val qty = if (isPlus) {
            cartModel.qty!!.toInt() + 1
        } else {
            cartModel.qty!!.toInt() - 1
        }
        showLoadingProgress(requireActivity())
        val map = HashMap<String, String>()
        map["cart_id"] = cartModel.id.toString()
        map["item_id"] = cartModel.itemId.toString()
        map["qty"] = qty.toString()
        map["type"] = type
        map["user_id"] = SharePreference.getStringPref(requireActivity(), SharePreference.userId)!!
        showLoadingProgress(requireActivity())

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setQtyUpdate(map)) {
                is NetworkResponse.Success -> {
                    val restResponse: SingleResponsee = response.body
                    if (restResponse.status == 1) {
                        callApiCart(true)
                    } else {
                        dismissLoadingProgress()
                    }
                }
                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(requireActivity(), response.body.message.toString().replace("\\n", System.lineSeparator()))
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

    private fun isOpenOrClose() {
        showLoadingProgress(requireActivity())

        lifecycleScope.launch {
            val request= HashMap<String, String>()
            request["user_id"]=
                SharePreference.getStringPref(requireActivity(), SharePreference.userId) ?:""
            when (val response = ApiClient.getClient().isOpenClose(request)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()
                    if (response.body.status == 1) {
                        if(response.body.isEmptyCart=="0")
                        {
                            selectDeliveryOptionDialog()

                        }else
                        {
                            (requireActivity() as DashboardActivity).setFragment(1)

                        }
                    } else if (response.body.status == 0) {
                        alertErrorOrValidationDialog(
                            requireActivity(),
                             resources.getString(R.string.close_restaurant_error)
                        )
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

    private fun callApiCartItemDelete(strCartId: String, pos: Int) {
        showLoadingProgress(requireActivity())
        val map = HashMap<String, String>()
        map["cart_id"] = strCartId
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setDeleteCartItem(map)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()
                    if (response.body.status == 1) {
                        Common.isCartTrue = true
                        Common.isCartTrueOut = true
                        cartItem.removeAt(pos)
                        cartItemAdapter?.notifyDataSetChanged()
                        SharePreference.setStringPref(requireActivity(),SharePreference.BadgesCount,response.body.cartCount.toString())
                        (requireActivity() as DashboardActivity).setCartCount()
                        if (cartItem.size > 0) {
                            binding.rvCartFoodList.visibility = View.VISIBLE
                            binding.tvNoDataFound.visibility = View.GONE
                            binding.tvCheckout.visibility = View.VISIBLE

                        } else {
                            binding.rvCartFoodList.visibility = View.GONE
                            binding.tvNoDataFound.visibility = View.VISIBLE
                            binding.tvCheckout.visibility = View.GONE

                        }
                    } else if (response.body.status == 0) {
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            response.body.message.toString()
                        )
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

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(requireActivity(), false)
    }

    override fun getBinding(): FragmentCartBinding {
        binding = FragmentCartBinding.inflate(layoutInflater)
        return binding
    }

}