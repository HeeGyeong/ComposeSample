package com.example.domain.model

// domain 모듈은 순수 Kotlin 유지. 프로퍼티명이 JSON 키와 일치하므로 Gson 어노테이션 불필요.
data class PostData(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)
