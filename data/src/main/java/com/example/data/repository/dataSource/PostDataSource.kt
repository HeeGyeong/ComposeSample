package com.example.data.repository.dataSource

import com.example.domain.model.PostData
import retrofit2.Call

interface PostDataSource {
    fun getPosts(): Call<List<PostData>>
}