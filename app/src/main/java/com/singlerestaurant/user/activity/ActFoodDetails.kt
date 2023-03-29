package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.adaptor.AddOnsListAdapter
import com.singlerestaurant.user.adaptor.RelatedItemAdapter
import com.singlerestaurant.user.adaptor.VariationListAdapter
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActFoodDetailsBinding
import com.singlerestaurant.user.fragment.CartFragment
import com.singlerestaurant.user.fragment.FragVariation
import com.singlerestaurant.user.model.*
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.dismissLoadingProgress
import com.singlerestaurant.user.utils.Common.showLoadingProgress
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ActFoodDetails : BaseActivity() {
    private var itemModel: FoodItemData? = null
    private lateinit var relatedItemAdapter: RelatedItemAdapter
    private lateinit var variationAdapter: VariationListAdapter
    private lateinit var addOnsListAdapter: AddOnsListAdapter
    private var relatedItemList = ArrayList<RelateditemsItem>()
    private var variationId = "0"
    private var variationName = ""
    private var variationPrice = "0.00"
    private var isFavourite = 0
    private var manager: LinearLayoutManager? = null
    private var itemPrice = "0.00"
    private var variationList = ArrayList<VariationItem>()
    private var addonsList = ArrayList<AddOnsDataModel>()
    var currency = ""
    var currencyPosition = ""
    var categoryInfo = CategoryInfo()

    private lateinit var binding: ActFoodDetailsBinding
    override fun setLayout(): View = binding.root

    override fun InitView() {
        binding = ActFoodDetailsBinding.inflate(layoutInflater)
        currency = SharePreference.getStringPref(
            this@ActFoodDetails,
            SharePreference.isCurrancy
        ).toString()
        currencyPosition = SharePreference.getStringPref(
            this@ActFoodDetails,
            SharePreference.CurrencyPosition
        ).toString()
        manager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        if (Common.isCheckNetwork(this@ActFoodDetails)) {
            callApiFoodDetail(intent.getStringExtra("foodItemId")!!)
        } else {
            Common.alertErrorOrValidationDialog(
                this@ActFoodDetails,
                resources.getString(R.string.no_internet)
            )
        }

        if (SharePreference.getStringPref(
                this@ActFoodDetails,
                SharePreference.SELECTED_LANGUAGE
            ).equals(
                resources.getString(R.string.language_hindi)
            )
        ) {
            binding.ivBack.rotation = 180F

        } else {
            binding.ivBack.rotation = 0F

        }

        binding.ivBack.setOnClickListener {
            if (intent.getBooleanExtra(Constants.FromNotification, false)) {
                val i = Intent(this@ActFoodDetails, DashboardActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
                finish()
            } else {
                finish()

            }
        }

        binding.ivwishlist.setOnClickListener {

            if (SharePreference.getBooleanPref(
                    this@ActFoodDetails,
                    SharePreference.isLogin
                )
            ) {

                if (isFavourite == 1) {
                    callFavouritesApi(false, "unfavorite", itemModel?.id.toString(), 0)
                } else {
                    callFavouritesApi(false, "favorite", itemModel?.id.toString(), 0)
                }
            } else {
                openActivity(LoginActivity::class.java)
                finish()
                finishAffinity()
            }

        }

        binding.ivCategories.setOnClickListener {
            val intent = Intent(this@ActFoodDetails, ActSubCategoryProducts::class.java)
            intent.putExtra("category_id", categoryInfo.Id.toString())
            intent.putExtra("category_name", categoryInfo.categoryName.toString())
            startActivity(intent)
        }


        binding.btnViewCart.setOnClickListener {

            val intent =
                Intent(this@ActFoodDetails, DashboardActivity::class.java).putExtra("pos", "3")
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
        binding.btnViewCart.text = "${resources.getString(R.string.view_cart)} (${SharePreference.getStringPref(this@ActFoodDetails, SharePreference.BadgesCount)})"

    }


    private fun relatedItemAddToCartFlow(i: Int) {
        if (relatedItemList[i].hasVariation != 1 && relatedItemList[i].addons?.size == 0 || relatedItemList[i].variation?.size == 0 && relatedItemList[i].addons?.size == 0) {
            relatedITemAddToCart(
                relatedItemList[i].tax.toString(),
                relatedItemList[i].id.toString(),
                relatedItemList[i].itemType.toString(),
                relatedItemList[i].itemName.toString(),
                relatedItemList[i].imageName.toString(),
                relatedItemList[i].price.toString(),
                i
            )
        } else {
            val fragVariation = FragVariation()
            val bundle = Bundle()
            bundle.putParcelableArrayList("variation", relatedItemList[i].variation)
            bundle.putParcelableArrayList("addons", relatedItemList[i].addons)
            bundle.putString("name", relatedItemList[i].itemName)

            bundle.putString("id", relatedItemList[i].id.toString())
            bundle.putString("itemImage", relatedItemList[i].imageName.toString())
            bundle.putString("itemType", relatedItemList[i].itemType.toString())
            bundle.putString("tax", relatedItemList[i].tax.toString())
            bundle.putString("itemPrice", relatedItemList[i].price.toString())
            bundle.putString("from", Constants.FoodDetail)
            bundle.putString("pos", i.toString())


            fragVariation.arguments = bundle
            fragVariation.show(
                supportFragmentManager,
                fragVariation.tag
            )
        }
    }


    inner class ImageSlider(
        private var mContext: Context,
        private var imageList: ArrayList<ItemImage>
    ) : PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return imageList.size
        }

        override fun instantiateItem(view: ViewGroup, position: Int): Any {
            val inflater = LayoutInflater.from(mContext)
            val itemView =
                inflater.inflate(R.layout.row_imageviewpager_item, view, false) as ViewGroup
            val mImageView =
                itemView.findViewById<View>(R.id.img_pager) as androidx.appcompat.widget.AppCompatImageView
            Glide.with(mContext).load(imageList[position].image_url)
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .into(mImageView)

            mImageView.setOnClickListener {
                mContext.startActivity(
                    Intent(
                        mContext,
                        ImageSliderActivity::class.java
                    ).putParcelableArrayListExtra("imageList", imageList).putExtra("pos", position)
                )

            }

            (view as ViewPager).addView(itemView)
            return itemView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            (container as ViewPager).removeView(`object` as View?)
        }

    }

    private fun setupRelatedItemData(relatedItemList: ArrayList<RelateditemsItem>) {
        binding.rvItemRelatedProduct.layoutManager =
            LinearLayoutManager(this@ActFoodDetails, LinearLayoutManager.HORIZONTAL, false)
        relatedItemAdapter =
            RelatedItemAdapter(this@ActFoodDetails, relatedItemList) { i: Int, s: String ->
                if (s == Constants.ItemClick) {
                    startActivity(
                        Intent(this@ActFoodDetails, ActFoodDetails::class.java).putExtra(
                            "foodItemId",
                            relatedItemList[i].id.toString()
                        )
                    )
                } else if (s == Constants.SelectVariationClick) {
                    relatedItemAddToCartFlow(i)
                } else if (s == Constants.FavClick) {
                    if (SharePreference.getBooleanPref(
                            this@ActFoodDetails,
                            SharePreference.isLogin
                        )
                    ) {
                        if (relatedItemList[i].isFavorite == 1) {
                            callFavouritesApi(
                                true,
                                "unfavorite",
                                relatedItemList[i].id.toString(),
                                i
                            )
                        } else {
                            callFavouritesApi(true, "favorite", relatedItemList[i].id.toString(), i)
                        }
                    } else {
                        openActivity(LoginActivity::class.java)
                        finish()
                    }
                } else if (s == Constants.AddToCart) {
                    relatedItemAddToCartFlow(i)
                } else if (s == Constants.DeleteItem) {
                    dlgDeleteCartAlert()
                }
            }

        binding.rvItemRelatedProduct.adapter = relatedItemAdapter
    }


    private val refreshData: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, data: Intent?) {
            try {
                /*   if (Common.isCheckNetwork(this@ActFoodDetails)) {
                       callApiFoodDetail(intent.getStringExtra("foodItemId")!!)
                   } else {
                       Common.alertErrorOrValidationDialog(
                           this@ActFoodDetails,
                           resources.getString(R.string.no_internet)
                       )
                   }*/
                val position = data?.getStringExtra("pos")!!.toInt()
                Log.e("position", position.toString())
                relatedItemList[position].isCart = 1
                relatedItemList[position].itemQty = relatedItemList[position].itemQty?.plus(1)
                relatedItemAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Message", e.message.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(refreshData, IntentFilter("refreshData"))
    }

    override fun onPause() {
        super.onPause()

        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshData)


    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshData)

    }


    private fun dlgDeleteCartAlert() {
        var dialog: Dialog? = null
        try {
            dialog?.dismiss()
            dialog = Dialog(this@ActFoodDetails, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            );
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(true)
            val mInflater = LayoutInflater.from(this@ActFoodDetails)
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
                val intent =
                    Intent(this@ActFoodDetails, DashboardActivity::class.java).putExtra("pos", "3")
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


    private fun callFavouritesApi(
        fromRelatedItem: Boolean,
        favType: String,
        serviceId: String,
        position: Int
    ) {
        showLoadingProgress(this@ActFoodDetails)

        val hashMap = HashMap<String, String>()
        hashMap["user_id"] =
            SharePreference.getStringPref(this@ActFoodDetails, SharePreference.userId) ?: ""
        hashMap["type"] = favType
        hashMap["item_id"] = serviceId
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().manageFavoritesApi(hashMap)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()
                    if (fromRelatedItem) {
                        if (favType == "favorite") {
                            relatedItemList[position].isFavorite = 1

                        } else {
                            relatedItemList[position].isFavorite = 0

                        }
                        relatedItemAdapter.notifyDataSetChanged()

                    } else {
                        if (favType == "favorite") {
                            isFavourite = 1
                            binding.ivwishlist.setImageResource(R.drawable.ic_fav)
                            binding.ivwishlist.imageTintList = ColorStateList.valueOf(
                                ResourcesCompat.getColor(
                                    resources,
                                    R.color.white,
                                    null
                                )
                            )

                        } else {
                            isFavourite = 0
                            binding.ivwishlist.setImageResource(R.drawable.ic_unfav)
                            binding.ivwishlist.imageTintList = ColorStateList.valueOf(
                                ResourcesCompat.getColor(
                                    resources,
                                    R.color.white,
                                    null
                                )
                            )
                        }


                    }

                }
                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFoodDetails,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFoodDetails,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFoodDetails,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }


    private fun callApiFoodDetail(strFoodId: String) {
        showLoadingProgress(this@ActFoodDetails)
        val map = HashMap<String, String>()
        map["item_id"] = strFoodId
        map["user_id"] =
            SharePreference.getStringPref(this@ActFoodDetails, SharePreference.userId) ?: ""
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setItemDetail(map)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()

                    val responseBody = response.body
                    Log.e("Food Detail Response", Gson().toJson(responseBody))
                    setRestaurantData(responseBody.data)

                    if (responseBody.relateditems?.size!! > 0) {
                        binding.rvItemRelatedProduct.visibility = View.VISIBLE
                        binding.clRelated.visibility = View.VISIBLE
                        relatedItemList.clear()
                        relatedItemList.addAll(responseBody.relateditems)
                        setupRelatedItemData(relatedItemList)
                    } else {
                        binding.rvItemRelatedProduct.visibility = View.GONE
                        binding.clRelated.visibility = View.GONE
                    }

                    binding.clDetailLayout.visibility = View.VISIBLE

                }
                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFoodDetails,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFoodDetails,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFoodDetails,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }


    @SuppressLint("SetTextI18n", "NewApi")
    private fun setRestaurantData(restResponce: FoodItemData?) {
        itemModel = restResponce

        addonsList.clear()
        variationList.clear()

        binding.viewPager.adapter =
            restResponce?.itemImage?.let { ImageSlider(this@ActFoodDetails, it) }
        binding.tabLayout?.setupWithViewPager(binding.viewPager, true)

        isFavourite = restResponce?.isFavorite ?: 0
        if (restResponce?.isFavorite == 1) {

            binding.ivwishlist.setImageResource(R.drawable.ic_fav)
            binding.ivwishlist.imageTintList =
                ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.white, null))

        } else {
            binding.ivwishlist.setImageResource(R.drawable.ic_unfav)
            binding.ivwishlist.imageTintList =
                ColorStateList.valueOf(ResourcesCompat.getColor(resources, R.color.white, null))
        }

        if (restResponce?.itemType == 2) {
            binding.ivveg.setImageResource(R.drawable.ic_nonveg)
        } else {
            binding.ivveg.setImageResource(R.drawable.ic_vegetarian)
        }

        if ((restResponce?.addons?.size ?: 0) > 0) {
            restResponce?.addons?.let { addonsList.addAll(it) }

            binding.clAddons.visibility = View.VISIBLE
            binding.rvAddonslist.visibility = View.VISIBLE

        } else {
            binding.clAddons.visibility = View.GONE
            binding.rvAddonslist.visibility = View.GONE
        }
        if ((restResponce?.variation?.size ?: 0) > 0) {
            restResponce?.variation?.let { variationList.addAll(it) }

            binding.clVariation.visibility = View.VISIBLE
            binding.rvVariation.visibility = View.VISIBLE


        } else {
            binding.clVariation.visibility = View.GONE
            binding.rvVariation.visibility = View.GONE
        }


        setupAdapter(restResponce?.price ?: "0.00")

        binding.tvItemName.text = restResponce?.itemName

        if (!restResponce?.tax.isNullOrEmpty() && restResponce?.tax != "0") {
            binding.tvTaxes.text = "+ ".plus(restResponce?.tax).plus("%").plus(" Additional Tax")
            binding.tvTaxes.setTextColor(Color.RED)
        }
        categoryInfo = restResponce?.categoryInfo!!
        binding.tvType.text = restResponce.categoryInfo.categoryName
        binding.tvTimeMin.text = restResponce.preparationTime
        binding.tvItemDetails.text = restResponce.itemDescription
        if (restResponce.itemDescription.isNullOrEmpty()) {
            binding.tvItemDetails.visibility = View.GONE
            binding.cldetails.visibility = View.GONE
        }


        binding.btnAddCart.setOnClickListener {
            try {
                if (SharePreference.getBooleanPref(this@ActFoodDetails, SharePreference.isLogin)) {
                    Log.e("addOnsList", addonsList.size.toString())
                    Log.e("variationList", variationList.size.toString())
                    if (Common.isCheckNetwork(this@ActFoodDetails)) {
                        getVariation()
                        if (variationList.size != 0 && addonsList.size != 0) {
                            val addOnsID = TextUtils.join(",", getAddOnsId())
                            val addOnsName = TextUtils.join(",", getAddOnsName())
                            val addOnsPrice = TextUtils.join(",", getAddOnsPrice())
                            val addOnsTotal =
                                getAddOnsPrice().map { it.toDouble() }.toTypedArray().sum()
                            Log.e("totalPriceAddOns", addOnsTotal.toString())
                            callApiAddToCart(
                                itemModel!!,
                                addOnsID,
                                addOnsName,
                                addOnsPrice,
                                addOnsTotal.toString()
                            )

                        } else if (variationList.size == 0 && addonsList.size != 0) {
                            val addOnsID = TextUtils.join(",", getAddOnsId())
                            val addOnsName = TextUtils.join(",", getAddOnsName())
                            val addOnsPrice = TextUtils.join(",", getAddOnsPrice())
                            val addOnsTotal = getAddOnsPrice().map { it.toDouble() }.toTypedArray().sum()

                            callApiAddToCart(
                                itemModel!!,
                                addOnsID,
                                addOnsName,
                                addOnsPrice,
                                addOnsTotal.toString()
                            )

                        } else {
                            callApiAddToCart(itemModel!!, "", "", "0.00", "0.00")
                        }

                    } else {
                        Common.alertErrorOrValidationDialog(
                            this@ActFoodDetails,
                            resources.getString(R.string.no_internet)
                        )
                    }
                } else {
                    openActivity(LoginActivity::class.java)
                    finish()
                }

            } catch (e: Exception) {
                Log.e("error", e.message.toString())
            }

        }
    }


    private fun getVariation() {
        for (i in 0 until variationList.size) {
            if (variationList[i].isSelect == true) {

                variationId = variationList[i].id.toString()
                variationName = variationList[i].variation.toString()
                variationPrice = variationList[i].productPrice.toString()
            }
        }

    }


    private fun setupAdapter(price: String) {
        if (variationList.size > 0) {
            variationList[0].isSelect = true

            binding.tvItemPrice.text = Common.getPrices(
                currencyPosition, currency,
                variationList[0].productPrice ?: "0.00"
            )
            itemPrice = variationList[0].productPrice ?: "0.00"
        } else {
            binding.tvItemPrice.text = Common.getPrices(
                currencyPosition, currency,
                price
            )

            itemPrice = price


        }


        binding.rvVariation.apply {
            layoutManager = LinearLayoutManager(this@ActFoodDetails)
            variationAdapter = VariationListAdapter(
                this@ActFoodDetails,
                variationList
            ) { pos: Int, type: String ->
                if (type == Constants.ItemClick) {
                    binding.tvItemPrice.text = Common.getPrices(
                        currencyPosition, currency,
                        variationList[pos].productPrice ?: "0.00"
                    )

                    itemPrice = variationList[pos].productPrice ?: "0.00"
                }
            }
            adapter = variationAdapter
        }


        binding.rvAddonslist.apply {

            layoutManager = LinearLayoutManager(this@ActFoodDetails)
            addOnsListAdapter = AddOnsListAdapter(
                this@ActFoodDetails,
                addonsList)
            adapter = addOnsListAdapter
        }
    }

    private fun getAddOnsId(): ArrayList<String> {
        val list = ArrayList<String>()

        for (i in 0 until addonsList.size) {
            if (addonsList[i].isAddOnsSelect) {
                list.add(addonsList[i].Id.toString())
            }
        }
        return list
    }

    private fun getAddOnsName(): ArrayList<String> {
        val list = ArrayList<String>()

        for (i in 0 until addonsList.size) {
            if (addonsList[i].isAddOnsSelect) {
                list.add(addonsList[i].name.toString())
            }
        }
        return list
    }

    private fun getAddOnsPrice(): ArrayList<String> {
        val list = ArrayList<String>()

        for (i in 0 until addonsList.size) {
            if (addonsList[i].isAddOnsSelect) {

                list.add(String.format(Locale.US, "%.02f", addonsList[i].price?.toDouble()))
            }
        }
        return list
    }


    private fun relatedITemAddToCart(
        tax: String,
        itemId: String,
        itemType: String,
        itemName: String,
        itemImage: String,
        price: String,
        position: Int
    ) {
        showLoadingProgress(this@ActFoodDetails)
        val map = java.util.HashMap<String, String>()
        map["user_id"] =
            SharePreference.getStringPref(this@ActFoodDetails, SharePreference.userId) ?: ""
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


                    relatedItemList[position].isCart = 1
                    relatedItemList[position].itemQty = relatedItemList[position].itemQty?.plus(1)
                    SharePreference.setStringPref(
                        this@ActFoodDetails,
                        SharePreference.BadgesCount,
                        response.body.cartCount.toString()
                    )

                    relatedItemAdapter.notifyDataSetChanged()


                }

                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFoodDetails,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFoodDetails,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFoodDetails,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }


    private fun callApiAddToCart(
        itemDetailModel: FoodItemData,
        strAddonsGetId: String,
        strAddonsGetName: String,
        strAddonsGetPrice: String,
        addOnsTotalPrice: String
    ) {
        showLoadingProgress(this@ActFoodDetails)
        val map = HashMap<String, String>()
        map["user_id"] =
            SharePreference.getStringPref(this@ActFoodDetails, SharePreference.userId)!!
        map["item_id"] = itemDetailModel.id.toString()
        map["item_price"] = String.format(Locale.US, "%.02f", itemPrice.toDouble())
        map["item_type"] = itemDetailModel.itemType.toString()
        map["item_image"] = itemDetailModel.itemImage!![0].image_name.toString()
        map["item_name"] = itemDetailModel.itemName.toString()
        map["variation_id"] = variationId
        map["variation_price"] = variationPrice
        map["variation"] = variationName
        map["addons_price"] = strAddonsGetPrice
        map["addons_name"] = strAddonsGetName
        map["addons_id"] = strAddonsGetId
        map["tax"] = itemDetailModel.tax.toString()
        map["addons_total_price"] = String.format(Locale.US, "%.02f", addOnsTotalPrice.toDouble())

        Log.d("request", Gson().toJson(map))
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().addToCart(map)) {
                is NetworkResponse.Success -> {
                    dismissLoadingProgress()
                    if (response.body.status == 1) {
                        dismissLoadingProgress()
                        for (i in 0 until variationList.size) {
                            variationList[i].isSelect = false
                        }
                        for (i in 0 until addonsList.size) {
                            addonsList[i].isAddOnsSelect = false
                        }
                        SharePreference.setStringPref(
                            this@ActFoodDetails,
                            SharePreference.BadgesCount,
                            response.body.cartCount.toString()
                        )
                        binding.btnViewCart.text = "${resources.getString(R.string.view_cart)} (${
                            SharePreference.getStringPref(
                                this@ActFoodDetails,
                                SharePreference.BadgesCount
                            )
                        })"

                        addOnsListAdapter.notifyDataSetChanged()
                        variationAdapter.notifyDataSetChanged()
                    }
                }

                is NetworkResponse.ApiError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFoodDetails,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFoodDetails,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFoodDetails,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (intent.getBooleanExtra(Constants.FromNotification, false)) {
            val i = Intent(this@ActFoodDetails, DashboardActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
            finish()
        } else {
            finish()
        }
    }
}