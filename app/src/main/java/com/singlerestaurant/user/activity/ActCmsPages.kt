package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.View
import android.webkit.WebViewClient
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_COMPACT
import androidx.lifecycle.lifecycleScope
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.ActBlogsDetailBinding
import com.singlerestaurant.user.databinding.ActCmsPagesBinding
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import kotlinx.coroutines.launch

class ActCmsPages : AppCompatActivity() {
    private lateinit var binding: ActCmsPagesBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActCmsPagesBinding.inflate(layoutInflater)
        setContentView(binding.root)





        binding.ivBack.setOnClickListener {
            finish()
        }

        if (SharePreference.getStringPref(
                this@ActCmsPages,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))) {

            binding.ivBack.rotation=180f
        }else{
            binding.ivBack.rotation=0f

        }

        binding.webView.webViewClient = WebViewClient()
        binding.webView.settings.loadsImagesAutomatically = true
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        if(intent.getStringExtra("title")==resources.getString(R.string.about_us))
        {
            binding.tvTitle.text = resources.getString(R.string.about_us)
            binding.webView.loadData(SharePreference.getStringPref(this@ActCmsPages, SharePreference.aboutUs).toString(), "text/html", "UTF-8");
        }else
        {
            callCmsPages()
        }
    }

    @SuppressLint("NewApi")
    private fun callCmsPages() {
        Common.showLoadingProgress(this@ActCmsPages)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().cmsPages()) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    val responseBody = response.body
                    if (intent.getStringExtra("title") == "privacypolicy") {
                        binding.tvTitle.text = resources.getString(R.string.privacy_policy)
                        binding.webView.loadData(responseBody.privacypolicy.toString(), "text/html", "UTF-8");

                    } else if (intent.getStringExtra("title") == "termscondition") {
                        binding.tvTitle.text = resources.getString(R.string.terms_amp_condition)
                        binding.webView.loadData(responseBody.termscondition.toString(), "text/html", "UTF-8");

                    }

                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActCmsPages,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )

                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActCmsPages,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActCmsPages,
                        resources.getString(R.string.error_msg)
                    )


                }


            }
        }


    }
}