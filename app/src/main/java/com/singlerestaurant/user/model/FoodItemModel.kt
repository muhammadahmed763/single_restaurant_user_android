package com.singlerestaurant.user.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


data class FoodItemResponseModel(

	@field:SerializedName("first_page_url")
	val firstPageUrl: String? = null,

	@field:SerializedName("path")
	val path: String? = null,

	@field:SerializedName("per_page")
	val perPage: Int? = null,

	@field:SerializedName("total")
	val total: Int? = null,

	@field:SerializedName("data")
	val data: List<FoodItemModel>? = null,

	@field:SerializedName("last_page")
	val lastPage: Int? = null,

	@field:SerializedName("last_page_url")
	val lastPageUrl: String? = null,

	@field:SerializedName("next_page_url")
	val nextPageUrl: Any? = null,

	@field:SerializedName("from")
	val from: Int? = null,

	@field:SerializedName("to")
	val to: Int? = null,

	@field:SerializedName("prev_page_url")
	val prevPageUrl: Any? = null,

	@field:SerializedName("current_page")
	val currentPage: Int? = null
)

data class FoodItemModel(

	@field:SerializedName("itemimage")
	var itemimage: ItemImage? = null,

	@field:SerializedName("is_favorite")
	var isFavorite: String? = null,

	@field:SerializedName("item_name")
	var itemName: String? = null,
	@field:SerializedName("item_name_ar")
	var itemNameAr: String? = null,

	@field:SerializedName("id")
	var id: String? = null,

	@field:SerializedName("variation")
	var variation: List<VariationItem>? = null
)


/*
data class VariationItem(

	@field:SerializedName("item_id")
	val itemId: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("product_price")
	var productPrice: String? = null,

	@field:SerializedName("sale_price")
	val salePrice: Int? = null,

	@field:SerializedName("variation")
	val variation: String? = null,

	var isSelect:Boolean?=false
)*/
