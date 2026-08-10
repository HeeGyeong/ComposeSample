package com.example.composesample.presentation.example.component.data.paging

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * DB 에 캐시되는 페이징 아이템.
 * RemoteMediator 예제에서는 이 테이블이 "단일 진실 공급원(Single Source of Truth)" 이며,
 * 화면은 네트워크가 아니라 항상 이 테이블을 통해서만 데이터를 본다.
 */
@Entity(tableName = "rm_articles")
data class ArticleEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val author: String,
    // 이 아이템이 어느 네트워크 페이지에서 왔는지 — 시연용 표시 목적
    val page: Int,
    val fetchedAt: Long
)

/**
 * 원격 페이지 키 테이블.
 *
 * PagingSource 는 DB 를 읽으므로 "다음에 네트워크에서 몇 페이지를 받아야 하는지"를 알 수 없다.
 * 그래서 RemoteMediator 는 아이템별로 prev/next 페이지 키를 따로 저장해두고,
 * PREPEND/APPEND 시 경계 아이템의 키를 조회해 다음 요청 페이지를 결정한다.
 *
 * createdAt 은 캐시 신선도 판단용 — initialize() 에서 MAX(createdAt) 로 마지막 갱신 시각을 얻는다.
 */
@Entity(tableName = "rm_article_remote_keys")
data class ArticleRemoteKey(
    @PrimaryKey val articleId: Int,
    val prevKey: Int?,
    val nextKey: Int?,
    val createdAt: Long
)
