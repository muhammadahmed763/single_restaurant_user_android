package com.singlerestaurant.user.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singlerestaurant.user.R
import com.singlerestaurant.user.activity.ActFoodDetails
import com.singlerestaurant.user.activity.DashboardActivity
import com.singlerestaurant.user.activity.LoginActivity
import com.singlerestaurant.user.adaptor.SubCategoryItemsAdapter
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.base.BaseFragment
import com.singlerestaurant.user.databinding.FragDynamicBinding
import com.singlerestaurant.user.model.SubcategoryItemsItem
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.*


class DynamicFragment : BaseFragment<FragDynamicBinding>() {

    private var catename = ""
    private lateinit var subCategoryAdapter: SubCategoryItemsAdapter
    private var productList = ArrayList<SubcategoryItemsItem>()
    private var manager1: GridLayoutManager? = null


    private lateinit var binding:FragDynamicBinding

    override fun Init(view: View) {



        productList = arguments?.getParcelableArrayList<SubcategoryItemsItem>("foodList") as ArrayList<SubcategoryItemsItem>

        if(productList.size==0)
        {
            binding.tvNoDataFound.visibility =View.VISIBLE
            binding.rvFoodData.visibility =View.GONE
        }else
        {
            binding.tvNoDataFound.visibility =View.GONE
            binding.rvFoodData.visibility =View.VISIBLE
        }

        subCategoryProductListSetup()
    }

    companion object {
        fun newInstance(
            subCat_id: String,
            cat_id: String,
            foodList: ArrayList<SubcategoryItemsItem>
        ): DynamicFragment {
            val dynamicFragment = DynamicFragment()
            val bundle = Bundle()
            bundle.putString("Id", subCat_id)
            bundle.putString("cat_id", cat_id)
            bundle.putParcelableArrayList("foodList", foodList)
            dynamicFragment.arguments = bundle
            return dynamicFragment
        }
    }


    private fun subCategoryProductListSetup() {
        manager1 = GridLayoutManager(requireActivity(), 2, GridLayoutManager.VERTICAL, false)
        binding.rvFoodData.layoutManager = manager1
        subCategoryAdapter =
            SubCategoryItemsAdapter(requireActivity(), productList) { i: Int, s: String ->
                if (s == Constants.ItemClick) {
                    val intent = Intent(requireActivity(), ActFoodDetails::class.java)
                    intent.putExtra("foodItemId", productList[i].id.toString())
                    startActivity(intent)

                } else if (s == Constants.SelectVariationClick) {
                    if (SharePreference.getBooleanPref(requireActivity(), SharePreference.isLogin))
                    {
                        featuredItemAddToCartFlow(i)
                    }else
                    {
                        openActivity(LoginActivity::class.java)
                        requireActivity().finish()
                        requireActivity().finishAffinity()
                    }


                } else if (s == Constants.FavClick) {
                    if (SharePreference.getBooleanPref(
                            requireActivity(),
                            SharePreference.isLogin
                        )
                    ) {
                        if (productList[i].isFavorite == "1") {
                            callFavouritesApi(
                                "unfavorite",
                                productList[i].id.toString(),
                                i
                            )
                        } else {
                            callFavouritesApi(
                                "favorite",
                                productList[i].id.toString(),
                                i
                            )
                        }
                    } else {
                        openActivity(LoginActivity::class.java)
                        activity?.finish()
                    }
                } else if (s == Constants.AddToCart) {
                    if (SharePreference.getBooleanPref(requireActivity(), SharePreference.isLogin))
                    {
                        featuredItemAddToCartFlow(i)
                    }else
                    {
                        openActivity(LoginActivity::class.java)
                        requireActivity().finish()
                        requireActivity().finishAffinity()
                    }
                } else if (s == Constants.DeleteItem) {
                    if (SharePreference.getBooleanPref(requireActivity(), SharePreference.isLogin))
                    {
                        dlgDeleteCartAlert()
                    }else
                    {
                        openActivity(LoginActivity::class.java)
                        requireActivity().finish()
                        requireActivity().finishAffinity()
                    }
                }
            }
        binding.rvFoodData.adapter = subCategoryAdapter
        binding.rvFoodData.isNestedScrollingEnabled = true
    }

    private fun dlgDeleteCartAlert() {
        var dialog: Dialog? = null
        try {
            dialog?.dismiss()
            dialog = Dialog(requireActivity(), R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
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



    private fun featuredItemAddToCartFlow(i: Int) {
        if (productList[i].hasVariation != "1" && productList[i].addons?.size == 0 || productList[i].variation?.size == 0 && productList[i].addons?.size == 0) {
            callAddToCartApi(
                productList[i].tax.toString(),
                productList[i].id.toString(),
                productList[i].itemType.toString(),
                productList[i].itemName.toString(),
                productList[i].imageName.toString(),
                productList[i].price.toString(),
                i,
                Constants.Featured
            )
        } else {
            val fragVariation = FragVariation()
            val bundle = Bundle()
            bundle.putParcelableArrayList("variation", productList[i].variation)
            bundle.putParcelableArrayList("addons", productList[i].addons)
            bundle.putString("name", productList[i].itemName)
            bundle.putString("id", productList[i].id.toString())
            bundle.putString("itemImage", productList[i].imageName.toString())
            bundle.putString("itemType", productList[i].itemType.toString())
            bundle.putString("itemPrice", productList[i].price.toString())
            bundle.putString("pos",i.toString())

            bundle.putString("tax", productList[i].tax.toString())
            bundle.putString("from",Constants.SubCategory)

            fragVariation.arguments = bundle
            fragVariation.show(
                requireActivity().supportFragmentManager,
                fragVariation.tag
            )
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun callAddToCartApi(
        tax: String,
        itemId: String,
        itemType: String,
        itemName: String,
        itemImage: String,
        price: String,
        position: Int,
        type: String
    ) {
        Common.showLoadingProgress(requireActivity())
        val map = HashMap<String, String>()
        map["user_id"] = SharePreference.getStringPref(requireActivity(), SharePreference.userId) ?: ""
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
                    Common.dismissLoadingProgress()
                    productList[position].isCart="1"
                    productList[position].itemQty = productList[position].itemQty?.plus(1)
                    SharePreference.setStringPref(requireActivity(),SharePreference.BadgesCount,response.body.cartCount.toString())

                    subCategoryAdapter.notifyDataSetChanged()

                }

                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        requireActivity(),
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        requireActivity(),
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        requireActivity(),
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun callFavouritesApi(
        favType: String,
        serviceId: String,
        position: Int
    ) {
        Common.showLoadingProgress(requireActivity())
        val hashMap = HashMap<String, String>()
        hashMap["user_id"] =
            SharePreference.getStringPref(requireActivity(), SharePreference.userId) ?: ""
        hashMap["type"] = favType
        hashMap["item_id"] = serviceId
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().manageFavoritesApi(hashMap)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    requireActivity().runOnUiThread {
                        if (response.body.status == 1) {
                            if (favType == "favorite") {

                                productList[position].isFavorite = "1"
                                subCategoryAdapter.notifyDataSetChanged()
                            } else {
                                productList[position].isFavorite = "0"
                                subCategoryAdapter.notifyDataSetChanged()
                            }


                        }
                    }
                }

                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        requireActivity(),
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        requireActivity(),
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        requireActivity(),
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }



    private val refreshData: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, data: Intent?) {
            try {

                val position= data?.getStringExtra("pos")!!.toInt()
                Log.e("position",position.toString())
                productList[position].isCart = "1"
                productList[position].itemQty = productList[position].itemQty?.plus(1)
                subCategoryAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Message", e.message.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(refreshData, IntentFilter("refreshData"))
        Common.getCurrentLanguage(requireActivity(), false)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(refreshData)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(refreshData)
    }

    override fun getBinding(): FragDynamicBinding {
        binding= FragDynamicBinding.inflate(layoutInflater)
        return binding
    }


}