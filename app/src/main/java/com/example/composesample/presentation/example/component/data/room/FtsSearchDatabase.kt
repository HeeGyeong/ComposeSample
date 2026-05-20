package com.example.composesample.presentation.example.component.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DocEntity::class,
        DocFtsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FtsSearchDatabase : RoomDatabase() {
    abstract fun docDao(): DocDao
    abstract fun docFtsDao(): DocFtsDao

    companion object {
        // 예제용 in-memory DB — 재진입 시 시드 재생성
        fun create(context: Context): FtsSearchDatabase =
            Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                FtsSearchDatabase::class.java
            ).build()
    }
}
