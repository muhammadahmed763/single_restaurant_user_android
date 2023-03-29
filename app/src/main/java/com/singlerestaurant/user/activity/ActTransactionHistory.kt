package com.singlerestaurant.user.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.singlerestaurant.user.R
import com.singlerestaurant.user.adaptor.TransactionHistoryAdapter
import com.singlerestaurant.user.api.ApiClient
import com.singlerestaurant.user.databinding.ActTransactionHistoryBinding
import com.singlerestaurant.user.model.TransactionsItem
import com.singlerestaurant.user.remote.NetworkResponse
import com.singlerestaurant.user.utils.Common
import com.singlerestaurant.user.utils.Constants
import com.singlerestaurant.user.utils.SharePreference

import kotlinx.coroutines.launch

class ActTransactionHistory : AppCompatActivity() {
    private lateinit var binding: ActTransactionHistoryBinding
    var transactionHistoryList = ArrayList<TransactionsItem>()
    private lateinit var transactionAdapter: TransactionHistoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTransactionAdapter()

        if (SharePreference.getBooleanPref(this@ActTransactionHistory,SharePreference.isLogin)) {
            callApiWalletTransactionList()

        }else
        {
            startActivity(Intent(this@ActTransactionHistory,LoginActivity::class.java))
            finish()
            finishAffinity()
        }


        binding.ivBack.setOnClickListener {
            if (intent.getBooleanExtra(Constants.FromNotification, false)) {
                val i = Intent(this@ActTransactionHistory, DashboardActivity::class.java)
                i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(i)
                finish()
            } else {
                finish()
            }
        }

        if (SharePreference.getStringPref(
                this@ActTransactionHistory,
                SharePreference.SELECTED_LANGUAGE
            ).equals(
                resources.getString(R.string.language_hindi)
            )
        ) {
            binding.ivBack.rotation = 180F
        } else {
            binding.ivBack.rotation = 0F
        }

    }


    private fun setupTransactionAdapter() {
        transactionAdapter =
            TransactionHistoryAdapter(
                this@ActTransactionHistory,
                transactionHistoryList
            ) { pos, type ->

            }
        binding.rvTransactionHistory.apply {
            layoutManager = LinearLayoutManager(this@ActTransactionHistory)
            adapter = transactionAdapter
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun callApiWalletTransactionList() {
        Common.showLoadingProgress(this@ActTransactionHistory)
        val map = HashMap<String, String>()
        map["user_id"] =
            SharePreference.getStringPref(this@ActTransactionHistory, SharePreference.userId) ?: ""
        lifecycleScope.launch {
            when (val response = ApiClient.getClient().walletTransaction(map)) {
                is NetworkResponse.Success -> {
                    Common.dismissLoadingProgress()
                    val responseBody = response.body
                    transactionHistoryList.clear()
                    responseBody.transactions?.let { transactionHistoryList.addAll(it) }
                    transactionAdapter.notifyDataSetChanged()
                    if(transactionHistoryList.size>0)
                    {
                        binding.rvTransactionHistory.visibility= View.VISIBLE
                        binding.tvNoDataFound.visibility= View.GONE
                    }else
                    {
                        binding.rvTransactionHistory.visibility= View.GONE
                        binding.tvNoDataFound.visibility= View.VISIBLE
                    }
                }
                is NetworkResponse.ApiError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActTransactionHistory,
                        response.body.message.toString().replace("\\n", System.lineSeparator())
                    )
                }
                is NetworkResponse.NetworkError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActTransactionHistory,
                        resources.getString(R.string.no_internet)
                    )
                }
                is NetworkResponse.UnknownError -> {
                    Common.dismissLoadingProgress()
                    Common.showErrorFullMsg(
                        this@ActTransactionHistory,
                        resources.getString(R.string.error_msg)
                    )
                }


            }
        }


    }

    override fun onBackPressed() {
        if (intent.getBooleanExtra(Constants.FromNotification, false)) {
            val i = Intent(this@ActTransactionHistory, DashboardActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
            finish()
        } else {
            finish()
        }
    }
}