package com.singlerestaurant.user.model

import com.google.gson.annotations.SerializedName

data class EditProfileResponse(

	@field:SerializedName("data")
	val data: getprofiledata? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)

data class Data(

	@field:SerializedName("profile_image")
	val profileImage: String? = null,

	@field:SerializedName("login_type")
	val loginType: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("mobile")
	val mobile: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("email")
	val email: String? = null
)
