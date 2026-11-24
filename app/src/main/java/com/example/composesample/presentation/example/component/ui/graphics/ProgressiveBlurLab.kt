package com.example.composesample.presentation.example.component.ui.graphics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SweepGradientShader
import androidx.compose.ui.graphics.blur.BlurRadiusSpec
import androidx.compose.ui.graphics.blur.BlurStop
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToLong

// ==================== 공통 ====================

/** 상태를 바꾼 뒤 화면에 실제로 그려질 때까지 — 프레임 몇 개 + RenderThread 여유 */
suspend fun settleFrames(frames: Int = 3) {
    repeat(frames) { withFrameNanos { } }
    delay(60)
}

/** 리컴포지션 횟수 — 상태가 아닌 평범한 필드(SideEffect 가 올린다) */
class RecomposeTally {
    var count = 0
}

/**
 * 픽셀 계산 — 읽은 캡처(수십만 픽셀)를 훑는다. 메인 스레드가 아니라 [Dispatchers.Default] 에서 부르고(아래 sharpnessOf),
 * debug 의 HotSwan 재작성에서 뺐다(build.gradle 주석 — 재작성 아래에서는 한 장에 수 초가 걸려 ANR 이 났다).
 */
object BlurPixelMath {
    fun luminance(argb: Int): Float {
        val r = (argb ushr 16) and 0xFF
        val g = (argb ushr 8) and 0xFF
        val b = argb and 0xFF
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    /** 선명도 — 가로 · 세로 이웃 픽셀 밝기 차의 평균. 블러가 걸리면 줄어든다 */
    fun sharpness(pixels: IntArray, width: Int, height: Int): Double {
        if (width < 2 || height < 2) return 0.0
        var sum = 0.0
        var n = 0
        for (y in 0 until height - 1) {
            val row = y * width
            for (x in 0 until width - 1) {
                val p = luminance(pixels[row + x])
                sum += abs(p - luminance(pixels[row + x + 1])) + abs(p - luminance(pixels[row + width + x]))
                n += 2
            }
        }
        return sum / n
    }

    /**
     * [marginPx] 만큼 넓게 읽은 픽셀에서 — 바깥 띠(원래 칸 밖)의 '배경과 다른' 픽셀 수와,
     * 안쪽 가장자리 [ringPx] 띠의 평균 밝기.
     */
    fun edgeStats(pixels: IntArray, width: Int, height: Int, marginPx: Int, ringPx: Int, background: Int, tolerance: Int = 24): EdgeStats {
        var spill = 0
        var ringSum = 0.0
        var ringN = 0
        val innerRight = width - marginPx
        val innerBottom = height - marginPx
        for (y in 0 until height) for (x in 0 until width) {
            val p = pixels[y * width + x]
            val inside = x in marginPx until innerRight && y in marginPx until innerBottom
            if (!inside) {
                var d = 0
                for (shift in intArrayOf(16, 8, 0)) d = max(d, abs(((p ushr shift) and 0xFF) - ((background ushr shift) and 0xFF)))
                if (d > tolerance) spill++
            } else if (x < marginPx + ringPx || x >= innerRight - ringPx || y < marginPx + ringPx || y >= innerBottom - ringPx) {
                ringSum += luminance(p)
                ringN++
            }
        }
        return EdgeStats(spill, if (ringN == 0) 0.0 else ringSum / ringN)
    }
}

internal suspend fun sharpnessOf(captured: CapturedPixels?): Double? = captured?.let {
    withContext(Dispatchers.Default) { BlurPixelMath.sharpness(it.pixels, it.width, it.height) }
}

internal suspend fun edgeStatsOf(captured: CapturedPixels?, marginPx: Int, background: Int): EdgeStats? = captured?.let {
    withContext(Dispatchers.Default) { BlurPixelMath.edgeStats(it.pixels, it.width, it.height, marginPx, 3, background) }
}

/** 블러 전 선명도 대비 — 1 이면 그대로, 0 에 가까울수록 흐리다 */
fun sharpnessRatio(value: Double, reference: Double): Double = if (reference <= 0.0) 0.0 else value / reference

/** 블러가 '걸렸다'고 보는 선(기준 대비 선명도) */
const val BLURRED_BELOW = 0.8

/** 소수 [digits] 자리 반올림 — String.format 의 기기 로캘(쉼표 소수점)을 피한다 */
fun fixed2(value: Double, digits: Int = 2): String {
    var scale = 1L
    repeat(digits) { scale *= 10 }
    val scaled = (abs(value) * scale).roundToLong()
    val sign = if (value < 0 && scaled != 0L) "-" else ""
    if (digits == 0) return "$sign$scaled"
    return "$sign${scaled / scale}.${(scaled % scale).toString().padStart(digits, '0')}"
}

fun ratioText(value: Double?): String = value?.let { "${fixed2(it)}배" } ?: "-"

// ==================== 1. 반경 모양 ====================

/** BlurRadiusSpec 팩토리 — 균일만 API 31+, 나머지는 API 33+ (31~32 는 identity 효과로 선명하게) */
enum class SpecKind(val label: String, val minApi: Int, val code: String) {
    Uniform("균일", 31, "BlurRadiusSpec.uniform(r)"),
    Vertical("세로", 33, "BlurRadiusSpec.verticalGradient(startRadius = 0.dp, endRadius = r)"),
    VerticalStops(
        "세로 stop 3개", 33,
        "BlurRadiusSpec.verticalGradient(listOf(\n    BlurStop(0f, r), BlurStop(0.5f, 0.dp), BlurStop(1f, r)))"
    ),
    Horizontal("가로", 33, "BlurRadiusSpec.horizontalGradient(startRadius = 0.dp, endRadius = r)"),
    Linear("대각선", 33, "BlurRadiusSpec.linearGradient(\n    DpOffset.Zero, DpOffset(size.width, size.height), 0.dp, r)"),
    Radial("방사형", 33, "BlurRadiusSpec.radialGradient(startRadius = 0.dp, endRadius = r)"),
    ShaderMask(
        "셰이더 마스크", 33,
        "BlurRadiusSpec.shader(maxRadius = r) { sizePx ->\n    SweepGradientShader(가운데, 투명 → 검정 → 투명) // 알파 = 세기\n}"
    )
}

val galleryRadiusChoices = listOf(4f, 12f, 24f)

/** 블록 안에서 부른다 — size 는 BlurScope.size(레이어 크기, dp) */
fun specFor(kind: SpecKind, radiusDp: Float, size: DpSize): BlurRadiusSpec {
    val r = radiusDp.dp
    return when (kind) {
        SpecKind.Uniform -> BlurRadiusSpec.uniform(r)
        SpecKind.Vertical -> BlurRadiusSpec.verticalGradient(0.dp, r)
        SpecKind.VerticalStops ->
            BlurRadiusSpec.verticalGradient(listOf(BlurStop(0f, r), BlurStop(0.5f, 0.dp), BlurStop(1f, r)))
        SpecKind.Horizontal -> BlurRadiusSpec.horizontalGradient(0.dp, r)
        SpecKind.Linear -> BlurRadiusSpec.linearGradient(DpOffset.Zero, DpOffset(size.width, size.height), 0.dp, r)
        SpecKind.Radial -> BlurRadiusSpec.radialGradient(startRadius = 0.dp, endRadius = r)
        SpecKind.ShaderMask -> BlurRadiusSpec.shader(r) { sizePx ->
            SweepGradientShader(
                center = Offset(sizePx.width / 2f, sizePx.height / 2f),
                colors = listOf(Color.Transparent, Color.Black, Color.Transparent)
            )
        }
    }
}

/** 이 기기에서 그 스펙이 어떻게 되나 — ui-graphics 1.13.0 의 UniformBlurRenderEffect · RuntimeShaderBlurRenderEffect 그대로 */
enum class SpecSupport(val label: String, val blurs: Boolean) {
    Applied("적용", true),
    IdentityBelow33("API 31~32 — identity 효과(선명)", false),
    IgnoredBelow31("API 31 미만 — 무시(선명)", false)
}

fun specSupport(kind: SpecKind, sdk: Int): SpecSupport = when {
    sdk < 31 -> SpecSupport.IgnoredBelow31
    sdk < kind.minApi -> SpecSupport.IdentityBelow33
    else -> SpecSupport.Applied
}

data class GalleryResult(val kind: SpecKind, val support: SpecSupport, val ratio: Double) {
    val blurred: Boolean get() = ratio < BLURRED_BELOW
    val matchesExpectation: Boolean get() = blurred == support.blurs
}

internal class GalleryLabState {
    var kind by mutableStateOf(SpecKind.Vertical)
    var radiusDp by mutableFloatStateOf(12f)

    /** 재는 동안 잠깐 블러를 끄는 스위치 — 같은 자리의 '블러 전' 선명도를 잰다 */
    var blurOff by mutableStateOf(false)
    var running by mutableStateOf(false)
        private set
    val bounds = MeasuredBounds()
    val results = mutableStateMapOf<SpecKind, GalleryResult>()

    suspend fun measureAll(sdk: Int, capture: suspend (Rect) -> CapturedPixels?) {
        if (running) return
        running = true
        try {
            results.clear()
            for (k in SpecKind.entries) {
                kind = k
                blurOff = true
                settleFrames()
                val reference = sharpnessOf(capture(bounds.bounds)) ?: continue
                blurOff = false
                settleFrames()
                val blurred = sharpnessOf(capture(bounds.bounds)) ?: continue
                results[k] = GalleryResult(k, specSupport(k, sdk), sharpnessRatio(blurred, reference))
            }
        } finally {
            blurOff = false
            running = false
        }
    }
}

// ==================== 2. 가장자리 처리 ====================

enum class EdgeChoice(val label: String, val code: String) {
    Rectangle("Rectangle(기본)", "BlurredEdgeTreatment.Rectangle"),
    Unbounded("Unbounded", "BlurredEdgeTreatment.Unbounded"),
    Rounded("Shape(둥근 24dp)", "BlurredEdgeTreatment(RoundedCornerShape(24.dp))");

    val treatment: BlurredEdgeTreatment
        get() = when (this) {
            Rectangle -> BlurredEdgeTreatment.Rectangle
            Unbounded -> BlurredEdgeTreatment.Unbounded
            Rounded -> BlurredEdgeTreatment(RoundedCornerShape(24.dp))
        }
}

const val EDGE_RADIUS_DP = 16f
const val EDGE_MARGIN_DP = 24f

data class EdgeStats(val spill: Int, val ringLuminance: Double)

data class EdgeResult(val edge: EdgeChoice, val reference: EdgeStats, val blurred: EdgeStats) {
    /** 블러 전보다 새로 생긴 바깥 번짐 픽셀 */
    val newSpill: Int get() = (blurred.spill - reference.spill).coerceAtLeast(0)

    /** 안쪽 가장자리 띠가 밝아진 양(0~255) — 가장자리가 투명해져 흰 바탕이 비치면 커진다 */
    val ringBrighter: Double get() = blurred.ringLuminance - reference.ringLuminance
}

internal class EdgeLabState {
    var edge by mutableStateOf(EdgeChoice.Rectangle)
    var blurOff by mutableStateOf(false)
    var running by mutableStateOf(false)
        private set

    /** 여백까지 포함한 바깥 상자 */
    val bounds = MeasuredBounds()
    val results = mutableStateMapOf<EdgeChoice, EdgeResult>()

    suspend fun measureAll(marginPx: Int, background: Int, capture: suspend (Rect) -> CapturedPixels?) {
        if (running) return
        running = true
        try {
            results.clear()
            for (e in EdgeChoice.entries) {
                edge = e
                blurOff = true
                settleFrames()
                val ref = edgeStatsOf(capture(bounds.bounds), marginPx, background) ?: continue
                blurOff = false
                settleFrames()
                val got = edgeStatsOf(capture(bounds.bounds), marginPx, background) ?: continue
                results[e] = EdgeResult(e, ref, got)
            }
        } finally {
            blurOff = false
            running = false
        }
    }
}

// ==================== 3. satin glass 헤더 ====================

const val GLASS_HEADER_DP = 72f
const val GLASS_FADE_DP = 48f
const val GLASS_MAX_RADIUS_DP = 18f
const val GLASS_SCROLL_DP = 420f
const val GLASS_SCROLL_MILLIS = 900

/** 스크롤 진행 0..1 — 첫 칸이 [rampPx] 만큼 올라가면 1 */
fun glassProgress(firstIndex: Int, firstOffsetPx: Int, rampPx: Float): Float =
    if (firstIndex > 0 || rampPx <= 0f) 1f else (firstOffsetPx / rampPx).coerceIn(0f, 1f)

/** 헤더 밑 띠만 흐리고 그 아래 [GLASS_FADE_DP] 에서 0 으로 — 레이어 높이(dp) 기준 비율 */
fun glassStops(amount: Float, layerHeightDp: Float, maxRadiusDp: Float = GLASS_MAX_RADIUS_DP): List<BlurStop> {
    val r = (maxRadiusDp * amount.coerceIn(0f, 1f)).dp
    if (layerHeightDp <= 0f) return listOf(BlurStop(0f, r), BlurStop(1f, 0.dp))
    val header = (GLASS_HEADER_DP / layerHeightDp).coerceIn(0f, 1f)
    val fade = ((GLASS_HEADER_DP + GLASS_FADE_DP) / layerHeightDp).coerceIn(header, 1f)
    return listOf(BlurStop(0f, r), BlurStop(header, r), BlurStop(fade, 0.dp), BlurStop(1f, 0.dp))
}

fun glassSpec(amount: Float, layerHeightDp: Float): BlurRadiusSpec =
    BlurRadiusSpec.verticalGradient(glassStops(amount, layerHeightDp))

enum class GlassVariant(val label: String, val detail: String) {
    Composition(
        "① 컴포지션에서 읽기",
        "val p = glassProgress(listState…) 를 컴포지션에서 계산하고 blur { } 블록은 그 값을 받기만 한다 — 스크롤 한 프레임마다 목록을 품은 함수가 다시 실행된다"
    ),
    Block(
        "② blur { } 블록에서 읽기",
        "Modifier.blur { blurRadiusSpec = glassSpec(glassProgress(listState…), size.height.value) } — 레이어를 설정할 때 읽으므로 리컴포지션이 없다"
    )
}

data class GlassRun(val variant: GlassVariant, val frames: Int, val recompositions: Int, val blockRuns: Int, val scrimDraws: Int) {
    val matchesExpectation: Boolean
        get() = when (variant) {
            GlassVariant.Composition -> recompositions * 2 >= frames
            GlassVariant.Block -> recompositions == 0 && blockRuns * 2 >= frames
        }
}

class GlassLabState {
    val listState = LazyListState()
    var variant by mutableStateOf(GlassVariant.Block)
    var running by mutableStateOf(false)
        private set
    val hostRecompositions = RecomposeTally()

    /** 블러 블록 · 스크림 그리기 횟수 — 상태가 아닌 필드(세는 것이 다시 그리기를 부르면 안 된다) */
    var blockRuns = 0
    var scrimDraws = 0
    val results = mutableStateMapOf<GlassVariant, GlassRun>()

    suspend fun measureAll(scrollPx: Float) {
        if (running) return
        running = true
        try {
            results.clear()
            for (v in GlassVariant.entries) {
                variant = v
                listState.scrollToItem(0)
                settleFrames()
                hostRecompositions.count = 0
                blockRuns = 0
                scrimDraws = 0
                var frames = 0
                coroutineScope {
                    val ticker = launch { while (true) withFrameNanos { frames++ } }
                    listState.animateScrollBy(scrollPx, tween(GLASS_SCROLL_MILLIS, easing = LinearEasing))
                    ticker.cancel()
                }
                settleFrames(2)
                results[v] = GlassRun(v, frames, hostRecompositions.count, blockRuns, scrimDraws)
            }
            listState.scrollToItem(0)
        } finally {
            running = false
        }
    }
}

// ==================== 4. 공유 요소가 헤더 밑으로 돌아올 때 ====================

enum class ReturnVariant(val label: String, val detail: String) {
    Overlay(
        "① 기본(오버레이)",
        "sharedElement 기본값 — 전환 동안 칸은 SharedTransitionLayout 의 오버레이에 그려진다. 목록에 단 blur 도, 헤더의 스크림도 그 위에는 없다"
    ),
    InPlace(
        "② 오버레이 안 씀",
        "sharedElement(renderInOverlayDuringTransition = false) — 칸을 목록 안에서 그려 전환 중에도 blur · 스크림을 함께 받는다"
    ),
    FadeInAfter(
        "③ 원문 해법",
        "뒤로 가는 전환 동안은 블러를 끄고(스크림만), 전환이 끝난 뒤 800ms 에 걸쳐 블러를 올린다"
    )
}

const val RETURN_TRANSITION_MILLIS = 700
const val BLUR_FADE_IN_MILLIS = 800
const val RETURN_TILE_COUNT = 30
const val RETURN_COLUMNS = 3
const val RETURN_TILE_HEIGHT_DP = 96f
const val RETURN_SPACING_DP = 8f

/** 헤더 밑에 둘 칸 — 둘째 줄 가운데 */
const val RETURN_TARGET_INDEX = 4

/** 둘째 줄 첫 칸을 화면 위 4dp 에 오게 하는 스크롤 오프셋(dp) — contentPadding top 이 헤더 높이라 첫 칸은 헤더 바로 아래에서 시작한다 */
const val RETURN_SCROLL_OFFSET_DP = GLASS_HEADER_DP - 4f

/** 전환 중 캡처 시점 — 경계 애니메이션이 이만큼 진행했을 때(선형) */
const val RETURN_SAMPLE_FRACTION = 0.85f

data class ReturnRun(
    val variant: ReturnVariant,
    val reference: Double,
    val during: Double?,
    val firstAfter: Double?,
    val after150: Double?,
    val after800: Double?
) {
    fun ratio(value: Double?): Double? = value?.let { sharpnessRatio(it, reference) }

    /** 전환 85% → 끝난 첫 프레임 사이 선명도가 한 번에 떨어진 양(기준 대비) */
    val jump: Double?
        get() {
            val a = ratio(during) ?: return null
            val b = ratio(firstAfter) ?: return null
            return a - b
        }
}

internal class ReturnLabState {
    val gridState = LazyGridState()
    var variant by mutableStateOf(ReturnVariant.Overlay)
    var selected by mutableStateOf<Int?>(null)

    /** 헤더 밑 블러의 세기 0..1 — ③ 만 뒤로 가는 동안 0, 끝나면 [BLUR_FADE_IN_MILLIS] 에 걸쳐 1 */
    val blurAmount = Animatable(1f)
    var running by mutableStateOf(false)
        private set

    /** SharedTransitionLayout 이 채운다 — 전환(경계 애니메이션)이 도는 중인가 */
    var transitionActive: () -> Boolean = { false }

    /** 헤더 밑에 둔 목표 칸의 창 좌표 */
    val targetBounds = MeasuredBounds()
    val results = mutableStateMapOf<ReturnVariant, ReturnRun>()

    suspend fun placeTargetUnderHeader(offsetPx: Int) {
        gridState.scrollToItem((RETURN_TARGET_INDEX / RETURN_COLUMNS) * RETURN_COLUMNS, offsetPx)
    }

    suspend fun open(index: Int = RETURN_TARGET_INDEX) {
        selected = index
        awaitTransitionEnd()
    }

    /** 뒤로 — ③ 은 전환 동안 블러를 끄고, 끝나면 서서히 올린다 */
    suspend fun back() {
        if (selected == null) return
        if (variant == ReturnVariant.FadeInAfter) blurAmount.snapTo(0f)
        selected = null
        awaitTransitionEnd()
        if (variant == ReturnVariant.FadeInAfter) {
            blurAmount.animateTo(1f, tween(BLUR_FADE_IN_MILLIS, easing = LinearEasing))
        }
    }

    private suspend fun awaitTransitionStart() {
        var waited = 0
        while (!transitionActive() && waited < 20) {
            withFrameNanos { }
            waited++
        }
    }

    private suspend fun awaitTransitionEnd() {
        awaitTransitionStart()
        while (transitionActive()) withFrameNanos { }
    }

    /**
     * 세 방식마다: 목표 칸을 헤더 밑에 두고 → 블러 없는 선명도(기준) → 칸 열기 → 뒤로 가며
     * 전환 85% · 끝난 첫 프레임 · +150ms · +800ms 의 목표 칸 자리 선명도를 잰다.
     */
    suspend fun measureAll(offsetPx: Int, capture: suspend (Rect) -> CapturedPixels?) = coroutineScope {
        if (running) return@coroutineScope
        running = true
        try {
            results.clear()
            for (v in ReturnVariant.entries) {
                variant = v
                selected = null
                blurAmount.snapTo(1f)
                settleFrames()
                placeTargetUnderHeader(offsetPx)
                settleFrames(4)
                val region = targetBounds.bounds.deflate(4f)
                blurAmount.snapTo(0f)
                settleFrames()
                val reference = sharpnessOf(capture(region)) ?: continue
                blurAmount.snapTo(1f)
                settleFrames()
                open()
                settleFrames()
                delay(200)

                val going = launch { back() }
                awaitTransitionStart()
                // 읽기만 하고 계산은 끝난 뒤에 — 시점은 프레임 수가 아니라 시간으로(debug · release 가 같은 순간을 잰다)
                val startedAt = System.nanoTime()
                delayUntil(startedAt + (RETURN_TRANSITION_MILLIS * RETURN_SAMPLE_FRACTION).toLong() * 1_000_000L)
                val during = if (transitionActive()) capture(region) else null
                while (transitionActive()) withFrameNanos { }
                val firstAfter = capture(region)
                val endedAt = System.nanoTime()
                delayUntil(endedAt + 150_000_000L)
                val after150 = capture(region)
                delayUntil(endedAt + 800_000_000L)
                val after800 = capture(region)
                going.join()
                results[v] = ReturnRun(
                    v, reference, sharpnessOf(during), sharpnessOf(firstAfter), sharpnessOf(after150), sharpnessOf(after800)
                )
            }
        } finally {
            selected = null
            blurAmount.snapTo(1f)
            running = false
        }
    }
}

private suspend fun delayUntil(nanoTime: Long) {
    val remaining = (nanoTime - System.nanoTime()) / 1_000_000L
    if (remaining > 0) delay(remaining)
}
