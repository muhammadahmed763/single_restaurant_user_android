package com.singlerestaurant.user.activity

import android.view.View
import android.webkit.WebViewClient
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivityTermsAndConditionBinding
import com.singlerestaurant.user.utils.SharePreference


class TermsAndConditionActivity : BaseActivity() {
    private lateinit var binding: ActivityTermsAndConditionBinding
    override fun setLayout(): View = binding.root
    override fun InitView() {
        binding = ActivityTermsAndConditionBinding.inflate(layoutInflater)
        binding.ivBack.setOnClickListener {
            finish()
        }
        binding.webView.webViewClient = WebViewClient()
        binding.webView.settings.loadsImagesAutomatically = true
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
//        binding.webView.loadUrl(ApiClient.termscondition)
//        binding.webView.loadData(yourData, "text/html", "UTF-8");


        if (SharePreference.getStringPref(
                this@TermsAndConditionActivity,
                SharePreference.SELECTED_LANGUAGE
            )
                .equals(resources.getString(R.string.language_hindi))
        ) {
            binding.ivBack.rotation = 180F
        } else {
            binding.ivBack.rotation = 0F
        }
    }
}