package com.singlerestaurant.user.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.*
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.activity.*
import com.singlerestaurant.user.adaptor.*
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.base.BaseAdaptorr
import com.singlerestaurant.user.base.BaseFragment
import com.singlerestaurant.user.databinding.FragmentHomeBinding
import com.singlerestaurant.user.databinding.RowBannersliderBinding
import com.singlerestaurant.user.model.*
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.isCheckNetwork
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import com.google.gson.Gson
import com.singlerestaurant.user.databinding.RowSquareBannerBinding
import com.singlerestaurant.user.databinding.RowTopBannerBinding
import kotlinx.coroutines.launch
import java.util.*


class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private var categoriesList = ArrayList<CategoriesItem>()
    private lateinit var categoriesAdapter: CategoriesAdapter
    private val featuredList = ArrayList<FeatureditemsItem>()
    private lateinit var featuredadapter: FeaturedAdapter
    private val recommendedList = ArrayList<RecommendeditemsItem>()
    private lateinit var recommendedAdapter: RecommendedAdapter
    private lateinit var trendingItemAdapter: TrendingItemAdapter
    private lateinit var testimonialAdapter: TestimonialAdapter
    private val trendingItemList = ArrayList<FeatureditemsItem>()
    private val reviewList = ArrayList<TestimonialData>()
    private lateinit var binding: FragmentHomeBinding


    override fun Init(view: View) {
        Common.getCurrentLanguage(requireActivity(), false)


        binding.ivCategorie.setOnClickListener {

            val intent = Intent(requireActivity(), ActCategories::class.java)
            intent.putParcelableArrayListExtra("categoryArray", categoriesList)
            startActivity(intent)
        }

        binding.clSerach.setOnClickListener {
            val intent = Intent(requireActivity(), SearchActivity::class.java)
            startActivity(intent)
        }
        if (isCheckNetwork(requireActivity())) {
            callHomeFeedApi()
        } else {
            Common.showErrorFullMsg(requireActivity(), resources.getString(R.string.no_internet))
        }

        categoriesAdapterSetup()
        setupTrendingAdapter()
        featuredAdapterSetup()
        recommendedAdapterSetup()
        setupTestimonialAdapter()
        initClickListeners()

        setupSnapHelper()
    }




    private fun setupSnapHelper() {

        val linearSnapHelper: SnapHelper = LinearSnapHelper()
        linearSnapHelper.attachToRecyclerView(binding.rvTopBanner)
        linearSnapHelper.attachToRecyclerView(binding.rvBanner)
        linearSnapHelper.attachToRecyclerView(binding.rvSecondBanner)
        linearSnapHelper.attachToRecyclerView(binding.rvThirdBanner)


    }


    private fun initClickListeners() {

        binding.ivTrending.setOnClickListener {
            val intent = Intent(requireActivity(), ActViewAll::class.java)
            intent.putExtra("type", "2")
            startActivity(intent)
        }

        binding.ivFeatured.setOnClickListener {
            val intent = Intent(requireActivity(), ActViewAll::class.java)
            intent.putExtra("type", "1")
            startActivity(intent)
        }

        binding.ivRecommended.setOnClickListener {
            val intent = Intent(requireActivity(), ActViewAll::class.java)
            intent.putExtra("type", "3")
            startActivity(intent)
        }
        binding.ivTestimonials.setOnClickListener {
            val intent = Intent(requireActivity(), ActRattings::class.java)
            startActivity(intent)
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun callHomeFeedApi() {
        Common.showLoadingProgress(requireActivity())
        val hashMap = HashMap<String, String>()
        hashMap["user_id"] =
            SharePreference.getStringPref(requireActivity(), SharePreference.userId) ?: ""
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().getHomeFeedApi(hashMap)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    val homeFeedData = response.body

                    if (homeFeedData.status == 1) {

                        homeFeedData.getprofiledata?.let {
                            setProfileData(it)
                        }
                        homeFeedData.appdata?.let {
                            setupAppData(it)
                        }
                        if (homeFeedData.checkaddons == "mobile") {
                            SharePreference.setStringPref(requireActivity(),SharePreference.otpAddon,"1")
                        }
                        else {
                            SharePreference.setStringPref(requireActivity(),SharePreference.otpAddon,"2")
                        }
                        SharePreference.setStringPref(requireActivity(),SharePreference.BadgesCount,response.body.cartdata?.totalCount.toString())

                        (requireActivity() as DashboardActivity).setCartCount()

                        Log.e("profileData", Gson().toJson(response.body.getprofiledata))

                        categoriesList.clear()
                        homeFeedData.categories?.let { categoriesList.addAll(it) }
                        categoriesAdapter.notifyDataSetChanged()

                        setupBannersAdapter(homeFeedData)

                        trendingItemList.clear()
                        homeFeedData.trendingitems?.let { trendingItemList.addAll(it) }
                        trendingItemAdapter.notifyDataSetChanged()

                        featuredList.clear()
                        homeFeedData.todayspecial?.let { featuredList.addAll(it) }
                        featuredadapter.notifyDataSetChanged()

                        recommendedList.clear()
                        homeFeedData.recommendeditems?.let { recommendedList.addAll(it) }
                        recommendedAdapter.notifyDataSetChanged()

                        homeFeedData.testimonials?.let { reviewList.addAll(it) }
                        binding.mainLayout.visibility = View.VISIBLE

                        viewVisibility()

                    } else if (homeFeedData.status == 5) {
                        openActivity(LoginActivity::class.java)
                        requireActivity().finish()
                        requireActivity().finishAffinity()
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


    private fun setupBannersAdapter(homeFeedData: HomeFeedModel) {
        homeFeedData.banners?.topbanners.let {
            it?.let { it1 ->
                setupTopBanner(
                    it1,
                    binding.rvTopBanner
                )
            }
        }
        homeFeedData.banners?.bannersection2.let {
            it?.let { it1 ->
                setupSquareBanner(
                    it1,
                    binding.rvBanner
                )
            }
        }

        homeFeedData.banners?.bannersection1.let {
            it?.let { it1 ->
                setupBanner(
                    it1,
                    binding.rvSecondBanner
                )
            }
        }
        homeFeedData.banners?.bannersection3.let {
            it?.let { it1 ->
                setupBanner(
                    it1,
                    binding.rvThirdBanner
                )
            }
        }


    }


    private fun viewVisibility() {

        if (categoriesList.size > 0) {
            binding.tvCategorie.visibility = View.VISIBLE
            binding.clCategorie.visibility = View.VISIBLE
            binding.ivCategorie.visibility = View.VISIBLE
            binding.rvCategorie.visibility = View.VISIBLE
        } else {
            binding.tvCategorie.visibility = View.GONE
            binding.clCategorie.visibility = View.GONE
            binding.ivCategorie.visibility = View.GONE
            binding.rvCategorie.visibility = View.GONE
        }


        if (trendingItemList.size > 0) {
            binding.tvTrending.visibility = View.VISIBLE
            binding.ivTrending.visibility = View.VISIBLE
            binding.rvTrending.visibility = View.VISIBLE
        } else {
            binding.tvTrending.visibility = View.GONE
            binding.ivTrending.visibility = View.GONE
            binding.rvTrending.visibility = View.GONE
        }
        if (featuredList.size > 0) {
            binding.tvFeatured.visibility = View.VISIBLE
            binding.ivFeatured.visibility = View.VISIBLE
            binding.rvFeatured.visibility = View.VISIBLE
        } else {
            binding.tvFeatured.visibility = View.GONE
            binding.ivFeatured.visibility = View.GONE
            binding.rvFeatured.visibility = View.GONE
        }



        if (recommendedList.size > 0) {
            binding.tvRecommended.visibility = View.VISIBLE
            binding.ivRecommended.visibility = View.VISIBLE
            binding.rvRecommended.visibility = View.VISIBLE
        } else {
            binding.tvRecommended.visibility = View.GONE
            binding.ivRecommended.visibility = View.GONE
            binding.rvRecommended.visibility = View.GONE
        }

        if (reviewList.size > 0) {
            binding.tvTestimonials.visibility = View.VISIBLE
            binding.ivTestimonials.visibility = View.VISIBLE
            binding.rvTestimonials.visibility = View.VISIBLE
        } else {
            binding.tvTestimonials.visibility = View.GONE
            binding.ivTestimonials.visibility = View.GONE
            binding.rvTestimonials.visibility = View.GONE
        }

    }

    private fun setupBanner(bannerList: ArrayList<BannersItem>, recyclerView: RecyclerView) {
        lateinit var binding: RowBannersliderBinding
        val categoryAdapter =
            object :
                BaseAdaptorr<BannersItem, RowBannersliderBinding>(requireActivity(), bannerList) {
                @SuppressLint("ResourceType")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: BannersItem,
                    position: Int
                ) {
                    Glide.with(requireActivity()).load(bannerList[position].image)
                        .transition(DrawableTransitionOptions.withCrossFade(500))
                        .into(binding.ivBannerSlider)


                    holder?.itemView?.setOnClickListener {

                        if (bannerList[position].type == "1") {
                            val intent =
                                Intent(requireActivity(), ActSubCategoryProducts::class.java)
                            intent.putExtra("category_id", bannerList[position].catId.toString())
                            intent.putExtra(
                                "category_name",
                                bannerList[position].catInfo?.categoryName.toString()
                            )
                            startForResult.launch(intent)
                        } else if (bannerList[position].type == "2") {
                            val intent = Intent(requireActivity(), ActFoodDetails::class.java)
                            intent.putExtra("foodItemId", bannerList[position].itemId.toString())
                            startForResult.launch(intent)
                        }


                    }


                }

                override fun getBinding(parent: ViewGroup): RowBannersliderBinding {
                    binding = RowBannersliderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }


            }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            adapter = categoryAdapter
            isNestedScrollingEnabled = true
        }


    }


    private fun setupTopBanner(bannerList: ArrayList<BannersItem>, recyclerView: RecyclerView) {
        lateinit var binding: RowTopBannerBinding

        val categoryAdapter = object : BaseAdaptorr<BannersItem, RowTopBannerBinding>(requireActivity(), bannerList) {
                @SuppressLint("ResourceType")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: BannersItem,
                    position: Int
                ) {
                    Glide.with(requireActivity()).load(bannerList[position].image)
                        .transition(DrawableTransitionOptions.withCrossFade(500))
                        .into(binding.ivBannerSlider)


                    holder?.itemView?.setOnClickListener {

                        if (bannerList[position].type == "1") {
                            val intent =
                                Intent(requireActivity(), ActSubCategoryProducts::class.java)
                            intent.putExtra("category_id", bannerList[position].catId.toString())
                            intent.putExtra(
                                "category_name",
                                bannerList[position].catInfo?.categoryName.toString()
                            )
                            startForResult.launch(intent)
                        } else if (bannerList[position].type == "2") {
                            val intent = Intent(requireActivity(), ActFoodDetails::class.java)
                            intent.putExtra("foodItemId", bannerList[position].itemId.toString())
                            startForResult.launch(intent)
                        }


                    }


                }

                override fun getBinding(parent: ViewGroup): RowTopBannerBinding {
                    binding = RowTopBannerBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                    return binding
                }


            }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            adapter = categoryAdapter
            isNestedScrollingEnabled = true
        }


    }

    private fun setupSquareBanner(bannerList: ArrayList<BannersItem>, recyclerView: RecyclerView) {
        lateinit var binding: RowSquareBannerBinding

        val categoryAdapter = object : BaseAdaptorr<BannersItem, RowSquareBannerBinding>(requireActivity(), bannerList) {
            @SuppressLint("ResourceType")
            override fun onBindData(
                holder: RecyclerView.ViewHolder?,
                `val`: BannersItem,
                position: Int
            ) {
                Glide.with(requireActivity()).load(bannerList[position].image)
                    .transition(DrawableTransitionOptions.withCrossFade(500))
                    .into(binding.ivBannerSlider)

                holder?.itemView?.setOnClickListener {

                    if (bannerList[position].type == "1") {
                        val intent =
                            Intent(requireActivity(), ActSubCategoryProducts::class.java)
                        intent.putExtra("category_id", bannerList[position].catId.toString())
                        intent.putExtra(
                            "category_name",
                            bannerList[position].catInfo?.categoryName.toString()
                        )
                        startForResult.launch(intent)
                    } else if (bannerList[position].type == "2") {
                        val intent = Intent(requireActivity(), ActFoodDetails::class.java)
                        intent.putExtra("foodItemId", bannerList[position].itemId.toString())
                        startForResult.launch(intent)
                    }


                }


            }

            override fun getBinding(parent: ViewGroup): RowSquareBannerBinding {
                binding = RowSquareBannerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return binding
            }


        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)
            adapter = categoryAdapter
            isNestedScrollingEnabled = true
        }


    }

    private fun setupAppData(appData: Appdata?) {
        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.userRefralAmount,
            appData?.referralAmount.toString()
        )

        Glide.with(requireActivity())
            .load(appData?.appBottomImage)
            .transition(DrawableTransitionOptions.withCrossFade(500)).into(binding.ivAppImage)
        if(appData?.is_app_bottom_image=="1")
        {
            binding.ivAppImage.visibility=View.VISIBLE
        }else
        {
            binding.ivAppImage.visibility=View.GONE
        }

        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.ios,
            appData?.ios.toString()
        )

        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.android,
            appData?.android.toString()
        )

        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.isCurrancy,
            appData?.currency!!
        )

        SharePreference.setStringPref(requireActivity(), SharePreference.aboutUs, appData.aboutContent.toString())
        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.isMiniMum,
            appData.minOrderAmount.toString()
        )
        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.isMaximum,
            appData.maxOrderAmount.toString()
        )
        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.isMiniMumQty,
            appData.maxOrderQty.toString()
        )
        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.mapKey,
            appData.map.toString()
        )
        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.resLat,
            appData.lat.toString()
        )
        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.resLang,
            appData.lang.toString()
        )

        SharePreference.setStringPref(requireActivity(),SharePreference.adminMobile,appData.mobile.toString())
        SharePreference.setStringPref(requireActivity(),SharePreference.adminEmail,appData.email.toString())
        SharePreference.setStringPref(requireActivity(),SharePreference.adminAddress,appData.address.toString())
        SharePreference.setStringPref(requireActivity(),SharePreference.facebookLink,appData.fb.toString())
        SharePreference.setStringPref(requireActivity(),SharePreference.instaLink,appData.insta.toString())
        SharePreference.setStringPref(requireActivity(),SharePreference.youtubeLink,appData.youtube.toString())

        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.kmCharge,
            appData.deliveryCharge.toString())
        appData.currencyPosition?.let {
            SharePreference.setStringPref(
                requireActivity(),
                SharePreference.CurrencyPosition,
                it
            )
        }
    }

    private fun setProfileData(getProfile: getprofiledata?) {

        if (SharePreference.getStringPref(requireContext(),SharePreference.userName) !=null) {
            binding.tvUserName.text =
                SharePreference.getStringPref(requireContext(), SharePreference.userName)
            binding.ivProfile.visibility = View.VISIBLE
            Glide.with(requireActivity())
                .load(getProfile?.profileImage)
                .apply(RequestOptions.circleCropTransform())
                .transition(DrawableTransitionOptions.withCrossFade(500)).into(binding.ivProfile)
        }
        else {
            binding.ivProfile.visibility = View.GONE
            binding.tvUserName.text = "Newindian shop"
        }

        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.notificationStatus,
            getProfile?.isNotification.toString()
        )
        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.userRefralCode,
            getProfile?.referral_code.toString()
        )
        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.userProfile,
            getProfile?.profileImage.toString()
        )
        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.userEmail,
            getProfile?.email.toString()
        )

        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.userMobile,
            getProfile?.mobile.toString()
        )
        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.userId,
            getProfile?.id.toString()
        )

        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.wallet,
            getProfile?.wallet?:"00"
        )

        SharePreference.setStringPref(
            requireActivity(),
            SharePreference.emailStatus,
            getProfile?.isMail.toString()
        )
        SharePreference.setStringPref(requireActivity(), SharePreference.loginType, getProfile?.loginType.toString())
    }


    private fun categoriesAdapterSetup() {
        binding.rvCategorie.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

        categoriesAdapter =
            CategoriesAdapter(requireActivity(), categoriesList) { i: Int, s: String ->
                if (s == Constants.ItemClick) {
                    val intent = Intent(requireActivity(), ActSubCategoryProducts::class.java)
                    intent.putExtra("category_id", categoriesList[i].id.toString())
                    intent.putExtra("category_name", categoriesList[i].categoryName.toString())
                    startForResult.launch(intent)
                }
            }
        binding.rvCategorie.adapter = categoriesAdapter
        binding.rvCategorie.isNestedScrollingEnabled = true
    }

    private fun featuredAdapterSetup() {
        binding.rvFeatured.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        featuredadapter = FeaturedAdapter(requireActivity(), featuredList) { i: Int, s: String ->
            if (s == Constants.ItemClick) {
                val intent = Intent(requireActivity(), ActFoodDetails::class.java)
                intent.putExtra("foodItemId", featuredList[i].id.toString())
                startForResult.launch(intent)
            } else if (s == Constants.SelectVariationClick) {
                if (SharePreference.getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                    featuredItemAddToCartFlow(i)
                } else {
                    openActivity(LoginActivity::class.java)
                    activity?.finish()
                    activity?.finishAffinity()
                }
            } else if (s == Constants.FavClick) {
                if (SharePreference.getBooleanPref(
                        requireActivity(),
                        SharePreference.isLogin
                    )
                ) {
                    if (featuredList[i].isFavorite == 1) {
                        callFavouritesApi(
                            "unfavorite",
                            "featured",
                            featuredList[i].id.toString(),
                            i
                        )
                    } else {
                        callFavouritesApi(
                            "favorite",
                            "featured",
                            featuredList[i].id.toString(),
                            i
                        )
                    }
                } else {
                    openActivity(LoginActivity::class.java)
                    activity?.finish()
                }
            } else if (s == Constants.AddToCart) {
                if (SharePreference.getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                    featuredItemAddToCartFlow(i)
                } else {
                    openActivity(LoginActivity::class.java)
                    activity?.finish()
                    activity?.finishAffinity()
                }
            } else if (s == Constants.DeleteItem) {
                if (SharePreference.getBooleanPref(requireActivity(), SharePreference.isLogin)) {
                    dlgDeleteCartAlert()
                } else {
                    openActivity(LoginActivity::class.java)
                    activity?.finish()
                    activity?.finishAffinity()
                }
            }
        }
        binding.rvFeatured.adapter = featuredadapter
        binding.rvFeatured.isNestedScrollingEnabled = true
    }


    private fun setupTrendingAdapter() {
        binding.rvTrending.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        trendingItemAdapter =
            TrendingItemAdapter(requireActivity(), trendingItemList) { i: Int, s: String ->
                if (s == Constants.ItemClick) {
                    val intent = Intent(requireActivity(), ActFoodDetails::class.java)
                    intent.putExtra("foodItemId", trendingItemList[i].id.toString())
                    startForResult.launch(intent)
                } else if (s == Constants.SelectVariationClick) {
                    if (SharePreference.getBooleanPref(
                            requireActivity(),
                            SharePreference.isLogin
                        )
                    ) {
                        trendingItemAddToCartFlow(i)

                    } else {
                        openActivity(LoginActivity::class.java)
                        activity?.finish()
                        activity?.finishAffinity()
                    }

                } else if (s == Constants.FavClick) {
                    if (SharePreference.getBooleanPref(
                            requireActivity(),
                            SharePreference.isLogin
                        )
                    ) {
                        if (trendingItemList[i].isFavorite == 1) {
                            callFavouritesApi(
                                "unfavorite",
                                "trending",
                                trendingItemList[i].id.toString(),
                                i
                            )
                        } else {
                            callFavouritesApi(
                                "favorite",
                                "trending",
                                trendingItemList[i].id.toString(),
                                i
                            )
                        }
                    } else {
                        openActivity(LoginActivity::class.java)
                        activity?.finish()
                    }
                } else if (s == Constants.AddToCart) {
                    if (SharePreference.getBooleanPref(
                            requireActivity(),
                            SharePreference.isLogin
                        )
                    ) {
                        trendingItemAddToCartFlow(i)

                    } else {
                        openActivity(LoginActivity::class.java)
                        activity?.finish()
                        activity?.finishAffinity()
                    }
                } else if (s == Constants.DeleteItem) {
                    if (SharePreference.getBooleanPref(
                            requireActivity(),
                            SharePreference.isLogin
                        )
                    ) {
                        dlgDeleteCartAlert()
                    } else {
                        openActivity(LoginActivity::class.java)
                        activity?.finish()
                        activity?.finishAffinity()
                    }

                }
            }
        binding.rvTrending.adapter = trendingItemAdapter
        binding.rvTrending.isNestedScrollingEnabled = true
    }


    private fun featuredItemAddToCartFlow(i: Int) {
        if (featuredList[i].hasVariation != 1 && featuredList[i].addons?.size == 0 || featuredList[i].variation?.size == 0 && featuredList[i].addons?.size == 0) {
            callAddToCartApi(
                featuredList[i].tax.toString(),
                featuredList[i].id.toString(),
                featuredList[i].itemType.toString(),
                featuredList[i].itemName.toString(),
                featuredList[i].imageName.toString(),
                featuredList[i].price?.toDouble().toString(),
                i,
                Constants.Featured
            )
        } else {
            val fragVariation = FragVariation()
            val bundle = Bundle()
            bundle.putParcelable("data", featuredList[i])
            bundle.putParcelableArrayList("variation", featuredList[i].variation)
            bundle.putParcelableArrayList("addons", featuredList[i].addons)
            bundle.putString("name", featuredList[i].itemName)
            bundle.putString("id", featuredList[i].id.toString())
            bundle.putString("itemImage", featuredList[i].imageName.toString())
            bundle.putString("itemPrice", featuredList[i].price.toString())
            bundle.putString("pos", i.toString())

            bundle.putString("itemType", featuredList[i].itemType.toString())
            bundle.putString("tax", featuredList[i].tax.toString())
            bundle.putString("from", Constants.Featured)

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
                val type=data?.getStringExtra(Constants.Type)
                val position= data?.getStringExtra("pos")!!.toInt()
                Log.e("type",type.toString())
                Log.e("position",position.toString())
                (requireActivity() as DashboardActivity).setCartCount()

                when (type) {
                    Constants.Featured -> {
                        featuredList[position].isCart = 1
                        featuredList[position].itemQty = featuredList[position].itemQty?.plus(1)

                        featuredadapter.notifyDataSetChanged()
                    }
                    Constants.Trending -> {
                        trendingItemList[position].isCart = 1
                        trendingItemList[position].itemQty = trendingItemList[position].itemQty?.plus(1)

                        trendingItemAdapter.notifyDataSetChanged()
                    }
                    Constants.Recommended ->
                    {
                        recommendedList[position].isCart = 1
                        recommendedList[position].itemQty = recommendedList[position].itemQty?.plus(1)

                        recommendedAdapter.notifyDataSetChanged()
                    }
                }


            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Message", e.message.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
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


    private fun trendingItemAddToCartFlow(i: Int) {
        if (trendingItemList[i].hasVariation != 1 && trendingItemList[i].addons?.size == 0 || trendingItemList[i].variation?.size == 0 && trendingItemList[i].addons?.size == 0) {
            callAddToCartApi(
                trendingItemList[i].tax.toString(),
                trendingItemList[i].id.toString(),
                trendingItemList[i].itemType.toString(),
                trendingItemList[i].itemName.toString(),
                trendingItemList[i].imageName.toString(),
                trendingItemList[i].price.toString(),
                i,
                Constants.Trending
            )
        } else {
            val fragVariation = FragVariation()
            val bundle = Bundle()
            bundle.putParcelableArrayList("variation", trendingItemList[i].variation)
            bundle.putParcelableArrayList("addons", trendingItemList[i].addons)
            bundle.putParcelable("data", trendingItemList[i])
            bundle.putString("pos", i.toString())

            bundle.putString("name", trendingItemList[i].itemName)
            bundle.putString("id", trendingItemList[i].id.toString())
            bundle.putString("itemImage", trendingItemList[i].imageName.toString())
            bundle.putString("itemPrice", trendingItemList[i].price.toString())
            bundle.putString("itemType", trendingItemList[i].itemType.toString())
            bundle.putString("tax", trendingItemList[i].tax.toString())
            bundle.putString("from", Constants.Trending)

            fragVariation.arguments = bundle
            fragVariation.show(requireActivity().supportFragmentManager, fragVariation.tag)
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
                    Common.dismissLoadingProgress()
                    if (response.body.status == 1) {
                        SharePreference.setStringPref(
                            requireActivity(),
                            SharePreference.BadgesCount,
                            response.body.cartCount.toString()
                        )
                        (requireActivity() as DashboardActivity).setCartCount()

                        if (type == Constants.Featured) {
                            featuredList[position].isCart = 1
                            featuredList[position].itemQty = featuredList[position].itemQty?.plus(1)

                            featuredadapter.notifyDataSetChanged()
                        } else if (type == Constants.Recommended) {
                            recommendedList[position].isCart = 1
                            recommendedList[position].itemQty =
                                recommendedList[position].itemQty?.plus(1)
                            recommendedAdapter.notifyDataSetChanged()
                        } else if (type == Constants.Trending) {
                            trendingItemList[position].isCart = 1
                            trendingItemList[position].itemQty =
                                trendingItemList[position].itemQty?.plus(1)

                            trendingItemAdapter.notifyDataSetChanged()
                        }

                    } else {
                        Common.showErrorFullMsg(
                            requireActivity(),
                            response.body.message.toString()
                        )
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

    private fun setupTestimonialAdapter() {
        testimonialAdapter = TestimonialAdapter(requireActivity(), reviewList)
        binding.rvTestimonials.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.rvTestimonials.adapter = testimonialAdapter
        binding.rvTestimonials.isNestedScrollingEnabled = true

        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvTestimonials)
    }

    private fun recommendedAdapterSetup() {
        binding.rvRecommended.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        recommendedAdapter =
            RecommendedAdapter(requireActivity(), recommendedList) { i: Int, s: String ->
                if (s == Constants.ItemClick) {
                    val intent = Intent(requireActivity(), ActFoodDetails::class.java)
                    intent.putExtra("foodItemId", recommendedList[i].id.toString())
                    startForResult.launch(intent)
                } else if (s == Constants.SelectVariationClick) {
                    if (SharePreference.getBooleanPref(
                            requireActivity(),
                            SharePreference.isLogin
                        )
                    ) {
                        recommendedItemAddToCart(i)
                    } else {
                        openActivity(LoginActivity::class.java)
                        activity?.finish()
                        activity?.finishAffinity()
                    }
                } else if (s == Constants.FavClick) {
                    if (SharePreference.getBooleanPref(
                            requireActivity(),
                            SharePreference.isLogin
                        )
                    ) {
                        if (recommendedList[i].isFavorite == 1) {
                            callFavouritesApi(
                                "unfavorite",
                                "recommended",
                                recommendedList[i].id.toString(),
                                i
                            )
                        } else {
                            callFavouritesApi(
                                "favorite",
                                "recommended",
                                recommendedList[i].id.toString(),
                                i
                            )
                        }
                    } else {
                        openActivity(LoginActivity::class.java)
                        activity?.finish()
                    }
                } else if (s == Constants.AddToCart) {
                    if (SharePreference.getBooleanPref(
                            requireActivity(),
                            SharePreference.isLogin
                        )
                    ) {
                        recommendedItemAddToCart(i)
                    } else {
                        openActivity(LoginActivity::class.java)
                        activity?.finish()
                        activity?.finishAffinity()
                    }
                } else if (s == Constants.DeleteItem) {
                    if (SharePreference.getBooleanPref(
                            requireActivity(),
                            SharePreference.isLogin
                        )
                    ) {
                        dlgDeleteCartAlert()
                    } else {
                        openActivity(LoginActivity::class.java)
                        activity?.finish()
                        activity?.finishAffinity()
                    }
                }
            }
        binding.rvRecommended.adapter = recommendedAdapter
        binding.rvRecommended.isNestedScrollingEnabled = true
    }


    private fun recommendedItemAddToCart(i: Int) {
        if (recommendedList[i].hasVariation != 1 && recommendedList[i].addons?.size == 0 || recommendedList[i].variation?.size == 0 && recommendedList[i].addons?.size == 0) {
            callAddToCartApi(
                recommendedList[i].tax.toString(),
                recommendedList[i].id.toString(),
                recommendedList[i].itemType.toString(),
                recommendedList[i].itemName.toString(),
                recommendedList[i].imageName.toString(),
                recommendedList[i].price.toString(), i, Constants.Recommended
            )
        } else {
            val fragVariation = FragVariation()
            val bundle = Bundle()
            bundle.putParcelableArrayList("variation", recommendedList[i].variation)
            bundle.putParcelable("data", recommendedList[i])

            bundle.putParcelableArrayList("addons", recommendedList[i].addons)
            bundle.putString("name", recommendedList[i].itemName)
            bundle.putString("pos", i.toString())
            bundle.putString("id", recommendedList[i].id.toString())
            bundle.putString("itemImage", recommendedList[i].imageName.toString())
            bundle.putString("itemPrice", recommendedList[i].price.toString())

            bundle.putString("itemType", recommendedList[i].itemType.toString())
            bundle.putString("tax", recommendedList[i].tax.toString())
            bundle.putString("from", Constants.Recommended)

            fragVariation.arguments = bundle
            fragVariation.show(
                requireActivity().supportFragmentManager,
                fragVariation.tag
            )
        }
    }


    private var startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

            } else {
                if (isAdded) {
                    if (isCheckNetwork(requireActivity())) {
                        callHomeFeedApi()

                    } else {
                        Common.showErrorFullMsg(
                            requireActivity(),
                            resources.getString(R.string.no_internet)
                        )
                    }
                }
            }
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

                (requireActivity() as DashboardActivity).setFragment(3)
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

    //TODO Favourites Api calling
    @SuppressLint("NotifyDataSetChanged")
    private fun callFavouritesApi(
        favType: String,
        type: String,
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
                                when (type) {
                                    "trending" -> {
                                        trendingItemList[position].isFavorite = 1
                                        trendingItemAdapter.notifyDataSetChanged()
                                    }
                                    "featured" -> {
                                        featuredList[position].isFavorite = 1
                                        featuredadapter.notifyDataSetChanged()
                                    }
                                    else -> {
                                        recommendedList[position].isFavorite = 1
                                        recommendedAdapter.notifyDataSetChanged()
                                    }
                                }
                            } else {
                                when (type) {
                                    "trending" -> {
                                        trendingItemList[position].isFavorite = 0
                                        trendingItemAdapter.notifyDataSetChanged()
                                    }
                                    "featured" -> {
                                        featuredList[position].isFavorite = 0
                                        featuredadapter.notifyDataSetChanged()
                                    }
                                    else -> {
                                        recommendedList[position].isFavorite = 0
                                        recommendedAdapter.notifyDataSetChanged()
                                    }
                                }

                            }
                        } else {
                            Common.showErrorFullMsg(requireActivity(), response.body.message.toString().replace("\\n", System.lineSeparator()))
                        }
                    }
                }

                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(requireActivity(), response.body.message.toString().replace("\\n", System.lineSeparator()))
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(requireActivity(), resources.getString(R.string.no_internet))
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(requireActivity(), resources.getString(R.string.error_msg))
                }
            }
        }
    }

    override fun getBinding(): FragmentHomeBinding {

        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding
    }
}