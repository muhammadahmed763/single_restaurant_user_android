package com.singlerestaurant.user.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class FavouriteListResponse(

	@field:SerializedName("data")
	val data: ArrayList<FevData>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)



data class FevData(

	@field:SerializedName("is_cart")
	var isCart: Int? = null,

	@field:SerializedName("category_info")
	val categoryInfo: CategoryInfo? = null,

	@field:SerializedName("is_favorite")
	val isFavorite: Int? = null,

	@field:SerializedName("addons")
	val addons: ArrayList<AddOnsDataModel>? = null,

	@field:SerializedName("item_type")
	val itemType: Int? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("item_name")
	val itemName: String? = null,

	@field:SerializedName("tax")
	val tax: String? = null,

	@field:SerializedName("has_variation")
	val hasVariation: Int? = null,

	@field:SerializedName("subcategory_info")
	val subcategoryInfo: SubcategoryInfo? = null,

	@field:SerializedName("variation")
	val variation: ArrayList<VariationItem>? = null,

	@field:SerializedName("image_name")
	val imageName: String? = null,

	@field:SerializedName("price")
	val price: Int? = null,

	@field:SerializedName("preparation_time")
	val preparationTime: String? = null,

	@field:SerializedName("favorite_id")
	val favoriteId: Int? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("attribute")
	val attribute: String? = null,

	@field:SerializedName("item_description")
	val itemDescription: String? = null,

	@field:SerializedName("item_qty")
	var itemQty: Int? = null
)
