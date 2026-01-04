package com.example.composesample.presentation.example.component.system.platform.quicksettings

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Counter Tile Service
 *
 * 마이크로 인터랙션 패턴 - 카운터 타일
 * 클릭할 때마다 카운터 값이 증가합니다.
 */
@RequiresApi(Build.VERSION_CODES.N)
class CounterTileService : TileService() {

    companion object {
        private val _counterFlow = MutableStateFlow(0)
        val counterFlow: StateFlow<Int> = _counterFlow.asStateFlow()

        fun resetCounter() {
            _counterFlow.value = 0
        }

        private fun incrementCounter() {
            _counterFlow.value++
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
        incrementCounter()
        updateTileState()
    }

    private fun updateTileState() {
        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            label = "Counter"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = "Count: ${_counterFlow.value}"
            }
            contentDescription = "Counter tile. Current count: ${_counterFlow.value}"
            updateTile()
        }
    }
}

