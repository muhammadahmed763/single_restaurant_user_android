package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.databinding.RowFeaturedBinding
import com.singlerestaurant.user.model.RecommendeditemsItem
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference

class RecommendedAdapter(
    var context: Activity,
    private val recommendedList: ArrayList<RecommendeditemsItem>,
    private val itemClick: (Int, String) -> Unit

) :
    RecyclerView.Adapter<RecommendedAdapter.RecommendedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendedViewHolder {
        val view =RowFeaturedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecommendedViewHolder(view)
    }

    inner class RecommendedViewHolder(private val binding: RowFeaturedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItems(
            context: Activity,
            data: RecommendeditemsItem,
            itemClick: (Int, String) -> Unit,
            position: Int
        ) = with(binding) {
            val currency =
                SharePreference.getStringPref(context, SharePreference.isCurrancy)
                    ?: ""
            val currencyPos = SharePreference.getStringPref(
                context,
                SharePreference.CurrencyPosition
            ) ?: ""
            binding.tvItemName.text = data.itemName
            // binding.tvRatePro.text = data.rattingsAverage?.toDouble().toString()
            Glide.with(context).load(data.imageUrl).transition(DrawableTransitionOptions.withCrossFade(500)).into(binding.ivFood)
            if (data.hasVariation == 1&&data.price==0) {
                binding.tvFoodPriceGrid.text =
                    Common.getPrices(
                        currencyPos,
                        currency,
                        data.variation?.get(0)?.productPrice.toString()
                    )

            } else {
                binding.tvFoodPriceGrid.text =
                    Common.getPrices(currencyPos, currency, data.price.toString())
            }
            binding.tvStarter.text = data.categoryInfo?.categoryName

            if (data.itemType == 2) {
                binding.ivveg.setImageResource(R.drawable.ic_nonveg)
            } else {
                binding.ivveg.setImageResource(R.drawable.ic_vegetarian)
            }

            /*binding.tvFoodPriceGrid.text =
                Common.getPrices(currencyPos, currency, data.price.toString() ?: "0.00")*/
            if (data.isFavorite == 1) {
                binding.ivwishlist.background =
                    ResourcesCompat.getDrawable(context.resources, R.drawable.ic_fav, null)
                binding.ivwishlist.imageTintList = ColorStateList.valueOf(Color.WHITE)

            } else {
                binding.ivwishlist.background = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_unfav, null)
                binding.ivwishlist.imageTintList = ColorStateList.valueOf(Color.WHITE)

            }

            if (data.isCart == 1) {
                binding.clQtyUpdate.visibility = View.VISIBLE
                binding.btnAdd.visibility = View.GONE

                binding.tvFoodQty.text=data.itemQty.toString()

            } else if (data.isCart == 0) {
                binding.clQtyUpdate.visibility = View.GONE
                binding.btnAdd.visibility = View.VISIBLE
            }

            binding.ivwishlist.setOnClickListener {
                itemClick(position, Constants.FavClick)
            }
            binding.btnAdd.setOnClickListener {
                itemClick(position, Constants.SelectVariationClick)

            }
            binding.ivPlus.setOnClickListener {
                itemClick(position, Constants.AddToCart)

            }
            binding.ivMinus.setOnClickListener {
                itemClick(position, Constants.DeleteItem)

            }

            itemView.setOnClickListener {
                itemClick(position, Constants.ItemClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecommendedViewHolder, position: Int) {
        holder.bindItems(context, recommendedList[position], itemClick, position)
    }

    override fun getItemCount(): Int {
        return recommendedList.size
    }
}