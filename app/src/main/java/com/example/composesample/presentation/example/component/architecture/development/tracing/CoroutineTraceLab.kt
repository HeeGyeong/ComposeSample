package com.example.composesample.presentation.example.component.architecture.development.tracing

import android.content.Context
import androidx.tracing.DelicateTracingApi
import androidx.tracing.ExperimentalContextPropagation
import androidx.tracing.Tracer
import androidx.tracing.wire.TraceDriver
import androidx.tracing.wire.TraceSink
import androidx.tracing.wire.createPerfettoFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.Collections
import kotlin.coroutines.resume

const val TRACE_CATEGORY = "composesample"
const val TRACE_DIR = "coroutine_traces"

// ==================== 앱이 직접 쓰는 트레이스 세션 ====================

/**
 * tracing-wire 의 TraceDriver + 파일 TraceSink — 시스템 트레이싱(atrace)이 아니라 앱 프로세스가 Perfetto 프로토 파일을 직접 쓴다.
 * 카테고리를 늘 켜 두어(isCategoryEnabled = true) 캡처 도구 없이도 기록한다. close() 가 큐를 비우고 파일을 닫는다.
 */
class InProcessTraceSession(context: Context, prefix: String) : AutoCloseable {
    val file: File = File(context.filesDir, TRACE_DIR).apply { mkdirs() }.createPerfettoFile(prefix)
    private val driver = TraceDriver(
        context = context,
        sink = TraceSink(context = context, fileProvider = { file }),
        isCategoryEnabled = { true },
    )
    val tracer: Tracer get() = driver.tracer

    override fun close() = driver.close()
}

// ==================== 트레이스 파일 읽기(최소 protobuf 디코더) ====================

const val TRACK_EVENT_SLICE_BEGIN = 1
const val TRACK_EVENT_SLICE_END = 2
const val TRACK_EVENT_INSTANT = 3

data class TraceFileTrack(val uuid: Long, val name: String?, val threadName: String?, val tid: Long?)

data class TraceFileEvent(val type: Int, val trackUuid: Long, val name: String?, val flowIds: List<Long>, val timestamp: Long)

data class TraceFileContent(val packets: Int, val tracks: Map<Long, TraceFileTrack>, val events: List<TraceFileEvent>) {
    fun trackLabel(uuid: Long): String = tracks[uuid]?.let { it.threadName ?: it.name } ?: "track#$uuid"
}

/** protobuf 필드 하나 — LEN 이면 bytes, 그 외는 value(varint · fixed64 · fixed32) */
private class ProtoField(val number: Int, val wireType: Int, val value: Long, val bytes: ByteArray?)

private class ProtoReader(private val buf: ByteArray, private var pos: Int = 0, private val end: Int = buf.size) {
    fun hasMore() = pos < end

    private fun varint(): Long {
        var result = 0L
        var shift = 0
        while (true) {
            val b = buf[pos++].toInt() and 0xFF
            result = result or ((b and 0x7F).toLong() shl shift)
            if (b and 0x80 == 0) return result
            shift += 7
        }
    }

    private fun fixed(bytes: Int): Long {
        var result = 0L
        for (i in 0 until bytes) result = result or ((buf[pos + i].toLong() and 0xFF) shl (8 * i))
        pos += bytes
        return result
    }

    fun next(): ProtoField {
        val key = varint()
        val number = (key ushr 3).toInt()
        return when (val wireType = (key and 7).toInt()) {
            0 -> ProtoField(number, wireType, varint(), null)
            1 -> ProtoField(number, wireType, fixed(8), null)
            2 -> {
                val len = varint().toInt()
                val bytes = buf.copyOfRange(pos, pos + len)
                pos += len
                ProtoField(number, wireType, 0L, bytes)
            }
            5 -> ProtoField(number, wireType, fixed(4), null)
            else -> error("지원하지 않는 wire type $wireType (필드 $number)")
        }
    }
}

/** Trace { repeated TracePacket packet = 1 } 에서 TrackDescriptor(60)·TrackEvent(11)만 읽는다 */
fun parseTraceFile(bytes: ByteArray): TraceFileContent {
    val tracks = linkedMapOf<Long, TraceFileTrack>()
    val events = mutableListOf<TraceFileEvent>()
    var packets = 0
    val top = ProtoReader(bytes)
    while (top.hasMore()) {
        val f = top.next()
        if (f.number != 1 || f.bytes == null) continue
        packets++
        var timestamp = 0L
        var trackEvent: ByteArray? = null
        var trackDescriptor: ByteArray? = null
        val packet = ProtoReader(f.bytes)
        while (packet.hasMore()) {
            val p = packet.next()
            when (p.number) {
                8 -> timestamp = p.value
                11 -> trackEvent = p.bytes
                60 -> trackDescriptor = p.bytes
            }
        }
        trackDescriptor?.let { tracks += parseTrackDescriptor(it).let { t -> t.uuid to t } }
        trackEvent?.let { events += parseTrackEvent(it, timestamp) }
    }
    return TraceFileContent(packets, tracks, events)
}

private fun parseTrackDescriptor(bytes: ByteArray): TraceFileTrack {
    var uuid = 0L
    var name: String? = null
    var threadName: String? = null
    var tid: Long? = null
    val r = ProtoReader(bytes)
    while (r.hasMore()) {
        val f = r.next()
        when (f.number) {
            1 -> uuid = f.value
            2 -> name = f.bytes?.decodeToString()
            4 -> {
                val t = ProtoReader(f.bytes ?: continue)
                while (t.hasMore()) {
                    val g = t.next()
                    when (g.number) {
                        2 -> tid = g.value
                        5 -> threadName = g.bytes?.decodeToString()
                    }
                }
            }
        }
    }
    return TraceFileTrack(uuid, name, threadName, tid)
}

private fun parseTrackEvent(bytes: ByteArray, timestamp: Long): TraceFileEvent {
    var type = 0
    var track = 0L
    var name: String? = null
    val flows = mutableListOf<Long>()
    val r = ProtoReader(bytes)
    while (r.hasMore()) {
        val f = r.next()
        when (f.number) {
            9 -> type = f.value.toInt()
            11 -> track = f.value
            23 -> name = f.bytes?.decodeToString()
            47 -> if (f.bytes != null) {
                // packed 로 올 때(8바이트씩)
                val b = f.bytes
                var i = 0
                while (i + 8 <= b.size) {
                    var v = 0L
                    for (k in 0 until 8) v = v or ((b[i + k].toLong() and 0xFF) shl (8 * k))
                    flows += v
                    i += 8
                }
            } else {
                flows += f.value
            }
        }
    }
    return TraceFileEvent(type, track, name, flows, timestamp)
}

// ==================== 파일 분석 ====================

/** 같은 이름의 구간이 파일에 몇 조각으로, 어느 스레드 트랙에, 어떤 flow id 로 남았나 */
data class SliceStat(val name: String, val pieces: Int, val threads: List<String>, val flowIds: Set<Long>)

/** 트랙마다 BEGIN·END 수 — 다르면 닫히지 않은(또는 남는) 구간이 있다 */
data class TrackBalance(val track: String, val begins: Int, val ends: Int)

fun sliceStats(content: TraceFileContent): Map<String, SliceStat> =
    content.events
        .filter { it.type == TRACK_EVENT_SLICE_BEGIN && it.name != null }
        .groupBy { it.name!! }
        .mapValues { (name, begins) ->
            SliceStat(
                name = name,
                pieces = begins.size,
                threads = begins.map { content.trackLabel(it.trackUuid) }.distinct(),
                flowIds = begins.flatMap { it.flowIds }.toSet(),
            )
        }

fun trackBalances(content: TraceFileContent): List<TrackBalance> =
    content.events
        .filter { it.type == TRACK_EVENT_SLICE_BEGIN || it.type == TRACK_EVENT_SLICE_END }
        .groupBy { it.trackUuid }
        .map { (uuid, evs) ->
            TrackBalance(
                track = content.trackLabel(uuid),
                begins = evs.count { it.type == TRACK_EVENT_SLICE_BEGIN },
                ends = evs.count { it.type == TRACK_EVENT_SLICE_END },
            )
        }

/** 스레드 이름을 짧게 — DefaultDispatcher-worker-3 → worker-3 */
fun shortThread(name: String): String = name.removePrefix("DefaultDispatcher-")

fun describeSlice(stat: SliceStat?): String =
    if (stat == null) "기록 없음"
    else "조각 ${stat.pieces} · 스레드 ${stat.threads.size}(${stat.threads.joinToString(",") { shortThread(it) }}) · flow ${stat.flowIds.size}"

/** 두 구간이 flow id 를 하나라도 공유하면 Perfetto 에서 화살표로 이어진다 */
fun linked(a: SliceStat?, b: SliceStat?): Boolean =
    a != null && b != null && a.flowIds.intersect(b.flowIds).isNotEmpty()

// ==================== 시나리오 ====================

data class CoroutineRun(val index: Int, val beginThread: String, val endThread: String)

/** ① traceCoroutine 구간 8개를 Dispatchers.Default 에서 — 안에 delay 2번과 동기 trace 1개 */
suspend fun runCoroutineSlices(tracer: Tracer, count: Int = 8): List<CoroutineRun> {
    val runs = Collections.synchronizedList(mutableListOf<CoroutineRun>())
    coroutineScope {
        (1..count).map { i ->
            launch(Dispatchers.Default) {
                tracer.traceCoroutine(TRACE_CATEGORY, "load-$i") {
                    val begin = Thread.currentThread().name
                    delay(20L + i * 5)
                    tracer.trace(TRACE_CATEGORY, "parse-$i") { spinMillis(2) }
                    delay(20L)
                    runs += CoroutineRun(i, begin, Thread.currentThread().name)
                }
            }
        }.joinAll()
    }
    return runs.sortedBy { it.index }
}

data class SyncRun(val index: Int, val beginThread: String, val endThread: String, val error: String?)

/** ② 같은 Tracer 의 동기 beginSection 을 delay 너머에서 닫는다 — 기존 카드의 atrace 함정을 tracing 2.0 으로 */
suspend fun runSyncAcrossSuspend(tracer: Tracer, count: Int = 8): List<SyncRun> {
    val runs = Collections.synchronizedList(mutableListOf<SyncRun>())
    coroutineScope {
        (1..count).map { i ->
            launch(Dispatchers.Default) {
                val begin = Thread.currentThread().name
                val section = tracer.beginSection(TRACE_CATEGORY, "sync-$i", token = null) {}
                delay(20L + i * 5)
                val end = Thread.currentThread().name
                val error = try {
                    section.close()
                    null
                } catch (e: IllegalArgumentException) {
                    "${e::class.java.simpleName}: ${e.message?.lineSequence()?.firstOrNull()}"
                }
                runs += SyncRun(i, begin, end, error)
            }
        }.joinAll()
    }
    return runs.sortedBy { it.index }
}

/** ③ 흐름 연결 네 가지 — 부모 구간과 일을 이어받는 구간 이름 짝 */
enum class FlowCase(val label: String, val parent: String, val child: String) {
    CHILD("자식 코루틴(같은 구조) · 토큰 없음", "click-child", "work-child"),
    DETACHED("다른 스코프로 던짐 · 토큰 없음", "click-detached", "upload-detached"),
    TOKEN("다른 스코프로 던짐 · tokenFromCoroutineContext()", "click-token", "upload-token"),
    MANUAL("콜백 스레드 · tokenForManualPropagation()", "request-manual", "callback-manual"),
}

@OptIn(DelicateTracingApi::class, ExperimentalContextPropagation::class)
suspend fun runFlowCases(tracer: Tracer) {
    val detached = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    try {
        // 자식 코루틴 — 부모 구간의 컨텍스트 요소를 물려받는다
        tracer.traceCoroutine(TRACE_CATEGORY, FlowCase.CHILD.parent) {
            coroutineScope {
                launch(Dispatchers.Default) {
                    tracer.traceCoroutine(TRACE_CATEGORY, FlowCase.CHILD.child) { delay(30) }
                }
            }
        }
        // 다른 스코프 — 구조가 끊긴 fire-and-forget
        val jobs = mutableListOf<Job>()
        tracer.traceCoroutine(TRACE_CATEGORY, FlowCase.DETACHED.parent) {
            jobs += detached.launch { tracer.traceCoroutine(TRACE_CATEGORY, FlowCase.DETACHED.child) { delay(30) } }
        }
        // 같은 fire-and-forget 에 지금 코루틴의 토큰을 넘긴다
        tracer.traceCoroutine(TRACE_CATEGORY, FlowCase.TOKEN.parent) {
            val token = tracer.tokenFromCoroutineContext()
            jobs += detached.launch { tracer.traceCoroutine(TRACE_CATEGORY, FlowCase.TOKEN.child, token = token) { delay(30) } }
        }
        jobs.joinAll()
        // 코루틴이 아닌 콜백 스레드 — 수동 토큰을 양쪽에 준다
        val manual = tracer.tokenForManualPropagation()
        tracer.traceCoroutine(TRACE_CATEGORY, FlowCase.MANUAL.parent, token = manual) {
            suspendCancellableCoroutine { cont ->
                Thread({
                    tracer.trace(TRACE_CATEGORY, FlowCase.MANUAL.child, token = manual) { Thread.sleep(10) }
                    cont.resume(Unit)
                }, "callback-thread").start()
            }
        }
    } finally {
        detached.cancel()
    }
}

// ==================== 결과 문장(화면과 단위 테스트가 같은 문장을 쓴다) ====================

fun coroutineSliceLines(runs: List<CoroutineRun>, content: TraceFileContent): List<String> {
    val stats = sliceStats(content)
    val hopped = runs.count { it.beginThread != it.endThread }
    val unbalanced = trackBalances(content).filter { it.begins != it.ends }
    return runs.map { r ->
        val hop = if (r.beginThread != r.endThread) "${shortThread(r.beginThread)}→${shortThread(r.endThread)}" else shortThread(r.beginThread)
        "load-${r.index} [$hop] ${describeSlice(stats["load-${r.index}"])}"
    } + listOf(
        "스레드가 바뀐 코루틴 $hopped/${runs.size}",
        if (unbalanced.isEmpty()) "트랙마다 BEGIN=END ✓(${trackBalances(content).size}개 트랙)"
        else "BEGIN≠END 트랙 ${unbalanced.size}: " + unbalanced.joinToString { "${shortThread(it.track)} ${it.begins}/${it.ends}" },
    )
}

fun syncLines(runs: List<SyncRun>, content: TraceFileContent): List<String> {
    val begins = content.events.count { it.type == TRACK_EVENT_SLICE_BEGIN && it.name?.startsWith("sync-") == true }
    val unbalanced = trackBalances(content).filter { it.begins != it.ends }
    return runs.map { r ->
        val hop = if (r.beginThread != r.endThread) "${shortThread(r.beginThread)}→${shortThread(r.endThread)}" else "${shortThread(r.beginThread)}(같음)"
        "sync-${r.index} [$hop] " + (r.error ?: "close() 정상")
    } + listOf(
        "close() 예외 ${runs.count { it.error != null }}/${runs.size} · 스레드가 바뀐 것 ${runs.count { it.beginThread != it.endThread }}",
        "파일: sync BEGIN $begins · END 가 모자란 트랙 ${unbalanced.count { it.begins > it.ends }}" +
            if (unbalanced.isEmpty()) "" else " (" + unbalanced.joinToString { "${shortThread(it.track)} ${it.begins}/${it.ends}" } + ")",
    )
}

fun flowCaseLines(content: TraceFileContent): List<String> {
    val stats = sliceStats(content)
    return FlowCase.entries.flatMap { c ->
        val p = stats[c.parent]
        val ch = stats[c.child]
        listOf(
            "${c.label}: " + if (linked(p, ch)) "연결됨 ✓" else "끊김 ✗",
            "  ${c.parent} ${describeSlice(p)}",
            "  ${c.child} ${describeSlice(ch)}",
        )
    }
}

private fun spinMillis(ms: Long) {
    val until = System.nanoTime() + ms * 1_000_000
    while (System.nanoTime() < until) {
        // 동기 구간 안에서 2ms 동안 일하는 척 — delay 와 달리 스레드를 놓지 않는다
    }
}
