package com.example.composesample.presentation.example.component.system.platform.haptic

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HapticFeedbackExampleUI(onBackEvent: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    // 마지막으로 실행된 햅틱 이름을 잠시 표시
    var lastTriggered by remember { mutableStateOf("") }

    fun triggerCompose(type: HapticFeedbackType, label: String) {
        haptic.performHapticFeedback(type)
        lastTriggered = label
        scope.launch { delay(1500); lastTriggered = "" }
    }

    fun triggerView(constant: Int, label: String) {
        view.performHapticFeedback(constant)
        lastTriggered = label
        scope.launch { delay(1500); lastTriggered = "" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
            }
            Text(
                text = "Haptic Feedback",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 마지막 실행 표시 배너
        if (lastTriggered.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "✓ $lastTriggered 실행됨",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoCard(
                    title = "Haptic Feedback 두 가지 방법",
                    description = "① LocalHapticFeedback (Compose API)\n" +
                            "  → HapticFeedbackType.LongPress / TextHandleMove\n" +
                            "  → 플랫폼 독립적, Compose 표준 방식\n\n" +
                            "② LocalView + HapticFeedbackConstants (Android API)\n" +
                            "  → 더 다양한 피드백 타입, API 레벨별 차등 적용\n" +
                            "  → view.isHapticFeedbackEnabled 설정 우선 적용",
                    bgColor = Color(0xFFE8F5E9)
                )
            }

            item { HorizontalDivider() }
            item { SectionHeader("1. Compose API (LocalHapticFeedback)") }

            item {
                CodeCard(
                    code = """val haptic = LocalHapticFeedback.current

haptic.performHapticFeedback(HapticFeedbackType.LongPress)
haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)"""
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HapticButton(
                        label = "LongPress",
                        color = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f)
                    ) { triggerCompose(HapticFeedbackType.LongPress, "LongPress") }
                    HapticButton(
                        label = "TextHandleMove",
                        color = Color(0xFF1976D2),
                        modifier = Modifier.weight(1f)
                    ) { triggerCompose(HapticFeedbackType.TextHandleMove, "TextHandleMove") }
                }
            }

            item { HorizontalDivider() }
            item { SectionHeader("2. Android API (HapticFeedbackConstants)") }

            item {
                CodeCard(
                    code = """val view = LocalView.current

// API 24+ (Min SDK)
view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)

// API 30+
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    view.performHapticFeedback(HapticFeedbackConstants.REJECT)
    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_START)
    view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
}

// API 34+
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
    view.performHapticFeedback(HapticFeedbackConstants.TOGGLE_ON)
    view.performHapticFeedback(HapticFeedbackConstants.TOGGLE_OFF)
}"""
                )
            }

            item {
                InfoCard(
                    title = "API 24+ (항상 사용 가능)",
                    description = "",
                    bgColor = Color(0xFFF5F5F5)
                )
            }

            item {
                HapticButtonGrid(
                    items = listOf(
                        "LONG_PRESS" to { triggerView(HapticFeedbackConstants.LONG_PRESS, "LONG_PRESS") },
                        "VIRTUAL_KEY" to { triggerView(HapticFeedbackConstants.VIRTUAL_KEY, "VIRTUAL_KEY") },
                        "KEYBOARD_TAP" to { triggerView(HapticFeedbackConstants.KEYBOARD_TAP, "KEYBOARD_TAP") },
                        "CONTEXT_CLICK" to { triggerView(HapticFeedbackConstants.CONTEXT_CLICK, "CONTEXT_CLICK") },
                        "CLOCK_TICK" to { triggerView(HapticFeedbackConstants.CLOCK_TICK, "CLOCK_TICK") }
                    ),
                    color = Color(0xFF388E3C)
                )
            }

            item {
                InfoCard(
                    title = "API 30+ (Android 11+)",
                    description = "",
                    bgColor = Color(0xFFFFF8E1)
                )
            }

            item {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    HapticButtonGrid(
                        items = listOf(
                            "CONFIRM" to { triggerView(HapticFeedbackConstants.CONFIRM, "CONFIRM") },
                            "REJECT" to { triggerView(HapticFeedbackConstants.REJECT, "REJECT") },
                            "GESTURE_START" to { triggerView(HapticFeedbackConstants.GESTURE_START, "GESTURE_START") },
                            "GESTURE_END" to { triggerView(HapticFeedbackConstants.GESTURE_END, "GESTURE_END") }
                        ),
                        color = Color(0xFFF57C00)
                    )
                } else {
                    InfoCard(
                        title = "현재 기기: API 30 미만",
                        description = "이 기기는 Android 11 미만이므로 해당 피드백을 지원하지 않습니다.",
                        bgColor = Color(0xFFEEEEEE)
                    )
                }
            }

            item {
                InfoCard(
                    title = "API 34+ (Android 14+)",
                    description = "",
                    bgColor = Color(0xFFE3F2FD)
                )
            }

            item {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    HapticButtonGrid(
                        items = listOf(
                            "TOGGLE_ON" to { triggerView(HapticFeedbackConstants.TOGGLE_ON, "TOGGLE_ON") },
                            "TOGGLE_OFF" to { triggerView(HapticFeedbackConstants.TOGGLE_OFF, "TOGGLE_OFF") }
                        ),
                        color = Color(0xFF1565C0)
                    )
                } else {
                    InfoCard(
                        title = "현재 기기: API 34 미만",
                        description = "이 기기는 Android 14 미만이므로 해당 피드백을 지원하지 않습니다.",
                        bgColor = Color(0xFFEEEEEE)
                    )
                }
            }

            item {
                InfoCard(
                    title = "주의사항",
                    description = "• view.isHapticFeedbackEnabled = false 이면 무시됨\n" +
                            "• 기기의 햅틱 진동 설정(시스템 설정)에 따라 동작이 달라짐\n" +
                            "• 에뮬레이터에서는 햅틱이 동작하지 않을 수 있음\n" +
                            "• 플래그 없이 호출 시 기기 설정을 따름\n" +
                            "  FLAG_IGNORE_GLOBAL_SETTING: 시스템 설정 무시\n" +
                            "  FLAG_IGNORE_VIEW_SETTING: View 설정 무시",
                    bgColor = Color(0xFFFCE4EC)
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun HapticButton(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(label, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun HapticButtonGrid(
    items: List<Pair<String, () -> Unit>>,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { (label, onClick) ->
                    HapticButton(
                        label = label,
                        color = color,
                        modifier = Modifier.weight(1f),
                        onClick = onClick
                    )
                }
                // 홀수 개면 빈 Spacer로 정렬
                if (row.size < 2) Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF1976D2),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun InfoCard(title: String, description: String, bgColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (title.isNotEmpty()) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (description.isNotEmpty()) Spacer(modifier = Modifier.height(6.dp))
            }
            if (description.isNotEmpty()) {
                Text(text = description, fontSize = 13.sp, lineHeight = 20.sp)
            }
        }
    }
}

@Composable
private fun CodeCard(code: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = code,
            color = Color(0xFFCFD8DC),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            lineHeight = 17.sp,
            modifier = Modifier.padding(12.dp)
        )
    }
}
