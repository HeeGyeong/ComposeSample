package com.example.composesample.presentation.example.component.init

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InitTestViewModel : ViewModel() {
    private val _isLaunchedEffectLoading = MutableStateFlow(false)
    val isLaunchedEffectLoading = _isLaunchedEffectLoading.asStateFlow()

    private val _isViewModelInitLoading = MutableStateFlow(false)
    val isViewModelInitLoading = _isViewModelInitLoading.asStateFlow()

    private val _isInitLoading = MutableStateFlow(false)
    val isInitLoading = _isInitLoading
        .onStart { changeInitLoading() } // StateFlow를 flow로 변환, 해당 flow가 시작될 때 changeInitLoading() 호출
        .stateIn ( // flow 상태를 stateFlow로 반환
            scope = viewModelScope, // 해당 flow가 생성될 때의 scope
            started = SharingStarted.WhileSubscribed(5000L), // 구독 관련 설정. 구독 해제 후 5초가 지나면 데이터 유실.
            initialValue = false // 해당 flow가 생성될 때의 초기값
        )

    private val _testLoadingCount = MutableStateFlow(0)
    val testLoadingCount = _testLoadingCount.asStateFlow()

    // viewModel instance가 생성될 때 한번 호출 된다.
    init {
        Log.d("TAG", "Init ViewModel")
        _testLoadingCount.value = 0
        changeViewModelInitLoading()
    }

    fun changeLaunchedEffectLoading() {
        Log.d("TAG", "LaunchedEffect Loading")
        _testLoadingCount.value++

        viewModelScope.launch {
            Log.d("TAG", "LaunchedEffect loadingCount : ${testLoadingCount.value}")

            _isLaunchedEffectLoading.value = true
            delay(3000L)
            _isLaunchedEffectLoading.value = false
        }
    }

    private fun changeViewModelInitLoading() {
        Log.d("TAG", "ViewModel Init Loading")
        _testLoadingCount.value++

        viewModelScope.launch {
            Log.d("TAG", "ViewModel loadingCount : ${testLoadingCount.value}")
            _isViewModelInitLoading.value = true
            delay(3000L)
            _isViewModelInitLoading.value = false
        }
    }

    private fun changeInitLoading() {
        Log.d("TAG", "Init Loading")
        _testLoadingCount.value++

        viewModelScope.launch {
            Log.d("TAG", "changeInitLoading loadingCount : ${testLoadingCount.value}")
            _isInitLoading.value = true
            delay(3000L)
            _isInitLoading.value = false
        }
    }
}