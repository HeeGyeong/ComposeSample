package com.example.domain.model

// domain 모듈은 순수 Kotlin 유지. JSON 키와 프로퍼티명을 맞춰 Gson 어노테이션 없이 역직렬화한다.
data class MovieResponse(
    val display: Int,
    val items: List<MovieEntity>,
    val lastBuildDate: String,
    val start: Int,
    val total: Int
)