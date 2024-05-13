package com.example.composesample.sub

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.composesample.data.ItemDTO
import com.example.composesample.data.RoomSingleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SubActivityViewModel(application: Application) : AndroidViewModel(application) {

    private val db: RoomSingleton = RoomSingleton.getInstance(application)

    fun search(startsWith: String): Flow<List<ItemDTO>> {
        return db.itemDao().searchData(startsWith)
    }

    fun insert(item: ItemDTO) {
        viewModelScope.launch(Dispatchers.IO) {
            db.itemDao().insert(item)
        }
    }

    fun update(item: ItemDTO) {
        viewModelScope.launch(Dispatchers.IO) {
            db.itemDao().update(item)
        }
    }

    fun delete(item: ItemDTO) {
        viewModelScope.launch(Dispatchers.IO) {
            db.itemDao().delete(item)
        }
    }

    fun clear() {
        viewModelScope.launch(Dispatchers.IO) {
            db.itemDao().clear()
        }
    }
}