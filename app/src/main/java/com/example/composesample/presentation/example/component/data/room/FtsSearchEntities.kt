package com.example.composesample.presentation.example.component.data.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

// Room FTS4 vs LIKE 검색 데모용 엔티티
// - DocEntity: 일반 테이블 (LIKE 검색 대상)
// - DocFtsEntity: FTS4 가상 테이블 (MATCH 검색 대상)
//   contentEntity 지정 시 별도 INSERT 없이 원본 테이블과 동기화되지만,
//   in-memory DB 환경에서는 양쪽에 동일하게 INSERT 하는 방식이 단순하고 직관적

@Entity(tableName = "fts_doc")
data class DocEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val body: String
)

// FTS4 가상 테이블 — title/body 컬럼에 대해 토크나이저 기반 역색인을 자동 생성
// rowid 가 일반 테이블의 PRIMARY KEY 와 대응되도록 설계
@Fts4
@Entity(tableName = "fts_doc_fts")
data class DocFtsEntity(
    @PrimaryKey
    @ColumnInfo(name = "rowid") val rowid: Long,
    val title: String,
    val body: String
)
