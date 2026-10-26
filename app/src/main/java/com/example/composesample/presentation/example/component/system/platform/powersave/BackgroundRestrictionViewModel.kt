package com.example.composesample.presentation.example.component.system.platform.powersave

import android.app.ActivityManager
import android.app.Application
import android.app.ApplicationStartInfo
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.os.SystemClock
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 절전 모드 예제 '백그라운드 제한 진단' 탭의 상태 읽기부.
 *
 * 탭에 들어오면([startWatching]) 배터리 세이버·Doze·가벼운 Doze·저전력 대기 변경 브로드캐스트를 받기 시작하고,
 * 받을 때마다·화면으로 돌아올 때마다 모든 값을 다시 읽어 [diagnose] 로 판정한다. 탭을 벗어나면([stopWatching]) 수신을 멈춘다 —
 * ViewModel 은 BlogExampleActivity 범위라 onCleared 만 믿으면 목록으로 돌아가도 계속 받는다.
 */
class BackgroundRestrictionViewModel(application: Application) : AndroidViewModel(application) {

    data class TimelineEntry(val at: Long, val trigger: String, val summary: String)

    data class UiState(
        val snapshot: RestrictionSnapshot? = null,
        val findings: List<Finding> = emptyList(),
        val timeline: List<TimelineEntry> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 프레임워크 클래스 상속은 이름 있는 클래스로 둔다 — 컴포저블 안 익명 객체는 debug(HotSwan)에서 콜백 순간 깨진 전례가 있다
    private val receiver = PowerStateReceiver()
    private var watching = false

    /** 받을 액션 → 기록에 남길 이름. API 33 액션은 그 레벨에서만 넣는다 */
    private val actionLabels: Map<String, String> = buildMap {
        put(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED, "배터리 세이버 변경")
        put(PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED, "Doze 변경")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            put(PowerManager.ACTION_DEVICE_LIGHT_IDLE_MODE_CHANGED, "가벼운 Doze 변경")
            put(PowerManager.ACTION_LOW_POWER_STANDBY_ENABLED_CHANGED, "저전력 대기 변경")
        }
    }

    fun startWatching() {
        if (watching) return
        val filter = IntentFilter().apply { actionLabels.keys.forEach { addAction(it) } }
        // 시스템이 보내는 브로드캐스트만 받으므로 NOT_EXPORTED 로 충분하다
        ContextCompat.registerReceiver(getApplication(), receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        watching = true
    }

    fun stopWatching() {
        if (!watching) return
        runCatching { getApplication<Application>().unregisterReceiver(receiver) }
        watching = false
    }

    /** 모든 값을 다시 읽어 판정하고 기록 맨 위에 남긴다 */
    fun refresh(trigger: String) {
        val snapshot = readSnapshot()
        val findings = diagnose(snapshot)
        _uiState.update { state ->
            val entry = TimelineEntry(snapshot.readAt, trigger, summarize(snapshot))
            state.copy(
                snapshot = snapshot,
                findings = findings,
                timeline = (listOf(entry) + state.timeline).take(MAX_TIMELINE)
            )
        }
    }

    override fun onCleared() {
        stopWatching()
    }

    private inner class PowerStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            refresh("브로드캐스트: ${actionLabels[intent.action] ?: intent.action}")
        }
    }

    private fun readSnapshot(): RestrictionSnapshot {
        val app = getApplication<Application>()
        val powerManager = app.getSystemService(PowerManager::class.java)
        val activityManager = app.getSystemService(ActivityManager::class.java)
        val packageName = app.packageName
        return RestrictionSnapshot(
            sdkInt = Build.VERSION.SDK_INT,
            readAt = System.currentTimeMillis(),
            readUptimeMillis = SystemClock.uptimeMillis(),
            processStartUptimeMillis = Process.getStartUptimeMillis(),
            powerSave = powerManager.isPowerSaveMode,
            ignoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName),
            deviceIdle = powerManager.isDeviceIdleMode,
            lightIdle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) powerManager.isDeviceLightIdleMode else null,
            standbyBucket = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) readBucket(app) else null,
            backgroundRestricted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) activityManager.isBackgroundRestricted else null,
            lowPowerStandby = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) powerManager.isLowPowerStandbyEnabled else null,
            lowPowerStandbyExempt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                powerManager.isExemptFromLowPowerStandby
            } else null,
            exits = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) readExits(activityManager, packageName) else null,
            starts = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) readStarts(activityManager) else null,
            bucketHistory = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) readBucketHistory(app) else null
        )
    }

    /** 자기 앱의 버킷은 권한 없이 읽는다 */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun readBucket(app: Application): Int =
        app.getSystemService(UsageStatsManager::class.java).appStandbyBucket

    /** 자기 패키지의 모든 프로세스(:remote 등 포함) 종료 기록, 최신 순 */
    @RequiresApi(Build.VERSION_CODES.R)
    private fun readExits(activityManager: ActivityManager, packageName: String): List<ExitRecord> =
        activityManager.getHistoricalProcessExitReasons(packageName, 0, MAX_PROCESS_HISTORY).map {
            ExitRecord(it.reason, it.timestamp, it.processName, it.importance, it.description)
        }

    /** 시작 기록과 그 시각(부팅 기준 단조 시계 ns → ms) — 이번 실행의 것인지는 [currentStart] 가 시각으로 가린다 */
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun readStarts(activityManager: ActivityManager): List<StartRecord> =
        activityManager.getHistoricalProcessStartReasons(MAX_PROCESS_HISTORY).map {
            StartRecord(
                reason = it.reason,
                startType = it.startType,
                wasForceStopped = it.wasForceStopped(),
                launchUptimeMillis = it.startupTimestamps[ApplicationStartInfo.START_TIMESTAMP_LAUNCH]?.div(NANOS_PER_MILLI),
                forkUptimeMillis = it.startupTimestamps[ApplicationStartInfo.START_TIMESTAMP_FORK]?.div(NANOS_PER_MILLI)
            )
        }

    /**
     * 최근 24시간의 버킷 변경 — 앱이 화면에 있는 동안 읽는 현재 버킷은 대개 ACTIVE 라,
     * 백그라운드에 있던 동안의 버킷은 이 이력으로만 보인다. 기기 잠금 해제 전(Android 11+)에는 null.
     */
    @RequiresApi(Build.VERSION_CODES.P)
    private fun readBucketHistory(app: Application): List<BucketChange>? {
        val now = System.currentTimeMillis()
        val events = app.getSystemService(UsageStatsManager::class.java)
            .queryEventsForSelf(now - HISTORY_WINDOW_MILLIS, now) ?: return null
        val changes = mutableListOf<BucketChange>()
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.STANDBY_BUCKET_CHANGED) {
                changes += BucketChange(event.timeStamp, event.appStandbyBucket)
            }
        }
        return changes.asReversed().take(MAX_BUCKET_HISTORY)
    }

    private fun summarize(s: RestrictionSnapshot): String = listOfNotNull(
        s.standbyBucket?.let { "버킷 ${StandbyBucket.label(it)}" },
        s.backgroundRestricted?.let { "제한 $it" },
        "Doze ${s.deviceIdle}",
        s.lightIdle?.let { "가벼운 Doze $it" },
        "세이버 ${s.powerSave}",
        "최적화 예외 ${s.ignoringBatteryOptimizations}"
    ).joinToString(" · ")

    companion object {
        private const val MAX_TIMELINE = 30
        private const val MAX_PROCESS_HISTORY = 5
        private const val MAX_BUCKET_HISTORY = 12
        private const val HISTORY_WINDOW_MILLIS = 24 * 60 * 60 * 1000L
        private const val NANOS_PER_MILLI = 1_000_000L
    }
}
