package com.example.composesample.db

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Data 레이어의 db package에 있어야하지만, DI 문제로 잠시 Presentation 레이어에 이동시켜두었습니다.
 *
 * 24.06.24
 */
@Database(entities = [ItemDTO::class, UserData::class], version = 2, exportSchema = false)
abstract class RoomSingleton : RoomDatabase() {

    // Legacy SubActivity Dao
    abstract fun itemDao(): ItemDao

    // DataCache Example Dao
    abstract fun exampleDao(): ExampleDao
}