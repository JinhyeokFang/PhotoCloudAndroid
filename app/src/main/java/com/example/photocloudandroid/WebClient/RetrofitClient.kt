package com.example.photocloudandroid.WebClient

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient {
    fun getRetrofitService(): RetrofitApiInterface {
        return retrofitService
    }

    companion object {
        private var retrofitService: RetrofitApiInterface
        val INSTANCE: RetrofitClient

        init {
            INSTANCE = RetrofitClient()
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val logger = OkHttpClient.Builder().addInterceptor(interceptor).readTimeout(20L, TimeUnit.SECONDS)
                .writeTimeout(20L, TimeUnit.SECONDS).build()
            val retrofit = retrofit2.Retrofit.Builder().baseUrl("http://spear-server.run.goorm.io").addConverterFactory(
                GsonConverterFactory.create() as Converter.Factory
            ).client(logger).build()
            retrofitService = retrofit.create(RetrofitApiInterface::class.java)
        }
    }
}