package com.singlerestaurant.user.activity

import android.os.CountDownTimer
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.api.SingleResponsee
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivityOtpverificationBinding
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import com.google.firebase.messaging.FirebaseMessaging
import com.singlerestaurant.user.utils.Constants
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class OTPVerificationActivity : BaseActivity() {
    var strEmail: String = ""
    var strToken = ""
    var loginUserType = ""
    private lateinit var binding:ActivityOtpverificationBinding
    override fun setLayout(): View {
        return binding.root
    }

    override fun InitView() {
        binding=ActivityOtpverificationBinding.inflate(layoutInflater)
        loginUserType = SharePreference.getStringPref(
            this@OTPVerificationActivity,
            SharePreference.otpAddon
        ) ?: "2"
        strEmail = intent.getStringExtra("email")!!
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            strToken = it.result
        }
        if(ApiClient.System_environment==Constants.SendBox)
        {
            binding.edOTP.setText(intent.getStringExtra("otp"))
            binding.edOTP.isEnabled=false
            binding.edOTP.isClickable=false
        }

        binding.tvCheckout.setOnClickListener {
            Common.closeKeyBoard(this@OTPVerificationActivity)
            if (binding.edOTP.text.toString().length != 6) {
                Common.alertErrorOrValidationDialog(
                    this@OTPVerificationActivity,
                    resources.getString(R.string.validation_otp)
                )
            } else {
                if (loginUserType == "1") {
                    val map = HashMap<String, String>()
                    map["mobile"] = strEmail
                    map["otp"] = binding.edOTP.text.toString()
                    map["token"] = strToken
                    if (Common.isCheckNetwork(this@OTPVerificationActivity)) {
                        callApiOTP(map)
                    } else {
                        Common.alertErrorOrValidationDialog(
                            this@OTPVerificationActivity,
                            resources.getString(R.string.no_internet)
                        )
                    }
                } else {
                    val map = HashMap<String, String>()
                    map["email"] = strEmail
                    map["otp"] = binding.edOTP.text.toString()
                    map["token"] = strToken
                    if (Common.isCheckNetwork(this@OTPVerificationActivity)) {
                        callApiOTP(map)
                    } else {
                        Common.alertErrorOrValidationDialog(
                            this@OTPVerificationActivity,
                            resources.getString(R.string.no_internet)
                        )
                    }
                }


            }
        }

        object : CountDownTimer(120000, 1000) {
            override fun onTick(millis: Long) {
                binding.llOTP.visibility = View.GONE
                binding.tvResendOtp.visibility = View.GONE
                binding.tvTimer.visibility = View.VISIBLE
                val timer = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(
                            millis
                        )
                    ), TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millis)
                    )
                )
                binding.tvTimer.text = timer
                if (timer == "00:00") {
                    binding. tvTimer.visibility = View.GONE
                    binding. llOTP.visibility = View.VISIBLE
                    binding.tvResendOtp.visibility = View.VISIBLE
                }
            }

            override fun onFinish() {
                binding.  tvTimer.visibility = View.GONE
                binding. llOTP.visibility = View.VISIBLE
                binding. tvResendOtp.visibility = View.VISIBLE
            }
        }.start()

        binding.tvResendOtp.setOnClickListener {
            Common.closeKeyBoard(this@OTPVerificationActivity)
            val map = HashMap<String, String>()
            if (loginUserType == "1") {
                map["mobile"] = strEmail

            } else {
                map["email"] = strEmail

            }
            if (Common.isCheckNetwork(this@OTPVerificationActivity)) {
                callApiResendOTP(map)
            } else {
                Common.alertErrorOrValidationDialog(
                    this@OTPVerificationActivity,
                    resources.getString(R.string.no_internet)
                )
            }
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
        if (SharePreference.getStringPref(
                this@OTPVerificationActivity,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi)))
        {
            binding. ivBack.rotation=180f
        }else{
            binding.  ivBack.rotation=0f

        }
    }



    private fun callApiOTP(map: HashMap<String, String>) {
        Common.showLoadingProgress(this@OTPVerificationActivity)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setEmailVerify(map)) {
                is NetworkResponse.Success -> {
                    val mainObject = JSONObject(response.body.toString())
                    val statusType = mainObject.getInt("status")
                    val statusMessage = mainObject.getString("message")
                    if (statusType == 0) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(
                            this@OTPVerificationActivity,
                            statusMessage
                        )
                    } else if (statusType == 1) {
                        Common.dismissLoadingProgress()
                        SharePreference.setStringPref(
                            this@OTPVerificationActivity,
                            SharePreference.userId,
                            mainObject.getJSONObject("data").getString("id")
                        )
                        SharePreference.setStringPref(
                            this@OTPVerificationActivity,
                            SharePreference.userName,
                            mainObject.getJSONObject("data").getString("name")
                        )
                        SharePreference.setStringPref(
                            this@OTPVerificationActivity,
                            SharePreference.userEmail,
                            mainObject.getJSONObject("data").getString("email")
                        )
                        SharePreference.setStringPref(
                            this@OTPVerificationActivity,
                            SharePreference.loginType,
                            mainObject.getJSONObject("data").getString("login_type")
                        )
                        SharePreference.setStringPref(
                            this@OTPVerificationActivity,
                            SharePreference.userMobile,
                            mainObject.getJSONObject("data").getString("mobile")
                        )
                        SharePreference.setStringPref(
                            this@OTPVerificationActivity,
                            SharePreference.userProfile,
                            mainObject.getJSONObject("data").getString("profile_image")
                        )
                        SharePreference.setStringPref(
                            this@OTPVerificationActivity,
                            SharePreference.userRefralCode,
                            mainObject.getJSONObject("data").getString("referral_code")
                        )
                        SharePreference.setStringPref(
                            this@OTPVerificationActivity,
                            SharePreference.notificationStatus,
                            mainObject.getJSONObject("data").getString("is_notification")
                        )
                        SharePreference.setStringPref(
                            this@OTPVerificationActivity,
                            SharePreference.emailStatus,
                            mainObject.getJSONObject("data").getString("is_mail")
                        )
                        SharePreference.setStringPref(
                            this@OTPVerificationActivity,
                            SharePreference.wallet,
                            mainObject.getJSONObject("data").getString("wallet")
                        )

                        SharePreference.setBooleanPref(
                            this@OTPVerificationActivity,
                            SharePreference.isLogin,
                            true
                        )
                        openActivity(DashboardActivity::class.java)
                        finish()
                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OTPVerificationActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OTPVerificationActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OTPVerificationActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }

















    private fun callApiResendOTP(map: HashMap<String, String>) {
        Common.showLoadingProgress(this@OTPVerificationActivity)
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setResendEmailVerification(map)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()

                    val registrationResponse: SingleResponsee = response.body
                    if (registrationResponse.status==1) {

                        binding.edOTP.text?.clear()
                        if(ApiClient.System_environment==Constants.SendBox)
                        {
                            binding.edOTP.setText(response.body.otp)
                            binding.edOTP.isEnabled=false
                            binding.edOTP.isClickable=false

                        }

                        Common.showSuccessFullMsg(
                            this@OTPVerificationActivity,
                            registrationResponse.message.toString()
                        )
                    } else if (registrationResponse.status==0) {
                        Common.showErrorFullMsg(
                            this@OTPVerificationActivity,
                            registrationResponse.message!!
                        )
                    }

                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OTPVerificationActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OTPVerificationActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@OTPVerificationActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }



    override fun onBackPressed() {
        Common.closeKeyBoard(this@OTPVerificationActivity)
        openActivity(LoginActivity::class.java)
        finish()
        finishAffinity()
    }

}