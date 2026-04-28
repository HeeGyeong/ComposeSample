package com.example.composesample.presentation.example.component.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// ===== 1) 공통 BaseInsertDao 인터페이스 =====
// 제네릭 타입 T 의 단건/다건 insert 시그니처를 한 곳에 정의해
// 자식 DAO 마다 @Insert 보일러플레이트를 반복하지 않는다.
// Room 컴파일러는 BaseInsertDao 를 상속한 @Dao 의 구체 엔티티 타입 기준으로 구현체를 생성한다.
interface BaseInsertDao<T> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: T): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<T>): List<Long>
}

// ===== 2) 각 테이블 전용 DAO — Base 상속으로 insert 재사용 =====
@Dao
interface AuthorDao : BaseInsertDao<AuthorEntity> {
    @Query("SELECT COUNT(*) FROM mt_author")
    suspend fun count(): Int

    @Query("SELECT * FROM mt_author ORDER BY id DESC")
    suspend fun list(): List<AuthorEntity>

    @Query("DELETE FROM mt_author")
    suspend fun clear()
}

@Dao
interface PostDao : BaseInsertDao<PostEntity> {
    @Query("SELECT COUNT(*) FROM mt_post")
    suspend fun count(): Int

    @Query("SELECT * FROM mt_post ORDER BY id DESC")
    suspend fun list(): List<PostEntity>

    @Query("DELETE FROM mt_post")
    suspend fun clear()
}

@Dao
interface TagDao : BaseInsertDao<TagEntity> {
    @Query("SELECT COUNT(*) FROM mt_tag")
    suspend fun count(): Int

    @Query("SELECT * FROM mt_tag ORDER BY id DESC")
    suspend fun list(): List<TagEntity>

    @Query("DELETE FROM mt_tag")
    suspend fun clear()
}

@Dao
interface PostTagDao : BaseInsertDao<PostTagCrossRef> {
    @Query("SELECT COUNT(*) FROM mt_post_tag")
    suspend fun count(): Int

    @Query("DELETE FROM mt_post_tag")
    suspend fun clear()
}

data class PublishResult(
    val authorId: Long,
    val postId: Long,
    val tagIds: List<Long>
)
