package com.example.composesample.presentation.example.component.system.platform.quicksettings

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Toggle Tile Service
 *
 * 마이크로 인터랙션 패턴 - 토글 타일
 * 클릭할 때마다 on/off 상태가 전환됩니다.
 */
@RequiresApi(Build.VERSION_CODES.N)
class ToggleTileService : TileService() {

    companion object {
        private val _toggleStateFlow = MutableStateFlow(false)
        val toggleStateFlow: StateFlow<Boolean> = _toggleStateFlow.asStateFlow()

        private fun toggle() {
            _toggleStateFlow.value = !_toggleStateFlow.value
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
        toggle()
        updateTileState()
    }

    private fun updateTileState() {
        val isOn = _toggleStateFlow.value

        qsTile?.apply {
            state = if (isOn) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            label = "Toggle"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = if (isOn) "ON" else "OFF"
            }
            contentDescription = "Toggle tile. Current state: ${if (isOn) "On" else "Off"}"
            updateTile()
        }
    }
}

