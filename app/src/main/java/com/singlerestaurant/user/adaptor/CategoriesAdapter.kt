package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.singlerestaurant.user.databinding.RowFoodcategoryBinding
import com.singlerestaurant.user.model.CategoriesItem
import com.singlerestaurant.user.utils.Constants

class CategoriesAdapter(
    var context: Activity,
    private val categoriesList: ArrayList<CategoriesItem>,
    private val itemClick: (Int, String) -> Unit
) :
    RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val view = RowFoodcategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoriesViewHolder(view)
    }

    inner class CategoriesViewHolder(private val binding: RowFoodcategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItems(
            context: Activity,
            data: CategoriesItem,
            itemClick: (Int, String) -> Unit,
            position: Int
        ) = with(binding) {
            Glide.with(context).load(data.image).apply(RequestOptions.circleCropTransform())
                .transition(DrawableTransitionOptions.withCrossFade(500)).into(binding.ivCategories)
            binding.tvCategorie.text = data.categoryName
            itemView.setOnClickListener {
                itemClick(position, Constants.ItemClick)
            }
        }
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        holder.bindItems(context, categoriesList[position], itemClick, position)
    }

    override fun getItemCount(): Int {
        return categoriesList.size
    }
}