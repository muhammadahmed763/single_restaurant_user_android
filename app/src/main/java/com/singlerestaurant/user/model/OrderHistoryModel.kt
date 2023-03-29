package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName


data class OrderHistoryModel(

        @field:SerializedName("date")
        val date: String? = null,

        @field:SerializedName("transaction_type")
        val paymentType: String? = null,

        @field:SerializedName("grand_total")
        val totalPrice: String? = null,

        @field:SerializedName("order_number")
        val orderNumber: String? = null,

        @field:SerializedName("id")
        val id: Int? = null,

        @field:SerializedName("order_type")
        val orderType: String? = null,

        @field:SerializedName("users")
        val users: Any? = null,

        @field:SerializedName("status")
        val status: String? = null
)

