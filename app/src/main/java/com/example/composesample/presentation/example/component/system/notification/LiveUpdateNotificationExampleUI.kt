package com.example.composesample.presentation.example.component.system.notification

import android.Manifest
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.BundleCompat
import com.example.composesample.R
import com.example.composesample.presentation.MainHeader
import kotlinx.coroutines.delay

/**
 * Android 16 Live Updates 알림 예제
 * - NotificationCompat.ProgressStyle 로 세그먼트/포인트/트래커 아이콘을 가진 진행 알림을 만든다.
 * - progressMax 는 설정자가 없고 세그먼트 길이의 합으로 결정된다는 비직관적 계약을 실제 API 호출로 보여준다.
 * - API 36 미만 기기에서 compat 이 이 스타일을 무엇으로 축약하는지를, 빌드된 Notification 의
 *   extras 를 그대로 읽어 화면에 덤프해 확인한다(발행하지 않아도 되고 권한도 필요 없다).
 * - 참고 URL 과 개념 정리는 같은 폴더의 exampleGuide.kt 참조
 */

// ==================== 알림 구성부 (Compose 밖에서 단독으로 검증 가능) ====================

internal const val LIVE_UPDATE_CHANNEL_ID = "live_update_progress_example"
private const val LIVE_UPDATE_NOTIFICATION_ID = 4601

/**
 * 배달 시나리오 한 구간.
 *
 * length 의 합이 곧 progressMax 다 — ProgressStyle 에는 setProgressMax() 가 없다.
 */
internal data class DeliverySegment(
    val label: String,
    val length: Int,
    val color: Color
)

/** 진행 트랙 위에 찍히는 지점 마커(도착 예정 지점 등) */
internal data class DeliveryPoint(
    val label: String,
    val position: Int,
    val color: Color
)

internal val deliverySegments = listOf(
    DeliverySegment("주문 접수", 20, Color(0xFF90CAF9)),
    DeliverySegment("조리", 40, Color(0xFF4FC3F7)),
    DeliverySegment("픽업", 15, Color(0xFFFFB74D)),
    DeliverySegment("배달", 45, Color(0xFF66BB6A))
)

internal val deliveryPoints = listOf(
    DeliveryPoint("가게 출발", 80, Color(0xFFEF5350)),
    DeliveryPoint("도착 예정", 110, Color(0xFF7E57C2))
)

/** 데모 카드가 조작하는 알림 옵션 전부 */
internal data class LiveUpdateOptions(
    val progress: Int = 45,
    val indeterminate: Boolean = false,
    val styledByProgress: Boolean = true,
    val useSegments: Boolean = true,
    val requestPromotedOngoing: Boolean = true
)

/**
 * 옵션대로 ProgressStyle 을 만든다.
 *
 * 세그먼트를 끄면(useSegments = false) 세그먼트 목록이 비어 progressMax 가 기본값 100 이 된다.
 */
internal fun buildProgressStyle(
    context: Context,
    options: LiveUpdateOptions
): NotificationCompat.ProgressStyle {
    val style = NotificationCompat.ProgressStyle()
        .setProgress(options.progress)
        .setProgressIndeterminate(options.indeterminate)
        // styledByProgress = true 면 트랙 전체가 현재 진행률 색으로 칠해지고,
        // false 면 세그먼트가 각자의 색을 유지한다.
        .setStyledByProgress(options.styledByProgress)
        .setProgressTrackerIcon(
            IconCompat.createWithResource(context, R.drawable.ic_launcher_foreground)
        )

    if (options.useSegments) {
        deliverySegments.forEachIndexed { index, segment ->
            style.addProgressSegment(
                NotificationCompat.ProgressStyle.Segment(segment.length)
                    .setId(index)
                    .setColor(segment.color.toArgb())
            )
        }
        deliveryPoints.forEachIndexed { index, point ->
            style.addProgressPoint(
                NotificationCompat.ProgressStyle.Point(point.position)
                    .setId(index)
                    .setColor(point.color.toArgb())
            )
        }
    }
    return style
}

/**
 * 발행 여부와 무관하게 Notification 객체를 만든다.
 *
 * 이 객체의 extras 를 읽는 것이 이 예제의 실측 수단이다 — 발행하지 않아도,
 * POST_NOTIFICATIONS 권한이 없어도 compat 이 무엇을 넣었는지 확인할 수 있다.
 */
internal fun buildLiveUpdateNotification(
    context: Context,
    options: LiveUpdateOptions
): Notification =
    NotificationCompat.Builder(context, LIVE_UPDATE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("치킨 배달 중")
        .setContentText("조리가 끝나 픽업을 기다리는 중입니다")
        // 상태 바 칩에 들어갈 짧은 문구. Android 16 에서만 칩으로 렌더된다.
        .setShortCriticalText("12분")
        // 승격 요청은 compat 에 버전 분기가 없다 — extras 에 boolean 한 줄을 넣을 뿐이고,
        // 실제 승격 판단은 Android 16 시스템이 한다.
        .setRequestPromotedOngoing(options.requestPromotedOngoing)
        // 진행 중 작업이므로 ongoing. 승격 후보가 되려면 필요한 성격이기도 하다.
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setStyle(buildProgressStyle(context, options))
        .build()

/** NotificationChannelCompat 은 API 26 미만에서 no-op 이라 버전 분기가 필요 없다 */
internal fun createLiveUpdateChannel(context: Context) {
    val channel = NotificationChannelCompat.Builder(
        LIVE_UPDATE_CHANNEL_ID,
        NotificationManagerCompat.IMPORTANCE_DEFAULT
    )
        .setName("Live Update 진행 알림")
        .setDescription("ProgressStyle 예제가 사용하는 알림 채널")
        .setShowBadge(false)
        .build()
    NotificationManagerCompat.from(context).createNotificationChannel(channel)
}

private fun hasNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

/**
 * 빌드된 알림의 extras 중 ProgressStyle 관련 항목만 뽑는다.
 *
 * 키 이름은 전부 플랫폼과 같은 문자열이다. compat 상수로 공개된 것은 앞의 세 개뿐이라
 * (android.progress / progressMax / progressIndeterminate) 표를 한 줄로 유지하기 위해
 * 문자열을 그대로 쓴다.
 */
internal fun progressExtrasOf(notification: Notification): List<Pair<String, String>> {
    val extras: Bundle = notification.extras
    fun listSize(key: String): String =
        BundleCompat.getParcelableArrayList(extras, key, Bundle::class.java)
            ?.size?.let { "$it 개" } ?: "없음"

    return listOf(
        "android.progressMax" to extras.getInt("android.progressMax").toString(),
        "android.progress" to extras.getInt("android.progress").toString(),
        "android.progressIndeterminate" to extras.getBoolean("android.progressIndeterminate").toString(),
        "android.styledByProgress" to extras.getBoolean("android.styledByProgress").toString(),
        "android.progressSegments" to listSize("android.progressSegments"),
        "android.progressPoints" to listSize("android.progressPoints"),
        "android.requestPromotedOngoing" to extras.getBoolean("android.requestPromotedOngoing").toString(),
        "android.shortCriticalText" to (extras.getString("android.shortCriticalText") ?: "없음"),
        "androidx.core.app.extra.COMPAT_TEMPLATE" to
                (extras.getString("androidx.core.app.extra.COMPAT_TEMPLATE")
                    ?.substringAfterLast('.') ?: "없음")
    )
}

// ==================== 화면 ====================

@Composable
fun LiveUpdateNotificationExampleUI(onBackEvent: () -> Unit) {
    var options by remember { mutableStateOf(LiveUpdateOptions()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Live Updates 알림",
            onBackIconClicked = onBackEvent
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ProgressStyleConceptCard() }
            item {
                LiveUpdateDemoCard(
                    options = options,
                    onOptionsChange = { options = it }
                )
            }
            item { NotificationExtrasCard(options = options) }
            item { ProgressMaxRuleCard() }
            item { FallbackCard() }
            item { LiveUpdatePitfallCard() }
        }
    }
}

// ==================== 1. 개념 ====================

@Composable
private fun ProgressStyleConceptCard() {
    NotificationSectionCard(title = "1. setProgress 와 무엇이 다른가") {
        BodyText(
            "Android 16 의 Live Updates 는 \"지금 진행 중인 일\"을 알림 그늘에서 끌어올려 " +
                "상태 바와 잠금 화면에 계속 보여 주는 승격(promoted) 알림이다. " +
                "그 진행 상황을 그리는 도구가 NotificationCompat.ProgressStyle 이고, " +
                "기존 setProgress() 와는 표현할 수 있는 정보량 자체가 다르다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        ExtrasRow("구분", "setProgress(max, progress, indeterminate)", isHeader = true)
        ExtrasRow("표현", "단일 색 막대 하나. 진행률 외에는 아무 정보도 담을 수 없다")
        Spacer(modifier = Modifier.height(8.dp))
        ExtrasRow("구분", "ProgressStyle", isHeader = true)
        ExtrasRow("세그먼트", "구간마다 길이와 색 — 주문 접수 / 조리 / 픽업 / 배달")
        ExtrasRow("포인트", "트랙 위 특정 위치의 지점 마커 — 가게 출발, 도착 예정")
        ExtrasRow("트래커", "현재 위치를 따라다니는 아이콘(setProgressTrackerIcon)")
        ExtrasRow("시작/끝", "트랙 양 끝 아이콘(setProgressStartIcon / setProgressEndIcon)")

        Spacer(modifier = Modifier.height(12.dp))
        CaptionText(
            "승격 자체는 앱이 결정하지 못한다. setRequestPromotedOngoing(true) 은 \"요청\"일 뿐이고, " +
                "실제 승격 여부는 Android 16 시스템이 판단한다."
        )
    }
}

// ==================== 2. 실측 데모 ====================

@Composable
private fun LiveUpdateDemoCard(
    options: LiveUpdateOptions,
    onOptionsChange: (LiveUpdateOptions) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasNotificationPermission(context)) }
    var isPosted by remember { mutableStateOf(false) }
    var autoAdvance by remember { mutableStateOf(false) }
    var notifyCount by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    // 진행률의 상한은 세그먼트 길이 합이다. 세그먼트를 끄면 100 이 된다.
    val progressMax = remember(options.useSegments) {
        buildProgressStyle(context, options).progressMax
    }

    fun postNotification() {
        createLiveUpdateChannel(context)
        NotificationManagerCompat.from(context)
            .notify(LIVE_UPDATE_NOTIFICATION_ID, buildLiveUpdateNotification(context, options))
        notifyCount++
        isPosted = true
    }

    // 자동 진행 — 알림을 매 프레임 갱신하면 시스템이 스로틀링하므로 800ms 간격으로만 다시 그린다.
    LaunchedEffect(autoAdvance, progressMax) {
        while (autoAdvance) {
            delay(800L)
            val next = if (options.progress >= progressMax) 0 else options.progress + 5
            onOptionsChange(options.copy(progress = next.coerceAtMost(progressMax)))
            if (isPosted && hasPermission) {
                postNotification()
            }
        }
    }

    NotificationSectionCard(title = "2. 알림을 직접 발행해 보기") {
        BodyText("진행률 ${options.progress} / $progressMax")
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = options.progress.toFloat(),
            onValueChange = { onOptionsChange(options.copy(progress = it.toInt())) },
            valueRange = 0f..progressMax.toFloat()
        )

        SegmentTrack(progress = options.progress, progressMax = progressMax, useSegments = options.useSegments)

        Spacer(modifier = Modifier.height(10.dp))
        OptionSwitch(
            label = "세그먼트/포인트 사용",
            checked = options.useSegments,
            onCheckedChange = { onOptionsChange(options.copy(useSegments = it)) }
        )
        OptionSwitch(
            label = "styledByProgress (트랙을 진행률 색으로 통일)",
            checked = options.styledByProgress,
            onCheckedChange = { onOptionsChange(options.copy(styledByProgress = it)) }
        )
        OptionSwitch(
            label = "indeterminate (진행률 미상 표시)",
            checked = options.indeterminate,
            onCheckedChange = { onOptionsChange(options.copy(indeterminate = it)) }
        )
        OptionSwitch(
            label = "requestPromotedOngoing (승격 요청)",
            checked = options.requestPromotedOngoing,
            onCheckedChange = { onOptionsChange(options.copy(requestPromotedOngoing = it)) }
        )
        OptionSwitch(
            label = "자동 진행 (0.8초마다 +5)",
            checked = autoAdvance,
            onCheckedChange = { autoAdvance = it }
        )

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(
                text = if (isPosted) "알림 갱신" else "알림 발행",
                color = Color(0xFF1976D2)
            ) {
                if (hasPermission) {
                    postNotification()
                } else {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            DemoButton(text = "알림 제거", color = Color(0xFF757575)) {
                NotificationManagerCompat.from(context).cancel(LIVE_UPDATE_NOTIFICATION_ID)
                autoAdvance = false
                isPosted = false
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            if (hasPermission) {
                "POST_NOTIFICATIONS 허용됨 · 발행 ${notifyCount}회"
            } else {
                "POST_NOTIFICATIONS 미허용 — 발행 버튼이 먼저 권한을 요청한다(API 33+)"
            }
        )
        CaptionText(
            "이 기기는 API ${Build.VERSION.SDK_INT} 다. " +
                if (Build.VERSION.SDK_INT >= 36) {
                    "세그먼트와 포인트가 그대로 렌더된다."
                } else {
                    "compat 이 단색 진행 막대 하나로 축약해 발행한다(5번 카드)."
                }
        )
    }
}

/** 알림이 아니라 화면 안에서 세그먼트 구성을 눈으로 보기 위한 재현 트랙 */
@Composable
private fun SegmentTrack(progress: Int, progressMax: Int, useSegments: Boolean) {
    val segments = if (useSegments) deliverySegments else listOf(
        DeliverySegment("기본 트랙", 100, Color(0xFFBDBDBD))
    )

    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {
        segments.forEach { segment ->
            Box(
                modifier = Modifier
                    .weight(segment.length.toFloat())
                    .fillMaxSize()
                    .padding(horizontal = 1.dp)
                    .background(segment.color, RoundedCornerShape(3.dp))
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
    val ratio = if (progressMax == 0) 0 else progress * 100 / progressMax
    CaptionText(
        segments.joinToString(" · ") { "${it.label}(${it.length})" } + "  →  진행 ${ratio}%"
    )
}

// ==================== 3. 빌드 결과 실측 ====================

@Composable
private fun NotificationExtrasCard(options: LiveUpdateOptions) {
    val context = LocalContext.current
    // 발행하지 않고 빌드만 한다. 권한이 없어도 되고 알림도 뜨지 않는다.
    val extras = remember(options) { progressExtrasOf(buildLiveUpdateNotification(context, options)) }

    NotificationSectionCard(title = "3. 빌드된 Notification 의 extras (이 기기 실측)") {
        BodyText(
            "위 옵션으로 만든 Notification 객체를 그대로 뜯어본 결과다. " +
                "NotificationCompatBuilder.build() 가 Style.addCompatExtras(extras) 를 호출하기 때문에, " +
                "API 36 미만 기기에서도 세그먼트·포인트·승격 요청은 extras 에 그대로 실려 있다 — " +
                "다만 그것을 그려 줄 시스템이 없을 뿐이다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        ExtrasRow("키", "값", isHeader = true)
        extras.forEach { (key, value) ->
            ExtrasRow(key, value)
        }

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "android.progressMax 는 세그먼트 길이의 합이다. 세그먼트를 끄면 120 에서 100 으로 " +
                "떨어지는 것을 2번 카드의 스위치로 바로 확인할 수 있다."
        )
        CaptionText(
            "⚠️ 여기 보이는 android.progress 는 렌더된 값이 아니라 요청값이다. 하위 버전 폴백은 " +
                "min(progress, max) 로 잘라 막대를 그리지만, 그 뒤에 addCompatExtras 가 원본 " +
                "progress 로 덮어쓴다 — 실측(SM-A725F/API 33): progress=999, max=120 으로 빌드하면 " +
                "extras 에는 999 가 남는다."
        )
    }
}

// ==================== 4. progressMax 규칙 ====================

@Composable
private fun ProgressMaxRuleCard() {
    val context = LocalContext.current
    var cookingLength by remember { mutableIntStateOf(40) }

    // 우리가 더한 값이 아니라 ProgressStyle 이 실제로 계산한 값을 그대로 읽는다.
    val measuredMax = remember(cookingLength) {
        NotificationCompat.ProgressStyle()
            .addProgressSegment(NotificationCompat.ProgressStyle.Segment(20))
            .addProgressSegment(NotificationCompat.ProgressStyle.Segment(cookingLength))
            .addProgressSegment(NotificationCompat.ProgressStyle.Segment(15))
            .addProgressSegment(NotificationCompat.ProgressStyle.Segment(45))
            .progressMax
    }
    val emptyMax = remember { NotificationCompat.ProgressStyle().progressMax }
    val trackerMax = remember { buildProgressStyle(context, LiveUpdateOptions()).progressMax }

    NotificationSectionCard(title = "4. progressMax 에는 설정자가 없다") {
        BodyText(
            "setProgress(int) 는 있는데 setProgressMax(int) 는 없다. getProgressMax() 는 " +
                "세그먼트 길이를 모두 더해 그 자리에서 계산하고, 세그먼트가 하나도 없으면 100 을 돌려준다. " +
                "즉 진행률의 축은 세그먼트를 어떻게 나눴는지에 따라 바뀐다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        ExtrasRow("세그먼트 없음", "$emptyMax (기본값)")
        ExtrasRow("배달 시나리오 4구간", "$trackerMax (20+40+15+45)")
        ExtrasRow("조리 구간 = $cookingLength", "$measuredMax  ← 실제 getProgressMax() 호출값")

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(text = "조리 -10", color = Color(0xFF546E7A)) {
                cookingLength = (cookingLength - 10).coerceAtLeast(0)
            }
            DemoButton(text = "조리 +10", color = Color(0xFF546E7A)) {
                cookingLength += 10
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "길이가 0 이하인 세그먼트는 합계에서 제외된다 — 실측: Segment(0) + Segment(30) 의 " +
                "progressMax 는 100 이 아니라 30 이다(빈 목록일 때만 100). 합이 int 범위를 넘으면 " +
                "Math.addExact 가 던지는 오버플로를 잡아 100 으로 되돌린다."
        )
    }
}

// ==================== 5. API 36 미만 폴백 ====================

@Composable
private fun FallbackCard() {
    val context = LocalContext.current
    val canPostPromoted = remember {
        NotificationManagerCompat.from(context).canPostPromotedNotifications()
    }

    NotificationSectionCard(title = "5. API 36 미만에서는 무엇으로 축약되는가") {
        BodyText(
            "ProgressStyle.apply() 는 Build.VERSION.SDK_INT 를 36 과 비교해 두 갈래로 갈린다. " +
                "36 이상이면 플랫폼 Notification.ProgressStyle 을 만들어 세그먼트·포인트·아이콘·" +
                "styledByProgress 를 전부 옮겨 담고, 미만이면 단 한 줄로 끝난다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        CodeText("setProgress(getProgressMax(), min(progress, max), indeterminate)")

        Spacer(modifier = Modifier.height(10.dp))
        BodyText(
            "즉 하위 버전에서는 진행률만 살아남고 세그먼트·포인트·트래커 아이콘은 렌더에서 사라진다. " +
                "값 자체가 지워지는 것은 아니라서(3번 카드의 extras 참조) 같은 코드가 Android 16 에서는 " +
                "분기 없이 그대로 승격 후보가 된다."
        )
        Spacer(modifier = Modifier.height(8.dp))
        BodyText(
            "이 두 경로는 같은 키를 두 번 쓴다. 폴백의 setProgress() 가 android.progress 에 " +
                "min(progress, max) 를 쓰고, 그 뒤 addCompatExtras() 가 원본 progress 로 덮는다. " +
                "실측으로 확인한 순서다 — progress=999 / max=120 으로 빌드하면 extras 에는 999 가 남는다. " +
                "extras 를 렌더 결과로 읽으면 안 되는 이유다."
        )

        Spacer(modifier = Modifier.height(12.dp))
        ExtrasRow("이 기기 SDK_INT", Build.VERSION.SDK_INT.toString())
        ExtrasRow("canPostPromotedNotifications()", canPostPromoted.toString())
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText(
            "canPostPromotedNotifications() 도 SDK_INT < 36 이면 플랫폼을 부르지 않고 즉시 false 를 " +
                "돌려준다. 따라서 이 값이 false 라고 해서 사용자가 승격을 껐다는 뜻은 아니다 — " +
                "Android 16 미만에서는 언제나 false 다."
        )
    }
}

// ==================== 6. 함정 정리 ====================

@Composable
private fun LiveUpdatePitfallCard() {
    NotificationSectionCard(title = "6. 실기기에서 걸리는 것들") {
        PitfallRow(
            "승격은 요청일 뿐이다",
            "setRequestPromotedOngoing(true) 는 extras 에 android.requestPromotedOngoing 을 " +
                "넣는 게 전부다(compat 에 버전 분기조차 없다). 승격 여부는 시스템이 정한다."
        )
        PitfallRow(
            "ongoing 이 아니면 후보가 아니다",
            "진행 중 알림이라는 성격을 setOngoing(true) 으로 밝혀야 한다."
        )
        PitfallRow(
            "POST_NOTIFICATIONS",
            "API 33+ 에서는 권한이 없으면 notify() 가 조용히 무시된다. 예외도 나지 않는다."
        )
        PitfallRow(
            "갱신 빈도",
            "진행률이 바뀔 때마다 notify() 를 부르면 시스템이 알림 갱신을 스로틀링한다. " +
                "이 예제의 자동 진행이 0.8초 간격인 이유다."
        )
        PitfallRow(
            "채널 중요도",
            "IMPORTANCE_MIN 채널은 상태 바에 아이콘조차 올리지 않아 승격 대상이 되기 어렵다."
        )
        PitfallRow(
            "⚠️ Android 16 실기기",
            "세그먼트/포인트/트래커의 실제 렌더는 API 36 기기에서만 확인할 수 있다. " +
                "그 미만에서는 5번 카드의 축약 경로만 검증 가능하다."
        )
    }
}

// ==================== 공통 요소 ====================

@Composable
private fun NotificationSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = Color(0xFF424242),
        lineHeight = 19.sp
    )
}

@Composable
private fun CaptionText(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        color = Color(0xFF757575),
        lineHeight = 16.sp
    )
}

@Composable
private fun CodeText(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFECEFF1), RoundedCornerShape(6.dp))
            .padding(10.dp),
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF37474F),
        lineHeight = 16.sp
    )
}

@Composable
private fun ExtrasRow(
    key: String,
    value: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp)
    ) {
        Text(
            text = key,
            modifier = Modifier.width(150.dp),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            fontSize = 11.sp,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF616161)
        )
    }
}

@Composable
private fun PitfallRow(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 10.dp)) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F)
        )
        Spacer(modifier = Modifier.height(2.dp))
        CaptionText(description)
    }
}

@Composable
private fun OptionSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            color = Color(0xFF424242)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1976D2))
        )
    }
}

@Composable
private fun DemoButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text = text, fontSize = 12.sp, color = Color.White)
    }
}
