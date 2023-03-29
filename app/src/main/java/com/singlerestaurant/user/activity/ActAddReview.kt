package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.api.SingleResponsee
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivityActAddReviewBinding
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import kotlinx.coroutines.launch
import java.util.*

class ActAddReview : BaseActivity() {
    private lateinit var binding: ActivityActAddReviewBinding
    override fun setLayout(): View = binding.root

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun InitView() {
        binding = ActivityActAddReviewBinding.inflate(layoutInflater)
        var rattingValue = 1
        binding.ivStar1.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.colorPrimary))
        binding.ivStar2.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
        binding.ivStar3.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
        binding.ivStar4.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
        binding.ivStar5.imageTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
        binding.ivStar1.setOnClickListener {
            binding.ivStar1.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar2.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
            binding.ivStar3.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
            binding.ivStar4.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
            binding.ivStar5.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
            rattingValue = 1
        }
        binding.ivStar2.setOnClickListener {
            binding.ivStar1.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar2.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar3.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
            binding.ivStar4.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
            binding.ivStar5.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
            rattingValue = 2
        }
        binding.ivStar3.setOnClickListener {
            binding.ivStar1.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar2.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar3.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar4.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
            binding.ivStar5.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
            rattingValue = 3
        }
        binding.ivStar4.setOnClickListener {
            binding.ivStar1.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar2.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar3.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar4.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar5.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(this@ActAddReview, R.color.gray))
            rattingValue = 4
        }
        binding.ivStar5.setOnClickListener {
            binding.ivStar1.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar2.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar3.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar4.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            binding.ivStar5.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@ActAddReview,
                    R.color.colorPrimary
                )
            )
            rattingValue = 5
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
        if (SharePreference.getStringPref(
                this@ActAddReview,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))
        ) {

            binding.ivBack.rotation = 180f
        } else {
            binding.ivBack.rotation = 0f

        }

        binding.btnSubmit.setOnClickListener {
            if (SharePreference.getBooleanPref(this@ActAddReview, SharePreference.isLogin)) {
                if (binding.edDescription.text.toString() != "") {
                    if (Common.isCheckNetwork(this@ActAddReview)) {
                        callApiPutRatting(
                            rattingValue.toString(),
                            binding.edDescription.text.toString()
                        )
                    } else {
                        Common.alertErrorOrValidationDialog(
                            this@ActAddReview,
                            resources.getString(R.string.no_internet)
                        )
                    }
                }
            } else {
                startActivity(Intent(this@ActAddReview, LoginActivity::class.java))
                finish()
                finishAffinity()
            }
        }
    }

    @SuppressLint("NewApi")
    private fun callApiPutRatting(rattingValue: String, discription: String) {
        Common.showLoadingProgress(this@ActAddReview)
        val map = HashMap<String, String>()
        map["user_id"] = SharePreference.getStringPref(
            this@ActAddReview,
            SharePreference.userId
        )!!
        map["ratting"] = rattingValue
        map["comment"] = discription
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setRatting(map)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    val restResponse: SingleResponsee = response.body
                    if (restResponse.status == 1) {
                        val c: Calendar = Calendar.getInstance()
                        println("Current time => " + c.time)
                        val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        df.format(c.time)
                        if (Common.isCheckNetwork(this@ActAddReview)) {
                            finish()
                            setResult(200)
                        } else {
                            Common.alertErrorOrValidationDialog(
                                this@ActAddReview,
                                resources.getString(R.string.no_internet)
                            )
                        }

                    } else {
                        Common.showErrorFullMsg(
                            this@ActAddReview,
                            response.body.message.toString()
                        )
                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActAddReview,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActAddReview,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActAddReview,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }

}