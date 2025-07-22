package com.example.composesample.presentation.example.component.architecture.state

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * SnapshotFlow vs collectAsState Example ViewModel
 *
 * 주요 차이점과 올바른 사용법:
 * 1. collectAsState: Flow → State 변환, UI 업데이트용
 * 2. snapshotFlow: Compose state → Flow 변환, side-effect용
 * 3. collectAsStateWithLifecycle: 라이프사이클 인식 버전
 */
class SnapshotFlowExampleViewModel : ViewModel() {

    // 1. ViewModel → UI 데이터 바인딩용 (collectAsState 올바른 사용)
    private val _userInputCount = MutableStateFlow(0)
    val userInputCount: StateFlow<Int> = _userInputCount

    private val _animationProgress = MutableStateFlow(0f)
    val animationProgress: StateFlow<Float> = _animationProgress

    // 2. 고빈도 업데이트 데이터 (collectAsState로 하면 성능 문제)
    private val _highFrequencyData = MutableStateFlow(0)
    val highFrequencyData: StateFlow<Int> = _highFrequencyData

    // 3. 로그 메시지들 (side-effect 결과)
    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages

    init {
        // 고빈도 데이터 생성 (매 100ms마다 업데이트)
        viewModelScope.launch {
            while (true) {
                delay(100)
                _highFrequencyData.value += 1
            }
        }

        // 애니메이션 진행도 시뮬레이션
        viewModelScope.launch {
            while (true) {
                for (i in 0..100) {
                    _animationProgress.value = i / 100f
                    delay(50)
                }
                delay(2000) // 잠시 대기 후 다시 시작
            }
        }
    }

    fun incrementUserInput() {
        _userInputCount.value += 1
    }

    fun addLogMessage(message: String) {
        _logMessages.value = _logMessages.value + "[${System.currentTimeMillis() % 10000}] $message"
        Log.d("SnapshotFlowExample", message)
    }

    fun clearLogs() {
        _logMessages.value = emptyList()
    }
} 