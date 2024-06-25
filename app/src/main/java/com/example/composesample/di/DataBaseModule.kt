package com.example.composesample.di

import androidx.room.Room
import com.example.composesample.db.RoomSingleton
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidApplication(), RoomSingleton::class.java, "room_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    single { get<RoomSingleton>().itemDao() }
    single { get<RoomSingleton>().exampleDao() }
}