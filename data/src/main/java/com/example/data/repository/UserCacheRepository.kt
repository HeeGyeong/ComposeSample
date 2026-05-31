package com.example.data.repository

import com.example.data.db.UserData
import kotlinx.coroutines.flow.Flow

/**
 * DataCache 예제용 Repository 추상화.
 *
 * presentation(DataCacheViewModel)이 RoomSingleton/DAO 구현을 직접 참조하지 않고
 * 이 인터페이스에 의존하도록 하여 의존성 역전을 적용한다.
 *
 * UserData가 Room @Entity 라 순수 Kotlin domain 모듈에 둘 수 없으므로
 * (도메인 모델 매핑은 본 변경 범위에서 제외) Repository 추상화를 data 레이어에 배치한다.
 * 기존 PostDataSource/PostDataSourceImpl 의 data 내부 추상화 선례와 동일한 방식이다.
 */
interface UserCacheRepository {
    fun searchUserName(name: String): Flow<List<UserData>>
    suspend fun insertUserData(item: UserData)
    suspend fun updateUserData(item: UserData)
    suspend fun deleteUserData(item: UserData)
    suspend fun allDataDelete()
}
