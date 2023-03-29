package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.databinding.CellRattingBinding
import com.singlerestaurant.user.model.RattingDataItem
import com.singlerestaurant.user.utils.Common

class RattingAdapter(var context: Activity,
                     private val rattingList: ArrayList<RattingDataItem>,
                     private val itemClick: (Int, String) -> Unit,):RecyclerView.Adapter<RattingAdapter.RattingViewHolder>() {

    inner class RattingViewHolder(var itemBinding:CellRattingBinding):RecyclerView.ViewHolder(itemBinding.root)
    {
        fun bindItems(context: Activity,data: RattingDataItem)=with(itemBinding)
        {
            Glide.with(context).load(data.profileImage).apply(RequestOptions.circleCropTransform())
                .transition(DrawableTransitionOptions.withCrossFade(500)).into(itemBinding.ivUserProfile)
            itemBinding.tvRattingName.text=data.name
            itemBinding.tvRattingMessage.text=data.comment.toString()
            itemBinding.tvDate.text=Common.getDate(data.date.toString())
            when {
                data.ratting.equals("1") -> {
                    itemBinding.ivRatting.setImageDrawable(
                        ResourcesCompat.getDrawable(
                           context.resources,
                            R.drawable.ratting1,
                            null
                        )
                    )
                }
                data.ratting.equals("2") -> {
                    itemBinding.ivRatting.setImageDrawable(
                        ResourcesCompat.getDrawable(
                           context.resources,
                            R.drawable.ratting2,
                            null
                        )
                    )
                }
                data.ratting.equals("3") -> {
                    itemBinding.ivRatting.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            context.resources,
                            R.drawable.ratting3,
                            null
                        )
                    )
                }
                data.ratting.equals("4") -> {
                    itemBinding.ivRatting.setImageDrawable(
                        ResourcesCompat.getDrawable(
                           context.resources,
                            R.drawable.ratting4,
                            null
                        )
                    )
                }
                data.ratting.equals("5") -> {
                    itemBinding.ivRatting.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            context.resources,
                            R.drawable.ratting5,
                            null
                        )
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RattingViewHolder {
        val view=CellRattingBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return RattingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RattingViewHolder, position: Int) {
        holder.bindItems(context,rattingList[position])
    }

    override fun getItemCount(): Int {
        return rattingList.size
    }
}