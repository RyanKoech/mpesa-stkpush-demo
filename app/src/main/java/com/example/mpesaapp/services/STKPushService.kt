package com.example.mpesaapp.services

import com.example.mpesaapp.model.AccessToken
import com.example.mpesaapp.model.STKPush
import com.example.mpesaapp.model.STKPushResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface STKPushService {
    @POST("mpesa/stkpush/v1/processrequest")
    fun sendPush(@Body stkPush: STKPush?): Call<STKPushResponse?>?

    @get:GET("oauth/v1/generate?grant_type=client_credentials")
    val accessToken: Call<AccessToken?>?
}