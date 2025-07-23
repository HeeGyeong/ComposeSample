package com.example.composesample.presentation.example.component.architecture.state

import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
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
class SnapshotFlowExampleViewModel : ViewModel(), DefaultLifecycleObserver {

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

    // 애니메이션 Job 관리용
    private var animationJob: Job? = null
    private var currentAnimationStep = 0
    private var isInWaitingPeriod = false

    init {
        // 고빈도 데이터 생성 (매 100ms마다 업데이트)
        viewModelScope.launch {
            while (true) {
                delay(100)
                _highFrequencyData.value += 1
            }
        }

        // 애니메이션은 라이프사이클에 따라 시작/정지
        startAnimation()
    }

    // 라이프사이클 콜백
    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        startAnimation()
        addLogMessage("라이프사이클: onResume - 애니메이션 재개 (${currentAnimationStep}%부터)")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        stopAnimation()
        addLogMessage("라이프사이클: onPause - 애니메이션 일시정지 (${currentAnimationStep}%에서)")
    }

    private fun startAnimation() {
        // 이미 실행 중이면 리턴
        if (animationJob?.isActive == true) return
        
        animationJob = viewModelScope.launch {
            while (true) {
                // 대기 기간이면 대기 완료 후 0부터 시작
                if (isInWaitingPeriod) {
                    delay(2000)
                    isInWaitingPeriod = false
                    currentAnimationStep = 0
                }
                
                // 현재 위치부터 100까지 애니메이션
                while (currentAnimationStep <= 100) {
                    _animationProgress.value = currentAnimationStep / 100f
                    delay(50)
                    currentAnimationStep++
                }
                
                // 100% 완료 후 대기 모드로 전환
                isInWaitingPeriod = true
            }
        }
    }

    private fun stopAnimation() {
        animationJob?.cancel()
        animationJob = null
    }

    fun setLifecycleOwner(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(this)
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