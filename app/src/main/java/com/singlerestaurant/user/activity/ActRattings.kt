package com.singlerestaurant.user.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.singlerestaurant.user.R
import com.singlerestaurant.user.adaptor.RattingAdapter
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.ActGetRattingBinding
import com.singlerestaurant.user.model.RattingDataItem
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import kotlinx.coroutines.launch
import java.util.HashMap

class ActRattings : AppCompatActivity() {
    private lateinit var binding: ActGetRattingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActGetRattingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnAddReview.setOnClickListener {
            if (SharePreference.getBooleanPref(this@ActRattings, SharePreference.isLogin)) {
                val intent = Intent(this@ActRattings, ActAddReview::class.java)
                activityResult.launch(intent)
            } else {
                startActivity(Intent(this@ActRattings, LoginActivity::class.java))
                finish()
                finishAffinity()
            }


        }
        callRattingApi()

        if (SharePreference.getStringPref(
                this@ActRattings,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))
        ) {

            binding.ivBack.rotation = 180f
        } else {
            binding.ivBack.rotation = 0f

        }

    }


    private fun callRattingApi() {
        Common.showLoadingProgress(this@ActRattings)
        val map = HashMap<String, String>()
        map["user_id"] = SharePreference.getStringPref(
            this@ActRattings,
            SharePreference.userId
        )!!
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().setRattingList(map)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    binding.clRatting.visibility = View.VISIBLE
                    val responseBody = response.body
                    if((responseBody.data?.size ?: 0) > 0)
                    {
                        binding.rvRattings.visibility=View.VISIBLE
                        binding.tvNoDataFound.visibility=View.GONE
                    }else
                    {
                        binding.rvRattings.visibility=View.GONE
                        binding.tvNoDataFound.visibility=View.VISIBLE
                    }

                    setupAdapter(responseBody.data)


                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActRattings,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActRattings,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActRattings,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }


    private var activityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == 200) {

                callRattingApi()
            }


        }


    private fun setupAdapter(galleryList: ArrayList<RattingDataItem>?) {
        binding.rvRattings.layoutManager = LinearLayoutManager(this@ActRattings)
        binding.rvRattings.adapter = galleryList?.let {
            RattingAdapter(this@ActRattings, it) { s, i ->

            }
        }
    }


}