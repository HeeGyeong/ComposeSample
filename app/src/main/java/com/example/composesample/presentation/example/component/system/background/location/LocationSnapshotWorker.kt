package com.example.composesample.presentation.example.component.system.background.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * 주기적으로 위치를 한 번씩만 찍는 Worker — Foreground Service 의 대조군.
 *
 * [LocationTrackingService] 가 "끊기면 안 되는 지속 추적"을 맡는다면, 이쪽은
 * "가끔 한 번이면 충분하고, 조금 늦어도 되는 스냅샷"을 맡는다. 두 축의 경계가
 * 이 예제의 핵심이므로, 여기서는 의도적으로 스트림이 아닌 단발 조회
 * ([LocationManager.getLastKnownLocation])만 사용한다.
 *
 * ### 여기서 setForeground()/ForegroundInfo 를 쓰지 않는 이유
 * WorkManager 의 expedited work 는 API 30 이하에서 WorkManager 자신의
 * `androidx.work.impl.foreground.SystemForegroundService` 를 통해 포그라운드로 승격된다.
 * 그런데 work-runtime 2.9.1 의 매니페스트는 이 서비스에 `foregroundServiceType` 을
 * 선언하지 않는다(AAR 매니페스트 확인). 따라서 `ForegroundInfo(id, notification, 위치타입)` 을
 * 넘기려면 앱 매니페스트에서 라이브러리 서비스 선언을 override 해야 한다.
 * → **위치 타입 FGS 가 필요하면 WorkManager 를 우회하지 말고 직접 Service 를 만드는 편이 낫다**는
 * 것이 이 예제가 서비스와 Worker 를 나눠 놓은 이유다.
 */
class LocationSnapshotWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val trigger = inputData.getString(KEY_TRIGGER) ?: "unknown"

        if (!hasLocationPermission(context)) {
            addEvent("[$trigger] 위치 권한 없음 → Result.failure()")
            return Result.failure(workDataOf(KEY_ERROR to "permission_denied"))
        }

        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager == null) {
            addEvent("[$trigger] LocationManager 없음 → Result.failure()")
            return Result.failure(workDataOf(KEY_ERROR to "no_location_manager"))
        }

        val location = PROVIDERS
            .filter { provider -> runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false) }
            .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
            .maxByOrNull { it.time }

        if (location == null) {
            // 실패가 아니라 "이번엔 찍을 게 없었다"에 가깝다. retry 로 두면 백오프가 계속 쌓이므로
            // 성공으로 종료하고 결과만 비워 둔다.
            addEvent("[$trigger] 마지막 알려진 위치 없음 → Result.success(빈 결과)")
            _snapshotCount.value += 1
            return Result.success(workDataOf(KEY_ERROR to "no_last_known_location"))
        }

        _lastSnapshot.value = LocationSnapshot(
            latitude = location.latitude,
            longitude = location.longitude,
            provider = location.provider ?: "unknown",
            trigger = trigger,
            takenAt = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        )
        _snapshotCount.value += 1
        addEvent(
            "[$trigger] 스냅샷 ${formatCoordinate(location.latitude)}, " +
                    "${formatCoordinate(location.longitude)} (${location.provider})"
        )

        return Result.success(
            workDataOf(
                KEY_LATITUDE to location.latitude,
                KEY_LONGITUDE to location.longitude
            )
        )
    }

    private fun hasLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    companion object {
        const val KEY_TRIGGER = "trigger"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_ERROR = "error"

        private const val ONE_TIME_WORK_NAME = "location_snapshot_once"
        private const val PERIODIC_WORK_NAME = "location_snapshot_periodic"

        /**
         * PeriodicWorkRequest 가 허용하는 최소 주기.
         * 15분보다 짧게 지정해도 시스템이 15분으로 올려 버린다 — 지속 추적을 Worker 로
         * 흉내 낼 수 없는 가장 직접적인 이유.
         */
        const val MIN_PERIODIC_MINUTES = 15L

        private const val MAX_EVENTS = 8

        private val PROVIDERS = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )

        private val _lastSnapshot = MutableStateFlow<LocationSnapshot?>(null)
        val lastSnapshot: StateFlow<LocationSnapshot?> = _lastSnapshot.asStateFlow()

        private val _snapshotCount = MutableStateFlow(0)
        val snapshotCount: StateFlow<Int> = _snapshotCount.asStateFlow()

        private val _events = MutableStateFlow<List<String>>(emptyList())
        val events: StateFlow<List<String>> = _events.asStateFlow()

        /** 지금 즉시 한 번 — 버튼 반응을 눈으로 확인하는 용도 */
        fun enqueueOnce(context: Context) {
            val request = OneTimeWorkRequestBuilder<LocationSnapshotWorker>()
                .setInputData(workDataOf(KEY_TRIGGER to "one-time"))
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(ONE_TIME_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
            addEvent("OneTimeWorkRequest enqueue")
        }

        /**
         * 15분 주기 예약.
         *
         * Constraints 로 "배터리가 부족하지 않을 때만" 실행하도록 제한한다.
         * 이런 실행 조건 지정은 Service 로는 직접 만들 수 없는 WorkManager 의 강점이다.
         */
        fun enqueuePeriodic(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
            val request = PeriodicWorkRequestBuilder<LocationSnapshotWorker>(
                MIN_PERIODIC_MINUTES,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_TRIGGER to "periodic"))
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
            addEvent("PeriodicWorkRequest 예약 (${MIN_PERIODIC_MINUTES}분 주기, 배터리 부족 시 제외)")
        }

        fun cancelPeriodic(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
            addEvent("PeriodicWorkRequest 취소")
        }

        fun clearEvents() {
            _events.value = emptyList()
        }

        private fun addEvent(message: String) {
            val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            _events.value = (listOf("$stamp  $message") + _events.value).take(MAX_EVENTS)
        }
    }
}

/** Worker 가 남긴 마지막 스냅샷 */
data class LocationSnapshot(
    val latitude: Double,
    val longitude: Double,
    val provider: String,
    /** one-time / periodic — 어떤 요청으로 실행됐는지 */
    val trigger: String,
    val takenAt: String,
)
