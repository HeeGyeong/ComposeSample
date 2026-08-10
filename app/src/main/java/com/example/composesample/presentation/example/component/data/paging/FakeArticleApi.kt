package com.example.composesample.presentation.example.component.data.paging

import java.io.IOException
import kotlinx.coroutines.delay

/**
 * 네트워크 응답 DTO. DB 엔티티(ArticleEntity)와 분리해 두는 것이 실제 구조와 같다
 * — 원격 모델에는 page/fetchedAt 같은 캐시 메타가 없다.
 */
data class RemoteArticle(
    val id: Int,
    val title: String,
    val author: String
)

/**
 * 예제용 가짜 원격 API.
 *
 * 실제 서버 없이 오프라인 우선 동작을 시연하기 위해
 * ① 인위적 지연 ② 오프라인 토글(IOException) ③ 유한한 전체 개수(마지막 페이지 도달)
 * 세 가지만 제공한다.
 */
class FakeArticleApi {
    // UI 토글로 바뀌고 코루틴에서 읽히므로 @Volatile
    @Volatile
    var isOffline: Boolean = false

    suspend fun fetchArticles(page: Int, pageSize: Int): List<RemoteArticle> {
        delay(NETWORK_DELAY_MS)

        if (isOffline) {
            throw IOException("네트워크 연결 없음 — 오프라인 모드")
        }

        val startId = (page - STARTING_PAGE) * pageSize
        if (startId >= TOTAL_ITEMS) {
            // 마지막 페이지를 넘어선 요청 → 빈 응답으로 endOfPaginationReached 판정 근거를 준다
            return emptyList()
        }

        val endId = minOf(startId + pageSize, TOTAL_ITEMS)
        return (startId until endId).map { id ->
            RemoteArticle(
                id = id,
                title = "아티클 #$id",
                author = AUTHORS[id % AUTHORS.size]
            )
        }
    }

    companion object {
        const val STARTING_PAGE = 1
        const val TOTAL_ITEMS = 60
        private const val NETWORK_DELAY_MS = 900L
        private val AUTHORS = listOf("김하늘", "이준호", "박서연", "최민수", "정다은")
    }
}
