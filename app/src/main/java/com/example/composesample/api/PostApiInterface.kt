package com.example.composesample.api

import com.example.composesample.model.PostData
import retrofit2.Call
import retrofit2.http.GET

interface PostApiInterface {
    @GET("/posts")
    fun getPosts(): Call<List<PostData>>
}