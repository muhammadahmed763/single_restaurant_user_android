package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class WalletTransactionResponse(

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("transactions")
	val transactions: ArrayList<TransactionsItem>? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class TransactionsItem(

	@field:SerializedName("transaction_id")
	val transactionId: String? = null,

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("amount")
	val amount: String? = null,

	@field:SerializedName("user_id")
	val userId: String? = null,

	@field:SerializedName("order_number")
	val orderNumber: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("transaction_type")
	val transactionType: Int? = null,

	@field:SerializedName("order_id")
	val orderId: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)
