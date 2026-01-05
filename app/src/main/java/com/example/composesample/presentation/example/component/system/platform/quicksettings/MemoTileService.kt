package com.example.composesample.presentation.example.component.system.platform.quicksettings

import android.app.PendingIntent
import android.content.Intent
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

/**
 * Memo Tile Service
 *
 * 마이크로 인터랙션 패턴 - 메모 타일
 * 클릭하면 메모 입력 다이얼로그를 띄우는 Activity로 이동합니다.
 */
@RequiresApi(Build.VERSION_CODES.N)
class MemoTileService : TileService() {

    companion object {
        private val _memosFlow = MutableStateFlow<List<MemoItem>>(emptyList())
        val memosFlow: StateFlow<List<MemoItem>> = _memosFlow.asStateFlow()

        private val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

        fun addMemo(content: String) {
            if (content.isNotBlank()) {
                val newMemo = MemoItem(
                    id = System.currentTimeMillis(),
                    content = content,
                    timestamp = dateFormat.format(Date())
                )
                _memosFlow.value = listOf(newMemo) + _memosFlow.value
            }
        }

        fun deleteMemo(id: Long) {
            _memosFlow.value = _memosFlow.value.filter { it.id != id }
        }

        fun clearAllMemos() {
            _memosFlow.value = emptyList()
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
        // 메모 입력 Activity 실행
        val intent = Intent(this, MemoInputActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ requires PendingIntent
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    private fun updateTileState() {
        val memoCount = _memosFlow.value.size

        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            label = "Memo"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                subtitle = if (memoCount > 0) "$memoCount 개" else "탭하여 추가"
            }
            contentDescription = "Memo tile. $memoCount memos saved"
            updateTile()
        }
    }
}

data class MemoItem(
    val id: Long,
    val content: String,
    val timestamp: String
)

