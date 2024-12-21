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

suspend fun HttpClient.getPosts(): List<PostData> {
    return this.get("/posts").body()
}

/**
 * Example Case
 *
 * Used Parameter
 */
suspend fun HttpClient.getPostsByUserId(userId: Int): List<PostData> {
    return this.get("/posts") {
        parameter("userId", userId)
    }.body()
}

/**
 * Example Case
 *
 * Create Post
 */
suspend fun HttpClient.createPost(postData: PostData): PostData {
    return this.post("/posts") {
        // Content-Type을 application/json으로 설정
        // ktorModule에 선언되어 있지만, 명시적으로 처리해주는 것이 안전한 방법.
        contentType(ContentType.Application.Json)

        // postData 객체를 JSON으로 직렬화하여 요청 본문에 포함
        setBody(postData)
    }.body()
}