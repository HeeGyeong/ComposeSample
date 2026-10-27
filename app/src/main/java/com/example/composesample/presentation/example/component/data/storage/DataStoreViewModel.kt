package com.example.composesample.presentation.example.component.data.storage

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.SystemClock
import androidx.core.content.edit
import androidx.datastore.core.CorruptionHandler
import androidx.datastore.core.DataStore
import androidx.datastore.core.FileStorage
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * DataStore 예제의 실행부 — 같은 일을 SharedPreferences 와 DataStore 로 각각 해 보고 결과를 나란히 둔다.
 *
 * ① 동시 증가: 읽고-고치고-쓰기를 100개 코루틴이 동시에 → SP 는 갱신을 잃고, updateData 는 직렬화된다
 * ② 쓰기 비용: 200번 쓰기 — SP apply/commit, updateData 200번, updateData 한 번에 묶기
 * ③ 손상 파일: SP 는 빈 값으로 넘어가고, DataStore 는 기본으로 CorruptionException · ReplaceFileCorruptionHandler 로 복구
 * ④ SharedPreferencesMigration: 데모용 SP 파일을 Preferences DataStore 로 옮기고 원본이 지워지는지
 * ⑤ 같은 파일에 DataStore 두 개: 두 번째가 IllegalStateException — 첫 번째의 scope 를 끝내야 풀린다
 * ⑥ 두 프로세스: 기본 프로세스와 `:remote`([DataStoreRemoteService])가 같은 파일을 같은 시각부터 +1 — SP · 단일 프로세스용 DataStore ·
 *    MultiProcessDataStoreFactory. 이어서 원격만 몇 번 더 쓰는 동안 기본 프로세스가 그 쓰기를 받는지
 * ⑦ 밀린 apply: 큰 값을 apply 로 쌓고 서비스를 시작하거나 화면을 떠나면, ActivityThread 가 QueuedWork.waitToFinish 로
 *    메인 스레드에서 쓰기를 마저 한다 — 그 메시지가 끝나기까지의 시간을 잰다
 *
 * 실험마다 DataStore 를 새로 만들고 scope 를 끝내 파일을 놓는다(⑤의 규칙 때문). 실제 앱은 파일당 하나를 싱글턴으로 둔다.
 * 모든 파일은 이 앱의 filesDir/datastore_demo 와 datastore_demo_* SharedPreferences 뿐이다(⑥·⑦ 은 끝나면 지운다).
 */
class DataStoreViewModel(application: Application) : AndroidViewModel(application) {

    data class UiState(
        val race: List<RaceResult>? = null,
        val cost: List<CostResult>? = null,
        val corruption: List<Pair<String, String>>? = null,
        val migration: List<Pair<String, String>>? = null,
        val twin: List<Pair<String, String>>? = null,
        val boundary: List<BoundaryResult>? = null,
        val waits: List<WaitResult> = emptyList(),
        val busy: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val context: Context get() = getApplication()
    private val demoDir: File get() = File(context.filesDir, "datastore_demo")
    private val sharedPrefsDir: File get() = File(context.applicationInfo.dataDir, "shared_prefs")

    // ==================== ① 동시 증가 ====================

    fun runRace() = experiment("동시 증가 100회") {
        val prefs = prefs(PREFS_RACE).also { it.edit(commit = true) { clear() } }
        val results = mutableListOf<RaceResult>()
        // 같은 SP 를 두 번 — 읽기와 쓰기 사이에 아무것도 없을 때와 1ms 작업이 끼었을 때
        for (gap in listOf(0L, WORK_MILLIS)) {
            prefs.edit(commit = true) { putInt(KEY_COUNT, 0) }
            val millis = measure {
                coroutineScope {
                    repeat(RACE_COUNT) {
                        launch(Dispatchers.Default) {
                            val current = prefs.getInt(KEY_COUNT, 0)
                            if (gap > 0) delay(gap)
                            prefs.edit { putInt(KEY_COUNT, current + 1) } // apply — 메모리는 즉시 바뀐다
                        }
                    }
                }
            }
            results += RaceResult("SharedPreferences · 사이 ${gap}ms", RACE_COUNT, prefs.getInt(KEY_COUNT, 0), millis)
        }
        val file = demoFile("race.settings").also { it.delete() }
        withStore(file) { store ->
            val millis = measure {
                coroutineScope {
                    repeat(RACE_COUNT) {
                        launch(Dispatchers.Default) {
                            // transform 은 한 번에 하나씩 — 앞 transform 의 결과가 다음 transform 의 입력이다
                            store.updateData { current ->
                                delay(WORK_MILLIS)
                                current.copy(count = current.count + 1)
                            }
                        }
                    }
                }
            }
            results += RaceResult("DataStore.updateData · 사이 ${WORK_MILLIS}ms", RACE_COUNT, store.data.first().count, millis)
        }
        _uiState.update { it.copy(race = results) }
    }

    // ==================== ② 쓰기 비용 ====================

    fun runCost() = experiment("쓰기 ${COST_COUNT}회") {
        val prefs = prefs(PREFS_COST).also { it.edit(commit = true) { clear() } }
        val results = mutableListOf<CostResult>()
        results += CostResult(
            "SP apply ×$COST_COUNT", COST_COUNT, "호출 스레드는 메모리만 — 디스크는 QueuedWork 가 뒤에서",
            measure { repeat(COST_COUNT) { i -> prefs.edit { putInt(KEY_COUNT, i) } } }
        )
        results += CostResult(
            "SP commit ×$COST_COUNT", COST_COUNT, "호출마다 동기 쓰기",
            measure { repeat(COST_COUNT) { i -> prefs.edit(commit = true) { putInt(KEY_COUNT, i) } } }
        )
        val counting = CountingSerializer()
        val file = demoFile("cost.settings").also { it.delete() }
        withStore(file, serializer = counting) { store ->
            store.data.first() // 첫 읽기(파일 없음 → 기본값)는 측정에서 뺀다
            counting.writes.set(0)
            val separate = measure { repeat(COST_COUNT) { store.updateData { it.copy(count = it.count + 1) } } }
            results += CostResult("updateData ×$COST_COUNT", COST_COUNT, "파일 쓰기 ${counting.writes.get()}회(임시 파일 → fsync → 이름 바꾸기)", separate)
            counting.writes.set(0)
            val batched = measure {
                store.updateData { start ->
                    var value = start
                    repeat(COST_COUNT) { value = value.copy(count = value.count + 1) }
                    value
                }
            }
            results += CostResult("updateData 1회에 $COST_COUNT 변경", COST_COUNT, "파일 쓰기 ${counting.writes.get()}회", batched)
        }
        _uiState.update { it.copy(cost = results) }
    }

    // ==================== ③ 손상 파일 ====================

    fun runCorruption() = experiment("손상 파일") {
        val rows = mutableListOf<Pair<String, String>>()

        // SharedPreferences — 이 이름으로 아직 열지 않은 상태(캐시 없음)에서 깨진 파일을 읽힌다
        context.deleteSharedPreferences(PREFS_BROKEN) // 프로세스 캐시와 파일을 함께 지운다
        val spFile = File(sharedPrefsDir, "$PREFS_BROKEN.xml")
        spFile.parentFile?.mkdirs()
        spFile.writeBytes(GARBAGE)
        val broken = prefs(PREFS_BROKEN)
        val read = try {
            "getAll() = ${broken.all} · getInt(count, -1) = ${broken.getInt(KEY_COUNT, -1)}"
        } catch (e: Exception) {
            describeFailure(e)
        }
        rows += "SP 깨진 파일 읽기" to read
        broken.edit(commit = true) { putString(KEY_LABEL, "새 값") }
        rows += "SP 이어서 쓰기" to "파일 = ${escapeControl(spFile.readText()).take(160)}"

        // DataStore — 정상 값을 쓰고, 파일을 같은 쓰레기로 덮은 뒤 다시 연다
        val file = demoFile("corrupt.settings").also { it.delete() }
        withStore(file) { store -> store.updateData { DemoSettings(7, "소중한 값") } }
        rows += "DataStore 정상 파일" to escapeControl(file.readText())
        file.writeBytes(GARBAGE)
        rows += "기본(ReThrow) 처리기" to try {
            withStore(file) { store -> "읽힘 — ${store.data.first()}" }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            describeFailure(e)
        }
        rows += "ReThrow 뒤 파일" to "${file.length()}바이트 · ${if (file.readBytes().contentEquals(GARBAGE)) "덮은 내용 그대로" else "바뀜"}"
        var handlerMessage = ""
        val replace = ReplaceFileCorruptionHandler { e ->
            handlerMessage = describeFailure(e)
            DemoSettings(0, "복구 기본값")
        }
        rows += "ReplaceFileCorruptionHandler" to withStore(file, handler = replace) { store -> "읽힘 — ${store.data.first()}" }
        rows += "처리기가 받은 예외" to handlerMessage
        rows += "복구 뒤 파일" to escapeControl(file.readText())
        _uiState.update { it.copy(corruption = rows) }
    }

    // ==================== ④ 이관 ====================

    fun runMigration(someKeysOnly: Boolean) = experiment("SharedPreferences 이관") {
        val rows = mutableListOf<Pair<String, String>>()
        val target = demoFile("migrated.preferences_pb").also { it.delete() }
        context.deleteSharedPreferences(PREFS_LEGACY)
        prefs(PREFS_LEGACY).edit(commit = true) {
            putInt(KEY_COUNT, 3)
            putString(KEY_LABEL, "이전 앱")
            putBoolean(KEY_DARK, true)
            putStringSet(KEY_TAGS, setOf("a", "b"))
        }
        val legacyFile = File(sharedPrefsDir, "$PREFS_LEGACY.xml")
        rows += "이관 전 SP" to "${prefs(PREFS_LEGACY).all.toSortedMap()} · 파일 ${legacyFile.exists()}"

        // 이관 객체는 열 때마다 새로 — 같은 설정으로 두 번 연다
        fun migration() = if (someKeysOnly) {
            SharedPreferencesMigration(context, PREFS_LEGACY, keysToMigrate = setOf(KEY_COUNT, KEY_LABEL))
        } else {
            SharedPreferencesMigration(context, PREFS_LEGACY)
        }
        val migrated = withScope { scope ->
            val store = PreferenceDataStoreFactory.create(migrations = listOf(migration()), scope = scope) { target }
            store.data.first().asMap().entries.associate { it.key.name to it.value }.toSortedMap()
        }
        rows += if (someKeysOnly) "keysToMigrate = count·label" to migrated.toString() else "전체 키 이관" to migrated.toString()
        rows += "이관 뒤 SP" to "${prefs(PREFS_LEGACY).all.toSortedMap()} · 파일 ${legacyFile.exists()}"

        // 같은 설정으로 다시 열기 — 옮길 키가 SP 에 남아 있지 않으면 이관은 다시 돌지 않는다(shouldRunMigration)
        val reopened = withScope { scope ->
            val store = PreferenceDataStoreFactory.create(migrations = listOf(migration()), scope = scope) { target }
            store.data.first().asMap().keys.map { it.name }.sorted()
        }
        rows += "같은 설정으로 다시 열기" to "DataStore 키 $reopened · SP ${prefs(PREFS_LEGACY).all.keys.sorted()}"
        _uiState.update { it.copy(migration = rows) }
    }

    // ==================== ⑤ 같은 파일 두 개 ====================

    fun runTwin() = experiment("같은 파일에 DataStore 두 개") {
        val rows = mutableListOf<Pair<String, String>>()
        val file = demoFile("twin.settings").also { it.delete() }
        val firstJob = SupervisorJob()
        val first = CoroutineScope(Dispatchers.IO + firstJob)
        try {
            val storeA = DataStore.Builder(FileStorage(DemoSettingsSerializer) { file }, first.coroutineContext).build()
            rows += "A 읽기" to storeA.updateData { it.copy(count = 1, label = "A") }.toString()
            rows += "B 읽기(A 살아 있음)" to try {
                withStore(file) { store -> "읽힘 — ${store.data.first()}" }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                describeFailure(e)
            }
        } finally {
            withContext(NonCancellable) { firstJob.cancelAndJoin() }
        }
        rows += "A scope 취소·join 뒤 C 읽기" to withStore(file) { store -> "읽힘 — ${store.data.first()}" }
        _uiState.update { it.copy(twin = rows) }
    }

    // ==================== ⑥ 두 프로세스 ====================

    fun runBoundary() = experiment("두 프로세스 동시 증가") {
        val remote = remoteMessenger()
        val rows = mutableListOf<BoundaryResult>()
        _uiState.update { it.copy(boundary = emptyList()) }
        for (mode in BoundaryMode.entries) {
            resetBoundary(context)
            // 두 프로세스가 같은 시각(uptimeMillis 는 기기 전체가 같은 시계)부터 시작한다
            val startAt = SystemClock.uptimeMillis() + START_DELAY_MILLIS
            val request = Bundle().apply {
                putInt(BoundaryProtocol.KEY_MODE, mode.ordinal)
                putInt(BoundaryProtocol.KEY_COUNT, BOUNDARY_COUNT)
                putLong(BoundaryProtocol.KEY_START_AT, startAt)
            }
            val (main, far) = coroutineScope {
                val far = async { requestSide(remote, BoundaryProtocol.MSG_RACE, request) }
                runBoundarySide(context, mode, BOUNDARY_COUNT, startAt) to far.await()
            }
            val onDisk = boundaryOnDisk(context, mode)
            // 관찰 중 실패(깨진 파일 읽기 등)는 이 방식의 결과로 남기고 다음 방식으로 넘어간다
            val observation = try {
                observeRemoteWrites(remote, mode)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Observation(emptyList(), null, describeFailure(e))
            }
            rows += BoundaryResult(
                mode, BOUNDARY_COUNT, main, far, onDisk,
                observation.values, observation.reread, boundaryOnDisk(context, mode), observation.error
            )
            _uiState.update { it.copy(boundary = rows.toList()) }
        }
        resetBoundary(context)
    }

    private class Observation(val values: List<Int>, val reread: Int?, val error: String? = null)

    /** 원격이 SEQUENCE_COUNT 번 더 쓰는 동안 기본 프로세스가 받은 값, 그리고 그 뒤 다시 읽은 값 */
    private suspend fun observeRemoteWrites(remote: Messenger, mode: BoundaryMode): Observation {
        val seen = Collections.synchronizedList(mutableListOf<Int>())
        val request = Bundle().apply {
            putInt(BoundaryProtocol.KEY_MODE, mode.ordinal)
            putInt(BoundaryProtocol.KEY_COUNT, SEQUENCE_COUNT)
            putLong(BoundaryProtocol.KEY_INTERVAL, SEQUENCE_INTERVAL_MILLIS)
        }
        if (mode == BoundaryMode.PREFS) {
            val prefs = prefs(BOUNDARY_PREFS)
            seen += prefs.getInt(BOUNDARY_KEY, 0)
            // 변경 리스너 — 이 프로세스의 SharedPreferences 객체에서 일어난 변경만 온다
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { changed, key ->
                if (key == BOUNDARY_KEY) seen += changed.getInt(BOUNDARY_KEY, 0)
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            try {
                requestSide(remote, BoundaryProtocol.MSG_SEQUENCE, request)
                delay(OBSERVE_TAIL_MILLIS)
            } finally {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
            return Observation(seen.toList(), prefs.getInt(BOUNDARY_KEY, 0))
        }
        return withBoundaryStore(context, mode) { store ->
            coroutineScope {
                val first = CompletableDeferred<Unit>()
                val collector = launch {
                    store.data.collect {
                        seen += it.count
                        first.complete(Unit)
                    }
                }
                first.await()
                requestSide(remote, BoundaryProtocol.MSG_SEQUENCE, request)
                delay(OBSERVE_TAIL_MILLIS)
                collector.cancel()
                Observation(seen.toList(), store.data.first().count)
            }
        }
    }

    private val nextRequestId = AtomicInteger(0)
    private val pending = ConcurrentHashMap<Int, CompletableDeferred<Bundle>>()

    /** 원격의 응답을 받는 쪽 — 요청 번호(arg1)로 기다리던 요청을 깨운다 */
    private val replyMessenger = Messenger(Handler(Looper.getMainLooper()) { msg ->
        pending.remove(msg.arg1)?.complete(Bundle(msg.data))
        true
    })

    @Volatile
    private var remoteBound = false

    @Volatile
    private var remote = CompletableDeferred<Messenger>()

    private val remoteConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            remote.complete(Messenger(service))
        }

        // 원격 프로세스가 죽었다 — 기다리던 요청을 실패로 깨우고 다음 연결을 새로 기다린다
        override fun onServiceDisconnected(name: ComponentName) {
            remote = CompletableDeferred()
            pending.values.forEach { it.completeExceptionally(IllegalStateException(":remote 프로세스가 끝났다")) }
        }
    }

    private suspend fun remoteMessenger(): Messenger {
        if (!remoteBound) {
            remoteBound = context.bindService(Intent(context, DataStoreRemoteService::class.java), remoteConnection, Context.BIND_AUTO_CREATE)
            check(remoteBound) { "DataStoreRemoteService 에 bind 하지 못했다" }
        }
        return withTimeoutOrNull(REMOTE_TIMEOUT_MILLIS) { remote.await() } ?: error(":remote 연결 시간 초과")
    }

    private suspend fun requestSide(messenger: Messenger, what: Int, data: Bundle): BoundarySide {
        val id = nextRequestId.incrementAndGet()
        val deferred = CompletableDeferred<Bundle>()
        pending[id] = deferred
        try {
            messenger.send(Message.obtain(null, what, id, 0).apply {
                replyTo = replyMessenger
                this.data = data
            })
            val reply = withTimeoutOrNull(REMOTE_TIMEOUT_MILLIS) { deferred.await() } ?: error(":remote 응답 시간 초과")
            return BoundaryProtocol.sideFromBundle(reply)
        } finally {
            pending.remove(id)
        }
    }

    // ==================== ⑦ 밀린 apply ====================

    /** 서비스 시작으로 — 쌓지 않았을 때와 쌓았을 때 */
    fun runApplyWaitByService() = experiment("밀린 apply · 서비스 시작") {
        addWait(startWaitService(pendingFiles = 0))
        warmWaitPrefs()
        // 쌓기와 서비스 시작을 메인 스레드에서 붙여서 — 둘 사이가 100ms(QueuedWork 지연)를 넘지 않게
        withContext(Dispatchers.Main) { queueApplies() }
        addWait(startWaitService(pendingFiles = WAIT_FILES))
        deleteWaitPrefs()
    }

    /** apply 를 쌓고 leave(홈으로 나가기)를 부른다 — 결과는 onStop 에서 잰다 */
    fun queueAppliesThenLeave(leave: () -> Unit) = experiment("밀린 apply · 홈으로") {
        warmWaitPrefs()
        withContext(Dispatchers.Main) {
            pendingAtStop = queueApplies()
            leave()
        }
    }

    /** 다음 onStop 이 치를 apply 파일 수 — 0 이면 그냥 나간 것(대조) */
    @Volatile
    private var pendingAtStop = 0

    /** 마지막 apply 를 끝낸 시각(uptimeMillis) */
    @Volatile
    private var queuedAt = 0L

    /** onStop 을 지났고 상태 저장을 기다리는 중 */
    private var stopped = false

    private val stopProbe = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityStopped(activity: Activity) {
            // P(API 28)부터 상태 저장은 onStop 뒤다 — 콜백이 있는 API 29+ 는 저장이 끝난 뒤부터 잰다
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) stopped = true else measureStop()
        }

        override fun onActivityPostSaveInstanceState(activity: Activity, outState: Bundle) {
            if (stopped) {
                stopped = false
                measureStop()
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityResumed(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }

    /** ActivityThread.handleStopActivity 는 이 뒤 같은 메시지에서 QueuedWork.waitToFinish 를 부른다 */
    private fun measureStop() {
        val pendingFiles = pendingAtStop.also { pendingAtStop = 0 }
        val since = if (pendingFiles > 0) SystemClock.uptimeMillis() - queuedAt else null
        measureRestOfMainMessage { millis ->
            addWait(WaitResult("화면 떠남(onStop)", pendingFiles, millis, since))
            if (pendingFiles > 0) viewModelScope.launch(Dispatchers.IO) { deleteWaitPrefs() }
        }
    }

    // 프로퍼티 초기화 순서 — stopProbe 가 만들어진 뒤에 등록한다
    init {
        getApplication<Application>().registerActivityLifecycleCallbacks(stopProbe)
    }

    private suspend fun startWaitService(pendingFiles: Int): WaitResult = coroutineScope {
        // 서비스가 보내기 전에 구독부터
        val result = async(start = CoroutineStart.UNDISPATCHED) { ApplyWaitProbe.results.first() }
        val intent = Intent(context, ApplyWaitService::class.java).putExtra(ApplyWaitService.EXTRA_PENDING, pendingFiles)
        if (pendingFiles > 0) intent.putExtra(ApplyWaitService.EXTRA_QUEUED_AT, queuedAt)
        context.startService(intent)
        withTimeoutOrNull(WAIT_TIMEOUT_MILLIS) { result.await() } ?: run {
            result.cancel()
            error("ApplyWaitService 응답 시간 초과")
        }
    }

    /** 쌓기 전에 파일을 미리 열어 둔다 — 첫 edit 가 디스크 읽기를 기다리는 동안 100ms 지연이 지나가 버리지 않게 */
    private fun warmWaitPrefs() = repeat(WAIT_FILES) { i -> prefs("$PREFS_WAIT_PREFIX$i").all }

    /** 데모 SP 파일 WAIT_FILES 개에 큰 값을 apply — 메모리만 바로 바뀌고 디스크 쓰기는 QueuedWork 에서 100ms 뒤로 미뤄진다 */
    private fun queueApplies(): Int {
        // 매번 다른 값 — 메모리 값이 그대로면 SharedPreferencesImpl 이 디스크 쓰기를 건너뛴다
        val value = SystemClock.uptimeMillis().toString() + "x".repeat(WAIT_VALUE_CHARS)
        repeat(WAIT_FILES) { i -> prefs("$PREFS_WAIT_PREFIX$i").edit { putString(KEY_LABEL, value) } }
        queuedAt = SystemClock.uptimeMillis()
        return WAIT_FILES
    }

    private fun deleteWaitPrefs() = repeat(WAIT_FILES) { i -> context.deleteSharedPreferences("$PREFS_WAIT_PREFIX$i") }

    private fun addWait(result: WaitResult) = _uiState.update { it.copy(waits = (listOf(result) + it.waits).take(MAX_WAITS)) }

    override fun onCleared() {
        getApplication<Application>().unregisterActivityLifecycleCallbacks(stopProbe)
        if (remoteBound) {
            context.unbindService(remoteConnection)
            remoteBound = false
        }
        pending.values.forEach { it.cancel() }
    }

    // ==================== 내부 ====================

    /** 실험 하나를 IO 에서 — 실패는 busy 줄에 남긴다 */
    private fun experiment(name: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(busy = "$name 진행 중") }
            val error = withContext(Dispatchers.IO) {
                try {
                    demoDir.mkdirs()
                    block()
                    null
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    "$name 실패 — ${describeFailure(e)}"
                }
            }
            _uiState.update { it.copy(busy = error) }
        }
    }

    /** 실험용 scope — 끝나면 취소하고 join 해서 DataStore 가 파일을 놓게 한다 */
    private suspend fun <R> withScope(block: suspend (CoroutineScope) -> R): R {
        val job = SupervisorJob()
        val scope = CoroutineScope(Dispatchers.IO + job)
        try {
            return block(scope)
        } finally {
            withContext(NonCancellable) { job.cancelAndJoin() }
        }
    }

    private suspend fun <R> withStore(
        file: File,
        serializer: Serializer<DemoSettings> = DemoSettingsSerializer,
        handler: CorruptionHandler<DemoSettings>? = null,
        block: suspend (DataStore<DemoSettings>) -> R
    ): R = withScope { scope ->
        val builder = DataStore.Builder(FileStorage(serializer) { file }, scope.coroutineContext)
        handler?.let { builder.setCorruptionHandler(it) }
        block(builder.build())
    }

    private fun prefs(name: String): SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    private fun demoFile(name: String) = File(demoDir, name)

    private inline fun measure(block: () -> Unit): Long {
        val started = System.nanoTime()
        block()
        return (System.nanoTime() - started) / 1_000_000
    }

    /** 실제 파일 쓰기 횟수를 세는 직렬화기 */
    private class CountingSerializer : Serializer<DemoSettings> {
        val writes = AtomicInteger()
        override val defaultValue: DemoSettings = DemoSettingsSerializer.defaultValue
        override suspend fun readFrom(input: InputStream): DemoSettings = DemoSettingsSerializer.readFrom(input)
        override suspend fun writeTo(t: DemoSettings, output: OutputStream) {
            writes.incrementAndGet()
            DemoSettingsSerializer.writeTo(t, output)
        }
    }

    companion object {
        private const val RACE_COUNT = 100
        private const val COST_COUNT = 200
        private const val WORK_MILLIS = 1L
        private const val KEY_COUNT = "count"
        private const val KEY_LABEL = "label"
        private const val KEY_DARK = "dark"
        private const val KEY_TAGS = "tags"
        private const val PREFS_RACE = "datastore_demo_race"
        private const val PREFS_COST = "datastore_demo_cost"
        private const val PREFS_BROKEN = "datastore_demo_broken"
        private const val PREFS_LEGACY = "datastore_demo_legacy"
        private const val PREFS_WAIT_PREFIX = "datastore_demo_wait_"

        private const val BOUNDARY_COUNT = 100
        private const val START_DELAY_MILLIS = 300L
        private const val SEQUENCE_COUNT = 5
        private const val SEQUENCE_INTERVAL_MILLIS = 100L
        private const val OBSERVE_TAIL_MILLIS = 500L
        private const val REMOTE_TIMEOUT_MILLIS = 60_000L

        private const val WAIT_FILES = 20
        private const val WAIT_VALUE_CHARS = 50_000
        private const val WAIT_TIMEOUT_MILLIS = 10_000L
        private const val MAX_WAITS = 8

        /** 저장 중 전원 차단·디스크 오류를 흉내 낸 임의 바이트 */
        private val GARBAGE = byteArrayOf(0, 0, 0x3C, 0x6D, 0x61, 0x70, 0x3E, 0x7F, 0x01)
    }
}
