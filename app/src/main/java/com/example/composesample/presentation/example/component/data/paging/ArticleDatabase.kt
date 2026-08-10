package com.example.composesample.presentation.example.component.data.paging

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ArticleEntity::class, ArticleRemoteKey::class],
    version = 1,
    exportSchema = false
)
abstract class ArticleDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun remoteKeyDao(): ArticleRemoteKeyDao

    companion object {
        // 같은 패키지의 다른 Room 예제(RoomIndexDatabase 등)는 in-memory DB 를 쓰지만,
        // 이 예제는 "오프라인 우선(offline-first)" 이 주제라 프로세스가 죽어도 캐시가 남아야 한다.
        // 그래서 파일 DB 를 쓰고, 파일 하나에 인스턴스가 여러 개 붙지 않도록 단일 인스턴스로 유지한다.
        @Volatile
        private var instance: ArticleDatabase? = null

        fun getInstance(context: Context): ArticleDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ArticleDatabase::class.java,
                    "remote_mediator_example.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
