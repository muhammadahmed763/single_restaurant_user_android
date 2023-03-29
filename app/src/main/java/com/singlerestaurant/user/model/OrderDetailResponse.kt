package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class OrderDetailResponse(

	@field:SerializedName("data")
	val data: ArrayList<OrderDetailModel>? = null,

	@field:SerializedName("driver_info")
	val driverInfo: DriverInfo? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("summery")
	val summery: Summery? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class DriverInfo(
	@field:SerializedName("id")
	val id: String? = null,
	@field:SerializedName("name")
	val name: String? = null,
	@field:SerializedName("email")
	val email: String? = null,
	@field:SerializedName("mobile")
	val mobile: String? = null,
	@field:SerializedName("token")
	val token: String? = null,
	@field:SerializedName("profile_image")
	val profile_image: String? = null
)
data class Summery(

	@field:SerializedName("area")
	val area: String? = null,

	@field:SerializedName("transaction_id")
	val transactionId: String? = null,

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("address")
	val address: String? = null,

	@field:SerializedName("order_notes")
	val orderNotes: String? = null,

	@field:SerializedName("discount_amount")
	val discountAmount: String? = null,

	@field:SerializedName("order_number")
	val orderNumber: String? = null,

	@field:SerializedName("tax")
	val tax: String? = null,

	@field:SerializedName("offer_code")
	val offerCode: String? = null,

	@field:SerializedName("transaction_type")
	val transactionType: String? = null,

	@field:SerializedName("delivery_charge")
	val deliveryCharge: String? = null,

	@field:SerializedName("house_no")
	val houseNo: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("grand_total")
	val grandTotal: String? = null,

	@field:SerializedName("order_total")
	val orderTotal: String? = null,

	@field:SerializedName("order_type")
	val orderType: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class OrderDetailModel(

	@field:SerializedName("total_price")
	val totalPrice: Int? = null,

	@field:SerializedName("item_id")
	val itemId: Int? = null,

	@field:SerializedName("item_type")
	val itemType: Int? = null,

	@field:SerializedName("item_price")
	val itemPrice: String? = null,

	@field:SerializedName("addons_id")
	val addonsId: String? = null,

	@field:SerializedName("item_name")
	val itemName: String? = null,

	@field:SerializedName("variation")
	val variation: String? = null,

	@field:SerializedName("addons_price")
	val addonsPrice: String? = null,

	@field:SerializedName("addons_total_price")
	val addons_total_price: String? = null,



	@field:SerializedName("variation_id")
	val variationId: String? = null,

	@field:SerializedName("qty")
	val qty: String? = null,

	@field:SerializedName("item_image")
	val itemImage: String? = null,

	@field:SerializedName("addons_name")
	val addonsName: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
