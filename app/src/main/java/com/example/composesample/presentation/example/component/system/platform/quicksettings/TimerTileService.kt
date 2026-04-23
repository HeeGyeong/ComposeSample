package com.example.composesample.presentation.example.component.system.platform.quicksettings

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.N)
class TimerTileService : TileService() {

    companion object {
        private val _timerSecondsFlow = MutableStateFlow(0)
        val timerSecondsFlow: StateFlow<Int> = _timerSecondsFlow.asStateFlow()

        private val _isRunningFlow = MutableStateFlow(false)
        val isRunningFlow: StateFlow<Boolean> = _isRunningFlow.asStateFlow()

        private val timerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        private var timerJob: Job? = null

        fun resetTimer() {
            stopTimer()
            _timerSecondsFlow.value = 0
        }

        fun toggleTimer() {
            if (_isRunningFlow.value) {
                stopTimer()
            } else {
                startTimer()
            }
        }

        private fun startTimer() {
            if (_isRunningFlow.value) return
            _isRunningFlow.value = true
            timerJob = timerScope.launch {
                while (_isRunningFlow.value) {
                    delay(1000)
                    _timerSecondsFlow.value++
                }
            }
        }

        private fun stopTimer() {
            _isRunningFlow.value = false
            timerJob?.cancel()
            timerJob = null
        }

        fun formatTime(seconds: Int): String {
            val mins = seconds / 60
            val secs = seconds % 60
            return String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }

    override fun onTileAdded() {
        super.onTileAdded()
        updateTileState()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        toggleTimer()
        updateTileState()
    }

    private fun updateTileState() {
        val isRunning = _isRunningFlow.value
        val seconds = _timerSecondsFlow.value

        qsTile?.apply {
            state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = "Timer"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = if (isRunning) "▶ ${formatTime(seconds)}" else "⏸ ${formatTime(seconds)}"
            }
            contentDescription = "Timer tile. ${if (isRunning) "Running" else "Stopped"}: ${formatTime(seconds)}"
            updateTile()
        }
    }
}

