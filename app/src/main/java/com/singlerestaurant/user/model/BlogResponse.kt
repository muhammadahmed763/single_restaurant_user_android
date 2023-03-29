package com.singlerestaurant.user.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class BlogResponse(

	@field:SerializedName("data")
	val data: ArrayList<BlogsData>? = null,

	@field:SerializedName("message")
	val message: String? = null,

	@field:SerializedName("status")
	val status: Int? = null
)
@Parcelize
data class BlogsData(

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("title")
	val title: String? = null,
	@field:SerializedName("date")
	val date: String? = null
):Parcelable
