package com.example.data.repository.dataSource

import com.example.domain.model.PostData

interface PostDataSource {
    suspend fun getPosts(): List<PostData>
}