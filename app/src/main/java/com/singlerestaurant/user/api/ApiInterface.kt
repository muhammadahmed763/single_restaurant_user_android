package com.singlerestaurant.user.api

import com.singlerestaurant.user.model.*
import com.singlerestaurant.user.remote.NetworkResponse
import com.google.gson.JsonObject
import com.singlerestaurant.user.model.ListResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @POST("login")
    fun getLogin(@Body map: HashMap<String, String>): Call<RestResponse<LoginModel>>

    @POST("register")
    fun setRegistration(@Body map: HashMap<String, String>): Call<RestResponse<RegistrationModel>>

    @POST("ratting")
    suspend fun setRatting(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @GET("tutorial")
    suspend fun tutorial() :NetworkResponse<TutorialResponse,SingleResponsee>

    @POST("isopenclose")
    suspend fun isOpenClose(@Body map: HashMap<String, String>) :NetworkResponse<SingleResponsee,SingleResponsee>

    @Multipart
    @POST("editprofile")
     fun setProfile(@Part("user_id") userId: RequestBody, @Part("name") name: RequestBody, @Part profileimage: MultipartBody.Part?): Call<EditProfileResponse>

    @POST("addwallet")
    suspend fun addMoney(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee,SingleResponsee>

    @POST("rattinglist")
    suspend fun setRattingList(@Body map: HashMap<String, String>): NetworkResponse<RattingResponse, SingleResponsee>

    @POST("otpverify")
    suspend fun setEmailVerify(@Body map: HashMap<String, String>): NetworkResponse<JsonObject, SingleResponsee>

    @POST("forgotPassword")
    suspend fun setforgotPassword(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("qtyupdate")
    suspend fun setQtyUpdate(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("getprofile")
    suspend fun getProfile(@Body map: HashMap<String, String>): NetworkResponse<GetProfileResponse, SingleResponsee>

    @POST("ordercancel")
    suspend fun setOrderCancel(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("removefavorite")
    suspend fun setRemoveFavourite(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("changepassword")
    suspend fun setChangePassword(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("addaddress")
    suspend fun addAddress(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("updateaddress")
    suspend fun updateAddress(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("getaddress")
    suspend fun getAddress(@Body map: HashMap<String, String>): NetworkResponse<ListResponse<AddressResponse>, SingleResponsee>

    @POST("contact")
    suspend fun contactUs(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>


    @POST("deleteaddress")
    suspend fun deleteAddress(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("resendotp")
    suspend fun setResendEmailVerification(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("checkpromocode")
    suspend fun setApplyPromocode(@Body map: HashMap<String, String>): NetworkResponse<RestResponse<PromoCodeData>, SingleResponsee>

    @GET("promocodelist")
    suspend fun getPromoCodeList(): NetworkResponse<ListResponse<PromocodeModel>, SingleResponsee>

    @POST("itemdetails")
    suspend fun setItemDetail(@Body map: HashMap<String, String>): NetworkResponse<ItemDetailResponse, SingleResponsee>

    @POST("orderhistory")
    suspend fun getOrderHistory(@Body map: HashMap<String, String>): NetworkResponse<ListResponse<OrderHistoryModel>, SingleResponsee>

    @POST("deletecartitem")
    suspend fun setDeleteCartItem(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("summary")
    suspend fun setSummary(@Body map: HashMap<String, String>): NetworkResponse<RestSummaryResponse, SingleResponsee>

    @POST("order")
    suspend fun setOrderPayment(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>


    @POST("getorderdetails")
    suspend fun setGetOrderDetail(@Body map: HashMap<String, String>): NetworkResponse<OrderDetailResponse, SingleResponsee>

    @POST("searchitem")
    suspend fun setSearch(@Body map: HashMap<String, String>): NetworkResponse<ListResponse<SearchItemModel>, SingleResponsee>

    @POST("favoritelist")
    suspend fun getFavouriteList(@Body map: HashMap<String, String>): NetworkResponse<FavouriteListResponse, SingleResponsee>

    @POST("paymentmethodlist")
    suspend fun getPaymentType(@Body map: HashMap<String, String>): NetworkResponse<PaymentListModel, SingleResponsee>


    //Todo getHomeFeed api
    @POST("home")
    suspend fun getHomeFeedApi(@Body map: HashMap<String, String>): NetworkResponse<HomeFeedModel, SingleResponsee>

    //Todo favoritesApi
    @POST("managefavorite")
    suspend fun manageFavoritesApi(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("addtocart")
    suspend fun addToCart(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("wallettransactions")
    suspend fun walletTransaction(@Body map: HashMap<String, String>): NetworkResponse<WalletTransactionResponse, SingleResponsee>

    @POST("isnotification")
    suspend fun isNotification(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("categoryitems")
    suspend fun categoryItems(@Body map: HashMap<String, String>): NetworkResponse<CategoryWiseResponse, SingleResponsee>


    @GET("faq")
    suspend fun getFaq(): NetworkResponse<FaqResponse, SingleResponsee>


    @GET("gallery")
    suspend fun gallery(): NetworkResponse<GalleryResponse, SingleResponsee>

    @GET("blogs")
    suspend fun getBlogs(): NetworkResponse<BlogResponse, SingleResponsee>

    @GET("cmspages")
    suspend fun cmsPages(): NetworkResponse<CmsPagesResponse, SingleResponsee>

    @GET("ourteam")
    suspend fun getTeamData(): NetworkResponse<OurTeamResponse, SingleResponsee>

    @POST("checkdeliveryzone")
    suspend fun checkDeliveryZone(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>

    @POST("booktable")
    suspend fun bookTable(@Body map: HashMap<String, String>): NetworkResponse<SingleResponsee, SingleResponsee>
}