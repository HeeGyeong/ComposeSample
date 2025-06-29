package com.example.composesample.presentation.example.component.init

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 초기화 타이밍과 StateFlow 동작을 테스트하기 위한 ViewModel
 *
 * 3가지 초기화 패턴을 비교:
 * 1. LaunchedEffect에서 트리거되는 로딩
 * 2. ViewModel init 블록에서 트리거되는 로딩
 * 3. StateFlow의 onStart에서 트리거되는 로딩
 */
class InitTestViewModel : ViewModel() {

    // LaunchedEffect에서 수동으로 호출되는 로딩 상태
    private val _isLaunchedEffectLoading = MutableStateFlow(false)
    val isLaunchedEffectLoading = _isLaunchedEffectLoading.asStateFlow()

    // ViewModel 생성 시점에 자동으로 트리거되는 로딩 상태
    private val _isViewModelInitLoading = MutableStateFlow(false)
    val isViewModelInitLoading = _isViewModelInitLoading.asStateFlow()

    // StateFlow 구독 시점에 자동으로 트리거되는 로딩 상태
    private val _isInitLoading = MutableStateFlow(false)
    val isInitLoading = _isInitLoading
        .onStart { changeInitLoading() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = false
        )

    // 각 로딩이 몇 번 호출되었는지 카운트하는 변수 (디버깅용)
    private val _testLoadingCount = MutableStateFlow(0)
    val testLoadingCount = _testLoadingCount.asStateFlow()

    init {
        _testLoadingCount.value = 0
        changeViewModelInitLoading()
    }

    /**
     * LaunchedEffect에서 수동으로 호출하는 로딩 함수
     */
    fun changeLaunchedEffectLoading() {
        _testLoadingCount.value++

        viewModelScope.launch {
            _isLaunchedEffectLoading.value = true
            delay(3000L)
            _isLaunchedEffectLoading.value = false
        }
    }

    /**
     * ViewModel init 블록에서 자동으로 호출되는 로딩 함수
     */
    private fun changeViewModelInitLoading() {
        _testLoadingCount.value++

        viewModelScope.launch {
            _isViewModelInitLoading.value = true
            delay(3000L)
            _isViewModelInitLoading.value = false
        }
    }

    /**
     * StateFlow의 onStart에서 자동으로 호출되는 로딩 함수
     */
    private fun changeInitLoading() {
        _testLoadingCount.value++

        viewModelScope.launch {
            _isInitLoading.value = true
            delay(3000L)
            _isInitLoading.value = false
        }
    }
}