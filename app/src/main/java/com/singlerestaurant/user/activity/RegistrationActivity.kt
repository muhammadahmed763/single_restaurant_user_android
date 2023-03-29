package com.singlerestaurant.user.activity

import android.content.Context
import `in`.mayanknagwanshi.countrypicker.CountrySelectActivity
import `in`.mayanknagwanshi.countrypicker.bean.CountryData
import android.content.Intent
import android.text.InputType
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.api.RestResponse
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivityRegistrationBinding
import com.singlerestaurant.user.model.RegistrationModel
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.alertErrorOrValidationDialog
import com.singlerestaurant.user.utils.Common.showErrorFullMsg
import com.singlerestaurant.user.utils.SharePreference
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistrationActivity : BaseActivity() {
    var countryCode = "+91"
    var strToken = ""

    private lateinit var binding:ActivityRegistrationBinding

    override fun setLayout(): View {

        return binding.root
    }


    override fun InitView() {
        binding=ActivityRegistrationBinding.inflate(layoutInflater)
        Common.getCurrentLanguage(this@RegistrationActivity, false)
        binding.   tvTermsAndCondition.setOnClickListener {

            startActivity(Intent(this@RegistrationActivity,ActCmsPages::class.java).putExtra("title","termscondition"))
//            openActivity(TermsAndConditionActivity::class.java)
        }
        FirebaseApp.initializeApp(this@RegistrationActivity)

        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            strToken = it.result
        }

        if (intent.getStringExtra("loginType") != null) {
            binding. edFullName.setText(intent.getStringExtra("name")!!)
            binding.edEmail.setText(intent.getStringExtra("profileEmail")!!)
            binding. tvPass.visibility = View.GONE
            binding. edEmail.isActivated = false
            binding. edEmail.inputType = InputType.TYPE_NULL
        } else {
            binding.tvPass.visibility = View.VISIBLE
        }

        if (SharePreference.getStringPref(
                this@RegistrationActivity, SharePreference.otpAddon
            ) == "1") {
            binding. tvPass.visibility = View.GONE
            }

        binding.edCountryCode.setOnClickListener {
            val intent = Intent(this, CountrySelectActivity::class.java)
            intent.putExtra(CountrySelectActivity.EXTRA_SELECTED_COUNTRY, CountryData("IN"))
            activityResult.launch(intent)
        }
    }

    fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvLogin -> {
                openActivity(LoginActivity::class.java)
            }
            R.id.tvSignup -> {
                if (intent.getStringExtra("loginType") != null) {
                    if (binding.edFullName.text.toString() == "") {
                        showErrorFullMsg(
                            this@RegistrationActivity,
                            resources.getString(R.string.validation_all)
                        )
                    } else if (binding.edMobile.text.toString() == "") {
                        showErrorFullMsg(
                            this@RegistrationActivity,
                            resources.getString(R.string.validation_all)
                        )
                    } else if (intent.getStringExtra("loginType") == "facebook" || intent.getStringExtra(
                            "loginType"
                        ) == "google"
                    ) {
                        val hasmap = HashMap<String, String>()
                        hasmap["name"] = intent.getStringExtra("name")!!
                        hasmap["email"] = intent.getStringExtra("profileEmail")!!
                        hasmap["mobile"] = countryCode.plus(binding.edMobile.text.toString())
                        hasmap["referral_code"] = binding.edRefralcode.text.toString()
                        hasmap["token"] = intent.getStringExtra("strToken")!!
                        hasmap["register_type"] = "email"
                        hasmap["login_type"] = intent.getStringExtra("loginType")!!
                        if (intent.getStringExtra("loginType") == "google") {
                            hasmap["google_id"] = intent.getStringExtra("profileId")!!
                            hasmap["facebook_id"] = ""
                        } else {
                            hasmap["facebook_id"] = intent.getStringExtra("profileId")!!
                            hasmap["google_id"] = ""
                        }
                        if (Common.isCheckNetwork(this@RegistrationActivity)) {
                            if (binding.cbCheck.isChecked) {
                                callApiRegistration(hasmap)
                            } else {
                                showErrorFullMsg(
                                    this@RegistrationActivity,
                                    resources.getString(R.string.terms_condition_error)
                                )
                            }
                        } else {
                            alertErrorOrValidationDialog(
                                this@RegistrationActivity,
                                resources.getString(R.string.no_internet)
                            )
                        }
                    }
                } else {
                    if (binding.edFullName.text.toString() == "") {
                        showErrorFullMsg(
                            this@RegistrationActivity,
                            resources.getString(R.string.validation_all)
                        )
                    } else if (binding.edEmail.text.toString() == "") {
                        showErrorFullMsg(
                            this@RegistrationActivity,
                            resources.getString(R.string.validation_all)
                        )
                    } else if (!Common.isValidEmail(binding.edEmail.text.toString())) {
                        showErrorFullMsg(
                            this@RegistrationActivity,
                            resources.getString(R.string.validation_valid_email)
                        )
                    } else if (binding.edMobile.text.toString() == "") {
                        showErrorFullMsg(
                            this@RegistrationActivity,
                            resources.getString(R.string.validation_all)
                        )
                    } else if (SharePreference.getStringPref(
                            this@RegistrationActivity, SharePreference.otpAddon
                        ) == "1") {
                        if (binding.cbCheck.isChecked) {
                            val hasmap = HashMap<String, String>()
                            hasmap["name"] = binding.edFullName.text.toString()
                            hasmap["email"] = binding.edEmail.text.toString()
                            hasmap["mobile"] = countryCode.plus(binding.edMobile.text.toString())
                            hasmap["token"] = strToken
                            hasmap["login_type"] = "email"
                            hasmap["register_type"] = "email"
                            hasmap["referral_code"] = binding.edRefralcode.text.toString()
                            if (Common.isCheckNetwork(this@RegistrationActivity)) {
                                callApiRegistration(hasmap)
                            } else {
                                alertErrorOrValidationDialog(
                                    this@RegistrationActivity,
                                    resources.getString(R.string.no_internet)
                                )
                            }
                        } else {
                            showErrorFullMsg(
                                this@RegistrationActivity,
                                resources.getString(R.string.terms_condition_error)
                            )
                        }
                        } else {
                        if (binding.edPassword.text.toString() == "") {
                            showErrorFullMsg(
                                this@RegistrationActivity,
                                resources.getString(R.string.validation_all)
                            )
                        } else if (binding.cbCheck.isChecked) {
                            val hasmap = HashMap<String, String>()
                            hasmap["name"] = binding.edFullName.text.toString()
                            hasmap["email"] = binding.edEmail.text.toString()
                            hasmap["mobile"] = countryCode.plus(binding.edMobile.text.toString())
                            hasmap["password"] = binding.edPassword.text.toString()
                            hasmap["token"] = strToken
                            hasmap["login_type"] = "email"
                            hasmap["register_type"] = "email"
                            hasmap["referral_code"] = binding.edRefralcode.text.toString()
                            val userName=binding.edFullName.text.toString()
                            SharePreference.saveUserName(this,userName)
                            if (Common.isCheckNetwork(this@RegistrationActivity)) {
                                callApiRegistration(hasmap)
                            } else {
                                alertErrorOrValidationDialog(
                                    this@RegistrationActivity,
                                    resources.getString(R.string.no_internet)
                                )
                            }
                        } else {
                            showErrorFullMsg(
                                this@RegistrationActivity,
                                resources.getString(R.string.terms_condition_error)
                            )
                        }

                    }

                }

            }
            R.id.tvSkip -> {
                openActivity(DashboardActivity::class.java)
                finish()
                finishAffinity()
            }

        }
    }

    private fun callApiRegistration(hasmap: HashMap<String, String>) {
        Common.showLoadingProgress(this@RegistrationActivity)
        val call = ApiClient.getClient.setRegistration(hasmap)
        call.enqueue(object : Callback<RestResponse<RegistrationModel>> {
            override fun onResponse(
                call: Call<RestResponse<RegistrationModel>>,
                response: Response<RestResponse<RegistrationModel>>
            ) {
                if (response.code() == 200) {
                    val registrationResponse: RestResponse<RegistrationModel> = response.body()!!
                    if (registrationResponse.getStatus().equals("1")) {
                        Common.dismissLoadingProgress()
                        var otpController = ""
                        otpController = if (SharePreference.getStringPref(
                                this@RegistrationActivity,
                                SharePreference.otpAddon
                            ) == "1"
                        ) {
                            countryCode.plus(binding.edMobile.text.toString())
                        } else {
                            binding.edEmail.text.toString()
                        }
                        startActivity(
                            Intent(
                                this@RegistrationActivity,
                                OTPVerificationActivity::class.java
                            ).putExtra("email", otpController).putExtra("otp",registrationResponse.getOtp())
                        )
                    } else if (registrationResponse.getStatus().equals("0")) {
                        Common.dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            this@RegistrationActivity,
                            registrationResponse.getMessage()
                        )
                    }
                } else {
                    val error = JSONObject(response.errorBody()!!.string())
                    val status = error.getInt("status")
                    if (status == 2) {
                        Common.dismissLoadingProgress()
                        val otpController = if (SharePreference.getStringPref(
                                this@RegistrationActivity,
                                SharePreference.otpAddon
                            ) == "1"
                        ) {
                            countryCode.plus(binding.edMobile.text.toString())
                        } else {
                            binding.edEmail.text.toString()
                        }
                        startActivity(
                            Intent(
                                this@RegistrationActivity,
                                OTPVerificationActivity::class.java
                            ).putExtra("email", otpController).putExtra("otp",error.getString("otp"))
                        )
                    } else {
                        Common.dismissLoadingProgress()
                        showErrorFullMsg(
                            this@RegistrationActivity,
                            error.getString("message")
                        )
                    }
                }
            }

            override fun onFailure(call: Call<RestResponse<RegistrationModel>>, t: Throwable) {
                Common.dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@RegistrationActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }


    private var activityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data?.hasExtra(CountrySelectActivity.RESULT_COUNTRY_DATA) == true) {
                val countryData = result.data?.getSerializableExtra(CountrySelectActivity.RESULT_COUNTRY_DATA) as CountryData?
                binding.edCountryCode.text = countryData?.countryISD
                countryCode = countryData?.countryISD.toString()
            }


        }


    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@RegistrationActivity, false)
    }
}