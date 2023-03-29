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
import com.singlerestaurant.user.databinding.CellTestimonialBinding
import com.singlerestaurant.user.model.RelateditemsItem
import com.singlerestaurant.user.model.TestimonialData
import java.util.*
import kotlin.collections.ArrayList

class TestimonialAdapter(
    private var context: Activity,
    private var reviewList: ArrayList<TestimonialData>
) : RecyclerView.Adapter<TestimonialAdapter.TestimonialViewHolder>() {


    inner class TestimonialViewHolder(var itemBinding: CellTestimonialBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun bindItems(
            context: Activity,
            data: TestimonialData,
            position: Int
        ) = with(itemBinding.root)
        {
            Glide.with(context).load(data.profileImage)
                .apply(RequestOptions.circleCropTransform()).transition(DrawableTransitionOptions.withCrossFade(500)).into(itemBinding.ivUser)
            itemBinding.tvUserName.text = data.name.toString()
            itemBinding.tvDescription.text = data.comment.toString()
            itemBinding.tvRatting.text =String.format(Locale.US, "%.1f",data.ratting?.toDouble()).plus(" / 5.0 Reviews")

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestimonialViewHolder {
        val view = CellTestimonialBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return TestimonialViewHolder(view)
    }

    override fun onBindViewHolder(holder: TestimonialViewHolder, position: Int) {

        holder.bindItems(context, reviewList[position], position)
    }

    override fun getItemCount(): Int {
        return reviewList.size
    }
}