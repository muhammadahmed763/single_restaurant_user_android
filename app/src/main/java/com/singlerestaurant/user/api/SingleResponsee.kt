package com.singlerestaurant.user.api

import com.google.gson.annotations.SerializedName

data class SingleResponsee(
	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("total_wallet")
	val totalWallet: String? = null,

	@field:SerializedName("cart_count")
	val cartCount: String? = null,

	@field:SerializedName("otp")
	val otp: String? = null,
	@field:SerializedName("order_id")
	val orderId: String? = null,
	@field:SerializedName("is_cart_empty")
	val isEmptyCart: String? = null,
	@field:SerializedName("status")
	val status: Int? = null,

)
