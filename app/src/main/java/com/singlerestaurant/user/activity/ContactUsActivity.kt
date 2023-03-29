package com.singlerestaurant.user.activity

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.api.SingleResponsee
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivityContactUsBinding
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import kotlinx.coroutines.launch


class ContactUsActivity : BaseActivity() {
    private lateinit var binding:ActivityContactUsBinding
    override fun setLayout(): View = binding.root

    override fun InitView() {
        binding=ActivityContactUsBinding.inflate(layoutInflater)
        val adminResponse = SharePreference.getStringPref(this@ContactUsActivity, SharePreference.admin)
        setProfileData()

        binding.ivBack.setOnClickListener {
            finish()
        }

        if (SharePreference.getStringPref(
                this@ContactUsActivity,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))) {

            binding.ivBack.rotation=180f
        }else{
            binding. ivBack.rotation=0f

        }
        binding.linerPhone.setOnClickListener {
            openPhone(SharePreference.getStringPref(this@ContactUsActivity,SharePreference.adminMobile).toString())
        }

        binding. linearEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:") // only email apps should handle this

            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(SharePreference.getStringPref(this@ContactUsActivity,SharePreference.adminEmail)))
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }
        binding.tvAddress.setOnClickListener {
            try {
                val gmmIntentUri = Uri.parse("geo:0,0?q=${binding.tvAddress.text}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)

            }catch (e:Exception)
            {

            }

        }

        binding.ivFacebook.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(SharePreference.getStringPref(this@ContactUsActivity,SharePreference.facebookLink))
            startActivity(i)
        }

        binding.ivInstagram.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(SharePreference.getStringPref(this@ContactUsActivity,SharePreference.instaLink))
            startActivity(i)
        }

        binding.ivYoutube.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(SharePreference.getStringPref(this@ContactUsActivity,SharePreference.youtubeLink))
            startActivity(i)
        }
        binding.btnSubmit.setOnClickListener {
            when {
                binding.tvFirstName.text?.isEmpty() == true -> {
                    Common.showErrorFullMsg(
                        this@ContactUsActivity,
                        resources.getString(R.string.validation_all)
                    )

                }
                binding. tvLastName.text?.isEmpty() == true -> {
                    Common.showErrorFullMsg(
                        this@ContactUsActivity,
                        resources.getString(R.string.validation_all)
                    )

                }
                binding. edEmail.text?.isEmpty() == true -> {
                    Common.showErrorFullMsg(
                        this@ContactUsActivity,
                        resources.getString(R.string.validation_all)
                    )

                }
                binding. edMessage.text?.isEmpty() == true -> {
                    Common.showErrorFullMsg(
                        this@ContactUsActivity,
                        resources.getString(R.string.validation_all)
                    )

                }
                else -> {
                    val contactUsRequest = HashMap<String, String>()
                    contactUsRequest["firstname"] = binding.tvFirstName.text.toString()
                    contactUsRequest["email"] = binding.edEmail.text.toString()
                    contactUsRequest["lastname"] = binding.tvLastName.text.toString()
                    contactUsRequest["message"] = binding.edMessage.text.toString()
                    callApiContactUS(contactUsRequest)
                }
            }
        }


    }


    private fun openPhone(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phone")
        startActivity(intent)
    }





    private fun callApiContactUS(hasmap: HashMap<String, String>) {
        Common.showLoadingProgress(this@ContactUsActivity)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().contactUs(hasmap)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    val restResponce: SingleResponsee = response.body
                    if (restResponce.status==1) {
                        Common.dismissLoadingProgress()
                        Common.showSuccessFullMsg(this@ContactUsActivity, restResponce.message.toString())
                        finish()
                    } else if (restResponce.status==0) {
                        Common.dismissLoadingProgress()
                        Common.alertErrorOrValidationDialog(this@ContactUsActivity, restResponce.message)
                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ContactUsActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ContactUsActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ContactUsActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }

    private fun setProfileData() {
        binding.tvPhoneNumber.text = SharePreference.getStringPref(this@ContactUsActivity,SharePreference.adminMobile)
        binding. tvEmailAddress.text = SharePreference.getStringPref(this@ContactUsActivity,SharePreference.adminEmail)
        binding.tvAddress.text = SharePreference.getStringPref(this@ContactUsActivity,SharePreference.adminAddress)

    }


}