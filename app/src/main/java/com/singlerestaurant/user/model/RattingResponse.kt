package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class RattingResponse(

	@field:SerializedName("data")
	val data: ArrayList<RattingDataItem>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class RattingDataItem(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("profile_image")
	val profileImage: String? = null,

	@field:SerializedName("user_id")
	val userId: String? = null,

	@field:SerializedName("ratting")
	val ratting: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("comment")
	val comment: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
