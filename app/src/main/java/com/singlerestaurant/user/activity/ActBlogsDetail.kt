package com.singlerestaurant.user.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.databinding.ActBlogsBinding
import com.singlerestaurant.user.databinding.ActBlogsDetailBinding
import com.singlerestaurant.user.model.BlogsData
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference

class ActBlogsDetail : AppCompatActivity() {
    var data=BlogsData()
    private lateinit var binding:ActBlogsDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActBlogsDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        data =intent.getParcelableExtra<BlogsData>("BlogsData") as BlogsData

        binding.tvBlogsName.text=data.title.toString()
        Glide.with(this@ActBlogsDetail).load(data.imageUrl)
            .transition(DrawableTransitionOptions.withCrossFade(500)).into(binding.ivUserProfile)
        binding.tvDate.text= Common.getDate(data.date.toString())
        binding.tvDescription.text=data.description.toString()
        binding.ivBack.setOnClickListener {
            finish()
        }
        if (SharePreference.getStringPref(
                this@ActBlogsDetail,
                SharePreference.SELECTED_LANGUAGE
            ).equals(resources.getString(R.string.language_hindi))) {

            binding.ivBack.rotation=180f
        }else{
            binding.ivBack.rotation=0f

        }
    }
}