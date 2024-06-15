package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ItemDTO::class, UserData::class], version = 2, exportSchema = false)
abstract class RoomSingleton : RoomDatabase() {

    // Legacy SubActivity Dao
    abstract fun itemDao(): ItemDao

    // DataCache Example Dao
    abstract fun exampleDao(): ExampleDao

    companion object {
        private var INSTANCE: RoomSingleton? = null
        fun getInstance(context: Context): RoomSingleton {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    RoomSingleton::class.java,
                    "room_db")
                    .build()
            }
            return INSTANCE as RoomSingleton
        }
    }
}