package com.example.domain.repository

import com.example.domain.model.PostData
import retrofit2.Call

interface PostRepository {
    fun getPosts(): Call<List<PostData>>
}