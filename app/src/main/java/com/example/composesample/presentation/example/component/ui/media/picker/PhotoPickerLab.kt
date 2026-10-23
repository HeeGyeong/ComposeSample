package com.example.composesample.presentation.example.component.ui.media.picker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.ext.SdkExtensions
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.DefaultTab
import androidx.core.content.edit
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * MediaStore.ACTION_PICK_IMAGES — 상수는 API 33 필드지만 API 30~32 도 SDK 확장 R 2 이상이면 같은 액션을 처리한다.
 * 하위 기기에서도 결과를 가르려고 문자열로 둔다(값은 같다).
 */
const val ACTION_PICK_IMAGES = "android.provider.action.PICK_IMAGES"

/** 강조색 옵션 — 진한 색과 아주 밝은 색(피커가 대비를 이유로 무시하는지 본다) */
const val ACCENT_DARK = 0xFF6A1B9AL
const val ACCENT_LIGHT = 0xFFFFF59DL

// ==================== 피커 단계 ====================

/** PickVisualMedia.createIntent 가 고르는 세 단계(activity 1.13.0 소스 그대로 — GMS 전용 단계는 없다) */
enum class PickerTier(val label: String) {
    SYSTEM("① 시스템 피커 · MediaStore.ACTION_PICK_IMAGES"),
    SYSTEM_FALLBACK("② 시스템 앱 대체 피커 · ACTION_SYSTEM_FALLBACK_PICK_IMAGES"),
    OPEN_DOCUMENT("③ 문서 선택기 · ACTION_OPEN_DOCUMENT"),
}

fun tierOf(action: String?): PickerTier? = when (action) {
    ACTION_PICK_IMAGES -> PickerTier.SYSTEM
    PickVisualMedia.ACTION_SYSTEM_FALLBACK_PICK_IMAGES -> PickerTier.SYSTEM_FALLBACK
    Intent.ACTION_OPEN_DOCUMENT -> PickerTier.OPEN_DOCUMENT
    else -> null
}

// ==================== 요청 옵션 ====================

enum class MediaChoice(val label: String, val type: PickVisualMedia.VisualMediaType) {
    IMAGE("이미지", PickVisualMedia.ImageOnly),
    VIDEO("동영상", PickVisualMedia.VideoOnly),
    BOTH("둘 다", PickVisualMedia.ImageAndVideo),
    GIF("GIF 만", PickVisualMedia.SingleMimeType("image/gif")),
}

enum class AccentChoice(val label: String, val color: Long?) {
    NONE("지정 안 함", null),
    DARK("진한 보라", ACCENT_DARK),
    LIGHT("아주 밝은 노랑", ACCENT_LIGHT),
}

/** 화면 칩으로 고른 값 — maxItems 가 null 이면 Builder 기본값(피커 한도)을 그대로 둔다 */
data class PickOptions(
    val media: MediaChoice = MediaChoice.IMAGE,
    val albumsTab: Boolean = false,
    val accent: AccentChoice = AccentChoice.NONE,
    val maxItems: Int? = null,
    val ordered: Boolean = false,
)

fun PickOptions.toRequest(): PickVisualMediaRequest = PickVisualMediaRequest.Builder()
    .setMediaType(media.type)
    .setDefaultTab(if (albumsTab) DefaultTab.AlbumsTab else DefaultTab.PhotosTab)
    .setOrderedSelection(ordered)
    .apply {
        accent.color?.let { setAccentColor(it) }
        maxItems?.let { setMaxItems(it) }
    }
    .build()

// ==================== 표시용 변환 ====================

private val EXTRA_PREFIXES = listOf(
    "android.provider.extra.",
    "androidx.activity.result.contract.extra.",
    "android.intent.extra.",
)

/** 엑스트라 키에서 패키지 접두어를 떼어 화면 폭에 맞춘다 */
fun shortExtraKey(key: String): String =
    EXTRA_PREFIXES.firstOrNull { key.startsWith(it) }?.let { key.removePrefix(it) } ?: key

/** 결과 인텐트에 붙어 온 URI 권한 플래그 */
fun grantFlagsText(flags: Int): String = buildList {
    if (flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0) add("READ")
    if (flags and Intent.FLAG_GRANT_WRITE_URI_PERMISSION != 0) add("WRITE")
    if (flags and Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION != 0) add("PERSISTABLE")
    if (flags and Intent.FLAG_GRANT_PREFIX_URI_PERMISSION != 0) add("PREFIX")
}.ifEmpty { listOf("없음") }.joinToString(" | ")

// ==================== 위치 메타데이터 ====================

/** EXIF GPS 태그 원문과 ExifInterface.getLatLong 결과 */
data class GpsReading(
    val latitude: String?,
    val latitudeRef: String?,
    val longitude: String?,
    val longitudeRef: String?,
    val latLong: Pair<Double, Double>?,
)

enum class GpsState(val label: String) {
    PRESENT("위치 있음"),
    ZEROED("태그는 있는데 0 — 값만 지워짐"),
    ABSENT("GPS 태그 없음"),
    UNREADABLE("EXIF 를 못 읽음"),
}

fun gpsState(reading: GpsReading?): GpsState = when {
    reading == null -> GpsState.UNREADABLE
    listOf(reading.latitude, reading.longitude, reading.latitudeRef, reading.longitudeRef).all { it == null } -> GpsState.ABSENT
    reading.latLong != null && (abs(reading.latLong.first) > 1e-6 || abs(reading.latLong.second) > 1e-6) -> GpsState.PRESENT
    else -> GpsState.ZEROED
}

fun formatLatLong(latLong: Pair<Double, Double>?): String =
    latLong?.let { String.format(Locale.US, "%.5f, %.5f", it.first, it.second) } ?: "없음"

// ==================== 실패 분류 ====================

enum class FailureKind(val label: String) {
    SECURITY("SecurityException — 권한 없음"),
    ILLEGAL_ARGUMENT("IllegalArgumentException"),
    UNSUPPORTED("UnsupportedOperationException"),
    NOT_FOUND("FileNotFoundException"),
    OTHER("그 밖의 예외"),
}

/**
 * 예외를 원인 사슬로 가른다 — debug(HotSwan 재작성)는 checked 예외(FileNotFoundException)를
 * UndeclaredThrowableException 으로 감쌀 수 있어 바깥 타입만 보면 틀린다(ComposeHotReloadGuide).
 */
fun classify(t: Throwable): FailureKind {
    val chain = generateSequence(t) { it.cause }.take(8).toList()
    return when {
        chain.any { it is SecurityException } -> FailureKind.SECURITY
        chain.any { it is UnsupportedOperationException } -> FailureKind.UNSUPPORTED
        chain.any { it is IllegalArgumentException } -> FailureKind.ILLEGAL_ARGUMENT
        chain.any { it is java.io.FileNotFoundException } -> FailureKind.NOT_FOUND
        else -> FailureKind.OTHER
    }
}

/** '타입: 메시지 첫 줄' — 감싸기만 하는 예외는 건너뛴다 */
fun describeFailure(t: Throwable): String {
    val shown = generateSequence(t) { it.cause }.take(8).firstOrNull {
        it !is java.lang.reflect.UndeclaredThrowableException && it !is java.lang.reflect.InvocationTargetException
    } ?: t
    val message = shown.message?.substringBefore('\n')?.take(140)?.let { ": $it" }.orEmpty()
    return "${shown.javaClass.simpleName}$message"
}

// ==================== 결과 행 ====================

/** 피커가 돌려준 결과 그대로 — contract.parseResult 를 거치기 전 */
data class RawResult(
    val source: String,
    val resultCode: Int,
    val dataUri: Uri?,
    val clipCount: Int,
    val flags: Int,
    val parsed: List<Uri>,
    val time: String,
)

/** 받은 URI 하나를 읽어 본 결과 */
data class UriInspection(
    val uri: Uri,
    val displayName: String?,
    val size: Long?,
    val mime: String?,
    val bytesRead: Long?,
    val gps: GpsReading?,
    val dateTimeOriginal: String?,
    val model: String?,
    val failure: String?,
)

/** 마지막으로 받은 URI — 새 액티비티·새 프로세스에서 다시 읽으려고 SharedPreferences 에 적는다 */
data class SavedPick(val source: String, val uris: List<Uri>, val pid: Int, val time: String)

// ==================== 실험 로직 ====================

/**
 * 시스템 Photo Picker 실험 — 화면 상태는 UI 가 들고, 이 클래스는 기기 조회·URI 읽기·권한 조작만 한다.
 * URI 읽기는 IO 디스패처에서.
 */
class PhotoPickerLab(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // -------------------- 기기 --------------------

    /** SDK 확장 버전 — 피커 기능은 API 레벨이 아니라 이 숫자로 켜진다(R 2 = 피커, R 12 = 탭·강조색, T 23 = 위치 요청) */
    fun extensionLines(): List<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return listOf("SDK 확장 없음(API 30 미만)")
        val levels = listOf(
            "R" to Build.VERSION_CODES.R,
            "S" to Build.VERSION_CODES.S,
            "T" to Build.VERSION_CODES.TIRAMISU,
            "U" to Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        ).map { (name, code) -> "$name ${runCatching { SdkExtensions.getExtensionVersion(code) }.getOrDefault(0)}" }
        return listOf("SDK 확장 " + levels.joinToString(" · "))
    }

    /** 한 번에 고를 수 있는 최대 개수 — 시스템 피커가 있을 때만 의미가 있다 */
    fun pickImagesMaxLimit(): Int? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> MediaStore.getPickImagesMaxLimit()
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R) >= 2 -> MediaStore.getPickImagesMaxLimit()
        else -> null
    }

    fun handlerOf(intent: Intent): String {
        val info = resolve(intent, PackageManager.MATCH_DEFAULT_ONLY) ?: return "처리 앱 없음"
        val a = info.activityInfo
        return "${a.packageName}/${a.name.substringAfterLast('.')}"
    }

    /** contract 의 ②단계 판정과 같은 조회 — 시스템 앱만(MATCH_SYSTEM_ONLY) */
    fun fallbackHandler(): String {
        val info = resolve(
            Intent(PickVisualMedia.ACTION_SYSTEM_FALLBACK_PICK_IMAGES),
            PackageManager.MATCH_DEFAULT_ONLY or PackageManager.MATCH_SYSTEM_ONLY
        ) ?: return "없음"
        return "${info.activityInfo.packageName}/${info.activityInfo.name.substringAfterLast('.')}"
    }

    /** 같은 액션을 처리하는 액티비티 전부 — 기기에 피커가 둘 이상 있는지(새 photopicker 앱 + MediaProvider 모듈) */
    fun handlersOf(intent: Intent): List<String> {
        val pm = context.packageManager
        val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong()))
        } else {
            @Suppress("DEPRECATION") // API 33 미만에는 ResolveInfoFlags 판이 없다
            pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        }
        return list.map { "${it.activityInfo.packageName}/${it.activityInfo.name.substringAfterLast('.')} (priority ${it.priority})" }
    }

    private fun resolve(intent: Intent, flags: Int): ResolveInfo? {
        val pm = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.resolveActivity(intent, PackageManager.ResolveInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION") // API 33 미만에는 ResolveInfoFlags 판이 없다
            pm.resolveActivity(intent, flags)
        }
    }

    /** contract 가 만든 인텐트를 그대로 펼친다 — 어떤 옵션이 어떤 엑스트라로 가는지(또는 빠지는지) */
    fun describeIntent(intent: Intent): List<String> = buildList {
        add("action = ${intent.action}")
        add("type = ${intent.type}")
        intent.component?.let { add("component = ${it.packageName}/${it.className.substringAfterLast('.')}") }
        val extras = intent.extras ?: return@buildList
        extras.keySet().sorted().forEach { key ->
            @Suppress("DEPRECATION") // 타입이 섞인 엑스트라를 표시용으로 꺼낸다 — 타입별 getter 는 키마다 타입을 알아야 한다
            val value = when (val v = extras.get(key)) {
                is Long -> if (key.contains("ACCENT")) "0x%08X".format(v) else v.toString()
                is Array<*> -> v.joinToString(prefix = "[", postfix = "]")
                else -> v.toString()
            }
            add("${shortExtraKey(key)} = $value")
        }
    }

    /** contract 의 ③단계와 같은 인텐트 — 시스템 피커가 있는 기기에서도 문서 선택기 결과와 대조하려고 직접 만든다 */
    fun openDocumentIntent(): Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "image/*"
    }

    fun rawResult(source: String, resultCode: Int, data: Intent?, parsed: List<Uri>) = RawResult(
        source = source,
        resultCode = resultCode,
        dataUri = data?.data,
        clipCount = data?.clipData?.itemCount ?: 0,
        flags = data?.flags ?: 0,
        parsed = parsed,
        time = now(),
    )

    // -------------------- URI 읽기 --------------------

    suspend fun inspect(uri: Uri): UriInspection = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var name: String? = null
        var size: Long? = null
        val metaFailure = runCatching {
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    name = c.getString(0)
                    size = if (c.isNull(1)) null else c.getLong(1)
                }
            }
        }.exceptionOrNull()
        val mime = runCatching { resolver.getType(uri) }.getOrNull()
        val read = runCatching {
            resolver.openInputStream(uri)?.use { stream ->
                val buffer = ByteArray(64 * 1024)
                var total = 0L
                while (true) {
                    val n = stream.read(buffer)
                    if (n < 0) break
                    total += n
                }
                total
            }
        }
        val failure = (read.exceptionOrNull() ?: metaFailure)?.let { describeFailure(it) }
        val exif = if (read.isSuccess && mime?.startsWith("image/") == true) readExif(uri) else null
        UriInspection(
            uri = uri,
            displayName = name,
            size = size,
            mime = mime,
            bytesRead = read.getOrNull(),
            gps = exif?.first,
            dateTimeOriginal = exif?.second,
            model = exif?.third,
            failure = failure,
        )
    }

    /**
     * 프레임워크 ExifInterface(InputStream, API 24+) — lint 는 androidx.exifinterface 를 권하지만(옛 플랫폼 파서의 보안 버그)
     * 이 예제는 의존성을 늘리지 않으려 프레임워크판으로 GPS·촬영 시각·기종 태그만 읽는다.
     */
    @SuppressLint("ExifInterface") // import 줄도 경고 대상이라 import 없이 전체 이름으로 쓴다
    private fun readExif(uri: Uri): Triple<GpsReading, String?, String?>? = runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val exif = android.media.ExifInterface(stream)
            val out = FloatArray(2)
            val latLong = if (exif.getLatLong(out)) out[0].toDouble() to out[1].toDouble() else null
            Triple(
                GpsReading(
                    latitude = exif.getAttribute(android.media.ExifInterface.TAG_GPS_LATITUDE),
                    latitudeRef = exif.getAttribute(android.media.ExifInterface.TAG_GPS_LATITUDE_REF),
                    longitude = exif.getAttribute(android.media.ExifInterface.TAG_GPS_LONGITUDE),
                    longitudeRef = exif.getAttribute(android.media.ExifInterface.TAG_GPS_LONGITUDE_REF),
                    latLong = latLong,
                ),
                exif.getAttribute(android.media.ExifInterface.TAG_DATETIME_ORIGINAL),
                exif.getAttribute(android.media.ExifInterface.TAG_MODEL),
            )
        }
    }.getOrNull()

    /** 원본 요청 — MediaStore URI 라면 ACCESS_MEDIA_LOCATION 이 있어야 위치가 남은 원본을 연다. 피커 URI 에도 통하는지 본다 */
    suspend fun openOriginal(uri: Uri): String = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@withContext "API 29 미만 — setRequireOriginal 없음"
        val original = runCatching { MediaStore.setRequireOriginal(uri) }
            .getOrElse { return@withContext "setRequireOriginal → ${describeFailure(it)}" }
        runCatching {
            context.contentResolver.openInputStream(original)?.use { it.read() }
            "열림 · ${original.query}"
        }.getOrElse { describeFailure(it) }
    }

    // -------------------- 권한 수명 --------------------

    fun persist(uri: Uri): String = runCatching {
        context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        "takePersistableUriPermission 성공"
    }.getOrElse { describeFailure(it) }

    /**
     * 이 앱이 쥔 영구 URI 권한 — 이 예제가 받은 것만 URI 를 보이고, 다른 예제(SAF 등)가 받은 것은 개수만 센다
     * (실기기에서는 사용자가 고른 파일 이름이 URI 에 들어 있다).
     */
    fun persistedSummary(): Pair<List<String>, Int> {
        val saved = loadSaved()?.uris?.toSet().orEmpty()
        val all = context.contentResolver.persistedUriPermissions
        val ours = all.filter { it.uri in saved }.map {
            "${it.uri} · ${if (it.isReadPermission) "READ" else "-"}${if (it.isWritePermission) " WRITE" else ""}"
        }
        return ours to (all.size - ours.size)
    }

    /** 이 예제가 받은 URI 의 영구 권한만 놓는다 */
    fun releaseSaved(): Int {
        val saved = loadSaved()?.uris?.toSet().orEmpty()
        var released = 0
        context.contentResolver.persistedUriPermissions.filter { it.uri in saved }.forEach {
            runCatching {
                context.contentResolver.releasePersistableUriPermission(it.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                released++
            }
        }
        return released
    }

    fun save(source: String, uris: List<Uri>) {
        if (uris.isEmpty()) return
        prefs.edit {
            putString(KEY_SOURCE, source)
            putString(KEY_URIS, uris.joinToString("\n"))
            putInt(KEY_PID, Process.myPid())
            putString(KEY_TIME, now())
        }
    }

    fun loadSaved(): SavedPick? {
        val uris = prefs.getString(KEY_URIS, null)?.split("\n")?.filter { it.isNotBlank() }?.map { it.toUri() } ?: return null
        return SavedPick(
            source = prefs.getString(KEY_SOURCE, "?").orEmpty(),
            uris = uris,
            pid = prefs.getInt(KEY_PID, 0),
            time = prefs.getString(KEY_TIME, "").orEmpty(),
        )
    }

    fun clearSaved() = prefs.edit { clear() }

    private fun now(): String = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

    companion object {
        private const val PREFS = "system_photo_picker_demo"
        private const val KEY_SOURCE = "source"
        private const val KEY_URIS = "uris"
        private const val KEY_PID = "pid"
        private const val KEY_TIME = "time"
    }
}
