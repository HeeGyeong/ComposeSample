package com.example.data.api

import com.example.domain.model.PostData
import retrofit2.Call
import retrofit2.http.GET

interface PostApiInterface {
    @GET("/posts")
    fun getPosts(): Call<List<PostData>>
}