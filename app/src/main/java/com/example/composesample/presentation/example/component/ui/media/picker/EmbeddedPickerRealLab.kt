package com.example.composesample.presentation.example.component.ui.media.picker

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.os.ext.SdkExtensions
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
 * Embedded Photo Picker 실물(#154) — 두 예제(View 판 · Compose 판)의 '실물' 탭이 함께 쓰는 기기 조건 · 이벤트 기록 · 받은 URI 추적.
 * 화면 없이 쓸 수 있는 부분(조건 판정 · 기록 · 항목 갱신)은 문자열 키로만 다뤄 JVM 단위 테스트에서 돈다.
 */

/** 임베디드 피커가 뜨는 최소 조건 — API 34 + U 확장 15(@RequiresExtension(UPSIDE_DOWN_CAKE, 15)) */
const val EMBEDDED_PICKER_MIN_API = 34
const val EMBEDDED_PICKER_MIN_EXTENSION = 15

/** 플랫폼 EmbeddedPhotoPickerFeatureInfo.Builder 메서드가 요구하는 U 확장(compileSdk 37.1 api-versions.xml) */
enum class PickerFeatureLevel(val extension: Int, val label: String) {
    Base(15, "최대 개수 · 순서 번호 · MIME · 강조색 · 미리 선택 · 다크 모드"),
    Highlight(19, "앨범 · 검색어 강조(setHighlightAlbumId · setHighlightSearchMediaTextQuery)"),
    LaunchExpanded(21, "펼친 채 시작(setPickerLaunchedInExpandedState) · Builder(기존 값) 복사"),
    SelectionParams(22, "선택 · UI 맞춤 파라미터(setSelectionParams · setUiCustomizationParams)"),
    LaunchTab(23, "시작 탭 · 접힌 상태 스크롤 · 위치 정보 요청(setLaunchTab 등)")
}

/** 기기 조건 — SDK_INT 와 U 확장 버전 두 숫자로만 판정한다 */
data class EmbeddedPickerSupport(val sdkInt: Int, val extensionU: Int) {
    val available: Boolean get() = sdkInt >= EMBEDDED_PICKER_MIN_API && extensionU >= EMBEDDED_PICKER_MIN_EXTENSION

    fun supports(level: PickerFeatureLevel): Boolean = available && extensionU >= level.extension

    val reason: String
        get() = when {
            sdkInt < EMBEDDED_PICKER_MIN_API -> "API $sdkInt — Android 14(API 34) 미만이라 플랫폼에 피커 클래스가 없다"
            extensionU < EMBEDDED_PICKER_MIN_EXTENSION ->
                "API $sdkInt · U 확장 $extensionU — 확장 $EMBEDDED_PICKER_MIN_EXTENSION 미만(Google Play 시스템 업데이트로 올라간다)"
            else -> "API $sdkInt · U 확장 $extensionU — 사용 가능"
        }
}

/** 이 기기의 조건. U 확장은 API 30 미만에 SdkExtensions 가 없어 0 으로 본다 */
fun currentEmbeddedPickerSupport(): EmbeddedPickerSupport {
    val extension = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        SdkExtensions.getExtensionVersion(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    } else {
        0
    }
    return EmbeddedPickerSupport(Build.VERSION.SDK_INT, extension)
}

/** 실물 탭 이벤트 종류 */
enum class PickerEventKind(val label: String) {
    SessionOpened("세션 열림"),
    SessionClosed("세션 닫힘"),
    Granted("권한 받음"),
    Revoked("권한 회수"),
    Complete("선택 완료"),
    Error("세션 오류"),
    AppDeselect("앱이 해제 요청"),
    Expanded("펼침 알림"),
    Visibility("가시성"),
    Config("설정")
}

/** atMillis 는 첫 이벤트 기준 경과 시간 */
data class PickerEvent(val seq: Int, val atMillis: Long, val kind: PickerEventKind, val detail: String)

/** 최근 이벤트를 위에서부터 보여 주는 기록 — 종류별 개수는 잘린 뒤에도 남는다 */
class PickerEventLog(
    private val now: () -> Long = SystemClock::uptimeMillis,
    private val capacity: Int = 30,
    private val sink: (PickerEvent) -> Unit = {}
) {
    private val _events = mutableStateListOf<PickerEvent>()
    val events: List<PickerEvent> get() = _events
    private val counts = IntArray(PickerEventKind.entries.size)
    private var seq = 0
    private var origin = -1L

    fun add(kind: PickerEventKind, detail: String = "") {
        val t = now()
        if (origin < 0) origin = t
        counts[kind.ordinal]++
        val event = PickerEvent(++seq, t - origin, kind, detail)
        _events.add(0, event)
        sink(event)
        while (_events.size > capacity) _events.removeAt(_events.lastIndex)
    }

    fun count(kind: PickerEventKind): Int = counts[kind.ordinal]
}

/** URI 를 읽은 결과 — 회수 뒤에 다시 읽어 권한이 정말 사라졌는지 본다 */
sealed interface UriRead {
    data class Bytes(val count: Long) : UriRead
    data class Denied(val message: String) : UriRead
    data class Failed(val type: String, val message: String) : UriRead
}

fun UriRead.summary(): String = when (this) {
    is UriRead.Bytes -> "읽힘 ${count / 1024}KB"
    is UriRead.Denied -> "SecurityException"
    is UriRead.Failed -> type
}

/** 받은 URI 한 개의 이력. key 는 uri.toString() */
data class PickedItem(
    val key: String,
    val grantedAt: Long,
    val selected: Boolean = true,
    val grantCount: Int = 1,
    val readAtGrant: UriRead? = null,
    val readAfterRevoke: UriRead? = null,
    val latestRead: UriRead? = null,
    val deselectRequestedAt: Long? = null,
    val revokeLatencyMillis: Long? = null
)

/** 권한 받음 — 처음이면 끝에 붙이고, 회수됐던 것이면 다시 선택으로 되돌린다(이전 회수 기록은 지운다) */
fun List<PickedItem>.granted(keys: List<String>, at: Long): List<PickedItem> {
    val result = toMutableList()
    keys.forEach { key ->
        val i = result.indexOfFirst { it.key == key }
        if (i < 0) {
            result += PickedItem(key = key, grantedAt = at)
        } else {
            val old = result[i]
            result[i] = old.copy(
                selected = true,
                grantCount = old.grantCount + 1,
                readAfterRevoke = null,
                deselectRequestedAt = null,
                revokeLatencyMillis = null
            )
        }
    }
    return result
}

/** 권한 회수 — 앱이 해제를 요청한 항목이면 요청부터 회수 콜백까지 걸린 시간을 남긴다 */
fun List<PickedItem>.revoked(keys: List<String>, at: Long): List<PickedItem> = map { item ->
    if (item.key in keys) {
        item.copy(selected = false, revokeLatencyMillis = item.deselectRequestedAt?.let { at - it })
    } else {
        item
    }
}

/** 앱 쪽 해제 요청 시각 기록 */
fun List<PickedItem>.deselectRequested(key: String, at: Long): List<PickedItem> =
    map { if (it.key == key) it.copy(deselectRequestedAt = at) else it }

/** 회수 콜백 없이 기다린 시간이 넘은 해제 요청(앱이 직접 상태를 고쳐야 하는 경우) */
fun List<PickedItem>.unansweredDeselects(now: Long, waitMillis: Long): List<PickedItem> =
    filter { it.selected && it.deselectRequestedAt != null && now - it.deselectRequestedAt >= waitMillis }

/** 앱이 피커를 치운 뒤 이 시간 안에 온 오류는 닫힌 세션의 것으로 본다(실측 도착 20~600ms) */
const val CLOSING_ERROR_WINDOW_MILLIS = 1000L

/** 닫기 요청 뒤 경과 시간으로 '닫은 세션의 오류' 인지 가른다. 닫은 적이 없으면(null) 진짜 오류 */
fun isClosingError(sinceCloseMillis: Long?): Boolean =
    sinceCloseMillis != null && sinceCloseMillis in 0..CLOSING_ERROR_WINDOW_MILLIS

/** 긴 피커 URI 를 끝 두 마디만 남겨 줄인다 */
fun shortUri(key: String): String {
    val parts = key.split('/').filter { it.isNotEmpty() }
    return if (parts.size <= 2) key else "…/" + parts.takeLast(2).joinToString("/")
}

/** 기록 문장 안의 content:// URI 를 모두 shortUri 로 줄인다(화면 표시용 — logcat 에는 전체가 남는다) */
fun shortenUris(text: String): String =
    Regex("""content://[^\s,]+""").replace(text) { shortUri(it.value) }

/** 두 실물 탭이 함께 쓰는 상태 — 이벤트 기록 · 받은 URI 이력 · 읽기 결과 · 썸네일 */
class PickerLabState(
    private val resolver: ContentResolver,
    private val scope: CoroutineScope,
    private val now: () -> Long = SystemClock::uptimeMillis
) {
    val log = PickerEventLog(now) { Log.i(LAB_TAG, "+${it.atMillis}ms ${it.kind.name} ${it.detail}") }
    private val _items = mutableStateListOf<PickedItem>()
    val items: List<PickedItem> get() = _items
    private val uris = mutableMapOf<String, Uri>()
    val thumbnails = mutableStateMapOf<String, ImageBitmap>()
    var sessionsOpened by mutableIntStateOf(0)
        private set

    fun uriOf(key: String): Uri? = uris[key]

    fun sessionOpened(detail: String) {
        sessionsOpened++
        log.add(PickerEventKind.SessionOpened, "#$sessionsOpened $detail")
    }

    fun granted(granted: List<Uri>) {
        val at = now()
        val keys = granted.map { it.toString() }
        granted.forEach { uris[it.toString()] = it }
        log.add(PickerEventKind.Granted, keys.joinToString())
        replaceAll(_items.toList().granted(keys, at))
        granted.forEach { uri -> scope.launch { updateRead(uri, afterRevoke = false) } }
    }

    fun revoked(revoked: List<Uri>) {
        val at = now()
        val keys = revoked.map { it.toString() }
        val latency = _items.firstOrNull { it.key in keys && it.deselectRequestedAt != null }
            ?.let { at - (it.deselectRequestedAt ?: at) }
        log.add(PickerEventKind.Revoked, keys.joinToString() + (latency?.let { " · 요청 뒤 ${it}ms" } ?: ""))
        replaceAll(_items.toList().revoked(keys, at))
        revoked.forEach { uri -> scope.launch { updateRead(uri, afterRevoke = true) } }
    }

    private var lastCloseRequestAt = -1L

    /** 앱이 세션을 닫으려고 피커를 치운 시각 — 그 직후 오는 오류를 닫힌 세션 것으로 가른다 */
    fun closeRequested() {
        lastCloseRequestAt = now()
    }

    /**
     * 세션 오류 — 화면에는 종류 · 메시지, logcat 에는 스택까지.
     * onError 는 세션이 아니라 state 하나에 묶여 있어, 앱이 닫은 세션의 서비스가 보낸 오류도 같은 곳으로 온다.
     * 닫은 세션의 오류면 true.
     */
    fun sessionError(where: String, throwable: Throwable): Boolean {
        Log.w(LAB_TAG, "sessionError($where)", throwable)
        val sinceClose = if (lastCloseRequestAt < 0) null else now() - lastCloseRequestAt
        val closing = isClosingError(sinceClose)
        log.add(
            PickerEventKind.Error,
            "$where ${throwable::class.java.simpleName}: ${throwable.message}" +
                (sinceClose?.let { " · 닫기 요청 뒤 ${it}ms" } ?: "") +
                if (closing) " → 닫은 세션의 오류(대체 UI 로 넘기지 않음)" else ""
        )
        if (!closing) realErrors++
        return closing
    }

    /** 닫은 세션 탓이 아닌 오류 수 — 대체 UI 로 넘길지 판단에 쓴다 */
    var realErrors by mutableIntStateOf(0)
        private set

    fun deselectRequested(key: String) {
        log.add(PickerEventKind.AppDeselect, key)
        replaceAll(_items.toList().deselectRequested(key, now()))
    }

    fun unanswered(waitMillis: Long): List<PickedItem> = _items.toList().unansweredDeselects(now(), waitMillis)

    /** 받은 URI 를 지금 모두 다시 읽는다 — 세션을 닫거나 다시 연 뒤에도 권한이 남는지 본다 */
    fun rereadAll() {
        val targets = _items.toList().mapNotNull { item -> uris[item.key] }
        log.add(PickerEventKind.Config, "모두 다시 읽기 ${targets.size}개")
        targets.forEach { uri ->
            scope.launch {
                val (read, _) = readUri(resolver, uri)
                Log.i(LAB_TAG, "read latest $uri → $read")
                val i = _items.indexOfFirst { it.key == uri.toString() }
                if (i >= 0) _items[i] = _items[i].copy(latestRead = read)
            }
        }
    }

    private suspend fun updateRead(uri: Uri, afterRevoke: Boolean) {
        val (read, thumb) = readUri(resolver, uri)
        val key = uri.toString()
        if (thumb != null) thumbnails[key] = thumb
        val i = _items.indexOfFirst { it.key == key }
        Log.i(LAB_TAG, "read ${if (afterRevoke) "afterRevoke" else "atGrant"} $key → $read")
        if (i >= 0) {
            _items[i] = if (afterRevoke) _items[i].copy(readAfterRevoke = read) else _items[i].copy(readAtGrant = read)
        }
    }

    private fun replaceAll(next: List<PickedItem>) {
        _items.clear()
        _items.addAll(next)
    }
}

/** logcat 태그 — 기기 측정은 adb logcat -s EmbeddedPickerLab 로 거둔다 */
private const val LAB_TAG = "EmbeddedPickerLab"

/** URI 를 끝까지 읽어 바이트 수를 세고, 읽히면 작은 썸네일도 만든다(IO 스레드) */
suspend fun readUri(resolver: ContentResolver, uri: Uri): Pair<UriRead, ImageBitmap?> = withContext(Dispatchers.IO) {
    try {
        val bytes = resolver.openInputStream(uri)?.use { input ->
            val buffer = ByteArray(16 * 1024)
            var total = 0L
            while (true) {
                val n = input.read(buffer)
                if (n < 0) break
                total += n
            }
            total
        } ?: return@withContext UriRead.Failed("null", "openInputStream 이 null") to null
        val thumb = resolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, BitmapFactory.Options().apply { inSampleSize = 16 })?.asImageBitmap()
        }
        UriRead.Bytes(bytes) to thumb
    } catch (e: SecurityException) {
        UriRead.Denied(e.message.orEmpty()) to null
    } catch (e: Exception) {
        UriRead.Failed(e::class.java.simpleName, e.message.orEmpty()) to null
    }
}
