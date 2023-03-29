package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class GalleryResponse(

	@field:SerializedName("data")
	val data: ArrayList<ItemImage>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class GalleryData(

	@field:SerializedName("image_url")
	val imageUrl: String? = null
)
