package com.example.domain.repository

import com.example.domain.model.PostData

interface PostRepository {
    suspend fun getPosts(): List<PostData>
}