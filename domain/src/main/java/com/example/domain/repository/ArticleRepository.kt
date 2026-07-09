package com.example.domain.repository

import com.example.domain.model.ArticleResult

/**
 * Memory → Disk → Network 우선순위로 데이터를 해석하는 Repository.
 *
 * ArticleData 가 순수 Kotlin 모델(Room @Entity 아님)이라 이 인터페이스는 domain 모듈에 둘 수 있다.
 * (data/repository/UserCacheRepository 는 UserData 가 Room @Entity 라 data 레이어에 있어야 했던 것과 대조됨)
 */
interface ArticleRepository {
    suspend fun getArticle(id: String, forceRefresh: Boolean = false): ArticleResult
    fun invalidateMemory()
    fun invalidateDisk()
}
