package com.example.composesample.presentation.example.component.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AuthorEntity::class,
        PostEntity::class,
        TagEntity::class,
        PostTagCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MultiTableInsertDatabase : RoomDatabase() {
    abstract fun authorDao(): AuthorDao
    abstract fun postDao(): PostDao
    abstract fun tagDao(): TagDao
    abstract fun postTagDao(): PostTagDao

    companion object {
        // 예제용 in-memory DB — 앱 종료/재진입 시 리셋
        fun create(context: Context): MultiTableInsertDatabase =
            Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                MultiTableInsertDatabase::class.java
            ).build()
    }
}
