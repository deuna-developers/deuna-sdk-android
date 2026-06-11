package com.deuna.maven.client

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ApiService {
    @GET("merchants/orders/{orderToken}")
    fun getOrder(
        @Path("orderToken") orderToken: String,
        @Header("X-Api-Key") apiKey: String
    ): Call<Any>
}