package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.base.BaseAdaptor
import com.singlerestaurant.user.databinding.ActivitySearchBinding
import com.singlerestaurant.user.model.FoodItemModel
import com.singlerestaurant.user.model.ListResponse
import com.singlerestaurant.user.model.SearchItemModel
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.util.*


class SearchActivity : BaseActivity() {
    private var strSearch: String = ""

    private var searchAdapter: BaseAdaptor<SearchItemModel>? = null
    private var foodList: ArrayList<FoodItemModel>? = null
    private var searchList = ArrayList<SearchItemModel>()
    private var newSearchData = ArrayList<SearchItemModel>()
    private var manager1: LinearLayoutManager? = null
    private var filterType = ""
    private lateinit var binding: ActivitySearchBinding
    override fun setLayout(): View = binding.root


    override fun InitView() {
        binding = ActivitySearchBinding.inflate(layoutInflater)
        Common.getCurrentLanguage(this@SearchActivity, false)
        foodList = ArrayList()
        manager1 = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        binding.ivBack.setOnClickListener {
            val intent =
                Intent(this@SearchActivity, DashboardActivity::class.java).putExtra("pos", "1")
            startActivity(intent)
            finish()
            finishAffinity()
        }
        setupAdapter(newSearchData)

        callApiSearchFood(true)




        if (SharePreference.getStringPref(this@SearchActivity, SharePreference.SELECTED_LANGUAGE)
                .equals(resources.getString(R.string.language_hindi))
        ) {
            binding.ivBack.rotation = 180F
        } else {
            binding.ivBack.rotation = 0F
        }





        binding.edSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(cs: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                val text: String = binding.edSearch.text.toString().lowercase(Locale.getDefault())
                filter(text)
                if (binding.edSearch.text?.isEmpty() == true) {
                    newSearchData.clear()
                    searchAdapter?.notifyDataSetChanged()
                    if (newSearchData.isEmpty()) {
                        binding.tvNoDataFound.visibility = View.VISIBLE
                        binding.rvSearchOrder.visibility = View.GONE
                    } else {
                        binding.tvNoDataFound.visibility = View.GONE
                        binding.rvSearchOrder.visibility = View.VISIBLE
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                arg0: CharSequence, arg1: Int, arg2: Int,
                arg3: Int
            ) {

            }
        })
    }


    fun filter(text: String) {
        Log.d("tag ::", "mainitemList 02" + searchList.size)
        newSearchData.clear()
        for (d in searchList) {
            if (d.itemName?.lowercase(Locale.ROOT)?.contains(text.lowercase(Locale.ROOT))!!) {
                newSearchData.add(d)
                Log.d("tag ::", "itemList 02" + newSearchData.size)
            }
        }
        searchAdapter?.notifyDataSetChanged()
        viewVisibility()

    }

    private fun viewVisibility() {
        if (newSearchData.isEmpty()) {
            binding.tvNoDataFound.visibility = View.VISIBLE
            binding.rvSearchOrder.visibility = View.GONE
        } else {
            binding.tvNoDataFound.visibility = View.GONE
            binding.rvSearchOrder.visibility = View.VISIBLE
        }
    }


    private fun callApiSearchFood(fromSearch: Boolean) {
        Common.showLoadingProgress(this@SearchActivity)
        val map = HashMap<String, String>()
        map["user_id"] =
            SharePreference.getStringPref(this@SearchActivity, SharePreference.userId) ?: ""
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
                            searchList.clear()
                            binding.tvNoDataFound.visibility = View.VISIBLE
                            binding.rvSearchOrder.visibility = View.GONE
                            restResponse.data?.let { searchList.addAll(it) }

                        } else {
                            binding.tvNoDataFound.visibility = View.VISIBLE
                            binding.rvSearchOrder.visibility = View.GONE
                            Common.dismissLoadingProgress()

                        }

                    } else {
                        Common.showErrorFullMsg(
                            this@SearchActivity,
                            response.body.message.toString()
                                .replace("\\n", System.lineSeparator())
                        )
                    }

                }

                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@SearchActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@SearchActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@SearchActivity,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this@SearchActivity, DashboardActivity::class.java).putExtra("pos", "1")
        startActivity(intent)
        finish()
        finishAffinity()
    }


    private fun setupAdapter(searchList: ArrayList<SearchItemModel>) {
        searchAdapter = object : BaseAdaptor<SearchItemModel>(this@SearchActivity, searchList) {
            @SuppressLint("NewApi")
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: SearchItemModel,
                position: Int
            ) {
                val data = searchList[position]

                val tvItemName: TextView = holder!!.itemView.findViewById(R.id.tvItemName)
                val tvFoodPriceGrid: TextView = holder.itemView.findViewById(R.id.tvFoodPriceGrid)
                val ivVeg: ImageView = holder.itemView.findViewById(R.id.ivveg)
                val tvStarter: TextView = holder.itemView.findViewById(R.id.tvStarter)
                val ivFood: ImageView = holder.itemView.findViewById(R.id.ivFood)
                val ivWishList: ImageView = holder.itemView.findViewById(R.id.ivwishlist)


                val currency =
                    SharePreference.getStringPref(this@SearchActivity, SharePreference.isCurrancy)
                        ?: ""
                val currencyPos = SharePreference.getStringPref(
                    this@SearchActivity,
                    SharePreference.CurrencyPosition
                ) ?: ""
                Glide.with(this@SearchActivity).load(data.imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade(500)).into(ivFood)

                tvStarter.text = data.categoryInfo?.categoryName
                tvFoodPriceGrid.text =
                    Common.getPrices(
                        currencyPos,
                        currency,
                        data.price.toString() ?: "0.00"
                    )

                if (data.itemType == "2") {
                    ivVeg.setImageResource(R.drawable.ic_nonveg)
                } else {
                    ivVeg.setImageResource(R.drawable.ic_vegetarian)
                }
                if (data.isFavorite == 1) {
                    ivWishList.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_fav, null)
                } else {
                    ivWishList.background =
                        ResourcesCompat.getDrawable(resources, R.drawable.ic_unfav, null)
                }

                ivWishList.setOnClickListener {

                    if (SharePreference.getBooleanPref(
                            this@SearchActivity,
                            SharePreference.isLogin
                        )
                    ) {
                        if (data.isFavorite == 1) {
                            callFavouritesApi(
                                "unfavorite",
                                data.id.toString(),
                                position

                            )
                            ivWishList.background =
                                ResourcesCompat.getDrawable(resources, R.drawable.ic_unfav, null)
                        } else {
                            callFavouritesApi(
                                "favorite",
                                data.id.toString(),
                                position
                            )
                            ivWishList.background =
                                ResourcesCompat.getDrawable(resources, R.drawable.ic_fav, null)
                        }
                    } else {
                        openActivity(LoginActivity::class.java)
                        finish()
                    }
                }


                tvItemName.text = data.itemName
                holder.itemView.setOnClickListener {
                    startActivity(
                        Intent(this@SearchActivity, ActFoodDetails::class.java)
                            .putExtra("foodItemId", searchList[position].id.toString())
                    )


                }

            }

            override fun setItemLayout(): Int {
                return R.layout.cell_search_product
            }

        }
        binding.rvSearchOrder.adapter = searchAdapter
        binding.rvSearchOrder.layoutManager =
            GridLayoutManager(this@SearchActivity, 2, RecyclerView.VERTICAL, false)
        binding.rvSearchOrder.itemAnimator = DefaultItemAnimator()
        binding.rvSearchOrder.isNestedScrollingEnabled = true
    }


    private fun callFavouritesApi(
        favType: String,
        serviceId: String,
        position: Int
    ) {
        val hashMap = HashMap<String, String>()
        hashMap["user_id"] =
            SharePreference.getStringPref(this@SearchActivity, SharePreference.userId) ?: ""
        hashMap["type"] = favType
        hashMap["item_id"] = serviceId
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().manageFavoritesApi(hashMap)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    if (response.body.status == 1) {
                        if (favType == "favorite") {
                            newSearchData[position].isFavorite = 1

                            for (i in 0 until searchList.size) {
                                if (searchList[i].id == newSearchData[position].id) {
                                    searchList[i].isFavorite = 1
                                }
                            }
                        } else {
                            newSearchData[position].isFavorite = 0
                            for (i in 0 until searchList.size) {
                                if (searchList[i].id == newSearchData[position].id) {
                                    searchList[i].isFavorite = 0
                                }
                            }

                        }
                        searchAdapter?.notifyDataSetChanged()


                    } else {
                        Common.showErrorFullMsg(
                            this@SearchActivity,
                            response.body.message.toString()
                                .replace("\\n", System.lineSeparator())
                        )
                    }

                }

                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@SearchActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@SearchActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@SearchActivity,
                        resources.getString(R.string.error_msg)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@SearchActivity, false)
    }

}