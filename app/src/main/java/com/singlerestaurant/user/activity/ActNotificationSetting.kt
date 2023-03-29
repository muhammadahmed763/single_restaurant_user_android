package com.singlerestaurant.user.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.ActNotificationSettingBinding
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import kotlinx.coroutines.launch

class ActNotificationSetting : AppCompatActivity() {
    private lateinit var binding: ActNotificationSettingBinding
    private var isNotification = ""
    private var isEmails = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActNotificationSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initClickListeners()
        isNotification = SharePreference.getStringPref(
            this@ActNotificationSetting,
            SharePreference.notificationStatus
        ) ?: ""
        isEmails =
            SharePreference.getStringPref(this@ActNotificationSetting, SharePreference.emailStatus)
                ?: ""

        binding.swEmail.isChecked = isEmails == "1"
        binding.swPushNotification.isChecked = isNotification == "1"
        if (SharePreference.getStringPref(
                this@ActNotificationSetting,
                SharePreference.SELECTED_LANGUAGE
            ).equals(
                resources.getString(R.string.language_hindi)
            )
        ) {
           binding.ivBack.rotation = 180F
        } else {
            binding.ivBack.rotation = 0F
        }

    }

    private fun initClickListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.swPushNotification.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked) {
                isNotification = "1"
                callNotificationStatus(isNotification, isEmails)

            } else {
                isNotification = "2"
                callNotificationStatus(isNotification, isEmails)

            }
        }

        binding.swEmail.setOnCheckedChangeListener { view, isChecked ->
            if (isChecked) {
                isEmails = "1"
                callNotificationStatus(isNotification, isEmails)

            } else {
                isEmails = "2"
                callNotificationStatus(isNotification, isEmails)
            }
        }
    }


    private fun callNotificationStatus(notificationStatus: String, emailStatus: String) {
        Common.showLoadingProgress(this@ActNotificationSetting)
        val map = HashMap<String, String>()
        map["user_id"] =
            SharePreference.getStringPref(this@ActNotificationSetting, SharePreference.userId) ?: ""
        map["notification_status"] = notificationStatus
        map["mail_status"] = emailStatus
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().isNotification(map)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()


                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActNotificationSetting,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActNotificationSetting,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActNotificationSetting,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }

}
