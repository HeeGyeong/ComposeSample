package com.example.composesample.presentation.example.component.architecture.development.tracing

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

// ==================== 일부러 메인 스레드에 올리는 부하 ====================

/** 원문(Sentry) 사례 — 큰 JSON 을 Gson 으로 메인 스레드에서 파싱한다. 사용자 [count] 명짜리 배열 문자열 */
fun buildUsersJson(count: Int): String = buildString {
    append('[')
    for (i in 0 until count) {
        if (i > 0) append(',')
        append("{\"id\":").append(i)
            .append(",\"name\":\"user").append(i)
            .append("\",\"email\":\"u").append(i).append("@example.com\"")
            .append(",\"tags\":[\"a\",\"b\",\"c\"]}")
    }
    append(']')
}

private val usersType = object : TypeToken<List<Map<String, Any>>>() {}.type

/** Gson 으로 한 번 파싱하고 원소 수를 돌려준다 — 프로파일 스택에 com.google.gson.* 프레임이 남는 부분 */
fun parseUsersWithGson(json: String): Int {
    val users: List<Map<String, Any>> = Gson().fromJson(json, usersType)
    return users.size
}

const val PROFILE_USERS = 20_000
const val PROFILE_PARSE_ROUNDS = 8

/**
 * 레거시 카드는 부하를 줄이고 백그라운드 스레드에서 돈다 — API 30 에뮬 debug 에서 전수 추적(startMethodTracing)이
 * 위 부하(20,000명 × 8번)를 143초(추적 없이 2.2초)로 늘리고 64MB 버퍼를 채웠다. 메인 스레드였다면 2분 넘게 화면이 멈춘다.
 */
const val LEGACY_USERS = 2_000
const val LEGACY_PARSE_ROUNDS = 4
const val LEGACY_BUFFER_BYTES = 16 * 1024 * 1024
const val STACK_SAMPLING_DURATION_MS = 4_000
const val STACK_SAMPLING_FREQUENCY_HZ = 100

/** 부하가 남기는 프레임을 찾을 때 쓰는 접두어 */
const val GSON_FRAME_PREFIX = "com.google.gson."

// ==================== ProfilingResult 오류 코드 ====================

/**
 * android.os.ProfilingResult 의 오류 코드(API 35, android-35 javap 값). 화면·테스트가 API 35 클래스를 직접 참조하지 않도록
 * 값으로 옮겨 둔다 — 버전 분기 밖에서 API 35 타입을 읽으면 그 미만 기기에서 클래스 해석 오류가 날 수 있다.
 */
fun profilingErrorLabel(code: Int): String = when (code) {
    0 -> "ERROR_NONE — 성공"
    1 -> "ERROR_FAILED_RATE_LIMIT_SYSTEM — 기기 전체 한도"
    2 -> "ERROR_FAILED_RATE_LIMIT_PROCESS — 이 앱의 한도"
    3 -> "ERROR_FAILED_PROFILING_IN_PROGRESS — 이미 기록 중"
    4 -> "ERROR_FAILED_EXECUTING"
    5 -> "ERROR_FAILED_POST_PROCESSING"
    6 -> "ERROR_FAILED_NO_DISK_SPACE"
    7 -> "ERROR_FAILED_INVALID_REQUEST"
    8 -> "ERROR_UNKNOWN"
    else -> "알 수 없는 코드 $code"
}

// ==================== 앱 프로세스 스레드 ====================

/** 이 프로세스의 스레드 이름(/proc/self/task/<tid>/comm — 커널이 15자에서 자른다) */
fun currentThreadNames(): List<String> =
    File("/proc/self/task").listFiles()
        ?.mapNotNull { runCatching { File(it, "comm").readText().trim() }.getOrNull() }
        ?.sorted()
        ?: emptyList()

/** tid 의 스레드 이름 — 이미 끝난 스레드면 null */
fun threadNameOf(tid: Int): String? =
    runCatching { File("/proc/self/task/$tid/comm").readText().trim() }.getOrNull()

/** [after] 에만 있는 스레드(같은 이름이 여러 개면 개수까지 비교) */
fun addedThreads(before: List<String>, after: List<String>): List<String> {
    val remaining = before.groupingBy { it }.eachCount().toMutableMap()
    return after.filter { name ->
        val left = remaining[name] ?: 0
        if (left > 0) {
            remaining[name] = left - 1
            false
        } else {
            true
        }
    }.sorted()
}

// ==================== 스택 샘플 파일(Perfetto) 분석 ====================

data class StackSampleSummary(
    val packets: Int,
    val samples: Int,
    /** 이 프로세스(pid) 샘플을 스레드별로 — 많은 순 */
    val samplesByTid: List<Pair<Int, Int>>,
    val mainThreadSamples: Int,
    /** 메인 스레드 샘플 중 스택 어딘가에 [GSON_FRAME_PREFIX] 프레임이 있는 수 */
    val mainWithMarker: Int,
    /** 메인 스레드 샘플의 맨 안쪽 Java 프레임 — 많은 순 */
    val topLeafFrames: List<Pair<String, Int>>,
    /** 스택에 이름 있는 Java 프레임이 하나도 없는 메인 스레드 샘플 */
    val mainWithoutJava: Int,
)

/** 함수 이름이 Java/Kotlin 메서드처럼 생겼는지 — 네이티브 기호(_ZN…, .__uniq., .llvm.)와 점 없는 이름은 뺀다 */
fun isJavaFrame(name: String?): Boolean =
    name != null &&
        !name.startsWith("_Z") &&
        !name.contains(".__uniq.") &&
        !name.contains(".llvm.") &&
        JAVA_FRAME.matches(name)

private val JAVA_FRAME = Regex("^[A-Za-z_$][\\w$]*(\\.[\\w$<>-]+){2,}$")

private class InternTables {
    val functionNames = HashMap<Long, String>()
    val frames = HashMap<Long, Long>()
    val callstacks = HashMap<Long, LongArray>()
}

/**
 * ProfilingManager 스택 샘플링 결과(.perfetto-stack-sample)를 읽는다.
 * Trace.packet=1 → TracePacket.trusted_packet_sequence_id=10 · interned_data=12 · sequence_flags=13 · perf_sample=66.
 * InternedData.function_names=5{iid=1, str=2} · frames=6{iid=1, function_name_id=2} · callstacks=7{iid=1, frame_ids=2}.
 * PerfSample.pid=2 · tid=3 · callstack_iid=4. 콜스택 프레임은 바깥(main) → 안쪽 순서다(기기 파일 실측).
 */
fun parseStackSampleFile(bytes: ByteArray, pid: Int, marker: String = GSON_FRAME_PREFIX): StackSampleSummary {
    val tables = HashMap<Long, InternTables>()
    val byTid = HashMap<Int, Int>()
    val leaf = HashMap<String, Int>()
    var packets = 0
    var samples = 0
    var main = 0
    var mainMarker = 0
    var mainNoJava = 0
    val top = ProtoReader(bytes)
    while (top.hasMore()) {
        val f = top.next()
        if (f.number != 1 || f.bytes == null) continue
        packets++
        var seq = 0L
        var flags = 0L
        var interned: ByteArray? = null
        var sample: ByteArray? = null
        val packet = ProtoReader(f.bytes)
        while (packet.hasMore()) {
            val p = packet.next()
            when (p.number) {
                10 -> seq = p.value
                12 -> interned = p.bytes
                13 -> flags = p.value
                66 -> sample = p.bytes
            }
        }
        // SEQ_INCREMENTAL_STATE_CLEARED(1) — 그 시퀀스의 인턴 표를 비운다
        if (flags and 1L != 0L) tables.remove(seq)
        val table = tables.getOrPut(seq) { InternTables() }
        interned?.let { readInterned(it, table) }
        val s = sample ?: continue
        var samplePid = -1
        var tid = -1
        var callstack = -1L
        val r = ProtoReader(s)
        while (r.hasMore()) {
            val p = r.next()
            when (p.number) {
                2 -> samplePid = p.value.toInt()
                3 -> tid = p.value.toInt()
                4 -> callstack = p.value
            }
        }
        if (samplePid != pid) continue
        samples++
        byTid[tid] = (byTid[tid] ?: 0) + 1
        if (tid != pid) continue
        main++
        val names = table.callstacks[callstack]
            ?.map { frameId -> table.frames[frameId]?.let { table.functionNames[it] } }
            ?: emptyList()
        if (names.any { it?.startsWith(marker) == true }) mainMarker++
        val leafJava = names.lastOrNull { isJavaFrame(it) }
        if (leafJava == null) mainNoJava++ else leaf[leafJava] = (leaf[leafJava] ?: 0) + 1
    }
    return StackSampleSummary(
        packets = packets,
        samples = samples,
        samplesByTid = byTid.entries.sortedByDescending { it.value }.map { it.key to it.value },
        mainThreadSamples = main,
        mainWithMarker = mainMarker,
        topLeafFrames = leaf.entries.sortedByDescending { it.value }.take(5).map { it.key to it.value },
        mainWithoutJava = mainNoJava,
    )
}

private fun readInterned(bytes: ByteArray, table: InternTables) {
    val r = ProtoReader(bytes)
    while (r.hasMore()) {
        val f = r.next()
        val body = f.bytes ?: continue
        when (f.number) {
            5 -> {
                var iid = 0L
                var str: String? = null
                val x = ProtoReader(body)
                while (x.hasMore()) {
                    val p = x.next()
                    if (p.number == 1) iid = p.value
                    if (p.number == 2) str = p.bytes?.toString(Charsets.UTF_8)
                }
                str?.let { table.functionNames[iid] = it }
            }
            6 -> {
                var iid = 0L
                var nameId = 0L
                val x = ProtoReader(body)
                while (x.hasMore()) {
                    val p = x.next()
                    if (p.number == 1) iid = p.value
                    if (p.number == 2) nameId = p.value
                }
                table.frames[iid] = nameId
            }
            7 -> {
                var iid = 0L
                val ids = ArrayList<Long>()
                val x = ProtoReader(body)
                while (x.hasMore()) {
                    val p = x.next()
                    if (p.number == 1) iid = p.value
                    if (p.number == 2) {
                        // packed(LEN) 와 비-packed(varint) 둘 다 받는다
                        val packed = p.bytes
                        if (packed == null) ids += p.value else ids += readPackedVarints(packed)
                    }
                }
                table.callstacks[iid] = ids.toLongArray()
            }
        }
    }
}

private fun readPackedVarints(bytes: ByteArray): List<Long> {
    val out = ArrayList<Long>()
    var pos = 0
    while (pos < bytes.size) {
        var result = 0L
        var shift = 0
        while (true) {
            val b = bytes[pos++].toInt() and 0xFF
            result = result or ((b and 0x7F).toLong() shl shift)
            if (b and 0x80 == 0) break
            shift += 7
        }
        out += result
    }
    return out
}
