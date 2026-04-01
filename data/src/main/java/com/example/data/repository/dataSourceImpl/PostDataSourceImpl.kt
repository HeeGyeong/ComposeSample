package com.example.data.repository.dataSourceImpl

import com.example.data.api.PostApiInterface
import com.example.data.repository.dataSource.PostDataSource
import com.example.domain.model.PostData

class PostDataSourceImpl(
    private val postApiInterface: PostApiInterface
) : PostDataSource {
    override suspend fun getPosts(): List<PostData> {
        return postApiInterface.getPosts()
    }
}