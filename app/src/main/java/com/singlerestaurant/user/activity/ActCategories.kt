package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.base.BaseAdaptor
import com.singlerestaurant.user.databinding.ActCategoriesBinding
import com.singlerestaurant.user.model.CategoriesItem
import com.singlerestaurant.user.utils.SharePreference

class ActCategories : BaseActivity() {
    private lateinit var binding:ActCategoriesBinding
    override fun setLayout(): View = binding.root

    override fun InitView() {
        binding= ActCategoriesBinding.inflate(layoutInflater)
        val categoryList = intent.getParcelableArrayListExtra<CategoriesItem>("categoryArray") as ArrayList<CategoriesItem>
        setFoodCategoryAdaptor(categoryList)
        binding.ivBack.setOnClickListener { finish() }

        if (SharePreference.getStringPref(
                this@ActCategories,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))) {

            binding.ivBack.rotation=180f
        }else{
            binding.  ivBack.rotation=0f

        }
    }


    private fun setFoodCategoryAdaptor(foodCategoryList: ArrayList<CategoriesItem>) {
        val foodCategoryAdapter =
            object : BaseAdaptor<CategoriesItem>(this@ActCategories, foodCategoryList) {
                @SuppressLint("ResourceType")
                override fun onBindData(
                    holder: RecyclerView.ViewHolder?,
                    `val`: CategoriesItem,
                    position: Int
                ) {
                    val tvFoodCategoryName: TextView = holder!!.itemView.findViewById(R.id.tvCategorie)
                    val ivFoodCategory: ImageView = holder.itemView.findViewById(R.id.ivCategories)

                    tvFoodCategoryName.text = foodCategoryList[position].categoryName
                    Glide.with(this@ActCategories).load(foodCategoryList[position].image)
                        .apply(RequestOptions.circleCropTransform())
                        .transition(DrawableTransitionOptions.withCrossFade(500)).into(ivFoodCategory)
                    holder.itemView.setOnClickListener {
                        startActivity(Intent(this@ActCategories, ActSubCategoryProducts::class.java).putExtra("category_name", foodCategoryList[position].categoryName).putExtra("category_id", foodCategoryList[position].id.toString()))
                    }
                }

                override fun setItemLayout(): Int {
                    return R.layout.row_foodoutcategory
                }

            }

        binding.rvFoodCategory.apply {
            adapter = foodCategoryAdapter
            layoutManager = GridLayoutManager(this@ActCategories, 3, GridLayoutManager.VERTICAL, false)
            itemAnimator = DefaultItemAnimator()
            isNestedScrollingEnabled = true
        }

    }
}