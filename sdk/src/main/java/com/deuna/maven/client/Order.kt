package com.deuna.maven.client

import retrofit2.Callback

fun sendOrder(baseUrl: String, orderToken: String, apiKey: String, callback: Callback<Any>) {
    val call = RetrofitClient.getInstance(baseUrl).getOrder(orderToken, apiKey)

    call.enqueue(callback)
}