package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class FaqResponse(
	@field:SerializedName("data")
	val data: ArrayList<FaqData>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class FaqData(

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("title")
	val title: String? = null
)
