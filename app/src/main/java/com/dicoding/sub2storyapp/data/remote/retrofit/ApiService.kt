package com.dicoding.sub2storyapp.data.remote.retrofit

import com.dicoding.sub2storyapp.data.remote.response.FileUploadResponse
import com.dicoding.sub2storyapp.data.remote.response.LoginResponse
import com.dicoding.sub2storyapp.data.remote.response.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    fun createAccount(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<FileUploadResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") header: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): StoryResponse

    @GET("stories")
    fun getStoriesWithLocation(
        @Header("Authorization") header: String,
        @Query("location") location: Int
    ): Call<StoryResponse>

    @Multipart
    @POST("stories")
    fun uploadStories(
        @Header("Authorization") header: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
    ): Call<FileUploadResponse>

}