package com.example.domain.model

/** Advanced Repository Pattern 예제용 순수 도메인 모델 (Android/Room 의존성 없음) */
data class ArticleData(
    val id: String,
    val title: String,
    val body: String
)

/** 데이터가 어느 계층에서 해석되었는지 (우선순위: MEMORY > DISK > NETWORK) */
enum class ArticleSource { MEMORY, DISK, NETWORK }

/** 조회 결과 + 출처 + 소요 시간(ms) */
data class ArticleResult(
    val article: ArticleData,
    val source: ArticleSource,
    val latencyMs: Long
)
