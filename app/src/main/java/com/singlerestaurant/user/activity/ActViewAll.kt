package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.singlerestaurant.user.R
import com.singlerestaurant.user.adaptor.ViewAllDataAdapter
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.ActViewAllBinding
import com.singlerestaurant.user.fragment.FragVariation
import com.singlerestaurant.user.model.ListResponse
import com.singlerestaurant.user.model.SearchItemModel
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.*

class ActViewAll : AppCompatActivity() {

    private var productList = ArrayList<SearchItemModel>()
    private var manager1: LinearLayoutManager? = null
    private var filterType = ""
    private lateinit var searchAdapter: ViewAllDataAdapter

    private lateinit var binding:ActViewAllBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActViewAllBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Common.getCurrentLanguage(this@ActViewAll, false)
        manager1 = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        binding.ivBack.setOnClickListener {
            val intent = Intent(this@ActViewAll, DashboardActivity::class.java).putExtra("pos", "1")
            startActivity(intent)
            finish()
            finishAffinity()
        }
        viewAllDataAdapterSetup()

        callApiSearchFood()


        if (intent.getStringExtra("type")?.isNotEmpty() == true) {
            if (intent.getStringExtra("type") == "1") {
                binding.tvTitle.text = resources.getString(R.string.today_s_special)
            } else if (intent.getStringExtra("type") == "2") {
                binding.tvTitle.text = resources.getString(R.string.trending_items)
            } else if (intent.getStringExtra("type") == "3") {
                binding.tvTitle.text = resources.getString(R.string.recommended)
            }
        }

        if (SharePreference.getStringPref(this@ActViewAll, SharePreference.SELECTED_LANGUAGE)
                .equals(resources.getString(R.string.language_hindi))
        ) {
            binding.ivBack.rotation = 180F
        } else {
            binding.ivBack.rotation = 0F
        }


        binding.ivFilter.setOnClickListener {
            showFilterBottomSheet()
        }



    }

    override fun onBackPressed() {
        val intent = Intent(this@ActViewAll, DashboardActivity::class.java).putExtra("pos", "1")
        startActivity(intent)
        finish()
        finishAffinity()
    }


    private fun showFilterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(R.layout.dlg_filter_bottomsheet)
        val tvVeg = bottomSheetDialog.findViewById<AppCompatTextView>(R.id.tvVeg)
        val tvNonVeg = bottomSheetDialog.findViewById<AppCompatTextView>(R.id.tvNonVeg)
        val tvBoth = bottomSheetDialog.findViewById<AppCompatTextView>(R.id.tvBoth)
        val ivClose = bottomSheetDialog.findViewById<AppCompatImageView>(R.id.ivClose)

        ivClose?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        tvVeg?.setOnClickListener {
            filterType = "1"
            bottomSheetDialog.dismiss()
            callApiSearchFood()
        }
        tvNonVeg?.setOnClickListener {
            filterType = "2"
            bottomSheetDialog.dismiss()

            callApiSearchFood()
        }
        tvBoth?.setOnClickListener {
            filterType = ""
            bottomSheetDialog.dismiss()

            callApiSearchFood()
        }


        bottomSheetDialog.show()
    }


    private fun callApiSearchFood() {
        Common.showLoadingProgress(this@ActViewAll)
        val map = HashMap<String, String>()
        map["user_id"] =
            SharePreference.getStringPref(this@ActViewAll, SharePreference.userId) ?: ""
        map["search"] = intent.getStringExtra("type") ?: ""
        map["filter"] = filterType
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setSearch(map)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    if (response.body.status == 1) {
                        val restResponse: ListResponse<SearchItemModel> = response.body
                        if (restResponse.status == 1) {
                            Common.dismissLoadingProgress()
                            productList.clear()

                            restResponse.data?.let { productList.addAll(it) }
                            if(productList.size>0)
                            {
                                binding.tvNoDataFound.visibility = View.GONE
                                binding.rvViewAllData.visibility = View.VISIBLE
                            }else
                            {
                                binding.tvNoDataFound.visibility = View.VISIBLE
                                binding.rvViewAllData.visibility = View.GONE
                            }

                            searchAdapter?.notifyDataSetChanged()


                        } else {
                            binding.tvNoDataFound.visibility = View.VISIBLE
                            binding.rvViewAllData.visibility = View.GONE
                            Common.dismissLoadingProgress()

                        }

                    } else {
                        Common.showErrorFullMsg(
                            this@ActViewAll,
                            response.body.message.toString()
                                .replace("\\n", System.lineSeparator())
                        )
                    }

                }

                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActViewAll,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActViewAll,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActViewAll,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }


    private fun callFavouritesApi(
        favType: String,
        serviceId: String,
        position: Int
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["user_id"] = SharePreference.getStringPref(this@ActViewAll, SharePreference.userId) ?: ""
        hashMap["type"] = favType
        hashMap["item_id"] = serviceId
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().manageFavoritesApi(hashMap)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    if (response.body.status == 1) {
                        if (favType == "favorite") {
                            productList[position].isFavorite = 1
                        } else {
                            productList[position].isFavorite = 0
                        }
                        searchAdapter?.notifyDataSetChanged()


                    } else {
                        Common.showErrorFullMsg(
                            this@ActViewAll,
                            response.body.message.toString()
                                .replace("\\n", System.lineSeparator())
                        )
                    }

                }

                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActViewAll,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActViewAll,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActViewAll,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }

    private fun viewAllDataAdapterSetup() {
        manager1 = GridLayoutManager(this@ActViewAll, 2, GridLayoutManager.VERTICAL, false)
        binding.rvViewAllData.layoutManager = manager1
        searchAdapter = ViewAllDataAdapter(this@ActViewAll, productList) { i: Int, s: String ->
                if (s == Constants.ItemClick) {
                    val intent = Intent(this@ActViewAll, ActFoodDetails::class.java)
                    intent.putExtra("foodItemId", productList[i].id.toString())
                    startActivity(intent)

                } else if (s == Constants.SelectVariationClick) {
                    if (SharePreference.getBooleanPref(
                            this@ActViewAll,
                            SharePreference.isLogin
                        )
                    ) {
                        featuredItemAddToCartFlow(i)
                    }else
                    {
                        startActivity(Intent(this@ActViewAll,LoginActivity::class.java))
                        finish()
                        finishAffinity()
                    }



                } else if (s == Constants.FavClick) {
                    if (SharePreference.getBooleanPref(this@ActViewAll, SharePreference.isLogin))
                    {
                        if (productList[i].isFavorite == 1) {
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
                        startActivity(Intent(this@ActViewAll,LoginActivity::class.java))
                        finish()
                        finishAffinity()

                    }
                } else if (s == Constants.AddToCart) {
                    if (SharePreference.getBooleanPref(
                            this@ActViewAll,
                            SharePreference.isLogin
                        )
                    ) {
                        featuredItemAddToCartFlow(i)
                    }else
                    {
                        startActivity(Intent(this@ActViewAll,LoginActivity::class.java))
                        finish()
                        finishAffinity()

                    }
                } else if (s == Constants.DeleteItem) {
                    if (SharePreference.getBooleanPref(
                            this@ActViewAll,
                            SharePreference.isLogin
                        )
                    ) {
                        dlgDeleteCartAlert()
                    }else
                    {
                        startActivity(Intent(this@ActViewAll,LoginActivity::class.java))
                        finish()
                        finishAffinity()
                    }


                }
            }
        binding.rvViewAllData.adapter = searchAdapter
        binding.rvViewAllData.isNestedScrollingEnabled = true
    }

    private fun dlgDeleteCartAlert() {
        var dialog: Dialog? = null
        try {
            dialog?.dismiss()
            dialog = Dialog(this@ActViewAll, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(true)
            val mInflater = LayoutInflater.from(this@ActViewAll)
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
                val intent = Intent(this@ActViewAll, DashboardActivity::class.java).putExtra("pos", "3")
                startActivity(intent)
                finish()
                finishAffinity()
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
                i)
        } else {
            val fragVariation = FragVariation()
            val bundle = Bundle()
            bundle.putParcelableArrayList("variation", productList[i].variation)
            bundle.putParcelableArrayList("addons", productList[i].addons)
            bundle.putString("name", productList[i].itemName)
            bundle.putString("id", productList[i].id.toString())
            bundle.putString("pos",i.toString())
            bundle.putString("itemImage", productList[i].imageName.toString())
            bundle.putString("itemPrice", productList[i].price.toString())
            bundle.putString("itemType", productList[i].itemType.toString())
            bundle.putString("tax", productList[i].tax.toString())
            bundle.putString("from", Constants.ViewAll)
            fragVariation.arguments = bundle
            fragVariation.show(supportFragmentManager, fragVariation.tag)
        }
    }


    private val refreshData: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, data: Intent?) {
            try {

                val position= data?.getStringExtra("pos")!!.toInt()
                Log.e("position",position.toString())
                productList[position].isCart = 1
                productList[position].itemQty = productList[position].itemQty?.plus(1)
                searchAdapter.notifyDataSetChanged()


            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Message", e.message.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(refreshData, IntentFilter("refreshData"))
        Common.getCurrentLanguage(this@ActViewAll, false)

    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshData)


    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshData)

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
    ) {
        Common.showLoadingProgress( this@ActViewAll)
        val map = HashMap<String, String>()
        map["user_id"] =
            SharePreference.getStringPref( this@ActViewAll, SharePreference.userId) ?: ""
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
                    productList[position].isCart=1
                    productList[position].itemQty = productList[position].itemQty?.plus(1)
                    SharePreference.setStringPref(this@ActViewAll,SharePreference.BadgesCount,response.body.cartCount.toString())

                    searchAdapter.notifyDataSetChanged()

                }

                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActViewAll,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActViewAll,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActViewAll,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }



}