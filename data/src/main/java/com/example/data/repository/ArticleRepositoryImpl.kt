package com.example.data.repository

import com.example.domain.model.ArticleData
import com.example.domain.model.ArticleResult
import com.example.domain.model.ArticleSource
import com.example.domain.repository.ArticleRepository
import kotlinx.coroutines.delay

/**
 * Memory → Disk → Network 우선순위 해석 Repository 구현.
 *
 * 실제 앱이라면 Disk 계층은 Room/DataStore 가 담당하지만, 이 예제의 목적은 "우선순위 해석" 패턴
 * 자체를 보여주는 것이라 Disk/Network 를 인메모리 맵 + 인위적 delay 로 시뮬레이션한다
 * (KtorAdvancedConfig 의 MockEngine, FeatureFlag 의 fetchRemote() 와 동일한 시뮬레이션 방식).
 * 하위 계층에서 찾은 결과는 상위 계층에 채워 넣어(cache population) 다음 조회를 가속한다.
 */
class ArticleRepositoryImpl : ArticleRepository {

    private val memoryCache = mutableMapOf<String, ArticleData>()
    private val diskCache = mutableMapOf<String, ArticleData>()

    override suspend fun getArticle(id: String, forceRefresh: Boolean): ArticleResult {
        if (!forceRefresh) {
            memoryCache[id]?.let { return ArticleResult(it, ArticleSource.MEMORY, 0L) }

            val diskStart = System.currentTimeMillis()
            delay(DISK_LATENCY_MS)
            diskCache[id]?.let { fromDisk ->
                memoryCache[id] = fromDisk
                return ArticleResult(fromDisk, ArticleSource.DISK, System.currentTimeMillis() - diskStart)
            }
        }

        val networkStart = System.currentTimeMillis()
        delay(NETWORK_LATENCY_MS)
        val fetched = fetchFromNetwork(id)
        diskCache[id] = fetched
        memoryCache[id] = fetched
        return ArticleResult(fetched, ArticleSource.NETWORK, System.currentTimeMillis() - networkStart)
    }

    override fun invalidateMemory() {
        memoryCache.clear()
    }

    override fun invalidateDisk() {
        diskCache.clear()
    }

    private fun fetchFromNetwork(id: String): ArticleData = ArticleData(
        id = id,
        title = "Article #$id",
        body = "네트워크에서 새로 가져온 본문입니다 (id=$id)."
    )

    companion object {
        private const val DISK_LATENCY_MS = 300L
        private const val NETWORK_LATENCY_MS = 900L
    }
}
