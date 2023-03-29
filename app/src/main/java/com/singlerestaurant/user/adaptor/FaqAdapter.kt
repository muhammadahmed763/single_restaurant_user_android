package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.singlerestaurant.user.databinding.CellFaqBinding
import com.singlerestaurant.user.model.FaqData
import com.singlerestaurant.user.model.GalleryData
import java.util.ArrayList

class FaqAdapter(var context: Activity,
                 private val faqList: ArrayList<FaqData>,
                 private val itemClick: (Int, String) -> Unit,):RecyclerView.Adapter<FaqAdapter.FaqViewHolder>()  {
    inner class FaqViewHolder(var itemBinding:CellFaqBinding):RecyclerView.ViewHolder(itemBinding.root)
    {
        fun bindItems(data:FaqData)=with(itemBinding)
        {
            itemBinding.tvSubTitle.text= data.description.toString()
            itemBinding.tvTitle.text= data.title.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val view=CellFaqBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FaqViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        holder.bindItems(faqList[position])
    }

    override fun getItemCount(): Int {

    return faqList.size
    }
}