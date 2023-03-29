package com.singlerestaurant.user.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.singlerestaurant.user.R
import com.singlerestaurant.user.adaptor.BlogsAdapter
import com.singlerestaurant.user.adaptor.GalleryAdapter
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.ActBlogsBinding
import com.singlerestaurant.user.model.BlogsData
import com.singlerestaurant.user.model.ItemImage
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.SpacingItemDecorator
import kotlinx.coroutines.launch

class ActBlogs : AppCompatActivity() {
    private lateinit var binding:ActBlogsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActBlogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            finish()
        }
        if (SharePreference.getStringPref(
                this@ActBlogs,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))) {

            binding.ivBack.rotation=180f
        }else{
            binding.ivBack.rotation=0f

        }
        callBlogsApi()

    }


    private fun callBlogsApi() {
        Common.showLoadingProgress(this@ActBlogs)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().getBlogs()) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    binding.clBlogs.visibility= View.VISIBLE
                    val responseBody = response.body
                    if((responseBody.data?.size ?: 0) > 0)
                    {
                        binding.rvBlogs.visibility=View.VISIBLE
                        binding.tvNoDataFound.visibility=View.GONE
                    }else
                    {
                        binding.rvBlogs.visibility=View.GONE
                        binding.tvNoDataFound.visibility=View.VISIBLE
                    }
                    setupAdapter(responseBody.data)
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActBlogs,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )

                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActBlogs,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActBlogs,
                        resources.getString(R.string.error_msg)
                    )


                }


            }
        }


    }

    private fun setupAdapter(blogList: ArrayList<BlogsData>?) {
        binding.rvBlogs.layoutManager = LinearLayoutManager(this@ActBlogs)
        val x = (resources.displayMetrics.density * 4).toInt() //converting dp to pixels
        binding.rvBlogs.addItemDecoration(SpacingItemDecorator(x)) //setting space between items in RecyclerView
        binding.rvBlogs.adapter = blogList?.let {
            BlogsAdapter(this@ActBlogs, it) { data ->

                startActivity(Intent(this@ActBlogs,ActBlogsDetail::class.java).putExtra("BlogsData",data))

            }
        }
    }
}