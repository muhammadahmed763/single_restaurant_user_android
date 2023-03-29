package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class PaymentListModel(

    @field:SerializedName("total_wallet")
    val totalWallet: String? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("paymentmethods")
    val paymentmethods: ArrayList<PaymentmethodsItem>? = null,

    @field:SerializedName("status")
    val status: Int? = null
)

data class PaymentmethodsItem(

    @field:SerializedName("environment")
    var environment: Int? = null,

    @field:SerializedName("public_key")
    var publicKey: String? = null,

    @field:SerializedName("encryption_key")
    val encryptionKey: String? = null,

    @field:SerializedName("payment_name")
    var paymentName: String? = null,

    @field:SerializedName("image")
    var image: String? = null,

    @field:SerializedName("currency")
    var currency: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    var isSelect: Boolean = false
)
