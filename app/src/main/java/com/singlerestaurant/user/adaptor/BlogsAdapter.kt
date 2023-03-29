package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.singlerestaurant.user.databinding.CellBlogsBinding
import com.singlerestaurant.user.model.BlogsData
import com.singlerestaurant.user.model.ItemImage
import com.singlerestaurant.user.utils.Common
import java.util.ArrayList

class BlogsAdapter(
    var context: Activity,
    private val blogList: ArrayList<BlogsData>,
    private val itemClick: (BlogsData) -> Unit,
) : RecyclerView.Adapter<BlogsAdapter.BlogsViewHolder>() {

    inner class BlogsViewHolder(val binding: CellBlogsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItems(data: BlogsData, position: Int) = with(binding) {
            binding.tvBlogsName.text = data.title.toString()
            Glide.with(context).load(data.imageUrl).apply(RequestOptions.centerCropTransform())
                .transition(DrawableTransitionOptions.withCrossFade(500))
                .transform(RoundedCorners(16))

                .into(binding.ivUserProfile)
            binding.tvDate.text = Common.getDate(data.date.toString())
            binding.tvDescription.text = data.description.toString()
            itemView.setOnClickListener {
                itemClick(data)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogsViewHolder {
        val view = CellBlogsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BlogsViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogsViewHolder, position: Int) {
        holder.bindItems(blogList[position], position)
    }

    override fun getItemCount(): Int {
        return blogList.size
    }
}