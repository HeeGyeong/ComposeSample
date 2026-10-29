package com.example.composesample.example

import com.example.composesample.presentation.example.component.architecture.development.tracing.CoroutineRun
import com.example.composesample.presentation.example.component.architecture.development.tracing.SyncRun
import com.example.composesample.presentation.example.component.architecture.development.tracing.TRACK_EVENT_SLICE_BEGIN
import com.example.composesample.presentation.example.component.architecture.development.tracing.TRACK_EVENT_SLICE_END
import com.example.composesample.presentation.example.component.architecture.development.tracing.coroutineSliceLines
import com.example.composesample.presentation.example.component.architecture.development.tracing.flowCaseLines
import com.example.composesample.presentation.example.component.architecture.development.tracing.linked
import com.example.composesample.presentation.example.component.architecture.development.tracing.parseTraceFile
import com.example.composesample.presentation.example.component.architecture.development.tracing.sliceStats
import com.example.composesample.presentation.example.component.architecture.development.tracing.syncLines
import com.example.composesample.presentation.example.component.architecture.development.tracing.trackBalances
import org.junit.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * tracing 2.0 예제 — 트레이스 파일 파서와 분석 문장
 *
 * TraceDriver 는 Android Context 가 필요해 JVM 에서 실제 파일을 만들 수 없으므로, tracing-wire 가 쓰는 모양
 * (Trace.packet=1 · TracePacket.timestamp=8 / track_event=11 / track_descriptor=60 · TrackEvent.type=9 / track_uuid=11 /
 * name=23 / flow_ids=47 fixed64 비-packed · TrackDescriptor.uuid=1 / thread=4{tid=2, thread_name=5})을 손으로 만들어 넣는다.
 * 실제 파일과의 대조는 기기 실측(독립 파이썬 파서와 패킷·조각 수 일치)으로 했다.
 */
class CoroutineTraceLabTest {

    // ---- 아주 작은 protobuf 인코더 ----
    private fun ByteArrayOutputStream.varint(v: Long) {
        var x = v
        while (true) {
            if (x and 0x7FL.inv() == 0L) { write(x.toInt()); return }
            write(((x and 0x7F) or 0x80).toInt())
            x = x ushr 7
        }
    }

    private fun ByteArrayOutputStream.tag(field: Int, wire: Int) = varint(((field shl 3) or wire).toLong())
    private fun ByteArrayOutputStream.uint(field: Int, v: Long) { tag(field, 0); varint(v) }
    private fun ByteArrayOutputStream.bytes(field: Int, b: ByteArray) { tag(field, 2); varint(b.size.toLong()); write(b) }
    private fun ByteArrayOutputStream.text(field: Int, s: String) = bytes(field, s.encodeToByteArray())
    private fun ByteArrayOutputStream.fixed64(field: Int, v: Long) {
        tag(field, 1)
        for (i in 0 until 8) write(((v ushr (8 * i)) and 0xFF).toInt())
    }

    private fun msg(block: ByteArrayOutputStream.() -> Unit): ByteArray = ByteArrayOutputStream().apply(block).toByteArray()

    private fun thread(uuid: Long, tid: Long, name: String) = msg {
        bytes(1, msg { bytes(60, msg { uint(1, uuid); bytes(4, msg { uint(2, tid); text(5, name) }) }) })
    }

    private fun event(type: Int, track: Long, name: String? = null, flows: List<Long> = emptyList(), packed: Boolean = false, ts: Long = 1) = msg {
        bytes(1, msg {
            uint(8, ts)
            bytes(11, msg {
                uint(9, type.toLong())
                uint(11, track)
                if (name != null) text(23, name)
                if (packed) bytes(47, msg { flows.forEach { v -> for (i in 0 until 8) write(((v ushr (8 * i)) and 0xFF).toInt()) } })
                else flows.forEach { fixed64(47, it) }
            })
        })
    }

    private fun file(vararg parts: ByteArray) = parts.fold(ByteArray(0)) { acc, b -> acc + b }

    @Test
    fun `트랙 이름·구간 이름·flow id 를 읽고 조각과 스레드를 센다`() {
        val bytes = file(
            thread(10, 101, "DefaultDispatcher-worker-1"),
            thread(20, 102, "DefaultDispatcher-worker-2"),
            event(TRACK_EVENT_SLICE_BEGIN, 10, "load-1", listOf(7L)),
            event(TRACK_EVENT_SLICE_END, 10),
            event(TRACK_EVENT_SLICE_BEGIN, 20, "load-1", listOf(7L), packed = true),
            event(TRACK_EVENT_SLICE_END, 20),
            // 큰 flow id(부호 비트) 도 그대로
            event(TRACK_EVENT_SLICE_BEGIN, 20, "upload", listOf(-2L)),
        )

        val content = parseTraceFile(bytes)
        assertEquals(7, content.packets)
        assertEquals("DefaultDispatcher-worker-2", content.tracks.getValue(20).threadName)
        val load = sliceStats(content).getValue("load-1")
        assertEquals(2, load.pieces)
        assertEquals(listOf("DefaultDispatcher-worker-1", "DefaultDispatcher-worker-2"), load.threads)
        assertEquals(setOf(7L), load.flowIds)
        assertEquals(setOf(-2L), sliceStats(content).getValue("upload").flowIds)
        // worker-2 트랙은 BEGIN 2 · END 1 — upload 가 닫히지 않았다
        assertEquals(listOf(1 to 1, 2 to 1), trackBalances(content).map { it.begins to it.ends })
    }

    @Test
    fun `flow id 를 하나라도 공유해야 연결이다`() {
        val content = parseTraceFile(
            file(
                event(TRACK_EVENT_SLICE_BEGIN, 1, "click-token", listOf(5L)),
                event(TRACK_EVENT_SLICE_BEGIN, 1, "upload-token", listOf(5L)),
                event(TRACK_EVENT_SLICE_BEGIN, 1, "click-detached", listOf(6L)),
                event(TRACK_EVENT_SLICE_BEGIN, 1, "upload-detached", listOf(8L)),
            )
        )
        val stats = sliceStats(content)
        assertTrue(linked(stats["click-token"], stats["upload-token"]))
        assertFalse(linked(stats["click-detached"], stats["upload-detached"]))
        assertFalse(linked(stats["click-detached"], null))

        val lines = flowCaseLines(content)
        assertTrue(lines.contains("다른 스코프로 던짐 · tokenFromCoroutineContext(): 연결됨 ✓"))
        assertTrue(lines.contains("다른 스코프로 던짐 · 토큰 없음: 끊김 ✗"))
        assertTrue(lines.any { it.startsWith("  click-child 기록 없음") })
    }

    @Test
    fun `결과 문장은 스레드 이동과 트랙 균형을 그대로 보인다`() {
        val content = parseTraceFile(
            file(
                thread(10, 101, "DefaultDispatcher-worker-1"),
                thread(20, 102, "DefaultDispatcher-worker-2"),
                event(TRACK_EVENT_SLICE_BEGIN, 10, "load-1", listOf(3L)),
                event(TRACK_EVENT_SLICE_END, 10),
                event(TRACK_EVENT_SLICE_BEGIN, 20, "load-1", listOf(3L)),
                event(TRACK_EVENT_SLICE_END, 20),
                event(TRACK_EVENT_SLICE_BEGIN, 10, "sync-1"),
            )
        )
        val coroutine = coroutineSliceLines(listOf(CoroutineRun(1, "DefaultDispatcher-worker-1", "DefaultDispatcher-worker-2")), content)
        assertEquals("load-1 [worker-1→worker-2] 조각 2 · 스레드 2(worker-1,worker-2) · flow 1", coroutine[0])
        assertEquals("스레드가 바뀐 코루틴 1/1", coroutine[1])
        assertEquals("BEGIN≠END 트랙 1: worker-1 2/1", coroutine[2])

        val sync = syncLines(listOf(SyncRun(1, "DefaultDispatcher-worker-1", "DefaultDispatcher-worker-2", "IllegalArgumentException: Invariant violation.")), content)
        assertEquals("sync-1 [worker-1→worker-2] IllegalArgumentException: Invariant violation.", sync[0])
        assertEquals("close() 예외 1/1 · 스레드가 바뀐 것 1", sync[1])
        assertEquals("파일: sync BEGIN 1 · END 가 모자란 트랙 1 (worker-1 2/1)", sync[2])
    }
}
