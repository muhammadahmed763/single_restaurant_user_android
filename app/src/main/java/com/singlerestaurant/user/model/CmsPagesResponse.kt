package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class CmsPagesResponse(

	@field:SerializedName("privacypolicy")
	val privacypolicy: String? = null,

	@field:SerializedName("termscondition")
	val termscondition: String? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)
