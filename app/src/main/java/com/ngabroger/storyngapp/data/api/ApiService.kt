package com.ngabroger.storyngapp.data.api

import com.ngabroger.storyngapp.data.response.ErrorResponse
import com.ngabroger.storyngapp.data.response.LoginResponse
import com.ngabroger.storyngapp.data.response.RegisterResponse
import com.ngabroger.storyngapp.data.response.StoryResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<RegisterResponse>

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>


    @GET("stories")
    suspend fun getStories(
        @Query("page") page : Int,
        @Query("size") size : Int,
    ): StoryResponse

    @GET("stories/{id}")
   suspend fun getStoryById(@Path("id") id: String): Response<StoryResponse>

   @GET("stories")
   suspend fun getStoriesWithLocation(@Query("location") location: Int = 1): Response<StoryResponse>


   @Multipart
   @POST("stories")
    suspend fun sendStory(
       @Part("description") description: String,
       @Part file: MultipartBody.Part,
       @Part("lat") lat: Float?,
       @Part("lon") lon: Float?

    ): Response<ErrorResponse>

}