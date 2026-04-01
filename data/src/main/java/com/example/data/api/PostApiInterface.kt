package com.example.data.api

import com.example.domain.model.PostData
import retrofit2.http.GET

interface PostApiInterface {
    @GET("/posts")
    suspend fun getPosts(): List<PostData>
}