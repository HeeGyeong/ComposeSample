package com.example.composesample.presentation.example.component.data.room

import androidx.room.withTransaction

/**
 * Multi-table insert 오케스트레이션.
 * - withTransaction { } 으로 여러 DAO 호출을 한 트랜잭션에 묶음 → 부분 실패 시 전체 롤백
 * - 각 DAO 는 BaseInsertDao<T> 상속으로 insert/insertAll 메서드를 공유 (보일러플레이트 제거)
 */
class MultiTableInsertRepository(
    private val db: MultiTableInsertDatabase
) {
    private val authorDao = db.authorDao()
    private val postDao = db.postDao()
    private val tagDao = db.tagDao()
    private val postTagDao = db.postTagDao()

    suspend fun publishPost(
        author: AuthorEntity,
        post: PostEntity,
        tags: List<TagEntity>,
        forceFailure: Boolean = false
    ): PublishResult = db.withTransaction {
        val authorId = authorDao.insert(author)
        require(authorId != -1L) { "Author insert conflict: ${author.email}" }

        val postId = postDao.insert(post.copy(authorId = authorId))
        require(postId != -1L) { "Post insert conflict: ${post.title}" }

        if (forceFailure) {
            // 트랜잭션 내부 예외 → author/post insert 모두 롤백되어야 한다.
            error("의도적 실패 — 트랜잭션 롤백 시연")
        }

        val tagIds = tagDao.insertAll(tags)
        require(tagIds.none { it == -1L }) { "Tag insert conflict in batch" }

        postTagDao.insertAll(tagIds.map { tagId -> PostTagCrossRef(postId = postId, tagId = tagId) })

        PublishResult(authorId = authorId, postId = postId, tagIds = tagIds)
    }

    suspend fun snapshot(): TableSnapshot = TableSnapshot(
        authorCount = authorDao.count(),
        postCount = postDao.count(),
        tagCount = tagDao.count(),
        postTagCount = postTagDao.count(),
        authors = authorDao.list(),
        posts = postDao.list(),
        tags = tagDao.list()
    )

    suspend fun clearAll() = db.withTransaction {
        // FK CASCADE 가 동작하지만 명시적으로 모든 테이블 비움
        postTagDao.clear()
        postDao.clear()
        tagDao.clear()
        authorDao.clear()
    }
}

data class TableSnapshot(
    val authorCount: Int,
    val postCount: Int,
    val tagCount: Int,
    val postTagCount: Int,
    val authors: List<AuthorEntity>,
    val posts: List<PostEntity>,
    val tags: List<TagEntity>
)
