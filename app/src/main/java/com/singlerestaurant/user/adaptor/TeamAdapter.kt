package com.singlerestaurant.user.adaptor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.singlerestaurant.user.R
import com.singlerestaurant.user.databinding.CellTeamBinding
import com.singlerestaurant.user.model.BlogsData
import com.singlerestaurant.user.model.TeamData
import java.util.ArrayList

class TeamAdapter(var context: Activity,
                  private val teamList: ArrayList<TeamData>,
                  private val itemClick: (Int, String) -> Unit):RecyclerView.Adapter<TeamAdapter.TeamViewHolder>() {
    inner class TeamViewHolder(val binding: CellTeamBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindItems(data:TeamData) = with(binding)
        {
            binding.tvName.text=data.title.toString()
            binding.tvRole.text=data.subtitle.toString()
            Glide.with(context).load(data.imageUrl).apply(RequestOptions.circleCropTransform())
                .transition(DrawableTransitionOptions.withCrossFade(500)).into(binding.ivUserProfile)
            binding.tvDescription.text=data.description.toString()


            binding.ivFacebook.setOnClickListener {
            try {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(data.fb)
               context.startActivity(i)
            }catch (e:Exception)
            {
                Toast.makeText(context,context.resources.getString(R.string.invalid_link),Toast.LENGTH_SHORT).show()

            }
            }
            binding.ivInstagram.setOnClickListener {
                try {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(data.insta)
                    context.startActivity(i)
                }catch (e:Exception)
                {
                    Toast.makeText(context,context.resources.getString(R.string.invalid_link),Toast.LENGTH_SHORT).show()

                }

            }
            binding.ivYoutube.setOnClickListener {
                try {
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = Uri.parse(data.youtube)
                    context.startActivity(i)
                }catch (e:Exception)
                {
                Toast.makeText(context,context.resources.getString(R.string.invalid_link),Toast.LENGTH_SHORT).show()
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view=CellTeamBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return TeamViewHolder(view)

    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bindItems(teamList[position])
    }

    override fun getItemCount(): Int {
        return teamList.size
    }
}