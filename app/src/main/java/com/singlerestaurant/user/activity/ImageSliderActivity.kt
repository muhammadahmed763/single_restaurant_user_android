package com.singlerestaurant.user.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.singlerestaurant.user.R
import com.singlerestaurant.user.base.BaseActivity
import com.singlerestaurant.user.utils.Common
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.singlerestaurant.user.databinding.ActivityImagepagerBinding
import com.singlerestaurant.user.model.ItemImage

class ImageSliderActivity : BaseActivity() {
    private lateinit var binding:ActivityImagepagerBinding
    override fun setLayout(): View =binding.root

    override fun InitView() {
        binding=ActivityImagepagerBinding.inflate(layoutInflater)
      val  imgList = intent.getParcelableArrayListExtra<ItemImage>("imageList")
        val pos =intent.getIntExtra("pos",0)
        val imageSlider = imgList?.let { ImageSlider(this@ImageSliderActivity, it) }
        binding. pager.adapter = imageSlider
        binding. pager.currentItem=pos

        binding. ivCancel.setOnClickListener {
            finish()
        }
    }


    inner class ImageSlider(var mContext: Context, var imageList: ArrayList<ItemImage>) : PagerAdapter() {
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return imageList.size
        }

        override fun instantiateItem(view: ViewGroup, position: Int): Any {
            val inflater = LayoutInflater.from(mContext)
            val itemView = inflater.inflate(R.layout.row_full_screen, view, false) as ViewGroup
            val mImageView = itemView.findViewById<View>(R.id.img_pager) as ImageView
            Glide.with(mContext).load(imageList[position].image_url)
                .transition(DrawableTransitionOptions.withCrossFade(500))
                    .into(mImageView)

            (view as ViewPager).addView(itemView)
            return itemView
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            (container as ViewPager).removeView(`object` as View?)
        }

    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        Common.getCurrentLanguage(this@ImageSliderActivity, false)
    }
}