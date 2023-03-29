package com.singlerestaurant.user.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.activity.ActFoodDetails
import com.singlerestaurant.user.activity.DashboardActivity
import com.singlerestaurant.user.activity.LoginActivity
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.api.SingleResponsee
import com.singlerestaurant.user.base.BaseAdaptor
import com.singlerestaurant.user.base.BaseFragment
import com.singlerestaurant.user.databinding.FragmentFavouriteBinding
import com.singlerestaurant.user.model.FevData
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.alertErrorOrValidationDialog
import com.singlerestaurant.user.utils.Common.dismissLoadingProgress
import com.singlerestaurant.user.utils.Common.isCheckNetwork
import com.singlerestaurant.user.utils.Common.showLoadingProgress
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import com.google.gson.Gson

import kotlinx.coroutines.launch
import java.util.*

class FavouriteFragment : BaseFragment<FragmentFavouriteBinding>() {

    var foodAdapter: BaseAdaptor<FevData>? = null
    var foodList = ArrayList<FevData>()
    var manager1: GridLayoutManager? = null
    var rvFavourite: RecyclerView? = null
    var tvNoDataFound: TextView? = null
    var currency = ""
    var currencyPosition = ""
    private lateinit var binding: FragmentFavouriteBinding


    override fun Init(view: View) {
        Common.getCurrentLanguage(requireActivity(), false)
        rvFavourite = view.findViewById(R.id.rvFavourite)
        tvNoDataFound = view.findViewById(R.id.tvNoDataFound)
        manager1 = GridLayoutManager(activity, 1, GridLayoutManager.VERTICAL, false)
        rvFavourite?.layoutManager = manager1

        currency = SharePreference.getStringPref(
            requireActivity(),
            SharePreference.isCurrancy
        ).toString()
        currencyPosition = SharePreference.getStringPref(
            requireActivity(),
            SharePreference.CurrencyPosition
        ).toString()
        if (isCheckNetwork(requireActivity())) {
            callApiFavouriteFood()
        } else {
            alertErrorOrValidationDialog(
                requireActivity(),
                resources.getString(R.string.no_internet)
            )
        }


    }


    @SuppressLint("NotifyDataSetChanged")
    private fun callApiFavouriteFood() {
        showLoadingProgress(requireActivity())
        val map = HashMap<String, String>()
        map["user_id"] =
            SharePreference.getStringPref(requireActivity(), SharePreference.userId) ?: ""
        Log.d("request", Gson().toJson(map))
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().getFavouriteList(map)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()
                    if (response.body.status == 1) {
                        binding.rlFavourites.visibility = View.VISIBLE
                        if ((response.body.data?.size ?: 0) > 0) {
                            binding.rvFavourite.visibility = View.VISIBLE
                            binding.tvNoDataFound.visibility = View.GONE
                            response.body.data?.let { foodList.addAll(it) }
                            setFoodAdaptor()


                        } else {
                            binding.rvFavourite.visibility = View.GONE
                            binding.tvNoDataFound.visibility = View.VISIBLE
                        }
                    } else if (response.body.status == 0) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            response.body.message
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


    private fun setFoodAdaptor() {

        foodAdapter = object : BaseAdaptor<FevData>(requireActivity(), foodList) {
            @SuppressLint("NewApi")
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: FevData,
                position: Int
            ) {
                val ivFood: ImageView = holder!!.itemView.findViewById(R.id.ivFoodCart)
                val icLike: ImageView = holder.itemView.findViewById(R.id.ivwishlist)
                val ivVeg: ImageView = holder.itemView.findViewById(R.id.ivveg)
                val tvFavName: AppCompatTextView = holder.itemView.findViewById(R.id.tvFavName)
                val tvType: AppCompatTextView = holder.itemView.findViewById(R.id.tvType)
                val tvItemPrice: AppCompatTextView = holder.itemView.findViewById(R.id.tvItemPrice)
                val btnAddCart: AppCompatTextView = holder.itemView.findViewById(R.id.btnAddCart)
                val tvFoodQty: AppCompatTextView = holder.itemView.findViewById(R.id.tvFoodQty)
                val ivPlus: AppCompatImageView = holder.itemView.findViewById(R.id.ivPlus)
                val ivMinus: AppCompatImageView = holder.itemView.findViewById(R.id.ivMinus)
                val clQtyUpdate: ConstraintLayout = holder.itemView.findViewById(R.id.clQtyUpdate)

                tvType.text = `val`.categoryInfo?.categoryName


                if (`val`.isCart == 1) {
                    clQtyUpdate.visibility = View.VISIBLE
                    btnAddCart.visibility = View.GONE
                    tvFoodQty.text = `val`.itemQty.toString()

                } else if (`val`.isCart == 0) {
                    clQtyUpdate.visibility = View.GONE
                    btnAddCart.visibility = View.VISIBLE
                }


                if (`val`.itemType == 2) {
                    ivVeg.setImageResource(R.drawable.ic_nonveg)
                } else {
                    ivVeg.setImageResource(R.drawable.ic_vegetarian)
                }


                btnAddCart.setOnClickListener {
                    if (SharePreference.getBooleanPref(
                            requireActivity(),
                            SharePreference.isLogin
                        )
                    ) {
                        fevFoodAddToCart(position)

                    } else {
                        openActivity(LoginActivity::class.java)
                        requireActivity().finish()
                        requireActivity().finishAffinity()
                    }

                }

                ivPlus.setOnClickListener {
                    if (SharePreference.getBooleanPref(
                            requireActivity(),
                            SharePreference.isLogin
                        )
                    ) {
                        fevFoodAddToCart(position)

                    } else {
                        openActivity(LoginActivity::class.java)
                        requireActivity().finish()
                        requireActivity().finishAffinity()
                    }
                }

                ivMinus.setOnClickListener {
                    if (SharePreference.getBooleanPref(
                            requireActivity(),
                            SharePreference.isLogin
                        )
                    ) {
                        dlgDeleteCartAlert()

                    } else {
                        openActivity(LoginActivity::class.java)
                        requireActivity().finish()
                        requireActivity().finishAffinity()
                    }
                }

                tvFavName.text = foodList[position].itemName
                if (foodList[position].hasVariation == 1) {

                    tvItemPrice.text = Common.getPrices(
                        currencyPosition,
                        currency,
                        foodList[position].variation?.get(0)?.productPrice ?: "0.00"
                    )


                } else {
                    tvItemPrice.text = Common.getPrices(
                        currencyPosition,
                        currency,
                        foodList[position].price.toString() ?: "0.00"
                    )


                }

                Glide.with(requireActivity()).load(foodList[position].imageUrl).transition(DrawableTransitionOptions.withCrossFade(500)).into(ivFood)
                holder.itemView.setOnClickListener {
                    startActivity(
                        Intent(
                            activity!!,
                            ActFoodDetails::class.java
                        ).putExtra("foodItemId", foodList[position].id.toString())
                    )
                }
                tvItemPrice.visibility = View.VISIBLE

                icLike.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.ic_fav,
                        null
                    )
                )
                icLike.imageTintList = ColorStateList.valueOf(Color.WHITE)

                icLike.setOnClickListener {
                    val map = HashMap<String, String>()
                    map["favorite_id"] = foodList[position].favoriteId.toString()
                    map["user_id"] =
                        SharePreference.getStringPref(requireActivity(), SharePreference.userId)!!
                    if (isCheckNetwork(requireActivity())) {
                        callApiFavourite(map, position)
                    } else {
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            resources.getString(R.string.no_internet)
                        )
                    }

                }
            }

            override fun setItemLayout(): Int {
                return R.layout.row_favoritelist
            }

        }
        if (isAdded) {
            rvFavourite!!.adapter = foodAdapter
            rvFavourite!!.itemAnimator = DefaultItemAnimator()
            rvFavourite!!.isNestedScrollingEnabled = true
        }

    }

    private fun dlgDeleteCartAlert() {
        var dialog: Dialog? = null
        try {
            dialog?.dismiss()
            dialog = Dialog(requireActivity(), R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            );
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(true)
            val mInflater = LayoutInflater.from(requireActivity())
            val mView = mInflater.inflate(R.layout.dlg_delete_cart, null, false)
            val textDesc: TextView = mView.findViewById(R.id.tvDesc)
            val tvOk: TextView = mView.findViewById(R.id.tvYes)
            val tvCancel: TextView = mView.findViewById(R.id.tvNo)
            tvOk.text = resources.getString(R.string.go_to_cart)
            tvCancel.text = resources.getString(R.string.cancel)
            textDesc.text = resources.getString(R.string.delete_cart_msg)
            val finalDialog: Dialog = dialog
            tvOk.setOnClickListener {
                finalDialog.dismiss()

                (requireActivity() as DashboardActivity).replaceFragment(CartFragment())
            }
            tvCancel.setOnClickListener {
                finalDialog.dismiss()
            }
            dialog.setContentView(mView)

            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun fevFoodAddToCart(i: Int) {
        if (foodList[i].hasVariation != 1 && foodList[i].addons?.size == 0 || foodList[i].variation?.size == 0 && foodList[i].addons?.size == 0) {
            callAddToCartApi(
                foodList[i].tax.toString(),
                foodList[i].id.toString(),
                foodList[i].itemType.toString(),
                foodList[i].itemName.toString(),
                foodList[i].imageName.toString(),
                foodList[i].price.toString(),
                i
            )
        } else {
            val fragVariation = FragVariation()
            val bundle = Bundle()
            bundle.putParcelableArrayList("variation", foodList[i].variation)
            bundle.putParcelableArrayList("addons", foodList[i].addons)
            bundle.putString("name", foodList[i].itemName)
            bundle.putString("id", foodList[i].id.toString())
            bundle.putString("pos",i.toString())
            bundle.putString("itemImage", foodList[i].imageName.toString())
            bundle.putString("itemType", foodList[i].itemType.toString())
            bundle.putString("itemPrice", foodList[i].price.toString())
            bundle.putString("tax", foodList[i].tax.toString())
            bundle.putString("from", Constants.Favourites)
            fragVariation.arguments = bundle
            fragVariation.show(
                requireActivity().supportFragmentManager,
                fragVariation.tag
            )
        }
    }


     private val refreshData: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, data: Intent?) {
            try {
                val position= data?.getStringExtra("pos")!!.toInt()
                Log.e("position",position.toString())
                foodList[position].isCart = 1
                foodList[position].itemQty = foodList[position].itemQty?.plus(1)
                foodAdapter?.notifyDataSetChanged()
                (requireActivity() as DashboardActivity).setCartCount()


            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Message", e.message.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(requireActivity(), false)

        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(refreshData, IntentFilter("refreshData"))
    }

    override fun onPause() {
        super.onPause()

        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(refreshData)


    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(refreshData)

    }


    @SuppressLint("NotifyDataSetChanged")
    private fun callAddToCartApi(
        tax: String,
        itemId: String,
        itemType: String,
        itemName: String,
        itemImage: String,
        price: String,
        pos: Int
    ) {
        showLoadingProgress(requireActivity())
        val map = HashMap<String, String>()
        map["user_id"] =
            SharePreference.getStringPref(requireActivity(), SharePreference.userId) ?: ""
        map["item_id"] = itemId
        map["item_image"] = itemImage
        map["item_type"] = itemType
        map["item_name"] = itemName
        map["tax"] = tax
        map["item_price"] = String.format(Locale.US, "%.02f", price.toDouble())
        map["variation_id"] = ""
        map["variation"] = ""
        map["addons_id"] = ""
        map["addons_price"] = "0.00"
        map["addons_name"] = ""
        map["addons_total_price"] = "0.00"
        Log.d("request", Gson().toJson(map))
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().addToCart(map)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()
                    if (response.body.status == 1) {

                        foodList[pos].isCart = 1
                        foodList[pos].itemQty = foodList[pos].itemQty?.plus(1)
                        SharePreference.setStringPref(
                            requireActivity(),
                            SharePreference.BadgesCount,
                            response.body.cartCount.toString()
                        )
                        (requireActivity() as DashboardActivity).setCartCount()

                        foodAdapter?.notifyDataSetChanged()
                    } else {
                        Common.showErrorFullMsg(
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


    private fun callApiFavourite(hashmap: HashMap<String, String>, pos: Int) {
        showLoadingProgress(requireActivity())
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setRemoveFavourite(hashmap)) {
                is NetworkResponse.Success -> {
                    val restResponse: SingleResponsee = response.body
                    if (restResponse.status == 1) {
                        dismissLoadingProgress()
                        foodList.removeAt(pos)
                        foodAdapter?.notifyDataSetChanged()

                        if (foodList.size == 0) {
                            binding.rvFavourite.visibility = View.GONE
                            binding.tvNoDataFound.visibility = View.VISIBLE
                        } else {
                            binding.rvFavourite.visibility = View.VISIBLE
                            binding.tvNoDataFound.visibility = View.GONE
                        }
                    } else if (restResponse.status == 0) {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            requireActivity(),
                            restResponse.message
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




    override fun getBinding(): FragmentFavouriteBinding {
        binding = FragmentFavouriteBinding.inflate(layoutInflater)
        return binding
    }
}