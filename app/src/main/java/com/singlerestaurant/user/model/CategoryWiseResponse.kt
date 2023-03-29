package com.singlerestaurant.user.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class CategoryWiseResponse(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("items")
	val items: ArrayList<ItemsItem>? = null,

	@field:SerializedName("status")
	val status: Int? = null
)
@Parcelize
data class SubcategoryItemsItem(

	@field:SerializedName("is_cart")
	var isCart: String? = null,

	@field:SerializedName("category_info")
	val categoryInfo: CategoryInfo? = null,

	@field:SerializedName("is_favorite")
	var isFavorite: String? = null,

	@field:SerializedName("addons")
	val addons: ArrayList<AddOnsDataModel>? = null,

	@field:SerializedName("item_type")
	val itemType: String? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("item_name")
	val itemName: String? = null,

	@field:SerializedName("tax")
	val tax: String? = null,

	@field:SerializedName("has_variation")
	val hasVariation: String? = null,

	@field:SerializedName("subcategory_info")
	val subcategoryInfo: SubcategoryInfo? = null,

	@field:SerializedName("variation")
	val variation: ArrayList<VariationItem>? = null,

	@field:SerializedName("image_name")
	val imageName: String? = null,

	@field:SerializedName("price")
	val price: String? = null,

	@field:SerializedName("preparation_time")
	val preparationTime: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("attribute")
	val attribute: String? = null,

	@field:SerializedName("item_description")
	val itemDescription: String? = null,

	@field:SerializedName("item_qty")
	var itemQty: Int? = null
):Parcelable

data class ItemsItem(
	@field:SerializedName("cat_id")
	val catId: String? = null,

	@field:SerializedName("subcategory_name")
	val subcategoryName: String? = null,

	@field:SerializedName("subcategory_items")
	val subcategoryItems: ArrayList<SubcategoryItemsItem>? = null,

	@field:SerializedName("id")
	val id: String? = null
)

