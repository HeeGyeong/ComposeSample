package com.example.composesample.presentation.example.component.architecture.development.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ─── UI 상태 ───────────────────────────────────────────────────────────────────

sealed class FlowTestUiState {
    object Empty : FlowTestUiState()
    object Loading : FlowTestUiState()
    data class Success(val items: List<String>) : FlowTestUiState()
    data class Error(val message: String) : FlowTestUiState()
}

// ─── 단방향 이벤트 (SharedFlow 테스트 대상) ──────────────────────────────────────

sealed class FlowTestEvent {
    data class ShowToast(val message: String) : FlowTestEvent()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

class TurbineFlowTestViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<FlowTestUiState>(FlowTestUiState.Empty)
    val uiState: StateFlow<FlowTestUiState> = _uiState.asStateFlow()

    // 단방향 이벤트 스트림 - Turbine이 빛나는 영역
    private val _events = MutableSharedFlow<FlowTestEvent>()
    val events: SharedFlow<FlowTestEvent> = _events.asSharedFlow()

    /**
     * 데이터 패치 시뮬레이션
     * Empty → Loading → Success / Error 순서로 상태 변화
     */
    fun fetchData(shouldFail: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = FlowTestUiState.Loading
            delay(1_000L) // 네트워크 지연 시뮬레이션
            if (shouldFail) {
                _uiState.value = FlowTestUiState.Error("데이터 로딩 실패")
                _events.emit(FlowTestEvent.ShowToast("오류가 발생했습니다"))
            } else {
                _uiState.value = FlowTestUiState.Success(
                    items = listOf("결과 Item 1", "결과 Item 2", "결과 Item 3")
                )
                _events.emit(FlowTestEvent.ShowToast("로딩 완료"))
            }
        }
    }

    fun reset() {
        _uiState.value = FlowTestUiState.Empty
    }
}
