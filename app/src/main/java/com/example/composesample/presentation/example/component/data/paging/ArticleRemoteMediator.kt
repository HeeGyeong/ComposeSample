package com.example.composesample.presentation.example.component.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import java.io.IOException

/**
 * 네트워크 + DB 이중 소스를 잇는 RemoteMediator.
 *
 * 역할 분담이 이 예제의 핵심이다.
 * - PagingSource(Room 생성) : DB 를 읽어 화면에 보여준다. 네트워크를 전혀 모른다.
 * - RemoteMediator          : DB 데이터가 부족할 때만 호출돼 네트워크에서 받아 DB 에 쓴다.
 *                             화면에 직접 데이터를 주지 않는다 — DB 를 거쳐서만 반영된다.
 *
 * 따라서 네트워크가 죽어도 DB 에 있는 만큼은 계속 보인다(오프라인 우선).
 */
@OptIn(ExperimentalPagingApi::class)
class ArticleRemoteMediator(
    private val db: ArticleDatabase,
    private val api: FakeArticleApi,
    private val log: (String) -> Unit
) : RemoteMediator<Int, ArticleEntity>() {

    private val articleDao = db.articleDao()
    private val remoteKeyDao = db.remoteKeyDao()

    /**
     * Paging 이 시작될 때 딱 한 번 호출돼 "초기 REFRESH 를 강제할지"를 결정한다.
     * - LAUNCH_INITIAL_REFRESH : 캐시를 무시하고 네트워크부터 다시 받는다(기본값)
     * - SKIP_INITIAL_REFRESH   : 캐시를 그대로 쓰고, 사용자가 스크롤할 때만 APPEND 한다
     *
     * 캐시 유효시간(CACHE_TIMEOUT_MS)을 두고 판단하는 것이 실제 앱의 일반적 구현이다.
     */
    override suspend fun initialize(): InitializeAction {
        val lastUpdated = remoteKeyDao.lastUpdatedAt()

        if (lastUpdated == null) {
            log("initialize() → LAUNCH_INITIAL_REFRESH (캐시 없음)")
            return InitializeAction.LAUNCH_INITIAL_REFRESH
        }

        val elapsed = System.currentTimeMillis() - lastUpdated
        return if (elapsed <= CACHE_TIMEOUT_MS) {
            log("initialize() → SKIP_INITIAL_REFRESH (캐시 신선, ${elapsed / 1000}초 경과)")
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            log("initialize() → LAUNCH_INITIAL_REFRESH (캐시 만료, ${elapsed / 1000}초 경과)")
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        // 1) 어떤 네트워크 페이지를 요청할지 결정한다 — RemoteKey 테이블이 그 근거다.
        val page = when (loadType) {
            LoadType.REFRESH -> {
                // 현재 보고 있는 위치에 가장 가까운 아이템의 키에서 역산.
                // 아무것도 없으면(최초 진입) 첫 페이지부터.
                val anchorKey = state.anchorPosition
                    ?.let { position -> state.closestItemToPosition(position) }
                    ?.let { article -> remoteKeyDao.remoteKeyByArticleId(article.id) }
                val target = anchorKey?.nextKey?.minus(1) ?: FakeArticleApi.STARTING_PAGE
                log("load(REFRESH) → page=$target")
                target
            }

            LoadType.PREPEND -> {
                val firstItem = state.firstItemOrNull()
                val remoteKey = firstItem?.let { remoteKeyDao.remoteKeyByArticleId(it.id) }
                val prevKey = remoteKey?.prevKey
                if (prevKey == null) {
                    // 키가 있는데 prevKey 가 null → 진짜 맨 앞(더 받을 게 없음)
                    // 키 자체가 없다 → 아직 DB 에 경계 아이템이 없을 뿐이므로 끝이 아니다
                    val reachedEnd = remoteKey != null
                    log("load(PREPEND) → 요청 없음 (endOfPaginationReached=$reachedEnd)")
                    return MediatorResult.Success(endOfPaginationReached = reachedEnd)
                }
                log("load(PREPEND) → page=$prevKey")
                prevKey
            }

            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                val remoteKey = lastItem?.let { remoteKeyDao.remoteKeyByArticleId(it.id) }
                val nextKey = remoteKey?.nextKey
                if (nextKey == null) {
                    val reachedEnd = remoteKey != null
                    log("load(APPEND) → 요청 없음 (endOfPaginationReached=$reachedEnd)")
                    return MediatorResult.Success(endOfPaginationReached = reachedEnd)
                }
                log("load(APPEND) → page=$nextKey")
                nextKey
            }
        }

        return try {
            // 2) 네트워크 호출
            val remoteArticles = api.fetchArticles(page = page, pageSize = state.config.pageSize)
            val endOfPaginationReached = remoteArticles.isEmpty()

            // 3) DB 에 쓴다. 아이템과 키를 함께 원자적으로 갱신해야
            //    "아이템은 있는데 키가 없어 다음 페이지를 못 받는" 상태가 생기지 않는다.
            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    log("REFRESH → 기존 캐시 삭제 후 재적재")
                    articleDao.clearAll()
                    remoteKeyDao.clearAll()
                }

                val now = System.currentTimeMillis()
                val prevKey = if (page == FakeArticleApi.STARTING_PAGE) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                remoteKeyDao.insertAll(
                    remoteArticles.map { article ->
                        ArticleRemoteKey(
                            articleId = article.id,
                            prevKey = prevKey,
                            nextKey = nextKey,
                            createdAt = now
                        )
                    }
                )
                articleDao.insertAll(
                    remoteArticles.map { article ->
                        ArticleEntity(
                            id = article.id,
                            title = article.title,
                            author = article.author,
                            page = page,
                            fetchedAt = now
                        )
                    }
                )
            }

            log("DB 저장 완료: ${remoteArticles.size}건 (endOfPaginationReached=$endOfPaginationReached)")
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            // 네트워크만 실패하고 DB 는 건드리지 않았으므로 화면의 캐시는 그대로 유지된다.
            // 이 실패는 loadState.mediator 로 올라가고, loadState.source 는 여전히 NotLoading 이다.
            log("네트워크 실패 → MediatorResult.Error (캐시는 그대로 노출)")
            MediatorResult.Error(e)
        }
    }

    companion object {
        // 캐시 유효시간 — 짧게 잡아야 SKIP/LAUNCH 전환을 화면에서 바로 확인할 수 있다
        const val CACHE_TIMEOUT_MS = 60_000L
    }
}
