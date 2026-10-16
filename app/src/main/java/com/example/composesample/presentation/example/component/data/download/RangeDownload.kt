package com.example.composesample.presentation.example.component.data.download

import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.header
import io.ktor.client.request.prepareRequest
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicLongArray
import java.util.concurrent.atomic.AtomicReference
import java.util.zip.CRC32

/**
 * 이어받기·분할 다운로드(HTTP Range)의 엔진 — 안드로이드 클래스 없이 JVM 단위 테스트(RangeDownloadTest)로 검증한다.
 *
 * 출처: linroid/Ketch docs "How a download works"(Resolve → Plan → Download → Throttle → Persist → Complete → Resume).
 * Ketch 라이브러리는 쓰지 않고, 같은 단계를 Ktor 클라이언트 + RandomAccessFile 로 직접 구현한다.
 * 서버는 [FakeFileServer] 를 MockEngine 에 물려 흉내 낸다 — Range·If-Range·ETag 를 지키고, Range 미지원·HEAD 거부·
 * 응답 도중 끊김·파일 교체를 스위치로 재현한다.
 */

const val RANGE_FILE_URL = "https://files.example/sample.bin"
const val RANGE_FILE_SIZE = 2 * 1024 * 1024
private const val CHUNK = 16 * 1024

// ==================== 가짜 서버 ====================

data class ServerHit(
    val seq: Int,
    val method: String,
    val range: String?,
    val ifRange: String?,
    val status: Int,
    val bodyBytes: Int,
    val note: String
)

/**
 * 파일 하나를 내주는 서버. 버전마다 내용이 거의 모든 바이트에서 다르고(섞이면 어느 버전과도 CRC 가 안 맞는다), ETag 는 "v<버전>".
 * 여러 연결이 동시에 부르므로 [handle] 은 한 번에 하나씩 같은 버전을 보고 답한다.
 */
class FakeFileServer(private val size: Int = RANGE_FILE_SIZE) {
    @Volatile var rangeSupported = true
    @Volatile var headAllowed = true

    /** 0 보다 크면 응답 본문을 이 바이트까지만 보내고 끊는다(Content-Length 는 원래 길이 그대로) */
    @Volatile var dropAfterBytes = 0

    /** true 면 다음 Range GET 을 처리하기 직전에 파일을 바꾼다 — "확인과 요청 사이" 경쟁 창 재현 */
    @Volatile var replaceOnNextRangeGet = false

    var onHit: (ServerHit) -> Unit = {}

    // (content 초기화가 쓰므로 그보다 먼저 선언) 버전별 내용·CRC 는 결정적이라 한 번만 만든다 — 판정마다 2MiB 를 다시 만들면 시나리오 하나가 수 초씩 걸렸다
    private val contents = ConcurrentHashMap<Int, ByteArray>()
    private val crcs = ConcurrentHashMap<Int, Long>()

    @Volatile var version = 1
        private set
    private var content: ByteArray = contentOfVersion(1)
    val hits = CopyOnWriteArrayList<ServerHit>()
    private val seq = AtomicInteger()

    // @Synchronized 대신 synchronized 블록 — debug(HotSwan) 재작성 아래에서는 @Synchronized 메서드의 상호 배제가 사라졌다
    // (단위 테스트 실측: 동시 요청이 한꺼번에 들어와 교체 전 내용을 읽음. dispatchRewriteEnabled=false 거나 블록이면 정상)
    private val lock = Any()

    val etag: String get() = "\"v$version\""
    val lastModified: String get() = "Thu, %02d Oct 2026 00:00:00 GMT".format(java.util.Locale.ROOT, version)

    fun replaceFile() = synchronized(lock) {
        version++
        content = contentOfVersion(version)
    }

    fun reset() = synchronized(lock) {
        version = 1
        content = contentOfVersion(1)
        rangeSupported = true
        headAllowed = true
        dropAfterBytes = 0
        replaceOnNextRangeGet = false
        hits.clear()
        seq.set(0)
    }

    class Reply(val status: Int, val headers: List<Pair<String, String>>, val body: ByteArray)

    /** 여러 연결이 동시에 부른다 — 한 요청은 한 버전만 본다 */
    fun handle(method: String, header: (String) -> String?): Reply = synchronized(lock) { handleLocked(method, header) }

    private fun handleLocked(method: String, header: (String) -> String?): Reply {
        val range = header(HttpHeaders.Range)
        val ifRange = header(HttpHeaders.IfRange)
        var note = ""
        if (method == "HEAD") {
            if (!headAllowed) return record(method, range, ifRange, Reply(405, listOf(HttpHeaders.Allow to "GET"), ByteArray(0)), "HEAD 거부")
            return record(method, range, ifRange, Reply(200, baseHeaders() + (HttpHeaders.ContentLength to "$size"), ByteArray(0)), "")
        }
        if (range != null && replaceOnNextRangeGet) {
            replaceOnNextRangeGet = false
            replaceFile()
            note = "처리 직전 파일 교체 → v$version · "
        }
        val full = Reply(200, baseHeaders() + (HttpHeaders.ContentLength to "$size"), content)
        val reply = when {
            range == null -> full.also { note += "Range 없음 → 전체" }
            !rangeSupported -> full.also { note += "Range 미지원 → 전체" }
            ifRange != null && ifRange != etag && ifRange != lastModified -> full.also { note += "If-Range $ifRange ≠ $etag → 전체" }
            else -> {
                val parsed = parseRange(range, size.toLong())
                if (parsed == null) {
                    Reply(416, baseHeaders() + (HttpHeaders.ContentRange to "bytes */$size"), ByteArray(0)).also { note += "범위 밖" }
                } else {
                    val (start, end) = parsed
                    Reply(
                        206,
                        baseHeaders() + listOf(
                            HttpHeaders.ContentRange to "bytes $start-$end/$size",
                            HttpHeaders.ContentLength to "${end - start + 1}"
                        ),
                        content.copyOfRange(start.toInt(), end.toInt() + 1)
                    )
                }
            }
        }
        val cut = dropAfterBytes
        val sent = if (cut > 0 && reply.body.size > cut) {
            note += " · ${cut / 1024}KiB 후 끊김"
            Reply(reply.status, reply.headers, reply.body.copyOf(cut))
        } else reply
        return record(method, range, ifRange, sent, note)
    }

    private fun baseHeaders(): List<Pair<String, String>> = buildList {
        add(HttpHeaders.ETag to etag)
        add(HttpHeaders.LastModified to lastModified)
        if (rangeSupported) add(HttpHeaders.AcceptRanges to "bytes")
    }

    private fun record(method: String, range: String?, ifRange: String?, reply: Reply, note: String): Reply {
        val hit = ServerHit(seq.incrementAndGet(), method, range, ifRange, reply.status, reply.body.size, note)
        hits += hit
        onHit(hit)
        return reply
    }

    /** 버전별 내용의 CRC32 — 받은 파일이 어느 버전인지(또는 섞였는지) 판정한다 */
    fun crcOfVersion(v: Int): Long = crcs.getOrPut(v) { crcOf(contentOfVersion(v)) }

    fun contentOfVersion(v: Int): ByteArray = contents.getOrPut(v) { contentOf(v, size) }

    /** MockEngine 에 물리는 엔진 — 요청마다 [handle] 로 답한다 */
    fun engine(): MockEngine = MockEngine { request ->
        val reply = handle(request.method.value) { request.headers[it] }
        respond(
            content = reply.body,
            status = HttpStatusCode.fromValue(reply.status),
            headers = headersOf(*reply.headers.map { it.first to listOf(it.second) }.toTypedArray())
        )
    }
}

fun contentOf(version: Int, size: Int): ByteArray {
    val bytes = ByteArray(size)
    for (i in 0 until size) bytes[i] = ((i * 31 + version * 101) xor (i ushr 7) xor (version shl 5)).toByte()
    return bytes
}

fun crcOf(bytes: ByteArray): Long = CRC32().apply { update(bytes) }.value

fun crcOf(file: File): Long = CRC32().apply { update(file.readBytes()) }.value

/** "bytes=a-b" · "bytes=a-" 를 [start, end] 로 — 만족할 수 없으면 null(416) */
fun parseRange(header: String, size: Long): Pair<Long, Long>? {
    val m = Regex("""bytes=(\d+)-(\d*)""").matchEntire(header.trim()) ?: return null
    val start = m.groupValues[1].toLong()
    val end = m.groupValues[2].takeIf { it.isNotEmpty() }?.toLong()?.coerceAtMost(size - 1) ?: (size - 1)
    return if (start >= size || start > end) null else start to end
}

/** "bytes a-b/total" 의 시작과 전체 크기 */
fun parseContentRange(header: String?): Pair<Long, Long>? {
    val m = Regex("""bytes (\d+)-(\d+)/(\d+)""").matchEntire(header?.trim() ?: return null) ?: return null
    return m.groupValues[1].toLong() to m.groupValues[3].toLong()
}

// ==================== 계획 · 상태 ====================

data class SegmentState(val start: Long, val end: Long, val done: Long) {
    val length: Long get() = end - start + 1
    val complete: Boolean get() = done >= length
}

data class DownloadState(
    val url: String,
    val size: Long,
    val etag: String?,
    val lastModified: String?,
    val rangeSupported: Boolean,
    val segments: List<SegmentState>
) {
    val doneBytes: Long get() = segments.sumOf { it.done.coerceAtMost(it.length) }
}

/** 크기를 연결 수만큼 나눈다 — Range 미지원이면 연결 하나로 처음부터 끝까지 */
fun planSegments(size: Long, rangeSupported: Boolean, connections: Int): List<SegmentState> {
    val parts = if (rangeSupported) connections.coerceIn(1, 16).toLong().coerceAtMost(size) else 1L
    val base = size / parts
    return (0 until parts).map { i ->
        val start = i * base
        val end = if (i == parts - 1) size - 1 else start + base - 1
        SegmentState(start, end, 0)
    }
}

/**
 * 상태 파일(JSON)의 저장 모델 — 앱이 쓴 파일이어도 이전 버전·중간에 끊긴 쓰기로 필드가 빠질 수 있다.
 * Gson 은 Kotlin 기본값·null 안전을 무시하므로(DataRules.md) 전부 nullable 로 읽고 [toDownloadState] 가 검증한다.
 */
data class StoredDownloadState(
    val url: String?,
    val size: Long?,
    val etag: String?,
    val lastModified: String?,
    val rangeSupported: Boolean?,
    val segments: List<StoredSegment?>?
)

data class StoredSegment(val start: Long?, val end: Long?, val done: Long?)

/** 깨진 상태 파일이면 null — 호출부는 처음부터 받는다(Ketch 의 CorruptResumeState 와 같은 결론) */
fun StoredDownloadState.toDownloadState(): DownloadState? {
    val url = url ?: return null
    val size = size?.takeIf { it > 0 } ?: return null
    val segments = segments?.map { s ->
        SegmentState(s?.start ?: return null, s.end ?: return null, s.done ?: return null)
    }?.takeIf { it.isNotEmpty() } ?: return null
    return DownloadState(url, size, etag, lastModified, rangeSupported ?: false, segments)
}

fun DownloadState.toStored() = StoredDownloadState(
    url, size, etag, lastModified, rangeSupported, segments.map { StoredSegment(it.start, it.end, it.done) }
)

// ==================== 속도 제한 ====================

/**
 * 토큰 버킷 — 초당 [bytesPerSecond] 만큼 채워지고, 연결 전부가 같은 버킷에서 꺼낸다(Ketch 의 전역 버킷).
 * 모자라면 일단 꺼내 빚을 지고 빚을 갚는 시간만큼 쉰다 — 청크가 버킷보다 커도 멈추지 않는다.
 */
class TokenBucket(
    private val bytesPerSecond: Long,
    private val capacity: Long = (bytesPerSecond / 4).coerceAtLeast(CHUNK.toLong()),
    private val nanoTime: () -> Long = System::nanoTime,
    private val sleep: suspend (Long) -> Unit = { delay(it) }
) {
    private val mutex = Mutex()
    private var tokens = capacity.toDouble()
    private var last = nanoTime()

    suspend fun acquire(bytes: Int) {
        val waitMs = mutex.withLock {
            val now = nanoTime()
            tokens = (tokens + (now - last) / 1e9 * bytesPerSecond).coerceAtMost(capacity.toDouble())
            last = now
            tokens -= bytes
            if (tokens >= 0) 0L else (-tokens * 1000.0 / bytesPerSecond).toLong().coerceAtLeast(1L)
        }
        if (waitMs > 0) sleep(waitMs)
    }
}

// ==================== 다운로더 ====================

/** 재개할 때 서버 파일이 그대로인지 확인하는 방법 */
enum class ResumeCheck(val label: String) {
    IF_RANGE("If-Range"),
    HEAD_COMPARE("HEAD 로 ETag 비교 후 Range"),
    NONE("확인 없이 Range 만")
}

data class ClientOptions(
    val connections: Int = 4,
    /** 0 = 무제한 */
    val bytesPerSecond: Long = 512 * 1024,
    val resumeCheck: ResumeCheck = ResumeCheck.IF_RANGE,
    /** false = 함정: 200(전체)도 206(부분)처럼 요청한 위치에 이어 쓴다 */
    val checkStatus: Boolean = true,
    /** false = 함정: 본문이 일찍 끝나도(끊김) 그 구간을 완료로 본다 */
    val checkLength: Boolean = true,
    val maxRetriesPerSegment: Int = 8,
    /** 받은 비율이 이 값을 넘으면 스스로 일시정지 — 시나리오를 결정적으로 만든다 */
    val pauseAtFraction: Float? = null
)

data class ResolveResult(
    val viaHead: Boolean,
    val status: Int,
    val size: Long?,
    val acceptRanges: Boolean,
    val etag: String?,
    val lastModified: String?,
    val lines: List<String>
)

sealed interface RunOutcome {
    data class Completed(val size: Long, val crc: Long, val restarts: Int, val retries: Int, val receivedBytes: Long) : RunOutcome
    data class Paused(val state: DownloadState) : RunOutcome
    data class Failed(val reason: String) : RunOutcome
}

/**
 * 200 을 받았거나 범위가 어긋나 처음부터 다시 받아야 할 때. RuntimeException 이어야 한다 — checked 예외를 람다에서 던지면
 * debug(HotSwan)에서 UndeclaredThrowableException 으로 감싸여 catch 가 받지 못한다(ComposeHotReloadGuide, #130).
 */
private class RestartNeeded(message: String) : RuntimeException(message)

class RangeDownloader(
    private val client: HttpClient,
    dir: File,
    name: String = "sample.bin",
    private val log: (String) -> Unit = {},
    private val onProgress: (List<SegmentState>) -> Unit = {}
) {
    val partFile = File(dir, "$name.part")
    val stateFile = File(dir, "$name.state")
    val finalFile = File(dir, name)
    private val gson = Gson()
    private val pauseRequested = AtomicBoolean(false)
    private val receivedBytes = AtomicLong()
    private val stateLock = Any()

    fun requestPause() = pauseRequested.set(true)

    /** 크기·Range 지원·ETag·Last-Modified 조회 — HEAD 를 거부하면 첫 1바이트 GET(Range: bytes=0-0) */
    suspend fun resolve(url: String): ResolveResult {
        val head = client.request(url) { method = HttpMethod.Head }
        if (head.status.value in 200..299) {
            return ResolveResult(
                viaHead = true,
                status = head.status.value,
                size = head.headers[HttpHeaders.ContentLength]?.toLongOrNull(),
                acceptRanges = head.headers[HttpHeaders.AcceptRanges] == "bytes",
                etag = head.headers[HttpHeaders.ETag],
                lastModified = head.headers[HttpHeaders.LastModified],
                lines = headerLines("HEAD", head)
            )
        }
        log("HEAD → ${head.status.value} · 첫 1바이트 GET 으로 대신")
        val probe = client.request(url) {
            method = HttpMethod.Get
            header(HttpHeaders.Range, "bytes=0-0")
        }
        val probeBytes = probe.bodyAsBytes().size
        receivedBytes.addAndGet(probeBytes.toLong())
        val contentRange = parseContentRange(probe.headers[HttpHeaders.ContentRange])
        val partial = probe.status.value == 206 && contentRange != null
        return ResolveResult(
            viaHead = false,
            status = probe.status.value,
            size = if (partial) contentRange?.second else probe.headers[HttpHeaders.ContentLength]?.toLongOrNull(),
            acceptRanges = partial,
            etag = probe.headers[HttpHeaders.ETag],
            lastModified = probe.headers[HttpHeaders.LastModified],
            lines = listOf("HEAD → ${head.status.value}") + headerLines("GET bytes=0-0", probe) + "본문 ${probeBytes}B"
        )
    }

    private fun headerLines(label: String, response: HttpResponse): List<String> = buildList {
        add("$label → ${response.status.value}")
        listOf(HttpHeaders.ContentLength, HttpHeaders.ContentRange, HttpHeaders.AcceptRanges, HttpHeaders.ETag, HttpHeaders.LastModified)
            .forEach { name -> response.headers[name]?.let { add("$name: $it") } }
    }

    /** 부분 파일·상태 파일을 지우고 처음부터 */
    suspend fun start(url: String, options: ClientOptions): RunOutcome {
        receivedBytes.set(0)
        return startFresh(url, options, restarts = 0, retries = 0)
    }

    private suspend fun startFresh(url: String, options: ClientOptions, restarts: Int, retries: Int): RunOutcome {
        if (restarts > 3) return RunOutcome.Failed("처음부터 다시 받기가 3번을 넘었다")
        clearPartial()
        val info = resolve(url)
        val size = info.size ?: return RunOutcome.Failed("크기를 모름 — 이어받기 불가")
        val state = DownloadState(url, size, info.etag, info.lastModified, info.acceptRanges, planSegments(size, info.acceptRanges, options.connections))
        withContext(Dispatchers.IO) {
            RandomAccessFile(partFile, "rw").use { it.setLength(size) } // 미리 크기를 잡아 두면 각 연결이 제 위치에 바로 쓴다
        }
        saveState(state)
        log("시작: ${size / 1024}KiB · 구간 ${state.segments.size}개 · ETag ${state.etag}")
        return run(state, options, restarts, retries)
    }

    /** 상태 파일에서 이어받는다 — 확인 방법은 [ClientOptions.resumeCheck] */
    suspend fun resume(options: ClientOptions): RunOutcome {
        receivedBytes.set(0)
        val state = loadState() ?: return RunOutcome.Failed("상태 파일이 없거나 깨졌다 — 처음부터 받아야 한다")
        if (!partFile.exists()) return startFresh(state.url, options, restarts = 1, retries = 0)
        if (!state.rangeSupported && options.checkStatus) {
            log("Range 미지원 서버였다 — 처음부터")
            return startFresh(state.url, options, restarts = 1, retries = 0)
        }
        if (options.resumeCheck == ResumeCheck.HEAD_COMPARE) {
            val info = resolve(state.url)
            if (info.etag != state.etag) {
                log("HEAD: ETag ${state.etag} → ${info.etag} — 처음부터")
                return startFresh(state.url, options, restarts = 1, retries = 0)
            }
            log("HEAD: ETag ${info.etag} 그대로 — 이어받기")
        }
        log("이어받기: ${state.doneBytes / 1024}KiB 부터")
        return run(state, options, restarts = 0, retries = 0)
    }

    private suspend fun run(initial: DownloadState, options: ClientOptions, restarts: Int, retries: Int): RunOutcome {
        pauseRequested.set(false)
        val bucket = if (options.bytesPerSecond > 0) TokenBucket(options.bytesPerSecond) else null
        val done = AtomicLongArray(initial.segments.map { it.done }.toLongArray())
        val retryCount = AtomicInteger(retries)
        val restart = AtomicReference<String?>(null)
        val failure = AtomicReference<String?>(null)
        val pauseAt = options.pauseAtFraction?.let { (initial.size * it).toLong() }
        val total = AtomicLong(initial.doneBytes)
        val lastSave = AtomicLong(0)

        fun snapshot() = initial.copy(segments = initial.segments.mapIndexed { i, s -> s.copy(done = done.get(i)) })
        fun stopping() = pauseRequested.get() || restart.get() != null || failure.get() != null

        withContext(Dispatchers.IO) {
            RandomAccessFile(partFile, "rw").use { raf ->
                val channel = raf.channel
                coroutineScope {
                    initial.segments.forEachIndexed { i, seg ->
                        if (seg.complete) return@forEachIndexed
                        launch {
                            var attempts = 0
                            while (!stopping() && done.get(i) < seg.length) {
                                val from = seg.start + done.get(i)
                                val expected = seg.end - from + 1
                                var got = 0L
                                try {
                                    client.prepareRequest(initial.url) {
                                        method = HttpMethod.Get
                                        if (initial.rangeSupported || !options.checkStatus) header(HttpHeaders.Range, "bytes=$from-${seg.end}")
                                        // 알고 있는 ETag 를 모든 범위 요청에 붙인다 — 서버가 바뀌었으면 206 대신 200(전체)이 온다
                                        if (options.resumeCheck == ResumeCheck.IF_RANGE && initial.etag != null) {
                                            header(HttpHeaders.IfRange, initial.etag)
                                        }
                                    }.execute { response ->
                                        val status = response.status.value
                                        val wholeFile = from == 0L && seg.end == initial.size - 1
                                        when {
                                            status == 206 -> {
                                                val start = parseContentRange(response.headers[HttpHeaders.ContentRange])?.first
                                                if (options.checkStatus && start != from) throw RestartNeeded("206 인데 시작 위치가 $start (요청 $from)")
                                            }
                                            status == 200 && wholeFile -> Unit // 처음부터 통째로 받는 연결은 200 이 정상
                                            status == 200 && options.checkStatus ->
                                                throw RestartNeeded("구간 $i 요청에 200(전체) — 서버가 범위를 무시했다")
                                            status == 200 -> log("구간 $i: 200 을 206 처럼 위치 $from 에 이어 쓴다(함정)")
                                            status == 416 -> throw RestartNeeded("416 범위 밖 — 서버 파일이 바뀌었다")
                                            else -> {
                                                failure.compareAndSet(null, "구간 $i: HTTP $status")
                                                return@execute
                                            }
                                        }
                                        val body = response.bodyAsChannel()
                                        val buf = ByteArray(CHUNK)
                                        var position = from
                                        while (!stopping()) {
                                            val n = body.readAvailable(buf, 0, buf.size)
                                            if (n < 0) break
                                            if (n == 0) continue
                                            bucket?.acquire(n)
                                            channel.writeFully(buf, n, position)
                                            position += n
                                            got += n
                                            receivedBytes.addAndGet(n.toLong())
                                            // 구간 진행은 구간 길이까지만 센다(함정에서 200 전체를 이어 쓰면 넘친다)
                                            val counted = (done.get(i) + n).coerceAtMost(seg.length)
                                            done.set(i, counted)
                                            val now = total.addAndGet(n.toLong())
                                            if (pauseAt != null && now >= pauseAt) pauseRequested.set(true)
                                            onProgress(snapshot().segments)
                                            val t = System.nanoTime()
                                            val previous = lastSave.get()
                                            if (t - previous > 300_000_000L && lastSave.compareAndSet(previous, t)) {
                                                saveState(snapshot())
                                            }
                                        }
                                    }
                                } catch (e: RestartNeeded) {
                                    restart.compareAndSet(null, e.message)
                                } catch (e: CancellationException) {
                                    throw e
                                } catch (e: Exception) {
                                    // 연결이 도중에 끊기면 엔진에 따라 IOException(OkHttp 의 unexpected end of stream 등)이 난다 — 원인 사슬로 가른다
                                    if (generateSequence<Throwable>(e) { it.cause }.none { it is IOException }) throw e
                                    log("구간 $i: 읽기 오류 ${e.javaClass.simpleName}")
                                }
                                if (stopping()) break
                                if (got < expected && done.get(i) < seg.length) {
                                    if (!options.checkLength) {
                                        log("구간 $i: ${got / 1024}KiB 만 받고 끝났는데 완료로 본다(함정)")
                                        done.set(i, seg.length)
                                        break
                                    }
                                    attempts++
                                    retryCount.incrementAndGet()
                                    if (attempts > options.maxRetriesPerSegment) {
                                        failure.compareAndSet(null, "구간 $i: 재시도 ${options.maxRetriesPerSegment}회 초과")
                                        break
                                    }
                                    log("구간 $i: ${got / 1024}KiB 에서 끊김 → ${(seg.start + done.get(i)) / 1024}KiB 부터 다시(${attempts}회)")
                                }
                            }
                        }
                    }
                }
            }
        }

        val state = snapshot()
        saveState(state)
        restart.get()?.let { reason ->
            log("처음부터: $reason")
            return startFresh(initial.url, options, restarts + 1, retryCount.get())
        }
        failure.get()?.let { return RunOutcome.Failed(it) }
        if (pauseRequested.get() && state.segments.any { !it.complete }) {
            log("일시정지: ${state.doneBytes / 1024}KiB / ${state.size / 1024}KiB")
            return RunOutcome.Paused(state)
        }
        return withContext(Dispatchers.IO) {
            // 완료: 부분 파일을 최종 이름으로 옮기고 상태 파일을 지운다
            finalFile.delete()
            if (!partFile.renameTo(finalFile)) return@withContext RunOutcome.Failed("이름 바꾸기 실패")
            stateFile.delete()
            log("완료: ${finalFile.length() / 1024}KiB · 처음부터 ${restarts}회 · 재시도 ${retryCount.get()}회")
            RunOutcome.Completed(finalFile.length(), crcOf(finalFile), restarts, retryCount.get(), receivedBytes.get())
        }
    }

    /** 취소: 부분 파일과 상태 파일을 지운다(일시정지는 둘 다 남긴다) */
    fun clearPartial() {
        partFile.delete()
        stateFile.delete()
    }

    fun clearAll() {
        clearPartial()
        finalFile.delete()
    }

    private fun saveState(state: DownloadState) = synchronized(stateLock) {
        val tmp = File(stateFile.parentFile, stateFile.name + ".tmp")
        tmp.writeText(gson.toJson(state.toStored()))
        tmp.renameTo(stateFile) // 쓰다 끊겨도 이전 상태 파일이 남도록 임시 파일을 옮긴다
    }

    fun loadState(): DownloadState? = runCatching {
        gson.fromJson(stateFile.readText(), StoredDownloadState::class.java)?.toDownloadState()
    }.getOrNull()

    fun stateJson(): String? = runCatching { stateFile.readText() }.getOrNull()
}

private fun FileChannel.writeFully(buf: ByteArray, length: Int, position: Long) {
    val buffer = ByteBuffer.wrap(buf, 0, length)
    var at = position
    while (buffer.hasRemaining()) at += write(buffer, at)
}

// ==================== 시나리오 — 화면 카드와 단위 테스트가 같은 함수를 쓴다 ====================

/** 서버 파일을 언제 바꾸나 */
enum class ReplaceTiming(val label: String) {
    WHILE_PAUSED("멈춘 동안 교체"),
    AFTER_CHECK("확인과 범위 요청 사이에 교체")
}

/** 받은 응답을 확인하지 않을 때 깨지는 두 경우 */
enum class Trap(val label: String) {
    STATUS_200("Range 미지원 서버의 200 을 206 처럼"),
    EARLY_EOF("도중에 끊긴 응답을 완료로")
}

data class ScenarioResult(
    val title: String,
    val ok: Boolean,
    val verdict: String,
    val size: Long,
    val expectedSize: Long,
    val sameAsV1: Long,
    val sameAsV2: Long,
    val restarts: Int,
    val retries: Int,
    val receivedBytes: Long,
    val requests: String
)

/** 파일이 버전 v 의 내용과 같은 바이트 수 — 섞인 파일이면 앞부분은 v1, 뒷부분은 v2 와 같다 */
fun sameBytes(file: File, expected: ByteArray): Long {
    if (!file.exists()) return 0
    val actual = file.readBytes()
    var same = 0L
    for (i in 0 until minOf(actual.size, expected.size)) if (actual[i] == expected[i]) same++
    return same
}

private fun requestSummary(hits: List<ServerHit>): String =
    hits.joinToString(" · ") { hit ->
        val ifRange = if (hit.ifRange != null) "+If-Range" else ""
        "${hit.method}${if (hit.range != null) "(Range$ifRange)" else ""} ${hit.status}"
    }.let { summary -> if (summary.length > 260) summary.take(260) + " …" else summary }

private fun resultOf(
    title: String,
    outcome: RunOutcome,
    server: FakeFileServer,
    downloader: RangeDownloader,
    expectedVersion: Int,
    hits: List<ServerHit>
): ScenarioResult {
    val expected = server.contentOfVersion(expectedVersion)
    val v1 = sameBytes(downloader.finalFile, server.contentOfVersion(1))
    val v2 = if (server.version >= 2) sameBytes(downloader.finalFile, server.contentOfVersion(2)) else 0L
    return when (outcome) {
        is RunOutcome.Completed -> {
            val ok = outcome.size == expected.size.toLong() && outcome.crc == crcOf(expected)
            val verdict = when {
                ok -> "CRC 가 v$expectedVersion 내용과 일치"
                outcome.size != expected.size.toLong() -> "크기 ${outcome.size} (기대 ${expected.size}) — 손상"
                outcome.crc == server.crcOfVersion(1) -> "v1 과 일치 — 서버의 새 파일을 놓침"
                else -> "크기는 같지만 CRC 불일치 — 손상(섞임·구멍)"
            }
            ScenarioResult(title, ok, verdict, outcome.size, expected.size.toLong(), v1, v2, outcome.restarts, outcome.retries, outcome.receivedBytes, requestSummary(hits))
        }
        is RunOutcome.Paused -> ScenarioResult(title, false, "일시정지로 끝남", 0, expected.size.toLong(), 0, 0, 0, 0, 0, requestSummary(hits))
        is RunOutcome.Failed -> ScenarioResult(title, false, "실패: ${outcome.reason}", 0, expected.size.toLong(), 0, 0, 0, 0, 0, requestSummary(hits))
    }
}

/** 40% 에서 멈춤 → 서버 파일 교체(timing) → check 방식으로 재개 → 결과가 서버의 새 파일(v2)과 같은가 */
suspend fun runResumeScenario(
    server: FakeFileServer,
    downloader: RangeDownloader,
    check: ResumeCheck,
    timing: ReplaceTiming,
    base: ClientOptions = ClientOptions(bytesPerSecond = 0)
): ScenarioResult {
    server.reset()
    downloader.clearAll()
    val options = base.copy(resumeCheck = check)
    val first = downloader.start(RANGE_FILE_URL, options.copy(pauseAtFraction = 0.4f))
    if (first !is RunOutcome.Paused) return resultOf("${check.label} · ${timing.label}", first, server, downloader, 1, server.hits.toList())
    val from = server.hits.size
    when (timing) {
        ReplaceTiming.WHILE_PAUSED -> server.replaceFile()
        ReplaceTiming.AFTER_CHECK -> server.replaceOnNextRangeGet = true
    }
    val outcome = downloader.resume(options)
    return resultOf("${check.label} · ${timing.label}", outcome, server, downloader, 2, server.hits.drop(from))
}

/** 함정 재현 — careful 이면 상태 코드·길이를 확인하는 클라이언트, 아니면 확인을 생략한 클라이언트 */
suspend fun runTrap(
    server: FakeFileServer,
    downloader: RangeDownloader,
    trap: Trap,
    careful: Boolean,
    base: ClientOptions = ClientOptions(bytesPerSecond = 0)
): ScenarioResult {
    server.reset()
    downloader.clearAll()
    val title = "${trap.label} · ${if (careful) "확인함" else "확인 생략"}"
    return when (trap) {
        Trap.STATUS_200 -> {
            server.rangeSupported = false
            val options = base.copy(connections = 1, checkStatus = careful)
            val first = downloader.start(RANGE_FILE_URL, options.copy(pauseAtFraction = 0.4f))
            if (first !is RunOutcome.Paused) return resultOf(title, first, server, downloader, 1, server.hits.toList())
            val from = server.hits.size
            val outcome = downloader.resume(options)
            resultOf(title, outcome, server, downloader, 1, server.hits.drop(from))
        }
        Trap.EARLY_EOF -> {
            server.dropAfterBytes = 300 * 1024
            val outcome = downloader.start(RANGE_FILE_URL, base.copy(connections = 4, checkLength = careful))
            resultOf(title, outcome, server, downloader, 1, server.hits.toList())
        }
    }
}
