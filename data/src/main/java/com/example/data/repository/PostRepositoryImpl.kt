package com.example.data.repository

import com.example.data.repository.dataSource.PostDataSource
import com.example.domain.model.PostData
import com.example.domain.repository.PostRepository

// domain 레이어에 있는 repository의 구현부
class PostRepositoryImpl(
    private val postDataSource: PostDataSource
): PostRepository {
    override suspend fun getPosts(): List<PostData> = postDataSource.getPosts()
}