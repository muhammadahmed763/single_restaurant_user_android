package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.databinding.RowFoodsubcategoryBinding
import com.singlerestaurant.user.model.SearchItemModel
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference

class ViewAllDataAdapter(
    var context: Activity,
    private val productList: ArrayList<SearchItemModel>,
    private val itemClick: (Int, String) -> Unit
):RecyclerView.Adapter<ViewAllDataAdapter.ViewAllViewHolder>()  {

    inner class ViewAllViewHolder(val binding: RowFoodsubcategoryBinding):RecyclerView.ViewHolder(binding.root)
    {
        fun bindItems(
            context: Activity,
            data: SearchItemModel,
            itemClick: (Int, String) -> Unit,
            position: Int
        ) = with(binding) {
            val currency = SharePreference.getStringPref(context, SharePreference.isCurrancy) ?: ""
            val currencyPos =
                SharePreference.getStringPref(context, SharePreference.CurrencyPosition) ?: ""
            binding.tvItemName.text = data.itemName
            Glide.with(context).load(data.imageUrl)
                .transition(DrawableTransitionOptions.withCrossFade(500)).into(binding.ivFood)
            if (data.hasVariation == "1" && data.price == 0) {
                binding.tvFoodPriceGrid.text =
                    Common.getPrices(
                        currencyPos,
                        currency,
                        data.variation?.get(0)?.productPrice.toString() ?: "0.00"
                    )

            } else {
                binding.tvFoodPriceGrid.text =
                    Common.getPrices(currencyPos, currency, data.price.toString() ?: "0.00")
            }
            binding.tvStarter.text = data.categoryInfo?.categoryName
            if (data.itemType == "2") {
                binding.ivveg.setImageResource(R.drawable.ic_nonveg)
            } else {
                binding.ivveg.setImageResource(R.drawable.ic_vegetarian)
            }

            if (data.isFavorite == 1) {
                binding.ivwishlist.background =
                    androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, R.drawable.ic_fav, null)
                binding.ivwishlist.imageTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.WHITE)

            } else {
                binding.ivwishlist.background =
                    androidx.core.content.res.ResourcesCompat.getDrawable(context.resources, R.drawable.ic_unfav, null)
                binding.ivwishlist.imageTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.WHITE)

            }
            if (data.isCart == 1) {
                binding.clQtyUpdate.visibility = android.view.View.VISIBLE
                binding.btnAdd.visibility = android.view.View.GONE

                binding.tvFoodQty.text = data.itemQty.toString()

            } else if (data.isCart == 0) {
                binding.clQtyUpdate.visibility = android.view.View.GONE
                binding.btnAdd.visibility = android.view.View.VISIBLE
            }

            binding.btnAdd.setOnClickListener {
                itemClick(position, com.singlerestaurant.user.utils.Constants.SelectVariationClick)
            }
            binding.ivPlus.setOnClickListener {
                itemClick(position, com.singlerestaurant.user.utils.Constants.AddToCart)

            }
            binding.ivMinus.setOnClickListener {
                itemClick(position, com.singlerestaurant.user.utils.Constants.DeleteItem)

            }

            binding.ivwishlist.setOnClickListener {
                itemClick(position, com.singlerestaurant.user.utils.Constants.FavClick)
            }
            itemView.setOnClickListener {
                itemClick(position, com.singlerestaurant.user.utils.Constants.ItemClick)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewAllViewHolder {
    val view=RowFoodsubcategoryBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewAllViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewAllViewHolder, position: Int) {
        holder.bindItems(context,productList[position],itemClick,position)
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}