package com.example.data.repository

import com.example.data.repository.dataSource.PostDataSource
import com.example.domain.model.PostData
import com.example.domain.repository.PostRepository
import retrofit2.Call

// domain 레이어에 있는 repository의 구현부
class PostRepositoryImpl(
    private val postDataSource: PostDataSource
): PostRepository {
    override fun getPosts(): Call<List<PostData>> = postDataSource.getPosts()
}