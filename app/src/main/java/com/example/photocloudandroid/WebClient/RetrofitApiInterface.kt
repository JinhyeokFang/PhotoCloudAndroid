package com.example.photocloudandroid.WebClient

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitApiInterface {
    @POST("/auth/checktoken")
    @FormUrlEncoded
    fun checktoken(@Field("token") token: String): Call<ResponseBody>

    @POST("/auth/login")
    @FormUrlEncoded
    fun login(@Field("username") username: String, @Field("password") password: String): Call<ResponseBody>

    @POST("/auth/register")
    @FormUrlEncoded
    fun register(@Field("username") username: String, @Field("password") password: String): Call<ResponseBody>

    @GET("/user/image")
    fun getImage(@Query("token") token: String): Call<ResponseBody>

    @Multipart
    @POST("/user/image")
    fun uploadImage(@Part image: MultipartBody.Part, @Part("date") date: RequestBody, @Part("token") token: RequestBody): Call<ResponseBody>

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "/user/image", hasBody = true)
    fun removeImage(@Field("token") token: String, @Field("imageDate") imageDate: String): Call<ResponseBody>
}
