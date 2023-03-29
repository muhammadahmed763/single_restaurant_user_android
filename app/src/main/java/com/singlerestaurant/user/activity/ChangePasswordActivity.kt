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
import com.singlerestaurant.user.databinding.ActivityChangepasswordBinding
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Common.alertErrorOrValidationDialog
import com.singlerestaurant.user.utils.Common.dismissLoadingProgress
import com.singlerestaurant.user.utils.Common.isCheckNetwork
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import kotlinx.coroutines.launch

class ChangePasswordActivity : BaseActivity() {
    private lateinit var binding: ActivityChangepasswordBinding
    override fun setLayout(): View = binding.root
    override fun InitView() {
        binding = ActivityChangepasswordBinding.inflate(layoutInflater)
        Common.getCurrentLanguage(this@ChangePasswordActivity, false)
        if (SharePreference.getStringPref(
                this@ChangePasswordActivity,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))
        ) {
            binding.ivBack.rotation = 180F
        } else {
            binding.ivBack.rotation = 0F
        }
    }

    fun onClick(v: View?) {
        when (v!!.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.tvSubmit -> {
                if(ApiClient.System_environment==Constants.SendBox)
                {
                    Common.showErrorFullMsg(this@ChangePasswordActivity, resources.getString(R.string.send_box_error_alert))

                }else
                {



                if (binding.edOldPass.text.toString() == "") {
                    Common.showErrorFullMsg(
                        this@ChangePasswordActivity,
                        resources.getString(R.string.validation_all)
                    )
                } else if (binding.edNewPassword.text.toString() == "") {
                    Common.showErrorFullMsg(
                        this@ChangePasswordActivity,
                        resources.getString(R.string.validation_all)
                    )
                } else if (binding.edConfirmPassword.text.toString() == "") {
                    Common.showErrorFullMsg(
                        this@ChangePasswordActivity,
                        resources.getString(R.string.validation_all)
                    )
                } else if (binding.edConfirmPassword.text.toString() != binding.edNewPassword.text.toString()) {
                    Common.showErrorFullMsg(
                        this@ChangePasswordActivity,
                        resources.getString(R.string.validation_valid_cpassword)
                    )
                } else {
                    if (SharePreference.getBooleanPref(
                            this@ChangePasswordActivity,
                            SharePreference.isLogin
                        )
                    ) {
                        val hasmap = HashMap<String, String>()
                        hasmap["user_id"] =
                            SharePreference.getStringPref(
                                this@ChangePasswordActivity,
                                SharePreference.userId
                            )!!
                        hasmap["old_password"] = binding.edOldPass.text.toString()
                        hasmap["new_password"] = binding.edNewPassword.text.toString()
                        if (isCheckNetwork(this@ChangePasswordActivity)) {
                            callApiChangePassword(hasmap)

                        } else {
                            alertErrorOrValidationDialog(
                                this@ChangePasswordActivity,
                                resources.getString(R.string.no_internet)
                            )
                        }
                    } else {
                        openActivity(LoginActivity::class.java)
                        finish()
                        finishAffinity()
                    }

                }
                }
            }
        }
    }


    private fun callApiChangePassword(hasmap: HashMap<String, String>) {
        Common.showLoadingProgress(this@ChangePasswordActivity)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setChangePassword(hasmap)) {
                is NetworkResponse.Success -> {
                    val restResponse: SingleResponsee = response.body
                    if (restResponse.status == 1) {
                        dismissLoadingProgress()
                        successfulDialog(
                            this@ChangePasswordActivity,
                            restResponse.message.toString()
                        )
                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ChangePasswordActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ChangePasswordActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ChangePasswordActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }


    fun successfulDialog(act: Activity, msg: String?) {
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
        Common.getCurrentLanguage(this@ChangePasswordActivity, false)
    }
}