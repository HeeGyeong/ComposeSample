package com.example.composesample.presentation.example.component.system.platform.process

import android.Manifest
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.os.SystemClock
import com.example.composesample.util.currentProcessName
import com.example.composesample.util.isMainProcess
import com.example.composesample.util.processNameFromProcFs
import org.koin.core.context.GlobalContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicInteger

/**
 * 멀티프로세스 예제의 서비스 쪽.
 *
 * 같은 코드([ProcessDemoService])를 매니페스트에 세 번 선언해 서로 다른 프로세스에 띄운다.
 * - [LocalProcessDemoService]    : process 지정 없음 → 화면과 같은 기본 프로세스
 * - [RemoteProcessDemoService]   : `android:process=":remote"`
 * - [IsolatedProcessDemoService] : `android:process=":isolated"` + `android:isolatedProcess="true"`
 *
 * 통신은 Messenger 다. 메시지는 전용 HandlerThread 에서 처리한다 — 점검 항목의 소켓 연결이
 * 메인 스레드 네트워크 금지(NetworkOnMainThreadException)에 걸려 결과가 오염되지 않게 하기 위해서다.
 */
open class ProcessDemoService : Service() {

    private lateinit var thread: HandlerThread
    private lateinit var messenger: Messenger

    override fun onCreate() {
        super.onCreate()
        thread = HandlerThread("process-demo").apply { start() }
        messenger = Messenger(Handler(thread.looper) { msg -> handle(msg); true })
    }

    override fun onBind(intent: Intent): IBinder = messenger.binder

    override fun onDestroy() {
        thread.quitSafely()
        super.onDestroy()
    }

    private fun handle(msg: Message) {
        // 응답은 요청과 같은 what·arg1(요청 번호)을 달아 돌려보낸다
        val reply = Message.obtain(null, msg.what, msg.arg1, 0)
        when (msg.what) {
            ProcessDemoProtocol.MSG_SNAPSHOT -> reply.data = ProcessSnapshot.capture().toBundle()
            ProcessDemoProtocol.MSG_INCREMENT -> {
                ProcessLocalState.counter.incrementAndGet()
                reply.data = ProcessSnapshot.capture().toBundle()
            }
            // Bundle 은 꺼낼 때 역직렬화되므로 여기서 실제로 받은 바이트 수를 센다
            ProcessDemoProtocol.MSG_PAYLOAD ->
                reply.arg2 = msg.data.getByteArray(ProcessDemoProtocol.KEY_PAYLOAD)?.size ?: -1
            ProcessDemoProtocol.MSG_PROBE -> reply.data = ProcessProbe.run(applicationContext).toBundle()
            ProcessDemoProtocol.MSG_CRASH -> {
                // 같은 코드가 기본 프로세스에도 떠 있으므로, 거기서는 화면까지 죽지 않게 거부한다
                if (applicationContext.isMainProcess()) {
                    reply.arg2 = -1
                } else {
                    throw IllegalStateException("MultiProcessExample: ${currentProcessName()} 에서 의도적으로 던진 예외")
                }
            }
        }
        // 요청한 쪽 프로세스가 이미 사라졌으면 보낼 곳이 없다 — 이 프로세스까지 죽을 이유는 없으므로 무시한다
        runCatching { msg.replyTo?.send(reply) }
    }
}

/** 기본 프로세스(화면과 같은 프로세스)에 뜨는 서비스 */
class LocalProcessDemoService : ProcessDemoService()

/** `:remote` 프로세스에 뜨는 서비스 — 앱과 같은 UID 의 별도 프로세스 */
class RemoteProcessDemoService : ProcessDemoService()

/** `:isolated` 프로세스에 뜨는 서비스 — 격리 UID, 앱 권한·데이터 접근 없음 */
class IsolatedProcessDemoService : ProcessDemoService()

/** Messenger 메시지 규약 */
object ProcessDemoProtocol {
    const val MSG_SNAPSHOT = 1
    const val MSG_INCREMENT = 2
    const val MSG_PAYLOAD = 3
    const val MSG_PROBE = 4
    const val MSG_CRASH = 5

    const val KEY_PAYLOAD = "payload"

    /** 기본 프로세스가 써 두고 각 프로세스가 읽어 보는 점검용 파일(filesDir 안) */
    const val PROBE_FILE_NAME = "multi_process_probe.txt"
}

/**
 * 프로세스마다 따로 존재하는 싱글턴.
 * 클래스도 코드도 같지만 프로세스가 다르면 메모리가 다르므로, 한쪽에서 올린 값이 다른 쪽에 보이지 않는다.
 */
object ProcessLocalState {
    val counter = AtomicInteger(0)
}

/** 한 프로세스가 스스로 보고하는 자기 상태 */
data class ProcessSnapshot(
    val pid: Int,
    val uid: Int,
    val processName: String?,
    val procFsName: String?,
    val aliveMs: Long,
    val counter: Int,
    val koinStarted: Boolean,
    val isolated: Boolean,
    val pssKb: Long
) {
    fun toBundle(): Bundle = Bundle().apply {
        putInt("pid", pid)
        putInt("uid", uid)
        putString("processName", processName)
        putString("procFsName", procFsName)
        putLong("aliveMs", aliveMs)
        putInt("counter", counter)
        putBoolean("koinStarted", koinStarted)
        putBoolean("isolated", isolated)
        putLong("pssKb", pssKb)
    }

    companion object {
        fun fromBundle(bundle: Bundle) = ProcessSnapshot(
            pid = bundle.getInt("pid"),
            uid = bundle.getInt("uid"),
            processName = bundle.getString("processName"),
            procFsName = bundle.getString("procFsName"),
            aliveMs = bundle.getLong("aliveMs"),
            counter = bundle.getInt("counter"),
            koinStarted = bundle.getBoolean("koinStarted"),
            isolated = bundle.getBoolean("isolated"),
            pssKb = bundle.getLong("pssKb")
        )

        /** 호출한 프로세스 자신의 상태를 잰다 */
        fun capture() = ProcessSnapshot(
            pid = Process.myPid(),
            uid = Process.myUid(),
            processName = currentProcessName(),
            procFsName = processNameFromProcFs(),
            // Process.getStartUptimeMillis() = 이 프로세스가 시작된 시각(부팅 후 경과, API 24)
            aliveMs = SystemClock.uptimeMillis() - Process.getStartUptimeMillis(),
            counter = ProcessLocalState.counter.get(),
            // BaseApplication 의 프로세스 가드가 기본 프로세스에서만 startKoin 을 부른다
            koinStarted = GlobalContext.getOrNull() != null,
            isolated = isIsolatedProcess(),
            // 자기 프로세스의 PSS 는 권한 없이 읽을 수 있다(KB)
            pssKb = runCatching { Debug.getPss() }.getOrDefault(-1L)
        )

        private fun isIsolatedProcess(): Boolean =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Process.isIsolated()
            } else {
                // 격리 프로세스 UID 대역(앱 ID 90000~99999)
                Process.myUid() % 100_000 in 90_000..99_999
            }
    }
}

/**
 * 프로세스가 무엇을 할 수 있는지 점검한다 — 같은 함수를 세 프로세스에서 실행해 결과를 나란히 비교한다.
 * 결과는 성공 여부가 아니라 "무슨 일이 일어났는지"를 문자열로 남긴다(예외 이름·메시지 포함).
 */
object ProcessProbe {

    data class Result(
        val internetPermission: String,
        val fileRead: String,
        val socket: String,
        val visibleProcesses: String
    ) {
        fun toBundle(): Bundle = Bundle().apply {
            putString("internetPermission", internetPermission)
            putString("fileRead", fileRead)
            putString("socket", socket)
            putString("visibleProcesses", visibleProcesses)
        }

        companion object {
            fun fromBundle(bundle: Bundle) = Result(
                internetPermission = bundle.getString("internetPermission").orEmpty(),
                fileRead = bundle.getString("fileRead").orEmpty(),
                socket = bundle.getString("socket").orEmpty(),
                visibleProcesses = bundle.getString("visibleProcesses").orEmpty()
            )
        }
    }

    fun run(context: Context): Result = Result(
        internetPermission = runCatching {
            // 매니페스트의 INTERNET 은 설치 시 자동 허용되는 권한이다 — 그래도 격리 프로세스는 받지 못한다
            if (context.checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
                "GRANTED"
            } else {
                "DENIED"
            }
        }.getOrElse { it.describe() },
        fileRead = runCatching {
            "읽음: \"${File(context.filesDir, ProcessDemoProtocol.PROBE_FILE_NAME).readText()}\""
        }.getOrElse { it.describe() },
        // 아무도 듣지 않는 루프백 포트 — 소켓을 만들 수 있으면 '연결 거부'로 끝나고, 만들 수 없으면 그 전에 막힌다
        socket = runCatching {
            Socket().use { it.connect(InetSocketAddress("127.0.0.1", 9), 500) }
            "연결됨"
        }.getOrElse { it.describe() },
        visibleProcesses = runCatching {
            val am = context.getSystemService(ActivityManager::class.java)
            val list = am.runningAppProcesses.orEmpty()
            "${list.size}개 " + list.joinToString(prefix = "[", postfix = "]") { it.processName.shortProcessName() }
        }.getOrElse { it.describe() }
    )

    private fun Throwable.describe(): String = "${javaClass.simpleName}: ${message.orEmpty()}"
}

/** "com.example.composesample:remote" → ":remote", 기본 프로세스는 "main" */
fun String.shortProcessName(): String = if (contains(':')) ":" + substringAfter(':') else "main"
