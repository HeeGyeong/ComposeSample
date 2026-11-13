package com.example.composesample.example

import com.example.composesample.presentation.example.component.architecture.development.tracing.LEAKED_LISTENER_CLASS
import com.example.composesample.presentation.example.component.architecture.development.tracing.LeakedScreenListener
import com.example.composesample.presentation.example.component.architecture.development.tracing.ScreenListenerRegistry
import com.example.composesample.presentation.example.component.architecture.development.tracing.parseJavaHeapDump
import org.junit.After
import org.junit.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

/**
 * ProfilingManager Java 힙 덤프 파서와 누수 레지스트리
 *
 * 실제 덤프는 시스템(perfetto_hprof 가 fork 한 자식 프로세스)이 기기에서 만들어 JVM 에서 얻을 수 없으므로, 기기 파일과
 * Perfetto heap_graph.proto 에서 확인한 모양을 손으로 만든다 — Trace.packet=1 → TracePacket.heap_graph=56 →
 * HeapGraph.pid=1 · objects=2{id=1 | id_delta=7, type_id=2, reference_object_id=5(packed)} · roots=7{object_ids=1, root_type=2}
 * · types=9{id=1, class_name=3}. 실제 파일과의 대조는 꺼낸 파일을 독립 파이썬 파서로 읽어 같은 수인지로 했다.
 */
class HeapDumpLabTest {

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
    private fun ByteArrayOutputStream.fixed64(field: Int) { tag(field, 1); write(ByteArray(8)) }
    private fun msg(block: ByteArrayOutputStream.() -> Unit): ByteArray = ByteArrayOutputStream().apply(block).toByteArray()

    private fun type(id: Long, name: String) = msg { uint(1, id); bytes(3, name.toByteArray()); uint(4, 16) }
    private fun objectWithId(id: Long, typeId: Long) = msg { uint(1, id); uint(2, typeId); bytes(5, msg { varint(0); varint(7) }) }
    private fun objectWithDelta(delta: Long, typeId: Long) = msg { uint(7, delta); uint(2, typeId); uint(3, 24) }
    private fun packedRoot(vararg ids: Long) = msg { bytes(1, msg { ids.forEach { varint(it) } }); uint(2, 5) }
    private fun unpackedRoot(id: Long) = msg { uint(1, id); uint(2, 3) }
    private fun graph(block: ByteArrayOutputStream.() -> Unit) = msg { uint(1, 100); block() }
    private fun packet(heapGraph: ByteArray?) = msg {
        uint(10, 1) // trusted_packet_sequence_id
        fixed64(8) // 관련 없는 fixed64 필드도 건너뛴다
        heapGraph?.let { bytes(56, it) }
    }

    private fun trace(vararg packets: ByteArray) = msg { packets.forEach { bytes(1, it) } }

    /** A 는 타입 id 10 · 12 두 개로 나온다(기기 덤프에서 이름 3,284개가 이랬다) */
    private fun sampleDump(): ByteArray = trace(
        packet(null),
        packet(graph {
            bytes(9, type(10, "com.example.A"))
            bytes(9, type(11, "java.lang.String"))
            bytes(2, objectWithId(1000, 10))
            bytes(2, objectWithDelta(4, 10))
            bytes(2, objectWithDelta(4, 11))
            bytes(7, packedRoot(1000, 1004, 1008))
        }),
        packet(graph {
            bytes(9, type(12, "com.example.A"))
            // 다음 패킷의 첫 객체도 id_delta 로 온다 — 개수만 세는 이 파서는 id 를 쓰지 않는다
            bytes(2, objectWithDelta(4, 12))
            bytes(2, objectWithDelta(4, 12))
            bytes(2, objectWithDelta(4, 11))
            bytes(2, objectWithDelta(4, 11))
            bytes(7, unpackedRoot(1016))
        }),
    )

    @After
    fun tearDown() = ScreenListenerRegistry.clear()

    @Test
    fun `패킷 · 객체 · 타입 · 루트 수를 센다`() {
        val s = parseJavaHeapDump(sampleDump(), emptySet())
        assertEquals(3, s.packets)
        assertEquals(2, s.heapGraphPackets)
        assertEquals(listOf(100), s.pids)
        assertEquals(7, s.objects)
        assertEquals(3, s.types)
        assertEquals(4, s.roots)
    }

    @Test
    fun `같은 이름의 타입 id 를 모두 합쳐 인스턴스를 센다`() {
        val s = parseJavaHeapDump(sampleDump(), setOf("com.example.A", "com.example.Missing"))
        assertEquals(mapOf("com.example.A" to 4, "com.example.Missing" to 0), s.instancesOf)
        assertEquals(1, s.duplicateTypeNames)
    }

    @Test
    fun `인스턴스가 많은 클래스를 많은 순으로`() {
        val s = parseJavaHeapDump(sampleDump(), emptySet(), top = 1)
        assertEquals(listOf("com.example.A" to 4), s.topClasses)
        val all = parseJavaHeapDump(sampleDump(), emptySet())
        assertEquals(listOf("com.example.A" to 4, "java.lang.String" to 3), all.topClasses)
    }

    @Test
    fun `힙 그래프가 없는 파일은 0 이다`() {
        val s = parseJavaHeapDump(trace(packet(null), packet(null)), setOf("com.example.A"))
        assertEquals(2, s.packets)
        assertEquals(0, s.heapGraphPackets)
        assertEquals(0, s.objects)
        assertEquals(mapOf("com.example.A" to 0), s.instancesOf)
    }

    @Test
    fun `레지스트리는 해제한 것만 빠진다`() {
        val kept = LeakedScreenListener(1)
        val released = LeakedScreenListener(2)
        ScreenListenerRegistry.register(kept)
        ScreenListenerRegistry.register(released)
        ScreenListenerRegistry.unregister(released)
        assertEquals(1, ScreenListenerRegistry.size)
        assertEquals("com.example.composesample.presentation.example.component.architecture.development.tracing.LeakedScreenListener", LEAKED_LISTENER_CLASS)
    }
}
