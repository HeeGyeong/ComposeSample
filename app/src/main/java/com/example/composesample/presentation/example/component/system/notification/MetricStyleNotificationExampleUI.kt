package com.example.composesample.presentation.example.component.system.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import com.example.composesample.R
import com.example.composesample.presentation.MainHeader
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.TimeZone

/**
 * Notification MetricStyle 예제 (androidx.core 1.19 신규)
 * - 운동 기록·배달·항공편처럼 "라벨 + 값" 지표 묶음을 보여주는 NotificationCompat.MetricStyle 을 다룬다.
 * - 앱 도메인 값 하나에서 (API 37+) MetricStyle 과 (전 버전) 폴백 문구·크로노미터를 함께 만드는 구조로 쓴다 —
 *   MetricStyle() 생성자와 TimeDifference 팩토리가 @RequiresApi(37) 이라 lint 가 버전 분기를 강제하기 때문이다.
 * - API 37 미만에서 apply() 가 아무것도 그리지 않는다는 것(ProgressStyle 처럼 축약 렌더가 없다)을,
 *   빌드된 Notification 의 extras 를 덤프해 이 기기에서 확인한다(발행하지 않아도 되고 권한도 필요 없다).
 * - 참고 URL 과 개념 정리는 같은 폴더의 exampleGuide.kt 참조
 */

// ==================== 알림 구성부 (Compose 밖에서 단독으로 검증 가능) ====================

internal const val METRIC_STYLE_CHANNEL_ID = "metric_style_example"
private const val METRIC_STYLE_NOTIFICATION_ID = 4602

// extras 키는 전부 core 1.19.1 소스의 private 상수와 같은 문자열이다(공개 상수가 없다).
private const val EXTRA_METRICS = "android.metrics"
private const val EXTRA_METRICS_CRITICAL_INDEX = "android.metrics.criticalIndex"

/** 범위 밖 critical 인덱스를 시험하려고 고른 값 — 시나리오 지표는 최대 4개다 */
private const val OUT_OF_RANGE_INDEX = 5

/** MetricStyle.METRIC_INDEX_NONE 과 같은 값. 그 상수의 클래스가 @RequiresApi(26) 이라 화면 코드에서는 값을 따로 둔다 */
private const val METRIC_INDEX_NONE = -1

private const val MILLIS_PER_DAY = 86_400_000L

/** TimeDifference 포맷(ADAPTIVE / CHRONOMETER) — Metric 상수로의 변환은 API 37 경로에서만 한다 */
internal enum class TimeFormat { ADAPTIVE, CHRONOMETER }

/** FixedDate 포맷(AUTOMATIC / LONG_DATE / SHORT_DATE) */
internal enum class DateStyle { AUTOMATIC, LONG, SHORT }

/**
 * 앱 쪽 도메인 값. MetricStyle(API 37+)과 하위 버전 폴백 문구를 둘 다 여기서 만든다.
 *
 * java.time·Metric 상수 대신 epoch 값과 앱 enum 으로 들고 있어 이 데이터와 폴백 문구에는 API 요구가 없다
 * (Metric·MetricStyle 은 java.time 때문에 클래스 단위로 @RequiresApi(26) 이다).
 */
internal sealed interface MetricSource {
    /** → Metric.FixedInt */
    data class Count(val value: Int, val unit: String? = null) : MetricSource

    /** → Metric.FixedFloat. 소수 자릿수는 0..6 이어야 하고 min ≤ max 여야 한다 */
    data class Decimal(
        val value: Float,
        val unit: String? = null,
        val minDigits: Int = 0,
        val maxDigits: Int = 2
    ) : MetricSource

    /** → Metric.FixedText */
    data class Text(val value: String, val unit: String? = null) : MetricSource

    /** → Metric.FixedDate */
    data class Date(val epochDay: Long, val style: DateStyle) : MetricSource

    /** → Metric.FixedTime (하루 중 초) */
    data class ClockTime(val secondOfDay: Int) : MetricSource

    /** → Metric.TimeDifference.forStopwatch — 시작 시각부터 경과 */
    data class Stopwatch(val startEpochMillis: Long, val format: TimeFormat) : MetricSource

    /** → Metric.TimeDifference.forTimer — 끝 시각까지 남은 시간 */
    data class Timer(val endEpochMillis: Long, val format: TimeFormat) : MetricSource
}

internal data class MetricEntry(
    val label: String,
    val source: MetricSource,
    val semanticStyle: Int = NotificationCompat.SEMANTIC_STYLE_UNSPECIFIED
)

internal enum class MetricScenario(val title: String, val defaultCritical: Int) {
    RUNNING("러닝 기록", 0),
    DELIVERY("음식 배달", 1),
    FLIGHT("항공편 탑승", 2)
}

/**
 * 시나리오별 지표. 시각은 전부 baseMillis 하나에서 파생해, 같은 입력이면 같은 지표가 나오게 한다
 * (extras 비교·왕복 비교가 결정적이 된다).
 */
internal fun entriesOf(scenario: MetricScenario, baseMillis: Long): List<MetricEntry> =
    when (scenario) {
        MetricScenario.RUNNING -> listOf(
            MetricEntry(
                "거리",
                MetricSource.Decimal(5.27f, "km", minDigits = 2, maxDigits = 2),
                NotificationCompat.SEMANTIC_STYLE_SAFE
            ),
            MetricEntry(
                "경과",
                MetricSource.Stopwatch(baseMillis - (31 * 60 + 4) * 1000L, TimeFormat.CHRONOMETER)
            ),
            MetricEntry(
                "심박",
                MetricSource.Count(152, "bpm"),
                NotificationCompat.SEMANTIC_STYLE_CAUTION
            ),
            // 네 번째 지표 — 펼친 알림에는 최대 3개만 보인다(javadoc). compat 은 개수를 자르지 않는다.
            MetricEntry("페이스", MetricSource.Text("5'54\"", "/km"))
        )

        MetricScenario.DELIVERY -> listOf(
            MetricEntry(
                "도착 예정",
                MetricSource.ClockTime(localSecondOfDay(baseMillis + 18 * 60_000L)),
                NotificationCompat.SEMANTIC_STYLE_INFO
            ),
            MetricEntry(
                "남은 시간",
                MetricSource.Timer(baseMillis + 18 * 60_000L, TimeFormat.ADAPTIVE),
                NotificationCompat.SEMANTIC_STYLE_CAUTION
            ),
            MetricEntry("기사 거리", MetricSource.Decimal(1.2f, "km", minDigits = 1, maxDigits = 1))
        )

        MetricScenario.FLIGHT -> listOf(
            MetricEntry(
                "출발일",
                MetricSource.Date(localEpochDay(baseMillis, plusDays = 3), DateStyle.SHORT)
            ),
            MetricEntry("탑승 시작", MetricSource.ClockTime(14 * 3600 + 25 * 60)),
            MetricEntry(
                "게이트 변경",
                MetricSource.Text("C4"),
                NotificationCompat.SEMANTIC_STYLE_DANGER
            )
        )
    }

/** 기기 시간대 기준 하루 중 초 — java.time(API 26) 없이 Calendar 로 계산한다 */
private fun localSecondOfDay(epochMillis: Long): Int {
    val calendar = Calendar.getInstance().apply { timeInMillis = epochMillis }
    return calendar.get(Calendar.HOUR_OF_DAY) * 3600 +
        calendar.get(Calendar.MINUTE) * 60 +
        calendar.get(Calendar.SECOND)
}

/** 기기 시간대 기준 날짜(+plusDays)의 epochDay — LocalDate.toEpochDay() 와 같은 값 */
private fun localEpochDay(epochMillis: Long, plusDays: Int): Long {
    val local = Calendar.getInstance().apply {
        timeInMillis = epochMillis
        add(Calendar.DAY_OF_MONTH, plusDays)
    }
    val utcMidnight = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }
    return utcMidnight.timeInMillis / MILLIS_PER_DAY
}

/** epochDay → 월·일 (UTC 자정 기준이라 시간대와 무관하다) */
private fun monthDayOf(epochDay: Long): Pair<Int, Int> {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = epochDay * MILLIS_PER_DAY
    }
    return (calendar.get(Calendar.MONTH) + 1) to calendar.get(Calendar.DAY_OF_MONTH)
}

@RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
private fun TimeFormat.toMetricFormat(): Int = when (this) {
    TimeFormat.ADAPTIVE -> NotificationCompat.Metric.TimeDifference.FORMAT_ADAPTIVE
    TimeFormat.CHRONOMETER -> NotificationCompat.Metric.TimeDifference.FORMAT_CHRONOMETER
}

@RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
private fun DateStyle.toMetricFormat(): Int = when (this) {
    DateStyle.AUTOMATIC -> NotificationCompat.Metric.FixedDate.FORMAT_AUTOMATIC
    DateStyle.LONG -> NotificationCompat.Metric.FixedDate.FORMAT_LONG_DATE
    DateStyle.SHORT -> NotificationCompat.Metric.FixedDate.FORMAT_SHORT_DATE
}

@RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
private fun MetricSource.toMetricValue(): NotificationCompat.Metric.MetricValue = when (this) {
    is MetricSource.Count -> NotificationCompat.Metric.FixedInt(value, unit)
    is MetricSource.Decimal -> NotificationCompat.Metric.FixedFloat(value, unit, minDigits, maxDigits)
    is MetricSource.Text -> NotificationCompat.Metric.FixedText(value, unit)
    is MetricSource.Date -> NotificationCompat.Metric.FixedDate(LocalDate.ofEpochDay(epochDay), style.toMetricFormat())
    is MetricSource.ClockTime -> NotificationCompat.Metric.FixedTime(LocalTime.ofSecondOfDay(secondOfDay.toLong()))
    is MetricSource.Stopwatch -> NotificationCompat.Metric.TimeDifference.forStopwatch(
        Instant.ofEpochMilli(startEpochMillis), format.toMetricFormat()
    )
    is MetricSource.Timer -> NotificationCompat.Metric.TimeDifference.forTimer(
        Instant.ofEpochMilli(endEpochMillis), format.toMetricFormat()
    )
}

@RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
internal fun MetricEntry.toMetric(): NotificationCompat.Metric =
    NotificationCompat.Metric(source.toMetricValue(), label, semanticStyle)

/** critical 인덱스는 검사 없이 그대로 저장된다 — 범위 밖이면 getCriticalMetric() 이 null 이 될 뿐이다 */
@RequiresApi(Build.VERSION_CODES.CINNAMON_BUN)
internal fun metricStyleOf(entries: List<MetricEntry>, criticalIndex: Int): NotificationCompat.MetricStyle =
    NotificationCompat.MetricStyle()
        .setMetrics(entries.map { it.toMetric() })
        .setCriticalMetric(criticalIndex)

private fun metricBaseBuilder(context: Context, scenario: MetricScenario): NotificationCompat.Builder =
    NotificationCompat.Builder(context, METRIC_STYLE_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(scenario.title)
        // 승격(promoted ongoing)이어야 critical 지표가 상태 바 칩 후보가 되고 semantic style 이 색으로 보인다.
        // 승격 여부는 시스템이 정한다 — 이 호출은 extras 에 요청 boolean 을 넣을 뿐이다.
        .setRequestPromotedOngoing(true)
        .setOngoing(true)
        .setOnlyAlertOnce(true)

/**
 * 앱이 실제로 쓸 경로.
 *
 * MetricStyle 은 API 37 이상에서만 붙이고, 그 아래에서는 폴백 문구(content text)와 크로노미터로 대신한다.
 * 37 미만에서 MetricStyle 을 붙여도 apply() 가 아무것도 그리지 않으므로(3번 카드) 이 폴백은 앱 몫이다.
 */
internal fun buildMetricNotification(
    context: Context,
    scenario: MetricScenario,
    entries: List<MetricEntry>,
    criticalIndex: Int,
    fallback: Boolean,
    nowMillis: Long
): Notification {
    val builder = metricBaseBuilder(context, scenario)
    if (fallback) {
        builder.setContentText(fallbackTextOf(entries, nowMillis))
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
        builder.setStyle(metricStyleOf(entries, criticalIndex))
    } else if (fallback) {
        applyChronometerFallback(builder, entries, criticalIndex)
    }
    return builder.build()
}

/**
 * 측정 전용 — 버전 분기 없이 MetricStyle 을 붙인다.
 *
 * 37 미만에서 compat 이 무엇을 하는지(그리지 않고 extras 에만 싣는다)를 이 기기에서 보려는 목적이다.
 *
 * NewApi 억제 근거(측정 함수 3개 공통 — 이 함수 · criticalMappingOf · runValidationCases):
 * MetricStyle() 생성자와 TimeDifference 팩토리 본문에는 플랫폼 호출이 없고 java.time(API 26)만 쓴다
 * (core 1.19.1 소스 확인). 플랫폼 클래스는 Api37Impl 안에서만 참조되며 그 경로는 SDK_INT ≥ 37 에서만 탄다.
 * API 26 은 화면 진입부(MetricStyleNotificationExampleUI)의 분기가 보장한다.
 */
@SuppressLint("NewApi")
internal fun buildForcedMetricNotification(
    context: Context,
    scenario: MetricScenario,
    entries: List<MetricEntry>,
    criticalIndex: Int
): Notification =
    metricBaseBuilder(context, scenario)
        .setStyle(metricStyleOf(entries, criticalIndex))
        .build()

/**
 * TimeDifference 의 실시간 갱신은 시스템이 그려 주는 것이라, 37 미만에서는 같은 효과를 크로노미터로 낸다.
 * critical 지표가 시간 지표면 그것을, 아니면 첫 번째 시간 지표를 쓴다.
 */
private fun applyChronometerFallback(
    builder: NotificationCompat.Builder,
    entries: List<MetricEntry>,
    criticalIndex: Int
) {
    val sources = entries.map { it.source }
    val target = sources.getOrNull(criticalIndex)?.takeIf { it.isRunningTime() }
        ?: sources.firstOrNull { it.isRunningTime() }
        ?: return
    when (target) {
        is MetricSource.Stopwatch -> builder
            .setWhen(target.startEpochMillis)
            .setShowWhen(true)
            .setUsesChronometer(true)

        is MetricSource.Timer -> builder
            .setWhen(target.endEpochMillis)
            .setShowWhen(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)

        else -> Unit
    }
}

private fun MetricSource.isRunningTime(): Boolean =
    this is MetricSource.Stopwatch || this is MetricSource.Timer

/** 하위 버전 폴백 문구 — 시스템이 지표를 그리지 못하는 기기에서 content text 로 대신 보여준다 */
internal fun fallbackTextOf(entries: List<MetricEntry>, nowMillis: Long): String =
    entries.joinToString(" · ") { "${it.label} ${it.source.displayText(nowMillis)}" }

internal fun MetricSource.displayText(nowMillis: Long): String = when (this) {
    is MetricSource.Count -> "$value${unitSuffix(unit)}"
    is MetricSource.Decimal -> NumberFormat.getInstance().apply {
        minimumFractionDigits = minDigits
        maximumFractionDigits = maxDigits
    }.format(value.toDouble()) + unitSuffix(unit)
    is MetricSource.Text -> value + unitSuffix(unit)
    is MetricSource.Date -> monthDayOf(epochDay).let { (month, day) -> "${month}월 ${day}일" }
    // FixedTime 은 시:분만 표시된다(javadoc) — 폴백도 같게 맞춘다
    is MetricSource.ClockTime -> "${(secondOfDay / 3600).twoDigits()}:${(secondOfDay % 3600 / 60).twoDigits()}"
    // 라벨("경과" · "남은 시간")이 의미를 말하므로 값에는 시간만 둔다
    is MetricSource.Stopwatch -> formatTimeDifference(nowMillis - startEpochMillis, format)
    is MetricSource.Timer -> formatTimeDifference(endEpochMillis - nowMillis, format)
}

private fun unitSuffix(unit: String?): String = if (unit.isNullOrEmpty()) "" else " $unit"

private fun Number.twoDigits(): String = toString().padStart(2, '0')

private fun formatTimeDifference(millis: Long, format: TimeFormat): String {
    val totalSeconds = millis.coerceAtLeast(0L) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (format == TimeFormat.CHRONOMETER) {
        if (hours > 0) "$hours:${minutes.twoDigits()}:${seconds.twoDigits()}" else "$minutes:${seconds.twoDigits()}"
    } else {
        // 적응형 — javadoc 예시는 "1h 5m; 15m; 1m 30s; 5s"
        when {
            hours > 0 -> "${hours}시간 ${minutes}분"
            minutes > 0 -> "${minutes}분"
            else -> "${seconds}초"
        }
    }
}

/** NotificationChannelCompat 은 API 26 미만에서 no-op 이라 버전 분기가 필요 없다 */
internal fun createMetricStyleChannel(context: Context) {
    val channel = NotificationChannelCompat.Builder(
        METRIC_STYLE_CHANNEL_ID,
        NotificationManagerCompat.IMPORTANCE_DEFAULT
    )
        .setName("MetricStyle 지표 알림")
        .setDescription("MetricStyle 예제가 사용하는 알림 채널")
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

// ==================== extras 실측 ====================

internal fun metricBundlesOf(notification: Notification): List<Bundle>? =
    BundleCompat.getParcelableArrayList(notification.extras, EXTRA_METRICS, Bundle::class.java)

internal fun criticalIndexExtraOf(notification: Notification): String =
    if (notification.extras.containsKey(EXTRA_METRICS_CRITICAL_INDEX)) {
        notification.extras.getInt(EXTRA_METRICS_CRITICAL_INDEX).toString()
    } else {
        "없음"
    }

/** 앱 경로와 강제 부착 알림을 같은 키로 나란히 비교하기 위한 표 */
internal fun metricExtrasOf(notification: Notification): List<Pair<String, String>> {
    val extras = notification.extras
    return listOf(
        "android.text" to (extras.getCharSequence(NotificationCompat.EXTRA_TEXT)?.toString() ?: "없음"),
        EXTRA_METRICS to (metricBundlesOf(notification)?.let { "${it.size} 개" } ?: "없음"),
        EXTRA_METRICS_CRITICAL_INDEX to criticalIndexExtraOf(notification),
        "android.showChronometer" to extras.getBoolean(NotificationCompat.EXTRA_SHOW_CHRONOMETER).toString(),
        "android.chronometerCountDown" to extras.getBoolean(NotificationCompat.EXTRA_CHRONOMETER_COUNT_DOWN).toString(),
        "android.requestPromotedOngoing" to extras.getBoolean("android.requestPromotedOngoing").toString(),
        "COMPAT_TEMPLATE" to (extras.getString("androidx.core.app.extra.COMPAT_TEMPLATE")
            ?.substringAfterLast('.') ?: "없음")
    )
}

/** android.metrics 의 Bundle 하나를 한 줄로. 키 이름은 core 1.19.1 소스의 상수와 같다. */
internal fun describeMetricBundle(metric: Bundle): String {
    val head = "${metric.getString("label")} · semantic=${metric.getInt("semanticStyle")}"
    val value = metric.getBundle("value") ?: return "$head · value 없음"
    val body = when (value.getInt("_type")) {
        1 -> {
            val zero = when {
                value.containsKey("zeroTime") -> "zeroTime=${value.getLong("zeroTime")}ms"
                value.containsKey("zeroElapsedRealtime") ->
                    "zeroElapsedRealtime=${value.getLong("zeroElapsedRealtime")}"
                else -> "pausedDuration=${value.getLong("pausedDuration")}ms"
            }
            "TimeDifference($zero, countDown=${value.getBoolean("countDown")}, format=${value.getInt("format")})"
        }
        2 -> "FixedDate(epochDay=${value.getLong("value")}, format=${value.getInt("format")})"
        3 -> "FixedTime(secondOfDay=${value.getLong("value")})"
        4 -> "FixedInt(${value.getInt("value")}, unit=${value.getString("unit")})"
        5 -> "FixedFloat(${value.getFloat("value")}, unit=${value.getString("unit")}, " +
                "digits=${value.getInt("minDigits")}..${value.getInt("maxDigits")})"
        6 -> "FixedText(${value.getString("value")}, unit=${value.getString("unit")})"
        else -> "_type=${value.getInt("_type")}"
    }
    return "$head · $body"
}

// ==================== critical 인덱스 / 생성 검증 / 왕복 실측 ====================

internal data class CriticalMapping(
    val requested: Int,
    val criticalLabel: String?,
    /** Api37Impl.toPlatformStyle 이 플랫폼 setCriticalMetric() 에 넘기는 값 */
    val platformIndex: Int
)

/**
 * critical 인덱스가 플랫폼으로 넘어갈 때의 값을 이 기기에서 계산한다.
 *
 * Api37Impl 은 저장된 인덱스를 그대로 넘기지 않고 getMetrics().indexOf(getCriticalMetric()) 를 넘긴다 —
 * 같은 식을 여기서 그대로 계산한다. NewApi 억제 근거는 buildForcedMetricNotification 참조.
 */
@SuppressLint("NewApi")
internal fun criticalMappingOf(entries: List<MetricEntry>, criticalIndex: Int): CriticalMapping {
    val style = metricStyleOf(entries, criticalIndex)
    val critical = style.criticalMetric
    return CriticalMapping(
        requested = criticalIndex,
        criticalLabel = critical?.label?.toString(),
        platformIndex = style.metrics.indexOf(critical)
    )
}

internal data class ValidationCase(val title: String, val result: String, val threw: Boolean)

/**
 * 생성·빌드 시점에 compat 이 검사하는 것과 검사하지 않는 것을 실제 호출로 확인한다.
 *
 * 예외는 원인 사슬에서 IllegalArgumentException 을 찾는다(디버그 빌드 인터프리터가 감쌀 수 있다).
 * NewApi 억제 근거는 buildForcedMetricNotification 참조. Range·WrongConstant 는 일부러 잘못된 값을 넣어
 * 런타임 검사를 재는 것이라 억제한다 — lint 가 이 두 경우는 컴파일 시점에 잡는다는 것 자체가 측정 결과다.
 */
@SuppressLint("NewApi", "Range", "WrongConstant")
internal fun runValidationCases(context: Context): List<ValidationCase> {
    fun case(title: String, block: () -> String): ValidationCase = try {
        ValidationCase(title, block(), threw = false)
    } catch (e: Exception) {
        val cause = generateSequence(e as Throwable) { it.cause }
            .firstOrNull { it is IllegalArgumentException } ?: e
        ValidationCase(title, "${cause.javaClass.simpleName}: ${cause.message}", threw = true)
    }

    val threeMetrics = listOf(
        NotificationCompat.Metric(NotificationCompat.Metric.FixedInt(1), "하나"),
        NotificationCompat.Metric(NotificationCompat.Metric.FixedInt(2), "둘"),
        NotificationCompat.Metric(NotificationCompat.Metric.FixedInt(3), "셋")
    )
    // 나노초가 섞인 시각 — Instant.now() 는 이 기기에서 ms 단위라 일부러 만든다
    val nanoInstant = Instant.ofEpochSecond(1_800_000_000L, 123_456_789L)

    return listOf(
        case("지표 0개로 build()") {
            metricBaseBuilder(context, MetricScenario.RUNNING)
                .setStyle(NotificationCompat.MetricStyle())
                .build()
            "빌드됨"
        },
        case("label 이 공백(\"  \")") {
            NotificationCompat.Metric(NotificationCompat.Metric.FixedInt(1), "  ")
            "생성됨"
        },
        case("FixedFloat 자릿수 min 3 > max 1") {
            NotificationCompat.Metric.FixedFloat(1f, null, 3, 1)
            "생성됨"
        },
        case("FixedFloat 자릿수 max 7") {
            NotificationCompat.Metric.FixedFloat(1f, null, 0, 7)
            "생성됨"
        },
        case("TimeDifference format 0") {
            NotificationCompat.Metric.TimeDifference.forTimer(nanoInstant, 0)
            "생성됨"
        },
        case("지표 3개에 setCriticalMetric(5)") {
            val style = NotificationCompat.MetricStyle().setMetrics(threeMetrics).setCriticalMetric(5)
            "예외 없음 · getCriticalMetric() = ${style.criticalMetric}"
        },
        case("FixedTime(09:30:15.5)") {
            "getValue() = ${NotificationCompat.Metric.FixedTime(LocalTime.of(9, 30, 15, 500_000_000)).value}"
        },
        case("zeroTime 의 나노초 → extras") {
            val notification = metricBaseBuilder(context, MetricScenario.RUNNING)
                .setStyle(
                    NotificationCompat.MetricStyle().addMetric(
                        NotificationCompat.Metric(
                            NotificationCompat.Metric.TimeDifference.forStopwatch(
                                nanoInstant,
                                NotificationCompat.Metric.TimeDifference.FORMAT_CHRONOMETER
                            ),
                            "경과"
                        )
                    )
                )
                .build()
            val stored = metricBundlesOf(notification)?.firstOrNull()
                ?.getBundle("value")?.getLong("zeroTime")
            if (stored == null) {
                "extras 에 android.metrics 없음(API 37+ 는 compat extras 를 쓰지 않는다)"
            } else {
                "원본 nano=${nanoInstant.nano} → extras ${stored}ms (ms 아래 ${nanoInstant.nano % 1_000_000}ns 손실)"
            }
        }
    )
}

internal data class RoundTrip(
    val beforeCount: Int?,
    val afterCount: Int?,
    val beforeCritical: String,
    val afterCritical: String,
    val identical: Boolean
)

/**
 * 공개 API 인 NotificationCompat.Builder(context, notification) 로 기존 알림에서 Builder 를 되살려 다시 빌드한다.
 *
 * 37 미만에서 extras 에 지표를 싣는 이유가 이것이다 — 이미 발행한 알림을 갱신할 때 스타일을 되살릴 근거가
 * extras 밖에 없다(내부적으로 COMPAT_TEMPLATE 으로 MetricStyle 을 만들고 restoreFromCompatExtras 를 부른다).
 */
internal fun roundTripOf(context: Context, original: Notification): RoundTrip {
    val rebuilt = NotificationCompat.Builder(context, original).build()
    val before = metricBundlesOf(original)?.map(::describeMetricBundle)
    val after = metricBundlesOf(rebuilt)?.map(::describeMetricBundle)
    val beforeCritical = criticalIndexExtraOf(original)
    val afterCritical = criticalIndexExtraOf(rebuilt)
    return RoundTrip(
        beforeCount = before?.size,
        afterCount = after?.size,
        beforeCritical = beforeCritical,
        afterCritical = afterCritical,
        identical = before == after && beforeCritical == afterCritical
    )
}

// ==================== 화면 ====================

// ObsoleteSdkInt 억제: debug 는 HotSwan 때문에 minSdk 26 이라 아래 분기를 불필요하다고 보지만,
// release 는 minSdk 24 라 java.time 을 쓰는 측정 함수들을 이 분기가 막아야 한다.
@SuppressLint("ObsoleteSdkInt")
@Composable
fun MetricStyleNotificationExampleUI(onBackEvent: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        MainHeader(
            title = "Notification MetricStyle",
            onBackIconClicked = onBackEvent
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            MetricStyleContent()
        } else {
            // Metric 은 java.time 때문에 클래스 단위로 @RequiresApi(26) 이다. 이 프로젝트는 desugaring 을 쓰지 않는다.
            Column(modifier = Modifier.padding(16.dp)) {
                MetricSectionCard(title = "이 기기에서는 실행할 수 없다") {
                    BodyText(
                        "NotificationCompat.Metric 과 MetricStyle 은 java.time 을 쓰기 때문에 클래스 단위로 " +
                            "@RequiresApi(26) 이다. 이 기기는 API ${Build.VERSION.SDK_INT} 다."
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricStyleContent() {
    val context = LocalContext.current
    // 시각 기준점 — 화면에 들어온 순간 하나로 고정해 같은 옵션이면 같은 지표·같은 extras 가 나오게 한다
    val baseMillis = remember { System.currentTimeMillis() }
    var scenario by remember { mutableStateOf(MetricScenario.RUNNING) }
    var criticalIndex by remember { mutableIntStateOf(MetricScenario.RUNNING.defaultCritical) }
    var fallback by remember { mutableStateOf(true) }

    val entries = remember(scenario) { entriesOf(scenario, baseMillis) }
    // 발행하지 않고 빌드만 한다. 권한이 없어도 되고 알림도 뜨지 않는다.
    val appPath = remember(entries, criticalIndex, fallback) {
        buildMetricNotification(context, scenario, entries, criticalIndex, fallback, baseMillis)
    }
    val forced = remember(entries, criticalIndex) {
        buildForcedMetricNotification(context, scenario, entries, criticalIndex)
    }

    // 진행 중(ongoing) 알림이라 화면을 떠날 때 걷어 낸다
    DisposableEffect(Unit) {
        onDispose { NotificationManagerCompat.from(context).cancel(METRIC_STYLE_NOTIFICATION_ID) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { MetricConceptCard() }
        item {
            MetricDemoCard(
                scenario = scenario,
                entries = entries,
                criticalIndex = criticalIndex,
                fallback = fallback,
                baseMillis = baseMillis,
                onScenarioChange = {
                    scenario = it
                    criticalIndex = it.defaultCritical
                },
                onCriticalChange = { criticalIndex = it },
                onFallbackChange = { fallback = it }
            )
        }
        item { MetricExtrasCard(appPath = appPath, forced = forced) }
        item { CriticalIndexCard(entries = entries, criticalIndex = criticalIndex) }
        item { ValidationCard() }
        item { RoundTripCard(forced = forced) }
        item { MetricPitfallCard() }
    }
}

// ==================== 1. 개념 ====================

@Composable
private fun MetricConceptCard() {
    MetricSectionCard(title = "1. MetricStyle 은 무엇인가") {
        BodyText(
            "운동 기록·타이머·배달 ETA 처럼 시간에 따라 바뀌는 수치를 \"라벨 + 값\" 지표로 묶어 보여주는 " +
                "알림 스타일이다(androidx.core 1.19, 플랫폼은 API 37). 펼친 알림에는 지표가 최대 3개 보이고, " +
                "알림이 승격(promoted ongoing)되면 그중 critical 지표 하나가 상태 바 칩에 들어갈 수 있다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        KeyValueRow("값 타입", "쓰임새", isHeader = true)
        KeyValueRow("FixedInt", "걸음 수·심박 (값 + 단위)")
        KeyValueRow("FixedFloat", "거리 (소수 자릿수 min..max, 0..6)")
        KeyValueRow("FixedText", "페이스·게이트 (문자열 + 단위)")
        KeyValueRow("FixedDate", "출발일 (AUTOMATIC / LONG / SHORT)")
        KeyValueRow("FixedTime", "도착 예정 (시:분만 표시)")
        KeyValueRow("TimeDifference", "타이머·스톱워치 — 시스템이 실시간으로 갱신")

        Spacer(modifier = Modifier.height(10.dp))
        KeyValueRow("semantic style", "승격 알림에서 값을 칠하는 색", isHeader = true)
        SemanticLegendRow()

        Spacer(modifier = Modifier.height(12.dp))
        CaptionText(
            "ProgressStyle 과 결정적으로 다른 점: ProgressStyle 은 API 36 미만에서 단색 진행 막대로라도 축약되지만, " +
                "MetricStyle 은 API 37 미만에서 아무것도 그리지 않는다. 지표 대신 content text 가 보일 뿐이라 " +
                "폴백은 앱이 직접 만들어야 한다(2·3번 카드)."
        )
    }
}

@Composable
private fun SemanticLegendRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(
            NotificationCompat.SEMANTIC_STYLE_INFO,
            NotificationCompat.SEMANTIC_STYLE_SAFE,
            NotificationCompat.SEMANTIC_STYLE_CAUTION,
            NotificationCompat.SEMANTIC_STYLE_DANGER
        ).forEach { style ->
            Text(
                text = semanticNameOf(style),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = semanticColorOf(style)
            )
        }
    }
}

// ==================== 2. 데모 ====================

@Composable
private fun MetricDemoCard(
    scenario: MetricScenario,
    entries: List<MetricEntry>,
    criticalIndex: Int,
    fallback: Boolean,
    baseMillis: Long,
    onScenarioChange: (MetricScenario) -> Unit,
    onCriticalChange: (Int) -> Unit,
    onFallbackChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(hasNotificationPermission(context)) }
    var isPosted by remember { mutableStateOf(false) }
    var notifyCount by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    // POST_NOTIFICATIONS 는 이 예제 화면이 런타임에 요청하고, 거부 상태면 먼저 권한을 묻는다.
    // lint 는 그 흐름을 따라가지 못해 오탐을 낸다.
    @SuppressLint("MissingPermission")
    fun postNotification() {
        createMetricStyleChannel(context)
        NotificationManagerCompat.from(context).notify(
            METRIC_STYLE_NOTIFICATION_ID,
            buildMetricNotification(
                context, scenario, entries, criticalIndex, fallback,
                nowMillis = System.currentTimeMillis()
            )
        )
        notifyCount++
        isPosted = true
    }

    val criticalOptions = entries.indices.map { it to "$it ${entries[it].label}" } +
        listOf(
            METRIC_INDEX_NONE to "NONE(-1)",
            OUT_OF_RANGE_INDEX to "$OUT_OF_RANGE_INDEX (범위 밖)"
        )

    MetricSectionCard(title = "2. 지표 알림 만들어 발행하기") {
        ChipRow {
            MetricScenario.entries.forEach { option ->
                FilterChip(
                    selected = option == scenario,
                    onClick = { onScenarioChange(option) },
                    label = { Text(option.title, fontSize = 12.sp) }
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        CaptionText("critical 지표 인덱스 (setCriticalMetric)")
        ChipRow {
            criticalOptions.forEach { (index, label) ->
                FilterChip(
                    selected = index == criticalIndex,
                    onClick = { onCriticalChange(index) },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        MetricPreview(entries = entries, criticalIndex = criticalIndex, nowMillis = baseMillis)

        Spacer(modifier = Modifier.height(10.dp))
        OptionSwitch(
            label = "하위 버전 폴백 (content text + 크로노미터)",
            checked = fallback,
            onCheckedChange = onFallbackChange
        )
        CaptionText("폴백 문구: " + if (fallback) fallbackTextOf(entries, baseMillis) else "(없음 — 제목만 남는다)")

        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DemoButton(
                text = if (isPosted) "알림 갱신" else "알림 발행",
                color = Color(0xFF1976D2)
            ) {
                if (hasPermission) {
                    postNotification()
                } else {
                    // InlinedApi 억제: hasPermission 이 API 33 미만에서 항상 true 라 이 줄은 33+ 에서만 실행된다
                    @Suppress("InlinedApi")
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            DemoButton(text = "알림 제거", color = Color(0xFF757575)) {
                NotificationManagerCompat.from(context).cancel(METRIC_STYLE_NOTIFICATION_ID)
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                    "MetricStyle 을 붙여 발행한다 — 지표가 시스템 UI 로 렌더된다."
                } else {
                    "MetricStyle 을 붙이지 않고(버전 분기) 폴백만 발행한다. 폴백을 끄면 알림에 제목만 남는다."
                }
        )
    }
}

/** 알림이 아니라 화면 안에서 지표 구성을 눈으로 보기 위한 재현 — 실제 시스템 렌더가 아니다 */
@Composable
private fun MetricPreview(entries: List<MetricEntry>, criticalIndex: Int, nowMillis: Long) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        CaptionText("화면 안 재현(펼친 알림 · 최대 3개)")
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            entries.take(3).forEachIndexed { index, entry ->
                MetricTile(
                    entry = entry,
                    isCritical = index == criticalIndex,
                    nowMillis = nowMillis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        entries.drop(3).forEachIndexed { offset, entry ->
            Spacer(modifier = Modifier.height(6.dp))
            CaptionText(
                "${offset + 3}번 \"${entry.label}\" — 펼친 알림에는 보이지 않지만 compat 은 자르지 않고 extras 에 싣는다"
            )
        }
    }
}

@Composable
private fun MetricTile(
    entry: MetricEntry,
    isCritical: Boolean,
    nowMillis: Long,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                if (isCritical) Color(0xFFFFF8E1) else Color(0xFFF5F5F5),
                RoundedCornerShape(6.dp)
            )
            .padding(8.dp)
    ) {
        Text(
            text = (if (isCritical) "★ " else "") + entry.label,
            fontSize = 11.sp,
            color = Color(0xFF616161)
        )
        Text(
            text = entry.source.displayText(nowMillis),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = semanticColorOf(entry.semanticStyle)
        )
        Text(
            text = semanticNameOf(entry.semanticStyle),
            fontSize = 9.sp,
            color = Color(0xFF9E9E9E)
        )
    }
}

// ==================== 3. extras 실측 ====================

@Composable
private fun MetricExtrasCard(appPath: Notification, forced: Notification) {
    val appRows = remember(appPath) { metricExtrasOf(appPath) }
    val forcedRows = remember(forced) { metricExtrasOf(forced) }
    val metricLines = remember(forced) { metricBundlesOf(forced)?.map(::describeMetricBundle).orEmpty() }

    MetricSectionCard(title = "3. 빌드된 extras — 앱 경로 vs 강제 부착 (이 기기 실측)") {
        BodyText(
            "왼쪽은 버전 분기를 지킨 앱 경로, 오른쪽은 분기 없이 MetricStyle 을 붙인 측정용 빌드다. " +
                "MetricStyle.apply() 는 SDK_INT ≥ 37 일 때만 플랫폼 스타일을 만들고 그 아래에서는 아무것도 하지 " +
                "않는다 — ProgressStyle 의 setProgress() 같은 축약 경로가 없다. 대신 addCompatExtras() 가 37 미만에서만 " +
                "지표를 extras 에 싣는다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        CompareRow("키", "앱 경로", "강제 부착", isHeader = true)
        appRows.zip(forcedRows).forEach { (app, forcedRow) ->
            CompareRow(app.first, app.second, forcedRow.second)
        }

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText("강제 부착 알림의 android.metrics 원소")
        if (metricLines.isEmpty()) {
            CaptionText("없음 — API 37 이상에서는 compat 이 extras 에 싣지 않는다(플랫폼 스타일이 대신 들고 있다)")
        } else {
            metricLines.forEachIndexed { index, line -> CodeText("[$index] $line") }
        }

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "강제 부착해도 37 미만에서 실제로 그려지는 것은 앱 경로와 같다(제목 + content text). 지표는 extras 에만 " +
                "남고 그것을 그려 줄 시스템이 없다. lint 를 지키면(앱 경로) 37 미만에서는 MetricStyle 객체 자체가 " +
                "만들어지지 않으므로 이 extras 도 생기지 않는다."
        )
    }
}

// ==================== 4. critical 인덱스 ====================

@Composable
private fun CriticalIndexCard(entries: List<MetricEntry>, criticalIndex: Int) {
    val mapping = remember(entries, criticalIndex) { criticalMappingOf(entries, criticalIndex) }
    // 첫 지표와 똑같은 지표를 끝에 하나 더 붙이고 그것을 critical 로 지정한다
    val duplicated = remember(entries) { entries + entries.first() }
    val duplicateMapping = remember(duplicated) { criticalMappingOf(duplicated, duplicated.lastIndex) }

    MetricSectionCard(title = "4. critical 지표 인덱스는 어떻게 넘어가는가") {
        BodyText(
            "setCriticalMetric(index) 는 범위를 검사하지 않고 정수를 그대로 저장한다. 37 미만 extras 에도 그 정수가 " +
                "그대로 실린다. 반면 API 37 경로(Api37Impl)는 저장된 정수 대신 " +
                "getMetrics().indexOf(getCriticalMetric()) 를 플랫폼에 넘긴다. 아래 값은 같은 식을 이 기기에서 계산한 것이다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        KeyValueRow("요청 인덱스", mapping.requested.toString())
        KeyValueRow("getCriticalMetric()", mapping.criticalLabel ?: "null")
        KeyValueRow("플랫폼에 넘길 값", mapping.platformIndex.toString())

        Spacer(modifier = Modifier.height(10.dp))
        KeyValueRow("같은 지표 두 개", "${duplicated.size}개 중 마지막(${duplicated.lastIndex})을 critical 로", isHeader = true)
        KeyValueRow("getCriticalMetric()", duplicateMapping.criticalLabel ?: "null")
        KeyValueRow("플랫폼에 넘길 값", duplicateMapping.platformIndex.toString())

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "범위 밖 인덱스(-1, 5)는 getCriticalMetric() 이 null 이 되고 indexOf(null) 이 -1(METRIC_INDEX_NONE)이 된다. " +
                "같은 값·라벨·semantic 의 지표가 둘이면 Metric.equals 가 같다고 보므로 indexOf 가 첫 번째를 돌려준다 — " +
                "의도와 다른 지표가 칩 후보로 바뀐다. 기본값은 0(첫 지표)이다."
        )
    }
}

// ==================== 5. 생성 시점 검증 ====================

@Composable
private fun ValidationCard() {
    val context = LocalContext.current
    val cases = remember { runValidationCases(context) }

    MetricSectionCard(title = "5. 생성·빌드 시점에 무엇을 검사하는가 (이 기기 실측)") {
        BodyText(
            "값이 잘못되면 알림을 띄울 때가 아니라 객체를 만들거나 build() 할 때 IllegalArgumentException 이 난다. " +
                "지표 0개 검사는 SDK_INT 분기보다 앞에 있어서, 아무것도 그리지 않는 API 37 미만에서도 build() 가 실패한다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        cases.forEach { case ->
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                Text(
                    text = (if (case.threw) "❌ " else "✅ ") + case.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (case.threw) Color(0xFFC62828) else Color(0xFF2E7D32)
                )
                CaptionText(case.result)
            }
        }

        CaptionText(
            "FixedTime 은 생성자에서 초 단위로 자른다(truncatedTo(SECONDS)) — 화면에는 시:분만 나온다. " +
                "TimeDifference 의 zeroTime 은 extras 에 epoch ms 로 저장돼 ms 아래가 사라진다."
        )
        CaptionText(
            "lint 가 컴파일 시점에 잡는 것은 자릿수 7(Range)과 format 0(WrongConstant) 두 가지뿐이다(이 화면은 측정을 위해 " +
                "억제했다). min 3 > max 1 같은 인자 사이 관계, 공백 label, 범위 밖 critical 인덱스는 lint 를 통과하고 " +
                "실행 시점에야 드러나거나 끝까지 드러나지 않는다."
        )
    }
}

// ==================== 6. 왕복 ====================

@Composable
private fun RoundTripCard(forced: Notification) {
    val context = LocalContext.current
    val roundTrip = remember(forced) { roundTripOf(context, forced) }

    MetricSectionCard(title = "6. 기존 알림에서 Builder 되살리기 (왕복)") {
        BodyText(
            "NotificationCompat.Builder(context, notification) 는 이미 발행한 알림을 고칠 때 쓰는 공개 생성자다. " +
                "내부에서 COMPAT_TEMPLATE 으로 MetricStyle 을 만들고 extras 에서 지표를 되살린다. 3번 카드의 " +
                "강제 부착 알림을 이 생성자로 되살려 다시 build() 한 결과다."
        )
        Spacer(modifier = Modifier.height(10.dp))

        CompareRow("항목", "원본", "되살린 뒤", isHeader = true)
        CompareRow(
            "android.metrics",
            roundTrip.beforeCount?.let { "$it 개" } ?: "없음",
            roundTrip.afterCount?.let { "$it 개" } ?: "없음"
        )
        CompareRow("criticalIndex", roundTrip.beforeCritical, roundTrip.afterCritical)
        Spacer(modifier = Modifier.height(6.dp))
        KeyValueRow("원소까지 동일", roundTrip.identical.toString())

        Spacer(modifier = Modifier.height(10.dp))
        CaptionText(
            "37 미만에서 그리지도 않을 지표를 extras 에 싣는 이유가 이것이다 — 알림을 갱신하려고 Builder 를 " +
                "되살릴 때 스타일을 복원할 근거가 extras 밖에 없다. 범위 밖 critical 인덱스(5)도 그대로 왕복한다."
        )
    }
}

// ==================== 7. 함정 정리 ====================

@Composable
private fun MetricPitfallCard() {
    MetricSectionCard(title = "7. 실기기에서 걸리는 것들") {
        PitfallRow(
            "37 미만에는 렌더 폴백이 없다",
            "apply() 가 아무것도 하지 않아 지표가 통째로 사라진다. content text 와 크로노미터" +
                "(setUsesChronometer · setChronometerCountDown) 로 앱이 직접 대신해야 한다."
        )
        PitfallRow(
            "클래스는 API 26, 생성자는 API 37",
            "MetricStyle·Metric 은 java.time 때문에 @RequiresApi(26) 이지만 MetricStyle() 생성자와 " +
                "TimeDifference 팩토리는 @RequiresApi(37) 이다. lint 를 지키면 37 분기가 강제된다."
        )
        PitfallRow(
            "지표 0개는 build() 에서 터진다",
            "\"A MetricStyle must have at least one Metric\" — 37 미만에서도 같다. 지표 목록이 비는 상황을 미리 걸러야 한다."
        )
        PitfallRow(
            "critical 인덱스는 검사되지 않는다",
            "범위 밖이면 조용히 null(-1) 이 되고, 같은 지표가 둘이면 indexOf 가 첫 번째로 바꾼다."
        )
        PitfallRow(
            "최대 3개는 표시 한도다",
            "compat 은 개수를 자르지 않는다. 4번째부터는 extras 에만 남는다."
        )
        PitfallRow(
            "시각의 정밀도",
            "FixedTime 은 초 단위로 잘리고 시:분만 보인다. TimeDifference 의 zeroTime 은 ms 로 저장된다."
        )
        PitfallRow(
            "semantic style 과 칩은 승격 알림에서만",
            "색(INFO/SAFE/CAUTION/DANGER)과 상태 바 칩은 FLAG_PROMOTED_ONGOING 알림에만 적용된다. " +
                "setRequestPromotedOngoing(true) 는 요청일 뿐이다."
        )
        PitfallRow(
            "⚠️ API 37 실기기",
            "지표의 실제 렌더·칩·semantic 색은 API 37 기기에서만 확인할 수 있다. 그 미만에서는 3·5·6번 카드의 " +
                "compat 경로만 검증 가능하다."
        )
    }
}

// ==================== 공통 요소 ====================

private fun semanticColorOf(style: Int): Color = when (style) {
    NotificationCompat.SEMANTIC_STYLE_INFO -> Color(0xFF1E88E5)
    NotificationCompat.SEMANTIC_STYLE_SAFE -> Color(0xFF43A047)
    NotificationCompat.SEMANTIC_STYLE_CAUTION -> Color(0xFFFB8C00)
    NotificationCompat.SEMANTIC_STYLE_DANGER -> Color(0xFFE53935)
    else -> Color(0xFF212121)
}

private fun semanticNameOf(style: Int): String = when (style) {
    NotificationCompat.SEMANTIC_STYLE_INFO -> "INFO"
    NotificationCompat.SEMANTIC_STYLE_SAFE -> "SAFE"
    NotificationCompat.SEMANTIC_STYLE_CAUTION -> "CAUTION"
    NotificationCompat.SEMANTIC_STYLE_DANGER -> "DANGER"
    else -> "UNSPECIFIED"
}

@Composable
private fun MetricSectionCard(
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
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        content()
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
            .padding(vertical = 2.dp)
            .background(Color(0xFFECEFF1), RoundedCornerShape(6.dp))
            .padding(8.dp),
        fontSize = 10.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF37474F),
        lineHeight = 14.sp
    )
}

@Composable
private fun KeyValueRow(
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
            modifier = Modifier.width(130.dp),
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
private fun CompareRow(
    key: String,
    left: String,
    right: String,
    isHeader: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isHeader) Color(0xFFEEEEEE) else Color.Transparent)
            .padding(vertical = 5.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = key,
            modifier = Modifier.width(110.dp),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
            color = Color(0xFF424242)
        )
        listOf(left, right).forEach { value ->
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                fontSize = 11.sp,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                color = Color(0xFF616161)
            )
        }
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
