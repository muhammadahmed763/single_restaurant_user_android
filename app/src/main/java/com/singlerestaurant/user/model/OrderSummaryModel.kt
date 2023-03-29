package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class OrderSummaryModel(

        @field:SerializedName("total_price")
        val totalPrice: String? = null,

        @field:SerializedName("item_id")
        val itemId: String? = null,

        @field:SerializedName("item_price")
        val itemPrice: String? = null,

        @field:SerializedName("addons_price")
        val addonsPrice: String? = null,

        @field:SerializedName("addons_total_price")
        val addons_total_price: String? = null,


        @field:SerializedName("qty")
        val qty: String? = null,

        @field:SerializedName("addons_id")
        val addonsId: String? = null,

        @field:SerializedName("addons_name")
        val addonsName: String? = null,

        @field:SerializedName("item_image")
        val itemImage: Any? = null,

        @field:SerializedName("item_name")
        val itemName: String? = null,
        @field:SerializedName("variation")
        val variation: String? = null,

        @field:SerializedName("item_type")
        val itemType: Int? = null,


        @field:SerializedName("id")
        val id: Int? = null,

        @field:SerializedName("item_notes")
        val itemNotes: String? = null
)
