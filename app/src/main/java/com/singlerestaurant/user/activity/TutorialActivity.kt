package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.databinding.ActivityTutorialBinding
import com.singlerestaurant.user.model.TutorialData
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference.Companion.isTutorial
import com.singlerestaurant.user.utils.SharePreference.Companion.setBooleanPref
import kotlinx.coroutines.launch

class TutorialActivity : BaseActivity() {
    var imagelist = ArrayList<TutorialData>()
    private lateinit var binding:ActivityTutorialBinding
    override fun setLayout(): View {
        return binding.root
    }

    override fun InitView() {
        binding=ActivityTutorialBinding.inflate(layoutInflater)
        Common.getCurrentLanguage(this@TutorialActivity, false)
        callTutorialApi()

        binding. viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(i: Int, v: Float, i1: Int) {}
            override fun onPageSelected(i: Int) {
                if (i == imagelist.size - 1) {
                    binding.    tvBtnSkip.text = resources.getString(R.string.start_)
                } else {
                    binding. tvBtnSkip.text = resources.getString(R.string.skip)
                }
            }

            override fun onPageScrollStateChanged(i: Int) {

            }
        })

        binding.  tvBtnSkip.setOnClickListener {
            setBooleanPref(this@TutorialActivity, isTutorial, true)
            openActivity(DashboardActivity::class.java)
            finish()
        }


        binding. ivNext.setOnClickListener {

            if(imagelist.size>0) {


                if (binding.viewPager.currentItem == imagelist.size - 1) {
                    setBooleanPref(this@TutorialActivity, isTutorial, true)
                    openActivity(DashboardActivity::class.java)
                    finish()
                } else {
                    binding.viewPager.currentItem = binding.viewPager.currentItem + 1
                }
            }else
            {
                setBooleanPref(this@TutorialActivity, isTutorial, true)
                openActivity(DashboardActivity::class.java)
                finish()
            }
        }
    }

    private fun callTutorialApi() {
        Common.showLoadingProgress(this@TutorialActivity)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().tutorial()) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    val responseBody = response.body
                    responseBody.data?.let { imagelist.addAll(it) }
                    if(imagelist.size==0)
                    {
                        setBooleanPref(this@TutorialActivity, isTutorial, true)
                        openActivity(DashboardActivity::class.java)
                        finish()
                    }else
                    {
                        binding.viewPager.adapter = StartScreenAdapter(this@TutorialActivity, imagelist)
                        binding.  tabLayout.setupWithViewPager(binding.viewPager, true)
                    }

                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@TutorialActivity,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@TutorialActivity,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@TutorialActivity,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }

    class StartScreenAdapter(var mContext: Context, var mImagelist: ArrayList<TutorialData>) :
        PagerAdapter() {
        @SuppressLint("SetTextI18n")
        override fun instantiateItem(collection: ViewGroup, position: Int): Any {
            val inflater = LayoutInflater.from(mContext)
            val layout = inflater.inflate(R.layout.row_tutorial, collection, false) as ViewGroup
            val iv: ImageView = layout.findViewById(R.id.ivScreen)
            val tvTitle: AppCompatTextView = layout.findViewById(R.id.tvTitle)
            val tvDescription: AppCompatTextView = layout.findViewById(R.id.tvDescription)
            Glide.with(mContext).load(mImagelist[position].imageUrl).transition (
                DrawableTransitionOptions.withCrossFade(500)).into(iv)
            tvTitle.text = mImagelist[position].title
            tvDescription.text = mImagelist[position].description
            collection.addView(layout)
            return layout
        }

        override fun destroyItem(
            collection: ViewGroup,
            position: Int,
            view: Any
        ) {
            collection.removeView(view as View)
        }

        override fun getCount(): Int {
            return mImagelist.size
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@TutorialActivity, false)
    }
}