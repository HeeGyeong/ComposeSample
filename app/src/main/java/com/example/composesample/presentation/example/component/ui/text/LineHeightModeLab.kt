package com.example.composesample.presentation.example.component.ui.text

import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToLong

/*
 * 줄 높이 모드(LineHeightStyle.Mode, Compose 1.13 PerLine) 비교 — TextStyleExampleUI 의 보강(#186).
 * 같은 문단을 모드마다 배치하고, 한 줄만 보이게(나머지는 투명 span) 오프스크린에 그려
 * ① 줄 상자(getLineTop/Bottom) ② 그 줄 잉크가 상자 밖으로 나간 정도 ③ 이웃 줄 잉크와 겹친 픽셀 을 센다.
 * 색 span 은 MetricAffectingSpan 이 아니라 배치를 바꾸지 않는다 — 그래도 줄 상자가 같은지 매번 확인한다.
 */

/** 본문 글자 크기 · 요청 줄 높이(1.5배) · 큰 글씨 크기 · 미리보기 폭 — 화면 · 테스트가 같은 값을 쓴다 */
const val LH_FONT_SP = 14f
const val LH_LINE_HEIGHT_SP = 21f
const val LH_BIG_FONT_SP = 30f
const val LH_PREVIEW_WIDTH_DP = 260f

/** 오프스크린 렌더 위아래 여백 — 요청 줄 높이의 배수(상자 밖으로 나간 잉크를 담는다) */
private const val MARGIN_LINES = 2

/** 비교하는 줄 높이 방식. Natural 은 lineHeight 를 주지 않은 기준(줄마다 글꼴이 정한 높이) */
enum class LineMode(val label: String, val mode: LineHeightStyle.Mode?) {
    Natural("lineHeight 없음", null),
    Fixed("Fixed(기본)", LineHeightStyle.Mode.Fixed),
    Minimum("Minimum", LineHeightStyle.Mode.Minimum),
    Tight("Tight", LineHeightStyle.Mode.Tight),
    PerLine("PerLine", LineHeightStyle.Mode.PerLine),
}

/**
 * 모드별 TextStyle. 줄 상자가 요청한 높이 그대로 보이도록 첫 줄 위 · 끝 줄 아래 다듬기는 끈다(Trim.None).
 * 정렬은 기본값(Proportional)이다.
 */
fun lineModeStyle(mode: LineMode, color: Color = Color.Black): TextStyle {
    val m = mode.mode
    return TextStyle(
        color = color,
        fontSize = LH_FONT_SP.sp,
        lineHeight = if (m == null) TextUnit.Unspecified else LH_LINE_HEIGHT_SP.sp,
        lineHeightStyle = m?.let {
            LineHeightStyle(LineHeightStyle.Alignment.Proportional, LineHeightStyle.Trim.None, it)
        }
    )
}

/** 비교 문단 — 크기가 섞인 문단 두 가지(큰 줄이 둘째 · 첫 줄)와 키 큰 문자가 섞인 문단 */
enum class LineSample(val label: String) {
    SizeSecond("큰 글씨가 둘째 줄"),
    SizeFirst("큰 글씨가 첫 줄"),
    Scripts("키 큰 문자"),
}

/**
 * 줄을 나누는 방식 — 줄마다 \n(문단이 줄마다 새로 시작) · 한 문단을 폭에 맞춰 자동 줄바꿈 ·
 * 자동 줄바꿈 + 글자마다 같은 크기 span(원인 확인용 — 줄 끝이 늘 스타일 구간 끝과 맞는다)
 */
enum class LineWrap(val label: String) {
    HardBreaks("줄마다 \\n"),
    SoftWrap("한 문단 자동 줄바꿈"),
    SoftWrapCharSpans("자동 줄바꿈 + 글자마다 span"),
}

/** 큰 글씨 구간 — 굵게 · 30sp */
private val BigSpan = SpanStyle(fontSize = LH_BIG_FONT_SP.sp, fontWeight = FontWeight.Bold)

/** {중괄호} 안을 큰 글씨로 */
private fun styled(parts: List<String>, separator: String): AnnotatedString = buildAnnotatedString {
    parts.forEachIndexed { index, part ->
        if (index > 0) append(separator)
        var rest = part
        while (rest.isNotEmpty()) {
            val open = rest.indexOf('{')
            if (open < 0) {
                append(rest)
                break
            }
            val close = rest.indexOf('}', open)
            append(rest.substring(0, open))
            withStyle(BigSpan) { append(rest.substring(open + 1, close)) }
            rest = rest.substring(close + 1)
        }
    }
}

/** 미얀마어 · 티베트어 · 크메르어 · 태국어 — 시스템 폴백 글꼴(Noto)이 위아래로 큰 문자 */
const val SAMPLE_MYANMAR = "မင်္ဂလာပါ"
const val SAMPLE_TIBETAN = "བཀྲ་ཤིས་བདེ་ལེགས།"
const val SAMPLE_KHMER = "សួស្ដី"
const val SAMPLE_THAI = "สวัสดีครับ"

fun sampleText(sample: LineSample, wrap: LineWrap): AnnotatedString = when (wrap) {
    LineWrap.HardBreaks -> when (sample) {
        LineSample.SizeSecond -> styled(listOf("오늘의 메뉴 안내", "특가 {9,900}원 오늘만", "주문은 오후 3시까지", "배송비는 무료입니다"), "\n")
        LineSample.SizeFirst -> styled(listOf("특가 {9,900}원 오늘만", "오늘의 메뉴 안내", "주문은 오후 3시까지", "배송비는 무료입니다"), "\n")
        LineSample.Scripts -> styled(
            listOf("인사말 Greetings", "미얀마어 $SAMPLE_MYANMAR", "티베트어 $SAMPLE_TIBETAN", "크메르어 $SAMPLE_KHMER", "태국어 $SAMPLE_THAI", "끝 The end"),
            "\n"
        )
    }
    // 같은 내용을 한 문단으로 — 줄은 폭이 정한다
    LineWrap.SoftWrap -> softWrapText(sample)
    LineWrap.SoftWrapCharSpans -> withCharSpans(softWrapText(sample))
}

private fun softWrapText(sample: LineSample): AnnotatedString = when (sample) {
    LineSample.SizeSecond -> styled(
        listOf("오늘의 메뉴를 안내합니다. 오늘만 특가 {9,900}원에 드리고 주문은 오후 3시까지 받습니다. 배송비는 무료이고 남은 수량은 화면 위에 표시합니다."),
        ""
    )
    LineSample.SizeFirst -> styled(
        listOf("특가 {9,900}원에 드립니다. 오늘의 메뉴를 안내하고 주문은 오후 3시까지 받습니다. 배송비는 무료이고 남은 수량은 화면 위에 표시합니다."),
        ""
    )
    LineSample.Scripts -> styled(
        listOf("인사말을 한 문단에 이어 씁니다. 티베트어 $SAMPLE_TIBETAN 다음에 미얀마어 $SAMPLE_MYANMAR 와 태국어 $SAMPLE_THAI 를 지나 줄이 바뀌어도 높이가 그대로인지 봅니다."),
        ""
    )
}

/**
 * 글자마다 그 글자와 같은 크기의 fontSize span — 크기 span 은 MetricAffectingSpan 이라 StaticLayout 의 스타일 구간이
 * 글자마다 끊긴다. 그러면 줄이 늘 구간 끝에서 끝나 다음 줄이 앞줄 메트릭을 물려받지 않는다(원인 확인용, 권장 아님).
 */
fun withCharSpans(text: AnnotatedString): AnnotatedString = buildAnnotatedString {
    append(text)
    val big = text.spanStyles.filter { it.item.fontSize == BigSpan.fontSize }
    for (i in text.indices) {
        val size = if (big.any { i >= it.start && i < it.end }) LH_BIG_FONT_SP else LH_FONT_SP
        addStyle(SpanStyle(fontSize = size.sp), i, i + 1)
    }
}

/** 한 줄의 글자 범위와 상자(레이아웃 좌표 px) */
data class LineBox(val start: Int, val end: Int, val top: Float, val bottom: Float) {
    val height: Float get() = bottom - top
}

/** 잉크가 있는 첫 행과 마지막 행 + 1(레이아웃 좌표 px) */
data class InkRows(val top: Int, val bottom: Int)

/** 한 모드의 측정 — 줄 상자 · 줄마다 잉크 범위 · 이웃 줄과 겹친 픽셀(i 와 i+1) */
class ModeMeasure(
    val mode: LineMode,
    val lines: List<LineBox>,
    val ink: List<InkRows?>,
    val collisions: List<Int>,
    val height: Float,
    /** 투명 span 을 넣은 배치의 줄 상자가 원래와 같았는가(아니면 잉크 측정을 믿을 수 없다) */
    val isolationKeptLayout: Boolean,
) {
    fun overflowTop(i: Int): Float = ink[i]?.let { max(0f, lines[i].top - it.top) } ?: 0f
    fun overflowBottom(i: Int): Float = ink[i]?.let { max(0f, it.bottom - lines[i].bottom) } ?: 0f
    fun overflow(i: Int): Float = max(overflowTop(i), overflowBottom(i))
    val maxOverflow: Float get() = lines.indices.maxOfOrNull { overflow(it) } ?: 0f
    val totalCollisions: Int get() = collisions.sum()

    /** i 줄 잉크 아래와 i+1 줄 잉크 위 사이(px) — 음수면 두 줄 글자가 같은 행에 걸친다 */
    val inkGaps: List<Float?>
        get() = (0 until lines.size - 1).map { i ->
            val a = ink[i]
            val b = ink[i + 1]
            if (a == null || b == null) null else (b.top - a.bottom).toFloat()
        }
    val minInkGap: Float? get() = inkGaps.filterNotNull().minOrNull()
}

/** 같은 문단을 모든 모드로 잰 결과 */
class SampleMeasure(
    val sample: LineSample,
    val wrap: LineWrap,
    val byMode: Map<LineMode, ModeMeasure>,
    /** 요청 줄 높이(px, 올림) — LineHeightStyleSpan 이 ceil(lineHeight) 로 쓴다 */
    val requestedPx: Int,
) {
    val natural: ModeMeasure get() = byMode.getValue(LineMode.Natural)
    val perLine: ModeMeasure get() = byMode.getValue(LineMode.PerLine)

    /** 줄바꿈 위치가 자연 배치와 같은가(줄 높이는 줄바꿈에 영향을 주지 않아야 한다) */
    val sameBreaks: Boolean
        get() = natural.lines.map { it.start to it.end } == perLine.lines.map { it.start to it.end }

    /** 문서가 말하는 PerLine — 줄마다 max(자연 높이, 요청 높이) */
    val perLineExpected: List<Float>
        get() = perLineExpectation(natural.lines.map { it.height }, requestedPx)

    /** 기대보다 높은 PerLine 줄(앞줄 높이를 물려받은 줄) */
    val inheritedLines: List<Int>
        get() = if (sameBreaks) tallerThanExpected(perLine.lines.map { it.height }, perLineExpected) else emptyList()
}

/** 줄마다 max(자연 높이, 요청 높이) */
fun perLineExpectation(naturalHeights: List<Float>, requestedPx: Int): List<Float> =
    naturalHeights.map { max(it, requestedPx.toFloat()) }

/** 기대 높이보다 tolerance 넘게 높은 줄 번호 */
fun tallerThanExpected(actual: List<Float>, expected: List<Float>, tolerancePx: Float = 1f): List<Int> =
    actual.indices.filter { it < expected.size && actual[it] > expected[it] + tolerancePx }

/** 그 줄 밖의 글자를 투명으로 — 색 span 이라 배치는 그대로여야 한다 */
fun isolateLine(text: AnnotatedString, start: Int, end: Int): AnnotatedString = buildAnnotatedString {
    append(text)
    val hidden = SpanStyle(color = Color.Transparent)
    if (start > 0) addStyle(hidden, 0, start)
    if (end < text.length) addStyle(hidden, end, text.length)
}

private fun TextLayoutResult.lineBoxes(): List<LineBox> = (0 until lineCount).map {
    LineBox(getLineStart(it), getLineEnd(it), getLineTop(it), getLineBottom(it))
}

/** 배치를 알파만 남긴 픽셀(ARGB)로 — 위아래 marginPx 여백 */
private fun renderPixels(layout: TextLayoutResult, density: Density, width: Int, height: Int, marginPx: Int): IntArray {
    val bitmap = ImageBitmap(width, height)
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), Size(width.toFloat(), height.toFloat())) {
        translate(top = marginPx.toFloat()) { drawText(layout) }
    }
    val pixels = IntArray(width * height)
    bitmap.readPixels(pixels)
    return pixels
}

/** 한 모드로 배치하고 줄마다 따로 그려 잉크 · 겹침을 잰다(메인 밖에서 불러도 된다 — TextMeasurer 는 호출마다 새로) */
fun measureMode(
    text: AnnotatedString,
    mode: LineMode,
    widthPx: Int,
    density: Density,
    resolver: FontFamily.Resolver,
): ModeMeasure {
    val measurer = TextMeasurer(resolver, density, LayoutDirection.Ltr, cacheSize = 0)
    val style = lineModeStyle(mode)
    val constraints = Constraints(maxWidth = widthPx)
    val layout = measurer.measure(text, style, constraints = constraints, density = density)
    val lines = layout.lineBoxes()
    val marginPx = with(density) { (LH_LINE_HEIGHT_SP * MARGIN_LINES).sp.toPx() }.toInt()
    val width = layout.size.width.coerceAtLeast(1)
    val height = layout.size.height + marginPx * 2
    var kept = true
    val ink = ArrayList<InkRows?>(lines.size)
    val collisions = ArrayList<Int>()
    var previous: IntArray? = null
    for (line in lines) {
        val isolated = measurer.measure(isolateLine(text, line.start, line.end), style, constraints = constraints, density = density)
        if (isolated.lineBoxes() != lines) kept = false
        val pixels = renderPixels(isolated, density, width, height, marginPx)
        ink += LineInkMath.inkRows(pixels, width, height)?.let { (top, bottom) -> InkRows(top - marginPx, bottom - marginPx) }
        previous?.let { collisions += LineInkMath.overlap(it, pixels) }
        previous = pixels
    }
    return ModeMeasure(mode, lines, ink, collisions, layout.size.height.toFloat(), kept)
}

/** 미리보기 BasicText 폭(Modifier.width)과 같은 px — 측정도 이 폭으로 줄을 나눈다 */
fun previewWidthPx(density: Density): Int = with(density) { LH_PREVIEW_WIDTH_DP.dp.roundToPx() }

/**
 * 같은 문단을 span 없음 · 글자마다 span 으로 PerLine 배치한 시간의 중앙값(µs).
 * 번갈아 재서 JIT 예열 · 클럭 변화가 한쪽에만 쏠리지 않게 한다.
 */
fun medianLayoutMicros(
    plain: AnnotatedString,
    charSpans: AnnotatedString,
    widthPx: Int,
    density: Density,
    resolver: FontFamily.Resolver,
    runs: Int = 80,
): Pair<Long, Long> {
    val measurer = TextMeasurer(resolver, density, LayoutDirection.Ltr, cacheSize = 0)
    val style = lineModeStyle(LineMode.PerLine)
    val constraints = Constraints(maxWidth = widthPx)
    val texts = listOf(plain, charSpans)
    repeat(runs / 2) { texts.forEach { measurer.measure(it, style, constraints = constraints, density = density) } }
    val micros = Array(2) { LongArray(runs) }
    for (i in 0 until runs) {
        for (k in texts.indices) {
            val start = System.nanoTime()
            measurer.measure(texts[k], style, constraints = constraints, density = density)
            micros[k][i] = (System.nanoTime() - start) / 1_000
        }
    }
    micros.forEach { it.sort() }
    return micros[0][runs / 2] to micros[1][runs / 2]
}

fun measureSample(
    sample: LineSample,
    wrap: LineWrap,
    widthPx: Int,
    density: Density,
    resolver: FontFamily.Resolver,
): SampleMeasure {
    val text = sampleText(sample, wrap)
    val byMode = LineMode.entries.associateWith { measureMode(text, it, widthPx, density, resolver) }
    val requested = ceil(with(density) { LH_LINE_HEIGHT_SP.sp.toPx() }).toInt()
    return SampleMeasure(sample, wrap, byMode, requested)
}

/**
 * 픽셀 루프 — 줄 하나당 수십만 픽셀을 훑는다. HotSwan 재작성에서 뺀다(build.gradle) — 반복문은 이 object 안에만 둔다.
 */
object LineInkMath {
    /** 알파가 이 값 이상이면 잉크(안티에일리어싱 옅은 가장자리는 뺀다) */
    const val INK_ALPHA = 32

    /** 겹침은 두 줄 모두 이 알파 이상인 픽셀 */
    const val OVERLAP_ALPHA = 96

    /** 잉크가 있는 첫 행 · 마지막 행 + 1 (없으면 null) */
    fun inkRows(pixels: IntArray, width: Int, height: Int, threshold: Int = INK_ALPHA): Pair<Int, Int>? {
        var top = -1
        var bottom = -1
        for (y in 0 until height) {
            val row = y * width
            for (x in 0 until width) {
                if ((pixels[row + x] ushr 24) >= threshold) {
                    if (top < 0) top = y
                    bottom = y
                    break
                }
            }
        }
        return if (top < 0) null else top to bottom + 1
    }

    /** 두 그림이 같은 자리에 잉크를 둔 픽셀 수 */
    fun overlap(a: IntArray, b: IntArray, threshold: Int = OVERLAP_ALPHA): Int {
        var count = 0
        val n = minOf(a.size, b.size)
        for (i in 0 until n) {
            if ((a[i] ushr 24) >= threshold && (b[i] ushr 24) >= threshold) count++
        }
        return count
    }
}

/** 화면 루트에 두는 결과 보관 — Lazy 카드가 스크롤로 빠졌다 돌아와도 다시 재지 않는다 */
@Stable
class LineHeightLabState {
    val results = mutableStateMapOf<Pair<LineSample, LineWrap>, SampleMeasure>()

    /** 폭 · 밀도(글자 크기 배율 포함)가 바뀌면 다시 잰다 */
    private var measuredFor: Triple<Int, Float, Float>? = null

    suspend fun ensure(sample: LineSample, wrap: LineWrap, widthPx: Int, density: Density, resolver: FontFamily.Resolver) {
        val key = Triple(widthPx, density.density, density.fontScale)
        if (measuredFor != key) {
            results.clear()
            timings.clear()
            measuredFor = key
        }
        if (results.containsKey(sample to wrap)) return
        val measured = withContext(Dispatchers.Default) { measureSample(sample, wrap, widthPx, density, resolver) }
        results[sample to wrap] = measured
    }

    /** 자동 줄바꿈 두 가지(span 없음 · 글자마다)의 배치 시간 */
    val timings = mutableStateMapOf<Pair<LineSample, LineWrap>, Long>()

    suspend fun ensureTiming(sample: LineSample, widthPx: Int, density: Density, resolver: FontFamily.Resolver) {
        if (timings.containsKey(sample to LineWrap.SoftWrap)) return
        val (plain, chars) = withContext(Dispatchers.Default) {
            medianLayoutMicros(sampleText(sample, LineWrap.SoftWrap), sampleText(sample, LineWrap.SoftWrapCharSpans), widthPx, density, resolver)
        }
        timings[sample to LineWrap.SoftWrap] = plain
        timings[sample to LineWrap.SoftWrapCharSpans] = chars
    }
}

/** 소수 첫째 자리(로캘 무관 · 반올림) */
fun oneDecimal(value: Double): String {
    val tenths = (value * 10).roundToLong()
    val sign = if (tenths < 0) "-" else ""
    val abs = kotlin.math.abs(tenths)
    return "$sign${abs / 10}.${abs % 10}"
}

/** px → dp 소수 첫째 자리 */
fun pxToDpText(px: Float, density: Float): String = oneDecimal((px / density).toDouble())

/** b 대비 a 의 배수(소수 첫째 자리), b 가 0 이면 null */
fun ratioText(a: Long, b: Long): String? = if (b <= 0) null else oneDecimal(a.toDouble() / b)
