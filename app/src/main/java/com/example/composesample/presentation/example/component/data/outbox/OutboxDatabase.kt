package com.example.composesample.presentation.example.component.data.outbox

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

enum class OutboxStatus { PENDING, SENT, FAILED }

/**
 * 아웃박스 행 = 메시지 하나. **보내기는 요청이 아니라 이 행을 저장하는 것**이다 — 말풍선은 이 행을 그린다.
 * 기본 키가 기기에서 만든 clientId 라서, 같은 메시지가 여러 경로로 다시 와도 한 행에 모인다.
 * seq 는 서버가 매긴 순서(보내기 전에는 null — 목록 맨 아래).
 */
@Entity(
    tableName = "outbox_message",
    indices = [Index(value = ["conversation_id"])]
)
data class OutboxMessageEntity(
    @PrimaryKey @ColumnInfo(name = "client_id") val clientId: String,
    @ColumnInfo(name = "conversation_id") val conversationId: String,
    val body: String,
    val status: OutboxStatus,
    @ColumnInfo(name = "server_id") val serverId: Long?,
    val seq: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    val attempts: Int,
    @ColumnInfo(name = "last_error") val lastError: String?
)

// ==================== 중복 도착 실험용 — 같은 메시지를 세 가지 키 설계로 받는다 ====================

/** ① clientId 가 기본 키 — @Upsert 의 충돌도, 그 뒤 UPDATE 도 clientId 로 맞는다 */
@Entity(tableName = "arrival_by_key")
data class ArrivalByKeyEntity(
    @PrimaryKey @ColumnInfo(name = "client_id") val clientId: String,
    val body: String,
    val seq: Long?,
    val source: String
)

/** ② 자동 증가 기본 키 + clientId 고유 인덱스 — @Upsert 가 충돌 뒤 **기본 키(local_id)로** UPDATE 한다 */
@Entity(
    tableName = "arrival_by_index",
    indices = [Index(value = ["client_id"], unique = true)]
)
data class ArrivalByIndexEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "local_id") val localId: Long = 0,
    @ColumnInfo(name = "client_id") val clientId: String,
    val body: String,
    val seq: Long?,
    val source: String
)

/** ③ ②와 같은 모양에 @Insert(REPLACE) — 충돌한 행을 지우고 새로 넣는다(local_id 가 바뀐다) */
@Entity(
    tableName = "arrival_by_index_replace",
    indices = [Index(value = ["client_id"], unique = true)]
)
data class ArrivalReplaceEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "local_id") val localId: Long = 0,
    @ColumnInfo(name = "client_id") val clientId: String,
    val body: String,
    val seq: Long?,
    val source: String
)

@Dao
interface OutboxDao {
    /** 같은 clientId 가 이미 있으면 실패해야 한다(ABORT) — 새 메시지는 언제나 새 id */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(row: OutboxMessageEntity)

    /** 보낸 것은 서버 seq 순, 아직 seq 가 없는 것(PENDING·FAILED)은 맨 아래 만든 순 */
    @Query(
        "SELECT * FROM outbox_message WHERE conversation_id = :conversationId " +
            "ORDER BY CASE WHEN seq IS NULL THEN 1 ELSE 0 END, seq, created_at"
    )
    fun observe(conversationId: String): Flow<List<OutboxMessageEntity>>

    @Query("SELECT * FROM outbox_message WHERE conversation_id LIKE :prefix || '%' ORDER BY created_at")
    fun observeByPrefix(prefix: String): Flow<List<OutboxMessageEntity>>

    @Query("SELECT * FROM outbox_message WHERE client_id = :clientId")
    suspend fun get(clientId: String): OutboxMessageEntity?

    @Query("SELECT * FROM outbox_message WHERE conversation_id = :conversationId AND status = 'PENDING' ORDER BY created_at")
    suspend fun pending(conversationId: String): List<OutboxMessageEntity>

    @Query("UPDATE outbox_message SET status = 'SENT', server_id = :serverId, seq = :seq, attempts = :attempts, last_error = NULL WHERE client_id = :clientId")
    suspend fun markSent(clientId: String, serverId: Long, seq: Long, attempts: Int)

    @Query("UPDATE outbox_message SET attempts = :attempts, last_error = :error WHERE client_id = :clientId")
    suspend fun recordAttempt(clientId: String, attempts: Int, error: String)

    @Query("UPDATE outbox_message SET status = 'FAILED', attempts = :attempts, last_error = :error WHERE client_id = :clientId")
    suspend fun markFailed(clientId: String, attempts: Int, error: String)

    /** '다시 보내기' — 같은 행(같은 clientId)을 PENDING 으로 되돌린다. 새 행을 만들지 않는다 */
    @Query("UPDATE outbox_message SET status = 'PENDING', attempts = 0, last_error = NULL WHERE client_id = :clientId")
    suspend fun resetToPending(clientId: String)

    @Query("DELETE FROM outbox_message WHERE conversation_id LIKE :prefix || '%'")
    suspend fun deleteByPrefix(prefix: String)
}

@Dao
interface ArrivalDao {
    @Upsert
    suspend fun upsertByKey(row: ArrivalByKeyEntity)

    @Upsert
    suspend fun upsertByIndex(row: ArrivalByIndexEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replaceByIndex(row: ArrivalReplaceEntity)

    @Query("SELECT * FROM arrival_by_key")
    suspend fun byKey(): List<ArrivalByKeyEntity>

    @Query("SELECT * FROM arrival_by_index ORDER BY local_id")
    suspend fun byIndex(): List<ArrivalByIndexEntity>

    @Query("SELECT * FROM arrival_by_index_replace ORDER BY local_id")
    suspend fun byReplace(): List<ArrivalReplaceEntity>

    @Query("DELETE FROM arrival_by_key")
    suspend fun clearKey()

    @Query("DELETE FROM arrival_by_index")
    suspend fun clearIndex()

    @Query("DELETE FROM arrival_by_index_replace")
    suspend fun clearReplace()
}

@Database(
    entities = [
        OutboxMessageEntity::class,
        ArrivalByKeyEntity::class,
        ArrivalByIndexEntity::class,
        ArrivalReplaceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OutboxDatabase : RoomDatabase() {
    abstract fun outboxDao(): OutboxDao
    abstract fun arrivalDao(): ArrivalDao

    companion object {
        // 아웃박스는 프로세스가 죽어도 남아야 하므로 파일 DB. 워커(앱을 죽인 뒤 시스템이 새 프로세스로 띄울 수도 있다)와
        // 화면이 같은 인스턴스를 쓰도록 단일 인스턴스로 둔다(RemoteMediator 예제의 ArticleDatabase 와 같은 방식).
        @Volatile
        private var instance: OutboxDatabase? = null

        fun getInstance(context: Context): OutboxDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OutboxDatabase::class.java,
                    "offline_outbox_example.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
