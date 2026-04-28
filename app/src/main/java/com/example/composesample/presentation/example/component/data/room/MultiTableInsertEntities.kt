package com.example.composesample.presentation.example.component.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// 다중 테이블 insert 데모용 엔티티들
// Author 1 - N Post / Post N - N Tag (PostTagCrossRef 매핑 테이블)

@Entity(tableName = "mt_author")
data class AuthorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val email: String
)

@Entity(
    tableName = "mt_post",
    foreignKeys = [
        ForeignKey(
            entity = AuthorEntity::class,
            parentColumns = ["id"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("authorId")]
)
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val authorId: Long,
    val title: String,
    val body: String
)

@Entity(tableName = "mt_tag")
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val label: String
)

@Entity(
    tableName = "mt_post_tag",
    primaryKeys = ["postId", "tagId"],
    indices = [Index("tagId")]
)
data class PostTagCrossRef(
    val postId: Long,
    val tagId: Long
)
