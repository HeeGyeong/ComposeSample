package com.example.composesample.presentation.example.component.ui.media.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RenderNode
import android.os.Build
import android.os.SystemClock
import android.util.LruCache
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min

/**
 * TiledImageDeepZoomExample 의 타일 계산·원본 생성·타일 디코더.
 * 화면(TiledImageDeepZoomExampleUI)과 단위 테스트(TiledImageMathTest)가 위쪽 순수 계산 함수를 함께 쓴다.
 */

// ──────────────────────────────────────────────────────────────────────────
// 순수 계산 — Android 클래스 없이 단위 테스트한다
// ──────────────────────────────────────────────────────────────────────────

/** 디코딩한 타일 한 장의 최대 픽셀 크기. 배율과 무관하게 타일 한 장은 512×512 px(ARGB 1MB) 이하다. */
const val TILE_PX = 512

/** 원본 좌표계 사각형(right·bottom 은 포함하지 않음). */
data class SrcRect(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
    val isEmpty: Boolean get() = width <= 0 || height <= 0
}

data class TileKey(val sample: Int, val col: Int, val row: Int)

data class Tile(val key: TileKey, val rect: SrcRect) {
    /** 이 타일을 inSampleSize 로 디코딩했을 때의 픽셀 수 — 가장자리 타일은 올림 */
    val decodedWidth: Int get() = (rect.width + key.sample - 1) / key.sample
    val decodedHeight: Int get() = (rect.height + key.sample - 1) / key.sample
}

/**
 * 화면 배율(화면 px / 원본 px)에 맞는 inSampleSize — 2의 거듭제곱 중 1/scale 을 넘지 않는 가장 큰 값.
 * 화면보다 성기게 디코딩하지 않으므로 늘려 그릴 일이 없다(1:1 보다 확대하면 1).
 */
fun sampleSizeFor(scale: Float): Int {
    var sample = 1
    while (sample * 2 <= 1f / scale) sample *= 2
    return sample
}

/** 화면 변환(이미지 원점의 화면 위치 offset, 배율 scale)에서 지금 보이는 원본 영역. */
fun viewportFor(offsetX: Float, offsetY: Float, scale: Float, viewWidth: Int, viewHeight: Int, imageWidth: Int, imageHeight: Int): SrcRect {
    val left = max(0, ((-offsetX) / scale).toInt())
    val top = max(0, ((-offsetY) / scale).toInt())
    val right = min(imageWidth, kotlin.math.ceil((viewWidth - offsetX) / scale).toInt())
    val bottom = min(imageHeight, kotlin.math.ceil((viewHeight - offsetY) / scale).toInt())
    return SrcRect(left, top, right, bottom)
}

/** 보이는 영역을 덮는 타일 목록. 타일 한 칸은 원본으로 TILE_PX × sample px, 가장자리는 이미지 경계에서 자른다. */
fun visibleTiles(viewport: SrcRect, imageWidth: Int, imageHeight: Int, sample: Int, tilePx: Int = TILE_PX): List<Tile> {
    if (viewport.isEmpty) return emptyList()
    val span = tilePx * sample
    val lastCol = (imageWidth - 1) / span
    val lastRow = (imageHeight - 1) / span
    val firstCol = (viewport.left / span).coerceIn(0, lastCol)
    val endCol = ((viewport.right - 1) / span).coerceIn(0, lastCol)
    val firstRow = (viewport.top / span).coerceIn(0, lastRow)
    val endRow = ((viewport.bottom - 1) / span).coerceIn(0, lastRow)
    val tiles = ArrayList<Tile>()
    for (row in firstRow..endRow) {
        for (col in firstCol..endCol) {
            val rect = SrcRect(col * span, row * span, min((col + 1) * span, imageWidth), min((row + 1) * span, imageHeight))
            tiles += Tile(TileKey(sample, col, row), rect)
        }
    }
    return tiles
}

/** 한 번에 디코딩했을 때의 ARGB_8888 바이트 수(계산값). */
fun argbBytes(width: Int, height: Int, sample: Int = 1): Long {
    val w = (width + sample - 1) / sample
    val h = (height + sample - 1) / sample
    return w.toLong() * h * 4
}

fun mb(bytes: Long): String = "%.1fMB".format(bytes / 1024f / 1024f)

// ──────────────────────────────────────────────────────────────────────────
// 원본 이미지 — APK 에 수십 MB 에셋을 넣지 않고 처음 한 번 그려서 캐시 폴더에 JPEG 로 저장한다
// ──────────────────────────────────────────────────────────────────────────

const val SOURCE_WIDTH = 8000
const val SOURCE_HEIGHT = 6000
private const val BLOCK = 1000
private const val CELL = 100

data class SourceInfo(val file: File, val width: Int, val height: Int, val fileBytes: Long, val createdMs: Long?)

fun sourceFile(cacheDir: File): File = File(cacheDir, "deepzoom_${SOURCE_WIDTH}x$SOURCE_HEIGHT.jpg")

/** 원본 크기를 디코딩 없이 읽는다(inJustDecodeBounds). 파일이 없거나 깨졌으면 null. */
fun readSourceInfo(file: File): SourceInfo? {
    if (!file.exists()) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.path, bounds)
    if (bounds.outWidth != SOURCE_WIDTH || bounds.outHeight != SOURCE_HEIGHT) return null
    return SourceInfo(file, bounds.outWidth, bounds.outHeight, file.length(), null)
}

/**
 * 8000×6000 원본을 그려 JPEG 로 저장한다. 그리는 동안만 RGB_565 비트맵(약 92MB 네이티브)을 잡고 바로 놓는다 —
 * 예제용 준비 단계일 뿐, 실제 앱의 원본은 카메라 파일·다운로드다.
 * 1000px 블록마다 큰 이름(A1~H6), 100px 칸마다 작은 좌표 — 전체 보기에서는 블록만, 1:1 에서는 좌표 글자까지 읽힌다.
 */
suspend fun createSourceImage(cacheDir: File): SourceInfo = withContext(Dispatchers.Default) {
    val started = SystemClock.elapsedRealtime()
    val bitmap = createBitmap(SOURCE_WIDTH, SOURCE_HEIGHT, Bitmap.Config.RGB_565)
    try {
        drawSource(Canvas(bitmap))
        val target = sourceFile(cacheDir)
        val temp = File(cacheDir, target.name + ".tmp")
        temp.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        temp.renameTo(target)
        SourceInfo(target, SOURCE_WIDTH, SOURCE_HEIGHT, target.length(), SystemClock.elapsedRealtime() - started)
    } finally {
        bitmap.recycle()
    }
}

private fun drawSource(canvas: Canvas) {
    val fill = Paint()
    val hues = floatArrayOf(0f, 30f, 55f, 120f, 170f, 200f, 240f, 280f)
    for (by in 0 until SOURCE_HEIGHT / BLOCK) {
        for (bx in 0 until SOURCE_WIDTH / BLOCK) {
            val hue = hues[(bx + by) % hues.size]
            fill.color = Color.HSVToColor(floatArrayOf(hue, 0.25f + 0.08f * by, 0.95f))
            canvas.drawRect(
                (bx * BLOCK).toFloat(), (by * BLOCK).toFloat(),
                ((bx + 1) * BLOCK).toFloat(), ((by + 1) * BLOCK).toFloat(), fill
            )
        }
    }
    val thin = Paint().apply { color = Color.argb(90, 0, 0, 0); strokeWidth = 1f }
    for (x in 0..SOURCE_WIDTH step CELL) canvas.drawLine(x.toFloat(), 0f, x.toFloat(), SOURCE_HEIGHT.toFloat(), thin)
    for (y in 0..SOURCE_HEIGHT step CELL) canvas.drawLine(0f, y.toFloat(), SOURCE_WIDTH.toFloat(), y.toFloat(), thin)
    val thick = Paint().apply { color = Color.argb(200, 0, 0, 0); strokeWidth = 8f }
    for (x in 0..SOURCE_WIDTH step BLOCK) canvas.drawLine(x.toFloat(), 0f, x.toFloat(), SOURCE_HEIGHT.toFloat(), thick)
    for (y in 0..SOURCE_HEIGHT step BLOCK) canvas.drawLine(0f, y.toFloat(), SOURCE_WIDTH.toFloat(), y.toFloat(), thick)

    val small = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(220, 30, 30, 30); textSize = 16f }
    for (y in 0 until SOURCE_HEIGHT step CELL) {
        for (x in 0 until SOURCE_WIDTH step CELL) {
            canvas.drawText("$x,$y", x + 6f, y + 22f, small)
        }
    }
    val big = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(170, 0, 0, 0); textSize = 360f; textAlign = Paint.Align.CENTER; isFakeBoldText = true
    }
    for (by in 0 until SOURCE_HEIGHT / BLOCK) {
        for (bx in 0 until SOURCE_WIDTH / BLOCK) {
            canvas.drawText("${'A' + bx}${by + 1}", bx * BLOCK + BLOCK / 2f, by * BLOCK + BLOCK / 2f + 130f, big)
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 타일 디코더 — 보이는 타일만 BitmapRegionDecoder 로, 전용 스레드 한 개에서, 바이트 기준 LRU 캐시에
// ──────────────────────────────────────────────────────────────────────────

/** 타일 캐시 상한 — 1:1 화면 한 장(타일 6~9장, 9MB 안팎)의 몇 배 */
private const val TILE_CACHE_BYTES = 24 * 1024 * 1024

class TiledImageState(val source: SourceInfo) {
    // 디코딩 전용 스레드 — BitmapRegionDecoder 호출을 한 줄로 세우고, 화면을 떠나면 닫는다
    private val decodeExecutor = Executors.newSingleThreadExecutor { r -> Thread(r, "tile-decoder") }
    private val decodeDispatcher = decodeExecutor.asCoroutineDispatcher()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val decoder: BitmapRegionDecoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        BitmapRegionDecoder.newInstance(source.file.path)
    } else {
        @Suppress("DEPRECATION") // API 31 미만에는 newInstance(String) 이 없다
        BitmapRegionDecoder.newInstance(source.file.path, false)
    } ?: error("BitmapRegionDecoder 생성 실패")

    /** 바닥용 개요 — 전체 보기에 필요한 해상도의 절반으로 한 번만 디코딩해 늘 깐다(타일이 오기 전의 빈칸 방지) */
    var overview: Bitmap? by mutableStateOf(null)
        private set
    var overviewSample by mutableIntStateOf(0)
        private set

    // 밀려난 비트맵은 recycle 하지 않는다 — 같은 프레임에서 그리는 중일 수 있어 GC 에 맡긴다
    private val cache = object : LruCache<TileKey, Bitmap>(TILE_CACHE_BYTES) {
        override fun sizeOf(key: TileKey, value: Bitmap): Int = value.allocationByteCount
    }
    private val inFlight = HashMap<TileKey, Job>()

    // 화면 변환 — 이미지 원점의 화면 위치와 배율
    var scale by mutableFloatStateOf(1f)
    var offsetX by mutableFloatStateOf(0f)
    var offsetY by mutableFloatStateOf(0f)
    var viewWidth by mutableIntStateOf(0)
        private set
    var viewHeight by mutableIntStateOf(0)
        private set

    /** 타일이 도착할 때마다 올려 Canvas 를 다시 그리게 한다 */
    var tileVersion by mutableIntStateOf(0)
        private set
    var decodedCount by mutableIntStateOf(0)
        private set
    var cancelledCount by mutableIntStateOf(0)
        private set
    var lastDecodeMs by mutableFloatStateOf(0f)
        private set
    private var totalDecodeMs = 0f
    // 1:1(inSampleSize 1) 타일의 행별 디코딩 시간 — 파일 안 위치에 따라 비용이 다른지 본다
    private val fullResRowMs = HashMap<Int, MutableList<Float>>()

    val fitScale: Float
        get() = if (viewWidth == 0) 1f else min(viewWidth / source.width.toFloat(), viewHeight / source.height.toFloat())
    val sample: Int get() = sampleSizeFor(scale)
    val viewport: SrcRect get() = viewportFor(offsetX, offsetY, scale, viewWidth, viewHeight, source.width, source.height)
    val visible: List<Tile> get() = visibleTiles(viewport, source.width, source.height, sample)
    /** 화면 가운데가 가리키는 원본 좌표 */
    val viewportCenterX: Float get() = (viewWidth / 2f - offsetX) / scale
    val viewportCenterY: Float get() = (viewHeight / 2f - offsetY) / scale
    val averageDecodeMs: Float get() = if (decodedCount == 0) 0f else totalDecodeMs / decodedCount
    val cacheBytes: Int get() = cache.size()
    val cacheCount: Int get() = cache.snapshot().size
    val inFlightCount: Int get() = inFlight.size
    val evictedCount: Int get() = cache.evictionCount() - evictedBase
    private var evictedBase = 0

    fun cached(key: TileKey): Bitmap? = cache.get(key)

    fun onViewSize(width: Int, height: Int) {
        if (width == viewWidth && height == viewHeight) return
        val first = viewWidth == 0
        viewWidth = width
        viewHeight = height
        if (first) fit()
    }

    suspend fun loadOverview() {
        if (overview != null) return
        // 전체 보기 inSampleSize 의 두 배 — 뷰어 폭 922px(SM-A725F)이면 전체 보기 8 → 개요 16 → 500×375(ARGB 0.7MB)
        val sample = sampleSizeFor(max(viewWidth, 1) / source.width.toFloat()) * 2
        val bitmap = withContext(decodeDispatcher) {
            BitmapFactory.decodeFile(source.file.path, BitmapFactory.Options().apply { inSampleSize = sample })
        }
        overviewSample = sample
        overview = bitmap
    }

    /** 배율을 바꾸되 화면의 한 점(focus)이 같은 원본 지점을 가리키게 한다 — 핀치의 중심 */
    fun zoomBy(factor: Float, focusX: Float, focusY: Float) {
        val newScale = (scale * factor).coerceIn(fitScale, 2f)
        val ratio = newScale / scale
        offsetX = focusX - (focusX - offsetX) * ratio
        offsetY = focusY - (focusY - offsetY) * ratio
        scale = newScale
        clamp()
    }

    fun panBy(dx: Float, dy: Float) {
        offsetX += dx
        offsetY += dy
        clamp()
    }

    fun fit() {
        scale = fitScale
        offsetX = (viewWidth - source.width * scale) / 2f
        offsetY = (viewHeight - source.height * scale) / 2f
    }

    /** 원본의 (srcX, srcY) 를 화면 가운데에 두고 배율을 맞춘다 */
    fun centerOn(srcX: Float, srcY: Float, newScale: Float) {
        scale = newScale.coerceIn(fitScale, 2f)
        offsetX = viewWidth / 2f - srcX * scale
        offsetY = viewHeight / 2f - srcY * scale
        clamp()
    }

    private fun clamp() {
        val contentW = source.width * scale
        val contentH = source.height * scale
        offsetX = if (contentW <= viewWidth) (viewWidth - contentW) / 2f else offsetX.coerceIn(viewWidth - contentW, 0f)
        offsetY = if (contentH <= viewHeight) (viewHeight - contentH) / 2f else offsetY.coerceIn(viewHeight - contentH, 0f)
    }

    /**
     * 지금 보이는 타일 중 캐시에 없는 것만 디코딩 요청하고, 더는 보이지 않는 대기 요청은 취소한다.
     * 빠르게 확대·이동하면 지나간 배율의 타일이 줄을 서는데, 스레드가 한 개라 취소하지 않으면 지금 화면이 그만큼 늦게 찬다.
     */
    fun requestVisible() {
        if (viewWidth == 0) return
        val needed = visible.associateBy { it.key }
        val stale = inFlight.keys.filter { it !in needed }
        stale.forEach { key ->
            inFlight.remove(key)?.cancel()
            cancelledCount++
        }
        needed.values.forEach { tile ->
            if (cache.get(tile.key) != null || tile.key in inFlight) return@forEach
            inFlight[tile.key] = scope.launch {
                val started = SystemClock.elapsedRealtime()
                val bitmap = withContext(decodeDispatcher) {
                    val options = BitmapFactory.Options().apply { inSampleSize = tile.key.sample }
                    val r = tile.rect
                    decoder.decodeRegion(Rect(r.left, r.top, r.right, r.bottom), options)
                }
                inFlight.remove(tile.key)
                if (bitmap != null) {
                    val ms = (SystemClock.elapsedRealtime() - started).toFloat()
                    cache.put(tile.key, bitmap)
                    decodedCount++
                    totalDecodeMs += ms
                    lastDecodeMs = ms
                    if (tile.key.sample == 1) fullResRowMs.getOrPut(tile.key.row) { ArrayList() } += ms
                    tileVersion++
                }
            }
        }
    }

    /** 1:1 타일 중 rows 에 속한 것의 평균 디코딩 시간(ms)과 장수. 없으면 null */
    fun fullResAverage(rows: IntRange): Pair<Float, Int>? {
        val times = rows.flatMap { fullResRowMs[it].orEmpty() }
        return if (times.isEmpty()) null else times.average().toFloat() to times.size
    }

    fun clearCache() {
        inFlight.values.forEach { it.cancel() }
        inFlight.clear()
        cache.evictAll()
        decodedCount = 0
        evictedBase = cache.evictionCount()
        cancelledCount = 0
        totalDecodeMs = 0f
        lastDecodeMs = 0f
        fullResRowMs.clear()
        tileVersion++
        requestVisible()
    }

    fun close() {
        scope.cancel()
        decodeExecutor.execute { decoder.recycle() }
        decodeDispatcher.close()
    }
}

// ──────────────────────────────────────────────────────────────────────────
// 대조군 — decodeFile 로 한 번에 전부
// ──────────────────────────────────────────────────────────────────────────

data class WholeDecodeResult(val text: String, val bytes: Long?, val ms: Long?, val drawProbe: String?)

/**
 * 원본을 inSampleSize 없이 한 번에 디코딩해 할당 크기와 시간을 재고, 그 비트맵을 하드웨어 가속 화면이 그릴 수 있는지
 * RenderNode 녹화 캔버스로 확인한 뒤 바로 놓는다(화면에는 그리지 않는다).
 */
suspend fun decodeWhole(source: SourceInfo): WholeDecodeResult = withContext(Dispatchers.Default) {
    val started = SystemClock.elapsedRealtime()
    val bitmap = try {
        BitmapFactory.decodeFile(source.file.path)
    } catch (e: OutOfMemoryError) {
        return@withContext WholeDecodeResult("OutOfMemoryError: ${e.message}", null, SystemClock.elapsedRealtime() - started, null)
    } ?: return@withContext WholeDecodeResult("디코딩 실패(null)", null, null, null)
    val ms = SystemClock.elapsedRealtime() - started
    val bytes = bitmap.allocationByteCount.toLong()
    val text = "${bitmap.width}×${bitmap.height} ${bitmap.config}"
    val probe = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val node = RenderNode("whole-decode-probe")
        node.setPosition(0, 0, 1, 1)
        val canvas = node.beginRecording()
        try {
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            "그릴 수 있음"
        } catch (e: RuntimeException) {
            "${e::class.java.simpleName}: ${e.message}"
        } finally {
            node.endRecording()
            node.discardDisplayList()
        }
    } else {
        null
    }
    bitmap.recycle()
    WholeDecodeResult(text, bytes, ms, probe)
}
