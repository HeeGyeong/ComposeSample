package com.example.composesample.presentation.example.component.refresh

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class RefreshViewModel(application: Application) : AndroidViewModel(application) {

    private val _refreshState = mutableStateOf(PullToRefreshState())
    val refreshState: State<PullToRefreshState> = _refreshState

    fun addRefreshItem() {
        viewModelScope.launch {
            _refreshState.value = _refreshState.value.copy(isLoading = true)
            delay(1000L)
            _refreshState.value = _refreshState.value.copy(
                isLoading = false,
                refreshItemList = _refreshState.value.refreshItemList.toMutableList().also {
                    it.add(
                        index = it.size,
                        element = RefreshItem(
                            index = "Index : ${it.size}",
                            randomIntData = Random.nextInt(1,1000)
                        )
                    )
                }
            )
        }
    }

    fun changeRefreshState(isLoading: Boolean) {
        _refreshState.value = _refreshState.value.copy(isLoading = isLoading)
    }

    fun updateItem() {
        _refreshState.value = _refreshState.value.copy(
            refreshItemList = _refreshState.value.refreshItemList.toMutableList().also {
                it.add(
                    index = it.size,
                    element = RefreshItem(
                        index = "Index : ${it.size}",
                        randomIntData = Random.nextInt(1,1000)
                    )
                )
            }
        )
    }
}

data class RefreshItem(
    val index: String,
    val randomIntData: Int
)

data class PullToRefreshState(
    val isLoading: Boolean = false,
    val refreshItemList: List<RefreshItem> = listOf(
        RefreshItem("Index : 0", 123),
        RefreshItem("Index : 1", 6),
        RefreshItem("Index : 2", 812),
        RefreshItem("Index : 3", 32),
        RefreshItem("Index : 4", 657),
        RefreshItem("Index : 5", 12),
        RefreshItem("Index : 6", 4),
    )
)
