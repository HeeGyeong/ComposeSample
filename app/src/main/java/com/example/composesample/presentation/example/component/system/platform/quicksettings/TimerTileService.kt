package com.example.composesample.presentation.example.component.system.platform.quicksettings

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@RequiresApi(Build.VERSION_CODES.N)
class TimerTileService : TileService() {

    companion object {
        private val _timerSecondsFlow = MutableStateFlow(0)
        val timerSecondsFlow: StateFlow<Int> = _timerSecondsFlow.asStateFlow()

        private val _isRunningFlow = MutableStateFlow(false)
        val isRunningFlow: StateFlow<Boolean> = _isRunningFlow.asStateFlow()

        private var handler: Handler? = null
        private var runnable: Runnable? = null

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
            handler = Handler(Looper.getMainLooper())
            runnable = object : Runnable {
                override fun run() {
                    if (_isRunningFlow.value) {
                        _timerSecondsFlow.value++
                        handler?.postDelayed(this, 1000)
                    }
                }
            }
            handler?.post(runnable!!)
        }

        private fun stopTimer() {
            _isRunningFlow.value = false
            runnable?.let { handler?.removeCallbacks(it) }
            handler = null
            runnable = null
        }

        fun formatTime(seconds: Int): String {
            val mins = seconds / 60
            val secs = seconds % 60
            return String.format("%02d:%02d", mins, secs)
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

