package com.example.composesample.presentation.example.component.system.platform.powersave

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/**
 * 절전 모드 예제의 '백그라운드 제한 진단' 탭.
 *
 * 푸시·작업이 어떤 기기에선 바로 오고 어떤 기기에선 안 오는 이유를 앱이 스스로 읽는다 — 배터리 세이버 · Doze ·
 * 앱 대기 버킷 · 사용자의 '제한' · 배터리 최적화 예외 · 강제 중지 이력을 한 화면에 모으고 [diagnose] 로 판정한다.
 */
@Composable
internal fun BackgroundRestrictionTab() {
    val viewModel: BackgroundRestrictionViewModel = koinViewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    var openError by remember { mutableStateOf<String?>(null) }

    // 탭에 머무는 동안만 브로드캐스트를 받는다
    DisposableEffect(Unit) {
        viewModel.startWatching()
        onDispose { viewModel.stopWatching() }
    }
    // 처음 보일 때와 설정 화면에서 돌아올 때 다시 읽는다 — 버킷·제한·예외 목록은 바뀌어도 브로드캐스트가 없다
    LifecycleResumeEffect(Unit) {
        viewModel.refresh("화면 표시(진입·복귀)")
        onPauseOrDispose { }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { LayerCard() }
        item { StatusCard(uiState.snapshot, uiState.findings) { viewModel.refresh("다시 읽기 버튼") } }
        item { FindingCard(uiState.findings) }
        item { BucketHistoryCard(uiState.snapshot) }
        item { ProcessHistoryCard(uiState.snapshot) }
        item { TimelineCard(uiState.timeline) }
        item {
            SettingsCard(openError) { intent -> openError = openSettings(context, intent) }
        }
        item { AdbCard() }
        item { ReadingCard() }
    }
}

private fun openSettings(context: Context, intent: Intent): String? = try {
    context.startActivity(intent)
    null
} catch (e: ActivityNotFoundException) {
    "이 기기에는 해당 설정 화면이 없다: ${intent.action}"
}

// ==================== 1. 층 ====================

@Composable
private fun LayerCard() {
    RestrictionCard(title = "1. 앱을 백그라운드에서 막는 층") {
        BodyText(
            "같은 푸시·작업이 어떤 기기에선 바로 오고 어떤 기기에선 안 온다. 서버가 아니라 기기가 앱을 막는 층이 여러 겹이라서다. " +
                "앱이 직접 읽을 수 있는 층은 아래와 같고, 제조사 자체 절전(자동 실행 끔·잠자는 앱 등)은 공개 API 가 없다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        BulletText("배터리 세이버 — 기기 전체 절전. PowerManager.isPowerSaveMode(첫 탭이 다루는 값)")
        BulletText("Doze — 화면이 꺼진 채 시간이 지나면 가벼운 유휴, 기기가 가만히 있으면 깊은 유휴. isDeviceIdleMode · isDeviceLightIdleMode(API 33)")
        BulletText("앱 대기 버킷 — 사용 빈도로 정해지는 ACTIVE~RESTRICTED. 버킷마다 작업 시간·알람 개수·네트워크가 제한된다. UsageStatsManager.getAppStandbyBucket(API 28)")
        BulletText("사용자의 '제한' — 앱 정보 → 배터리에서 사용자가 고른다(Android 15 AOSP: '백그라운드 사용 허용' 끔). ActivityManager.isBackgroundRestricted(API 28)")
        BulletText("배터리 최적화 예외 — 같은 화면의 '제한 없음'. Doze·앱 대기 중에도 네트워크·wake lock 허용. PowerManager.isIgnoringBatteryOptimizations")
        BulletText(
            "강제 중지 — 앱을 stopped 상태로 만든다. 사용자가 다시 열 때까지 알람·작업·브로드캐스트가 오지 않는다. " +
                "ActivityManager.getHistoricalProcessExitReasons(API 30) · ApplicationStartInfo.wasForceStopped(API 35)"
        )
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText("이 탭의 값은 앱이 화면에 떠 있는 순간에 읽는다 — 백그라운드에 있던 동안의 버킷은 4번 이력으로 본다.")
    }
}

// ==================== 2. 지금 값 ====================

@Composable
private fun StatusCard(snapshot: RestrictionSnapshot?, findings: List<Finding>, onRefresh: () -> Unit) {
    RestrictionCard(title = "2. 지금 값") {
        if (snapshot == null) {
            CaptionText("값을 읽는 중…")
            return@RestrictionCard
        }
        val top = findings.firstOrNull()?.severity ?: Severity.CLEAR
        Text(
            text = headline(findings),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = severityColor(top),
            lineHeight = 19.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        LabeledText("읽은 시각", "${formatTime(snapshot.readAt)} · API ${snapshot.sdkInt}")
        LabeledText("배터리 세이버", snapshot.powerSave.toString())
        LabeledText("Doze(깊은)", snapshot.deviceIdle.toString())
        LabeledText("가벼운 Doze", snapshot.lightIdle?.toString() ?: below(33))
        LabeledText("앱 대기 버킷", snapshot.standbyBucket?.let { StandbyBucket.label(it) } ?: below(28))
        LabeledText("백그라운드 사용 제한", snapshot.backgroundRestricted?.toString() ?: below(28))
        LabeledText("배터리 최적화 예외", snapshot.ignoringBatteryOptimizations.toString())
        LabeledText(
            "저전력 대기",
            when (snapshot.lowPowerStandby) {
                null -> below(33)
                false -> "꺼짐"
                true -> "켜짐 · 이 앱 예외 ${snapshot.lowPowerStandbyExempt?.toString() ?: below(34)}"
            }
        )
        LabeledText(
            "직전 종료",
            when {
                snapshot.exits == null -> below(30)
                snapshot.exits.isEmpty() -> "기록 없음"
                else -> snapshot.exits.first().let { "${ExitReason.label(it.reason)} · ${formatTime(it.timestamp)}" }
            }
        )
        LabeledText(
            "이번 시작",
            when {
                snapshot.starts == null -> below(35)
                else -> currentStart(snapshot)?.let {
                    "${StartReason.label(it.reason)} · ${startTypeLabel(it.startType)} · wasForceStopped ${it.wasForceStopped}"
                } ?: "이번 실행의 기록이 목록에 없다(목록 ${snapshot.starts.size}건은 이전 실행 것) — wasForceStopped 를 쓸 수 없음"
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        ActionButton(label = "다시 읽기", onClick = onRefresh)
    }
}

private fun below(api: Int): String = "API $api 미만 — 읽을 수 없음"

/** 시작 기록의 시각은 부팅 기준 단조 시계라, 지금 가동 시간보다 크면 지난 부팅의 기록이다(작다고 이번 부팅이라는 보장은 없다) */
private fun startTimeLabel(record: StartRecord, snapshot: RestrictionSnapshot): String {
    val at = record.launchUptimeMillis ?: return "시각 없음"
    return if (at > snapshot.readUptimeMillis) "지난 부팅 기록" else "부팅 기준 ${at / 1000}초"
}

// ==================== 3. 판정 ====================

@Composable
private fun FindingCard(findings: List<Finding>) {
    RestrictionCard(title = "3. 판정") {
        if (findings.isEmpty()) {
            CaptionText("판정할 값이 없다.")
            return@RestrictionCard
        }
        findings.forEach { finding ->
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = severityColor(finding.severity))) {
                        append("[${finding.severity.label}] ${finding.title}")
                    }
                    append("\n")
                    append(finding.detail)
                },
                fontSize = 12.sp,
                color = Color(0xFF424242),
                lineHeight = 18.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText("제한 내용 출처: 각 API 의 javadoc · developer.android.com 'Power management resource limits' 표(값은 보장이 아닌 근사치, 충전 중이면 대부분 풀린다).")
    }
}

// ==================== 4. 버킷 이력 ====================

@Composable
private fun BucketHistoryCard(snapshot: RestrictionSnapshot?) {
    RestrictionCard(title = "4. 버킷 이력 — 최근 24시간") {
        BodyText(
            "UsageStatsManager.queryEventsForSelf 의 STANDBY_BUCKET_CHANGED 이벤트(권한 불필요, 자기 앱만). " +
                "앱이 백그라운드에 있던 동안 어느 버킷까지 내려갔는지는 이 이력으로만 보인다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        val history = snapshot?.bucketHistory
        when {
            snapshot == null -> CaptionText("값을 읽는 중…")
            snapshot.standbyBucket == null -> CaptionText(below(28))
            history == null -> CaptionText("기기 잠금 해제 전이라 읽을 수 없다(Android 11+ 에서 null).")
            history.isEmpty() -> CaptionText("최근 24시간에 버킷 변경 기록이 없다.")
            else -> history.forEach { LabeledText(formatTime(it.timestamp), StandbyBucket.label(it.bucket)) }
        }
        Spacer(modifier = Modifier.height(4.dp))
        StandbyBucket.entries.forEach { BulletText("${it.name}(${it.code}) — ${it.limits}") }
    }
}

// ==================== 5. 종료·시작 기록 ====================

@Composable
private fun ProcessHistoryCard(snapshot: RestrictionSnapshot?) {
    RestrictionCard(title = "5. 프로세스 종료·시작 기록") {
        BodyText(
            "getHistoricalProcessExitReasons(API 30)는 이 앱 프로세스가 왜 끝났는지를, getHistoricalProcessStartReasons(API 35)는 " +
                "왜 시작됐는지와 강제 중지 뒤 첫 실행인지를 준다. 둘 다 자기 앱 것이라 권한이 없다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        SubTitle("종료(최신 순)")
        when {
            snapshot == null -> CaptionText("값을 읽는 중…")
            snapshot.exits == null -> CaptionText(below(30))
            snapshot.exits.isEmpty() -> CaptionText("기록 없음")
            else -> snapshot.exits.forEach {
                LabeledText(
                    formatTime(it.timestamp),
                    "${ExitReason.label(it.reason)} · ${it.processName} · importance ${it.importance}" +
                        (it.description?.let { d -> " · \"$d\"" } ?: "")
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        SubTitle("시작(최신 순)")
        when {
            snapshot == null -> CaptionText("값을 읽는 중…")
            snapshot.starts == null -> CaptionText(below(35))
            snapshot.starts.isEmpty() -> CaptionText("기록 없음")
            else -> {
                val current = currentStart(snapshot)
                snapshot.starts.forEach {
                    LabeledText(
                        startTimeLabel(it, snapshot) + if (it == current) " ← 이번 실행" else "",
                        "${StartReason.label(it.reason)} · ${startTypeLabel(it.startType)} · wasForceStopped ${it.wasForceStopped}"
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "REASON_USER_STOPPED(11)는 '사용자가 앱을 멈춤'이 아니라 멀티 유저 기기에서 그 기기 사용자가 중지된 경우다 — " +
                "사용자의 강제 중지는 REASON_USER_REQUESTED(10)인데, 같은 값이 최근 앱에서 제거·(Android 14 전) 업데이트에도 쓰인다. " +
                "WorkManager 2.12 도 API 30+ 에서 이 기록에 새 REASON_USER_REQUESTED 가 있으면 강제 중지로 보고 작업을 다시 예약한다(ForceStopRunnable)."
        )
    }
}

// ==================== 6. 변화 기록 ====================

@Composable
private fun TimelineCard(timeline: List<BackgroundRestrictionViewModel.TimelineEntry>) {
    RestrictionCard(title = "6. 변화 기록") {
        BodyText(
            "배터리 세이버·Doze·가벼운 Doze·저전력 대기는 바뀔 때 브로드캐스트가 온다(런타임 등록만). " +
                "버킷·제한·예외 목록은 브로드캐스트가 없어 화면 복귀·버튼으로 다시 읽는다."
        )
        Spacer(modifier = Modifier.height(6.dp))
        if (timeline.isEmpty()) {
            CaptionText("기록 없음")
            return@RestrictionCard
        }
        timeline.forEach { LabeledText("${formatTime(it.at)} ${it.trigger}", it.summary) }
    }
}

// ==================== 7. 설정으로 보내기 ====================

@Composable
private fun SettingsCard(openError: String?, onOpen: (Intent) -> Unit) {
    val context = LocalContext.current
    RestrictionCard(title = "7. 사용자를 설정 화면으로 보내기") {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ActionButton(label = "배터리 최적화 목록") {
                onOpen(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
            ActionButton(label = "앱 정보") {
                onOpen(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)))
            }
            ActionButton(label = "배터리 세이버") {
                onOpen(Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS))
            }
        }
        openError?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = it, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
        }
        Spacer(modifier = Modifier.height(8.dp))
        BulletText(
            "목록 화면(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)은 권한 없이 연다. 예외를 바로 묻는 " +
                "ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS(+ 같은 이름 권한)는 Play 정책상 Doze·앱 대기가 핵심 기능을 깨는 앱(메신저·통화 등)만 쓸 수 있다."
        )
        BulletText("제조사의 '자동 실행'·'절전 앱' 화면은 공개 인텐트가 없다 — 앱 정보 화면으로 보내고 어느 스위치를 바꿀지 글로 안내한다.")
        BulletText("묻는 시점은 푸시에 기대는 기능을 켤 때 한 번 — 이유를 한 화면으로 설명하고 버튼 하나로 보낸다.")
        BulletText("앱을 열 때 놓친 것을 다시 받아 오면 막힌 푸시는 '늦게' 올 뿐 '사라지지' 않는다.")
    }
}

// ==================== 8. adb ====================

@Composable
private fun AdbCard() {
    RestrictionCard(title = "8. adb 로 상태 만들기") {
        CodeBlock(
            "P=com.example.composesample\n" +
                "# 앱 대기 버킷(active|working_set|frequent|rare|restricted)\n" +
                "adb shell am get-standby-bucket \$P\n" +
                "adb shell am set-standby-bucket \$P rare\n\n" +
                "# 사용자의 '제한'과 같은 앱옵스 — 되돌리기는 allow\n" +
                "adb shell cmd appops set \$P RUN_ANY_IN_BACKGROUND ignore\n\n" +
                "# 배터리 최적화 예외 목록 — 되돌리기는 -\$P\n" +
                "adb shell dumpsys deviceidle whitelist +\$P\n\n" +
                "# Doze(기기 전체) — 전원 분리 후, force-idle light 는 가벼운 유휴\n" +
                "adb shell dumpsys battery unplug\n" +
                "adb shell dumpsys deviceidle force-idle\n" +
                "adb shell dumpsys deviceidle unforce\n" +
                "adb shell dumpsys battery reset\n\n" +
                "# 배터리 세이버 — 되돌리기는 set-mode 0\n" +
                "adb shell cmd power set-mode 1\n\n" +
                "# 강제 중지 → 다시 열면 5번에 기록\n" +
                "adb shell am force-stop \$P"
        )
        CaptionText("바꾸기 전에 원래 값을 읽어 두고 그 값으로 되돌린다. force-idle 은 기기 전체(다른 앱 알림까지)에 걸리니 에뮬레이터에서.")
    }
}

// ==================== 9. 읽는 법 ====================

@Composable
private fun ReadingCard() {
    RestrictionCard(title = "9. 읽는 법") {
        BulletText(
            "화면에 떠 있는 동안 읽은 버킷은 거의 늘 ACTIVE 다. 백그라운드에서 RARE 로 내려 둔 앱도 돌아오는 순간 ACTIVE 로 올라가 " +
                "RARE 였던 구간은 4번 이력에만 남는다(앱이 앞에 있는 동안 adb 로 바꾼 값은 그대로 읽힌다)."
        )
        BulletText(
            "같은 REASON_USER_REQUESTED 가 강제 중지(stopped 상태 O)·최근 앱에서 제거와 am kill(stopped 상태 X)·Android 14 전 업데이트에 함께 쓰였다. " +
                "가르는 것은 시스템 설명 문자열뿐 — 이유 코드만 보고 '강제 중지였으니 다시 등록'하면 최근 앱을 밀 때마다 다시 등록한다."
        )
        BulletText(
            "wasForceStopped 는 이번 실행의 시작 기록에서만 뜻이 있다. 목록의 첫 기록을 그대로 쓰면 지난 실행·지난 부팅의 값을 읽을 수 있어, " +
                "이 탭은 fork 시각이 이번 프로세스 시작과 맞는 기록만 쓴다."
        )
        BulletText(
            "설정의 세 선택지는 두 API 로 갈린다 — '백그라운드 사용 허용' 끔 = isBackgroundRestricted true, '제한 없음' = 배터리 최적화 예외(버킷도 숨은 값 5), " +
                "'최적화됨'(기본) = 둘 다 아님."
        )
        BulletText("Doze 는 화면이 켜져 있어도 force-idle 로 들어간다 — 깊은·가벼운 유휴 브로드캐스트가 따로, 바로 온다.")
        BulletText("'제한'은 앱이 풀 수 없다. 판정이 [막힘]이면 이유를 설명하고 앱 정보로 보내는 것밖에 없다.")
        Spacer(modifier = Modifier.height(4.dp))
        CaptionText(
            "실측: Galaxy A72(SM-A725F, API 33) — 사흘 쉰 앱의 버킷 이력 WORKING_SET → RARE → 열자마자 ACTIVE, 종료 설명 업데이트 'installPackageLI'·" +
                "최근 앱 제거 'remove task'·강제 중지 'from pid N'. Pixel Tablet API 35 에뮬레이터 — 업데이트는 PACKAGE_UPDATED(16), 설명 접두어 " +
                "'[FORCE STOP]'·'[REMOVE TASK]'·'[KILL BACKGROUND]', 시작 기록은 부팅 직후(가동 약 10분)엔 이번 실행 것이 없이 지난 부팅(10/23) 기록뿐이었고 " +
                "완료 리스너도 오지 않았다 — 가동 20분 무렵 들어온 이번 실행 기록(강제 중지 뒤 실행 → wasForceStopped true)도 지난 부팅의 기록 뒤 둘째 자리였다, " +
                "'제한 없음' 선택 시 버킷 5. adb 로 만든 제한·예외·Doze·세이버는 모두 판정에 그대로 반영됐다. debug·release 동일."
        )
    }
}

// ==================== 공통 UI ====================

private fun severityColor(severity: Severity): Color = when (severity) {
    Severity.BLOCKED -> Color(0xFFC62828)
    Severity.DEFERRED -> Color(0xFFE65100)
    Severity.HISTORY -> Color(0xFF6A1B9A)
    Severity.NOTE -> Color(0xFF1565C0)
    Severity.CLEAR -> Color(0xFF2E7D32)
}

@Composable
private fun ActionButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
    ) {
        Text(text = label, color = Color.White, fontSize = 13.sp)
    }
}

@Composable
private fun RestrictionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun SubTitle(text: String) {
    Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF37474F))
}

/** 라벨·값을 한 덩어리로 — Row(라벨, 값)는 라벨이 길면 값 칸이 0 폭에 가까워져 글자가 세로로 쌓인다 */
@Composable
private fun LabeledText(label: String, value: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF37474F))) { append(label) }
            append("  ")
            append(value)
        },
        fontSize = 11.sp,
        color = Color(0xFF424242),
        lineHeight = 16.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun BodyText(text: String) {
    Text(text = text, fontSize = 13.sp, color = Color(0xFF424242), lineHeight = 19.sp)
}

@Composable
private fun CaptionText(text: String) {
    Text(text = text, fontSize = 11.sp, color = Color(0xFF757575), lineHeight = 16.sp)
}

@Composable
private fun BulletText(text: String) {
    Text(
        text = "· $text",
        fontSize = 12.sp,
        color = Color(0xFF616161),
        lineHeight = 18.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

@Composable
private fun CodeBlock(text: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFF263238), RoundedCornerShape(6.dp))
            .horizontalScroll(rememberScrollState())
            .padding(10.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFECEFF1),
            lineHeight = 15.sp
        )
    }
}
