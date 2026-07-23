package com.example.composesample.presentation.example.component.system.security

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

/**
 * Screenshot Detection 예제
 *
 * 화면 캡처를 실시간으로 감지하는 두 가지 방식을 비교합니다.
 * 1) Android 14+ 콜백  — Activity.registerScreenCaptureCallback() (API 34+, 권한 불필요, 정확)
 * 2) 레거시 MediaStore — ContentObserver 로 새 이미지 삽입을 감지해 파일명/경로 휴리스틱으로 판정 (권한 필요, 오탐 가능)
 */
@Composable
fun ScreenshotDetectionExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B2A33))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF2C3E50))
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackEvent) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Screenshot Detection",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ModernCallbackSection()
            LegacyMediaStoreSection()
            ComparisonSection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ============================================================
// 1) Android 14+ 콜백 — Activity.registerScreenCaptureCallback()
// ============================================================

@Composable
private fun ModernCallbackSection() {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

    var isMonitoring by remember { mutableStateOf(false) }
    val events = remember { mutableStateListOf<String>() }
    var callback by remember { mutableStateOf<Activity.ScreenCaptureCallback?>(null) }

    // 화면을 벗어날 때 콜백이 계속 등록된 채로 남지 않도록 정리
    DisposableEffect(Unit) {
        onDispose {
            val act = activity
            val cb = callback
            if (supported && act != null && cb != null) {
                unregisterModernCallback(act, cb)
            }
        }
    }

    SectionCard(title = "① Android 14+ 콜백 (registerScreenCaptureCallback)") {
        BodyText("API 34부터는 Activity.registerScreenCaptureCallback() 하나로 이 화면이 캡처되는 순간 시스템이 직접 콜백을 호출합니다. 별도 권한이 전혀 필요 없고, 콜백이 등록된 Activity 가 화면에 보이는 동안에만 정확히 발화합니다.")
        Spacer(modifier = Modifier.height(8.dp))

        if (!supported) {
            MonoText("이 단말은 API ${Build.VERSION.SDK_INT} 입니다 — 이 콜백은 API 34+ 에서만 사용 가능합니다.")
        } else {
            SmallButton(if (isMonitoring) "모니터링 중지" else "모니터링 시작") {
                val act = activity ?: return@SmallButton
                if (isMonitoring) {
                    callback?.let { unregisterModernCallback(act, it) }
                    callback = null
                    isMonitoring = false
                } else {
                    callback = registerModernCallback(act, ContextCompat.getMainExecutor(context)) {
                        events.add(0, "📸 감지됨 @ ${timestamp()}")
                    }
                    isMonitoring = true
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            MonoText(if (isMonitoring) "상태: 등록됨 — 지금 화면 캡처를 해보세요" else "상태: 미등록")
        }

        Spacer(modifier = Modifier.height(8.dp))
        EventLog(events)
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
private fun registerModernCallback(
    activity: Activity,
    executor: Executor,
    onCaptured: () -> Unit
): Activity.ScreenCaptureCallback {
    val callback = Activity.ScreenCaptureCallback { onCaptured() }
    activity.registerScreenCaptureCallback(executor, callback)
    return callback
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
private fun unregisterModernCallback(activity: Activity, callback: Activity.ScreenCaptureCallback) {
    runCatching { activity.unregisterScreenCaptureCallback(callback) }
}

// ============================================================
// 2) 레거시 MediaStore — ContentObserver 로 새 이미지 삽입 감지
// ============================================================

@Composable
private fun LegacyMediaStoreSection() {
    val context = LocalContext.current
    // targetSDK 33+ 는 READ_MEDIA_IMAGES, 그 미만은 READ_EXTERNAL_STORAGE(maxSdkVersion=32) 를 사용한다.
    val requiredPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, requiredPermission) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    var isMonitoring by remember { mutableStateOf(false) }
    val events = remember { mutableStateListOf<String>() }
    var observer by remember { mutableStateOf<ContentObserver?>(null) }
    // onChange 안에서 MediaStore 쿼리(디스크 I/O)를 수행하므로 메인 스레드를 막지 않도록 전용 백그라운드 스레드에서 관찰한다.
    val observerThread = remember { HandlerThread("ScreenshotObserver").apply { start() } }

    DisposableEffect(Unit) {
        onDispose {
            observer?.let { runCatching { context.contentResolver.unregisterContentObserver(it) } }
            observerThread.quitSafely()
        }
    }

    SectionCard(title = "② 레거시 MediaStore ContentObserver") {
        BodyText("API 34 미만에서는 스크린샷을 직접 감지할 공식 API 가 없어, MediaStore 이미지 테이블을 ContentObserver 로 지켜보다가 새로 삽입된 파일의 이름/경로가 스크린샷 패턴과 일치하는지 휴리스틱으로 판정합니다. 사진 편집 앱 저장본 등에 오탐할 수 있고, 이미지 접근 권한이 필요합니다.")
        Spacer(modifier = Modifier.height(8.dp))

        if (!hasPermission) {
            SmallButton("권한 요청 (${requiredPermission.substringAfterLast('.')})") {
                permissionLauncher.launch(requiredPermission)
            }
        } else {
            SmallButton(if (isMonitoring) "모니터링 중지" else "모니터링 시작") {
                if (isMonitoring) {
                    observer?.let { context.contentResolver.unregisterContentObserver(it) }
                    observer = null
                    isMonitoring = false
                } else {
                    val mainExecutor = ContextCompat.getMainExecutor(context)
                    val obs = object : ContentObserver(Handler(observerThread.looper)) {
                        override fun onChange(selfChange: Boolean, uri: Uri?) {
                            super.onChange(selfChange, uri)
                            // 여기까지는 observerThread(백그라운드)에서 실행 — 쿼리 완료 후 상태 갱신만 메인으로 전달
                            val (name, path) = queryLatestImageInfo(context) ?: return
                            if (isLikelyScreenshot(name, path)) {
                                val message = "📸 감지 @ ${timestamp()} — $name ($path)"
                                mainExecutor.execute { events.add(0, message) }
                            }
                        }
                    }
                    context.contentResolver.registerContentObserver(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        true,
                        obs
                    )
                    observer = obs
                    isMonitoring = true
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            MonoText(if (isMonitoring) "상태: 등록됨 — 지금 화면 캡처를 해보세요" else "상태: 미등록")
        }

        Spacer(modifier = Modifier.height(8.dp))
        EventLog(events)
    }
}

/** 갤러리 최신 이미지의 파일명/경로를 조회한다. RELATIVE_PATH 는 API 29+ 전용 컬럼이라 그 미만은 DATA 로 대체한다. */
private fun queryLatestImageInfo(context: Context): Pair<String, String>? {
    val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.RELATIVE_PATH
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.DATA
    }
    val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME, pathColumn)
    return runCatching {
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT 1"
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val pathIdx = cursor.getColumnIndexOrThrow(pathColumn)
                cursor.getString(nameIdx) to (cursor.getString(pathIdx) ?: "")
            } else {
                null
            }
        }
    }.getOrNull()
}

private fun isLikelyScreenshot(displayName: String, path: String): Boolean {
    return displayName.lowercase(Locale.US).contains("screenshot") ||
        path.lowercase(Locale.US).contains("screenshot")
}

// ============================================================
// 3) 비교 & 실무 가이드
// ============================================================

@Composable
private fun ComparisonSection() {
    SectionCard(title = "③ 비교 & 실무 가이드") {
        ComparisonRow("최소 API", "34 (Android 14) 전용", "24+ (전 버전 지원)")
        ComparisonRow("권한", "불필요", "READ_MEDIA_IMAGES(33+) / READ_EXTERNAL_STORAGE(~32)")
        ComparisonRow("정확도", "정확 — 실제 캡처 시점에만 발화", "휴리스틱 — 파일명/경로 매칭, 오탐 가능")
        ComparisonRow("범위", "콜백이 등록된 Activity 가 보이는 동안", "권한이 있으면 앱이 살아있는 동안 계속 감지 가능")
        Spacer(modifier = Modifier.height(8.dp))
        BodyText("실무 팁: minSdk 가 34 미만이면 SDK_INT 분기로 두 방식을 병행하세요 — 34+ 는 콜백을, 그 미만은 ContentObserver 를 폴백으로 사용합니다. 두 방식 모두 화면 녹화(스크린 레코딩)는 감지하지 못합니다 — 레코딩 감지는 MediaProjection 콜백이 별도로 필요합니다.")
    }
}

@Composable
private fun ComparisonRow(label: String, modern: String, legacy: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "  • Android 14+: $modern",
            color = Color(0xFFB0BEC5),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "  • 레거시: $legacy",
            color = Color(0xFFB0BEC5),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

// ============================================================
// 공통 유틸 / UI 컴포넌트
// ============================================================

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun timestamp(): String = SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(Date())

@Composable
private fun EventLog(events: List<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .background(Color(0xFF17232B), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        if (events.isEmpty()) {
            Text(
                text = "아직 감지된 이벤트가 없습니다.",
                color = Color(0xFF78909C),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        } else {
            Column {
                events.take(10).forEach { msg ->
                    Text(
                        text = msg,
                        color = Color(0xFF81C784),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}

// SectionCard는 system/security 패키지 공용(SecurityUiComponents.kt)으로 이동

@Composable
private fun BodyText(text: String) {
    Text(text = text, color = Color(0xFFCFD8DC), fontSize = 13.sp)
}

@Composable
private fun MonoText(text: String) {
    Text(
        text = text,
        color = Color(0xFFB0BEC5),
        fontSize = 12.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(vertical = 1.dp)
    )
}

@Composable
private fun SmallButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
    ) {
        Text(label, color = Color.White, fontSize = 13.sp)
    }
}
