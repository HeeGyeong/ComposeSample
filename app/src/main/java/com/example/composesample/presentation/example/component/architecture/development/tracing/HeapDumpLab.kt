package com.example.composesample.presentation.example.component.architecture.development.tracing

// ==================== 떠난 화면이 남기는 리스너(일부러 만든 누수) ====================

/** 한 리스너가 붙잡는 크기 — 덤프에는 배열 내용이 아니라 크기만 남는다 */
const val LISTENER_PAYLOAD_BYTES = 64 * 1024

/** 화면이 들어올 때 레지스트리에 등록하는 리스너 — 힙 덤프에서 이 클래스 이름으로 센다 */
class LeakedScreenListener(val screen: Int) {
    val payload = ByteArray(LISTENER_PAYLOAD_BYTES)
}

/**
 * 앱 전체에서 하나뿐인 등록처(정적 참조). 화면이 떠날 때 해제하지 않으면 리스너와 그 리스너가 붙잡은 것이
 * 앱이 살아 있는 동안 남는다 — 공식 OOM 사례(StatsReporter.registerListener)와 같은 모양.
 */
object ScreenListenerRegistry {
    private val listeners = ArrayList<LeakedScreenListener>()

    val size: Int get() = synchronized(listeners) { listeners.size }

    fun register(listener: LeakedScreenListener) {
        synchronized(listeners) { listeners += listener }
    }

    fun unregister(listener: LeakedScreenListener) {
        synchronized(listeners) { listeners.remove(listener) }
    }

    fun clear() {
        synchronized(listeners) { listeners.clear() }
    }
}

val LEAKED_LISTENER_CLASS: String = LeakedScreenListener::class.java.name

// ==================== Java 힙 덤프 파일(.perfetto-java-heap-dump) 읽기 ====================

data class HeapDumpSummary(
    val packets: Int,
    val heapGraphPackets: Int,
    val pids: List<Int>,
    val objects: Int,
    val types: Int,
    val roots: Int,
    /** 같은 클래스 이름이 타입 id 여럿으로 나온 이름 수 — 그래서 개수는 이름으로 모아 센다 */
    val duplicateTypeNames: Int,
    /** 찾으려는 클래스 이름 → 인스턴스 수(같은 이름의 모든 타입 id 합) */
    val instancesOf: Map<String, Int>,
    /** 인스턴스가 많은 클래스 — 많은 순 */
    val topClasses: List<Pair<String, Int>>,
)

/**
 * ProfilingManager Java 힙 덤프를 복사 없이 한 번 훑어 클래스별 인스턴스 수를 센다.
 * 필드 번호는 기기 파일과 Perfetto heap_graph.proto 로 확인했다:
 * Trace.packet=1 → TracePacket.heap_graph=56 → HeapGraph.pid=1 · objects=2 · roots=7 · types=9.
 * HeapGraphObject.type_id=2 · HeapGraphType.id=1 · class_name=3 · HeapGraphRoot.object_ids=1(packed).
 * 수십 MB · 수십만 객체라 필드마다 바이트를 복사하는 ProtoReader 대신 위치만 옮긴다.
 */
fun parseJavaHeapDump(bytes: ByteArray, classNames: Set<String>, top: Int = 5): HeapDumpSummary {
    val r = DumpCursor(bytes)
    val perType = TypeCounter()
    val typeNames = HashMap<Long, String>()
    val pids = LinkedHashSet<Int>()
    var packets = 0
    var graphs = 0
    var objects = 0
    var roots = 0
    while (r.pos < bytes.size) {
        val key = r.varint()
        if (key ushr 3 != 1L || key and 7L != 2L) {
            r.skip((key and 7L).toInt())
            continue
        }
        val packetEnd = r.lengthEnd()
        packets++
        while (r.pos < packetEnd) {
            val pk = r.varint()
            if (pk ushr 3 != 56L || pk and 7L != 2L) {
                r.skip((pk and 7L).toInt())
                continue
            }
            val graphEnd = r.lengthEnd()
            graphs++
            while (r.pos < graphEnd) {
                val gk = r.varint()
                val field = (gk ushr 3).toInt()
                val wire = (gk and 7L).toInt()
                when {
                    field == 1 && wire == 0 -> pids += r.varint().toInt()
                    field == 2 && wire == 2 -> {
                        objects++
                        val end = r.lengthEnd()
                        var typeId = -1L
                        while (r.pos < end) {
                            val ok = r.varint()
                            if (ok == TAG_OBJECT_TYPE_ID) typeId = r.varint() else r.skip((ok and 7L).toInt())
                        }
                        if (typeId >= 0) perType.add(typeId)
                    }
                    field == 9 && wire == 2 -> {
                        val end = r.lengthEnd()
                        var id = -1L
                        var name: String? = null
                        while (r.pos < end) {
                            val tk = r.varint()
                            when (tk) {
                                TAG_TYPE_ID -> id = r.varint()
                                TAG_TYPE_NAME -> {
                                    val len = r.varint().toInt()
                                    name = String(bytes, r.pos, len, Charsets.UTF_8)
                                    r.pos += len
                                }
                                else -> r.skip((tk and 7L).toInt())
                            }
                        }
                        if (id >= 0 && name != null) typeNames[id] = name
                    }
                    field == 7 && wire == 2 -> {
                        val end = r.lengthEnd()
                        while (r.pos < end) {
                            val rk = r.varint()
                            when (rk) {
                                // packed — 길이 안의 varint 수가 루트 수
                                TAG_ROOT_IDS_PACKED -> {
                                    val idsEnd = r.lengthEnd()
                                    while (r.pos < idsEnd) {
                                        r.varint()
                                        roots++
                                    }
                                }
                                TAG_ROOT_ID -> {
                                    r.varint()
                                    roots++
                                }
                                else -> r.skip((rk and 7L).toInt())
                            }
                        }
                    }
                    else -> r.skip(wire)
                }
            }
        }
    }

    val byName = HashMap<String, Int>()
    val idsPerName = HashMap<String, Int>()
    for ((id, name) in typeNames) {
        byName[name] = (byName[name] ?: 0) + perType.get(id)
        idsPerName[name] = (idsPerName[name] ?: 0) + 1
    }
    return HeapDumpSummary(
        packets = packets,
        heapGraphPackets = graphs,
        pids = pids.toList(),
        objects = objects,
        types = typeNames.size,
        roots = roots,
        duplicateTypeNames = idsPerName.count { it.value > 1 },
        instancesOf = classNames.associateWith { byName[it] ?: 0 },
        topClasses = byName.entries.sortedByDescending { it.value }.take(top).map { it.key to it.value },
    )
}

// 키 = (필드 번호 shl 3) or wire type
private const val TAG_OBJECT_TYPE_ID = 2L shl 3
private const val TAG_TYPE_ID = 1L shl 3
private const val TAG_TYPE_NAME = (3L shl 3) or 2L
private const val TAG_ROOT_ID = 1L shl 3
private const val TAG_ROOT_IDS_PACKED = (1L shl 3) or 2L

/** 바이트 배열 위를 움직이는 커서 — 하위 메시지도 새 배열 없이 끝 위치로만 다룬다 */
private class DumpCursor(private val buf: ByteArray) {
    var pos = 0

    fun varint(): Long {
        var result = 0L
        var shift = 0
        while (true) {
            val b = buf[pos++].toInt() and 0xFF
            result = result or ((b and 0x7F).toLong() shl shift)
            if (b and 0x80 == 0) return result
            shift += 7
        }
    }

    /** 길이(varint)를 읽고 그 길이가 끝나는 위치를 돌려준다 */
    fun lengthEnd(): Int {
        val len = varint().toInt()
        return pos + len
    }

    fun skip(wire: Int) {
        when (wire) {
            0 -> varint()
            1 -> pos += 8
            2 -> pos = lengthEnd()
            5 -> pos += 4
            else -> error("지원하지 않는 wire type $wire")
        }
    }
}

/** 타입 id → 인스턴스 수. 기기 덤프의 타입 id 는 수만 이하라 배열로 세고, 큰 값만 맵으로 */
private class TypeCounter {
    private var counts = IntArray(1 shl 16)
    private val large = HashMap<Long, Int>()

    fun add(id: Long) {
        if (id >= MAX_ARRAY_ID) {
            large[id] = (large[id] ?: 0) + 1
            return
        }
        val i = id.toInt()
        if (i >= counts.size) counts = counts.copyOf(maxOf(i + 1, counts.size * 2))
        counts[i]++
    }

    fun get(id: Long): Int = if (id >= MAX_ARRAY_ID) large[id] ?: 0 else counts.getOrElse(id.toInt()) { 0 }

    private companion object {
        const val MAX_ARRAY_ID = 1L shl 22
    }
}
