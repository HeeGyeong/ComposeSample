package com.example.composesample.presentation.example.component.ui.media.picker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.Locale
import java.util.zip.CRC32

// ==================== 올려도 되는 위치 ====================

/**
 * 올려도 되는 위치 — 실제 값이 있을 때(PRESENT)만 돌려준다.
 * 피커가 0 으로 덮은 GPS 는 프레임워크 getLatLong 이 true · (0, 0) 을 돌려준다(방향 글자가 비어도 변환은 양수로 읽는다).
 * 그 결과만 믿으면 적도와 본초자오선이 만나는 기니만 앞바다(Null Island)를 실제 위치로 올리게 된다.
 */
fun usableLocation(reading: GpsReading?): Pair<Double, Double>? =
    reading?.latLong?.takeIf { gpsState(reading) == GpsState.PRESENT }

/** getLatLong 결과만 보는 코드가 보는 것 — 대조용 */
fun naiveLocationText(reading: GpsReading?): String =
    reading?.latLong?.let { "true (${formatLatLong(it)})" } ?: "false"

/**
 * 지울 GPS 태그 — 프레임워크 ExifInterface 의 TAG_GPS_* 31개와 같은 이름(단위 테스트가 대조한다).
 * 위도·경도만 지우면 고도·시각·방위 같은 나머지 GPS 태그가 남는다.
 */
val GPS_TAG_NAMES = listOf(
    "GPSVersionID", "GPSLatitudeRef", "GPSLatitude", "GPSLongitudeRef", "GPSLongitude",
    "GPSAltitudeRef", "GPSAltitude", "GPSTimeStamp", "GPSSatellites", "GPSStatus",
    "GPSMeasureMode", "GPSDOP", "GPSSpeedRef", "GPSSpeed", "GPSTrackRef", "GPSTrack",
    "GPSImgDirectionRef", "GPSImgDirection", "GPSMapDatum", "GPSDestLatitudeRef", "GPSDestLatitude",
    "GPSDestLongitudeRef", "GPSDestLongitude", "GPSDestBearingRef", "GPSDestBearing",
    "GPSDestDistanceRef", "GPSDestDistance", "GPSProcessingMethod", "GPSAreaInformation",
    "GPSDateStamp", "GPSDifferential",
)

/**
 * 제어 문자를 \xNN 으로 — BYTE 형식 태그(GPSVersionID)는 getAttribute 가 바이트를 그대로 글자로 돌려준다.
 * 피커가 0 으로 덮은 값은 NUL 글자가 되고, NUL 이 화면에 있으면 uiautomator dump 가 통째로 실패한다.
 */
fun printable(text: String): String = buildString {
    text.forEach { c -> if (c.code < 0x20 || c.code == 0x7F) append(String.format(Locale.US, "\\x%02X", c.code)) else append(c) }
}

private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

/**
 * PNG eXIf 청크의 CRC 가 맞는지 — PNG 가 아니거나 eXIf 청크가 없으면 null.
 * 피커는 PNG 의 GPS 바이트를 0 으로 바꾸면서 청크 CRC 는 그대로 둔다. 프레임워크 ExifInterface 는 CRC 가 틀리면
 * 로그만 남기고 EXIF 전체를 버려서, 태그가 남아 있는데도 'GPS 태그 없음' · 기종 없음처럼 읽히고 saveAttributes 도 거절한다.
 */
fun pngExifCrcOk(bytes: ByteArray): Boolean? {
    if (bytes.size < PNG_SIGNATURE.size || !bytes.copyOfRange(0, PNG_SIGNATURE.size).contentEquals(PNG_SIGNATURE)) return null
    var offset = PNG_SIGNATURE.size
    while (offset + 12 <= bytes.size) {
        val length = ByteBuffer.wrap(bytes, offset, 4).int
        if (length < 0 || offset + 12 + length > bytes.size) return null
        when (String(bytes, offset + 4, 4, Charsets.US_ASCII)) {
            "eXIf" -> {
                val calculated = CRC32().apply { update(bytes, offset + 4, 4 + length) }.value
                val recorded = ByteBuffer.wrap(bytes, offset + 8 + length, 4).int.toLong() and 0xFFFFFFFFL
                return calculated == recorded
            }
            "IEND" -> return null
        }
        offset += 12 + length
    }
    return null
}

// ==================== 방향(EXIF Orientation) ====================

/** 뷰어가 Orientation 을 적용하는 방식 — 좌우로 뒤집은 다음 시계 방향으로 돌린다(androidx ExifInterface 의 getRotationDegrees · isFlipped 규칙) */
data class OrientationTransform(val degrees: Int, val mirrored: Boolean)

fun orientationTransform(orientation: Int?): OrientationTransform = when (orientation) {
    2 -> OrientationTransform(0, true)
    3 -> OrientationTransform(180, false)
    4 -> OrientationTransform(180, true)
    5 -> OrientationTransform(270, true)
    6 -> OrientationTransform(90, false)
    7 -> OrientationTransform(90, true)
    8 -> OrientationTransform(270, false)
    else -> OrientationTransform(0, false) // 1(그대로) · 0(정의 안 됨) · 태그 없음
}

/** 뷰어에 보이는 가로×세로 — 90°·270° 면 저장된 가로·세로가 뒤바뀐다 */
fun displayedSize(width: Int, height: Int, orientation: Int?): Pair<Int, Int> =
    if (orientationTransform(orientation).degrees % 180 == 0) width to height else height to width

fun orientationText(orientation: Int?): String {
    if (orientation == null) return "태그 없음(= 그대로)"
    val t = orientationTransform(orientation)
    val how = when {
        orientation == 0 -> "정의 안 됨 · 태그가 없어도 프레임워크가 이 값을 채운다"
        orientation !in 1..8 -> "정의 안 됨"
        t.degrees == 0 && !t.mirrored -> "그대로"
        else -> listOfNotNull("좌우 반전".takeIf { t.mirrored }, "시계 ${t.degrees}°".takeIf { t.degrees != 0 }).joinToString(" + ")
    }
    return "$orientation ($how)"
}

/** 긴 변이 maxEdge 이하가 되는 가장 작은 2의 거듭제곱 inSampleSize — BitmapFactory 는 2의 거듭제곱으로 내림해 쓴다 */
fun sampleSizeFor(width: Int, height: Int, maxEdge: Int): Int {
    var sample = 1
    while (maxOf(width, height) / sample > maxEdge) sample *= 2
    return sample
}

// ==================== 내보내기 ====================

/** 받은 사진을 앱 캐시로 복사한 뒤 올리기 전에 고르는 네 가지 */
enum class ExportVariant(val mark: String, val label: String) {
    AS_IS("①", "받은 그대로 복사"),
    STRIP_GPS("②", "GPS 태그 제거 — ExifInterface.saveAttributes"),
    REENCODE("③", "재인코딩 — BitmapFactory → JPEG"),
    REENCODE_UPRIGHT("④", "재인코딩 + 방향을 픽셀에 반영"),
}

/** 내보낸 파일 하나를 같은 판정(GPS · getLatLong · 방향 · 크기)으로 읽은 결과 */
data class ExportCheck(
    val variant: ExportVariant,
    val fileBytes: Long? = null,
    val mime: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val orientation: Int? = null,
    val gps: GpsReading? = null,
    /** 남아 있는 GPS 태그 전부(이름 → 원문) — 위도·경도 말고도 고도·시각이 남는지 */
    val gpsTags: Map<String, String> = emptyMap(),
    val dateTimeOriginal: String? = null,
    val model: String? = null,
    val failure: String? = null,
    /** PNG eXIf 청크 CRC 가 맞는지(PNG 가 아니면 null) */
    val pngExifCrcOk: Boolean? = null,
    /** 뷰어가 보는 방향으로 돌린 미리보기 */
    val preview: Bitmap? = null,
)

/** 화면에 보이는 판정 줄 — 네 사본이 같은 순서·같은 문장으로 비교되도록 한곳에서 만든다 */
fun ExportCheck.lines(): List<String> = buildList {
    failure?.let { add("✗ $it") }
    val bytes = fileBytes ?: return@buildList
    add("${mime ?: "형식 ?"} · 픽셀 ${width ?: "?"}×${height ?: "?"} · 파일 ${String.format(Locale.US, "%,d", bytes)} B")
    if (pngExifCrcOk == false) add("⚠ PNG eXIf 청크 CRC 불일치 — 프레임워크 ExifInterface 가 EXIF 전체를 버렸다(아래가 비어 보이는 이유)")
    add("GPS: ${gpsState(gps).label}")
    add("  getLatLong → ${naiveLocationText(gps)} · 올릴 위치 → ${usableLocation(gps)?.let(::formatLatLong) ?: "없음"}")
    val tags = gpsTags.entries.joinToString(" · ") { "${it.key.removePrefix("GPS")}=${printable(it.value)}" }
    add("  남은 GPS 태그 ${gpsTags.size}개${if (tags.isEmpty()) "" else " — $tags"}")
    val shape = if (width != null && height != null) {
        val (w, h) = displayedSize(width, height, orientation)
        "$w×$h ${if (h > w) "세로" else "가로"}"
    } else {
        "?"
    }
    add("방향 ${orientationText(orientation)} → 보이는 모양 $shape")
    add("촬영 시각 ${dateTimeOriginal ?: "-"} · 기종 ${model ?: "-"}")
}

/** 앱이 직접 만든 파일로 형식마다 saveAttributes 쓰기·지우기를 해 본 결과 */
data class FormatTrial(
    val format: String,
    val writeFailure: String?,
    val afterWrite: GpsReading?,
    val stripFailure: String?,
    val afterStrip: GpsReading?,
)

fun FormatTrial.lines(): List<String> = listOf(
    "$format 쓰기 ${writeFailure?.let { "✗ $it" } ?: "✓"} → ${gpsState(afterWrite).label}${usableLocation(afterWrite)?.let { " " + formatLatLong(it) }.orEmpty()}",
    "  지우기 ${stripFailure?.let { "✗ $it" } ?: "✓"} → ${gpsState(afterStrip).label}",
)

private enum class TrialFormat(val label: String, val extension: String) {
    JPEG("JPEG", "jpg"),
    PNG("PNG", "png"),
    WEBP("WebP", "webp"),
}

// ==================== 실험 로직 ====================

/**
 * 올리기 전 위치 정리 실험 — 받은 URI 를 캐시로 복사해 네 가지로 내보내고 같은 판정으로 다시 읽는다.
 * 받은 URI 자체에는 쓰지 않는다(문서 선택기 결과는 WRITE 권한까지 붙어 오므로 쓰면 사용자 원본이 바뀐다).
 * 파일 작업은 IO 디스패처에서.
 */
class LocationCleanupLab(private val context: Context) {

    private val exportDir get() = File(context.cacheDir, "location_cleanup")
    private val trialDir get() = File(context.cacheDir, "location_cleanup_formats")

    suspend fun exportAll(uri: Uri): List<ExportCheck> = withContext(Dispatchers.IO) {
        val dir = exportDir.apply { deleteRecursively(); mkdirs() }
        val extension = extensionOf(context.contentResolver.getType(uri))
        val source = File(dir, "source.$extension")
        val copied = runCatching {
            context.contentResolver.openInputStream(uri)?.use { input -> source.outputStream().use { input.copyTo(it) } }
                ?: error("openInputStream 이 null")
        }
        copied.exceptionOrNull()?.let { return@withContext listOf(ExportCheck(ExportVariant.AS_IS, failure = describeFailure(it))) }

        ExportVariant.entries.map { variant ->
            when (variant) {
                ExportVariant.AS_IS -> check(variant, source, null)
                ExportVariant.STRIP_GPS -> {
                    val out = source.copyTo(File(dir, "strip.$extension"))
                    check(variant, out, runCatching { stripGps(out) }.exceptionOrNull())
                }
                ExportVariant.REENCODE -> {
                    val out = File(dir, "reencode.jpg")
                    check(variant, out, runCatching { reencode(source, out, upright = false) }.exceptionOrNull())
                }
                ExportVariant.REENCODE_UPRIGHT -> {
                    val out = File(dir, "reencode_upright.jpg")
                    check(variant, out, runCatching { reencode(source, out, upright = true) }.exceptionOrNull())
                }
            }
        }
    }

    /** 형식마다 작은 비트맵을 저장해 GPS 를 써 넣고(saveAttributes) 다시 지운다 — 사진을 고르지 않아도 기기의 지원 형식이 보인다 */
    suspend fun formatTrials(): List<FormatTrial> = withContext(Dispatchers.IO) {
        val dir = trialDir.apply { deleteRecursively(); mkdirs() }
        val bitmap = demoBitmap()
        TrialFormat.entries.map { format ->
            val file = File(dir, "trial.${format.extension}")
            file.outputStream().use { bitmap.compress(compressFormatOf(format), JPEG_QUALITY, it) }
            val write = runCatching { writeDemoGps(file) }.exceptionOrNull()
            val afterWrite = readGps(file)
            val strip = runCatching { stripGps(file) }.exceptionOrNull()
            FormatTrial(
                format = format.label,
                writeFailure = write?.let(::describeFailure),
                afterWrite = afterWrite,
                stripFailure = strip?.let(::describeFailure),
                afterStrip = readGps(file),
            )
        }
    }

    /** 화면을 떠날 때 캐시에 남은 사본을 지운다 */
    fun clear() {
        exportDir.deleteRecursively()
        trialDir.deleteRecursively()
    }

    // -------------------- 쓰기 --------------------

    /** 프레임워크 ExifInterface 는 파일 경로(또는 쓰기 가능한 FD)로 열었을 때만 저장한다 — InputStream 으로 열면 IOException */
    @SuppressLint("ExifInterface") // 의존성을 늘리지 않으려 프레임워크판 — import 줄도 경고 대상이라 전체 이름으로
    private fun stripGps(file: File) {
        val exif = android.media.ExifInterface(file.absolutePath)
        GPS_TAG_NAMES.forEach { exif.setAttribute(it, null) }
        exif.saveAttributes()
    }

    @SuppressLint("ExifInterface")
    private fun writeDemoGps(file: File) {
        val exif = android.media.ExifInterface(file.absolutePath)
        exif.setAttribute(android.media.ExifInterface.TAG_GPS_LATITUDE, "37/1,33/1,5954/100")
        exif.setAttribute(android.media.ExifInterface.TAG_GPS_LATITUDE_REF, "N")
        exif.setAttribute(android.media.ExifInterface.TAG_GPS_LONGITUDE, "126/1,58/1,4066/100")
        exif.setAttribute(android.media.ExifInterface.TAG_GPS_LONGITUDE_REF, "E")
        exif.saveAttributes()
    }

    /** 디코딩한 픽셀만 다시 압축한다 — EXIF 는 하나도 옮기지 않는다 */
    private fun reencode(source: File, out: File, upright: Boolean) {
        val bounds = boundsOf(source)
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, MAX_EDGE)
        }
        val decoded = BitmapFactory.decodeFile(source.absolutePath, options) ?: error("BitmapFactory 가 디코딩하지 못함")
        val bitmap = if (upright) applyOrientation(decoded, readOrientation(source)) else decoded
        out.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
    }

    // -------------------- 읽기 --------------------

    private fun check(variant: ExportVariant, file: File, failure: Throwable?): ExportCheck {
        if (!file.exists()) return ExportCheck(variant, failure = failure?.let(::describeFailure) ?: "파일이 없음")
        val bounds = boundsOf(file)
        val tags = readTags(file)
        val orientation = tags?.orientation
        return ExportCheck(
            variant = variant,
            fileBytes = file.length(),
            mime = bounds.outMimeType,
            width = bounds.outWidth.takeIf { it > 0 },
            height = bounds.outHeight.takeIf { it > 0 },
            orientation = orientation,
            gps = tags?.gps,
            gpsTags = tags?.gpsTags.orEmpty(),
            dateTimeOriginal = tags?.dateTimeOriginal,
            model = tags?.model,
            pngExifCrcOk = if (bounds.outMimeType == "image/png") pngExifCrcOk(file.readBytes()) else null,
            failure = failure?.let(::describeFailure),
            preview = runCatching { preview(file, bounds, orientation) }.getOrNull(),
        )
    }

    private class Tags(
        val gps: GpsReading,
        val gpsTags: Map<String, String>,
        val orientation: Int?,
        val dateTimeOriginal: String?,
        val model: String?,
    )

    @SuppressLint("ExifInterface")
    private fun readTags(file: File): Tags? = runCatching {
        val exif = android.media.ExifInterface(file.absolutePath)
        Tags(
            gps = gpsReadingOf(exif),
            gpsTags = GPS_TAG_NAMES.mapNotNull { name -> exif.getAttribute(name)?.let { name to it } }.toMap(),
            orientation = exif.getAttribute(android.media.ExifInterface.TAG_ORIENTATION)?.toIntOrNull(),
            dateTimeOriginal = exif.getAttribute(android.media.ExifInterface.TAG_DATETIME_ORIGINAL),
            model = exif.getAttribute(android.media.ExifInterface.TAG_MODEL),
        )
    }.getOrNull()

    private fun readGps(file: File): GpsReading? = readTags(file)?.gps

    private fun readOrientation(file: File): Int? = readTags(file)?.orientation

    private fun boundsOf(file: File): BitmapFactory.Options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
        BitmapFactory.decodeFile(file.absolutePath, this)
    }

    private fun preview(file: File, bounds: BitmapFactory.Options, orientation: Int?): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, PREVIEW_EDGE)
        }
        val decoded = BitmapFactory.decodeFile(file.absolutePath, options) ?: return null
        return applyOrientation(decoded, orientation)
    }

    /** 뷰어처럼 Orientation 을 픽셀에 적용한다 — 좌우 반전 먼저, 그다음 회전 */
    private fun applyOrientation(bitmap: Bitmap, orientation: Int?): Bitmap {
        val t = orientationTransform(orientation)
        if (t.degrees == 0 && !t.mirrored) return bitmap
        val matrix = Matrix().apply {
            if (t.mirrored) setScale(-1f, 1f)
            postRotate(t.degrees.toFloat())
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun demoBitmap(): Bitmap = createBitmap(96, 64).apply {
        Canvas(this).apply {
            drawColor(android.graphics.Color.rgb(0x2E, 0x7D, 0x32))
            drawRect(0f, 0f, 96f, 16f, Paint().apply { color = android.graphics.Color.WHITE })
        }
    }

    private fun compressFormatOf(format: TrialFormat): Bitmap.CompressFormat = when (format) {
        TrialFormat.JPEG -> Bitmap.CompressFormat.JPEG
        TrialFormat.PNG -> Bitmap.CompressFormat.PNG
        TrialFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
        } else {
            @Suppress("DEPRECATION") // API 30 미만에는 WEBP_LOSSY 가 없다
            Bitmap.CompressFormat.WEBP
        }
    }

    private fun extensionOf(mime: String?): String = when (mime) {
        "image/png" -> "png"
        "image/webp" -> "webp"
        "image/heic", "image/heif" -> "heic"
        "image/gif" -> "gif"
        else -> "jpg"
    }

    companion object {
        private const val JPEG_QUALITY = 90
        /** 재인코딩 상한 — 큰 원본(50MP 등)을 통째로 디코딩하면 메모리가 모자란다 */
        private const val MAX_EDGE = 4096
        private const val PREVIEW_EDGE = 240
    }
}

/** EXIF 의 GPS 태그 원문과 getLatLong 결과 — 피커 결과(PhotoPickerLab)와 내보낸 사본이 같은 판정을 쓴다 */
@SuppressLint("ExifInterface")
internal fun gpsReadingOf(exif: android.media.ExifInterface): GpsReading {
    val out = FloatArray(2)
    val latLong = if (exif.getLatLong(out)) out[0].toDouble() to out[1].toDouble() else null
    return GpsReading(
        latitude = exif.getAttribute(android.media.ExifInterface.TAG_GPS_LATITUDE),
        latitudeRef = exif.getAttribute(android.media.ExifInterface.TAG_GPS_LATITUDE_REF),
        longitude = exif.getAttribute(android.media.ExifInterface.TAG_GPS_LONGITUDE),
        longitudeRef = exif.getAttribute(android.media.ExifInterface.TAG_GPS_LONGITUDE_REF),
        latLong = latLong,
    )
}
