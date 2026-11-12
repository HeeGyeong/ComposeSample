package com.example.composesample.example

import com.example.composesample.presentation.example.component.architecture.development.tracing.addedThreads
import com.example.composesample.presentation.example.component.architecture.development.tracing.buildUsersJson
import com.example.composesample.presentation.example.component.architecture.development.tracing.isJavaFrame
import com.example.composesample.presentation.example.component.architecture.development.tracing.parseStackSampleFile
import com.example.composesample.presentation.example.component.architecture.development.tracing.parseUsersWithGson
import com.example.composesample.presentation.example.component.architecture.development.tracing.profilingErrorLabel
import org.junit.Test
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * ProfilingManager 예제 — 스택 샘플 파일 파서와 보조 함수
 *
 * 실제 결과 파일은 시스템(traced_perf)이 기기에서 만들어 JVM 에서 얻을 수 없으므로, 기기 파일에서 확인한 모양
 * (Trace.packet=1 · TracePacket.trusted_packet_sequence_id=10 / interned_data=12 / sequence_flags=13 / perf_sample=66 ·
 * InternedData.function_names=5{iid=1,str=2} / frames=6{iid=1,function_name_id=2} / callstacks=7{iid=1,frame_ids=2} ·
 * PerfSample.pid=2 / tid=3 / callstack_iid=4, 콜스택은 바깥 → 안쪽)을 손으로 만들어 넣는다.
 * 실제 파일과의 대조는 기기 실측(꺼낸 파일을 독립 파이썬 파서로 읽어 같은 수)으로 했다.
 */
class ProfilingLabTest {

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
    private fun msg(block: ByteArrayOutputStream.() -> Unit): ByteArray = ByteArrayOutputStream().apply(block).toByteArray()

    private fun internedString(iid: Long, s: String) = msg { uint(1, iid); bytes(2, s.toByteArray()) }
    private fun frame(iid: Long, nameId: Long) = msg { uint(1, iid); uint(2, nameId) }
    private fun packedCallstack(iid: Long, vararg ids: Long) = msg {
        uint(1, iid)
        bytes(2, msg { ids.forEach { varint(it) } })
    }
    private fun unpackedCallstack(iid: Long, vararg ids: Long) = msg { uint(1, iid); ids.forEach { uint(2, it) } }
    private fun sample(pid: Long, tid: Long, callstack: Long) = msg { uint(1, 0); uint(2, pid); uint(3, tid); uint(4, callstack) }

    private fun packet(seq: Long, flags: Long = 0, interned: ByteArray? = null, sample: ByteArray? = null) = msg {
        uint(10, seq)
        if (flags != 0L) uint(13, flags)
        interned?.let { bytes(12, it) }
        sample?.let { bytes(66, it) }
    }

    private fun trace(vararg packets: ByteArray) = msg { packets.forEach { bytes(1, it) } }

    /** pid 100 = 이 앱, 101 = 이 앱의 다른 스레드, 200 = 다른 프로세스 */
    private fun sampleTrace(): ByteArray {
        val interned1 = msg {
            bytes(5, internedString(1, "main"))
            bytes(5, internedString(2, "com.example.App.onClick"))
            bytes(5, internedString(3, "com.google.gson.Gson.fromJson"))
            bytes(5, internedString(4, "java.lang.StringFactory.newStringFromChars"))
            bytes(5, internedString(5, "_ZN3art11interpreter6DoCallE"))
            (1L..5L).forEach { bytes(6, frame(it, it)) }
            // 바깥 → 안쪽. 맨 안쪽이 네이티브(5)라 Java 맨 안쪽은 StringFactory(4)
            bytes(7, packedCallstack(1, 1, 2, 3, 4, 5))
            bytes(7, packedCallstack(2, 1, 5))
            bytes(7, unpackedCallstack(3, 1, 2))
        }
        // 다른 시퀀스는 같은 iid 를 다른 뜻으로 쓴다
        val interned2 = msg {
            bytes(5, internedString(1, "com.google.gson.stream.JsonReader.nextQuotedValue"))
            bytes(6, frame(1, 1))
            bytes(7, packedCallstack(1, 1))
        }
        return trace(
            packet(1, flags = 1, interned = interned1, sample = sample(100, 100, 1)),
            packet(1, sample = sample(100, 100, 2)),
            packet(1, sample = sample(100, 101, 1)),
            packet(1, sample = sample(200, 200, 1)),
            packet(1, sample = sample(100, 100, 3)),
            packet(2, flags = 1, interned = interned2, sample = sample(100, 100, 1)),
            packet(1),
        )
    }

    @Test
    fun `이 프로세스 샘플만 스레드별로 센다`() {
        val s = parseStackSampleFile(sampleTrace(), pid = 100)
        assertEquals(7, s.packets)
        assertEquals(5, s.samples)
        assertEquals(listOf(100 to 4, 101 to 1), s.samplesByTid)
        assertEquals(4, s.mainThreadSamples)
    }

    @Test
    fun `메인 스레드 샘플 중 Gson 프레임 포함 — 시퀀스마다 인턴 표가 다르다`() {
        val s = parseStackSampleFile(sampleTrace(), pid = 100)
        // seq1 콜스택 1(Gson 있음) + seq2 콜스택 1(JsonReader) — seq1 의 콜스택 2·3 은 없음
        assertEquals(2, s.mainWithMarker)
    }

    @Test
    fun `맨 안쪽 Java 프레임 — 네이티브는 건너뛰고, Java 가 없는 샘플은 따로 센다`() {
        val s = parseStackSampleFile(sampleTrace(), pid = 100)
        assertEquals(
            setOf(
                "java.lang.StringFactory.newStringFromChars" to 1,
                "com.example.App.onClick" to 1,
                "com.google.gson.stream.JsonReader.nextQuotedValue" to 1,
            ),
            s.topLeafFrames.toSet()
        )
        assertEquals(1, s.mainWithoutJava)
    }

    @Test
    fun `다른 pid 로 보면 그 프로세스 샘플만`() {
        val s = parseStackSampleFile(sampleTrace(), pid = 200)
        assertEquals(1, s.samples)
        assertEquals(1, s.mainThreadSamples)
        assertEquals(1, s.mainWithMarker)
    }

    @Test
    fun `Java 프레임 판정 — 네이티브 기호와 점 없는 이름은 아니다`() {
        assertTrue(isJavaFrame("com.google.gson.Gson.fromJson"))
        assertTrue(isJavaFrame("com.google.gson.internal.bind.MapTypeAdapterFactory\$Adapter.read"))
        assertTrue(isJavaFrame("android.os.Looper.loopOnce"))
        assertFalse(isJavaFrame("ExecuteSwitchImplAsm"))
        assertFalse(isJavaFrame("art_quick_invoke_stub"))
        assertFalse(isJavaFrame("_ZN3art11interpreter6DoCallILb0EEEb"))
        assertFalse(isJavaFrame("_ZN3art11interpreterL7ExecuteEbb.__uniq.1124.llvm.2845"))
        assertFalse(isJavaFrame("Foo.bar"))
        assertFalse(isJavaFrame(null))
    }

    @Test
    fun `오류 코드 이름 — android-35 ProfilingResult 상수 값`() {
        assertTrue(profilingErrorLabel(0).startsWith("ERROR_NONE"))
        assertTrue(profilingErrorLabel(1).startsWith("ERROR_FAILED_RATE_LIMIT_SYSTEM"))
        assertTrue(profilingErrorLabel(2).startsWith("ERROR_FAILED_RATE_LIMIT_PROCESS"))
        assertTrue(profilingErrorLabel(3).startsWith("ERROR_FAILED_PROFILING_IN_PROGRESS"))
        assertTrue(profilingErrorLabel(8).startsWith("ERROR_UNKNOWN"))
        assertTrue(profilingErrorLabel(9).startsWith("알 수 없는 코드"))
    }

    @Test
    fun `늘어난 스레드 — 같은 이름은 개수까지 비교`() {
        val before = listOf("main", "binder:1_1", "binder:1_1")
        val after = listOf("binder:1_1", "Sampling Profil", "main", "binder:1_1", "binder:1_1")
        assertEquals(listOf("Sampling Profil", "binder:1_1"), addedThreads(before, after))
        assertEquals(emptyList(), addedThreads(before, before.reversed()))
    }

    @Test
    fun `부하 JSON 을 Gson 으로 파싱하면 원소 수가 맞다`() {
        assertEquals(3, parseUsersWithGson(buildUsersJson(3)))
        assertEquals(0, parseUsersWithGson(buildUsersJson(0)))
    }
}
