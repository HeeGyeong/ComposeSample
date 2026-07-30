package com.example.composesample.presentation.example.component.system.background.location

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.composesample.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** 화면에 표시할 서비스 상태 스냅샷 */
data class LocationTrackingState(
    val isRunning: Boolean = false,
    /** 서비스가 살아있는 동안 1초마다 증가 — 앱을 내려도 계속 도는지 확인하는 지표 */
    val elapsedSeconds: Int = 0,
    /** LocationListener 콜백 수신 횟수 */
    val updateCount: Int = 0,
    /** 알림을 다시 그린 횟수 */
    val notifyCount: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null,
    val provider: String? = null,
    /** 첫 fix 이전에 getLastKnownLocation 으로 채운 값인지 여부 */
    val isLastKnown: Boolean = false,
)

/**
 * 위치를 지속 수집하는 Foreground Service.
 *
 * 이 프로젝트의 WorkManager 예제가 "지연 가능한 작업"을 다룬다면, 여기서는 그 반대 축인
 * **사용자가 앱을 떠나도 끊기면 안 되는 지속 작업**을 다룬다. 지속 작업은 WorkManager 로
 * 흉내 낼 수 없다 — PeriodicWorkRequest 의 최소 주기가 15분이고, 그마저도 시스템이
 * 배터리 상태에 따라 미루기 때문이다.
 *
 * 상태 공유는 [TimerTileService][com.example.composesample.presentation.example.component.system.platform.quicksettings.TimerTileService]
 * 와 동일하게 companion 의 StateFlow 를 쓴다. 서비스는 UI 와 생명주기가 완전히 분리돼 있어
 * (화면이 없어도 살아있음) ViewModel 로는 상태를 들고 있을 수 없다.
 */
class LocationTrackingService : Service() {

    /** 서비스 인스턴스 수명과 묶인 스코프 — onDestroy 에서 반드시 취소한다 */
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var tickJob: Job? = null
    private var locationManager: LocationManager? = null
    private var startedAtMs = 0L

    /**
     * SAM 변환(`LocationListener { ... }`)을 쓰지 않고 익명 객체로 4개 메서드를 모두 구현한다.
     * API 29 이하 단말에서는 나머지 3개가 추상 메서드라, onLocationChanged 만 구현된 객체를
     * 넘기면 시스템이 콜백을 부르는 순간 AbstractMethodError 가 난다(minSdk 24 호환).
     */
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            onLocationUpdated(location, isLastKnown = false)
        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

        override fun onProviderEnabled(provider: String) {
            addEvent("provider 활성화: $provider")
        }

        override fun onProviderDisabled(provider: String) {
            addEvent("provider 비활성화: $provider")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // intent 가 null 이면 START_STICKY 로 시스템이 서비스를 되살린 경우다.
        // 이때는 원래 하던 일(추적)을 그대로 다시 시작한다.
        if (intent == null) {
            addEvent("intent == null — 시스템이 START_STICKY 로 서비스를 재생성")
        }

        return if (intent?.action == ACTION_STOP) {
            stopTracking()
            // 사용자가 명시적으로 끈 것이므로 되살리지 않는다
            START_NOT_STICKY
        } else {
            startTracking()
            START_STICKY
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        tickJob = null
        locationManager?.removeUpdates(locationListener)
        locationManager = null
        serviceScope.cancel()
        _state.value = _state.value.copy(isRunning = false)
        addEvent("onDestroy — 리스너 해제 + 스코프 취소")
    }

    // ==================== 추적 시작 / 중지 ====================

    private fun startTracking() {
        if (_state.value.isRunning) {
            addEvent("이미 실행 중 — onStartCommand 만 다시 호출됨")
            return
        }

        startedAtMs = System.currentTimeMillis()
        _state.value = LocationTrackingState(isRunning = true)
        addEvent("onStartCommand 진입")

        // Android 14(API 34)+ 는 location 타입 FGS 를 시작하는 시점에 위치 런타임 권한이
        // 반드시 granted 여야 한다. 없으면 startForeground 가 SecurityException 을 던진다.
        if (!hasLocationPermission()) {
            addEvent("위치 권한 없음 → 서비스 자체 종료")
            _state.value = LocationTrackingState(isRunning = false)
            stopSelf()
            return
        }

        createNotificationChannel()

        // startForegroundService() 로 시작했다면 5초 안에 반드시 startForeground() 를 불러야 한다.
        // 안 부르면 시스템이 ForegroundServiceDidNotStartInTimeException 으로 프로세스를 죽인다.
        // → 무거운 초기화(권한 확인·채널 생성 제외)는 전부 startForeground 이후로 미룬다.
        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(_state.value),
                foregroundServiceType()
            )
            _state.value = _state.value.copy(notifyCount = 1)
            addEvent("startForeground 성공 (type=location)")
        } catch (e: Exception) {
            // API 31+ 백그라운드에서 시작 시 ForegroundServiceStartNotAllowedException,
            // API 34+ 권한 미보유 시 SecurityException 이 여기로 온다.
            Log.e(TAG, "startForeground 실패", e)
            addEvent("startForeground 실패: ${e.javaClass.simpleName}")
            _state.value = LocationTrackingState(isRunning = false)
            stopSelf()
            return
        }

        startLocationUpdates()
        startTicker()
    }

    private fun stopTracking() {
        addEvent("중지 요청 → stopForeground(REMOVE) + stopSelf()")
        tickJob?.cancel()
        tickJob = null
        locationManager?.removeUpdates(locationListener)
        locationManager = null
        _state.value = _state.value.copy(isRunning = false)

        // STOP_FOREGROUND_REMOVE: 알림도 같이 제거. DETACH 를 쓰면 알림만 남는다.
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ==================== 위치 수신 ====================

    private fun startLocationUpdates() {
        val manager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (manager == null) {
            addEvent("LocationManager 를 가져오지 못했습니다")
            return
        }
        locationManager = manager

        val enabled = PROVIDERS.filter { provider ->
            runCatching { manager.isProviderEnabled(provider) }.getOrDefault(false)
        }
        if (enabled.isEmpty()) {
            addEvent("사용 가능한 provider 없음 (GPS/네트워크 위치가 꺼져 있음)")
            return
        }

        // 권한 확인을 이 함수 안에서 한 번 더 수행 — 호출 시점과 권한 상태가 어긋날 수 있고,
        // 정적 분석기도 requestLocationUpdates 직전의 검사를 근거로 삼는다.
        if (!hasLocationPermission()) {
            addEvent("위치 권한이 회수됨 → 업데이트 요청 생략")
            return
        }

        enabled.forEach { provider ->
            manager.requestLocationUpdates(
                provider,
                MIN_UPDATE_INTERVAL_MS,
                MIN_UPDATE_DISTANCE_M,
                locationListener,
                Looper.getMainLooper()
            )
        }
        addEvent("업데이트 요청: ${enabled.joinToString()} (최소 ${MIN_UPDATE_INTERVAL_MS / 1000}초 / ${MIN_UPDATE_DISTANCE_M.toInt()}m)")

        // 첫 fix 는 실외에서도 수 초~수십 초가 걸린다. 그동안 화면이 비어 보이지 않도록
        // 마지막으로 알려진 위치로 초기값을 채우되, 실측이 아님을 상태에 표시한다.
        val lastKnown = enabled.mapNotNull { provider ->
            runCatching { manager.getLastKnownLocation(provider) }.getOrNull()
        }.maxByOrNull { it.time }

        if (lastKnown != null) {
            onLocationUpdated(lastKnown, isLastKnown = true)
        } else {
            addEvent("getLastKnownLocation 결과 없음 — 첫 fix 를 기다립니다")
        }
    }

    private fun onLocationUpdated(location: Location, isLastKnown: Boolean) {
        val current = _state.value
        _state.value = current.copy(
            updateCount = if (isLastKnown) current.updateCount else current.updateCount + 1,
            latitude = location.latitude,
            longitude = location.longitude,
            accuracy = if (location.hasAccuracy()) location.accuracy else null,
            provider = location.provider,
            isLastKnown = isLastKnown
        )
        addEvent(
            if (isLastKnown) {
                "마지막 알려진 위치로 초기화 (${location.provider})"
            } else {
                "위치 수신 (${location.provider}) ${formatCoordinate(location.latitude)}, ${
                    formatCoordinate(location.longitude)
                }"
            }
        )
        updateNotification()
    }

    // ==================== 경과 시간 / 알림 ====================

    private fun startTicker() {
        tickJob = serviceScope.launch {
            while (true) {
                delay(1000L)
                val elapsed = ((System.currentTimeMillis() - startedAtMs) / 1000L).toInt()
                _state.value = _state.value.copy(elapsedSeconds = elapsed)

                // 알림은 매초가 아니라 일정 간격으로만 갱신한다.
                // 초당 갱신하면 시스템이 알림 업데이트를 스로틀링하고 배터리도 낭비된다.
                if (elapsed % NOTIFY_INTERVAL_SECONDS == 0) {
                    updateNotification()
                }
            }
        }
    }

    private fun updateNotification() {
        // API 33+ 에서 POST_NOTIFICATIONS 가 없으면 알림은 보이지 않는다.
        // 단, 서비스 자체는 정상 동작한다 — 알림 권한과 FGS 실행 가능 여부는 별개다.
        if (!hasNotificationPermission()) return

        val state = _state.value.copy(notifyCount = _state.value.notifyCount + 1)
        _state.value = state
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun createNotificationChannel() {
        // NotificationChannelCompat 은 API 26 미만에서 no-op 이므로 버전 분기가 필요 없다.
        val channel = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_LOW
        )
            .setName("위치 추적")
            .setDescription("Foreground Service 예제가 사용하는 알림 채널")
            .setShowBadge(false)
            .build()
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
    }

    private fun buildNotification(state: LocationTrackingState): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val contentIntent = launchIntent?.let {
            PendingIntent.getActivity(
                this,
                0,
                it,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, LocationTrackingService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val coordinate = if (state.latitude != null && state.longitude != null) {
            "${formatCoordinate(state.latitude)}, ${formatCoordinate(state.longitude)}"
        } else {
            "위치 수신 대기 중"
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("위치 추적 중 · ${formatElapsed(state.elapsedSeconds)}")
            .setContentText("$coordinate (수신 ${state.updateCount}회)")
            .setContentIntent(contentIntent)
            .addAction(R.drawable.ic_launcher_foreground, "중지", stopIntent)
            // ongoing: 스와이프로 지울 수 없는 알림 — FGS 알림의 기본 성격
            .setOngoing(true)
            // onlyAlertOnce: 갱신할 때마다 소리/진동이 반복되지 않게 한다
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    // ==================== 권한 ====================

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

    private fun foregroundServiceType(): Int =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        } else {
            // API 28 이하에는 서비스 타입 개념 자체가 없다
            0
        }

    companion object {
        private const val TAG = "LocationTrackingService"

        const val ACTION_STOP = "com.example.composesample.action.STOP_LOCATION_TRACKING"

        private const val CHANNEL_ID = "location_tracking_example"
        private const val NOTIFICATION_ID = 4501

        /** 알림 갱신 주기(초) */
        const val NOTIFY_INTERVAL_SECONDS = 5

        /** requestLocationUpdates 최소 간격(ms) */
        const val MIN_UPDATE_INTERVAL_MS = 3_000L

        /** requestLocationUpdates 최소 이동 거리(m) */
        const val MIN_UPDATE_DISTANCE_M = 0f

        private const val MAX_EVENTS = 12

        private val PROVIDERS = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER
        )

        private val _state = MutableStateFlow(LocationTrackingState())

        /** 서비스 → UI 단방향 상태. 서비스는 화면 없이도 살아있으므로 ViewModel 이 아닌 여기에 둔다 */
        val state: StateFlow<LocationTrackingState> = _state.asStateFlow()

        private val _events = MutableStateFlow<List<String>>(emptyList())

        /** 최신순 이벤트 로그(최대 [MAX_EVENTS]건) */
        val events: StateFlow<List<String>> = _events.asStateFlow()

        /**
         * 서비스 시작.
         *
         * API 26+ 에서는 [ContextCompat.startForegroundService] 로 시작해야 하며,
         * 이 호출 이후 5초 안에 서비스가 startForeground() 를 부르지 않으면 프로세스가 죽는다.
         */
        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, LocationTrackingService::class.java)
            )
        }

        /** 서비스 중지 — 액션을 담아 onStartCommand 로 보내 정리 절차를 태운다 */
        fun stop(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, LocationTrackingService::class.java).setAction(ACTION_STOP)
            )
        }

        fun clearEvents() {
            _events.value = emptyList()
        }

        private fun addEvent(message: String) {
            val stamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            _events.value = (listOf("$stamp  $message") + _events.value).take(MAX_EVENTS)
            Log.d(TAG, message)
        }
    }
}

/** 소수점 5자리(약 1m 해상도)로 좌표 표기 */
internal fun formatCoordinate(value: Double): String = String.format(Locale.US, "%.5f", value)

/** 초 단위를 mm:ss 로 */
internal fun formatElapsed(seconds: Int): String =
    String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60)
