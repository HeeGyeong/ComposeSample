package com.example.composesample.presentation.example.component.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        NoIndexEntity::class,
        SingleIndexEntity::class,
        CompositeIndexEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RoomIndexDatabase : RoomDatabase() {
    abstract fun noIndexDao(): NoIndexDao
    abstract fun singleIndexDao(): SingleIndexDao
    abstract fun compositeIndexDao(): CompositeIndexDao

    companion object {
        // 예제용 in-memory DB — 재진입 시 시드 재생성
        fun create(context: Context): RoomIndexDatabase =
            Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                RoomIndexDatabase::class.java
            ).build()
    }
}
