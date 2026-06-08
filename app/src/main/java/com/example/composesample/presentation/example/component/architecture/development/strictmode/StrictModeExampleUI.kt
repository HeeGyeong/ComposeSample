package com.example.composesample.presentation.example.component.architecture.development.strictmode

import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.os.strictmode.Violation
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

/**
 * StrictMode 위반 감지 — 메인 스레드 I/O(ThreadPolicy)와 Closeable 누수(VmPolicy)를 실시간 포착
 * - StrictMode 는 디버그 빌드에서 "느린/잘못된" 작업(메인 스레드 디스크·네트워크 I/O, 닫지 않은 리소스 누수)을 감지한다.
 * - 위반을 의도적으로 재현하고 penaltyListener 로 수집해 화면에 보여준다(API 28+). 28 미만은 penaltyLog 로 logcat 에만 남는다.
 * - 외부 라이브러리(Strictly) 없이 순수 StrictMode API 로만 구현했다. 참고 자료는 exampleGuide.kt 의 "StrictMode" 섹션 참고.
 */
@Composable
fun StrictModeExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    // penaltyListener 는 API 28(P) 이상에서만 제공된다.
    val listenerSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "StrictMode 위반 감지",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IntroCard(listenerSupported) }
            item {
                // ThreadPolicy — 메인 스레드에서 디스크 읽기/쓰기를 수행해 위반 유발
                ViolationCard(
                    title = "1. ThreadPolicy (메인 스레드 I/O)",
                    description = "detectDiskReads/detectDiskWrites/detectNetwork 정책을 설정하고, 메인 스레드에서 파일 쓰기·읽기를 수행한다. UI 스레드를 막는 디스크 I/O 가 DiskWrite/DiskRead 위반으로 잡힌다.",
                    accent = Color(0xFFE53935),
                    enabled = listenerSupported,
                    run = { onViolation -> detectThreadViolations(context, onViolation) }
                )
            }
            item {
                // VmPolicy — close() 하지 않은 Closeable 을 버려 GC 시점에 누수 위반 유발
                ViolationCard(
                    title = "2. VmPolicy (Closeable 누수)",
                    description = "detectLeakedClosableObjects 정책을 설정하고, FileInputStream 을 close() 없이 버린 뒤 GC 를 유도한다. 파이널라이즈 시점에 닫히지 않은 리소스가 LeakedClosableViolation 으로 잡힌다(타이밍 의존, 1~2회 재시도될 수 있음).",
                    accent = Color(0xFF8E24AA),
                    enabled = listenerSupported,
                    run = { onViolation -> detectVmViolations(context, onViolation) }
                )
            }
            item { GuideCard() }
        }
    }
}

@Composable
private fun ViolationCard(
    title: String,
    description: String,
    accent: Color,
    enabled: Boolean,
    run: suspend ((String) -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()
    // 수집된 위반 메시지 목록 (penaltyListener 콜백에서 추가됨)
    val violations = remember { mutableStateListOf<String>() }
    var running by remember { mutableStateOf(false) }
    var done by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accent, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, fontSize = 12.sp, color = Color(0xFF555555))
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    running = true
                    done = false
                    violations.clear()
                    scope.launch {
                        // penaltyListener 콜백은 메인 스레드(executor)에서 호출되어 목록에 추가된다.
                        run { msg -> violations.add(msg) }
                        running = false
                        done = true
                    }
                },
                enabled = enabled && !running,
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(
                    text = when {
                        !enabled -> "API 28+ 필요"
                        running -> "감지 중..."
                        else -> "위반 유발 & 감지"
                    },
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ResultBox(violations = violations, running = running, done = done, enabled = enabled)
        }
    }
}

@Composable
private fun ResultBox(
    violations: List<String>,
    running: Boolean,
    done: Boolean,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
            .padding(10.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        when {
            !enabled -> {
                Text(
                    text = "이 단말은 API ${Build.VERSION.SDK_INT} 입니다. penaltyListener 로 위반을 화면에 수집하려면 API 28+ 가 필요합니다. (28 미만은 penaltyLog 로 logcat 에만 기록)",
                    color = Color(0xFFFFCC80),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            running -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color(0xFFCFD8DC),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "위반 유발 후 감지 중...",
                        color = Color(0xFFCFD8DC),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            violations.isNotEmpty() -> {
                Column {
                    Text(
                        text = "감지된 위반 ${violations.size}건 ✗",
                        color = Color(0xFFEF9A9A),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    violations.forEach { msg ->
                        Text(
                            text = "• $msg",
                            color = Color(0xFFCFD8DC),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }
                }
            }

            done -> {
                Text(
                    text = "위반이 감지되지 않았습니다. (누수 감지는 GC 타이밍에 의존 → 다시 시도해 보세요)",
                    color = Color(0xFF81C784),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            else -> {
                Text(
                    text = "버튼을 눌러 위반을 유발하고 감지 결과를 확인하세요.",
                    color = Color(0xFF888888),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 위반 감지 로직 — 실제 StrictMode API 사용 (penaltyListener 는 API 28+)
// ──────────────────────────────────────────────────────────────────────────

/**
 * ThreadPolicy 위반 감지: 메인 스레드에서 디스크 I/O 를 수행해 DiskWrite/DiskRead 위반을 유발.
 * 임시로 정책을 설정하고 작업 후 원래 정책으로 복원한다.
 */
@RequiresApi(Build.VERSION_CODES.P)
private suspend fun detectThreadViolations(context: Context, onViolation: (String) -> Unit) {
    val executor = ContextCompat.getMainExecutor(context)
    val original = StrictMode.getThreadPolicy()
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectDiskReads()
            .detectDiskWrites()
            .detectNetwork()
            // penaltyListener: 위반 발생 시 콜백으로 Violation 전달 (executor 스레드에서 호출)
            .penaltyListener(executor) { violation -> onViolation(describe(violation)) }
            .build()
    )
    try {
        // 메인 스레드에서 동기 디스크 쓰기/읽기 → ThreadPolicy 위반
        val file = File(context.cacheDir, "strictmode_thread_demo.txt")
        file.writeText("StrictMode demo")
        file.readText()
    } finally {
        StrictMode.setThreadPolicy(original)
    }
    // 콜백이 메인 큐에 게시되어 목록에 반영될 시간을 잠시 확보
    delay(200)
}

/**
 * VmPolicy 위반 감지: close() 하지 않은 Closeable 을 버리고 GC/파이널라이즈를 유도해 누수 위반을 유발.
 * 누수 감지는 파이널라이저 시점에 보고되므로 정책을 잠시 유지한 뒤 복원한다.
 */
@RequiresApi(Build.VERSION_CODES.P)
private suspend fun detectVmViolations(context: Context, onViolation: (String) -> Unit) {
    val executor = ContextCompat.getMainExecutor(context)
    val original = StrictMode.getVmPolicy()
    StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
            .detectLeakedClosableObjects()
            .penaltyListener(executor) { violation -> onViolation(describe(violation)) }
            .build()
    )
    try {
        // 별도 함수에서 스트림을 열고 close() 없이 반환 → 참조가 사라져 GC 대상이 됨
        leakCloseable(context)
        // GC + 파이널라이즈를 반복 유도해 누수 감지 트리거 (타이밍 의존이라 여러 번 시도)
        repeat(6) {
            withContext(Dispatchers.Default) {
                Runtime.getRuntime().gc()
                System.runFinalization()
            }
            delay(150)
        }
    } finally {
        StrictMode.setVmPolicy(original)
    }
}

/** close() 를 호출하지 않고 FileInputStream 을 버려 누수 상황을 만든다. */
private fun leakCloseable(context: Context) {
    val file = File(context.cacheDir, "strictmode_leak_demo.txt")
    if (!file.exists()) file.writeText("leak")
    @Suppress("unused")
    val stream = FileInputStream(file) // 의도적으로 close() 하지 않음
    // stream 은 함수 종료와 함께 참조가 사라져 GC 대상이 되고, 파이널라이즈 시 누수로 감지됨
}

/** Violation 을 사람이 읽기 쉬운 한 줄로 변환: 위반 종류 + 프로젝트 코드 위치. */
@RequiresApi(Build.VERSION_CODES.P)
private fun describe(violation: Violation): String {
    val kind = violation::class.java.simpleName // 예: DiskWriteViolation, LeakedClosableViolation
    val frame = violation.stackTrace.firstOrNull { it.className.contains("composesample") }
        ?: violation.stackTrace.firstOrNull()
    return if (frame != null) "$kind @ ${frame.fileName}:${frame.lineNumber}" else kind
}

@Composable
private fun IntroCard(listenerSupported: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "StrictMode (정책 위반 감지)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "StrictMode 는 디버그 빌드에서 메인 스레드의 느린 I/O 나 리소스 누수처럼 '잘못된' 동작을 잡아내는 개발 도구다. " +
                        "ThreadPolicy(스레드 단위: 디스크/네트워크 I/O)와 VmPolicy(프로세스 단위: 닫지 않은 Closeable·Activity 누수)로 나뉜다. " +
                        "아래에서 위반을 의도적으로 유발하고 penaltyListener 로 잡아 화면에 표시한다. " +
                        if (listenerSupported) {
                            "이 단말(API ${Build.VERSION.SDK_INT})은 penaltyListener 를 지원한다."
                        } else {
                            "이 단말(API ${Build.VERSION.SDK_INT})은 penaltyListener 미지원 → penaltyLog 로 logcat 에만 남는다."
                        },
                fontSize = 12.sp,
                color = Color(0xFF333333)
            )
        }
    }
}

@Composable
private fun GuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = "실무 적용 가이드", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            listOf(
                "Application.onCreate 에서 BuildConfig.DEBUG 일 때만 설정 (릴리스 빌드 제외)",
                "ThreadPolicy: detectDiskReads/detectDiskWrites/detectNetwork → 메인 스레드 I/O 를 IO 디스패처로 이동",
                "VmPolicy: detectLeakedClosableObjects/detectActivityLeaks/detectLeakedSqlLiteObjects → 리소스 누수 조기 발견",
                "penaltyListener(API 28+): 위반을 콜백으로 받아 로깅/리포팅. 28 미만은 penaltyLog/penaltyDeath",
                "penaltyDeath 는 위반 시 앱을 강제 종료 → CI/QA 단계에서 누수 회귀 차단에 유용",
                "주의: 일부 detectXxx API 는 SDK 버전 분기 필요. 서드파티 라이브러리가 유발하는 위반은 화이트리스트 처리"
            ).forEach {
                Text(
                    text = "• $it",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}
