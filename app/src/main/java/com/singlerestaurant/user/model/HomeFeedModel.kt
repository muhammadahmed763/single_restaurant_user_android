package com.singlerestaurant.user.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

data class HomeFeedModel(

    @field:SerializedName("cartdata")
    val cartdata: Cartdata? = null,

    @field:SerializedName("trendingitems")
    val trendingitems: ArrayList<FeatureditemsItem>? = null,

    @field:SerializedName("todayspecial")
    val todayspecial: ArrayList<FeatureditemsItem>? = null,

    @field:SerializedName("checkaddons")
    val checkaddons: String? = null,

    @field:SerializedName("recommendeditems")
    val recommendeditems: ArrayList<RecommendeditemsItem>? = null,

    @field:SerializedName("categories")
    val categories: ArrayList<CategoriesItem>? = null,

    @field:SerializedName("testimonials")
    val testimonials: ArrayList<TestimonialData>? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("banners")
    val banners: Banners? = null,

    @field:SerializedName("status")
    val status: Int? = null,

    @field:SerializedName("appdata")
    val appdata: Appdata? = null,

    @field:SerializedName("getprofile")
    val getprofiledata: getprofiledata? = null
)


@Parcelize
data class CategoriesItem(
    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("category_name")
    val categoryName: String? = null,

    @field:SerializedName("id")
    val id: Int? = null
) : Parcelable


data class TestimonialData(

    @field:SerializedName("profile_image")
    val profileImage: String? = null,

    @field:SerializedName("category_name")
    val categoryName: String? = null,
    @field:SerializedName("ratting")
    val ratting: String? = null,
    @field:SerializedName("comment")
    val comment: String? = null,
    @field:SerializedName("user_id")
    val userId: String? = null,

    @field:SerializedName("date")
    val date: String? = null,
    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("id")
    val id: Int? = null
)


@Parcelize
data class AddOnsDataModel(

    @field:SerializedName("id")
    val Id: Int? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("price")
    val price: String? = null,
    var isAddOnsSelect: Boolean = false
):Parcelable


data class TodayspecialItem(

    @field:SerializedName("is_cart")
    val isCart: Int? = null,

    @field:SerializedName("category_info")
    val categoryInfo: CategoryInfo? = null,

    @field:SerializedName("is_favorite")
    val isFavorite: Int? = null,

    @field:SerializedName("addons")
    val addons: ArrayList<AddOnsDataModel>? = null,

    @field:SerializedName("item_type")
    val itemType: Int? = null,

    @field:SerializedName("image_url")
    val imageUrl: String? = null,

    @field:SerializedName("item_name")
    val itemName: String? = null,

    @field:SerializedName("tax")
    val tax: String? = null,

    @field:SerializedName("has_variation")
    val hasVariation: Int? = null,

    @field:SerializedName("subcategory_info")
    val subcategoryInfo: SubcategoryInfo? = null,

    @field:SerializedName("variation")
    val variation: ArrayList<VariationItem>? = null,

    @field:SerializedName("image_name")
    val imageName: String? = null,

    @field:SerializedName("price")
    val price: Int? = null,

    @field:SerializedName("preparation_time")
    val preparationTime: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("attribute")
    val attribute: String? = null,

    @field:SerializedName("item_description")
    val itemDescription: String? = null,

    @field:SerializedName("item_qty")
    val itemQty: Int? = null
)

data class TopbannersItem(

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("item_id")
    val itemId: Int? = null,

    @field:SerializedName("cat_id")
    val catId: Int? = null,

    @field:SerializedName("id")
    val id: Int? = null
)

@Parcelize
data class SubcategoryInfo(

    @field:SerializedName("subcategory_name")
    val subcategoryName: String? = null,

    @field:SerializedName("id")
    val id: Int? = null
) : Parcelable

@Parcelize
data class VariationItem(

    @field:SerializedName("item_id")
    val itemId: Int? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("product_price")
    val productPrice: String? = null,

    @field:SerializedName("sale_price")
    val salePrice: Int? = null,

    @field:SerializedName("variation")
    val variation: String? = null,

    var isSelect: Boolean? = false
) : Parcelable

data class Bannersection3Item(

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("item_id")
    val itemId: Int? = null,

    @field:SerializedName("cat_id")
    val catId: Int? = null,

    @field:SerializedName("id")
    val id: Int? = null
)

data class Appdata(

    @field:SerializedName("copyright")
    val copyright: String? = null,

    @field:SerializedName("og_title")
    val ogTitle: String? = null,

    @field:SerializedName("timezone")
    val timezone: String? = null,

    @field:SerializedName("app_bottom_image_url")
    val appBottomImage: String? = null,

    @field:SerializedName("is_app_bottom_image")
    val is_app_bottom_image: String? = null,

    @field:SerializedName("android")
    val android: String? = null,

    @field:SerializedName("created_at")
    val createdAt: String? = null,

    @field:SerializedName("ios")
    val ios: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("youtube")
    val youtube: String? = null,

    @field:SerializedName("short_title")
    val shortTitle: String? = null,

    @field:SerializedName("updated_at")
    val updatedAt: String? = null,

    @field:SerializedName("og_description")
    val ogDescription: String? = null,

    @field:SerializedName("max_order_qty")
    val maxOrderQty: Int? = null,

    @field:SerializedName("referral_amount")
    val referralAmount: Int? = null,

    @field:SerializedName("logo")
    val logo: String? = null,

    @field:SerializedName("currency")
    val currency: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("lang")
    val lang: String? = null,

    @field:SerializedName("map")
    val map: String? = null,

    @field:SerializedName("email")
    val email: String? = null,

    @field:SerializedName("lat")
    val lat: String? = null,

    @field:SerializedName("verification")
    val verification: String? = null,

    @field:SerializedName("insta")
    val insta: String? = null,

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("address")
    val address: String? = null,

    @field:SerializedName("favicon")
    val favicon: String? = null,

    @field:SerializedName("mobile")
    val mobile: String? = null,

    @field:SerializedName("current_version")
    val currentVersion: String? = null,

    @field:SerializedName("firebase")
    val firebase: String? = null,

    @field:SerializedName("about_content")
    val aboutContent: String? = null,

    @field:SerializedName("currency_position")
    val currencyPosition: String? = null,

    @field:SerializedName("og_image")
    val ogImage: String? = null,

    @field:SerializedName("min_order_amount")
    val minOrderAmount: Int? = null,

    @field:SerializedName("delivery_charge")
    val deliveryCharge: String? = null,

    @field:SerializedName("max_order_amount")
    val maxOrderAmount: Int? = null,

    @field:SerializedName("footer_logo")
    val footerLogo: String? = null,

    @field:SerializedName("fb")
    val fb: String? = null
)

@Parcelize
data class RecommendeditemsItem(

    @field:SerializedName("is_cart")
    var isCart: Int? = null,

    @field:SerializedName("category_info")
    val categoryInfo: CategoryInfo? = null,

    @field:SerializedName("is_favorite")
    var isFavorite: Int? = null,

    @field:SerializedName("addons")
    val addons: ArrayList<AddOnsDataModel>? = null,

    @field:SerializedName("item_type")
    val itemType: Int? = null,

    @field:SerializedName("image_url")
    val imageUrl: String? = null,

    @field:SerializedName("item_name")
    val itemName: String? = null,

    @field:SerializedName("tax")
    val tax: String? = null,

    @field:SerializedName("has_variation")
    val hasVariation: Int? = null,

    @field:SerializedName("subcategory_info")
    val subcategoryInfo: SubcategoryInfo? = null,

    @field:SerializedName("variation")
    val variation: ArrayList<VariationItem>? = null,

    @field:SerializedName("image_name")
    val imageName: String? = null,

    @field:SerializedName("price")
    val price: Int? = null,

    @field:SerializedName("preparation_time")
    val preparationTime: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("attribute")
    val attribute: String? = null,

    @field:SerializedName("item_description")
    val itemDescription: String? = null,

    @field:SerializedName("item_qty")
    var itemQty: Int? = null
) : Parcelable

@Parcelize
data class CategoryInfo(

    @field:SerializedName("id")
    val Id: Int? = null,

    @field:SerializedName("category_name")
    val categoryName: String? = null,

    @field:SerializedName("image_url")
    val imageUrl: String? = null,
) : Parcelable


data class BannersItem(

    @field:SerializedName("image")
    val image: String? = null,

    @field:SerializedName("item_id")
    val itemId: Int? = null,

    @field:SerializedName("cat_id")
    val catId: String? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("item_info")
    val itemInfo: ItemInfo? = null,

    @field:SerializedName("category_info")
    val catInfo: CategoryInfo? = null,

    @field:SerializedName("id")
    val id: Int? = null
)

data class ItemInfo(
    @field:SerializedName("slug")
    val slug: String? = null,

    @field:SerializedName("id")
    val Id: Int? = null,

    @field:SerializedName("item_name")
    val itemName: String? = null,
)


data class Banners(

    @field:SerializedName("bannersection2")
    val bannersection2: ArrayList<BannersItem>? = null,

    @field:SerializedName("bannersection1")
    val bannersection1: ArrayList<BannersItem>? = null,

    @field:SerializedName("topbanners")
    val topbanners: ArrayList<BannersItem>? = null,

    @field:SerializedName("bannersection3")
    val bannersection3: ArrayList<BannersItem>? = null
)

data class Cartdata(

    @field:SerializedName("total_count")
    val totalCount: Int? = null,

    @field:SerializedName("sub_total")
    val subTotal: Int? = null
)

@Parcelize
data class FeatureditemsItem(

    @field:SerializedName("is_cart")
    var isCart: Int? = null,

    @field:SerializedName("category_info")
    val categoryInfo: CategoryInfo? = null,

    @field:SerializedName("is_favorite")
    var isFavorite: Int? = null,

    @field:SerializedName("addons")
    val addons: ArrayList<AddOnsDataModel>? = null,

    @field:SerializedName("item_type")
    val itemType: Int? = null,

    @field:SerializedName("image_url")
    val imageUrl: String? = null,

    @field:SerializedName("item_name")
    val itemName: String? = null,

    @field:SerializedName("tax")
    val tax: String? = null,

    @field:SerializedName("has_variation")
    val hasVariation: Int? = null,

    @field:SerializedName("subcategory_info")
    val subcategoryInfo: SubcategoryInfo? = null,

    @field:SerializedName("variation")
    val variation: ArrayList<VariationItem>? = null,

    @field:SerializedName("image_name")
    val imageName: String? = null,

    @field:SerializedName("price")
    val price: Int? = null,

    @field:SerializedName("preparation_time")
    val preparationTime: String? = null,

    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("attribute")
    val attribute: String? = null,

    @field:SerializedName("item_description")
    val itemDescription: String? = null,

    @field:SerializedName("item_qty")
    var itemQty: Int? = null
) : Parcelable



