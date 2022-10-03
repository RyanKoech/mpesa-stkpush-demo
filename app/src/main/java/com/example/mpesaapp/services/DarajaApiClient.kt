package com.example.mpesaapp.services

import com.example.mpesaapp.Constants.BASE_URL
import com.example.mpesaapp.Constants.CONNECT_TIMEOUT
import com.example.mpesaapp.Constants.READ_TIMEOUT
import com.example.mpesaapp.Constants.WRITE_TIMEOUT
import com.example.mpesaapp.interceptor.AccessTokenInterceptor
import com.example.mpesaapp.interceptor.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class DarajaApiClient {
    private var retrofit: Retrofit? = null
    private var isDebug = false
    private var isGetAccessToken = false
    private var mAuthToken: String? = null
    private val httpLoggingInterceptor = HttpLoggingInterceptor()
    fun setIsDebug(isDebug: Boolean): DarajaApiClient {
        this.isDebug = isDebug
        return this
    }

    fun setAuthToken(authToken: String?): DarajaApiClient {
        mAuthToken = authToken
        return this
    }

    fun setGetAccessToken(getAccessToken: Boolean): DarajaApiClient {
        isGetAccessToken = getAccessToken
        return this
    }

    private fun okHttpClient(): OkHttpClient.Builder {
        val okHttpClient = OkHttpClient.Builder()
        okHttpClient
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor)
        return okHttpClient
    }

    private val restAdapter: Retrofit?
        private get() {
            val builder = Retrofit.Builder()
            builder.baseUrl(BASE_URL)
            builder.addConverterFactory(GsonConverterFactory.create())
            if (isDebug) {
                httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
            }
            val okhttpBuilder = okHttpClient()
            if (isGetAccessToken) {
                okhttpBuilder.addInterceptor(AccessTokenInterceptor())
            }
            if (mAuthToken != null && !mAuthToken!!.isEmpty()) {
                okhttpBuilder.addInterceptor(AuthInterceptor(mAuthToken!!))
            }
            builder.client(okhttpBuilder.build())
            retrofit = builder.build()
            return retrofit
        }

    fun mpesaService(): STKPushService {
        return restAdapter!!.create(STKPushService::class.java)
    }
}