package com.example.data.api

import com.example.domain.model.PostData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

const val baseUrl = "https://jsonplaceholder.typicode.com/posts"

suspend fun HttpClient.getPosts(): List<PostData> {
    return this.get(baseUrl).body()
}

/**
 * Example Case
 *
 * Used Parameter
 */
suspend fun HttpClient.getPostsByUserId(userId: Int): List<PostData> {
    return this.get(baseUrl) {
        parameter("userId", userId)
    }.body()
}

/**
 * Example Case
 *
 * Create Post
 */
suspend fun HttpClient.createPost(postData: PostData): PostData {
    return this.post(baseUrl) {
        contentType(ContentType.Application.Json) // Content-Type을 application/json으로 설정
        setBody(postData) // postData 객체를 JSON으로 직렬화하여 요청 본문에 포함
    }.body()
}