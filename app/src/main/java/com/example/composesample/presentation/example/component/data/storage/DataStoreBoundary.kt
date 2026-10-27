package com.example.composesample.presentation.example.component.data.storage

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.SystemClock
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.core.FileStorage
import androidx.datastore.core.MultiProcessDataStoreFactory
import com.example.composesample.presentation.example.component.system.platform.process.shortProcessName
import com.example.composesample.util.currentProcessName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/**
 * DataStore 예제 7·8번 카드(프로세스 경계)의 실행부 중 두 프로세스가 함께 쓰는 부분.
 *
 * - 7번: 기본 프로세스와 `:remote` 프로세스(#114 MultiProcessExample 과 같은 프로세스 이름)가 같은 파일을 같은 시각부터
 *   +1 한다. 두 쪽 모두 [runBoundarySide] 하나를 돈다 — 기본 프로세스는 ViewModel 에서, 원격은 [DataStoreRemoteService] 에서.
 * - 8번: 밀린 SharedPreferences.apply 를 ActivityThread 가 QueuedWork.waitToFinish 로 메인 스레드에서 치르는 시간 —
 *   [ApplyWaitService] 의 onStartCommand 와 화면의 onStop 에서 [measureRestOfMainMessage] 로 잰다.
 */

/** 7번 카드가 쓰는 파일 — 모든 방식이 같은 이름을 쓰고 실험마다 지운다 */
const val BOUNDARY_PREFS = "datastore_demo_boundary"
const val BOUNDARY_KEY = "count"

fun boundaryFile(context: Context) = File(File(context.filesDir, "datastore_demo"), "boundary.settings")

fun boundaryPrefsFile(context: Context) = File(File(context.applicationInfo.dataDir, "shared_prefs"), "$BOUNDARY_PREFS.xml")

/** 두 프로세스가 모두 닫은 뒤 — SP 파일·프로세스 캐시와 DataStore 파일·잠금·버전 파일을 지운다 */
fun resetBoundary(context: Context) {
    context.deleteSharedPreferences(BOUNDARY_PREFS)
    val file = boundaryFile(context)
    listOf(file, File(file.path + ".lock"), File(file.path + ".version")).forEach { it.delete() }
}

/** 프로세스 캐시를 거치지 않고 파일 그대로 읽은 값 */
fun boundaryOnDisk(context: Context, mode: BoundaryMode): Int? = when (mode) {
    BoundaryMode.PREFS -> boundaryPrefsFile(context).takeIf { it.exists() }?.readText()?.let { readPrefsInt(it, BOUNDARY_KEY) }
    else -> boundaryFile(context).takeIf { it.exists() }?.readText()?.let { parseSettings(it)?.count }
}

/** 방식에 맞는 DataStore 를 열고, 끝나면 scope 를 끝내 파일을 놓는다 — 프로세스마다 파일당 하나 규칙은 그대로다 */
suspend fun <R> withBoundaryStore(context: Context, mode: BoundaryMode, block: suspend (DataStore<DemoSettings>) -> R): R {
    val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.IO + job)
    try {
        val file = boundaryFile(context).also { it.parentFile?.mkdirs() }
        val store = if (mode == BoundaryMode.MULTI) {
            // 파일 옆에 .lock(FileChannel 배타 잠금)과 .version(공유 카운터)을 만든다
            MultiProcessDataStoreFactory.create(serializer = DemoSettingsSerializer, scope = scope, produceFile = { file })
        } else {
            // FileStorage 기본 coordinator — 잠금은 프로세스 안 Mutex, 버전은 프로세스 안 정수
            DataStore.Builder(FileStorage(DemoSettingsSerializer) { file }, scope.coroutineContext).build()
        }
        return block(store)
    } finally {
        withContext(NonCancellable) { job.cancelAndJoin() }
    }
}

/** 한 프로세스 몫 — startAt(부팅 후 경과 ms, 두 프로세스가 같은 시계를 본다)까지 기다렸다가 count 번 +1 */
suspend fun runBoundarySide(context: Context, mode: BoundaryMode, count: Int, startAt: Long): BoundarySide {
    delay((startAt - SystemClock.uptimeMillis()).coerceAtLeast(0))
    return incrementAndReport(context, mode, count, intervalMillis = 0)
}

/** 원격 쪽 연속 쓰기 — 기본 프로세스가 이 쓰기들을 알아채는지 본다 */
suspend fun runBoundarySequence(context: Context, mode: BoundaryMode, count: Int, intervalMillis: Long): BoundarySide =
    incrementAndReport(context, mode, count, intervalMillis)

private suspend fun incrementAndReport(context: Context, mode: BoundaryMode, count: Int, intervalMillis: Long): BoundarySide {
    val started = SystemClock.uptimeMillis()
    val tally = Tally()
    var last: Int? = null
    when (mode) {
        BoundaryMode.PREFS -> {
            val prefs = context.getSharedPreferences(BOUNDARY_PREFS, Context.MODE_PRIVATE)
            // commit — 매번 디스크까지 쓰지만, 읽는 쪽은 이 프로세스의 메모리 Map 이다
            repeat(count) {
                tally.attempt { prefs.edit(commit = true) { putInt(BOUNDARY_KEY, prefs.getInt(BOUNDARY_KEY, 0) + 1) } }
                if (intervalMillis > 0) delay(intervalMillis)
            }
            last = prefs.getInt(BOUNDARY_KEY, 0)
        }
        else -> withBoundaryStore(context, mode) { store ->
            repeat(count) {
                tally.attempt { store.updateData { it.copy(count = it.count + 1) } }
                if (intervalMillis > 0) delay(intervalMillis)
            }
            tally.attempt { last = store.data.first().count }
        }
    }
    return BoundarySide(processLabel(), last, SystemClock.uptimeMillis() - started, tally.failed, tally.firstError)
}

/**
 * 실패를 세고 계속한다 — 단일 프로세스용 DataStore 를 두 프로세스가 쓰면 같은 .tmp 파일을 두고 엇갈려
 * 쓰기 자체가 IOException("Unable to rename … .tmp") 으로 실패한다(실측)
 */
private class Tally {
    var failed = 0
    var firstError: String? = null

    suspend fun attempt(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            failed++
            if (firstError == null) firstError = shortenPaths(describeFailure(e))
        }
    }
}

private fun processLabel(): String = currentProcessName()?.shortProcessName() ?: "?"

/** 7번 카드 Messenger 규약 — 요청 번호는 arg1 */
object BoundaryProtocol {
    const val MSG_RACE = 1
    const val MSG_SEQUENCE = 2

    const val KEY_MODE = "mode"
    const val KEY_COUNT = "count"
    const val KEY_START_AT = "startAt"
    const val KEY_INTERVAL = "interval"
    const val KEY_PROCESS = "process"
    const val KEY_LAST = "last"
    const val KEY_MILLIS = "millis"
    const val KEY_FAILED = "failed"
    const val KEY_FIRST_ERROR = "firstError"
    const val KEY_ERROR = "error"

    fun sideToBundle(side: BoundarySide) = Bundle().apply {
        putString(KEY_PROCESS, side.process)
        side.lastSeen?.let { putInt(KEY_LAST, it) }
        putLong(KEY_MILLIS, side.millis)
        putInt(KEY_FAILED, side.failed)
        putString(KEY_FIRST_ERROR, side.firstError)
    }

    /** 원격이 요청 전체의 실패를 돌려보냈으면 그 설명으로 예외 */
    fun sideFromBundle(bundle: Bundle): BoundarySide {
        bundle.getString(KEY_ERROR)?.let { throw IllegalStateException("원격: $it") }
        return BoundarySide(
            process = bundle.getString(KEY_PROCESS).orEmpty(),
            lastSeen = if (bundle.containsKey(KEY_LAST)) bundle.getInt(KEY_LAST) else null,
            millis = bundle.getLong(KEY_MILLIS),
            failed = bundle.getInt(KEY_FAILED),
            firstError = bundle.getString(KEY_FIRST_ERROR)
        )
    }
}

/**
 * `:remote` 프로세스에서 같은 파일을 여는 쪽(매니페스트 android:process=":remote").
 * Application.onCreate 는 이 프로세스에서도 돌지만 Koin 은 기본 프로세스에서만 시작한다 — 여기서는 Koin 을 쓰지 않는다.
 */
class DataStoreRemoteService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val messenger = Messenger(Handler(Looper.getMainLooper()) { msg -> handle(msg); true })

    override fun onBind(intent: Intent): IBinder = messenger.binder

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun handle(msg: Message) {
        // Message 는 핸들러가 돌아가면 재활용된다 — 코루틴으로 넘기기 전에 값을 꺼내 둔다
        val what = msg.what
        val id = msg.arg1
        val data = Bundle(msg.data)
        val replyTo = msg.replyTo ?: return
        scope.launch {
            val reply = Message.obtain(null, what, id, 0)
            reply.data = try {
                val mode = BoundaryMode.entries[data.getInt(BoundaryProtocol.KEY_MODE)]
                val count = data.getInt(BoundaryProtocol.KEY_COUNT)
                val side = when (what) {
                    BoundaryProtocol.MSG_RACE -> {
                        // 이 프로세스의 SP 캐시(앞 실험의 값)를 비운다 — 파일은 기본 프로세스가 이미 지웠다
                        if (mode == BoundaryMode.PREFS) deleteSharedPreferences(BOUNDARY_PREFS)
                        runBoundarySide(applicationContext, mode, count, data.getLong(BoundaryProtocol.KEY_START_AT))
                    }
                    else -> runBoundarySequence(applicationContext, mode, count, data.getLong(BoundaryProtocol.KEY_INTERVAL))
                }
                BoundaryProtocol.sideToBundle(side)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Bundle().apply { putString(BoundaryProtocol.KEY_ERROR, describeFailure(e)) }
            }
            // 기본 프로세스가 이미 사라졌으면 보낼 곳이 없다
            runCatching { replyTo.send(reply) }
        }
    }
}

/**
 * 지금 메인 스레드가 처리 중인 메시지가 끝날 때까지 걸린 시간 — 큐 맨 앞에 표시를 꽂고 그것이 실행된 시각과의 차이.
 * onStartCommand·onStop(의 상태 저장 뒤) 안에서 부르면 그 뒤 ActivityThread 가 같은 메시지 안에서 하는 일
 * (QueuedWork.waitToFinish 포함)이 들어간다.
 */
fun measureRestOfMainMessage(onResult: (Long) -> Unit) {
    val started = SystemClock.uptimeMillis()
    Handler(Looper.getMainLooper()).postAtFrontOfQueue { onResult(SystemClock.uptimeMillis() - started) }
}

/** 기본 프로세스의 시작형 서비스 — ActivityThread.handleServiceArgs 는 onStartCommand 직후 QueuedWork.waitToFinish 를 부른다 */
class ApplyWaitService : Service() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pending = intent?.getIntExtra(EXTRA_PENDING, 0) ?: 0
        val queuedAt = intent?.getLongExtra(EXTRA_QUEUED_AT, 0L) ?: 0L
        val since = if (queuedAt > 0) SystemClock.uptimeMillis() - queuedAt else null
        measureRestOfMainMessage { millis ->
            ApplyWaitProbe.results.tryEmit(WaitResult("서비스 시작(onStartCommand)", pending, millis, since))
            stopSelf(startId)
        }
        return START_NOT_STICKY
    }

    companion object {
        const val EXTRA_PENDING = "pending"
        const val EXTRA_QUEUED_AT = "queuedAt"
    }
}

/** 서비스가 잰 값을 같은 프로세스의 ViewModel 로 넘기는 통로 */
object ApplyWaitProbe {
    val results = MutableSharedFlow<WaitResult>(extraBufferCapacity = 4)
}
