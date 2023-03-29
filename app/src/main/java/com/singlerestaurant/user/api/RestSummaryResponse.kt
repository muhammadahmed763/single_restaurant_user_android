package com.singlerestaurant.user.api

import com.singlerestaurant.user.model.OrderSummaryModel
import com.singlerestaurant.user.model.SummaryModel

class RestSummaryResponse {

    private var data: ArrayList<OrderSummaryModel>? = null

    private var message: String? = null

    private var status: String? = null

    private var summery: SummaryModel? = null

    fun getData(): ArrayList<OrderSummaryModel> {
        return data!!
    }

    fun getMessage(): String? {
        return message
    }

    fun getStatus(): String? {
        return status
    }

    fun getSummery(): SummaryModel? {
        return summery
    }
}