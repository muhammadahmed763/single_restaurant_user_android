package com.singlerestaurant.user.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.singlerestaurant.user.R
import com.singlerestaurant.user.adaptor.TeamAdapter
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.ActOurTeamBinding
import com.singlerestaurant.user.model.TeamData
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.SharePreference
import com.singlerestaurant.user.utils.SpacingItemDecorator
import kotlinx.coroutines.launch

class ActOurTeam : AppCompatActivity() {
    private lateinit var binding:ActOurTeamBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActOurTeamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener {
            finish()
        }
        if (SharePreference.getStringPref(this@ActOurTeam, SharePreference.SELECTED_LANGUAGE).equals(resources.getString(R.string.language_hindi))) {
            binding.ivBack.rotation=180f
        }else{
            binding.ivBack.rotation=0f
        }
        callTeamApi()
    }


    private fun callTeamApi() {
        Common.showLoadingProgress(this@ActOurTeam)

        lifecycleScope.launch {
            when (val response = ApiClient.getClient().getTeamData()) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    val responseBody = response.body
                    binding.clTeam.visibility=View.VISIBLE
                    if((responseBody.data?.size ?: 0) > 0)
                    {
                        binding.rvOurTeam.visibility=View.VISIBLE
                        binding.tvNoDataFound.visibility=View.GONE
                    }else
                    {
                        binding.rvOurTeam.visibility=View.GONE
                        binding.tvNoDataFound.visibility=View.VISIBLE
                    }
                    setupAdapter(responseBody.data)
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActOurTeam,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActOurTeam,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActOurTeam,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }

    private fun setupAdapter(blogList: ArrayList<TeamData>?) {
        binding.rvOurTeam.layoutManager = LinearLayoutManager(this@ActOurTeam)
        val x = (resources.displayMetrics.density * 4).toInt() //converting dp to pixels
        binding.rvOurTeam.addItemDecoration(SpacingItemDecorator(x)) //setting space between items in RecyclerView
        binding.rvOurTeam.adapter = blogList?.let {
            TeamAdapter(this@ActOurTeam, it) { s, i ->

            }
        }
    }
}