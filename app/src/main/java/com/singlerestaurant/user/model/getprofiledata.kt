package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class getprofiledata(

	@field:SerializedName("profile_image")
	val profileImage: String? = null,

	@field:SerializedName("wallet")
	val wallet: String? = null,
	@field:SerializedName("referral_code")
	val referral_code: String? = null,

	@field:SerializedName("login_type")
	val loginType: String? = null,

	@field:SerializedName("is_mail")
	val isMail: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("mobile")
	val mobile: String? = null,

	@field:SerializedName("is_notification")
	val isNotification: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("email")
	val email: String? = null
)
