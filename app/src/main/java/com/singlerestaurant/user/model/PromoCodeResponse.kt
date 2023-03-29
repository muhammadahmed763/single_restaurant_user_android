package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class PromoCodeResponse(

	@field:SerializedName("data")
	val data: ArrayList<PromoCodeData>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class PromoCodeData(

	@field:SerializedName("offer_amount")
	val offerAmount: String? = null,

	@field:SerializedName("expire_date")
	val expireDate: String? = null,

	@field:SerializedName("usage_type")
	val usageType: String? = null,

	@field:SerializedName("min_amount")
	val minAmount: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("offer_code")
	val offerCode: String? = null,

	@field:SerializedName("offer_name")
	val offerName: String? = null,

	@field:SerializedName("offer_type")
	val offerType: String? = null,

	@field:SerializedName("per_user")
	val perUser: String? = null,

	@field:SerializedName("start_date")
	val startDate: String? = null
)
