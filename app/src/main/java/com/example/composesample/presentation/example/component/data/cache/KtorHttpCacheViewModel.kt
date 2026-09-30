package com.example.composesample.presentation.example.component.data.cache

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.contentnegotiation.ContentTypeMergeStrategy
import io.ktor.client.plugins.plugin
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

private const val ORIGIN = "https://api.example.com"
private const val ITEMS_PATH = "/items"
private const val PRIVATE_PATH = "/me"
private const val LAST_MODIFIED_V1 = "Wed, 30 Sep 2026 00:00:00 GMT"

/**
 * Ktor 클라이언트 HttpCache 를 실제 플러그인 그대로 설치하고, 서버 역할은 MockEngine 이 맡는다.
 *
 * MockEngine 서버는 요청마다 [ServerHit] 을 남기고 `If-None-Match`/`If-Modified-Since` 가 현재 값과 같으면 304 를 돌려준다.
 * 그래서 한 요청이 "서버에 닿았는지·어떤 조건부 헤더를 달았는지·서버가 무엇을 돌려줬는지"(서버 기록)와
 * "앱이 무엇을 받았는지·캐시 이벤트가 났는지"(클라이언트 관찰)를 나란히 비교할 수 있다.
 * 시나리오마다 클라이언트를 새로 만들고 끝나면 닫는다 — 앞 시나리오의 캐시가 섞이지 않게 하기 위해서다.
 */
class KtorHttpCacheViewModel : ViewModel() {

    /** 서버가 붙이는 캐시 관련 응답 헤더 조합 */
    enum class Policy(val label: String, val headerLine: String) {
        MAX_AGE("max-age", "Cache-Control: max-age=2 · ETag: \"v1\""),
        NO_CACHE("no-cache", "Cache-Control: no-cache · ETag: \"v1\""),
        NO_STORE("no-store", "Cache-Control: no-store · ETag: \"v1\""),
        ETAG_ONLY("ETag 만", "Cache-Control 없음 · ETag: \"v1\""),
        LAST_MODIFIED("Last-Modified", "Cache-Control: no-cache · Last-Modified (ETag 없음)"),
        CONTENT_CHANGE("콘텐츠 변경", "Cache-Control: no-cache · ETag: \"v1\" → 도중에 \"v2\"")
    }

    /** 한 요청의 서버 기록 + 클라이언트 관찰 */
    data class RequestStep(
        val index: Int,
        val note: String,
        val reachedServer: Boolean,
        val conditionalHeader: String?,
        val serverStatus: Int?,
        val clientStatus: Int,
        val fromCacheEvent: Boolean,
        val body: String
    )

    data class PolicyResult(val policy: Policy, val steps: List<RequestStep>)

    /** Cache-Control(public/private) × isShared 조합 한 칸 */
    data class SharedCell(
        val cacheControl: String,
        val shared: Boolean,
        val serverHits: Int,
        val fromCacheEvents: Int,
        val publicEntries: Int,
        val privateEntries: Int
    )

    data class FileStorageResult(
        val pathEntryClass: String,
        val fileEntryClass: String,
        val filesAfterFirstRequest: List<String>,
        val newClientWithDisk: RequestStep,
        val newClientWithMemory: RequestStep,
        val filesAfterClear: Int,
        val directoryKept: Boolean
    )

    data class AcceptRow(val strategy: String, val explicitAccept: String?, val acceptValues: List<String>)

    private val _running = MutableStateFlow<String?>(null)
    /** 실행 중인 시나리오 이름(없으면 null) — 버튼을 잠그는 데 쓴다 */
    val running = _running.asStateFlow()

    private val _policyResult = MutableStateFlow<PolicyResult?>(null)
    val policyResult = _policyResult.asStateFlow()

    private val _sharedCells = MutableStateFlow<List<SharedCell>>(emptyList())
    val sharedCells = _sharedCells.asStateFlow()

    private val _clearLog = MutableStateFlow<List<String>>(emptyList())
    val clearLog = _clearLog.asStateFlow()

    private val _fileResult = MutableStateFlow<FileStorageResult?>(null)
    val fileResult = _fileResult.asStateFlow()

    private val _acceptRows = MutableStateFlow<List<AcceptRow>>(emptyList())
    val acceptRows = _acceptRows.asStateFlow()

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

    // ==================== 1. Cache-Control 정책별 동작 ====================

    fun runPolicy(policy: Policy) = launchScenario("policy") {
        val origin = when (policy) {
            Policy.MAX_AGE -> MockOrigin(cacheControl = { "max-age=2" })
            Policy.NO_CACHE, Policy.CONTENT_CHANGE -> MockOrigin(cacheControl = { "no-cache" })
            Policy.NO_STORE -> MockOrigin(cacheControl = { "no-store" })
            Policy.ETAG_ONLY -> MockOrigin(cacheControl = { null })
            Policy.LAST_MODIFIED -> MockOrigin(cacheControl = { "no-cache" }, etag = null, lastModified = LAST_MODIFIED_V1)
        }
        val probe = CachingClient(origin)
        val steps = mutableListOf<RequestStep>()
        suspend fun step(note: String) {
            steps += probe.request(steps.size + 1, note)
            _policyResult.value = PolicyResult(policy, steps.toList())
        }
        _policyResult.value = PolicyResult(policy, emptyList())
        when (policy) {
            Policy.MAX_AGE -> {
                step("첫 요청")
                step("바로 다시")
                delay(2_500)
                step("2.5초 뒤(만료)")
                step("바로 다시")
            }
            Policy.CONTENT_CHANGE -> {
                step("첫 요청")
                step("바로 다시")
                origin.etag = "\"v2\""
                origin.body = "body-v2"
                step("서버 콘텐츠 v2 로 교체 후")
                step("바로 다시")
            }
            Policy.NO_CACHE -> {
                step("첫 요청")
                step("바로 다시")
                step("한 번 더")
            }
            else -> {
                step("첫 요청")
                step("바로 다시")
            }
        }
        probe.close()
    }

    // ==================== 2. private 응답과 공유 캐시 ====================

    fun runSharedComparison() = launchScenario("shared") {
        val cells = mutableListOf<SharedCell>()
        for (cacheControl in listOf("max-age=60", "private, max-age=60")) {
            for (shared in listOf(false, true)) {
                val origin = MockOrigin(cacheControl = { cacheControl })
                val probe = CachingClient(origin, shared = shared)
                probe.request(1, "")
                probe.request(2, "")
                cells += SharedCell(
                    cacheControl = cacheControl,
                    shared = shared,
                    serverHits = origin.hits.size,
                    fromCacheEvents = probe.fromCacheEvents.get(),
                    publicEntries = probe.publicStorage.findAll(Url(ORIGIN + ITEMS_PATH)).size,
                    privateEntries = probe.privateStorage.findAll(Url(ORIGIN + ITEMS_PATH)).size
                )
                probe.close()
            }
        }
        _sharedCells.value = cells
    }

    // ==================== 3. clearAllCaches (3.6.0 신규) ====================

    fun runClearAllCaches() = launchScenario("clear") {
        val log = mutableListOf<String>()
        fun add(line: String) {
            log += line
            _clearLog.value = log.toList()
        }
        _clearLog.value = emptyList()
        val origin = MockOrigin(cacheControl = { path -> if (path == PRIVATE_PATH) "private, max-age=60" else "max-age=60" })
        val probe = CachingClient(origin)
        suspend fun counts(): String {
            val public = probe.publicStorage.findAll(Url(ORIGIN + ITEMS_PATH)).size
            val private = probe.privateStorage.findAll(Url(ORIGIN + PRIVATE_PATH)).size
            return "public 저장소 $public 건 · private 저장소 $private 건"
        }
        probe.request(1, "", ITEMS_PATH)
        probe.request(2, "", PRIVATE_PATH)
        add("① $ITEMS_PATH(max-age=60) · $PRIVATE_PATH(private, max-age=60) 요청 → 서버 도달 ${origin.hits.size}회")
        probe.request(3, "", ITEMS_PATH)
        probe.request(4, "", PRIVATE_PATH)
        add("② 같은 두 요청 반복 → 서버 도달 ${origin.hits.size}회(캐시 이벤트 ${probe.fromCacheEvents.get()}회)")
        add("   캐시 상태: ${counts()}")
        probe.client.plugin(HttpCache).clearAllCaches()
        add("③ client.plugin(HttpCache).clearAllCaches() 호출")
        add("   캐시 상태: ${counts()}")
        probe.request(5, "", ITEMS_PATH)
        probe.request(6, "", PRIVATE_PATH)
        add("④ 다시 요청 → 서버 도달 ${origin.hits.size}회(두 요청 모두 서버로 감)")
        probe.close()
    }

    // ==================== 4. FileStorage — kotlinx-io Path vs java.io.File ====================

    fun runFileStorage(cacheRoot: File) = launchScenario("file") {
        val result = withContext(Dispatchers.IO) {
            val directory = File(cacheRoot, "ktor-http-cache").apply {
                deleteRecursively()
                mkdirs()
            }
            val pathEntry = FileStorage(Path(directory.path), SystemFileSystem)
            // 두 입구가 무엇을 돌려주는지 비교만 하고, File 입구용 디렉터리는 바로 지운다
            val fileApiDirectory = File(cacheRoot, "ktor-http-cache-file-api")
            val fileEntry = FileStorage(fileApiDirectory)
            fileApiDirectory.deleteRecursively()

            // 디스크 저장소: 첫 클라이언트로 저장 → 닫고 → 새 클라이언트(같은 디렉터리)로 재요청
            val diskOrigin = MockOrigin(cacheControl = { "max-age=60" })
            val first = CachingClient(diskOrigin, publicStorage = pathEntry)
            first.request(1, "첫 클라이언트")
            first.close()
            val files = directory.walkTopDown().filter { it.isFile }.map { "${it.name.take(12)}… (${it.length()} B)" }.toList()
            val second = CachingClient(diskOrigin, publicStorage = FileStorage(Path(directory.path), SystemFileSystem))
            val diskStep = second.request(2, "새 클라이언트 · 같은 디렉터리")

            // 대조군: 메모리 저장소는 클라이언트가 바뀌면 비어 있다
            val memoryOrigin = MockOrigin(cacheControl = { "max-age=60" })
            CachingClient(memoryOrigin).also { it.request(1, "첫 클라이언트"); it.close() }
            val memoryProbe = CachingClient(memoryOrigin)
            val memoryStep = memoryProbe.request(2, "새 클라이언트 · 메모리 저장소")
            memoryProbe.close()

            second.client.plugin(HttpCache).clearAllCaches()
            val remaining = directory.walkTopDown().count { it.isFile }
            second.close()

            FileStorageResult(
                pathEntryClass = pathEntry::class.simpleName.orEmpty(),
                fileEntryClass = fileEntry::class.simpleName.orEmpty(),
                filesAfterFirstRequest = files,
                newClientWithDisk = diskStep,
                newClientWithMemory = memoryStep,
                filesAfterClear = remaining,
                directoryKept = directory.exists()
            )
        }
        _fileResult.value = result
    }

    // ==================== 5. Accept 헤더 병합 전략 (3.6.0 신규) ====================

    fun runAcceptMerge() = launchScenario("accept") {
        val rows = mutableListOf<AcceptRow>()
        for ((name, strategy) in listOf(
            "Default" to ContentTypeMergeStrategy.Default,
            "SkipIfPresent" to ContentTypeMergeStrategy.SkipIfPresent
        )) {
            val seen = CopyOnWriteArrayList<List<String>>()
            val client = HttpClient(MockEngine { request ->
                seen += request.headers.getAll(HttpHeaders.Accept).orEmpty()
                respond("{}", headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
            }) {
                install(ContentNegotiation) {
                    gson()
                    acceptHeaderMergeStrategy = strategy
                }
            }
            client.get(ORIGIN + ITEMS_PATH)
            rows += AcceptRow(name, null, seen.last())
            client.get(ORIGIN + ITEMS_PATH) { accept(ContentType.Text.Plain) }
            rows += AcceptRow(name, ContentType.Text.Plain.toString(), seen.last())
            client.close()
        }
        _acceptRows.value = rows
    }

    // ==================== 내부 구현 ====================

    private data class ServerHit(val path: String, val conditionalHeader: String?, val status: Int)

    /**
     * MockEngine 으로 만든 원 서버. 조건부 요청 헤더가 현재 ETag/Last-Modified 와 같으면 304 를 돌려준다.
     * 캐시 헤더는 경로별로 다르게 줄 수 있다(clearAllCaches 시나리오의 public/private 두 경로).
     */
    private class MockOrigin(
        val cacheControl: (path: String) -> String?,
        @Volatile var etag: String? = "\"v1\"",
        @Volatile var lastModified: String? = null,
        @Volatile var body: String = "body-v1"
    ) {
        val hits = CopyOnWriteArrayList<ServerHit>()

        val engine = MockEngine { request ->
            val path = request.url.encodedPath
            val ifNoneMatch = request.headers[HttpHeaders.IfNoneMatch]
            val ifModifiedSince = request.headers[HttpHeaders.IfModifiedSince]
            val notModified = when {
                ifNoneMatch != null -> ifNoneMatch == etag
                ifModifiedSince != null -> ifModifiedSince == lastModified
                else -> false
            }
            val conditional = ifNoneMatch?.let { "If-None-Match: $it" }
                ?: ifModifiedSince?.let { "If-Modified-Since: $it" }
            val status = if (notModified) HttpStatusCode.NotModified else HttpStatusCode.OK
            hits += ServerHit(path, conditional, status.value)
            val headers = buildList {
                cacheControl(path)?.let { add(HttpHeaders.CacheControl to listOf(it)) }
                etag?.let { add(HttpHeaders.ETag to listOf(it)) }
                lastModified?.let { add(HttpHeaders.LastModified to listOf(it)) }
            }
            respond(if (notModified) "" else body, status, headersOf(*headers.toTypedArray()))
        }
    }

    /** HttpCache 를 설치한 클라이언트 + 저장소 참조 + 캐시 이벤트 계수기 */
    private class CachingClient(
        private val origin: MockOrigin,
        shared: Boolean = false,
        val publicStorage: CacheStorage = CacheStorage.Unlimited(),
        val privateStorage: CacheStorage = CacheStorage.Unlimited()
    ) {
        val fromCacheEvents = AtomicInteger()

        val client = HttpClient(origin.engine) {
            install(HttpCache) {
                isShared = shared
                // Config 에도 같은 이름의 deprecated 프로퍼티(옛 HttpCacheStorage)가 있어 바깥 프로퍼티를 명시한다
                publicStorage(this@CachingClient.publicStorage)
                privateStorage(this@CachingClient.privateStorage)
            }
        }.also { client ->
            // 신선한 캐시 적중과 304 재검증 모두 이 이벤트가 난다(실측)
            client.monitor.subscribe(HttpCache.HttpResponseFromCache) { fromCacheEvents.incrementAndGet() }
        }

        suspend fun request(index: Int, note: String, path: String = ITEMS_PATH): RequestStep {
            val hitsBefore = origin.hits.size
            val eventsBefore = fromCacheEvents.get()
            val response = client.get(ORIGIN + path)
            val body = response.bodyAsText()
            val hit = origin.hits.getOrNull(hitsBefore)
            return RequestStep(
                index = index,
                note = note,
                reachedServer = hit != null,
                conditionalHeader = hit?.conditionalHeader,
                serverStatus = hit?.status,
                clientStatus = response.status.value,
                fromCacheEvent = fromCacheEvents.get() > eventsBefore,
                body = body
            )
        }

        fun close() = client.close()
    }
}
