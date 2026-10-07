package com.example.composesample.presentation.example.component.system.platform.haptic

import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoundEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoundEffect
import androidx.compose.ui.platform.SoundEffectOnInteraction
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HapticFeedbackExampleUI(onBackEvent: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    // 마지막으로 실행된 햅틱 이름을 잠시 표시
    var lastTriggered by remember { mutableStateOf("") }

    // 3번 섹션 구역별 계수 — LazyColumn 항목 안에 두면 스크롤로 화면 밖에 나갔다 오면 0 으로 돌아간다
    val defaultZone = remember { SoundZoneCounter() }
    val wrappedZone = remember { SoundZoneCounter() }
    val plainGesture = remember { SoundZoneCounter() }
    val manualGesture = remember { SoundZoneCounter() }
    var wrappedZoneSoundEnabled by remember { mutableStateOf(false) }
    val clickSoundEnvironment = rememberClickSoundEnvironment()

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
        MainHeader(
            title = "Haptic Feedback",
            onBackIconClicked = onBackEvent
        )

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
                    title = "Haptic Feedback 두 가지 방법 + 클릭 효과음",
                    description = "① LocalHapticFeedback (Compose API)\n" +
                            "  → HapticFeedbackType.LongPress / TextHandleMove\n" +
                            "  → 플랫폼 독립적, Compose 표준 방식\n\n" +
                            "② LocalView + HapticFeedbackConstants (Android API)\n" +
                            "  → 더 다양한 피드백 타입, API 레벨별 차등 적용\n" +
                            "  → view.isHapticFeedbackEnabled 설정 우선 적용\n\n" +
                            "③ LocalSoundEffect (Compose 1.12, 청각 피드백)\n" +
                            "  → clickable 이 탭마다 클릭음을 자동 요청\n" +
                            "  → SoundEffectOnInteraction 으로 구역별 끄기",
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

            item { HorizontalDivider() }
            item { SectionHeader("3. 상호작용 사운드 (Compose 1.12)") }

            item {
                InfoCard(
                    title = "1.12 부터는 탭하면 클릭음 요청이 나간다",
                    description = "clickable · toggleable · selectable 이 클릭을 처리할 때\n" +
                            "LocalSoundEffect.current.playClickSound() 를 자동 호출\n" +
                            "  → View.playSoundEffect(SoundEffectConstants.CLICK)\n" +
                            "  → 시스템 '터치음'이 켜져 있고 무음·진동 모드가 아닐 때만 실제 재생\n\n" +
                            "위 1·2번의 햅틱 버튼도 Material3 Button(clickable)이라\n" +
                            "진동과 함께 클릭음 요청이 같이 나간다 (촉각 + 청각)",
                    bgColor = Color(0xFFEDE7F6)
                )
            }

            item {
                ClickSoundStatusCard(environment = clickSoundEnvironment)
            }

            item {
                CodeCard(
                    code = """// 1.12: clickable 이 클릭마다 자동 호출
LocalSoundEffect.current.playClickSound()

// 하위 트리의 클릭음만 끄기 — 중첩하면 안쪽 값이 이긴다
SoundEffectOnInteraction(enabled = false) {
    Button(onClick = { }) { Text("조용한 버튼") }
}

// 직접 만든 제스처는 자동 재생이 없다 → 필요하면 직접 호출
val sound = LocalSoundEffect.current
Modifier.pointerInput(sound) {
    detectTapGestures { sound.playClickSound() }
}"""
                )
            }

            item {
                SoundZoneCard(
                    title = "A. 기본값 — 래퍼 없음",
                    description = "Button 과 Switch 를 탭하면 탭마다 요청 1회",
                    counter = defaultZone,
                    bgColor = Color(0xFFE3F2FD)
                ) {
                    CountingSoundZone(defaultZone) { ClickTargets(defaultZone) }
                }
            }

            item {
                SoundZoneCard(
                    title = "B. SoundEffectOnInteraction(enabled = $wrappedZoneSoundEnabled)",
                    description = "false 면 래퍼가 요청을 버려 플랫폼까지 가지 않는다",
                    counter = wrappedZone,
                    bgColor = Color(0xFFFFF3E0),
                    headerAction = {
                        // 이 스위치는 계수 구역 밖이라 B 의 숫자에 들어가지 않는다
                        Switch(
                            checked = wrappedZoneSoundEnabled,
                            onCheckedChange = { wrappedZoneSoundEnabled = it }
                        )
                    }
                ) {
                    CountingSoundZone(wrappedZone) {
                        SoundEffectOnInteraction(enabled = wrappedZoneSoundEnabled) {
                            ClickTargets(wrappedZone)
                        }
                    }
                }
            }

            item {
                GestureSoundCard(
                    plainGesture = plainGesture,
                    manualGesture = manualGesture,
                    onManualTap = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
                )
            }

            item {
                OutlinedButton(
                    onClick = {
                        listOf(defaultZone, wrappedZone, plainGesture, manualGesture).forEach { it.reset() }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("계수 초기화", fontSize = 13.sp)
                }
            }

            item {
                InfoCard(
                    title = "사운드 주의사항",
                    description = "• 숫자는 '플랫폼까지 간 요청' 수 — 터치음이 꺼져 있거나\n" +
                            "  무음·진동 모드면 요청은 나가도 소리가 나지 않는다\n" +
                            "• TalkBack 두 번 탭(시맨틱 onClick) · 키보드 Enter 도\n" +
                            "  같은 클릭 경로라 클릭음을 요청한다\n" +
                            "• onDoubleClick 이 있으면 첫 탭에서 바로 재생한다\n" +
                            "• 키보드·D-pad 포커스 이동음은 View 경로라\n" +
                            "  SoundEffectOnInteraction 으로 끌 수 없다\n" +
                            "• 전역 끄기 플래그(ComposeFoundationFlags 등)는\n" +
                            "  실험·임시 플래그 → 구역 단위로 끄는 쪽이 정석",
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

// ==================== 3. 상호작용 사운드 (Compose 1.12) ====================

/** 구역 하나의 계수 — 탭 수와 플랫폼까지 간 클릭음 요청 수를 나란히 비교한다 */
@Stable
private class SoundZoneCounter {
    var taps by mutableIntStateOf(0)
    var soundRequests by mutableIntStateOf(0)

    fun reset() {
        taps = 0
        soundRequests = 0
    }
}

/**
 * 하위 구역에서 나온 playClickSound 요청을 센 뒤 플랫폼 구현(AndroidSoundEffect)으로 그대로 넘긴다.
 * SoundEffectOnInteraction 바깥에 두면 "끄기 래퍼를 통과해 플랫폼까지 간 요청"만 세게 된다.
 */
private class CountingSoundEffect(
    private val platform: SoundEffect,
    private val onRequest: () -> Unit
) : SoundEffect {
    override fun playClickSound() {
        onRequest()
        platform.playClickSound()
    }
}

@Composable
private fun CountingSoundZone(counter: SoundZoneCounter, content: @Composable () -> Unit) {
    val platform = LocalSoundEffect.current
    val counting = remember(platform, counter) {
        CountingSoundEffect(platform) { counter.soundRequests++ }
    }
    CompositionLocalProvider(LocalSoundEffect provides counting, content = content)
}

/** 클릭 노드를 쓰는 기본 컴포넌트 두 개 — Button(clickable) 과 Switch(toggleable) */
@Composable
private fun ClickTargets(counter: SoundZoneCounter) {
    var checked by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = { counter.taps++ }) {
            Text("Button", fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                counter.taps++
            }
        )
    }
}

@Composable
private fun SoundZoneCard(
    title: String,
    description: String,
    counter: SoundZoneCounter,
    bgColor: Color,
    headerAction: @Composable () -> Unit = {},
    demo: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                headerAction()
            }
            Text(text = description, fontSize = 12.sp, color = Color(0xFF555555))
            demo()
            RequestCountText(taps = counter.taps, requests = counter.soundRequests)
        }
    }
}

@Composable
private fun RequestCountText(taps: Int, requests: Int) {
    Text(
        text = "탭 ${taps}회 · 클릭음 요청 ${requests}회",
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = if (taps > 0 && requests == 0) Color(0xFFC62828) else Color(0xFF1B5E20)
    )
}

/**
 * pointerInput 으로 만든 탭 영역은 클릭 노드가 아니라 자동 재생이 없다.
 * 오른쪽은 같은 제스처에서 햅틱과 클릭음을 직접 함께 낸다(촉각 + 청각).
 */
@Composable
private fun GestureSoundCard(
    plainGesture: SoundZoneCounter,
    manualGesture: SoundZoneCounter,
    onManualTap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "C. 직접 만든 제스처 (pointerInput)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(
                text = "detectTapGestures 는 클릭 노드가 아니라 자동 클릭음이 없다",
                fontSize = 12.sp,
                color = Color(0xFF555555)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CountingSoundZone(plainGesture) {
                    GestureTapBox(
                        label = "탭만 처리",
                        counter = plainGesture,
                        playSoundManually = false,
                        onTap = {},
                        modifier = Modifier.weight(1f)
                    )
                }
                CountingSoundZone(manualGesture) {
                    GestureTapBox(
                        label = "햅틱 + playClickSound()",
                        counter = manualGesture,
                        playSoundManually = true,
                        onTap = onManualTap,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GestureTapBox(
    label: String,
    counter: SoundZoneCounter,
    playSoundManually: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 계수 구역 안에서 읽은 값 — 키로 넘겨야 구역이 바뀌어도 오래된 SoundEffect 를 붙잡지 않는다
    val sound = LocalSoundEffect.current
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .pointerInput(sound, playSoundManually) {
                detectTapGestures {
                    counter.taps++
                    onTap()
                    if (playSoundManually) sound.playClickSound()
                }
            }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.height(36.dp), contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        RequestCountText(taps = counter.taps, requests = counter.soundRequests)
    }
}

@Composable
private fun ClickSoundStatusCard(environment: ClickSoundEnvironment) {
    val context = LocalContext.current
    val audible = environment.touchSoundEnabled && !environment.systemSoundMuted
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (audible) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "이 기기에서 클릭음이 들리나: ${if (audible) "들림" else "안 들림"}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "• 시스템 터치음 설정: ${if (environment.touchSoundEnabled) "켜짐" else "꺼짐"}\n" +
                        "• 시스템 소리: ${if (environment.systemSoundMuted) "음소거 (무음·진동 모드)" else "정상"}",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
            Text(
                text = if (audible) {
                    "아래 구역의 요청이 실제 클릭음으로 재생된다"
                } else {
                    "요청은 나가도 시스템이 재생하지 않는다 — 터치음을 켜고\n" +
                            "소리 모드로 바꾸면 들린다 (돌아오면 자동 갱신)"
                },
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            OutlinedButton(onClick = { openSoundSettings(context) }) {
                Text("소리 설정 열기", fontSize = 12.sp)
            }
        }
    }
}

/**
 * 클릭음이 실제로 들리는지를 정하는 시스템 상태 두 가지를 읽고, 바뀌면 다시 읽는 상태 홀더.
 * ① Settings.System.SOUND_EFFECTS_ENABLED(터치음) ② STREAM_SYSTEM 음소거(무음·진동 모드에서 음소거된다)
 *
 * 관찰자를 컴포저블 안 익명 객체(ContentObserver·BroadcastReceiver 처럼 '클래스'를 상속)로 두고 그 안에서
 * by 위임 지역 변수에 쓰면, 시스템이 콜백을 부르는 순간 debug(HotSwan 디스패치 재작성)에서 합성 접근자
 * NoSuchMethodError 로 크래시한다(release·재작성 끈 debug 정상, 인터페이스 구현 콜백은 정상) — 그래서 클래스로 뺐다.
 */
private class ClickSoundEnvironment(private val context: Context) {
    private val audioManager = context.getSystemService(AudioManager::class.java)

    var touchSoundEnabled by mutableStateOf(readTouchSoundEnabled())
        private set
    var systemSoundMuted by mutableStateOf(readSystemSoundMuted())
        private set

    private val settingObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) = refresh()
    }

    private val ringerModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = refresh()
    }

    fun startObserving() {
        refresh()
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.SOUND_EFFECTS_ENABLED),
            false,
            settingObserver
        )
        ContextCompat.registerReceiver(
            context,
            ringerModeReceiver,
            IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun stopObserving() {
        context.contentResolver.unregisterContentObserver(settingObserver)
        context.unregisterReceiver(ringerModeReceiver)
    }

    private fun refresh() {
        touchSoundEnabled = readTouchSoundEnabled()
        systemSoundMuted = readSystemSoundMuted()
    }

    // 값이 없으면 플랫폼 기본값(켜짐)으로 본다
    private fun readTouchSoundEnabled(): Boolean =
        Settings.System.getInt(context.contentResolver, Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0

    private fun readSystemSoundMuted(): Boolean = audioManager.isStreamMute(AudioManager.STREAM_SYSTEM)
}

@Composable
private fun rememberClickSoundEnvironment(): ClickSoundEnvironment {
    val context = LocalContext.current
    val environment = remember(context) { ClickSoundEnvironment(context) }
    DisposableEffect(environment) {
        environment.startObserving()
        onDispose { environment.stopObserving() }
    }
    return environment
}

private fun openSoundSettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS))
    } catch (e: ActivityNotFoundException) {
        // 소리 설정 화면이 없는 기기 — 전체 설정으로 대체
        context.startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}
