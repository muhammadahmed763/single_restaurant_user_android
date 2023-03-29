package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class TutorialResponse(

	@field:SerializedName("data")
	val data: ArrayList<TutorialData>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class TutorialData(

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("title")
	val title: String? = null
)
