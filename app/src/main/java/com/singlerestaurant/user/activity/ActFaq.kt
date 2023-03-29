package com.singlerestaurant.user.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.singlerestaurant.user.R
import com.singlerestaurant.user.adaptor.FaqAdapter
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.ActFaqBinding
import com.singlerestaurant.user.model.FaqData
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import kotlinx.coroutines.launch

class ActFaq : AppCompatActivity() {
    private lateinit var binding:ActFaqBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener {
            finish()
        }

        if (SharePreference.getStringPref(
                this@ActFaq,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))) {

            binding.ivBack.rotation=180f
        }else{
            binding.ivBack.rotation=0f

        }
        callFaqApi()
    }


    private fun callFaqApi() {
        Common.showLoadingProgress(this@ActFaq)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().getFaq()) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    val responseBody = response.body
                    if((responseBody.data?.size ?: 0) > 0)
                    {
                        binding.rvFaq.visibility= View.VISIBLE
                        binding.tvNoDataFound.visibility= View.GONE
                    }else
                    {
                        binding.rvFaq.visibility= View.GONE
                        binding.tvNoDataFound.visibility= View.VISIBLE
                    }
                    setupAdapter(responseBody.data)
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFaq,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFaq,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActFaq,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }


    private fun setupAdapter(faqList: ArrayList<FaqData>?) {
        binding.rvFaq.layoutManager = LinearLayoutManager(this@ActFaq)
        binding.rvFaq.adapter = faqList?.let {
            FaqAdapter(this@ActFaq, it) { s, i ->

            }
        }
    }
}