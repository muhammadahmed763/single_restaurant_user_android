package com.singlerestaurant.user.model

class RegistrationModel {
    private var profile_image: String? = null

    private var name: String? = null

    private var mobile: String? = null

    private var id: String? = null

    private var email: String? = null

    private var referral_code: String? = null
    private var login_type: String? = null

    private var is_notification: String? = null
    private var is_mail: String? = null
    private var wallet: String? = null


    fun emailStatus():String?{
        return is_mail
    }

    fun getWallet():String?{
        return wallet
    }
    fun getNotificationStatus():String?{
        return is_notification
    }


    fun getLoginType():String?{
        return login_type
    }

    fun getProfile_image(): String? {
        return profile_image
    }

    fun getName(): String? {
        return name
    }

    fun getMobile(): String? {
        return mobile
    }

    fun getId(): String? {
        return id
    }

    fun setId(id: String?) {
        this.id = id
    }

    fun getEmail(): String? {
        return email
    }

    fun getReferral_code(): String? {
        return referral_code
    }
}