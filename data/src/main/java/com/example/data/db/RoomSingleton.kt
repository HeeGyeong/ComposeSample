package com.example.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ItemDTO::class, UserData::class], version = 2, exportSchema = false)
abstract class RoomSingleton : RoomDatabase() {

    abstract fun itemDao(): ItemDao // 아이템 DAO

    abstract fun exampleDao(): ExampleDao // 유저 데이터 DAO
}