package com.example.composesample.presentation.example.component.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NoIndexDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<NoIndexEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM idx_no_index")
    suspend fun count(): Int

    @Query("DELETE FROM idx_no_index")
    suspend fun clear()

    // age 범위 조회 — 인덱스가 없어 전체 스캔(O(N))
    @Query("SELECT COUNT(*) FROM idx_no_index WHERE age BETWEEN :min AND :max")
    suspend fun countByAgeRange(min: Int, max: Int): Int

    // city 등호 + age 정렬 — 인덱스 없음
    @Query("SELECT * FROM idx_no_index WHERE city = :city ORDER BY age ASC LIMIT :limit")
    suspend fun findByCity(city: String, limit: Int): List<NoIndexEntity>
}

@Dao
interface SingleIndexDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SingleIndexEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM idx_single")
    suspend fun count(): Int

    @Query("DELETE FROM idx_single")
    suspend fun clear()

    // age 단일 인덱스를 활용한 범위 조회 → 인덱스 스캔
    @Query("SELECT COUNT(*) FROM idx_single WHERE age BETWEEN :min AND :max")
    suspend fun countByAgeRange(min: Int, max: Int): Int

    // city 조건은 인덱스(age)와 무관 → 부분적으로만 가속
    @Query("SELECT * FROM idx_single WHERE city = :city ORDER BY age ASC LIMIT :limit")
    suspend fun findByCity(city: String, limit: Int): List<SingleIndexEntity>
}

@Dao
interface CompositeIndexDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CompositeIndexEntity>): List<Long>

    @Query("SELECT COUNT(*) FROM idx_composite")
    suspend fun count(): Int

    @Query("DELETE FROM idx_composite")
    suspend fun clear()

    // age 단독 조건 — 복합 인덱스 (city, age) 의 leftmost(city)를 만족하지 못해 인덱스 미활용
    @Query("SELECT COUNT(*) FROM idx_composite WHERE age BETWEEN :min AND :max")
    suspend fun countByAgeRange(min: Int, max: Int): Int

    // city 등호 + age 정렬 — 복합 인덱스 (city, age)를 완전히 활용 (조회 + 정렬 모두 가속)
    @Query("SELECT * FROM idx_composite WHERE city = :city ORDER BY age ASC LIMIT :limit")
    suspend fun findByCity(city: String, limit: Int): List<CompositeIndexEntity>
}
