package com.example.data.repository.dataSourceImpl

import com.example.data.api.PostApiInterface
import com.example.data.repository.dataSource.PostDataSource
import com.example.domain.model.PostData
import retrofit2.Call

class PostDataSourceImpl(
    private val postApiInterface: PostApiInterface
) : PostDataSource {
    override fun getPosts(): Call<List<PostData>> {
        return postApiInterface.getPosts()
    }
}