package com.example.data.repository

import com.example.data.db.ExampleDao
import com.example.data.db.UserData
import kotlinx.coroutines.flow.Flow

/**
 * UserCacheRepository 의 Room 구현체.
 *
 * ExampleDao(= RoomSingleton.exampleDao())를 주입받아 CRUD 를 위임한다.
 * Room 접근 세부는 data 레이어에 캡슐화되고 presentation 은 인터페이스만 사용한다.
 */
class UserCacheRepositoryImpl(
    private val exampleDao: ExampleDao
) : UserCacheRepository {
    override fun searchUserName(name: String): Flow<List<UserData>> =
        exampleDao.searchData(name)

    override suspend fun insertUserData(item: UserData) =
        exampleDao.insertData(item)

    override suspend fun updateUserData(item: UserData) =
        exampleDao.updateData(item)

    override suspend fun deleteUserData(item: UserData) =
        exampleDao.deleteData(item)

    override suspend fun allDataDelete() =
        exampleDao.allDataDelete()
}
