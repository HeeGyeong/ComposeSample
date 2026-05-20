package com.example.composesample.presentation.example.component.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DocDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DocEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM fts_doc")
    suspend fun count(): Int

    @Query("DELETE FROM fts_doc")
    suspend fun clear()

    // LIKE 전체 스캔 검색 — 인덱스 미사용, 양쪽 와일드카드(%...%)는 SQLite 가 인덱스를 활용할 수 없음
    @Query(
        """
        SELECT * FROM fts_doc
        WHERE title LIKE '%' || :query || '%'
           OR body  LIKE '%' || :query || '%'
        ORDER BY id ASC
        LIMIT :limit
        """
    )
    suspend fun searchLike(query: String, limit: Int): List<DocEntity>
}

@Dao
interface DocFtsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DocFtsEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM fts_doc_fts")
    suspend fun count(): Int

    @Query("DELETE FROM fts_doc_fts")
    suspend fun clear()

    // FTS MATCH 검색 — FTS4 가상 테이블의 역색인을 사용
    // prefix matching 을 위해 query 뒤에 '*' 를 붙이면 "kotl*" 처럼 어두 일치를 지원
    @Query(
        """
        SELECT rowid AS id, title, body
        FROM fts_doc_fts
        WHERE fts_doc_fts MATCH :query
        ORDER BY rowid ASC
        LIMIT :limit
        """
    )
    suspend fun searchMatch(query: String, limit: Int): List<DocEntity>
}
