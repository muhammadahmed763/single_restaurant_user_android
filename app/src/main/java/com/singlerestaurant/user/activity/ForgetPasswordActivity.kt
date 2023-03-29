package com.singlerestaurant.user.activity

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.api.SingleResponsee
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivityForgetpasswordBinding
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import kotlinx.coroutines.launch

class ForgetPasswordActivity : BaseActivity() {
    private lateinit var binding:ActivityForgetpasswordBinding
    override fun setLayout(): View {
        return binding.root
    }

    override fun InitView() {
        binding=ActivityForgetpasswordBinding.inflate(layoutInflater)
        Common.getCurrentLanguage(this@ForgetPasswordActivity, false)


        if (SharePreference.getStringPref(this@ForgetPasswordActivity, SharePreference.SELECTED_LANGUAGE).equals(resources.getString(R.string.language_hindi))) {
            binding.ivBack.rotation = 180F
        } else {
            binding. ivBack.rotation = 0F
        }
    }

    fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivBack -> {
                onBackPressed()
            }
            R.id.tvSubmit -> {
                if(ApiClient.System_environment==Constants.SendBox)
                {
                    Common.showErrorFullMsg(this@ForgetPasswordActivity, resources.getString(R.string.send_box_error_alert))

                }else
                {



                if (binding.edEmail.text.toString() == "") {
                    Common.showErrorFullMsg(this@ForgetPasswordActivity, resources.getString(R.string.validation_all))
                } else if (!Common.isValidEmail(binding.edEmail.text.toString())) {
                    Common.showErrorFullMsg(this@ForgetPasswordActivity, resources.getString(R.string.validation_valid_email))
                } else {
                    val hasmap = HashMap<String, String>()
                    hasmap["email"] = binding.edEmail.text.toString()
                    if (Common.isCheckNetwork(this@ForgetPasswordActivity)) {
                        callApiForgetPassword(hasmap)
//                        Common.alertErrorOrValidationDialog(
//                            this@ForgetPasswordActivity,
//                            "Not Allowed in Demo Mode"
//                        )
                    } else {
                        Common.alertErrorOrValidationDialog(
                                this@ForgetPasswordActivity,
                                resources.getString(R.string.no_internet)
                        )
                    }
                }}
            }
            R.id.tvSignup -> {
                openActivity(RegistrationActivity::class.java)
                finish()
                finishAffinity()
            }
        }
    }


    private fun callApiForgetPassword(hasmap: HashMap<String, String>) {
        Common.showLoadingProgress(this@ForgetPasswordActivity)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setforgotPassword(hasmap)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    val restResponse: SingleResponsee = response.body
                    if (restResponse.status==1) {
                        successfulDialog(this@ForgetPasswordActivity, restResponse.message.toString())
                    }
                    else {
                        successfulDialog(this@ForgetPasswordActivity, restResponse.message.toString())
                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ForgetPasswordActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ForgetPasswordActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ForgetPasswordActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }



    private fun successfulDialog(act: Activity, msg: String?) {
        var dialog: Dialog? = null
        try {

            dialog?.dismiss()
            dialog = Dialog(act, R.style.AppCompatAlertDialogStyleBig)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            val mInflater = LayoutInflater.from(act)
            val mView = mInflater.inflate(R.layout.dlg_validation, null, false)
            val textDesc: TextView = mView.findViewById(R.id.tvMessage)
            textDesc.text = msg
            val tvOk: TextView = mView.findViewById(R.id.tvOk)
            val finalDialog: Dialog = dialog
            tvOk.setOnClickListener {
                finalDialog.dismiss()
                finish()
            }
            dialog.setContentView(mView)
            dialog.show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@ForgetPasswordActivity, false)
    }
}