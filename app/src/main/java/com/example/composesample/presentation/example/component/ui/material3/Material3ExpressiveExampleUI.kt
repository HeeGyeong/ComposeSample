package com.example.composesample.presentation.example.component.ui.material3

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.LocalTextFieldContentObserverRegistrationExecutor
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.composesample.presentation.MainHeader
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@Composable
fun Material3ExpressiveExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Material 3 Expressive (1.4.0)",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { OverviewCard() }
            item { SecureTextFieldDemoCard() }
            item { OutlinedSecureTextFieldDemoCard() }
            item { ShowPasswordSettingCard() }
            item { ObfuscationModeComparisonCard() }
            item { CodeExampleCard() }
            item { SummaryCard() }
        }
    }
}

@Composable
private fun OverviewCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Material3 1.4.0 신규 컴포넌트",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Material Design 3의 Expressive 업데이트에서 비밀번호 입력을 위한 " +
                        "SecureTextField가 stable API로 추가되었습니다. " +
                        "TextFieldState 기반으로 동작하며 난독화 모드를 고를 수 있습니다(foundation 1.12 에서 System 추가).",
                fontSize = 13.sp,
                color = Color(0xFF424242),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            val components = listOf(
                Triple("SecureTextField", "Filled 스타일 비밀번호 필드", "stable"),
                Triple("OutlinedSecureTextField", "Outlined 스타일 비밀번호 필드", "stable"),
                Triple("FloatingToolbar", "플로팅 액션 바", "alpha (1.5.0+)"),
                Triple("VerticalDragHandle", "BottomSheet 드래그 핸들", "stable")
            )
            components.forEach { (name, desc, status) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .background(Color(0xFFF3EDF7), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6750A4),
                        modifier = Modifier.weight(0.35f)
                    )
                    Text(
                        text = desc,
                        fontSize = 10.sp,
                        color = Color(0xFF49454F),
                        modifier = Modifier.weight(0.45f)
                    )
                    Text(
                        text = status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (status == "stable") Color(0xFF388E3C) else Color(0xFFE65100),
                        modifier = Modifier.weight(0.2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SecureTextFieldDemoCard() {
    // foundation 1.12 부터 사용자 설정을 따르는 모드는 System 이다(RevealLastTyped 는 설정과 무관하게 노출)
    var selectedMode by remember { mutableIntStateOf(2) }
    val state = rememberTextFieldState()

    val currentMode = obfuscationModeOf(selectedMode)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SecureTextField (Filled)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "비밀번호 입력 전용 Filled 스타일. 난독화 모드를 선택하고 직접 입력해보세요.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 모드 선택 탭
            ObfuscationModeSelector(selectedMode = selectedMode, onModeSelected = { selectedMode = it })

            Spacer(modifier = Modifier.height(12.dp))

            SecureTextField(
                state = state,
                label = { Text("비밀번호") },
                placeholder = { Text("입력해보세요") },
                textObfuscationMode = currentMode,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            ObfuscationModeDescription(selectedMode)
        }
    }
}

@Composable
private fun OutlinedSecureTextFieldDemoCard() {
    var selectedMode by remember { mutableIntStateOf(0) }
    val state = rememberTextFieldState()

    val currentMode = obfuscationModeOf(selectedMode)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "OutlinedSecureTextField (Outlined)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Outlined 스타일 비밀번호 필드. 같은 TextObfuscationMode를 사용합니다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            ObfuscationModeSelector(selectedMode = selectedMode, onModeSelected = { selectedMode = it })

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedSecureTextField(
                state = state,
                label = { Text("비밀번호") },
                placeholder = { Text("입력해보세요") },
                textObfuscationMode = currentMode,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            ObfuscationModeDescription(selectedMode)
        }
    }
}

private val obfuscationModeLabels = listOf("Hidden", "RevealLastTyped", "System", "Visible")

private fun obfuscationModeOf(index: Int): TextObfuscationMode = when (index) {
    0 -> TextObfuscationMode.Hidden
    1 -> TextObfuscationMode.RevealLastTyped
    2 -> TextObfuscationMode.System
    else -> TextObfuscationMode.Visible
}

@Composable
private fun ObfuscationModeSelector(selectedMode: Int, onModeSelected: (Int) -> Unit) {
    // 4개 칩이 좁은 화면에서 잘리지 않도록 가로 스크롤
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        obfuscationModeLabels.forEachIndexed { idx, label ->
            Box(
                modifier = Modifier
                    .background(
                        if (selectedMode == idx) Color(0xFF6750A4) else Color(0xFFE8DEF8),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onModeSelected(idx) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (selectedMode == idx) Color.White else Color(0xFF6750A4),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ObfuscationModeDescription(selectedMode: Int) {
    val modeDesc = when (selectedMode) {
        0 -> "Hidden — 모든 문자를 즉시 '•'로 표시. 가장 보안 강도가 높은 모드"
        1 -> "RevealLastTyped — 마지막 입력 문자를 잠깐 표시. foundation 1.12 부터 시스템 '비밀번호 표시' 설정과 무관하게 항상 표시"
        2 -> "System — 시스템 '비밀번호 표시'가 켜져 있으면 RevealLastTyped, 꺼져 있으면 Hidden 처럼 동작(1.12 신규)"
        else -> "Visible — 전체 텍스트를 그대로 표시. 비밀번호 보기 토글 용도"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3EDF7), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(text = modeDesc, fontSize = 11.sp, color = Color(0xFF49454F), lineHeight = 16.sp)
    }
}

@Composable
private fun ObfuscationModeComparisonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TextObfuscationMode 비교",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(12.dp))

            val modes = listOf(
                Triple("Hidden", "\"password\" → \"••••••••\"", "최고 보안. 모든 문자 즉시 난독화"),
                Triple("RevealLastTyped", "\"passwor\" + \"d\" → \"•••••••d\" → \"••••••••\"", "마지막 입력 확인. 1.12 부터 시스템 설정을 무시"),
                Triple("System", "설정 켜짐: RevealLastTyped / 꺼짐: Hidden", "사용자 설정을 따른다. 1.12 신규 · BasicSecureTextField 기본값"),
                Triple("Visible", "\"password\" → \"password\"", "비밀번호 보기 토글. 난독화 없음")
            )
            modes.forEach { (mode, example, desc) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = mode,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6750A4)
                    )
                    Text(
                        text = example,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF212121)
                    )
                    Text(text = desc, fontSize = 11.sp, color = Color(0xFF757575))
                }
            }
        }
    }
}

@Composable
private fun CodeExampleCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "코드 예제",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFF6750A4), RoundedCornerShape(8.dp))
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "// TextFieldState 기반 (onValueChange 아님)\n" +
                            "val state = rememberTextFieldState()\n\n" +
                            "SecureTextField(\n" +
                            "    state = state,\n" +
                            "    label = { Text(\"비밀번호\") },\n" +
                            "    // M3 1.4.0 기본값은 RevealLastTyped — 사용자 설정을 따르려면 System 을 명시\n" +
                            "    textObfuscationMode =\n" +
                            "        TextObfuscationMode.System,\n" +
                            "    // 커스텀 난독화 문자 (기본값 '•')\n" +
                            "    obfuscationCharacter = '●'\n" +
                            ")\n\n" +
                            "// Outlined 스타일\n" +
                            "OutlinedSecureTextField(\n" +
                            "    state = state,\n" +
                            "    label = { Text(\"비밀번호\") },\n" +
                            "    textObfuscationMode =\n" +
                            "        TextObfuscationMode.Hidden\n" +
                            ")",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF212121),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun SummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "핵심 정리",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val bullets = listOf(
                "SecureTextField는 TextFieldState 기반 — 기존 onValueChange가 아닌 상태 객체로 관리",
                "사용자의 '비밀번호 표시' 설정을 따르려면 TextObfuscationMode.System(foundation 1.12) — RevealLastTyped 는 1.12 부터 설정과 무관하게 노출",
                "M3 1.4.0 SecureTextField 의 기본값은 여전히 RevealLastTyped 라 foundation 1.12 와 함께 쓰면 설정을 꺼도 마지막 글자가 보인다",
                "obfuscationCharacter 파라미터로 난독화 문자를 커스터마이징 가능 (기본 '•')",
                "Filled(SecureTextField)와 Outlined(OutlinedSecureTextField) 두 가지 스타일 제공",
                "FloatingToolbar, HorizontalCenteredHeroCarousel 등은 alpha(1.5.0+)에서 사용 가능"
            )
            bullets.forEach { bullet ->
                Row(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(text = "• ", fontSize = 13.sp, color = Color(0xFF6750A4))
                    Text(text = bullet, fontSize = 12.sp, color = Color(0xFF424242), lineHeight = 17.sp)
                }
            }
        }
    }
}

// ==================== 시스템 '비밀번호 표시' 설정 (foundation 1.12) ====================

@Composable
private fun ShowPasswordSettingCard() {
    val context = LocalContext.current
    val setting = remember(context) { ShowPasswordSetting(context) }
    DisposableEffect(setting) {
        setting.startObserving()
        onDispose { setting.stopObserving() }
    }
    val revealState = rememberTextFieldState()
    val systemState = rememberTextFieldState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "시스템 '비밀번호 표시' 설정과 System 모드 (foundation 1.12)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "같은 글자를 두 필드에 입력해 보라. 시스템 설정을 끄면 System 필드만 마지막 글자를 숨긴다.",
                fontSize = 12.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (setting.showPassword) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = "이 기기의 비밀번호 표시: ${if (setting.showPassword) "켜짐" else "꺼짐"}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Settings.System.TEXT_SHOW_PASSWORD = ${setting.rawValue ?: "값 없음 → foundation 은 켜짐으로 본다"}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF49454F)
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(onClick = { openSecuritySettings(context) }) {
                    Text("보안 설정 열기", fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            SecureTextField(
                state = revealState,
                label = { Text("RevealLastTyped") },
                textObfuscationMode = TextObfuscationMode.RevealLastTyped,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            SecureTextField(
                state = systemState,
                label = { Text("System") },
                textObfuscationMode = TextObfuscationMode.System,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
            SettingTableRow("구분", "foundation 1.11.4 / 1.12.1", isHeader = true)
            SettingTableRow("RevealLastTyped", "설정을 따름(꺼지면 Hidden) / 설정 무시 — 항상 마지막 글자 표시")
            SettingTableRow("System", "없음 / 설정을 따름 — API 37+ 는 터치·물리 키보드 따로(ShowSecretsSetting)")
            SettingTableRow("Basic 기본값", "RevealLastTyped / System")
            SettingTableRow("M3 기본값", "SecureTextField 1.4.0 은 RevealLastTyped 그대로 → 1.12 와 쓰면 항상 노출")

            Spacer(modifier = Modifier.height(12.dp))
            ObserverExecutorDemo()

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "설정을 읽는 PasswordVisibilitySetting·SplitVisibilitySettings 는 internal 이라 앱이 정책을 주입할 수 없다. " +
                        "앱이 고를 수 있는 것은 모드와, 관찰자를 등록할 스레드뿐이다.",
                fontSize = 11.sp,
                color = Color(0xFF757575),
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * 비밀번호 필드는 표시 설정을 ContentObserver 로 관찰하는데, 등록·해제가 IPC 라 드물게 메인 스레드를 잡는다.
 * LocalTextFieldContentObserverRegistrationExecutor 로 Executor 를 주면 그 등록·해제를 그쪽에서 한다(1.12 신규).
 */
@Composable
private fun ObserverExecutorDemo() {
    val executor = remember { RecordingExecutor() }
    DisposableEffect(executor) {
        onDispose { executor.shutdown() }
    }
    var fieldShown by remember { mutableStateOf(true) }
    val state = rememberTextFieldState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3EDF7), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "관찰자 등록 스레드 — LocalTextFieldContentObserverRegistrationExecutor",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6750A4)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "필드 보이기 (넣었다 빼면 등록·해제)", fontSize = 11.sp, modifier = Modifier.weight(1f))
            Switch(checked = fieldShown, onCheckedChange = { fieldShown = it })
        }
        if (fieldShown) {
            CompositionLocalProvider(LocalTextFieldContentObserverRegistrationExecutor provides executor) {
                SecureTextField(
                    state = state,
                    label = { Text("등록을 백그라운드로") },
                    textObfuscationMode = TextObfuscationMode.System,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Executor 실행 ${executor.runs}회 · 마지막 스레드 ${executor.lastThread}",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF212121)
        )
    }
}

@Composable
private fun SettingTableRow(first: String, second: String, isHeader: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp)
    ) {
        Text(
            text = first,
            modifier = Modifier.weight(0.32f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Medium,
            color = Color(0xFF424242)
        )
        Text(
            text = second,
            modifier = Modifier.weight(0.68f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF616161),
            lineHeight = 16.sp
        )
    }
}

/**
 * Settings.System.TEXT_SHOW_PASSWORD 를 읽고 바뀌면 다시 읽는 상태 홀더.
 * 관찰자를 컴포저블 안 익명 객체로 두면 debug(HotSwan)에서 시스템 콜백 시 NoSuchMethodError 가 나므로 클래스에 둔다.
 */
private class ShowPasswordSetting(private val context: Context) {
    /** 설정값 원문(없으면 null) */
    var rawValue by mutableStateOf<Int?>(null)
        private set

    /** foundation 과 같은 해석 — 값이 없거나 읽기에 실패하면 켜짐으로 본다 */
    val showPassword: Boolean
        get() = rawValue?.let { it > 0 } ?: true

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) = refresh()
    }

    fun startObserving() {
        refresh()
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.TEXT_SHOW_PASSWORD),
            false,
            observer
        )
    }

    fun stopObserving() {
        context.contentResolver.unregisterContentObserver(observer)
    }

    private fun refresh() {
        rawValue = try {
            Settings.System.getInt(context.contentResolver, Settings.System.TEXT_SHOW_PASSWORD)
        } catch (e: Settings.SettingNotFoundException) {
            null
        }
    }
}

/** 받은 작업을 전용 스레드에서 실행하고, 몇 번·어느 스레드에서 돌았는지를 메인 스레드에서 상태로 갱신한다 */
private class RecordingExecutor : Executor {
    private val worker = Executors.newSingleThreadExecutor { runnable -> Thread(runnable, "pwd-observer-io") }
    private val mainHandler = Handler(Looper.getMainLooper())

    var runs by mutableIntStateOf(0)
        private set
    var lastThread by mutableStateOf("-")
        private set

    override fun execute(command: Runnable) {
        worker.execute {
            command.run()
            val threadName = Thread.currentThread().name
            mainHandler.post {
                runs++
                lastThread = threadName
            }
        }
    }

    // 종료 뒤 들어온 작업은 RejectedExecutionException — foundation 이 받아서 호출 스레드에서 바로 실행한다
    fun shutdown() = worker.shutdown()
}

private fun openSecuritySettings(context: Context) {
    try {
        context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}
