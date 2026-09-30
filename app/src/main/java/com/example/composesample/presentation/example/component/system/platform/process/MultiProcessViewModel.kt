package com.example.composesample.presentation.example.component.system.platform.process

import android.app.ActivityManager
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.DeadObjectException
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Parcel
import android.os.Process
import android.os.RemoteException
import android.os.SystemClock
import android.os.TransactionTooLargeException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

private const val KB = 1024
private const val REPLY_TIMEOUT_MS = 3_000L
private const val CONNECT_TIMEOUT_MS = 10_000L
private const val MAX_CAUSE_DEPTH = 8

/** 크기별 전송에 쓰는 페이로드 크기 */
private val PAYLOAD_SIZES = listOf(100 * KB, 400 * KB, 600 * KB, 1024 * KB, 2048 * KB)

/** 경계 이진 탐색 범위와 정밀도(바이트 배열은 4바이트 단위로 정렬돼 파셀에 들어간다) */
private const val LIMIT_SEARCH_LOW = 64 * KB
private const val LIMIT_SEARCH_HIGH = 2048 * KB
private const val LIMIT_PRECISION = 4

/**
 * 같은 서비스 코드를 기본 프로세스·`:remote`·`:isolated` 세 곳에 bind 하고 Messenger 로 왕복한다.
 *
 * 바인딩은 Application 컨텍스트로 하고 [onCleared] 에서 모두 푼다. ServiceConnection 콜백과
 * 응답 Handler 는 메인 루퍼에서 돌기 때문에 타임라인 같은 상태는 메인 스레드에서만 바뀐다.
 */
class MultiProcessViewModel(private val application: Application) : ViewModel() {

    enum class Target(val label: String, val serviceClass: Class<out ProcessDemoService>) {
        LOCAL("기본 프로세스", LocalProcessDemoService::class.java),
        REMOTE(":remote", RemoteProcessDemoService::class.java),
        ISOLATED(":isolated", IsolatedProcessDemoService::class.java)
    }

    enum class LinkStatus(val label: String) {
        UNBOUND("bind 안 함"),
        CONNECTING("연결 중"),
        CONNECTED("연결됨"),
        DISCONNECTED("끊김(재시작 대기)"),
        DEAD("바인딩 죽음(다시 bind 필요)")
    }

    data class Outcome(val ok: Boolean, val text: String)

    /** 크기별 전송 한 줄 — 같은 크기를 같은 프로세스 서비스와 :remote 서비스에 보낸 결과 */
    data class PayloadRow(
        val payloadBytes: Int,
        val parcelBytes: Int,
        val local: Outcome? = null,
        val remote: Outcome? = null
    )

    data class LimitResult(
        val maxOkPayload: Int,
        val minFailPayload: Int,
        val maxOkParcel: Int,
        val failMessage: String,
        val attempts: Int
    )

    data class TimelineEvent(val atMs: Long, val text: String)

    data class IsolatedBinding(val index: Int, val pid: Int, val uid: Int)

    data class ProcessRow(val pid: Int, val name: String, val importance: String, val uid: Int, val pssKb: Long?)

    private class Reply(val arg2: Int, val data: Bundle)

    private val _status = MutableStateFlow(Target.entries.associateWith { LinkStatus.UNBOUND })
    val status = _status.asStateFlow()

    /** 기본 프로세스(이 화면)의 상태 — IPC 없이 직접 잰다 */
    private val _mainSnapshot = MutableStateFlow<ProcessSnapshot?>(null)
    val mainSnapshot = _mainSnapshot.asStateFlow()

    /** 각 서비스 프로세스가 Messenger 로 보고한 상태 */
    private val _snapshots = MutableStateFlow<Map<Target, ProcessSnapshot>>(emptyMap())
    val snapshots = _snapshots.asStateFlow()

    private val _payloadRows = MutableStateFlow<List<PayloadRow>>(emptyList())
    val payloadRows = _payloadRows.asStateFlow()

    private val _limit = MutableStateFlow<LimitResult?>(null)
    val limit = _limit.asStateFlow()

    private val _timeline = MutableStateFlow<List<TimelineEvent>>(emptyList())
    val timeline = _timeline.asStateFlow()

    private val _isolatedBindings = MutableStateFlow<List<IsolatedBinding>>(emptyList())
    val isolatedBindings = _isolatedBindings.asStateFlow()

    /** 점검 결과 — 키는 "main" / ":remote" / ":isolated" */
    private val _probes = MutableStateFlow<Map<String, ProcessProbe.Result>>(emptyMap())
    val probes = _probes.asStateFlow()

    private val _processTable = MutableStateFlow<List<ProcessRow>>(emptyList())
    val processTable = _processTable.asStateFlow()

    private val _running = MutableStateFlow<String?>(null)
    val running = _running.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private val nextRequestId = AtomicInteger(0)
    private val pending = ConcurrentHashMap<Int, CompletableDeferred<Reply>>()

    /** 서비스가 보낸 응답을 받는 쪽. 요청 번호(arg1)로 기다리던 요청을 깨운다 */
    private val replyMessenger = Messenger(Handler(Looper.getMainLooper()) { msg ->
        pending.remove(msg.arg1)?.complete(Reply(msg.arg2, msg.data))
        true
    })

    private val links = Target.entries.associateWith { Link(it) }

    /** 프로세스가 죽기 전의 Messenger — 죽은 바인더로 보내면 무슨 일이 나는지 보여주려고 남겨 둔다 */
    private var staleRemoteMessenger: Messenger? = null

    /** 타임라인 0ms 기준. 0 이면 진행 중인 시나리오가 없다 */
    private var timelineZero = 0L

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                File(application.filesDir, ProcessDemoProtocol.PROBE_FILE_NAME).writeText("기본 프로세스가 쓴 파일")
            }
            refreshMain()
        }
        bind(Target.LOCAL)
        bind(Target.REMOTE)
    }

    // ==================== ① 프로세스 정체 · 싱글턴 ====================

    fun incrementMain() = launchScenario("increment") {
        ProcessLocalState.counter.incrementAndGet()
        refreshMain()
    }

    fun incrementRemote() = launchScenario("increment") {
        val reply = safeRequest(Target.REMOTE, ProcessDemoProtocol.MSG_INCREMENT) ?: return@launchScenario
        _snapshots.update { it + (Target.REMOTE to ProcessSnapshot.fromBundle(reply.data)) }
    }

    fun refreshAll() = launchScenario("refresh") {
        refreshMain()
        Target.entries.forEach { refreshSnapshot(it) }
        refreshProcessTable()
    }

    // ==================== ② 바인더 트랜잭션 한계 ====================

    fun runPayloadComparison() = launchScenario("payload") {
        _payloadRows.value = PAYLOAD_SIZES.map { PayloadRow(payloadBytes = it, parcelBytes = parcelSizeOf(it)) }
        PAYLOAD_SIZES.forEachIndexed { index, size ->
            val local = sendPayload(Target.LOCAL, size)
            val remote = sendPayload(Target.REMOTE, size)
            _payloadRows.update { rows ->
                rows.toMutableList().also { it[index] = it[index].copy(local = local, remote = remote) }
            }
        }
    }

    /** :remote 로 보낼 수 있는 최대 페이로드를 이진 탐색한다 — 매 시도는 응답을 받은 뒤에 다음으로 넘어간다 */
    fun findRemoteLimit() = launchScenario("limit") {
        _limit.value = null
        var ok = LIMIT_SEARCH_LOW
        var fail = LIMIT_SEARCH_HIGH
        if (!sendPayload(Target.REMOTE, ok).ok) return@launchScenario
        var failMessage = sendPayload(Target.REMOTE, fail).takeUnless { it.ok }?.text ?: return@launchScenario
        var attempts = 2
        while (fail - ok > LIMIT_PRECISION) {
            val mid = (ok + fail) / 2
            val outcome = sendPayload(Target.REMOTE, mid)
            if (outcome.ok) {
                ok = mid
            } else {
                fail = mid
                failMessage = outcome.text
            }
            attempts++
        }
        _limit.value = LimitResult(
            maxOkPayload = ok,
            minFailPayload = fail,
            maxOkParcel = parcelSizeOf(ok),
            failMessage = failMessage,
            attempts = attempts
        )
    }

    private suspend fun sendPayload(target: Target, size: Int): Outcome {
        val messenger = links.getValue(target).messenger ?: return Outcome(false, "연결 안 됨")
        return try {
            val reply = request(messenger, ProcessDemoProtocol.MSG_PAYLOAD, payloadBundle(size))
                ?: return Outcome(false, "응답 없음(시간 초과)")
            Outcome(true, "수신 ${reply.arg2.formatBytes()}")
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            Outcome(false, t.describeRemoteFailure())
        }
    }

    private fun payloadBundle(size: Int) = Bundle().apply {
        putByteArray(ProcessDemoProtocol.KEY_PAYLOAD, ByteArray(size))
    }

    /**
     * 같은 모양의 Message 를 파셀에 써서 크기를 잰다. 프로세스를 건널 때 실제로 바인더에 실리는 것이 이 파셀이다
     * (IMessenger.send 트랜잭션은 여기에 인터페이스 토큰 등 수십 바이트를 더 얹는다).
     */
    private fun parcelSizeOf(payloadBytes: Int): Int {
        val message = Message.obtain(null, ProcessDemoProtocol.MSG_PAYLOAD, 0, 0).apply {
            replyTo = replyMessenger
            data = payloadBundle(payloadBytes)
        }
        val parcel = Parcel.obtain()
        return try {
            message.writeToParcel(parcel, 0)
            parcel.dataSize()
        } finally {
            parcel.recycle()
        }
    }

    // ==================== ③ 크래시 격리 · 재연결 ====================

    /** :remote 의 HandlerThread 에서 예외를 던지게 한다 — 그 프로세스만 죽는다 */
    fun crashRemote() {
        val messenger = links.getValue(Target.REMOTE).messenger ?: return
        startTimeline("원격(PID ${_snapshots.value[Target.REMOTE]?.pid})에 예외 요청 · ${mainStateLine()}")
        try {
            messenger.send(Message.obtain(null, ProcessDemoProtocol.MSG_CRASH))
        } catch (t: Throwable) {
            logTimeline("요청 실패 — ${t.describeRemoteFailure()}")
        }
    }

    /** 기본 프로세스에서 :remote 를 PID 로 죽인다 — 같은 UID 라 허용된다(저메모리 킬과 같은 상황) */
    fun killRemote() {
        val pid = _snapshots.value[Target.REMOTE]?.pid ?: return
        startTimeline("Process.killProcess($pid) · ${mainStateLine()}")
        Process.killProcess(pid)
    }

    /** 죽기 전 Messenger 로 보내 본다 — 새 프로세스가 떠 있어도 옛 바인더는 되살아나지 않는다 */
    fun sendToStaleMessenger() {
        val stale = staleRemoteMessenger
        if (timelineZero == 0L) startTimeline("이전 Messenger 로 보내기")
        if (stale == null) {
            logTimeline("이전 Messenger 없음 — 먼저 원격 프로세스를 죽여 보세요")
            return
        }
        try {
            stale.send(Message.obtain(null, ProcessDemoProtocol.MSG_SNAPSHOT))
            logTimeline("이전 Messenger.send() 성공(예상 밖)")
        } catch (t: Throwable) {
            logTimeline("이전 Messenger.send() → ${t.describeRemoteFailure()} · binderAlive=${stale.binder.isBinderAlive}")
        }
    }

    fun rebindRemote() {
        startTimeline("unbindService → bindService")
        unbind(Target.REMOTE)
        bind(Target.REMOTE)
    }

    private fun mainStateLine(): String =
        "메인 PID ${Process.myPid()} · 메인 카운터 ${ProcessLocalState.counter.get()}"

    private fun startTimeline(first: String) {
        timelineZero = SystemClock.elapsedRealtime()
        _timeline.value = listOf(TimelineEvent(0, first))
    }

    private fun logTimeline(text: String) {
        if (timelineZero == 0L) return
        _timeline.update { it + TimelineEvent(SystemClock.elapsedRealtime() - timelineZero, text) }
    }

    // ==================== ④ isolatedProcess ====================

    /** 격리 서비스를 풀었다가 다시 bind 한다 — 매번 새 프로세스·새 격리 UID 가 되는지 기록한다 */
    fun rebindIsolated() = launchScenario("isolated") {
        unbind(Target.ISOLATED)
        connectIsolated()
        refreshProcessTable()
    }

    /** 같은 점검 함수를 세 프로세스에서 실행한다 */
    fun runProbes() = launchScenario("probe") {
        val main = withContext(Dispatchers.IO) { ProcessProbe.run(application) }
        if (_status.value[Target.ISOLATED] != LinkStatus.CONNECTED) connectIsolated()
        val remote = safeRequest(Target.REMOTE, ProcessDemoProtocol.MSG_PROBE)?.let { ProcessProbe.Result.fromBundle(it.data) }
        val isolated = safeRequest(Target.ISOLATED, ProcessDemoProtocol.MSG_PROBE)?.let { ProcessProbe.Result.fromBundle(it.data) }
        _probes.value = buildMap {
            put("main", main)
            remote?.let { put(":remote", it) }
            isolated?.let { put(":isolated", it) }
        }
        refreshMain()
        refreshSnapshot(Target.REMOTE)
        refreshSnapshot(Target.ISOLATED)
    }

    /** 격리 서비스를 bind 하고 연결되면 그 프로세스의 PID·UID 를 이력에 남긴다 */
    private suspend fun connectIsolated() {
        bind(Target.ISOLATED)
        val connected = withTimeoutOrNull(CONNECT_TIMEOUT_MS) {
            _status.first { it[Target.ISOLATED] == LinkStatus.CONNECTED }
        } != null
        if (!connected) return
        val snapshot = refreshSnapshot(Target.ISOLATED) ?: return
        _isolatedBindings.update { it + IsolatedBinding(it.size + 1, snapshot.pid, snapshot.uid) }
    }

    // ==================== ⑤ 프로세스 표 ====================

    fun refreshProcessTable() {
        val am = application.getSystemService(ActivityManager::class.java)
        val pssByPid = (_snapshots.value.values + listOfNotNull(_mainSnapshot.value)).associate { it.pid to it.pssKb }
        _processTable.value = am.runningAppProcesses.orEmpty()
            .sortedBy { it.pid }
            .map {
                ProcessRow(
                    pid = it.pid,
                    name = it.processName.shortProcessName(),
                    importance = importanceLabel(it.importance),
                    uid = it.uid,
                    pssKb = pssByPid[it.pid]
                )
            }
    }

    private fun importanceLabel(importance: Int): String {
        val name = when (importance) {
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND -> "FOREGROUND"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE -> "FOREGROUND_SERVICE"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE -> "VISIBLE"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_PERCEPTIBLE -> "PERCEPTIBLE"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE -> "SERVICE"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED -> "CACHED"
            else -> null
        }
        return if (name != null) "$name($importance)" else importance.toString()
    }

    // ==================== 바인딩 · 요청 공통 ====================

    private inner class Link(val target: Target) : ServiceConnection {
        var bound = false
        var messenger: Messenger? = null

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            messenger = Messenger(service)
            setStatus(target, LinkStatus.CONNECTED)
            if (target == Target.REMOTE) {
                // 바인더 수준의 사망 통지. 콜백은 바인더 스레드에서 오므로 메인으로 넘긴다
                runCatching { service.linkToDeath({ mainHandler.post { logTimeline("binderDied (linkToDeath)") } }, 0) }
                logTimeline("onServiceConnected")
            }
            viewModelScope.launch {
                val snapshot = refreshSnapshot(target)
                if (target == Target.REMOTE && snapshot != null) {
                    logTimeline("새 원격 PID ${snapshot.pid} · 원격 카운터 ${snapshot.counter} · ${mainStateLine()}")
                }
                refreshProcessTable()
            }
        }

        // 서비스 프로세스가 죽었다. 바인딩은 남아 있어서, 시스템이 서비스를 다시 띄우면 onServiceConnected 가 다시 온다
        override fun onServiceDisconnected(name: ComponentName) {
            if (target == Target.REMOTE) {
                staleRemoteMessenger = messenger
                logTimeline("onServiceDisconnected")
            }
            messenger = null
            setStatus(target, LinkStatus.DISCONNECTED)
            _snapshots.update { it - target }
            refreshProcessTable()
        }

        // API 26+. 이 바인딩으로는 더 이상 연결되지 않는다 — unbind 후 다시 bind 해야 한다.
        // 실측: 1분 안에 두 번째 크래시가 나면 시스템이 재시작을 포기하고 이 콜백을 보낸다(kill 이나 첫 크래시는 자동 재연결)
        override fun onBindingDied(name: ComponentName) {
            setStatus(target, LinkStatus.DEAD)
            if (target == Target.REMOTE) logTimeline("onBindingDied — 다시 bind 해야 한다")
        }
    }

    private fun bind(target: Target) {
        val link = links.getValue(target)
        if (link.bound) return
        link.bound = application.bindService(Intent(application, target.serviceClass), link, Context.BIND_AUTO_CREATE)
        setStatus(target, if (link.bound) LinkStatus.CONNECTING else LinkStatus.UNBOUND)
    }

    // unbindService 는 onServiceDisconnected 를 부르지 않는다 — 상태를 직접 정리한다
    private fun unbind(target: Target) {
        val link = links.getValue(target)
        if (!link.bound) return
        application.unbindService(link)
        link.bound = false
        link.messenger = null
        setStatus(target, LinkStatus.UNBOUND)
        _snapshots.update { it - target }
    }

    private fun setStatus(target: Target, status: LinkStatus) {
        _status.update { it + (target to status) }
    }

    private suspend fun refreshMain() {
        _mainSnapshot.value = withContext(Dispatchers.Default) { ProcessSnapshot.capture() }
    }

    private suspend fun refreshSnapshot(target: Target): ProcessSnapshot? {
        val reply = safeRequest(target, ProcessDemoProtocol.MSG_SNAPSHOT) ?: return null
        return ProcessSnapshot.fromBundle(reply.data).also { snapshot -> _snapshots.update { it + (target to snapshot) } }
    }

    /** 연결돼 있지 않거나 실패하면 null — 상태 조회처럼 실패 이유를 따로 보여줄 필요가 없는 요청용 */
    private suspend fun safeRequest(target: Target, what: Int): Reply? {
        val messenger = links.getValue(target).messenger ?: return null
        return try {
            request(messenger, what, null)
        } catch (e: CancellationException) {
            throw e
        } catch (t: Throwable) {
            null
        }
    }

    /** 요청 번호를 arg1 에 담아 보내고 같은 번호의 응답을 기다린다 */
    private suspend fun request(messenger: Messenger, what: Int, data: Bundle?): Reply? {
        val id = nextRequestId.incrementAndGet()
        val deferred = CompletableDeferred<Reply>()
        pending[id] = deferred
        try {
            val message = Message.obtain(null, what, id, 0).apply {
                replyTo = replyMessenger
                if (data != null) this.data = data
            }
            // 받는 쪽이 다른 프로세스면 여기서 Message 가 파셀로 직렬화돼 바인더를 건넌다(oneway)
            messenger.send(message)
            return withTimeoutOrNull(REPLY_TIMEOUT_MS) { deferred.await() }
        } finally {
            pending.remove(id)
        }
    }

    private fun launchScenario(name: String, block: suspend () -> Unit) {
        if (_running.value != null) return
        _running.value = name
        viewModelScope.launch {
            try {
                block()
            } finally {
                _running.value = null
            }
        }
    }

    override fun onCleared() {
        Target.entries.forEach { unbind(it) }
        pending.values.forEach { it.cancel() }
        File(application.filesDir, ProcessDemoProtocol.PROBE_FILE_NAME).delete()
    }
}

/**
 * 바인더 호출 실패를 한 줄로 요약한다.
 * 디버그 빌드에서는 HotSwan 인터프리터가 checked 예외(RemoteException 계열)를 UndeclaredThrowableException 으로
 * 감쌀 수 있어서, 맨 바깥 타입이 아니라 원인 사슬에서 찾는다.
 */
private fun Throwable.describeRemoteFailure(): String {
    val chain = generateSequence(this) { it.cause }.take(MAX_CAUSE_DEPTH).toList()
    val cause = chain.firstOrNull { it is TransactionTooLargeException }
        ?: chain.firstOrNull { it is DeadObjectException }
        ?: chain.firstOrNull { it is RemoteException }
        ?: this
    return "${cause.javaClass.simpleName}: ${cause.message.orEmpty()}"
}

/** 614400 → "600.0 KB (614,400 B)" */
fun Int.formatBytes(): String =
    if (this >= KB) {
        String.format(Locale.US, "%,.1f KB (%,d B)", this / KB.toDouble(), this)
    } else {
        String.format(Locale.US, "%,d B", this)
    }
