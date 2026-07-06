package com.example.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserData::class], version = 3, exportSchema = false)
abstract class RoomSingleton : RoomDatabase() {

    abstract fun exampleDao(): ExampleDao // 유저 데이터 DAO
}