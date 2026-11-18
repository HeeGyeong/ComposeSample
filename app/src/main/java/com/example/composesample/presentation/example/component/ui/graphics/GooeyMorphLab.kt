package com.example.composesample.presentation.example.component.ui.graphics

import kotlin.math.max
import kotlin.math.pow

/**
 * Gooey 텍스트 모핑의 계산 — 원문(Juan Silva 'How Morph Text dissolves one word into the next', MorphText.kt)의
 * 진행 분할 · 블러/알파 곡선 · 알파 임계 행렬과, PixelCopy 로 읽은 화면의 잉크 분석.
 * Compose · Android 에 기대지 않는 순수 Kotlin 이라 JVM 단위 테스트(GooeyMorphLabTest)로 고정한다.
 */

/** 원문 코드의 기본 조절값(라이브 페이지 값은 [GooeyLivePagePreset]) */
internal data class GooeyMorphConfig(
    val morphSeconds: Float = 1f,
    val cooldownSeconds: Float = 0.25f,
    val blurBaseDp: Float = 8f,
    val blurMaxDp: Float = 40f,
    val thresholdGain: Float = 18f,
    // Android 의 ColorMatrix 오프셋 열은 채널과 같은 0~255 척도
    val thresholdBias: Float = -7f,
    val morphSequence: Float = 0.5f
)

/** 원문 데모 페이지에 걸린 값(원문 조절표) — morphSequence 7.24 는 반으로 접혀 0.499 에서 멈춘다 */
internal val GooeyLivePagePreset = GooeyMorphConfig(
    morphSeconds = 1.36f,
    cooldownSeconds = 0.79f,
    blurBaseDp = 12.19f,
    blurMaxDp = 20.76f,
    thresholdGain = 3.9f,
    thresholdBias = -38.25f,
    morphSequence = 7.24f
)

/** 한 단어를 그릴 값 — 진행 t(1 = 또렷) · 알파 · 블러 반경 */
internal data class WordLook(val t: Float, val alpha: Float, val blurDp: Float)

internal object GooeyMorph {
    /** 블러 식의 0 나눗셈을 막는 t 하한(원문과 같음) */
    const val MinT = 0.001f

    /** 알파 곡선 지수 — t^0.4 는 늦게까지 진하게 남아 임계에 넘길 블러 꼬리를 늘린다 */
    const val AlphaExponent = 0.4f

    /** 떠나는 단어가 그대로 버티는 앞부분 p — morphSequence 의 절반, 0.499 상한 */
    fun hold(sequence: Float): Float = (sequence * 0.5f).coerceIn(0f, 0.499f)

    /** 떠나는 단어의 t1 — 앞 p 동안 1, 그 뒤 0 으로 */
    fun outgoingT(fraction: Float, sequence: Float): Float {
        val p = hold(sequence)
        return ((1f - fraction) / (1f - p)).coerceIn(0f, 1f)
    }

    /** 오는 단어의 t2 — 앞 p 동안 0, 그 뒤 1 로 */
    fun incomingT(fraction: Float, sequence: Float): Float {
        val p = hold(sequence)
        return ((fraction - p) / (1f - p)).coerceIn(0f, 1f)
    }

    fun alpha(t: Float): Float = t.coerceIn(0f, 1f).pow(AlphaExponent)

    /** blurBase / t − blurBase 를 상한으로 자른다 — t = 1 이면 0, t = 0.5 면 blurBase, t = 0.25 면 3 × blurBase */
    fun blurDp(t: Float, baseDp: Float, maxDp: Float): Float =
        (baseDp / max(t, MinT) - baseDp).coerceIn(0f, maxDp)

    /** 블러가 상한에 닿는 t — base / (base + max) */
    fun capT(baseDp: Float, maxDp: Float): Float = baseDp / (baseDp + maxDp)

    fun look(t: Float, config: GooeyMorphConfig): WordLook =
        WordLook(t, alpha(t), blurDp(t, config.blurBaseDp, config.blurMaxDp))

    /** RGB 는 그대로 두고 알파 행만 gain · bias 인 4×5 행렬(행 우선) */
    fun thresholdMatrix(gain: Float, bias: Float): FloatArray = floatArrayOf(
        1f, 0f, 0f, 0f, 0f,
        0f, 1f, 0f, 0f, 0f,
        0f, 0f, 1f, 0f, 0f,
        0f, 0f, 0f, gain, bias
    )

    /** 오프셋을 0~255 척도로 읽을 때(Android ColorMatrix 문서)의 출력 알파 */
    fun thresholdAlpha(alpha255: Float, gain: Float, bias: Float): Float =
        (gain * alpha255 + bias).coerceIn(0f, 255f)

    /** 오프셋을 −1~1(정규화) 척도로 읽는다고 가정할 때의 출력 알파 — 원문이 말한 옛 Compose Multiplatform/Skia 쪽 */
    fun thresholdAlphaNormalizedOffset(alpha255: Float, gain: Float, bias: Float): Float =
        (gain * alpha255 + bias * 255f).coerceIn(0f, 255f)

    /** 출력이 0 을 넘기 시작하는 입력 알파(0~255) */
    fun cutoff(gain: Float, bias: Float): Float = (-bias / gain).coerceAtLeast(0f)

    /** 출력이 255 로 꽉 차는 입력 알파(0~255) */
    fun saturation(gain: Float, bias: Float): Float = (255f - bias) / gain
}

/** withFrameMillis 루프가 넘기는 시간으로 움직이는 진행 — 모핑(fraction 0 → 1) → 쉼 → 다음 단어 */
internal class MorphClock(private val config: GooeyMorphConfig = GooeyMorphConfig()) {
    var index: Int = 0
        private set
    var fraction: Float = 0f
        private set
    var cooling: Boolean = false
        private set
    private var cooldownLeft = 0f

    /** @return 단어가 바뀌었으면 true(화면은 이때만 리컴포즈한다) */
    fun advance(dtSeconds: Float, wordCount: Int): Boolean {
        if (wordCount < 2 || dtSeconds <= 0f) return false
        if (!cooling) {
            fraction = (fraction + dtSeconds / config.morphSeconds).coerceAtMost(1f)
            if (fraction >= 1f) {
                cooling = true
                cooldownLeft = config.cooldownSeconds
            }
            return false
        }
        cooldownLeft -= dtSeconds
        if (cooldownLeft > 0f) return false
        index = (index + 1) % wordCount
        fraction = 0f
        cooling = false
        return true
    }

    /** 멈춘 상태에서 슬라이더로 옮길 때 — 쉼은 버리고 그 진행으로 */
    fun seek(fraction: Float) {
        this.fraction = fraction.coerceIn(0f, 1f)
        cooling = false
    }
}

/** 화면 캡처 한 장의 분석 결과 */
internal data class InkStats(
    val width: Int,
    val height: Int,
    /** 덮임 0.5 이상인 픽셀 수 */
    val inkPixels: Int,
    /** 잉크 픽셀이 8방향으로 이어진 덩어리 수([InkAnalysis.MinBlobPixels] 미만은 뺀다) */
    val blobs: Int,
    /** 맨 바깥 1px 테두리에 걸린 잉크 픽셀 수 — 0 보다 크면 그림이 경계에서 잘렸다는 뜻 */
    val borderInk: Int,
    /** 덮임 0.03 ~ 0.5 의 옅은 픽셀 수 — 임계가 다 못 걷어 낸 블러 안개 */
    val hazePixels: Int,
    /** 가운데 세로줄의 잉크 픽셀 수 — 좌우 두 덩어리 사이 '목'의 굵기 */
    val centerColumnInk: Int
)

/** 흰 배경 위에 한 가지 잉크 색으로 그린 화면을 ARGB 픽셀로 받아 덮임 · 덩어리를 센다 */
internal object InkAnalysis {
    const val InkCoverage = 0.5f
    const val HazeCoverage = 0.03f
    const val MinBlobPixels = 4

    /**
     * 흰(255) 배경에 잉크 채널 c 를 덮임 a 로 섞으면 v = 255 − (255 − c)·a → a = (255 − v) / (255 − c).
     * 잉크와 흰색의 차이가 가장 큰 채널 하나로 읽는다(검정이면 어느 채널이든 a = (255 − v) / 255).
     */
    fun coverage(pixel: Int, inkArgb: Int): Float {
        val shift = strongestChannelShift(inkArgb)
        val ink = (inkArgb shr shift) and 0xFF
        val value = (pixel shr shift) and 0xFF
        val span = 255 - ink
        if (span <= 0) return 0f
        return ((255 - value).toFloat() / span).coerceIn(0f, 1f)
    }

    private fun strongestChannelShift(inkArgb: Int): Int {
        val r = (inkArgb shr 16) and 0xFF
        val g = (inkArgb shr 8) and 0xFF
        val b = inkArgb and 0xFF
        return when (minOf(r, g, b)) {
            r -> 16
            g -> 8
            else -> 0
        }
    }

    fun analyze(pixels: IntArray, width: Int, height: Int, inkArgb: Int): InkStats {
        require(pixels.size == width * height) { "pixels ${pixels.size} != $width × $height" }
        val ink = BooleanArray(pixels.size)
        var inkPixels = 0
        var haze = 0
        var border = 0
        var center = 0
        val centerX = width / 2
        for (i in pixels.indices) {
            val c = coverage(pixels[i], inkArgb)
            if (c >= InkCoverage) {
                ink[i] = true
                inkPixels++
                val x = i % width
                val y = i / width
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) border++
                if (x == centerX) center++
            } else if (c >= HazeCoverage) {
                haze++
            }
        }
        return InkStats(width, height, inkPixels, countBlobs(ink, width, height), border, haze, center)
    }

    /** 8방향 연결 덩어리 수 — 재귀 대신 배열 스택(큰 덩어리에서 스택 넘침 방지) */
    fun countBlobs(ink: BooleanArray, width: Int, height: Int, minPixels: Int = MinBlobPixels): Int {
        val seen = BooleanArray(ink.size)
        val stack = IntArray(ink.size)
        var blobs = 0
        for (start in ink.indices) {
            if (!ink[start] || seen[start]) continue
            var top = 0
            var size = 0
            stack[top++] = start
            seen[start] = true
            while (top > 0) {
                val i = stack[--top]
                size++
                val x = i % width
                val y = i / width
                for (dy in -1..1) {
                    val ny = y + dy
                    if (ny < 0 || ny >= height) continue
                    for (dx in -1..1) {
                        val nx = x + dx
                        if ((dx == 0 && dy == 0) || nx < 0 || nx >= width) continue
                        val n = ny * width + nx
                        if (ink[n] && !seen[n]) {
                            seen[n] = true
                            stack[top++] = n
                        }
                    }
                }
            }
            if (size >= minPixels) blobs++
        }
        return blobs
    }

    /** 검정을 알파 a 로 흰 바탕에 덮은 견본의 가운데 픽셀들 → a(0~255) 평균 */
    fun blackAlphaOnWhite(pixels: IntArray): Float {
        if (pixels.isEmpty()) return 0f
        var sum = 0L
        for (p in pixels) sum += 255 - ((p shr 8) and 0xFF)
        return sum.toFloat() / pixels.size
    }
}
