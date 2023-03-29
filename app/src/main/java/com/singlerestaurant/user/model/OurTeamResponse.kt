package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class OurTeamResponse(

	@field:SerializedName("data")
	val data: ArrayList<TeamData>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class TeamData(

	@field:SerializedName("insta")
	val insta: String? = null,

	@field:SerializedName("youtube")
	val youtube: String? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("subtitle")
	val subtitle: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("title")
	val title: String? = null,

	@field:SerializedName("fb")
	val fb: String? = null
)
