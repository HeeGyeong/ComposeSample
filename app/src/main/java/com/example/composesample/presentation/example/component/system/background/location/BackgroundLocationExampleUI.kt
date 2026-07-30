package com.example.composesample.presentation.example.component.system.background.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.composesample.presentation.MainHeader
import com.example.composesample.util.OnLifecycleEvent

// ==================== 권한 상태 ====================

/**
 * 이 예제가 다루는 4개 권한의 현재 상태.
 *
 * 백그라운드 위치 추적이 어려운 이유의 절반은 "권한이 하나가 아니라는 것"이다.
 * OS 버전마다 필요한 권한과 요청 방법이 다르고, 일부는 런타임 다이얼로그로 받을 수도 없다.
 */
private data class BgPermissionState(
    val fineLocation: Boolean,
    val coarseLocation: Boolean,
    val notification: Boolean,
    val backgroundLocation: Boolean,
) {
    val hasForegroundLocation: Boolean get() = fineLocation || coarseLocation
}

private fun isGranted(context: Context, permission: String): Boolean =
    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

private fun readPermissionState(context: Context) = BgPermissionState(
    fineLocation = isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION),
    coarseLocation = isGranted(context, Manifest.permission.ACCESS_COARSE_LOCATION),
    // POST_NOTIFICATIONS 는 API 33+ 에만 존재한다. 그 미만은 알림에 권한이 필요 없다.
    notification = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            isGranted(context, Manifest.permission.POST_NOTIFICATIONS),
    // ACCESS_BACKGROUND_LOCATION 은 API 29+ 에만 존재한다. 그 미만은 포그라운드 권한만으로 충분하다.
    backgroundLocation = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            isGranted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION),
)

private fun openAppSettings(context: Context) {
    context.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", context.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

// ==================== 화면 ====================

@Composable
fun BackgroundLocationExampleUI(onBackEvent: () -> Unit) {
    val context = LocalContext.current
    var permissions by remember { mutableStateOf(readPermissionState(context)) }

    // 설정 화면에서 권한을 바꾸고 돌아오는 경로가 있으므로 ON_RESUME 마다 다시 읽는다.
    // (백그라운드 위치는 API 30+ 에서 아예 설정 화면으로만 허용할 수 있다)
    OnLifecycleEvent { _, event ->
        if (event == Lifecycle.Event.ON_RESUME) {
            permissions = readPermissionState(context)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Background Location Tracking",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { BgLocationOverviewCard() }
            item {
                BgLocationPermissionCard(
                    permissions = permissions,
                    onPermissionChanged = { permissions = readPermissionState(context) }
                )
            }
            item { BgLocationServiceCard(permissions) }
            item { BgLocationLifecycleCard() }
            item { BgLocationWorkerCard(permissions) }
            item { BgLocationSummaryCard() }
        }
    }
}

// ==================== 1. 개요 ====================

@Composable
private fun BgLocationOverviewCard() {
    BgLocationCard {
        Text(
            text = "앱을 떠나도 끊기지 않는 위치 추적",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "러닝 기록·배달 추적처럼 '화면을 벗어나도 계속 돌아야 하는 작업'은 코루틴이나 " +
                    "ViewModel 로는 유지되지 않습니다. 화면이 사라지면 스코프가 취소되고, " +
                    "프로세스는 언제든 회수될 수 있기 때문입니다.\n\n" +
                    "① 지속 실행: 사용자에게 보이는 알림을 띄우고 프로세스를 살려 두는 " +
                    "Foreground Service 가 유일한 정식 수단입니다.\n" +
                    "② 권한: 포그라운드 위치와 백그라운드 위치가 별개 권한이고, Android 11+ 는 " +
                    "백그라운드 위치를 런타임 다이얼로그로 아예 받을 수 없습니다.\n" +
                    "③ 서비스 타입: Android 10+ 는 매니페스트에 foregroundServiceType 을, " +
                    "Android 14+ 는 그에 대응하는 런타임 권한까지 갖춰야 서비스가 시작됩니다.\n" +
                    "④ 경계: '가끔 한 번'이면 서비스가 아니라 WorkManager 가 맞습니다. " +
                    "아래에서 두 방식을 같은 화면에서 비교합니다.",
            fontSize = 13.sp,
            color = Color(0xFF424242),
            lineHeight = 19.sp
        )
    }
}

// ==================== 2. 권한 단계 (실동작) ====================

@Composable
private fun BgLocationPermissionCard(
    permissions: BgPermissionState,
    onPermissionChanged: () -> Unit,
) {
    val context = LocalContext.current

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { onPermissionChanged() }

    val singleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { onPermissionChanged() }

    BgLocationCard {
        Text(
            text = "1. 권한은 한 번에 받을 수 없다",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "포그라운드 위치 → 알림 → 백그라운드 위치 순서로만 받을 수 있습니다. " +
                    "순서를 건너뛰면 시스템이 다이얼로그를 띄우지 않고 즉시 거부합니다.",
            fontSize = 12.sp,
            color = Color(0xFF757575),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        BgLocationPermissionRow(
            step = "①",
            title = "위치 (FINE / COARSE)",
            granted = permissions.hasForegroundLocation,
            note = "앱이 보이는 동안의 위치. 이게 없으면 Android 14+ 에서 서비스 시작 자체가 실패합니다.",
            buttonLabel = "요청",
            enabled = !permissions.hasForegroundLocation,
            onClick = {
                locationLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        BgLocationPermissionRow(
            step = "②",
            title = "알림 (POST_NOTIFICATIONS)",
            granted = permissions.notification,
            note = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                "없어도 서비스는 돕니다. 다만 FGS 알림이 화면에 보이지 않아 사용자가 추적 사실을 알 수 없습니다."
            } else {
                "API 33 미만이라 별도 권한이 필요 없습니다."
            },
            buttonLabel = "요청",
            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !permissions.notification,
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    singleLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        BgLocationPermissionRow(
            step = "③",
            title = "백그라운드 위치 (ACCESS_BACKGROUND_LOCATION)",
            granted = permissions.backgroundLocation,
            note = when {
                Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ->
                    "API 29 미만이라 백그라운드 위치라는 개념 자체가 없습니다."

                Build.VERSION.SDK_INT == Build.VERSION_CODES.Q ->
                    "Android 10 은 런타임 다이얼로그로 요청할 수 있습니다."

                else ->
                    "Android 11+ 는 런타임 다이얼로그가 뜨지 않습니다. 설정 > 권한 > 위치에서 " +
                            "'항상 허용'을 직접 선택해야 합니다."
            },
            buttonLabel = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) "설정 열기" else "요청",
            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    permissions.hasForegroundLocation &&
                    !permissions.backgroundLocation,
            onClick = {
                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
                    singleLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    openAppSettings(context)
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))
        BgLocationInfoBox(
            text = "Foreground Service 를 쓰면 ③ 없이도 추적이 됩니다. ③ 이 필요한 경우는 " +
                    "서비스조차 없이 완전히 백그라운드에서(예: 지오펜스 콜백, WorkManager 실행 중) " +
                    "위치를 읽어야 할 때입니다."
        )
    }
}

@Composable
private fun BgLocationPermissionRow(
    step: String,
    title: String,
    granted: Boolean,
    note: String,
    buttonLabel: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$step $title",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121),
                modifier = Modifier.weight(1f)
            )
            BgLocationBadge(
                label = if (granted) "허용" else "거부",
                color = if (granted) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = note,
            fontSize = 11.sp,
            color = Color(0xFF757575),
            lineHeight = 15.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Button(
            onClick = onClick,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text(text = buttonLabel, fontSize = 12.sp)
        }
    }
}

// ==================== 3. Foreground Service (핵심 실동작) ====================

@Composable
private fun BgLocationServiceCard(permissions: BgPermissionState) {
    val context = LocalContext.current
    val state by LocationTrackingService.state.collectAsStateWithLifecycle()
    val events by LocationTrackingService.events.collectAsStateWithLifecycle()

    BgLocationCard {
        Text(
            text = "2. Foreground Service 로 지속 추적",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "시작한 뒤 홈 버튼으로 앱을 내려 보세요. 알림이 남아 있고 경과 시간이 계속 " +
                    "증가하면 프로세스가 살아 있는 것입니다. 다시 들어오면 그동안 쌓인 값이 그대로 보입니다.",
            fontSize = 12.sp,
            color = Color(0xFF757575),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { LocationTrackingService.start(context) },
                enabled = permissions.hasForegroundLocation && !state.isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text(text = "추적 시작", fontSize = 12.sp)
            }
            Button(
                onClick = { LocationTrackingService.stop(context) },
                enabled = state.isRunning,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
            ) {
                Text(text = "중지", fontSize = 12.sp)
            }
            Button(
                onClick = { LocationTrackingService.clearEvents() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF616161))
            ) {
                Text(text = "로그 지우기", fontSize = 12.sp)
            }
        }

        if (!permissions.hasForegroundLocation) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "위치 권한이 없어 시작 버튼이 비활성화되어 있습니다. 위 ① 을 먼저 허용하세요.",
                fontSize = 11.sp,
                color = Color(0xFFC62828)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        BgLocationStatRow("서비스 상태", if (state.isRunning) "실행 중" else "정지")
        BgLocationStatRow("경과 시간", formatElapsed(state.elapsedSeconds))
        BgLocationStatRow("위치 수신", "${state.updateCount}회")
        BgLocationStatRow(
            "알림 갱신",
            "${state.notifyCount}회 (${LocationTrackingService.NOTIFY_INTERVAL_SECONDS}초 주기)"
        )
        // state 는 위임 프로퍼티라 스마트 캐스트가 되지 않으므로 지역 변수로 받아 둔다
        val latitude = state.latitude
        val longitude = state.longitude
        BgLocationStatRow(
            "좌표",
            if (latitude != null && longitude != null) {
                "${formatCoordinate(latitude)}, ${formatCoordinate(longitude)}" +
                        if (state.isLastKnown) " (마지막 알려진 위치)" else ""
            } else {
                "수신 대기 중"
            }
        )
        BgLocationStatRow(
            "provider / 정확도",
            listOfNotNull(
                state.provider,
                state.accuracy?.let { "±${it.toInt()}m" }
            ).joinToString(" · ").ifEmpty { "-" }
        )

        Spacer(modifier = Modifier.height(12.dp))
        BgLocationEventLog(title = "서비스 이벤트", events = events)

        Spacer(modifier = Modifier.height(10.dp))
        BgLocationInfoBox(
            text = "에뮬레이터에서는 좌표가 오래 비어 있을 수 있습니다. Extended Controls > Location 에서 " +
                    "좌표를 보내면 '위치 수신' 카운터가 올라갑니다. 좌표가 없어도 경과 시간과 알림 갱신은 " +
                    "그대로 동작하므로 서비스 수명주기 자체는 확인할 수 있습니다."
        )
    }
}

// ==================== 4. 서비스 수명주기 ====================

@Composable
private fun BgLocationLifecycleCard() {
    BgLocationCard {
        Text(
            text = "3. Service 수명주기에서 지켜야 할 것",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "• 5초 룰: startForegroundService() 로 시작했다면 5초 안에 startForeground() 를 " +
                    "불러야 합니다. 놓치면 ForegroundServiceDidNotStartInTimeException 으로 프로세스가 죽습니다. " +
                    "그래서 이 예제는 권한 검사보다 startForeground 를 먼저 호출합니다 — 권한 검사를 앞에 두면 " +
                    "권한이 회수된 순간 startForeground 없이 return 하게 되어 그 자체가 계약 위반이 됩니다.\n" +
                    "• 중지 경로는 startForegroundService 로 깨우면 안 됩니다. 중지 분기는 startForeground 를 " +
                    "부르지 않으므로 같은 계약에 걸립니다. 이미 FGS 가 떠 있는 앱은 백그라운드에서도 " +
                    "startService 가 허용되므로 중지에는 startService 를 씁니다.\n" +
                    "• START_STICKY: 시스템이 서비스를 되살릴 때 intent 가 null 로 들어옵니다. " +
                    "이 분기를 처리하지 않으면 재생성된 서비스가 아무 일도 하지 않고 남아 있게 됩니다.\n" +
                    "• Android 12+: 앱이 백그라운드일 때 서비스를 시작하면 " +
                    "ForegroundServiceStartNotAllowedException 이 발생합니다.\n" +
                    "• Android 14+: 매니페스트의 foregroundServiceType 에 대응하는 런타임 권한이 없으면 " +
                    "startForeground 가 SecurityException 을 던집니다.\n" +
                    "• 정리: onDestroy 에서 removeUpdates 와 코루틴 스코프 취소를 반드시 함께 합니다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        BgLocationCodeBlock(
            code = """
                <!-- AndroidManifest.xml -->
                <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
                <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

                <service
                    android:name=".LocationTrackingService"
                    android:exported="false"
                    android:foregroundServiceType="location" />

                // Service.onStartCommand
                if (!hasLocationPermission()) { stopSelf(); return START_NOT_STICKY }
                ServiceCompat.startForeground(
                    this, NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION  // API 29+
                )
            """.trimIndent()
        )
    }
}

// ==================== 5. WorkManager 대조 (실동작) ====================

@Composable
private fun BgLocationWorkerCard(permissions: BgPermissionState) {
    val context = LocalContext.current
    val snapshot by LocationSnapshotWorker.lastSnapshot.collectAsStateWithLifecycle()
    val count by LocationSnapshotWorker.snapshotCount.collectAsStateWithLifecycle()
    val events by LocationSnapshotWorker.events.collectAsStateWithLifecycle()

    BgLocationCard {
        Text(
            text = "4. 같은 일을 WorkManager 로 하면",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Worker 는 위치를 '구독'하지 않고 한 번 찍고 끝납니다. 즉시 실행은 눈으로 확인되지만, " +
                    "주기 실행은 최소 ${LocationSnapshotWorker.MIN_PERIODIC_MINUTES}분이고 그마저 시스템이 " +
                    "배터리 상태에 따라 미룹니다.",
            fontSize = 12.sp,
            color = Color(0xFF757575),
            lineHeight = 16.sp
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { LocationSnapshotWorker.enqueueOnce(context) },
                enabled = permissions.hasForegroundLocation,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Text(text = "지금 한 번", fontSize = 12.sp)
            }
            Button(
                onClick = { LocationSnapshotWorker.enqueuePeriodic(context) },
                enabled = permissions.hasForegroundLocation,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00838F))
            ) {
                Text(text = "주기 예약", fontSize = 12.sp)
            }
            Button(
                onClick = { LocationSnapshotWorker.cancelPeriodic(context) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF616161))
            ) {
                Text(text = "예약 취소", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        BgLocationStatRow("실행 횟수", "${count}회")
        BgLocationStatRow(
            "마지막 스냅샷",
            snapshot?.let {
                "${formatCoordinate(it.latitude)}, ${formatCoordinate(it.longitude)}"
            } ?: "없음"
        )
        BgLocationStatRow(
            "찍은 시각 / 트리거",
            snapshot?.let { "${it.takenAt} · ${it.trigger} · ${it.provider}" } ?: "-"
        )

        Spacer(modifier = Modifier.height(12.dp))
        BgLocationEventLog(title = "Worker 이벤트", events = events)

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "왜 Worker 를 포그라운드로 승격시키지 않았나\n" +
                    "expedited work 는 API 30 이하에서 WorkManager 자신의 SystemForegroundService 로 " +
                    "승격되는데, work-runtime 2.9.1 의 매니페스트는 그 서비스에 foregroundServiceType 을 " +
                    "선언하지 않습니다. 위치 타입으로 승격하려면 라이브러리의 서비스 선언을 앱 매니페스트에서 " +
                    "override 해야 합니다. 그런 우회가 필요하다는 것 자체가 '지속 추적은 직접 만든 Service 의 " +
                    "몫'이라는 신호입니다.",
            fontSize = 11.sp,
            color = Color(0xFF424242),
            lineHeight = 16.sp
        )
    }
}

// ==================== 6. 정리 ====================

@Composable
private fun BgLocationSummaryCard() {
    BgLocationCard {
        Text(
            text = "정리 — 무엇을 언제 쓰나",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF388E3C)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "• 끊기면 안 되고, 사용자가 인지해야 하고, 초 단위 갱신이 필요하다 → Foreground Service.\n" +
                    "• 늦어도 되고, 실행 조건(충전 중·배터리 여유)이 있고, 앱이 죽어도 다시 실행돼야 한다 → WorkManager.\n" +
                    "• 둘 다 아니고 화면이 떠 있는 동안만 필요하다 → 그냥 화면 스코프의 코루틴이면 충분합니다.\n" +
                    "• 권한은 순서가 있는 절차입니다. 포그라운드 위치 없이 백그라운드 위치를 요청하면 " +
                    "다이얼로그조차 뜨지 않습니다.\n" +
                    "• 알림 권한과 서비스 실행 가능 여부는 별개입니다. 알림이 안 보여도 서비스는 돌고, " +
                    "그 상태가 오히려 사용자에게 더 나쁩니다.",
            fontSize = 12.sp,
            color = Color(0xFF424242),
            lineHeight = 18.sp
        )
    }
}

// ==================== 공용 UI 조각 ====================

@Composable
private fun BgLocationCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
private fun BgLocationBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BgLocationStatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF757575),
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            color = Color(0xFF212121),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun BgLocationEventLog(title: String, events: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            fontSize = 11.sp,
            color = Color(0xFF80CBC4),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (events.isEmpty()) {
            Text(
                text = "아직 기록이 없습니다.",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF90A4AE)
            )
        } else {
            events.forEach { line ->
                Text(
                    text = line,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFFE0E0E0),
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun BgLocationInfoBox(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Color(0xFF1565C0),
        lineHeight = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFE3F2FD))
            .padding(10.dp)
    )
}

@Composable
private fun BgLocationCodeBlock(code: String) {
    Text(
        text = code,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFFE0E0E0),
        lineHeight = 17.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF263238))
            .padding(12.dp)
    )
}
