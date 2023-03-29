package com.singlerestaurant.user.activity


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.view.View
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivitySplashBinding
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.printKeyHash
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.SharePreference.Companion.getBooleanPref
import com.google.firebase.messaging.FirebaseMessaging


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding
    override fun setLayout(): View {
        return binding.root
    }

    override fun InitView() {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        Common.getLog("getShaKey", printKeyHash(this@SplashActivity)!!)
        Common.getCurrentLanguage(this@SplashActivity, false)

        var strToken = ""
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            strToken = it.result
        }
        Common.getLog("Token== ", strToken)
        Common.getLog("Token== ", getBooleanPref(this@SplashActivity, SharePreference.isTutorial).toString())
        Handler(Looper.getMainLooper()).postDelayed({
            if (!getBooleanPref(this@SplashActivity, SharePreference.isTutorial)) {
                openActivity(TutorialActivity::class.java)
                finish()
            } else {
                if (intent.getStringExtra("type").toString() == "promotion") {
                    if (!intent.getStringExtra("category_id").isNullOrEmpty()) {
                        val actIntent = Intent(this, ActSubCategoryProducts::class.java)
                        actIntent.putExtra("category_id", intent.getStringExtra("category_id").toString())
                        actIntent.putExtra(Constants.FromNotification,true)
                        actIntent.putExtra("category_name", intent.getStringExtra("category_name").toString())
                        startActivity(actIntent)
                    } else if (!intent.getStringExtra("item_id").isNullOrEmpty()) {
                        val orderIntent = Intent(this, ActFoodDetails::class.java)
                        orderIntent.putExtra(Constants.FromNotification,true)
                        orderIntent.putExtra("foodItemId", intent.getStringExtra("item_id").toString())
                        startActivity(orderIntent)
                    } else {
                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                    }
                } else if (intent.getStringExtra("type").toString() == "order") {
                    val detailIntent = Intent(this, OrderDetailActivity::class.java)
                    detailIntent.putExtra("order_id", intent.getStringExtra("order_id").toString())
                    detailIntent.putExtra(Constants.FromNotification,true)
                    startActivity(detailIntent)
                } else if (intent.getStringExtra("type").toString() == "wallet") {
                    val walletIntent=Intent(this@SplashActivity,ActTransactionHistory::class.java)
                    walletIntent.putExtra(Constants.FromNotification,true)
                    startActivity(walletIntent)
                } else {
                    openActivity(DashboardActivity::class.java)
                }
                finish()
            }
        }, 3000)
    }



    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@SplashActivity, false)
    }
}