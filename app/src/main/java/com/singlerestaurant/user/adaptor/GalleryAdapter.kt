package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.activity.ImageSliderActivity
import com.singlerestaurant.user.databinding.CellGalleryBinding
import com.singlerestaurant.user.model.GalleryData
import com.singlerestaurant.user.model.GalleryResponse
import com.singlerestaurant.user.model.ItemImage
import java.util.ArrayList

class GalleryAdapter(
    var context: Activity,
    private val imageList: ArrayList<ItemImage>,
    private val itemClick: (Int, String) -> Unit,
) :
    RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    inner class GalleryViewHolder(var itemBinding: CellGalleryBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItem(context: Activity, data:ItemImage,position: Int)= with(itemBinding)
        {
            Glide.with(context).load(data.image_url).transition(DrawableTransitionOptions.withCrossFade(500)).into(itemBinding.ivGallery)

            itemView.setOnClickListener {
             context.startActivity(
                    Intent(
                        context,
                        ImageSliderActivity::class.java
                    ).putParcelableArrayListExtra("imageList", imageList).putExtra("pos", position)
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = CellGalleryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bindItem(context,imageList[position],position)

    }

    override fun getItemCount(): Int {
        return imageList.size
    }
}