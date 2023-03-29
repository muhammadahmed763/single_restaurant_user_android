package com.singlerestaurant.user.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.singlerestaurant.user.R
import com.singlerestaurant.user.adaptor.GalleryAdapter
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.ActGalleryBinding
import com.singlerestaurant.user.model.ItemImage
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.SpacingItemDecorator
import kotlinx.coroutines.launch

class ActGallery : AppCompatActivity() {

    private lateinit var binding: ActGalleryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener {
            finish()
        }
        if (SharePreference.getStringPref(
                this@ActGallery,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))) {

            binding.ivBack.rotation=180f
        }else{
            binding.ivBack.rotation=0f

        }
        callGalleryApi()
    }

    private fun callGalleryApi() {
        Common.showLoadingProgress(this@ActGallery)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().gallery()) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    val responseBody = response.body
                    if((responseBody.data?.size ?: 0) > 0)
                    {
                        binding.rvGallery.visibility= View.VISIBLE
                        binding.tvNoDataFound.visibility= View.GONE
                    }else
                    {
                        binding.rvGallery.visibility= View.GONE
                        binding.tvNoDataFound.visibility= View.VISIBLE
                    }
                    setupAdapter(responseBody.data)
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActGallery,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActGallery,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActGallery,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }


    private fun setupAdapter(galleryList: ArrayList<ItemImage>?) {
        binding.rvGallery.layoutManager = GridLayoutManager(this@ActGallery, 3, GridLayoutManager.VERTICAL, false)
        val x = (resources.displayMetrics.density * 4).toInt() //converting dp to pixels
        binding.rvGallery.addItemDecoration(SpacingItemDecorator(x)) //setting space between items in RecyclerView
        binding.rvGallery.adapter = galleryList?.let {
            GalleryAdapter(this@ActGallery, it) { s, i ->

            }
        }
    }
}