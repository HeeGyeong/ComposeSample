package com.example.composesample.presentation.example.component.data.room

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Room Database Indices 데모용 엔티티
// 동일한 컬럼 구성(name/age/city)을 가진 세 테이블로 인덱스 유무·종류에 따른
// 쿼리 성능 차이를 비교한다.
//
// - NoIndexEntity   : 인덱스 없음 → WHERE/ORDER BY 시 전체 스캔(O(N))
// - SingleIndexEntity: 단일 컬럼 인덱스(age) → 등호/범위 조회 가속
// - CompositeIndexEntity: 복합 인덱스(city, age) + city 유니크 아님
//   → leftmost prefix 규칙: (city) 또는 (city, age) 조건만 인덱스 활용 가능,
//     (age) 단독 조건은 복합 인덱스를 타지 못함

@Entity(tableName = "idx_no_index")
data class NoIndexEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val age: Int,
    val city: String
)

// 단일 컬럼 인덱스 — age 컬럼에 B-Tree 인덱스 생성
@Entity(
    tableName = "idx_single",
    indices = [Index(value = ["age"])]
)
data class SingleIndexEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val age: Int,
    val city: String
)

// 복합 인덱스 — (city, age) 순서로 생성
// 컬럼 순서가 곧 정렬 우선순위이므로 leftmost prefix(city 우선)를 만족해야 인덱스를 탄다
@Entity(
    tableName = "idx_composite",
    indices = [Index(value = ["city", "age"])]
)
data class CompositeIndexEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val age: Int,
    val city: String
)
