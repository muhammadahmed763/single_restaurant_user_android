package com.singlerestaurant.user.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.singlerestaurant.user.R
import com.singlerestaurant.user.activity.DashboardActivity
import com.singlerestaurant.user.adaptor.AddOnsListAdapter
import com.singlerestaurant.user.adaptor.VariationListAdapter
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.FragVariationBinding
import com.singlerestaurant.user.model.AddOnsDataModel
import com.singlerestaurant.user.model.FeatureditemsItem
import com.singlerestaurant.user.model.VariationItem
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class FragVariation : BottomSheetDialogFragment() {
    private var variationList = ArrayList<VariationItem>()
    private var addonsList = ArrayList<AddOnsDataModel>()
    private var variationId=""
    private var variationPrice="0.00"
    private var variationName=""
    private lateinit var binding:FragVariationBinding
    private var productData= FeatureditemsItem()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragVariationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        variationList = arguments?.getParcelableArrayList<VariationItem>("variation") as ArrayList<VariationItem>
        addonsList = arguments?.getParcelableArrayList<AddOnsDataModel>("addons") as ArrayList<AddOnsDataModel>
        binding.tvItemName.text = arguments?.getString("name")
        Log.e("VariationList",Gson().toJson(variationList))
        Log.e("AddonsList",Gson().toJson(addonsList))
        if(variationList.size==0)
        {
            binding.tvVariationTitle.visibility=View.GONE
            binding.rvVariation.visibility=View.GONE
        }else
        {
            variationList[0].isSelect = true

        }


        if(addonsList.size==0)
        {
            binding.tvAddOnsTitle.visibility=View.GONE
            binding.rvAddOns.visibility=View.GONE
        }



        initClickListeners()
        setupAdapter()
    }

    private fun initClickListeners() {
        binding.ivClose.setOnClickListener {
            dialog?.dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dialog?.dismiss()
        }

        binding.tvContinue.setOnClickListener {
            getVariation()
            if(variationList.size!=0 && addonsList.size!=0)
            {
                val addOnsID = TextUtils.join(",", getAddOnsId())
                val addOnsName = TextUtils.join(",", getAddOnsName())
                val addOnsPrice = TextUtils.join(",", getAddOnsPrice())
                val addOnsTotal = getAddOnsPrice().map { it.toDouble() }.toTypedArray().sum()

                callAddToCartApi(variationId,variationName,variationPrice, addOnsID, addOnsName, addOnsPrice,addOnsTotal.toString())

            }else if(variationList.size==0 &&addonsList.size!=0){
                val addOnsID = TextUtils.join(",", getAddOnsId())
                val addOnsName = TextUtils.join(",", getAddOnsName())
                val addOnsPrice = TextUtils.join(",", getAddOnsPrice())
                val addOnsTotal = getAddOnsPrice().map { it.toDouble() }.toTypedArray().sum()
                val itemPrice=arguments?.getString("itemPrice")?.toDouble()
                callAddToCartApi(variationId,variationName,itemPrice.toString(), addOnsID, addOnsName, addOnsPrice,addOnsTotal.toString())

            }else if(variationList.size!=0 &&addonsList.size==0)
            {
                callAddToCartApi(variationId,variationName,variationPrice, "", "", "0","0")
            }



        }

    }

    private fun getVariation() {
        for (i in 0 until variationList.size) {
            if (variationList[i].isSelect == true) {

                variationId=variationList[i].id.toString()
                variationName=variationList[i].variation.toString()
                variationPrice=variationList[i].productPrice?.toDouble().toString()
            }
        }

    }



    override fun onDetach() {
        super.onDetach()
        for (i in 0 until variationList.size) {
           variationList[i].isSelect=false
        }
        for (i in 0 until addonsList.size) {
            addonsList[i].isAddOnsSelect=false
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


    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun callAddToCartApi(variationId:String, variation:String,itemPrice:String,addOnsId: String, addOnsName: String, addOnsPrice: String,addOnsTotalPrice: String) {
        Common.showLoadingProgress(requireActivity())
        val map = HashMap<String, String>()
        map["user_id"] =
            SharePreference.getStringPref(requireActivity(), SharePreference.userId) ?: ""
        map["item_id"] = arguments?.getString("id") ?: ""
        map["item_image"] = arguments?.getString("itemImage") ?: ""
        map["item_type"] = arguments?.getString("itemType") ?: ""
        map["item_name"] = arguments?.getString("name") ?: ""
        map["tax"] = arguments?.getString("tax") ?: ""
        map["item_price"] = String.format(Locale.US, "%.02f", itemPrice.toDouble())
        map["variation_id"] = variationId.toString()
        map["variation"] = variation
        map["addons_id"] = addOnsId
        map["addons_price"] =  addOnsPrice
        map["addons_name"] = addOnsName
        map["addons_total_price"] = String.format(Locale.US, "%.02f", addOnsTotalPrice.toDouble())


        Log.d("request", Gson().toJson(map))
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().addToCart(map)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    dialog?.dismiss()
                    if(response.body.status==1)
                    {
                        SharePreference.setStringPref(requireActivity(),SharePreference.BadgesCount,response.body.cartCount.toString())

                        if(arguments?.getString("from")==Constants.Home)
                        {
                            (requireActivity() as DashboardActivity).replaceFragment(HomeFragment())

                        }else if(arguments?.getString("from")==Constants.Favourites)
                        {
                            val broadCastIntent = Intent("refreshData")
                            broadCastIntent.putExtra("pos",arguments?.getString("pos"))
                            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(broadCastIntent)

                        }else if(arguments?.getString("from")==Constants.SubCategory)
                        {
                            val broadCastIntent = Intent("refreshData")
                            broadCastIntent.putExtra("pos",arguments?.getString("pos"))

                            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(broadCastIntent)
                            dialog?.dismiss()
                        }else if(arguments?.getString("from")==Constants.Trending)
                        {
                            val broadCastIntent = Intent("refreshData")
                            broadCastIntent.putExtra(Constants.Type,Constants.Trending)
                            broadCastIntent.putExtra("pos",arguments?.getString("pos"))

                            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(broadCastIntent)
                            dialog?.dismiss()
                        }else if(arguments?.getString("from")==Constants.Featured)
                        {
                            val broadCastIntent = Intent("refreshData")
                            broadCastIntent.putExtra(Constants.Type,Constants.Featured)
                            broadCastIntent.putExtra("pos",arguments?.getString("pos"))

                            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(broadCastIntent)
                            dialog?.dismiss()
                        } else if(arguments?.getString("from")==Constants.Recommended)
                        {
                            val broadCastIntent = Intent("refreshData")
                            broadCastIntent.putExtra(Constants.Type,Constants.Recommended)
                            broadCastIntent.putExtra("pos",arguments?.getString("pos"))
                            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(broadCastIntent)
                            dialog?.dismiss()
                        }

                        else if(arguments?.getString("from")==Constants.FoodDetail)
                        {
                            val broadCastIntent = Intent("refreshData")
                            broadCastIntent.putExtra("pos",arguments?.getString("pos"))
                            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(broadCastIntent)
                            dialog?.dismiss()
                        }else if(arguments?.getString("from")==Constants.ViewAll)
                        {
                            val broadCastIntent = Intent("refreshData")
                            broadCastIntent.putExtra("pos",arguments?.getString("pos"))

                            LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(broadCastIntent)
                            dialog?.dismiss()
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

    private fun setupAdapter() {
        binding.rvVariation.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = VariationListAdapter(
                requireActivity(),
                variationList
            ) { id: Int, name:String ->

            }
            isNestedScrollingEnabled=true

        }


        binding.rvAddOns?.apply {
            layoutManager = LinearLayoutManager(requireActivity())
            adapter = AddOnsListAdapter(
                requireActivity(),
                addonsList)
            isNestedScrollingEnabled=true
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            setupFullHeight(bottomSheetDialog)
        }
        return dialog
    }


    private fun setupFullHeight(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(view?.parent as View)
        val layoutParams = bottomSheet!!.layoutParams
        val windowHeight = getWindowHeight()
        if (layoutParams != null) {
            layoutParams.height = windowHeight
        }
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onStart() {
        super.onStart()
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    private fun getWindowHeight(): Int {
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }


}