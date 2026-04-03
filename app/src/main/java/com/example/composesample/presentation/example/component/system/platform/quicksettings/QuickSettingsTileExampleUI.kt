package com.example.composesample.presentation.example.component.system.platform.quicksettings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.R
import com.example.composesample.presentation.MainHeader

/**
 * Quick Settings Tile Example UI
 *
 * 마이크로 인터랙션 패턴을 활용한 Quick Settings Tile 예제입니다.
 * - 카운터 타일: 클릭할 때마다 값 증가
 * - 토글 타일: on/off 전환
 */
@Composable
fun QuickSettingsTileExampleUI(
    onBackEvent: () -> Unit
) {
    val context = LocalContext.current
    val counterValue by CounterTileService.counterFlow.collectAsStateWithLifecycle()
    val toggleState by ToggleTileService.toggleStateFlow.collectAsStateWithLifecycle()
    val timerSeconds by TimerTileService.timerSecondsFlow.collectAsStateWithLifecycle()
    val timerRunning by TimerTileService.isRunningFlow.collectAsStateWithLifecycle()
    val actionCount by QuickActionTileService.actionCountFlow.collectAsStateWithLifecycle()
    val lastActionTime by QuickActionTileService.lastActionTimeFlow.collectAsStateWithLifecycle()
    val memos by MemoTileService.memosFlow.collectAsStateWithLifecycle()

    var showMemoDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        MainHeader(
            title = "Quick Settings Tile",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { CounterTileCard(counterValue, context) }
            item { ToggleTileCard(toggleState, context) }
            item { TimerTileCard(timerSeconds, timerRunning, context) }
            item { QuickActionTileCard(actionCount, lastActionTime, context) }
            item {
                MemoTileCard(
                    memos = memos,
                    onAddMemoClick = { showMemoDialog = true },
                    onDeleteMemo = { MemoTileService.deleteMemo(it) },
                    onClearAll = { MemoTileService.clearAllMemos() },
                    context = context
                )
            }
            item { HowToAddTileCard(context) }
        }
    }

    if (showMemoDialog) {
        MemoInputDialog(
            onDismiss = { showMemoDialog = false },
            onSave = { memo ->
                MemoTileService.addMemo(memo)
                showMemoDialog = false
            }
        )
    }
}

@Composable
private fun CounterTileCard(counterValue: Int, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE8F5E9),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔢 카운터 타일 데모",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "빠른 설정 패널에서 타일을 클릭하면 카운터가 증가합니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2E7D32).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "현재 카운터 값",
                        fontSize = 14.sp,
                        color = Color(0xFF2E7D32)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$counterValue",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "빠른 설정 패널에서 'Counter' 타일을 클릭하세요",
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { CounterTileService.resetCounter() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("카운터 리셋", color = Color.White, fontSize = 12.sp)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Button(
                        onClick = {
                            TileService.requestListeningState(
                                context,
                                ComponentName(context, CounterTileService::class.java)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("타일 새로고침", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleTileCard(toggleState: Boolean, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFCE4EC),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "🔄 토글 타일 데모",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC2185B)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "빠른 설정 패널에서 타일을 클릭하면 상태가 전환됩니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (toggleState) Color(0xFF4CAF50).copy(alpha = 0.2f)
                else Color(0xFFF44336).copy(alpha = 0.2f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "현재 상태",
                        fontSize = 14.sp,
                        color = if (toggleState) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (toggleState) "ON ✅" else "OFF ❌",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (toggleState) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "빠른 설정 패널에서 'Toggle' 타일을 클릭하세요",
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Button(
                    onClick = {
                        TileService.requestListeningState(
                            context,
                            ComponentName(context, ToggleTileService::class.java)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFC2185B)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("타일 새로고침", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun TimerTileCard(timerSeconds: Int, isRunning: Boolean, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFE1F5FE),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⏱️ 타이머 타일 데모",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0277BD)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "빠른 설정 패널에서 타일을 클릭하면 타이머가 시작/정지됩니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (isRunning) Color(0xFF4CAF50).copy(alpha = 0.2f)
                else Color(0xFF0277BD).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRunning) "타이머 실행 중" else "타이머 정지됨",
                        fontSize = 14.sp,
                        color = if (isRunning) Color(0xFF2E7D32) else Color(0xFF0277BD)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = TimerTileService.formatTime(timerSeconds),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRunning) Color(0xFF2E7D32) else Color(0xFF0277BD)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "빠른 설정 패널에서 'Timer' 타일을 클릭하세요",
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { TimerTileService.toggleTimer() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (isRunning) Color(0xFFF44336) else Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (isRunning) "정지" else "시작",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = { TimerTileService.resetTimer() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF0277BD)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("리셋", color = Color.White, fontSize = 12.sp)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Button(
                        onClick = {
                            TileService.requestListeningState(
                                context,
                                ComponentName(context, TimerTileService::class.java)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF03A9F4)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("새로고침", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionTileCard(actionCount: Int, lastActionTime: String?, context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF8E1),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚡ 퀵 액션 타일 데모",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF8F00)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "빠른 설정 패널에서 타일을 클릭하면 즉시 액션이 실행됩니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFF8F00).copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "실행 횟수",
                        fontSize = 14.sp,
                        color = Color(0xFFFF8F00)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "⚡ x$actionCount",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF8F00)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    lastActionTime?.let { time ->
                        Text(
                            text = "마지막 실행: $time",
                            fontSize = 12.sp,
                            color = Color(0xFF666666)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = "빠른 설정 패널에서 'Quick Action' 타일을 클릭하세요",
                        fontSize = 11.sp,
                        color = Color(0xFF666666)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { QuickActionTileService.resetActionCount() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF8F00)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("카운트 리셋", color = Color.White, fontSize = 12.sp)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Button(
                        onClick = {
                            TileService.requestListeningState(
                                context,
                                ComponentName(context, QuickActionTileService::class.java)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFFC107)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("타일 새로고침", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoTileCard(
    memos: List<MemoItem>,
    onAddMemoClick: () -> Unit,
    onDeleteMemo: (Long) -> Unit,
    onClearAll: () -> Unit,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFF3E5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📝 메모 타일 데모",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6A1B9A)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "빠른 설정 패널에서 타일을 클릭하면 메모를 입력할 수 있습니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF6A1B9A).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "저장된 메모 (${memos.size}개)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6A1B9A)
                        )

                        if (memos.isNotEmpty()) {
                            Text(
                                text = "전체 삭제",
                                fontSize = 12.sp,
                                color = Color(0xFFC62828),
                                modifier = Modifier.clickable { onClearAll() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (memos.isEmpty()) {
                        Text(
                            text = "저장된 메모가 없습니다.\n빠른 설정 패널에서 'Memo' 타일을 클릭하거나\n아래 버튼을 눌러 메모를 추가하세요.",
                            fontSize = 12.sp,
                            color = Color(0xFF666666),
                            lineHeight = 18.sp
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            memos.take(5).forEach { memo ->
                                MemoItemRow(
                                    memo = memo,
                                    onDelete = { onDeleteMemo(memo.id) }
                                )
                            }

                            if (memos.size > 5) {
                                Text(
                                    text = "... 외 ${memos.size - 5}개 더",
                                    fontSize = 11.sp,
                                    color = Color(0xFF666666)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onAddMemoClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6A1B9A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("메모 추가", color = Color.White, fontSize = 12.sp)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Button(
                        onClick = {
                            TileService.requestListeningState(
                                context,
                                ComponentName(context, MemoTileService::class.java)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9C27B0)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("타일 새로고침", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoItemRow(
    memo: MemoItem,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = memo.content,
                    fontSize = 13.sp,
                    color = Color(0xFF333333),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = memo.timestamp,
                    fontSize = 10.sp,
                    color = Color(0xFF999999)
                )
            }

            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "삭제",
                tint = Color(0xFFC62828),
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable { onDelete() }
            )
        }
    }
}

@Composable
private fun MemoInputDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var memoText by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            backgroundColor = Color.White,
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "📝 빠른 메모",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6A1B9A)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = memoText,
                    onValueChange = { memoText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text(
                            "메모를 입력하세요...",
                            color = Color.Gray
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFF6A1B9A),
                        cursorColor = Color(0xFF6A1B9A)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("취소", color = Color(0xFF666666))
                    }

                    Button(
                        onClick = {
                            if (memoText.isNotBlank()) {
                                onSave(memoText)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF6A1B9A)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = memoText.isNotBlank()
                    ) {
                        Text("저장", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun HowToAddTileCard(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        backgroundColor = Color(0xFFFFF3E0),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📋 타일 추가 방법",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE65100).copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    StepRow("1", "화면 상단에서 알림 패널을 내립니다")
                    StepRow("2", "다시 한 번 내려 빠른 설정 패널을 엽니다")
                    StepRow("3", "연필(편집) 아이콘을 탭합니다")
                    StepRow("4", "'Counter' 또는 'Toggle' 타일을 찾습니다")
                    StepRow("5", "타일을 드래그하여 활성 영역에 추가합니다")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFE65100)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("설정 열기", color = Color.White, fontSize = 12.sp)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Button(
                        onClick = {
                            requestAddTile(context, CounterTileService::class.java, "Counter")
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFFF9800)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("타일 추가 요청", color = Color.White, fontSize = 11.sp)
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Android 13+에서는 프로그래매틱하게 타일 추가를 요청할 수 있습니다.",
                    fontSize = 11.sp,
                    color = Color(0xFFE65100)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun requestAddTile(context: Context, serviceClass: Class<*>, label: String) {
    try {
        val statusBarManager = context.getSystemService(android.app.StatusBarManager::class.java)
        statusBarManager?.requestAddTileService(
            ComponentName(context, serviceClass),
            label,
            Icon.createWithResource(context, R.drawable.ic_launcher_foreground),
            context.mainExecutor
        ) { resultCode ->
            // 결과 처리
        }
    } catch (e: Exception) {
        Log.e("QuickSettingsTile", "타일 업데이트 실패: ${e.message}", e)
    }
}

@Composable
private fun StepRow(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color(0xFFE65100)
        ) {
            Text(
                text = number,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                fontSize = 12.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
    }
}