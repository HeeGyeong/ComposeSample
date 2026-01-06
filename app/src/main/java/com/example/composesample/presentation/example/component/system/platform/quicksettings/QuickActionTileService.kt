package com.example.composesample.presentation.example.component.system.platform.quicksettings

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.N)
class QuickActionTileService : TileService() {

    companion object {
        private val _actionCountFlow = MutableStateFlow(0)
        val actionCountFlow: StateFlow<Int> = _actionCountFlow.asStateFlow()

        private val _lastActionTimeFlow = MutableStateFlow<String?>(null)
        val lastActionTimeFlow: StateFlow<String?> = _lastActionTimeFlow.asStateFlow()

        private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        fun resetActionCount() {
            _actionCountFlow.value = 0
            _lastActionTimeFlow.value = null
        }

        private fun executeAction() {
            _actionCountFlow.value++
            _lastActionTimeFlow.value = dateFormat.format(Date())
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
        executeAction()
        updateTileState()
    }

    private fun updateTileState() {
        val count = _actionCountFlow.value

        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            label = "Quick Action"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = "⚡ x$count"
            }
            contentDescription = "Quick Action tile. Executed $count times"
            updateTile()
        }
    }
}

