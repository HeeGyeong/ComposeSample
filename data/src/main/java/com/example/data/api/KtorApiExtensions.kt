package com.example.data.api

import com.example.domain.model.PostData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

suspend fun HttpClient.getPosts(): List<PostData> {
    return this.get("https://jsonplaceholder.typicode.com/posts").body()
}