package com.example.composesample.presentation.example.ui.cache

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.RoomSingleton
import com.example.data.db.UserData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DataCacheViewModel(application: Application) : AndroidViewModel(application) {

    private val db: RoomSingleton = RoomSingleton.getInstance(application)

    fun searchUserName(name: String): Flow<List<UserData>> {
        return db.exampleDao().searchData(name)
    }

    fun insertUserData(item: UserData) {
        viewModelScope.launch(Dispatchers.IO) {
            db.exampleDao().insertData(item)
        }
    }

    fun updateUserData(item: UserData) {
        viewModelScope.launch(Dispatchers.IO) {
            db.exampleDao().updateData(item)
        }
    }

    fun deleteUserData(item: UserData) {
        viewModelScope.launch(Dispatchers.IO) {
            db.exampleDao().deleteData(item)
        }
    }

    fun allDataDelete() {
        viewModelScope.launch(Dispatchers.IO) {
            db.exampleDao().allDataDelete()
        }
    }
}