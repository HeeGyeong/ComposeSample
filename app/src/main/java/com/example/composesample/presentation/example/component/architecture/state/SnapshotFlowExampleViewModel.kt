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
 */
class SnapshotFlowExampleViewModel : ViewModel(), DefaultLifecycleObserver {

    private val _userInputCount = MutableStateFlow(0)
    val userInputCount: StateFlow<Int> = _userInputCount

    private val _animationProgress = MutableStateFlow(0f)
    val animationProgress: StateFlow<Float> = _animationProgress

    private val _highFrequencyData = MutableStateFlow(0)
    val highFrequencyData: StateFlow<Int> = _highFrequencyData

    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages: StateFlow<List<String>> = _logMessages

    private var animationJob: Job? = null
    private var currentAnimationStep = 0
    private var isInWaitingPeriod = false

    init {
        viewModelScope.launch {
            var counter = 0
            while (true) {
                delay(100)
                counter++
                _highFrequencyData.value = counter
            }
        }

        startAnimation()
    }

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
        if (animationJob?.isActive == true) return
        
        animationJob = viewModelScope.launch {
            while (true) {
                if (isInWaitingPeriod) {
                    delay(2000)
                    isInWaitingPeriod = false
                    currentAnimationStep = 0
                }
                
                while (currentAnimationStep <= 100) {
                    _animationProgress.value = currentAnimationStep / 100f
                    delay(50)
                    currentAnimationStep++
                }
                
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
        val timestamp = System.currentTimeMillis() % 10000
        _logMessages.value = _logMessages.value + "[$timestamp] $message"
        Log.d("SnapshotFlowExample", message)
    }

    fun clearLogs() {
        _logMessages.value = emptyList()
    }
} 