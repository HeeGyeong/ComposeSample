package com.example.composesample.presentation.example.component.data.paging

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    /**
     * Room 이 PagingSource 구현체를 자동 생성한다(LimitOffsetPagingSource).
     * DB 가 변경되면 이 PagingSource 는 스스로 invalidate 되어 Paging 이 새 소스를 만들고,
     * 그 결과 RemoteMediator 가 DB 에 쓴 내용이 곧바로 화면에 반영된다.
     *
     * 이 반환 타입을 쓰려면 `androidx.room:room-paging` 의존성이 필요하다.
     * 없으면 KSP 가 "Cannot find required type element ... LimitOffsetPagingSource" 로 실패한다.
     */
    @Query("SELECT * FROM rm_articles ORDER BY id ASC")
    fun pagingSource(): PagingSource<Int, ArticleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ArticleEntity>)

    @Query("DELETE FROM rm_articles")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM rm_articles")
    fun countFlow(): Flow<Int>
}

@Dao
interface ArticleRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keys: List<ArticleRemoteKey>)

    @Query("SELECT * FROM rm_article_remote_keys WHERE articleId = :articleId")
    suspend fun remoteKeyByArticleId(articleId: Int): ArticleRemoteKey?

    @Query("DELETE FROM rm_article_remote_keys")
    suspend fun clearAll()

    // 캐시 신선도 판단용 — 한 건도 없으면 null
    @Query("SELECT MAX(createdAt) FROM rm_article_remote_keys")
    suspend fun lastUpdatedAt(): Long?
}
