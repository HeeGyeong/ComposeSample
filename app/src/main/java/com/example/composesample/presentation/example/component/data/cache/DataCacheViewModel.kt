package com.example.composesample.presentation.example.component.data.cache

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.UserData
import com.example.data.repository.UserCacheRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DataCacheViewModel(
    application: Application,
    // RoomSingleton/DAO 직접 참조 대신 Repository 추상화에 의존 (의존성 역전)
    private val userCacheRepository: UserCacheRepository
) : AndroidViewModel(application) {

    fun searchUserName(name: String): Flow<List<UserData>> {
        return userCacheRepository.searchUserName(name)
    }

    fun insertUserData(item: UserData) {
        viewModelScope.launch(Dispatchers.IO) {
            userCacheRepository.insertUserData(item)
        }
    }

    fun updateUserData(item: UserData) {
        viewModelScope.launch(Dispatchers.IO) {
            userCacheRepository.updateUserData(item)
        }
    }

    fun deleteUserData(item: UserData) {
        viewModelScope.launch(Dispatchers.IO) {
            userCacheRepository.deleteUserData(item)
        }
    }

    fun allDataDelete() {
        viewModelScope.launch(Dispatchers.IO) {
            userCacheRepository.allDataDelete()
        }
    }
}