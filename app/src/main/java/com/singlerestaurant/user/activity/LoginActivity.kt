package com.singlerestaurant.user.activity

import `in`.mayanknagwanshi.countrypicker.CountrySelectActivity
import `in`.mayanknagwanshi.countrypicker.bean.CountryData
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.api.RestResponse
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivityLoginBinding
import com.singlerestaurant.user.model.LoginModel
import com.singlerestaurant.user.model.RegistrationModel
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.alertErrorOrValidationDialog
import com.singlerestaurant.user.utils.Common.dismissLoadingProgress
import com.singlerestaurant.user.utils.Common.getLog
import com.singlerestaurant.user.utils.Common.showErrorFullMsg
import com.singlerestaurant.user.utils.Common.showLoadingProgress
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.SharePreference.Companion.loginType
import com.singlerestaurant.user.utils.SharePreference.Companion.setBooleanPref
import com.singlerestaurant.user.utils.SharePreference.Companion.setStringPref
import com.singlerestaurant.user.utils.SharePreference.Companion.userEmail
import com.singlerestaurant.user.utils.SharePreference.Companion.userId
import com.singlerestaurant.user.utils.SharePreference.Companion.userMobile
import com.singlerestaurant.user.utils.SharePreference.Companion.userName
import com.singlerestaurant.user.utils.SharePreference.Companion.userProfile
import com.singlerestaurant.user.utils.SharePreference.Companion.userRefralCode
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.singlerestaurant.user.utils.Constants
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : BaseActivity() {

    //:::::::::::::::Google Login::::::::::::::::://
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 1

    //:::::::::::::::Facebook Login::::::::::::::::://
    private var callbackManager: CallbackManager? = null
    var countryCode="+91"

    var strToken = ""
    var getUserLoginType="2"
    private lateinit var binding:ActivityLoginBinding
    override fun setLayout(): View {
        return binding.root
    }

    override fun InitView() {
        binding=ActivityLoginBinding.inflate(layoutInflater)
        getUserLoginType  =SharePreference.getStringPref(this@LoginActivity,SharePreference.otpAddon)?:"2"
        initClickListeners()
        Common.getCurrentLanguage(this@LoginActivity, false)
        FirebaseApp.initializeApp(this@LoginActivity)
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            strToken = it.result
        }

        if(ApiClient.System_environment==Constants.SendBox)
        {
            binding.edEmail.setText("user@gmail.com")
            binding.edPassword.setText("123456")
        }

        getLog("Token== ", strToken)
        if(getUserLoginType=="1")
        {
            binding. linearMobile.visibility =View.VISIBLE
            binding.  emailLayout.visibility =View.GONE
            binding.tvForgetPassword.visibility = View.GONE
        }else
        {
            binding. linearMobile.visibility =View.GONE
            binding. emailLayout.visibility =View.VISIBLE
            binding.tvForgetPassword.visibility = View.VISIBLE
        }
        //:::::::::::::::Google Login::::::::::::::::://
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.rlbtnGoogle.setOnClickListener {
            if (Common.isCheckNetwork(this@LoginActivity)) {
                mGoogleSignInClient!!.signOut().addOnCompleteListener(this) { signInGoogle() }
            } else {
                alertErrorOrValidationDialog(
                    this@LoginActivity,
                    resources.getString(R.string.no_internet)
                )
            }
        }


        //::::::::::::::Facebook Login::::::::::::::::://
        FacebookSdk.setApplicationId(resources.getString(R.string.facebook_id))
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    updateFacebookUI(loginResult)
                }

                override fun onCancel() {}
                override fun onError(error: FacebookException) {
                    Log.e("error",error.toString())
                    Toast.makeText(applicationContext, "" + error.message, Toast.LENGTH_LONG)
                        .show()
                }
            })

        binding.rlbtnFacebook.setOnClickListener {
            if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut()
            }
            LoginManager.getInstance().logInWithReadPermissions(this, getFacebookPermissions())
        }


        binding.edCountryCode.setOnClickListener {
            val intent = Intent(this, CountrySelectActivity::class.java)
            intent.putExtra(CountrySelectActivity.EXTRA_SELECTED_COUNTRY, CountryData("IN"))
            startActivityForResult(intent, 121)
        }

    }

    private fun getFacebookPermissions(): List<String> {
        return listOf("public_profile","email")
    }

    private fun initClickListeners()
    {
        binding. tvLogin.setOnClickListener {
            Log.e("loginUserType",getUserLoginType)
            if(getUserLoginType=="1")
            {
                if ( binding.edMobile.text.toString() == "") {
                    showErrorFullMsg(
                        this@LoginActivity,
                        resources.getString(R.string.validation_all)
                    )
                } else {
                    val hasmap = HashMap<String, String>()
                    hasmap["mobile"] = countryCode.plus( binding.edMobile.text.toString())
                    hasmap["token"] = strToken
                    if (Common.isCheckNetwork(this@LoginActivity)) {
                        callApiLogin(hasmap)
                    } else {
                        alertErrorOrValidationDialog(
                            this@LoginActivity,
                            resources.getString(R.string.no_internet)
                        )
                    }
                }
            } else
            {
                if ( binding.edEmail.text.toString() == "") {
                    showErrorFullMsg(
                        this@LoginActivity,
                        resources.getString(R.string.validation_all)
                    )
                } else if (!Common.isValidEmail(binding.edEmail.text.toString())) {
                    showErrorFullMsg(
                        this@LoginActivity,
                        resources.getString(R.string.validation_valid_email)
                    )
                } else if (binding.edPassword.text.toString() == "") {
                    showErrorFullMsg(
                        this@LoginActivity,
                        resources.getString(R.string.validation_all)
                    )
                } else {
                    val hasmap = HashMap<String, String>()
                    hasmap["email"] = binding.edEmail.text.toString()
                    hasmap["password"] = binding.edPassword.text.toString()
                    hasmap["token"] = strToken
                    if (Common.isCheckNetwork(this@LoginActivity)) {
                        callApiLogin(hasmap)
                    } else {
                        alertErrorOrValidationDialog(
                            this@LoginActivity,
                            resources.getString(R.string.no_internet)
                        )
                    }
                }
            }

        }
    }

    fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvSignup -> {
                openActivity(RegistrationActivity::class.java)
            }
            R.id.tvForgetPassword -> {
                openActivity(ForgetPasswordActivity::class.java)
            }
            R.id.tvSkip -> {
                openActivity(DashboardActivity::class.java)
                finish()
                finishAffinity()
            }

        }
    }







    private fun callApiLogin(hasmap: HashMap<String, String>) {
        showLoadingProgress(this@LoginActivity)
        val call = ApiClient.getClient.getLogin(hasmap)
        call.enqueue(object : Callback<RestResponse<LoginModel>> {
            override fun onResponse(
                call: Call<RestResponse<LoginModel>>,
                response: Response<RestResponse<LoginModel>>
            ) {
                if (response.code() == 200) {
                    val loginResponce: RestResponse<LoginModel> = response.body()!!
                    dismissLoadingProgress()

                    if (loginResponce.getStatus().equals("1")) {
                        dismissLoadingProgress()
                        val loginModel: LoginModel = loginResponce.getData()!!
                        setBooleanPref(
                            this@LoginActivity,
                            SharePreference.isLogin,
                            true
                        )
                        setStringPref(this@LoginActivity, userId, loginModel.getId()!!)
                        setStringPref(this@LoginActivity, userMobile, loginModel.getMobile()!!)
                        setStringPref(this@LoginActivity, loginType, loginModel.getLoginType()!!)
                        setStringPref(this@LoginActivity, userEmail, loginModel.getEmail()!!)
                        setStringPref(this@LoginActivity, userName, loginModel.getName()!!)
                        setStringPref(this@LoginActivity, userRefralCode, loginModel.getReferralCode().toString())
                        setStringPref(this@LoginActivity, userProfile, loginModel.getProfile()!!)
                        setStringPref(this@LoginActivity, SharePreference.notificationStatus, loginModel.getNotificationStatus().toString())
                        setStringPref(this@LoginActivity, SharePreference.emailStatus, loginModel.emailStatus().toString())
                        setStringPref(this@LoginActivity, SharePreference.wallet, loginModel.getWallet()?:"0.00")


                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent);
                        finish()
                        finishAffinity()
                    } else if (loginResponce.getStatus().equals("2")) {
                        dismissLoadingProgress()

                      val  otpController = if(SharePreference.getStringPref(this@LoginActivity,SharePreference.otpAddon)=="1") {
                            countryCode.plus(binding.edMobile.text.toString())
                        }else {
                          binding.edEmail.text.toString()
                        }
                        val otp = loginResponce.getOtp()

                        startActivity(Intent(this@LoginActivity, OTPVerificationActivity::class.java).putExtra("email", otpController).putExtra("otp",otp))
                    }else
                    {
                        showErrorFullMsg(this@LoginActivity, response.body()?.getMessage().toString())
                    }
                } else {
                    dismissLoadingProgress()

                    val error = JSONObject(response.errorBody()!!.string())
                    val status = error.getInt("status")
                    val otp = error.getInt("otp")?:""
                    if (status == 2) {
                        dismissLoadingProgress()
                        val  otpController = if(SharePreference.getStringPref(this@LoginActivity,SharePreference.otpAddon)=="1") {
                            countryCode.plus(binding.edMobile.text.toString())
                        }else {
                            binding.edEmail.text.toString()
                        }
                        startActivity(
                            Intent(
                                this@LoginActivity,
                                OTPVerificationActivity::class.java
                            ).putExtra("email", otpController).putExtra("otp",otp)
                        )
                    } else {
                        dismissLoadingProgress()
                        showErrorFullMsg(this@LoginActivity, error.getString("message"))
                    }

                }
            }

            override fun onFailure(call: Call<RestResponse<LoginModel>>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@LoginActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@LoginActivity, false)
        if(getUserLoginType=="1")
        {
            binding.linearMobile?.visibility=View.VISIBLE
            binding.emailLayout?.visibility=View.GONE
        }else
        {
            binding.linearMobile?.visibility=View.GONE
            binding.emailLayout?.visibility=View.VISIBLE
        }
    }

    override fun onBackPressed() {
        finish()
        finishAffinity()
    }

    private fun signInGoogle() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager!!.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
        if (requestCode == 121 && data!!.hasExtra(CountrySelectActivity.RESULT_COUNTRY_DATA)) {
            val countryData = data.getSerializableExtra(CountrySelectActivity.RESULT_COUNTRY_DATA) as CountryData?
            binding.edCountryCode.text = countryData?.countryISD
            countryCode = countryData?.countryISD.toString()

            Log.e("Code", countryData?.countryISD + countryData?.countryCode)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)!!
            nextGmailActivity(account)
        } catch (e: ApiException) {
            Log.e("Google Login", "signInResult:failed code=" + e.statusCode)
        }
    }


    @SuppressLint("HardwareIds")
    private fun nextGmailActivity(profile: GoogleSignInAccount?) {
        if (profile != null) {
            val loginType = "google"
            val FristName = profile.displayName
            val profileEmail = profile.email
            val profileId = profile.id
            loginApiCall(FristName!!, profileEmail!!, profileId!!, loginType, strToken)
        }
    }

    private fun mGoToRegistration(
        name: String,
        profileEmail: String,
        profileId: String,
        loginType: String,
        strToken: String
    ) {
        val intent = Intent(this@LoginActivity, RegistrationActivity::class.java)
        intent.putExtra("name", name)
        intent.putExtra("profileEmail", profileEmail)
        intent.putExtra("profileId", profileId)
        intent.putExtra("loginType", loginType)
        intent.putExtra("strToken", strToken)
        startActivity(intent)
    }

    //::::::::::::::FacebookLogin:::::::::::::://
    private fun updateFacebookUI(loginResult: LoginResult) {
        val request = GraphRequest.newMeRequest(
            loginResult.accessToken
        ) { `object`, response ->
            if (`object` != null) {
                getFacebookData(`object`)
            }
        }
        val parameters = Bundle()
        parameters.putString(
            "fields",
            "id, first_name, last_name, email,age_range, gender, birthday, location"
        ) // Parámetros que pedimos a facebook
        request.parameters = parameters
        request.executeAsync()
    }

    private fun getFacebookData(`object`: JSONObject) {
        try {
            val profileId = `object`.getString("id")
            var name = ""
            if (`object`.has("first_name")) {
                name = `object`.getString("first_name")
            }
            if (`object`.has("last_name")) {
                name += " " + `object`.getString("last_name")
            }
            var email = ""
            if (`object`.has("email")) {
                email = `object`.getString("email")
            }
            val loginType = "facebook"
            loginApiCall(name, email, profileId, loginType, strToken)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun loginApiCall(
        name: String,
        email: String,
        profileId: String,
        loginType: String,
        strToken: String
    ) {
        val hasmap = HashMap<String, String>()
        hasmap["name"] = name
        hasmap["email"] = email
        hasmap["mobile"] = ""
        hasmap["token"] = strToken
        hasmap["login_type"] = loginType
        if (loginType == "google") {
            hasmap["google_id"] = profileId
            hasmap["facebook_id"] = ""
        } else {
            hasmap["facebook_id"] = profileId
            hasmap["google_id"] = ""
        }
        showLoadingProgress(this@LoginActivity)
        val call = ApiClient.getClient.setRegistration(hasmap)
        call.enqueue(object : Callback<RestResponse<RegistrationModel>> {
            override fun onResponse(
                call: Call<RestResponse<RegistrationModel>>,
                response: Response<RestResponse<RegistrationModel>>
            ) {
                if (response.code() == 200) {
                    val registrationResponse: RestResponse<RegistrationModel> = response.body()!!
                    if (registrationResponse.getStatus().toString() == "1") {
                        dismissLoadingProgress()
                        setProfileData(registrationResponse.getData())
                    } else if (registrationResponse.getStatus().toString() == "0") {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            this@LoginActivity,
                            registrationResponse.getMessage()
                        )
                    } else if (registrationResponse.getStatus().toString() == "2") {
                        dismissLoadingProgress()
                        mGoToRegistration(name, email, profileId, loginType, strToken)
                    } else if (registrationResponse.getStatus().toString() == "3") {
                        dismissLoadingProgress()
                        if(getUserLoginType=="1")
                        {
                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    OTPVerificationActivity::class.java
                                ).putExtra("email", registrationResponse.getEmail().toString()).putExtra("otp",registrationResponse.getOtp())
                            )
                        }else
                        {
                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    OTPVerificationActivity::class.java
                                ).putExtra("email", email).putExtra("otp",registrationResponse.getOtp())
                            )
                        }

                    }
                } else {
                    val error = JSONObject(response.errorBody()!!.string())
                    if (error.getString("status") == "3") {
                        dismissLoadingProgress()

                        if(getUserLoginType=="1")
                        {
                            startActivity(Intent(this@LoginActivity, OTPVerificationActivity::class.java).putExtra("email", error.getString("mobile").toString()).putExtra("otp",error.getString("otp")))
                        }else
                        {
                            startActivity(Intent(this@LoginActivity, OTPVerificationActivity::class.java).putExtra("email", email).putExtra("otp",error.getString("otp")))
                        }

                    } else {
                        dismissLoadingProgress()
                        alertErrorOrValidationDialog(
                            this@LoginActivity,
                            error.getString("message")
                        )
                    }

                }
            }

            override fun onFailure(call: Call<RestResponse<RegistrationModel>>, t: Throwable) {
                dismissLoadingProgress()
                alertErrorOrValidationDialog(
                    this@LoginActivity,
                    resources.getString(R.string.error_msg)
                )
            }
        })

    }

    private fun setProfileData(dataResponse: RegistrationModel?) {
        setBooleanPref(this@LoginActivity, SharePreference.isLogin, true)
        setStringPref(this@LoginActivity, userId, dataResponse?.getId().toString())
        setStringPref(this@LoginActivity, loginType, dataResponse?.getLoginType().toString())
        setStringPref(this@LoginActivity, userName, dataResponse?.getName().toString())
        setStringPref(this@LoginActivity, userMobile, dataResponse?.getMobile().toString())
        setStringPref(this@LoginActivity, userEmail, dataResponse?.getEmail().toString())
        setStringPref(this@LoginActivity, userProfile, dataResponse?.getProfile_image().toString())
        setStringPref(this@LoginActivity, SharePreference.notificationStatus, dataResponse?.getNotificationStatus()!!)
        setStringPref(this@LoginActivity, SharePreference.emailStatus, dataResponse?.emailStatus()!!)
        setStringPref(this@LoginActivity, SharePreference.wallet, dataResponse?.getWallet()?:"0.00")

        setStringPref(
            this@LoginActivity,
            userRefralCode,
            dataResponse?.getReferral_code().toString()
        )
        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
        finish()
        finishAffinity()
    }

}