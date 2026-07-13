package com.example.data.api

import com.example.domain.model.PostData
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

/**
 * Ktor에서 사용하는 API 호출 함수
 */
suspend fun HttpClient.getPosts(): List<PostData> {
    return this.get("/posts").body()
}