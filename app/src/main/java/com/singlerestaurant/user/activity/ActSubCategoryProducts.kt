package com.singlerestaurant.user.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.singlerestaurant.user.R
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.ActSubCategoryProductsBinding
import com.singlerestaurant.user.fragment.DynamicFragment
import com.singlerestaurant.user.model.ItemsItem
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class ActSubCategoryProducts : AppCompatActivity() {
    private var categories = ArrayList<ItemsItem>()
    private var catId = ""
    private lateinit var binding: ActSubCategoryProductsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActSubCategoryProductsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initClickListeners()
        catId = intent.getStringExtra("category_id") ?: ""
        callCategoriesItem(catId)


    }


    private fun initMediator() {

        binding.viewpager?.let {
            TabLayoutMediator(
                binding.tabLayout, it,
            ) { tab: TabLayout.Tab, position: Int ->


                var textView: TextView? = null

                textView = tab.customView as TextView?

                textView?.typeface = ResourcesCompat.getFont(this, R.font.poppins_regular)
                tab.text = categories[position].subcategoryName

            }.attach()
        }

    }


    private fun initView() {

        binding.tvTitle.text = intent.getStringExtra("category_name") ?: ""
        if (SharePreference.getStringPref(
                this@ActSubCategoryProducts,
                SharePreference.SELECTED_LANGUAGE
            ).equals(
                resources.getString(R.string.language_hindi)
            )
        ) {
            binding.ivBack?.rotation = 180F
        } else {
            binding.ivBack?.rotation = 0F
        }

    }

    private fun initClickListeners() {
        binding.ivBack.setOnClickListener {
            if (intent.getBooleanExtra(Constants.FromNotification, false)) {
                val i = Intent(this@ActSubCategoryProducts, DashboardActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
                finish()
            } else {
                finish()

            }
        }
    }


    private fun callCategoriesItem(cat_id: String) {
        Common.showLoadingProgress(this@ActSubCategoryProducts)
        val map = HashMap<String, String>()
        map["user_id"] =
            SharePreference.getStringPref(this@ActSubCategoryProducts, SharePreference.userId) ?: ""
        map["cat_id"] = cat_id
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().categoryItems(map)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    val responseBody = response.body
                    categories.clear()
                    responseBody.items?.let { categories.addAll(it) }
                    setupViewPagerAdapter()
                    initMediator()
                    binding.clSubCatProducts.visibility = View.VISIBLE

                    if (categories.size > 0) {
                        binding.viewpager.visibility = View.VISIBLE
                        binding.tvNoDataFound.visibility = View.GONE
                    } else {
                        binding.viewpager.visibility = View.GONE
                        binding.tvNoDataFound.visibility = View.VISIBLE
                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActSubCategoryProducts,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActSubCategoryProducts,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActSubCategoryProducts,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }

    /* private val refreshData: BroadcastReceiver = object : BroadcastReceiver() {
         override fun onReceive(context: Context?, intent: Intent?) {
             try {


                 callCategoriesItem(catId)
             } catch (e: Exception) {
                 e.printStackTrace()
                 Log.e("Message", e.message.toString())
             }
         }
     }

     override fun onResume() {
         super.onResume()
         LocalBroadcastManager.getInstance(this).registerReceiver(refreshData, IntentFilter("refreshData"))
     }

     override fun onPause() {
         super.onPause()
         LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshData)



     }

     override fun onDestroy() {
         super.onDestroy()
         LocalBroadcastManager.getInstance(this).unregisterReceiver(refreshData)

     }*/
    override fun onBackPressed() {
        if (intent.getBooleanExtra(Constants.FromNotification, false)) {
            val i = Intent(this@ActSubCategoryProducts, DashboardActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
            finish()
        } else {
            finish()
        }
    }


    private fun setupViewPagerAdapter() {

        binding.viewpager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                Log.e("id", categories[position].id.toString())
                return DynamicFragment.newInstance(
                    categories[position].id.toString()?:"0",
                    intent.getStringExtra("cat_id") ?: "",
                    categories[position].subcategoryItems!!
                )
            }

            override fun getItemCount(): Int {
                return categories.size
            }
        }
        binding.viewpager?.currentItem = 0
    }
}