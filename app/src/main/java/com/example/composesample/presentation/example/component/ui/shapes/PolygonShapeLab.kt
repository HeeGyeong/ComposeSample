package com.example.composesample.presentation.example.component.ui.shapes

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CornerRounding
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.MorphPolygonShape
import androidx.compose.foundation.shape.PolygonShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.toPolygonShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

// ==================== 공통: 외곽선 계산을 세는 껍데기 ====================

/** 외곽선을 만든 횟수와 걸린 시간 — 컴포지션 상태가 아니다(세는 것이 다시 그리기를 부르면 안 된다) */
class OutlineCounter {
    var calls = 0
        private set
    var nanos = 0L
        private set

    /** 마지막으로 외곽선을 만들 때 기록한 값(transform 카드가 '그때의 각도'를 적는다) */
    var lastTag = 0f
        private set

    fun record(elapsed: Long, tag: Float) {
        calls++
        nanos += elapsed
        lastTag = tag
    }

    fun reset() {
        calls = 0
        nanos = 0L
        lastTag = 0f
    }
}

/**
 * 감싼 도형이 외곽선을 만들 때마다 센다. 도형 안의 상태 읽기는 그대로 delegate 가 하므로
 * clip · background · border 의 읽기 관찰도 그대로다. [tag] 는 외곽선을 만든 그 순간의 값을 적는 데 쓴다.
 */
class CountingShape(
    private val delegate: Shape,
    private val counter: OutlineCounter,
    private val tag: () -> Float = { 0f }
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val start = System.nanoTime()
        val outline = delegate.createOutline(size, layoutDirection, density)
        counter.record(System.nanoTime() - start, tag())
        return outline
    }
}

// ==================== 1. 갤러리 ====================

/** CornerRounding.fraction 이 무엇의 몇 배인지 — PolygonShape.kt 의 toRoundedPolygonRounding 에 넘기는 기준 */
enum class FractionBase(val label: String) {
    Radius("반지름 = 짧은 변 ÷ 2"),
    PillStarRadius("짧은 변 ÷ 2 (정사각형 칸 기준)"),
    MinSide("짧은 변 전체"),
    VertexBounds("꼭짓점 범위의 짧은 변"),
    NoRounding("둥글기 인자 없음")
}

enum class GalleryKind(val label: String, val usesVertices: Boolean, val fractionBase: FractionBase) {
    Regular("정다각형", true, FractionBase.Radius),
    Star("별", true, FractionBase.Radius),
    PillStar("필스타", true, FractionBase.PillStarRadius),
    Rectangle("사각형", false, FractionBase.MinSide),
    Custom("꼭짓점 직접", false, FractionBase.VertexBounds),
    Pill("필", false, FractionBase.NoRounding),
    Circle("원(근사)", true, FractionBase.NoRounding)
}

enum class RoundingChoice(val label: String) {
    None("없음"),
    Dp("dp(8.dp)"),
    Fraction("fraction(0.25f)")
}

const val ROUNDING_DP = 8f
const val ROUNDING_FRACTION = 0.25f
val vertexChoices = listOf(3, 5, 6, 8, 12)
val smoothingChoices = listOf(0f, 0.6f, 1f)
val gallerySizesDp = listOf(56f, 128f)

fun fractionBaseDp(base: FractionBase, widthDp: Float, heightDp: Float): Float {
    val shorter = min(widthDp, heightDp)
    return when (base) {
        FractionBase.Radius -> shorter / 2f
        // PillStarPolygonShape: 짧은 변 ÷ 2 만큼 안쪽으로 줄인 폭 · 높이 중 짧은 쪽
        FractionBase.PillStarRadius -> min(widthDp - shorter / 2f, heightDp - shorter / 2f)
        FractionBase.MinSide -> shorter
        // 이 예제의 '꼭짓점 직접' 도형은 칸 전체에 걸친다
        FractionBase.VertexBounds -> shorter
        FractionBase.NoRounding -> 0f
    }
}

/**
 * 요청한 모서리 반경(dp). dp 는 칸 크기와 무관하고 fraction 은 [FractionBase] 의 배수다.
 * 변 길이에 안 들어가는 반경은 graphics-shapes 가 줄이므로 '요청값'이다. 둥글기 인자가 없는 도형은 null.
 */
fun requestedCornerRadiusDp(kind: GalleryKind, rounding: RoundingChoice, widthDp: Float, heightDp: Float): Float? {
    if (kind.fractionBase == FractionBase.NoRounding) return null
    return when (rounding) {
        RoundingChoice.None -> 0f
        RoundingChoice.Dp -> ROUNDING_DP
        RoundingChoice.Fraction -> ROUNDING_FRACTION * fractionBaseDp(kind.fractionBase, widthDp, heightDp)
    }
}

fun RoundingChoice.toCornerRounding(smoothing: Float): CornerRounding = when (this) {
    RoundingChoice.None -> CornerRounding.Unrounded
    RoundingChoice.Dp -> CornerRounding.dp(ROUNDING_DP.dp, smoothing)
    RoundingChoice.Fraction -> CornerRounding.fraction(ROUNDING_FRACTION, smoothing)
}

/** 갤러리 도형. 호출하는 쪽이 remember 로 감싼다(PolygonShape 팩토리는 상태를 가진 새 인스턴스를 만든다) */
fun galleryShape(kind: GalleryKind, vertices: Int, rounding: RoundingChoice, smoothing: Float): PolygonShape {
    val corner = rounding.toCornerRounding(smoothing)
    return when (kind) {
        GalleryKind.Regular -> PolygonShape.regularPolygon(vertices, corner)
        GalleryKind.Star -> PolygonShape.star(vertices, innerRadiusRatio = 0.5f, outerRounding = corner)
        GalleryKind.PillStar -> PolygonShape.pillStar(vertices, innerRadiusRatio = 0.6f, outerRounding = corner)
        GalleryKind.Rectangle -> PolygonShape.rectangle(corner)
        GalleryKind.Custom -> speechBubble(corner)
        GalleryKind.Pill -> PolygonShape.pill(smoothing)
        GalleryKind.Circle -> PolygonShape.circle(vertices)
    }
}

/** 꼭짓점을 직접 준 말풍선 — 좌표는 레이아웃 픽셀(PolygonShapeScope.size 기준) */
fun speechBubble(corner: CornerRounding): PolygonShape = PolygonShape {
    val w = size.width
    val h = size.height
    polygon(
        vertices = listOf(
            Offset(0f, 0f), Offset(w, 0f), Offset(w, h * 0.75f), Offset(w * 0.45f, h * 0.75f),
            Offset(w * 0.2f, h), Offset(w * 0.25f, h * 0.75f), Offset(0f, h * 0.75f)
        ),
        rounding = corner
    )
}

// ==================== 2. 모핑 ====================

/** 하나만 다른 세 방식 — ③ 이 기준이고 ① 은 '도형을 매 프레임 새로', ② 는 '크기를 레이아웃에서' 만 다르다 */
enum class MorphVariant(val label: String, val shortLabel: String, val detail: String) {
    ValueNew(
        "① 값으로 · 매 프레임 새 도형",
        "① 값 · 새 도형",
        "progress 를 컴포지션에서 읽고 MorphPolygonShape(start, end) { p } 를 매번 만든다. 크기는 graphicsLayer scale"
    ),
    LambdaLayout(
        "② 람다 · 크기는 레이아웃",
        "② 람다 · 레이아웃",
        "remember 한 MorphPolygonShape(start, end) { progress.value }. 크기는 Modifier.layout 에서 키운다"
    ),
    LambdaScale(
        "③ 람다 · 크기는 graphicsLayer",
        "③ 람다 · scale",
        "remember 한 MorphPolygonShape(start, end) { progress.value }. 칸은 큰 크기로 두고 graphicsLayer scale 로 키운다"
    )
}

const val MORPH_MILLIS = 700
const val MORPH_SMALL_DP = 64f
const val MORPH_LARGE_DP = 128f
const val MORPH_ROUNDS = 2

/** 원문 사례의 '일그러진 아바타' — 꼭짓점 9개 쿠키 */
fun avatarStartShape(): PolygonShape =
    PolygonShape.star(9, innerRadiusRatio = 0.8f, outerRounding = CornerRounding.fraction(0.4f))

fun avatarEndShape(): PolygonShape = PolygonShape.circle()

/** ① 이 매 프레임 만드는 진행률 람다 — 화면 파일이 아니라 여기서 만든다(HotSwan 제외 대상, build.gradle 주석) */
fun constantProgress(p: Float): () -> Float = { p }

data class MorphRun(
    val variant: MorphVariant,
    val rounds: Int,
    val frames: Int,
    val recompositions: Int,
    val outlines: Int,
    val outlineNanos: Long
) {
    val microsPerOutline: Double get() = if (outlines == 0) 0.0 else outlineNanos / 1000.0 / outlines

    /** 한 프레임이 외곽선에 쓴 시간 — ② 처럼 프레임당 외곽선이 둘이면 1회 시간보다 이것이 비용이다 */
    val microsPerFrame: Double get() = if (frames == 0) 0.0 else outlineNanos / 1000.0 / frames

    /** ① 은 프레임마다 리컴포즈해야 하고 ②·③ 은 0 이어야 한다. 셋 다 프레임마다 외곽선을 다시 만든다 */
    val matchesExpectation: Boolean
        get() {
            val recomposeOk = when (variant) {
                MorphVariant.ValueNew -> recompositions * 2 >= frames
                MorphVariant.LambdaLayout, MorphVariant.LambdaScale -> recompositions == 0
            }
            return recomposeOk && outlines * 2 >= frames
        }
}

/** 리컴포지션 횟수 — 상태가 아닌 평범한 필드(SideEffect 가 올린다) */
class RecomposeCounter {
    var count = 0
}

class MorphLabState {
    val progress = Animatable(0f)

    /** remember 한 MorphPolygonShape 에 넘기는 진행률 람다 — 외곽선을 만들 때 불려 그 자리에서 읽는다 */
    val progressReader: () -> Float = { progress.value }
    var variant by mutableStateOf(MorphVariant.LambdaScale)
    var running by mutableStateOf(false)
        private set
    val counter = OutlineCounter()
    val recompositions = RecomposeCounter()
    val results = mutableStateMapOf<MorphVariant, MorphRun>()

    /** 지금 방식으로 한 번 오간다(작은 쿠키 ↔ 큰 원) */
    suspend fun playOnce() {
        if (running) return
        running = true
        try {
            animateTo(if (progress.value < 0.5f) 1f else 0f)
        } finally {
            running = false
        }
    }

    /** 세 방식을 차례로 [MORPH_ROUNDS] 번씩 오가며 잰다 */
    suspend fun measureAll(select: (MorphVariant) -> Unit = { variant = it }) {
        if (running) return
        running = true
        try {
            results.clear()
            for (v in MorphVariant.entries) {
                select(v)
                progress.snapTo(0f)
                settle()
                counter.reset()
                recompositions.count = 0
                var frames = 0
                repeat(MORPH_ROUNDS) { round ->
                    frames += animateTo(if (round % 2 == 0) 1f else 0f)
                }
                settle()
                results[v] = MorphRun(v, MORPH_ROUNDS, frames, recompositions.count, counter.calls, counter.nanos)
            }
        } finally {
            running = false
        }
    }

    private suspend fun animateTo(target: Float): Int {
        var frames = 0
        progress.animateTo(target, tween(MORPH_MILLIS, easing = FastOutSlowInEasing)) { frames++ }
        return frames
    }

    /** 방식을 바꾼 컴포지션 · 첫 외곽선이 끝날 때까지 두 프레임 기다린다 */
    private suspend fun settle() {
        repeat(2) { withFrameNanos { } }
    }
}

// ==================== 3. transform · copy · 공유 ====================

const val TRANSFORM_STEP_DEGREES = 36f

/**
 * 같은 별 하나(shared)를 두 칸이 나눠 쓰고, 셋째 칸은 transform 전에 copy() 해 둔 것을 쓴다.
 * 넷째 칸은 copy() 에 상태를 읽는 transform 을 한 번 걸어 둔 것.
 */
class TransformLabState {
    /** 컴포지션 밖(클릭)에서 제자리로 바꿀 공유 인스턴스 */
    val shared: PolygonShape = PolygonShape.star(5, innerRadiusRatio = 0.5f, outerRounding = CornerRounding.dp(2.dp))

    /** transform 전에 만든 복사본 — 이후 shared 의 transform 을 따라가지 않는다 */
    val copied: PolygonShape = shared.copy()

    /** 상태로 돌리는 칸 — transform 블록은 외곽선을 만들 때 실행되어 angle 을 그 자리에서 읽는다 */
    val angle = Animatable(0f)
    val stateShape: PolygonShape = shared.copy().apply { transform { rotate(angle.value) } }

    /**
     * 공유 인스턴스에 지금까지 제자리로 건 회전. 상태가 아닌 평범한 필드다 —
     * 칸의 CountingShape 가 외곽선을 만들 때 이 값을 적는데, 상태였다면 그 읽기가 관찰되어 값을 바꾸는 순간 다시 그려 버린다.
     */
    var sharedDegrees = 0f
        private set
    var remountKey by mutableIntStateOf(0)
        private set

    val stateCounter = OutlineCounter()
    val sharedACounter = OutlineCounter()
    val sharedBCounter = OutlineCounter()
    val copiedCounter = OutlineCounter()
    val stateRecompositions = RecomposeCounter()

    var lastReport by mutableStateOf<TransformReport?>(null)
        private set

    suspend fun rotateWithState() {
        val before = stateRecompositions.count
        val calls = stateCounter.calls
        angle.animateTo(angle.value + TRANSFORM_STEP_DEGREES, tween(400))
        repeat(2) { withFrameNanos { } }
        lastReport = TransformReport.StateRotation(
            degrees = angle.value,
            recompositions = stateRecompositions.count - before,
            outlines = stateCounter.calls - calls
        )
    }

    /** 클릭 핸들러에서 공유 인스턴스를 제자리로 돌린다 — 상태 쓰기가 아니라서 아무것도 다시 그리지 않는다 */
    suspend fun transformSharedInPlace() {
        val before = listOf(sharedACounter.calls, sharedBCounter.calls, copiedCounter.calls)
        val target = sharedDegrees + TRANSFORM_STEP_DEGREES
        shared.transform { rotate(target) }
        sharedDegrees = target
        // 다음 프레임 두 개가 지나도 외곽선을 다시 만들었는지 본다
        repeat(2) { withFrameNanos { } }
        val after = listOf(sharedACounter.calls, sharedBCounter.calls, copiedCounter.calls)
        lastReport = TransformReport.InPlace(
            sharedDegrees = target,
            rebuilt = after.zip(before) { a, b -> a - b },
            drawnDegrees = listOf(sharedACounter.lastTag, sharedBCounter.lastTag, copiedCounter.lastTag)
        )
    }

    /** key 를 바꿔 칸을 새로 붙인다 — 새 노드는 외곽선을 처음부터 만든다 */
    suspend fun remount() {
        remountKey++
        repeat(2) { withFrameNanos { } }
        lastReport = TransformReport.Remounted(
            sharedDegrees = sharedDegrees,
            drawnDegrees = listOf(sharedACounter.lastTag, sharedBCounter.lastTag, copiedCounter.lastTag)
        )
    }
}

sealed interface TransformReport {
    data class StateRotation(val degrees: Float, val recompositions: Int, val outlines: Int) : TransformReport
    data class InPlace(val sharedDegrees: Float, val rebuilt: List<Int>, val drawnDegrees: List<Float>) : TransformReport
    data class Remounted(val sharedDegrees: Float, val drawnDegrees: List<Float>) : TransformReport
}

// ---- 캐시는 마지막 크기 하나 ----

enum class CacheScenario(val label: String) {
    SharedTwoSizes("인스턴스 하나 · 크기 둘"),
    CopyPerSize("크기마다 copy()"),
    SharedTwoSizesRotating("하나 · 크기 둘 · 매 프레임 회전"),
    CopyPerSizeRotating("크기마다 copy() · 매 프레임 회전")
}

const val CACHE_FRAMES = 1000
const val CACHE_WARMUP_FRAMES = 1000
val cacheSmall = Size(96f, 96f)
val cacheLarge = Size(320f, 320f)

data class CacheResult(val scenario: CacheScenario, val microsPerFrame: Double)

/** 한 '프레임'에 작은 칸 · 큰 칸이 외곽선을 하나씩 만든다 — 실제 clip · background 가 부르는 그 함수를 그대로 부른다 */
fun cacheFrameRunner(scenario: CacheScenario, density: Density): (Int) -> Unit {
    val base = avatarStartShape()
    val rotating = scenario == CacheScenario.SharedTwoSizesRotating || scenario == CacheScenario.CopyPerSizeRotating
    var degrees = 0f
    val small: PolygonShape
    val large: PolygonShape
    when (scenario) {
        CacheScenario.SharedTwoSizes, CacheScenario.SharedTwoSizesRotating -> {
            small = base
            large = base
        }
        CacheScenario.CopyPerSize, CacheScenario.CopyPerSizeRotating -> {
            small = base.copy()
            large = base.copy()
        }
    }
    if (rotating) {
        small.transform { rotate(degrees) }
        if (large !== small) large.transform { rotate(degrees) }
    }
    return { frame ->
        if (rotating) degrees = frame * 3f
        small.createOutline(cacheSmall, LayoutDirection.Ltr, density)
        large.createOutline(cacheLarge, LayoutDirection.Ltr, density)
    }
}

/**
 * 모든 방식을 먼저 [CACHE_WARMUP_FRAMES] 프레임 데운다 — 처음 몇천 번은 JIT 전이라 재는 순서에 따라 값이 달라진다.
 * 재는 반복문은 일반 함수에 둔다: suspend 람다(withContext 블록)는 별도 클래스로 컴파일되어 HotSwan 제외(파일 facade)에 안 든다.
 */
fun measureCacheScenarios(density: Density): List<CacheResult> {
    val runners = CacheScenario.entries.associateWith { cacheFrameRunner(it, density) }
    runners.values.forEach { frame -> repeat(CACHE_WARMUP_FRAMES) { frame(it) } }
    return runners.map { (scenario, frame) ->
        val start = System.nanoTime()
        repeat(CACHE_FRAMES) { frame(CACHE_WARMUP_FRAMES + it) }
        CacheResult(scenario, (System.nanoTime() - start) / 1000.0 / CACHE_FRAMES)
    }
}

suspend fun runCacheBench(density: Density): List<CacheResult> =
    withContext(Dispatchers.Default) { measureCacheScenarios(density) }

class CacheBenchState {
    var running by mutableStateOf(false)
        private set
    var results by mutableStateOf<List<CacheResult>>(emptyList())
        private set

    suspend fun run(density: Density) {
        if (running) return
        running = true
        try {
            results = runCacheBench(density)
        } finally {
            running = false
        }
    }
}

// ==================== 4. 크기 0 ====================

/**
 * 폭이나 높이가 0 이면 빈 사각형 외곽선을 돌려주고, 아니면 감싼 도형에 맡긴다.
 * PolygonShape 계열은 크기 0 에서 IllegalArgumentException 을 던진다(4번 카드 실측).
 */
class ZeroSafeShape(val delegate: Shape) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        if (size.width <= 0f || size.height <= 0f) Outline.Rectangle(Rect(Offset.Zero, size))
        else delegate.createOutline(size, layoutDirection, density)

    // 매 리컴포지션 새로 감싸도 같은 도형이면 같게 본다(Background 등은 shape 가 같으면 캐시한 외곽선을 쓴다)
    override fun equals(other: Any?): Boolean = other is ZeroSafeShape && other.delegate == delegate
    override fun hashCode(): Int = delegate.hashCode()
}

enum class ZeroCase(val label: String, val polygon: Boolean) {
    RoundedCorner("RoundedCornerShape(24.dp)", false),
    RoundedToPolygon("RoundedCornerShape(24.dp).toPolygonShape()", true),
    CutToPolygon("CutCornerShape(16.dp).toPolygonShape()", true),
    Star("PolygonShape.star(5)", true),
    Regular("PolygonShape.regularPolygon(6)", true),
    Circle("PolygonShape.circle()", true),
    Pill("PolygonShape.pill()", true),
    Rectangle("PolygonShape.rectangle(dp(8.dp))", true),
    Builder("PolygonShape { polygon(세 꼭짓점) }", true),
    Morph("MorphPolygonShape(별, 원)", true),
    SafeStar("ZeroSafeShape(PolygonShape.star(5))", false);

    fun make(): Shape = when (this) {
        RoundedCorner -> RoundedCornerShape(24.dp)
        RoundedToPolygon -> RoundedCornerShape(24.dp).toPolygonShape()
        CutToPolygon -> CutCornerShape(16.dp).toPolygonShape()
        Star -> PolygonShape.star(5)
        Regular -> PolygonShape.regularPolygon(6)
        Circle -> PolygonShape.circle()
        Pill -> PolygonShape.pill()
        Rectangle -> PolygonShape.rectangle(CornerRounding.dp(8.dp))
        Builder -> PolygonShape {
            polygon(listOf(Offset(0f, 0f), Offset(size.width, 0f), Offset(size.width / 2f, size.height)))
        }
        Morph -> MorphPolygonShape(PolygonShape.star(5), PolygonShape.circle()) { 0.5f }
        SafeStar -> ZeroSafeShape(PolygonShape.star(5))
    }
}

/** 0 × 0 · 폭만 있고 높이 0 · 높이 1px */
val zeroSizes = listOf(Size(0f, 0f), Size(240f, 0f), Size(240f, 1f))

fun sizeLabel(size: Size): String = "${size.width.toInt()}×${size.height.toInt()}"

data class ZeroCell(val error: Throwable?) {
    val ok: Boolean get() = error == null
    val shortError: String get() = error?.let { "${it.javaClass.simpleName}: ${it.message}" } ?: "그림"
}

fun probeZero(case: ZeroCase, size: Size, density: Density): ZeroCell =
    ZeroCell(runCatching { case.make().createOutline(size, LayoutDirection.Ltr, density) }.exceptionOrNull())

class ZeroLabState {
    var cells by mutableStateOf<Map<Pair<ZeroCase, Size>, ZeroCell>>(emptyMap())
        private set

    fun run(density: Density) {
        cells = buildMap {
            for (case in ZeroCase.entries) for (size in zeroSizes) put(case to size, probeZero(case, size, density))
        }
    }
}

// ==================== 5. 변환 · 테두리 ====================

data class PixelDiff(val total: Int, val differing: Int, val maxChannel: Int)

/** ARGB 픽셀 배열 두 개의 차이 — 채널 차가 1 이상인 픽셀 수와 가장 큰 채널 차(0~255) */
fun pixelDiff(a: IntArray, b: IntArray): PixelDiff {
    require(a.size == b.size) { "크기가 다르다: ${a.size} vs ${b.size}" }
    var differing = 0
    var maxChannel = 0
    for (i in a.indices) {
        val p = a[i]
        val q = b[i]
        if (p == q) continue
        var d = 0
        for (shift in intArrayOf(24, 16, 8, 0)) {
            d = max(d, abs(((p ushr shift) and 0xFF) - ((q ushr shift) and 0xFF)))
        }
        if (d > 0) differing++
        maxChannel = max(maxChannel, d)
    }
    return PixelDiff(a.size, differing, maxChannel)
}

enum class ConversionCase(val label: String) {
    Round8("RoundedCornerShape(8.dp)"),
    Round24("RoundedCornerShape(24.dp)"),
    Round50("RoundedCornerShape(50)"),
    Mixed("RoundedCornerShape(40 · 4 · 24 · 0 dp)"),
    Cut16("CutCornerShape(16.dp)");

    fun original(): Shape = when (this) {
        Round8 -> RoundedCornerShape(8.dp)
        Round24 -> RoundedCornerShape(24.dp)
        Round50 -> RoundedCornerShape(50)
        Mixed -> RoundedCornerShape(topStart = 40.dp, topEnd = 4.dp, bottomEnd = 24.dp, bottomStart = 0.dp)
        Cut16 -> CutCornerShape(16.dp)
    }

    fun converted(): PolygonShape = when (this) {
        Round8 -> RoundedCornerShape(8.dp).toPolygonShape()
        Round24 -> RoundedCornerShape(24.dp).toPolygonShape()
        Round50 -> RoundedCornerShape(50).toPolygonShape()
        Mixed -> RoundedCornerShape(topStart = 40.dp, topEnd = 4.dp, bottomEnd = 24.dp, bottomStart = 0.dp).toPolygonShape()
        Cut16 -> CutCornerShape(16.dp).toPolygonShape()
    }
}

const val CONVERSION_PX = 240

data class ConversionResult(val case: ConversionCase, val diff: PixelDiff)

/** 흰 바탕에 외곽선을 검게 칠한 픽셀 — 소프트웨어 캔버스(ImageBitmap) 라 화면(하드웨어) 과 안티에일리어싱이 조금 다를 수 있다 */
fun renderOutlinePixels(shape: Shape, sizePx: Int, density: Density): IntArray {
    val bitmap = ImageBitmap(sizePx, sizePx)
    val drawSize = Size(sizePx.toFloat(), sizePx.toFloat())
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), drawSize) {
        drawRect(Color.White)
        drawOutline(shape.createOutline(size, layoutDirection, this), Color.Black)
    }
    val pixels = IntArray(sizePx * sizePx)
    bitmap.readPixels(pixels)
    return pixels
}

class ConversionLabState {
    var results by mutableStateOf<List<ConversionResult>>(emptyList())
        private set

    fun run(density: Density) {
        results = ConversionCase.entries.map { case ->
            val a = renderOutlinePixels(case.original(), CONVERSION_PX, density)
            val b = renderOutlinePixels(case.converted(), CONVERSION_PX, density)
            ConversionResult(case, pixelDiff(a, b))
        }
    }
}

/** RuntimeShader 테두리 — 칸 가운데를 도는 무지개 */
const val HOLO_AGSL = """
uniform float2 size;
half4 main(float2 p) {
    float2 c = p / size - 0.5;
    float a = atan(c.y, c.x) / 6.2831853 + 0.5;
    half3 col = half3(0.5 + 0.5 * cos(6.2831853 * (a + float3(0.0, 0.33, 0.67))));
    return half4(col, 1.0);
}
"""

// ==================== 포맷 ====================

/** 소수 [digits] 자리 반올림 — String.format 의 기기 로캘(쉼표 소수점)을 피한다 */
fun fixed(value: Double, digits: Int): String {
    var scale = 1L
    repeat(digits) { scale *= 10 }
    // roundToLong 은 0.5 를 올린다(kotlin.math.round 는 짝수 쪽 — 1.25 → 1.2)
    val scaled = (abs(value) * scale).roundToLong()
    val sign = if (value < 0 && scaled != 0L) "-" else ""
    if (digits == 0) return "$sign$scaled"
    return "$sign${scaled / scale}.${(scaled % scale).toString().padStart(digits, '0')}"
}

fun formatMicros(value: Double): String = when {
    value >= 100 -> "${fixed(value, 0)} µs"
    value >= 10 -> "${fixed(value, 1)} µs"
    else -> "${fixed(value, 2)} µs"
}

fun formatDp(value: Float): String =
    if (value == value.toInt().toFloat()) "${value.toInt()}dp" else "${fixed(value.toDouble(), 1)}dp"

fun ratioLabel(slow: Double, fast: Double): String = if (fast <= 0.0) "-" else "${fixed(slow / fast, 0)}배"
