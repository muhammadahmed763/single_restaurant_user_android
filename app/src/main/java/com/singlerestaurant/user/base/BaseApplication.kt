package com.singlerestaurant.user.base


import android.app.Application
import androidx.multidex.MultiDex
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import com.singlerestaurant.user.R
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PaystackSdk.initialize(applicationContext)
        CalligraphyConfig.initDefault(
                CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Poppins-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .disableCustomViewInflation()
                        .build()
        )
        MultiDex.install(this);
    }
}